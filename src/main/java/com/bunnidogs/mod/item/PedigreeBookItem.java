package com.bunnidogs.mod.item;

import java.util.function.Consumer;

import com.bunnidogs.mod.client.ClientScreens;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * The Pedigree Book (Phase 5c): a notebook of bunnidogs. Right-click a bunnidog to log it (its genome and, if it hatched
 * from a seed, its parents' genomes); right-click the air to open the book (Phase 8 S3: a contents page and one two-page sheet per animal; before S3 it
 * printed a family list in the chat, which {@link Pedigree#lines()} still builds). The log is the {@link Pedigree} text in the data component
 * {@code bunnidogs:pedigree_data}. {@code Bunnidog#mobInteract} calls {@link #log}.
 */
public class PedigreeBookItem extends Item {
    public PedigreeBookItem(Properties properties) {
        super(properties);
    }

    /** The log held by this stack (empty if it has none or it is unreadable). */
    public static Pedigree read(ItemStack stack) {
        return Pedigree.parse(stack.get(ModItems.PEDIGREE_DATA.get()));
    }

    /** Logs one animal into the book and tells the player what happened. Call on the server. */
    public static void log(Player player, ItemStack book, Pedigree.Entry entry) {
        Pedigree log = read(book);
        Pedigree.Added result = log.add(entry);
        String who = Plain.look(entry.genome()) + (entry.stats().isPresent() ? (entry.pup() ? ", pup" : ", adult") : "");
        String message = switch (result) {
            case ADDED -> "Logged as #" + log.size() + (entry.tag().isPresent() ? " (ear tag " + entry.tag().get() + ")" : "") + ": " + who
                    + (entry.mother().isPresent() ? " (parents recorded)." : " (no recorded parents).");
            case UPDATED -> "Already in the book: its entry was brought up to date" + (entry.tag().isPresent() ? " (ear tag " + entry.tag().get() + ")" : "") + ": " + who + ".";
            case ALREADY -> entry.id().isPresent() ? "Already in the book (this very animal)." : "Already in the book (same genes and parents).";
            case FULL -> "The book is full (" + Pedigree.MAX_ENTRIES + " animals).";
        };
        if (result == Pedigree.Added.ADDED || result == Pedigree.Added.UPDATED) {
            book.set(ModItems.PEDIGREE_DATA.get(), log.toString());
        }
        player.sendSystemMessage(Component.literal(message).withStyle(result == Pedigree.Added.ADDED || result == Pedigree.Added.UPDATED ? ChatFormatting.GOLD : ChatFormatting.GRAY));
    }

    /**
     * Right-click in the air: open the book (Phase 8 S3). The window opens on the CLIENT, which already has the book's text
     * (it is a synced data component of the stack); an empty book says so in the chat instead.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Pedigree log = read(player.getItemInHand(hand));
        if (log.size() == 0) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.literal("The book is empty. Right-click a bunnidog to log it.").withStyle(ChatFormatting.GRAY));
            }
        } else if (level.isClientSide()) {
            ClientScreens.openBook(log);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal(read(stack).size() + " of " + Pedigree.MAX_ENTRIES + " animals logged").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Right-click a bunnidog to log it, the air to open").withStyle(ChatFormatting.DARK_GRAY));
    }
}
