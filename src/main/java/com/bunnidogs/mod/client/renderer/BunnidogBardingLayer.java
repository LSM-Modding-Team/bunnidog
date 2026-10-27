package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.client.model.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

/** The approved fitted meshes share every body, head, ear, limb and tail animation. */
public final class BunnidogBardingLayer extends RenderLayer<BunnidogRenderState,BunnidogModel> {
    private final BunnidogModel[] models=new BunnidogModel[5];
    private final Identifier[] textures=new Identifier[5];
    public BunnidogBardingLayer(RenderLayerParent<BunnidogRenderState,BunnidogModel> parent) {
        super(parent);
        for(int i=0;i<5;i++) {
            var name="barding_"+com.bunnidogs.mod.equipment.Barding.NAMES[i];
            models[i]=new BunnidogModel(BunnidogMesh.load(BunnidogModelLayer.createBodyLayer().bakeRoot(),name));
            textures[i]=Identifier.fromNamespaceAndPath("bunnidogs","textures/entity/"+name+".png");
        }
    }
    @Override public void submit(PoseStack poses,SubmitNodeCollector collector,int light,BunnidogRenderState state,float yRot,float xRot) {
        int tier=state.barding;if(tier<0 || tier>=5)return;
        int tint=state.redFuse && (int)(state.swelling*10)%2!=0 ? 0xFFFF3030 : -1;
        coloredCutoutModelCopyLayerRender(models[tier],textures[tier],poses,collector,light,state,tint,4);
    }
}
