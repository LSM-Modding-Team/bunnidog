package com.bunnidogs.mod.block;

import java.util.Comparator;
import java.util.Optional;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NestHome;
import com.bunnidogs.mod.item.PlaceMarkers;
import com.bunnidogs.mod.registry.ModPoi;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Native home tickets, led reassignment and persistent compass place markers. */
public final class NestBlock extends Block {
    private static final double LEAD_RANGE = 8.0;
    private static final int SIGNAL_REFRESH = 100;
    public NestBlock(Properties properties) { super(properties); }

    public static boolean claimed(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getPoiManager().getCountInRange(holder -> holder.is(ModPoi.NEST.getKey()),
                pos,0,PoiManager.Occupancy.IS_OCCUPIED) > 0;
    }
    /** Remote ticket release must not load terrain just to notify a comparator. */
    public static void ticketChanged(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos).is(ModBlocks.NEST.get())) return;
        // Match native analog notifications, guarding the second step through a conductor too.
        for (var direction : new Direction[]{Direction.WEST,Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.DOWN,Direction.UP}) {
            var next = pos.relative(direction);
            if (!level.hasChunkAt(next)) continue;
            var state = level.getBlockState(next); state.onNeighborChange(level,next,pos);
            if (state.isRedstoneConductor(level,next)) {
                next = next.relative(direction);
                if (!level.hasChunkAt(next)) continue;
                state = level.getBlockState(next);
                if (state.getWeakChanges(level,next) && (direction.getAxis().isHorizontal() || !state.is(Blocks.COMPARATOR)))
                    level.neighborChanged(state,next,ModBlocks.NEST.get(),null,false);
            }
        }
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level instanceof ServerLevel server) || !server.hasChunkAt(pos)) return 0;
        // Native scheduled ticks survive unload. Only circuits that read this Nest keep refreshing.
        server.scheduleTick(pos,this,SIGNAL_REFRESH);
        return claimed(server,pos) ? 15 : 0;
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ticketChanged(level,pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.COMPASS)) return mark(level,pos,player,hand,hit);
        if (StationUse.inspectHeld(player,hand,entry->entry.is(Items.LEAD) || entry.is(Items.COMPASS))) return this.rehome(level,pos,player);
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
            PlaceMarkers.bind(level,pos,player,hand);
            level.playSound(null,pos,SoundEvents.LODESTONE_COMPASS_LOCK,SoundSource.PLAYERS,.6F,1);
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.nest.marked",pos.getX(),pos.getY(),pos.getZ()));
        }
        return InteractionResult.SUCCESS;
    }
    private InteractionResult inspect(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) {
            boolean claimed = claimed(server,pos);
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
    public static boolean hasLedAdult(Level level,BlockPos pos,Player player) {
        return !level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(LEAD_RANGE),dog->dog.isAlive() && !dog.isBaby()
                && dog.acceptsCare() && dog.isLeashed() && dog.getLeashHolder()==player && !dog.isPassenger()
                && dog.distanceToSqr(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5)<=LEAD_RANGE*LEAD_RANGE).isEmpty();
    }
    public InteractionResult assignLed(Level level,BlockPos pos,Player player) { return rehome(level,pos,player); }
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
            if (result==NestHome.MoveResult.MOVED || result==NestHome.MoveResult.ALREADY_HOME)
                com.bunnidogs.mod.item.JourneyProgress.housed(player);
            if (result == NestHome.MoveResult.MOVED) {
                level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, .3F, 1.2F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
