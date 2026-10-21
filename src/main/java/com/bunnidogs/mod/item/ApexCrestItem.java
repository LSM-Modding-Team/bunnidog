package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
import java.util.Optional;
import java.util.function.Consumer;
import com.bunnidogs.mod.client.ClientScreens;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Tissue;
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

/** A historical source-line sample, never usable as another ritual or splice donor. */
public final class ApexCrestItem extends Item {
    public ApexCrestItem(Properties properties) { super(properties); }
    public static ItemStack create(Tissue source) {
        var crest = new ItemStack(ModItems.APEX_CREST.get());
        crest.set(ModItems.TISSUE_DATA.get(), source.toString());
        return crest;
    }
    public static Optional<Tissue> read(ItemStack stack) {
        return stack.is(ModItems.APEX_CREST.get()) ? Tissue.parse(stack.get(ModItems.TISSUE_DATA.get())) : Optional.empty();
    }
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) read(player.getItemInHand(hand)).ifPresent(ClientScreens::openCrest);
        return InteractionResult.SUCCESS;
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.apex_crest.source").withStyle(ChatFormatting.GOLD));
        read(stack).ifPresent(tissue -> lines.accept(TextComponents.look(tissue.genome()).withStyle(ChatFormatting.GRAY)));
        lines.accept(Component.translatable("tooltip.bunnidogs.apex_crest.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
