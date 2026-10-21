package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Actual seed offspring, live selection, native growth pause, paid care and one real Board submission. */
public final class FamilyGoalsGameTest {
    private FamilyGoalsGameTest() {}
    private static Genome fixed(Genome genome,Gene gene,String name) {
        int id=gene.alleleNamed(name).id(); return genome.with(gene,0,id).with(gene,1,id);
    }
    private static void press(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        int button=menu.buttonId(action); h.assertTrue(menu.view().permits(button),"The live family action must be enabled: "+action);
        player.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,button));
    }
    private static int count(ServerPlayer player,net.minecraft.world.item.Item item) {
        int sum=0; for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i); if (stack.is(item)) sum+=stack.getCount();
        } return sum;
    }
    private static CareMenu animal(GameTestHelper h,ServerPlayer player,Bunnidog dog) {
        player.closeContainer(); CareMenuGameTest.stow(player); player.snapTo(dog.position().add(1.5,0,0));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.containerMenu instanceof CareMenu,"The actual offspring opens its own native care menu");
        return (CareMenu)player.containerMenu;
    }
    public static void run(GameTestHelper h) {
        int floor=374; var level=h.getLevel();
        for (int x=0;x<=16;x++) for (int z=24;z<=42;z++) {
            h.setBlock(x,floor,z,Blocks.STONE); for (int up=1;up<=4;up++) h.setBlock(x,floor+up,z,Blocks.AIR);
        }
        // This fixture extends beyond the small test template. Let the real chunk/entity
        // tracking activate before hatching and querying its living family.
        h.runAfterDelay(2, () -> { family(h, floor); h.succeed(); });
    }
    private static void family(GameTestHelper h, int floor) {
        var level=h.getLevel();
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); CareMenuGameTest.prepare(player);
        var tablePos=h.absolutePos(new BlockPos(5,floor+1,31));
        level.setBlock(tablePos,ModBlocks.GENETICS_TABLE.get().defaultBlockState(),3);
        var table=(GeneticsTableBlockEntity)level.getBlockEntity(tablePos);
        var a=fixed(fixed(Genome.DEFAULT,Gene.COAT,"pale"),Gene.EARS,"bat");
        var b=fixed(a,Gene.BUILD,"lean"); var points=Stats.of(0,0,0,30,30);
        var sampleA=TissueSampleItem.create(a,points); sampleA.set(DataComponents.CUSTOM_NAME,Component.literal("Sampled mother"));
        var sampleB=TissueSampleItem.create(b,points); sampleB.set(DataComponents.CUSTOM_NAME,Component.literal("Sampled father"));
        table.setItem(0,sampleA); table.setItem(1,sampleB); player.snapTo(Vec3.atBottomCenterOf(tablePos.east(2)));
        h.assertTrue(table.setGoalFromPlayer(player,Commissions.Request.NIGHT_GUIDE),"The real Table saves its normal breeding goal");
        var seed=Seed.of(a,points,b,points,0); var family=new ArrayList<Bunnidog>();
        LitterPlanningGameTest.run(h,player,tablePos,table,seed);
        // The native seed hatch accepts a deterministic RNG; no fabricated birth records or sample stand-ins.
        for (int litter=0;litter<3;litter++) {
            var pups=Bunnidog.hatch(level,Vec3.atBottomCenterOf(tablePos.north(3)),seed,0,new Random(500+litter));
            h.assertTrue(!pups.isEmpty(),"Each actual seed hatch must produce living offspring");
            for (var pup:pups) {
                pup.setNoAi(true); pup.setOnGround(true);
                pup.snapTo(Vec3.atBottomCenterOf(tablePos.offset(family.size()%4-1,0,-2-family.size()/4)));
                family.add(pup);
            }
        }
        var calipers=new ItemStack(ModItems.CALIPERS.get()); player.getInventory().setItem(13,calipers);
        h.assertTrue(family.size()>FamilyPanels.PAGE_SIZE && FamilyPanels.relatives(player,tablePos,table).size()==family.size(),
                "All nearby actual birth records match either sampled parental order");
        h.assertTrue(FamilyPanels.relatives(player,tablePos,table,true).isEmpty(),"A different actual seed cannot borrow the sampled pair's living family");
        var differentSeed=player.getInventory().getItem(15); var originalSeed=player.getInventory().getItem(16);
        player.getInventory().setItem(15,originalSeed); player.getInventory().setItem(16,differentSeed);
        h.assertTrue(FamilyPanels.relatives(player,tablePos,table,true).size()==family.size(),"The actual seed uses its own recorded pair when finding living offspring");
        var seedMenu=CareMenuGameTest.open(h,player,tablePos); press(h,player,seedMenu,LitterPanel.LITTER); press(h,player,seedMenu,FamilyPanels.SEED_FAMILY);
        h.assertTrue(seedMenu.buttonId(LitterPanel.LITTER)>=0 && seedMenu.buttonId(WorkshopPanels.BACK_TABLE)<0,"The actual seed's family returns to its source rather than losing the handoff");
        press(h,player,seedMenu,LitterPanel.LITTER); player.closeContainer();
        // Tick-zero queries need an already tracked chunk; a neighboring chunk can still be activating on CI.
        var known=family.getFirst();
        var reverseSpot=known.position().add(0,0,(known.getBlockZ()&15)<2 ? 2 : -2);
        var reverse=Bunnidog.hatch(level,reverseSpot,Seed.of(b,points,a,points,0),0,new Random(600));
        reverse.forEach(dog->dog.setNoAi(true));
        var measured=FamilyPanels.relatives(player,tablePos,table);
        h.assertTrue(measured.size()==family.size()+reverse.size(),"Reverse parent order remains the same sampled cross: found="+measured.size()
                +", expected="+(family.size()+reverse.size())+", missing="+reverse.stream().filter(dog->!measured.contains(dog)).map(Bunnidog::position).toList());
        var stranger=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),12,floor+1,35); stranger.setGenome(a); stranger.setStats(points); stranger.setBaby(true);
        h.assertTrue(FamilyPanels.fit(player,stranger,Commissions.Request.NIGHT_GUIDE).next().getString().contains("no recorded birth parents"),
                "A matching wild pup must not invite a pointless wait for birth records that growing cannot create");
        stranger.setParents(a,Genome.DEFAULT);
        h.assertTrue(!FamilyPanels.relatives(player,tablePos,table).contains(stranger),"Similar looks cannot substitute for the sampled birth parents");
        var saved=table.saveWithFullMetadata(level.registryAccess()); var menu=CareMenuGameTest.open(h,player,tablePos);
        press(h,player,menu,FamilyPanels.FAMILY);
        CohesionGameTest.preview("Living sampled family",menu.view());
        h.assertTrue(menu.view().rows().size()<=12 && menu.view().controls().size()==8 && saved.equals(table.saveWithFullMetadata(level.registryAccess()))
                && table.item(0).getHoverName().getString().equals("Sampled mother"),"Family inspection is bounded and preserves every complete sample, goal and saved field");
        press(h,player,menu,FamilyPanels.NEXT); press(h,player,menu,FamilyPanels.PREV);
        int oldSelection=menu.buttonId(FamilyPanels.SELECT); var first=FamilyPanels.visible(FamilyPanels.relatives(player,tablePos,table),0).getFirst();
        var originalPos=first.position(); first.snapTo(originalPos.add(30,0,0));
        h.assertTrue(!menu.clickMenuButton(player,oldSelection) && player.containerMenu==menu,"A removed row cannot turn an old click into another animal");
        first.snapTo(originalPos); menu.broadcastChanges();
        player.closeContainer(); menu=CareMenuGameTest.open(h,player,tablePos); press(h,player,menu,FamilyPanels.FAMILY);
        press(h,player,menu,FamilyPanels.SHOW_FAMILY);
        h.assertTrue(player.containerMenu!=menu && saved.equals(table.saveWithFullMetadata(level.registryAccess())),"Requested family glints close the view and conserve the workshop");
        var pup=family.stream().filter(dog->Commissions.Request.NIGHT_GUIDE.match(dog.genome(),dog.stats(),0)==Commissions.Status.READY).findFirst().orElseThrow();
        var genome=pup.genome(); var inherited=pup.stats();
        menu=CareMenuGameTest.open(h,player,tablePos); press(h,player,menu,FamilyPanels.FAMILY);
        var relatives=FamilyPanels.relatives(player,tablePos,table); int index=relatives.indexOf(pup);
        for (int i=0;i<index/FamilyPanels.PAGE_SIZE;i++) press(h,player,menu,FamilyPanels.NEXT);
        player.snapTo(pup.position().add(1,0,0)); press(h,player,menu,FamilyPanels.SELECT+index%FamilyPanels.PAGE_SIZE);
        h.assertTrue(player.containerMenu instanceof CareMenu && player.containerMenu!=menu,"A real row opens the exact living offspring rather than a copied sample");
        var fit=(CareMenu)player.containerMenu; CohesionGameTest.preview("Promising measured pup",fit.view());
        h.assertTrue(fit.view().rows().getFirst().getString().contains("Night Guide") && pup.isBaby()
                && CommissionCandidate.condition(pup)==Commissions.Status.ADULT,"The Table's goal follows into the actual pup's measurement while adult submission remains unfinished");
        press(h,player,fit,FamilyPanels.CHOOSE_GOAL); press(h,player,fit,FamilyPanels.SELECT_GOAL+Commissions.Request.MOUNTAIN_COURIER.ordinal());
        h.assertTrue(table.goal().orElseThrow()==Commissions.Request.NIGHT_GUIDE && pup.genome().equals(genome),"Comparing another purpose changes neither the shared Table nor the animal");
        player.getInventory().setItem(13,ItemStack.EMPTY);
        h.assertTrue(!fit.clickMenuButton(player,fit.buttonId(FamilyPanels.CHOOSE_GOAL)) && fit.view().rows().isEmpty(),"Losing the actual Calipers hides exact measurements and refuses an old action");
        player.getInventory().setItem(13,calipers); player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        menu=animal(h,player,pup); press(h,player,menu,CarePanels.ROLES); press(h,player,menu,FamilyPanels.COMPARE);
        press(h,player,menu,FamilyPanels.SELECT_GOAL+Commissions.Request.NIGHT_GUIDE.ordinal());
        h.assertTrue(pup.genome().equals(genome) && table.goal().orElseThrow()==Commissions.Request.NIGHT_GUIDE,"Adventure can measure and choose a private comparison without mutating shared gameplay");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.closeContainer();
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GOLDEN_DANDELION,2));
        player.interactOn(pup,InteractionHand.MAIN_HAND,Vec3.ZERO); int pausedAge=pup.getAge();
        for (int i=0;i<45;i++) pup.tick();
        h.assertTrue(pup.isAgeLocked() && pup.getAge()==pausedAge && player.getMainHandItem().getCount()==1,
                "A real paid golden dandelion pauses both vanilla and positive-Bloom growth");
        h.assertTrue(FamilyPanels.growth(pup).getString().contains("paused"),"Live care names growth that is actually paused");
        player.interactOn(pup,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(!pup.isAgeLocked() && player.getMainHandItem().isEmpty(),"A second native golden dandelion resumes ordinary growth");
        player.getInventory().setItem(11,new ItemStack(ModItems.GUAVA.get(),2));
        menu=animal(h,player,pup); int age=pup.getAge(); press(h,player,menu,com.bunnidogs.mod.menu.AnimalPanels.CARE_MENU); press(h,player,menu,CarePanels.CARE);
        h.assertTrue(pup.getAge()>age && count(player,ModItems.GUAVA.get())==1 && pup.genome().equals(genome) && pup.stats().equals(inherited),
                "Real paid feeding helps the chosen offspring grow without inventing genetic or point progress");
        // Fast-forward the wait; the last age step is still native AgeableMob rather than a fabricated adult record.
        pup.setAge(-1); pup.aiStep(); h.assertTrue(!pup.isBaby(),"The actually hatched and fed animal crosses the native adult boundary");
        var boardPos=tablePos.offset(6,0,0); level.setBlock(boardPos,ModBlocks.COMMISSION_BOARD.get().defaultBlockState(),3);
        var board=(CommissionBoardBlockEntity)level.getBlockEntity(boardPos); pup.snapTo(Vec3.atBottomCenterOf(boardPos.north()));
        pup.setHealth(pup.getMaxHealth());
        var breeder=h.spawnWithNoFreeWill(EntityTypes.VILLAGER,13,floor+1,32); breeder.setBaby(false);
        breeder.setVillagerData(breeder.getVillagerData().withProfession(ModVillagers.BREEDER));
        player.getInventory().setItem(12,new ItemStack(Items.PAPER,2));
        menu=CareMenuGameTest.open(h,player,boardPos);
        h.assertTrue(board.read().status()==Commissions.Status.READY && CommissionCandidate.condition(pup)==Commissions.Status.READY,
                "The same actual offspring now meets the Board's shared adult conditions and inherited target");
        press(h,player,menu,CarePanels.SUBMIT);
        h.assertTrue(board.completed(player,Commissions.Request.NIGHT_GUIDE) && count(player,Items.PAPER)==1 && count(player,Items.EMERALD)==4
                && pup.isAlive() && pup.genome().equals(genome) && pup.stats().equals(inherited),"The complete purpose loop pays one real form, grants native rewards and retains the actual raised animal");
        for (var dog:family) dog.discard(); reverse.forEach(Bunnidog::discard); stranger.discard(); breeder.discard();
    }
}
