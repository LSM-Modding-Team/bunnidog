package com.bunnidogs.mod.client.model;

import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** The five crests follow the same head/body proportions as the underlying animal. */
public final class CrownModel extends EntityModel<BunnidogRenderState> {
    private final ModelPart rootPart,head,body;
    public CrownModel(ModelPart root) {super(root);rootPart=root;head=root.getChild("head");body=root.getChild("body");}
    @Override public void setupAnim(BunnidogRenderState s) {
        super.setupAnim(s);head.visible=body.visible=s.crowned;
        BunnidogBodyPose.root(rootPart,s);BunnidogBodyPose.body(body,s);
        head.y-=BunnidogModelLayer.LEG_LENGTH*(s.legScale-1);
        head.xRot+=Mth.clamp(s.xRot,-30,30)*Mth.DEG_TO_RAD;head.yRot+=Mth.clamp(s.yRot,-55,55)*Mth.DEG_TO_RAD;
        float headSize=s.isBaby ? 1.08F : 1;head.xScale=head.yScale=head.zScale=headSize;
        if(s.crownWarning>0)body.yScale*=1+.035F*Mth.sin(s.ageInTicks*.8F);
    }
}
