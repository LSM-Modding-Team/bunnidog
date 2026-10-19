package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.layout.BarnSite;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;

/** Loads the shipped native NBT, builds a real start and places/reloads native pieces in every rotation. */
public final class BreedingBarnGameTest {
    private BreedingBarnGameTest() {}
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("bunnidogs",path); }
    private static BlockPos at(BlockPos origin, Rotation rotation, int x, int y, int z) {
        return new BlockPos(x,y,z).rotate(rotation).offset(origin);
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var manager = level.getServer().getStructureManager();
        var template = manager.get(id("breeding_barn/v1")).orElseThrow();
        h.assertTrue(template.getSize().getX() == 19 && template.getSize().getY() == 11 && template.getSize().getZ() == 19,
                "The shipped native barn template must load at its authored bounds");
        try (var stream = BreedingBarnGameTest.class.getResourceAsStream("/data/bunnidogs/structure/breeding_barn/v1.nbt")) {
            var raw = NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
            for (var entry : raw.getListOrEmpty("palette")) {
                var state = (CompoundTag)entry;
                var block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(state.getStringOr("Name",""))).orElseThrow();
                var props = state.getCompoundOrEmpty("Properties");
                for (var key : props.keySet()) {
                    var property = block.getStateDefinition().getProperty(key);
                    h.assertTrue(property != null && property.getValue(props.getStringOr(key,"")).isPresent(),
                            "Every authored palette property must exist in the resolved native block: "+state);
                }
            }
            h.assertTrue(raw.getListOrEmpty("entities").isEmpty(),"An abandoned barn must generate no animals or villagers");
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
        h.assertTrue(BarnSite.height((x,z) -> new BarnSite.Surface(64,true)).orElseThrow() == 64
                && BarnSite.height((x,z) -> new BarnSite.Surface(x > 0 ? 65 : 64,true)).orElseThrow() == 65,
                "A flat or one-step dry clearing fits at its highest support");
        h.assertTrue(BarnSite.height((x,z) -> new BarnSite.Surface(x > 0 ? 66 : 64,true)).isEmpty()
                && BarnSite.height((x,z) -> new BarnSite.Surface(64,x != 0 || z != 5)).isEmpty(),
                "A steep clearing or sampled pond/trees must refuse the complete barn");
        var generator = level.getChunkSource().getGenerator();
        var structure = (BreedingBarnStructure)level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE,id("breeding_barn"))).value();
        var context = new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),
                level.getChunkSource().randomState(),manager,level.getSeed(),new ChunkPos(0,0),level,biome -> true);
        var start = structure.findGenerationPoint(context).orElseThrow();
        var pieces = start.getPiecesBuilder().build().pieces();
        h.assertTrue(pieces.size() == 1,"Actual native dry-site planning must produce its saved root piece");
        int firstFree = generator.getFirstFreeHeight(8,8,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,level,context.randomState());
        h.assertTrue(pieces.getFirst().getBoundingBox().minY() == firstFree-1,
                "The named barn anchor plus native ground delta must not bury the actual generated foundation");
        h.assertTrue(level.getServer().getAdvancements().get(id("old_breeding_grounds")) != null,"The native barn discovery advancement must parse");
        var pool = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
                .getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL,id("breeding_barn/start"))).value();
        int turn = 0;
        for (var rotation : Rotation.values()) {
            var origin = h.absolutePos(new BlockPos(10,200+turn++*20,10));
            var element = pool.getRandomTemplate(RandomSource.create(1));
            var bounds = element.getBoundingBox(manager,origin,rotation);
            for (int cx = bounds.minX()>>4; cx <= bounds.maxX()>>4; cx++) for (int cz = bounds.minZ()>>4; cz <= bounds.maxZ()>>4; cz++) level.getChunk(cx,cz);
            var protectedPos = at(origin,rotation,4,1,6); var wet = at(origin,rotation,5,1,6); var ore = at(origin,rotation,4,0,5);
            level.setBlockAndUpdate(protectedPos,Blocks.CHEST.defaultBlockState());
            var playerChest = (ChestBlockEntity)level.getBlockEntity(protectedPos); playerChest.setItem(0,new ItemStack(Items.DIAMOND,14));
            level.setBlock(wet,Blocks.WATER.defaultBlockState(),2); level.setBlock(ore,Blocks.DIAMOND_ORE.defaultBlockState(),2);
            var piece = new PoolElementStructurePiece(manager,element,origin,element.getGroundLevelDelta(),rotation,bounds,LiquidSettings.IGNORE_WATERLOGGING);
            var box = new BoundingBox(bounds.minX(),level.getMinY(),bounds.minZ(),bounds.maxX(),level.getMaxY(),bounds.maxZ());
            piece.postProcess(level,level.structureManager(),generator,RandomSource.create(44),box,new ChunkPos(origin.getX()>>4,origin.getZ()>>4),origin);
            h.assertTrue(level.getBlockEntity(protectedPos) == playerChest && playerChest.getItem(0).getCount() == 14
                    && level.getBlockState(wet).is(Blocks.WATER) && level.getBlockState(ore).is(Blocks.DIAMOND_ORE),
                    "Native template processors must preserve existing containers, fluids and ore in rotation "+rotation);
            h.assertTrue(level.getBlockState(at(origin,rotation,13,1,10)).is(ModBlocks.BREEDING_PEN.get())
                    && level.getBlockState(at(origin,rotation,15,0,8)).is(ModBlocks.NEST.get())
                    && level.getBlockState(at(origin,rotation,15,0,12)).is(ModBlocks.NEST.get())
                    && level.getBlockState(at(origin,rotation,3,1,5)).is(ModBlocks.JUICER.get())
                    && level.getBlockState(at(origin,rotation,8,1,5)).is(ModBlocks.GENETICS_TABLE.get())
                    && level.getBlockState(at(origin,rotation,8,2,5)).is(ModBlocks.SPECIMEN_JAR.get()),
                    "Every native rotation must keep the actual workshop and two floor Nests");
            h.assertTrue(level.getBlockState(at(origin,rotation,9,1,9)).isAir()
                    && level.getBlockState(at(origin,rotation,5,1,2)).isAir()
                    && level.getBlockState(at(origin,rotation,11,1,9)).isAir(),"The planning anchor and both doorways must become clear walking space");
            var chestPos = at(origin,rotation,8,1,12); var chest = (ChestBlockEntity)level.getBlockEntity(chestPos);
            h.assertTrue(chest != null && chest.getLootTable() != null,"The new supply chest keeps native deferred loot");
            chest.unpackLootTable(null);
            var stock = java.util.stream.IntStream.range(0,chest.getContainerSize()).mapToObj(slot -> chest.getItem(slot).copy()).toList();
            h.assertTrue(stock.stream().filter(s -> s.is(ModItems.FIELD_GUIDE.get())).mapToInt(ItemStack::getCount).sum() == 1
                    && stock.stream().anyMatch(s -> !s.isEmpty() && !s.is(ModItems.FIELD_GUIDE.get())),
                    "Actual deferred supply loot must yield one guide and ordinary supplies");
            var serialization = StructurePieceSerializationContext.fromLevel(level);
            var saved = piece.createTag(serialization); var loaded = new PoolElementStructurePiece(serialization,saved);
            loaded.postProcess(level,level.structureManager(),generator,RandomSource.create(99),box,new ChunkPos(origin.getX()>>4,origin.getZ()>>4),origin);
            h.assertTrue(level.getBlockEntity(chestPos) == chest && chest.getLootTable() == null
                    && java.util.stream.IntStream.range(0,chest.getContainerSize()).allMatch(slot -> ItemStack.matches(chest.getItem(slot),stock.get(slot)))
                    && level.getBlockEntity(protectedPos) == playerChest && playerChest.getItem(0).getCount() == 14,
                    "Reloaded native piece replay must neither replace existing block entities nor restock opened supply loot");
            h.assertTrue(level.getEntitiesOfClass(Bunnidog.class,AABB.of(bounds)).isEmpty(),"No structure pass may inject a founder or duplicate animals");
        }
    }
}
