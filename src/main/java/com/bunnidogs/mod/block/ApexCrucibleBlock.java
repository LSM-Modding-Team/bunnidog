package com.bunnidogs.mod.block;

import java.util.ArrayList;
import java.util.List;
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
    public static final List<BlockPos> RING = List.of(new BlockPos(0,0,-3), new BlockPos(3,0,0),
            new BlockPos(0,0,3), new BlockPos(-3,0,0), new BlockPos(2,0,-2),
            new BlockPos(2,0,2), new BlockPos(-2,0,2), new BlockPos(-2,0,-2));
    public ApexCrucibleBlock(Properties properties) { super(properties); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.ritual"));
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.ring"));
            player.sendSystemMessage(Component.translatable("message.bunnidogs.apex.legends"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.TISSUE_SAMPLE.get())) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!player.mayBuild() || !level.mayInteract(player, pos)) return refuse(player, "permission");
        var source = TissueSampleItem.read(stack);
        if (source.isEmpty()) return refuse(player, "sample");
        if (level.getDifficulty() == Difficulty.PEACEFUL) return refuse(player, "peaceful");
        if (!level.hasChunksAt(pos.offset(-4,0,-4), pos.offset(4,4,4))) return refuse(player, "loaded");
        if (!level.getEntitiesOfClass(Apex.class, new AABB(pos).inflate(48), Apex::isAlive).isEmpty()) return refuse(player, "active");
        List<TrophyPedestalBlockEntity> offerings = new ArrayList<>(5);
        for (Item legend : List.of(ModItems.TITAN_HAUNCH.get(), ModItems.GILDED_FOOT.get(), ModItems.MOON_SINEW.get(),
                ModItems.LISTENING_SHELL.get(), ModItems.EVERBLOOM.get())) {
            TrophyPedestalBlockEntity found = null;
            for (BlockPos offset : RING) {
                if (level.getBlockEntity(pos.offset(offset)) instanceof TrophyPedestalBlockEntity pedestal
                        && pedestal.item().is(legend)) { found = pedestal; break; }
            }
            if (found == null) return refuse(player, "missing");
            offerings.add(found);
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
            for (var offering : offerings) offering.takeItem();
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
