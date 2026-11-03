package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Actual native inventories, saved two-leg parcels, protection callbacks and autonomous walking. */
final class FurnaceWorkGameTest {
    private static void at(Bunnidog dog,BlockPos pos) {
        SocietyGameTest.rest(dog); dog.snapTo(Vec3.atBottomCenterOf(pos.east())); dog.setOnGround(true);
    }
    private static boolean act(SocietyGameTest.Context c,SocietyWork.Kind kind,BlockPos target,int index,BlockPos destination) {
        at(c.worker(),target);
        return c.worker().society().perform(new SocietyWork.Job(kind,target,index,c.h().getLevel().getBlockState(target),null,destination));
    }
    private static boolean fetch(SocietyGameTest.Context c,BlockPos furnace,int slot) {
        return act(c,SocietyWork.Kind.FETCH_FURNACE,c.tree().getBlockPos(),slot,furnace);
    }
    private static boolean load(SocietyGameTest.Context c,BlockPos furnace) {
        return act(c,SocietyWork.Kind.LOAD_FURNACE,furnace,-1,null);
    }
    private static boolean collect(SocietyGameTest.Context c,BlockPos furnace,int slot) {
        return act(c,SocietyWork.Kind.EMPTY_FURNACE,furnace,slot,c.tree().getBlockPos());
    }
    private static void finish(SocietyGameTest.Context c) {
        c.tree().residents().forEach(Bunnidog::discard);c.p().discard();c.h().succeed();
    }
    private static BlockPos furnace(SocietyGameTest.Context c,Block block,int distance) {
        var at=c.tree().getBlockPos().east(distance);c.h().getLevel().setBlock(at,block.defaultBlockState(),3);
        c.h().assertTrue(c.tree().workshop().available(at)==null && c.tree().link(at),"Only an explicit Staff worksite link donates a native furnace");
        return at;
    }
    static void processing(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var level=h.getLevel();
        t.put(new ItemStack(ModItems.GUAVA_LOG.get(),12));t.put(new ItemStack(Items.COAL,8));t.put(new ItemStack(Items.RAW_IRON,4));t.put(new ItemStack(Items.POTATO,8));
        var blocks=new Block[]{Blocks.FURNACE,Blocks.BLAST_FURNACE,Blocks.SMOKER};
        var inputs=new Item[]{ModItems.GUAVA_LOG.get(),Items.RAW_IRON,Items.POTATO};
        var results=new Item[]{Items.CHARCOAL,Items.IRON_INGOT,Items.BAKED_POTATO};
        for(int i=0;i<blocks.length;i++) {
            var pos=furnace(c,blocks[i],4+i*3);var nativeFurnace=(AbstractFurnaceBlockEntity)level.getBlockEntity(pos);
            h.assertTrue(fetch(c,pos,0) && !c.worker().society().workSupplies().isEmpty() && nativeFurnace.getItem(0).isEmpty(),"Input leaves the common store as physical cargo before the furnace receives it");
            h.assertTrue(load(c,pos) && nativeFurnace.getItem(0).is(inputs[i]) && c.worker().society().workSupplies().isEmpty(),"The exact paid parcel reaches the original native input slot");
            h.assertTrue(fetch(c,pos,1) && nativeFurnace.getItem(1).isEmpty() && load(c,pos) && nativeFurnace.getItem(1).is(Items.COAL),"Real fuel takes a separate walking cargo leg");
            for(int tick=0;tick<201;tick++)AbstractFurnaceBlockEntity.serverTick(level,pos,level.getBlockState(pos),nativeFurnace);
            h.assertTrue(nativeFurnace.getItem(2).is(results[i]),"The native furnace type cooks its real loaded recipe: "+blocks[i]);
            int output=nativeFurnace.getItem(2).getCount(),stored=t.count(results[i]);
            h.assertTrue(collect(c,pos,2) && nativeFurnace.getItem(2).isEmpty() && t.count(results[i])==stored,"Actual cooked output is carried, not teleported home");
            h.assertTrue(act(c,SocietyWork.Kind.DELIVER,t.getBlockPos(),-1,null) && t.count(results[i])==stored+output,"The courier physically returns the exact native output");
        }
        h.assertTrue(t.count(ModItems.GUAVA_LOG.get())==4 && t.count(Items.POTATO)==4,"Charcoal keeps four logs for tools/building; cooking keeps four planting potatoes");
        finish(c);
    }
    static void safety(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var level=h.getLevel();var pos=furnace(c,Blocks.FURNACE,5);
        var named=new ItemStack(ModItems.GUAVA_LOG.get());named.set(DataComponents.CUSTOM_NAME,Component.literal("Do not burn"));t.setItem(0,named.copy());t.setItem(1,new ItemStack(ModItems.GUAVA_LOG.get(),12));
        Consumer<EntityMobGriefingEvent> veto=e->{if(e.getEntity()==c.worker())e.setCanGrief(false);};
        NeoForge.EVENT_BUS.addListener(veto);try {h.assertTrue(!fetch(c,pos,0) && c.worker().society().workSupplies().isEmpty() && t.getItem(1).getCount()==12,"Native griefing refusal spends no stock");}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(fetch(c,pos,0) && ItemStack.matches(t.getItem(0),named),"An artisan excludes private named ingredients");var exact=c.worker().society().workSupplies();
        var original=t.workshop().available(pos);String token=t.workshop().token(pos);
        var saved=original.saveWithFullMetadata(level.registryAccess());
        var restored=(AbstractFurnaceBlockEntity)BlockEntity.loadStatic(pos,original.getBlockState(),saved,level.registryAccess());level.setBlockEntity(restored);
        var homeSaved=t.saveWithFullMetadata(level.registryAccess());t.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),homeSaved));at(c.worker(),t.getBlockPos());
        h.assertTrue(t.workshop().resolve(pos,token)==restored && ItemStack.matches(exact,c.worker().society().workSupplies()),"Native home, furnace and animal saves retain the original furnace identity and exact cargo");
        level.setBlock(pos.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);
        h.assertTrue(!load(c,pos) && c.worker().society().offers().isEmpty() && ItemStack.matches(exact,c.worker().society().workSupplies()),"Power pauses worker transfers while preserving the parcel");level.setBlock(pos.north(),Blocks.AIR.defaultBlockState(),3);
        // Replacement during the permission callback must not inherit the former container's parcel.
        Consumer<EntityMobGriefingEvent> replace=e->{if(e.getEntity()==c.worker()) {level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);level.setBlock(pos,Blocks.FURNACE.defaultBlockState(),3);}};
        NeoForge.EVENT_BUS.addListener(replace);try {h.assertTrue(!load(c,pos) && ItemStack.matches(exact,c.worker().society().workSupplies()),"A callback replacing the furnace keeps the original paid cargo");}finally {NeoForge.EVENT_BUS.unregister(replace);}
        h.assertTrue(t.link(pos) && !token.equals(t.workshop().token(pos)) && !load(c,pos),"Explicitly linking a replacement does not redirect an old in-flight parcel");
        h.assertTrue(act(c,SocietyWork.Kind.DELIVER,t.getBlockPos(),-1,null) && t.getItem(1).getCount()==12 && c.worker().society().workSupplies().isEmpty(),"The rejected material returns once to its original common store");
        var current=t.workshop().available(pos);var locked=current.saveWithFullMetadata(level.registryAccess());var lock=new net.minecraft.nbt.CompoundTag();lock.putString("items","minecraft:tripwire_hook");locked.put("lock",lock);current.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),locked));
        h.assertTrue(current.isLocked() && t.workshop().available(pos)==null && !fetch(c,pos,0),"Native container locks prevent household transfers");
        finish(c);
    }
    static void claims(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var pos=furnace(c,Blocks.FURNACE,5);t.put(new ItemStack(ModItems.GUAVA_LOG.get(),12));
        var other=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);other.setGenome(SocietyGameTest.bright());other.setStats(Stats.of(0,0,0,40,40));other.setAge(0);other.setNoAi(true);other.setHealth(other.getMaxHealth());other.snapTo(c.worker().position());h.getLevel().addFreshEntity(other);WorkerFixture.familiar(other);h.assertTrue(other.society().invite(c.p(),t,false),"Another actual worker joins the household");
        var job=new SocietyWork.Job(SocietyWork.Kind.FETCH_FURNACE,t.getBlockPos(),0,t.getBlockState(),null,pos);
        var fuelJob=new SocietyWork.Job(SocietyWork.Kind.FETCH_FURNACE,t.getBlockPos(),1,t.getBlockState(),null,pos);
        h.assertTrue(c.worker().society().claim(job) && !other.society().claim(fuelJob),"Input and fuel compete for one actual furnace front");
        c.worker().society().release();h.assertTrue(fetch(c,pos,0),"The first worker collects a paid parcel");c.worker().society().release();
        h.assertTrue(!t.jobs().available(other,fuelJob) && other.society().offers().stream().noneMatch(j->SocietyWork.furnaceJob(j.kind())),"A saved in-flight parcel retains the work front after a transient goal lease ends");
        var goods=c.worker().society().deathCargo();h.assertTrue(goods.getCount()==8 && c.worker().society().deathCargo().isEmpty() && t.jobs().available(other,job),"Death releases the front and yields the exact remaining cargo only once");t.put(goods);
        c.worker().setGenome(new Genome(c.worker().genome().packed(),0));h.assertTrue(!fetch(c,pos,0),"Familiar routines cannot bypass an inherited limited mind");
        finish(c);
    }
    static void ai(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var pos=furnace(c,Blocks.FURNACE,6);
        t.put(new ItemStack(ModItems.GUAVA_LOG.get(),8));t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),16));t.put(new ItemStack(ModItems.GUAVA.get(),64));t.put(new ItemStack(Items.WOODEN_AXE));
        c.worker().setNoAi(false);c.p().snapTo(Vec3.atBottomCenterOf(t.getBlockPos().west(16)));
        h.succeedWhen(()->{
            h.assertTrue(t.count(Items.CHARCOAL)>0,"Native AI must walk both supply legs, use the actual furnace tick and bring charcoal home; active="+c.worker().society().active()+", cargo="+c.worker().society().workSupplies()+", at="+c.worker().position());
            h.assertTrue(t.workshop().available(pos)!=null && t.count(ModItems.GUAVA_LOG.get())<8,"The result came from real linked stock and a real furnace");
            c.worker().discard();c.p().discard();
        });
    }
}
