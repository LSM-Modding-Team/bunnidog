package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.PedigreeComparatorBlock;
import com.bunnidogs.mod.menu.SignalPanels;
import com.bunnidogs.mod.block.PedigreeComparatorBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.PedigreeSignal;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Actual imprint/controls, hidden-copy and unphased matching, scheduled signals and vanilla redstone response. */
public final class PedigreeComparatorGameTest {
    private PedigreeComparatorGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 7; z++) h.setBlock(new BlockPos(x,180,z),Blocks.STONE);
        var pos = h.absolutePos(new BlockPos(2,181,3));
        level.setBlockAndUpdate(pos,ModBlocks.PEDIGREE_COMPARATOR.get().defaultBlockState().setValue(PedigreeComparatorBlock.FACING,Direction.EAST));
        var detector = (PedigreeComparatorBlockEntity)level.getBlockEntity(pos);
        var comparatorPos = pos.west(); var lampPos = pos.west(2);
        level.setBlockAndUpdate(comparatorPos,Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING,Direction.EAST));
        level.setBlockAndUpdate(lampPos,Blocks.REDSTONE_LAMP.defaultBlockState());
        var animal = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,181,3);
        animal.setGenome(Genome.DEFAULT); animal.setStats(Stats.ZERO); animal.setHealth(animal.getMaxHealth());
        int longEar = Gene.EARS.alleleNamed("long").id();
        var wanted = Genome.DEFAULT.with(Gene.EARS,1,longEar);
        var swapped = Genome.DEFAULT.with(Gene.EARS,0,longEar);
        h.assertTrue(PedigreeSignal.matchingPairs(wanted,swapped) == 7 && PedigreeSignal.matchingPairs(wanted,Genome.DEFAULT) == 6
                && wanted.traits().equals(Genome.DEFAULT.traits()),"Matching ignores parental copy order but must distinguish hidden copies behind the same phenotype");
        var sample = TissueSampleItem.create(wanted,Stats.of(10,20,30,40,50));
        sample.set(DataComponents.CUSTOM_NAME,Component.literal("Sort this carrier")); var original = sample.copy();
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.snapTo(Vec3.atCenterOf(pos).add(0,0,2));
        player.setItemInHand(InteractionHand.OFF_HAND,sample);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        h.runAtTickTime(3,() -> {
            detector.getBlockState().useItemOn(player.getOffhandItem(),level,player,InteractionHand.OFF_HAND,hit);
            h.assertTrue(detector.reference().orElseThrow().equals(TissueSampleItem.read(sample).orElseThrow()) && ItemStack.matches(player.getOffhandItem(),original)
                    && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 0,
                    "Off-hand imprint copies all reference data without consuming/altering the named sample; a hidden-copy mismatch gives exact output zero");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            CareMenuGameTest.click(h,player,pos,SignalPanels.ANALOG);
            h.assertTrue(detector.getBlockState().getValue(PedigreeComparatorBlock.ANALOG)
                    && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 13,"An explicit native mode switch exposes six matched pairs as analog 13");
        });
        h.runAtTickTime(8,() -> {
            h.assertTrue(((ComparatorBlockEntity)level.getBlockEntity(comparatorPos)).getOutputSignal() == 13
                    && level.getBlockState(lampPos).getValue(RedstoneLampBlock.LIT),"Vanilla comparators and lamps must receive the detector's real scheduled output");
            animal.setGenome(swapped); animal.setStats(Stats.of(500,400,300,200,100)); animal.setBaby(true);
        });
        h.runAtTickTime(18,() -> {
            h.assertTrue(detector.read().pairs() == 7 && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 15,
                    "Unphased seven-pair matches, changed bred stats and pups must reach the same exact genetic signal");
            var save = detector.saveWithFullMetadata(level.registryAccess());
            var restored = (PedigreeComparatorBlockEntity)BlockEntity.loadStatic(pos,detector.getBlockState(),save,level.registryAccess());
            h.assertTrue(restored.reference().equals(detector.reference()),"The whole copied sample survives native block-entity saves");
            var tool = new ItemStack(ModItems.CALIPERS.get()); player.setItemInHand(InteractionHand.OFF_HAND,tool); player.setShiftKeyDown(true);
            var before = detector.saveWithFullMetadata(level.registryAccess());
            tool.useOn(new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.OFF_HAND,hit));
            h.assertTrue(before.equals(detector.saveWithFullMetadata(level.registryAccess())) && tool.getDamageValue() == 0,
                    "Calipers must inspect without changing the reference, mode, power or tool");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setShiftKeyDown(false);
            CareMenuGameTest.click(h,player,pos,SignalPanels.EXACT);
            animal.setGenome(Genome.DEFAULT);
        });
        h.runAtTickTime(33,() -> {
            h.assertTrue(!detector.getBlockState().getValue(PedigreeComparatorBlock.ANALOG)
                    && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 0
                    && !level.getBlockState(lampPos).getValue(RedstoneLampBlock.LIT),"A changed hidden copy must turn native exact-match circuits off");
            animal.setGenome(swapped);
            var extra = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,181,3); extra.setGenome(swapped); extra.setBaby(true);
            h.runAfterDelay(12,() -> {
                h.assertTrue(detector.read().status() == PedigreeSignal.Status.CROWDED && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 0,
                        "Multiple animals on the same pad must fail closed rather than choose a favored genotype"); extra.discard();
                level.setBlockAndUpdate(pos.east(),Blocks.GLASS.defaultBlockState());
                h.assertTrue(detector.read().status() == PedigreeSignal.Status.BLOCKED,"A blocked front pad cannot read through a solid block");
                level.setBlockAndUpdate(pos.east(),Blocks.AIR.defaultBlockState());
                animal.setPos(animal.position().add(3,0,0));
                h.assertTrue(detector.read().status() == PedigreeSignal.Status.ABSENT,"Moving the animal off the one-block pad removes its reading");
                player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); player.setShiftKeyDown(true);
                detector.getBlockState().useWithoutItem(level,player,hit);
                h.assertTrue(detector.reference().isPresent(),"Adventure inspection must not clear another keeper's reference");
                player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                detector.getBlockState().useWithoutItem(level,player,hit);
                h.assertTrue(detector.reference().isEmpty() && detector.getBlockState().getValue(PedigreeComparatorBlock.POWER) == 0
                        && !detector.getBlockState().getValue(PedigreeComparatorBlock.REFERENCE),"Deliberate sneak-empty clearing removes the copied reference and all output");
                animal.discard(); h.succeed();
            });
        });
    }
}
