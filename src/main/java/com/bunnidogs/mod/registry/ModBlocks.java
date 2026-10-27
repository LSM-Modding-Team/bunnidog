package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.block.GeneticsTableBlock;
import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.block.JudgingStandBlock;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.block.GuavaWoodBlocks;
import com.bunnidogs.mod.block.GuavaSaplingBlock;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.block.TrophyPedestalBlock;
import com.bunnidogs.mod.block.ApexCrucibleBlock;
import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.bunnidogs.mod.block.JuicerBlock;
import com.bunnidogs.mod.block.JuicerBlockEntity;
import com.bunnidogs.mod.block.BreedingPenBlock;
import com.bunnidogs.mod.block.BreedingPenBlockEntity;
import com.bunnidogs.mod.block.PeltRugBlock;
import com.bunnidogs.mod.block.SpecimenJarBlock;
import com.bunnidogs.mod.block.SpecimenJarBlockEntity;
import com.bunnidogs.mod.block.PedigreeComparatorBlock;
import com.bunnidogs.mod.block.PedigreeComparatorBlockEntity;
import com.bunnidogs.mod.genetics.Gene;

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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
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

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK,FEEDING_TROUGH_BE.get(),(trough,side) -> trough.automation());
        event.registerBlockEntity(Capabilities.Item.BLOCK,NURSERY_CRADLE_BE.get(),(cradle,side) -> cradle.automation());
        event.registerBlockEntity(Capabilities.Item.BLOCK,GENETICS_TABLE_BE.get(),(table,side) -> table.automation());
        event.registerBlockEntity(Capabilities.Item.BLOCK,JUICER_BE.get(),(juicer,side) -> juicer.automation(side));
        event.registerBlockEntity(Capabilities.Item.BLOCK,BREEDING_PEN_BE.get(),(pen,side) -> pen.automation(side));
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BunnidogsMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BunnidogsMod.MODID);
    public static final java.util.Map<String,DeferredBlock<PeltRugBlock>> PELT_RUGS = registerPeltRugs();
    private static java.util.Map<String,DeferredBlock<PeltRugBlock>> registerPeltRugs() {
        var rugs = new java.util.LinkedHashMap<String,DeferredBlock<PeltRugBlock>>();
        for (var coat : Gene.COAT.alleles()) rugs.put(coat.name(),BLOCKS.registerBlock(coat.name()+"_pelt_rug",PeltRugBlock::new,
                props -> props.strength(.1F).sound(SoundType.WOOL).noOcclusion().ignitedByLava()));
        return java.util.Collections.unmodifiableMap(rugs);
    }

    public static final DeferredBlock<com.bunnidogs.mod.society.HearthTreeBlock> HEARTH_TREE=BLOCKS.registerBlock("hearth_tree",com.bunnidogs.mod.society.HearthTreeBlock::new,p->p.strength(3).sound(SoundType.WOOD).noOcclusion().lightLevel(state->state.getValue(com.bunnidogs.mod.society.HearthTreeBlock.STAGE)*3));
    public static final DeferredBlock<com.bunnidogs.mod.society.CoppiceBlock> GUAVA_COPPICE=BLOCKS.registerBlock("guava_coppice",com.bunnidogs.mod.society.CoppiceBlock::new,p->p.strength(.8F).randomTicks().noOcclusion().noCollision().sound(SoundType.AZALEA));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.bunnidogs.mod.society.HearthTreeBlockEntity>> HEARTH_TREE_BE=BLOCK_ENTITIES.register("hearth_tree",()->new BlockEntityType<>(com.bunnidogs.mod.society.HearthTreeBlockEntity::new,HEARTH_TREE.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.bunnidogs.mod.society.CrucibleHeartBlockEntity>> CRUCIBLE_HEART_BE=BLOCK_ENTITIES.register("crucible_heart",()->new BlockEntityType<>(com.bunnidogs.mod.society.CrucibleHeartBlockEntity::new,ModBlocks.APEX_CRUCIBLE.get()));
    public static final DeferredBlock<Block> MOONSTONE=BLOCKS.registerBlock("moonstone",Block::new,p->p.strength(4,1200).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> ASTRALITE_ORE=BLOCKS.registerBlock("astralite_ore",Block::new,p->p.strength(6,1200).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> CINDERITE_ORE=BLOCKS.registerBlock("cinderite_ore",Block::new,p->p.strength(6,1200).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<com.bunnidogs.mod.realm.WarrenAnchorBlock> WARREN_ANCHOR=BLOCKS.registerBlock("warren_anchor",com.bunnidogs.mod.realm.WarrenAnchorBlock::new,
            props->props.strength(5,3600000).lightLevel(state->state.getValue(com.bunnidogs.mod.realm.WarrenAnchorBlock.ACTIVE) ? 14 : 3).noOcclusion());
    public static final DeferredBlock<com.bunnidogs.mod.crown.CrownSealBlock> CROWN_SEAL=BLOCKS.registerBlock("crown_seal",com.bunnidogs.mod.crown.CrownSealBlock::new,p->p.strength(-1,3600000).lightLevel(s->s.getValue(com.bunnidogs.mod.crown.CrownSealBlock.STATE)==0 ? 7 : 13).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredBlock<com.bunnidogs.mod.crown.CrownGateBlock> CROWN_GATE=BLOCKS.registerBlock("crown_gate",com.bunnidogs.mod.crown.CrownGateBlock::new,p->p.strength(-1,3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.bunnidogs.mod.crown.CrownSealBlockEntity>> CROWN_SEAL_BE=BLOCK_ENTITIES.register("crown_seal",()->new BlockEntityType<>(com.bunnidogs.mod.crown.CrownSealBlockEntity::new,CROWN_SEAL.get()));
    public static final DeferredBlock<com.bunnidogs.mod.realm.RootstoneBlock> ROOTSTONE=BLOCKS.registerBlock("rootstone",com.bunnidogs.mod.realm.RootstoneBlock::new,props->props.strength(3.5F,3600000));
    public static final DeferredBlock<Block> WARREN_WARD=BLOCKS.registerSimpleBlock("warren_ward",props->props.strength(4,1200).lightLevel(state->14));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.bunnidogs.mod.realm.WarrenAnchorBlockEntity>> WARREN_ANCHOR_BE=
            BLOCK_ENTITIES.register("warren_anchor",()->new BlockEntityType<>(com.bunnidogs.mod.realm.WarrenAnchorBlockEntity::new,WARREN_ANCHOR.get()));

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
    public static final DeferredBlock<com.bunnidogs.mod.block.CommissionBoardBlock> COMMISSION_BOARD = BLOCKS.registerBlock("commission_board",
            com.bunnidogs.mod.block.CommissionBoardBlock::new,props -> props.strength(2).sound(SoundType.WOOD).noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredBlock<FeedingTroughBlock> FEEDING_TROUGH = BLOCKS.registerBlock("feeding_trough",FeedingTroughBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<NurseryCradleBlock> NURSERY_CRADLE = BLOCKS.registerBlock("nursery_cradle",NurseryCradleBlock::new,
            props -> props.strength(2.5F).sound(SoundType.WOOD).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));

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

    public static final DeferredBlock<ApexCrucibleBlock> APEX_CRUCIBLE = BLOCKS.registerBlock("apex_crucible", ApexCrucibleBlock::new,
            props -> props.strength(5, 1200).requiresCorrectToolForDrops().sound(SoundType.STONE));

    /** The Genetics Table (T1, docs/TABLE_SPECS.md): a lab bench that holds and reads one Tissue Sample ({@link GeneticsTableBlockEntity}); its item is registered in {@link ModItems}. Loot: data/bunnidogs/loot_table/blocks/genetics_table.json. */
    public static final DeferredBlock<GeneticsTableBlock> GENETICS_TABLE = BLOCKS.registerBlock("genetics_table", GeneticsTableBlock::new,
            props -> props.strength(2.5F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<JuicerBlock> JUICER = BLOCKS.registerBlock("juicer",JuicerBlock::new,
            props -> props.strength(2.5F).sound(SoundType.WOOD).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredBlock<BreedingPenBlock> BREEDING_PEN = BLOCKS.registerBlock("breeding_pen",BreedingPenBlock::new,
            props -> props.strength(2.5F).sound(SoundType.WOOD).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreedingPenBlockEntity>> BREEDING_PEN_BE =
            BLOCK_ENTITIES.register("breeding_pen",() -> new BlockEntityType<>(BreedingPenBlockEntity::new,BREEDING_PEN.get()));
    public static final DeferredBlock<PedigreeComparatorBlock> PEDIGREE_COMPARATOR = BLOCKS.registerBlock("pedigree_comparator",PedigreeComparatorBlock::new,
            props -> props.strength(2.5F).sound(SoundType.METAL).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PedigreeComparatorBlockEntity>> PEDIGREE_COMPARATOR_BE =
            BLOCK_ENTITIES.register("pedigree_comparator",() -> new BlockEntityType<>(PedigreeComparatorBlockEntity::new,PEDIGREE_COMPARATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.bunnidogs.mod.block.CommissionBoardBlockEntity>> COMMISSION_BOARD_BE =
            BLOCK_ENTITIES.register("commission_board",() -> new BlockEntityType<>(com.bunnidogs.mod.block.CommissionBoardBlockEntity::new,COMMISSION_BOARD.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JuicerBlockEntity>> JUICER_BE =
            BLOCK_ENTITIES.register("juicer",() -> new BlockEntityType<>(JuicerBlockEntity::new,JUICER.get()));
    public static final DeferredBlock<SpecimenJarBlock> SPECIMEN_JAR = BLOCKS.registerBlock("specimen_jar",SpecimenJarBlock::new,
            props -> props.strength(.3F).sound(SoundType.GLASS).noOcclusion().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpecimenJarBlockEntity>> SPECIMEN_JAR_BE =
            BLOCK_ENTITIES.register("specimen_jar",() -> new BlockEntityType<>(SpecimenJarBlockEntity::new,SPECIMEN_JAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeedCropBlockEntity>> SEED_CROP_BE =
            BLOCK_ENTITIES.register("seed_crop", () -> new BlockEntityType<>(SeedCropBlockEntity::new, SEED_CROP.get()));

    /** What stands on a Trophy Pedestal (drops D8b). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrophyPedestalBlockEntity>> TROPHY_PEDESTAL_BE =
            BLOCK_ENTITIES.register("trophy_pedestal", () -> new BlockEntityType<>(TrophyPedestalBlockEntity::new, TROPHY_PEDESTAL.get()));

    /** The sample on a Genetics Table (T1). */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneticsTableBlockEntity>> GENETICS_TABLE_BE =
            BLOCK_ENTITIES.register("genetics_table", () -> new BlockEntityType<>(GeneticsTableBlockEntity::new, GENETICS_TABLE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FeedingTroughBlockEntity>> FEEDING_TROUGH_BE =
            BLOCK_ENTITIES.register("feeding_trough",() -> new BlockEntityType<>(FeedingTroughBlockEntity::new,FEEDING_TROUGH.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NurseryCradleBlockEntity>> NURSERY_CRADLE_BE =
            BLOCK_ENTITIES.register("nursery_cradle",() -> new BlockEntityType<>(NurseryCradleBlockEntity::new,NURSERY_CRADLE.get()));
}
