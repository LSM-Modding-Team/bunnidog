package com.bunnidogs.mod.orchard;
import com.bunnidogs.mod.block.GuavaFruitBlock;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;

/** Eight-block fruit-only reach; the actual first obstruction is authoritative. */
public final class FruitPickerItem extends Item {
    public static final double REACH=8;
    public FruitPickerItem(Properties p){super(p);}
    public InteractionResult harvest(Level world,Player player,InteractionHand hand,BlockPos at,boolean checkEvent) {
        if(!world.hasChunkAt(at) || !world.getBlockState(at).is(ModBlocks.GUAVA_FRUIT.get()))return InteractionResult.PASS;
        if(world.isClientSide())return InteractionResult.SUCCESS;
        var tool=player.getItemInHand(hand);var expected=tool.copy();var before=world.getBlockState(at);
        if(!(tool.getItem() instanceof FruitPickerItem) || player.getCooldowns().isOnCooldown(tool) || !player.mayBuild() || player.isSpectator() || !player.isAlive() || !world.mayInteract(player,at) || !player.mayUseItemAt(at,net.minecraft.core.Direction.UP,tool))return InteractionResult.FAIL;
        if(checkEvent && !Orchard.permit(player,hand,at))return InteractionResult.FAIL;
        var eye=player.getEyePosition();var aim=Vec3.atCenterOf(at);
        if(eye.distanceToSqr(aim)>81)return InteractionResult.FAIL;
        var sight=world.clip(new ClipContext(eye,aim,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
        if(sight.getType()!=HitResult.Type.BLOCK || !sight.getBlockPos().equals(at))return InteractionResult.FAIL;
        if(player.getItemInHand(hand)!=tool || !ItemStack.matches(tool,expected) || !world.getBlockState(at).equals(before) || !player.isAlive() || player.isSpectator() || !player.mayBuild() || !world.mayInteract(player,at) || !player.mayUseItemAt(at,net.minecraft.core.Direction.UP,tool))return InteractionResult.FAIL;
        var harvest=GuavaFruitBlock.take((ServerLevel)world,at);if(!harvest.picked())return InteractionResult.FAIL;
        player.getCooldowns().addCooldown(expected,6);tool.hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        com.bunnidogs.mod.block.StationUse.give(player,harvest.produce());return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult useOn(UseOnContext c){return c.getPlayer()==null ? InteractionResult.PASS : harvest(c.getLevel(),c.getPlayer(),c.getHand(),c.getClickedPos(),false);}
    @Override public InteractionResult use(Level world,Player player,InteractionHand hand) {
        var eye=player.getEyePosition();var end=eye.add(player.getLookAngle().scale(REACH));
        var a=BlockPos.containing(eye);var b=BlockPos.containing(end);
        if(!world.hasChunksAt(new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ())),new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()))))return InteractionResult.PASS;
        var hit=world.clip(new ClipContext(eye,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,player));
        return hit.getType()==HitResult.Type.BLOCK ? harvest(world,player,hand,hit.getBlockPos(),true) : InteractionResult.PASS;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay d,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,TooltipFlag f){super.appendHoverText(stack,c,d,lines,f);lines.accept(net.minecraft.network.chat.Component.translatable("tooltip.bunnidogs.orchard.picker").withStyle(net.minecraft.ChatFormatting.GRAY));}
}
