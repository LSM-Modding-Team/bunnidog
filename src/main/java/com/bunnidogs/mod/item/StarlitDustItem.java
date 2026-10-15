package com.bunnidogs.mod.item;

import java.util.function.Consumer;

import com.bunnidogs.mod.block.SeedCropBlock;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Starlit Dust (drops D7b, docs/DROPS_SPECS.md section 4): dropped by animals of the starborn line (End), and the one thing in
 * the mod that makes a planted seed crop ripe AT ONCE. Right-click an unripe {@link SeedCropBlock} with it: the crop jumps to its
 * last age (the seed inside stays untouched) and one dust is used up (not in creative mode). A ripe crop, or any other block,
 * passes and nothing is used. One dust, one crop. It is the sweetener for the starborn line's Bone Meal being x0.7.
 */
public class StarlitDustItem extends Item {
    public StarlitDustItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SeedCropBlock) || SeedCropBlock.isRipe(state)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            SeedCropBlock.ripen(level, pos, state);
            Player player = context.getPlayer();
            if (player == null || !player.hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Right-click a planted seed crop: it ripens at once").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("One dust, one crop").withStyle(ChatFormatting.DARK_GRAY));
    }
}
