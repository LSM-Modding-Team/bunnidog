package com.bunnidogs.mod.society;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;

/** Deliberate material contribution to an existing natural camp, without creating or claiming a household. */
public final class RootCacheItem extends Item {
    public RootCacheItem(Properties properties) {super(properties);}
    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();var level=context.getLevel();var pos=context.getClickedPos();
        if(player==null || !(level.getBlockEntity(pos) instanceof HearthTreeBlockEntity home) || !home.camp().bare())return InteractionResult.PASS;
        if(!(level instanceof ServerLevel))return InteractionResult.SUCCESS;
        var original=player.getItemInHand(context.getHand());if(!original.is(ModItems.ROOT_CACHE.get()) || !CivicRecipes.ordinary(original) || !player.mayUseItemAt(pos,context.getClickedFace(),original))return InteractionResult.FAIL;
        var exact=original.copy();boolean creative=player.hasInfiniteMaterials();
        boolean installed=home.camp().install(player,()->ItemStack.matches(exact,player.getItemInHand(context.getHand())),
                ()->{if(!creative)original.shrink(1);},()->{if(!creative) {var refund=exact.copyWithCount(1);if(player.getItemInHand(context.getHand()).isEmpty())player.setItemInHand(context.getHand(),refund);else if(!player.getInventory().add(refund))player.drop(refund,false);}});
        return installed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
