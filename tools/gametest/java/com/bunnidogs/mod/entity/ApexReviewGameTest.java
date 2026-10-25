package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import com.bunnidogs.mod.registry.ModEffects;
import com.bunnidogs.mod.registry.ModEntities;

final class ApexReviewGameTest {
    static void clientDeath(GameTestHelper h) {
        var client=new LogicalClientLevel(h.getLevel());
        for(boolean flying:new boolean[]{false,true}) {
            var actor=ModEntities.apex(ApexKind.SUPPORT,flying).create(client,EntitySpawnReason.LOAD);
            actor.handleEntityEvent((byte)3);
            h.assertTrue(client.isClientSide() && !actor.isAlive(),"Ground/flying Lifebinder client death notifications must not invoke server-only debt code");
        }
    }
    static void pillar(GameTestHelper h,ApexKind kind,boolean flying) {
        ApexTypesGameTest.floor(h);var pos=ApexTypesGameTest.center(h);
        for(int y=180;y<183;y++)h.setBlock(32,y,32,Blocks.STONE);
        var p=ApexTypesGameTest.player(h,pos.add(0,3,0));p.setNoGravity(true);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);p.setHealth(1024);
        float hp=p.getHealth();var a=ApexTypesGameTest.apex(h,kind,flying,pos.add(10,flying ? 8 : 0,0));a.setTarget(p);a.setNoAi(false);
        h.runAfterDelay(220,()->{
            h.assertTrue(p.getHealth()<hp,"Real AI must reach/damage a player above a three-block pillar: "+kind+"/"+flying+" hp="+p.getHealth()+" apex="+a.position()+" target="+p.position());
            a.discard();LifebinderMarks.get(h.getLevel()).clear(p);if(p.hasEffect(ModEffects.ANVIL_DAZE))ApexDaze.clear(p);h.succeed();
        });
    }
    static void swimming(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var pos=ApexTypesGameTest.center(h);
        for(int x=20;x<=52;x++)for(int z=24;z<=40;z++)for(int y=180;y<=185;y++)h.setBlock(x,y,z,Blocks.WATER);
        var p=ApexTypesGameTest.player(h,pos.add(17,4,0));p.setNoGravity(true);var list=new ArrayList<Apex>();
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true}) {
            var a=ApexTypesGameTest.apex(h,kind,flying,pos.add(-8,1,kind.ordinal()-2));a.setNoAi(false);a.setTarget(p);list.add(a);
        }
        var pups=new ArrayList<ApexMinion>();
        for(boolean flying:new boolean[]{false,true}) {
            var caller=list.stream().filter(a->a.kind()==ApexKind.BROOD && a.flying()==flying).findFirst().orElseThrow();
            var pup=ModEntities.APEX_MINION.get().create(h.getLevel(),EntitySpawnReason.COMMAND);pup.caller(caller);pup.snapTo(pos.add(-8,1,flying ? 3 : -3));pup.setTarget(p);h.getLevel().addFreshEntity(pup);pups.add(pup);
        }
        h.runAfterDelay(40,()->{
            for(var pup:pups)h.assertTrue(pup.getX()>pos.x-5 || pup.getY()>pos.y+6,"Ground/winged elite brood must also swim strongly under native physics");
            pups.forEach(net.minecraft.world.entity.Entity::discard);
            for(var a:list)h.assertTrue(a.getX()>pos.x-5 || a.getY()>pos.y+6,"Every Apex must make substantial real progress through water: "+a.kind()+"/"+a.flying()+" at "+a.position());
            list.forEach(Apex::discard);h.succeed();
        });
    }
    static void blast(GameTestHelper h) {
        var level=h.getLevel();var pos=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(56,180,56)));
        for(int x=35;x<78;x++)for(int z=35;z<78;z++)h.setBlock(x,179,z,Blocks.STONE);
        var a=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,pos);var p=ApexTypesGameTest.player(h,pos.add(8,3,0));
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);p.setHealth(1024);
        var dog=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.COMMAND);dog.setNoAi(true);dog.snapTo(pos.add(2,0,0));level.addFreshEntity(dog);float dogHp=dog.getHealth();
        a.setTarget(p);a.skills().fireball(level,p);var fire=level.getEntitiesOfClass(ApexFireball.class,a.getBoundingBox().inflate(3)).getFirst();
        for(int i=0;i<25 && fire.isAlive();i++)fire.tick();
        h.assertTrue(dog.getHealth()==dogHp,"Power-four fireballs spare ordinary bunnidogs too");
        h.assertTrue(p.getHealth()<1024,"The real power-four fireball reaches an elevated player");
        a.setHealth(a.blastCost()*.5F);var cast=new ApexSpecialGoal(a);cast.start();
        h.assertTrue(a.lastStand() && a.fuseLength()==60,"Lethal next self-cost chooses power twenty and a longer red fuse");
        float hp=a.getHealth();for(int t=0;t<a.fuseLength()-1;t++)cast.tick();h.assertTrue(a.isAlive() && a.getHealth()==hp,"Final blast cannot detonate before its full configured fuse");
        cast.tick();h.assertTrue(!a.isAlive(),"The real power-twenty last stand spends the caster's remaining life");
        var flying=ApexTypesGameTest.apex(h,ApexKind.BLAST,true,pos.add(0,8,0));h.assertTrue(flying.fuseLength()==70,"An aerial normal fuse adds one and a half seconds");flying.lastStand(true);
        h.assertTrue(flying.fuseLength()==90,"Aerial last stand retains its extra final-blast second");flying.discard();dog.discard();
    }
    static void support(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var pos=ApexTypesGameTest.center(h);var a=ApexTypesGameTest.apex(h,ApexKind.SUPPORT,false,pos);
        var p=ApexTypesGameTest.player(h,pos.add(8,0,0));var marks=LifebinderMarks.get(level);
        for(int i=0;i<12;i++)marks.mark(a,p);
        h.assertTrue(p.getMaxHealth()==2 && marks.lost(p.getUUID())==9,"Heart loss stops at one complete container");
        p.getAttribute(Attributes.MAX_HEALTH).removeModifier(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","lifebinder_hearts"));
        ApexPlayerEvents.login(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));
        h.assertTrue(p.getMaxHealth()==2,"Login immediately reapplies saved heart debt instead of granting a free-health window");
        var encoded=LifebinderMarks.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,marks).getOrThrow();var restored=LifebinderMarks.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow();
        h.assertTrue(restored.lost(p.getUUID())==9,"World save data retains debt while a victim/caster is unloaded");
        p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.TOTEM_OF_UNDYING));p.setHealth(1);p.invulnerableTime=0;
        p.hurtServer(level,p.damageSources().mobAttack(a),1000);ApexPlayerEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        h.assertTrue(p.isAlive() && p.getOffhandItem().isEmpty() && p.getMaxHealth()==8,"A successful real totem repays exactly three containers");
        p.removeAllEffects();p.invulnerableTime=0;p.hurtServer(level,p.damageSources().mobAttack(a),1000);
        h.assertTrue(!p.isAlive() && marks.lost(p.getUUID())==0 && p.getMaxHealth()==20,"Actual death restores every lost container");
        var q=ApexTypesGameTest.player(h,pos.add(6,0,0));marks.mark(a,q);a.invulnerableTime=0;a.hurtServer(level,q.damageSources().playerAttack(q),10000);
        h.assertTrue(marks.lost(q.getUUID())==0 && q.getMaxHealth()==20,"Killing the marking Lifebinder restores its victim's containers");
        var second=ApexTypesGameTest.apex(h,ApexKind.SUPPORT,false,pos);second.setTarget(q);q.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);q.setHealth(512);q.invulnerableTime=0;second.skills().anvil(level,q);
        var anvil=level.getEntitiesOfClass(ApexAnvil.class,second.getBoundingBox().inflate(3)).getFirst();for(int i=0;i<40 && anvil.isAlive();i++)anvil.tick();
        h.assertTrue(q.hasEffect(ModEffects.ANVIL_DAZE) && q.getAttributeValue(Attributes.MOVEMENT_SPEED)==0,"An actual anvil hit immobilizes the victim");
        int nonce=q.getPersistentData().getCompoundOrEmpty("BunnidogsAnvilDaze").getIntOr("Nonce",0);
        ApexDaze.struggle(q,nonce,Integer.MAX_VALUE);
        h.assertTrue(q.hasEffect(ModEffects.ANVIL_DAZE),"A single forged/burst update cannot skip the one-second escape budget");
        for(int i=1;i<=20;i++)h.runAfterDelay(i,()->ApexDaze.struggle(q,nonce,18));
        h.runAfterDelay(21,()->{h.assertTrue(!q.hasEffect(ModEffects.ANVIL_DAZE),"A second of intense look movement releases the stun");second.discard();h.succeed();});
    }
    static void brood(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var pos=ApexTypesGameTest.center(h);var a=ApexTypesGameTest.apex(h,ApexKind.BROOD,false,pos);
        var p=ApexTypesGameTest.player(h,pos.add(10,3,0));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);p.setHealth(512);p.setNoGravity(true);a.setTarget(p);
        a.skills().summon(level);var brood=level.getEntitiesOfClass(ApexMinion.class,a.getBoundingBox().inflate(16),m->m.belongsTo(a));
        h.assertTrue(brood.size()==5 && brood.stream().allMatch(m->m.getMaxHealth()<=36),"Brood wave/health are moderated while speed and bite stay elite");
        var meal=brood.getFirst();meal.snapTo(pos.add(1,0,0));meal.setHealth(12);a.setHealth(a.getMaxHealth()*.3F);float hp=a.getHealth();
        h.assertTrue(a.skills().consume(level,meal) && a.getHealth()==hp+12 && meal.isRemoved(),"A wounded caller eats actual remaining minion health once");
        var shot=brood.get(1);shot.snapTo(pos.add(1,1,0));shot.setHealth(20);shot.launchAt(p);float before=p.getHealth();
        final boolean[] knocked={false};
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent> impact=e->{if(e.getEntity()==p && e.getStrength()>=2)knocked[0]=true;};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(impact);
        h.runAfterDelay(35,()->{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(impact);h.assertTrue(p.getHealth()<before && knocked[0],"A real thrown bunnidog reaches an elevated victim and imparts knockback: hp="+p.getHealth()+" body="+shot.position());a.discard();h.succeed();});
    }
}
