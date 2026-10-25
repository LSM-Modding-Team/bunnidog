package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class AbyssalFlaskItem extends Item {
    public AbyssalFlaskItem(Properties properties) {super(properties);}
    public static boolean drink(ItemStack item) {return item.is(Items.POTION) || item.getItem() instanceof com.bunnidogs.mod.item.JuiceItem;}
    private String id(ItemStack flask) {
        var id=flask.get(ModItems.FLASK_ID.get());if(id==null) {id=java.util.UUID.randomUUID().toString();flask.set(ModItems.FLASK_ID.get(),id);}return id;
    }
    public void refresh(ItemStack flask,ServerLevel level) {
        var storage=FlaskStorage.get(level);var id=id(flask);flask.set(ModItems.FLASK_COUNT.get(),storage.size(id));
        var top=storage.top(id);if(top.isEmpty())flask.remove(ModItems.FLASK_TOP.get());else flask.set(ModItems.FLASK_TOP.get(),ItemStackTemplate.fromNonEmptyStack(top));
    }
    @Override public boolean overrideOtherStackedOnMe(ItemStack flask,ItemStack other,Slot slot,ClickAction action,Player player,SlotAccess carried) {
        if(!(player.level() instanceof ServerLevel level))return drink(other) || other.isEmpty() && action==ClickAction.SECONDARY;
        if(flask.getCount()!=1 || !slot.allowModification(player))return false;
        var storage=FlaskStorage.get(level);String id=id(flask);
        if(drink(other)) {int count=action==ClickAction.SECONDARY ? 1 : other.getCount();for(int n=0;n<count;n++)storage.push(id,other);other.shrink(count);refresh(flask,level);return true;}
        if(other.isEmpty() && action==ClickAction.SECONDARY) {carried.set(storage.pop(id));refresh(flask,level);return true;}
        return false;
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var flask=player.getItemInHand(hand);
        if(level instanceof ServerLevel server)refresh(flask,server);
        if(flask.getOrDefault(ModItems.FLASK_COUNT.get(),0)==0)return InteractionResult.FAIL;
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return 32;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.DRINK;}
    @Override public ItemStack finishUsingItem(ItemStack flask,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || player.getUseItem()!=flask || player.getItemInHand(player.getUsedItemHand())!=flask)return flask;
        var storage=FlaskStorage.get(server);String id=id(flask);var item=storage.top(id);if(item.isEmpty())return flask;
        // Reserve before effect callbacks, preventing the same entry being drunk twice through reentry.
        if(!player.hasInfiniteMaterials())storage.pop(id);
        var result=item.finishUsingItem(server,player);
        if(!result.isEmpty() && !drink(result))com.bunnidogs.mod.block.StationUse.give(player,result);
        refresh(flask,server);return flask;
    }
    @Override public void inventoryTick(ItemStack stack,ServerLevel level,Entity owner,EquipmentSlot slot) {if(owner.tickCount%40==0)refresh(stack,level);}
    @Override public boolean canFitInsideContainerItems(ItemStack stack) {return false;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.flask.count",stack.getOrDefault(ModItems.FLASK_COUNT.get(),0)));
        var top=stack.get(ModItems.FLASK_TOP.get());
        if(top!=null)lines.accept(Component.translatable("tooltip.bunnidogs.flask.top",top.create().getHoverName()));
    }
}
