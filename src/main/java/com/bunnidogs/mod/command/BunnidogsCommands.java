package com.bunnidogs.mod.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Founders;
import com.bunnidogs.mod.genetics.Drift;
import com.bunnidogs.mod.genetics.Drops;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Growth;
import com.bunnidogs.mod.genetics.Juice;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.registry.ModBiomeTags;
import com.bunnidogs.mod.worldgen.WarrenFeature;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Debug commands. There is no in-game tool for reading genes yet (Calipers / Pedigree Book come in Phase 5), so
 * {@code /bunnidogs genome} prints the genes and the resulting stats of the nearest bunnidogs, and
 * {@code /bunnidogs seed} does the same for the seed in your hand. {@code /bunnidogs hatch} hatches that seed at your
 * feet: a stand-in for the seed crop of step 4d (Phase 4c drops seeds instead of pups, so without it nothing could be born).
 */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class BunnidogsCommands {
    private static final double RADIUS = 12.0D;
    private static final int MAX_SHOWN = 5;
    private static final int MAX_SIMULATED_KILLS = 1000;

    private BunnidogsCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bunnidogs")
                .then(Commands.literal("genome").executes(BunnidogsCommands::genome))
                .then(Commands.literal("lineage").executes(BunnidogsCommands::lineage))
                .then(Commands.literal("seed").executes(BunnidogsCommands::seed))
                .then(Commands.literal("warren")
                        .executes(context -> warren(context, 0, false))
                        .then(Commands.literal("surface")
                                .executes(context -> warren(context, 0, true)))
                        .then(Commands.argument("level", IntegerArgumentType.integer(Settlement.MIN_LEVEL, Settlement.MAX_LEVEL))
                                .executes(context -> warren(context, IntegerArgumentType.getInteger(context, "level"), false))
                                .then(Commands.literal("surface")
                                        .executes(context -> warren(context, IntegerArgumentType.getInteger(context, "level"), true)))))
                .then(Commands.literal("drops")
                        .executes(context -> drops(context, 0))
                        .then(Commands.literal("simulate")
                                .then(Commands.argument("kills", IntegerArgumentType.integer(1, MAX_SIMULATED_KILLS))
                                        .executes(context -> drops(context, IntegerArgumentType.getInteger(context, "kills"))))))
                .then(Commands.literal("hatch")
                        .executes(context -> hatch(context, 0.0D))
                        .then(Commands.argument("bonus", DoubleArgumentType.doubleArg(0.0D))
                                .executes(context -> hatch(context, DoubleArgumentType.getDouble(context, "bonus"))))));
    }

    /**
     * Debug (Phase 4g, level argument 6a, surface 6b-fix): builds a settlement (level = number of guava trees; 0 = rolled) at your feet, as the
     * world generator would. With {@code surface} it is the surface kind, exactly as natural generation builds it on the ground (your feet
     * must be on open ground; the terrain around is followed up and down); without it the cave kind, which also works inside a cave.
     */
    private static int warren(CommandContext<CommandSourceStack> context, int level, boolean surface) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        WarrenFeature.Built built = WarrenFeature.buildSettlement(source.getLevel(), player.blockPosition(), source.getLevel().getRandom(), surface, level);
        if (built.trees() == 0) {
            source.sendFailure(Component.literal("No room for a warren here (it needs two air blocks above solid ground and space for the trunk). Try a flat open spot"
                    + (surface ? "; the surface kind also needs you to stand on the ground itself, not on a ledge over a cave." : ".")));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Built a settlement with " + built.trees() + (built.trees() == 1 ? " guava tree" : " guava trees")
                + ", " + built.houses() + (built.houses() == 1 ? " house" : " houses")
                + ", " + built.props() + (built.props() == 1 ? " landmark" : " landmarks")
                + ", " + built.paths() + (built.paths() == 1 ? " path block" : " path blocks")
                + " and " + built.dogs() + (built.dogs() == 1 ? " bunnidog" : " bunnidogs")
                + ". Its bunnidogs take the lineage of this biome on their first tick."), false);
        return 1;
    }

    private static final String SEED_HINT = "Hold a bunnidog seed with genes in your main hand. To make one: "
            + "/give @s bunnidogs:seed[bunnidogs:seed_data=\"00-00-00-11-00-00|00-00-00-11-00-00|1.5\"] "
            + "(mother|father|quality; each parent may end in /vitality,swiftness,leap,senses,bloom)";

    /** Debug: what the seed in your main hand holds and what it will do (the tooltip shows a short version). */
    private static int seed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Optional<Seed> held = SeedItem.read(player.getMainHandItem());
        if (held.isEmpty()) {
            source.sendFailure(Component.literal(SEED_HINT));
            return 0;
        }
        Seed seed = held.get();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Seed: quality %.2f, litter about %.1f pups, mutation chance %.1f%% per pup",
                seed.quality(), seed.expectedLitter(), seed.mutationChance() * 100.0D));
        appendParent(sb, "Mother", seed.mother(), seed.motherStats());
        appendParent(sb, "Father", seed.father(), seed.fatherStats());
        sb.append("\n  Stats drift per pup: ").append(Seed.driftText(seed.drift(0.0D)))
                .append("  (plus the parents' average points, rounded per pup)");
        String text = sb.toString();
        source.sendSuccess(() -> Component.literal(text), false);
        return Command.SINGLE_SUCCESS;
    }

    private static void appendParent(StringBuilder sb, String role, Genome genome, Stats stats) {
        sb.append("\n  ").append(role).append(' ').append(genome).append("  stats ").append(stats);
        for (Gene gene : Gene.values()) {
            sb.append("\n    ").append(genome.describe(gene));
        }
    }

    /**
     * Debug hatch of the seed in your main hand at your feet. {@code bonus} is
     * added to the seed's quality for this hatching only (what Fortune will do at harvest). If too many bunnidogs live
     * nearby nothing hatches and you keep the seed; otherwise one seed is used up.
     */
    private static int hatch(CommandContext<CommandSourceStack> context, double bonus) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        Optional<Seed> held = SeedItem.read(stack);
        if (held.isEmpty()) {
            source.sendFailure(Component.literal(SEED_HINT));
            return 0;
        }
        List<Bunnidog> born = Bunnidog.hatch(source.getLevel(), player.position(), held.get(), bonus,
                new Random(player.getRandom().nextLong()));
        if (born.isEmpty()) {
            source.sendFailure(Component.literal("Hatching was refused or " + Breeding.CROWD_LIMIT
                    + " bunnidogs already live within " + (int) Breeding.CROWD_RADIUS
                    + " blocks. You still have the seed."));
            return 0;
        }
        stack.shrink(1);
        int count = born.size();
        source.sendSuccess(() -> Component.literal("Hatched " + count + (count == 1 ? " pup" : " pups")
                + ". /bunnidogs genome shows their genes."), false);
        return count;
    }

    /** Debug: which wild lineage the biome under you belongs to, and what a founder of it is made of. */
    private static int lineage(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Lineage lineage = ModBiomeTags.lineageAt(source.getLevel(), BlockPos.containing(source.getPosition()));
        StringBuilder sb = new StringBuilder("Wild lineage here: " + lineage.key());
        if (Founders.recipe(lineage).isEmpty()) {
            sb.append(" (plain founders)");
        }
        for (Founders.Signature sig : Founders.recipe(lineage)) {
            sb.append(String.format(Locale.ROOT, "\n  %s: %s in %d%% of slots", sig.gene().displayName(), sig.allele(),
                    Math.round(sig.chance() * 100)));
        }
        sb.append("\nSpawn here: bunnidogs found here are founders of this lineage. Summon one to see it: /summon bunnidogs:bunnidog");
        String text = sb.toString();
        source.sendSuccess(() -> Component.literal(text), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * The bred stats (Phase 4a): points and the multiplier they put on the attribute, the growing-up time, what the
     * genes will push in the next generation (bred with an identical partner, quality 0) and the starborn quality bonus.
     * To set stats for testing: /summon bunnidogs:bunnidog ~ ~ ~ {Stats:"0,0,0,0,50"} or
     * /data merge entity @n[type=bunnidogs:bunnidog] {Stats:"0,0,0,0,50"} (order: vitality, swiftness, leap, senses, bloom).
     */
    private static void appendStats(StringBuilder sb, Bunnidog dog) {
        Stats stats = dog.stats();
        sb.append("\n  Stats: ");
        for (Stat stat : Stat.values()) {
            if (stat.ordinal() > 0) {
                sb.append(", ");
            }
            sb.append(String.format(Locale.ROOT, "%s %+d", stat.displayName(), stats.get(stat)));
            if (stat != Stat.BLOOM && stats.get(stat) != 0) {
                sb.append(String.format(Locale.ROOT, " (x%.2f)", stats.multiplier(stat)));
            }
        }
        sb.append(String.format(Locale.ROOT, "; grows up in %.1f min", Growth.minutesToAdult(stats.growthMultiplier())));
        double[] trend = Drift.trend(dog.genome(), 0.0D);
        StringBuilder push = new StringBuilder();
        for (Stat stat : Stat.values()) {
            double t = trend[stat.ordinal()];
            if (Math.abs(t) > 1.0e-9) {
                push.append(String.format(Locale.ROOT, "%s %s%.1f  ", stat.displayName(), t > 0 ? "+" : "", t));
            }
        }
        sb.append("\n  Next generation (identical partner, quality 0): ")
                .append(push.length() == 0 ? "no drift" : push.toString().trim());
        double bonus = Drift.qualityBonus(dog.genome(), stats);
        if (bonus > 0.0D) {
            sb.append(String.format(Locale.ROOT, "\n  Starborn: adds %.2f seed quality (half of it if only one parent has it)", bonus));
        }
    }

    private static int genome(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 origin = source.getPosition();
        List<Bunnidog> found = new ArrayList<>(level.getEntitiesOfClass(Bunnidog.class,
                new AABB(BlockPos.containing(origin)).inflate(RADIUS)));
        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No bunnidogs within " + (int) RADIUS + " blocks."), false);
            return 0;
        }
        found.sort(Comparator.comparingDouble(b -> b.distanceToSqr(origin)));

        int shown = Math.min(found.size(), MAX_SHOWN);
        for (int i = 0; i < shown; i++) {
            Bunnidog dog = found.get(i);
            Genome genome = dog.genome();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.ROOT, "Bunnidog #%d%s at (%d, %d, %d)  genome %s",
                    i + 1, dog.isBaby() ? " [pup]" : "", (int) dog.getX(), (int) dog.getY(), (int) dog.getZ(), genome));
            for (Gene gene : Gene.values()) {
                sb.append("\n  ").append(genome.describe(gene));
            }
            appendStats(sb, dog);
            sb.append("\n  ").append(com.bunnidogs.mod.genetics.MindText.summary(genome));
            sb.append("\n  Personal learning: ").append(dog.learning().knowledge());
            if (dog.loveTierTicks() > 0) {
                sb.append(String.format(Locale.ROOT, "\n  In love on a tier-%d guava (quality %s, remembered for %d more s)",
                        dog.loveTier(), GuavaTier.ofLevel(dog.loveTier()).qualityText(), dog.loveTierTicks() / 20));
            }
            sb.append("\n  Juice as a liquid pup (quality 0): ").append(Juice.describe(genome, dog.stats(), 0.0D));
            sb.append(String.format(Locale.ROOT, "\n  => health %.1f, speed %.3f, jump %.2f, follow range %.1f, temperament %s",
                    dog.getMaxHealth(),
                    dog.getAttributeValue(Attributes.MOVEMENT_SPEED),
                    dog.getAttributeValue(Attributes.JUMP_STRENGTH),
                    dog.getAttributeValue(Attributes.FOLLOW_RANGE),
                    dog.temperament()));
            String text = sb.toString();
            source.sendSuccess(() -> Component.literal(text), false);
        }
        if (found.size() > shown) {
            int more = found.size() - shown;
            source.sendSuccess(() -> Component.literal("... and " + more + " more nearby."), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Debug (drops, D1): what the nearest bunnidogs would drop if a player killed them (adults at full, pups at x0.25, no Looting).
     * Without a number: each animal's expectations, the very numbers the Calipers show. With {@code simulate n}: n kills are rolled
     * for each animal and the average count of every item is printed, so the maths can be checked in game (try it in creative).
     * Same target selection as {@code /bunnidogs genome}.
     */
    private static int drops(CommandContext<CommandSourceStack> context, int kills) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 origin = source.getPosition();
        List<Bunnidog> found = new ArrayList<>(level.getEntitiesOfClass(Bunnidog.class,
                new AABB(BlockPos.containing(origin)).inflate(RADIUS)));
        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No bunnidogs within " + (int) RADIUS + " blocks."), false);
            return 0;
        }
        found.sort(Comparator.comparingDouble(b -> b.distanceToSqr(origin)));

        int shown = Math.min(found.size(), MAX_SHOWN);
        for (int i = 0; i < shown; i++) {
            Bunnidog dog = found.get(i);
            Drops.Context ctx = Drops.Context.PLAIN_ADULT.withBaby(dog.isBaby());
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.ROOT, "Bunnidog #%d%s at (%d, %d, %d)  genome %s",
                    i + 1, dog.isBaby() ? " [pup]" : "", (int) dog.getX(), (int) dog.getY(), (int) dog.getZ(), dog.genome()));
            if (kills == 0) {
                sb.append("\n  ").append(Drops.describe(dog.genome(), dog.stats(), dog.isBaby()));
            } else {
                sb.append("\n  Average of ").append(kills).append(" simulated kills:");
                Drops.simulate(dog.genome(), dog.stats(), ctx, kills, new Random(level.getRandom().nextLong()))
                        .forEach((id, average) -> sb.append(String.format(Locale.ROOT, "\n    %s  %.2f", id, average)));
            }
            String text = sb.toString();
            source.sendSuccess(() -> Component.literal(text), false);
        }
        if (found.size() > shown) {
            int more = found.size() - shown;
            source.sendSuccess(() -> Component.literal("... and " + more + " more nearby."), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
