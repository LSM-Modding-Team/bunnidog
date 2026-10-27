package com.bunnidogs.mod.equipment;
import net.minecraft.world.item.Item;
/** A complete creature suit, stored in the native BODY equipment slot. */
public final class BardingItem extends Item {
    public final int tier;
    public BardingItem(Properties p,int tier){super(p);this.tier=tier;}
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);var v=Barding.bonuses(tier);
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.bunnidogs.barding.defense",(int)v[0],(int)v[1]));
        lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.bunnidogs.barding.power",(int)Math.round(v[2]*100),(int)Math.round(v[3]*100)));
    }
}
