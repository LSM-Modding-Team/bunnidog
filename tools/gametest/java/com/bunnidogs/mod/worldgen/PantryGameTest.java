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
        };
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
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
            int guavas = 0, local = 0, guides = 0;
            for (int slot = 0; slot < barrel.getContainerSize(); slot++) {
                var stack = barrel.getItem(slot); // Native deferred loot fills the real container.
                if (stack.isEmpty()) continue;
                if (stack.is(ModItems.GUAVA.get())) guavas += stack.getCount();
                else if (stack.is(ModItems.FIELD_GUIDE.get())) guides += stack.getCount();
                else {
                    h.assertTrue(ModDietTags.accepts(diet(style),stack),"Local pantry food must suit its lineage: " + style + " / " + stack);
                    local += stack.getCount();
                }
            }
            h.assertTrue(guavas >= 1 && guavas <= 2 && local >= 1 && local <= 9 && guides <= 1,
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
        surface(h);
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
