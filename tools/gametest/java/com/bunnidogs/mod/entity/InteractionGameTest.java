package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.menu.AnimalPanels;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.CombatPanel;
import com.bunnidogs.mod.menu.RolePanel;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModMenus;
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
        var opening=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
        try {
            menu.writeOpeningData(opening);
            var client=ModMenus.CARE.get().create(menu.containerId,player.getInventory(),opening);
            h.assertTrue(client.view()!=null && client.view().equals(menu.view()) && client.view().subjectId()==dog.getId(),
                    "The registered client factory must have the complete animal view before a screen or update packet exists");
        } finally { opening.release(); }
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
        var foodMenu=(CareMenu)player.containerMenu;
        h.assertTrue(food.getCount()==2 && !dog.isInLove() && dog.getHealth()<dog.getMaxHealth(),"Opening the guava menu does not use its food");
        CohesionGameTest.preview("Guava choices",foodMenu.view());
        h.assertTrue(foodMenu.clickMenuButton(player,foodMenu.buttonId(CarePanels.CARE)),"Choose the offered guava's care action");
        h.assertTrue(food.getCount()==1 && stone.getCount()==32 && dog.getHealth()==dog.getMaxHealth()
                        && !(player.containerMenu instanceof CareMenu),"Choosing off-hand care spends the offered guava and returns to the world");
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.CALIPERS.get()));
        h.assertTrue(!player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),"Off-hand Calipers retain priority");
        player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
        h.assertTrue(!(player.containerMenu instanceof CareMenu),"Direct Calipers still use the existing detailed reader");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        var golden=new ItemStack(ModItems.GOLDEN_GUAVA.get(),2); player.setItemInHand(InteractionHand.MAIN_HAND,golden);
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO); menu=(CareMenu)player.containerMenu;
        h.assertTrue(golden.getCount()==2 && !dog.isInLove() && menu.buttonId(CarePanels.CARE)<0,
                "Upgraded guavas open breeding choices without automatic courtship or medical use");
        int old=menu.buttonId(CarePanels.COURTSHIP);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.GUAVA.get(),3));
        h.assertTrue(!menu.clickMenuButton(player,old) && !dog.isInLove() && golden.getCount()==2 && player.getMainHandItem().getCount()==3,
                "Replacing the offered hand invalidates the old guava choice without spending either stack");
        player.setItemInHand(InteractionHand.MAIN_HAND,golden);
        h.assertTrue(CareMenuGameTest.chooseGuava(h,player,dog,InteractionHand.MAIN_HAND,CarePanels.COURTSHIP)
                        && golden.getCount()==1 && dog.isInLove() && !(player.containerMenu instanceof CareMenu),
                "Deliberate upgraded-guava courtship uses one actual offered item and releases conversation attention");
        dog.discard(); player.discard();
    }
    public static void conversation(GameTestHelper h) {
        int y=150;
        for (int x=0;x<8;x++) for (int z=0;z<8;z++) {
            h.setBlock(x,y,z,net.minecraft.world.level.block.Blocks.STONE);
            for (int up=1;up<5;up++) h.setBlock(x,y+up,z,net.minecraft.world.level.block.Blocks.AIR);
        }
        var dog=h.spawn(ModEntities.BUNNIDOG.get(),2.5F,y+1,3.5F);
        int skittish=Gene.TEMPERAMENT.alleleNamed("skittish").id();
        dog.setGenome(Genome.DEFAULT.with(Gene.TEMPERAMENT,0,skittish).with(Gene.TEMPERAMENT,1,skittish));
        dog.setAge(0); dog.setOnGround(true); dog.setHealth(dog.getMaxHealth());
        var player=h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.snapTo(dog.position().add(2,0,0));
        CareMenu.open(player,dog); var origin=dog.position();
        h.assertTrue(dog.conversation().recognizes(player),"A calm skittish animal recognizes the real nearby viewer");
        h.startSequence().thenIdle(20).thenExecute(()->{
            h.assertTrue(dog.position().distanceToSqr(origin)<.04 && dog.getNavigation().isDone() && !dog.isNoAi(),
                    "Ordinary AI must keep a calm animal in place during conversation without disabling AI or teleporting");
            player.closeContainer();
            h.assertTrue(dog.conversation().viewer()==null,"Closing the exact menu releases attention immediately");
            CareMenu.open(player,dog);
            h.assertTrue(dog.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(player),1),"The actual viewer can provoke the animal");
            h.assertTrue(!dog.conversation().recognizes(player) && dog.conversation().viewer()==null,
                    "A hostile or frightened animal must retain its response; an open menu cannot pacify it");
            player.closeContainer(); dog.discard(); player.discard();
        }).thenSucceed();
    }
}
