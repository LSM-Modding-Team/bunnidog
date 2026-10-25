package com.bunnidogs.mod.client.model;

import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Paired shoulder/hand joints: folded at rest, rhythmic flight, swept on a dive. */
public final class BunnidogWingModel extends EntityModel<BunnidogRenderState> {
    private final ModelPart rootPart,body,left,right;
    public BunnidogWingModel(ModelPart root) {
        super(root);rootPart=root;body=root.getChild("body");left=body.getChild("left");right=body.getChild("right");
    }
    @Override public void setupAnim(BunnidogRenderState state) {
        super.setupAnim(state);BunnidogBodyPose.body(body,state);BunnidogBodyPose.root(rootPart,state);
        body.visible=state.winged;
        float flap=state.wingFlying ? Mth.sin(state.ageInTicks*.45F)*.48F : .08F*Mth.sin(state.ageInTicks*.08F);
        float fold=state.wingFlying ? state.wingDiving ? .45F : .08F : 1.2F;
        left.zRot=-flap;right.zRot=flap;
        left.yRot=-fold;right.yRot=fold;
        left.getChild("hand").yRot=-fold*.75F;right.getChild("hand").yRot=fold*.75F;
        left.xRot=right.xRot=state.wingDiving ? -.25F : -.08F;
    }
}
