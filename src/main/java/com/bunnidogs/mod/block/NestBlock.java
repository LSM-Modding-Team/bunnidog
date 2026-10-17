package com.bunnidogs.mod.block;

import java.util.Comparator;
import java.util.Optional;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NestHome;
import com.bunnidogs.mod.registry.ModPoi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Native home tickets, led reassignment and persistent compass place markers. */
public final class NestBlock extends Block {
    private static final double LEAD_RANGE = 8.0;
    public NestBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.COMPASS)) return mark(level,pos,player,hand,hit);
        return stack.is(Items.LEAD) ? this.rehome(level, pos, player) : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
                ? this.rehome(level, pos, player) : InteractionResult.PASS;
    }
    private InteractionResult mark(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var actual = player.getItemInHand(hand);
        if (!player.mayBuild() || !player.mayUseItemAt(pos,hit.getDirection(),actual)) return InteractionResult.PASS;
        if (level instanceof ServerLevel) {
            var tracker = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),pos)),false);
            if (actual.getCount() == 1) actual.set(DataComponents.LODESTONE_TRACKER,tracker);
            else {
                var marked = actual.copyWithCount(1);
                marked.set(DataComponents.LODESTONE_TRACKER,tracker);
                actual.consume(1,player);
                if (!player.getInventory().add(marked)) player.drop(marked,false);
            }
            level.playSound(null,pos,SoundEvents.LODESTONE_COMPASS_LOCK,SoundSource.PLAYERS,.6F,1);
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.nest.marked",pos.getX(),pos.getY(),pos.getZ()));
        }
        return InteractionResult.SUCCESS;
    }
    private InteractionResult inspect(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) {
            boolean claimed = server.getPoiManager().getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),pos,0,PoiManager.Occupancy.IS_OCCUPIED) > 0;
            var resident = claimed ? level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(24),
                    dog -> dog.isAlive() && dog.acceptsCare() && pos.equals(dog.homeNest())).stream()
                    .min(Comparator.comparing(Bunnidog::getUUID)) : Optional.<Bunnidog>empty();
            player.sendOverlayMessage(!claimed ? Component.translatable("message.bunnidogs.nest.free")
                    : resident.isEmpty() ? Component.translatable("message.bunnidogs.nest.away")
                    : Component.translatable("message.bunnidogs.nest.resident",resident.get().getDisplayName(),
                            resident.get().earTag().orElse("—")));
        }
        return InteractionResult.SUCCESS;
    }
    private InteractionResult rehome(Level level, BlockPos pos, Player player) {
        var dogs = level.getEntitiesOfClass(Bunnidog.class, new AABB(pos).inflate(LEAD_RANGE),
                dog -> dog.isAlive() && dog.isLeashed() && dog.getLeashHolder() == player && !dog.isPassenger()
                        && dog.distanceToSqr(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5) <= LEAD_RANGE * LEAD_RANGE);
        if (dogs.isEmpty()) return inspect(level,pos,player);
        if (!player.mayBuild()) return InteractionResult.PASS;
        // One click affects exactly one dog, with a stable nearest-first order if several leads are held.
        var candidate = dogs.stream().filter(dog -> !dog.isBaby()).min(Comparator.comparingDouble((Bunnidog dog) ->
                dog.distanceToSqr(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5)).thenComparing(Bunnidog::getUUID));
        if (candidate.isEmpty()) {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.nest.adult_required"));
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel server) {
            var result = candidate.get().moveHomeTo(server, pos);
            String message = switch (result) {
                case MOVED -> "moved";
                case ALREADY_HOME -> "already_home";
                case OCCUPIED -> "occupied";
                case UNREACHABLE -> "unreachable";
                case INVALID -> "adult_required";
            };
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.nest." + message));
            if (result == NestHome.MoveResult.MOVED) {
                level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, .3F, 1.2F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
