package com.bunnidogs.mod.orchard;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
/** The same real sapling plants in soil or grafts a bud onto a living guava canopy. */
public final class GuavaSaplingItem extends BlockItem {
    public GuavaSaplingItem(Block block,Properties p){super(block,p);}
    @Override public InteractionResult useOn(UseOnContext context) {
        var world=context.getLevel();var at=context.getClickedPos().below();
        if(context.getClickedFace()!=Direction.DOWN || !GuavaFruitBlock.canPlant(world,at))return super.useOn(context);
        var player=context.getPlayer();if(player!=null && (!player.mayUseItemAt(at,Direction.DOWN,context.getItemInHand()) || !world.mayInteract(player,at) || !world.mayInteract(player,context.getClickedPos())))return InteractionResult.FAIL;
        if(world instanceof ServerLevel server) {
            if(!GuavaFruitBlock.plant(server,at))return InteractionResult.FAIL;
            if(player instanceof net.minecraft.server.level.ServerPlayer p)net.minecraft.advancements.triggers.CriteriaTriggers.PLACED_BLOCK.trigger(p,at,context.getItemInHand());
            world.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE,at,net.minecraft.world.level.gameevent.GameEvent.Context.of(player,world.getBlockState(at)));
            context.getItemInHand().consume(1,player);world.playSound(null,at,net.minecraft.sounds.SoundEvents.CROP_PLANTED,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        }return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,display,lines,flag);lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.bunnidogs.orchard.graft").withStyle(net.minecraft.ChatFormatting.GRAY));}
}
