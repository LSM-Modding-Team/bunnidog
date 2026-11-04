import com.bunnidogs.mod.genetics.*;
import java.math.*;
import java.util.*;

/** Read-only capacity model and actual WildHearth founder-roll sampling; no runtime rules change. */
public final class FounderCoverage {
    static final Learning.Routine[] ROLES={Learning.Routine.HARVEST,Learning.Routine.DELIVERY,Learning.Routine.CRAFT,Learning.Routine.BUILD,Learning.Routine.GARDEN,Learning.Routine.NURSERY};
    static final long[] law=new long[1<<ROLES.length];
    static int signature(Genome genome) {int mask=0;for(int i=0;i<ROLES.length;i++)if(CapabilityRules.potential(genome.mind(),ROLES[i].task())==CapabilityRules.Potential.INDEPENDENT)mask|=1<<i;return mask;}
    static void enumerate(int slot,int mental,long weight) {
        if(slot==CognitiveGene.COUNT*2) {law[signature(new Genome(Genome.DEFAULT.packed(),mental))]+=weight;return;}
        for(var allele:CognitiveGene.values()[slot/2].alleles())enumerate(slot+1,mental|(allele.id()<<(slot*4)),weight*allele.founderWeight());
    }
    static BigInteger[] combine(BigInteger[] prior) {
        var result=new BigInteger[law.length];Arrays.fill(result,BigInteger.ZERO);
        for(int i=0;i<prior.length;i++)if(prior[i].signum()!=0)for(int j=0;j<law.length;j++)if(law[j]!=0)result[i|j]=result[i|j].add(prior[i].multiply(BigInteger.valueOf(law[j])));return result;
    }
    static BigInteger covered(BigInteger[] distribution,int required) {var n=BigInteger.ZERO;for(int mask=0;mask<distribution.length;mask++)if((mask&required)==required)n=n.add(distribution[mask]);return n;}
    static String percent(BigInteger n,BigInteger d) {return new BigDecimal(n).multiply(BigDecimal.valueOf(100)).divide(new BigDecimal(d),8,RoundingMode.HALF_UP).toPlainString();}
    public static void main(String[] args) {
        enumerate(0,0,1);long total=Arrays.stream(law).sum();System.out.println("single_founder_weight="+total);
        for(int i=0;i<ROLES.length;i++) {long n=0;for(int m=0;m<law.length;m++)if((m&(1<<i))!=0)n+=law[m];System.out.println("individual_"+ROLES[i]+"="+n+"/"+total+" ("+percent(BigInteger.valueOf(n),BigInteger.valueOf(total))+"%)");}
        var triad=new BigInteger[law.length];Arrays.fill(triad,BigInteger.ZERO);triad[0]=BigInteger.ONE;for(int i=0;i<3;i++)triad=combine(triad);var denominator=BigInteger.valueOf(total).pow(3);
        for(int required:new int[]{7,15,55,63}) {var n=covered(triad,required);System.out.println("independent_draw_model_"+required+"="+n+"/"+denominator+" ("+percent(n,denominator)+"%)");}
        int count=100000,work=0,family=0,builders=0,craft=0,furnish=0,care=0;var seeds=new Random(0x42554E4E49L);
        for(int s=0;s<count;s++) {long seed=seeds.nextLong();int mask=0;for(int i=0;i<3;i++)mask|=signature(Genome.randomFounder(Lineage.MEADOW,new Random(seed+i*7919L)));if((mask&7)==7)furnish++;if((mask&55)==55)care++;if((mask&15)==15)work++;if(mask==63)family++;if((mask&8)!=0)builders++;if((mask&4)!=0)craft++;}
        System.out.println("actual_roll_sites="+count+", workshop_coverage="+work+", all_six_coverage="+family+", with_builder="+builders+", with_craft="+craft+", primitive_furnishing="+furnish+", primitive_care_coverage="+care);
        System.out.println("default_helper_mind="+Genome.DEFAULT.mind()+", signature="+signature(Genome.DEFAULT));
    }
}
