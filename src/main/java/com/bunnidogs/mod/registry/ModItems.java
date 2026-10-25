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
import com.bunnidogs.mod.item.TravelBasketItem;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
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
 * <p>Guava juice: eight tier carriers and the infused item share {@link JuiceItem}. The latter holds a
 * liquid pup in {@link #INFUSION_DATA}. Infuse through {@code SeedItem#use} or the Juicer.
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

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<net.minecraft.core.GlobalPos>> HEARTH_LINK=COMPONENTS.registerComponentType("hearth_link",b->b.persistent(net.minecraft.core.GlobalPos.CODEC).networkSynchronized(net.minecraft.core.GlobalPos.STREAM_CODEC));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> HEARTH_SITE=COMPONENTS.registerComponentType("hearth_site",b->b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> HUNT_MARKS=COMPONENTS.registerComponentType("hunt_marks",b->b.persistent(Codec.intRange(0,63)).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredItem<BlockItem> HEARTH_TREE=ITEMS.registerSimpleBlockItem(ModBlocks.HEARTH_TREE);
    public static final DeferredItem<BlockItem> GUAVA_COPPICE=ITEMS.registerSimpleBlockItem(ModBlocks.GUAVA_COPPICE);
    public static final DeferredItem<Item> LIVING_KEYSTONE=ITEMS.registerSimpleItem("living_keystone",p->p.rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.living_keystone")))));
    public static final DeferredItem<Item> GRAFTING_FRAME=ITEMS.registerSimpleItem("grafting_frame");
    public static final DeferredItem<Item> HEARTH_INCENSE=ITEMS.registerSimpleItem("hearth_incense");
    public static final DeferredItem<com.bunnidogs.mod.society.StewardStaffItem> STEWARD_STAFF=ITEMS.registerItem("steward_staff",com.bunnidogs.mod.society.StewardStaffItem::new,p->p.stacksTo(1).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.steward_staff")))));
    public static final DeferredItem<com.bunnidogs.mod.society.HearthPoulticeItem> HEARTH_POULTICE=ITEMS.registerItem("hearth_poultice",com.bunnidogs.mod.society.HearthPoulticeItem::new,p->p.stacksTo(16));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> TOOL_POWER=COMPONENTS.registerComponentType("tool_power",b->b.persistent(Codec.intRange(1,5)).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> FLASK_ID=COMPONENTS.registerComponentType("flask_id",b->b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> FLASK_COUNT=COMPONENTS.registerComponentType("flask_count",b->b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<net.minecraft.world.item.ItemStackTemplate>> FLASK_TOP=COMPONENTS.registerComponentType("flask_top",b->b.persistent(net.minecraft.world.item.ItemStackTemplate.CODEC).networkSynchronized(net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC));
    public static final DeferredItem<Item> APEX_FANG=ITEMS.registerSimpleItem("apex_fang",p->p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> VITALITY_ARTIFACT=ITEMS.registerSimpleItem("vitality_artifact",p->p.rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.vitality_artifact")))));
    public static final DeferredItem<Item> AERIAL_ARTIFACT=ITEMS.registerSimpleItem("aerial_artifact",p->p.rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.aerial_artifact")))));
    public static final DeferredItem<Item> VOLATILE_CHARGE=ITEMS.registerSimpleItem("volatile_charge");
    public static final DeferredItem<Item> CATACLYSM_CORE=ITEMS.registerSimpleItem("cataclysm_core",p->p.rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.cataclysm_core")))));
    public static final DeferredItem<Item> CATACLYSM_TOTEM=ITEMS.registerSimpleItem("cataclysm_totem",p->p.stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.cataclysm_totem"))))
            .component(DataComponents.DEATH_PROTECTION,net.minecraft.world.item.component.DeathProtection.TOTEM_OF_UNDYING));
    public static final DeferredItem<BlockItem> MOONSTONE=ITEMS.registerSimpleBlockItem(ModBlocks.MOONSTONE);
    public static final DeferredItem<BlockItem> ASTRALITE_ORE=ITEMS.registerSimpleBlockItem(ModBlocks.ASTRALITE_ORE);
    public static final DeferredItem<BlockItem> CINDERITE_ORE=ITEMS.registerSimpleBlockItem(ModBlocks.CINDERITE_ORE);
    public static final DeferredItem<Item> ASTRALITE_SHARD=ITEMS.registerSimpleItem("astralite_shard",p->p.fireResistant().rarity(Rarity.RARE));
    public static final DeferredItem<Item> RAW_CINDERITE=ITEMS.registerSimpleItem("raw_cinderite",p->p.fireResistant());
    public static final DeferredItem<Item> CINDERITE_INGOT=ITEMS.registerSimpleItem("cinderite_ingot",p->p.fireResistant());
    public static final DeferredItem<Item> ASTRALITE_INGOT=ITEMS.registerSimpleItem("astralite_ingot",p->p.fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ASTRALITE_UPGRADE=ITEMS.registerSimpleItem("astralite_upgrade",p->p.rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.astralite_upgrade")))));
    public static final Map<String,DeferredItem<Item>> ASTRALITE_ARMOR=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<Item>> ASTRALITE_TOOLS=new LinkedHashMap<>();
    static {
        for(var type:net.minecraft.world.item.equipment.ArmorType.values())if(type!=net.minecraft.world.item.equipment.ArmorType.BODY)ASTRALITE_ARMOR.put(type.getName(),ITEMS.registerSimpleItem("astralite_"+type.getName(),p->p.humanoidArmor(com.bunnidogs.mod.expedition.AstraliteMaterial.ARMOR,type).fireResistant().rarity(Rarity.EPIC)));
        for(String tool:List.of("sword","pickaxe","axe","shovel","hoe"))ASTRALITE_TOOLS.put(tool,ITEMS.registerSimpleItem("astralite_"+tool,p->{var m=com.bunnidogs.mod.expedition.AstraliteMaterial.TOOLS;return switch(tool) {
            case "sword"->p.sword(m,3,-2.4F).fireResistant().rarity(Rarity.EPIC);
            case "pickaxe"->p.pickaxe(m,1,-2.8F).fireResistant().rarity(Rarity.EPIC);
            case "axe"->p.axe(m,5,-3).fireResistant().rarity(Rarity.EPIC);
            case "shovel"->p.shovel(m,1.5F,-3).fireResistant().rarity(Rarity.EPIC);
            default->p.hoe(m,-6,0).fireResistant().rarity(Rarity.EPIC);
        };}));
    }
    public static final Map<String,DeferredItem<Item>> HEARTBOUND_ARMOR=new LinkedHashMap<>();
    static {
        for(var type:net.minecraft.world.item.equipment.ArmorType.values()) {
            if(type==net.minecraft.world.item.equipment.ArmorType.BODY)continue;
            String name="heartbound_"+type.getName();
            HEARTBOUND_ARMOR.put(type.getName(),ITEMS.registerSimpleItem(name,p->p.humanoidArmor(com.bunnidogs.mod.expedition.AstraliteMaterial.ARMOR,type).fireResistant().rarity(Rarity.EPIC)
                    .component(DataComponents.EQUIPPABLE,net.minecraft.world.item.equipment.Equippable.builder(type.getSlot())
                        .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                        .setAsset(net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("bunnidogs","heartbound"))).build())
                    .component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.heartbound"))))));
        }
    }
    public static final DeferredItem<com.bunnidogs.mod.expedition.FireballLauncherItem> FIREBALL_LAUNCHER=ITEMS.registerItem("fireball_launcher",com.bunnidogs.mod.expedition.FireballLauncherItem::new,p->p.durability(768).enchantable(15).rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.fireball_launcher")))));
    public static final DeferredItem<com.bunnidogs.mod.expedition.AbyssalFlaskItem> ABYSSAL_FLASK=ITEMS.registerItem("abyssal_flask",com.bunnidogs.mod.expedition.AbyssalFlaskItem::new,p->p.stacksTo(1).rarity(Rarity.EPIC).fireResistant().component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.abyssal_flask")))));
    public static final DeferredItem<BlockItem> ROOTSTONE=ITEMS.registerSimpleBlockItem(ModBlocks.ROOTSTONE);
    public static final DeferredItem<BlockItem> WARREN_ANCHOR=ITEMS.registerSimpleBlockItem(ModBlocks.WARREN_ANCHOR);
    public static final DeferredItem<BlockItem> WARREN_WARD=ITEMS.registerSimpleBlockItem(ModBlocks.WARREN_WARD);
    public static final DeferredItem<Item> RESONANT_EYE=ITEMS.registerSimpleItem("resonant_eye",props->props.rarity(Rarity.RARE).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.resonant_eye")))));
    public static final DeferredItem<Item> APEX_MARROW=ITEMS.registerSimpleItem("apex_marrow",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> PRISM_LENS=ITEMS.registerSimpleItem("prism_lens",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> CHORUS_HEART=ITEMS.registerSimpleItem("chorus_heart",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> VOLATILE_HEART=ITEMS.registerSimpleItem("volatile_heart",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> HEART_SPOOL=ITEMS.registerSimpleItem("heart_spool",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<Item> AERIAL_MEMBRANE=ITEMS.registerSimpleItem("aerial_membrane",props->props.rarity(Rarity.RARE));
    public static final DeferredItem<com.bunnidogs.mod.realm.PrismCellItem> PRISM_CELL=ITEMS.registerItem("prism_cell",com.bunnidogs.mod.realm.PrismCellItem::new);
    public static final DeferredItem<Item> APEX_WING_HARNESS=ITEMS.registerSimpleItem("apex_wing_harness",props->props.durability(864).enchantable(15).rarity(Rarity.EPIC)
            .component(DataComponents.GLIDER,net.minecraft.util.Unit.INSTANCE)
            .component(DataComponents.EQUIPPABLE,net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.CHEST)
                    .setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_ELYTRA)
                    .setAsset(net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("bunnidogs","apex_wing_harness")))
                    .setDamageOnHurt(false).build())
            .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.fromNamespaceAndPath("bunnidogs","armored_elytra"),8,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.fromNamespaceAndPath("bunnidogs","elytra_toughness"),3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST).build())
            .component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.apex_wing_harness"))))
            .repairable(AERIAL_MEMBRANE.get()));
    public static final DeferredItem<com.bunnidogs.mod.realm.PrismLanceItem> PRISM_LANCE=ITEMS.registerItem("prism_lance",com.bunnidogs.mod.realm.PrismLanceItem::new,props->props.stacksTo(1).durability(512).enchantable(15).rarity(Rarity.EPIC).repairable(PRISM_LENS.get()));
    public static final DeferredItem<com.bunnidogs.mod.expedition.GeneticVacuumItem> GENETIC_VACUUM=ITEMS.registerItem("genetic_vacuum",com.bunnidogs.mod.expedition.GeneticVacuumItem::new,p->p.durability(1024).enchantable(15).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.genetic_vacuum")))).repairable(ModItems.CHORUS_HEART.get()).rarity(Rarity.EPIC).component(DataComponents.BUNDLE_CONTENTS,net.minecraft.world.item.component.BundleContents.EMPTY));

    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> CROWN_SITE=COMPONENTS.registerComponentType("crown_site",b->b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final List<DeferredItem<Item>> CROWN_KEYS=java.util.stream.Stream.of("glass_keystone","brood_keystone","heart_keystone").map(id->ITEMS.registerSimpleItem(id,p->p.stacksTo(1).fireResistant().rarity(Rarity.RARE).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.crown_key")))))).toList();
    public static final DeferredItem<Item> CROWN_SIGIL=ITEMS.registerSimpleItem("crown_sigil",p->p.rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.crown_sigil")))));
    public static final DeferredItem<Item> SOVEREIGN_CORE=ITEMS.registerSimpleItem("sovereign_core",p->p.fireResistant().rarity(Rarity.EPIC).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.sovereign_core")))));
    public static final DeferredItem<Item> CROWN_TROPHY=ITEMS.registerSimpleItem("crown_trophy",p->p.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    public static final DeferredItem<com.bunnidogs.mod.crown.ConcordStandardItem> CONCORD_STANDARD=ITEMS.registerItem("concord_standard",com.bunnidogs.mod.crown.ConcordStandardItem::new,p->p.durability(1024).enchantable(15).rarity(Rarity.EPIC).fireResistant().repairable(ModItems.HEART_SPOOL.get()).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.concord_standard")))));
    public static final Map<String,DeferredItem<Item>> SOVEREIGN_ARMOR=new LinkedHashMap<>();
    static {
        for(var type:net.minecraft.world.item.equipment.ArmorType.values()) {
            if(type==net.minecraft.world.item.equipment.ArmorType.BODY)continue;
            SOVEREIGN_ARMOR.put(type.getName(),ITEMS.registerSimpleItem("sovereign_"+type.getName(),p->p.humanoidArmor(com.bunnidogs.mod.expedition.AstraliteMaterial.ARMOR,type).fireResistant().rarity(Rarity.EPIC)
                    .component(DataComponents.EQUIPPABLE,net.minecraft.world.item.equipment.Equippable.builder(type.getSlot()).setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_NETHERITE)
                    .setAsset(net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("bunnidogs","sovereign"))).build())
                    .component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.sovereign_armor"))))));
        }
    }
    public static final DeferredItem<com.bunnidogs.mod.realm.PrismLanceItem> SOVEREIGN_LANCE=ITEMS.registerItem("sovereign_lance",com.bunnidogs.mod.realm.PrismLanceItem::new,p->p.durability(768).enchantable(15).rarity(Rarity.EPIC).fireResistant().repairable(ModItems.PRISM_LENS.get()));
    public static final DeferredItem<Item> SOVEREIGN_ELYTRA=ITEMS.registerSimpleItem("sovereign_elytra",p->p.durability(1296).enchantable(15).rarity(Rarity.EPIC).fireResistant()
            .component(DataComponents.GLIDER,net.minecraft.util.Unit.INSTANCE)
            .component(DataComponents.EQUIPPABLE,net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.CHEST).setEquipSound(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_ELYTRA)
            .setAsset(net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("bunnidogs","sovereign_elytra"))).setDamageOnHurt(false).build())
            .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.fromNamespaceAndPath("bunnidogs","armored_elytra"),10,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.fromNamespaceAndPath("bunnidogs","elytra_toughness"),4,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST).build())
            .repairable(ModItems.AERIAL_MEMBRANE.get()).component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.sovereign_elytra")))));

    public static final Map<String,DeferredItem<com.bunnidogs.mod.item.ApexSigilItem>> APEX_SIGILS=new LinkedHashMap<>();
    static {
        for (var kind:com.bunnidogs.mod.entity.ApexKind.values()) for (boolean flying:new boolean[]{false,true}) {
            if (kind==com.bunnidogs.mod.entity.ApexKind.BRUTE && !flying) continue;
            String id=(flying ? "flying_" : "")+kind.id+"_sigil";
            APEX_SIGILS.put(id,ITEMS.registerItem(id,props->new com.bunnidogs.mod.item.ApexSigilItem(props,kind,flying),props->props.rarity(Rarity.RARE)));
        }
    }

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
    public static final DeferredItem<BlockItem> COMMISSION_BOARD = ITEMS.registerSimpleBlockItem(ModBlocks.COMMISSION_BOARD,
            props -> props.component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.commission.controls")))));
    public static final DeferredItem<BlockItem> FEEDING_TROUGH = ITEMS.registerSimpleBlockItem(ModBlocks.FEEDING_TROUGH);
    public static final DeferredItem<BlockItem> NURSERY_CRADLE = ITEMS.registerSimpleBlockItem(ModBlocks.NURSERY_CRADLE);
    public static final DeferredItem<BlockItem> JUICER = ITEMS.registerSimpleBlockItem(ModBlocks.JUICER);
    public static final DeferredItem<BlockItem> BREEDING_PEN = ITEMS.registerSimpleBlockItem(ModBlocks.BREEDING_PEN,
            props -> props.component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.pen.ports")))));
    public static final DeferredItem<BlockItem> SPECIMEN_JAR = ITEMS.registerSimpleBlockItem(ModBlocks.SPECIMEN_JAR);
    public static final DeferredItem<BlockItem> PEDIGREE_COMPARATOR = ITEMS.registerSimpleBlockItem(ModBlocks.PEDIGREE_COMPARATOR,
            props -> props.component(DataComponents.LORE,new ItemLore(List.of(Component.translatable("tooltip.bunnidogs.detector.controls")))));
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> BRONZE_RIBBON = ITEMS.registerItem("bronze_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.BRONZE), props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> SILVER_RIBBON = ITEMS.registerItem("silver_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.SILVER), props -> props.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<com.bunnidogs.mod.item.ShowRibbonItem> GOLD_RIBBON = ITEMS.registerItem("gold_show_ribbon",
            props -> new com.bunnidogs.mod.item.ShowRibbonItem(props, com.bunnidogs.mod.genetics.Show.Tier.GOLD), props -> props.stacksTo(1).rarity(Rarity.EPIC));

    /** Plain guava juice (Phase 4f): a guava and a glass bottle. Drinking it restores a little hunger; it is what a seed is infused into. */
    public static final DeferredItem<JuiceItem> GUAVA_JUICE = ITEMS.registerItem("guava_juice", JuiceItem::new, props -> props.stacksTo(16));
    /** Stable plain id, with the richer carriers appended in guava-ladder order. */
    public static final List<DeferredItem<JuiceItem>> JUICES = registerJuices();
    private static List<DeferredItem<JuiceItem>> registerJuices() {
        var juices = new java.util.ArrayList<DeferredItem<JuiceItem>>();
        juices.add(GUAVA_JUICE);
        for (GuavaTier tier : GuavaTier.values()) if (tier != GuavaTier.PLAIN)
            juices.add(ITEMS.registerItem(tier.id()+"_juice",props -> new JuiceItem(props,tier),
                    props -> props.stacksTo(16).rarity(rarityOf(tier))));
        return List.copyOf(juices);
    }

    /** A bottle holding a liquid pup (a seed that was infused). Make stacks with {@link JuiceItem#create}. */
    public static final DeferredItem<JuiceItem> INFUSED_JUICE = ITEMS.registerItem("infused_guava_juice", JuiceItem::new,
            props -> props.stacksTo(1).rarity(Rarity.UNCOMMON));

    /** The Calipers (Phase 5): read a bunnidog's or a seed's exact genes into the chat. Crafted from iron and copper (data/bunnidogs/recipe/calipers.json). */
    public static final DeferredItem<CalipersItem> CALIPERS = ITEMS.registerItem("calipers", CalipersItem::new, props -> props.stacksTo(1));
    public static final DeferredItem<com.bunnidogs.mod.item.TrailWhistleItem> TRAIL_WHISTLE = ITEMS.registerItem("trail_whistle",
            com.bunnidogs.mod.item.TrailWhistleItem::new,props -> props.stacksTo(1));
    public static final DeferredItem<SyringeItem> SYRINGE = ITEMS.registerItem("syringe", SyringeItem::new,
            props -> props.durability(com.bunnidogs.mod.genetics.Sampling.TOOL_USES));
    public static final DeferredItem<TravelBasketItem> TRAVEL_BASKET = ITEMS.registerItem("travel_basket",TravelBasketItem::new,props -> props.stacksTo(1));
    public static final DeferredItem<GeneSplicerItem> GENE_SPLICER = ITEMS.registerItem("gene_splicer",GeneSplicerItem::new,
            props -> props.durability(com.bunnidogs.mod.genetics.Splicing.TOOL_USES).rarity(Rarity.RARE));

    /** The Ear Tag (Phase 5d): clip it on a bunnidog for a permanent registration code. Crafted from an iron nugget and paper (data/bunnidogs/recipe/ear_tag.json). */
    public static final DeferredItem<EarTagItem> EAR_TAG = ITEMS.registerItem("ear_tag", EarTagItem::new, props -> props);

    /** The log of a Pedigree Book (Phase 5c): {@link com.bunnidogs.mod.genetics.Pedigree#toString()} as a string, saved and synced like {@link #SEED_DATA}. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PEDIGREE_DATA =
            COMPONENTS.registerComponentType("pedigree_data",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> LINEAGE_NAMES =
            COMPONENTS.registerComponentType("lineage_names",
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
    public static final Map<String,DeferredItem<BlockItem>> PELT_RUGS = registerPeltRugs();
    private static Map<String,DeferredItem<BlockItem>> registerPeltRugs() {
        var rugs = new LinkedHashMap<String,DeferredItem<BlockItem>>();
        for (var coat : Gene.COAT.alleles()) rugs.put(coat.name(),ITEMS.registerSimpleBlockItem(ModBlocks.PELT_RUGS.get(coat.name()),
                props -> props.rarity(coat.isRare() ? Rarity.RARE : coat.isWild() ? Rarity.UNCOMMON : Rarity.COMMON)));
        return Collections.unmodifiableMap(rugs);
    }

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
            for (var juice : JUICES) event.accept(juice.get());
            event.accept(RAW_BUNNIDOG_FLESH.get());
            event.accept(COOKED_BUNNIDOG_FLESH.get());
            event.accept(PRIME_HAUNCH.get());
            event.accept(ROAST_HAUNCH.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CALIPERS.get());
            event.accept(TRAIL_WHISTLE.get());
            event.accept(FIELD_GUIDE.get());
            event.accept(SYRINGE.get());
            event.accept(TRAVEL_BASKET.get());
            event.accept(GENE_SPLICER.get());
            event.accept(GUAVA_BOAT.get());
            event.accept(GUAVA_CHEST_BOAT.get());
            event.accept(PEDIGREE_BOOK.get());
            event.accept(PUREBRED_REGISTRY.get());
            event.accept(JUDGING_STAND.get());
            event.accept(COMMISSION_BOARD.get());
            event.accept(FEEDING_TROUGH.get());
            event.accept(NURSERY_CRADLE.get());
            event.accept(JUICER.get());
            event.accept(BREEDING_PEN.get());
            event.accept(PEDIGREE_COMPARATOR.get());
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
            event.accept(SPECIMEN_JAR.get());
            for (var rug : PELT_RUGS.values()) event.accept(rug.get());
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
            ASTRALITE_ARMOR.values().forEach(item->event.accept(item.get()));ASTRALITE_TOOLS.values().forEach(item->event.accept(item.get()));
            for(var item:List.of(MOONSTONE.get(),ASTRALITE_ORE.get(),CINDERITE_ORE.get(),ASTRALITE_SHARD.get(),RAW_CINDERITE.get(),CINDERITE_INGOT.get(),ASTRALITE_INGOT.get(),ASTRALITE_UPGRADE.get()))event.accept(item);
            HEARTBOUND_ARMOR.values().forEach(item->event.accept(item.get()));
            SOVEREIGN_ARMOR.values().forEach(item->event.accept(item.get()));CROWN_KEYS.forEach(item->event.accept(item.get()));
            for(var item:List.of(SOVEREIGN_CORE.get(),SOVEREIGN_LANCE.get(),SOVEREIGN_ELYTRA.get(),CONCORD_STANDARD.get(),CROWN_SIGIL.get(),CROWN_TROPHY.get()))event.accept(item);
            for(var item:java.util.List.of(APEX_FANG.get(),VITALITY_ARTIFACT.get(),AERIAL_ARTIFACT.get(),CATACLYSM_CORE.get(),VOLATILE_CHARGE.get(),CATACLYSM_TOTEM.get(),FIREBALL_LAUNCHER.get(),GENETIC_VACUUM.get(),ABYSSAL_FLASK.get()))event.accept(item);
            for(var item:java.util.List.of(ROOTSTONE.get(),WARREN_ANCHOR.get(),WARREN_WARD.get(),RESONANT_EYE.get(),APEX_MARROW.get(),PRISM_LENS.get(),CHORUS_HEART.get(),VOLATILE_HEART.get(),HEART_SPOOL.get(),AERIAL_MEMBRANE.get(),PRISM_CELL.get(),PRISM_LANCE.get(),APEX_WING_HARNESS.get()))event.accept(item);
            for(var item:List.of(HEARTH_TREE.get(),GUAVA_COPPICE.get(),LIVING_KEYSTONE.get(),GRAFTING_FRAME.get(),HEARTH_INCENSE.get(),STEWARD_STAFF.get(),HEARTH_POULTICE.get()))event.accept(item);
            event.accept(APEX_CREST.get());
            APEX_SIGILS.values().forEach(item->event.accept(item.get()));
            // One plain sample (the default genome, no bred points) so a sample can be had in creative mode.
            event.accept(TissueSampleItem.create(Genome.DEFAULT, Stats.ZERO), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
