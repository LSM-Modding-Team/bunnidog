package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native Brush entity interaction; all recovery belongs to the animal, not the tool/player. */
final class BunnidogGrooming {
    private BunnidogGrooming() {}
    static InteractionResult use(Bunnidog dog, Player player, ItemStack brush, InteractionHand hand) {
        if (dog.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!dog.acceptsCare() || !dog.isAlive() || dog.isBaby() || !Sampling.healthy(dog.getHealth(), dog.getMaxHealth())) return message(player, "health");
        var products = Grooming.yields(dog.genome(),dog.stats());
        if (products.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom.traits",Grooming.MIN_SENSES,Grooming.MIN_STARLIT_BLOOM));
            return InteractionResult.SUCCESS;
        }
        if (dog.groomingWait() > 0) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom.wait", (dog.groomingWait()+19)/20));
            return InteractionResult.SUCCESS;
        }
        dog.beginGroomingRecovery();
        for (var product : products) {
            var item = switch (product) {
                case WHISKER -> ModItems.WHISKER.get();
                case GLOWSTONE -> Items.GLOWSTONE_DUST;
                case STARLIT -> ModItems.STARLIT_DUST.get();
            };
            ItemStack reward = new ItemStack(item,Grooming.COUNT);
            if (!player.getInventory().add(reward)) player.drop(reward, false);
        }
        brush.hurtAndBreak(1, player, hand);
        dog.playSound(SoundEvents.BRUSH_GENERIC, .4F, 1.1F);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom.collected",
                ComponentUtils.formatList(products,p -> Component.translatable(p.translationKey()))));
        return InteractionResult.SUCCESS;
    }
    private static InteractionResult message(Player player, String key) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.groom." + key));
        return InteractionResult.SUCCESS;
    }
}
