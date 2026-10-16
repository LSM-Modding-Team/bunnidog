package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.block.GeneticsTableBlock;
import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.block.JudgingStandBlock;
import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.block.GuavaWoodBlocks;
import com.bunnidogs.mod.block.GuavaSaplingBlock;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.block.TrophyPedestalBlock;
import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The blocks of the mod (Phase 4d): the planted seed, {@link SeedCropBlock}, and the block entity that remembers which
 * {@link com.bunnidogs.mod.genetics.Seed} was planted in it. There is NO block item: the crop is planted by right-clicking
 * farmland with a seed ({@link com.bunnidogs.mod.item.SeedItem#useOn}). No loot table either: what a broken crop gives is
 * decided in {@link com.bunnidogs.mod.block.SeedCropHarvest}.
 *
 * <p>Phase 4g adds the two blocks of the guava tree: the hanging fruit and the sapling (this one HAS a block item, in {@link ModItems}).
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BunnidogsMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BunnidogsMod.MODID);

    /** Namespaced so the native sign editors resolve our GUI textures; interaction sounds remain oak's. */
    public static final WoodType GUAVA_WOOD_TYPE = WoodType.register(new WoodType(BunnidogsMod.MODID + ":guava", BlockSetType.OAK));

    /** A planted seed: grows through 8 ages like wheat, breaks instantly, has no collision (walk through it). */
    public static final DeferredBlock<SeedCropBlock> SEED_CROP = BLOCKS.registerBlock("seed_crop", SeedCropBlock::new,
            props -> props.noCollision().randomTicks().instabreak().noOcclusion().sound(SoundType.CROP).noLootTable());

    /** A guava hanging under the leaves of a guava tree (Phase 4g): 3 ages, right-click when ripe. Drops through data/bunnidogs/loot_table/blocks/guava_fruit.json. */
    public static final DeferredBlock<GuavaFruitBlock> GUAVA_FRUIT = BLOCKS.registerBlock("guava_fruit", GuavaFruitBlock::new,
            props -> props.noCollision().randomTicks().instabreak().noOcclusion().sound(SoundType.CROP));

    /** The guava sapling (Phase 4g): grows into a guava tree. Its item is registered in {@link ModItems}. */
    public static final DeferredBlock<GuavaSaplingBlock> GUAVA_SAPLING = BLOCKS.registerBlock("guava_sapling", GuavaSaplingBlock::new,
            props -> props.noCollision().randomTicks().instabreak().noOcclusion().sound(SoundType.GRASS));

    /**
     * Guava wood (Phase 7a). The log is a vanilla pillar block; its block tag {@code minecraft:logs} is what makes leaves count their
     * distance to it. The leaves are vanilla's leaf behaviour (they DECAY when no log is within 6 blocks; 0.0F = no falling-leaf particles),
     * pre-coloured by the texture (no biome tint). Their loot is data/bunnidogs/loot_table/blocks/guava_leaves.json.
     */
    public static final DeferredBlock<RotatedPillarBlock> GUAVA_LOG = BLOCKS.registerBlock("guava_log", RotatedPillarBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD));

    /**
     * Guava wood, part 2 (W1, docs/FIXES_PLAN.md section 4): planks, the stripped log, and the two "bark on all sides" forms (wood, stripped wood).
     * Same classes and numbers as vanilla's oak set (planks 2.0 / 3.0, logs 2.0), all in the axe-mineable tag. Loot: data/bunnidogs/loot_table/blocks/*.json
     * (each drops itself). The planks join {@code minecraft:planks} and the log forms join {@code minecraft:logs} and {@code logs_that_burn}
     * (data only), which is what makes every "any planks" recipe, fuel and leaf-distance rule accept them.
     */
    public static final DeferredBlock<Block> GUAVA_PLANKS = BLOCKS.registerBlock("guava_planks", Block::new,
            props -> props.strength(2.0F, 3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GUAVA_LOG = BLOCKS.registerBlock("stripped_guava_log", RotatedPillarBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<RotatedPillarBlock> GUAVA_WOOD = BLOCKS.registerBlock("guava_wood", RotatedPillarBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_GUAVA_WOOD = BLOCKS.registerBlock("stripped_guava_wood", RotatedPillarBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD));

    /**
     * The Nest (H1, docs/HOME_SPECS.md): a woven straw nest on a guava-plank base. A bunnidog claims a free one as its home, like a
     * villager a bed: its block states are the point-of-interest type {@link ModPoi#NEST}. A plain full block (no shape, no block
     * entity), axe-mineable, drops itself.
     */
    public static final DeferredBlock<NestBlock> NEST = BLOCKS.registerBlock("nest", NestBlock::new,
            props -> props.strength(0.8F).sound(SoundType.GRASS));
    public static final DeferredBlock<JudgingStandBlock> JUDGING_STAND = BLOCKS.registerBlock("judging_stand", JudgingStandBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD).noOcclusion());

    /**
     * Guava wood, part 3 (W2): stairs, slab and fence in the planks' texture, vanilla classes, planks' numbers. Their files come from
     * {@code art/gen_wood_parts.py}; the tags {@code wooden_stairs / wooden_slabs / wooden_fences / fences} give them fuel, the fence joining
     * and the stairs / slabs families. The stairs copy the planks' default state, so they are declared AFTER the planks (the factory runs
     * at registry time, in declaration order). The fence gate waits for W3 (it needs a WoodType).
     */
    public static final DeferredBlock<StairBlock> GUAVA_STAIRS = BLOCKS.registerBlock("guava_stairs",
            props -> new StairBlock(GUAVA_PLANKS.get().defaultBlockState(), props),
            props -> props.strength(2.0F, 3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<SlabBlock> GUAVA_SLAB = BLOCKS.registerBlock("guava_slab", SlabBlock::new,
            props -> props.strength(2.0F, 3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<FenceBlock> GUAVA_FENCE = BLOCKS.registerBlock("guava_fence", FenceBlock::new,
            props -> props.strength(2.0F, 3.0F).sound(SoundType.WOOD));

    public static final DeferredBlock<GuavaWoodBlocks.Door> GUAVA_DOOR = BLOCKS.registerBlock("guava_door", GuavaWoodBlocks.Door::new,
            props -> props.strength(3.0F).noOcclusion().ignitedByLava());
    public static final DeferredBlock<GuavaWoodBlocks.Trapdoor> GUAVA_TRAPDOOR = BLOCKS.registerBlock("guava_trapdoor", GuavaWoodBlocks.Trapdoor::new,
            props -> props.strength(3.0F).noOcclusion().ignitedByLava());
    public static final DeferredBlock<GuavaWoodBlocks.Button> GUAVA_BUTTON = BLOCKS.registerBlock("guava_button", GuavaWoodBlocks.Button::new,
            props -> props.strength(0.5F).noCollision().ignitedByLava());
    public static final DeferredBlock<GuavaWoodBlocks.PressurePlate> GUAVA_PRESSURE_PLATE = BLOCKS.registerBlock("guava_pressure_plate", GuavaWoodBlocks.PressurePlate::new,
            props -> props.strength(0.5F).noCollision().ignitedByLava());
    public static final DeferredBlock<FenceGateBlock> GUAVA_FENCE_GATE = BLOCKS.registerBlock("guava_fence_gate",
            props -> new FenceGateBlock(GUAVA_WOOD_TYPE,props), props -> props.strength(2.0F,3.0F).ignitedByLava());

    /** W4a: vanilla sign classes, ticking, text packets and block entities on both sides. */
    public static final DeferredBlock<StandingSignBlock> GUAVA_SIGN = BLOCKS.registerBlock("guava_sign",
            props -> new StandingSignBlock(GUAVA_WOOD_TYPE,props),
            props -> props.forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava());
    public static final DeferredBlock<WallSignBlock> GUAVA_WALL_SIGN = BLOCKS.registerBlock("guava_wall_sign",
            props -> new WallSignBlock(GUAVA_WOOD_TYPE,props),
            props -> props.overrideLootTable(GUAVA_SIGN.get().getLootTable()).overrideDescription(GUAVA_SIGN.get().getDescriptionId())
                    .forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava());
    public static final DeferredBlock<CeilingHangingSignBlock> GUAVA_HANGING_SIGN = BLOCKS.registerBlock("guava_hanging_sign",
            props -> new CeilingHangingSignBlock(GUAVA_WOOD_TYPE,props),
            props -> props.forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava());
    public static final DeferredBlock<WallHangingSignBlock> GUAVA_WALL_HANGING_SIGN = BLOCKS.registerBlock("guava_wall_hanging_sign",
            props -> new WallHangingSignBlock(GUAVA_WOOD_TYPE,props),
            props -> props.overrideLootTable(GUAVA_HANGING_SIGN.get().getLootTable()).overrideDescription(GUAVA_HANGING_SIGN.get().getDescriptionId())
                    .forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0F).ignitedByLava());

    public static void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityTypes.SIGN, GUAVA_SIGN.get(), GUAVA_WALL_SIGN.get());
        event.modify(BlockEntityTypes.HANGING_SIGN, GUAVA_HANGING_SIGN.get(), GUAVA_WALL_HANGING_SIGN.get());
    }

    public static final DeferredBlock<TintedParticleLeavesBlock> GUAVA_LEAVES = BLOCKS.registerBlock("guava_leaves",
            props -> new TintedParticleLeavesBlock(0.0F, props),
            props -> props.strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion());

    /** The Trophy Pedestal (drops D8a): a display stand (D8b: it holds one item, see {@link TrophyPedestalBlockEntity}); its item is registered in {@link ModItems}. Loot: data/bunnidogs/loot_table/blocks/trophy_pedestal.json. */
    public static final DeferredBlock<TrophyPedestalBlock> TROPHY_PEDESTAL = BLOCKS.registerBlock("trophy_pedestal", TrophyPedestalBlock::new,
            props -> props.strength(2.0F).sound(SoundType.STONE).noOcclusion());

    /** The Genetics Table (T1, docs/TABLE_SPECS.md): a lab bench that holds and reads one Tissue Sample ({@link GeneticsTableBlockEntity}); its item is registered in {@link ModItems}. Loot: data/bunnidogs/loot_table/blocks/genetics_table.json. */
    public static final DeferredBlock<GeneticsTableBlock> GENETICS_TABLE = BLOCKS.registerBlock("genetics_table", GeneticsTableBlock::new,
            props -> props.strength(2.5F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeedCropBlockEntity>> SEED_CROP_BE =
            BLOCK_ENTITIES.register("seed_crop", () -> new BlockEntityType<>(SeedCropBlockEntity::new, SEED_CROP.get()));

    /** What stands on a Trophy Pedestal (drops D8b). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyPedestalBlockEntity>> TROPHY_PEDESTAL_BE =
            BLOCK_ENTITIES.register("trophy_pedestal", () -> new BlockEntityType<>(TrophyPedestalBlockEntity::new, TROPHY_PEDESTAL.get()));

    /** The sample on a Genetics Table (T1). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneticsTableBlockEntity>> GENETICS_TABLE_BE =
            BLOCK_ENTITIES.register("genetics_table", () -> new BlockEntityType<>(GeneticsTableBlockEntity::new, GENETICS_TABLE.get()));
}
