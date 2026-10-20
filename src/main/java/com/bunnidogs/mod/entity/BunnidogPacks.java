package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Carrying;
import com.bunnidogs.mod.block.FeedingTroughBlockEntity;
import com.bunnidogs.mod.item.GuavaItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Whole native bundle items: reserve ownership before inventory/drop callbacks; no second container UI. */
final class BunnidogPacks {
    private final Bunnidog dog;
    private final List<ItemStack> bundles = new ArrayList<>(Carrying.MAX_BUNDLES);
    BunnidogPacks(Bunnidog dog) { this.dog = dog; }
    int count() { return this.bundles.size(); }
    List<ItemStack> snapshot() { return this.bundles.stream().map(ItemStack::copy).toList(); }
    /** Reserve one owned ordinary meal before healing callbacks. Preserve remaining order and all native components. */
    ItemStack takeProvision() {
        for (int i=bundles.size()-1;i>=0;i--) {
            var bundle=bundles.get(i); var contents=bundle.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY);
            if (contents.weight().isError()) continue;
            for (int j=0;j<contents.items().size();j++) {
                var template=contents.items().get(j); var food=template.create();
                if (!FeedingTroughBlockEntity.accepts(food) || GuavaItem.tierOf(food)!=0 || !dog.canEat(food)) continue;
                var remaining=new ArrayList<>(contents.items());
                if (food.getCount()==1) remaining.remove(j); else remaining.set(j,template.withCount(food.getCount()-1));
                var next=bundle.copy(); next.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.copyOf(remaining)));
                bundles.set(i,next);
                return food.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }
    Carrying.Status status() {
        if (!this.dog.acceptsCare() || !this.dog.isAlive()) return Carrying.Status.UNAVAILABLE;
        if (this.dog.isBaby()) return Carrying.Status.ADULT;
        if (this.dog.getTarget() != null || this.dog.seekingRefuge() || this.dog.isInLove() || this.dog.hurtTime > 0 || this.dog.isOnFire())
            return Carrying.Status.DANGER;
        if (this.dog.getLastHurtByMob() != null && this.dog.tickCount-this.dog.getLastHurtByMobTimestamp() < Carrying.SETTLE_TICKS)
            return Carrying.Status.DANGER;
        return count() >= Carrying.capacity(this.dog.genome()) ? Carrying.Status.FULL : Carrying.Status.READY;
    }
    InteractionResult equip(Player player, ItemStack actual) {
        if (!(this.dog.level() instanceof ServerLevel)) return InteractionResult.SUCCESS;
        var status = status();
        if (!player.mayBuild()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.packs.protected"));
        else if (status != Carrying.Status.READY)
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.packs."+status.name().toLowerCase(java.util.Locale.ROOT)));
        else if (!actual.isEmpty() && actual.getItem() instanceof BundleItem) {
            var reserved = actual.copyWithCount(1);
            actual.consume(1,player);
            this.bundles.add(reserved); changed();
            this.dog.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EQUIP);
            this.dog.playSound(SoundEvents.BUNDLE_INSERT,.7F,1);
            show(player,false);
        }
        return InteractionResult.SUCCESS;
    }
    InteractionResult emptyHands(Player player) {
        if (!(this.dog.level() instanceof ServerLevel)) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) retrieve(player);
        show(player,true); return InteractionResult.SUCCESS;
    }
    void retrieve(Player player) {
        if (this.dog.level() instanceof ServerLevel && player.mayBuild() && this.dog.acceptsCare() && this.dog.isAlive()
                && player.level()==dog.level() && player.distanceToSqr(dog)<=36 && !this.bundles.isEmpty()) {
            var reserved = this.bundles.removeLast(); changed();
            if (!player.getInventory().add(reserved)) player.drop(reserved,false);
            this.dog.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EQUIP);
            this.dog.playSound(SoundEvents.BUNDLE_REMOVE_ONE,.7F,1);
        }
    }
    void show(Player player, boolean contents) {
        player.sendOverlayMessage(Component.translatable(this.dog.carryingSlowdown() > 0 ? "message.bunnidogs.packs.overloaded" : "message.bunnidogs.packs.stock",
                count(),Carrying.capacity(this.dog.genome()),this.dog.carryingSlowdown()));
        if (contents) for (int i = 0; i < count(); i++)
            player.sendSystemMessage(Component.translatable("message.bunnidogs.packs.bundle",i+1,this.bundles.get(i).getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
    private void changed() { this.dog.syncBundleCount(count()); }
    void dropEquipment(ServerLevel level) {
        var reserved = List.copyOf(this.bundles);
        this.bundles.clear(); changed();
        for (var bundle : reserved) this.dog.spawnAtLocation(level,bundle);
    }
    void save(ValueOutput output) {
        for (int i = 0; i < count(); i++) output.store("Bundle"+i,ItemStack.CODEC,this.bundles.get(i));
    }
    void load(ValueInput input) {
        this.bundles.clear();
        for (int i = 0; i < Carrying.MAX_BUNDLES; i++) input.read("Bundle"+i,ItemStack.CODEC)
                .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof BundleItem)
                .ifPresent(stack -> this.bundles.add(stack.copyWithCount(1)));
        changed();
    }
}
