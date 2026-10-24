package com.bunnidogs.mod.layout;

import com.bunnidogs.mod.genetics.Texts;

import java.util.Locale;
import com.bunnidogs.mod.genetics.*;

/** A specific purpose for the existing forecast, with target rules supplied by the real commission predicate. */
public final class GoalSheet {
    private GoalSheet() {}
    public static Page.Spread spread(GoalForecast forecast,Metrics m) { return new Page.Spread(left(forecast,m),right(forecast,m)); }
    public static String percent(double chance) {
        if (chance>0 && chance<.0001) return "<0.01%";
        if (chance<1 && chance>.9999) return ">99.99%";
        return String.format(Locale.ROOT,"%.2f%%",chance*100);
    }
    public static String title(Commissions.Request goal) {
        return switch (goal) {
            case NIGHT_GUIDE -> Texts.t("Night Guide"); case COASTAL_SCOUT -> Texts.t("Coastal Scout"); case MOUNTAIN_COURIER -> Texts.t("Mountain Courier");
            case EMBER_GARDENER -> Texts.t("Ember Gardener"); case FROST_STEWARD -> Texts.t("Frost Steward"); case DESERT_MESSENGER -> Texts.t("Desert Messenger");
            case SWAMP_HERBALIST -> Texts.t("Swamp Herbalist"); case END_NURSERY -> Texts.t("End Nursery"); case CROSSING_BRANCHES -> Texts.t("Crossing Branches");
        };
    }
    private static void lines(Page page,int y,String text,Page.Tone tone,Metrics m,int limit) {
        var wrapped=Page.wrap(text,Page.WIDTH,m);
        for (int i=0;i<Math.min(limit,wrapped.size());i++) page.text(0,y+i*Page.LINE,Page.fit(wrapped.get(i),Page.WIDTH,m),tone);
    }
    private static Page left(GoalForecast f,Metrics m) {
        var page=new Page(); page.text(0,0,Texts.t("BREEDING GOAL"),Page.Tone.HEAD); lines(page,11,title(f.goal()),Page.Tone.GOOD,m,1);
        page.text(0,26,Texts.t("Matching pup"),Page.Tone.INK); page.textRight(Page.WIDTH,26,percent(f.perPup()),Page.Tone.GOOD);
        page.text(0,38,Texts.t("At least one"),Page.Tone.INK); page.textRight(Page.WIDTH,38,percent(f.inLitter()),Page.Tone.GOOD);
        page.text(0,52,Texts.t("Genes: ")+percent(f.genes()),Page.Tone.MUTED); page.text(0,61,Texts.t("Points: ")+percent(f.points()),Page.Tone.MUTED);
        page.hotspot(0,24,Page.WIDTH,47,Texts.t("Joint chance of all required genes and bred points, before mutation. Gene pairs inherit independently; points use the same parental average, planned drift and stochastic rounding as actual hatching. The litter figure mixes the actual two possible uncrowded litter sizes. It does not promise that a pup qualifies as an adult or that the whole litter will fit. Mutation can change these genes. Held feed, Mutagen and Fortune cues remain previews."),-1);
        boolean cargo=f.goal().checks(Genome.DEFAULT,Stats.ZERO,0).stream().anyMatch(c->c.failure()==Commissions.Status.CARGO);
        String next=f.genes()==0 ? Texts.t("Need different parent genes. Compare the targets at right; collect another sample or plan an optional splice.")
                : f.points()==0 ? Texts.t("Need more bred points. Improve parents or preview richer guavas and Fortune. Points come from breeding.")
                : Texts.t("Raise a healthy adult. At the Board: a Breeder, clear front pad, one paper.")+(cargo ? Texts.t(" Equip a bundle first.") : "");
        lines(page,78,next,Page.Tone.INK,m,6);
        page.text(0,138,Texts.t("Before mutation."),Page.Tone.MUTED);
        String plan=f.cross().plan().fortune()>0 ? Texts.t("Fortune ")+f.cross().plan().fortune()+Texts.t(": crop only")
                : f.cross().plan().mutagen() ? Texts.t("Mutation: ")+ForecastSheet.percent(f.cross().mutationChance())
                : String.format(Locale.ROOT,Texts.t("Quality %.2f"),f.cross().effectiveQuality());
        page.text(0,147,Page.fit(plan,Page.WIDTH,m),Page.Tone.MUTED);
        return page;
    }
    private static Page right(GoalForecast f,Metrics m) {
        var page=new Page(); page.text(0,0,Texts.t("TARGETS"),Page.Tone.HEAD); int y=13;
        var a=f.goal().checks(f.cross().a().genome(),f.cross().a().stats(),0);
        var b=f.goal().checks(f.cross().b().genome(),f.cross().b().stats(),0);
        for (int i=0;i<a.size();i++) {
            var check=a.get(i);
            if (check.failure()==Commissions.Status.CARGO) continue;
            if (check.stat().isPresent()) {
                var stat=check.stat().orElseThrow(); double expected=f.cross().expectedPoints(stat);
                page.text(0,y,Page.fit(Plain.statName(stat)+Texts.t(" target +")+check.required(),Page.WIDTH,m),Page.Tone.INK);
                page.text(0,y+Page.LINE,Texts.t("Pup about ")+ForecastSheet.signed(expected),expected>=check.required() ? Page.Tone.GOOD : Page.Tone.MUTED);
                page.hotspot(0,y,Page.WIDTH,18,Texts.t("A: ")+ForecastSheet.signed(f.cross().a().stats().get(stat))+Texts.t(". B: ")+ForecastSheet.signed(f.cross().b().stats().get(stat))+Texts.t(". Expected pup: ")+ForecastSheet.signed(expected)+Texts.t("; target +")+check.required()+Texts.t(". Siblings round independently; the left page includes that exact chance. Improve the parents' points or preview a different feed plan; traits do not improve from exercise."),-1);
            } else {
                String label=check.gene().isPresent() ? Plain.geneName(check.gene().orElseThrow())+": "+(check.key().equals("build") ? Texts.t("fixed, 3+ packs") : Texts.t(check.detail().replace('_',' ')))
                        : check.key().equals("fixed") ? Texts.t("All eight pairs fixed") : Texts.t("Four wild origins");
                page.text(0,y,Page.fit(label,Page.WIDTH,m),Page.Tone.INK);
                page.text(0,y+Page.LINE,Texts.t("A ")+check.current()+" / "+check.required()+Texts.t("; B ")+b.get(i).current()+" / "+check.required(),Page.Tone.MUTED);
                page.hotspot(0,y,Page.WIDTH,18,label+Texts.t(". These are the actual commission requirements, including hidden copies and Diet. A/B numbers describe the sampled parents; matching-pup odds on the left combine all targets. If a required copy is absent, find another founder, choose another mate or use an optional splice plan."),-1);
            }
            y+=24;
        }
        lines(page,138,Texts.t("Use the Board for adult checks."),Page.Tone.MUTED,m,2);
        return page;
    }
}
