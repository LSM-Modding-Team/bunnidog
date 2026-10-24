package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.entity.ApexAnvil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/** Full-size world-oriented anvil; inventory ground transforms and camera billboarding do not shrink it. */
public final class ApexAnvilRenderer extends EntityRenderer<ApexAnvil,ThrownItemRenderState> {
    private final ItemModelResolver items;
    public ApexAnvilRenderer(EntityRendererProvider.Context context) {super(context);items=context.getItemModelResolver();}
    @Override public ThrownItemRenderState createRenderState() {return new ThrownItemRenderState();}
    @Override public void extractRenderState(ApexAnvil entity,ThrownItemRenderState state,float partial) {
        super.extractRenderState(entity,state,partial);items.updateForNonLiving(state.item,entity.getItem(),ItemDisplayContext.NONE,entity);
    }
    @Override public void submit(ThrownItemRenderState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        pose.pushPose();pose.translate(0,1,0);pose.scale(2,2,2);
        state.item.submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);
        pose.popPose();super.submit(state,pose,collector,camera);
    }
}
