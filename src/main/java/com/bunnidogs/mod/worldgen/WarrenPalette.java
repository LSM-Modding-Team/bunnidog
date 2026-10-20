package com.bunnidogs.mod.worldgen;

import java.util.EnumMap;
import java.util.Map;

import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.HouseShape;
import com.bunnidogs.mod.registry.ModBlocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a settlement is built of, by the lineage of the biome it stands in (Phase 6f): earth mounds in the meadow, sandstone in the
 * desert, spruce and snow in the tundra, mud bricks and moss in the swamp, stone on the mountains, nether bricks in the Nether,
 * end stone bricks and purpur on the End islands. The SHAPES (houses, landmarks, paths) are the same everywhere; only the blocks change.
 * Every settlement uses ONE lineage for all its pieces (the one at its centre), so a village never mixes styles. Each style is one
 * call of {@link #put} with all {@link Role}s, so the compiler checks that none is missing.
 */
public final class WarrenPalette {
    private WarrenPalette() {}

    /** What a block is FOR. */
    public enum Role {
        /** The ring of a house. */ WALL,
        /** The roof and the little hill of a house. */ ROOF,
        /** What fills the low side of a slope under a house or landmark. */ FILL,
        /** One block of footpath. */ PATH,
        /** Well ring corners / plaza paving, first and second block. */ RING_A, RING_B,
        /** The haystack. */ HAY,
        /** The ground of the farm plot and what grows on it (three crops). */ SOIL, SPROUT_A, SPROUT_B, SPROUT_C,
        /** V1a: guava wood, the same in EVERY lineage (the common thread of all villages): a log, a plank and a plank slab. Not in the table below. */
        BEAM, PLANK, PLANK_SLAB,
        /** V1b: the posts (well, stall, lamps) and the roofs (slabs) of the landmarks are guava wood too, in every lineage. */
        FENCE, SLAB
    }

    private static final Map<Lineage, Block[]> TABLE = new EnumMap<>(Lineage.class);

    static {
        //   lineage            wall                  roof                  fill                  path                  ring A                ring B                hay                   soil                  sprouts
        put(Lineage.MEADOW,   Blocks.COARSE_DIRT,   Blocks.GRASS_BLOCK,   Blocks.DIRT,          Blocks.DIRT_PATH,     Blocks.COBBLESTONE,   Blocks.MOSSY_COBBLESTONE, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.TUNDRA,   Blocks.SPRUCE_PLANKS, Blocks.SNOW_BLOCK,    Blocks.DIRT,          Blocks.COBBLESTONE,   Blocks.STONE_BRICKS,  Blocks.COBBLESTONE, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.DESERT,   Blocks.SANDSTONE,     Blocks.CUT_SANDSTONE, Blocks.SANDSTONE,     Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE,  Blocks.CUT_SANDSTONE, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.SWAMP,    Blocks.MUD_BRICKS,    Blocks.MOSS_BLOCK,    Blocks.MUD_BRICKS,    Blocks.PACKED_MUD,    Blocks.MUD_BRICKS,    Blocks.MOSSY_COBBLESTONE, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.CAVE,     Blocks.COBBLESTONE,   Blocks.MOSS_BLOCK,    Blocks.COBBLESTONE,   Blocks.COBBLESTONE,   Blocks.COBBLESTONE,   Blocks.MOSSY_COBBLESTONE, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.MOUNTAIN, Blocks.STONE_BRICKS,  Blocks.COBBLESTONE,   Blocks.COBBLESTONE,   Blocks.COBBLESTONE,   Blocks.COBBLESTONE,   Blocks.STONE_BRICKS, Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
        put(Lineage.NETHER,   Blocks.NETHER_BRICKS, Blocks.BLACKSTONE,    Blocks.NETHER_BRICKS, Blocks.BLACKSTONE,    Blocks.NETHER_BRICKS, Blocks.BLACKSTONE, Blocks.NETHER_WART_BLOCK, Blocks.SOUL_SOIL, Blocks.NETHER_WART, Blocks.NETHER_WART, Blocks.NETHER_WART);
        put(Lineage.END,      Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICKS, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.MYCELIUM, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM);
        put(Lineage.ISLAND,   Blocks.SANDSTONE, Blocks.MOSSY_COBBLESTONE, Blocks.DIRT, Blocks.GRAVEL, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE,
                Blocks.HAY_BLOCK, Blocks.FARMLAND, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);
    }

    private static void put(Lineage lineage, Block wall, Block roof, Block fill, Block path, Block ringA, Block ringB, Block hay,
            Block soil, Block sproutA, Block sproutB, Block sproutC) {
        TABLE.put(lineage, new Block[] {wall, roof, fill, path, ringA, ringB, hay, soil, sproutA, sproutB, sproutC});
    }

    /** The block for a role in the style of this lineage. */
    public static BlockState get(Lineage lineage, Role role) {
        switch (role) {
            case BEAM:
                return ModBlocks.GUAVA_LOG.get().defaultBlockState();
            case PLANK:
                return ModBlocks.GUAVA_PLANKS.get().defaultBlockState();
            case PLANK_SLAB, SLAB:
                return ModBlocks.GUAVA_SLAB.get().defaultBlockState();
            case FENCE:
                return ModBlocks.GUAVA_FENCE.get().defaultBlockState();
            default:
                return TABLE.get(lineage)[role.ordinal()].defaultBlockState();
        }
    }

    /** The meadow keeps its footpaths of vanilla dirt path on soil; every other style paves any solid ground. */
    public static boolean pathsOnSoilOnly(Lineage lineage) {
        return lineage == Lineage.MEADOW;
    }

    /** Earth mounds in the meadow, the tundra, the swamp and the caves; flat-roofed towers with windows in the desert, the mountains, the Nether and the End. */
    public static HouseShape.Form houseForm(Lineage lineage) {
        return switch (lineage) {
            case MEADOW, TUNDRA, SWAMP, CAVE, ISLAND -> HouseShape.Form.MOUND;
            case DESERT, MOUNTAIN, NETHER, END -> HouseShape.Form.TOWER;
        };
    }
}
