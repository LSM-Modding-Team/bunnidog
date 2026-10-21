package com.bunnidogs.mod.client.model;

import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import net.minecraft.client.model.geom.ModelPart;

/** Shared body/ground transforms keep separate equipment on the exact approved body pose. */
public final class BunnidogBodyPose {
    private BunnidogBodyPose() {}
    public static void body(ModelPart body, BunnidogRenderState state) {
        body.y -= BunnidogModelLayer.LEG_LENGTH*(state.legScale-1);
        body.xScale = state.buildScale;
        body.yScale = 1+(state.buildScale-1)*.6F;
        body.zScale = 1+(state.buildScale-1)*.3F;
    }
    public static void root(ModelPart root, BunnidogRenderState state) {
        float size = state.isBaby ? .65F : 1;
        root.xScale = size; root.yScale = size; root.zScale = size;
        root.y += 24*(1-size);
        if (state.downed) root.y+=Math.max(.05F,BunnidogModelLayer.LEG_LENGTH*.35F*state.legScale-2.2F*(state.buildScale-1));
    }
}
