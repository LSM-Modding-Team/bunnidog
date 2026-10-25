package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Actual native damage, combat decisions, genetic support and saved experience above basic reliable orders. */
public final class TacticsGameTest {
    private static void floor(GameTestHelper h) {for(int x=23;x<42;x++)for(int z=26;z<39;z++){h.setBlock(x,239,z,Blocks.STONE);for(int y=240;y<245;y++)h.setBlock(x,y,z,Blocks.AIR);}}
    private static Bunnidog fighter(GameTestHelper h,int x,boolean walk) {
        var d=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);d.setGenome(Genome.DEFAULT);d.setStats(Stats.ZERO);d.setAge(0);d.setHealth(d.getMaxHealth());d.setNoAi(!walk);d.snapTo(h.absoluteVec(new Vec3(x+.5,240,30.5)));d.setOnGround(true);h.getLevel().addFreshEntity(d);return d;
    }
    private static ServerPlayer keeper(GameTestHelper h,Bunnidog... dogs) {
        var p=ApexTypesGameTest.player(h,h.absoluteVec(new Vec3(31.5,240,31.5)));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),12));
        for(var d:dogs)h.assertTrue(d.trail().trainFromStock(p,p.getOffhandItem()) && d.combat().assist(p),"Each fighter bonds and joins the native chosen combat roster deliberately");return p;
    }
    private static Mob enemy(GameTestHelper h,int z) {var e=h.spawnWithNoFreeWill(EntityTypes.HUSK,34.5F,240,z+.5F);e.setHealth(e.getMaxHealth());return e;}
    private static CompoundTag save(Bunnidog d) {var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,d.registryAccess());d.saveWithoutId(out);return out.buildResult();}
    private static void load(Bunnidog d,CompoundTag tag) {d.load(TagValueInput.create(ProblemReporter.DISCARDING,d.registryAccess(),tag));d.setOnGround(true);}
    private static void experience(Bunnidog d,int value) {var tag=save(d);tag.putInt("CombatTactics",value);load(d,tag);}
    public static void learning(GameTestHelper h) {
        floor(h);var dog=fighter(h,29,true);var p=keeper(h,dog);var enemy=enemy(h,30);p.attack(enemy);float after=enemy.getHealth();
        Consumer<LivingIncomingDamageEvent> deny=e->{if(e.getSource().getEntity()==dog)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(deny);
        h.runAfterDelay(35,()->{
            NeoForge.EVENT_BUS.unregister(deny);h.assertTrue(enemy.getHealth()==after && dog.combat().tactics().experience()==0,"Cancelled native bites grant no tactical experience");p.attack(enemy);
            h.runAfterDelay(65,()->{h.assertTrue(enemy.getHealth()<after && dog.combat().tactics().experience()==Learning.MASTERED && dog.combat().tactics().coordinates(),"Real native assisted exchanges teach the helper profile group coordination: experience="+dog.combat().tactics().experience()+", enemy="+enemy.getHealth()+", intent="+dog.combat().intent());
                var saved=save(dog);load(dog,saved);h.assertTrue(dog.combat().tactics().experience()==Learning.MASTERED,"Whole-animal native saves retain personal combat experience");dog.discard();enemy.discard();p.discard();h.succeed();});
        });
    }
    public static void coordination(GameTestHelper h) {
        floor(h);var one=fighter(h,27,false);var two=fighter(h,28,false);var three=fighter(h,29,false);var p=keeper(h,one,two,three);var a=enemy(h,30);var b=enemy(h,32);var c=enemy(h,34);for(var e:new Mob[]{a,b,c})e.setTarget(p);
        for(var d:new Bunnidog[]{one,two,three})d.combat().tick();h.assertTrue(one.getTarget()==a && two.getTarget()==a && three.getTarget()==a,"New recruits retain their straightforward nearest-threat instinct");
        for(var d:new Bunnidog[]{one,two,three})experience(d,Learning.MASTERED);for(var d:new Bunnidog[]{one,two,three})d.combat().tick();
        h.assertTrue(java.util.stream.Stream.of(one,two,three).map(Bunnidog::getTarget).distinct().count()==3,"Experienced capable fighters cover three visible legal threats through actual combat target selection");
        var body=one.genome();one.setGenome(new Genome(body.packed(),0));h.assertTrue(!one.combat().tactics().coordinates() && one.combat().assisting(),"A limiting splice makes advanced knowledge dormant without removing basic Assist");
        Genome supported=null;for(int code=0;code<256 && supported==null;code++){int word=0;for(int i=0;i<4;i++)word|=(((code>>>(i*2))&3)*17)<<(i*8);var candidate=new Genome(body.packed(),word);if(CapabilityRules.potential(candidate.mind(),CapabilityRules.Task.TACTICS)==CapabilityRules.Potential.SUPPORTED)supported=candidate;}
        h.assertTrue(supported!=null,"Several actual loci can produce supported tactical capacity");one.setGenome(supported);h.assertTrue(!one.combat().tactics().coordinates(),"Supported experience alone does not invent an active instruction");p.attack(a);h.assertTrue(one.combat().tactics().coordinates(),"A visible participating keeper supplies concrete local guidance");p.snapTo(p.position().add(30,0,0));h.assertTrue(!one.combat().tactics().coordinates(),"Supported coordination stops outside actual hearing and sight");p.snapTo(one.position().add(1,0,0));TrailCompanion.call(p,InteractionHand.MAIN_HAND);h.assertTrue(one.getTarget()==null && two.getTarget()==null && three.getTarget()==null,"The normal heard Whistle recalls the whole group regardless of tactical capacity");
        one.setGenome(body);var old=save(one);old.putBoolean("CombatAssist",true);old.remove("CombatTactics");load(one,old);h.assertTrue(one.combat().tactics().experience()==Learning.MASTERED,"Old active fighters retain familiar tactical experience once");old.putInt("CombatTactics",0);load(one,old);h.assertTrue(one.combat().tactics().experience()==0,"An explicit saved beginner never gains repeated migration mastery");
        for(var d:new Bunnidog[]{one,two,three})d.discard();for(var e:new Mob[]{a,b,c})e.discard();p.discard();h.succeed();
    }
}
