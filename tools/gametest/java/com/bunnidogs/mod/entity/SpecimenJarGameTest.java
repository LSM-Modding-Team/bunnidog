package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.SpecimenJarBlock;
import com.bunnidogs.mod.block.SpecimenJarBlockEntity;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SpecimenJarGameTest {
    private SpecimenJarGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(3,1,3));
        h.setBlock(3,1,3,ModBlocks.SPECIMEN_JAR.get()); h.setBlock(4,0,3,Blocks.STONE);
        h.setBlock(new BlockPos(4,1,3),Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.WEST));
        var jar = (SpecimenJarBlockEntity)level.getBlockEntity(pos);
        var comparator = (ComparatorBlockEntity)level.getBlockEntity(pos.east());
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        for (var coat : Gene.COAT.alleles()) {
            var genome = Genome.DEFAULT.with(Gene.COAT,0,coat.id()).with(Gene.COAT,1,coat.id());
            var sample = TissueSampleItem.create(genome,Stats.of(1,2,3,4,5));
            sample.setCount(2); sample.set(DataComponents.CUSTOM_NAME,Component.literal("Museum original "+coat.name()));
            player.setItemInHand(InteractionHand.OFF_HAND,sample);
            player.gameMode.useItemOn(player,level,sample,InteractionHand.OFF_HAND,hit);
            h.assertTrue(sample.getCount() == 1 && jar.getBlockState().getValue(SpecimenJarBlock.SPECIMEN) == coat.id()+1
                    && ItemStack.matches(jar.sample(),sample),"Native off-hand insertion pays one sample and displays its actual saved coat id");
            var saved = jar.saveWithFullMetadata(level.registryAccess());
            var loaded = (SpecimenJarBlockEntity)BlockEntity.loadStatic(pos,jar.getBlockState(),saved,level.registryAccess());
            h.assertTrue(ItemStack.matches(loaded.sample(),jar.sample()) && loaded.fixedSignal() == 15,"Native jar saves retain every sample component and all seven fixed pairs");
            jar.take();
        }
        var unreadable = new ItemStack(ModItems.TISSUE_SAMPLE.get());
        h.assertTrue(!jar.insertFromHand(player,unreadable) && unreadable.getCount() == 1,"Blank samples cannot occupy a jar");
        var mixed = Genome.DEFAULT;
        for (var gene : Gene.values()) mixed = mixed.with(gene,0,0).with(gene,1,1);
        var sample = TissueSampleItem.create(mixed,Stats.ZERO); player.setItemInHand(InteractionHand.OFF_HAND,sample);
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        player.gameMode.useItemOn(player,level,sample,InteractionHand.OFF_HAND,hit);
        h.assertTrue(jar.sample().isEmpty() && sample.getCount() == 1,"Read-only adventure use cannot store or spend a sample");
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        player.gameMode.useItemOn(player,level,sample,InteractionHand.OFF_HAND,hit);
        h.assertTrue(sample.getCount() == 1 && jar.fixedSignal() == 1,"Creative insertion keeps the actual hand and mixed pairs give a presence-only signal");
        h.runAfterDelay(3,() -> {
            h.assertTrue(comparator.getOutputSignal() == 1,"A real comparator reads zero fixed pairs as one");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setShiftKeyDown(true);
            jar.getBlockState().useWithoutItem(level,player,hit); player.setShiftKeyDown(false);
            h.assertTrue(jar.sample().isEmpty() && jar.getBlockState().getValue(SpecimenJarBlock.SPECIMEN) == 0,"Deliberate empty-hand withdrawal clears the vial and visual state");
            var fixed = TissueSampleItem.create(Genome.DEFAULT,Stats.of(0,0,0,0,80)); fixed.set(DataComponents.CUSTOM_NAME,Component.literal("Original stable line"));
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setItemInHand(InteractionHand.OFF_HAND,fixed);
            player.gameMode.useItemOn(player,level,fixed,InteractionHand.OFF_HAND,hit);
            h.runAfterDelay(3,() -> {
                h.assertTrue(comparator.getOutputSignal() == 15,"Seven two-copy pairs reach the real full comparator signal");
                level.destroyBlock(pos,true);
                int samples = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),e -> e.getItem().is(ModItems.TISSUE_SAMPLE.get())
                        && e.getItem().getHoverName().getString().equals("Original stable line")).stream().mapToInt(e -> e.getItem().getCount()).sum();
                int jars = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),e -> e.getItem().is(ModItems.SPECIMEN_JAR.get())).stream().mapToInt(e -> e.getItem().getCount()).sum();
                h.assertTrue(samples == 1 && jars == 1,"Breaking a jar returns exactly its container and full-component original sample");
                h.succeed();
            });
        });
    }
}
