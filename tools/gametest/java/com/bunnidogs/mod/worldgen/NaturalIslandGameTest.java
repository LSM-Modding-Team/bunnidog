package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.BunnidogsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/** One real default-Overworld site; no search loop, generated chunks or live-world seed dependence. */
public final class NaturalIslandGameTest {
    private NaturalIslandGameTest() {}
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("bunnidogs",path); }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); var registries=level.registryAccess();
        var generator=WorldPresets.getNormalOverworld(registries).generator();
        var random=RandomState.create(registries,NoiseGeneratorSettings.OVERWORLD,0);
        var state=generator.createState(registries.lookupOrThrow(Registries.STRUCTURE_SET),random,0);
        var structure=(IslandRefugeStructure)registries.lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE,id("island_refuge"))).value();
        var placement=(RandomSpreadStructurePlacement)registries.lookupOrThrow(Registries.STRUCTURE_SET)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,id("island_refuge"))).value().placement();
        var eligible=TagKey.<Biome>create(Registries.BIOME,id("has_structure/island_refuge"));
        var chunk=new ChunkPos(-88,-16);
        int bx=chunk.getMinBlockX()+8,bz=chunk.getMinBlockZ()+8;
        h.assertTrue(generator.getBiomeSource().getNoiseBiome(bx>>2,63>>2,bz>>2,random.sampler()).is(eligible)
                && placement.isStructureChunk(state,chunk.x(),chunk.z()),
                "The seed-zero site must remain a native eligible random-spread candidate outside the monument exclusion");
        var context=new Structure.GenerationContext(registries,generator,generator.getBiomeSource(),random,
                level.getServer().getStructureManager(),0,chunk,level,biome->biome.is(eligible));
        var found=structure.findGenerationPoint(context);
        BunnidogsMod.LOGGER.info("Natural island fixture: default Overworld seed 0, native candidate {}",chunk);
        h.assertTrue(found.isPresent()&&found.orElseThrow().getPiecesBuilder().build().pieces().size()==1,
                "A real default-Overworld candidate must fit with native ocean biomes, density columns, sea height and monument exclusion");
    }
}
