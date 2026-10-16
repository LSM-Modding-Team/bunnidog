package com.bunnidogs.mod.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Allele;
import com.bunnidogs.mod.genetics.Drops;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.item.FieldGuideItem;
import com.bunnidogs.mod.item.SyringeItem;
import com.bunnidogs.mod.item.GeneSplicerItem;
import com.bunnidogs.mod.item.EarTagItem;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.item.JuiceItem;
import com.bunnidogs.mod.item.MutagenItem;
import com.bunnidogs.mod.item.PedigreeBookItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.item.StarlitDustItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.WornEarTagItem;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All items of the mod, and the data component they carry.
 *
 * <p>Phase 4b: the guava, the food of bunnidogs (it replaces the carrot). How the plain guava is found in the world
 * (warrens, guava trees) comes in step 4g, so for now it is only in the creative "Food &amp; Drinks" tab and available
 * through {@code /give @s bunnidogs:guava 64}; the higher tiers are crafted from it (Phase 4e, below).
 *
 * <p>Phase 4e: the guava is a LADDER of eight items ({@link GuavaTier}: plain, golden, diamond, netherite, nether star,
 * beacon, dragon egg, command block), all {@link GuavaItem}s. A bunnidog remembers the tier of the guava it was fed; the seed's quality is
 * the average of the two parents' tiers. The recipes are data ({@code data/bunnidogs/recipe/}).
 *
 * <p>Phase 4c: the seed. A mating drops ONE seed holding both parents' genes ({@link Seed}); planting it comes in step
 * 4d. The payload lives in the data component {@link #SEED_DATA} as the seed's canonical TEXT (see {@link Seed#toString()}),
 * so it can be written by hand: {@code /give @s bunnidogs:seed[bunnidogs:seed_data="00-00-00-11-00-00|00-00-00-11-00-00|1.5"]}.
 * Seeds with different payloads never stack.
 *
 * <p>Phase 4f: guava juice. The plain juice and the infused one are two items of one class ({@link JuiceItem}); the second holds a
 * liquid pup in the component {@link #INFUSION_DATA}. Infusing is done with the seed ({@code SeedItem#use}), not with a recipe.
 *
 * <p>Phase 5: the Calipers ({@link CalipersItem}), the first tool; the Pedigree Book ({@link PedigreeBookItem}); the Ear Tag ({@link EarTagItem}).
 *
 * <p>What bunnidogs accept as food is DATA, not code: the item tag {@code bunnidogs:bunnidog_food}
 * (data/bunnidogs/tags/item/bunnidog_food.json). Add {@code minecraft:carrot} to it to make carrots work again.
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BunnidogsMod.MODID);
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BunnidogsMod.MODID);

    /** An "okay" food for players too: 3 hunger points, low saturation. The same for every tier (do not eat your investment). */
    private static final FoodProperties GUAVA_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();

    /** The plain guava (tier 0) and the six higher tiers of the ladder (Phase 4e, table in {@link GuavaTier}). */
    public static final DeferredItem<GuavaItem> GUAVA = registerGuava(GuavaTier.PLAIN);
    public static final DeferredItem<GuavaItem> GOLDEN_GUAVA = registerGuava(GuavaTier.GOLDEN);
    public static final DeferredItem<GuavaItem> DIAMOND_GUAVA = registerGuava(GuavaTier.DIAMOND);
    public static final DeferredItem<GuavaItem> NETHERITE_GUAVA = registerGuava(GuavaTier.NETHERITE);
    public static final DeferredItem<GuavaItem> NETHER_STAR_GUAVA = registerGuava(GuavaTier.NETHER_STAR);
    public static final DeferredItem<GuavaItem> BEACON_GUAVA = registerGuava(GuavaTier.BEACON);
    public static final DeferredItem<GuavaItem> DRAGON_EGG_GUAVA = registerGuava(GuavaTier.DRAGON_EGG);
    public static final DeferredItem<GuavaItem> COMMAND_BLOCK_GUAVA = registerGuava(GuavaTier.COMMAND_BLOCK);

    /** All guavas in ladder order (the creative tab shows them like this). */
    public static final List<DeferredItem<GuavaItem>> GUAVAS = List.of(
            GUAVA, GOLDEN_GUAVA, DIAMOND_GUAVA, NETHERITE_GUAVA, NETHER_STAR_GUAVA, BEACON_GUAVA, DRAGON_EGG_GUAVA, COMMAND_BLOCK_GUAVA);

    private static DeferredItem<GuavaItem> registerGuava(GuavaTier tier) {
        Rarity rarity = rarityOf(tier);
        return ITEMS.registerItem(tier.id(), props -> new GuavaItem(props, tier),
                props -> props.food(GUAVA_FOOD).rarity(rarity));
    }

    /** Name colour in tooltips and inventory: white, yellow, aqua (x2), then purple for the absurd tiers. */
    private static Rarity rarityOf(GuavaTier tier) {
        return switch (tier.level()) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2, 3 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    /**
     * The payload of a seed: {@link Seed#toString()}. Saved to disk as a string and sent to clients as a string, so it
     * shows in tooltips and survives {@code /data} edits. A stack without it is an empty seed (holds no genes).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SEED_DATA =
            COMPONENTS.registerComponentType("seed_data",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SPLICE_GENE =
            COMPONENTS.registerComponentType("splice_gene",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** The bunnidog seed (Phase 4c). Create stacks with {@link SeedItem#create(Seed)}, read them with {@link SeedItem#read}. */
    public static final DeferredItem<SeedItem> SEED = ITEMS.registerItem("seed", SeedItem::new, props -> props);

    /**
     * The liquid pup of an infused juice (Phase 4f): {@link com.bunnidogs.mod.genetics.Infusion#toString()} as a string, saved and synced
     * like {@link #SEED_DATA}. Write one by hand with {@code /give @s bunnidogs:infused_guava_juice[bunnidogs:infusion_data="00-00-00-11-00-00/0,0,0,0,0|4"]}.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> INFUSION_DATA =
            COMPONENTS.registerComponentType("infusion_data",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** The guava sapling (Phase 4g): a plain block item for {@link ModBlocks#GUAVA_SAPLING}; crafted from one guava (data/bunnidogs/recipe/guava_sapling.json). */
    public static final DeferredItem<BlockItem> GUAVA_SAPLING = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_SAPLING);
    /** Guava wood (Phase 7a): plain block items for the log and the leaves of the guava tree. */
    public static final DeferredItem<BlockItem> GUAVA_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_LOG);
    public static final DeferredItem<BlockItem> GUAVA_LEAVES = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_LEAVES);
    public static final DeferredItem<BlockItem> GUAVA_PLANKS = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_PLANKS);
    public static final DeferredItem<BlockItem> STRIPPED_GUAVA_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.STRIPPED_GUAVA_LOG);
    public static final DeferredItem<BlockItem> GUAVA_WOOD = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_WOOD);
    public static final DeferredItem<BlockItem> STRIPPED_GUAVA_WOOD = ITEMS.registerSimpleBlockItem(ModBlocks.STRIPPED_GUAVA_WOOD);
    public static final DeferredItem<BlockItem> GUAVA_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_STAIRS);
    public static final DeferredItem<BlockItem> GUAVA_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_SLAB);
    public static final DeferredItem<BlockItem> GUAVA_FENCE = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_FENCE);
    public static final DeferredItem<DoubleHighBlockItem> GUAVA_DOOR = ITEMS.registerItem("guava_door",
            props -> new DoubleHighBlockItem(ModBlocks.GUAVA_DOOR.get(),props));
    public static final DeferredItem<BlockItem> GUAVA_TRAPDOOR = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_TRAPDOOR);
    public static final DeferredItem<BlockItem> GUAVA_BUTTON = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_BUTTON);
    public static final DeferredItem<BlockItem> GUAVA_PRESSURE_PLATE = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_PRESSURE_PLATE);
    public static final DeferredItem<BlockItem> GUAVA_FENCE_GATE = ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_FENCE_GATE);
    public static final DeferredItem<SignItem> GUAVA_SIGN = ITEMS.registerItem("guava_sign",
            props -> new SignItem(ModBlocks.GUAVA_SIGN.get(), ModBlocks.GUAVA_WALL_SIGN.get(), props), props -> props.stacksTo(16));
    public static final DeferredItem<HangingSignItem> GUAVA_HANGING_SIGN = ITEMS.registerItem("guava_hanging_sign",
            props -> new HangingSignItem(ModBlocks.GUAVA_HANGING_SIGN.get(), ModBlocks.GUAVA_WALL_HANGING_SIGN.get(), props), props -> props.stacksTo(16));
    public static final DeferredItem<BoatItem> GUAVA_BOAT = ITEMS.registerItem("guava_boat",
            props -> new BoatItem(ModEntities.GUAVA_BOAT.get(), props), props -> props.stacksTo(1));
    public static final DeferredItem<BoatItem> GUAVA_CHEST_BOAT = ITEMS.registerItem("guava_chest_boat",
            props -> new BoatItem(ModEntities.GUAVA_CHEST_BOAT.get(), props), props -> props.stacksTo(1));
    /** The Trophy Pedestal (drops D8a). */
    public static final DeferredItem<BlockItem> TROPHY_PEDESTAL = ITEMS.registerSimpleBlockItem(ModBlocks.TROPHY_PEDESTAL);
    public static final DeferredItem<BlockItem> APEX_CRUCIBLE = ITEMS.registerSimpleBlockItem(ModBlocks.APEX_CRUCIBLE);
    public static final DeferredItem<FieldGuideItem> FIELD_GUIDE = ITEMS.registerItem("field_guide", FieldGuideItem::new,
            props -> props.stacksTo(1));
    public static final DeferredItem<com.bunnidogs.mod.item.ApexCrestItem> APEX_CREST = ITEMS.registerItem("apex_crest",
            com.bunnidogs.mod.item.ApexCrestItem::new, props -> props.stacksTo(1).rarity(Rarity.EPIC));

    /** The Genetics Table's block item (T1): crafted from 2 whiskers, a copper ingot and 5 planks (data/bunnidogs/recipe/genetics_table.json). */
    public static final DeferredItem<BlockItem> GENETICS_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.GENETICS_TABLE);
    /** The Nest (H1): a bunnidog's home. */
    public static final DeferredItem<BlockItem> NEST = ITEMS.registerSimpleBlockItem(ModBlocks.NEST);
    public static final DeferredItem<BlockItem> JUDGING_STAND = ITEMS.registerSimpleBlockItem(ModBlocks.JUDGING_STAND);
    public static final DeferredItem<BlockItem> FEEDING_TROUGH = ITEMS.registerSimpleBlockItem(ModBlocks.FEEDING_TROUGH);
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> BRONZE_RIBBON = ITEMS.registerItem("bronze_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.BRONZE), props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> SILVER_RIBBON = ITEMS.registerItem("silver_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.SILVER), props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> GOLD_RIBBON = ITEMS.registerItem("gold_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.GOLD), props -> props.stacksTo(1).rarity(Rarity.EPIC));

    /** Plain guava juice (Phase 4f): a guava and a glass bottle. Drinking it restores a little hunger; it is what a seed is infused into. */
    public static final DeferredItem<JuiceItem> GUAVA_JUICE = ITEMS.registerItem("guava_juice", JuiceItem::new, props -> props.stacksTo(16));

    /** A bottle holding a liquid pup (a seed that was infused). Make stacks with {@link JuiceItem#create}. */
    public static final DeferredItem<JuiceItem> INFUSED_JUICE = ITEMS.registerItem("infused_guava_juice", JuiceItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    /** The Calipers (Phase 5): read a bunnidog's or a seed's exact genes into the chat. Crafted from iron and copper (data/bunnidogs/recipe/calipers.json). */
    public static final DeferredItem<CalipersItem> CALIPERS = ITEMS.registerItem("calipers", CalipersItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<SyringeItem> SYRINGE = ITEMS.registerItem("syringe", SyringeItem::new,
            props -> props.durability(com.bunnidogs.mod.genetics.Sampling.TOOL_USES));
    public static final DeferredItem<GeneSplicerItem> GENE_SPLICER = ITEMS.registerItem("gene_splicer",GeneSplicerItem::new,
            props -> props.durability(com.bunnidogs.mod.genetics.Splicing.TOOL_USES).rarity(Rarity.RARE));

    /** The Ear Tag (Phase 5d): clip it on a bunnidog for a permanent registration code. Crafted from an iron nugget and paper (data/bunnidogs/recipe/ear_tag.json). */
    public static final DeferredItem<EarTagItem> EAR_TAG = ITEMS.registerItem("ear_tag", EarTagItem::new, props -> props);

    /** The log of a Pedigree Book (Phase 5c): {@link com.bunnidogs.mod.genetics.Pedigree#toString()} as a string, saved and synced like {@link #SEED_DATA}. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PEDIGREE_DATA =
            COMPONENTS.registerComponentType("pedigree_data",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** The Pedigree Book (Phase 5c): a book and a guava (data/bunnidogs/recipe/pedigree_book.json). */
    public static final DeferredItem<PedigreeBookItem> PEDIGREE_BOOK = ITEMS.registerItem("pedigree_book", PedigreeBookItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.bunnidogs.mod.item.PurebredRegistryItem> PUREBRED_REGISTRY = ITEMS.registerItem("purebred_registry",
            com.bunnidogs.mod.item.PurebredRegistryItem::new, props -> props.stacksTo(1).rarity(Rarity.RARE));

    /** Bunnidog meat (drops D1): raw is a little better than vanilla rabbit and harmless to eat; cooked (smelting, smoking, campfire, or a bunnidog that dies burning) is steak-like. */
    private static final FoodProperties RAW_FLESH_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build();
    private static final FoodProperties COOKED_FLESH_FOOD = new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build();

    /** Dropped by a bunnidog: the more Vitality and the chonkier, the more of it (genetics/Drops.java). Cook it for {@link #COOKED_BUNNIDOG_FLESH}. */
    public static final DeferredItem<Item> RAW_BUNNIDOG_FLESH = ITEMS.registerItem("raw_bunnidog_flesh", Item::new, props -> props.food(RAW_FLESH_FOOD));
    public static final DeferredItem<Item> COOKED_BUNNIDOG_FLESH = ITEMS.registerItem("cooked_bunnidog_flesh", Item::new, props -> props.food(COOKED_FLESH_FOOD));

    /**
     * The haunch (drops D5): the prize cut of an animal bred hard for Vitality (genetics/Drops.java {@code HAUNCH}, from Vitality +20 on). Prime is a
     * little better than cooked flesh; the roast (cooked in a furnace, smoker or campfire, or from a bunnidog that dies burning) is the best food of the mod.
     */
    private static final FoodProperties PRIME_HAUNCH_FOOD = new FoodProperties.Builder().nutrition(6).saturationModifier(0.4F).build();
    private static final FoodProperties ROAST_HAUNCH_FOOD = new FoodProperties.Builder().nutrition(10).saturationModifier(1.0F).build();
    public static final DeferredItem<Item> PRIME_HAUNCH = ITEMS.registerItem("prime_haunch", Item::new, props -> props.food(PRIME_HAUNCH_FOOD));
    public static final DeferredItem<Item> ROAST_HAUNCH = ITEMS.registerItem("roast_haunch", Item::new, props -> props.food(ROAST_HAUNCH_FOOD));

    /**
     * The pelts (drops D2): ONE item per coat, registered from the coat table itself ({@link Gene#COAT}), so a new coat is a new pelt with no code change here.
     * The id is {@code <coat>_pelt}; {@link Drops#peltId} builds the same id for the drop. Name colour: common coats COMMON, the wild lineage coats UNCOMMON,
     * the mutation coat (spotted) RARE. Their files come from {@code art/gen_pelts.py}; every pelt is in the tag {@code bunnidogs:pelts}, and two of any
     * craft one leather (data/bunnidogs/recipe/leather_from_bunnidog_pelts.json).
     */
    public static final Map<String, DeferredItem<Item>> PELTS = registerPelts();

    private static Map<String, DeferredItem<Item>> registerPelts() {
        Map<String, DeferredItem<Item>> pelts = new LinkedHashMap<>();
        for (Allele coat : Gene.COAT.alleles()) {
            Rarity rarity = coat.isRare() ? Rarity.RARE : coat.isWild() ? Rarity.UNCOMMON : Rarity.COMMON;
            pelts.put(coat.name(), ITEMS.registerItem(coat.name() + Drops.PELT_SUFFIX, Item::new, props -> props.rarity(rarity)));
        }
        return Collections.unmodifiableMap(pelts);
    }

    /**
     * The three body-part materials (drops D4), plain items like the pelts. Sinew comes from the Leap line, whiskers from the Senses line
     * (genetics/Drops.java {@code SINEW}, {@code WHISKER}), the fang from a feral bunnidog killed by a player (a gene signature). Their art is
     * {@code art/gen_materials.py}. Each ships with a use (docs/DROPS_SPECS.md section 8): 1 sinew -> 2 string, whisker + copper ingot + stick
     * -> a vanilla Brush, fang + stick + feather -> 4 arrows (data/bunnidogs/recipe/); the Leash and the Genetics Table keep them useful later.
     */
    public static final DeferredItem<Item> SINEW = ITEMS.registerItem("sinew", Item::new, props -> props);
    public static final DeferredItem<Item> WHISKER = ITEMS.registerItem("whisker", Item::new, props -> props);
    public static final DeferredItem<Item> FANG = ITEMS.registerItem("fang", Item::new, props -> props);

    /**
     * Mutant Marrow (drops D6b), layer 3 of the drops: an ADULT bunnidog killed by a player rolls once per rare (mutation) allele it shows,
     * 25% each (genetics/Drops.java {@code MARROW_CHANCE}). A plain item like the D4 materials; RARE because only a mutant line drops it.
     * It ships with its use (docs/DROPS_SPECS.md section 8): 1 marrow + 1 fermented spider eye + 1 glass bottle -> 1 Mutagen
     * (data/bunnidogs/recipe/mutagen.json). Art: {@code art/gen_marrow.py}.
     */
    public static final DeferredItem<Item> MUTANT_MARROW = ITEMS.registerItem("mutant_marrow", Item::new, props -> props.rarity(Rarity.RARE));

    /**
     * The Mutagen (drops D6a): applied to a SEED (seed in the main hand, Mutagen in the off hand, use the seed), it makes every pup of that
     * seed mutate with at least a 50% chance (genetics/Seed.java {@code mutagen}). Since D6b it is crafted from a Mutant Marrow (shapeless with a
     * fermented spider eye and a glass bottle); the creative Ingredients tab and {@code /give @s bunnidogs:mutagen} still work.
     */
    public static final DeferredItem<MutagenItem> MUTAGEN = ITEMS.registerItem("mutagen", MutagenItem::new,
            props -> props.stacksTo(16).rarity(Rarity.UNCOMMON));

    /**
     * The payload of a Tissue Sample (drops D7a): {@link com.bunnidogs.mod.genetics.Tissue#toString()} as a string, saved and synced like
     * {@link #SEED_DATA}. Write one by hand with {@code /give @s bunnidogs:tissue_sample[bunnidogs:tissue_data="33-33-33-33-33-33/0,0,0,0,0"]}.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TISSUE_DATA =
            COMPONENTS.registerComponentType("tissue_data",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /**
     * The Tissue Sample (drops D7a, layer 4 of {@code genetics/Drops}): a vial with the genes of the animal that dropped it (4% of the
     * player kills, certain with the Calipers in the killer's off hand). Make stacks with {@link TissueSampleItem#create}; the Calipers read it.
     */
    public static final DeferredItem<TissueSampleItem> TISSUE_SAMPLE = ITEMS.registerItem("tissue_sample", TissueSampleItem::new,
            props -> props.rarity(Rarity.UNCOMMON));

    /**
     * The payload of a Worn Ear Tag (drops D8e): the {@code BD-XXXXXX} code the animal wore, as a string, saved and synced like
     * {@link #TISSUE_DATA}. Write one by hand with {@code /give @s bunnidogs:worn_ear_tag[bunnidogs:worn_tag_code="BD-3F7A2C"]}.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> WORN_TAG_CODE =
            COMPONENTS.registerComponentType("worn_tag_code",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /**
     * The Worn Ear Tag (drops D8e): left behind by ANY bunnidog that wears an Ear Tag when it dies (certain, pup or adult, any cause; the
     * {@code mobDrops} game rule still applies). Make stacks with {@link WornEarTagItem#create}. A keepsake for the Trophy Pedestal. UNCOMMON.
     */
    public static final DeferredItem<WornEarTagItem> WORN_EAR_TAG = ITEMS.registerItem("worn_ear_tag", WornEarTagItem::new,
            props -> props.rarity(Rarity.UNCOMMON));

    /**
     * Starlit Dust (drops D7b, the starborn signature row of {@code genetics/Drops}): right-click a planted seed crop and it ripens at once
     * (one dust, one crop; {@link StarlitDustItem}). UNCOMMON.
     */
    public static final DeferredItem<StarlitDustItem> STARLIT_DUST = ITEMS.registerItem("starlit_dust", StarlitDustItem::new,
            props -> props.rarity(Rarity.UNCOMMON));

    /**
     * The five legends (drops D8d), layer 5 of {@code genetics/Drops}: an ADULT bunnidog killed by a player rolls once per stat bred to 100
     * points or more (2% at 100, 4% at 200, 8% at 400, 15% from 750; Looting does not help). One per stat: the Titan Haunch (Vitality), the
     * Gilded Foot (Swiftness), the Moon Sinew (Leap), the Listening Shell (Senses) and the Everbloom (Bloom). Plain EPIC items: their use is
     * to stand on the Trophy Pedestal (docs/DROPS_SPECS.md sections 7 and 8; the Apex they were meant for is not planned). Art: {@code art/gen_legends.py}.
     */
    public static final DeferredItem<Item> TITAN_HAUNCH = ITEMS.registerItem("titan_haunch", com.bunnidogs.mod.item.ApexOfferingItem::new, props -> props.rarity(Rarity.EPIC));
    public static final DeferredItem<Item> GILDED_FOOT = ITEMS.registerItem("gilded_foot", com.bunnidogs.mod.item.ApexOfferingItem::new, props -> props.rarity(Rarity.EPIC));
    public static final DeferredItem<Item> MOON_SINEW = ITEMS.registerItem("moon_sinew", com.bunnidogs.mod.item.ApexOfferingItem::new, props -> props.rarity(Rarity.EPIC));
    public static final DeferredItem<Item> LISTENING_SHELL = ITEMS.registerItem("listening_shell", com.bunnidogs.mod.item.ApexOfferingItem::new, props -> props.rarity(Rarity.EPIC));
    public static final DeferredItem<Item> EVERBLOOM = ITEMS.registerItem("everbloom", com.bunnidogs.mod.item.ApexOfferingItem::new, props -> props.rarity(Rarity.EPIC));

    /** Everything a bunnidog will eat, fall in love over, and grow up faster on. */
    public static final TagKey<Item> BUNNIDOG_FOOD = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(BunnidogsMod.MODID, "bunnidog_food"));

    /** Registered on the mod event bus from {@link BunnidogsMod}. */
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            for (DeferredItem<GuavaItem> guava : GUAVAS) {
                event.accept(guava.get());
            }
            event.accept(GUAVA_JUICE.get());
            event.accept(RAW_BUNNIDOG_FLESH.get());
            event.accept(COOKED_BUNNIDOG_FLESH.get());
            event.accept(PRIME_HAUNCH.get());
            event.accept(ROAST_HAUNCH.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CALIPERS.get());
            event.accept(FIELD_GUIDE.get());
            event.accept(SYRINGE.get());
            event.accept(GENE_SPLICER.get());
            event.accept(GUAVA_BOAT.get());
            event.accept(GUAVA_CHEST_BOAT.get());
            event.accept(PEDIGREE_BOOK.get());
            event.accept(PUREBRED_REGISTRY.get());
            event.accept(JUDGING_STAND.get());
            event.accept(FEEDING_TROUGH.get());
            event.accept(BRONZE_RIBBON.get());
            event.accept(SILVER_RIBBON.get());
            event.accept(GOLD_RIBBON.get());
            event.accept(EAR_TAG.get());
            event.accept(WornEarTagItem.create("BD-3F7A2C"), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.accept(TROPHY_PEDESTAL.get());
            event.accept(APEX_CRUCIBLE.get());
            event.accept(GENETICS_TABLE.get());
            event.accept(NEST.get());
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(GUAVA_SAPLING.get());
            event.accept(GUAVA_LOG.get());
            event.accept(STRIPPED_GUAVA_LOG.get());
            event.accept(GUAVA_WOOD.get());
            event.accept(STRIPPED_GUAVA_WOOD.get());
            event.accept(GUAVA_LEAVES.get());
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(GUAVA_PLANKS.get());
            event.accept(GUAVA_STAIRS.get());
            event.accept(GUAVA_SLAB.get());
            event.accept(GUAVA_FENCE.get());
            event.accept(GUAVA_DOOR.get());
            event.accept(GUAVA_TRAPDOOR.get());
            event.accept(GUAVA_BUTTON.get());
            event.accept(GUAVA_PRESSURE_PLATE.get());
            event.accept(GUAVA_FENCE_GATE.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(GUAVA_SIGN.get());
            event.accept(GUAVA_HANGING_SIGN.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            // One plain sample seed (two default parents, quality 0) so a seed can be had in creative mode.
            Seed plain = new Seed(Genome.DEFAULT, Stats.ZERO, Genome.DEFAULT, Stats.ZERO, 0.0D);
            event.accept(SeedItem.create(plain), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            for (DeferredItem<Item> pelt : PELTS.values()) {
                event.accept(pelt.get());
            }
            event.accept(SINEW.get());
            event.accept(WHISKER.get());
            event.accept(FANG.get());
            event.accept(MUTANT_MARROW.get());
            event.accept(MUTAGEN.get());
            event.accept(STARLIT_DUST.get());
            event.accept(TITAN_HAUNCH.get());
            event.accept(GILDED_FOOT.get());
            event.accept(MOON_SINEW.get());
            event.accept(LISTENING_SHELL.get());
            event.accept(EVERBLOOM.get());
            event.accept(APEX_CREST.get());
            // One plain sample (the default genome, no bred points) so a sample can be had in creative mode.
            event.accept(TissueSampleItem.create(Genome.DEFAULT, Stats.ZERO), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
