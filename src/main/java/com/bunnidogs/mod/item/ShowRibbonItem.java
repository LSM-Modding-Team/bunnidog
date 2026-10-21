package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
import java.util.Optional;
import java.util.function.Consumer;
import com.bunnidogs.mod.client.ClientScreens;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Show;
import com.bunnidogs.mod.genetics.Stat;
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

/** Historical winning animal, using the same persisted snapshot as the Pedigree Book. */
public final class ShowRibbonItem extends Item {
    private final Show.Tier tier;
    public ShowRibbonItem(Properties properties, Show.Tier tier) { super(properties); this.tier = tier; }
    public static ItemStack create(Show.Tier tier, Pedigree.Entry entry) {
        ItemStack ribbon = new ItemStack(switch (tier) {
            case BRONZE -> ModItems.BRONZE_RIBBON.get();
            case SILVER -> ModItems.SILVER_RIBBON.get();
            case GOLD -> ModItems.GOLD_RIBBON.get();
            case NONE -> throw new IllegalArgumentException("No ribbon for an unranked animal");
        });
        ribbon.set(ModItems.PEDIGREE_DATA.get(), entry.toString());
        return ribbon;
    }
    public static Optional<Pedigree.Entry> read(ItemStack stack) {
        return stack.getItem() instanceof ShowRibbonItem ? Pedigree.parseEntry(stack.get(ModItems.PEDIGREE_DATA.get())) : Optional.empty();
    }
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) read(player.getItemInHand(hand)).ifPresent(entry ->
                ClientScreens.openRibbon(entry, Component.translatable("item.bunnidogs." + this.tier.key() + "_show_ribbon")));
        return InteractionResult.SUCCESS;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.ribbon.requirement", this.tier.minimum()).withStyle(ChatFormatting.GOLD));
        var winner = read(stack);
        if (winner.isEmpty()) lines.accept(Component.translatable("tooltip.bunnidogs.ribbon.blank").withStyle(ChatFormatting.GRAY));
        winner.ifPresent(entry -> {
            lines.accept(entry.tag().<net.minecraft.network.chat.MutableComponent>map(Component::literal).orElseGet(() -> TextComponents.look(entry.genome())).withStyle(ChatFormatting.GRAY));
            entry.stats().ifPresent(stats -> {
                for (Stat stat : Stat.values()) lines.accept(TextComponents.t("%s: %s", TextComponents.stat(stat), stats.get(stat)).withStyle(ChatFormatting.GRAY));
            });
        });
        lines.accept(Component.translatable("tooltip.bunnidogs.ribbon.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
