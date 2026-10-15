package com.bunnidogs.mod.genetics;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The code on an Ear Tag (Phase 5d): a short registration number such as {@code BD-3F7A2C}. It is derived from the
 * bunnidog's UUID, so every animal gets its own code, the same one every time, with no world-wide counter to save.
 * Pure Java (the glue passes the two halves of the UUID in), so the format is checked here without Minecraft.
 */
public final class EarTag {
    private EarTag() {}

    public static final String PREFIX = "BD-";
    private static final Pattern FORMAT = Pattern.compile("BD-[0-9A-F]{6}");

    /** "BD-" and six upper-case hex digits (24 bits, about 16.7 million codes) from the two halves of a UUID. */
    public static String code(long most, long least) {
        long h = most * 0x9E3779B97F4A7C15L ^ least;
        h ^= h >>> 32;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return String.format("%s%06X", PREFIX, (int) (h & 0xFFFFFFL));
    }

    /** True for text of the form {@code BD-XXXXXX}. */
    public static boolean isValid(String text) {
        return text != null && FORMAT.matcher(text).matches();
    }

    /** A saved code back again; anything malformed (or empty, for an untagged animal) is "no tag". */
    public static Optional<String> parse(String text) {
        return isValid(text) ? Optional.of(text) : Optional.empty();
    }
}
