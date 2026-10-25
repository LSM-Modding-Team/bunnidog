package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

public final class SocietyGameTest {
    static void root(GameTestHelper h,ServerPlayer p,BlockPos pos) {var old=p.position();p.snapTo(Vec3.atBottomCenterOf(pos.east(2)));p.getInventory().setItem(8,new ItemStack(ModItems.LIVING_KEYSTONE.get()));h.assertTrue(((CrucibleHeartBlockEntity)h.getLevel().getBlockEntity(pos)).install(p,p.getInventory().getItem(8)),"A real held Keystone installs once");p.getInventory().setItem(7,new ItemStack(ModItems.HEARTH_INCENSE.get(),8));p.snapTo(old);}
    private static net.minecraft.nbt.CompoundTag save(Bunnidog d) {var o=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,d.level().registryAccess());d.saveWithoutId(o);return o.buildResult();}
    // Fast-forward only the personal work rest in a save fixture, never fake successful work/evidence.
    private static void rest(Bunnidog d) {var tag=save(d);tag.putInt("SocietyWait",0);tag.putInt("GardenWait",0);d.load(TagValueInput.create(ProblemReporter.DISCARDING,d.level().registryAccess(),tag));}
    private static Genome bright() {var g=Genome.DEFAULT;for(var c:CognitiveGene.values())for(int i=0;i<2;i++)g=g.with(c,i,3);return g;}
    private record Context(GameTestHelper h,HearthTreeBlockEntity tree,ServerPlayer p,Bunnidog worker) {}
    private static Context setup(GameTestHelper h) {
        var l=h.getLevel();var pos=h.absolutePos(new BlockPos(32,180,32));
        for(int x=20;x<45;x++)for(int z=20;z<46;z++) {h.setBlock(x,179,z,Blocks.DIRT);for(int y=180;y<187;y++)h.setBlock(x,y,z,Blocks.AIR);}
        l.setBlock(pos,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var tree=(HearthTreeBlockEntity)l.getBlockEntity(pos);var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(pos.east(2)));tree.found(p);tree.survey(p);tree.survey(p);h.assertTrue(tree.authorized(),"A real clear surveyed plot requires the second explicit authorization");
        var d=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);d.setGenome(bright());d.setStats(Stats.of(0,0,0,40,40));d.setAge(0);d.setHealth(d.getMaxHealth());d.setNoAi(true);d.snapTo(Vec3.atBottomCenterOf(pos.east(2)));l.addFreshEntity(d);var t=save(d);t.putString("Learning",Learning.LEGACY_FAMILIAR.toString());d.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),t));h.assertTrue(d.society().invite(p,tree,false),"Explicit invitation enrolls the founder");return new Context(h,tree,p,d);
    }
    private static boolean act(Context c,SocietyWork.Kind k,BlockPos pos) {rest(c.worker);c.worker.snapTo(Vec3.atBottomCenterOf(pos.east()));return c.worker.society().perform(new SocietyWork.Job(k,pos,-1,c.h.getLevel().getBlockState(pos)));}
    public static void household(GameTestHelper h) {
        var c=setup(h);var l=h.getLevel();var t=c.tree;var d=c.worker;t.put(new ItemStack(ModItems.GUAVA.get(),64));t.put(new ItemStack(ModItems.GUAVA_LOG.get(),32));t.put(new ItemStack(Items.WHEAT,32));t.put(new ItemStack(Items.BONE_MEAL,64));t.put(new ItemStack(ModItems.GUAVA_COPPICE.get(),4));
        h.assertTrue(!t.ripe() && !t.tend(),"A stocked chest and gifted founder alone cannot produce a Keystone");
        for(int i=0;i<45;i++)act(c,SocietyWork.Kind.CRAFT,t.getBlockPos());
        for(var p:t.orchard()) {h.assertTrue(act(c,SocietyWork.Kind.PLANT,p),"The grower pays and plants a real renewable coppice");for(int i=0;i<4;i++) {l.setBlock(p,l.getBlockState(p).setValue(CoppiceBlock.AGE,7),2);h.assertTrue(act(c,SocietyWork.Kind.PRUNE,p),"A real mature plant resets and yields wood and food");}}
        for(int i=0;i<80;i++)act(c,SocietyWork.Kind.CRAFT,t.getBlockPos());
        for(var cell:t.shelter()) {if(t.count(cell.item())==0) {t.put(new ItemStack(ModItems.GUAVA_LOG.get(),16));for(int i=0;i<40;i++)act(c,SocietyWork.Kind.CRAFT,t.getBlockPos());}h.assertTrue(act(c,SocietyWork.Kind.BUILD,cell.pos()),"The crew physically builds each paid shelter/nest cell");}
        h.assertTrue(t.shelterComplete(),"The result is a real walkable roofed shelter with nests");
        var trough=t.getBlockPos().west(3);l.setBlock(trough,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);t.link(trough);var stock=(com.bunnidogs.mod.block.FeedingTroughBlockEntity)l.getBlockEntity(trough);stock.insert(new ItemStack(ModItems.GUAVA.get(),16));
        for(int i=0;i<8;i++) {h.assertTrue(act(c,SocietyWork.Kind.HAUL,trough),"A hauler withdraws actual surplus above the reserve");rest(d);var s=save(d);var restored=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),s));h.assertTrue(restored.society().belongs(t),"Cargo and site identity survive a native animal save");h.assertTrue(act(c,SocietyWork.Kind.DELIVER,t.getBlockPos()),"The same owned cargo reaches communal stores");}
        for(int i=0;i<4;i++)h.assertTrue(act(c,SocietyWork.Kind.MEAL,t.getBlockPos()),"Resident meals consume shared real food");
        h.assertTrue(!t.ripe(),"An orchard, warehouse, craftspeople and shelter still need a real next generation");
        family(c);
        h.assertTrue(t.ripe(),"A genuine paid household, new generation and independently working pupil qualify the graft");
        for(int i=0;i<4;i++) {for(int j=0;j<12 && t.count(ModItems.GRAFTING_FRAME.get())<1;j++)act(c,SocietyWork.Kind.CRAFT,t.getBlockPos());h.assertTrue(act(c,SocietyWork.Kind.TEND,t.getBlockPos()),"Each cultivation stage pays its real food, bone meal and frame");}
        h.assertTrue(t.count(ModItems.LIVING_KEYSTONE.get())==1 && !t.tend(),"The full chain produces one physical Keystone, then pauses with output present");
        t.residents().forEach(Bunnidog::discard);c.p.discard();
    }
    public static void family(GameTestHelper h) {var c=setup(h);family(c);c.tree.residents().forEach(Bunnidog::discard);c.p.discard();}
    private static void family(Context c) {
        var h=c.h;var l=h.getLevel();var t=c.tree;var first=c.worker;t.put(new ItemStack(ModItems.GUAVA.get(),64));
        var second=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);second.setGenome(bright());second.setStats(Stats.of(0,0,0,40,40));second.setAge(0);second.setHealth(second.getMaxHealth());second.setNoAi(true);second.snapTo(first.position().add(1,0,0));l.addFreshEntity(second);second.society().invite(c.p,t,false);
        var n1=t.getBlockPos().offset(2,0,3);var n2=n1.east(2);l.setBlock(n1,ModBlocks.NEST.get().defaultBlockState(),3);l.setBlock(n2,ModBlocks.NEST.get().defaultBlockState(),3);first.setOnGround(true);second.setOnGround(true);l.getPoiManager().add(n1,ModPoi.NEST);l.getPoiManager().add(n2,ModPoi.NEST);var m1=first.moveHomeTo(l,n1);var m2=second.moveHomeTo(l,n2);h.assertTrue(m1==NestHome.MoveResult.MOVED && m2==NestHome.MoveResult.MOVED,"Both parents have real claimed homes: "+m1+" / "+m2);
        var cradle=t.getBlockPos().north(6);l.setBlock(cradle,ModBlocks.NURSERY_CRADLE.get().defaultBlockState().setValue(com.bunnidogs.mod.block.NurseryCradleBlock.FACING,Direction.NORTH),3);t.link(cradle);
        h.assertTrue(act(c,SocietyWork.Kind.COURT,t.getBlockPos()) && first.isInLove() && second.isInLove(),"A caretaker pays two guavas and starts real native courtship");first.spawnChildFromBreeding(l,second);int slot=-1;for(int i=0;i<27;i++)if(SocietyWork.communitySeed(t.getItem(i),t)) {slot=i;break;}h.assertTrue(slot>=0,"The actual parents produce one exact community seed");
        first.resetLove();second.resetLove();rest(first);first.snapTo(Vec3.atBottomCenterOf(t.getBlockPos().east()));h.assertTrue(first.society().perform(new SocietyWork.Job(SocietyWork.Kind.SEED,t.getBlockPos(),slot,l.getBlockState(t.getBlockPos()))),"The nurse carries the real seed");h.assertTrue(act(c,SocietyWork.Kind.DELIVER,cradle),"The seed is inserted into the actual linked cradle");
        for(int i=0;i<7;i++)act(c,SocietyWork.Kind.NURTURE,cradle);first.snapTo(Vec3.atBottomCenterOf(cradle.east(2)));h.assertTrue(act(c,SocietyWork.Kind.HATCH,cradle),"The caretaker hatches a native litter");
        var child=t.residents().stream().filter(d->d.society().child()).findFirst().orElseThrow();h.assertTrue(child.isBaby() && child.learning().knowledge().equals(Learning.EMPTY),"Offspring inherit the society and genes, never their parents' knowledge");child.setAge(0);child.setNoAi(true);child.snapTo(first.position().add(1,0,0));first.setAge(0);
        var crop=t.orchard().getFirst();l.setBlock(crop,ModBlocks.GUAVA_COPPICE.get().defaultBlockState().setValue(CoppiceBlock.AGE,7),3);t.put(new ItemStack(Items.WOODEN_AXE));
        var pupil=new Context(h,t,c.p,child);child.snapTo(Vec3.atBottomCenterOf(crop.east()));first.snapTo(Vec3.atBottomCenterOf(crop.east(2)));h.assertTrue(child.society().mentor(Learning.Routine.HARVEST)==first,"A capable knowledgeable resident, rather than a player, guides the pupil");
        h.assertTrue(act(pupil,SocietyWork.Kind.PRUNE,crop) && child.learning().knowledge().knows(Learning.Routine.HARVEST),"Actual successful work teaches the inherited-capable offspring");first.snapTo(first.position().add(12,0,0));l.setBlock(crop,l.getBlockState(crop).setValue(CoppiceBlock.AGE,7),3);h.assertTrue(act(pupil,SocietyWork.Kind.PRUNE,crop),"The offspring repeats useful paid work without its mentor");
        var data=t.saveWithFullMetadata(l.registryAccess());var restored=(HearthTreeBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(t.getBlockPos(),t.getBlockState(),data,l.registryAccess());h.assertTrue(restored!=null && restored.site().equals(t.site()) && restored.count(ModItems.GUAVA.get())==t.count(ModItems.GUAVA.get()),"The community's identity, supplies and earned work survive native saves");
    }
    public static void gestures(GameTestHelper h) {
        var c=setup(h);var p=c.p;var t=c.tree;var d=c.worker;p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.STEWARD_STAFF.get()));
        var hit=new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(t.getBlockPos()),Direction.UP,t.getBlockPos(),false);
        p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
        h.assertTrue(t==StewardStaffItem.hearth(p.getMainHandItem(),p),"Native block use binds the staff instead of opening a storage menu");
        p.setShiftKeyDown(true);p.interactOn(d,InteractionHand.MAIN_HAND,Vec3.ZERO);h.assertTrue(!d.society().belongs(t),"Native staff interaction reaches leave rather than the animal conversation");p.setShiftKeyDown(false);p.interactOn(d,InteractionHand.MAIN_HAND,Vec3.ZERO);h.assertTrue(d.society().belongs(t),"Native item dispatch rejoins the resident");
        var compost=t.getBlockPos().east(3);h.getLevel().setBlock(compost,Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,8),3);p.snapTo(Vec3.atBottomCenterOf(compost.east()));
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock> protect=e->{if(e.getPos().equals(compost))e.setCanceled(true);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(protect);try {p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(compost),Direction.UP,compost,false));}finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(protect);}h.assertTrue(!t.sites().contains(compost),"A native interaction protection veto runs before Staff linking");p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(compost),Direction.UP,compost,false));h.assertTrue(t.sites().contains(compost) && h.getLevel().getBlockState(compost).getValue(ComposterBlock.LEVEL)==8,"The native composter interaction links it without extracting or losing its produce");
        d.discard();p.discard();
    }
    public static void conservation(GameTestHelper h) {
        var c=setup(h);var t=c.tree;var l=h.getLevel();t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),16));
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> reject=e->{if(e.getEntity()==c.worker)e.setCanceled(true);};
        var cell=t.shelter().getFirst();net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(reject);try {h.assertTrue(!act(c,SocietyWork.Kind.BUILD,cell.pos()),"Protection can veto a planned resident block placement");}finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(reject);}
        h.assertTrue(l.getBlockState(cell.pos()).isAir() && t.count(ModItems.GUAVA_PLANKS.get())==16,"Canceled construction returns the exact escrowed material");
        var p=t.getBlockPos().west(3);l.setBlock(p,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);t.link(p);var trough=(com.bunnidogs.mod.block.FeedingTroughBlockEntity)l.getBlockEntity(p);var food=new ItemStack(ModItems.GUAVA.get(),16);food.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Exact supplies"));trough.insert(food);
        h.assertTrue(act(c,SocietyWork.Kind.HAUL,p) && trough.food().getCount()==15,"Native hauling removes one real surplus item");
        var event=new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(c.worker,l.damageSources().generic(),new java.util.ArrayList<>(),true);SocietyEvents.drops(event);h.assertTrue(event.getDrops().size()==1 && event.getDrops().iterator().next().getItem().getHoverName().equals(food.getHoverName()),"A carrying worker's actual death inventory preserves exact item components");SocietyEvents.drops(event);h.assertTrue(event.getDrops().size()==1,"Repeated death notifications cannot duplicate cargo");l.setBlock(p,trough.getBlockState().setValue(com.bunnidogs.mod.block.FeedingTroughBlock.TENDING,false),3);h.assertTrue(!act(c,SocietyWork.Kind.HAUL,p) && trough.food().getCount()==15,"Manually disabling a linked trough pauses communal hauling too");
        var loot=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> collect=e->{if(e.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item && item.distanceToSqr(Vec3.atCenterOf(t.getBlockPos()))<16)loot.add(item);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(collect);try {l.destroyBlock(t.getBlockPos(),false);}finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(collect);}h.assertTrue(loot.stream().filter(e->e.getItem().is(ModItems.GUAVA_PLANKS.get())).mapToInt(e->e.getItem().getCount()).sum()==16,"Destroying the Hearth drops its real stored materials once");
        c.worker.discard();c.p.discard();
    }
    public static void ai(GameTestHelper h) {
        var c=setup(h);var d=c.worker;var t=c.tree;t.put(new ItemStack(ModItems.GUAVA_LOG.get()));d.snapTo(Vec3.atBottomCenterOf(t.getBlockPos().east(8)));d.setNoAi(false);c.p.snapTo(c.p.position().add(18,0,0));
        h.runAfterDelay(200,()->{h.assertTrue(t.count(ModItems.GUAVA_LOG.get())==0 && (t.count(ModItems.GUAVA_PLANKS.get())>=4 || t.count(Items.WOODEN_AXE)==1),"Native AI walks to the Hearth and crafts paid supplies without a player supervising");d.discard();c.p.discard();h.succeed();});
    }
    public static void hunts(GameTestHelper h) {
        var l=h.getLevel();var pos=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,pos.add(3,0,0));var friend=ApexTypesGameTest.player(h,pos.add(4,0,0));var idle=ApexTypesGameTest.player(h,pos.add(5,0,0));var ledger=HuntProgress.get(l);
        for(var kind:ApexKind.values()) {var a=ApexTypesGameTest.apex(h,kind,false,pos);a.hurtServer(l,l.damageSources().playerAttack(p),20);a.invulnerableTime=0;var aid=new ItemStack(ModItems.HEARTH_POULTICE.get(),4);friend.setItemInHand(InteractionHand.MAIN_HAND,aid);for(int heal=0;heal<3;heal++) {p.setHealth(5);friend.getCooldowns().removeCooldown(friend.getCooldowns().getCooldownGroup(aid));ModItems.HEARTH_POULTICE.get().interactLivingEntity(aid,friend,p,InteractionHand.MAIN_HAND);}a.hurtServer(l,l.damageSources().playerAttack(p),10000);HuntProgress.finish(a);h.assertTrue((ledger.marks(p.getUUID())&(1<<kind.ordinal()))!=0,"A real ground death grants its distinct role even on the killing hit");a.discard();}
        h.assertTrue(!ledger.qualified(p.getUUID()) && ledger.marks(friend.getUUID())==31 && ledger.marks(idle.getUUID())==0,"Five ground hunts alone and idle proximity do not unlock entry; support earns its actual contribution: "+ledger.marks(p.getUUID())+"/"+ledger.marks(friend.getUUID())+"/"+ledger.marks(idle.getUUID()));var a=ApexTypesGameTest.apex(h,ApexKind.PRISM,true,pos);a.hurtServer(l,l.damageSources().playerAttack(p),10000);HuntProgress.finish(a);h.assertTrue(ledger.qualified(p.getUUID()),"Any one flying role completes the five distinct ground hunts");
        var copy=HuntProgress.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,HuntProgress.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,ledger).getOrThrow()).getOrThrow();h.assertTrue(copy.qualified(p.getUUID()),"World-owned progress survives its actual saved-data codec");
        var anchor=BlockPos.containing(pos).east(6);l.setBlock(anchor,ModBlocks.WARREN_ANCHOR.get().defaultBlockState(),3);idle.snapTo(Vec3.atBottomCenterOf(anchor.east()));idle.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.RESONANT_EYE.get()));var borrowed=new ItemStack(ModItems.APEX_CREST.get());borrowed.set(ModItems.HUNT_MARKS.get(),63);idle.setItemInHand(InteractionHand.OFF_HAND,borrowed);var be=(com.bunnidogs.mod.realm.WarrenAnchorBlockEntity)l.getBlockEntity(anchor);be.attune(idle,InteractionHand.MAIN_HAND);h.assertTrue(!be.linked() && idle.getMainHandItem().getCount()==1,"A borrowed completed Crest cannot create a portal or consume its Eye");a.discard();p.discard();friend.discard();idle.discard();
    }
}
