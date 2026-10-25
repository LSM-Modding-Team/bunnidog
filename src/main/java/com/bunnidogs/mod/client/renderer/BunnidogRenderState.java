package com.bunnidogs.mod.client.renderer;

import com.bunnidogs.mod.genetics.Temperament;
import com.bunnidogs.mod.genetics.FertilityMark;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Render state = a snapshot of what the renderer/model need from the entity (the 26.x renderers never touch the
 * entity directly). Filled in {@link BunnidogRenderer#extractRenderState}.
 */
public class BunnidogRenderState extends LivingEntityRenderState {
    /** Index of the expressed Coat allele; picks the texture. */
    public int coat;
    public boolean winged,wingFlying,wingDiving;
    public float swelling;
    public boolean redFuse;
    /** Ear length multiplier (Ears gene). */
    public float earScale = 1.0F;
    /** Leg length multiplier (Legs gene). */
    public float legScale = 1.0F;
    /** Body width multiplier (Build gene). */
    public float buildScale = 1.0F;
    public boolean webbed;
    public boolean paddling;
    public boolean downed;
    public boolean alerted;
    public float bite;
    public int bundleCount;
    public FertilityMark fertilityMark = FertilityMark.NONE;
    public Temperament temperament = Temperament.LOYAL;
}
