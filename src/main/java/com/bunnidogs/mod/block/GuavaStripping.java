package com.bunnidogs.mod.block;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * An axe strips the guava log to the stripped guava log and the guava wood to the stripped guava wood (W1b, docs/FIXES_PLAN.md 4.1).
 * Vanilla's {@code AxeItem} asks the clicked block for its tool-modified state; NeoForge turns that question into
 * {@link BlockEvent.BlockToolModificationEvent}, and setting the final state here is all it takes: the axe's own code then swaps the block,
 * plays the stripping sound and wears the tool. The mining axis is kept. Same pattern as {@code SeedCropHarvest}: a static subscriber.
 */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class GuavaStripping {
    private GuavaStripping() {}

    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) {
            return;
        }
        BlockState state = event.getState();
        RotatedPillarBlock stripped = null;
        if (state.is(ModBlocks.GUAVA_LOG.get())) {
            stripped = ModBlocks.STRIPPED_GUAVA_LOG.get();
        } else if (state.is(ModBlocks.GUAVA_WOOD.get())) {
            stripped = ModBlocks.STRIPPED_GUAVA_WOOD.get();
        }
        if (stripped != null) {
            event.setFinalState(stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
    }
}
