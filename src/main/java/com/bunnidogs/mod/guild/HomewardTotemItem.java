package com.bunnidogs.mod.guild;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
public final class HomewardTotemItem extends Item {
    public HomewardTotemItem(Properties p){super(p);}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay d,java.util.function.Consumer<Component> lines,TooltipFlag f) {
        super.appendHoverText(s,c,d,lines,f);var home=s.get(ModItems.GUILD_HOME.get());
        lines.accept(Component.translatable("tooltip.bunnidogs.homeward_totem"));
        if(home!=null)lines.accept(Component.literal(LanternTowns.name(home.pos())).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
