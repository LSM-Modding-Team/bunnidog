package com.bunnidogs.mod.item;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import com.bunnidogs.mod.block.SeedCropBlock;
import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Infusion;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * The bunnidog seed (Phase 4c): an item whose only state is the {@link Seed} in its {@code bunnidogs:seed_data}
 * component. Right-clicking the top of farmland plants it as a {@link com.bunnidogs.mod.block.SeedCropBlock} (Phase 4d);
 * the litter is born when the ripe crop is harvested ({@link com.bunnidogs.mod.block.SeedCropHarvest}). The debug commands
 * {@code /bunnidogs hatch} (hatch the seed in your hand at once) and {@code /bunnidogs seed} (read it) still work.
 *
 * <p>The tooltip is a stand-in for the Calipers of Phase 5: a short readable summary always, and the exact parents
 * (genomes and stat points) with advanced tooltips (F3+H).
 */
public class SeedItem extends Item {
    public SeedItem(Properties properties) {
        super(properties);
    }

    /** A stack of one seed holding this payload. */
    public static ItemStack create(Seed seed) {
        ItemStack stack = new ItemStack(ModItems.SEED.get());
        stack.set(ModItems.SEED_DATA.get(), seed.toString());
        return stack;
    }

    /** The payload of this stack, or empty if it is not a seed, has no payload or the payload is malformed. */
    public static Optional<Seed> read(ItemStack stack) {
        if (!stack.is(ModItems.SEED.get())) {
            return Optional.empty();
        }
        String text = stack.get(ModItems.SEED_DATA.get());
        return text == null ? Optional.empty() : Seed.parse(text);
    }

    /**
     * Plants the seed: only on the TOP of farmland with air above, and only if the seed holds genes. The payload is copied
     * into the crop's block entity and one seed is used up (not in creative mode). Both sides return SUCCESS so the arm
     * swings; the block is only placed on the server.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos soil = context.getClickedPos();
        BlockPos above = soil.above();
        Optional<Seed> seed = read(context.getItemInHand());
        if (seed.isEmpty() || context.getClickedFace() != Direction.UP
                || !level.getBlockState(soil).is(Blocks.FARMLAND) || !level.getBlockState(above).isAir()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (!(level instanceof ServerLevel server) || !SeedCropBlock.plant(server,above,context.getItemInHand())) return InteractionResult.FAIL;
            Player player = context.getPlayer();
            if (player == null || !player.hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Infusion: with a seed in the main hand and uninfused guava juice in the OFF hand, using the seed (a right-click
     * that planting did not take: not on farmland) rolls ONE phantom pup from the seed ({@link Infusion#roll}), uses up the
     * seed and the juice, and hands over a bottle of infused juice that holds it. Anything else passes. Only uninfused juice is
     * accepted as a carrier (an infused bottle cannot be infused again). Rich carriers lift effect quality, not inherited
     * stat drift. The Juicer offers the same paid random roll for automated processing.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack seedStack = player.getItemInHand(hand);
        ItemStack juiceStack = player.getItemInHand(InteractionHand.OFF_HAND);
        Optional<Seed> seed = read(seedStack);
        if (hand == InteractionHand.MAIN_HAND && seed.isPresent() && juiceStack.is(ModItems.MUTAGEN.get())) {
            return applyMutagen(level, player, hand, seedStack, juiceStack, seed.get());
        }
        var carrier = JuiceItem.carrier(juiceStack);
        if (hand != InteractionHand.MAIN_HAND || seed.isEmpty() || carrier.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Infusion phantom = Infusion.roll(seed.get(), carrier.get(), new Random(level.getRandom().nextLong()));
            ItemStack bottle = JuiceItem.create(phantom);
            if (player.hasInfiniteMaterials()) {
                giveOrDrop(player, bottle);
            } else {
                juiceStack.shrink(1);
                if (seedStack.getCount() <= 1) {
                    // Session 12 fix: a bred seed is a stack of ONE. Shrinking it empties the held slot, which is then the first free
                    // slot, so the bottle used to land in the very slot that vanilla clears afterwards: the seed and the juice were
                    // gone and no bottle appeared. The bottle takes the seed's place in the hand instead.
                    player.setItemInHand(hand, bottle);
                } else {
                    seedStack.shrink(1);
                    giveOrDrop(player, bottle);
                }
            }
            BunnidogsMod.LOGGER.info("Infused: {}", phantom);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The Mutagen (drops D6a): treats the seed in the main hand with the Mutagen in the off hand. The result is the SAME seed with the extra
     * text part ({@link Seed#withMutagen}), one Mutagen used up. A seed already treated refuses (nothing is used up) and says so. The held
     * stack is handled like the infusion above: a stack of ONE seed is replaced in the hand, a bigger stack gives up one seed and the
     * treated one goes to the inventory; in creative nothing is used up and the treated seed is handed over as an extra.
     */
    private static InteractionResult applyMutagen(Level level, Player player, InteractionHand hand, ItemStack seedStack,
            ItemStack mutagenStack, Seed seed) {
        if (!level.isClientSide()) {
            if (seed.mutagen()) {
                player.sendSystemMessage(Component.literal("This seed already carries a Mutagen.").withStyle(ChatFormatting.GRAY));
                return InteractionResult.SUCCESS;
            }
            ItemStack treated = seedStack.copyWithCount(1);
            treated.set(ModItems.SEED_DATA.get(),seed.withMutagen().toString());
            if (player.hasInfiniteMaterials()) {
                giveOrDrop(player, treated);
            } else {
                mutagenStack.shrink(1);
                if (seedStack.getCount() <= 1) {
                    player.setItemInHand(hand, treated);
                } else {
                    seedStack.shrink(1);
                    giveOrDrop(player, treated);
                }
            }
            BunnidogsMod.LOGGER.info("Mutagen applied: {}", seed.withMutagen());
        }
        return InteractionResult.SUCCESS;
    }

    /** Puts the item into the inventory, or drops it at the player's feet if there is no room. */
    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        Optional<Seed> seed = read(stack);
        if (seed.isEmpty()) {
            lines.accept(Component.literal("Empty: holds no genes").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (String line : seed.get().summary()) {
            lines.accept(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        lines.accept(Component.translatable("tooltip.bunnidogs.seed.quality_band",com.bunnidogs.mod.genetics.SeedQuality.band(seed.get().quality()).displayName())
                .withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.seed.dispenser").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) {
            lines.accept(Component.literal("Mother " + seed.get().mother() + " / " + seed.get().motherStats())
                    .withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(Component.literal("Father " + seed.get().father() + " / " + seed.get().fatherStats())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
