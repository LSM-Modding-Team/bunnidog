package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.CrownEncounter;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/** Reported combat failures exercised with native projectiles, shields, saves and running AI. */
final class CombatReviewGameTest {
    private static ServerPlayer namedPlayer(GameTestHelper h,Vec3 at,String name) {
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),name),net.minecraft.server.level.ClientInformation.createDefault()) {
            @Override public boolean isClientAuthoritative() {return false;}
        };
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        p.connection.markClientLoaded();p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);CareMenuGameTest.prepare(p);p.snapTo(at);return p;
    }
    static void launcher(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var at=ApexTypesGameTest.center(h);
        var p=namedPlayer(h,at,"launcher_owner");p.setYRot(-90);p.setXRot(0);
        var friend=namedPlayer(h,at.add(4,0,2),"launcher_ally");
        var enemy=namedPlayer(h,at.add(10,0,0),"launcher_enemy");enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);enemy.setHealth(1024);
        var scoreboard=level.getScoreboard();var team=scoreboard.addPlayerTeam("launcher_"+p.getUUID().toString().substring(0,8));team.setAllowFriendlyFire(true);
        scoreboard.addPlayerToTeam(p.getScoreboardName(),team);scoreboard.addPlayerToTeam(friend.getScoreboardName(),team);
        var ally=EntityTypes.COW.create(level,EntitySpawnReason.TRIGGERED);ally.setNoAi(true);ally.snapTo(at.add(5,0,0));level.addFreshEntity(ally);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        var pet=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.TRIGGERED);pet.setNoAi(true);pet.setAge(0);pet.setOnGround(true);pet.snapTo(at.add(2,0,0));level.addFreshEntity(pet);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),2));
        h.assertTrue(pet.trail().trainFromStock(p,p.getOffhandItem()),"The shot protects an actually bonded animal");
        var launcher=new ItemStack(ModItems.FIREBALL_LAUNCHER.get());launcher.set(ModItems.TOOL_POWER.get(),5);p.setItemInHand(InteractionHand.MAIN_HAND,launcher);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOLATILE_CHARGE.get(),5));
        ModItems.FIREBALL_LAUNCHER.get().use(level,p,InteractionHand.MAIN_HAND);
        var ball=level.getEntitiesOfClass(LauncherFireball.class,p.getBoundingBox().inflate(4)).getFirst();
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());ball.saveWithoutId(out);ball.discard();
        var loaded=ModEntities.LAUNCHER_FIREBALL.get().create(level,EntitySpawnReason.LOAD);loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));level.addFreshEntity(loaded);
        h.assertTrue(!loaded.canHitEntity(pet) && !loaded.canHitEntity(ally) && !loaded.canHitEntity(friend) && loaded.canHitEntity(enemy),"Restored ammunition passes allied bodies and still collides with enemies");
        float ownerHp=p.getHealth(),friendHp=friend.getHealth(),allyHp=ally.getHealth(),petHp=pet.getHealth();
        p.setDeltaMovement(Vec3.ZERO);friend.setDeltaMovement(Vec3.ZERO);ally.setDeltaMovement(Vec3.ZERO);pet.setDeltaMovement(Vec3.ZERO);
        for(int t=0;t<20 && loaded.isAlive();t++)loaded.tick();
        h.assertTrue(loaded.isRemoved() && enemy.getHealth()<1024,"The real saved power-15 shot crosses allies and strikes an enemy");
        h.assertTrue(p.getHealth()==ownerHp && friend.getHealth()==friendHp && ally.getHealth()==allyHp && pet.getHealth()==petHp,"Shooter, teammate, team animal and bonded pet take no direct or blast damage");
        h.assertTrue(p.getDeltaMovement().lengthSqr()==0 && friend.getDeltaMovement().lengthSqr()==0 && ally.getDeltaMovement().lengthSqr()==0 && pet.getDeltaMovement().lengthSqr()==0,"Friendly entities receive no explosion knockback");
        h.assertTrue(launcher.getDamageValue()==5 && p.getOffhandItem().isEmpty(),"Safety preserves the real five-charge/five-wear payment");
        var cancelled=ModEntities.LAUNCHER_FIREBALL.get().create(level,EntitySpawnReason.TRIGGERED);cancelled.prepare(p,1);cancelled.setPos(enemy.position());level.addFreshEntity(cancelled);
        float before=enemy.getHealth();enemy.invulnerableTime=0;
        Consumer<ExplosionEvent.Start> reject=e->{if(e.getExplosion().getDirectSourceEntity()==cancelled)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(reject);
        try {cancelled.onHit(new BlockHitResult(cancelled.position(),Direction.UP,enemy.blockPosition().below(),false));}
        finally {NeoForge.EVENT_BUS.unregister(reject);}
        h.assertTrue(enemy.getHealth()==before,"Native explosion cancellation still prevents damage");
        scoreboard.removePlayerTeam(team);pet.discard();ally.discard();p.discard();friend.discard();enemy.discard();
    }
    private static void block(GameTestHelper h,ServerPlayer p) {
        p.stopUsingItem();for(int t=0;t<100;t++)p.getCooldowns().tick();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));p.startUsingItem(InteractionHand.OFF_HAND);
        for(int t=0;t<6;t++)p.doTick();p.invulnerableTime=0;p.setDeltaMovement(Vec3.ZERO);
        h.assertTrue(p.getItemBlockingWith()!=null,"The real shield has passed its native raising delay");
    }
    static void shield(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var at=ApexTypesGameTest.center(h);var level=h.getLevel();var p=ApexTypesGameTest.player(h,at.add(2,0,0));p.setYRot(90);p.setXRot(0);
        var ground=ApexTypesGameTest.apex(h,ApexKind.BRUTE,false,at);
        block(h,p);float hp=p.getHealth();ground.doHurtTarget(level,p);
        h.assertTrue(p.getHealth()==hp && !p.isUsingItem() && p.getCooldowns().isOnCooldown(p.getOffhandItem()),"A facing melee block stops this bite but disables the shield for following attacks");
        for(int t=0;t<100;t++)p.getCooldowns().tick();h.assertTrue(!p.getCooldowns().isOnCooldown(p.getOffhandItem()),"Native shield recovery lasts five seconds");
        var flying=ApexTypesGameTest.apex(h,ApexKind.BRUTE,true,at);block(h,p);flying.doHurtTarget(level,p);
        h.assertTrue(p.isUsingItem() && !p.getCooldowns().isOnCooldown(p.getOffhandItem()),"The flying melee variant does not inherit shield breaking");
        var crown=ModEntities.CROWNED_APEX.get().create(level,EntitySpawnReason.TRIGGERED);crown.prepare(BlockPos.containing(at).below(),1);crown.snapTo(at);crown.setNoAi(true);level.addFreshEntity(crown);
        block(h,p);crown.doHurtTarget(level,p);
        h.assertTrue(p.isUsingItem() && !p.getCooldowns().isOnCooldown(p.getOffhandItem()),"The Crowned Apex does not inherit shield breaking");
        block(h,p);p.setYRot(-90);p.setYHeadRot(-90);ground.doHurtTarget(level,p);
        h.assertTrue(p.getHealth()<hp && !p.getCooldowns().isOnCooldown(p.getOffhandItem()),"A hit behind the shield does damage without falsely disabling it");
        ground.discard();flying.discard();crown.discard();p.discard();
    }
    static void beam(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var at=Vec3.atBottomCenterOf(c.above());var p=ApexTypesGameTest.player(h,at.add(20,0,0));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);p.setHealth(1024);
        var boss=ModEntities.CROWNED_APEX.get().create(h.getLevel(),EntitySpawnReason.TRIGGERED);boss.prepare(c,1);boss.snapTo(at);h.getLevel().addFreshEntity(boss);boss.setTarget(p);boss.beginCast(2,p);
        for(int t=0;t<34;t++)boss.tick();h.assertTrue(p.getHealth()==1024,"The tracking laser retains its full harmless warning");
        p.snapTo(at.add(20,0,5));
        for(int t=0;t<40;t++) {p.invulnerableTime=0;boss.tick();}
        h.assertTrue(p.getHealth()<1024,"A target outside the original beam is caught by its actual native tracking sweep");
        var x=new Vec3(1,0,0);var opposite=CrownedApex.turnBeam(x,new Vec3(-1,0,0),.012);
        h.assertTrue(Math.abs(opposite.length()-1)<1e-8 && opposite.x>0 && Math.acos(opposite.dot(x))<=.0120001,"Even a complete aim reversal respects the angular limit");
        boss.cancel();boss.beginCast(2,p);for(int t=0;t<34;t++)boss.tick();float hp=p.getHealth();
        p.snapTo(at.add(-20,0,0));for(int t=0;t<60;t++) {p.invulnerableTime=0;boss.tick();}
        h.assertTrue(p.getHealth()==hp,"A fast reposition can escape the entire sweep rather than causing a tracking snap");
        boss.discard();p.discard();
    }
    static void pack(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var at=Vec3.atBottomCenterOf(c.above());var level=h.getLevel();
        var seal=CrownDungeonGameTest.seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(seal.getBlockPos().east()));
        CrownDungeonGameTest.unlock(h,seal,p);seal.begin(p,InteractionHand.MAIN_HAND);p.snapTo(at.add(10,0,4));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);p.setHealth(1024);
        var boss=(CrownedApex)level.getEntity(seal.enemies().iterator().next());boss.setTarget(p);boss.setHealth(boss.getMaxHealth()*.6F);boss.tick();boss.setNoAi(true);
        var adds=level.getEntitiesOfClass(Apex.class,boss.getBoundingBox().inflate(60),e->e!=boss && CrownEncounter.controller(e)==seal);
        h.assertTrue(adds.size()==4 && adds.stream().allMatch(e->!e.isNoAi() && e.getTarget()==p),"Every champion is active and receives the boss's legal target immediately");
        // Reproduce the old saved NoAI pack, then load its original boss identity without respawning escorts.
        for(var add:adds)if(add.kind()!=ApexKind.BRUTE)add.setNoAi(true);
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());boss.saveWithoutId(out);var tag=out.buildResult();tag.remove("CrownActivePack");tag.putBoolean("NoAI",false);boss.discard();
        var restored=ModEntities.CROWNED_APEX.get().create(level,EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));restored.setTarget(p);level.addFreshEntity(restored);restored.tick();restored.setNoAi(true);
        h.assertTrue(adds.stream().allMatch(e->!e.isNoAi()) && level.getEntitiesOfClass(Apex.class,restored.getBoundingBox().inflate(60),e->e!=restored && CrownEncounter.controller(e)==seal).size()==4,"Old disabled escorts resume once without duplicating the saved pack");
        var start=new HashMap<UUID,Vec3>();for(var add:adds)start.put(add.getUUID(),add.position());
        final Set<UUID> casting=new HashSet<>();
        for(int t=1;t<=160;t++)h.runAfterDelay(t,()->{for(var add:adds)if(add.casting() || add.mobility().leaping())casting.add(add.getUUID());});
        h.runAfterDelay(161,()->{
            for(var add:adds) {
                h.assertTrue(add.position().distanceToSqr(start.get(add.getUUID()))>1,"Each formerly idle escort actually moves under native AI: "+add.kind()+"/"+add.flying());
                if(add.kind()!=ApexKind.BRUTE)h.assertTrue(casting.contains(add.getUUID()),"Each ranged/support champion starts an actual native ability: "+add.kind()+"/"+add.flying());
            }
            var support=adds.stream().filter(e->e.kind()==ApexKind.SUPPORT).findFirst().orElseThrow();
            var marks=LifebinderMarks.get(level);marks.mark(support,p);h.assertTrue(marks.lost(p.getUUID())>0,"The active support can create actual saved heart debt");
            restored.setHealth(restored.getMaxHealth()*.2F);restored.tick();
            h.assertTrue(marks.lost(p.getUUID())==0,"Retiring the middle-phase support releases its debt instead of leaving an unkillable caster");
            seal.reset();p.discard();h.succeed();
        });
    }
    static void director(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var at=Vec3.atBottomCenterOf(c.above());var level=h.getLevel();
        var p=ApexTypesGameTest.player(h,at.add(24,0,0));p.setInvulnerable(true);
        var boss=ModEntities.CROWNED_APEX.get().create(level,EntitySpawnReason.TRIGGERED);boss.prepare(c,1);boss.snapTo(at);boss.setTarget(p);level.addFreshEntity(boss);
        var seen=List.of(new HashSet<Integer>(),new HashSet<Integer>(),new HashSet<Integer>());final double[] travel={0};
        for(int tick=1;tick<=1600;tick++)h.runAfterDelay(tick,()->{
            if(boss.warning()!=0)seen.get(boss.phase()-1).add(boss.warning());
            travel[0]=Math.max(travel[0],boss.position().distanceToSqr(at));
            if(boss.warning()==0 && !boss.mobility().leaping() && boss.distanceToSqr(p)<144)
                p.snapTo(at.add(boss.getX()>at.x ? -28 : 28,0,boss.getZ()>at.z ? -12 : 12));
        });
        h.runAfterDelay(660,()->{h.assertTrue(seen.getFirst().containsAll(Set.of(1,2,3,4)),"The Hunt actually uses pounce, beam and blast through its own AI: "+seen);boss.setHealth(boss.getMaxHealth()*.55F);p.snapTo(at.add(-24,0,0));});
        h.runAfterDelay(960,()->{h.assertTrue(seen.get(1).containsAll(Set.of(1,2)) && !seen.get(1).contains(4),"The Living Crown mixes leaps and beams without a hook/blast overlap: "+seen);boss.setHealth(boss.getMaxHealth()*.2F);p.snapTo(at.add(0,0,24));});
        h.runAfterDelay(1601,()->{
            h.assertTrue(seen.get(2).containsAll(Set.of(1,2,3,4)),"The aerial finale actually uses every major attack, including its tracking beam: "+seen);
            h.assertTrue(travel[0]>100 && boss.isNoGravity() && boss.getY()>at.y+4,"The native boss moves across the arena and returns to sustained flight");
            boss.discard();p.discard();h.succeed();
        });
    }
}
