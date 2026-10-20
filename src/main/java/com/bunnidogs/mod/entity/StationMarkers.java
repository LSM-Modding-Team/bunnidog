package com.bunnidogs.mod.entity;

import java.util.List;
import com.bunnidogs.mod.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A requested, brief world-space cue, derived from the same positions used by the station's job. */
public final class StationMarkers {
    private StationMarkers() {}
    public static final int SHOW_PAD=152;
    public static List<BlockPos> cells(Level level,BlockPos pos) {
        if (!level.hasChunkAt(pos)) return List.of();
        var state=level.getBlockState(pos);
        if (state.getBlock() instanceof NurseryCradleBlock) return NurseryHatching.pad(pos,state.getValue(NurseryCradleBlock.FACING));
        if (state.getBlock() instanceof CommissionBoardBlock) return List.of(pos.relative(state.getValue(CommissionBoardBlock.FACING)));
        if (state.getBlock() instanceof PedigreeComparatorBlock) return List.of(pos.relative(state.getValue(PedigreeComparatorBlock.FACING)));
        return List.of();
    }
    public static void show(Player player,BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level) || !player.isAlive() || player.isSpectator()
                || player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>36) return;
        var cells=cells(level,pos);
        for (var cell:cells) if (level.hasChunkAt(cell)) level.sendParticles(ParticleTypes.END_ROD,cell.getX()+.5,cell.getY()+.1,cell.getZ()+.5,12,.16,.05,.16,0);
        if (level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof NurseryCradleBlock) {
            var check=NurseryHatching.inspect(level,pos,level.getBlockState(pos).getValue(NurseryCradleBlock.FACING)); var problem=check.problem();
            if (problem!=null && level.hasChunkAt(problem)) level.sendParticles(ParticleTypes.ANGRY_VILLAGER,problem.getX()+.5,problem.getY()+.5,problem.getZ()+.5,8,.15,.15,.15,0);
        }
        if (!cells.isEmpty()) player.sendOverlayMessage(Component.translatable("care.bunnidogs.area_marked"));
    }
}
