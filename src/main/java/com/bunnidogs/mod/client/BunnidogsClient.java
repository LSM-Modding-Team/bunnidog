package com.bunnidogs.mod.client;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.client.model.BunnidogModelLayer;
import com.bunnidogs.mod.client.renderer.BunnidogRenderer;
import com.bunnidogs.mod.client.renderer.TrophyPedestalRenderer;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only registration (models + renderers). Never loaded on a dedicated server. */
@EventBusSubscriber(modid = BunnidogsMod.MODID, value = Dist.CLIENT)
public final class BunnidogsClient {
    private BunnidogsClient() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BunnidogRenderer.LAYER, BunnidogModelLayer::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BUNNIDOG.get(), BunnidogRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.TROPHY_PEDESTAL_BE.get(), TrophyPedestalRenderer::new);
    }
}
