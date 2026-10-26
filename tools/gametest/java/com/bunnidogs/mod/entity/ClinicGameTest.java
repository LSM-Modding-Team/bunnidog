package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.HearthTreeBlockEntity;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/** Paid, physical family care, with original stock ownership and native healing cancellation. */
public final class ClinicGameTest {
    private record Context(GameTestHelper h,HearthTreeBlockEntity hearth,ServerPlayer p,Bunnidog doctor,Bunnidog patient,ItemStack dressing) {
        void finish() {doctor.discard();patient.discard();p.discard();h.succeed();}
        void rest() {var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());doctor.saveWithoutId(out);var saved=out.buildResult();saved.putInt("SocietyWait",0);doctor.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));doctor.setOnGround(true);}
        SocietyWork.Job pickup() {return new SocietyWork.Job(SocietyWork.Kind.MEDICINE,hearth.getBlockPos(),-1,hearth.getBlockState(),new SocietyWork.Patient(patient.getUUID(),patient.homeNest()));}
        SocietyWork.Job treatment() {return new SocietyWork.Job(SocietyWork.Kind.CARE,patient.blockPosition(),-1,h.getLevel().getBlockState(patient.blockPosition()),new SocietyWork.Patient(patient.getUUID(),patient.homeNest()));}
        void collect() {doctor.snapTo(Vec3.atBottomCenterOf(hearth.getBlockPos().east()));h.assertTrue(doctor.society().perform(pickup()),"The carer collects one actual dressing at its original Hearth");}
    }
    private static Bunnidog resident(GameTestHelper h,Vec3 at) {
        var d=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);var g=Genome.DEFAULT;for(var gene:CognitiveGene.values())for(int i=0;i<2;i++)g=g.with(gene,i,3);
        d.setGenome(g);d.setStats(Stats.of(0,0,0,40,40));d.setAge(0);d.setHealth(d.getMaxHealth());d.setNoAi(true);d.snapTo(at);d.setOnGround(true);h.getLevel().addFreshEntity(d);WorkerFixture.familiar(d);return d;
    }
    private static void setup(GameTestHelper h,Consumer<Context> work) {
        for(int x=23;x<44;x++)for(int z=25;z<41;z++){h.setBlock(x,219,z,Blocks.STONE);for(int y=220;y<225;y++)h.setBlock(x,y,z,Blocks.AIR);}
        var l=h.getLevel();var at=h.absolutePos(new BlockPos(30,220,32));l.setBlock(at,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var hearth=(HearthTreeBlockEntity)l.getBlockEntity(at);
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(at.east(2)));hearth.found(p);
        var doctor=resident(h,Vec3.atBottomCenterOf(at.east()));var bed=at.east(6);l.setBlock(bed,ModBlocks.NEST.get().defaultBlockState(),3);
        var patient=resident(h,Vec3.atBottomCenterOf(bed.south()));patient.setHealth(Math.max(1,patient.getMaxHealth()-6));
        h.assertTrue(doctor.society().invite(p,hearth,false) && patient.society().invite(p,hearth,false),"Only explicitly enrolled residents join the household");
        var dressing=new ItemStack(ModItems.HEARTH_POULTICE.get(),3);dressing.set(DataComponents.CUSTOM_NAME,Component.literal("Family dressings"));hearth.setItem(0,dressing.copy());
        h.runAfterDelay(2,()->{l.getPoiManager().add(bed,ModPoi.NEST);h.assertTrue(patient.moveHomeTo(l,bed)==NestHome.MoveResult.MOVED,"The actual patient has a claimed reachable recovery Nest");patient.setOnGround(true);work.accept(new Context(h,hearth,p,doctor,patient,dressing));});
    }
    public static void ai(GameTestHelper h) {
        setup(h,c->{float before=c.patient.getHealth();c.doctor.setNoAi(false);
            h.runAfterDelay(240,()->{h.assertTrue(c.patient.getHealth()==before+ClinicCare.HEAL && c.hearth.getItem(0).getCount()==2 && c.doctor.society().cargoEmpty(),"Native AI walks both care legs and spends exactly one real dressing: health="+c.patient.getHealth()+", stock="+c.hearth.getItem(0)+", cargo="+c.doctor.society().medicalSupplies()+", position="+c.doctor.position());
                int wait=c.patient.clinic().waitTicks();var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());c.patient.saveWithoutId(saved);c.patient.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved.buildResult()));h.assertTrue(wait>0 && c.patient.clinic().waitTicks()==wait,"The actual patient's dressing interval survives whole-animal saves");c.finish();});
        });
    }
    private static void downed(Context c) {
        var h=c.h;var level=h.getLevel();c.patient.setHealth(c.patient.getMaxHealth());c.p.snapTo(c.patient.position().add(1,0,0));c.p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));c.p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),2));
        h.assertTrue(c.patient.trail().trainFromStock(c.p,c.p.getOffhandItem()) && c.patient.trail().command(c.p,com.bunnidogs.mod.genetics.Trail.Order.RESIDENT),"The real same-owner housemate has its own paid keeper bond and resident order");
        var enemy=h.spawnWithNoFreeWill(net.minecraft.world.entity.EntityTypes.HUSK,38,220,32);h.assertTrue(c.patient.hurtServer(level,level.damageSources().mobAttack(enemy),100) && c.patient.combat().downed(),"An actual lethal combat hit creates recoverable defeat");enemy.discard();
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());c.patient.saveWithoutId(out);var quiet=out.buildResult();quiet.putInt("CombatDanger",0);c.patient.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),quiet));c.patient.snapTo(Vec3.atBottomCenterOf(c.patient.homeNest().south()));c.patient.setDeltaMovement(Vec3.ZERO);c.patient.setOnGround(true);c.hearth.put(new ItemStack(ModItems.GUAVA.get(),2));
    }
    private static SocietyWork.Job aidPickup(Context c) {return new SocietyWork.Job(SocietyWork.Kind.FETCH_AID,c.hearth.getBlockPos(),-1,c.hearth.getBlockState(),new SocietyWork.Patient(c.patient.getUUID(),c.patient.homeNest()));}
    public static void firstAid(GameTestHelper h) {
        setup(h,c->{downed(c);c.doctor.setNoAi(false);c.p.snapTo(c.hearth.getBlockPos().getX()+18,c.hearth.getBlockPos().getY(),c.hearth.getBlockPos().getZ()+.5);
            h.runAfterDelay(280,()->{h.assertTrue(!c.patient.combat().downed() && c.patient.combat().recovering() && c.patient.combat().recoveryLeft()>0 && c.patient.getHealth()>1 && c.hearth.count(ModItems.GUAVA.get())==1 && c.doctor.society().cargoEmpty(),"A native carer walks both first-aid legs, pays one real guava and preserves injury recovery: patient="+c.patient.combat().intent()+", health="+c.patient.getHealth()+", stock="+c.hearth.count(ModItems.GUAVA.get())+", job="+c.doctor.society().active()+", cargo="+c.doctor.society().workSupplies());c.finish();});
        });
    }
    public static void firstAidCallbacks(GameTestHelper h) {
        setup(h,c->{downed(c);c.hearth.getItem(1).set(ModItems.FARM_ORIGIN.get(),c.hearth.site().toString());c.hearth.setChanged();c.doctor.snapTo(Vec3.atBottomCenterOf(c.hearth.getBlockPos().east()));h.assertTrue(c.doctor.society().perform(aidPickup(c)) && c.doctor.society().workSupplies().has(ModItems.FARM_ORIGIN.get()),"The carer collects an actual owned first-aid ration");c.rest();c.doctor.snapTo(c.patient.position().add(-1,0,0));
            Consumer<LivingHealEvent> deny=e->{if(e.getEntity()==c.patient)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(deny);try {h.assertTrue(!c.doctor.society().perform(c.treatment()) && c.patient.combat().downed() && c.doctor.society().workSupplies().getCount()==1,"A native healing veto keeps defeat and the exact paid ration intact");}finally{NeoForge.EVENT_BUS.unregister(deny);}
            boolean[] protectedCargo={false};Consumer<LivingHealEvent> move=e->{if(e.getEntity()==c.patient) {protectedCargo[0]=!c.doctor.society().takeSupplies(c.p) && !c.patient.combat().firstAid(c.p,c.p.getOffhandItem());c.patient.snapTo(c.patient.position().add(0,0,8));}};NeoForge.EVENT_BUS.addListener(move);
            try {h.assertTrue(!c.doctor.society().perform(c.treatment()) && protectedCargo[0] && c.patient.combat().downed() && c.doctor.society().workSupplies().getCount()==1,"Reentrant recovery/manual aid and a departing patient cannot consume a stale first-aid payment");}finally{NeoForge.EVENT_BUS.unregister(move);}
            c.patient.snapTo(Vec3.atBottomCenterOf(c.patient.homeNest().south()));c.patient.setOnGround(true);c.doctor.snapTo(c.patient.position().add(-1,0,0));h.assertTrue(c.doctor.society().perform(c.treatment()) && !c.patient.combat().downed() && c.patient.combat().recovering() && c.doctor.society().cargoEmpty(),"The original saved ration restores mobility once while retaining aftercare");c.finish();
        });
    }
    public static void stock(GameTestHelper h) {
        setup(h,c->{c.collect();h.assertTrue(c.hearth.getItem(0).getCount()==2 && ItemStack.isSameItemSameComponents(c.doctor.society().medicalSupplies(),c.dressing),"Collection retains the exact original dressing components");c.rest();h.assertTrue(c.doctor.society().medicalSupplies().getCount()==1 && !c.doctor.society().perform(c.pickup()),"Reload preserves owned supplies and a second pickup cannot duplicate them");
            var stranger=ApexTypesGameTest.player(h,c.doctor.position().add(1,0,0));h.assertTrue(!c.doctor.society().takeSupplies(stranger),"Another player cannot confiscate a household's paid dressing");stranger.discard();
            h.getLevel().setBlock(c.hearth.getBlockPos(),Blocks.STONE.defaultBlockState(),3);c.p.snapTo(c.doctor.position().add(1,0,0));CareMenu.open(c.p,c.doctor);var menu=(CareMenu)c.p.containerMenu;
            h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(CarePanels.CARGO)),"Actual cargo inspection remains available after its source was destroyed");
            h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(CarePanels.TAKE_SUPPLIES)) && c.doctor.society().cargoEmpty(),"The original owner recovers the stranded dressing through the native animal menu");
            int count=0;for(int i=0;i<c.p.getInventory().getContainerSize();i++)if(ItemStack.isSameItemSameComponents(c.p.getInventory().getItem(i),c.dressing))count+=c.p.getInventory().getItem(i).getCount();h.assertTrue(count==1,"Recovery returns exactly the original paid item");c.finish();
        });
    }
    public static void callbacks(GameTestHelper h) {
        setup(h,c->{c.collect();c.rest();c.doctor.snapTo(c.patient.position().add(-1,0,0));float health=c.patient.getHealth();var job=c.treatment();
            Consumer<LivingHealEvent> deny=e->{if(e.getEntity()==c.patient)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(deny);
            try{h.assertTrue(!c.doctor.society().perform(job) && c.patient.getHealth()==health && c.doctor.society().medicalSupplies().getCount()==1,"A native healing veto changes neither health nor the owned dressing");}finally{NeoForge.EVENT_BUS.unregister(deny);}
            boolean[] refused={false};Consumer<LivingHealEvent> move=e->{if(e.getEntity()==c.patient){refused[0]=!c.doctor.society().takeSupplies(c.p);c.patient.snapTo(c.patient.position().add(0,0,8));}};NeoForge.EVENT_BUS.addListener(move);
            try{h.assertTrue(!c.doctor.society().perform(job) && refused[0] && c.patient.getHealth()==health && c.doctor.society().medicalSupplies().getCount()==1,"Reentrant supply retrieval and a patient departing during healing cannot pay for a stale treatment");}finally{NeoForge.EVENT_BUS.unregister(move);}
            c.doctor.snapTo(Vec3.atBottomCenterOf(c.hearth.getBlockPos().east()));var back=c.doctor.society().plan();h.assertTrue(back!=null && back.kind()==SocietyWork.Kind.DELIVER,"A departed patient sends unused supplies back to their source");
            Consumer<net.neoforged.neoforge.event.entity.EntityMobGriefingEvent> protect=e->{if(e.getEntity()==c.doctor)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(protect);
            try{h.assertTrue(!c.doctor.society().perform(back) && c.doctor.society().medicalSupplies().getCount()==1 && c.hearth.getItem(0).getCount()==2,"A native protection veto at return keeps both owned cargo and original stock intact");}finally{NeoForge.EVENT_BUS.unregister(protect);}
            h.assertTrue(c.doctor.society().perform(back) && c.hearth.getItem(0).getCount()==3 && c.doctor.society().cargoEmpty(),"The returned original stack balances without lost or duplicated dressings");c.finish();
        });
    }
    public static void lesson(GameTestHelper h) {
        setup(h,c->{var l=h.getLevel();var bed=c.hearth.getBlockPos().west(3);l.setBlock(bed,ModBlocks.NEST.get().defaultBlockState(),3);l.getPoiManager().add(bed,ModPoi.NEST);h.assertTrue(c.doctor.moveHomeTo(l,bed)==NestHome.MoveResult.MOVED,"The learner has an actual home for the selected care lesson");
            var tag=new net.minecraft.nbt.CompoundTag();tag.putString("Learning",Learning.EMPTY.toString());c.doctor.learning().restore(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),tag),false);
            c.p.snapTo(c.doctor.position().add(1,0,0));CareMenu.open(c.p,c.doctor);var menu=(CareMenu)c.p.containerMenu;h.assertTrue(menu.clickMenuButton(c.p,menu.buttonId(CarePanels.ROLES)) && menu.clickMenuButton(c.p,menu.buttonId(LearningPanel.OPEN)) && menu.clickMenuButton(c.p,menu.buttonId(LearningPanel.teachId(Learning.Routine.NURSERY))),"The existing Nursery lesson also teaches actual family care");
            c.p.snapTo(c.patient.position().add(1,0,0));var real=c.dressing.copyWithCount(2);c.p.setItemInHand(InteractionHand.MAIN_HAND,real);float before=c.patient.getHealth();c.p.interactOn(c.patient,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.runAfterDelay(3,()->{h.assertTrue(real.getCount()==1 && c.patient.getHealth()==Math.min(c.patient.getMaxHealth(),before+6) && c.doctor.learning().knowledge().knows(Learning.Routine.NURSERY),"Observed paid manual treatment teaches the same personal care routine: supplies="+real.getCount()+", health="+c.patient.getHealth()+", knowledge="+c.doctor.learning().knowledge());c.p.interactOn(c.patient,InteractionHand.MAIN_HAND,Vec3.ZERO);h.assertTrue(real.getCount()==1,"A repeat click retains the existing manual poultice cooldown");c.finish();});
        });
    }
    public static void limits(GameTestHelper h) {
        setup(h,c->{c.doctor.setGenome(new Genome(c.doctor.genome().packed(),0));h.assertTrue(c.doctor.society().plan()==null && !c.doctor.society().perform(c.pickup()) && c.hearth.getItem(0).getCount()==3,"Learned words and high Bloom cannot bypass inherited care limits");
            c.p.snapTo(c.patient.position().add(1,0,0));var supplies=c.dressing.copyWithCount(1);c.p.setItemInHand(InteractionHand.MAIN_HAND,supplies);float before=c.patient.getHealth();c.p.interactOn(c.patient,InteractionHand.MAIN_HAND,Vec3.ZERO);h.assertTrue(supplies.isEmpty() && c.patient.getHealth()>before,"Player care remains available regardless of a resident's cognitive limitations");c.finish();
        });
    }
    public static void craft(GameTestHelper h) {
        setup(h,c->{c.hearth.setItem(0,new ItemStack(ModItems.GUAVA.get(),9));c.hearth.setItem(1,new ItemStack(Items.BONE_MEAL));c.hearth.setItem(2,new ItemStack(Items.PAPER));c.hearth.setItem(3,new ItemStack(Items.HONEYCOMB));var job=c.doctor.society().plan();
            h.assertTrue(job!=null && job.kind()==SocietyWork.Kind.CRAFT && c.doctor.society().perform(job),"A skilled resident notices a visible care need and crafts the existing real recipe");h.assertTrue(c.hearth.count(ModItems.HEARTH_POULTICE.get())==4 && c.hearth.count(ModItems.GUAVA.get())==8 && c.hearth.count(Items.BONE_MEAL)==0 && c.hearth.count(Items.PAPER)==0 && c.hearth.count(Items.HONEYCOMB)==0,"Four dressings spend exactly the authored ingredients while preserving the food reserve");c.finish();
        });
    }
}
