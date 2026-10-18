package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

/** Actual player placement/cancellation, consumable growth, native loot and dispenser pulses. */
public final class OrchardGameTest {
    private OrchardGameTest() {}
    private static void use(ServerPlayer player, BlockPos pos, Direction face, InteractionHand hand) {
        player.gameMode.useItemOn(player,player.level(),player.getItemInHand(hand),hand,
                new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false));
    }
    private static void pulse(GameTestHelper h, BlockPos dispenser, Runnable after) {
        var back = dispenser.west(); h.getLevel().setBlock(back,Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.runAfterDelay(6,() -> { h.getLevel().setBlock(back,Blocks.AIR.defaultBlockState(),3); after.run(); });
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) {
            h.setBlock(x,0,z,Blocks.STONE);
            for (int y = 1; y < 6; y++) h.setBlock(x,y,z,Blocks.AIR);
        }
        var fruit = h.absolutePos(new BlockPos(3,2,3)); var leaf = fruit.above();
        level.setBlock(leaf,ModBlocks.GUAVA_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var guavas = new ItemStack(ModItems.GUAVA.get(),4); guavas.set(DataComponents.CUSTOM_NAME,Component.literal("Orchard stock"));
        player.setItemInHand(InteractionHand.OFF_HAND,guavas);
        Consumer<EntityPlaceEvent> protect = event -> { if (event.getLevel() == level && event.getPos().equals(fruit)) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(protect);
        try { use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND); }
        finally { NeoForge.EVENT_BUS.unregister(protect); }
        h.assertTrue(level.getBlockState(fruit).isAir() && guavas.getCount() == 4
                && guavas.getHoverName().getString().equals("Orchard stock"),"Canceled native placement must restore block and exact held stock");
        use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 0
                && guavas.getCount() == 3 && player.getMainHandItem().isEmpty(),"Survival off-hand planting must spend one plain guava and start green");
        use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(guavas.getCount() == 3,"Existing fruit must not be replaced or charged again");
        level.removeBlock(fruit,false); player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).isAir() && guavas.getCount() == 3,"Adventure use without placement permission must refuse");
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && guavas.getCount() == 3,"Creative planting must keep its guava");
        level.removeBlock(fruit,false); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var golden = new ItemStack(ModItems.GOLDEN_GUAVA.get(),2); player.setItemInHand(InteractionHand.OFF_HAND,golden);
        use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).isAir() && golden.getCount() == 2,"Enhanced guavas must not be planted or spent");
        player.setItemInHand(InteractionHand.OFF_HAND,guavas); use(player,leaf,Direction.UP,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).isAir() && guavas.getCount() == 3,"Planting requires the underside");
        level.setBlock(fruit,Blocks.WATER.defaultBlockState(),3); use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).is(Blocks.WATER) && guavas.getCount() == 3,"Planting must preserve fluids");
        level.setBlock(fruit,Blocks.AIR.defaultBlockState(),3); level.setBlock(leaf,Blocks.STONE.defaultBlockState(),3); use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).isAir() && guavas.getCount() == 3,"Solid non-leaf supports must refuse");
        level.setBlock(leaf,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),3);
        use(player,leaf,Direction.DOWN,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).is(ModBlocks.GUAVA_FRUIT.get()) && guavas.getCount() == 2,"Existing any-leaf fruit support applies to propagation too");
        var meal = new ItemStack(Items.BONE_MEAL,4); player.setItemInHand(InteractionHand.OFF_HAND,meal);
        use(player,fruit,Direction.NORTH,InteractionHand.OFF_HAND);
        h.assertTrue(level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 1 && meal.getCount() == 3,"One native bone meal must add one age");
        use(player,fruit,Direction.NORTH,InteractionHand.OFF_HAND);
        h.assertTrue(GuavaFruitBlock.isRipe(level.getBlockState(fruit)) && meal.getCount() == 2,"Two native bone-meal uses must ripen a green fruit");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); use(player,fruit,Direction.NORTH,InteractionHand.MAIN_HAND);
        h.assertTrue(level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 0
                && level.getEntitiesOfClass(ItemEntity.class,new AABB(fruit).inflate(2),e -> e.getItem().is(ModItems.GUAVA.get())).stream()
                    .mapToInt(e -> e.getItem().getCount()).sum() == 1,"Picking must produce exactly one guava and keep the green fruit");
        var leaves = ModBlocks.GUAVA_LEAVES.get().defaultBlockState();
        var silk = new ItemStack(Items.DIAMOND_HOE);
        silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),1);
        for (var tool : new ItemStack[]{silk,new ItemStack(Items.SHEARS)}) {
            var drops = Block.getDrops(leaves,level,leaf,null,player,tool);
            h.assertTrue(drops.size() == 1 && drops.getFirst().is(ModItems.GUAVA_LEAVES.get()) && drops.getFirst().getCount() == 1,
                    "Silk Touch and shears must recover exactly the leaf block without seed/stick extras");
        }
        var dispenserPos = fruit.west(); level.setBlock(dispenserPos,Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,Direction.EAST),3);
        var dispenser = (DispenserBlockEntity)level.getBlockEntity(dispenserPos); var supply = new ItemStack(Items.BONE_MEAL,4); dispenser.setItem(0,supply);
        pulse(h,dispenserPos,() -> {
            h.assertTrue(supply.getCount() == 3 && level.getBlockState(fruit).getValue(GuavaFruitBlock.AGE) == 1,"Native dispenser bone meal must share one-stage growth");
            pulse(h,dispenserPos,() -> {
                h.assertTrue(supply.getCount() == 2 && GuavaFruitBlock.isRipe(level.getBlockState(fruit)),"Second native pulse must ripen the existing fruit");
                pulse(h,dispenserPos,() -> {
                    h.assertTrue(supply.getCount() == 2,"Ripe dispenser target must retain bone meal");
                    level.removeBlock(leaf,false);
                    h.assertTrue(!ModBlocks.GUAVA_FRUIT.get().isValidBonemealTarget(level,fruit,level.getBlockState(fruit)),"Unsupported fruit must refuse forced growth");
                    h.runAfterDelay(2,() -> {
                        h.assertTrue(level.getBlockState(fruit).isAir(),"Native scheduled support loss must still remove fruit");
                        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(fruit).inflate(3),e -> e.getItem().is(ModItems.GUAVA.get())).stream()
                                .mapToInt(e -> e.getItem().getCount()).sum() == 2,"Support loss must drop exactly the ripe guava in addition to the earlier pick");
                        h.succeed();
                    });
                });
            });
        });
    }
}
