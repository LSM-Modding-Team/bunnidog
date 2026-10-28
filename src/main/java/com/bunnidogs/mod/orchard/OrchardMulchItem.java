package com.bunnidogs.mod.orchard;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** One purpose cue, with no fertility meter or upkeep instructions. */
public final class OrchardMulchItem extends BlockItem {
    public OrchardMulchItem(Block block, Properties properties) { super(block,properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.orchard.mulch").withStyle(ChatFormatting.GRAY));
    }
}
