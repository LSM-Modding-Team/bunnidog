package com.bunnidogs.mod.item;

import com.bunnidogs.mod.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

/** A ground hunt leaves a physical, tradable ingredient; the recipe never consults a player ledger. */
public final class ApexRemnantItem extends Item {
    public ApexRemnantItem(Properties p) {super(p);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flags) {
        super.appendHoverText(stack,context,display,lines,flags);
        if(stack.getOrDefault(ModItems.GROUND_REMNANT.get(),false))lines.accept(Component.translatable("tooltip.bunnidogs.ground_remnant").withStyle(net.minecraft.ChatFormatting.GOLD));
    }
}
