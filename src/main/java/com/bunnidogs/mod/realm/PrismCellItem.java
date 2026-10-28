package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The same real cells fuel the lance and a harness's deliberate climbing burst. */
public final class PrismCellItem extends Item {
    public PrismCellItem(Properties properties) {super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        return boost(level,player,hand);
    }
    public static InteractionResult boost(Level level,Player player,InteractionHand hand) {
        var actual=player.getItemInHand(hand);var harness=player.getItemBySlot(EquipmentSlot.CHEST);
        if(!harness.is(ModItems.APEX_WING_HARNESS.get()) && !harness.is(ModItems.SOVEREIGN_ELYTRA.get()) || !player.isFallFlying() || player.isInWater() || player.isSpectator() || !player.isAlive())return InteractionResult.PASS;
        if(player.getCooldowns().isOnCooldown(new net.minecraft.world.item.ItemStack(ModItems.PRISM_CELL.get())) || player.getCooldowns().isOnCooldown(actual) || harness.getDamageValue()>=harness.getMaxDamage()-5)return InteractionResult.FAIL;
        if(level instanceof ServerLevel server) {
            var cooldownItem=actual.copy();
            if(!com.bunnidogs.mod.expedition.PowerModes.pay(player,ModItems.PRISM_CELL.get(),1))return InteractionResult.FAIL;
            var look=player.getLookAngle();var motion=player.getDeltaMovement().scale(.35).add(look.scale(1.3)).add(0,.65,0);
            player.setDeltaMovement(motion);player.hurtMarked=true;
            player.getCooldowns().addCooldown(new net.minecraft.world.item.ItemStack(ModItems.PRISM_CELL.get()),harness.is(ModItems.SOVEREIGN_ELYTRA.get()) ? 30 : 40);player.getCooldowns().addCooldown(cooldownItem,harness.is(ModItems.SOVEREIGN_ELYTRA.get()) ? 30 : 40);harness.hurtAndBreak(harness.is(ModItems.SOVEREIGN_ELYTRA.get()) ? 3 : 4,player,EquipmentSlot.CHEST);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,player.getX(),player.getY()+.5,player.getZ(),12,.3,.2,.3,.06);
            server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH,net.minecraft.sounds.SoundSource.PLAYERS,.8F,.7F);
        }
        return InteractionResult.SUCCESS;
    }
}
