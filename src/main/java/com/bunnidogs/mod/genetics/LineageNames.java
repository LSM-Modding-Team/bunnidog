package com.bunnidogs.mod.genetics;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Keeper-written names for exact recorded genotypes. Separate from immutable animal snapshots and generated wild lineages. */
public final class LineageNames {
    public static final int MAX_CODE_POINTS = 40;
    private final Map<Genome,String> names = new LinkedHashMap<>();
    public Optional<String> name(Genome genome) { return Optional.ofNullable(this.names.get(genome)); }
    public boolean rename(Genome genome, String text) {
        var clean = clean(text);
        if (clean.isEmpty()) return false;
        if (clean.get().isEmpty()) { this.names.remove(genome); return true; }
        if (!this.names.containsKey(genome) && this.names.size() >= Pedigree.MAX_ENTRIES) return false;
        this.names.put(genome,clean.get()); return true;
    }
    /** Empty clears a name; controls, formatting and overlong names refuse rather than silently changing player text. */
    public static Optional<String> clean(String text) {
        if (text == null || text.length() > MAX_CODE_POINTS*2) return Optional.empty();
        String value = Normalizer.normalize(text,Normalizer.Form.NFC).strip();
        if (value.codePointCount(0,value.length()) > MAX_CODE_POINTS || text.codePoints().anyMatch(c -> c == 0x00A7
                || Character.isISOControl(c) || Character.getType(c) == Character.FORMAT
                || Character.getType(c) == Character.LINE_SEPARATOR || Character.getType(c) == Character.PARAGRAPH_SEPARATOR
                || Character.getType(c) == Character.SURROGATE)) return Optional.empty();
        return Optional.of(value);
    }
    public LineageNames retain(Pedigree book) {
        var out = new LineageNames();
        for (var entry : book.entries()) name(entry.genome()).ifPresent(name -> out.rename(entry.genome(),name));
        return out;
    }
    @Override public String toString() {
        var entries = new java.util.ArrayList<String>();
        this.names.forEach((genome,name) -> entries.add(genome+"~"+Base64.getUrlEncoder().withoutPadding()
                .encodeToString(name.getBytes(StandardCharsets.UTF_8))));
        return String.join(";",entries);
    }
    public static LineageNames parse(String text) {
        var out = new LineageNames();
        if (text == null || text.length() > 8192) return out;
        for (String piece : text.split(";")) {
            int split = piece.indexOf('~');
            if (split < 0) continue;
            var genome = Genome.parse(piece.substring(0,split));
            if (genome.isEmpty()) continue;
            try {
                String encoded = piece.substring(split+1);
                byte[] bytes = Base64.getUrlDecoder().decode(encoded);
                String value = new String(bytes,StandardCharsets.UTF_8);
                if (!Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)).equals(encoded)) continue;
                out.rename(genome.get(),value);
            } catch (IllegalArgumentException ignored) { }
        }
        return out;
    }
}
