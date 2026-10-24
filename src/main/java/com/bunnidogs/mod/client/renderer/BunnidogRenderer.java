package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.client.model.BunnidogModel;
import com.bunnidogs.mod.client.model.BunnidogModelLayer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Allele;
import com.bunnidogs.mod.genetics.Adaptations;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Traits;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class BunnidogRenderer extends MobRenderer<Bunnidog, BunnidogRenderState, BunnidogModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "bunnidog"), "main");

    /** One lightweight atlas per expressed coat allele. */
    private static final Identifier[] TEXTURES = buildTextures();

    private static Identifier[] buildTextures() {
        Identifier[] textures = new Identifier[Gene.COAT.alleleCount()];
        for (Allele coat : Gene.COAT.alleles()) {
            textures[coat.id()] = Identifier.fromNamespaceAndPath(
                    BunnidogsMod.MODID, "textures/entity/bunnidog_" + coat.name() + ".png");
        }
        return textures;
    }

    public BunnidogRenderer(EntityRendererProvider.Context context) {
        super(context, new BunnidogModel(BunnidogModelLayer.sculpt(context.bakeLayer(LAYER))), 0.35F);
        this.addLayer(new BunnidogPackLayer(this,context.bakeLayer(BunnidogPackLayer.LAYER)));
    }

    @Override
    public BunnidogRenderState createRenderState() {
        return new BunnidogRenderState();
    }

    @Override
    protected int getBlockLightLevel(Bunnidog entity, BlockPos pos) {
        return Adaptations.blockLight(entity.genome(),super.getBlockLightLevel(entity,pos));
    }

    @Override
    public void extractRenderState(Bunnidog entity, BunnidogRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swelling=entity instanceof com.bunnidogs.mod.entity.Apex apex ? apex.swelling(partialTicks) : 0;
        state.redFuse=entity instanceof com.bunnidogs.mod.entity.Apex apex && apex.lastStand();
        Traits traits = entity.traits();
        state.coat = traits.coat();
        state.earScale = traits.earScale();
        state.legScale = traits.legScale();
        state.buildScale = traits.buildScale();
        state.temperament = traits.temperament();
        state.webbed = Adaptations.swimmingFeet(entity.genome());
        state.paddling = state.webbed && entity.isInWater();
        state.bundleCount = entity.acceptsCare() ? entity.carriedBundleCount() : 0;
        state.downed = entity.combatState()==3;
        state.alerted = entity.combatState()==6;
        state.bite = entity.getAttackAnim(partialTicks);
        state.fertilityMark = com.bunnidogs.mod.genetics.FertilityMark.of(entity.genome());
    }

    @Override protected void scale(BunnidogRenderState state,com.mojang.blaze3d.vertex.PoseStack pose) {
        float swelling=state.swelling;
        float wobble=1+net.minecraft.util.Mth.sin(swelling*100)*swelling*.01F;
        float swell=swelling*swelling*swelling*swelling;
        pose.scale((1+swell*.4F)*wobble,(1+swell*.1F)/wobble,(1+swell*.4F)*wobble);
    }
    @Override protected float getWhiteOverlayProgress(BunnidogRenderState state) {
        return state.redFuse || (int)(state.swelling*10)%2==0 ? 0 : Math.clamp(state.swelling,.5F,1F);
    }
    @Override protected int getModelTint(BunnidogRenderState state) {
        return state.redFuse && (int)(state.swelling*10)%2!=0 ? 0xFFFF3030 : -1;
    }
    @Override
    public Identifier getTextureLocation(BunnidogRenderState state) {
        return TEXTURES[state.coat];
    }
}
