package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Whole-item persistent modes, shared costs and one deliberate configuration gesture. */
public final class PowerModes {
    public static int get(ItemStack stack) {return Math.clamp(stack.getOrDefault(ModItems.TOOL_POWER.get(),1),1,5);}
    public static boolean cycle(Level level,Player player,ItemStack stack) {
        if(!player.isShiftKeyDown() || player.isSpectator() || !player.isAlive())return false;
        if(!level.isClientSide()) {int next=get(stack)%5+1;stack.set(ModItems.TOOL_POWER.get(),next);player.sendOverlayMessage(Component.translatable("message.bunnidogs.tool.power",next));}
        return true;
    }
    public static boolean available(Player player,Item item,int count) {
        if(player.hasInfiniteMaterials())return true;
        int found=0;for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(item))found+=player.getInventory().getItem(i).getCount();
        return found>=count;
    }
    public static boolean pay(Player player,Item item,int count) {
        if(!available(player,item,count))return false;if(player.hasInfiniteMaterials())return true;
        for(int i=0;i<player.getInventory().getContainerSize() && count>0;i++) {var stack=player.getInventory().getItem(i);if(stack.is(item)) {int take=Math.min(count,stack.getCount());stack.shrink(take);count-=take;}}
        return true;
    }
    public static void tooltip(ItemStack stack,java.util.function.Consumer<Component> lines) {lines.accept(Component.translatable("tooltip.bunnidogs.tool.power",get(stack)).withStyle(net.minecraft.ChatFormatting.GRAY));}
    private PowerModes() {}
}
