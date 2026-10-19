package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Camouflage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;

/** Single loaded support check, evaluated at perception/event time rather than cached each tick. */
public final class CoatCamouflage {
    private CoatCamouflage() {}
    private static final java.util.Map<Camouflage.Cover,TagKey<Block>> GROUND = new java.util.EnumMap<>(Camouflage.Cover.class);
    static {
        for (var cover : Camouflage.Cover.values()) if (cover != Camouflage.Cover.NONE)
            GROUND.put(cover,TagKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath("bunnidogs","camouflage/"+cover.key())));
    }
    public static Camouflage.Status status(Bunnidog dog) {
        if (!dog.isAlive() || !dog.acceptsCare()) return Camouflage.Status.UNAVAILABLE;
        var cover = Camouflage.cover(dog.genome());
        if (cover == Camouflage.Cover.NONE) return Camouflage.Status.LUMINOUS;
        if (dog.getHealth() < dog.getMaxHealth() || dog.hurtTime > 0 || dog.getTarget() != null
                || dog.seekingRefuge() || dog.isInLove() || dog.loveTierTicks() > 0 || dog.beingTransported()
                || dog.isVehicle() || dog.carriedBundleCount() > 0 || dog.isSprinting() || dog.isOnFire() || dog.isCurrentlyGlowing()
                || dog.isInWater() || dog.isInLava() || !dog.onGround()
                || dog.getLastHurtByMob() != null && dog.tickCount-dog.getLastHurtByMobTimestamp() < Camouflage.DISTURBED_TICKS)
            return Camouflage.Status.DISTURBED;
        var support = dog.getOnPos();
        if (!dog.level().hasChunkAt(support)) return Camouflage.Status.EXPOSED;
        var state = dog.level().getBlockState(support);
        return !state.isAir() && state.getFluidState().isEmpty() && state.is(GROUND.get(cover))
                ? Camouflage.Status.BLENDED : Camouflage.Status.EXPOSED;
    }
    /** Only STEP is suppressed: damage, landing, eating, tools and other sources remain audible to sculk. */
    public static void onGameEvent(VanillaGameEvent event) {
        if (!event.isCanceled() && event.getVanillaEvent().equals(GameEvent.STEP)
                && event.getCause() instanceof Bunnidog dog && dog.level() == event.getLevel()
                && status(dog) == Camouflage.Status.BLENDED) event.setCanceled(true);
    }
}
