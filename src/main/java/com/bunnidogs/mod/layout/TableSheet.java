package com.bunnidogs.mod.layout;

import java.util.Optional;

import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.BreedingPlan;
import com.bunnidogs.mod.genetics.Cross;
import com.bunnidogs.mod.genetics.Tissue;

/**
 * What the Genetics Table window shows (T2 and T3, docs/TABLE_SPECS.md): the samples on slot A and slot B. One spread per sample that is there,
 * in the order A, B: the sheet of the animal it came from, headed {@code Table: sample A} / {@code Table: sample B}. With both slots full
 * there are three analysis spreads: pup looks/stats, inherited copies and both-direction splice plans.
 * Pure Java, like {@link SeedSheet}: the screen only draws what this lays out, and the text form below is what the server sends.
 *
 * <p>Wire text: {@code A|B} or {@code A|B|tierA,tierB,treated[,fortune]}, each side the {@link Tissue} text ({@code genome/stats}) or nothing for an empty slot, for example
 * {@code 33-33-33-33-33-33/0,0,0,0,0|} (only A) or {@code |22-32-00-10-00-01/3,-1,0,2,12} (only B).
 */
public record TableSheet(Optional<Tissue> a, Optional<Tissue> b, BreedingPlan plan) {
    public TableSheet(Optional<Tissue> a, Optional<Tissue> b) { this(a,b,BreedingPlan.PLAIN); }
    public static final String SEPARATOR = "|";

    /** The text form. Round-trips through {@link #parse}. */
    @Override
    public String toString() {
        return this.a.map(Tissue::toString).orElse("") + SEPARATOR + this.b.map(Tissue::toString).orElse("")
                + (this.plan.equals(BreedingPlan.PLAIN) ? "" : SEPARATOR+this.plan);
    }

    /** Parses the text form; empty if it has no separator or if neither slot holds a readable sample (nothing to show). */
    public static Optional<TableSheet> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String[] fields = text.split("\\|",-1);
        if (fields.length < 2 || fields.length > 3) return Optional.empty();
        var plan = fields.length == 2 ? Optional.of(BreedingPlan.PLAIN) : BreedingPlan.parse(fields[2]);
        if (plan.isEmpty()) return Optional.empty();
        TableSheet sheet = new TableSheet(slot(fields[0]),slot(fields[1]),plan.get());
        return sheet.spreadCount() == 0 ? Optional.empty() : Optional.of(sheet);
    }

    private static Optional<Tissue> slot(String text) {
        return text.isBlank() ? Optional.empty() : Tissue.parse(text);
    }

    public static final int FORECAST = 2;
    public static final int INHERITANCE = 3;
    public static final int SPLICING = 4;

    /** One spread per sample, plus look/inheritance/splice plans when both are there: 0, 1 or 5. */
    public int spreadCount() {
        int samples = (this.a.isPresent() ? 1 : 0) + (this.b.isPresent() ? 1 : 0);
        return samples == 2 ? 5 : samples;
    }

    /** True for the look/stat forecast, spread 2, with two samples. */
    public boolean isForecast(int n) {
        return this.a.isPresent() && this.b.isPresent() && n == FORECAST;
    }

    /** Analysis spreads have no individual portrait. */
    public boolean isAnalysis(int n) {
        return this.a.isPresent() && this.b.isPresent() && n >= FORECAST;
    }

    /** Which slot spread {@code n} shows: 0 = A, 1 = B (with only one sample, its slot whatever {@code n} is; meaningless for the forecast, see {@link #isForecast}). */
    public int slotOf(int n) {
        if (this.a.isPresent() && this.b.isPresent()) {
            return Math.max(0, Math.min(n, 1));
        }
        return this.a.isPresent() ? 0 : 1;
    }

    /** The sample spread {@code n} shows; call only for a nonempty sample spread, never {@link #isAnalysis}. */
    public Tissue sample(int n) {
        return slotOf(n) == 0 ? this.a.orElseThrow() : this.b.orElseThrow();
    }

    /** The sample of spread {@code n} as a sheet entry (for the portrait). */
    public Pedigree.Entry entry(int n) {
        return SheetLayout.sampleEntry(sample(n));
    }

    /** Spread {@code n}: A, B, look/stat forecast, inherited alleles, splice plans. */
    public Page.Spread spread(int n, Metrics m) {
        n = Math.max(0, Math.min(n, spreadCount() - 1));
        if (isAnalysis(n) && n == SPLICING) return SpliceSheet.spread(this.a.orElseThrow(), this.b.orElseThrow(), m);
        if (isAnalysis(n) && n == INHERITANCE) return InheritanceSheet.spread(this.a.orElseThrow(), this.b.orElseThrow(), m);
        if (isForecast(n)) {
            return ForecastSheet.spread(new Cross(this.a.orElseThrow(),this.b.orElseThrow(),this.plan), m);
        }
        return SheetLayout.tableSample(sample(n), slotOf(n) == 0, m);
    }
}
