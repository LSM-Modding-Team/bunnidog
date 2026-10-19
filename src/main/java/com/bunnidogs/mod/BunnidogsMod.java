package com.bunnidogs.mod;

import com.bunnidogs.mod.network.ModNetwork;
import com.bunnidogs.mod.entity.ResidentBellCalls;
import com.bunnidogs.mod.entity.CoatCamouflage;
import com.bunnidogs.mod.block.TroughInteraction;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModFeatures;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModDispensers;
import com.bunnidogs.mod.registry.ModPoi;
import com.bunnidogs.mod.registry.ModStructures;
import com.bunnidogs.mod.registry.ModVillagers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;

/**
 * Mod entry point (runs on both client and server).
 * Keep this class thin: it only wires registries onto the mod event bus.
 * Client-only code lives in {@link com.bunnidogs.mod.client.BunnidogsClient}.
 */
@Mod(BunnidogsMod.MODID)
public class BunnidogsMod {
    public static final String MODID = "bunnidogs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BunnidogsMod(IEventBus modEventBus) {
        ModEntities.ENTITIES.register(modEventBus);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModEntities::registerSpawnPlacements);
        ModBlocks.BLOCKS.register(modEventBus);
        ModPoi.POI_TYPES.register(modEventBus);
        ModVillagers.PROFESSIONS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.PIECE_TYPES.register(modEventBus);
        ModStructures.PROCESSORS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(ModBlocks::addSignBlocks);
        modEventBus.addListener(ModBlocks::registerCapabilities);
        ModItems.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTabs);
        modEventBus.addListener(ModNetwork::register);
        modEventBus.addListener(ModDispensers::setup);
        NeoForge.EVENT_BUS.addListener(ResidentBellCalls::onGameEvent);
        NeoForge.EVENT_BUS.addListener(CoatCamouflage::onGameEvent);
        NeoForge.EVENT_BUS.addListener(TroughInteraction::onRightClick);
    }
}
