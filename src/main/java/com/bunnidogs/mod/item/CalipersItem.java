package com.bunnidogs.mod.item;

import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.function.Consumer;

import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Diet;
import com.bunnidogs.mod.genetics.Foraging;
import com.bunnidogs.mod.genetics.Gardening;
import com.bunnidogs.mod.genetics.Grooming;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Readout;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.network.ModNetwork;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * The Calipers (Phase 5): measure, do not touch. They show the exact genes and stats of what they touch (windows since Phase 8 S6 and S8; the chat only for a non-player and for the "holds no genes" note):
 * <ul>
 *   <li>right-click a bunnidog: its sheet opens in a window (Phase 8 S6: {@code Bunnidog#mobInteract} sends the animal to the
 *       player's client, see {@code network/ModNetwork}); before S6 its genome, stats and drift were printed in the chat
 *       ({@link #send}, still the fallback for a non-player);</li>
 *   <li>a bunnidog seed in the OTHER hand, right-click into the air: the seed's window opens (Phase 8 S8: quality, litter,
 *       mutation chance, drift, then the mother's and the father's sheets) ({@link #use}); either hand may hold the calipers;</li>
 *   <li>(drops D7a) a Tissue Sample in the OTHER hand, right-click into the air: its window opens, one sheet of the animal it
 *       was taken from (S8) ({@link #use});</li>
 *   <li>right-click a planted seed crop: the window of the seed inside it ({@link #useOn}).</li>
 * </ul>
 * The texts come from the pure class {@link Readout}. Messages are sent on the server only; both sides return SUCCESS
 * so the arm swings.
 */
public class CalipersItem extends Item {
    public CalipersItem(Properties properties) {
        super(properties);
    }

    /** Sends the lines to the player's chat: the first one (the headline) gold, the details grey. Call on the server. */
    public static void send(Player player, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            player.sendSystemMessage(Component.literal(lines.get(i))
                    .withStyle(i == 0 ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        }
    }

    /** Server-side care snapshot. No items, timers or animal state are changed. */
    public static void showCare(Player player, Bunnidog dog) {
        player.sendSystemMessage(Component.translatable("message.bunnidogs.care.header", dog.getDisplayName()).withStyle(ChatFormatting.GOLD));
        if (!dog.acceptsCare()) {
            player.sendSystemMessage(Component.translatable("message.bunnidogs.care.unavailable").withStyle(ChatFormatting.GRAY));
            return;
        }
        var nest = dog.homeNest();
        careRow(player,"home",nest == null ? Component.translatable("message.bunnidogs.care.home_none")
                : Component.translatable("message.bunnidogs.care.home_at",nest.getX(),nest.getY(),nest.getZ(),
                        String.format(Locale.ROOT,"%.1f",Math.sqrt(dog.blockPosition().distSqr(nest)))));
        careRow(player,"food",dog.diet() == Diet.PLAIN ? Component.translatable("message.bunnidogs.care.food_guavas")
                : Component.translatable("message.bunnidogs.care.food_other",dog.diet().foods()));
        var meals = dog.foragingStatus();
        Component foodStatus = meals == Foraging.Status.RESTING ? recovery(dog.forageWait())
                : Component.translatable("message.bunnidogs.foraging." + meals.name().toLowerCase(Locale.ROOT));
        if (dog.forageWait() > 0 && meals != Foraging.Status.RESTING)
            foodStatus = Component.translatable("message.bunnidogs.care.status_recovery",foodStatus,recovery(dog.forageWait()));
        careRow(player,"foraging",foodStatus);
        boolean healthy = Sampling.healthy(dog.getHealth(), dog.getMaxHealth());
        Component health = Component.translatable("message.bunnidogs.care.health");
        Component adult = Component.translatable("message.bunnidogs.care.adult");
        careRow(player, "sampling", !healthy ? health : recovery(dog.samplingWait()));
        careRow(player, "fleece", dog.isBaby() ? adult
                : !dog.genome().expressed(Gene.BUILD).name().equals("woolly") ? Component.translatable("message.bunnidogs.care.woolly")
                : !healthy ? health : dog.fleeceWait() > 0 ? recovery(dog.fleeceWait())
                : Component.translatable("message.bunnidogs.care.fleece_ready",ComponentUtils.formatList(dog.fleeceProducts(),
                        product -> Component.translatable("message.bunnidogs.care.fleece_product",product.getCount(),product.getHoverName()))));
        var products = Grooming.yields(dog.genome(),dog.stats());
        careRow(player, "grooming", dog.isBaby() ? adult
                : products.isEmpty() ? Component.translatable("message.bunnidogs.groom.traits",Grooming.MIN_SENSES,Grooming.MIN_STARLIT_BLOOM)
                : !healthy ? health : dog.groomingWait() > 0 ? recovery(dog.groomingWait())
                : Component.translatable("message.bunnidogs.care.groom_ready",
                        ComponentUtils.formatList(products,p -> Component.translatable(p.translationKey()))));
        careRow(player, "splicing", dog.isBaby() ? adult : !healthy ? health
                : dog.splicingWait() > 0 ? recovery(dog.splicingWait()) : Component.translatable("message.bunnidogs.care.splice_ready"));
        var work = dog.gardeningStatus();
        careRow(player, "gardening", work == Gardening.Status.RESTING ? recovery(dog.gardeningWait())
                : Component.translatable("message.bunnidogs.gardening." + work.name().toLowerCase(Locale.ROOT)));
    }

    private static Component recovery(int ticks) {
        if (ticks == 0) return Component.translatable("message.bunnidogs.care.ready");
        int seconds = (ticks + 19) / 20;
        return Component.translatable("message.bunnidogs.care.wait", String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60));
    }

    private static void careRow(Player player, String activity, Component status) {
        player.sendSystemMessage(Component.translatable("message.bunnidogs.care." + activity, status).withStyle(ChatFormatting.GRAY));
    }

    /** Opens the window of a seed on a real player's screen (Phase 8 S8); a non-player gets the chat lines, as before. Call on the server. */
    private static void showSeed(Player player, Seed seed) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.sendSeed(serverPlayer, seed);
        } else {
            send(player, Readout.seed(seed));
        }
    }

    /** Opens the window of a Tissue Sample on a real player's screen (Phase 8 S8); a non-player gets the chat lines. Call on the server. */
    private static void showSample(Player player, Tissue tissue) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.sendSample(serverPlayer, tissue);
        } else {
            send(player, Readout.tissue(tissue));
        }
    }

    /** A seed or a Tissue Sample in the other hand is read; with nothing there the click passes (so the calipers do nothing in the air). */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack held = player.getItemInHand(other);
        Optional<Seed> seed = SeedItem.read(held);
        if (seed.isPresent()) {
            if (!level.isClientSide()) {
                showSeed(player, seed.get());
            }
            return InteractionResult.SUCCESS;
        }
        var carried = TravelBasketItem.readEntry(held);
        if (carried.isPresent()) {
            if (!level.isClientSide()) {
                if (player instanceof ServerPlayer serverPlayer) ModNetwork.sendSheet(serverPlayer,carried.get());
                else send(player,Readout.tissue(new Tissue(carried.get().genome(),carried.get().stats().orElseThrow())));
            }
            return InteractionResult.SUCCESS;
        }
        Optional<Tissue> tissue = TissueSampleItem.read(held);
        if (tissue.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            showSample(player, tissue.get());
        }
        return InteractionResult.SUCCESS;
    }

    /** Reads the seed planted in a seed crop; any other block passes. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(context.getClickedPos()) instanceof SeedCropBlockEntity crop)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Optional<Seed> seed = crop.seed();
            if (seed.isPresent()) {
                showSeed(player, seed.get());
            } else {
                player.sendSystemMessage(Component.literal("This crop holds no genes.").withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.literal("Right-click a bunnidog: its sheet opens").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.calipers.care").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Right-click a planted seed: its window opens").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.literal("Or hold a seed, sample or occupied Travel Basket in the other hand").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.literal("Off hand when you kill: a Tissue Sample").withStyle(ChatFormatting.DARK_GRAY));
    }
}
