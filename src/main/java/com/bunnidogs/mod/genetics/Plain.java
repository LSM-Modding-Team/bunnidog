package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Plain language for the Animal Sheet (docs/SHEET_SPECS.md section 5): the genome code is never the explanation, these
 * words are. Pure Java; presentation uses a scoped translator with English fallbacks.
 *
 * <p>Effect sentences are built from the same numbers the game uses ({@link Gene} values, {@link Traits}, {@link Breeding},
 * {@link Trends}), so they cannot disagree with it. Every allele of every gene MUST have an entry in {@link #NOTES}
 * (the self-check fails if one is missing), so a future allele cannot be forgotten.
 */
public final class Plain {
    private Plain() {}

    /** How to read the genome code, for page 1 of the book and the code's hover box (spec 5.4). */
    public static final String HOW_TO_READ =
            "Seven pairs, one per gene, in this order: Ears, Coat, Legs, Build, Temperament, Fertility, Diet. In each pair the first digit came from the mother and the second from the father. When the two differ, the stronger one shows and the other is carried: it does not show on this animal but it can pass to its pups.";

    // ------------------------------------------------------------------ words

    /** The plain word for an allele: its name (none of the names is jargon enough to rename; the effect sentence explains). */
    public static String word(Allele allele) {
        return Texts.t(allele.name().replace('_', ' '));
    }

    /** "Ears", "Coat", ... */
    public static String geneName(Gene gene) {
        return Texts.t(gene.displayName());
    }

    /** The label used on the sheet's gene rows, short enough for a 124 px page: "Ears", "Temper.", "Fertil.". */
    public static String shortGeneName(Gene gene) {
        if (gene == Gene.BUILD) return Texts.named("text.bunnidogs.short_build", "Build");
        return Texts.t(switch (gene) {
            case TEMPERAMENT -> "Temper.";
            case FERTILITY -> "Fertil.";
            default -> gene.displayName();
        });
    }

    /** The stat's name for players: Swiftness is shown as "Speed" (the enum keeps its saved name). */
    public static String statName(Stat stat) {
        return Texts.t(stat == Stat.SWIFTNESS ? "Speed" : stat.displayName());
    }

    /** "short ears", "spotted coat", "standard legs", "average build", "loyal", "average fertility". */
    public static String phrase(Gene gene, Allele allele) {
        return switch (gene) {
            case EARS -> Texts.t("%s ears", word(allele));
            case COAT -> Texts.t("%s coat", word(allele));
            case LEGS -> Texts.t("%s legs", word(allele));
            case BUILD -> Texts.t("%s build", word(allele));
            case TEMPERAMENT -> word(allele);
            case FERTILITY -> Texts.t("%s fertility", word(allele));
            case DIET -> Texts.t("%s diet", word(allele));
        };
    }

    /** The phrase of the gene as it shows, with " carrying x" when a hidden gene is carried: "spotted coat carrying brown". */
    public static String phrase(Genome genome, Gene gene) {
        String s = phrase(gene, genome.expressed(gene));
        Optional<Allele> hidden = genome.hidden(gene);
        return hidden.map(a -> Texts.t("%s carrying %s", s, word(a))).orElse(s);
    }

    /** The allele's word, with its lineage for a wild allele: "tiny [tundra]", "short". */
    public static String label(Allele allele) {
        return allele.isWild() ? word(allele) + " [" + Texts.t(allele.lineage().key()) + "]" : word(allele);
    }

    /** One gene as a chat row: {@code Ears: short, carries long} / {@code Coat: tan}. */
    public static String row(Genome genome, Gene gene) {
        String s = geneName(gene) + ": " + label(genome.expressed(gene));
        return genome.hidden(gene).map(a -> Texts.t("%s, carries %s", s, label(a))).orElse(s);
    }

    /**
     * The whole genome in words, in code order, e.g. {@code short ears, spotted coat carrying brown, standard legs, average
     * build carrying stocky, loyal, average fertility carrying prolific}.
     */
    public static String describe(Genome genome) {
        List<String> parts = new ArrayList<>();
        for (Gene gene : Gene.values()) {
            parts.add(phrase(genome, gene));
        }
        return String.join(", ", parts);
    }

    /**
     * The look phrase (index lines, parent summaries): the coat always, then every other gene whose SHOWING allele is not
     * the common default (allele id 0), the temperament always last. Hidden genes are not in it. {@code spotted, short ears,
     * long legs, loyal} / {@code tan, loyal}.
     */
    public static String look(Genome genome) {
        List<String> parts = new ArrayList<>();
        parts.add(word(genome.expressed(Gene.COAT)));
        for (Gene gene : Gene.values()) {
            if (gene == Gene.COAT || gene == Gene.TEMPERAMENT) {
                continue;
            }
            Allele shown = genome.expressed(gene);
            if (shown.id() != 0) {
                parts.add(phrase(gene, shown));
            }
        }
        parts.add(word(genome.expressed(Gene.TEMPERAMENT)));
        return String.join(", ", parts);
    }

    /** The hidden gene of every gene that has one, in code order, as plain words (spec 5.2); empty if it carries nothing. */
    public static List<String> carries(Genome genome) {
        List<String> out = new ArrayList<>();
        for (Gene gene : Gene.values()) {
            genome.hidden(gene).ifPresent(a -> out.add(word(a)));
        }
        return out;
    }

    /** {@code Carries: brown, stocky, prolific} or {@code Carries nothing hidden}. */
    public static String carriesText(Genome genome) {
        List<String> carried = carries(genome);
        return carried.isEmpty() ? Texts.t("Carries nothing hidden") : Texts.t("Carries: ") + String.join(", ", carried);
    }

    // ------------------------------------------------------------------ one sentence per allele

    /** The part of an allele's text that is NOT a number: jargon explained, behaviour, flavour. One entry per allele, all required. */
    private static final Map<String, String> NOTES = new HashMap<>();

    private static void note(Gene gene, String allele, String text) {
        NOTES.put(gene.key() + "/" + allele, text);
    }

    static {
        note(Gene.EARS, "standard", "The common default.");
        note(Gene.EARS, "long", "");
        note(Gene.EARS, "short", "");
        note(Gene.EARS, "huge", "Huge ears.");
        note(Gene.EARS, "tiny", "Tiny ears keep the heat in.");
        note(Gene.EARS, "fennec", "Fennec ears: big radiators, like the desert fox's.");
        note(Gene.EARS, "bat", "Bat ears: big listening ears.");

        note(Gene.COAT, "tan", "The common default coat.");
        note(Gene.COAT, "cream", "");
        note(Gene.COAT, "brown", "");
        note(Gene.COAT, "spotted", "Spotted: a coat of dark patches.");
        note(Gene.COAT, "snow", "A white tundra coat.");
        note(Gene.COAT, "sand", "A sandy desert coat.");
        note(Gene.COAT, "moss", "A mossy swamp coat. Brush healthy adults with Bloom +10 for one loose moss carpet every ten minutes.");
        note(Gene.COAT, "pale", "A pale cave coat.");
        note(Gene.COAT, "slate", "A grey mountain coat.");
        note(Gene.COAT, "ember", "A glowing-red Nether coat.");
        note(Gene.COAT, "void", "A dark End coat.");
        note(Gene.COAT, "glow", "Glow: a shining swamp coat that is easy to carry and hard to show.");
        note(Gene.COAT, "seal", "Seal: slate-gray coastal fur with dark patches.");

        note(Gene.LEGS, "standard", "The common default.");
        note(Gene.LEGS, "long", "");
        note(Gene.LEGS, "short", "");
        note(Gene.LEGS, "springy", "Springy legs: a spring in every step.");
        note(Gene.LEGS, "fleet", "Fleet legs: built to run.");
        note(Gene.LEGS, "hoofed", "Hoofed legs: goat legs, made for jumping.");
        note(Gene.LEGS, "phase", "Phase legs: an End lineage's defensive blink.");
        note(Gene.LEGS, "webbed", "Webbed paws: the swamp lineage's swimming feet.");
        note(Gene.LEGS, "paddle", "Paddle paws: short, strongly swimming island legs with visible toe webbing.");

        note(Gene.BUILD, "average", "The common default.");
        note(Gene.BUILD, "stocky", "Stocky: broad and tough.");
        note(Gene.BUILD, "slim", "Slim: light and nimble.");
        note(Gene.BUILD, "chonker", "Chonker: a big round tank.");
        note(Gene.BUILD, "woolly", "Woolly: thick wool makes it tough.");
        note(Gene.BUILD, "lean", "Lean: light and fragile.");

        note(Gene.TEMPERAMENT, "loyal", "Loyal: relaxed and friendly, it wags its tail a lot. Runs away when hurt.");
        note(Gene.TEMPERAMENT, "skittish", "Skittish: keeps its distance from players, with a tucked tail. Runs away when hurt.");
        note(Gene.TEMPERAMENT, "feral", "Feral: never runs away; it bites back whoever hurts it.");
        note(Gene.TEMPERAMENT, "stoic", "Stoic: calm and unmoved, it barely wags its tail. Runs away when hurt.");

        note(Gene.FERTILITY, "average", "The common default.");
        note(Gene.FERTILITY, "prolific", "Prolific: has big litters.");
        note(Gene.FERTILITY, "shy", "Shy: has small litters, slowly.");
        note(Gene.FERTILITY, "swarm", "Swarm: breeds like a swarm.");
        note(Gene.FERTILITY, "starborn",
                "Starborn: it turns Bloom into extra seed quality, at a toll on Vitality and Senses each generation. Brush healthy adults with Bloom +20 for one Starlit Dust every ten minutes.");
        for (Diet diet : Diet.values()) note(Gene.DIET, diet.key(), diet.foods());
    }

    /** True if the allele has its note (the self-check requires it for every allele of every gene). */
    public static boolean hasNote(Gene gene, Allele allele) {
        return NOTES.containsKey(gene.key() + "/" + allele.name());
    }

    /** The note of an allele; throws for an allele nobody wrote one for, so a new allele fails loudly instead of showing nothing. */
    private static String note(Gene gene, Allele allele) {
        String text = NOTES.get(gene.key() + "/" + allele.name());
        if (text == null) {
            throw new IllegalStateException("Plain has no text for " + gene.key() + "/" + allele.name());
        }
        return Texts.t(text) + (gene == Gene.DIET ? Texts.t(" Guavas remain its breeding food; other accepted foods heal or grow pups, without starting love.") : "");
    }

    /** "35%": a percentage, rounded half away from zero, safe against 22.4999999 style float noise. */
    static long pct(double fraction) {
        return Math.round(Math.abs(fraction) * 100.0D + 1.0e-4D);
    }

    /** The numbers of an allele as a sentence, derived from the table; "" for a coat (it has none). */
    private static String numbers(Gene gene, Allele allele) {
        float v = allele.value();
        switch (gene) {
            case EARS: {
                double d = v - 1.0D;
                String s = Texts.t(d > 0 ? "Hears %s%% further." : "Hears %s%% less far.", pct(d));
                return Math.abs(d) < 1e-9 ? Texts.t("Normal hearing.") : s;
            }
            case COAT:
                return Adaptations.explanation(gene,allele).isEmpty()
                        ? Texts.t("Cosmetic only: it changes the look and the pelt it drops, not what it can do.") : "";
            case LEGS: {
                double speed = (v - 1.0D) * 0.6D;
                double jump = (v - 1.0D) * 1.2D;
                if (Math.abs(speed) < 1e-9) {
                    return Texts.t("Normal speed and jumps.");
                }
                return Texts.t(speed > 0 ? "About %s%% faster and %s%% higher jumps." : "About %s%% slower and %s%% lower jumps.", pct(speed), pct(jump));
            }
            case BUILD: {
                double health = (v - 1.0D) * 1.5D;
                return Math.abs(health) < 1e-9 ? Texts.t("Normal health.") : Texts.t(health > 0 ? "About %s%% more health." : "About %s%% less health.", pct(health));
            }
            case TEMPERAMENT:
                return "";
            case DIET:
                return Texts.t("Does not change body stats, stat drift or juice effects.");
            case FERTILITY: {
                double litter = v - 1.0D;
                double rest = 1.0D / v - 1.0D;
                if (Math.abs(litter) < 1e-9) {
                    return Texts.t("Normal litters.");
                }
                return Texts.t("Paired with one like it: litters ") + pct(litter) + (litter > 0 ? Texts.t("% bigger") : Texts.t("% smaller")) + ", "
                        + pct(rest) + (rest < 0 ? Texts.t("% less rest between litters") : Texts.t("% more rest between litters"))
                        + Texts.t(", mutations ") + String.format(Locale.ROOT, "%.1f", v * v) + Texts.t(" times as likely.");
            }
            default:
                throw new IllegalStateException("unhandled gene " + gene);
        }
    }

    /** How the allele enters the population, from its data: rare, wild (recessive or not), or nothing to say for a common one. */
    private static String origin(Allele allele) {
        if (allele.isRare()) {
            return Texts.t("A rare mutation: it shows even when only one parent gave it.");
        }
        if (allele.isWild()) {
            return allele.dominance() == 0
                    ? Texts.t("Found only in the %s lineage, and it shows only when both parents gave it.", Texts.t(allele.lineage().key()))
                    : Texts.t("Found only in the %s lineage.", Texts.t(allele.lineage().key()));
        }
        return "";
    }

    /**
     * The sentence of one allele: the note (jargon explained) then the numbers it brings, then where it comes from. For
     * tiny ears: "Tiny ears keep the heat in (tundra). Hears 50% less far. Found only in the tundra lineage."
     */
    public static String effect(Gene gene, Allele allele) {
        List<String> parts = new ArrayList<>(3);
        for (String part : new String[] { note(gene, allele), numbers(gene, allele), Adaptations.explanation(gene,allele), origin(allele) }) {
            if (!part.isEmpty()) {
                parts.add(part);
            }
        }
        return String.join(" ", parts);
    }

    /** What the allele does to the next generation's stats: {@code Each generation: +1 Vitality, -1 Speed}, or "" if nothing. */
    public static String trend(Gene gene, Allele allele) {
        List<String> parts = new ArrayList<>();
        for (Trends.Term term : Trends.terms(gene, allele)) {
            String sign = term.kind() == Trends.Kind.GAIN ? "+" : "-";
            parts.add(sign + stripZero(term.amount()) + " " + statWord(term.stat()));
        }
        return parts.isEmpty() ? "" : Texts.t("Each generation: ") + String.join(", ", parts) + ".";
    }

    private static String statWord(Stat stat) {
        return statName(stat);
    }

    private static String stripZero(double x) {
        return x == Math.rint(x) ? String.valueOf((long) x) : String.format(Locale.ROOT, "%.1f", x);
    }

    // ------------------------------------------------------------------ the hover text of a gene row

    /**
     * What a gene row says when hovered: the showing allele's sentence, and how it is inherited, e.g.
     * {@code Short ears: hears 35% less far. Hidden "long": about half of its pups get it.}
     */
    public static String hover(Genome genome, Gene gene) {
        Allele shown = genome.expressed(gene);
        StringBuilder sb = new StringBuilder();
        sb.append(capitalize(phrase(gene, shown))).append(": ").append(effect(gene, shown));
        String trend = trend(gene, shown);
        if (!trend.isEmpty()) {
            sb.append(' ').append(trend);
        }
        Optional<Allele> hidden = genome.hidden(gene);
        if (hidden.isPresent()) {
            sb.append(Texts.t(" Hidden \"")).append(word(hidden.get())).append(Texts.t("\": about half of its pups get it."));
        } else {
            sb.append(Texts.t(" Both parents gave the same gene, so every pup gets it."));
        }
        return sb.toString();
    }

    static String capitalize(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
