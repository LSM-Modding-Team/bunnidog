package com.bunnidogs.mod.block;

import java.util.List;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The optional encounter starts from an explicit, sample-bound preparation and summon menu. */
public final class ApexCrucibleBlock extends Block {
    public static final List<BlockPos> RING=List.of(new BlockPos(0,0,-3),new BlockPos(3,0,0),new BlockPos(0,0,3),new BlockPos(-3,0,0),
            new BlockPos(2,0,-2),new BlockPos(2,0,2),new BlockPos(-2,0,2),new BlockPos(-2,0,-2));
    public ApexCrucibleBlock(Properties properties) { super(properties); }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
        CareMenu.open(player,pos); return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if (stack.is(ModItems.TISSUE_SAMPLE.get())) { CareMenu.open(player,pos,player.getItemInHand(hand)); return InteractionResult.SUCCESS; }
        if (stack.is(ModItems.CALIPERS.get())) {
            CareMenu.open(player,pos,player.getItemInHand(hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack,state,level,pos,player,hand,hit);
    }
    /** Brief world-space markers, requested by the inspecting player; never loads a ring chunk. */
    public void showRing(Player player,BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level) || !level.hasChunkAt(pos) || !level.getBlockState(pos).is(this)
                || player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>36) return;
        for (var offset:RING) {
            var at=pos.offset(offset); if (!level.hasChunkAt(at)) continue;
            level.sendParticles(ParticleTypes.END_ROD,at.getX()+.5,at.getY()+1.1,at.getZ()+.5,16,.15,.3,.15,0);
        }
    }
}
