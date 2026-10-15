package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.worldgen.WarrenPiece;
import com.bunnidogs.mod.worldgen.WarrenStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The structure type and piece type of the bunnidog settlement (Phase 6d). Where it generates is data (worldgen/structure, structure_set). */
public final class ModStructures {
    private ModStructures() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, BunnidogsMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, BunnidogsMod.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<WarrenStructure>> WARREN_VILLAGE =
            STRUCTURE_TYPES.register("warren_village", () -> () -> WarrenStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> WARREN_PIECE =
            PIECE_TYPES.register("warren_piece", () -> (StructurePieceType.ContextlessType) WarrenPiece::new);
}
