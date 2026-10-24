package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
import java.util.function.Consumer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.CognitiveGene;
import com.bunnidogs.mod.genetics.Locus;
import com.bunnidogs.mod.genetics.Genome;
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
    private static int selection(ItemStack stack) {
        return Math.floorMod(stack.getOrDefault(ModItems.SPLICE_GENE.get(),0), Gene.COUNT + 2 * CognitiveGene.COUNT);
    }
    public static Locus selected(ItemStack stack) {
        int index = selection(stack);
        return index<7 ? Gene.values()[index] : index==15 ? Gene.WINGS : CognitiveGene.values()[(index-7)/2];
    }
    public static int donorCopy(ItemStack stack) { return Math.max(0, selection(stack) - 7) % 2; }
    private static Component label(ItemStack stack) {
        Locus locus = selected(stack);
        return locus instanceof Gene gene ? TextComponents.gene(gene)
                : TextComponents.t("%s (donor copy %s)", TextComponents.t(locus.displayName()), donorCopy(stack) + 1);
    }
    private static InteractionResult select(ItemStack stack, Player player) {
        if (!player.level().isClientSide()) {
            stack.set(ModItems.SPLICE_GENE.get(), (selection(stack) + 1) % (Gene.COUNT + 2 * CognitiveGene.COUNT));
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice.selected",label(stack)));
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
        if (!dog.acceptsCare()) {
            if (!dog.level().isClientSide()) player.sendOverlayMessage(Component.translatable("message.bunnidogs.apex.no_care"));
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) return select(player.getItemInHand(hand),player);
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
        Locus locus = selected(stack);
        Genome changed;
        Component introduced;
        if (locus instanceof Gene gene) {
            var change = Splicing.plan(dog.genome(),donor.get().genome(),gene);
            if (change.isEmpty()) return refuse(player,"carried");
            changed = change.get().genome(); introduced = TextComponents.allele(change.get().donor());
        } else {
            var change = Splicing.plan(dog.genome(),donor.get().genome(),(CognitiveGene) locus,donorCopy(stack));
            if (change.isEmpty()) return refuse(player,"carried");
            changed = change.get().genome(); introduced = TextComponents.t(change.get().donor().key());
        }
        int mutagen = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.MUTAGEN.get())) { mutagen = i; break; }
        }
        if (mutagen < 0 && !player.hasInfiniteMaterials()) return refuse(player,"mutagen");
        float healthFraction = dog.getHealth()/dog.getMaxHealth();
        dog.setGenome(changed);
        dog.setHealth(dog.getMaxHealth()*healthFraction);
        dog.beginSplicingRecovery();
        donorStack.consume(1,player);
        if (mutagen >= 0) player.getInventory().getItem(mutagen).consume(1,player);
        stack.hurtAndBreak(1,player,hand);
        dog.playSound(SoundEvents.BOTTLE_EMPTY,.35F,1.2F);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.splice.done",label(stack),introduced));
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
        lines.accept(Component.translatable("message.bunnidogs.splice.selected",label(stack)).withStyle(ChatFormatting.GOLD));
        lines.accept(TextComponents.t("Mind genes use both copies. Select donor copy 1 or 2.").withStyle(ChatFormatting.GRAY));
        for (String key : new String[]{"select","donor","cost","slot","copy","preview"})
            lines.accept(Component.translatable("tooltip.bunnidogs.gene_splicer." + key).withStyle(ChatFormatting.GRAY));
    }
}
