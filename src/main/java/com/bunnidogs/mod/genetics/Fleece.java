package com.bunnidogs.mod.genetics;

import java.util.List;

/** Modest renewable loose fleece; full pelts and the death-drop tables keep their existing role. */
public final class Fleece {
    private Fleece() {}
    public static final int COUNT = 2;
    public static final int REGROW_TICKS = 10 * 60 * 20;
    public record Yield(String color, int count) {}
    public static List<Yield> yields(Genome genome) {
        if (!genome.expressed(Gene.BUILD).name().equals("woolly")) return List.of();
        if (genome.expressed(Gene.COAT).name().equals("spotted"))
            return List.of(new Yield("yellow",1),new Yield("brown",1));
        String color = switch (genome.expressed(Gene.COAT).name()) {
            case "tan", "sand" -> "yellow";
            case "brown" -> "brown";
            case "moss" -> "green";
            case "glow" -> "cyan";
            case "pale" -> "light_gray";
            case "slate", "seal" -> "gray";
            case "ember" -> "red";
            case "void" -> "purple";
            default -> "white"; // cream, snow and a safe fallback for future coats
        };
        return List.of(new Yield(color,COUNT));
    }
    public static boolean ready(Genome genome, boolean adult, float health, float maximum, boolean recovered) {
        return adult && recovered && genome.expressed(Gene.BUILD).name().equals("woolly") && Sampling.healthy(health,maximum);
    }
}
