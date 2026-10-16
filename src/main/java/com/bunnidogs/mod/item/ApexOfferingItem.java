package com.bunnidogs.mod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** The legendary drops explain their endgame use where players discover them. */
public final class ApexOfferingItem extends Item {
    public ApexOfferingItem(Properties properties) { super(properties); }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.apex_offering.use").withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.bunnidogs.apex_offering.pedestal").withStyle(ChatFormatting.GRAY));
    }
}
