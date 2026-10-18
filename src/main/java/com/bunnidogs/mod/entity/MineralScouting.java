package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Scouting;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.item.PlaceMarkers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

/** Deliberate local survey; no terrain edits, random rolls, path search or chunk requests. */
public final class MineralScouting {
    private MineralScouting() {}
    public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("bunnidogs","surveyable_ores"));
    private static TagKey<Block> oreTag(String mineral) {
        return TagKey.create(Registries.BLOCK,Identifier.withDefaultNamespace(mineral+"_ores"));
    }
    enum Focus {
        ANY(null,null), COAL(oreTag("coal"),null), IRON(oreTag("iron"),null), COPPER(oreTag("copper"),null),
        GOLD(oreTag("gold"),null), REDSTONE(oreTag("redstone"),null), LAPIS(oreTag("lapis"),null),
        DIAMOND(oreTag("diamond"),null), EMERALD(oreTag("emerald"),null),
        QUARTZ(null,Blocks.NETHER_QUARTZ_ORE), DEBRIS(null,Blocks.ANCIENT_DEBRIS);
        private final TagKey<Block> family;
        private final Block exact;
        Focus(TagKey<Block> family, Block exact) { this.family = family; this.exact = exact; }
        boolean matches(BlockState state) {
            return state.is(ORES) && (this == ANY || this.family != null && state.is(this.family)
                    || this.exact != null && state.is(this.exact));
        }
        Component label() { return Component.translatable("survey.bunnidogs.focus."+name().toLowerCase(java.util.Locale.ROOT)); }
    }
    private static Focus requested(ItemStack mineral) {
        if (mineral.is(Items.COAL)) return Focus.COAL;
        if (mineral.is(Items.RAW_IRON) || mineral.is(Items.IRON_INGOT)) return Focus.IRON;
        if (mineral.is(Items.RAW_COPPER) || mineral.is(Items.COPPER_INGOT)) return Focus.COPPER;
        if (mineral.is(Items.RAW_GOLD) || mineral.is(Items.GOLD_INGOT)) return Focus.GOLD;
        if (mineral.is(Items.REDSTONE)) return Focus.REDSTONE;
        if (mineral.is(Items.LAPIS_LAZULI)) return Focus.LAPIS;
        if (mineral.is(Items.DIAMOND)) return Focus.DIAMOND;
        if (mineral.is(Items.EMERALD)) return Focus.EMERALD;
        if (mineral.is(Items.QUARTZ)) return Focus.QUARTZ;
        if (mineral.is(Items.NETHERITE_SCRAP) || mineral.is(Items.NETHERITE_INGOT)) return Focus.DEBRIS;
        return Focus.ANY;
    }
    static BlockPos nearest(ServerLevel level, BlockPos center) {
        return nearest(level,center,Focus.ANY);
    }
    private static BlockPos nearest(ServerLevel level, BlockPos center, Focus focus) {
        BlockPos best = null;
        double distance = Double.POSITIVE_INFINITY;
        int r = Scouting.RADIUS;
        for (int dx = -r; dx <= r; dx++) for (int dy = -r; dy <= r; dy++) for (int dz = -r; dz <= r; dz++) {
            double squared = dx*dx+dy*dy+dz*dz;
            if (squared > r*r || squared >= distance) continue;
            var pos = center.offset(dx,dy,dz);
            if (level.hasChunkAt(pos) && level.getWorldBorder().isWithinBounds(pos)
                    && focus.matches(level.getBlockState(pos))) { best = pos; distance = squared; }
        }
        return best;
    }
    public static InteractionResult use(Bunnidog dog, Player player, InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(dog.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var status = dog.scoutingStatus();
        if (status != Scouting.Status.READY) {
            Component reason = status == Scouting.Status.RESTING
                    ? Component.translatable("message.bunnidogs.scouting.wait",(dog.scoutingWait()+19)/20)
                    : Component.translatable("message.bunnidogs.scouting."+status.name().toLowerCase(java.util.Locale.ROOT));
            player.sendOverlayMessage(reason);
            return InteractionResult.SUCCESS;
        }
        var focus = requested(player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        if (focus != Focus.ANY && dog.stats().get(Stat.SENSES) < Scouting.MIN_FOCUSED_SENSES) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.scouting.focus_requirement",Scouting.MIN_FOCUSED_SENSES));
            return InteractionResult.SUCCESS;
        }
        var pos = nearest(level,dog.blockPosition(),focus);
        // Empty surveys also rest, so repeated clicks/gene edits cannot turn this into a continuous scanner.
        dog.beginScoutingRecovery();
        if (pos == null) player.sendOverlayMessage(Component.translatable("message.bunnidogs.scouting.none",focus.label(),Scouting.RADIUS));
        else {
            PlaceMarkers.bind(level,pos,player,hand);
            player.sendSystemMessage(Component.translatable("message.bunnidogs.scouting.marked",level.getBlockState(pos).getBlock().getName(),
                    pos.getX(),pos.getY(),pos.getZ()));
            WorkingLineProgress.survey(player,focus != Focus.ANY);
        }
        dog.playSound(SoundEvents.AMETHYST_BLOCK_CHIME,.5F,1.2F);
        return InteractionResult.SUCCESS;
    }
}
