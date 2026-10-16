package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Native Brush entity interaction; all recovery belongs to the animal, not the tool/player. */
final class BunnidogGrooming {
    private BunnidogGrooming() {}
    static InteractionResult use(Bunnidog dog, Player player, ItemStack brush, InteractionHand hand) {
        if (dog.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!dog.isAlive() || dog.isBaby() || !Sampling.healthy(dog.getHealth(), dog.getMaxHealth())) return message(player, "health");
        if (!Grooming.developed(dog.stats())) return message(player, "senses");
        if (dog.groomingWait() > 0) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom.wait", (dog.groomingWait()+19)/20));
            return InteractionResult.SUCCESS;
        }
        dog.beginGroomingRecovery();
        ItemStack whisker = new ItemStack(ModItems.WHISKER.get(), Grooming.COUNT);
        if (!player.getInventory().add(whisker)) player.drop(whisker, false);
        brush.hurtAndBreak(1, player, hand);
        dog.playSound(SoundEvents.BRUSH_GENERIC, .4F, 1.1F);
        return message(player, "done");
    }
    private static InteractionResult message(Player player, String key) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom." + key));
        return InteractionResult.SUCCESS;
    }
}
