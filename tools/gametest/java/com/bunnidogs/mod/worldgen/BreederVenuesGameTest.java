package com.bunnidogs.mod.worldgen;

import com.bunnidogs.mod.block.BreedingPenBlock;
import com.bunnidogs.mod.block.BreedingPenBlockEntity;
import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.NestHome;
import com.bunnidogs.mod.entity.PenBreeding;
import com.bunnidogs.mod.entity.CareMenuGameTest;
import com.bunnidogs.mod.menu.WorkshopPanels;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Pen;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.ShowRibbonItem;
import com.bunnidogs.mod.layout.VenueSite;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Exercise shipped layouts as actual chunk-clipped, rotated, saved native pieces and usable venues. */
public final class BreederVenuesGameTest {
    private BreederVenuesGameTest() {}
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("bunnidogs",path); }
    private static BlockPos at(BlockPos origin, Rotation rotation, int x, int y, int z) {
        return new BlockPos(x,y,z).rotate(rotation).offset(origin);
    }
    private static void place(ServerLevel level, PoolElementStructurePiece piece, BoundingBox bounds, BlockPos origin) {
        var generator = level.getChunkSource().getGenerator();
        for (int x = bounds.minX()>>4; x <= bounds.maxX()>>4; x++) for (int z = bounds.minZ()>>4; z <= bounds.maxZ()>>4; z++) {
            level.getChunk(x,z);
            piece.postProcess(level,level.structureManager(),generator,RandomSource.create(527),
                    new BoundingBox(x*16,level.getMinY(),z*16,x*16+15,level.getMaxY(),z*16+15),new ChunkPos(x,z),origin);
        }
    }
    private static Bunnidog dog(ServerLevel level, BlockPos feet) {
        var dog = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.STRUCTURE);
        dog.setNoAi(true); dog.setGenome(Genome.DEFAULT); dog.setAge(0); dog.setHealth(dog.getMaxHealth());
        dog.setPos(feet.getX()+.5,feet.getY(),feet.getZ()+.5); dog.setOnGround(true);
        level.addFreshEntity(dog); return dog;
    }
    public static void run(GameTestHelper h) {
        h.assertTrue(VenueSite.height(12,(x,z) -> new VenueSite.Surface(x > 0 ? 65 : 64,true)).orElseThrow() == 65
                && VenueSite.height(16,(x,z) -> new VenueSite.Surface(64,x != 16 || z != -16)).isEmpty()
                && VenueSite.height(16,(x,z) -> new VenueSite.Surface(x == 0 ? 66 : 64,true)).isEmpty()
                && VenueSite.height(3,(x,z) -> new VenueSite.Surface(64,true)).isEmpty(),
                "Rotation-safe clearings fit one dry step and reject edge ponds, slopes and invalid extents");
        var level = h.getLevel(); var manager = level.getServer().getStructureManager();
        var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        int theme = 0;
        for (String kind : new String[]{"meadow_kennel","desert_kennel","snowy_kennel","show_ring"}) {
            boolean arena = kind.equals("show_ring");
            var template = manager.get(id(kind+"/v1")).orElseThrow();
            h.assertTrue(template.getSize().getX() == (arena ? 31 : 23),"Every authored venue must load at its real native bounds");
            try (var stream = BreederVenuesGameTest.class.getResourceAsStream("/data/bunnidogs/structure/"+kind+"/v1.nbt")) {
                var raw = NbtIo.readCompressed(stream,NbtAccounter.unlimitedHeap());
                for (var entry : raw.getListOrEmpty("palette")) {
                    var state = (CompoundTag)entry;
                    var block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(state.getStringOr("Name",""))).orElseThrow();
                    var props = state.getCompoundOrEmpty("Properties");
                    for (var key : props.keySet()) {
                        var property = block.getStateDefinition().getProperty(key);
                        h.assertTrue(property != null && property.getValue(props.getStringOr(key,"")).isPresent(),
                                "Every shipped venue property must exist in its resolved block: "+state);
                    }
                }
                h.assertTrue(raw.getListOrEmpty("entities").isEmpty(),"Venues must not inject founders, villagers or counterfeit winners");
            } catch (java.io.IOException e) { throw new IllegalStateException(e); }
            var structure = (BreederVenueStructure)level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE,id(kind))).value();
            var generator = level.getChunkSource().getGenerator();
            var context = new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),
                    level.getChunkSource().randomState(),manager,level.getSeed(),new ChunkPos(0,0),level,biome -> true);
            var planned = structure.findGenerationPoint(context).orElseThrow().getPiecesBuilder().build().pieces();
            h.assertTrue(planned.size() == 1,
                    "Each parsed venue codec and actual preflight must produce one saved native piece");
            int firstFree = generator.getFirstFreeHeight(8,8,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,level,context.randomState());
            h.assertTrue(planned.getFirst().getBoundingBox().minY() == firstFree-1,
                    "Native anchor and ground offsets must put the actual venue foundation in the supporting surface, never one block buried");
            var pool = level.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
                    .getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL,id(kind+"/start"))).value();
            int turn = 0;
            for (var rotation : Rotation.values()) {
                var origin = h.absolutePos(new BlockPos(320+theme*96,100+turn++*16,10));
                var element = pool.getRandomTemplate(RandomSource.create(1));
                var bounds = element.getBoundingBox(manager,origin,rotation);
                // Load the whole fixture and isolation census without letting the processor request terrain.
                for (int x = (bounds.minX()-16)>>4; x <= (bounds.maxX()+16)>>4; x++)
                    for (int z = (bounds.minZ()-16)>>4; z <= (bounds.maxZ()+16)>>4; z++) level.getChunk(x,z);
                var protectedPos = at(origin,rotation,arena ? 14 : 5,1,arena ? 14 : 9);
                level.setBlockAndUpdate(protectedPos,Blocks.CHEST.defaultBlockState());
                var protectedChest = (ChestBlockEntity)level.getBlockEntity(protectedPos);
                protectedChest.setItem(0,new ItemStack(Items.DIAMOND,7));
                var wet = at(origin,rotation,arena ? 17 : 7,1,arena ? 17 : 9);
                var ore = at(origin,rotation,arena ? 18 : 7,0,arena ? 17 : 10);
                level.setBlock(wet,Blocks.WATER.defaultBlockState(),2); level.setBlock(ore,Blocks.GOLD_ORE.defaultBlockState(),2);
                var piece = new PoolElementStructurePiece(manager,element,origin,element.getGroundLevelDelta(),rotation,bounds,LiquidSettings.IGNORE_WATERLOGGING);
                place(level,piece,bounds,origin);
                h.assertTrue(level.getBlockEntity(protectedPos) == protectedChest && protectedChest.getItem(0).getCount() == 7
                        && level.getBlockState(wet).is(Blocks.WATER) && level.getBlockState(ore).is(Blocks.GOLD_ORE),
                        "Chunk-clipped venue placement must preserve existing stock, fluid and tagged ore in "+kind+rotation);
                h.assertTrue(level.getEntitiesOfClass(Bunnidog.class,AABB.of(bounds)).isEmpty(),"Actual venue placement must not spawn animals");
                if (arena) arena(h,origin,rotation,player); else kennel(h,origin,rotation,player);
                var chestPos = at(origin,rotation,arena ? 20 : 8,1,arena ? 5 : 16);
                var chest = (ChestBlockEntity)level.getBlockEntity(chestPos);
                h.assertTrue(chest != null && chest.getLootTable() != null,"Supplies must remain actual deferred native chest loot");
                chest.unpackLootTable(player);
                var stock = java.util.stream.IntStream.range(0,chest.getContainerSize()).mapToObj(slot -> chest.getItem(slot).copy()).toList();
                h.assertTrue(stock.stream().filter(s -> s.is(ModItems.FIELD_GUIDE.get())).mapToInt(ItemStack::getCount).sum() == 1
                        && (!arena || stock.stream().anyMatch(s -> s.is(Items.LEAD)))
                        && stock.stream().noneMatch(s -> s.getItem() instanceof ShowRibbonItem),"Supplies give a guide and arena lead, never awarded ribbons");
                var serialization = StructurePieceSerializationContext.fromLevel(level);
                var trophyPos = at(origin,rotation,18,1,5);
                var trophy = arena ? ((TrophyPedestalBlockEntity)level.getBlockEntity(trophyPos)).item().copy() : ItemStack.EMPTY;
                var restored = new PoolElementStructurePiece(serialization,piece.createTag(serialization));
                place(level,restored,bounds,origin);
                h.assertTrue(level.getBlockEntity(chestPos) == chest && chest.getLootTable() == null
                        && java.util.stream.IntStream.range(0,chest.getContainerSize()).allMatch(slot -> ItemStack.matches(chest.getItem(slot),stock.get(slot)))
                        && level.getBlockEntity(protectedPos) == protectedChest,"Saved native venue replay must preserve opened stock without restocking");
                if (arena) h.assertTrue(ItemStack.matches(((TrophyPedestalBlockEntity)level.getBlockEntity(trophyPos)).item(),trophy),
                        "Native saved arena replay must preserve the genuine player-earned ribbon on its pedestal");
            }
            theme++;
        }
        h.assertTrue(level.getServer().getAdvancements().get(id("regional_kennel")) != null
                && level.getServer().getAdvancements().get(id("show_ring")) != null,"Both native venue discovery criteria must parse");
        player.discard();
    }
    private static void kennel(GameTestHelper h, BlockPos origin, Rotation rotation, net.minecraft.server.level.ServerPlayer player) {
        var level = h.getLevel(); var penPos = at(origin,rotation,15,1,12);
        var pen = (BreedingPenBlockEntity)level.getBlockEntity(penPos);
        h.assertTrue(pen != null && pen.food(0).isEmpty() && pen.food(1).isEmpty()
                && level.getBlockState(at(origin,rotation,8,1,12)).is(ModBlocks.NURSERY_CRADLE.get())
                && level.getBlockState(at(origin,rotation,3,1,8)).is(ModBlocks.JUICER.get())
                && level.getBlockState(at(origin,rotation,8,2,8)).is(ModBlocks.SPECIMEN_JAR.get()),"An intact kennel has its complete unstocked workshop");
        h.assertTrue(level.getBlockState(at(origin,rotation,10,1,13)).isAir()
                && level.getBlockState(at(origin,rotation,11,1,13)).isAir()
                && level.getBlockState(at(origin,rotation,16,1,20)).is(ModBlocks.GUAVA_FENCE_GATE.get()),"Workshop-to-yard and outside entries must remain usable");
        var a = dog(level,at(origin,rotation,14,1,10)); var b = dog(level,at(origin,rotation,16,1,10));
        h.assertTrue(a.moveHomeTo(level,at(origin,rotation,14,0,10)) == NestHome.MoveResult.MOVED
                && b.moveHomeTo(level,at(origin,rotation,16,0,10)) == NestHome.MoveResult.MOVED,"Both real floor Nests must accept housed parents");
        var inspected = PenBreeding.inspect(level,penPos,pen.getBlockState().getValue(BreedingPenBlock.FACING));
        h.assertTrue(inspected.status() == Pen.Status.READY,"The authored parent positions must satisfy real courtship bounds: "+inspected.status());
        for (int slot = 0; slot < 2; slot++) {
            var fruit = new ItemStack(ModItems.GUAVA.get()); player.setItemInHand(InteractionHand.MAIN_HAND,fruit);
            h.assertTrue(pen.insertFromHand(player,fruit,slot) == 1 && fruit.isEmpty(),"The actual kennel controller must consume real hand stock");
        }
        h.assertTrue(pen.activate(false) == Pen.Status.FED && a.isInLove() && b.isInLove()
                && pen.food(0).isEmpty() && pen.food(1).isEmpty(),"A placed regional kennel must feed its own paid pair without manufactured seed output");
        a.discard(); b.discard();
    }
    private static void arena(GameTestHelper h, BlockPos origin, Rotation rotation, net.minecraft.server.level.ServerPlayer player) {
        var level = h.getLevel(); var stand = at(origin,rotation,15,1,7); var state = level.getBlockState(stand);
        h.assertTrue(state.is(ModBlocks.JUDGING_STAND.get()) && level.getBlockState(at(origin,rotation,4,0,12)).is(ModBlocks.NEST.get())
                && level.getBlockState(at(origin,rotation,4,0,19)).is(ModBlocks.NEST.get())
                && level.getBlockState(at(origin,rotation,15,1,29)).isAir(),"The arena's actual judging stand, two bays and wide entrance must survive rotation");
        var champion = dog(level,at(origin,rotation,15,1,11));
        champion.setParents(Genome.DEFAULT,Genome.DEFAULT); champion.setStats(Stats.of(50,50,50,50,50));
        champion.setLeashedTo(player,false); player.setPos(Vec3.atCenterOf(at(origin,rotation,15,1,8)));
        player.getInventory().clearContent(); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var hit = new BlockHitResult(Vec3.atCenterOf(stand),Direction.UP,stand,false);
        var scorecard=CareMenuGameTest.open(h,player,stand);
        h.assertTrue(champion.highestShowAward()==0,"A public arena scorecard must inspect without issuing a ribbon");
        h.assertTrue(scorecard.clickMenuButton(player,scorecard.buttonId(WorkshopPanels.AWARD))
                && !scorecard.clickMenuButton(player,scorecard.buttonId(WorkshopPanels.AWARD)),"A public arena award requires one explicit claim");
        player.closeContainer();
        ItemStack ribbon = ItemStack.EMPTY; int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var item = player.getInventory().getItem(slot);
            if (item.is(ModItems.GOLD_RIBBON.get())) { count += item.getCount(); ribbon = item.copy(); }
        }
        h.assertTrue(count == 1 && champion.highestShowAward() == 3 && ShowRibbonItem.read(ribbon).orElseThrow().id().orElseThrow().equals(champion.getUUID()),
                "The marked place must judge a real qualified entrant once and award its genuine snapshot");
        var pedestal = (TrophyPedestalBlockEntity)level.getBlockEntity(at(origin,rotation,18,1,5));
        h.assertTrue(pedestal != null && pedestal.item().isEmpty(),"Discovered trophy pedestals start empty");
        pedestal.setItem(ribbon); champion.discard();
    }
}
