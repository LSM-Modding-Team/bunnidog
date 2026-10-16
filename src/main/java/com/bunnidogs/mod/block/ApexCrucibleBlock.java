package com.bunnidogs.mod.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.bunnidogs.mod.genetics.ApexRules;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Units;
import com.bunnidogs.mod.entity.Apex;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Finite, non-ticking ritual. Nothing is spent before the world accepts the boss. */
public final class ApexCrucibleBlock extends Block {
    private record Offerings(List<TrophyPedestalBlockEntity> ready, List<Item> missing) {}
    public static final List<BlockPos> RING = List.of(new BlockPos(0,0,-3), new BlockPos(3,0,0),
            new BlockPos(0,0,3), new BlockPos(-3,0,0), new BlockPos(2,0,-2),
            new BlockPos(2,0,2), new BlockPos(-2,0,2), new BlockPos(-2,0,-2));
    public ApexCrucibleBlock(Properties properties) { super(properties); }

    /** Shared bounded read for readiness, previews and the actual transaction. */
    private static Offerings offerings(Level level, BlockPos pos) {
        var pedestals = new ArrayList<TrophyPedestalBlockEntity>(8);
        for (var offset : RING) {
            var p = pos.offset(offset);
            if (level.hasChunkAt(p) && level.getBlockEntity(p) instanceof TrophyPedestalBlockEntity pedestal) pedestals.add(pedestal);
        }
        var ready = new ArrayList<TrophyPedestalBlockEntity>(5);
        var missing = new ArrayList<Item>(5);
        for (Item legend : List.of(ModItems.TITAN_HAUNCH.get(),ModItems.GILDED_FOOT.get(),ModItems.MOON_SINEW.get(),
                ModItems.LISTENING_SHELL.get(),ModItems.EVERBLOOM.get())) {
            var pedestal = pedestals.stream().filter(p -> p.item().is(legend)).findFirst();
            if (pedestal.isPresent()) ready.add(pedestal.get()); else missing.add(legend);
        }
        return new Offerings(List.copyOf(ready),List.copyOf(missing));
    }
    private static void readiness(Level level, BlockPos pos, Player player) {
        if (!level.hasChunksAt(pos.offset(-4,0,-4),pos.offset(4,4,4))) { refuse(player,"loaded"); return; }
        var stock = offerings(level,pos);
        Component names = Component.translatable("message.bunnidogs.apex.none_missing");
        if (!stock.missing().isEmpty()) {
            var list = Component.empty();
            for (Item item : stock.missing()) {
                if (!list.getSiblings().isEmpty()) list.append(", ");
                list.append(new ItemStack(item).getHoverName());
            }
            names = list;
        }
        player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.offerings_status",stock.ready().size(),names));
    }
    private static String number(double value) { return String.format(Locale.ROOT,"%.1f",value); }
    private static void forecast(Level level, BlockPos pos, Player player, InteractionHand hand) {
        var other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var source = TissueSampleItem.read(player.getItemInHand(other));
        if (source.isEmpty()) { refuse(player,"preview_hint"); return; }
        var plan = ApexRules.forecast(source.get());
        player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.forecast_source",Plain.look(source.get().genome())));
        player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.forecast_combat",number(plan.normal().health()),
                number(plan.normal().damage()),number(plan.enraged().damage())));
        player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.forecast_defense",number(plan.armor()),
                Math.round(plan.knockbackResistance()*100),number(plan.normal().awareness())));
        player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.forecast_motion",
                Math.round((plan.enraged().speed()/plan.normal().speed()-1)*100),number(Units.jumpHeight(plan.normal().jump()))));
        readiness(level,pos,player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            readiness(level,pos,player);
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.ritual"));
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.ring"));
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.preview_hint"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.CALIPERS.get())) {
            if (!level.isClientSide()) forecast(level,pos,player,hand);
            return InteractionResult.SUCCESS;
        }
        if (!stack.is(ModItems.TISSUE_SAMPLE.get())) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!player.mayBuild() || !level.mayInteract(player, pos)) return refuse(player, "permission");
        var source = TissueSampleItem.read(stack);
        if (source.isEmpty()) return refuse(player, "sample");
        if (level.getDifficulty() == Difficulty.PEACEFUL) return refuse(player, "peaceful");
        if (!level.hasChunksAt(pos.offset(-4,0,-4), pos.offset(4,4,4))) return refuse(player, "loaded");
        if (!level.getEntitiesOfClass(Apex.class, new AABB(pos).inflate(48), Apex::isAlive).isEmpty()) return refuse(player, "active");
        var stock = offerings(level,pos);
        if (!stock.missing().isEmpty()) {
            var missing = stock.missing();
            player.sendOverlayMessage(missing.size() == 1
                    ? Component.translatable("message.bunnidogs.apex.missing_one",new ItemStack(missing.getFirst()).getHoverName())
                    : Component.translatable("message.bunnidogs.apex.missing_many",new ItemStack(missing.getFirst()).getHoverName(),missing.size()-1));
            return InteractionResult.SUCCESS;
        }
        Apex apex = ModEntities.APEX.get().create(server, EntitySpawnReason.TRIGGERED);
        if (apex == null) return refuse(player, "blocked");
        apex.setGenome(source.get().genome());
        apex.setStats(source.get().stats());
        apex.setHealth(apex.getMaxHealth());
        apex.snapTo(pos.getX() + .5, pos.getY() + 1.01, pos.getZ() + .5, player.getYRot() + 180, 0);
        // Add shoulder room without expanding the feet into the crucible beneath them.
        AABB space = apex.getBoundingBox().inflate(.2, 0, .2);
        if (!level.noCollision(apex, space) || level.containsAnyLiquid(space)) {
            apex.discard();
            return refuse(player, "blocked");
        }
        if (!player.hasInfiniteMaterials() && !player.isSpectator()) apex.setTarget(player);
        if (!server.addFreshEntity(apex)) { apex.discard(); return refuse(player, "blocked"); }
        if (!player.hasInfiniteMaterials()) {
            for (var offering : stock.ready()) offering.takeItem();
            stack.shrink(1);
        }
        apex.playSound(SoundEvents.RAVAGER_ROAR, 1, .6F);
        server.sendParticles(ParticleTypes.WITCH, apex.getX(), apex.getY() + .5, apex.getZ(), 60, 1, .5, 1, .04);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex.summoned"));
        return InteractionResult.SUCCESS;
    }
    private static InteractionResult refuse(Player player, String reason) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex." + reason));
        return InteractionResult.SUCCESS;
    }
}
