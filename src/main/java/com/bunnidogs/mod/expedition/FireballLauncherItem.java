package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FireballLauncherItem extends Item {
    public FireballLauncherItem(Properties properties) {super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var weapon=player.getItemInHand(hand);if(player.isSpectator() || player.getCooldowns().isOnCooldown(weapon))return InteractionResult.FAIL;
        ItemStack ammo=ItemStack.EMPTY;
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(ModItems.VOLATILE_CHARGE.get())) {ammo=player.getInventory().getItem(i);break;}
        if(ammo.isEmpty() && !player.hasInfiniteMaterials())return InteractionResult.FAIL;
        if(level instanceof ServerLevel server) {
            var look=player.getLookAngle();var ball=new net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball(server,player,look,4);
            ball.setPos(player.getEyePosition().add(look.scale(1.3)));ball.setDeltaMovement(look.scale(1.2));
            if(!server.addFreshEntity(ball))return InteractionResult.FAIL;
            ammo.consume(1,player);weapon.hurtAndBreak(1,player,hand);player.getCooldowns().addCooldown(weapon,40);
            server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.GHAST_SHOOT,net.minecraft.sounds.SoundSource.PLAYERS,1,.8F);
        }return InteractionResult.SUCCESS;
    }
}
