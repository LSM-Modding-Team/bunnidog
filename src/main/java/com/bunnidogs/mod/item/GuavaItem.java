package com.bunnidogs.mod.item;

import java.util.function.Consumer;

import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.block.GuavaFruitBlock;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

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

    /** Native ItemStack use captures placement/protection rollback, including the actual hand's cost. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (tier != GuavaTier.PLAIN || context.getClickedFace() != Direction.DOWN) return InteractionResult.PASS;
        var level = context.getLevel(); var pos = context.getClickedPos().below();
        if (!GuavaFruitBlock.canPlant(level,pos)) return InteractionResult.PASS;
        var player = context.getPlayer(); var stack = context.getItemInHand();
        if (player != null && (!player.mayUseItemAt(pos,Direction.DOWN,stack)
                || !level.mayInteract(player,pos) || !level.mayInteract(player,context.getClickedPos()))) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server) {
            if (!GuavaFruitBlock.plant(server,pos)) return InteractionResult.FAIL;
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer,pos,stack);
            level.playSound(null,pos,SoundEvents.CROP_PLANTED,SoundSource.BLOCKS,1,1);
            level.gameEvent(GameEvent.BLOCK_PLACE,pos,GameEvent.Context.of(player,level.getBlockState(pos)));
            stack.consume(1,player);
        }
        return InteractionResult.SUCCESS;
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
        lines.accept(Component.translatable("care.bunnidogs.guava.use").withStyle(ChatFormatting.YELLOW));
        lines.accept(Component.literal("Seed quality " + tier.qualityText()).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Averaged over the guavas of both parents").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) lines.accept(Component.translatable("tooltip.bunnidogs.guava.table").withStyle(ChatFormatting.DARK_GRAY));
        if (tier == GuavaTier.PLAIN) {
            if (flag.isAdvanced()) lines.accept(Component.translatable("tooltip.bunnidogs.guava.plant").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.guava.care").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
