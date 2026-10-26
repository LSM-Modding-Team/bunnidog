package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Actual stock receipts teach; physical household records preserve procedures without uploading skill. */
public final class CourierCultureGameTest {
    private record Context(GameTestHelper h,HearthTreeBlockEntity home,ServerPlayer p,Bunnidog teacher,BlockPos source,BlockPos target,ItemStack goods) {
        Bunnidog pupil(Genome mind,boolean skilled) {var d=animal(h,Vec3.atBottomCenterOf(source.south(2)),mind,skilled);h.assertTrue(d.society().invite(p,home,false),"An apprentice joins the same authorized household");return d;}
        void finish() {home.residents().forEach(Bunnidog::discard);teacher.discard();p.discard();h.succeed();}
        Container sourceBox() {return (Container)h.getLevel().getBlockEntity(source);}
        Container targetBox() {return (Container)h.getLevel().getBlockEntity(target);}
    }
    private static Genome bright() {var g=Genome.DEFAULT;for(var gene:CognitiveGene.values())for(int i=0;i<2;i++)g=g.with(gene,i,3);return g;}
    private static Bunnidog animal(GameTestHelper h,Vec3 at,Genome genome,boolean skilled) {
        var dog=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);dog.setGenome(genome);dog.setStats(Stats.of(0,0,0,40,0));dog.setAge(0);dog.setHealth(dog.getMaxHealth());dog.setNoAi(true);dog.snapTo(at);dog.setOnGround(true);h.getLevel().addFreshEntity(dog);if(skilled)WorkerFixture.familiar(dog);return dog;
    }
    private static Context setup(GameTestHelper h) {
        for(int x=23;x<44;x++)for(int z=25;z<41;z++){h.setBlock(x,179,z,Blocks.STONE);for(int y=180;y<185;y++)h.setBlock(x,y,z,Blocks.AIR);}
        var level=h.getLevel();var source=h.absolutePos(new BlockPos(27,180,30));var target=h.absolutePos(new BlockPos(37,180,30));var hearth=h.absolutePos(new BlockPos(32,180,35));
        level.setBlock(source,Blocks.BARREL.defaultBlockState(),3);level.setBlock(target,Blocks.CHEST.defaultBlockState(),3);level.setBlock(hearth,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(source.south()));var home=(HearthTreeBlockEntity)level.getBlockEntity(hearth);home.found(p);
        var teacher=animal(h,Vec3.atBottomCenterOf(source.south(2)),bright(),true);h.assertTrue(teacher.society().invite(p,home,false),"The source courier belongs to the actual Hearth");
        var goods=new ItemStack(Items.COPPER_INGOT,32);goods.set(DataComponents.CUSTOM_NAME,Component.literal("Workshop copper"));((Container)level.getBlockEntity(source)).setItem(0,goods.copy());var c=new Context(h,home,p,teacher,source,target,goods);demonstrate(c);return c;
    }
    private static AbstractContainerMenu open(Context c,BlockPos at) {
        c.p.snapTo(Vec3.atBottomCenterOf(at.south()));c.teacher.snapTo(Vec3.atBottomCenterOf(at.south(2)));c.p.gameMode.useItemOn(c.p,c.h.getLevel(),c.p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false));
        c.h.assertTrue(c.p.containerMenu instanceof ChestMenu,"The player demonstrates in the real native storage menu");return c.p.containerMenu;
    }
    private static void demonstrate(Context c) {
        c.h.assertTrue(c.teacher.deliveries().begin(c.p),"The original courier learns through a real chosen player demonstration");var menu=open(c,c.source);menu.clicked(0,0,ContainerInput.QUICK_MOVE,c.p);c.p.closeContainer();menu=open(c,c.target);
        for(int slot=0;slot<menu.slots.size();slot++)if(menu.getSlot(slot).container==c.p.getInventory() && ItemStack.isSameItemSameComponents(menu.getSlot(slot).getItem(),c.goods)){menu.clicked(slot,0,ContainerInput.QUICK_MOVE,c.p);break;}c.p.closeContainer();
        c.h.assertTrue(c.teacher.deliveries().route()!=null && !c.teacher.deliveries().proven(),"A demonstration remembers places but does not pretend this courier delivered anything");c.sourceBox().setItem(0,c.goods.copy());
    }
    private static void at(Bunnidog dog,BlockPos stop) {dog.snapTo(Vec3.atBottomCenterOf(stop.south()));dog.setOnGround(true);}
    private static void rested(Bunnidog dog) {var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.registryAccess());dog.saveWithoutId(out);var tag=out.buildResult();tag.putInt("DeliveryRest",0);tag.putInt("SocietyWait",0);dog.load(TagValueInput.create(ProblemReporter.DISCARDING,dog.registryAccess(),tag));dog.setOnGround(true);}
    private static void ship(Context c) {at(c.teacher,c.source);c.h.assertTrue(c.teacher.deliveries().pickup(),"The original courier collects an actual parcel");at(c.teacher,c.target);c.h.assertTrue(c.teacher.deliveries().deposit() && c.teacher.deliveries().proven(),"The same parcel reaches its real destination before it can be archived");}
    private static void record(Context c) {rested(c.teacher);at(c.teacher,c.home.getBlockPos());c.h.assertTrue(c.teacher.society().perform(new SocietyWork.Job(SocietyWork.Kind.RECORD_ROUTE,c.home.getBlockPos(),0,c.home.getBlockState())),"The knowledgeable resident writes its proved route into an actual blank book");}
    public static void ai(GameTestHelper h) {
        var c=setup(h);var pupil=c.pupil(Genome.DEFAULT,false);at(c.teacher,c.source);at(pupil,c.source);c.p.snapTo(Vec3.atCenterOf(c.home.getBlockPos().offset(35,0,0)));c.teacher.setNoAi(false);pupil.setNoAi(false);
        h.succeedWhen(()->{
            h.assertTrue(pupil.deliveries().route()!=null && pupil.learning().knowledge().knows(Learning.Routine.DELIVERY) && pupil.deliveries().proven(),"The pupil must follow two real lessons and then complete its own independent delivery");
            int total=c.sourceBox().getItem(0).getCount()+c.targetBox().getItem(0).getCount()+c.teacher.deliveries().cargo().getCount()+pupil.deliveries().cargo().getCount();h.assertTrue(total==64 && !pupil.courierLore().apprenticing(),"Actual two-courier AI conserves all real stock and releases the apprenticeship");c.home.residents().forEach(Bunnidog::discard);c.p.discard();
        });
    }
    public static void archive(GameTestHelper h) {
        var c=setup(h);ship(c);var blank=new ItemStack(Items.WRITABLE_BOOK);blank.set(DataComponents.CUSTOM_NAME,Component.literal("Workshop notebook"));c.home.setItem(0,blank);record(c);var book=c.home.getItem(0).copy();var notes=RouteNotes.read(book,h.getLevel()).orElseThrow();
        h.assertTrue(book.is(ModItems.ROUTE_NOTES.get()) && book.getHoverName().getString().equals("Workshop notebook") && DeliveryMemory.sameRoute(notes.route(),c.teacher.deliveries().route()),"A physical record preserves exact route components, authority and the named cover");
        var ops=RegistryOps.create(NbtOps.INSTANCE,h.getLevel().registryAccess());var saved=ItemStack.CODEC.encodeStart(ops,book).getOrThrow();var restored=ItemStack.CODEC.parse(ops,saved).getOrThrow();h.assertTrue(saved.equals(ItemStack.CODEC.encodeStart(ops,restored).getOrThrow()) && RouteNotes.read(restored,h.getLevel()).isPresent(),"Native item/component saves preserve the exact encoded book and written procedure");
        c.teacher.discard();var pupil=c.pupil(Genome.DEFAULT,true);var knowledge=pupil.learning().knowledge();at(pupil,c.home.getBlockPos());var job=pupil.society().plan();h.assertTrue(job!=null && job.kind()==SocietyWork.Kind.STUDY_ROUTE && pupil.society().perform(job),"A competent resident studies a real accessible record after its teacher is gone");
        h.assertTrue(knowledge.equals(pupil.learning().knowledge()) && !pupil.deliveries().proven() && ItemStack.matches(c.home.getItem(0),book),"Study grants route familiarity, consumes no archived book and invents no skill or completed shipment");
        at(pupil,c.source);h.assertTrue(pupil.deliveries().pickup(),"The inherited procedure still needs actual source stock");at(pupil,c.target);h.assertTrue(pupil.deliveries().deposit() && pupil.deliveries().proven() && c.targetBox().getItem(0).getCount()==48,"The new worker independently walks the record's real delivery contract");c.finish();
    }
    public static void books(GameTestHelper h) {
        var c=setup(h);var journal=new ItemStack(Items.WRITABLE_BOOK);journal.set(DataComponents.WRITABLE_BOOK_CONTENT,new WritableBookContent(List.of(Filterable.passThrough("Private family notes"))));c.home.setItem(0,journal.copy());at(c.teacher,c.home.getBlockPos());
        h.assertTrue(!c.teacher.courierLore().record(c.home,0) && ItemStack.matches(journal,c.home.getItem(0)),"An unproved route and a private journal cannot manufacture a procedure");ship(c);rested(c.teacher);at(c.teacher,c.home.getBlockPos());h.assertTrue(!c.teacher.courierLore().record(c.home,0) && ItemStack.matches(journal,c.home.getItem(0)),"A proven teacher still cannot overwrite real journal pages");
        c.home.setItem(0,new ItemStack(Items.WRITABLE_BOOK));for(int slot=1;slot<27;slot++)c.home.setItem(slot,new ItemStack(Items.STONE,64));
        Consumer<EntityMobGriefingEvent> deny=e->{if(e.getEntity()==c.teacher)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(deny);
        try{h.assertTrue(!c.teacher.courierLore().record(c.home,0) && c.home.getItem(0).is(Items.WRITABLE_BOOK),"Native protection vetoes writing before any book is spent");}finally{NeoForge.EVENT_BUS.unregister(deny);}
        h.assertTrue(c.teacher.courierLore().record(c.home,0),"The original slot journal can replace its paid blank even in a full Hearth");c.home.setItem(1,new ItemStack(Items.WRITABLE_BOOK));h.assertTrue(!c.teacher.courierLore().record(c.home,1) && c.home.getItem(1).is(Items.WRITABLE_BOOK),"One proven route does not consume endless books to duplicate its record");
        var future=c.home.getItem(0).copy();String opaque="{\"version\":2,\"future\":\"retained\"}";future.set(ModItems.ROUTE_NOTES_DATA.get(),opaque);var ops=RegistryOps.create(NbtOps.INSTANCE,h.getLevel().registryAccess());c.home.setItem(0,ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,future).getOrThrow()).getOrThrow());var pupil=c.pupil(Genome.DEFAULT,true);at(pupil,c.home.getBlockPos());h.assertTrue(!pupil.courierLore().study(c.home,0) && opaque.equals(c.home.getItem(0).get(ModItems.ROUTE_NOTES_DATA.get())),"An unknown future procedure remains opaque stock without uploading fictitious knowledge");c.finish();
    }
    public static void interruptions(GameTestHelper h) {
        var c=setup(h);var pupil=c.pupil(Genome.DEFAULT,false);at(c.teacher,c.source);at(pupil,c.source);pupil.courierLore().tick();h.assertTrue(pupil.courierLore().apprenticing(),"A real nearby skilled household courier attracts one eligible pupil");
        Consumer<EntityMobGriefingEvent> deny=e->{if(e.getEntity()==c.teacher)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(deny);
        try{h.assertTrue(!c.teacher.deliveries().pickup() && pupil.learning().knowledge().equals(Learning.EMPTY),"A cancelled collection teaches nothing and creates no imagined parcel");}finally{NeoForge.EVENT_BUS.unregister(deny);}
        h.assertTrue(c.teacher.deliveries().pickup(),"The source can really be collected after protection allows it");rested(pupil);at(c.teacher,c.target);h.assertTrue(c.teacher.deliveries().deposit() && pupil.learning().knowledge().equals(Learning.EMPTY) && pupil.deliveries().route()==null,"Reload discards a transient half-witnessed lesson without losing source/target stock");
        rested(c.teacher);at(c.teacher,c.source);at(pupil,c.source);pupil.courierLore().tick();h.assertTrue(c.teacher.deliveries().pickup(),"A new physical example starts normally");at(c.teacher,c.target);pupil.snapTo(Vec3.atCenterOf(c.home.getBlockPos().south(10)));h.assertTrue(c.teacher.deliveries().deposit() && pupil.learning().knowledge().equals(Learning.EMPTY),"A pupil absent from the destination cannot learn from an unseen arrival");pupil.setWorkPaused(true);pupil.courierLore().tick();h.assertTrue(!pupil.courierLore().apprenticing(),"Explicit work pause releases the apprenticeship");c.finish();
    }
    public static void limits(GameTestHelper h) {
        var c=setup(h);ship(c);c.home.setItem(0,new ItemStack(Items.WRITABLE_BOOK));record(c);var limited=c.pupil(new Genome(Genome.DEFAULT.packed(),0),true);at(limited,c.home.getBlockPos());h.assertTrue(!limited.courierLore().study(c.home,0) && limited.deliveries().route()==null,"Familiar words and a physical book cannot bypass a genuinely limited mind");
        Genome supported=null;for(int code=0;code<256 && supported==null;code++){int word=0;for(int i=0;i<4;i++)word|=(((code>>>(i*2))&3)*17)<<(i*8);var candidate=new Genome(Genome.DEFAULT.packed(),word);if(CapabilityRules.potential(candidate.mind(),CapabilityRules.Task.DELIVERY)==CapabilityRules.Potential.SUPPORTED)supported=candidate;}
        h.assertTrue(supported!=null,"The actual contributing loci contain supported courier minds");var pupil=c.pupil(supported,true);at(pupil,c.home.getBlockPos());at(c.teacher,c.home.getBlockPos());h.assertTrue(pupil.courierLore().study(c.home,0) && pupil.deliveries().capable(),"A knowledgeable supported reader can interpret the route with its actual expert nearby");c.teacher.snapTo(Vec3.atCenterOf(c.home.getBlockPos().east(20)));c.p.snapTo(Vec3.atCenterOf(c.home.getBlockPos().west(20)));h.assertTrue(!pupil.deliveries().capable() && pupil.deliveries().state()==DeliveryMemory.State.GUIDANCE,"Reading never erases the inherited need for guidance");
        var otherAt=c.home.getBlockPos().south(5);h.getLevel().setBlock(otherAt,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var other=(HearthTreeBlockEntity)h.getLevel().getBlockEntity(otherAt);other.found(c.p);other.setItem(0,c.home.getItem(0).copy());var outsider=animal(h,Vec3.atBottomCenterOf(otherAt.east()),Genome.DEFAULT,true);h.assertTrue(outsider.society().invite(c.p,other,false) && !outsider.courierLore().study(other,0),"Even another Hearth of the same keeper cannot silently inherit site-bound private procedures");outsider.discard();c.finish();
    }
    public static void stale(GameTestHelper h) {
        var c=setup(h);ship(c);c.home.setItem(0,new ItemStack(Items.WRITABLE_BOOK));record(c);c.teacher.discard();var pupil=c.pupil(Genome.DEFAULT,true);at(pupil,c.home.getBlockPos());h.assertTrue(pupil.courierLore().study(c.home,0),"A competent reader first learns the original valid endpoints");var remembered=pupil.deliveries().route();var book=c.home.getItem(0).copy();
        h.getLevel().setBlock(c.source,Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(c.source,Blocks.BARREL.defaultBlockState(),3);at(pupil,c.source);h.assertTrue(!pupil.deliveries().pickup() && pupil.deliveries().state()==DeliveryMemory.State.CHANGED_STORE,"A replacement container cannot inherit a recorded store's identity or authority");
        var second=c.pupil(Genome.DEFAULT,true);at(second,c.home.getBlockPos());h.assertTrue(!second.courierLore().study(c.home,0) && ItemStack.matches(book,c.home.getItem(0)),"Stale records remain actual owned books but do not authorize new workers");c.home.setItem(0,ItemStack.EMPTY);h.assertTrue(DeliveryMemory.sameRoute(remembered,pupil.deliveries().route()) && pupil.learning().knowledge().knows(Learning.Routine.DELIVERY),"Losing the archive does not erase an individual's learned route or skill");c.finish();
    }
}
