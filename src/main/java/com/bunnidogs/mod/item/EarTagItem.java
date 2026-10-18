package com.bunnidogs.mod.item;

import java.util.Optional;
import java.util.Locale;
import java.util.function.Consumer;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.EarTag;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Ear Tag (Phase 5d): clip it onto a bunnidog to give it a permanent registration code ({@code BD-3F7A2C}, see
 * {@link com.bunnidogs.mod.genetics.EarTag}). {@code Bunnidog#mobInteract} calls {@link #apply}. The code is shown above the
 * animal when you look at it (unless it already has a name of its own) and in the Calipers' read-out. One tag per animal:
 * an animal that already wears one keeps it and the tag is not used up.
 */
public class EarTagItem extends Item {
    public EarTagItem(Properties properties) {
        super(properties);
    }

    /** A deliberately renamed spare tag is a reusable cue, not a second registration or a saved animal. */
    public static Optional<String> finderCode(ItemStack stack) {
        if (!(stack.getItem() instanceof EarTagItem)) return Optional.empty();
        var name = stack.get(DataComponents.CUSTOM_NAME);
        return name == null ? Optional.empty() : EarTag.parse(name.getString().strip().toUpperCase(Locale.ROOT));
    }

    /** Tags the bunnidog and tells the player; uses up one tag unless the player has infinite materials. Call on the server. */
    public static void apply(Player player, ItemStack stack, Bunnidog bunnidog) {
        Optional<String> code = bunnidog.applyEarTag();
        if (code.isEmpty()) {
            player.sendSystemMessage(Component.literal("This bunnidog already wears ear tag " + bunnidog.earTag().orElse("?") + ".")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        player.sendSystemMessage(Component.literal("Ear tag " + code.get() + " clipped on.").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Right-click a bunnidog to give it a registration code").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("One tag per animal, for good").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.ear_tag.finder").withStyle(ChatFormatting.DARK_GRAY));
    }
}
