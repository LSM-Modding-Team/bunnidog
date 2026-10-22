package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
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
import net.minecraft.world.item.context.UseOnContext;
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
        var book = Pedigree.parse(stack.get(ModItems.PEDIGREE_DATA.get()));
        book.setLineageNames(com.bunnidogs.mod.genetics.LineageNames.parse(stack.get(ModItems.LINEAGE_NAMES.get())));
        return book;
    }

    /** Logs one animal into the book and tells the player what happened. Call on the server. */
    public static void log(Player player, ItemStack book, Pedigree.Entry entry) {
        if (protectFutureRecords(player, book)) return;
        Pedigree log = read(book);
        Pedigree.Added result = log.add(entry);
        Component who = TextComponents.look(entry.genome());
        if (entry.stats().isPresent()) who = TextComponents.t("%s, %s",who,TextComponents.t(entry.pup() ? "pup" : "adult"));
        Component tag = entry.tag().<Component>map(value -> TextComponents.t(" (ear tag %s)",value)).orElseGet(Component::empty);
        var message = switch (result) {
            case ADDED -> TextComponents.t("Logged as #%s%s: %s%s",log.size(),tag,who,
                    TextComponents.t(entry.mother().isPresent() ? " (parents recorded)." : " (no recorded parents)."));
            case UPDATED -> TextComponents.t("Already in the book: its entry was brought up to date%s: %s.",tag,who);
            case ALREADY -> TextComponents.t(entry.id().isPresent() ? "Already in the book (this very animal)." : "Already in the book (same genes and parents).");
            case FULL -> TextComponents.t("The book is full (%s animals).",Pedigree.MAX_ENTRIES);
        };
        if (result == Pedigree.Added.ADDED || result == Pedigree.Added.UPDATED) {
            book.set(ModItems.PEDIGREE_DATA.get(), log.toString());
        }
        DiscoveryProgress.observe(player,log);
        player.sendSystemMessage(message.withStyle(result == Pedigree.Added.ADDED || result == Pedigree.Added.UPDATED ? ChatFormatting.GOLD : ChatFormatting.GRAY));
    }

    static boolean protectFutureRecords(Player player, ItemStack stack) {
        if (!com.bunnidogs.mod.genetics.Genome.containsUnsupportedSchema(stack.get(ModItems.PEDIGREE_DATA.get()))
                && !com.bunnidogs.mod.genetics.Genome.containsUnsupportedSchema(stack.get(ModItems.LINEAGE_NAMES.get()))) return false;
        player.sendOverlayMessage(TextComponents.t("This book contains a newer genome format. Its records have been preserved; update Bunnidogs to edit them."));
        return true;
    }

    /**
     * Right-click in the air: open the book (Phase 8 S3). The window opens on the CLIENT, which already has the book's text
     * (it is a synced data component of the stack); an empty book says so in the chat instead.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (recordCarried(player,hand)) return InteractionResult.SUCCESS;
        Pedigree log = read(player.getItemInHand(hand));
        DiscoveryProgress.observe(player,log);
        if (log.size() == 0) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(TextComponents.t("The book is empty. Right-click a bunnidog to log it.").withStyle(ChatFormatting.GRAY));
            }
        } else if (level.isClientSide()) {
            ClientScreens.openBook(log,hand,player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }
    private boolean recordCarried(Player player, InteractionHand hand) {
        return TravelBasketItem.recordCarried(player,hand,entry -> log(player,player.getItemInHand(hand),entry));
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && recordCarried(context.getPlayer(),context.getHand())
                ? InteractionResult.SUCCESS : super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        var log=read(stack);
        lines.accept(TextComponents.t("%s of %s animals logged", log.size(), Pedigree.MAX_ENTRIES).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.records.wild",com.bunnidogs.mod.genetics.GeneticDiscoveries.wildLineages(log).size(),
                com.bunnidogs.mod.genetics.GeneticDiscoveries.wildLineageCount()).withStyle(ChatFormatting.DARK_GREEN));
        lines.accept(TextComponents.t("Right-click a bunnidog to log it, the air to open").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) lines.accept(Component.translatable("tooltip.bunnidogs.records.carried").withStyle(ChatFormatting.DARK_GRAY));
    }
}
