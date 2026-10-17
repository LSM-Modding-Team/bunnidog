package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Seed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Loaded-only, checked litter placement. Callers reserve their one seed while native spawn hooks run. */
public final class NurseryHatching {
    private NurseryHatching() {}
    public enum Status { EMPTY, NOT_RIPE, BLOCKED, CROWDED, REFUSED, HATCHED, READY }
    public record Outcome(Status status, int count) {}
    public record PadCheck(Status status, BlockPos problem, int nearby) {}
    public static Outcome refused(Status status) { return new Outcome(status,0); }
    public static List<BlockPos> pad(BlockPos cradle, Direction facing) {
        var center = cradle.relative(facing,2);
        var slots = new ArrayList<BlockPos>();
        slots.add(center);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) if (x != 0 || z != 0) slots.add(center.offset(x,0,z));
        return List.copyOf(slots);
    }
    /** Read-only loaded-world preflight shared by actual hatching and the Calipers planning view. */
    public static PadCheck inspect(ServerLevel level, BlockPos cradle, Direction facing) {
        var slots = pad(cradle,facing);
        var center = cradle.relative(facing,2);
        int nearby = level.getEntitiesOfClass(Bunnidog.class,new AABB(center).inflate(Breeding.CROWD_RADIUS)).size();
        for (var pos : slots) {
            if (!level.hasChunkAt(pos) || !level.isInWorldBounds(pos.above()) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) return new PadCheck(Status.BLOCKED,pos,nearby);
            if (!level.getBlockState(pos.above()).isAir() || !level.getFluidState(pos.above()).isEmpty()) return new PadCheck(Status.BLOCKED,pos.above(),nearby);
            var floor = level.getBlockState(pos.below());
            if (!floor.isFaceSturdy(level,pos.below(),Direction.UP) || !floor.getFluidState().isEmpty()
                    || floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CAMPFIRE) || floor.is(Blocks.SOUL_CAMPFIRE)) return new PadCheck(Status.BLOCKED,pos.below(),nearby);
        }
        var area = new AABB(center.getX()-1,center.getY(),center.getZ()-1,center.getX()+2,center.getY()+2,center.getZ()+2);
        var occupants = level.getEntitiesOfClass(LivingEntity.class,area,e -> e.isAlive() && !e.isSpectator());
        if (!occupants.isEmpty()) return new PadCheck(Status.BLOCKED,occupants.getFirst().blockPosition(),nearby);
        if (!level.noCollision(null,area,true)) return new PadCheck(Status.BLOCKED,center,nearby);
        return new PadCheck(nearby >= Breeding.CROWD_LIMIT ? Status.CROWDED : Status.READY,null,nearby);
    }
    public static Outcome hatch(ServerLevel level, BlockPos cradle, Direction facing, Seed seed) {
        var check = inspect(level,cradle,facing);
        if (check.status() != Status.READY) return refused(check.status());
        var slots = pad(cradle,facing);
        var rng = new Random(level.getRandom().nextLong());
        var litter = seed.hatch(0,check.nearby(),rng);
        if (litter.isEmpty()) return refused(Status.CROWDED);
        var prepared = new ArrayList<Bunnidog>();
        try {
            for (int index = 0; index < litter.size(); index++) {
                var pup = Bunnidog.createPup(level,seed,litter.get(index));
                if (pup == null) return refused(Status.REFUSED);
                pup.snapTo(Vec3.atBottomCenterOf(slots.get(index)),rng.nextFloat()*360,0);
                if (!level.noCollision(pup,pup.getBoundingBox(),true)) return refused(Status.BLOCKED);
                prepared.add(pup);
            }
            for (var pup : prepared) if (!level.addFreshEntity(pup)) {
                prepared.forEach(Bunnidog::discard); return refused(Status.REFUSED);
            }
            return new Outcome(Status.HATCHED,prepared.size());
        } catch (RuntimeException failure) {
            prepared.forEach(Bunnidog::discard);
            BunnidogsMod.LOGGER.warn("Nursery hatch refused at {}",cradle,failure);
            return refused(Status.REFUSED);
        }
    }
}
