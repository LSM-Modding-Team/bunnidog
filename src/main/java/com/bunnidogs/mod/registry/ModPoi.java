package com.bunnidogs.mod.registry;

import java.util.Set;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Home;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The points of interest of the mod (H1, docs/HOME_SPECS.md). A point of interest is what villagers use for beds and job sites: the
 * game keeps an index of the blocks of that type per chunk and a number of tickets on each, so a mob can ask for "a free one near me"
 * and claim it. The Nest is ours; the claiming itself is H2.
 */
public final class ModPoi {
    private ModPoi() {}

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, BunnidogsMod.MODID);

    public static final DeferredHolder<PoiType,PoiType> WARREN_WARD=POI_TYPES.register("warren_ward",()->{
        var states=new java.util.HashSet<>(ModBlocks.WARREN_WARD.get().getStateDefinition().getPossibleStates());
        states.addAll(ModBlocks.WARREN_ANCHOR.get().getStateDefinition().getPossibleStates());
        return new PoiType(states,1,1);
    });
    /** Every state of the Nest block, {@link Home#NEST_TICKETS} ticket(s), {@link Home#NEST_RANGE} block of valid range. */
    public static final DeferredHolder<PoiType, PoiType> NEST = POI_TYPES.register("nest",
            () -> new PoiType(Set.copyOf(ModBlocks.NEST.get().getStateDefinition().getPossibleStates()), Home.NEST_TICKETS, Home.NEST_RANGE));
    public static final DeferredHolder<PoiType, PoiType> FEEDING_TROUGH = POI_TYPES.register("feeding_trough",
            () -> new PoiType(Set.copyOf(ModBlocks.FEEDING_TROUGH.get().getStateDefinition().getPossibleStates()),1,1));
    /** A villager's work ticket is independent of the two animals' real Nest claims. */
    public static final DeferredHolder<PoiType, PoiType> BREEDING_PEN = POI_TYPES.register("breeding_pen",
            () -> new PoiType(Set.copyOf(ModBlocks.BREEDING_PEN.get().getStateDefinition().getPossibleStates()),1,1));
}
