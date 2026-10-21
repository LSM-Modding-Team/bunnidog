package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
import java.util.Optional;
import java.util.function.Consumer;

import com.bunnidogs.mod.genetics.EarTag;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Worn Ear Tag (drops D8e, docs/DROPS_SPECS.md section 7): what a tagged bunnidog leaves behind when it dies. A keepsake with
 * the registration code the animal wore ({@code BD-3F7A2C}), kept in the data component {@code bunnidogs:worn_tag_code} (the same
 * string pattern as the seed and the Tissue Sample). It does nothing by itself and is NOT an {@link EarTagItem}: it cannot be clipped
 * onto another animal. Its use is the Trophy Pedestal, where it stands on show.
 */
public class WornEarTagItem extends Item {
    public WornEarTagItem(Properties properties) {
        super(properties);
    }

    /** A stack of one worn tag bearing this code. */
    public static ItemStack create(String code) {
        ItemStack stack = new ItemStack(ModItems.WORN_EAR_TAG.get());
        stack.set(ModItems.WORN_TAG_CODE.get(), code);
        return stack;
    }

    /** The code on this stack, or empty if it is not a worn tag, has no code or the code is malformed. */
    public static Optional<String> read(ItemStack stack) {
        if (!stack.is(ModItems.WORN_EAR_TAG.get())) {
            return Optional.empty();
        }
        String text = stack.get(ModItems.WORN_TAG_CODE.get());
        return text == null ? Optional.empty() : EarTag.parse(text);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        Optional<String> code = read(stack);
        if (code.isPresent()) {
            lines.accept(Component.literal(code.get()).withStyle(ChatFormatting.GOLD));
        } else {
            lines.accept(TextComponents.t("Blank and scuffed").withStyle(ChatFormatting.GRAY));
        }
        lines.accept(TextComponents.t("Worn by a bunnidog to the end").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(TextComponents.t("A keepsake: show it on a Trophy Pedestal").withStyle(ChatFormatting.DARK_GRAY));
    }
}
