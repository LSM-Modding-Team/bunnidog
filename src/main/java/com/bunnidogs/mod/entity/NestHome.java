package com.bunnidogs.mod.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Home;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModPoi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.pathfinder.Path;

/**
 * The nest a bunnidog has claimed (H2, docs/HOME_SPECS.md rules R1-R7), the way a villager keeps its bed. One instance per dog.
 * Server side only. {@code Bunnidog} calls {@link #tick}, {@link #release} (on death) and the save/restore pair; the decisions
 * (when to look, when to check) are pure and live in {@link Home}.
 *
 * <p>The nest position is OUR field, saved by us, so the ticket we hold is never in doubt. Vanilla's own mob home is NOT used (its
 * method names changed in this Minecraft version and were never verified): the walk home is {@link GoHomeGoal}.
 */
public final class NestHome {
    public enum MoveResult { MOVED, ALREADY_HOME, OCCUPIED, UNREACHABLE, INVALID }
    /** Marks "no nest" in the saved data (a block Y can never be this). {@link #nest()} is null while there is none. */
    public static final int NO_NEST = Integer.MIN_VALUE;

    private final Bunnidog dog;
    private BlockPos nest;
    private int timer;
    /** R4: nests this dog could not reach (not saved: a reloaded dog simply tries again). */
    private final Set<Long> rejected = new HashSet<>();

    public NestHome(Bunnidog dog) {
        this.dog = dog;
        this.timer = Home.lookDelay(0);
    }

    public BlockPos nest() {
        return this.nest;
    }

    public boolean hasNest() {
        return this.nest != null;
    }

    /** Player-directed relocation. Acquire the destination before releasing the old ticket. */
    public MoveResult moveTo(ServerLevel level, BlockPos destination) {
        if (this.dog.isBaby() || !level.hasChunkAt(destination) || !level.getBlockState(destination).is(ModBlocks.NEST.get())) {
            return MoveResult.INVALID;
        }
        if (destination.equals(this.nest)) return MoveResult.ALREADY_HOME;
        if (!this.canReach(destination)) return MoveResult.UNREACHABLE;
        var taken = level.getPoiManager().take(holder -> holder.is(ModPoi.NEST.getKey()),
                (holder, pos) -> pos.equals(destination), destination, 1);
        if (taken.isEmpty()) return MoveResult.OCCUPIED;
        BlockPos previous = this.nest;
        this.claim(taken.get());
        this.rejected.clear();
        // POI storage can release a remote ticket without requesting its terrain chunk.
        if (previous != null && level.getPoiManager().exists(previous, holder -> holder.is(ModPoi.NEST.getKey()))) {
            this.giveBack(level, previous);
        }
        return MoveResult.MOVED;
    }

    /** Called from the dog's server tick. Counts down, then looks (R2-R4) or checks (R6). */
    public void tick(ServerLevel level, boolean adult) {
        // Keep the old home during a journey; do not claim passing Nests or request remote home chunks.
        if (this.dog.beingTransported()) return;
        this.timer--;
        switch (Home.step(adult, this.hasNest(), this.timer)) {
            case LOOK -> this.look(level);
            case CHECK -> this.check(level);
            case NONE -> {
                if (this.timer <= 0) {
                    // a pup (or a dog that just stopped needing a look): wait a normal interval and ask again
                    this.timer = Home.lookDelay(this.dog.getRandom().nextInt(Home.LOOK_JITTER + 1));
                }
            }
        }
    }

    /** R3 + R4 + R5: take a free nest within reach, check the path, claim it. */
    private void look(ServerLevel level) {
        this.timer = Home.lookDelay(this.dog.getRandom().nextInt(Home.LOOK_JITTER + 1));
        PoiManager pois = level.getPoiManager();
        Optional<BlockPos> taken = pois.take(
                holder -> holder.is(ModPoi.NEST.getKey()),
                (holder, pos) -> !this.rejected.contains(pos.asLong()),
                this.dog.blockPosition(), Home.SEARCH_RADIUS);
        if (taken.isEmpty()) {
            return;
        }
        BlockPos pos = taken.get();
        if (!this.canReach(pos)) {
            // R4: give the ticket back at once and do not pick it again soon
            this.giveBack(level, pos);
            if (Home.rejectsFull(this.rejected.size())) {
                this.rejected.clear();
            }
            this.rejected.add(pos.asLong());
            return;
        }
        this.claim(pos);
    }

    /** R4: the path finder can bring the dog next to the nest (the block above it is the target; the nest itself is solid). */
    private boolean canReach(BlockPos pos) {
        Path path = this.dog.getNavigation().createPath(pos.above(), 1);
        return path != null && path.canReach();
    }

    /** R5: remember the nest; {@link GoHomeGoal} brings the dog back whenever it is more than {@link Home#HOME_RADIUS} away. */
    private void claim(BlockPos pos) {
        this.nest = pos;
        this.timer = Home.CHECK_INTERVAL;
    }

    /** R6: the nest block is still there, or the home is forgotten (the block took its ticket with it). */
    private void check(ServerLevel level) {
        this.timer = Home.CHECK_INTERVAL;
        if (this.nest != null && level.hasChunkAt(this.nest) && !level.getBlockState(this.nest).is(ModBlocks.NEST.get())) {
            this.forget();
        }
    }

    /** R7: the dog died; give the ticket back so the nest is free for the next dog. A chunk unloading does NOT call this. */
    public void release(ServerLevel level) {
        if (this.nest != null) {
            BlockPos pos = this.nest;
            if (level.getBlockState(pos).is(ModBlocks.NEST.get())) {
                this.giveBack(level, pos);
            }
            this.forget();
        }
    }

    private void forget() {
        this.nest = null;
        this.timer = Home.lookDelay(this.dog.getRandom().nextInt(Home.LOOK_JITTER + 1));
    }

    /** Releasing a ticket that was never taken may throw inside the game; a failed give-back must never crash the tick. */
    private void giveBack(ServerLevel level, BlockPos pos) {
        try {
            level.getPoiManager().release(pos);
        } catch (RuntimeException e) {
            BunnidogsMod.LOGGER.warn("Bunnidog could not give back the ticket of the nest at {}", pos, e);
        }
    }

    // ------------------------------------------------------------------ saving

    /** Loading: the dog keeps its nest (the ticket was saved with the chunk's points of interest). */
    public void restore(int x, int y, int z) {
        if (y == NO_NEST) {
            this.nest = null;
            return;
        }
        this.nest = new BlockPos(x, y, z);
        this.timer = Home.CHECK_INTERVAL;
    }
}
