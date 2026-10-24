package com.bunnidogs.mod.item;

import java.util.function.Consumer;
import com.bunnidogs.mod.entity.ApexKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ApexSigilItem extends Item {
    public final ApexKind kind;
    public final boolean flying;
    public ApexSigilItem(Properties properties,ApexKind kind,boolean flying) { super(properties); this.kind=kind; this.flying=flying; }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag); lines.accept(Component.translatable("tooltip.bunnidogs.apex_sigil"));
    }
}
