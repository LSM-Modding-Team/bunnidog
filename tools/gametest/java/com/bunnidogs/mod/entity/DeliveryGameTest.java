package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Real container demonstrations and paid two-stop deliveries, including native AI and journal callbacks. */
public final class DeliveryGameTest {
    private record Context(GameTestHelper h,Bunnidog dog,ServerPlayer p,BlockPos source,BlockPos target,ItemStack goods) {}
    private static Context setup(GameTestHelper h) {
        var l=h.getLevel();for(int x=18;x<47;x++)for(int z=24;z<42;z++) {h.setBlock(x,179,z,Blocks.STONE);for(int y=180;y<185;y++)h.setBlock(x,y,z,Blocks.AIR);}
        var src=h.absolutePos(new BlockPos(27,180,32));var dst=h.absolutePos(new BlockPos(38,180,32));l.setBlock(src,Blocks.BARREL.defaultBlockState(),3);l.setBlock(dst,Blocks.CHEST.defaultBlockState(),3);
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(src.south()));var dog=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);var g=Genome.DEFAULT;for(var gene:CognitiveGene.values())for(int i=0;i<2;i++)g=g.with(gene,i,2);
        dog.setGenome(g);dog.setStats(Stats.of(0,0,0,40,30));dog.setAge(0);dog.setHealth(dog.getMaxHealth());dog.setNoAi(true);dog.snapTo(Vec3.atBottomCenterOf(src.south(2)));l.addFreshEntity(dog);
        var goods=new ItemStack(Items.COPPER_INGOT,32);goods.set(DataComponents.CUSTOM_NAME,Component.literal("Workshop copper"));((Container)l.getBlockEntity(src)).setItem(0,goods.copy());return new Context(h,dog,p,src,dst,goods);
    }
    private static AbstractContainerMenu open(Context c,BlockPos at) {
        c.p.snapTo(Vec3.atBottomCenterOf(at.south()));c.dog.snapTo(Vec3.atBottomCenterOf(at.south(2)));c.dog.setOnGround(true);
        c.p.gameMode.useItemOn(c.p,c.h.getLevel(),c.p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
        c.h.assertTrue(c.p.containerMenu instanceof ChestMenu,"A demonstration opens the real native storage menu");return c.p.containerMenu;
    }
    private static com.bunnidogs.mod.menu.CareMenu lessons(Context c) {
        c.p.snapTo(c.dog.position().add(1,0,0));com.bunnidogs.mod.menu.CareMenu.open(c.p,c.dog);var menu=(com.bunnidogs.mod.menu.CareMenu)c.p.containerMenu;
        c.h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(com.bunnidogs.mod.menu.CarePanels.ROLES)),"Skills and roles opens through the native animal menu");
        c.h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(com.bunnidogs.mod.menu.LearningPanel.OPEN)),"Lessons exposes the actual delivery demonstration action");return menu;
    }
    private static void demonstrate(Context c) {
        var lesson=lessons(c);c.h.assertTrue(lesson.clickMenuButton(c.p,lesson.buttonId(com.bunnidogs.mod.menu.LearningPanel.WATCH)) && c.p.containerMenu!=lesson && c.dog.deliveries().watching(),"The authoritative demonstration action closes conversation so the adult can accompany its teacher");
        var menu=open(c,c.source);int pickup=-1;for(int i=0;i<menu.slots.size();i++)if(menu.getSlot(i).container!=c.p.getInventory() && ItemStack.isSameItemSameComponents(menu.getSlot(i).getItem(),c.goods)) {pickup=i;break;}c.h.assertTrue(pickup>=0,"The real source contains the demonstrated item");menu.clicked(pickup,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();c.h.assertTrue(c.dog.deliveries().route()==null,"Pickup alone teaches no invented destination");
        menu=open(c,c.target);int slot=-1;for(int i=0;i<menu.slots.size();i++)if(menu.getSlot(i).container==c.p.getInventory() && ItemStack.isSameItemSameComponents(menu.getSlot(i).getItem(),c.goods)) {slot=i;break;}c.h.assertTrue(slot>=0,"The demonstrating player actually owns the removed shipment");menu.clicked(slot,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();
        c.h.assertTrue(c.dog.deliveries().route()!=null && !c.dog.deliveries().watching() && c.dog.deliveries().route().batch()==8,"A balanced native deposit teaches the two actual stops and bounded shipment");
        c.h.assertTrue(ItemStack.isSameItemSameComponents(c.dog.deliveries().route().filter(),c.goods),"The remembered filter preserves exact custom components");((Container)c.h.getLevel().getBlockEntity(c.source)).setItem(0,c.goods.copy());
    }
    private static void position(Context c,BlockPos at) {c.dog.snapTo(Vec3.atBottomCenterOf(at.south()));c.dog.setOnGround(true);}
    public static void observation(GameTestHelper h) {
        var c=setup(h);demonstrate(c);h.assertTrue(c.dog.learning().knowledge().independent(c.dog.mind(),Learning.Routine.DELIVERY),"Observed actual work teaches a genetically capable learner");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.dog.saveWithoutId(out);var restored=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
        h.assertTrue(restored.deliveries().route()!=null && restored.deliveries().route().source().equals(c.dog.deliveries().route().source()) && restored.learning().knowledge().equals(c.dog.learning().knowledge()),"Personal route memory and practice survive real entity saves");c.dog.discard();c.p.discard();
    }
    public static void transfer(GameTestHelper h) {
        var c=setup(h);demonstrate(c);position(c,c.source);int before=((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount();h.assertTrue(c.dog.deliveries().pickup(),"Pickup withdraws actual stock into one owned parcel");
        h.assertTrue(((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount()==before-8 && c.dog.deliveries().cargo().getCount()==8,"Source and parcel balance exactly");position(c,c.target);h.assertTrue(c.dog.deliveries().deposit() && c.dog.deliveries().cargo().isEmpty(),"Only reaching the destination deposits the parcel");h.assertTrue(((Container)h.getLevel().getBlockEntity(c.target)).getItem(0).getCount()==40,"Destination receives exactly the original named copper");
        h.assertTrue(c.dog.deliveries().setPaused(c.p,true) && !c.dog.deliveries().pickup(),"Explicit pause prevents another shipment");h.assertTrue(c.dog.deliveries().clear(c.p),"Clearing an idle route removes only its remembered work");c.dog.discard();c.p.discard();
    }
    public static void changes(GameTestHelper h) {
        var c=setup(h);demonstrate(c);position(c,c.source);h.assertTrue(c.dog.deliveries().pickup(),"A real parcel exists before interruption");var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.dog.saveWithoutId(out);c.dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));h.assertTrue(c.dog.deliveries().cargo().getCount()==8,"Reload cannot lose in-flight stock");
        h.getLevel().setBlock(c.target,Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(c.target,Blocks.CHEST.defaultBlockState(),3);position(c,c.target);h.assertTrue(c.dog.deliveries().state()==DeliveryMemory.State.CHANGED_STORE && !c.dog.deliveries().deposit() && c.dog.deliveries().cargo().getCount()==8,"A replacement chest cannot inherit another store's authorization or confiscate the parcel");
        h.assertTrue(!c.dog.deliveries().clear(c.p),"Clearing cannot erase a carried item");int n=0;for(int i=0;i<c.p.getInventory().getContainerSize();i++)if(ItemStack.isSameItemSameComponents(c.p.getInventory().getItem(i),c.goods))n+=c.p.getInventory().getItem(i).getCount();h.assertTrue(c.dog.deliveries().unload(c.p),"The owner can recover a stranded parcel explicitly");int after=0;for(int i=0;i<c.p.getInventory().getContainerSize();i++)if(ItemStack.isSameItemSameComponents(c.p.getInventory().getItem(i),c.goods))after+=c.p.getInventory().getItem(i).getCount();h.assertTrue(after-n==8 && c.dog.deliveries().cargo().isEmpty(),"Recovery preserves exact item components and quantity");c.dog.discard();c.p.discard();
    }
    public static void limits(GameTestHelper h) {
        var c=setup(h);var low=c.dog.genome();for(var gene:CognitiveGene.values())for(int i=0;i<2;i++)low=low.with(gene,i,0);c.dog.setGenome(low);h.assertTrue(!c.dog.deliveries().begin(c.p),"A limited mind cannot be given a fictitious courier program");
        var supported=Genome.DEFAULT.with(CognitiveGene.MEMORY,0,0).with(CognitiveGene.MEMORY,1,0);for(var gene:new CognitiveGene[]{CognitiveGene.REASONING,CognitiveGene.HANDLING,CognitiveGene.COMMUNICATION})for(int i=0;i<2;i++)supported=supported.with(gene,i,1);c.dog.setGenome(supported);demonstrate(c);h.assertTrue(!c.dog.learning().knowledge().independent(c.dog.mind(),Learning.Routine.DELIVERY),"Learning does not erase an inherited need for guidance");
        c.p.snapTo(Vec3.atBottomCenterOf(c.target.east(20)));h.assertTrue(!c.dog.deliveries().canWork() && c.dog.deliveries().state()==DeliveryMemory.State.GUIDANCE,"A supported courier pauses when its guide leaves");c.dog.discard();c.p.discard();
    }
    public static void watchSafety(GameTestHelper h) {
        var c=setup(h);h.assertTrue(c.dog.deliveries().begin(c.p),"An ordinary eligible demonstration begins");open(c,c.source);c.p.closeContainer();h.assertTrue(c.dog.deliveries().route()==null,"Simply opening storage invents no shipment");
        var menu=open(c,c.source);menu.clicked(0,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();menu=open(c,c.source);int slot=-1;for(int i=0;i<menu.slots.size();i++)if(menu.getSlot(i).container==c.p.getInventory() && ItemStack.isSameItemSameComponents(menu.getSlot(i).getItem(),c.goods)) {slot=i;break;}menu.clicked(slot,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();h.assertTrue(c.dog.deliveries().route()==null,"Returning stock to the same container is not a delivery route");
        c.dog.deliveries().stopWatching();h.assertTrue(c.dog.deliveries().begin(c.p),"A fresh deliberate demonstration replaces the incomplete observation");
        menu=open(c,c.source);var barrier=c.source.south();h.getLevel().setBlock(barrier,Blocks.STONE.defaultBlockState(),3);h.getLevel().setBlock(barrier.above(),Blocks.STONE.defaultBlockState(),3);menu.clicked(0,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();h.getLevel().removeBlock(barrier,false);h.getLevel().removeBlock(barrier.above(),false);
        menu=open(c,c.target);for(int i=0;i<menu.slots.size();i++)if(menu.getSlot(i).container==c.p.getInventory() && ItemStack.isSameItemSameComponents(menu.getSlot(i).getItem(),c.goods)) {menu.clicked(i,0,ContainerInput.QUICK_MOVE,c.p);break;}c.p.closeContainer();h.assertTrue(c.dog.deliveries().route()==null,"An occluded pickup cannot teach a route from the deposit alone");c.dog.discard();c.p.discard();
    }
    public static void food(GameTestHelper h) {
        var initial=setup(h);var guavas=new ItemStack(ModItems.GUAVA.get(),16);guavas.set(DataComponents.CUSTOM_NAME,Component.literal("Nursery guavas"));((Container)h.getLevel().getBlockEntity(initial.source)).setItem(0,guavas.copy());var c=new Context(h,initial.dog,initial.p,initial.source,initial.target,guavas);demonstrate(c);
        position(c,c.source);h.assertTrue(c.dog.deliveries().pickup() && ((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount()==8,"A courier leaves eight actual ordinary food items in its source");position(c,c.target);h.assertTrue(c.dog.deliveries().deposit(),"Reserved food delivery arrives normally");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.dog.saveWithoutId(out);var saved=out.buildResult();saved.putInt("DeliveryRest",0);c.dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));position(c,c.source);h.assertTrue(c.dog.deliveries().canWork() && !c.dog.deliveries().pickup() && ((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount()==8,"An exhausted route cannot consume its protected food reserve");c.dog.discard();c.p.discard();
    }
    public static void doubleChest(GameTestHelper h) {
        var c=setup(h);var l=h.getLevel();var left=Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.NORTH).setValue(ChestBlock.TYPE,net.minecraft.world.level.block.state.properties.ChestType.LEFT);var other=c.source.relative(ChestBlock.getConnectedDirection(left));l.setBlock(c.source,left,3);l.setBlock(other,left.setValue(ChestBlock.TYPE,net.minecraft.world.level.block.state.properties.ChestType.RIGHT),3);((Container)l.getBlockEntity(c.source)).setItem(0,c.goods.copy());demonstrate(c);
        h.assertTrue(c.dog.deliveries().route().source().stores().size()==2,"Both actual chest halves are remembered and authorized");position(c,c.source);h.assertTrue(c.dog.deliveries().pickup(),"A double chest uses its original stock journals");l.setBlock(other,Blocks.AIR.defaultBlockState(),3);position(c,c.target);h.assertTrue(c.dog.deliveries().deposit(),"Breaking the pickup store cannot erase an already-owned shipment or prevent its safe delivery");h.assertTrue(c.dog.deliveries().state()==DeliveryMemory.State.CHANGED_STORE,"The altered source stops the next collection instead of adopting another container");c.dog.discard();c.p.discard();
    }
    public static void pause(GameTestHelper h) {
        var c=setup(h);demonstrate(c);var lesson=lessons(c);h.assertTrue(lesson.clickMenuButton(c.p,lesson.buttonId(com.bunnidogs.mod.menu.LearningPanel.PAUSE_ROUTE)) && c.dog.deliveries().paused(),"The native route pause button controls actual work");h.assertTrue(lesson.clickMenuButton(c.p,lesson.buttonId(com.bunnidogs.mod.menu.LearningPanel.PAUSE_ROUTE)) && !c.dog.deliveries().paused(),"The same native action resumes delivery");c.p.closeContainer();position(c,c.source);
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityMobGriefingEvent> deny=e->{if(e.getEntity()==c.dog)e.setCanGrief(false);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(deny);
        try {h.assertTrue(!c.dog.deliveries().pickup() && c.dog.deliveries().cargo().isEmpty() && ((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount()==32,"A native protection veto prevents withdrawal from the original store");}
        finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);}
        h.getLevel().setBlock(c.target.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);h.assertTrue(c.dog.deliveries().state()==DeliveryMemory.State.PAUSED_STORE && !c.dog.deliveries().pickup(),"Redstone pauses both ends before any stock is withdrawn");h.getLevel().removeBlock(c.target.east(),false);
        var box=(Container)h.getLevel().getBlockEntity(c.target);for(int i=0;i<box.getContainerSize();i++)box.setItem(i,new ItemStack(Items.STONE,64));h.assertTrue(!c.dog.deliveries().pickup() && c.dog.deliveries().cargo().isEmpty() && ((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount()==32,"A full destination is detected before pickup, with no stranded or duplicated parcel");
        for(int i=0;i<box.getContainerSize();i++)box.setItem(i,ItemStack.EMPTY);
        h.assertTrue(c.dog.deliveries().pickup(),"Removing the actual blockers permits one owned shipment");position(c,c.target);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(deny);
        try {h.assertTrue(!c.dog.deliveries().deposit() && c.dog.deliveries().cargo().getCount()==8 && box.isEmpty(),"A deposit veto preserves the entire owned parcel and destination stock");}
        finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);}
        h.assertTrue(c.dog.deliveries().deposit() && c.dog.deliveries().cargo().isEmpty(),"The same parcel can arrive after native protection allows it");
        var stranger=ApexTypesGameTest.player(h,c.p.position().add(1,0,0));h.assertTrue(!c.dog.deliveries().begin(stranger) && !c.dog.deliveries().clear(stranger),"Another player cannot overwrite or clear the demonstrated route");stranger.discard();c.dog.discard();c.p.discard();
    }
    public static void ai(GameTestHelper h) {
        var c=setup(h);demonstrate(c);position(c,c.source);c.p.snapTo(Vec3.atBottomCenterOf(c.source.east(25)));c.dog.setNoAi(false);
        h.runAfterDelay(210,()->{int source=((Container)h.getLevel().getBlockEntity(c.source)).getItem(0).getCount();int target=((Container)h.getLevel().getBlockEntity(c.target)).getItem(0).getCount();h.assertTrue(source==24 && target==40 && c.dog.deliveries().cargo().isEmpty(),"Actual unsupervised native AI walks both legs and delivers once: "+source+" / "+target+" / "+c.dog.deliveries().state());c.dog.discard();c.p.discard();h.succeed();});
    }
}
