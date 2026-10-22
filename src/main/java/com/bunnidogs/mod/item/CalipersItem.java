package com.bunnidogs.mod.item;

import com.bunnidogs.mod.network.TextComponents;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.function.Consumer;

import com.bunnidogs.mod.block.SeedCropBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.block.NurseryCradleBlock;
import com.bunnidogs.mod.entity.NurseryHatching;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.entity.ResidentFinding;
import com.bunnidogs.mod.entity.ResidentCensus;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * The Calipers show the exact genes and stats of what they touch. Ordinary measurements are read-only;
 * sneak-use with a renamed spare Ear Tag deliberately outlines nearby matches for ten seconds.
 * Measurements (windows since Phase 8 S6 and S8; chat remains the fallback):
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
        var combat=com.bunnidogs.mod.genetics.Combat.profile(dog.genome(),dog.stats());
        player.sendSystemMessage(Component.translatable("care.bunnidogs.combat.bite",String.format(Locale.ROOT,"%.1f",combat.bite()),String.format(Locale.ROOT,"%.1f",combat.interval()/20.0)).withStyle(ChatFormatting.GRAY));
        var nest = dog.homeNest();
        careRow(player,"combat",Component.translatable("care.bunnidogs.combat.intent."+dog.combat().intent()));
        if (dog.trail().keeper()!=null) careRow(player,"trail",Component.translatable("message.bunnidogs.trail.inspect",
                Component.translatable("message.bunnidogs.trail.order."+dog.trail().order().name().toLowerCase(Locale.ROOT)),
                Component.translatable(dog.trail().belongsTo(player) ? "message.bunnidogs.trail.yours" : "message.bunnidogs.trail.other")));
        if (dog.trail().keeper()!=null) careRow(player,"provisions",Component.translatable(dog.trail().provisions()
                ? "message.bunnidogs.trail.provisions_enabled" : "message.bunnidogs.trail.provisions_disabled"));
        careRow(player,"home",nest == null ? Component.translatable("message.bunnidogs.care.home_none")
                : Component.translatable("message.bunnidogs.care.home_at",nest.getX(),nest.getY(),nest.getZ(),
                        String.format(Locale.ROOT,"%.1f",Math.sqrt(dog.blockPosition().distSqr(nest)))));
        careRow(player,"food",dog.diet() == Diet.PLAIN ? Component.translatable("message.bunnidogs.care.food_guavas")
                : Component.translatable("message.bunnidogs.care.food_other",TextComponents.diet(dog.diet())));
        careRow(player,"camouflage",Component.translatable("message.bunnidogs.camouflage."+dog.camouflageStatus().name().toLowerCase(Locale.ROOT),
                TextComponents.t(com.bunnidogs.mod.genetics.Camouflage.cover(dog.genome()).ground())));
        careRow(player,"packs",Component.translatable(dog.carryingSlowdown() > 0 ? "message.bunnidogs.packs.overloaded" : "message.bunnidogs.packs.stock",
                dog.carriedBundleCount(),dog.bundleCapacity(),dog.carryingSlowdown()));
        var meals = dog.troughFeedingStatus();
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
        var products = BunnidogCare.groomingProducts(dog);
        careRow(player, "grooming", dog.isBaby() ? adult
                : products.isEmpty() ? Component.translatable("message.bunnidogs.groom.traits",Grooming.MIN_SENSES,Grooming.MIN_STARLIT_BLOOM,Grooming.MIN_MOSS_BLOOM,Grooming.MIN_FROST_BLOOM)
                : !healthy ? health : dog.groomingWait() > 0 ? recovery(dog.groomingWait())
                : Component.translatable("message.bunnidogs.care.groom_ready",
                        ComponentUtils.formatList(products,p -> Component.translatable(p.translationKey()))));
        careRow(player, "splicing", dog.isBaby() ? adult : !healthy ? health
                : dog.splicingWait() > 0 ? recovery(dog.splicingWait()) : Component.translatable("message.bunnidogs.care.splice_ready"));
        var work = dog.gardeningStatus();
        careRow(player, "gardening", work == Gardening.Status.RESTING ? recovery(dog.gardeningWait())
                : Component.translatable("message.bunnidogs.gardening." + work.name().toLowerCase(Locale.ROOT)));
        careRow(player,"cultivation",Component.translatable(Gardening.cultivates(dog.stats())
                ? "message.bunnidogs.care.cultivation_bonus" : "message.bunnidogs.care.cultivation_requirement",Gardening.MIN_CULTIVATION_BLOOM,Gardening.COMPOST_RESERVE));
        careRow(player,"nursery",Component.translatable(Gardening.nurses(dog.stats())
                ? "message.bunnidogs.care.nursery_bonus" : "message.bunnidogs.care.nursery_requirement",Gardening.MIN_NURSERY_BLOOM,Gardening.COMPOST_RESERVE));
        if (dog.canEat(new ItemStack(Items.CHORUS_FRUIT))) careRow(player,"chorus",Component.translatable(Gardening.tendsChorus(dog.stats())
                ? "message.bunnidogs.care.chorus_bonus" : "message.bunnidogs.care.chorus_requirement",Gardening.MIN_CHORUS_BLOOM));
        careRow(player,"guarding",Component.translatable("message.bunnidogs.guarding."+dog.guardingStatus().name().toLowerCase(Locale.ROOT)));
        careRow(player,"gathering",Component.translatable(com.bunnidogs.mod.genetics.Scavenging.gathers(dog.stats())
                ? "message.bunnidogs.care.gathering_bonus" : "message.bunnidogs.care.gathering_requirement",
                com.bunnidogs.mod.genetics.Scavenging.MIN_GATHERING_SENSES));
        careRow(player,"supplying",Component.translatable(com.bunnidogs.mod.genetics.Supplying.qualified(dog.stats())
                ? "message.bunnidogs.care.supplying_bonus" : "message.bunnidogs.care.supplying_requirement",
                com.bunnidogs.mod.genetics.Supplying.MIN_SENSES,com.bunnidogs.mod.genetics.Supplying.RESERVE));
        var scouting = dog.scoutingStatus();
        careRow(player,"scouting",scouting == com.bunnidogs.mod.genetics.Scouting.Status.RESTING ? recovery(dog.scoutingWait())
                : Component.translatable("message.bunnidogs.scouting."+scouting.name().toLowerCase(Locale.ROOT)));
        if (dog.genome().expressed(com.bunnidogs.mod.genetics.Gene.COAT).name().equals("pale")) {
            var sight=dog.caveSightStatus(player);
            careRow(player,"cave_sight",sight==com.bunnidogs.mod.genetics.CaveSight.Status.RESTING ? recovery(dog.caveSightWait())
                    : Component.translatable("message.bunnidogs.cave_sight."+sight.name().toLowerCase(Locale.ROOT)));
        }
        careRow(player,"recovery_rest",Component.translatable("message.bunnidogs.recovery_rest."+dog.recoveryRestStatus().name().toLowerCase(Locale.ROOT)));
        if (com.bunnidogs.mod.genetics.Adaptations.phasing(dog.genome())) careRow(player,"phasing",dog.isBaby() ? adult
                : dog.beingTransported() || dog.isVehicle() ? Component.translatable("message.bunnidogs.phasing.transported")
                : dog.phaseWait() > 0 ? recovery(dog.phaseWait()) : Component.translatable("message.bunnidogs.phasing.ready"));
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
        if (player.isShiftKeyDown() && held.getItem() instanceof EarTagItem) return ResidentFinding.use(player,hand,held);
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

    /** Reads litter genetics, or inspects a Cradle's loaded incubation/pad while crouching. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && level.getBlockState(context.getClickedPos()).is(Blocks.BELL)) {
            if (level instanceof ServerLevel server) ResidentCensus.show(server,context.getClickedPos(),player);
            return InteractionResult.SUCCESS;
        }
        var block = level.getBlockEntity(context.getClickedPos());
        if (block instanceof com.bunnidogs.mod.block.CommissionBoardBlockEntity board) {
            board.inspect(player); return InteractionResult.SUCCESS;
        }
        if (block instanceof com.bunnidogs.mod.block.PedigreeComparatorBlockEntity) {
            com.bunnidogs.mod.block.PedigreeComparatorBlock.inspect(level,context.getClickedPos(),player);
            return InteractionResult.SUCCESS;
        }
        if (block instanceof com.bunnidogs.mod.block.BreedingPenBlockEntity) {
            com.bunnidogs.mod.block.BreedingPenBlock.inspect(level,context.getClickedPos(),player);
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() && block instanceof com.bunnidogs.mod.block.FeedingTroughBlockEntity) {
            if (level instanceof ServerLevel server) com.bunnidogs.mod.entity.WorkCensus.show(server,context.getClickedPos(),player);
            return InteractionResult.SUCCESS;
        }
        Optional<Seed> planted;
        if (block instanceof SeedCropBlockEntity crop) planted = crop.seed();
        else if (block instanceof NurseryCradleBlockEntity cradle) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide()) showCradle(player,cradle);
                return InteractionResult.SUCCESS;
            }
            planted = cradle.seed();
        }
        else return InteractionResult.PASS;
        if (!level.isClientSide()) {
            Optional<Seed> seed = planted;
            if (seed.isPresent()) {
                showSeed(player, seed.get());
            } else {
                player.sendSystemMessage(TextComponents.t("This crop holds no genes.").withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void showCradle(Player player, NurseryCradleBlockEntity cradle) {
        if (!(player.level() instanceof ServerLevel server)) return;
        var facing = cradle.getBlockState().getValue(NurseryCradleBlock.FACING);
        var check = NurseryHatching.inspect(server,cradle.getBlockPos(),facing);
        player.sendSystemMessage(Component.translatable(cradle.manualHatchOnly() ? "message.bunnidogs.cradle.manual_only" : "message.bunnidogs.cradle.redstone_enabled"));
        var item = cradle.storedSeed();
        player.sendSystemMessage(item.isEmpty() ? Component.translatable("message.bunnidogs.cradle.readout_empty")
                : Component.translatable("message.bunnidogs.cradle.readout",item.getHoverName(),
                    cradle.age(),(cradle.growthWait()+19)/20,cradle.stockSignal()));
        var problem = check.problem();
        String location = problem == null ? "—" : problem.getX()+", "+problem.getY()+", "+problem.getZ();
        player.sendSystemMessage(Component.translatable("message.bunnidogs.cradle.pad."+check.status().name().toLowerCase(Locale.ROOT),check.nearby(),location));
        if (problem != null) {
            if (server.hasChunkAt(problem)) server.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    problem.getX()+.5,problem.getY()+.8,problem.getZ()+.5,2,.1,.1,.1,0);
        } else for (var slot : NurseryHatching.pad(cradle.getBlockPos(),facing)) {
            if (server.hasChunkAt(slot)) server.sendParticles(check.status() == NurseryHatching.Status.READY
                    ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE,
                    slot.getX()+.5,slot.getY()+.15,slot.getZ()+.5,1,0,0,0,0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.calipers.purpose").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.calipers.care").withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced()) {
            lines.accept(TextComponents.t("Right-click a bunnidog: its sheet opens").withStyle(ChatFormatting.GRAY));
            lines.accept(TextComponents.t("Right-click a planted seed or Nursery Cradle: litter forecast").withStyle(ChatFormatting.GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.calipers.cradle").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.calipers.finder").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.calipers.census").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(TextComponents.t("Or hold a seed, sample or occupied Travel Basket in the other hand").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(TextComponents.t("Off hand when you kill: a Tissue Sample").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
