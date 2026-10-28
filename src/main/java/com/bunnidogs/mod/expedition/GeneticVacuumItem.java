package com.bunnidogs.mod.expedition;

import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

/** Native bundle interaction/tooltip, with a finite capacity and one real captured animal per cycle. */
public final class GeneticVacuumItem extends BundleItem {
    public GeneticVacuumItem(Properties properties) {super(properties);}
    @Override public boolean overrideStackedOnOther(ItemStack stack,net.minecraft.world.inventory.Slot slot,ClickAction action,Player player) {
        if(!slot.getItem().isEmpty() && !slot.getItem().is(ModItems.SEED.get()))return false;
        return super.overrideStackedOnOther(stack,slot,action,player);
    }
    @Override public boolean overrideOtherStackedOnMe(ItemStack stack,ItemStack other,net.minecraft.world.inventory.Slot slot,ClickAction action,Player player,net.minecraft.world.entity.SlotAccess carried) {
        if(!other.isEmpty() && !other.is(ModItems.SEED.get()))return false;
        return super.overrideOtherStackedOnMe(stack,other,slot,action,player,carried);
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player.isShiftKeyDown())return super.use(level,player,hand);
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public int getUseDuration(ItemStack stack,LivingEntity user) {return user instanceof Player p && p.isShiftKeyDown() ? super.getUseDuration(stack,user) : 20;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.BOW;}
    private static Bunnidog target(ServerLevel server,Player player) {
        return server.getEntitiesOfClass(Bunnidog.class,player.getBoundingBox().inflate(9),dog->!(dog instanceof Apex) && !(dog instanceof com.bunnidogs.mod.guild.WarrenCitizen) && dog.isAlive() && dog.getLeashData()==null && !dog.isPassenger() && !dog.isVehicle()
                && (dog.trail().keeper()==null || dog.trail().belongsTo(player)) && (!dog.hasCustomName() || dog.trail().belongsTo(player)) && dog.carriedBundleCount()==0 && com.bunnidogs.mod.equipment.Barding.tier(dog)<0 && player.hasLineOfSight(dog)
                && dog.getEyePosition().subtract(player.getEyePosition()).normalize().dot(player.getLookAngle())>.94)
                .stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
    }
    @Override public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining) {
        if(user instanceof Player p && p.isShiftKeyDown()) {super.onUseTick(level,user,stack,remaining);return;}
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || remaining%4!=0)return;
        var dog=target(server,player);if(dog==null)return;
        boolean fuel=PowerModes.available(player,ModItems.PRISM_CELL.get(),1);
        if(!fuel)return;
        dog.setDeltaMovement(dog.getDeltaMovement().lerp(player.getEyePosition().add(0,-.5,0).subtract(dog.position()).normalize().scale(.6),.6));dog.hurtMarked=true;
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,dog.getX(),dog.getY()+.5,dog.getZ(),5,.25,.3,.25,.02);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user) {
        if(!(level instanceof ServerLevel server) || !(user instanceof Player player) || player.isSpectator() || player.getUseItem()!=stack || player.getItemInHand(player.getUsedItemHand())!=stack)return stack;
        if(player.isShiftKeyDown())return stack;
        var target=target(server,player);
        if(target==null || stack.getDamageValue()>=stack.getMaxDamage()-1)return stack;
        if(!PowerModes.available(player,ModItems.PRISM_CELL.get(),1))return stack;
        var contents=new BundleContents.Mutable(stack.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY));
        var seed=SeedItem.create(Seed.captured(target.genome(),target.stats()));
        if(contents.tryInsert(seed)!=1)return stack;
        if(!PowerModes.pay(player,ModItems.PRISM_CELL.get(),1))return stack;
        // Extraction, not a kill: no loot, XP, tissue roll or death effects.
        target.releaseHomeForTravel(server);target.discard();stack.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable());
        stack.hurtAndBreak(1,player,player.getUsedItemHand());
        return stack;
    }
}
