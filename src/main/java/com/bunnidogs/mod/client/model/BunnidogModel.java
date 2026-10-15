package com.bunnidogs.mod.client.model;

import com.bunnidogs.mod.client.renderer.BunnidogRenderState;
import com.bunnidogs.mod.genetics.Temperament;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Bunnidog model: hand-written ANIMATION and GENE PROPORTIONS only.
 * The skeleton/loader is in GENERATED {@link BunnidogModelLayer}; the connected
 * textured mesh is generated alongside it by art/gen_bunnidog.py.
 *
 * Genes are applied here with per-part scale (no extra models): ears grow up from their base, legs stretch and lift
 * the whole body so the paws still touch the ground, build widens the body and thickens the legs.
 * Pups (Phase 2) are the same model shrunk about the ground plane with a proportionally bigger head.
 */
public class BunnidogModel extends EntityModel<BunnidogRenderState> {
    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;
    /** Ground-to-hip distance shared with the generated skeleton. */
    private static final float LEG_LENGTH = BunnidogModelLayer.LEG_LENGTH;
    /** Ground level in model space (y-down, feet at y=24). */
    private static final float GROUND_Y = 24.0F;
    /**
     * Pups: how big the whole model is, and how much bigger the head is on top of that (babies have big heads).
     * If pups ever look TINY in game, vanilla is already shrinking babies too: set PUP_SCALE to 1.0F.
     */
    private static final float PUP_SCALE = 0.65F;
    private static final float PUP_HEAD_SCALE = 1.08F;

    private final ModelPart rootPart;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earR;
    private final ModelPart earL;
    private final ModelPart legFr;
    private final ModelPart legFl;
    private final ModelPart legBr;
    private final ModelPart legBl;
    private final ModelPart tail;

    public BunnidogModel(ModelPart root) {
        super(root);
        this.rootPart = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.earR = this.head.getChild("ear_r");
        this.earL = this.head.getChild("ear_l");
        this.legFr = root.getChild("leg_fr");
        this.legFl = root.getChild("leg_fl");
        this.legBr = root.getChild("leg_br");
        this.legBl = root.getChild("leg_bl");
        this.tail = root.getChild("tail");
    }

    @Override
    public void setupAnim(BunnidogRenderState state) {
        super.setupAnim(state);   // resets every part to its default pose first

        applyProportions(state);
        applyPupSize(state);

        // Head follows where the mob is looking.
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG_TO_RAD;
        this.head.yRot += Mth.clamp(state.yRot, -55.0F, 55.0F) * DEG_TO_RAD;

        // Trotting legs (diagonal pairs move together, like a dog).
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        float swing = Mth.cos(pos * 0.6662F) * 0.85F * speed;
        this.legFr.xRot = swing;
        this.legBl.xRot = swing;
        this.legFl.xRot = -swing;
        this.legBr.xRot = -swing;

        // Temperament shows in the tail and ears (defaults = loyal: big happy wag).
        float wagRate = 0.55F;
        float wagAmount = 0.55F;
        float tailPitch = 0.0F;     // + up, - down (the model's default tail already tilts up)
        float earSplay = 0.0F;      // ears flattened outward
        Temperament temperament = state.temperament;
        if (temperament == Temperament.SKITTISH) {          // tail tucked, ears back, trembling
            wagRate = 1.1F; wagAmount = 0.12F; tailPitch = -0.9F; earSplay = 0.45F;
        } else if (temperament == Temperament.FERAL) {      // stiff raised tail, ears pinned
            wagRate = 0.25F; wagAmount = 0.05F; tailPitch = 0.35F; earSplay = 0.3F;
        } else if (temperament == Temperament.STOIC) {      // barely moves
            wagRate = 0.2F; wagAmount = 0.15F;
        }
        this.tail.yRot = Mth.cos(state.ageInTicks * wagRate) * wagAmount;
        this.tail.xRot += tailPitch;

        // Tiny ear twitch, driven by age so it idles even when standing still.
        this.earR.zRot += earSplay + Mth.sin(state.ageInTicks * 0.11F) * 0.05F;
        this.earL.zRot -= earSplay + Mth.sin(state.ageInTicks * 0.11F + 1.7F) * 0.05F;
    }

    /** Ears / Legs / Build genes -> part scales. Must run right after the pose reset. */
    private void applyProportions(BunnidogRenderState state) {
        // Legs: stretch, and raise/lower everything above them by the same amount so paws stay on the ground.
        float lift = LEG_LENGTH * (state.legScale - 1.0F);
        this.body.y -= lift;
        this.head.y -= lift;
        this.tail.y -= lift;
        this.legFr.y -= lift;
        this.legFl.y -= lift;
        this.legBr.y -= lift;
        this.legBl.y -= lift;
        float legThickness = 1.0F + (state.buildScale - 1.0F) * 0.6F;
        scaleLeg(this.legFr, state.legScale, legThickness);
        scaleLeg(this.legFl, state.legScale, legThickness);
        scaleLeg(this.legBr, state.legScale, legThickness);
        scaleLeg(this.legBl, state.legScale, legThickness);

        // Build: chonk widens the body most, lengthens it a little and deepens it about half as much as it widens.
        this.body.xScale = state.buildScale;
        this.body.yScale = 1.0F + (state.buildScale - 1.0F) * 0.6F;
        this.body.zScale = 1.0F + (state.buildScale - 1.0F) * 0.3F;

        // Ears: grow up from the base; a bit wider when longer so they do not look like sticks.
        float earWidth = 1.0F + (state.earScale - 1.0F) * 0.4F;
        this.earR.yScale = state.earScale;
        this.earL.yScale = state.earScale;
        this.earR.xScale = earWidth;
        this.earL.xScale = earWidth;
    }

    /**
     * Pups are shrunk about the ground plane (scale, then move the root down by the amount the feet rose) and get a
     * bigger head. Scales are assigned every frame, adults included: this one model instance draws every bunnidog.
     */
    private void applyPupSize(BunnidogRenderState state) {
        float size = state.isBaby ? PUP_SCALE : 1.0F;
        this.rootPart.xScale = size;
        this.rootPart.yScale = size;
        this.rootPart.zScale = size;
        this.rootPart.y += GROUND_Y * (1.0F - size);

        float headSize = state.isBaby ? PUP_HEAD_SCALE : 1.0F;
        this.head.xScale = headSize;
        this.head.yScale = headSize;
        this.head.zScale = headSize;
    }

    private static void scaleLeg(ModelPart leg, float length, float thickness) {
        leg.yScale = length;
        leg.xScale = thickness;
        leg.zScale = thickness;
    }
}
