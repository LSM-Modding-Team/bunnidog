package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.CrownArchives;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.JuiceItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.realm.GreatRuinPiece;
import com.google.gson.*;
import java.util.Random;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Native drinking/attributes and finite samples of the actual archive generators. */
final class InfusionGameTest {
    static void drink(GameTestHelper h) {
        var level = h.getLevel();
        var player = ApexTypesGameTest.player(h, ApexTypesGameTest.center(h));
        double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        var combat = ExpeditionInfusions.ENDGAME_STRENGTH.bottle();
        var bottle = JuiceItem.create(combat);
        String frozen = bottle.get(com.bunnidogs.mod.registry.ModItems.INFUSION_DATA.get());
        var returned = bottle.getItem().finishUsingItem(bottle, level, player);
        h.assertTrue(returned.is(Items.GLASS_BOTTLE) && bottle.isEmpty(), "A real sip consumes one bottle and returns its glass");
        h.assertTrue(player.getEffect(MobEffects.STRENGTH).getAmplifier() == 29
                && player.getAttributeValue(Attributes.ATTACK_DAMAGE) == attack + 90,
                "The real Q1/116-stat bottle grants native Strength XXX and its actual +90 attack modifier");
        h.assertTrue(player.getEffect(MobEffects.RESISTANCE).getAmplifier() == 2
                && player.getAbsorptionAmount() == 32, "Native Resistance III and Absorption VIII retain distinct defensive jobs");
        h.assertTrue(Infusion.parse(frozen).orElseThrow().equals(combat), "Drinking never rewrites the frozen individual");
        player.removeAllEffects();
        var recovery = ExpeditionInfusions.ENDGAME_RECOVERY.bottle();
        var recoveryBottle = JuiceItem.create(recovery);
        recoveryBottle.getItem().finishUsingItem(recoveryBottle, level, player);
        var effect = player.getEffect(MobEffects.REGENERATION);
        h.assertTrue(effect.getAmplifier() == 4 && effect.getDuration() == 880,
                "A separate Senses-240 drink supplies native Regeneration V for the unchanged 44-second duration");
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); player.setHealth(1);
        for (int tick = 0; tick < 300; tick++) {
            if (MobEffects.REGENERATION.value().shouldApplyEffectTickThisTick(tick, effect.getAmplifier()))
                MobEffects.REGENERATION.value().applyEffectTick(level, player, effect.getAmplifier());
        }
        h.assertTrue(player.getHealth() == 101, "Actual Regeneration V heals 100 HP over this controlled 15-second tick window");
        player.discard(); h.succeed();
    }

    private static JsonObject observation(String source, long sample, Seed seed) {
        var rolled = Infusion.roll(seed, new Random(sample));
        var row = new JsonObject();
        row.addProperty("source", source); row.addProperty("sample", sample);
        row.addProperty("seed", seed.toString()); row.addProperty("rolled_individual", rolled.toString());
        var effects = new JsonArray();
        for (var spec : rolled.specs()) {
            var effect = new JsonObject(); effect.addProperty("id", spec.id());
            effect.addProperty("level", spec.amplifier() + 1); effect.addProperty("ticks", spec.ticks()); effects.add(effect);
        }
        row.add("effects", effects);
        // Archive quality is an input to this seed, not a quality inherited by all later offspring.
        row.addProperty("plain_descendant_quality", Seed.of(rolled.genome(), rolled.stats(), rolled.genome(), rolled.stats(), 0).quality());
        return row;
    }

    static void sources(GameTestHelper h) {
        var level = h.getLevel(); var base = h.absolutePos(new net.minecraft.core.BlockPos(30, 100, 30));
        var samples = new JsonArray();
        int index = 0;
        for (long sample : new long[]{1379, 49157, 926481503, 57120}) {
            var origin = base.above(index++ * 25);
            var piece = new GreatRuinPiece(origin.getX(), origin.getY(), origin.getZ(), sample);
            for (int x = (origin.getX() - 12) >> 4; x <= (origin.getX() + 12) >> 4; x++)
                for (int z = (origin.getZ() - 12) >> 4; z <= (origin.getZ() + 12) >> 4; z++) {
                    var chunk = new ChunkPos(x, z);
                    piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(sample),
                            new BoundingBox(chunk.getMinBlockX(), level.getMinY(), chunk.getMinBlockZ(), chunk.getMaxBlockX(), level.getMaxY() - 1, chunk.getMaxBlockZ()), chunk, origin);
                }
            var archive = (ChestBlockEntity)level.getBlockEntity(origin.offset(-5, 1, 5));
            var ordinary = SeedItem.read(archive.getItem(11)).orElseThrow();
            h.assertTrue(ordinary.quality() >= 20 && ordinary.quality() <= 33, "Observe actual ruin archives, not invented parent qualities");
            samples.add(observation("ordinary_ruin", sample, ordinary));
            level.setBlock(origin, Blocks.CHEST.defaultBlockState(), 3);
            var crown = (ChestBlockEntity)level.getBlockEntity(origin); crown.clearContent();
            CrownArchives.fill(crown, sample, new UUID(0, sample));
            var royal = SeedItem.read(crown.getItem(10)).orElseThrow();
            h.assertTrue(royal.quality() >= 36 && royal.quality() <= 47, "Observe the real finite Crown archive generator");
            samples.add(observation("crown_archive", sample, royal));
        }
        com.bunnidogs.mod.BunnidogsMod.LOGGER.info("INFUSION_SOURCE_AUDIT {}", samples);
        h.succeed();
    }
}
