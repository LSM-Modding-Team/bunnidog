package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.menu.AnimalPanels;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/** A normal AI companion must obey the selected command after the menu releases its attention. */
public final class InteractionCommandsGameTest {
    private InteractionCommandsGameTest() {}
    public static void run(GameTestHelper h) {
        int floor=175;
        for (int x=0;x<8;x++) for (int z=0;z<8;z++) {
            h.setBlock(x,floor,z,Blocks.STONE);
            for (int y=1;y<5;y++) h.setBlock(x,floor+y,z,Blocks.AIR);
        }
        var dog=h.spawn(ModEntities.BUNNIDOG.get(),2.5F,floor+1,3.5F);
        int loyal=Gene.TEMPERAMENT.alleleNamed("loyal").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.TEMPERAMENT,0,loyal).with(Gene.TEMPERAMENT,1,loyal));
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        var player=h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.snapTo(dog.position().add(4,0,0));
        var guava=new ItemStack(ModItems.GUAVA.get()); player.getInventory().setItem(9,guava);
        player.getInventory().setItem(10,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        CareMenu.open(player,dog);
        h.runAfterDelay(2,()->{
            var menu=(CareMenu)player.containerMenu;
            h.assertTrue(menu.clickMenuButton(player,menu.buttonId(AnimalPanels.ORDERS)) && menu.clickMenuButton(player,menu.buttonId(CarePanels.TRAIN)),"A normal animal is befriended through its real interaction choices");
            h.assertTrue(guava.isEmpty() && player.containerMenu!=menu && dog.conversation().viewer()==null && dog.trail().belongsTo(player),"Successful befriending pays once and releases conversation immediately");
            CareMenu.open(player,dog); menu=(CareMenu)player.containerMenu;
            h.assertTrue(menu.clickMenuButton(player,menu.buttonId(AnimalPanels.ORDERS)) && menu.clickMenuButton(player,menu.buttonId(CarePanels.WAIT)) && player.containerMenu!=menu,"The actual Wait choice returns to the world");
            var origin=dog.position();
            h.runAfterDelay(15,()->{
                h.assertTrue(dog.trail().order()==Trail.Order.WAIT && dog.position().distanceToSqr(origin)<.1 && !dog.isNoAi(),"Waiting persists after the menu closes, with ordinary AI still enabled");
                CareMenu.open(player,dog); var follow=(CareMenu)player.containerMenu;
                h.assertTrue(follow.clickMenuButton(player,follow.buttonId(AnimalPanels.ORDERS)) && follow.clickMenuButton(player,follow.buttonId(CarePanels.FOLLOW)),"The real Follow choice commits");
                h.assertTrue(player.containerMenu!=follow && dog.conversation().viewer()==null,"Follow cannot leave a conversation goal pinning the companion");
                h.succeedWhen(()->h.assertTrue(dog.position().distanceToSqr(origin)>.5 && dog.distanceToSqr(player)<12 && dog.trail().order()==Trail.Order.FOLLOW,"The actual follower walks toward its keeper after the command"));
            });
        });
    }
}
