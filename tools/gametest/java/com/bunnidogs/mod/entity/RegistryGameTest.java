package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.PurebredRegistryItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class RegistryGameTest {
    private RegistryGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 3, 1, 3);
        dog.setGenome(Genome.DEFAULT); dog.setAge(0); dog.setStats(Stats.of(7, 8, 9, 10, 11));
        var player = h.makeMockServerPlayerInLevel();
        var book = new ItemStack(ModItems.PUREBRED_REGISTRY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, book);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(PurebredRegistryItem.read(book).size() == 0, "A wild animal without birth parents must not register");
        dog.setParents(Genome.DEFAULT, Genome.DEFAULT);
        dog.setAge(-100);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(PurebredRegistryItem.read(book).size() == 0, "A pup must mature before registration");
        dog.setAge(0);
        dog.setGenome(Genome.DEFAULT.with(Gene.DIET, 1, 1));
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(PurebredRegistryItem.read(book).size() == 0, "Diet's seventh pair must match too");
        dog.setGenome(Genome.DEFAULT);
        float health = dog.getHealth();
        var stats = dog.stats();
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        var registry = PurebredRegistryItem.read(book);
        h.assertTrue(registry.size() == 1 && registry.entries().getFirst().id().orElseThrow().equals(dog.getUUID())
                && registry.entries().getFirst().stats().orElseThrow().equals(stats), "Native use must register the correct adult snapshot");
        h.assertTrue(dog.genome().equals(Genome.DEFAULT) && dog.stats().equals(stats) && dog.getHealth() == health
                && dog.motherGenome().orElseThrow().equals(Genome.DEFAULT) && book.getCount() == 1,
                "Registration must preserve the animal and the reusable book");
        var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs", "breeds_true"));
        h.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(),
                "A successful native registration must award Breeds True");
        var twin = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 5, 1, 3);
        twin.setAge(0); twin.setGenome(Genome.DEFAULT); twin.setParents(Genome.DEFAULT, Genome.DEFAULT);
        twin.setStats(Stats.of(99, 99, 99, 99, 99));
        player.interactOn(twin, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(PurebredRegistryItem.read(book).entries().equals(registry.entries()), "Same line must keep its first representative and stats");
        dog.setGenome(Genome.DEFAULT.with(Gene.COAT, 0, 1));
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(PurebredRegistryItem.read(book).entries().equals(registry.entries()), "Changing a live animal must not alter an old registry snapshot");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        out.store("Registry", ItemStack.CODEC, book);
        var loaded = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult())
                .read("Registry", ItemStack.CODEC).orElseThrow();
        h.assertTrue(PurebredRegistryItem.read(loaded).entries().equals(registry.entries()), "Native item save/load must retain registry snapshots");
    }
}
