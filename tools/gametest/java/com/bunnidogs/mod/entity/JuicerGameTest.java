package com.bunnidogs.mod.entity;

import java.util.Random;
import com.bunnidogs.mod.block.JuicerBlockEntity;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.JuiceItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Native station, paid inventory journals, saved work and the real two-hand fallback. */
public final class JuicerGameTest {
    private JuicerGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var relative = new BlockPos(3,2,3); var pos = h.absolutePos(relative);
        h.setBlock(relative,ModBlocks.JUICER.get());
        var juicer = (JuicerBlockEntity)level.getBlockEntity(pos);
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        for (var tier : GuavaTier.values()) {
            var fruit = new ItemStack(ModItems.GUAVAS.get(tier.level()).get());
            var glass = new ItemStack(Items.GLASS_BOTTLE);
            player.setItemInHand(InteractionHand.OFF_HAND,fruit);
            player.gameMode.useItemOn(player,level,fruit,InteractionHand.OFF_HAND,hit);
            h.assertTrue(fruit.isEmpty() && juicer.item(0).getCount() == 1,"Off-hand fruit insertion pays the actual hand");
            player.setItemInHand(InteractionHand.OFF_HAND,glass);
            player.gameMode.useItemOn(player,level,glass,InteractionHand.OFF_HAND,hit);
            for (int tick = 0; tick < JuicerBlockEntity.PROCESS_TICKS; tick++) juicer.serverTick(level);
            var bottle = juicer.take(2);
            h.assertTrue(JuiceItem.carrier(bottle).orElseThrow() == tier && juicer.item(0).isEmpty() && juicer.item(1).isEmpty(),
                    "Every guava presses exactly one matching-tier carrier from one fruit and bottle");
            player.getFoodData().setFoodLevel(10);
            var returned = bottle.getItem().finishUsingItem(bottle,level,player);
            h.assertTrue(returned.is(Items.GLASS_BOTTLE) && player.hasEffect(net.minecraft.world.effect.MobEffects.SATURATION),"Uninfused carriers apply native Saturation and return their glass");
        }
        var seed = new Seed(Genome.DEFAULT,Stats.of(7,8,9,10,11),Genome.DEFAULT,Stats.ZERO,0);
        var ordinary = Infusion.roll(seed,new Random(1234));
        var rich = Infusion.roll(seed,GuavaTier.DRAGON_EGG,new Random(1234));
        h.assertTrue(ordinary.genome().equals(rich.genome()) && ordinary.stats().equals(rich.stats()) && rich.quality() == 20,
                "Richer juice lifts effect quality without buying extra stat drift or gene rolls");
        var genes = SeedItem.create(seed.withMutagen()); genes.set(DataComponents.CUSTOM_NAME,Component.literal("Reserved litter"));
        var carrier = new ItemStack(ModItems.JUICES.get(5).get());
        player.setItemInHand(InteractionHand.MAIN_HAND,genes);
        player.setItemInHand(InteractionHand.OFF_HAND,carrier);
        genes.getItem().use(level,player,InteractionHand.MAIN_HAND);
        h.assertTrue(carrier.isEmpty() && JuiceItem.read(player.getMainHandItem()).orElseThrow().quality() == 12,
                "Native seed use accepts upgraded off-hand carriers and returns the frozen bottle in the paid hand");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        var pending = SeedItem.create(seed); pending.set(DataComponents.CUSTOM_NAME,Component.literal("Saved litter"));
        player.setItemInHand(InteractionHand.MAIN_HAND,pending);
        juicer.insertFromHand(player,pending);
        var juice = new ItemStack(ModItems.JUICES.get(3).get());
        player.setItemInHand(InteractionHand.MAIN_HAND,juice); juicer.insertFromHand(player,juice);
        for (int tick = 0; tick < 40; tick++) juicer.serverTick(level);
        var saved = juicer.saveWithFullMetadata(level.registryAccess());
        var loaded = (JuicerBlockEntity)BlockEntity.loadStatic(pos,juicer.getBlockState(),saved,level.registryAccess());
        h.assertTrue(loaded.progress() == 40 && loaded.item(0).getHoverName().getString().equals("Saved litter"),
                "Exact seed components and unfinished work survive native saves");
        level.setBlockEntity(loaded); juicer = loaded;
        level.setBlock(pos.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        for (int tick = 0; tick < 110; tick++) juicer.serverTick(level);
        h.assertTrue(juicer.progress() == 40 && juicer.item(2).isEmpty(),"Power pauses the current recipe without consuming stock");
        level.setBlock(pos.east(),Blocks.AIR.defaultBlockState(),3);
        for (int tick = 0; tick < 60; tick++) juicer.serverTick(level);
        var frozen = juicer.item(2);
        h.assertTrue(JuiceItem.read(frozen).orElseThrow().quality() == 4 && juicer.stockSignal() == 15,
                "Saved infusion resumes to one paid native output and a full signal");
        var top = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.UP);
        var side = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.EAST);
        var bottom = level.getCapability(Capabilities.Item.BLOCK,pos,Direction.DOWN);
        try (var tx = Transaction.openRoot()) {
            h.assertTrue(bottom.insert(ItemResource.of(frozen),1,tx) == 0 && top.insert(ItemResource.of(Items.GLASS_BOTTLE),1,tx) == 0
                    && side.insert(ItemResource.of(ModItems.SEED.get()),1,tx) == 0,"Directional ports refuse wrong and unreadable inputs");
            h.assertTrue(bottom.extract(ItemResource.of(frozen),1,tx) == 1,"A result can be tentatively extracted");
        }
        h.assertTrue(ItemStack.matches(juicer.item(2),frozen),"Aborted automation restores the exact frozen output");
        player.setItemInHand(InteractionHand.MAIN_HAND,SeedItem.create(seed)); juicer.insertFromHand(player,player.getMainHandItem());
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.GUAVA_JUICE.get())); juicer.insertFromHand(player,player.getMainHandItem());
        for (int tick = 0; tick < 150; tick++) juicer.serverTick(level);
        h.assertTrue(juicer.progress() == 0 && juicer.item(0).getCount() == 1 && juicer.item(1).getCount() == 1
                && ItemStack.matches(juicer.item(2),frozen),"An occupied infusion output retains both new inputs without rerolling or consuming them");
        juicer.take(0); juicer.take(1);
        h.setBlock(relative.below(),Blocks.HOPPER);
        var hopper = (HopperBlockEntity)level.getBlockEntity(pos.below());
        HopperBlockEntity.pushItemsTick(level,pos.below(),hopper.getBlockState(),hopper);
        h.assertTrue(juicer.item(2).isEmpty() && ItemStack.matches(hopper.getItem(0),frozen),"A real lower hopper retrieves the frozen bottle intact");
        // A genuine block-entity ticker must also finish a plain recipe, without manually advancing the station.
        h.setBlock(relative.above(),Blocks.HOPPER);
        h.setBlock(relative.east(),Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.WEST));
        var upper = (HopperBlockEntity)level.getBlockEntity(pos.above());
        var lateral = (HopperBlockEntity)level.getBlockEntity(pos.east());
        upper.setItem(0,new ItemStack(Items.GLASS_BOTTLE)); upper.setItem(1,new ItemStack(ModItems.GUAVA.get()));
        lateral.setItem(0,SeedItem.create(seed)); lateral.setItem(1,new ItemStack(Items.GLASS_BOTTLE));
        HopperBlockEntity.pushItemsTick(level,pos.above(),upper.getBlockState(),upper);
        HopperBlockEntity.pushItemsTick(level,pos.east(),lateral.getBlockState(),lateral);
        h.assertTrue(juicer.item(0).is(ModItems.GUAVA.get()) && juicer.item(1).is(Items.GLASS_BOTTLE)
                && upper.getItem(0).is(Items.GLASS_BOTTLE) && SeedItem.read(lateral.getItem(0)).isPresent(),
                "Real top and side hoppers insert the correct ingredients while preserving wrong-side stock");
        var actual = juicer;
        h.runAfterDelay(101,() -> {
            h.assertTrue(actual.item(0).isEmpty() && actual.item(1).isEmpty()
                    && (actual.item(2).is(ModItems.GUAVA_JUICE.get()) || hopper.getItem(1).is(ModItems.GUAVA_JUICE.get())),
                    "Native loaded-world ticking completes the next recipe");
            h.succeed();
        });
    }
}
