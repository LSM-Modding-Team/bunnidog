package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

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
    }
}
