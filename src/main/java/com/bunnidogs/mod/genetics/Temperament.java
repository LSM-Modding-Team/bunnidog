package com.bunnidogs.mod.genetics;

/** How a bunnidog behaves around players. The code is the expressed Temperament allele's {@code value}. */
public enum Temperament {
    LOYAL(0),      // relaxed, wags a lot
    SKITTISH(1),   // keeps its distance from players
    FERAL(2),      // bites back when hurt
    STOIC(3);      // calm; slow wag

    private final int code;

    Temperament(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static Temperament fromCode(int code) {
        for (Temperament t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return LOYAL;
    }
}
