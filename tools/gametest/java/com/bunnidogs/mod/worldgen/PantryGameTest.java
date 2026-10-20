package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.genetics.Diet;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.registry.ModDietTags;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class PantryGameTest {
    private PantryGameTest() {}
    private static Diet diet(Lineage style) {
        return switch (style) {
            case MEADOW -> Diet.OMNIVORE;
            case TUNDRA -> Diet.FISHER;
            case DESERT -> Diet.DESERT_BROWSER;
            case SWAMP -> Diet.MUSHROOM_EATER;
            case CAVE -> Diet.CAVE_FORAGER;
            case MOUNTAIN -> Diet.MOUNTAIN_GRAZER;
            case NETHER -> Diet.NETHER_FORAGER;
            case END -> Diet.CHORUS_EATER;
            case ISLAND -> Diet.SHORE_FORAGER;
        };
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        maps(h);
        int index = 0;
        int[] cells = {0,0,0,DenShape.Kind.BARREL.ordinal()};
        for (var style : Lineage.values()) {
            var pos = h.absolutePos(new BlockPos(1+index%4,1,1+index/4)); index++;
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
            int x = pos.getX() & ~15, z = pos.getZ() & ~15;
            var owner = new BoundingBox(x,level.getMinY(),z,x+15,level.getMaxY()-1,z+15);
            var foreign = new BoundingBox(x+16,owner.minY(),z,x+31,owner.maxY(),z+15);
            WarrenTunnels.place(level,pos,cells,foreign,style);
            h.assertTrue(level.getBlockState(pos).isAir(),"Another chunk must not place or seed a pantry");
            WarrenTunnels.place(level,pos,cells,owner,style);
            var barrel = (RandomizableContainerBlockEntity)level.getBlockEntity(pos);
            h.assertTrue(barrel != null && WarrenPantries.table(style).equals(barrel.getLootTable()),"New barrels need the settlement's native loot key: " + style);
            var saved = barrel.saveWithFullMetadata(level.registryAccess());
            var loaded = (RandomizableContainerBlockEntity)BlockEntity.loadStatic(pos,barrel.getBlockState(),saved,level.registryAccess());
            h.assertTrue(loaded != null && WarrenPantries.table(style).equals(loaded.getLootTable())
                    && loaded.getLootTableSeed() == barrel.getLootTableSeed(),"Unopened native saves must retain lineage and seed");
            int guavas = 0, local = 0, guides = 0, seeds = 0;
            for (int slot = 0; slot < barrel.getContainerSize(); slot++) {
                var stack = barrel.getItem(slot); // Native deferred loot fills the real container.
                if (stack.isEmpty()) continue;
                if (stack.is(ModItems.GUAVA.get())) guavas += stack.getCount();
                else if (stack.is(ModItems.FIELD_GUIDE.get())) guides += stack.getCount();
                else if (stack.is(Items.MELON_SEEDS)) {
                    h.assertTrue(style == Lineage.MEADOW,"Only meadow pantries may have separate melon seed supplies");
                    seeds += stack.getCount();
                }
                else {
                    h.assertTrue(ModDietTags.accepts(diet(style),stack),"Local pantry food must suit its lineage: " + style + " / " + stack);
                    local += stack.getCount();
                }
            }
            h.assertTrue(guavas >= 1 && guavas <= 2 && local >= 1 && local <= 9 && guides <= 1 && seeds <= 2,
                    "Each parsed lineage table must provide modest guavas/local food and at most one guide");
            barrel.clearContent();
            WarrenTunnels.place(level,pos,cells,owner,style);
            h.assertTrue(level.getBlockEntity(pos) == barrel && barrel.getLootTable() == null && barrel.isEmpty(),
                    "Repeated generation must never refill an emptied pantry");
        }
        var pos = h.absolutePos(new BlockPos(6,1,1));
        level.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
        var existing = (RandomizableContainerBlockEntity)level.getBlockEntity(pos);
        ResourceKey<LootTable> legacy = ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("bunnidogs","chests/warren_pantry"));
        existing.setLootTable(legacy); existing.setLootTableSeed(73);
        WarrenPantries.seedFresh(level,pos,Lineage.END);
        WarrenTunnels.place(level,pos,cells,null,Lineage.END);
        h.assertTrue(legacy.equals(existing.getLootTable()) && existing.getLootTableSeed() == 73,
                "Existing unopened legacy tables must remain unopened and unchanged");
        existing.unpackLootTable(null); existing.clearContent();
        var keepsake = new ItemStack(Items.DIAMOND);
        keepsake.set(DataComponents.CUSTOM_NAME,Component.literal("Player keepsake"));
        existing.setItem(0,keepsake.copy());
        WarrenPantries.seedFresh(level,pos,Lineage.NETHER);
        WarrenTunnels.place(level,pos,cells,null,Lineage.NETHER);
        h.assertTrue(existing.getLootTable() == null && ItemStack.isSameItemSameComponents(keepsake,existing.getItem(0)),
                "Player stock and its native components must be preserved");
        seedSupplies(h);
        surface(h);
    }
    private static void seedSupplies(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(6,1,2));
        level.setBlock(pos,Blocks.BARREL.defaultBlockState(),2);
        var barrel = (RandomizableContainerBlockEntity)level.getBlockEntity(pos);
        int supplied = 0, omitted = 0;
        for (int roll = 1; roll <= 16; roll++) {
            barrel.clearContent(); barrel.setLootTable(WarrenPantries.table(Lineage.MEADOW)); barrel.setLootTableSeed(roll); barrel.unpackLootTable(null);
            int seeds = 0, guavas = 0, food = 0;
            for (int slot = 0; slot < barrel.getContainerSize(); slot++) {
                var stack = barrel.getItem(slot);
                if (stack.is(Items.MELON_SEEDS)) seeds += stack.getCount();
                else if (stack.is(ModItems.GUAVA.get())) guavas += stack.getCount();
                else if (!stack.isEmpty() && !stack.is(ModItems.FIELD_GUIDE.get())) {
                    h.assertTrue(ModDietTags.accepts(Diet.OMNIVORE,stack),"Native meadow food rolls must suit their residents"); food += stack.getCount();
                }
            }
            h.assertTrue(seeds <= 2 && guavas >= 1 && guavas <= 2 && food >= 1 && food <= 9,
                    "Optional seeds must preserve modest guaranteed guava/local-food pools");
            if (seeds == 0) omitted++; else supplied++;
        }
        h.assertTrue(supplied > 0 && omitted > 0,"Native seeded chest rolls must exercise both optional supply outcomes");
    }
    private static void maps(GameTestHelper h) {
        var level = h.getLevel();
        var trades = level.registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        var trade = trades.getOrThrow(ResourceKey.create(Registries.VILLAGER_TRADE,
                Identifier.fromNamespaceAndPath("bunnidogs","cartographer/warren_map")));
        var pool = level.registryAccess().lookupOrThrow(Registries.TRADE_SET).getOrThrow(ResourceKey.create(Registries.TRADE_SET,
                Identifier.withDefaultNamespace("cartographer/level_3"))).value();
        h.assertTrue(pool.getTrades().contains(trade) && pool.getTrades().stream().anyMatch(t -> t.is(ResourceKey.create(
                Registries.VILLAGER_TRADE,Identifier.withDefaultNamespace("cartographer/3/compass_emerald")))),
                "The native cartographer pool must contain the warren map and retain ordinary trades");
        var context = new LootContext.Builder(new LootParams.Builder(level).create(LootContextParamSets.EMPTY))
                .create(java.util.Optional.empty());
        h.assertTrue(trade.value().getOffer(context) == null,
                "Native map modifiers must discard an unlocated blank map instead of offering it for sale");
    }
    private static void surface(GameTestHelper h) {
        var level = h.getLevel();
        for (var kind : new PropShape.Kind[]{PropShape.Kind.HAYSTACK,PropShape.Kind.STALL}) {
            for (int x = 1; x < 8; x++) for (int z = 1; z < 8; z++) {
                h.setBlock(x,0,z,Blocks.GRASS_BLOCK);
                for (int y = 1; y <= 8; y++) h.setBlock(x,y,z,Blocks.AIR);
            }
            var centre = h.absolutePos(new BlockPos(4,1,4));
            var prop = new Settlement.Prop(kind,0,0,1);
            h.assertTrue(WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW,true),
                    "A fresh surface feed store/market must fit");
            for (var cell : PropShape.cells(kind,1)) if (cell.mat() == PropShape.Mat.BARREL) {
                var pos = centre.offset(cell.dx(),cell.dy(),cell.dz());
                var barrel = (RandomizableContainerBlockEntity)level.getBlockEntity(pos);
                h.assertTrue(barrel != null && WarrenPantries.table(Lineage.MEADOW).equals(barrel.getLootTable()),
                        "New surface barrels must have deferred lineage food");
                barrel.unpackLootTable(null); barrel.clearContent();
                boolean rebuilt = WarrenFeature.buildProp(level,centre.getX(),centre.getY(),centre.getZ(),prop,true,Lineage.MEADOW,true);
                h.assertTrue(!rebuilt && level.getBlockEntity(pos) == barrel && barrel.getLootTable() == null && barrel.isEmpty(),
                        "Repeated surface generation must preserve an emptied barrel");
            }
        }
    }
}
