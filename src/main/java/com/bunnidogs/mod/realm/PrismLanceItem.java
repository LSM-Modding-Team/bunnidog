package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.expedition.PowerModes;
import com.bunnidogs.mod.network.ApexBeamPayload;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

/** A deliberate line-piercing shot, fueled by real cells; interrupted charges cost nothing. */
public final class PrismLanceItem extends Item {
    public PrismLanceItem(Properties properties) {super(properties);}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return stack.is(ModItems.SOVEREIGN_LANCE.get()) ? 24 : 32;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BOW;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(stackMode(level,player,hand))return InteractionResult.SUCCESS;
        if(player.isSpectator() || !player.isAlive() || !PowerModes.available(player,ModItems.PRISM_CELL.get(),player.getItemInHand(hand).is(ModItems.SOVEREIGN_LANCE.get()) ? PowerModes.get(player.getItemInHand(hand)) : 1))return InteractionResult.FAIL;
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    private boolean stackMode(Level level,Player player,InteractionHand hand) {return player.getItemInHand(hand).is(ModItems.SOVEREIGN_LANCE.get()) && PowerModes.cycle(level,player,player.getItemInHand(hand));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || !player.isAlive() || player.getUseItem()!=stack || player.getItemInHand(player.getUsedItemHand())!=stack || player.getCooldowns().isOnCooldown(stack))return stack;
        int power=stack.is(ModItems.SOVEREIGN_LANCE.get()) ? PowerModes.get(stack) : 1;
        if(!PowerModes.available(player,ModItems.PRISM_CELL.get(),power))return stack;
        var start=player.getEyePosition();var end=start.add(player.getLookAngle().scale(stack.is(ModItems.SOVEREIGN_LANCE.get()) ? 48 : 32));
        var bounds=new AABB(start,end).inflate(.75);
        if(!server.hasChunksAt(net.minecraft.core.BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),net.minecraft.core.BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ)))return stack;
        if(!PowerModes.pay(player,ModItems.PRISM_CELL.get(),power))return stack;
        var key=ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","prism_lance"));
        var source=new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key),player);
        for(var enemy:server.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(.75),e->e!=player && e.isAlive() && !e.isSpectator()))
            if(enemy.getBoundingBox().inflate(.75).clip(start,end).isPresent())enemy.hurtServer(server,source,stack.is(ModItems.SOVEREIGN_LANCE.get()) ? 45*power : 64);
        PacketDistributor.sendToPlayersNear(server,null,start.x,start.y,start.z,80,new ApexBeamPayload(start,end,4));
        server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,net.minecraft.sounds.SoundSource.PLAYERS,1,1.5F);
        player.getCooldowns().addCooldown(stack,stack.is(ModItems.SOVEREIGN_LANCE.get()) ? 24 : 30);stack.hurtAndBreak(power,player,player.getUsedItemHand());
        return stack;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        if(stack.is(ModItems.SOVEREIGN_LANCE.get()))PowerModes.tooltip(stack,lines);
        lines.accept(Component.translatable(stack.is(ModItems.SOVEREIGN_LANCE.get()) ? "tooltip.bunnidogs.sovereign_lance" : "tooltip.bunnidogs.prism_lance").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
