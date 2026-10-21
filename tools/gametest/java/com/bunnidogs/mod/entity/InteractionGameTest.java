package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.menu.AnimalPanels;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.CombatPanel;
import com.bunnidogs.mod.menu.RolePanel;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/** First contact exposes actions; real tools alone expose measurements, with normal hand dispatch intact. */
public final class InteractionGameTest {
    private InteractionGameTest() {}
    public static void run(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.prepare(player);
        var dog=h.spawn(ModEntities.BUNNIDOG.get(),2.5F,180,2.5F);
        dog.setNoAi(true); dog.setAge(0); dog.setHealth(dog.getMaxHealth());
        player.snapTo(dog.position().add(1,0,0));
        var stone=new ItemStack(Items.STONE,32); player.setItemInHand(InteractionHand.MAIN_HAND,stone);
        var genome=dog.genome(); var stats=dog.stats();
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.containerMenu instanceof CareMenu,"Ordinary held blocks must not prevent interaction");
        var menu=(CareMenu)player.containerMenu;
        h.assertTrue(menu.view().rows().isEmpty() && menu.view().next().getString().isEmpty()
                        && menu.view().controls().size()==3 && menu.view().subjectId()==dog.getId()
                        && menu.buttonId(CarePanels.GENETICS)<0 && menu.buttonId(CarePanels.CARE)<0,
                "First contact must show three action groups, no unsolicited instructions/measurements and no hidden care verbs");
        CohesionGameTest.preview("First contact",menu.view());
        h.assertTrue(stone.getCount()==32 && dog.genome().equals(genome) && dog.stats().equals(stats),
                "Opening with an ordinary item must preserve supplies and the animal");
        var work=RolePanel.view(player,dog); var combat=CombatPanel.view(player,dog);
        dog.setStats(Stats.of(31,37,41,43,47));
        h.assertTrue(RolePanel.view(player,dog).equals(work) && CombatPanel.view(player,dog).rows().equals(combat.rows()),
                "Changing hidden bred points must not change ordinary role or combat readouts");
        dog.setStats(stats);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(AnimalPanels.CARE_MENU)),"The named Care group opens");
        h.assertTrue(menu.view().rows().isEmpty() && menu.view().controls().stream()
                .filter(c->(c.id()&255)==CarePanels.GROOM).allMatch(c->!c.enabled() && !c.hint().getString().isEmpty()),
                "Missing equipment is explained on its unavailable action, without another guide paragraph");
        CohesionGameTest.preview("Care without tools",menu.view());
        var wire=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
        try {
            var packet=new CareViewPayload(menu.containerId,menu.view()); CareViewPayload.STREAM_CODEC.encode(wire,packet);
            h.assertTrue(CareViewPayload.STREAM_CODEC.decode(wire).equals(packet),"Action reasons and the subject survive the native wire codec");
        } finally { wire.release(); }
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.BACK)),"Care returns to the action menu");
        player.getInventory().setItem(9,new ItemStack(ModItems.CALIPERS.get()));
        for (int i=0;i<11;i++) menu.broadcastChanges();
        int measured=menu.buttonId(CarePanels.GENETICS);
        h.assertTrue(measured>=0 && menu.view().rows().isEmpty(),"Carrying Calipers offers a deliberate Measure action without dumping data");
        player.getInventory().setItem(9,ItemStack.EMPTY);
        h.assertTrue(!menu.clickMenuButton(player,measured) && menu.buttonId(CarePanels.GENETICS)<0,
                "Losing the real Calipers rejects a stale measurement action and hides it");
        player.closeContainer();
        dog.setHealth(dog.getMaxHealth()-Foraging.HEAL);
        var food=new ItemStack(ModItems.GUAVA.get(),2); player.setItemInHand(InteractionHand.OFF_HAND,food);
        h.assertTrue(!player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),
                "Ordinary main-hand items must defer to real off-hand ingredients");
        player.setShiftKeyDown(true); player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO); player.setShiftKeyDown(false);
        h.assertTrue(food.getCount()==1 && stone.getCount()==32 && dog.getHealth()==dog.getMaxHealth()
                        && !(player.containerMenu instanceof CareMenu),"Off-hand feeding remains a paid direct action, not a menu interception");
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.CALIPERS.get()));
        h.assertTrue(!player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),"Off-hand Calipers retain priority");
        player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(!(player.containerMenu instanceof CareMenu),"Direct Calipers still use the existing detailed reader");
        dog.discard(); player.discard();
    }
}
