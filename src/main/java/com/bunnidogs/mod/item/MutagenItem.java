package com.bunnidogs.mod.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Mutagen (drops D6a, docs/DROPS_SPECS.md section 14). A plain item that does nothing by itself: it is APPLIED to a seed,
 * with the seed in the main hand and the Mutagen in the off hand, by using the seed ({@link SeedItem#use}). A treated seed's pups
 * mutate with at least a 50% chance each ({@link com.bunnidogs.mod.genetics.Seed#MUTAGEN_MUTATION_FLOOR}). This class only
 * exists to carry the tooltip that says so (the same pattern as the hint on the plain guava juice).
 */
public class MutagenItem extends Item {
    public MutagenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Makes the pups of a seed mutate far more often").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Hold a seed and this vial in the other hand, then use the seed").withStyle(ChatFormatting.DARK_GRAY));
    }
}
