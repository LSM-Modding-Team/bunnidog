package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.CaveSight;
import com.bunnidogs.mod.genetics.Sampling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** One actual berry pays for native keeper vision. A reentry guard and exact refund preserve callbacks and other effects. */
final class CaveGuiding {
    private final Bunnidog dog;
    private long nextUse;
    private boolean giving;
    CaveGuiding(Bunnidog dog) { this.dog=dog; }
    int waitTicks() { return (int)Math.clamp(nextUse-dog.level().getGameTime(),0L,(long)CaveSight.RECOVERY_TICKS); }
    void restore(int remaining) { nextUse=dog.level().getGameTime()+Math.clamp(remaining,0,CaveSight.RECOVERY_TICKS); }
    CaveSight.Status status(Player player) {
        if (!dog.acceptsCare() || !dog.isAlive() || dog.level()!=player.level() || dog.distanceToSqr(player)>36
                || !dog.level().hasChunkAt(player.blockPosition())) return CaveSight.Status.UNAVAILABLE;
        if (giving) return CaveSight.Status.BUSY;
        if (dog.isBaby()) return CaveSight.Status.ADULT;
        if (!Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) return CaveSight.Status.HEALTH;
        if (!CaveSight.qualified(dog.genome(),dog.stats())) return CaveSight.Status.TRAITS;
        if (dog.getTarget()!=null || dog.seekingRefuge() || dog.isInLove()
                || dog.getLastHurtByMob()!=null && dog.tickCount-dog.getLastHurtByMobTimestamp()<100) return CaveSight.Status.COMBAT;
        if (waitTicks()>0) return CaveSight.Status.RESTING;
        if (player.hasEffect(MobEffects.NIGHT_VISION)) return CaveSight.Status.ALREADY;
        return dog.level().getMaxLocalRawBrightness(player.blockPosition())>CaveSight.MAX_LIGHT ? CaveSight.Status.BRIGHT : CaveSight.Status.READY;
    }
    InteractionResult use(Player player,InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (!(dog.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var held=player.getItemInHand(hand);
        if (!held.is(Items.GLOW_BERRIES)) return InteractionResult.PASS;
        var status=status(player);
        if (status!=CaveSight.Status.READY) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.cave_sight."+status.name().toLowerCase(java.util.Locale.ROOT),(waitTicks()+19)/20));
            return InteractionResult.SUCCESS;
        }
        boolean paid=!player.hasInfiniteMaterials(); var berry=held.copyWithCount(1);
        giving=true; boolean applied=false;
        try {
            if (paid) held.shrink(1);
            applied=player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,CaveSight.DURATION_TICKS));
            if (applied) {
                nextUse=level.getGameTime()+CaveSight.RECOVERY_TICKS;
                dog.playEatingSound(); player.sendOverlayMessage(Component.translatable("message.bunnidogs.cave_sight.given",CaveSight.DURATION_TICKS/20));
                if (player instanceof ServerPlayer server) {
                    var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","pale_guide"));
                    if (advancement!=null) server.getAdvancements().award(advancement,"guided");
                }
            } else player.sendOverlayMessage(Component.translatable("message.bunnidogs.cave_sight.denied"));
        } finally {
            try {
                if (!applied && paid) {
                    var actual=player.getItemInHand(hand);
                    if (actual==held && (actual.isEmpty() || ItemStack.isSameItemSameComponents(actual,berry))) {
                        if (actual.isEmpty()) player.setItemInHand(hand,berry); else actual.grow(1);
                    } else if (!player.getInventory().add(berry)) player.drop(berry,false);
                }
            } finally { giving=false; }
        }
        return InteractionResult.SUCCESS;
    }
}
