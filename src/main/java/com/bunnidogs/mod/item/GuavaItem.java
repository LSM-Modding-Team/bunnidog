package com.bunnidogs.mod.item;

import java.util.function.Consumer;

import com.bunnidogs.mod.genetics.GuavaTier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * One guava of the ladder (Phase 4e): the plain guava and its golden, diamond, ... versions are all this class with a
 * different {@link GuavaTier}. The tier is a property of the ITEM (not a data component), so every tier has its own
 * name, texture, recipe and tag entry, and nothing has to be read from the stack.
 *
 * <p>All of them are food for bunnidogs through the tag {@code bunnidogs:bunnidog_food}; the tier a bunnidog was fed
 * is remembered until it mates and becomes half of the seed's quality ({@link GuavaTier#average}; the quality of each
 * tier is the table in {@link GuavaTier}, not its position on the ladder).
 */
public class GuavaItem extends Item {
    private final GuavaTier tier;

    public GuavaItem(Properties properties, GuavaTier tier) {
        super(properties);
        this.tier = tier;
    }

    public GuavaTier tier() {
        return tier;
    }

    /**
     * The ladder level of whatever a bunnidog is fed: the level of a guava, 0 for any other food that somebody added to
     * the tag {@code bunnidogs:bunnidog_food} by hand (carrots, a modpack's fruit). The bunnidog remembers the LEVEL;
     * the quality is looked up from the table when the two mate ({@link GuavaTier#average}).
     */
    public static int tierOf(ItemStack stack) {
        return stack.getItem() instanceof GuavaItem guava ? guava.tier().level() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Seed quality " + tier.qualityText()).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Averaged over the guavas of both parents").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.guava.table").withStyle(ChatFormatting.DARK_GRAY));
    }
}
