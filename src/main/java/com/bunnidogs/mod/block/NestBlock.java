package com.bunnidogs.mod.block;

import java.util.Comparator;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NestHome;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Keeps the existing Nest block/save identity; adds explicit rehoming of a dog led by the interacting player. */
public final class NestBlock extends Block {
    private static final double LEAD_RANGE = 8.0;
    public NestBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.is(Items.LEAD) ? this.rehome(level, pos, player) : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
                ? this.rehome(level, pos, player) : InteractionResult.PASS;
    }
    private InteractionResult rehome(Level level, BlockPos pos, Player player) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        var dogs = level.getEntitiesOfClass(Bunnidog.class, new AABB(pos).inflate(LEAD_RANGE),
                dog -> dog.isAlive() && dog.isLeashed() && dog.getLeashHolder() == player && !dog.isPassenger()
                        && dog.distanceToSqr(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5) <= LEAD_RANGE * LEAD_RANGE);
        // One click affects exactly one dog, with a stable nearest-first order if several leads are held.
        var candidate = dogs.stream().filter(dog -> !dog.isBaby()).min(Comparator.comparingDouble((Bunnidog dog) ->
                dog.distanceToSqr(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5)).thenComparing(Bunnidog::getUUID));
        if (candidate.isEmpty()) {
            if (dogs.isEmpty()) return InteractionResult.PASS;
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
