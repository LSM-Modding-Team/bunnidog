package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.*;
import com.bunnidogs.mod.registry.*;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Native entity cache, item codecs, chosen-copy splice and carried-identity migration. */
public final class CognitionGameTest {
    private CognitionGameTest() {}
    private static ItemStack copy(GameTestHelper h, ItemStack stack) {
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        out.store("Item", ItemStack.CODEC, stack);
        return TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), out.buildResult())
                .read("Item", ItemStack.CODEC).orElseThrow();
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(), 1, 1, 1);
        var full = Genome.DEFAULT.with(Gene.DIET, 0, 12).with(Gene.DIET, 1, 12)
                .with(CognitiveGene.MEMORY, 0, 0).with(CognitiveGene.MEMORY, 1, 3)
                .with(CognitiveGene.COMMUNICATION, 0, 2);
        dog.setGenome(full); dog.setStats(Stats.of(4, 5, 6, 7, 8)); dog.setAge(0);
        float max = dog.getMaxHealth(); dog.setHealth(max / 2);
        var profile = dog.mind();
        full = full.with(CognitiveGene.REASONING, 0, 3);
        dog.setGenome(full); dog.setParents(full, Genome.DEFAULT);
        h.assertTrue(!dog.mind().equals(profile) && dog.genome().equals(full), "A mind-only synced edit must invalidate the complete profile cache");
        h.assertTrue(dog.getMaxHealth() == max && dog.getHealth() == max / 2 && dog.stats().equals(Stats.of(4, 5, 6, 7, 8)),
                "Mind edits cannot improve body stats or refill health");
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess()); dog.addAdditionalSaveData(out);
        var restored = ModEntities.BUNNIDOG.get().create(level, EntitySpawnReason.LOAD);
        restored.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
        h.assertTrue(restored.genome().equals(full) && restored.motherGenome().orElseThrow().equals(full)
                && restored.fatherGenome().orElseThrow().equals(Genome.DEFAULT) && restored.getHealth() == dog.getHealth(),
                "Entity and birth-parent saves must keep both genotype words and exact health");
        var legacy = out.buildResult().copy(); legacy.putString("Genome", full.bodyCode()); legacy.remove("Learning");
        restored.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), legacy));
        h.assertTrue(restored.genome().packed() == full.packed() && restored.genome().mindPacked() == CognitiveGene.LEGACY_HELPER,
                "Old animals gain the same heritable helper mind without changing their body");
        h.assertTrue(restored.learning().knowledge().equals(Learning.LEGACY_FAMILIAR), "Migrated adults retain their established chores as personal familiarity");
        var tissue = new Tissue(full, dog.stats());
        h.assertTrue(TissueSampleItem.read(copy(h, TissueSampleItem.create(full, dog.stats()))).orElseThrow().equals(tissue),
                "Native sample components must retain every cognitive copy");
        var seed = Seed.of(full, dog.stats(), Genome.DEFAULT, Stats.ZERO, 3).withMutagen();
        h.assertTrue(SeedItem.read(copy(h, SeedItem.create(seed))).orElseThrow().equals(seed), "Native seeds retain both full parents and treatment");
        var oldSeed = Seed.parse(full.bodyCode() + "|" + full.bodyCode() + "|3|mutagen").orElseThrow();
        h.assertTrue(SeedItem.read(copy(h, SeedItem.create(oldSeed))).orElseThrow().legacyMutation(), "A carried legacy seed retains its body-only mutation law");
        var future = new ItemStack(ModItems.TISSUE_SAMPLE.get()); String opaque = "g3.future/data";
        future.set(ModItems.TISSUE_DATA.get(), opaque);
        h.assertTrue(TissueSampleItem.read(copy(h, future)).isEmpty() && future.get(ModItems.TISSUE_DATA.get()).equals(opaque), "Unknown future samples remain recoverable");
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var tool = new ItemStack(ModItems.GENE_SPLICER.get());
        tool.set(ModItems.SPLICE_GENE.get(), Gene.COUNT + 2 * CognitiveGene.HANDLING.ordinal() + 1);
        var donor = Genome.DEFAULT.with(CognitiveGene.HANDLING, 0, 0).with(CognitiveGene.HANDLING, 1, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        player.setItemInHand(InteractionHand.OFF_HAND, TissueSampleItem.create(donor, Stats.ZERO));
        player.getInventory().add(new ItemStack(ModItems.MUTAGEN.get())); dog.setHealth(max);
        player.interactOn(dog, InteractionHand.MAIN_HAND, Vec3.ZERO);
        h.assertTrue(dog.genome().alleleId(CognitiveGene.HANDLING, 0) == 3 && dog.genome().alleleId(CognitiveGene.HANDLING, 1) == 1
                && dog.genome().packed() == full.packed() && player.getOffhandItem().isEmpty() && tool.getDamageValue() == 1,
                "Native cognitive splice must introduce chosen donor copy 2 once, preserving the body");
        h.assertTrue(GeneSplicerItem.selected(copy(h, tool)) == CognitiveGene.HANDLING && GeneSplicerItem.donorCopy(copy(h, tool)) == 1,
                "Cognitive tool selection survives item codecs");
        var book = new ItemStack(ModItems.PEDIGREE_BOOK.get()); String futureRecords = "g3.opaque<g3.parent+g3.parent";
        book.set(ModItems.PEDIGREE_DATA.get(), futureRecords);
        var entry = new Pedigree.Entry(full, Optional.of(full), Optional.of(full), Optional.of(dog.getUUID()), Optional.empty(), Optional.of(dog.stats()), false);
        PedigreeBookItem.log(player, book, entry);
        h.assertTrue(book.get(ModItems.PEDIGREE_DATA.get()).equals(futureRecords), "Logging cannot erase future archive entries");
        var basket = new ItemStack(ModItems.TRAVEL_BASKET.get());
        h.assertTrue(TravelBasketItem.capture(level, basket, dog).success(), "A healthy adult can be carried after a mind splice");
        var carried = TravelBasketItem.readEntry(copy(h, basket)).orElseThrow();
        h.assertTrue(carried.genome().equals(dog.genome()) && carried.mother().orElseThrow().equals(full)
                && carried.stats().orElseThrow().equals(dog.stats()) && carried.id().orElseThrow().equals(dog.getUUID()),
                "Basket snapshots retain the full live and birth-parent identity");
        h.assertTrue(basket.get(DataComponents.BUCKET_ENTITY_DATA).copyTag().getStringOr("Genome", "").equals(dog.genome().toString()),
                "The basket stores the complete versioned genome");
    }
}
