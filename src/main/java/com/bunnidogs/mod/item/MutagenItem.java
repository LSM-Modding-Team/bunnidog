package com.bunnidogs.mod.item;

import java.util.function.Consumer;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.block.SeedCropBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Treat a held seed through SeedItem, or use one vial on a saved crop/Cradle without restarting its growth.
 * The saved flag uses the existing mutation floor; the litter is rolled only when it hatches.
 */
public class MutagenItem extends Item {
    public MutagenItem(Properties properties) {
        super(properties);
    }

    /** Shared success-only hand/dispenser operation; mutate only the existing seed data. */
    public static boolean apply(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel) || !level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
        if (level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity cradle) return cradle.applyMutagen();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SeedCropBlock) || !state.canSurvive(level,pos)
                || !(level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop)) return false;
        var seed = crop.seed();
        if (seed.isEmpty() || seed.get().mutagen()) return false;
        crop.setSeed(seed.get().withMutagen()); return true;
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var player = context.getPlayer();
        if (!(level.getBlockEntity(pos) instanceof SeedCropBlockEntity || level.getBlockEntity(pos) instanceof NurseryCradleBlockEntity)
                || player != null && (!player.mayBuild() || !player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()))) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (!apply(level,pos)) return InteractionResult.FAIL;
            var actual = player == null ? context.getItemInHand() : player.getItemInHand(context.getHand());
            if (player == null) actual.shrink(1); else actual.consume(1,player);
            level.levelEvent(2005,pos,15);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Makes the pups of a seed mutate far more often").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Hold a seed and this vial in the other hand, then use the seed").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.mutagen.nursery").withStyle(ChatFormatting.DARK_GRAY));
    }
}
