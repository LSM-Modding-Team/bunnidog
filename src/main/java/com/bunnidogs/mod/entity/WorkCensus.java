package com.bunnidogs.mod.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Scavenging;
import com.bunnidogs.mod.genetics.Supplying;
import com.bunnidogs.mod.genetics.TroughWork;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A bounded read-only eligibility report; never search targets, create paths, claim homes or assign tags. */
public final class WorkCensus {
    public static final int RANGE = 8, MAX_ROWS = 8;
    public record Worker(Bunnidog dog, boolean ready, Component reason) {}
    public record Report(TroughWork mode, boolean enabled, boolean paused, ItemStack food, ItemStack filter,
                         int total, int ready, List<Worker> workers) {}
    private WorkCensus() {}
    private static Worker reason(Bunnidog dog, String key, Object... args) {
        return new Worker(dog,false,Component.translatable("message.bunnidogs.work."+key,args));
    }
    private static Worker readiness(Bunnidog dog, BlockPos pos, FeedingTroughBlockEntity stock, boolean enabled, boolean paused) {
        if (!enabled) return reason(dog,"disabled");
        if (paused) return reason(dog,"paused");
        var status = dog.gardeningStatus();
        if (status != Gardening.Status.READY) return new Worker(dog,false,Component.translatable("message.bunnidogs.gardening."+status.name().toLowerCase(Locale.ROOT)));
        if (!dog.foodWithinHome(pos)) return reason(dog,"outside_home");
        var food = stock.food(); var template = food.isEmpty() ? stock.filter() : food;
        if (!template.isEmpty() && !dog.canEat(template)) return reason(dog,"diet");
        var mode = stock.workMode();
        if (mode == TroughWork.NURSERY) {
            if (!Gardening.nurses(dog.stats())) return reason(dog,"bloom",Gardening.MIN_NURSERY_BLOOM);
            if (!food.is(ModItems.GUAVA.get()) || food.getCount() <= Gardening.COMPOST_RESERVE) return reason(dog,"nursery_food",Gardening.COMPOST_RESERVE);
        } else if (mode == TroughWork.SUPPLY) {
            if (!Supplying.qualified(dog.stats())) return reason(dog,"senses",Supplying.MIN_SENSES);
            if (GuavaItem.tierOf(template) > 0) return reason(dog,"ordinary_food");
            if (food.getCount() <= Supplying.RESERVE) return reason(dog,"supply_food",Supplying.RESERVE);
        } else if (mode == TroughWork.GATHER) {
            if (!Scavenging.gathers(dog.stats())) return reason(dog,"senses",Scavenging.MIN_GATHERING_SENSES);
            if (GuavaItem.tierOf(template) > 0) return reason(dog,"ordinary_food");
            if (!food.isEmpty() && !stock.canInsert(food.copyWithCount(1))) return reason(dog,"full");
        } else if (mode == TroughWork.COMPOST) {
            if (!Gardening.cultivates(dog.stats())) return reason(dog,"bloom",Gardening.MIN_CULTIVATION_BLOOM);
            if (food.getCount() <= Gardening.COMPOST_RESERVE || ComposterBlock.getValue(food) <= 0) return reason(dog,"compost_food",Gardening.COMPOST_RESERVE);
        } else if (!food.isEmpty() && !stock.canInsert(food.copyWithCount(1))) {
            boolean spending = Gardening.cultivates(dog.stats()) && food.getCount() > Gardening.COMPOST_RESERVE
                    && (mode == TroughWork.GARDEN ? ResidentPlanting.supports(food) : ComposterBlock.getValue(food) > 0);
            if (!spending) return reason(dog,"full");
        }
        return new Worker(dog,true,Component.translatable("message.bunnidogs.work.ready"));
    }
    public static Optional<Report> inspect(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity stock)) return Optional.empty();
        boolean enabled = stock.getBlockState().getValue(FeedingTroughBlock.TENDING);
        // Native conductor power checks may read two blocks away. Unknown boundary power is reported as paused.
        boolean paused = !level.hasChunksAt(pos.offset(-2,-2,-2),pos.offset(2,2,2)) || stock.residentsPaused();
        var workers = level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(RANGE),
                dog -> dog.isAlive() && dog.acceptsCare() && dog.distanceToSqr(Vec3.atCenterOf(pos)) <= RANGE*RANGE
                        && level.getWorldBorder().isWithinBounds(dog.blockPosition())).stream()
                .map(dog -> readiness(dog,pos,stock,enabled,paused))
                .sorted(Comparator.comparing((Worker row) -> !row.ready()).thenComparing(row -> row.dog().getUUID())).toList();
        return Optional.of(new Report(stock.workMode(),enabled,paused,stock.food().copy(),stock.filter(),workers.size(),
                (int)workers.stream().filter(Worker::ready).count(),workers.subList(0,Math.min(MAX_ROWS,workers.size()))));
    }
    public static void show(ServerLevel level, BlockPos pos, Player player) {
        var result = inspect(level,pos); if (result.isEmpty()) return;
        var report = result.get();
        player.sendSystemMessage(Component.translatable("message.bunnidogs.work.header",Component.translatable("work.bunnidogs.mode."+report.mode().key()),
                Component.translatable(!report.enabled() ? "work.bunnidogs.disabled" : report.paused() ? "message.bunnidogs.work.paused" : "work.bunnidogs.enabled"),
                report.ready(),report.total(),RANGE).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("message.bunnidogs.work.stock",report.food().getCount(),
                report.food().isEmpty() ? Component.translatable("message.bunnidogs.work.empty") : report.food().getHoverName(),
                report.filter().isEmpty() ? Component.translatable("message.bunnidogs.work.any_food") : report.filter().getHoverName()).withStyle(ChatFormatting.GRAY));
        for (var worker : report.workers()) player.sendSystemMessage(Component.translatable("message.bunnidogs.work.row",worker.dog().getDisplayName(),
                worker.dog().earTag().orElse("—"),worker.reason()).withStyle(worker.ready() ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        if (report.total() > MAX_ROWS) player.sendSystemMessage(Component.translatable("message.bunnidogs.census.more",report.total()-MAX_ROWS).withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.translatable("message.bunnidogs.work.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
