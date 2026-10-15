package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Draws the one item that stands on a Trophy Pedestal (drops D8c): the item's own model in the "ground" pose (what a dropped item
 * looks like), hovering above the top plate and turning slowly. The shape of this class follows NeoForge's own item-hanger test
 * renderer (tests/.../CustomItemDisplayContextTest): a render state class, {@code extractRenderState} copies what is needed, {@code submit}
 * draws it.
 */
public class TrophyPedestalRenderer implements BlockEntityRenderer<TrophyPedestalBlockEntity, TrophyPedestalRenderState> {
    /** One full turn takes this many milliseconds. */
    private static final long SPIN_MILLIS = 8000L;
    /** Height of the item's centre above the block's floor: the plate's top is at 1.0, a flat item hovers a little above it. */
    private static final double HOVER_Y = 1.32D;

    private final ItemModelResolver itemModelResolver;

    public TrophyPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public TrophyPedestalRenderState createRenderState() {
        return new TrophyPedestalRenderState();
    }

    @Override
    public void extractRenderState(TrophyPedestalBlockEntity blockEntity, TrophyPedestalRenderState renderState, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        ItemStack stack = blockEntity.item();
        if (stack.isEmpty()) {
            renderState.item = null;
            return;
        }
        ItemStackRenderState stackState = new ItemStackRenderState();
        this.itemModelResolver.updateForTopItem(stackState, stack, ItemDisplayContext.GROUND, null, null, 0);
        renderState.item = stackState;
        renderState.spinDegrees = (System.currentTimeMillis() % SPIN_MILLIS) / (float) SPIN_MILLIS * 360.0F;
    }

    @Override
    public void submit(TrophyPedestalRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (renderState.item == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, HOVER_Y, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.spinDegrees));
        renderState.item.submit(poseStack, submitNodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
