package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.worldgen.WarrenPiece;
import com.bunnidogs.mod.worldgen.WarrenStructure;
import com.bunnidogs.mod.worldgen.BreedingBarnStructure;
import com.bunnidogs.mod.worldgen.BreederVenueStructure;
import com.bunnidogs.mod.worldgen.IslandRefugeStructure;
import com.bunnidogs.mod.worldgen.IslandRefugePiece;
import com.bunnidogs.mod.worldgen.BarnPreservation;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The structure type and piece type of the bunnidog settlement (Phase 6d). Where it generates is data (worldgen/structure, structure_set). */
public final class ModStructures {
    private ModStructures() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, BunnidogsMod.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, BunnidogsMod.MODID);
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR,BunnidogsMod.MODID);
    public static final DeferredHolder<MapCodec<? extends StructureProcessor>,MapCodec<BarnPreservation>> BARN_PRESERVATION =
            PROCESSORS.register("barn_preservation",() -> BarnPreservation.CODEC);

    public static final DeferredHolder<StructureType<?>,StructureType<com.bunnidogs.mod.crown.CrownWarrenStructure>> CROWN_WARREN=STRUCTURE_TYPES.register("crown_warren",()->()->com.bunnidogs.mod.crown.CrownWarrenStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> CROWN_WARREN_PIECE=PIECE_TYPES.register("crown_warren_piece",()->(StructurePieceType.ContextlessType)com.bunnidogs.mod.crown.CrownWarrenPiece::new);
    public static final DeferredHolder<StructureType<?>,StructureType<com.bunnidogs.mod.realm.GreatRuinStructure>> GREAT_RUIN=
            STRUCTURE_TYPES.register("great_ruin",()->()->com.bunnidogs.mod.realm.GreatRuinStructure.CODEC);
    public static final DeferredHolder<StructurePieceType,StructurePieceType> GREAT_RUIN_PIECE=
            PIECE_TYPES.register("great_ruin_piece",()->(StructurePieceType.ContextlessType)com.bunnidogs.mod.realm.GreatRuinPiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<WarrenStructure>> WARREN_VILLAGE =
            STRUCTURE_TYPES.register("warren_village", () -> () -> WarrenStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<BreedingBarnStructure>> BREEDING_BARN =
            STRUCTURE_TYPES.register("breeding_barn", () -> () -> BreedingBarnStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<BreederVenueStructure>> BREEDER_VENUE =
            STRUCTURE_TYPES.register("breeder_venue", () -> () -> BreederVenueStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<IslandRefugeStructure>> ISLAND_REFUGE =
            STRUCTURE_TYPES.register("island_refuge", () -> () -> IslandRefugeStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> WARREN_PIECE =
            PIECE_TYPES.register("warren_piece", () -> (StructurePieceType.ContextlessType) WarrenPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> ISLAND_PIECE =
            PIECE_TYPES.register("island_piece", () -> (StructurePieceType.ContextlessType) IslandRefugePiece::new);
}
