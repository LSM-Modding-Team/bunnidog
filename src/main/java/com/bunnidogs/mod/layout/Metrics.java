package com.bunnidogs.mod.layout;

/**
 * How wide a string is, in GUI pixels. The layout is pure Java and cannot ask Minecraft's font, so the screen passes
 * {@code font::width} and the self-check / preview pass {@link #ESTIMATE}, an approximation of the default font (most
 * glyphs 6 px, narrow ones 2 to 5 px). The layout never assumes more than this interface.
 */
@FunctionalInterface
public interface Metrics {
    int width(String text);

    /** The default font as far as the layout needs it: close enough to check that text fits, deliberately not exact. */
    Metrics ESTIMATE = text -> {
        int w = 0;
        for (int i = 0; i < text.length(); i++) {
            w += switch (text.charAt(i)) {
                case 'i', '!', '.', ',', ':', ';', '|', '\'' -> 2;
                case 'l' -> 3;
                case 'I', 't', '[', ']', ' ' -> 4;
                case 'f', 'k', '(', ')', '<', '>', '*', '{', '}' -> 5;
                case '@', '~' -> 7;
                default -> 6;
            };
        }
        return w;
    };
}
