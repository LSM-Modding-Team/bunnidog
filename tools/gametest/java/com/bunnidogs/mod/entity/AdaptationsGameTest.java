package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Adaptations;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class AdaptationsGameTest {
    private AdaptationsGameTest() {}
    private static Genome with(Gene gene, String name) {
        int allele = gene.alleleNamed(name).id();
        return Genome.DEFAULT.with(gene,0,allele).with(gene,1,allele);
    }
    private static Bunnidog dog(GameTestHelper h, Genome genome, int x) {
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),x,1,3);
        dog.setGenome(genome); dog.setStats(Stats.ZERO); dog.setHealth(dog.getMaxHealth());
        return dog;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var normal = dog(h,Genome.DEFAULT,1);
        var ember = dog(h,with(Gene.COAT,"ember"),2);
        float health = ember.getHealth();
        h.assertTrue(ember.fireImmune() && !ember.hurtServer(level,level.damageSources().lava(),4)
                && ember.getHealth() == health,"Expressed ember must resist actual lava damage");
        h.assertTrue(normal.hurtServer(level,level.damageSources().inFire(),2) && normal.getHealth() < normal.getMaxHealth(),
                "Ordinary coats must retain native fire damage");
        h.assertTrue(!dog(h,with(Gene.COAT,"snow"),3).canFreeze(),"Expressed snow must prevent powder-snow freezing");
        h.assertTrue(!dog(h,with(Gene.BUILD,"woolly"),4).canFreeze(),"Woolly build must provide inherited cold protection");
        h.assertTrue(normal.canFreeze(),"Ordinary dogs must retain freezing");
        int snow = Gene.COAT.alleleNamed("snow").id(), fire = Gene.COAT.alleleNamed("ember").id();
        ember.setGenome(Genome.DEFAULT.with(Gene.COAT,0,snow).with(Gene.COAT,1,fire));
        h.assertTrue(ember.fireImmune() && ember.canFreeze(),"Hidden snow must not protect a dog expressing the dominant ember coat");
        ember.setGenome(Genome.DEFAULT);
        h.assertTrue(!ember.fireImmune(),"Changing the expressed allele must remove its protection");
        var ordinaryFall = dog(h,Genome.DEFAULT,5);
        var hoofedFall = dog(h,with(Gene.LEGS,"hoofed"),6);
        ordinaryFall.causeFallDamage(9,1,level.damageSources().fall());
        hoofedFall.causeFallDamage(9,1,level.damageSources().fall());
        float ordinaryDamage = ordinaryFall.getMaxHealth()-ordinaryFall.getHealth();
        float hoofedDamage = hoofedFall.getMaxHealth()-hoofedFall.getHealth();
        h.assertTrue(ordinaryDamage > 0 && hoofedDamage > 0 && hoofedDamage <= Math.ceil(ordinaryDamage/2),
                "Hoofed legs must reduce real fall damage while preserving native damage calculation");
        h.assertTrue(Plain.effect(Gene.COAT,Gene.COAT.alleleNamed("ember")).contains("fire and lava")
                && Plain.effect(Gene.BUILD,Gene.BUILD.alleleNamed("woolly")).contains("powder snow"),"Gene readouts must explain inherited protection");
        var moss = dog(h,with(Gene.COAT,"moss"),2);
        h.assertTrue(!moss.addEffect(new MobEffectInstance(MobEffects.POISON,100)) && !moss.hasEffect(MobEffects.POISON),
                "Moss must refuse ordinary native poison applications");
        h.assertTrue(moss.addEffect(new MobEffectInstance(MobEffects.WITHER,100)),"Moss must retain other native harmful effects");
        var desert = dog(h,with(Gene.COAT,"sand"),2);
        float desertHealth = desert.getHealth();
        h.assertTrue(!desert.hurtServer(level,level.damageSources().cactus(),3) && desert.getHealth() == desertHealth,
                "Expressed sand must prevent native cactus damage");
        h.assertTrue(desert.hurtServer(level,level.damageSources().generic(),3),"Sand must not provide general damage immunity");
        var end = dog(h,with(Gene.COAT,"void"),2);
        h.assertTrue(!end.addEffect(new MobEffectInstance(MobEffects.LEVITATION,100)),"Void must refuse native levitation applications");
        h.assertTrue(end.addEffect(new MobEffectInstance(MobEffects.POISON,100)),"Void must retain poison vulnerability");
        var unchangedStats = end.stats();
        end.setGenome(with(Gene.COAT,"moss"));
        h.assertTrue(!end.hasEffect(MobEffects.POISON) && end.stats().equals(unchangedStats)
                && end.addEffect(new MobEffectInstance(MobEffects.LEVITATION,100)),
                "Changing the expressed coat must remove its blocked effect and restore the old vulnerability without altering stats");
        int mossId = Gene.COAT.alleleNamed("moss").id(), voidId = Gene.COAT.alleleNamed("void").id();
        end.setGenome(Genome.DEFAULT.with(Gene.COAT,0,mossId).with(Gene.COAT,1,voidId));
        h.assertTrue(!end.hasEffect(MobEffects.LEVITATION) && end.addEffect(new MobEffectInstance(MobEffects.POISON,100)),
                "Hidden moss must not protect an animal expressing void");
        int sandId = Gene.COAT.alleleNamed("sand").id();
        var hiddenSand = dog(h,Genome.DEFAULT.with(Gene.COAT,0,sandId).with(Gene.COAT,1,mossId),2);
        h.assertTrue(hiddenSand.hurtServer(level,level.damageSources().cactus(),3),"Hidden sand must not protect an animal expressing moss");
        h.assertTrue(Plain.effect(Gene.COAT,Gene.COAT.alleleNamed("moss")).contains("poison")
                && Plain.effect(Gene.COAT,Gene.COAT.alleleNamed("sand")).contains("cactus")
                && Plain.effect(Gene.COAT,Gene.COAT.alleleNamed("void")).contains("levitation"),"Readouts must explain each new protection");
        var bare = dog(h,Genome.DEFAULT,3);
        var slate = dog(h,with(Gene.COAT,"slate"),4);
        var attack = level.damageSources().mobAttack(normal);
        bare.hurtServer(level,attack,6); slate.hurtServer(level,attack,6);
        h.assertTrue(slate.getAttributeValue(Attributes.ARMOR) == 4 && slate.getHealth() > bare.getHealth()
                && slate.getHealth() < slate.getMaxHealth(),"Expressed slate must reduce actual ordinary attack damage without immunity");
        int slateId = Gene.COAT.alleleNamed("slate").id();
        slate.setGenome(Genome.DEFAULT.with(Gene.COAT,0,slateId).with(Gene.COAT,1,voidId));
        h.assertTrue(slate.getAttributeValue(Attributes.ARMOR) == 0,"Hidden slate must not retain armor");
        var heavy = dog(h,with(Gene.BUILD,"chonker"),4);
        var stocky = dog(h,with(Gene.BUILD,"stocky"),5);
        h.assertTrue(Math.abs(stocky.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.15) < .001,
                "Stocky builds must have their smaller knockback benefit");
        bare.setDeltaMovement(Vec3.ZERO); heavy.setDeltaMovement(Vec3.ZERO);
        bare.setOnGround(true); heavy.setOnGround(true);
        bare.knockback(1,1,0,attack,1); heavy.knockback(1,1,0,attack,1);
        h.assertTrue(heavy.getDeltaMovement().horizontalDistanceSqr() > 0
                && heavy.getDeltaMovement().horizontalDistanceSqr() < bare.getDeltaMovement().horizontalDistanceSqr(),
                "Chonker builds must reduce actual native knockback without eliminating it");
        heavy.setGenome(with(Gene.COAT,"slate").with(Gene.BUILD,0,3).with(Gene.BUILD,1,3));
        heavy.getAttribute(Attributes.ARMOR).setBaseValue(2);
        for (int i = 0; i < 2; i++) {
            var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            heavy.saveWithoutId(out);
            var reloaded = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);
            reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));
            h.assertTrue(reloaded.getAttributeValue(Attributes.ARMOR) == 6
                    && Math.abs(reloaded.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.3) < .001,
                    "Reload must restore one genetic bonus alongside external armor base values");
            heavy = reloaded;
        }
        heavy.setGenome(Genome.DEFAULT);
        h.assertTrue(heavy.getAttributeValue(Attributes.ARMOR) == 2 && heavy.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0,
                "Changing genes must remove only the genetic modifiers, preserving external armor");
        var apex = ModEntities.APEX.get().create(level,EntitySpawnReason.COMMAND);
        apex.setGenome(with(Gene.COAT,"slate").with(Gene.BUILD,0,3).with(Gene.BUILD,1,3));
        h.assertTrue(apex.getAttributeValue(Attributes.ARMOR) == 8
                && Math.abs(apex.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.8) < .001,
                "Apex copies must combine inherited defenses with their native boss bases");
        int glowId = Gene.COAT.alleleNamed("glow").id();
        h.assertTrue(Adaptations.luminous(with(Gene.COAT,"glow"))
                && !Adaptations.luminous(Genome.DEFAULT.with(Gene.COAT,0,glowId)),"Recessive glow requires visible expression");
        h.assertTrue(Plain.effect(Gene.COAT,Gene.COAT.alleleNamed("slate")).contains("armor")
                && Plain.effect(Gene.BUILD,Gene.BUILD.alleleNamed("chonker")).contains("knockback"),"Readouts must explain inherited physical defenses");
    }
}
