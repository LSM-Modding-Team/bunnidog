package com.bunnidogs.mod.orchard;

import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.CommonHooks;

/** Loaded, physical roots and a bounded canopy harvest. No crop manager, timers or chunk tickets. */
public final class Orchard {
    /** Follow only actual guava leaves, at most six steps, then the standing trunk to its soil. */
    public static BlockPos root(LevelReader world,BlockPos fruit) {
        var start=fruit.above();if(!world.hasChunkAt(start) || !world.getBlockState(start).is(ModBlocks.GUAVA_LEAVES.get()))return null;
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(start);
        for(int depth=0;depth<=6 && !queue.isEmpty();depth++) {
            int count=queue.size();while(count-->0) {
                var pos=queue.remove();if(!seen.add(pos) || seen.size()>384)continue;
                if(!world.hasChunkAt(pos))continue;var state=world.getBlockState(pos);
                if(state.is(ModBlocks.GUAVA_LOG.get()) && state.getValue(RotatedPillarBlock.AXIS)==Direction.Axis.Y) {var root=trunkRoot(world,pos);if(root!=null)return root;continue;}
                if(!state.is(ModBlocks.GUAVA_LEAVES.get()))continue;
                for(var dir:Direction.values()) {var next=pos.relative(dir);if(!seen.contains(next) && world.hasChunkAt(next))queue.add(next);}
            }
        }
        return null;
    }
    public static BlockPos trunkRoot(LevelReader world,BlockPos log) {
        var at=log.immutable();
        for(int i=0;i<24;i++) {
            if(!world.hasChunkAt(at) || !world.getBlockState(at).is(ModBlocks.GUAVA_LOG.get()) || world.getBlockState(at).getValue(RotatedPillarBlock.AXIS)!=Direction.Axis.Y)return null;
            var below=at.below();if(!world.hasChunkAt(below))return null;var soil=world.getBlockState(below);
            if(soil.is(BlockTags.SUPPORTS_VEGETATION) || soil.is(Blocks.FARMLAND))return at;
            if(!soil.is(ModBlocks.GUAVA_LOG.get()))return null;at=below;
        }return null;
    }
    public static int cropSize(LevelReader world,BlockPos fruit) {
        var root=root(world,fruit);return root==null ? 1 : world.getBlockState(root.below()).is(ModBlocks.ORCHARD_MULCH.get()) ? 3 : 2;
    }
    /** A harvest triggered away from the clicked trunk/ranged tool still honours target protection. */
    public static boolean permit(Player player,InteractionHand hand,BlockPos pos) {
        var world=player.level();if(!player.isAlive() || player.isSpectator() || !player.mayBuild() || !world.hasChunkAt(pos) || !world.mayInteract(player,pos) || !player.mayUseItemAt(pos,Direction.UP,player.getItemInHand(hand)))return false;
        var event=CommonHooks.onRightClickBlock(player,hand,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        return !event.isCanceled() && !event.getUseBlock().isFalse() && !event.getUseItem().isFalse();
    }
    public static int shake(ServerLevel world,BlockPos log,Player player) {
        if(!player.isAlive() || player.isSpectator() || !player.mayBuild() || player.distanceToSqr(Vec3.atCenterOf(log))>36 || !world.hasChunkAt(log) || !world.mayInteract(player,log))return 0;
        long now=world.getGameTime();if(player.getPersistentData().getLongOr("OrchardShakeUntil",Long.MIN_VALUE)>now)return 0;
        var root=trunkRoot(world,log);if(root==null)return 0;
        player.getPersistentData().putLong("OrchardShakeUntil",now+12);
        int picked=0;var hand=player.getMainHandItem();var original=hand.copy();
        for(var cell:BlockPos.betweenClosed(root.offset(-4,0,-4),root.offset(4,12,4))) {
            if(!world.hasChunkAt(cell))continue;var state=world.getBlockState(cell);
            if(!state.is(ModBlocks.GUAVA_FRUIT.get()) || !GuavaFruitBlock.isRipe(state) || !root.equals(root(world,cell)))continue;
            if(!permit(player,InteractionHand.MAIN_HAND,cell))continue;
            if(player.getMainHandItem()!=hand || !ItemStack.matches(hand,original) || !player.isAlive() || !player.mayBuild() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(log))>36 || !world.mayInteract(player,cell) || !world.getBlockState(cell).equals(state) || !root.equals(root(world,cell)))continue;
            if(GuavaFruitBlock.pick(world,cell) && ++picked>=32)break;
        }
        if(picked>0) {
            world.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,ModBlocks.GUAVA_LEAVES.get().defaultBlockState()),log.getX()+.5,log.getY()+1.5,log.getZ()+.5,18,.7,.7,.7,.05);
            world.playSound(null,log,net.minecraft.sounds.SoundEvents.AZALEA_LEAVES_HIT,net.minecraft.sounds.SoundSource.BLOCKS,.8F,.8F);
        }return picked;
    }
    private Orchard(){}
}
