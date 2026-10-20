package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NestHome;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.IslandShape;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.AABB;

/** Native ocean preflight, per-chunk terrain, stable founders, partial saves and irreversible completed progress. */
public final class IslandRefugeGameTest {
    private IslandRefugeGameTest() {}
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("bunnidogs",path); }
    private static BoundingBox chunkBox(GameTestHelper h,ChunkPos chunk) {
        return new BoundingBox(chunk.getMinBlockX(),h.getLevel().getMinY(),chunk.getMinBlockZ(),
                chunk.getMinBlockX()+15,h.getLevel().getMaxY(),chunk.getMinBlockZ()+15);
    }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); var manager=level.getServer().getStructureManager();
        var structure=(IslandRefugeStructure)level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE,id("island_refuge"))).value();
        var settings=new FlatLevelGeneratorSettings(Optional.empty(),level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.OCEAN),List.of());
        settings.getLayersInfo().add(new FlatLayerInfo(114,Blocks.STONE));
        settings.getLayersInfo().add(new FlatLayerInfo(13,Blocks.WATER)); settings.updateLayers();
        var ocean=new FlatLevelSource(settings);
        var context=new Structure.GenerationContext(level.registryAccess(),ocean,ocean.getBiomeSource(),level.getChunkSource().randomState(),
                manager,level.getSeed(),new ChunkPos(0,0),level,biome->true);
        var planned=structure.findGenerationPoint(context).orElseThrow().getPiecesBuilder().build().pieces();
        h.assertTrue(planned.size()==1 && planned.getFirst() instanceof IslandRefugePiece
                && planned.getFirst().getBoundingBox().minY()==49,"A real native shallow-ocean context must produce one rooted saved island piece");
        var dry=level.getChunkSource().getGenerator();
        var rejected=new Structure.GenerationContext(level.registryAccess(),dry,dry.getBiomeSource(),level.getChunkSource().randomState(),
                manager,level.getSeed(),new ChunkPos(0,0),level,biome->true);
        h.assertTrue(structure.findGenerationPoint(rejected).isEmpty(),"The actual native flat mainland context must refuse island generation");
        var serialization=StructurePieceSerializationContext.fromLevel(level);
        BlockPos previous=null; IslandRefugePiece previousPiece=null;
        for (int order=0;order<2;order++) {
            var center=h.absolutePos(new BlockPos(768+order*128,120,10)); int sea=center.getY();
            var piece=new IslandRefugePiece(center.getX(),sea,center.getZ(),sea-12,4711);
            var bounds=piece.getBoundingBox(); var chunks=new ArrayList<ChunkPos>();
            for (int x=(bounds.minX()-2)>>4;x<=(bounds.maxX()+2)>>4;x++) for (int z=(bounds.minZ()-2)>>4;z<=(bounds.maxZ()+2)>>4;z++) level.getChunk(x,z);
            for (int x=-26;x<=26;x++) for (int z=-26;z<=26;z++) {
                level.setBlock(center.offset(x,-12,z),Blocks.STONE.defaultBlockState(),2);
                for (int y=-11;y<=-1;y++) level.setBlock(center.offset(x,y,z),Blocks.WATER.defaultBlockState(),2);
            }
            var kept=center.offset(7,4,-3); level.setBlockAndUpdate(kept,Blocks.CHEST.defaultBlockState());
            var oldChest=(ChestBlockEntity)level.getBlockEntity(kept); oldChest.setItem(0,new ItemStack(Items.DIAMOND,7));
            var ore=center.offset(-8,-2,-8); level.setBlock(ore,Blocks.DIAMOND_ORE.defaultBlockState(),2);
            var wet=center.offset(8,-1,-6); level.setBlock(wet,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,true),2);
            var outside=center.offset(25,3,0); level.setBlock(outside,Blocks.GOLD_BLOCK.defaultBlockState(),2);
            for (int x=bounds.minX()>>4;x<=bounds.maxX()>>4;x++) for (int z=bounds.minZ()>>4;z<=bounds.maxZ()>>4;z++) chunks.add(new ChunkPos(x,z));
            if (order==1) Collections.reverse(chunks);
            var untouched=center.offset(0,3,0); var original=level.getBlockState(untouched);
            for (int i=0;i<chunks.size();i++) {
                var chunk=chunks.get(i); piece.postProcess(level,level.structureManager(),dry,RandomSource.create(1),chunkBox(h,chunk),chunk,center);
                if (i==0) {
                    h.assertTrue(level.getBlockState(untouched).equals(original),"The first distant chunk must not write the center column");
                    piece=new IslandRefugePiece(piece.createTag(serialization));
                }
            }
            h.assertTrue(level.getBlockState(center.offset(0,-12,0)).is(Blocks.STONE)
                    && level.getBlockState(center.offset(0,3,-4)).is(ModBlocks.NEST.get())
                    && level.getBlockState(center.offset(0,4,1)).is(Blocks.BELL)
                    && level.getBlockState(center.offset(11,3,4)).is(Blocks.KELP),"Actual island terrain must root, house founders and retain its contained native tide garden");
            h.assertTrue(level.getBlockState(center.offset(0,3,14)).is(Blocks.SANDSTONE_STAIRS)
                    &&level.getBlockState(center.offset(0,2,16)).is(Blocks.SANDSTONE_STAIRS),"The authored shore trail supplies real half-step transitions for short legs");
            h.assertTrue(level.getBlockEntity(kept)==oldChest && oldChest.getItem(0).getCount()==7
                    && level.getBlockState(ore).is(Blocks.DIAMOND_ORE) && level.getBlockState(wet).is(Blocks.OAK_FENCE)
                    && level.getBlockState(wet).getValue(FenceBlock.WATERLOGGED) && level.getBlockState(outside).is(Blocks.GOLD_BLOCK),
                    "Native chunk ownership preserves old containers, ore, waterlogged builds and every outside column");
            var founders=level.getEntitiesOfClass(Bunnidog.class,AABB.of(bounds));
            h.assertTrue(founders.size()==3,"Actual native refuge generation produces exactly three one-time founders");
            for (var dog:founders) {
                dog.setNoAi(true); dog.setOnGround(true);
                h.assertTrue(dog.isPersistenceRequired() && !dog.isBaby() && dog.motherGenome().isEmpty(),
                        "Refuge residents are persistent wild adults rather than instant pedigree winners");
                for (var gene:Gene.values()) for (int slot=0;slot<2;slot++) {
                    var allele=gene.allele(dog.genome().alleleId(gene,slot));
                    h.assertTrue(!allele.isRare() && (!allele.isWild() || allele.lineage()==Lineage.ISLAND),"Every assigned founder retains the correct isolated recipe");
                }
                var floor=dog.blockPosition().below();
                var moved=dog.moveHomeTo(level,floor);
                h.assertTrue(moved==NestHome.MoveResult.MOVED,"The real sheltered floor Nests accept their actual residents: "+moved);
            }
            var walker=founders.getFirst();
            walker.setGenome(walker.genome().with(Gene.LEGS,0,Gene.LEGS.alleleNamed("paddle").id()).with(Gene.LEGS,1,Gene.LEGS.alleleNamed("paddle").id()));
            walker.setStats(com.bunnidogs.mod.genetics.Stats.of(0,0,0,20,0));
            walker.setPos(center.getX()+.5,sea+2,center.getZ()+18.5); walker.setOnGround(true);
            var route=walker.getNavigation().createPath(center.offset(0,4,-4),1);
            h.assertTrue(route!=null&&route.canReach(),"A real short-paddle navigation plan must climb the fitted shore stairs to the shelter");
            var chestPos=center.offset(4,4,-2); var chest=(ChestBlockEntity)level.getBlockEntity(chestPos);
            h.assertTrue(chest!=null && chest.getLootTable()!=null,"Island supplies use native deferred loot"); chest.unpackLootTable(null);
            var stock=java.util.stream.IntStream.range(0,chest.getContainerSize()).mapToObj(s->chest.getItem(s).copy()).toList();
            h.assertTrue(stock.stream().anyMatch(s->s.is(ModItems.FIELD_GUIDE.get())) && stock.stream().anyMatch(s->s.is(Items.LEAD)),"The native supply table yields its actual guide and lead");
            if (previous!=null) for (int x=-24;x<=24;x++) for (int z=-24;z<=24;z++) for (int y=-12;y<=16;y++) {
                h.assertTrue(level.getBlockState(center.offset(x,y,z)).equals(level.getBlockState(previous.offset(x,y,z))),
                        "Per-chunk order and partial reload must reproduce the same actual island blocks");
            }
            previous=center; previousPiece=piece;
        }
        var center=previous; var piece=new IslandRefugePiece(previousPiece.createTag(serialization)); var bounds=piece.getBoundingBox();
        var founders=level.getEntitiesOfClass(Bunnidog.class,AABB.of(bounds)); founders.getFirst().discard();
        var changed=center.offset(0,3,0); level.setBlock(changed,Blocks.GOLD_BLOCK.defaultBlockState(),2);
        var chest=(ChestBlockEntity)level.getBlockEntity(center.offset(4,4,-2)); chest.clearContent(); chest.setItem(0,new ItemStack(Items.EMERALD,9));
        for (int x=bounds.minX()>>4;x<=bounds.maxX()>>4;x++) for (int z=bounds.minZ()>>4;z<=bounds.maxZ()>>4;z++) {
            var chunk=new ChunkPos(x,z); piece.postProcess(level,level.structureManager(),dry,RandomSource.create(9),chunkBox(h,chunk),chunk,center);
        }
        h.assertTrue(level.getBlockState(changed).is(Blocks.GOLD_BLOCK) && chest.getLootTable()==null && chest.getItem(0).is(Items.EMERALD)
                && chest.getItem(0).getCount()==9 && level.getEntitiesOfClass(Bunnidog.class,AABB.of(bounds)).size()==2,
                "Saved completed progress never rebuilds changed terrain, restocks supplies or respawns a removed founder");
        h.assertTrue(level.getServer().getAdvancements().get(id("island_refuge"))!=null,"The native island discovery advancement parses");
    }
}
