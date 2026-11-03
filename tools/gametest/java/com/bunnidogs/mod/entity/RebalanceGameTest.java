package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.command.KitCommands;
import com.bunnidogs.mod.equipment.Barding;
import com.bunnidogs.mod.expedition.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.JuiceItem;
import com.bunnidogs.mod.registry.*;
import com.google.gson.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;

/** Controlled native evidence, explicitly distinct from human Survival balance acceptance. */
final class RebalanceGameTest {
    private static Juice.Spec effect(Infusion bottle,String id) {return bottle.specs().stream().filter(s->s.id().equals("minecraft:"+id)).findFirst().orElseThrow();}
    static void supply(GameTestHelper h) {
        var out=new JsonObject();out.addProperty("human_acceptance","pending");var donors=new JsonArray();
        for(var line:List.of(ExpeditionInfusions.LATE_STRENGTH,ExpeditionInfusions.ENDGAME_STRENGTH,ExpeditionInfusions.LATE_RECOVERY,ExpeditionInfusions.ENDGAME_RECOVERY)) {
            var bottle=line.bottle();var row=new JsonObject();row.addProperty("genome",line.genome().toString());row.addProperty("parents",line.parents().toString());row.addProperty("frozen_bottle",bottle.toString());
            row.addProperty("rich_generations",line.richGenerations());row.addProperty("plain_generations",line.plainGenerations());row.addProperty("plain_seed_quality",line.seed().quality());
            var specs=new JsonArray();for(var spec:bottle.specs()) {var e=new JsonObject();e.addProperty("effect",spec.id());e.addProperty("level",spec.amplifier()+1);e.addProperty("seconds",spec.ticks()/20.0);specs.add(e);}row.add("effects",specs);donors.add(row);
        }out.add("conditional_donors",donors);
        var sustain=new JsonArray();
        for(boolean golden:new boolean[]{false,true}) {
            var strength=ExpeditionInfusions.ENDGAME_STRENGTH.bottle();var recovery=ExpeditionInfusions.ENDGAME_RECOVERY.bottle();
            if(!golden) {strength=new Infusion(strength.genome(),strength.stats(),0);recovery=new Infusion(recovery.genome(),recovery.stats(),0);}
            double combatSeconds=effect(strength,"strength").ticks()/20.0,recoverySeconds=effect(recovery,"regeneration").ticks()/20.0;
            double combatPerHour=3600/combatSeconds,recoveryPerHour=3600/recoverySeconds;
            double combatCapacity=72000.0/Breeding.cooldownTicks(strength.genome().traits().fertility());
            double recoveryCapacity=72000.0/Breeding.cooldownTicks(recovery.genome().traits().fertility());
            var row=new JsonObject();row.addProperty("carrier",golden ? "golden" : "plain");row.addProperty("strength_seconds",combatSeconds);row.addProperty("recovery_seconds",recoverySeconds);
            row.addProperty("strength_drinks_per_hour_at_full_uptime",combatPerHour);row.addProperty("recovery_drinks_per_hour_at_full_uptime",recoveryPerHour);
            row.addProperty("strength_pair_capacity_per_loaded_hour",combatCapacity);row.addProperty("recovery_pair_capacity_per_loaded_hour",recoveryCapacity);
            row.addProperty("ideal_strength_pairs",(int)Math.ceil(combatPerHour/combatCapacity));row.addProperty("ideal_recovery_pairs",(int)Math.ceil(recoveryPerHour/recoveryCapacity));
            double cycle=Math.min(combatSeconds,recoverySeconds);
            row.addProperty("alternating_cycle_seconds",cycle);row.addProperty("alternating_strength_drinks_per_hour",3600/cycle);row.addProperty("alternating_recovery_drinks_per_hour",3600/cycle);
            row.addProperty("alternating_strength_pairs",(int)Math.ceil(3600/cycle/combatCapacity));row.addProperty("alternating_recovery_pairs",(int)Math.ceil(3600/cycle/recoveryCapacity));
            row.addProperty("endgame_stock_cycles",48);row.addProperty("endgame_ideal_overlap_minutes",48*cycle/60);sustain.add(row);
        }out.add("conditional_continuous_uptime",sustain);
        var healing=new JsonArray();
        for(int level=1;level<=6;level++) {
            var patient=ModEntities.BUNNIDOG.get().create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);patient.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);patient.setHealth(1);
            var instance=new MobEffectInstance(MobEffects.REGENERATION,600,level-1);
            for(int tick=0;tick<600;tick++)instance.tickServer(h.getLevel(),patient,()->{});
            var row=new JsonObject();row.addProperty("level",level);row.addProperty("native_hp_in_30_seconds",patient.getHealth()-1);row.addProperty("native_hp_per_second",(patient.getHealth()-1)/30.0);healing.add(row);patient.discard();
        }out.add("native_regeneration",healing);
        var weapons=new JsonArray();for(var weapon:LanceProfile.values()) {
            var row=new JsonObject();row.addProperty("weapon",weapon.name());row.addProperty("damage_per_cell",weapon.damage(1));row.addProperty("charge_ticks",weapon.chargeTicks());row.addProperty("recovery_ticks",weapon.recoveryTicks());row.addProperty("range",weapon.range());
            row.addProperty("mode_one_raw_damage_per_second",weapon.damage(1)*20.0/weapon.cycleTicks());weapons.add(row);
        }out.add("lances",weapons);
        h.assertTrue(effect(ExpeditionInfusions.ENDGAME_RECOVERY.bottle(),"regeneration").amplifier()==4 && effect(ExpeditionInfusions.LATE_RECOVERY.bottle(),"regeneration").amplifier()==2,"Actual specialised references retain distinct III/V recovery and honest plain seeds");
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("REBALANCE_SUPPLY_AUDIT {}",out);h.succeed();
    }
    private static ItemStack flask(ServerPlayer p) {
        for(int slot=0;slot<9;slot++)if(p.getInventory().getItem(slot).is(ModItems.ABYSSAL_FLASK.get())) {p.getInventory().setSelectedSlot(slot);return p.getMainHandItem();}
        throw new IllegalStateException("The native endgame kit has no accessible flask");
    }
    private static DamageSource beam(Apex caster) {return new DamageSource(caster.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","apex_beam"))),caster);}
    private static final class Probe {
        ServerPlayer player;Apex first,second,prism,support;ItemStack flask;Vec3 at;int level,stop=-1,totemsBefore;float minimum=Float.MAX_VALUE;double acceptedHealing,healthDamage;int hits;
    }
    static void pressure(GameTestHelper h) {
        var world=h.getLevel();ApexTypesGameTest.floor(h);var center=ApexTypesGameTest.center(h);var probes=new ArrayList<Probe>();
        for(int index=0;index<4;index++) {
            var probe=new Probe();probe.level=index+3;probe.at=center.add(index%2==0 ? -12 : 12,0,index<2 ? -12 : 12);probe.player=ApexTypesGameTest.player(h,probe.at);KitCommands.apply(probe.player,"bunnidog","endgame");probe.flask=flask(probe.player);
            probe.totemsBefore=probe.player.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
            probe.first=ApexTypesGameTest.apex(h,ApexKind.BRUTE,false,probe.at.add(2,0,0));probe.second=ApexTypesGameTest.apex(h,ApexKind.BRUTE,false,probe.at.add(-2,0,0));
            probe.prism=ApexTypesGameTest.apex(h,ApexKind.PRISM,false,probe.at.add(0,0,-6));probe.support=ApexTypesGameTest.apex(h,ApexKind.SUPPORT,false,probe.at.add(0,0,2));
            for(var actor:List.of(probe.first,probe.second,probe.prism,probe.support)) {actor.setGenome(Genome.DEFAULT);actor.setStats(Stats.of(40,40,40,40,20));Barding.astralite(actor);actor.setHealth(actor.getMaxHealth());}
            probes.add(probe);
        }
        Consumer<LivingHealEvent> healed=e->{for(var probe:probes)if(e.getEntity()==probe.player && e.getAmount()>0)probe.acceptedHealing+=Math.min(e.getAmount(),Math.max(0,probe.player.getMaxHealth()-probe.player.getHealth()));};
        Consumer<LivingDamageEvent.Post> hurt=e->{for(var probe:probes)if(e.getEntity()==probe.player) {probe.healthDamage+=e.getHealthDamage();probe.hits++;}};
        NeoForge.EVENT_BUS.addListener(healed);NeoForge.EVENT_BUS.addListener(hurt);
        boolean[] active={false},clean={false};long[] began={0};
        Runnable cleanup=()->{if(clean[0])return;clean[0]=true;NeoForge.EVENT_BUS.unregister(healed);NeoForge.EVENT_BUS.unregister(hurt);for(var probe:probes) {LifebinderMarks.get(world).clear(probe.player);ApexDaze.clear(probe.player);probe.player.discard();for(var actor:List.of(probe.first,probe.second,probe.prism,probe.support))actor.discard();}};
        // Native player ticks perform both complete 32-tick drinks; the probe never gives itself a free refill.
        h.runAfterDelay(2,()->{try {for(var probe:probes)ModItems.ABYSSAL_FLASK.get().use(world,probe.player,InteractionHand.MAIN_HAND);}catch(Throwable e){cleanup.run();throw e;}});
        h.runAfterDelay(20,()->{try {for(var probe:probes)h.assertTrue(FlaskStorage.get(world).size(probe.flask.get(ModItems.FLASK_ID.get()))==96 && probe.player.isUsingItem(),"Half a real drink cannot spend a flask entry or skip its remaining native use ticks");}catch(Throwable e){cleanup.run();throw e;}});
        h.runAfterDelay(36,()->{try {for(var probe:probes) {h.assertTrue(FlaskStorage.get(world).size(probe.flask.get(ModItems.FLASK_ID.get()))==95,"A real complete first flask drink must spend its original entry");ModItems.ABYSSAL_FLASK.get().use(world,probe.player,InteractionHand.MAIN_HAND);}}catch(Throwable e){cleanup.run();throw e;}});
        h.runAfterDelay(70,()->{try {for(var probe:probes) {
            var p=probe.player;h.assertTrue(FlaskStorage.get(world).size(probe.flask.get(ModItems.FLASK_ID.get()))==94 && p.getEffect(MobEffects.STRENGTH).getAmplifier()==29 && p.getEffect(MobEffects.REGENERATION).getAmplifier()==4 && p.getArmorValue()==20,"The actual kit, equipment and two paid native drinks must establish the same initial loadout");
            h.assertTrue(JuiceItem.read(FlaskStorage.get(world).top(probe.flask.get(ModItems.FLASK_ID.get()))).orElseThrow().specs().stream().anyMatch(s->s.id().equals("minecraft:strength")),"The next original entry alternates back to Strength instead of wasting another recovery drink");
            int duration=p.getEffect(MobEffects.REGENERATION).getDuration();p.removeEffect(MobEffects.REGENERATION);p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,duration,probe.level-1));p.getInventory().setSelectedSlot(0);
        }began[0]=world.getGameTime();active[0]=true;}catch(Throwable e){cleanup.run();throw e;}});
        h.onEachTick(()->{if(clean[0])return;
            try {
                // Embedded mock connections do not drive ServerPlayer.doTick; run its actual native body/food/effects once per test-world tick.
                for(var probe:probes)if(probe.player.isAlive())probe.player.doTick();
                if(!active[0])return;
                int tick=(int)(world.getGameTime()-began[0]);
                if(tick<600) {
                    for(var probe:probes) {
                        var p=probe.player;if(!p.isAlive()) {if(probe.stop<0)probe.stop=tick;continue;}p.snapTo(probe.at);
                        if(tick%20==0)probe.first.doHurtTarget(world,p);if(tick%20==10)probe.second.doHurtTarget(world,p);
                        if(tick%200==80)p.hurtServer(world,beam(probe.prism),probe.prism.skills().beamDamage());
                        if(tick%240==0)probe.support.skills().hookHit(p);
                        probe.minimum=Math.min(probe.minimum,p.getHealth());
                    }return;
                }
                var out=new JsonObject();out.addProperty("human_acceptance","pending");out.addProperty("window_ticks",600);out.addProperty("actual_world_ticks",true);out.addProperty("native_mock_player_body_tick_driver",true);out.addProperty("position_pinned",true);out.addProperty("scheduled_attacks_not_autonomous_combat",true);out.addProperty("player_attacks_apples_and_emergency_actions_scripted",false);
                out.addProperty("loadout","actual endgame kit, two original flask drinks, only Regeneration amplifier overridden for comparison");out.addProperty("natural_regeneration",world.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.NATURAL_HEALTH_REGENERATION));
                var rows=new JsonArray();for(var probe:probes) {
                    var p=probe.player;var row=new JsonObject();row.addProperty("regeneration_level",probe.level);row.addProperty("alive",p.isAlive());row.addProperty("minimum_health",probe.minimum);row.addProperty("health_left",p.getHealth());row.addProperty("max_health_left",p.getMaxHealth());row.addProperty("absorption_left",p.getAbsorptionAmount());row.addProperty("native_accepted_healing_hp",probe.acceptedHealing);row.addProperty("native_health_damage_hp",probe.healthDamage);row.addProperty("native_damage_events",probe.hits);
                    row.addProperty("normal_totems_used",p.getStats().getValue(net.minecraft.stats.Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING))-probe.totemsBefore);row.addProperty("death_tick",probe.stop);row.addProperty("flask_drinks_left",FlaskStorage.get(world).size(probe.flask.get(ModItems.FLASK_ID.get())));row.addProperty("heart_debt_left",LifebinderMarks.get(world).lost(p.getUUID()));
                    row.addProperty("brute_attack",probe.first.getAttributeValue(Attributes.ATTACK_DAMAGE));row.addProperty("prism_beam_raw",probe.prism.skills().beamDamage());h.assertTrue(probe.hits>0,"Each native pressure profile must receive real accepted hits");rows.add(row);
                }out.add("profiles",rows);com.bunnidogs.mod.BunnidogsMod.LOGGER.info("REBALANCE_PRESSURE_AUDIT {}",out);cleanup.run();h.succeed();
            } catch(Throwable e) {cleanup.run();throw e;}
        });
        h.runAfterDelay(900,cleanup);
    }
    static void defence(GameTestHelper h) {
        var world=h.getLevel();ApexTypesGameTest.floor(h);var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));KitCommands.apply(p,"bunnidog","endgame");p.tick();
        var caster=ApexTypesGameTest.apex(h,ApexKind.PRISM,false,p.position().add(6,0,0));caster.setGenome(Genome.DEFAULT);caster.setStats(Stats.of(40,40,40,40,20));
        JuiceItem.create(ExpeditionInfusions.ENDGAME_STRENGTH.bottle()).finishUsingItem(world,p);
        h.assertTrue(p.getAbsorptionAmount()==32,"The actual combat bottle starts with its native thirty-two-HP buffer");
        var source=beam(caster);h.assertTrue(source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.BYPASSES_RESISTANCE) && !source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS),"Native beam tags bypass armour separately from Resistance and enchantments");
        for(int hit=0;hit<6;hit++) {p.invulnerableTime=0;p.hurtServer(world,source,40);}
        float damaged=p.getHealth();h.assertTrue(p.getAbsorptionAmount()==0 && damaged>0 && damaged<p.getMaxHealth(),"Six separate real beam hits can spend the buffer and reach native health");
        JuiceItem.create(ExpeditionInfusions.ENDGAME_RECOVERY.bottle()).finishUsingItem(world,p);var regen=p.getEffect(MobEffects.REGENERATION);
        for(int tick=0;tick<300;tick++)regen.tickServer(world,p,()->{});
        h.assertTrue(p.getHealth()>damaged && p.getAbsorptionAmount()==0,"Native regeneration heals missing health without regenerating the spent buffer");
        JuiceItem.create(ExpeditionInfusions.ENDGAME_STRENGTH.bottle()).finishUsingItem(world,p);
        h.assertTrue(p.getAbsorptionAmount()==32,"A newly paid combat bottle follows native Absorption refresh semantics");
        var standard=new ItemStack(ModItems.CONCORD_STANDARD.get());standard.set(ModItems.TOOL_POWER.get(),5);p.setItemInHand(InteractionHand.MAIN_HAND,standard);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.HEART_SPOOL.get(),5));
        ModItems.CONCORD_STANDARD.get().use(world,p,InteractionHand.MAIN_HAND);standard.finishUsingItem(world,p);p.stopUsingItem();var resistance=p.getEffect(MobEffects.RESISTANCE);
        h.assertTrue(resistance.getAmplifier()==4 && resistance.getDuration()==80 && p.getOffhandItem().isEmpty(),"The separately paid Standard supplies only its real four-second Resistance V window");
        for(int tick=0;tick<80;tick++)resistance.tickServer(world,p,()->{});
        h.assertTrue(p.getEffect(MobEffects.RESISTANCE).getAmplifier()==2,"Native hidden effects restore the already-paid juice Resistance III afterward");
        var out=new JsonObject();out.addProperty("human_acceptance","pending");out.addProperty("absorption_refill_per_paid_combat_bottle_hp",32);out.addProperty("regeneration_refills_absorption",false);out.addProperty("standard_power_five_spools",5);out.addProperty("standard_resistance_v_ticks",80);out.addProperty("juice_resistance_returns_after_standard",true);
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("REBALANCE_DEFENCE_AUDIT {}",out);LifebinderMarks.get(world).clear(p);caster.discard();p.discard();h.succeed();
    }
}
