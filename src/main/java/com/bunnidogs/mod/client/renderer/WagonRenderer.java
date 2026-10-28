package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.guild.KennelWagon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Timber carriage with a low covered kennel, copper-bound wheels and four visible native riders. */
public final class WagonRenderer extends MobRenderer<KennelWagon,LivingEntityRenderState,WagonRenderer.Model> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(Identifier.fromNamespaceAndPath("bunnidogs","kennel_wagon"),"main");
    public WagonRenderer(EntityRendererProvider.Context c) {super(c,new Model(c.bakeLayer(LAYER)),1.1F);}
    @Override public LivingEntityRenderState createRenderState(){return new LivingEntityRenderState();}
    @Override public Identifier getTextureLocation(LivingEntityRenderState s){return Identifier.fromNamespaceAndPath("bunnidogs","textures/entity/kennel_wagon.png");}
    public static LayerDefinition layer() {
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("frame",CubeListBuilder.create().texOffs(0,0).addBox(-15,14,-21,30,3,42).texOffs(0,48).addBox(-16,6,-21,2,8,42).addBox(14,6,-21,2,8,42)
            .texOffs(0,96).addBox(-16,-8,-22,32,2,44).texOffs(0,0).addBox(-15,6,19,30,8,2),PartPose.ZERO);
        for(int side:new int[]{-1,1})for(int end:new int[]{-1,1}) {
            root.addOrReplaceChild("post"+side+end,CubeListBuilder.create().texOffs(0,0).addBox(side*14-1,-6,end*19-1,2,20,2),PartPose.ZERO);
            root.addOrReplaceChild("wheel"+side+end,CubeListBuilder.create().texOffs(80,0).addBox(-2,-6,-6,4,12,12),PartPose.offset(side*17,19,end*14));
        }
        root.addOrReplaceChild("drawbar",CubeListBuilder.create().texOffs(0,0).addBox(-1,14,-37,2,2,18),PartPose.ZERO);
        return LayerDefinition.create(mesh,256,256);
    }
    static final class Model extends EntityModel<LivingEntityRenderState> {
        Model(ModelPart root){super(root);}
    }
}
