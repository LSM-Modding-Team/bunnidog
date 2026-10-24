package com.bunnidogs.mod.entity;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.*;
import com.bunnidogs.mod.menu.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/** Actual role factories, attacks and focused ritual costs over a correctly bounded native arena. */
final class ApexTypesGameTest {
    private static Vec3 center(GameTestHelper h) { return Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(32,180,32))); }
    private static void floor(GameTestHelper h) {
        for (int x=8;x<57;x++) for (int z=8;z<57;z++) h.setBlock(x,179,z,Blocks.STONE);
    }
    private static ServerPlayer player(GameTestHelper h,Vec3 at) {
        var p=(ServerPlayer)h.makeMockServerPlayer(GameType.SURVIVAL);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        p.connection.markClientLoaded();GameType.SURVIVAL.updatePlayerAbilities(p.getAbilities());
        CareMenuGameTest.prepare(p);p.snapTo(at);return p;
    }
    private static Apex apex(GameTestHelper h,ApexKind kind,boolean flying,Vec3 at) {
        var a=ModEntities.apex(kind,flying).create(h.getLevel(),EntitySpawnReason.COMMAND);a.snapTo(at);a.setNoAi(true);h.getLevel().addFreshEntity(a);return a;
    }
    static void variants(GameTestHelper h) {
        floor(h);var pos=center(h);var source=new Tissue(Genome.DEFAULT,Stats.of(12,15,30,24,11));
        for (var kind:ApexKind.values()) for (boolean flying:new boolean[]{false,true}) {
            var a=apex(h,kind,flying,pos);a.setGenome(source.genome());a.setStats(source.stats());a.setHealth(a.getMaxHealth()*.4F);a.tick();a.beginAbility();
            h.assertTrue(a.kind()==kind && a.flying()==flying && a.acceptsCare()==false && a.isPersistenceRequired(),"Every distinct registration has its intended hostile role/movement and persistent ritual behavior");
            h.assertTrue(a.getAttributeValue(Attributes.ATTACK_DAMAGE)==ApexRules.combat(source.genome(),source.stats(),true).damage()
                    && a.getMaxHealth()==(float)ApexRules.combat(source.genome(),source.stats(),true).health(),"All roles use the +60% damage forecast without extra health");
            float fraction=a.getHealth()/a.getMaxHealth();
            var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());a.saveWithoutId(out);
            var b=ModEntities.apex(kind,flying).create(h.getLevel(),EntitySpawnReason.LOAD);b.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
            h.assertTrue(b.kind()==kind && b.flying()==flying && b.genome().equals(source.genome()) && b.stats().equals(source.stats())
                    && b.abilityWait()==kind.recovery && Math.abs(b.getHealth()/b.getMaxHealth()-fraction)<.001,"Native save: "+kind+"/"+flying+" reloaded="+b.kind()+"/"+b.flying()+" stats="+b.stats()+" genome="+b.genome()+" wait="+b.abilityWait()+" health="+b.getHealth()+"/"+b.getMaxHealth());
            a.discard();
        }
        h.assertTrue(ApexRules.combat(Genome.DEFAULT,Stats.of(100000,0,100000,0,0),true).damage()==24,"The increased damage is applied after the old cap, not capped back to fifteen");
        var flyer=apex(h,ApexKind.PRISM,true,pos);var target=player(h,pos.add(8,0,0));flyer.setTarget(target);
        var goal=new ApexFlightGoal(flyer);h.assertTrue(goal.canUse(),"Flying roles can begin real movement");goal.tick();
        h.assertTrue(flyer.getDeltaMovement().y>0,"The flight controller produces upward movement toward an accessible altitude");flyer.landFor(60);
        h.assertTrue(!goal.canUse() && !flyer.isNoGravity(),"Flight has a real vulnerable landing interval");flyer.discard();
    }
    static void attacks(GameTestHelper h) {
        floor(h);var level=h.getLevel();var pos=center(h);var a=apex(h,ApexKind.PRISM,false,pos);var p=player(h,pos.add(8,0,0));a.setTarget(p);
        for (int y=180;y<185;y++) for (int z=30;z<35;z++) h.setBlock(36,y,z,Blocks.STONE);
        float hp=p.getHealth();a.skills().trace(level,new Vec3(1,0,0),true);
        h.assertTrue(p.getHealth()==hp,"Solid cover stops the real damaging beam");
        for (int y=180;y<185;y++) for (int z=30;z<35;z++) h.setBlock(36,y,z,Blocks.AIR);
        var beam=new ApexSpecialGoal(a);beam.start();for (int t=0;t<29;t++) beam.tick();
        h.assertTrue(p.getHealth()==hp,"The mouth beam gives warning before its first pulse");beam.tick();beam.stop();
        h.assertTrue(p.getHealth()<hp,"The warned beam damages an unobstructed actual player");a.discard();
        var caller=apex(h,ApexKind.BROOD,false,pos);caller.setTarget(p);caller.skills().summon(level);caller.skills().summon(level);caller.skills().summon(level);
        var brood=level.getEntitiesOfClass(ApexMinion.class,caller.getBoundingBox().inflate(40),m->m.belongsTo(caller));
        h.assertTrue(brood.size()==6 && brood.stream().allMatch(m->m.genome().equals(caller.genome()) && !m.acceptsCare()),"Repeated calls stop at six actual hostile source-bearing minions");
        var minion=brood.getFirst();minion.hurtServer(level,p.damageSources().playerAttack(p),1000);
        h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,minion.getBoundingBox().inflate(2)).isEmpty(),"A credited minion kill produces no ordinary or Apex materials");
        caller.discard();for (var m:brood) if (m.isAlive()) {m.tickCount=10;m.tick();}
        h.assertTrue(brood.stream().noneMatch(m->m.isAlive() && !m.isRemoved()),"The temporary pack ends when its caller disappears");
    }
    static void blast(GameTestHelper h) {
        floor(h);var level=h.getLevel();var a=apex(h,ApexKind.BLAST,false,center(h));
        var ally=apex(h,ApexKind.SUPPORT,true,center(h).add(3,0,0));var p=player(h,center(h).add(9,0,0));
        p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(512);p.setHealth(512);float before=a.getHealth(),allyHp=ally.getHealth();
        Consumer<net.neoforged.neoforge.event.level.ExplosionEvent.Start> cancel=e->{if(e.getLevel()==level)e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(cancel);
        try {h.assertTrue(!a.skills().blast(level) && a.getHealth()==before,"A cancelled native explosion has no backlash cost");} finally {NeoForge.EVENT_BUS.unregister(cancel);}
        h.assertTrue(a.skills().blast(level),"The fully loaded arena permits an actual native power-ten explosion");
        h.assertTrue(a.getHealth()<before && a.isAlive() && p.getHealth()<512,"A real blast hurts victims and charges a surviving caster");
        h.assertTrue(ally.getHealth()==allyHp && ally.getDeltaMovement().lengthSqr()==0,"A different flying Apex is protected from both blast damage and knockback");
        h.assertTrue(level.getBlockState(h.absolutePos(new BlockPos(32,179,32))).is(Blocks.STONE),"Ritual blasts preserve player terrain");
        a.invulnerableTime=0;h.assertTrue(a.skills().blast(level) && a.isAlive(),"The caster can detonate again without creeper death");
        a.setHealth(a.getMaxHealth()*.1F);h.assertTrue(!a.skills().blast(level),"A near-dead caster cannot loop free detonations");a.discard();ally.discard();
    }
    static void support(GameTestHelper h) {
        floor(h);var level=h.getLevel();var a=apex(h,ApexKind.SUPPORT,false,center(h));var p=player(h,center(h).add(8,0,0));
        a.setTarget(p);a.skills().restore(0);p.getAttribute(Attributes.ARMOR).setBaseValue(20);p.invulnerableTime=0;
        float hp=p.getHealth(), paid=a.skills().drain(level,p);
        h.assertTrue(Math.abs(paid-2.4)<.001 && Math.abs(hp-p.getHealth()-paid)<.001 && Math.abs(a.skills().vitality()-paid)<.001,"Armor-bypassing drain earns only actual accepted damage as vitality");
        Consumer<net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent> cancel=e->{if(e.getEntity()==p)e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(cancel);float stored=a.skills().vitality();
        try {p.invulnerableTime=0;h.assertTrue(a.skills().drain(level,p)==0 && a.skills().vitality()==stored,"Cancelled damage cannot fund healing");} finally {NeoForge.EVENT_BUS.unregister(cancel);}
        var ally=apex(h,ApexKind.BLAST,false,center(h).add(-4,0,0));ally.setHealth(ally.getMaxHealth()-10);float allyHp=ally.getHealth();a.tickCount=10;a.skills().tick(level);
        h.assertTrue(ally.getHealth()==allyHp+1 && Math.abs(a.skills().vitality()-(stored-1))<.001,"Healing a real ally spends the corresponding finite vitality");
        a.skills().hook(level,p);var hooks=level.getEntitiesOfClass(ApexHook.class,a.getBoundingBox().inflate(2));
        h.assertTrue(hooks.size()==1,"The hook is a registered colliding projectile");
        for(int t=0;t<14 && hooks.getFirst().isAlive();t++)hooks.getFirst().tick();
        h.assertTrue(p.hasEffect(ModEffects.STAGGER),"An unobstructed physical hook applies its short movement stun");
        var second=apex(h,ApexKind.SUPPORT,false,center(h).add(0,0,3));
        h.assertTrue(!second.skills().hookHit(p),"A second support cannot chain-stun the same victim during shared recovery");
        p.removeEffect(ModEffects.STAGGER);h.assertTrue(p.getAttributeValue(Attributes.JUMP_STRENGTH)>0,"The timed effect restores jump capacity when removed");a.discard();ally.discard();second.discard();
    }
    static void focus(GameTestHelper h) {
        floor(h);var level=h.getLevel();var original=level.getDifficulty();level.getServer().setDifficulty(Difficulty.NORMAL,true);
        try {
            var pos=BlockPos.containing(center(h));level.setBlock(pos,ModBlocks.APEX_CRUCIBLE.get().defaultBlockState(),3);
            var p=player(h,center(h).add(0,0,3));var sample=TissueSampleItem.create(Genome.DEFAULT,Stats.of(1,2,3,4,5));sample.setCount(2);
            var sigil=new ItemStack(ModItems.APEX_SIGILS.get("flying_prismmaw_sigil").get(),2);
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sigil);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,sample);
            for(int i=0;i<5;i++){var at=pos.offset(ApexCrucibleBlock.RING.get(i));level.setBlock(at,ModBlocks.TROPHY_PEDESTAL.get().defaultBlockState(),3);((TrophyPedestalBlockEntity)level.getBlockEntity(at)).setItem(new ItemStack(ApexRitual.legends().get(i)));}
            p.gameMode.useItemOn(p,level,sigil,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(pos),net.minecraft.core.Direction.UP,pos,false));
            var menu=(CareMenu)p.containerMenu;h.assertTrue(sigil.getCount()==2 && sample.getCount()==2,"Native sigil use opens its chosen preview without paying");
            CohesionGameTest.preview("Focused flying Apex",menu.view());
            h.assertTrue(menu.clickMenuButton(p,menu.buttonId(ApexPanel.PREPARE)),"Chosen focus can be reviewed");
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(!menu.stillValid(p) && !menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON)) && sample.getCount()==2,"Losing the selected sigil invalidates its menu instead of silently falling back to the Brute");
            p.closeContainer();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sigil);
            CareMenu.open(p,pos,sample);menu=(CareMenu)p.containerMenu;menu.clickMenuButton(p,menu.buttonId(ApexPanel.PREPARE));
            Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> replace=e->{if(e.getEntity() instanceof Apex)p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.PAPER));};
            NeoForge.EVENT_BUS.addListener(replace);try{menu.clickMenuButton(p,menu.buttonId(ApexPanel.SUMMON));}finally{NeoForge.EVENT_BUS.unregister(replace);}
            h.assertTrue(sample.getCount()==2 && sigil.getCount()==2 && level.getEntitiesOfClass(Apex.class,new net.minecraft.world.phys.AABB(pos).inflate(5)).isEmpty(),"Spawn callbacks replacing the exact focus leave both costs and remove the unpaid boss");
            p.closeContainer();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sigil);
            h.assertTrue(ApexRitual.summon(p,pos,sample,sigil)==ApexRitual.Status.SUMMONED,"A valid focused ritual commits");
            var boss=level.getEntitiesOfClass(Apex.class,new net.minecraft.world.phys.AABB(pos).inflate(5)).getFirst();
            h.assertTrue(boss.kind()==ApexKind.PRISM && boss.flying() && sigil.getCount()==1 && sample.getCount()==1 && boss.stats().equals(Stats.of(1,2,3,4,5)),"Chosen flight/role/source survive the paid spawn with exactly one sample and sigil consumed");boss.discard();
        } finally {level.getServer().setDifficulty(original,true);}
    }
}
