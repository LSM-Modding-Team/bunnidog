package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.client.model.BunnidogModel;
import com.bunnidogs.mod.client.model.BunnidogWingModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

/** Native deferred model submission uses each entity's render snapshot and correct visibility/light. */
public final class BunnidogWingLayer extends RenderLayer<BunnidogRenderState,BunnidogModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("bunnidogs","bunnidog_wings"),"main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("bunnidogs","textures/entity/bunnidog_wings.png");
    private final BunnidogWingModel model;
    public BunnidogWingLayer(RenderLayerParent<BunnidogRenderState,BunnidogModel> parent, ModelPart root) {
        super(parent); this.model = new BunnidogWingModel(root);
    }
    @Override public void submit(PoseStack poses, SubmitNodeCollector collector, int light, BunnidogRenderState state, float yRot, float xRot) {
        if (state.winged) coloredCutoutModelCopyLayerRender(this.model,TEXTURE,poses,collector,light,state,-1,2);
    }
}
