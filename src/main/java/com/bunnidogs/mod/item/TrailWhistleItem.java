package com.bunnidogs.mod.item;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.TrailCompanion;
import java.util.function.Consumer;
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
import net.minecraft.world.level.Level;

public final class TrailWhistleItem extends Item {
    public TrailWhistleItem(Properties properties) { super(properties); }
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand) {
        return target instanceof Bunnidog dog ? dog.trail().use(player,hand) : InteractionResult.PASS;
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) { return TrailCompanion.call(player,hand); }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.trail.training").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.trail.controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}
