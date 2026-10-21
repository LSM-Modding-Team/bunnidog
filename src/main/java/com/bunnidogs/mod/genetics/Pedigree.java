package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The log of a Pedigree Book (Phase 5c): bunnidogs the player has registered, each with the genomes of its recorded
 * parents ({@code Bunnidog#motherGenome}). Pure Java, no Minecraft. Entries are identified by their genes, not by an entity
 * (a bunnidog has no id of its own): two bunnidogs with the same genome and the same parents are ONE entry. Parent links
 * are worked out at read time: a logged animal's parent is the first OTHER entry that has the parent's genome.
 *
 * <p>Phase 5e: an animal logged by the game also carries its UUID, which identifies it exactly: two animals with the same
 * genes are two entries, one animal is one entry however often it is logged. Entries WITHOUT a UUID (books from 5c) keep the
 * old gene-based identity; logging an animal whose old entry has no UUID replaces that entry in place (UPDATED). An animal's
 * Ear Tag ({@link EarTag}), if it wears one, is only a label shown in the list; tagging an animal that is already logged
 * updates its entry the same way.
 *
 * <p>Text form (stored in the book): entries joined by {@code ;}, each {@code genome} or {@code genome<mother+father}, with an
 * optional {@code #uuid} and then an optional {@code @BD-XXXXXX} right after the genome, e.g.
 * {@code 00-00-00-11-00-00#123e4567-e89b-12d3-a456-426614174000@BD-3F7A2C<00-00-00-11-00-00+00-00-00-11-00-00;...}. Books from
 * before 5e have neither and still read. Phase 8 S2 (format v2) appends the snapshot {@code $a,b,c,d,e} (the five stat points) after
 * the tag and a {@code !} right after it for "was a pup": {@code genome#uuid@tag$3,-1,0,2,12!<mother+father}. Books without {@code $} still read
 * (no stats recorded). Malformed entries are skipped when parsing.
 */
public final class Pedigree {
    /** A book holds this many animals: it is a notebook, not a database (the text lives in an item component). */
    public static final int MAX_ENTRIES = 24;

    /**
     * One logged animal. {@code stats} and {@code pup} are the SNAPSHOT taken when it was logged (save format v2, Phase 8 S2):
     * books from before have no stats, and then the pup flag is meaningless (false), because the text form only carries the
     * flag after the stats.
     */
    public record Entry(Genome genome, Optional<Genome> mother, Optional<Genome> father, Optional<UUID> id, Optional<String> tag,
            Optional<Stats> stats, boolean pup) {
        /** Parents are recorded as a pair or not at all; a tag that is not a valid ear tag code means no tag; no stats, no pup flag. */
        public Entry {
            if (mother.isPresent() != father.isPresent()) {
                mother = Optional.empty();
                father = Optional.empty();
            }
            tag = tag.filter(EarTag::isValid);
            if (stats.isEmpty()) {
                pup = false;
            }
        }

        /** An animal without recorded stats (every entry made before Phase 8 S2). */
        public Entry(Genome genome, Optional<Genome> mother, Optional<Genome> father, Optional<UUID> id, Optional<String> tag) {
            this(genome, mother, father, id, tag, Optional.empty(), false);
        }

        /** An animal known only by its genes (how every entry was made before Phase 5e). */
        public Entry(Genome genome, Optional<Genome> mother, Optional<Genome> father) {
            this(genome, mother, father, Optional.empty(), Optional.empty());
        }

        /** Same genes and the same recorded parents, whatever the ids, tags and snapshot. */
        boolean sameGenes(Entry other) {
            return genome.equals(other.genome) && mother.equals(other.mother) && father.equals(other.father);
        }

        @Override
        public String toString() {
            String head = genome + (id.isPresent() ? "#" + id.get() : "") + (tag.isPresent() ? "@" + tag.get() : "")
                    + (stats.isPresent() ? "$" + stats.get() + (pup ? "!" : "") : "");
            return mother.isPresent() ? head + "<" + mother.get() + "+" + father.get() : head;
        }
    }

    /** UPDATED = the animal was already in the book and its entry changed (it got its UUID or its ear tag); the book did not grow. */
    public enum Added { ADDED, UPDATED, ALREADY, FULL }

    private final List<Entry> entries = new ArrayList<>();
    private LineageNames lineageNames = new LineageNames();
    public LineageNames lineageNames() { return this.lineageNames; }
    public void setLineageNames(LineageNames names) { this.lineageNames = names.retain(this); }
    public Optional<String> lineageName(Genome genome) { return this.lineageNames.name(genome); }

    public int size() {
        return entries.size();
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Added add(Entry entry) {
        if (entry.id().isPresent()) {
            for (int i = 0; i < entries.size(); i++) {
                Entry e = entries.get(i);
                if (e.id().equals(entry.id())) {
                    if (e.equals(entry)) {
                        return Added.ALREADY;
                    }
                    entries.set(i, entry);          // same animal, newer facts (it was tagged since)
                    return Added.UPDATED;
                }
            }
            // a 5c entry (genes only) of this animal: take it over in place
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).id().isEmpty() && entries.get(i).sameGenes(entry)) {
                    entries.set(i, entry);
                    return Added.UPDATED;
                }
            }
        } else if (entries.contains(entry)) {
            return Added.ALREADY;       // equals() includes the id, so this only matches entries without one
        }
        if (entries.size() >= MAX_ENTRIES) {
            return Added.FULL;
        }
        entries.add(entry);
        return Added.ADDED;
    }

    /** Index of the first entry other than {@code self} whose genome is {@code genome}, or -1. */
    private int find(Genome genome, int self) {
        for (int i = 0; i < entries.size(); i++) {
            if (i != self && entries.get(i).genome().equals(genome)) {
                return i;
            }
        }
        return -1;
    }

    /** Generation of entry {@code index} (the family list's number): 0 if none of its parents is logged. Used by the sheet (Phase 8 S3). */
    public int generationOf(int index) {
        return generation(index, new HashSet<>());
    }

    /** Index of the logged entry that is the mother ({@code mother} true) or father of entry {@code index}, or -1 (not recorded, or not logged). */
    public int parentIndex(int index, boolean mother) {
        Entry e = entries.get(index);
        Optional<Genome> parent = mother ? e.mother() : e.father();
        return parent.isPresent() ? find(parent.get(), index) : -1;
    }

    /** Generation of entry i: 0 if no parent of it is logged, else 1 + the older of its logged parents. Loops (inbred lines) count as 0. */
    private int generation(int i, Set<Integer> visiting) {
        Entry e = entries.get(i);
        if (e.mother().isEmpty() || !visiting.add(i)) {
            return 0;
        }
        int best = -1;
        for (Genome parent : List.of(e.mother().get(), e.father().get())) {
            int p = find(parent, i);
            if (p >= 0) {
                best = Math.max(best, generation(p, visiting));
            }
        }
        visiting.remove(i);
        return best + 1;
    }

    /** What the book shows: a headline, then each animal with its generation and where its parents are in the book. */
    public List<String> lines() {
        List<String> lines = new ArrayList<>();
        lines.add("Pedigree Book: " + entries.size() + " of " + MAX_ENTRIES + " animals logged");
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            lines.add("#" + (i + 1) + (e.tag().isPresent() ? "  " + e.tag().get() : "") + "  generation " + generation(i, new HashSet<>()) + "  "
                    + Plain.look(e.genome()) + (e.stats().isPresent() ? (e.pup() ? " (pup)" : " (adult)") : "") + "  [" + e.genome() + "]");
            if (e.mother().isEmpty()) {
                lines.add("    no recorded parents");
            } else {
                lines.add("    mother " + where(find(e.mother().get(), i), e.mother().get())
                        + ", father " + where(find(e.father().get(), i), e.father().get()));
            }
        }
        return lines;
    }

    private static String where(int index, Genome genome) {
        return index >= 0 ? "#" + (index + 1) : Plain.look(genome) + Texts.t(" (not logged)");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(e);
        }
        return sb.toString();
    }

    /** Reads the text form; null, empty or malformed pieces give fewer entries, never an error. Respects {@link #MAX_ENTRIES}. */
    public static Pedigree parse(String text) {
        Pedigree book = new Pedigree();
        if (text == null || text.isBlank()) {
            return book;
        }
        for (String piece : text.split(";")) {
            String[] sides = piece.split("<", -1);
            String[] snap = sides[0].split("\\$", -1);
            Optional<Stats> stats = Optional.empty();
            boolean pup = false;
            if (snap.length > 2) {
                continue;
            }
            if (snap.length == 2) {
                String text1 = snap[1];
                if (text1.endsWith("!")) {
                    pup = true;
                    text1 = text1.substring(0, text1.length() - 1);
                }
                stats = Stats.parse(text1);
                if (stats.isEmpty() || !stats.get().toString().equals(text1)) {
                    continue;       // only the canonical five numbers: "3,-1" or "3 -1" is not a snapshot
                }
            }
            String[] named = snap[0].split("@", -1);
            String[] identified = named[0].split("#", -1);
            Optional<Genome> genome = Genome.parse(identified[0]);
            if (genome.isEmpty() || sides.length > 2 || named.length > 2 || identified.length > 2) {
                continue;
            }
            Optional<String> tag = Optional.empty();
            if (named.length == 2) {
                tag = EarTag.parse(named[1]);
                if (tag.isEmpty()) {
                    continue;
                }
            }
            Optional<UUID> id = Optional.empty();
            if (identified.length == 2) {
                id = parseUuid(identified[1]);
                if (id.isEmpty()) {
                    continue;
                }
            }
            Optional<Genome> mother = Optional.empty();
            Optional<Genome> father = Optional.empty();
            if (sides.length == 2) {
                String[] parents = sides[1].split("\\+", -1);
                if (parents.length != 2) {
                    continue;
                }
                mother = Genome.parse(parents[0]);
                father = Genome.parse(parents[1]);
                if (mother.isEmpty() || father.isEmpty()) {
                    continue;
                }
            }
            book.add(new Entry(genome.get(), mother, father, id, tag, stats, pup));
        }
        return book;
    }

    /**
     * The first entry of a text form, or empty if there is none (Phase 8 S6: the Calipers window sends ONE animal as the text of its
     * {@link Entry}, and the client reads it back with this).
     */
    public static Optional<Entry> parseEntry(String text) {
        List<Entry> found = parse(text).entries();
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    /** A UUID in its usual text form; anything else is empty (UUID.fromString alone is too forgiving: it accepts "1-2-3-4-5"). */
    private static Optional<UUID> parseUuid(String text) {
        if (!text.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(text));
    }
}
