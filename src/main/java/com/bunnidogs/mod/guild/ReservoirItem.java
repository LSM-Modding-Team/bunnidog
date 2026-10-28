package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.expedition.PowerModes;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

/** The carried component belongs to one nonstackable shell; reloads never pay for overflow. */
public final class ReservoirItem extends Item {
    private final boolean prism;
    public ReservoirItem(Properties p,boolean prism) {super(p);this.prism=prism;}
    public Item ammo() {return prism ? ModItems.PRISM_CELL.get() : ModItems.VOLATILE_CHARGE.get();}
    public int capacity() {return prism ? 512 : 128;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(!player.isShiftKeyDown())return prism ? com.bunnidogs.mod.realm.PrismCellItem.boost(level,player,hand) : InteractionResult.PASS;
        if(level.isClientSide())return InteractionResult.SUCCESS;
        int batch=prism ? 256 : 64,units=GuildProjects.units(stack,ammo());
        if(capacity()-units<batch) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.full"));return InteractionResult.FAIL;}
        var bills=new java.util.LinkedHashMap<Item,Integer>();bills.put(ModItems.CINDERITE_INGOT.get(),18);bills.put(ModItems.STARLIT_DUST.get(),prism ? 36 : 18);
        if(!prism) {bills.put(GuildProjects.material("root_fibre"),18);bills.put(ModItems.VOLATILE_HEART.get(),1);}
        if(bills.entrySet().stream().anyMatch(e->!PowerModes.available(player,e.getKey(),e.getValue()))) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.reload"));return InteractionResult.FAIL;}
        bills.forEach((item,count)->PowerModes.pay(player,item,count));stack.set(ModItems.AMMO_UNITS.get(),units+batch);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.units",units+batch,capacity()));return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay d,java.util.function.Consumer<Component> lines,TooltipFlag f) {
        super.appendHoverText(s,c,d,lines,f);lines.accept(Component.translatable("message.bunnidogs.guild.units",GuildProjects.units(s,ammo()),capacity()));
        lines.accept(Component.translatable(prism ? "tooltip.bunnidogs.prism_battery" : "tooltip.bunnidogs.volatile_magazine"));
    }
}
