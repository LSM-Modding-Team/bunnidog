package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

final class ApexFollowupGameTest {
    private static void ready(Apex a,GameTestHelper h) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());a.saveWithoutId(out);
        var tag=out.buildResult();tag.putInt("ApexAbilityWait",0);a.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),tag));
    }
    static void blast(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var pos=ApexTypesGameTest.center(h);
        var a=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,pos);var p=ApexTypesGameTest.player(h,pos.add(9,0,0));
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);p.setHealth(512);p.setNoGravity(true);
        var nativeBlast=new ServerExplosion(level,a,null,null,pos,10,false,Explosion.BlockInteraction.KEEP);
        float expected=new ExplosionDamageCalculator().getEntityDamageAmount(nativeBlast,p,ServerExplosion.getSeenPercent(pos,p))*.96F;
        float before=p.getHealth();h.assertTrue(a.skills().blast(level,false),"The ordinary native explosion resolves");
        h.assertTrue(Math.abs(before-p.getHealth()-expected)<.01,"Self-explosion victim damage receives exactly the stacked damage reductions");
        p.snapTo(pos.add(5,0,0));p.invulnerableTime=0;before=p.getHealth();
        var nativeFire=new ServerExplosion(level,a,null,null,pos,4,false,Explosion.BlockInteraction.KEEP);
        expected=new ExplosionDamageCalculator().getEntityDamageAmount(nativeFire,p,ServerExplosion.getSeenPercent(pos,p))*.6F;
        h.assertTrue(ApexSkills.explosion(level,a,pos,4,true) && Math.abs(before-p.getHealth()-expected)<.01,"Power-four fireball damage receives the same stacked reductions");
        ready(a,h);a.setNoAi(false);a.setTarget(p);p.snapTo(pos.add(20,0,0));
        for(int t=0;t<75;t++)a.skills().tick(level);
        var fire=level.getEntitiesOfClass(ApexFireball.class,a.getBoundingBox().inflate(4),Entity::isAlive);
        h.assertTrue(!fire.isEmpty() && a.abilityWait()==0,"Ready Blastmaws shoot at distant targets instead of withholding fire until recovery");fire.forEach(Entity::discard);
        a.invulnerableTime=0;h.assertTrue(a.skills().blast(level,false),"A second detonation begins a fresh fireball delay");
        for(int t=0;t<74;t++)a.skills().tick(level);
        h.assertTrue(level.getEntitiesOfClass(ApexFireball.class,a.getBoundingBox().inflate(4),Entity::isAlive).isEmpty(),"A detonation resets the complete fireball cooldown");a.skills().tick(level);
        h.assertTrue(!level.getEntitiesOfClass(ApexFireball.class,a.getBoundingBox().inflate(4),Entity::isAlive).isEmpty(),"Fireballs resume only after the new delay");
        h.setBlock(32,179,32,Blocks.STONE);a.setOnGround(true);var jump=new ApexAbilityGoal(a);
        h.assertTrue(jump.canUse(),"A ready ground Blastmaw may jump to close a distant gap");jump.start();for(int t=0;t<24 && !a.mobility().leaping();t++)jump.tick();
        h.assertTrue(a.mobility().leaping() && a.abilityWait()==0 && !a.doHurtTarget(level,p),"Approach jumps preserve the self-explosion charge and never become melee attacks");jump.stop();a.discard();
    }
    static void anvil(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var pos=ApexTypesGameTest.center(h);
        var a=ApexTypesGameTest.apex(h,ApexKind.SUPPORT,false,pos);var p=ApexTypesGameTest.player(h,pos.add(12,0,0));
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);p.setHealth(512);p.setNoGravity(true);a.setTarget(p);
        a.skills().anvil(level,p);var anvil=level.getEntitiesOfClass(ApexAnvil.class,a.getBoundingBox().inflate(3),Entity::isAlive).getFirst();
        h.assertTrue(anvil.getBbWidth()==2 && anvil.getBbHeight()==2,"The projectile has an actual two-by-two collision body");
        double start=anvil.getY(),peak=start;int ticks=0;float hp=p.getHealth();
        while(anvil.isAlive() && ticks++<50) {anvil.tick();peak=Math.max(peak,anvil.getY());}
        var brute=ApexTypesGameTest.apex(h,ApexKind.BRUTE,true,pos.add(-8,8,0));brute.setGenome(a.genome());brute.setStats(a.stats());
        h.assertTrue(peak>start+1 && ticks>=10,"Anvils follow a visibly raised, slower native ballistic arc");
        h.assertTrue(Math.abs(hp-p.getHealth()-brute.getAttributeValue(Attributes.ATTACK_DAMAGE))<.001,"Anvil damage equals a flying melee Apex's strike for the same line");brute.discard();
        a.setNoAi(false);for(int t=0;t<70;t++)a.skills().tick(level);
        var first=level.getEntitiesOfClass(ApexAnvil.class,a.getBoundingBox().inflate(3),Entity::isAlive);
        h.assertTrue(first.size()==1,"The first automatic anvil has its doubled initial delay");first.forEach(Entity::discard);
        for(int t=0;t<279;t++)a.skills().tick(level);
        h.assertTrue(level.getEntitiesOfClass(ApexAnvil.class,a.getBoundingBox().inflate(3),Entity::isAlive).isEmpty(),"Another anvil cannot bypass the doubled fourteen-second cooldown");a.skills().tick(level);
        h.assertTrue(level.getEntitiesOfClass(ApexAnvil.class,a.getBoundingBox().inflate(3),Entity::isAlive).size()==1,"Anvils resume when the full cooldown elapses");
        ready(a,h);p.snapTo(pos.add(12,0,0));a.setTarget(p);
        var cast=new ApexSpecialGoal(a);cast.start();
        for(int t=0;t<20;t++)cast.tick();
        var locked=p.getEyePosition().subtract(a.skills().mouth()).normalize();
        p.snapTo(pos.add(12,0,8));for(int t=0;t<10;t++)cast.tick();
        var hooks=level.getEntitiesOfClass(ApexHook.class,a.getBoundingBox().inflate(3),Entity::isAlive);
        h.assertTrue(!hooks.isEmpty() && hooks.getLast().getDeltaMovement().normalize().dot(locked)>.999,"Hook aim locks ten ticks before launch, allowing a late lateral dodge");cast.stop();hooks.forEach(Entity::discard);
        a.discard();ApexDaze.clear(p);LifebinderMarks.get(level).clear(p);
        var caller=ApexTypesGameTest.apex(h,ApexKind.BROOD,true,pos.add(-10,8,0));caller.setTarget(p);
        var pup=ModEntities.APEX_MINION.get().create(level,net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        pup.caller(caller);pup.setPos(p.position().add(4,6,0));level.addFreshEntity(pup);pup.setNoAi(true);
        var flight=new WingFlightGoal(pup);h.assertTrue(flight.canUse(),"Winged brood inherit aerial combat");flight.start();
        for(int t=0;t<47;t++)flight.tick();
        h.assertTrue(pup.getDeltaMovement().length()<.01,"A winged brood pauses for its small pre-dive warning");
        flight.tick();h.assertTrue(pup.getDeltaMovement().y<-.1,"Winged brood dive toward the target after warning");
        pup.setPos(p.position());p.invulnerableTime=0;float health=p.getHealth();flight.tick();
        h.assertTrue(p.getHealth()<health,"A brood dive lands its own gene-derived bite");
        for(int t=0;t<15;t++)flight.tick();
        h.assertTrue(pup.getDeltaMovement().y>0,"After one strike winged brood climb back to their orbit");
        flight.stop();pup.discard();caller.discard();
    }
    static void dive(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var pos=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,pos.add(0,3,0));p.setNoGravity(true);
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);p.setHealth(512);var a=ApexTypesGameTest.apex(h,ApexKind.BRUTE,true,pos.add(10,11,0));a.setTarget(p);a.setNoAi(false);
        final boolean[] built={false};
        h.onEachTick(()->{
            if(built[0] || a.getDeltaMovement().y>-.3 || a.getDeltaMovement().lengthSqr()<.65)return;
            built[0]=true;
            // Construct three complete wall layers after the actual stoop starts, like taking cover during an attack.
            for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)if(Math.max(Math.abs(x),Math.abs(z))>=2)
                for(int y=180;y<196;y++)h.setBlock(32+x,y,32+z,Blocks.STONE);
        });
        h.runAfterDelay(220,()->{
            h.assertTrue(built[0] && p.getHealth()<512,"The real flying melee attack must break all three wall layers and reach its victim");a.discard();h.succeed();
        });
    }
    static void meals(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var pos=ApexTypesGameTest.center(h);var a=ApexTypesGameTest.apex(h,ApexKind.BROOD,false,pos);
        var p=ApexTypesGameTest.player(h,pos.add(12,3,0));p.setNoGravity(true);a.setTarget(p);a.setHealth(a.getMaxHealth()*.05F);
        a.skills().summon(level);var pack=level.getEntitiesOfClass(ApexMinion.class,a.getBoundingBox().inflate(20),m->m.belongsTo(a));
        for(var pup:pack) {pup.snapTo(pos.add(1,0,0));pup.setNoAi(true);}float hp=a.getHealth();a.setNoAi(false);
        h.runAfterDelay(10,()->{
            long remaining=pack.stream().filter(Entity::isAlive).count();
            h.assertTrue(a.getHealth()>hp+15 && remaining<=pack.size()-2,"A critically wounded caller eats several real nearby brood without the old eighty-tick wait");a.discard();h.succeed();
        });
    }
}
