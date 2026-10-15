package com.bunnidogs.mod.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/** The two synced sample slots, copied on the render thread; null slots draw nothing. */
public final class GeneticsTableRenderState extends BlockEntityRenderState {
    public ItemStackRenderState sampleA;
    public ItemStackRenderState sampleB;
}
