package com.bunnidogs.mod.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/** What {@link TrophyPedestalRenderer} copies out of the block entity each frame (drops D8c): the item's render state (null = nothing on the pedestal) and the spin angle. */
public final class TrophyPedestalRenderState extends BlockEntityRenderState {
    public ItemStackRenderState item;
    public float spinDegrees;
}
