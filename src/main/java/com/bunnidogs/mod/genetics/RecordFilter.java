package com.bunnidogs.mod.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Local snapshot queries return original record indices; parent links and saved order never change. */
public final class RecordFilter {
    private RecordFilter() {}
    private static final Pattern COMPARISON = Pattern.compile("([a-z]+)(<=|>=|=|<|>)([+-]?\\d+)");
    /** Navigate matching original sheets; a parent link may put the current sheet outside this result list. */
    public static int turn(Pedigree book, String query, int current, int direction) {
        int previous = 0;
        for (int index : indices(book,query)) {
            int spread = index+1;
            if (direction > 0 && spread > current) return spread;
            if (direction < 0 && spread < current) previous = spread;
        }
        return direction < 0 ? previous : current;
    }
    public static List<Integer> indices(Pedigree book, String query) {
        var terms = (query == null ? "" : query.strip().toLowerCase(Locale.ROOT)).split("\\s+");
        var out = new ArrayList<Integer>();
        var entries = book.entries();
        for (int i = 0; i < entries.size(); i++) {
            boolean match = true;
            for (var term : terms) if (!matches(entries.get(i),term)) { match = false; break; }
            if (match) out.add(i);
        }
        return List.copyOf(out);
    }
    private static boolean matches(Pedigree.Entry entry, String term) {
        if (term.isEmpty()) return true;
        var comparison = COMPARISON.matcher(term);
        if (comparison.matches()) {
            var stat = Stat.fromKey(comparison.group(1).equals("speed") ? "swiftness" : comparison.group(1));
            if (stat.isEmpty() || entry.stats().isEmpty()) return false;
            long target;
            try { target = Long.parseLong(comparison.group(3)); } catch (NumberFormatException ignored) { return false; }
            int points = entry.stats().get().get(stat.get());
            return switch (comparison.group(2)) {
                case ">=" -> points >= target; case "<=" -> points <= target; case ">" -> points > target;
                case "<" -> points < target; default -> points == target;
            };
        }
        if (term.equals("pup") || term.equals("adult")) return entry.stats().isPresent() && entry.pup() == term.equals("pup");
        if (term.startsWith("tag:")) return entry.tag().orElse("").toLowerCase(Locale.ROOT).contains(term.substring(4));
        boolean shown = term.startsWith("shown:"), carried = term.startsWith("carried:");
        if (shown || carried) {
            var name = term.substring(shown ? 6 : 8);
            if (name.isEmpty()) return false;
            for (var gene : Gene.values()) {
                var expressed = entry.genome().expressed(gene).name();
                if (shown && expressed.contains(name)) return true;
                if (carried) for (int copy = 0; copy < 2; copy++) {
                    var allele = entry.genome().allele(gene,copy).name();
                    if (!allele.equals(expressed) && allele.contains(name)) return true;
                }
            }
            return false;
        }
        var text = new StringBuilder(entry.tag().orElse("")).append(' ').append(entry.genome()).append(' ')
                .append(Plain.look(entry.genome()));
        if (entry.stats().isEmpty()) text.append(" unknown stats age");
        for (var gene : Gene.values()) for (int copy = 0; copy < 2; copy++) text.append(' ').append(entry.genome().allele(gene,copy).name());
        return text.toString().toLowerCase(Locale.ROOT).contains(term);
    }
}
