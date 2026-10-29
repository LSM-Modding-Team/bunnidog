package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.*;
import com.bunnidogs.mod.equipment.Barding;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.realm.*;
import com.bunnidogs.mod.registry.*;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Native evidence for the night/death corrections and the numeric progression audit. */
final class ProgressionScaleGameTest {
    private static void time(ServerLevel level,long ticks) {
        level.clockManager().setTotalTicks(level.dimensionType().defaultClock().orElseThrow(),ticks);
        level.environmentAttributes().invalidateTickCache();level.updateSkyBrightness();
    }
    static void night(GameTestHelper h) throws Exception {
        try(var fixture=new RealmFixture(h.getLevel(),"night-scale")) {
            var realm=fixture.realm;var clock=realm.dimensionType().defaultClock().orElseThrow();long old=realm.clockManager().getTotalTicks(clock);
            try {
                var pos=new BlockPos(0,80,0);var biome=realm.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crownroot_groves")));
                var base=biome.value().getMobSettings().getMobs(MobCategory.MONSTER);
                time(realm,1000);var day=new LevelEvent.PotentialSpawns(realm,MobCategory.MONSTER,pos,base);NeoForge.EVENT_BUS.post(day);
                h.assertTrue(!RealmNight.active(realm) && day.getSpawnerDataList().equals(base.unwrap()),"Daytime keeps the native biome spawn composition");
                time(realm,18000);h.assertTrue(RealmNight.active(realm),"The real dimension's clock/sky attributes identify night");
                var night=new LevelEvent.PotentialSpawns(realm,MobCategory.MONSTER,pos,base);NeoForge.EVENT_BUS.post(night);
                int airDay=0,airNight=0;
                for(var entry:base.unwrap())if(ModEntities.FLYING_APEX.stream().anyMatch(t->t.get()==entry.value().type()))airDay+=entry.weight();
                for(var entry:night.getSpawnerDataList())if(ModEntities.FLYING_APEX.stream().anyMatch(t->t.get()==entry.value().type())) {airNight+=entry.weight();h.assertTrue(entry.value().maxCount()>=2,"Night flyers form actual packs");}
                h.assertTrue(airNight==airDay*4,"Native nighttime spawn selection quadruples aerial weights without adding a second spawn loop");
                int[] tiers=new int[5];int bare=0;
                for(int i=0;i<160;i++) {
                    var a=ModEntities.APEX.get().create(realm,EntitySpawnReason.NATURAL);a.getRandom().setSeed(i*739L+42);
                    a.finalizeSpawn(realm,realm.getCurrentDifficultyAt(pos),EntitySpawnReason.NATURAL,null);
                    int tier=Barding.tier(a);if(tier<0)bare++;else tiers[tier]++;
                    h.assertTrue(a.wild() && a.checkSpawnRules(realm,EntitySpawnReason.NATURAL) && !a.isPersistenceRequired(),"Night Apexes retain real hostile spawn eligibility and wild despawning");a.discard();
                }
                h.assertTrue(bare>60 && bare<135 && tiers[1]>0 && tiers[2]>0 && tiers[3]>0 && tiers[4]==0,"Night naturally mixes bare, iron, diamond and Netherite actors, never Astralite");
            }finally {time(realm,old);}
        }h.succeed();
    }
    static void deaths(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var seal=CrownDungeonGameTest.seal(h,0,c.above(),c,UUID.randomUUID());
        var p=ApexTypesGameTest.player(h,net.minecraft.world.phys.Vec3.atBottomCenterOf(c.offset(2,1,0)));
        h.assertTrue(seal.begin(p,InteractionHand.MAIN_HAND),"A real dungeon encounter starts");
        var actors=seal.enemies().stream().map(id->(Apex)h.getLevel().getEntity(id)).toList();actors.forEach(a->a.setNoAi(true));
        var drops=new ArrayList<ItemEntity>();java.util.function.Consumer<net.neoforged.neoforge.event.entity.living.LivingDropsEvent> capture=e->{if(actors.contains(e.getEntity()))drops.addAll(e.getDrops());};
        NeoForge.EVENT_BUS.addListener(capture);try {for(var a:actors)CrownDungeonGameTest.kill(h,a,p);}finally {NeoForge.EVENT_BUS.unregister(capture);}
        h.assertTrue(drops.stream().filter(e->e.getItem().is(ModItems.APEX_MARROW.get())).mapToInt(e->e.getItem().getCount()).sum()>=8,"Actual dungeon kills pay ordinary native role materials and Marrow");
        h.assertTrue(actors.stream().allMatch(a->!a.isRemoved() && a.isDeadOrDying() && a.getBaseExperienceReward(h.getLevel())==500),"Killed wave actors retain their bodies and ordinary 500 XP");
        var minion=ModEntities.APEX_MINION.get().create(h.getLevel(),EntitySpawnReason.MOB_SUMMONED);
        minion.caller(actors.getFirst());minion.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(c.offset(8,1,8)));CrownEncounter.bind(minion,seal);h.getLevel().addFreshEntity(minion);CrownDungeonGameTest.kill(h,minion,p);
        // Explicit reset must preserve the bodies too, rather than erasing the last kill of a patrol/wave.
        seal.reset();CrownedApex.cleanup(h.getLevel(),seal.run(),net.minecraft.world.phys.Vec3.atCenterOf(c));
        h.assertTrue(actors.stream().noneMatch(Entity::isRemoved),"Encounter cleanup cannot remove confirmed dying actors");
        h.runAfterDelay(12,()->{
            h.assertTrue(actors.stream().allMatch(a->!a.isRemoved() && a.deathTime>0) && !minion.isRemoved() && minion.deathTime>0,"Native death animations continue even past the dead caller's ten-tick expiry check");
            h.runAfterDelay(14,()->{h.assertTrue(actors.stream().allMatch(Entity::isRemoved) && minion.isRemoved(),"Native death ticking eventually removes the bodies");p.discard();h.succeed();});
        });
    }
    static void audit(GameTestHelper h) {
        var out=new JsonObject();var effects=new JsonArray();var def=Juice.DEFS.get("temperament:loyal").getFirst();
        for(double quality:new double[]{0,1,2,4,7,12,20,24,28,60,116,124,252}) {
            var spec=Juice.spec(def,quality,Stats.of(80,70,75,80,60));var row=new JsonObject();row.addProperty("quality",quality);row.addProperty("strength_level",Juice.amplifierFor(quality)+1);row.addProperty("regen_level",spec.amplifier()+1);row.addProperty("duration_ticks",spec.ticks());
            var patient=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);patient.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);patient.setHealth(1);
            var nativeEffect=MobEffects.REGENERATION.value();for(int t=0;t<500;t++)if(nativeEffect.shouldApplyEffectTickThisTick(t,spec.amplifier()))nativeEffect.applyEffectTick(h.getLevel(),patient,spec.amplifier());
            row.addProperty("native_healing_hp_per_second",(patient.getHealth()-1)/25.0);effects.add(row);patient.discard();
        }
        h.assertTrue(Juice.regenerationAmplifier(20)==2 && Juice.amplifierFor(116)==29 && Juice.regenerationAmplifier(252)==6,"Healing gets exponential quality thresholds without capping Strength or Regeneration levels");out.add("effects",effects);
        var fighters=new JsonArray();
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true})for(int tier:new int[]{-1,1,2,3,4}) {
            var a=ModEntities.apex(kind,flying).create(h.getLevel(),EntitySpawnReason.COMMAND);
            a.setGenome(flying ? Wings.winged(Genome.DEFAULT) : Genome.DEFAULT);a.setStats(Stats.of(40,40,40,40,20));
            if(tier>=0){a.setItemSlot(EquipmentSlot.BODY,new net.minecraft.world.item.ItemStack(ModItems.BARDING.get(Barding.NAMES[tier]).get()));Barding.refresh(a);}
            var row=new JsonObject();row.addProperty("role",kind.id);row.addProperty("flying",flying);row.addProperty("barding",tier<0 ? "none" : Barding.NAMES[tier]);row.addProperty("health",a.getMaxHealth());row.addProperty("attack",a.getAttributeValue(Attributes.ATTACK_DAMAGE));row.addProperty("armor",a.getArmorValue());row.addProperty("toughness",a.getAttributeValue(Attributes.ARMOR_TOUGHNESS));fighters.add(row);a.discard();
        }out.add("fighters",fighters);
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("PROGRESSION_SCALE_AUDIT {}",out);h.succeed();
    }
}
