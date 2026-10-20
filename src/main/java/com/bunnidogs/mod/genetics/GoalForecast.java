package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

/** Exact Mendelian target and stochastic point odds, before mutation/crowding; never rolls or edits a seed. */
public final class GoalForecast {
    private final Cross cross;
    private final Commissions.Request goal;
    private final double genes,points;
    public GoalForecast(Cross cross,Commissions.Request goal) {
        this.cross=cross; this.goal=goal;
        var checks=goal.checks(Genome.DEFAULT,Stats.ZERO,0);
        var relevant=EnumSet.noneOf(Gene.class);
        for (var check:checks) if (check.failure()==Commissions.Status.GENES) {
            if (check.gene().isPresent()) relevant.add(check.gene().orElseThrow()); else relevant.addAll(EnumSet.allOf(Gene.class));
        }
        boolean allFixed=checks.stream().anyMatch(check->check.key().equals("fixed") && check.required()==Gene.COUNT);
        var options=new ArrayList<List<Pair>>(); var genes=new ArrayList<>(relevant);
        for (var gene:genes) {
            var counts=new LinkedHashMap<Long,Integer>();
            for (int a=0;a<2;a++) for (int b=0;b<2;b++) {
                int first=cross.a().genome().alleleId(gene,a), second=cross.b().genome().alleleId(gene,b);
                if (allFixed && first!=second) continue;
                counts.merge(((long)first<<32)|second,1,Integer::sum);
            }
            options.add(counts.entrySet().stream().map(e->new Pair((int)(e.getKey()>>32),(int)(long)e.getKey(),e.getValue()/4.0)).toList());
        }
        this.genes=geneChance(genes,options,0,Genome.DEFAULT,1);
        this.points=pointChance(0,Stats.ZERO,1);
    }
    private record Pair(int a,int b,double chance) {}
    private double geneChance(List<Gene> genes,List<List<Pair>> options,int n,Genome genome,double chance) {
        if (n==genes.size()) return goal.checks(genome,Stats.ZERO,0).stream().filter(c->c.failure()==Commissions.Status.GENES).allMatch(Commissions.Check::met) ? chance : 0;
        double total=0; var gene=genes.get(n);
        for (var pair:options.get(n)) total+=geneChance(genes,options,n+1,genome.with(gene,0,pair.a()).with(gene,1,pair.b()),chance*pair.chance());
        return total;
    }
    private double pointChance(int n,Stats stats,double chance) {
        if (n==Stat.COUNT) return goal.checks(Genome.DEFAULT,stats,0).stream().filter(c->c.failure()==Commissions.Status.STATS).allMatch(Commissions.Check::met) ? chance : 0;
        var stat=Stat.values()[n]; double expected=cross.expectedPoints(stat), floor=Math.floor(expected), fraction=expected-floor;
        double result=pointChance(n+1,stats.with(stat,(int)floor),chance*(1-fraction));
        if (fraction>0) result+=pointChance(n+1,stats.with(stat,(int)floor+1),chance*fraction);
        return result;
    }
    public Cross cross() { return cross; }
    public Commissions.Request goal() { return goal; }
    public double genes() { return Math.clamp(genes,0,1); }
    public double points() { return Math.clamp(points,0,1); }
    public double perPup() { return genes()*points(); }
    public double inLitter() { return cross.atLeastOneInLitter(perPup()); }
}
