package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.TableUse;
import com.mojang.blaze3d.vertex.PoseStack;

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

/** T4: stationary A/B vials on the front of the bench, clear of its central microscope. */
public final class GeneticsTableRenderer implements BlockEntityRenderer<GeneticsTableBlockEntity, GeneticsTableRenderState> {
    private final ItemModelResolver resolver;

    public GeneticsTableRenderer(BlockEntityRendererProvider.Context context) { resolver = context.itemModelResolver(); }

    @Override public GeneticsTableRenderState createRenderState() { return new GeneticsTableRenderState(); }

    @Override
    public void extractRenderState(GeneticsTableBlockEntity table, GeneticsTableRenderState state, float partialTick,
            Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(table,state,partialTick,camera,overlay);
        int seed = table.getBlockPos().hashCode();
        state.sampleA = item(table.item(TableUse.SLOT_A),seed);
        state.sampleB = item(table.item(TableUse.SLOT_B),seed+1);
    }

    private ItemStackRenderState item(ItemStack stack, int seed) {
        if (stack.isEmpty()) return null;
        ItemStackRenderState state = new ItemStackRenderState();
        resolver.updateForTopItem(state,stack,ItemDisplayContext.GROUND,null,null,seed);
        return state;
    }

    @Override
    public void submit(GeneticsTableRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(state.sampleA,0.22D,state.lightCoords,pose,collector);
        draw(state.sampleB,0.78D,state.lightCoords,pose,collector);
    }

    private static void draw(ItemStackRenderState item, double x, int light, PoseStack pose, SubmitNodeCollector collector) {
        if (item == null) return;
        pose.pushPose();
        pose.translate(x,0.86D,0.22D);
        pose.scale(0.65F,0.65F,0.65F);
        item.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,0);
        pose.popPose();
    }
}
