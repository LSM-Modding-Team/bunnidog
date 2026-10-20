package com.bunnidogs.mod.block;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Commissions;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModVillagers;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** Selection is saved on the board; progress and one-time payouts live in native player advancements. */
public final class CommissionBoardBlockEntity extends BlockEntity {
    private Commissions.Request request=Commissions.Request.NIGHT_GUIDE;
    private boolean submitting;
    public record Reading(Commissions.Status status,Bunnidog candidate) {}
    public CommissionBoardBlockEntity(BlockPos pos,BlockState state) { super(ModBlocks.COMMISSION_BOARD_BE.get(),pos,state); }
    public Commissions.Request request() { return request; }
    private boolean live() {
        return level instanceof ServerLevel && level.getBlockEntity(worldPosition)==this && getBlockState().is(ModBlocks.COMMISSION_BOARD.get());
    }
    public void cycle() {
        select(request.next());
    }
    public boolean select(Commissions.Request selected) {
        if (!live() || submitting || selected==request) return false;
        request=selected; setChanged(); refresh(); return true;
    }
    public boolean completed(Player player,Commissions.Request selected) {
        if (!(player instanceof ServerPlayer server)) return false;
        var advancement=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","commissions/"+selected.key()));
        return advancement!=null && server.getAdvancements().getOrStartProgress(advancement).isDone();
    }
    public int completedCount(Player player) {
        int total=0; for (var choice:Commissions.Request.values()) if (completed(player,choice)) total++; return total;
    }
    public Reading read() {
        if (!live()) return new Reading(Commissions.Status.UNAVAILABLE,null);
        var front=worldPosition.relative(getBlockState().getValue(CommissionBoardBlock.FACING)); var pad=new AABB(front);
        if (!level.hasChunkAt(front) || !level.hasChunkAt(front.below()) || !level.getWorldBorder().isWithinBounds(pad)
                || !level.getBlockState(front).isAir() || !level.getBlockState(front.above()).isAir()
                || !level.getFluidState(front).isEmpty() || !level.getBlockState(front.below()).isFaceSturdy(level,front.below(),Direction.UP))
            return new Reading(Commissions.Status.BLOCKED,null);
        var candidates=level.getEntitiesOfClass(Bunnidog.class,pad,dog->dog.isAlive() && pad.contains(dog.position()));
        if (candidates.isEmpty()) return new Reading(Commissions.Status.ABSENT,null);
        if (candidates.size()!=1) return new Reading(Commissions.Status.CROWDED,null);
        var dog=candidates.getFirst();
        if (!dog.acceptsCare()) return new Reading(Commissions.Status.UNAVAILABLE,dog);
        if (dog.isBaby()) return new Reading(Commissions.Status.ADULT,dog);
        if (dog.motherGenome().isEmpty() || dog.fatherGenome().isEmpty()) return new Reading(Commissions.Status.PARENTS,dog);
        if (!Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) return new Reading(Commissions.Status.HEALTH,dog);
        if (dog.isPassenger() || dog.isVehicle()) return new Reading(Commissions.Status.TRANSPORTED,dog);
        if (dog.getTarget()!=null || dog.seekingRefuge() || dog.isInLove()
                || dog.getLastHurtByMob()!=null && dog.tickCount-dog.getLastHurtByMobTimestamp()<100)
            return new Reading(Commissions.Status.DANGER,dog);
        boolean breeder=level.getEntitiesOfClass(Villager.class,new AABB(worldPosition).inflate(Commissions.BREEDER_RANGE),v->v.isAlive() && !v.isBaby()
                && v.getVillagerData().profession().is(ModVillagers.BREEDER.getKey())
                && v.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=Commissions.BREEDER_RANGE*Commissions.BREEDER_RANGE).size()>0;
        return new Reading(breeder ? request.match(dog.genome(),dog.stats(),dog.carriedBundleCount()) : Commissions.Status.BREEDER,dog);
    }
    public void refresh() {
        if (!live()) return;
        var state=getBlockState(); boolean ready=read().status()==Commissions.Status.READY;
        if (state.getValue(CommissionBoardBlock.READY)!=ready) {
            level.setBlock(worldPosition,state.setValue(CommissionBoardBlock.READY,ready),3);
            if (live()) level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());
        }
    }
    public boolean submit(Player player,InteractionHand hand) {
        return submit(player,player.getItemInHand(hand),hand);
    }
    public boolean submitFromInventory(Player player) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) return submit(player,stack,null);
        }
        return false;
    }
    private boolean submit(Player player,ItemStack paper,InteractionHand hand) {
        if (!(player instanceof ServerPlayer server) || !player.mayBuild() || !live() || player.level()!=level
                || player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>36) return false;
        if (submitting) { message(player,Commissions.Status.BUSY); return false; }
        if (!paper.is(Items.PAPER)) return false;
        var selected=request;
        var advancement=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","commissions/"+selected.key()));
        if (advancement==null) { message(player,Commissions.Status.UNAVAILABLE); return false; }
        if (server.getAdvancements().getOrStartProgress(advancement).isDone()) { message(player,Commissions.Status.ALREADY); return false; }
        var reading=read();
        if (reading.status()!=Commissions.Status.READY) { inspect(player); return false; }
        boolean paid=!player.hasInfiniteMaterials(), awarded=false; var receipt=paper.copyWithCount(1);
        submitting=true;
        try {
            if (paid) paper.shrink(1);
            // Native progress is committed before its reward/advancement callbacks. Reentry cannot claim it again.
            awarded=server.getAdvancements().award(advancement,"submitted");
            if (awarded) player.sendOverlayMessage(Component.translatable("message.bunnidogs.commission.completed",title(selected)));
            return awarded;
        } finally {
            try {
                if (!awarded && paid) {
                    var actual=hand==null ? ItemStack.EMPTY : player.getItemInHand(hand);
                    if (hand!=null && actual==paper && (actual.isEmpty() || ItemStack.isSameItemSameComponents(actual,receipt))) {
                        if (actual.isEmpty()) player.setItemInHand(hand,receipt); else actual.grow(1);
                    } else if (!player.getInventory().add(receipt)) player.drop(receipt,false);
                }
            } finally { submitting=false; }
        }
    }
    private static Component title(Commissions.Request selected) { return Component.translatable("commission.bunnidogs."+selected.key()+".title"); }
    private void message(Player player,Commissions.Status status) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.commission.status",title(request),
                Component.translatable("message.bunnidogs.commission."+status.name().toLowerCase(Locale.ROOT))));
    }
    public void inspect(Player player) {
        if (!live()) return;
        var status=read().status();
        if (player instanceof ServerPlayer server) {
            var advancement=server.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","commissions/"+request.key()));
            if (advancement!=null && server.getAdvancements().getOrStartProgress(advancement).isDone()) status=Commissions.Status.ALREADY;
        }
        message(player,status);
        player.sendSystemMessage(Component.translatable("commission.bunnidogs."+request.key()+".requirements").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("commission.bunnidogs."+request.key()+".reward").withStyle(ChatFormatting.DARK_GREEN));
    }
    @Override public void onLoad() {
        super.onLoad(); if (live()) level.scheduleTick(worldPosition,getBlockState().getBlock(),1);
    }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); output.putString("Request",request.key()); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); request=Commissions.Request.parse(input.getStringOr("Request","")); submitting=false; }
}
