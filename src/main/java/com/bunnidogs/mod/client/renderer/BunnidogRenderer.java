package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.client.model.BunnidogModel;
import com.bunnidogs.mod.client.model.BunnidogModelLayer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Allele;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Traits;

import net.minecraft.client.model.geom.ModelLayerLocation;
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
    }

    @Override
    public BunnidogRenderState createRenderState() {
        return new BunnidogRenderState();
    }

    @Override
    public void extractRenderState(Bunnidog entity, BunnidogRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Traits traits = entity.traits();
        state.coat = traits.coat();
        state.earScale = traits.earScale();
        state.legScale = traits.legScale();
        state.buildScale = traits.buildScale();
        state.temperament = traits.temperament();
    }

    @Override
    public Identifier getTextureLocation(BunnidogRenderState state) {
        return TEXTURES[state.coat];
    }
}
