package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.ShowRibbonItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.WorkshopPanels;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ShowGameTest {
    private ShowGameTest() {}
    private static long count(Player player, Item item) {
        long count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var pos = new BlockPos(3, 1, 3);
        h.setBlock(pos, ModBlocks.JUDGING_STAND.get());
        var absolute = h.absolutePos(pos);
        var state = level.getBlockState(absolute);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
        var player = h.makeMockServerPlayerInLevel();
        CareMenuGameTest.prepare(player);
        player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(3, 1, 2))));
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Runnable judge = () -> {
            player.closeContainer();
            state.useWithoutItem(level,player,hit);
            if (player.containerMenu instanceof CareMenu menu) menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.AWARD));
        };
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 3, 1, 4);
        dog.setAge(0); dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.of(5,5,5,5,5));
        dog.setHealth(dog.getMaxHealth());
        judge.run();
        h.assertTrue(dog.highestShowAward() == 0, "Unled animals must not be judged");
        dog.setLeashedTo(player, false);
        judge.run();
        h.assertTrue(dog.highestShowAward() == 0, "Wild animals without birth parents must not qualify");
        dog.setParents(Genome.DEFAULT, Genome.DEFAULT);
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET, 1, 1));
        judge.run();
        h.assertTrue(dog.highestShowAward() == 0, "Mixed Diet must prevent a show award");
        dog.setGenome(Genome.DEFAULT); dog.setHealth(dog.getMaxHealth() * .25F);
        judge.run();
        h.assertTrue(dog.highestShowAward() == 0, "Injured animals must recover before judging");
        dog.setHealth(dog.getMaxHealth());
        dog.setStats(Stats.of(5,5,5,5,4));
        judge.run();
        h.assertTrue(dog.highestShowAward() == 0, "One insufficient bred stat must prevent a ribbon");
        dog.setStats(Stats.of(5,5,5,5,5));
        float health = dog.getHealth();
        player.setShiftKeyDown(true);
        judge.run();
        player.setShiftKeyDown(false);
        h.assertTrue(dog.highestShowAward() == 0 && count(player, ModItems.BRONZE_RIBBON.get()) == 0,
                "Sneaking must show a read-only scorecard without claiming an award");
        judge.run();
        h.assertTrue(dog.highestShowAward() == 1 && count(player, ModItems.BRONZE_RIBBON.get()) == 1,
                "A healthy balanced stable adult must receive exactly one bronze ribbon");
        h.assertTrue(dog.getHealth() == health && dog.isLeashed() && dog.getLeashHolder() == player
                && dog.genome().equals(Genome.DEFAULT) && dog.stats().equals(Stats.of(5,5,5,5,5)),
                "Judging must preserve health, leash, genes and stats");
        judge.run();
        h.assertTrue(count(player, ModItems.BRONZE_RIBBON.get()) == 1, "Repeated judging must not duplicate a ribbon");
        dog.setStats(Stats.of(20,20,20,20,20));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LEAD));
        state.useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hit);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        h.assertTrue(dog.highestShowAward() == 2 && count(player, ModItems.SILVER_RIBBON.get()) == 1,
                "Lead interaction must permit one higher-tier silver award");
        dog.setStats(Stats.of(50,50,50,50,50));
        judge.run();
        h.assertTrue(dog.highestShowAward() == 3 && count(player, ModItems.GOLD_RIBBON.get()) == 1, "Gold milestone must award once");
        var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs", "champion_line"));
        h.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "Native gold judging must award Champion Line");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        dog.saveWithoutId(out);
        var saved = out.buildResult();
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        dog.setLeashedTo(player, false);
        judge.run();
        h.assertTrue(dog.highestShowAward() == 3 && count(player, ModItems.GOLD_RIBBON.get()) == 1,
                "Reloading and rejudging must not duplicate the gold reward");
        saved.putInt("ShowAward", 99);
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.highestShowAward() == 99 && !dog.recordShowAward(3), "Unknown future tiers must block old rewards");
        saved.remove("ShowAward");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
        h.assertTrue(dog.highestShowAward() == 0, "Legacy animals must default to unjudged");

        ItemStack gold = ItemStack.EMPTY;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var item = player.getInventory().getItem(i);
            if (item.is(ModItems.GOLD_RIBBON.get())) gold = item.copy();
        }
        var entry = ShowRibbonItem.read(gold).orElseThrow();
        h.assertTrue(entry.id().orElseThrow().equals(dog.getUUID()) && entry.stats().orElseThrow().equals(Stats.of(50,50,50,50,50))
                && entry.mother().orElseThrow().equals(Genome.DEFAULT), "Ribbon must contain the exact winning snapshot");
        var pedestalPos = h.absolutePos(new BlockPos(5, 1, 3));
        level.setBlock(pedestalPos, ModBlocks.TROPHY_PEDESTAL.get().defaultBlockState(), 3);
        var pedestal = (TrophyPedestalBlockEntity) level.getBlockEntity(pedestalPos);
        pedestal.setItem(gold);
        var restored = (TrophyPedestalBlockEntity) BlockEntity.loadStatic(pedestalPos, pedestal.getBlockState(),
                pedestal.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
        h.assertTrue(ShowRibbonItem.read(restored.item()).orElseThrow().equals(entry), "Pedestal save/load must keep the winning record");
    }
}
