package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.BreedingPlan;
import com.bunnidogs.mod.genetics.Cross;
import com.bunnidogs.mod.item.TablePlanning;
import com.bunnidogs.mod.network.TablePayload;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class TablePlanningGameTest {
    private TablePlanningGameTest() {}
    public static void run(GameTestHelper h) {
        var pos = new BlockPos(2, 1, 2);
        h.setBlock(pos, ModBlocks.GENETICS_TABLE.get());
        var level = h.getLevel();
        var absolute = h.absolutePos(pos);
        var state = level.getBlockState(absolute);
        var table = (GeneticsTableBlockEntity) level.getBlockEntity(absolute);
        var recipient = TissueSampleItem.create(Genome.DEFAULT, Stats.of(7, 8, 9, 10, 11));
        int glow = Gene.COAT.alleleNamed("glow").id();
        var donor = TissueSampleItem.create(Genome.DEFAULT.with(Gene.COAT, 0, glow).with(Gene.COAT, 1, glow), Stats.ZERO);
        table.setItem(0, recipient); table.setItem(1, donor);
        // The native mock connection has no NeoForge payload negotiation; exercise the block without sending UI packets.
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var splicer = new ItemStack(ModItems.GENE_SPLICER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, splicer);
        state.useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        h.assertTrue(ItemStack.matches(recipient, table.item(0)) && ItemStack.matches(donor, table.item(1))
                && splicer.getDamageValue() == 0, "Reading a splice plan must not consume or change samples or tool");
        var saved = table.saveWithFullMetadata(level.registryAccess());
        var restored = (GeneticsTableBlockEntity) BlockEntity.loadStatic(absolute, state, saved, level.registryAccess());
        var sheet = new TableSheet(TissueSampleItem.read(restored.item(0)), TissueSampleItem.read(restored.item(1)));
        h.assertTrue(sheet.spreadCount() == 5 && sheet.sample(0).stats().equals(Stats.of(7, 8, 9, 10, 11))
                && sheet.sample(1).genome().alleleId(Gene.COAT, 0) == glow, "Saved table contents must retain both exact planning inputs");
        var golden = new ItemStack(ModItems.GOLDEN_GUAVA.get(),3);
        var diamond = new ItemStack(ModItems.DIAMOND_GUAVA.get(),4);
        golden.set(DataComponents.CUSTOM_NAME,Component.literal("Mother's planned feed"));
        diamond.set(DataComponents.CUSTOM_NAME,Component.literal("Father's planned feed"));
        var beforeGolden = golden.copy(); var beforeDiamond = diamond.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,golden); player.setItemInHand(InteractionHand.OFF_HAND,diamond);
        state.useWithoutItem(level,player,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false));
        var planned = TablePlanning.snapshot(table,player);
        h.assertTrue(planned.plan().equals(new BreedingPlan(1,2,false))
                && new Cross(planned.a().orElseThrow(),planned.b().orElseThrow(),planned.plan()).quality() == 1.5
                && ItemStack.matches(golden,beforeGolden) && ItemStack.matches(diamond,beforeDiamond),
                "Two actual named guavas must preview their average quality without spending or editing cues");
        var vial = new ItemStack(ModItems.MUTAGEN.get(),2); var beforeVial = vial.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,vial);
        state.useWithoutItem(level,player,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false));
        var treated = TablePlanning.snapshot(table,player);
        var treatedCross = new Cross(treated.a().orElseThrow(),treated.b().orElseThrow(),treated.plan());
        h.assertTrue(treated.plan().equals(new BreedingPlan(2,2,true)) && treatedCross.quality() == 2
                && treatedCross.mutationChance() == .5 && ItemStack.matches(vial,beforeVial) && ItemStack.matches(diamond,beforeDiamond)
                && ItemStack.matches(recipient,table.item(0)) && ItemStack.matches(donor,table.item(1)),
                "One held guava applies to both parents; an opposite Mutagen previews treatment while preserving all table/cue items");
        var pick = new ItemStack(Items.DIAMOND_PICKAXE);
        var fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        pick.enchant(fortune,3); pick.setDamageValue(17);
        pick.set(DataComponents.CUSTOM_NAME,Component.literal("Planned harvest tool")); var beforePick = pick.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,pick);
        state.useWithoutItem(level,player,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false));
        var harvest = TablePlanning.snapshot(table,player);
        var harvestCross = new Cross(harvest.a().orElseThrow(),harvest.b().orElseThrow(),harvest.plan());
        h.assertTrue(harvest.plan().equals(new BreedingPlan(2,2,false,3)) && harvestCross.quality() == 2 && harvestCross.effectiveQuality() == 3.5
                && ItemStack.matches(pick,beforePick) && ItemStack.matches(diamond,beforeDiamond),
                "Actual gameplay Fortune must add only the future harvest bonus without wearing the named tool or editing the feed");
        var secondPick = pick.copy(); secondPick.enchant(fortune,2);
        player.setItemInHand(InteractionHand.OFF_HAND,secondPick);
        h.assertTrue(TablePlanning.snapshot(table,player).plan().fortune() == 3,"Two held Fortune tools choose the highest level rather than adding them");
        var bytes = Unpooled.buffer();
        try {
            TablePayload.STREAM_CODEC.encode(bytes,new TablePayload(harvest.toString()));
            h.assertTrue(TableSheet.parse(TablePayload.STREAM_CODEC.decode(bytes).table()).orElseThrow().equals(harvest),
                    "The actual native table payload codec must retain the server's plan snapshot");
        } finally { bytes.release(); }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        h.assertTrue(TablePlanning.snapshot(table,player).plan().equals(BreedingPlan.PLAIN)
                && planned.plan().equals(new BreedingPlan(1,2,false)),"Clearing held cues restores defaults without rewriting an already captured snapshot");
    }
}
