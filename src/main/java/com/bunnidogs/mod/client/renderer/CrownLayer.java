package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.client.model.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

public final class CrownLayer extends RenderLayer<BunnidogRenderState,BunnidogModel> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.fromNamespaceAndPath("bunnidogs","crowned_apex"),"main");
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("bunnidogs","textures/entity/crowned_apex.png");
    private final CrownModel crown;
    public CrownLayer(RenderLayerParent<BunnidogRenderState,BunnidogModel> p,ModelPart root) {super(p);crown=new CrownModel(root);}
    @Override public void submit(PoseStack poses,SubmitNodeCollector collector,int light,BunnidogRenderState state,float yRot,float xRot) {
        if(state.crowned)coloredCutoutModelCopyLayerRender(crown,TEXTURE,poses,collector,state.crownWarning>0 ? 0xF000F0 : light,state,-1,3);
    }
}
