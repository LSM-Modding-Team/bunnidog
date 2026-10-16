package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
    }
}
