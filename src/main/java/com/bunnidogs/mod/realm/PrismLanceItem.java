package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.registry.ModItems;
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
    private static ItemStack cell(Player player) {
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(ModItems.PRISM_CELL.get()))return player.getInventory().getItem(i);
        return ItemStack.EMPTY;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return 32;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BOW;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player.isSpectator() || !player.isAlive() || !player.hasInfiniteMaterials() && cell(player).isEmpty())return InteractionResult.FAIL;
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || !player.isAlive() || player.getUseItem()!=stack || player.getItemInHand(player.getUsedItemHand())!=stack || player.getCooldowns().isOnCooldown(stack))return stack;
        var ammo=cell(player);if(!player.hasInfiniteMaterials() && ammo.isEmpty())return stack;
        var start=player.getEyePosition();var end=start.add(player.getLookAngle().scale(32));
        if(!server.hasChunksAt(net.minecraft.core.BlockPos.containing(start).offset(-1,-1,-1),net.minecraft.core.BlockPos.containing(end).offset(1,1,1)))return stack;
        ammo.consume(1,player);
        var key=ResourceKey.create(Registries.DAMAGE_TYPE,Identifier.fromNamespaceAndPath("bunnidogs","prism_lance"));
        var source=new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key),player);
        for(var enemy:server.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(.75),e->e!=player && e.isAlive() && !e.isSpectator()))
            if(enemy.getBoundingBox().inflate(.75).clip(start,end).isPresent())enemy.hurtServer(server,source,24);
        PacketDistributor.sendToPlayersNear(server,null,start.x,start.y,start.z,80,new ApexBeamPayload(start,end,4));
        server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,net.minecraft.sounds.SoundSource.PLAYERS,1,1.5F);
        player.getCooldowns().addCooldown(stack,30);stack.hurtAndBreak(1,player,player.getUsedItemHand());
        return stack;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.prism_lance").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
