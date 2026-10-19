package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.client.model.BunnidogModel;
import com.bunnidogs.mod.client.model.BunnidogPackModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

/** Native deferred model submission uses each entity's render snapshot and correct visibility/light. */
public final class BunnidogPackLayer extends RenderLayer<BunnidogRenderState,BunnidogModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath("bunnidogs","bunnidog_packs"),"main");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("bunnidogs","textures/entity/bunnidog_packs.png");
    private final BunnidogPackModel model;
    public BunnidogPackLayer(RenderLayerParent<BunnidogRenderState,BunnidogModel> parent, ModelPart root) {
        super(parent); this.model = new BunnidogPackModel(root);
    }
    @Override public void submit(PoseStack poses, SubmitNodeCollector collector, int light, BunnidogRenderState state, float yRot, float xRot) {
        if (state.bundleCount > 0) coloredCutoutModelCopyLayerRender(this.model,TEXTURE,poses,collector,light,state,-1,2);
    }
}
