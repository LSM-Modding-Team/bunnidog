package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.gametest.framework.GameTestHelper;

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
    }
}
