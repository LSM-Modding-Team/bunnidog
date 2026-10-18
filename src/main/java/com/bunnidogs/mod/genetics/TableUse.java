package com.bunnidogs.mod.genetics;

/**
 * What a right-click on the Genetics Table does (docs/TABLE_SPECS.md, chunks T1 and T2). Pure Java so the whole truth table is checked
 * without Minecraft; {@code GeneticsTableBlock} gathers the facts and does what this says.
 *
 * <p>The table has two slots, A and B, and works like a small stack: samples go on A first, then B; when both are full a new sample
 * SWAPS with the one on B (the last one put on); taking back empties B first, then A.
 *
 * <ul>
 *   <li>a readable Tissue Sample in hand: {@link #STORE} it when a slot is free, {@link #SWAP} it for the one on B when both are full;</li>
 *   <li>anything else in hand, or nothing: sneaking with BOTH hands empty takes the last sample back ({@link #TAKE}); any other click
 *       reads what is on the table ({@link #READ}); a click on an empty table does {@link #NOTHING}.</li>
 * </ul>
 */
public final class TableUse {
    private TableUse() {}

    public enum Action { STORE, SWAP, TAKE, READ, NOTHING }

    public static final int SLOT_A = 0;
    public static final int SLOT_B = 1;
    /** Returned by {@link #slot} for an action that touches no slot. */
    public static final int NO_SLOT = -1;

    /**
     * @param handHoldsSample the actual interacting hand holds a Tissue Sample whose genes can be read
     * @param handEmpty       both hands hold nothing (required for withdrawal)
     * @param sneaking        the player is sneaking
     * @param aFull           a sample stands on slot A
     * @param bFull           a sample stands on slot B
     */
    public static Action decide(boolean handHoldsSample, boolean handEmpty, boolean sneaking, boolean aFull, boolean bFull) {
        if (handHoldsSample) {
            return aFull && bFull ? Action.SWAP : Action.STORE;
        }
        if (!aFull && !bFull) {
            return Action.NOTHING;
        }
        return handEmpty && sneaking ? Action.TAKE : Action.READ;
    }

    /**
     * The slot an action works on: STORE goes to the first free slot (A, then B), SWAP replaces B, TAKE empties the last full slot
     * (B, then A). READ and NOTHING touch no slot ({@link #NO_SLOT}), and neither does a STORE with no free slot or a TAKE from an empty table
     * (the rules above never ask for those).
     */
    public static int slot(Action action, boolean aFull, boolean bFull) {
        return switch (action) {
            case STORE -> !aFull ? SLOT_A : !bFull ? SLOT_B : NO_SLOT;
            case SWAP -> SLOT_B;
            case TAKE -> bFull ? SLOT_B : aFull ? SLOT_A : NO_SLOT;
            case READ, NOTHING -> NO_SLOT;
        };
    }
}
