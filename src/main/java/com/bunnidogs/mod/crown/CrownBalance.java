package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.genetics.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.*;

/** Dungeon-only formations and trained lines. Ordinary wild/ritual Apex rules stay authoritative. */
public final class CrownBalance {
    public record Fighter(ApexKind kind,boolean flying) {}
    private static Fighter ground(ApexKind kind) {return new Fighter(kind,false);}
    private static Fighter air(ApexKind kind) {return new Fighter(kind,true);}
    public static List<Fighter> patrol(int wing) {
        return List.of(ground(switch(wing) {case 0->ApexKind.PRISM;case 1->ApexKind.BROOD;case 2->ApexKind.SUPPORT;default->ApexKind.BRUTE;}),ground(wing==3 ? ApexKind.PRISM : ApexKind.BRUTE));
    }
    public static List<Fighter> wave(int wing,int wave) {
        return switch(wing) {
            case 0 -> wave==0
                ? List.of(ground(ApexKind.PRISM),ground(ApexKind.BRUTE),ground(ApexKind.SUPPORT),air(ApexKind.BRUTE))
                : List.of(ground(ApexKind.PRISM),air(ApexKind.PRISM),ground(ApexKind.BRUTE),ground(ApexKind.BLAST),ground(ApexKind.SUPPORT));
            case 1 -> wave==0
                ? List.of(ground(ApexKind.BROOD),ground(ApexKind.BRUTE),ground(ApexKind.PRISM),air(ApexKind.BRUTE))
                : List.of(ground(ApexKind.BROOD),ground(ApexKind.BRUTE),air(ApexKind.PRISM),ground(ApexKind.SUPPORT),ground(ApexKind.BRUTE));
            default -> wave==0
                ? List.of(ground(ApexKind.SUPPORT),ground(ApexKind.BLAST),ground(ApexKind.BRUTE),ground(ApexKind.PRISM))
                : List.of(ground(ApexKind.BLAST),ground(ApexKind.SUPPORT),ground(ApexKind.PRISM),ground(ApexKind.BRUTE),air(ApexKind.BRUTE));
        };
    }
    public static final Identifier VITALITY=Identifier.fromNamespaceAndPath("bunnidogs","crown_champion_vitality");
    public static final Identifier DAMAGE=Identifier.fromNamespaceAndPath("bunnidogs","crown_champion_damage");
    /** Called once on a fresh actor, before native spawn callbacks. Permanent modifiers survive mood
     * recalculation and native saves; no per-tick rebuff, stacking or healing on reload. */
    public static void train(Apex mob,long seed,int players,boolean champion) {
        var rng=new Random(seed);var lineage=switch(mob.kind()) {case PRISM->Lineage.CAVE;case BLAST->Lineage.NETHER;case SUPPORT->Lineage.SWAMP;default->Lineage.MEADOW;};
        var genes=Genome.randomFounder(lineage,rng);
        for(int allele=0;allele<2;allele++)genes=genes.with(Gene.BUILD,allele,Gene.BUILD.alleleNamed("stocky").id());
        if(mob.flying())genes=Wings.winged(genes);
        mob.setGenome(genes);mob.setStats(Stats.of(75+rng.nextInt(11),60+rng.nextInt(11),65+rng.nextInt(11),70+rng.nextInt(11),30+rng.nextInt(11)));
        if(champion) {
            var health=mob.getAttribute(Attributes.MAX_HEALTH);var damage=mob.getAttribute(Attributes.ATTACK_DAMAGE);
            double multiplier=Math.min(960/health.getValue(),1.5*(1+.15*(Math.clamp(players,1,4)-1)));
            health.addPermanentModifier(new AttributeModifier(VITALITY,multiplier-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            damage.addPermanentModifier(new AttributeModifier(DAMAGE,.15,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        mob.setHealth(mob.getMaxHealth());
    }
    private CrownBalance() {}
}
