package com.bunnidogs.mod.item;

import java.util.Optional;
import java.util.function.Consumer;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The Tissue Sample (drops D7a, docs/DROPS_SPECS.md 2.5): a vial holding the genes of the bunnidog that dropped it, in the data
 * component {@code bunnidogs:tissue_data} ({@link Tissue#toString()}, the same pattern as the seed and the infused juice). It does
 * nothing by itself: hold it in one hand and the Calipers in the other, right-click the air, and the Calipers read it
 * ({@link CalipersItem#use}). The Gene Splicer consumes it as a donor; a Sampling Syringe can collect it non-lethally.
 */
public class TissueSampleItem extends Item {
    public TissueSampleItem(Properties properties) {
        super(properties);
    }

    /** A stack of one sample holding the genes of this animal. */
    public static ItemStack create(Genome genome, Stats stats) {
        ItemStack stack = new ItemStack(ModItems.TISSUE_SAMPLE.get());
        stack.set(ModItems.TISSUE_DATA.get(), new Tissue(genome, stats).toString());
        return stack;
    }

    /** The payload of this stack, or empty if it is not a sample, has no payload or the payload is malformed. */
    public static Optional<Tissue> read(ItemStack stack) {
        if (!stack.is(ModItems.TISSUE_SAMPLE.get())) {
            return Optional.empty();
        }
        String text = stack.get(ModItems.TISSUE_DATA.get());
        return text == null ? Optional.empty() : Tissue.parse(text);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        Optional<Tissue> tissue = read(stack);
        if (tissue.isPresent()) {
            lines.accept(Component.literal(Plain.look(tissue.get().genome())).withStyle(ChatFormatting.GRAY));
            lines.accept(Component.literal("Code " + tissue.get().genome()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.accept(Component.literal("Holds no genes").withStyle(ChatFormatting.GRAY));
        }
        lines.accept(Component.literal("Hold the Calipers in the other hand to read it").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.tissue.apex_preview").withStyle(ChatFormatting.DARK_GRAY));
    }
}
