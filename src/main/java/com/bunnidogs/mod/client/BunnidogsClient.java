package com.bunnidogs.mod.client;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.client.model.BunnidogModelLayer;
import com.bunnidogs.mod.client.renderer.BunnidogRenderer;
import com.bunnidogs.mod.client.renderer.TrophyPedestalRenderer;
import com.bunnidogs.mod.client.renderer.GeneticsTableRenderer;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.Identifier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

/** Client-only registration (models + renderers). Never loaded on a dedicated server. */
@EventBusSubscriber(modid = BunnidogsMod.MODID, value = Dist.CLIENT)
public final class BunnidogsClient {
    private BunnidogsClient() {}
    @SubscribeEvent public static void itemProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath("bunnidogs","seed_quality"),SeedQualityProperty.CODEC);
    }
    public static final ModelLayerLocation GUAVA_BOAT_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "boat/guava"), "main");
    public static final ModelLayerLocation GUAVA_CHEST_BOAT_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "chest_boat/guava"), "main");

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BunnidogRenderer.LAYER, BunnidogModelLayer::createBodyLayer);
        event.registerLayerDefinition(GUAVA_BOAT_LAYER, BoatModel::createBoatModel);
        event.registerLayerDefinition(GUAVA_CHEST_BOAT_LAYER, BoatModel::createChestBoatModel);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BUNNIDOG.get(), BunnidogRenderer::new);
        event.registerEntityRenderer(ModEntities.APEX.get(), BunnidogRenderer::new);
        event.registerEntityRenderer(ModEntities.GUAVA_BOAT.get(), context -> new BoatRenderer(context, GUAVA_BOAT_LAYER));
        event.registerEntityRenderer(ModEntities.GUAVA_CHEST_BOAT.get(), context -> new BoatRenderer(context, GUAVA_CHEST_BOAT_LAYER));
        event.registerBlockEntityRenderer(ModBlocks.TROPHY_PEDESTAL_BE.get(), TrophyPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.GENETICS_TABLE_BE.get(), GeneticsTableRenderer::new);
    }
}
