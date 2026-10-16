package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * One page of the Pedigree Book as a LIST OF THINGS TO DRAW (Phase 8 S3): text lines, heart icons and the portrait box,
 * all in pixels from the page's top-left corner. Pure Java: the screen only draws what a page lists, so what the
 * self-check tests is what the player sees. Also holds the shared geometry (a spread is two pages) and the two text helpers
 * {@link #fit} and {@link #wrap}.
 */
public final class Page {
    /** Colour KIND of a text line; the screen maps it to a colour. */
    public enum Tone { INK, HEAD, MUTED, GOOD, BAD }

    /** A line of text. {@code right} = {@code x} is the RIGHT edge of the text (right-aligned), else its left edge. */
    public record Text(int x, int y, String text, Tone tone, boolean right) {}

    /** A row of hearts: {@code full} whole icons, then a half one if {@code half}; {@code times > 0} means one icon (the text says "xN"). */
    public record Hearts(int x, int y, int full, boolean half, int times) {}

    /** The framed picture box of the animal. {@code coat} is the allele name; S3 paints it as a flat swatch, S4 will draw the animal. */
    public record Portrait(int x, int y, int w, int h, String coat) {}

    /**
     * A rectangle that shows a hover box with {@code text} when the mouse is over it (Phase 8 S5). {@code pair} is the gene index
     * of the genome code pair to light up while it is hovered, or -1 for none. The screen wraps the text to the box it draws.
     */
    public record Hotspot(int x, int y, int w, int h, String text, int pair) {}

    /** A rectangle that jumps to spread {@code spread} (0 = contents, n = the sheet of animal n) when clicked (Phase 8 S7). */
    public record Link(int x, int y, int w, int h, int spread) {}

    /** Where one pair of the genome code is drawn (x of its left edge, y, width), so the screen can light it up. */
    public record Pair(int x, int y, int w) {}

    /** The two pages of the open book. */
    public record Spread(Page left, Page right) {}

    // ------------------------------------------------------------------ geometry (GUI pixels)

    /** Width of a page's text area. */
    public static final int WIDTH = 130;
    /** Height of a page's text area: 17 lines of {@link #LINE} px. */
    public static final int HEIGHT = 156;
    public static final int LINE = 9;

    /** The open book (drawn by the screen with plain fills, no texture) and where each page's text area starts inside it. */
    public static final int BOOK_W = 312;
    public static final int BOOK_H = 200;
    public static final int LEFT_X = 18;
    public static final int RIGHT_X = 164;
    public static final int TOP = 22;

    private final List<Text> texts = new ArrayList<>();
    private final List<Hearts> hearts = new ArrayList<>();
    private Portrait portrait;
    private final List<Hotspot> hotspots = new ArrayList<>();
    private final List<Pair> pairs = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();

    public List<Link> links() {
        return links;
    }

    public void link(int x, int y, int w, int h, int spread) {
        links.add(new Link(x, y, w, h, spread));
    }

    public List<Hotspot> hotspots() {
        return hotspots;
    }

    /** The genome's pairs in code order (empty on pages without a code). */
    public List<Pair> pairs() {
        return pairs;
    }

    public void hotspot(int x, int y, int w, int h, String text, int pair) {
        hotspots.add(new Hotspot(x, y, w, h, text, pair));
    }

    public void pair(int x, int y, int w) {
        pairs.add(new Pair(x, y, w));
    }

    public List<Text> texts() {
        return texts;
    }

    public List<Hearts> hearts() {
        return hearts;
    }

    /** The portrait box, or null if the page has none. */
    public Portrait portrait() {
        return portrait;
    }

    public void text(int x, int y, String text, Tone tone) {
        texts.add(new Text(x, y, text, tone, false));
    }

    /** Text whose right edge is at {@code rightEdge}. */
    public void textRight(int rightEdge, int y, String text, Tone tone) {
        texts.add(new Text(rightEdge, y, text, tone, true));
    }

    public void hearts(int x, int y, int full, boolean half, int times) {
        hearts.add(new Hearts(x, y, full, half, times));
    }

    public void portrait(int x, int y, int w, int h, String coat) {
        portrait = new Portrait(x, y, w, h, coat);
    }

    // ------------------------------------------------------------------ text helpers

    /** The text if it fits in {@code max} px, else cut and ended with ".." so that it does. */
    public static String fit(String text, int max, Metrics m) {
        if (m.width(text) <= max) {
            return text;
        }
        String s = text;
        while (!s.isEmpty() && m.width(s + "..") > max) {
            s = s.substring(0, s.length() - 1);
        }
        return s.stripTrailing() + "..";
    }

    /** Greedy word wrap into lines of at most {@code max} px (a single word wider than that is cut by {@link #fit}). */
    public static List<String> wrap(String text, int max, Metrics m) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            String tryLine = line.length() == 0 ? word : line + " " + word;
            if (m.width(tryLine) <= max) {
                line.setLength(0);
                line.append(tryLine);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                }
                line.setLength(0);
                line.append(fit(word, max, m));
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
