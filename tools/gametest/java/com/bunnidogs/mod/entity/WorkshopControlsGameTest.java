package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.WorkshopPanels;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Native open/button paths and conservation of the sample/entrant the player actually saw. */
public final class WorkshopControlsGameTest {
    private WorkshopControlsGameTest() {}
    private static void press(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        if (com.bunnidogs.mod.menu.StationInventory.transferButton(action)) { CareMenuGameTest.transfer(h,player,menu,action); return; }
        int button=menu.buttonId(action);
        h.assertTrue(menu.view().permits(button),"The displayed workshop control must be enabled: "+action);
        player.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,button));
    }
    private static boolean owns(ServerPlayer player,ItemStack expected) {
        return java.util.stream.IntStream.range(0,player.getInventory().getContainerSize())
                .anyMatch(i->ItemStack.matches(player.getInventory().getItem(i),expected));
    }
    public static void run(GameTestHelper h) {
        int y=368; var level=h.getLevel();
        for (int x=0;x<=12;x++) for (int z=0;z<=9;z++) {
            h.setBlock(x,y,z,Blocks.STONE);
            for (int up=1;up<=4;up++) h.setBlock(x,y+up,z,Blocks.AIR);
        }
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var pos=h.absolutePos(new BlockPos(3,y+1,3)); level.setBlock(pos,ModBlocks.GENETICS_TABLE.get().defaultBlockState(),3);
        var table=(GeneticsTableBlockEntity)level.getBlockEntity(pos);
        var before=table.saveWithFullMetadata(level.registryAccess());
        var menu=CareMenuGameTest.open(h,player,pos);
        h.assertTrue(before.equals(table.saveWithFullMetadata(level.registryAccess())) && !menu.view().permits(menu.buttonId(WorkshopPanels.READ_TABLE)),
                "Opening an empty Table does not edit stock or publish an unavailable analysis");
        var a=TissueSampleItem.create(Genome.DEFAULT,Stats.of(4,5,6,7,8));
        var b=TissueSampleItem.create(Genome.DEFAULT.with(Gene.COAT,0,2).with(Gene.COAT,1,2),Stats.of(9,8,7,6,5));
        a.set(DataComponents.CUSTOM_NAME,Component.literal("Founding parent")); b.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen mate"));
        var expectedA=a.copy(); var expectedB=b.copy();
        player.getInventory().setItem(9,a);
        menu=CareMenuGameTest.open(h,player,pos); int oldChoice=menu.buttonId(WorkshopPanels.GOALS);
        player.getInventory().setItem(9,b); player.getInventory().setItem(10,a);
        h.assertTrue(!menu.clickMenuButton(player,(oldChoice&~255)|WorkshopPanels.ADD_SAMPLE) && table.isEmpty(0) && owns(player,expectedA) && owns(player,expectedB),
                "An old transfer-button packet cannot place an automatically selected sample");
        h.assertTrue(menu.clickMenuButton(player,oldChoice) && table.isEmpty(0) && owns(player,expectedA) && owns(player,expectedB),
                "Reordering unrelated inventory no longer invalidates a simple breeding-goal choice");
        press(h,player,menu,WorkshopPanels.BACK_TABLE);
        press(h,player,menu,WorkshopPanels.ADD_SAMPLE);
        h.assertTrue(ItemStack.matches(table.item(0),expectedB) && !owns(player,expectedB) && owns(player,expectedA),
                "The selected real inventory sample enters A through native clicks");
        press(h,player,menu,WorkshopPanels.ADD_SAMPLE);
        h.assertTrue(ItemStack.matches(table.item(1),expectedA) && !owns(player,expectedA)
                && menu.view().controls().stream().noneMatch(c->com.bunnidogs.mod.menu.StationInventory.transferButton(c.id()&255)),"The native slots fill B without a bank of automatic placement/retrieval buttons");
        CohesionGameTest.preview("Genetics Table",menu);
        var stored=table.saveWithFullMetadata(level.registryAccess());
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        menu=CareMenuGameTest.open(h,player,pos);
        menu.clicked(0,0,net.minecraft.world.inventory.ContainerInput.QUICK_MOVE,player);
        h.assertTrue(menu.getCarried().isEmpty() && stored.equals(table.saveWithFullMetadata(level.registryAccess())),"Adventure inspection cannot withdraw a sample");
        press(h,player,menu,WorkshopPanels.READ_TABLE);
        h.assertTrue(player.containerMenu!=menu && stored.equals(table.saveWithFullMetadata(level.registryAccess())),
                "Adventure can open the existing native breeding reader without editing or spending samples");
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        player.setShiftKeyDown(true); level.getBlockState(pos).useWithoutItem(level,player,hit); player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND,TissueSampleItem.create(Genome.DEFAULT,Stats.ZERO));
        level.getBlockState(pos).useItemOn(player.getMainHandItem(),level,player,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(stored.equals(table.saveWithFullMetadata(level.registryAccess())),"Direct sneaking/sample swaps obey the same Adventure mutation rule");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        menu=CareMenuGameTest.open(h,player,pos); int oldTake=menu.buttonId(WorkshopPanels.READ_TABLE);
        table.setItem(0,expectedA.copy());
        h.assertTrue(!menu.clickMenuButton(player,oldTake) && !owns(player,expectedA),"Changed parents refuse an old analysis intent");
        table.setItem(0,expectedB.copy()); menu=CareMenuGameTest.open(h,player,pos);
        press(h,player,menu,WorkshopPanels.TAKE_A); press(h,player,menu,WorkshopPanels.TAKE_B);
        h.assertTrue(table.isEmpty(0) && table.isEmpty(1) && owns(player,expectedA) && owns(player,expectedB),
                "Independent retrieval preserves both complete named genetic samples");
        var replaced=CareMenuGameTest.open(h,player,pos); level.removeBlock(pos,false); level.setBlock(pos,ModBlocks.GENETICS_TABLE.get().defaultBlockState(),3);
        h.assertTrue(!replaced.stillValid(player) && !replaced.clickMenuButton(player,replaced.buttonId(WorkshopPanels.ADD_SAMPLE)),
                "A replacement Table invalidates the old target even at the same position");

        var stand=pos.east(5); level.setBlock(stand,ModBlocks.JUDGING_STAND.get().defaultBlockState(),3);
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),9,y+1,3); dog.setAge(0);
        dog.setParents(Genome.DEFAULT,Genome.DEFAULT); dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.of(5,5,5,5,5)); dog.setHealth(dog.getMaxHealth());
        dog.setLeashedTo(player,false);
        var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(saved);
        menu=CareMenuGameTest.open(h,player,stand);
        var after=net.minecraft.world.level.storage.TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(after);
        h.assertTrue(saved.buildResult().equals(after.buildResult()) && dog.highestShowAward()==0,"Opening a ready show scorecard does not judge or change the entrant");
        CohesionGameTest.preview("Judging Stand",menu);
        int oldAward=menu.buttonId(WorkshopPanels.AWARD);
        var other=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),8,y+1,4); other.setAge(0);
        other.setParents(Genome.DEFAULT,Genome.DEFAULT); other.setGenome(Genome.DEFAULT); other.setStats(Stats.of(5,5,5,5,5)); other.setHealth(other.getMaxHealth());
        dog.removeLeash(); other.setLeashedTo(player,false);
        h.assertTrue(!menu.clickMenuButton(player,oldAward) && dog.highestShowAward()==0 && other.highestShowAward()==0,
                "A new qualifying led entrant cannot receive the previous entrant's award click");
        oldAward=menu.buttonId(WorkshopPanels.AWARD); other.setStats(Stats.of(20,20,20,20,20));
        h.assertTrue(!menu.clickMenuButton(player,oldAward) && other.highestShowAward()==0,"Changing the winning snapshot also invalidates a shown award intent");
        other.setHealth(1); h.assertTrue(!menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.AWARD)) && other.highestShowAward()==0,
                "An injury is rechecked at submission even without a periodic view update");
        other.setHealth(other.getMaxHealth()); menu=CareMenuGameTest.open(h,player,stand);
        press(h,player,menu,WorkshopPanels.AWARD);
        h.assertTrue(other.highestShowAward()==2 && !menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.AWARD))
                && player.getInventory().countItem(ModItems.SILVER_RIBBON.get())==1,"Explicit judging awards the actual current ribbon once");
        other.setStats(Stats.of(50,50,50,50,50)); player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        menu=CareMenuGameTest.open(h,player,stand);
        h.assertTrue(!menu.clickMenuButton(player,menu.buttonId(WorkshopPanels.AWARD)) && other.highestShowAward()==2,
                "Adventure scorecards cannot claim awards");
        other.setGenome(other.genome().with(Gene.DIET,0,1));
        h.assertTrue(com.bunnidogs.mod.menu.WorkshopPanels.show(player,stand).rows().size()<=12,
                "An unfinished entrant with a mixed gene pair still fits the bounded view");
    }
}
