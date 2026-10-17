package com.bunnidogs.mod.item;

import java.util.function.Consumer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.genetics.Sampling;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A reusable, gentle way to obtain the same Tissue Sample consumed by existing genetics tools. */
public final class SyringeItem extends Item {
    public SyringeItem(Properties properties) { super(properties); }
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Bunnidog dog) || !dog.isAlive()) return InteractionResult.PASS;
        if (dog.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!dog.acceptsCare()) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex.no_care"));
            return InteractionResult.SUCCESS;
        }
        if (!Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.sampling.hurt"));
            return InteractionResult.SUCCESS;
        }
        int wait = dog.samplingWait();
        if (wait > 0) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.sampling.wait",(wait+19)/20));
            return InteractionResult.SUCCESS;
        }
        ItemStack sample = BunnidogCare.sample(dog);
        if (sample.isEmpty()) return InteractionResult.SUCCESS;
        if (!player.getInventory().add(sample)) player.drop(sample,false);
        stack.hurtAndBreak(1,player,hand);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.sampling.taken"));
        return InteractionResult.SUCCESS;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        for (String key : new String[]{"use","health","recovery"}) {
            lines.accept(Component.translatable("tooltip.bunnidogs.syringe." + key).withStyle(ChatFormatting.GRAY));
        }
    }
}
