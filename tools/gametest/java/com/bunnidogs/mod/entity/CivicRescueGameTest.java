package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.NestBlock;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.*;

/** Real defeat, native basket identity, walking parcels, original authority and interrupted rescue. */
final class CivicRescueGameTest {
    private record Context(SocietyGameTest.Context work,Bunnidog patient,BlockPos bed) {
        GameTestHelper h() {return work.h();} HearthTreeBlockEntity home() {return work.tree();} Bunnidog carer() {return work.worker();}
        void finish() {var actual=h().getLevel().getEntity(patient.getUUID());if(actual!=null)actual.discard();carer().discard();work.p().discard();h().succeed();}
    }
    private static void setup(GameTestHelper h,Consumer<Context> action) {
        var c=SocietyGameTest.setup(h);var l=h.getLevel();var bed=c.tree().getBlockPos().east(6);
        l.setBlock(bed,ModBlocks.NEST.get().defaultBlockState(),3);
        var patient=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);patient.setGenome(SocietyGameTest.bright());patient.setStats(Stats.ZERO);patient.setAge(0);patient.setHealth(patient.getMaxHealth());patient.setNoAi(true);
        patient.snapTo(Vec3.atBottomCenterOf(c.tree().getBlockPos().north(7)));patient.setOnGround(true);patient.setCustomName(Component.literal("Remembered housemate"));patient.applyEarTag();l.addFreshEntity(patient);WorkerFixture.familiar(patient);
        h.assertTrue(patient.society().invite(c.p(),c.tree(),false) && patient.trail().keeper()==null,"A registered citizen needs no keeper bond or combat recruitment");
        h.runAfterDelay(2,()->{l.getPoiManager().add(bed,ModPoi.NEST);h.assertTrue(patient.moveHomeTo(l,bed)==NestHome.MoveResult.MOVED,"The citizen has a real reachable original recovery Nest");action.accept(new Context(c,patient,bed));});
    }
    private static net.minecraft.nbt.CompoundTag save(Bunnidog dog) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess());dog.saveWithoutId(out);return out.buildResult();
    }
    private static void down(Context c,boolean quiet) {
        var h=c.h();var l=h.getLevel();var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,34,180,25);
        h.assertTrue(c.patient().hurtServer(l,l.damageSources().mobAttack(enemy),100) && c.patient().isAlive() && c.patient().combat().downed(),"An actual lethal mob hit incapacitates the registered adult instead of deleting its experience");enemy.discard();
        if(quiet) {var data=save(c.patient());data.putInt("CombatDanger",0);c.patient().load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),data));}
        c.patient().setDeltaMovement(Vec3.ZERO);c.patient().setOnGround(true);
    }
    private static SocietyWork.Job offer(Context c,SocietyWork.Kind kind) {
        SocietyGameTest.rest(c.carer());
        return c.carer().society().offers().stream().filter(job->job.kind()==kind).findFirst().orElseThrow(()->new IllegalStateException("Missing "+kind+"; offers="+c.carer().society().offers()+", cargo="+c.carer().society().workSupplies()));
    }
    private static boolean act(Context c,SocietyWork.Job job) {
        SocietyGameTest.rest(c.carer());c.carer().snapTo(Vec3.atBottomCenterOf(job.target().east()));c.carer().setOnGround(true);return c.carer().society().perform(job);
    }
    private static void act(Context c,SocietyWork.Kind kind) {c.h().assertTrue(act(c,offer(c,kind)),"The real "+kind+" action succeeds after its paid walking leg");}
    private static void basket(Context c) {c.home().put(new ItemStack(ModItems.TRAVEL_BASKET.get()));}
    private static void packed(Context c) {basket(c);act(c,SocietyWork.Kind.FETCH_RESCUE);act(c,SocietyWork.Kind.PICKUP_RESCUE);}

    static void policy(GameTestHelper h) {
        setup(h,c->{down(c,true);var l=h.getLevel();String knowledge=c.patient().learning().knowledge().toString();
            c.patient().invulnerableTime=0;var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,34,180,25);c.patient().hurtServer(l,l.damageSources().mobAttack(enemy),100);enemy.discard();
            h.assertTrue(c.patient().isAlive() && c.patient().getHealth()==1 && !c.patient().combat().selected(),"A downed citizen remains rescuable without joining a combat party");
            var data=save(c.patient());c.patient().load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),data));
            h.assertTrue(c.patient().combat().downed() && c.patient().society().belongs(c.home()) && c.patient().learning().knowledge().toString().equals(knowledge),"Native saves preserve civic defeat, membership and learned experience");
            c.patient().invulnerableTime=0;c.patient().hurtServer(l,l.damageSources().playerAttack(c.work().p()),100);
            h.assertTrue(c.patient().isDeadOrDying() && !NestBlock.claimed(l,c.bed()),"Unbonded civic care does not grant immunity to deliberate player harm; real death releases its Nest");
            var ordinary=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);ordinary.setGenome(SocietyGameTest.bright());ordinary.setAge(0);ordinary.setNoAi(true);ordinary.snapTo(c.patient().position().add(2,0,0));l.addFreshEntity(ordinary);
            var attacker=h.spawnWithNoFreeWill(EntityTypes.HUSK,34,180,25);ordinary.hurtServer(l,l.damageSources().mobAttack(attacker),100);attacker.discard();
            h.assertTrue(ordinary.isDeadOrDying(),"An unregistered animal does not silently gain civic defeat protection");ordinary.discard();
            var terrain=ModEntities.BUNNIDOG.get().create(l,EntitySpawnReason.COMMAND);terrain.setGenome(SocietyGameTest.bright());terrain.setAge(0);terrain.setNoAi(true);terrain.snapTo(Vec3.atBottomCenterOf(c.bed().south()));terrain.setOnGround(true);l.addFreshEntity(terrain);
            h.assertTrue(terrain.society().invite(c.work().p(),c.home(),false) && terrain.moveHomeTo(l,c.bed())==NestHome.MoveResult.MOVED,"Another actual registered adult claims the now-free Nest");terrain.hurtServer(l,l.damageSources().fall(),100);
            h.assertTrue(terrain.isDeadOrDying(),"Ordinary terrain damage can still kill a registered citizen");terrain.discard();c.finish();
        });
    }
    static void chain(GameTestHelper h) {
        setup(h,c->{down(c,true);var t=c.home();var l=h.getLevel();var identity=c.patient().getUUID();var genome=c.patient().genome();var stats=c.patient().stats();String knowledge=c.patient().learning().knowledge().toString();
            t.put(new ItemStack(ModItems.GUAVA_PLANKS.get(),6));t.put(new ItemStack(Items.LEATHER));t.put(new ItemStack(Items.LEAD));t.put(new ItemStack(ModItems.GUAVA.get(),3));
            h.assertTrue(CivicRescue.needsBasket(t) && act(c,new SocietyWork.Job(SocietyWork.Kind.PLAN,t.getBlockPos(),-1,t.getBlockState())) && t.agenda().need()==HearthAgenda.Need.RESCUE && t.agenda().steps().contains(CivicRecipes.RecipeId.BASKET),"A real downed citizen creates a public material dependency without granting stock");
            h.assertTrue(act(c,new SocietyWork.Job(SocietyWork.Kind.CRAFT,t.getBlockPos(),CivicRecipes.RecipeId.BASKET.ordinal(),t.getBlockState())) && t.count(ModItems.TRAVEL_BASKET.get())==1 && t.count(ModItems.GUAVA_PLANKS.get())==0 && t.count(Items.LEATHER)==0 && t.count(Items.LEAD)==0,"A craftsperson pays the exact native six-plank/leather/Lead basket recipe");
            act(c,SocietyWork.Kind.FETCH_RESCUE);h.assertTrue(t.count(ModItems.TRAVEL_BASKET.get())==0 && !TravelBasketItem.occupied(c.carer().society().workSupplies()) && l.getEntity(identity)==c.patient(),"The first leg carries an actual empty basket while the patient remains in the world");
            act(c,SocietyWork.Kind.PICKUP_RESCUE);var carried=c.carer().society().workSupplies();
            h.assertTrue(l.getEntity(identity)==null && TravelBasketItem.occupied(carried) && TravelBasketItem.readEntry(carried).flatMap(Pedigree.Entry::id).orElseThrow().equals(identity) && !NestBlock.claimed(l,c.bed()),"Native capture stores the same real body and releases its previous Nest ticket");
            SocietyGameTest.rest(c.carer());h.assertTrue(ItemStack.matches(carried,c.carer().society().workSupplies()),"The exact occupied rescue basket and its destination survive a whole-carer save");
            act(c,SocietyWork.Kind.RELEASE_RESCUE);var returned=(Bunnidog)l.getEntity(identity);
            h.assertTrue(returned!=null && returned!=c.patient() && returned.society().belongs(t) && returned.genome().equals(genome) && returned.stats().equals(stats)
                    && returned.learning().knowledge().toString().equals(knowledge) && returned.getName().getString().equals("Remembered housemate") && c.bed().equals(returned.homeNest()) && returned.combat().downed() && returned.getHealth()==1,"Native release preserves identity, genes, points, education, name and injury, then reclaims the original still-free Nest");
            h.assertTrue(!TravelBasketItem.occupied(c.carer().society().workSupplies()),"Successful transport leaves one reusable empty basket, with no free first aid");act(c,SocietyWork.Kind.DELIVER);act(c,SocietyWork.Kind.MEAL);
            act(c,SocietyWork.Kind.FETCH_AID);act(c,SocietyWork.Kind.CARE);
            h.assertTrue(!returned.combat().downed() && returned.combat().recovering() && returned.combat().recoveryLeft()>0 && returned.getHealth()>1 && t.count(ModItems.GUAVA.get())==1 && t.count(ModItems.TRAVEL_BASKET.get())==1 && c.carer().society().cargoEmpty(),"Separate paid first aid restores mobility while the real basket returns and aftercare remains");c.finish();
        });
    }
    static void callbacks(GameTestHelper h) {
        setup(h,c->{down(c,true);basket(c);var fetch=offer(c,SocietyWork.Kind.FETCH_RESCUE);
            Consumer<EntityMobGriefingEvent> veto=e->{if(e.getEntity()==c.carer())e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(veto);
            try {h.assertTrue(!act(c,fetch) && c.home().count(ModItems.TRAVEL_BASKET.get())==1 && c.carer().society().cargoEmpty(),"Native protection refuses rescue before any basket payment");}finally {NeoForge.EVENT_BUS.unregister(veto);}
            act(c,SocietyWork.Kind.FETCH_RESCUE);var empty=c.carer().society().workSupplies();var pickup=offer(c,SocietyWork.Kind.PICKUP_RESCUE);var original=c.patient().position();boolean[] guarded={false};
            c.work().p().snapTo(original.add(1,0,1));
            Consumer<EntityMobGriefingEvent> move=e->{if(e.getEntity()==c.carer()) {guarded[0]=!c.carer().society().takeSupplies(c.work().p());c.patient().snapTo(original.add(0,0,-5));}};NeoForge.EVENT_BUS.addListener(move);
            try {h.assertTrue(!act(c,pickup) && guarded[0] && ItemStack.matches(empty,c.carer().society().workSupplies()) && !c.patient().isRemoved(),"A reentrant cargo request and moving patient cannot turn a stale pickup into a lost or duplicate body");}finally {NeoForge.EVENT_BUS.unregister(move);}
            c.patient().snapTo(original);c.patient().setOnGround(true);act(c,SocietyWork.Kind.PICKUP_RESCUE);var full=c.carer().society().workSupplies();var release=offer(c,SocietyWork.Kind.RELEASE_RESCUE);
            Consumer<EntityJoinLevelEvent> refuse=e->{if(e.getEntity().getUUID().equals(c.patient().getUUID())) {guarded[0]=c.carer().society().deathCargo().isEmpty();e.setCanceled(true);}};NeoForge.EVENT_BUS.addListener(refuse);
            try {h.assertTrue(!act(c,release) && guarded[0] && ItemStack.matches(full,c.carer().society().workSupplies()) && h.getLevel().getEntity(c.patient().getUUID())==null,"A refused native spawn and reentrant death notification keep exactly one occupied basket");}finally {NeoForge.EVENT_BUS.unregister(refuse);}
            act(c,SocietyWork.Kind.RELEASE_RESCUE);act(c,SocietyWork.Kind.DELIVER);h.assertTrue(c.home().count(ModItems.TRAVEL_BASKET.get())==1,"A later accepted release returns the same one empty basket");c.finish();
        });
    }
    static void blocked(GameTestHelper h) {
        setup(h,c->{down(c,true);packed(c);var full=c.carer().society().workSupplies();
            for(var pos:HomeRoutes.nearHome(c.bed()))if(!pos.equals(c.bed()))h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),3);
            h.assertTrue(c.carer().society().offers().isEmpty() || c.carer().society().offers().stream().allMatch(job->job.kind()==SocietyWork.Kind.DELIVER),"An obstructed recovery area cannot release a body into stone");
            var home=c.home().getBlockPos();var cached=new SocietyWork.Job(SocietyWork.Kind.DELIVER,home,-1,c.home().getBlockState());h.getLevel().setBlock(home.north(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);SocietyGameTest.rest(c.carer());
            h.assertTrue(c.carer().society().offers().isEmpty() && !act(c,cached) && ItemStack.matches(full,c.carer().society().workSupplies()),"Redstone also refuses a stale return job without unpacking or losing its patient");h.getLevel().setBlock(home.north(),Blocks.AIR.defaultBlockState(),3);
            act(c,SocietyWork.Kind.DELIVER);h.assertTrue(c.carer().society().cargoEmpty() && TravelBasketItem.occupied(c.home().getItem(0)) && h.getLevel().getEntity(c.patient().getUUID())==null,"The blocked patient returns in its exact recoverable basket to the original Hearth");
            h.assertTrue(ItemStack.matches(full,c.home().getItem(0)),"Blocked transport never rewrites the saved animal");c.finish();
        });
    }
    static void recovery(GameTestHelper h) {
        setup(h,c->{down(c,true);packed(c);var full=c.carer().society().workSupplies();var l=h.getLevel();
            var event=new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(c.carer(),l.damageSources().generic(),new java.util.ArrayList<>(),false);SocietyEvents.drops(event);SocietyEvents.drops(event);
            h.assertTrue(event.getDrops().size()==1 && ItemStack.matches(full,event.getDrops().iterator().next().getItem()),"Repeated carrier death notifications produce one exact occupied rescue basket");
            var drop=event.getDrops().iterator().next().getItem();var spot=c.home().getBlockPos().south(3);
            h.assertTrue(TravelBasketItem.release(l,spot,drop)==TravelBasketItem.Result.RELEASED && !TravelBasketItem.occupied(drop) && l.getEntity(c.patient().getUUID()) instanceof Bunnidog body && body.combat().downed(),"The normal manual basket path can recover the very same downed citizen after carrier loss");c.finish();
        });
    }
    static void authority(GameTestHelper h) {
        setup(h,c->{down(c,true);basket(c);act(c,SocietyWork.Kind.FETCH_RESCUE);var genes=c.carer().genome();
            c.carer().setGenome(new Genome(genes.packed(),0));act(c,SocietyWork.Kind.DELIVER);
            h.assertTrue(!c.patient().isRemoved() && c.home().count(ModItems.TRAVEL_BASKET.get())==1,"A limiting cognitive splice returns the real unused basket rather than granting rescue skill");
            c.carer().setGenome(genes);act(c,SocietyWork.Kind.FETCH_RESCUE);act(c,SocietyWork.Kind.PICKUP_RESCUE);var full=c.carer().society().workSupplies();var l=h.getLevel();
            l.setBlock(c.home().getBlockPos(),Blocks.AIR.defaultBlockState(),3);SocietyGameTest.rest(c.carer());
            h.assertTrue(c.carer().society().hearth()==null && c.carer().society().offers().isEmpty() && ItemStack.matches(full,c.carer().society().workSupplies()),"Source loss pauses the saved occupied parcel without inventing another destination or body");
            var stranger=ApexTypesGameTest.player(h,c.carer().position().add(1,0,0));h.assertTrue(!c.carer().society().mayTakeSupplies(stranger),"Another keeper cannot recover the stranded original household's patient");stranger.discard();
            var p=c.work().p();p.snapTo(c.carer().position().add(1,0,0));for(int slot=0;slot<36;slot++)p.getInventory().setItem(slot,new ItemStack(Items.STONE,64));
            h.assertTrue(!c.carer().society().takeSupplies(p) && ItemStack.matches(full,c.carer().society().workSupplies()),"A full original-owner inventory leaves the exact rescue basket aboard");p.getInventory().setItem(0,ItemStack.EMPTY);
            h.assertTrue(c.carer().society().takeSupplies(p) && ItemStack.matches(full,p.getInventory().getItem(0)) && c.carer().society().cargoEmpty(),"The original owner recovers the actual occupied basket after the source is destroyed");
            h.assertTrue(TravelBasketItem.release(l,c.home().getBlockPos().south(3),p.getInventory().getItem(0))==TravelBasketItem.Result.RELEASED && l.getEntity(c.patient().getUUID()) instanceof Bunnidog body && body.combat().downed(),"Manual rescue still works after the old home is gone");c.finish();
        });
    }
    static void ai(GameTestHelper h) {
        setup(h,c->{down(c,false);basket(c);c.home().put(new ItemStack(ModItems.GUAVA.get(),2));c.carer().setNoAi(false);c.work().p().snapTo(Vec3.atBottomCenterOf(c.home().getBlockPos().west(10)));
            for(int tick:new int[]{200,600,1200,1900})h.runAfterDelay(tick,()->{
                var entity=h.getLevel().getEntity(c.patient().getUUID());
                com.bunnidogs.mod.BunnidogsMod.LOGGER.info("CIVIC_RESCUE_WAIT {} carer={} ready={} offers={} cargo={} body={}",tick,c.carer().position(),c.carer().society().ready(),c.carer().society().offers(),c.carer().society().workSupplies(),entity instanceof Bunnidog body ? body.position()+" "+body.combat().intent()+" danger="+body.combat().danger()+" grounded="+body.onGround()+" bed="+body.homeNest()+" claimed="+NestBlock.claimed(h.getLevel(),c.bed()) : "packed");
            });
            h.succeedWhen(()->{var body=h.getLevel().getEntity(c.patient().getUUID());
                h.assertTrue(body instanceof Bunnidog patient && !patient.combat().downed() && patient.combat().recovering() && c.bed().equals(patient.homeNest())
                        && patient.distanceToSqr(Vec3.atCenterOf(c.bed()))<=9 && c.home().count(ModItems.TRAVEL_BASKET.get())==1 && c.home().count(ModItems.GUAVA.get())==1 && c.carer().society().cargoEmpty(),
                        "Native AI must wait for quiet, fetch/walk/pick up/carry/release/return the real basket and then pay first aid; job="+c.carer().society().active()+", cargo="+c.carer().society().workSupplies()+", at="+c.carer().position()+", stock="+c.home().count(ModItems.GUAVA.get()));
                var actual=h.getLevel().getEntity(c.patient().getUUID());if(actual!=null)actual.discard();c.carer().discard();c.work().p().discard();
            });
        });
    }
}
