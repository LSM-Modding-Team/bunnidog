package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FireballLauncherItem extends Item {
    public FireballLauncherItem(Properties properties) {super(properties);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> lines,TooltipFlag flag) {super.appendHoverText(stack,context,display,lines,flag);PowerModes.tooltip(stack,lines);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var weapon=player.getItemInHand(hand);if(PowerModes.cycle(level,player,weapon))return InteractionResult.SUCCESS;int power=PowerModes.get(weapon);if(!player.isAlive() || player.isSpectator() || player.getCooldowns().isOnCooldown(weapon))return InteractionResult.FAIL;
        if(!PowerModes.available(player,ModItems.VOLATILE_CHARGE.get(),power))return InteractionResult.FAIL;
        if(level instanceof ServerLevel server) {
            var look=player.getLookAngle();var ball=new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(server,player,look,power*4);
            ball.setPos(player.getEyePosition().add(look.scale(1.3)));ball.setDeltaMovement(look.scale(1.2));
            var expected=weapon.copy();
            if(!server.addFreshEntity(ball))return InteractionResult.FAIL;
            if(player.getItemInHand(hand)!=weapon || !ItemStack.matches(weapon,expected)) {ball.discard();return InteractionResult.FAIL;}
            if(!PowerModes.pay(player,ModItems.VOLATILE_CHARGE.get(),power)) {ball.discard();return InteractionResult.FAIL;}player.getCooldowns().addCooldown(weapon,40);weapon.hurtAndBreak(power,player,hand);
            server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.GHAST_SHOOT,net.minecraft.sounds.SoundSource.PLAYERS,1,.8F);
        }return InteractionResult.SUCCESS;
    }
}
