package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.BunnidogsMod;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.registries.*;

/** Short native leases retain the known fight, without loading a new outer ring or persisting abandoned tickets. */
public final class CrownTickets {
    public static final DeferredRegister<TicketType> TYPES=DeferredRegister.create(Registries.TICKET_TYPE,BunnidogsMod.MODID);
    // TicketType is a record: distinct timeouts keep separately registered wing leases unequal.
    private static final String[] ROLES={"glass","brood","heart","throne"};
    private static final List<DeferredHolder<TicketType,TicketType>> LOAD=java.util.stream.IntStream.range(0,4)
            .mapToObj(i->TYPES.register("crown_"+ROLES[i],()->new TicketType(60+i,TicketType.FLAG_LOADING|TicketType.FLAG_KEEP_DIMENSION_ACTIVE))).toList();
    public static final DeferredHolder<TicketType,TicketType> SIMULATION=TYPES.register("crown_simulation",()->new TicketType(60,TicketType.FLAG_SIMULATION|TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    public static TicketType loading(int wing) {return LOAD.get(Math.clamp(wing,0,3)).get();}
    private static int radius(CrownSealBlockEntity seal) {return seal.wing()==3 ? 43 : 19;}
    private static int simulationRadius(CrownSealBlockEntity seal) {return seal.wing()==3 ? 5 : 4;}
    public static void hold(CrownSealBlockEntity seal) {
        if(!(seal.getLevel() instanceof ServerLevel level))return;
        var center=BlockPos.containing(seal.encounterCenter());int r=radius(seal);var source=level.getChunkSource();
        for(int x=(center.getX()-r)>>4;x<=((center.getX()+r)>>4);x++)for(int z=(center.getZ()-r)>>4;z<=((center.getZ()+r)>>4);z++)
            if(source.hasChunk(x,z))source.addTicketWithRadius(loading(seal.wing()),new ChunkPos(x,z),0);
        // The seal can straddle the footprint's last chunk; retain its actual loaded chunk explicitly.
        var home=ChunkPos.containing(seal.getBlockPos());if(source.hasChunk(home.x(),home.z()))source.addTicketWithRadius(loading(seal.wing()),home,0);
        // Simulation and loading are separate native graphs. This ticket cannot generate an outer ring.
        source.addTicketWithRadius(SIMULATION.get(),ChunkPos.containing(center),simulationRadius(seal));
    }
    public static void release(CrownSealBlockEntity seal) {
        if(!(seal.getLevel() instanceof ServerLevel level))return;
        var center=BlockPos.containing(seal.encounterCenter());int r=radius(seal);var source=level.getChunkSource();
        for(int x=(center.getX()-r)>>4;x<=((center.getX()+r)>>4);x++)for(int z=(center.getZ()-r)>>4;z<=((center.getZ()+r)>>4);z++)source.removeTicketWithRadius(loading(seal.wing()),new ChunkPos(x,z),0);
        source.removeTicketWithRadius(loading(seal.wing()),ChunkPos.containing(seal.getBlockPos()),0);
        source.removeTicketWithRadius(SIMULATION.get(),ChunkPos.containing(center),simulationRadius(seal));
    }
    /** On reload only, a saved nearby actor may recover its one known controller chunk for up to three seconds. */
    public static boolean restoreController(Entity actor,ServerLevel level,BlockPos home) {
        if(actor.tickCount>=60 || home.distToCenterSqr(actor.position())>128*128 || !level.getWorldBorder().isWithinBounds(home) || level.hasChunkAt(home))return false;
        int wing=actor.getPersistentData().getIntOr("CrownWing",3);var source=level.getChunkSource();var chunk=ChunkPos.containing(home);
        source.addTicketWithRadius(loading(wing),chunk,0);source.addTicketWithRadius(SIMULATION.get(),chunk,1);return true;
    }
    private CrownTickets() {}
}
