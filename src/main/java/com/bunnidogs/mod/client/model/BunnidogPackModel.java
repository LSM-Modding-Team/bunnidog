package com.bunnidogs.mod.client.model;

import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

/** A small equipment pass; none of the approved animal's sculpt, UVs or normals are changed. */
public final class BunnidogPackModel extends EntityModel<BunnidogRenderState> {
    private final ModelPart rootPart, body;
    private final ModelPart[] bags = new ModelPart[4];
    public BunnidogPackModel(ModelPart root) {
        super(root); this.rootPart = root; this.body = root.getChild("body");
        for (int i = 0; i < this.bags.length; i++) this.bags[i] = this.body.getChild("bag_"+i);
    }
    @Override public void setupAnim(BunnidogRenderState state) {
        super.setupAnim(state);
        BunnidogBodyPose.body(this.body,state); BunnidogBodyPose.root(this.rootPart,state);
        this.body.visible = state.bundleCount > 0;
        for (int i = 0; i < this.bags.length; i++) this.bags[i].visible = i < state.bundleCount;
    }
}
