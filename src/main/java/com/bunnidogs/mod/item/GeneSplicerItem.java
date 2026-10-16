package com.bunnidogs.mod.item;

import java.util.function.Consumer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Splicing;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class GeneSplicerItem extends Item {
    public GeneSplicerItem(Properties properties) { super(properties); }
    public static Gene selected(ItemStack stack) {
        return Gene.values()[Math.floorMod(stack.getOrDefault(ModItems.SPLICE_GENE.get(),0),Gene.COUNT)];
    }
    private static InteractionResult select(ItemStack stack, Player player) {
        if (!player.level().isClientSide()) {
            Gene gene = Gene.values()[(selected(stack).ordinal()+1)%Gene.COUNT];
            stack.set(ModItems.SPLICE_GENE.get(),gene.ordinal());
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice.selected",Plain.geneName(gene)));
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player.isShiftKeyDown() ? select(player.getItemInHand(hand),player) : InteractionResult.PASS;
    }
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Bunnidog dog) || !dog.isAlive()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return select(stack,player);
        if (dog.level().isClientSide()) return InteractionResult.SUCCESS;
        if (dog.isBaby() || !Sampling.healthy(dog.getHealth(),dog.getMaxHealth())) return refuse(player,"health");
        if (dog.splicingWait() > 0) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice.wait",(dog.splicingWait()+19)/20));
            return InteractionResult.SUCCESS;
        }
        var other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var donorStack = player.getItemInHand(other);
        var donor = TissueSampleItem.read(donorStack);
        if (donor.isEmpty()) return refuse(player,"donor");
        Gene gene = selected(stack);
        var change = Splicing.plan(dog.genome(),donor.get().genome(),gene);
        if (change.isEmpty()) return refuse(player,"carried");
        int mutagen = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.MUTAGEN.get())) { mutagen = i; break; }
        }
        if (mutagen < 0 && !player.hasInfiniteMaterials()) return refuse(player,"mutagen");
        float healthFraction = dog.getHealth()/dog.getMaxHealth();
        dog.setGenome(change.get().genome());
        dog.setHealth(dog.getMaxHealth()*healthFraction);
        dog.beginSplicingRecovery();
        donorStack.consume(1,player);
        if (mutagen >= 0) player.getInventory().getItem(mutagen).consume(1,player);
        stack.hurtAndBreak(1,player,hand);
        dog.playSound(SoundEvents.BOTTLE_EMPTY,.35F,1.2F);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice.done",Plain.geneName(gene),change.get().donor().name().replace('_',' ')));
        return InteractionResult.SUCCESS;
    }
    private static InteractionResult refuse(Player player, String reason) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice." + reason));
        return InteractionResult.SUCCESS;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        lines.accept(Component.translatable("message.bunnidogs.splice.selected",Plain.geneName(selected(stack))).withStyle(ChatFormatting.GOLD));
        for (String key : new String[]{"select","donor","cost","slot","copy"})
            lines.accept(Component.translatable("tooltip.bunnidogs.gene_splicer." + key).withStyle(ChatFormatting.GRAY));
    }
}
