package com.bunnidogs.mod.block;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class CradleStorageGameTest {
    private CradleStorageGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        h.setBlock(3,0,3,Blocks.REDSTONE_BLOCK); h.setBlock(4,1,3,Blocks.STONE);
        h.setBlock(new BlockPos(3,1,3),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.ENABLED,false));
        h.setBlock(3,2,3,ModBlocks.NURSERY_CRADLE.get()); h.setBlock(3,3,3,Blocks.HOPPER);
        h.setBlock(new BlockPos(4,2,3),Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.WEST));
        var pos = h.absolutePos(new BlockPos(3,2,3));
        var cradle = (NurseryCradleBlockEntity)level.getBlockEntity(pos);
        var input = (HopperBlockEntity)level.getBlockEntity(pos.above());
        var output = (HopperBlockEntity)level.getBlockEntity(pos.below());
        var comparator = (ComparatorBlockEntity)level.getBlockEntity(pos.east());
        var named = SeedItem.create(new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.of(6,7,8,9,10),2.5,true));
        named.set(DataComponents.CUSTOM_NAME,Component.literal("Cradle line")); named.setCount(4);
        h.runAfterDelay(2,() -> {
            var handler = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.UP);
            h.assertTrue(handler != null && handler == level.getCapability(Capabilities.Item.BLOCK,pos,Direction.DOWN),"Every face must share one cradle journal");
            input.setItem(0,named.copy());
            for (int tick = 0; tick < 80; tick++) HopperBlockEntity.pushItemsTick(level,pos.above(),input.getBlockState(),input);
            h.assertTrue(cradle.storedSeed().getCount() == 1 && input.getItem(0).getCount() == 3
                    && ItemStack.isSameItemSameComponents(named,cradle.storedSeed()) && cradle.stockSignal() == 1,
                    "Real hoppers must insert exactly one whole seed and leave the remaining stack intact");
            input.clearContent(); cradle.grow(false);
            int age = cradle.age(), wait = cradle.growthWait();
            try (var tx = Transaction.openRoot()) {
                h.assertTrue(handler.extract(ItemResource.of(named),1,tx) == 1,"The stored seed may be tentatively extracted");
            }
            h.assertTrue(cradle.age() == age && cradle.growthWait() == wait && cradle.storedSeed().getCount() == 1,
                    "Aborted extraction must restore whole stock without restarting incubation");
            var saved = cradle.saveWithFullMetadata(level.registryAccess());
            var restored = (NurseryCradleBlockEntity)BlockEntity.loadStatic(pos,cradle.getBlockState(),saved,level.registryAccess());
            h.assertTrue(restored.age() == age && restored.growthWait() == wait && ItemStack.matches(restored.storedSeed(),cradle.storedSeed()),
                    "Native block-entity saves must retain exact stock, stage and paused remaining time");
            saved.putInt("Age",3); saved.putInt("GrowthWait",2);
            cradle.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.runAfterDelay(5,() -> {
                h.assertTrue(cradle.age() == 4 && cradle.growthWait() <= NurseryCradleBlockEntity.STAGE_TICKS
                        && cradle.growthWait() >= NurseryCradleBlockEntity.STAGE_TICKS-5 && comparator.getOutputSignal() == 9,
                        "The actual native ticker must advance one saved stage and notify a real comparator");
                level.setBlock(pos.below(2),Blocks.STONE.defaultBlockState(),3);
                HopperBlockEntity.pushItemsTick(level,pos.below(),output.getBlockState(),output);
                level.setBlock(pos.below(2),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
                h.assertTrue(cradle.storedSeed().isEmpty() && cradle.age() == 0 && output.getItem(0).getCount() == 1
                        && ItemStack.isSameItemSameComponents(named,output.getItem(0)),"Real hopper withdrawal must return the seed once and reset progress");
                var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                player.setItemInHand(InteractionHand.MAIN_HAND,output.removeItem(0,1));
                var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
                player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
                h.assertTrue(player.getMainHandItem().isEmpty() && cradle.storedSeed().getCount() == 1,"Hand insertion must consume exactly one real seed");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BONE_MEAL));
                player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
                h.assertTrue(player.getMainHandItem().isEmpty() && cradle.age() >= 2 && cradle.age() <= 5,"One hand bone meal must add the ordinary two-to-five stages");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.STARLIT_DUST.get(),2));
                player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
                h.assertTrue(player.getMainHandItem().getCount() == 1 && cradle.age() == 7 && cradle.stockSignal() == 15,"One hand dust must make the litter ready with signal fifteen");
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.setShiftKeyDown(true);
                player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,hit);
                h.assertTrue(cradle.storedSeed().isEmpty() && player.getInventory().countItem(ModItems.SEED.get()) == 1,"Native sneak-empty use must withdraw exactly one seed");
                player.setShiftKeyDown(false);
                player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
                h.assertTrue(cradle.storedSeed().getCount() == 1 && cradle.age() == 0,"Reinserting the withdrawn seed must restart incubation");
                level.destroyBlock(pos,true);
                var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2));
                h.assertTrue(drops.stream().filter(e -> e.getItem().is(ModItems.SEED.get())).mapToInt(e -> e.getItem().getCount()).sum() == 1
                        && drops.stream().filter(e -> e.getItem().is(ModItems.NURSERY_CRADLE.get())).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                        "Breaking a cradle must drop its block and preserved stock exactly once");
                h.succeed();
            });
        });
    }
}
