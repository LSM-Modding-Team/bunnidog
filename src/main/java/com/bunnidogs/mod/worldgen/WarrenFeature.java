package com.bunnidogs.mod.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.block.FeedingTroughBlock;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.HouseShape;
import com.bunnidogs.mod.layout.HouseDetails;
import com.bunnidogs.mod.layout.PathShape;
import com.bunnidogs.mod.layout.PropShape;
import com.bunnidogs.mod.layout.RegionalFarms;
import com.bunnidogs.mod.layout.Settlement;
import com.bunnidogs.mod.layout.TunnelShape;
import com.bunnidogs.mod.layout.WarrenShape;
import com.bunnidogs.mod.registry.ModBiomeTags;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A warren (Phase 4g), since Phase 6a a SETTLEMENT whose development level is its number of guava trees (1 to 5, see {@link Settlement}); from two trees on it also has burrow houses (Phase 6b) and from three trees on footpaths (Phase 6c, {@link PathShape}):
 * where wild bunnidogs live. This REPLACES the random natural spawns of Phase 3, so finding one is how you
 * get your first bunnidogs and your first guava. One guava tree in the middle, a patch of coarse dirt around it, 3-4 burrow holes
 * and 2-4 wild bunnidogs that never despawn. The lineage of the bunnidogs comes from the biome they stand in (their founder roll
 * on the first tick, unchanged since Phase 3). The layout is {@link WarrenShape} (pure Java, tested); this class puts it into the world.
 *
 * <p>Where warrens appear is DATA: two feature types ({@code warren} and {@code surface_warren}, same class), four placed features
 * (surface, caves, nether, end islands) and four biome modifiers, see data/bunnidogs/worldgen and data/bunnidogs/neoforge/biome_modifier.
 * The CAVE kind ({@code warren}, used by caves and the nether) looks DOWN from the position it is given for the first spot with two air
 * blocks above solid ground, because its position is a random height in the biome. The SURFACE kind ({@code surface_warren}, used by
 * the surface and the end islands) is given the top of the terrain and ONLY accepts that exact spot: if the top is water or anything
 * else you cannot stand on, it gives up instead of digging down into whatever cave lies below (which put surface-lineage warrens in
 * caves before session 8). Both give up quietly when there is no spot or the trunk has no room. {@code /bunnidogs warren} builds one
 * at your feet for testing (cave kind, so it also works inside a cave; {@code /bunnidogs warren surface} uses the surface kind).
 *
 * <p>UNEVEN GROUND (session 9): the surface kind follows the terrain. Every other column (the patch, burrows, bunnidogs, the other
 * trees, the houses) looks for its own ground between {@link #SURFACE_RISE} blocks above and {@link #SURFACE_DROP} below the floor of
 * the centre, taking the TOPMOST block of the column (never a cave pocket under water or rock); before, the window was 2 up and 3 down,
 * so on a slope the bunnidogs, trees and houses further out found no ground and were silently left out. A house on a slope gets its 3x3
 * pad levelled (the low columns are filled with dirt up to the highest one, at most {@link #HOUSE_MAX_SLOPE} blocks of difference).
 * The cave kind keeps the narrow window on purpose (a wider one would find the next chamber).
 */
public class WarrenFeature extends Feature<NoneFeatureConfiguration> {
    /** How far down from the given position a floor is searched for (caves and the nether, whose position is a random height). */
    public static final int SEARCH_DEPTH = 40;
    /**
     * The surface kind is given the first air block above the terrain, so its floor is that block or nothing: one position, no search.
     * (Below it the next positions are never standable anyway: the block under the start is solid or a fluid, not air.)
     */
    public static final int SURFACE_SEARCH_DEPTH = 1;
    /** Surface kind: a column is searched from this many blocks above the floor of the centre ... */
    public static final int SURFACE_RISE = 6;
    /** ... down to this many blocks below it (hills and valleys within the 12 blocks of a settlement). */
    public static final int SURFACE_DROP = 8;
    /** Cave kind: the narrow window (a cave floor is rarely further off, and a wider one would find the next chamber). */
    private static final int CAVE_RISE = 2;
    private static final int CAVE_DROP = 3;
    /** A house is built where its 3x3 columns differ by at most this much in height; the low ones are filled up to the highest. */
    public static final int HOUSE_MAX_SLOPE = 3;
    /** What {@code buildCluster} answers when the trunk has no room (otherwise the number of bunnidogs it put in). */
    private static final int NO_CLUSTER = -1;
    /** Never search below this height (the bottom of the world is at -64). */
    private static final int LOWEST = -60;
    private static final int FLAGS = 2;

    /** True for the surface kind: the floor must be the start position itself, never something found by digging down. */
    private final boolean surfaceOnly;

    public WarrenFeature(Codec<NoneFeatureConfiguration> codec, boolean surfaceOnly) {
        super(codec);
        this.surfaceOnly = surfaceOnly;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return build(context.level(), context.origin(), context.random(), surfaceOnly);
    }

    /** Builds a settlement of a random level on the first floor found at or below {@code origin} (the cave kind). Returns whether one was built. */
    public static boolean build(WorldGenLevel level, BlockPos origin, RandomSource random) {
        return build(level, origin, random, false);
    }

    /** Builds a settlement of a random level; with {@code surfaceOnly} the floor must be {@code origin} itself, otherwise the first one up to 40 blocks below. */
    public static boolean build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean surfaceOnly) {
        return buildSettlement(level, origin, random, surfaceOnly, 0).trees() > 0;
    }

    /** What a settlement really got: the number of guava trees (its development level), of burrow houses, of bunnidogs and of path blocks. Trees 0 = nothing was built. */
    public record Built(int trees, int houses, int dogs, int paths, int props) {}

    /**
     * Builds a settlement (Phases 6a and 6b): the development level is the number of guava trees. {@code requestedLevel} 1..5 forces the
     * level (the debug command), 0 rolls it by {@link Settlement#WEIGHTS}. The centre cluster goes on the floor found at the start
     * position and the whole settlement is given up if it does not fit; every other cluster is built on the ground around its
     * spot or skipped (see {@link #groundAt}: on the surface that ground follows the terrain). When all the trees are in,
     * {@link Settlement#housesFor} of the trees really built decides how many burrow houses are built: the candidate spots are tried in
     * order and the ones where the ground is too steep or the space is not free are skipped. Last come the footpaths (Phase 6c, from three trees
     * on): {@link PathShape#network} works out the columns from the trees and houses really built and {@link #layPath} lays each one where there
     * is grass or dirt to lay it on. One line goes to the log for every settlement
     * (built, or given up and why), so a world can be checked for where warrens do and do not appear.
     */
    public static Built buildSettlement(WorldGenLevel level, BlockPos origin, RandomSource random, boolean surfaceOnly, int requestedLevel) {
        Random rng = new Random(random.nextLong());
        String kind = surfaceOnly ? "surface" : "cave";
        BlockPos floor = findFloor(level, origin, surfaceOnly ? SURFACE_SEARCH_DEPTH : SEARCH_DEPTH);
        if (floor == null) {
            BunnidogsMod.LOGGER.info("Warren gave up at {} ({} kind): no standable floor", origin, kind);
            return new Built(0, 0, 0, 0, 0);
        }
        // one style for the whole settlement: the lineage of the biome at its centre (Phase 6f)
        Lineage style = ModBiomeTags.lineageAt(level, floor);
        Settlement settlement = requestedLevel <= 0 ? Settlement.generate(rng) : Settlement.generate(rng, requestedLevel);
        if (!GuavaTree.fits(level, floor, settlement.clusters().getFirst().shape().tree())) {
            BunnidogsMod.LOGGER.info("Warren gave up at {} ({} kind): no room for the trunk", floor, kind);
            return new Built(0, 0, 0, 0, 0);
        }
        boolean open = !surfaceOnly || style == Lineage.END;
        TunnelShape.Network underground = open || settlement.level() >= 2
                ? WarrenTunnels.plan(level, floor, settlement, rng.nextLong(), surfaceOnly, open) : TunnelShape.Network.empty();
        if (!underground.cells().isEmpty()) {
            WarrenTunnels.place(level, floor, WarrenTunnels.encode(underground), null, style);
            var signs = com.bunnidogs.mod.layout.Wayfinding.plan(settlement,underground,floor.getX(),floor.getZ());
            WarrenSigns.place(level,floor,com.bunnidogs.mod.layout.Wayfinding.encode(signs),null);
            WarrenFixtures.place(level,floor,com.bunnidogs.mod.layout.SettlementFixtures.encode(
                    com.bunnidogs.mod.layout.SettlementFixtures.plan(settlement,underground,signs)),null);
        }
        int trees = 0;
        int dogs = 0;
        List<Settlement.Cluster> builtTrees = new ArrayList<>();
        int clusterIndex = 0;
        for (Settlement.Cluster cluster : settlement.clusters()) {
            BlockPos at = trees == 0 && cluster.dx() == 0 && cluster.dz() == 0
                    ? floor
                    : groundAt(level, floor.getX() + cluster.dx(), floor.getY(), floor.getZ() + cluster.dz(), surfaceOnly);
            boolean networkDen = underground.dens().contains(clusterIndex++);
            int here = at == null ? NO_CLUSTER : buildCluster(level, at, cluster.shape(), rng, surfaceOnly, style, settlement.level() == 1 && !open, networkDen,true);
            if (here == NO_CLUSTER) {
                if (trees == 0 && cluster.dx() == 0 && cluster.dz() == 0) {
                    BunnidogsMod.LOGGER.info("Warren gave up at {} ({} kind): no room for the trunk", floor, kind);
                    return new Built(0, 0, 0, 0, 0);
                }
                continue;
            }
            trees++;
            builtTrees.add(cluster);
            dogs += here;
        }
        int wanted = Settlement.housesFor(trees);
        int houses = 0;
        List<Settlement.House> builtHouses = new ArrayList<>();
        for (Settlement.House house : settlement.houses()) {
            if (houses == wanted) {
                break;
            }
            if (TunnelShape.blocksHouse(underground, house)) continue;
            if (buildHouse(level, floor.getX() + house.dx(), floor.getY(), floor.getZ() + house.dz(), house, surfaceOnly, style, trees)) {
                houses++;
                builtHouses.add(house);
            }
        }
        // landmarks (Phase 6e): the first ones the level of the trees really built allows; the surface kind only (water and farmland need open sky)
        int props = 0;
        List<Settlement.Prop> builtProps = new ArrayList<>();
        int propsWanted = surfaceOnly ? PropShape.kindsFor(trees).size() : 0;
        for (Settlement.Prop prop : settlement.props().subList(0, Math.min(propsWanted, settlement.props().size()))) {
            if (buildProp(level, floor.getX() + prop.dx(), floor.getY(), floor.getZ() + prop.dz(), prop, true, style,true,true)) {
                props++;
                builtProps.add(prop);
            }
        }
        int paths = 0;
        for (PathShape.Cell cell : PathShape.network(builtTrees, builtHouses, builtProps)) {
            if (underground.pathExclusions().contains(new DenShape.Pos(cell.dx(), cell.dz()))) continue;
            if (layPath(level, floor.getX() + cell.dx(), floor.getY(), floor.getZ() + cell.dz(), surfaceOnly, style)) {
                paths++;
            }
        }
        BunnidogsMod.LOGGER.info("Warren built at {} ({} kind, {} style): {} of {} trees, {} of {} houses, {} bunnidogs, {} path blocks, {} of {} landmarks; {} network dens, {} entrances, {} cellar ramps",
                floor, kind, style.key(), trees, settlement.level(), houses, wanted, dogs, paths, props, propsWanted, underground.dens().size(), underground.mouths().size(), underground.cellars().size());
        return new Built(trees, houses, dogs, paths, props);
    }

    /**
     * One burrow house ({@link HouseShape}) around the column (x, z). Every one of its 3x3 columns needs ground (see {@link #groundAt});
     * the columns may differ in height by at most {@link #HOUSE_MAX_SLOPE}, the low ones are filled with dirt up to the highest one
     * so the floor is level. Every block of the house must be free (air or something replaceable, never a fluid), and so must the two
     * blocks in front of the doorway, or the house would be walled in by the hill. {@code devLevel} (the trees built, or the planned level of a
     * structure piece) decides the guava-wood tier ({@link HouseShape#tierFor}). Returns false, having changed nothing, if it does not fit.
     */
    static boolean buildHouse(WorldGenLevel level, int x, int y, int z, Settlement.House house, boolean surface, Lineage style, int devLevel) {
        return buildHouse(level,x,y,z,house,surface,style,devLevel,true);
    }

    static boolean buildHouse(WorldGenLevel level, int x, int y, int z, Settlement.House house, boolean surface, Lineage style, int devLevel, boolean woodDetails) {
        BlockPos centre = groundAt(level, x, y, z, surface);
        if (centre == null) {
            return false;
        }
        BlockPos[] ground = new BlockPos[9];
        int top = centre.getY();
        int low = top;
        int n = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos g = groundAt(level, x + dx, centre.getY(), z + dz, surface);
                if (g == null) {
                    return false;
                }
                ground[n++] = g;
                top = Math.max(top, g.getY());
                low = Math.min(low, g.getY());
            }
        }
        if (top - low > HOUSE_MAX_SLOPE) {
            return false;
        }
        BlockPos base = new BlockPos(x, top, z);
        for (BlockPos g : ground) {
            for (int fy = g.getY(); fy < top; fy++) {
                if (!GuavaTree.replaceable(level.getBlockState(new BlockPos(g.getX(), fy, g.getZ())))) {
                    return false;
                }
            }
        }
        List<HouseShape.Cell> cells = HouseShape.cells(house.doorDx(), house.doorDz(), WarrenPalette.houseForm(style), HouseShape.tierFor(devLevel));
        for (HouseShape.Cell cell : cells) {
            if (!GuavaTree.replaceable(level.getBlockState(base.offset(cell.dx(), cell.dy(), cell.dz())))) {
                return false;
            }
        }
        BlockPos outside = base.offset(house.doorDx() * 2, 0, house.doorDz() * 2);
        if (!GuavaTree.replaceable(level.getBlockState(outside)) || !GuavaTree.replaceable(level.getBlockState(outside.above()))) {
            return false;
        }
        for (BlockPos g : ground) {
            for (int fy = g.getY(); fy < top; fy++) {
                level.setBlock(new BlockPos(g.getX(), fy, g.getZ()), WarrenPalette.get(style, WarrenPalette.Role.FILL), FLAGS);
            }
        }
        for (HouseShape.Cell cell : cells) {
            BlockState state = switch (cell.kind()) {
                case WALL -> WarrenPalette.get(style, WarrenPalette.Role.WALL);
                case ROOF -> WarrenPalette.get(style, WarrenPalette.Role.ROOF);
                case OPEN -> Blocks.AIR.defaultBlockState();
                case WINDOW -> Blocks.GLASS.defaultBlockState();
                case BEAM -> WarrenPalette.get(style, WarrenPalette.Role.BEAM);
                case PLANK -> WarrenPalette.get(style, WarrenPalette.Role.PLANK);
                case PLANK_SLAB -> WarrenPalette.get(style, WarrenPalette.Role.PLANK_SLAB);
                case FOUNDATION -> WarrenPalette.get(style, WarrenPalette.Role.RING_A);
            };
            if (woodDetails && devLevel == 4 && WarrenPalette.houseForm(style) == HouseShape.Form.MOUND && cell.dy() == 1
                    && (Math.abs(cell.dx()) + Math.abs(cell.dz()) == 1)
                    && (cell.dx() != house.doorDx() || cell.dz() != house.doorDz())) state = Blocks.GLASS.defaultBlockState();
            level.setBlock(base.offset(cell.dx(), cell.dy(), cell.dz()), state, FLAGS);
        }
        // H3b (docs/HOME_SPECS.md): the floor block under the hollow centre becomes a Nest, so a house is a home for one dog. The hollow is
        // two (mound) or three (tower, loft) blocks high and its doorway is two high, so a dog standing on the nest has room and a way in.
        // After the cells, so nothing above overwrites it, and set the way H3 sets the nests beside the burrows (a point of interest).
        level.setBlock(base.below(), ModBlocks.NEST.get().defaultBlockState(), FLAGS);
        if (woodDetails) {
            for (HouseDetails.Cell cell : HouseDetails.cells(house.doorDx(),house.doorDz(),WarrenPalette.houseForm(style),devLevel)) {
                BlockPos at = base.offset(cell.dx(),cell.dy(),cell.dz());
                if (!GuavaTree.replaceable(level.getBlockState(at))) continue;
                Direction facing = cell.facingX() > 0 ? Direction.EAST : cell.facingX() < 0 ? Direction.WEST
                        : cell.facingZ() > 0 ? Direction.SOUTH : Direction.NORTH;
                BlockState detail = switch (cell.kind()) {
                    // Generated doors start open so existing dogs retain access to their house Nests.
                    case DOOR_LOWER, DOOR_UPPER -> ModBlocks.GUAVA_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,facing)
                            .setValue(DoorBlock.OPEN,true).setValue(DoorBlock.HALF,cell.kind() == HouseDetails.Kind.DOOR_LOWER ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
                    case SHUTTER -> ModBlocks.GUAVA_TRAPDOOR.get().defaultBlockState().setValue(TrapDoorBlock.FACING,facing).setValue(TrapDoorBlock.OPEN,true);
                    case BEAM -> WarrenPalette.get(style,WarrenPalette.Role.BEAM);
                    case LANTERN -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
                };
                if (cell.kind() == HouseDetails.Kind.LANTERN && !level.getBlockState(at.above()).isFaceSturdy(level,at.above(),Direction.DOWN)) continue;
                level.setBlock(at,detail,FLAGS);
            }
        }
        return true;
    }

    /**
     * One landmark of a village (Phase 6e, {@link PropShape}) around the column (x, z): the same ground rules as a burrow house (ground in
     * all nine columns, at most {@link #HOUSE_MAX_SLOPE} blocks of difference, the low columns filled with dirt up to the highest one, every
     * block above the ground free), but nothing is changed unless everything fits. Cells at {@code dy = -1} REPLACE the ground block
     * (paving, water, farmland). Returns whether it was built.
     */
    static boolean buildProp(WorldGenLevel level, int x, int y, int z, Settlement.Prop prop, boolean surface, Lineage style) {
        return buildProp(level,x,y,z,prop,surface,style,false);
    }
    static boolean buildProp(WorldGenLevel level, int x, int y, int z, Settlement.Prop prop, boolean surface, Lineage style, boolean garden) {
        return buildProp(level,x,y,z,prop,surface,style,garden,false);
    }
    static boolean buildProp(WorldGenLevel level, int x, int y, int z, Settlement.Prop prop, boolean surface,
            Lineage style, boolean garden, boolean regional) {
        BlockPos centre = groundAt(level, x, y, z, surface);
        if (centre == null) {
            return false;
        }
        BlockPos[] ground = new BlockPos[9];
        int top = centre.getY();
        int low = top;
        int n = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos g = groundAt(level, x + dx, centre.getY(), z + dz, surface);
                if (g == null || level.getBlockState(g.below()).hasBlockEntity()
                        || regional && prop.kind() == PropShape.Kind.FARM
                            && level.getBlockState(g.below()).is(BlockTags.PLANKS)) {
                    return false;
                }
                ground[n++] = g;
                top = Math.max(top, g.getY());
                low = Math.min(low, g.getY());
            }
        }
        if (top - low > HOUSE_MAX_SLOPE) {
            return false;
        }
        for (BlockPos g : ground) {
            for (int fy = g.getY(); fy < top; fy++) {
                if (!GuavaTree.replaceable(level.getBlockState(new BlockPos(g.getX(), fy, g.getZ())))) {
                    return false;
                }
            }
        }
        BlockPos base = new BlockPos(x, top, z);
        boolean fittedGarden = garden && prop.kind() == PropShape.Kind.FARM && (regional || style != Lineage.END)
                && fitsGarden(level,base,prop.turns());
        List<PropShape.Cell> cells = regional && prop.kind() == PropShape.Kind.FARM
                ? RegionalFarms.cells(style,prop.turns(),fittedGarden)
                : PropShape.cells(prop.kind(), prop.turns(),fittedGarden);
        for (PropShape.Cell cell : cells) {
            if (cell.dy() >= 0 && !GuavaTree.replaceable(level.getBlockState(base.offset(cell.dx(), cell.dy(), cell.dz())))) {
                return false;
            }
        }
        for (BlockPos g : ground) {
            for (int fy = g.getY(); fy < top; fy++) {
                level.setBlock(new BlockPos(g.getX(), fy, g.getZ()), WarrenPalette.get(style, WarrenPalette.Role.FILL), FLAGS);
            }
        }
        for (PropShape.Cell cell : cells) {
            BlockPos pos = base.offset(cell.dx(), cell.dy(), cell.dz());
            if (level.setBlock(pos, propBlock(style, cell.mat()), FLAGS) && cell.mat() == PropShape.Mat.BARREL)
                WarrenPantries.seedFresh(level,pos,style);
        }
        return true;
    }

    /** The corner stays inside the old farm; both outside neighbours must already offer safe walking space. */
    private static boolean fitsGarden(WorldGenLevel level, BlockPos base, int turns) {
        var corner = PropShape.cells(PropShape.Kind.FARM,turns,true).stream().filter(c -> c.mat() == PropShape.Mat.TROUGH).findFirst().orElseThrow();
        BlockPos pos = base.offset(corner.dx(),0,corner.dz());
        for (BlockPos p : List.of(pos,pos.offset(Integer.signum(corner.dx()),0,0),pos.offset(0,0,Integer.signum(corner.dz())))) {
            if (!level.hasChunkAt(p) || !level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()
                    || !level.getFluidState(p).isEmpty() || !level.getFluidState(p.above()).isEmpty()) return false;
            if (!p.equals(pos) && !level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)) return false;
        }
        return true;
    }

    private static BlockState propBlock(Lineage style, PropShape.Mat mat) {
        return switch (mat) {
            case HAY -> WarrenPalette.get(style, WarrenPalette.Role.HAY);
            case FILL -> WarrenPalette.get(style, WarrenPalette.Role.FILL);
            case TROUGH -> ModBlocks.FEEDING_TROUGH.get().defaultBlockState()
                    .setValue(FeedingTroughBlock.TENDING,true);
            case COBBLE -> WarrenPalette.get(style, WarrenPalette.Role.RING_A);
            case MOSSY -> WarrenPalette.get(style, WarrenPalette.Role.RING_B);
            case FENCE -> WarrenPalette.get(style, WarrenPalette.Role.FENCE);
            case SLAB -> WarrenPalette.get(style, WarrenPalette.Role.SLAB);
            case FARMLAND -> WarrenPalette.get(style, WarrenPalette.Role.SOIL);
            case WHEAT -> WarrenPalette.get(style, WarrenPalette.Role.SPROUT_A);
            case CARROTS -> WarrenPalette.get(style, WarrenPalette.Role.SPROUT_B);
            case POTATOES -> WarrenPalette.get(style, WarrenPalette.Role.SPROUT_C);
            case BARREL -> Blocks.BARREL.defaultBlockState();
            case WATER -> Blocks.WATER.defaultBlockState();
            case LANTERN -> Blocks.LANTERN.defaultBlockState();
            case PUMPKIN -> Blocks.PUMPKIN.defaultBlockState();
            case MELON -> Blocks.MELON.defaultBlockState();
            case SAND -> Blocks.SAND.defaultBlockState();
            case CACTUS -> Blocks.CACTUS.defaultBlockState();
            case SOUL_SAND -> Blocks.SOUL_SAND.defaultBlockState();
            case NETHER_WART -> Blocks.NETHER_WART.defaultBlockState();
            case MYCELIUM -> Blocks.MYCELIUM.defaultBlockState();
            case RED_MUSHROOM -> Blocks.RED_MUSHROOM.defaultBlockState();
            case BROWN_MUSHROOM -> Blocks.BROWN_MUSHROOM.defaultBlockState();
            case END_STONE -> Blocks.END_STONE.defaultBlockState();
            case CHORUS_FLOWER -> Blocks.CHORUS_FLOWER.defaultBlockState();
            case PLANK -> ModBlocks.GUAVA_PLANKS.get().defaultBlockState();
            case GLOW_VINE -> Blocks.CAVE_VINES.defaultBlockState().setValue(net.minecraft.world.level.block.CaveVines.BERRIES,true);
        };
    }

    /**
     * One block of footpath (Phase 6c) in the column (x, z): the first block from the top of the column's window (the same window as
     * {@link #groundAt}) that is not air, a leaf or a soft plant must be grass, dirt or coarse dirt with room above it, and becomes a
     * dirt path (a plant on top of it is removed, as it would be in the game). Anything else (rock, water, the trunk, a fruit, the
     * bottom of a burrow under a crown) leaves the column as it is. Returns whether a path block was laid. On the first block that is
     * not skipped it decides, so a path never digs down into a cave. Nether and End ground is not grass or dirt: no paths there.
     */
    static boolean layPath(WorldGenLevel level, int x, int y, int z, boolean surface, Lineage style) {
        int rise = surface ? SURFACE_RISE : CAVE_RISE;
        int drop = surface ? SURFACE_DROP : CAVE_DROP;
        for (int dy = rise; dy >= -drop; dy--) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof LeavesBlock || (state.getFluidState().isEmpty() && state.canBeReplaced())) {
                continue;
            }
            BlockPos above = pos.above();
            boolean soil = WarrenPalette.pathsOnSoilOnly(style)
                    ? state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                    : state.isFaceSturdy(level, pos, Direction.UP);
            if (!soil || !GuavaTree.replaceable(level.getBlockState(above))) {
                return false;
            }
            level.setBlock(pos, WarrenPalette.get(style, WarrenPalette.Role.PATH), FLAGS);
            if (!level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.AIR.defaultBlockState(), FLAGS);
            }
            return true;
        }
        return false;
    }

    /**
     * One tree with its patch, burrows, nests (H3) and bunnidogs on {@code floor}. Returns {@link #NO_CLUSTER}, having changed nothing, if the trunk
     * has no room; otherwise the number of bunnidogs put in (on steep ground a spot can have no ground, so the missing ones, down to the
     * minimum of a warren, are taken from the patch around the trunk).
     */
    static int buildCluster(WorldGenLevel level, BlockPos floor, WarrenShape warren, Random rng, boolean surface, Lineage style, boolean den) {
        return buildCluster(level, floor, warren, rng, surface, style, den, false);
    }

    static int buildCluster(WorldGenLevel level, BlockPos floor, WarrenShape warren, Random rng, boolean surface, Lineage style, boolean den, boolean networkDen) {
        return buildCluster(level,floor,warren,rng,surface,style,den,networkDen,false);
    }

    static int buildCluster(WorldGenLevel level, BlockPos floor, WarrenShape warren, Random rng, boolean surface, Lineage style,
            boolean den, boolean networkDen, boolean lifeDetails) {
        if (!GuavaTree.fits(level, floor, warren.tree())) {
            return NO_CLUSTER;
        }
        for (WarrenShape.Spot spot : warren.patch()) {
            BlockPos ground = groundAt(level, floor.getX() + spot.dx(), floor.getY(), floor.getZ() + spot.dz(), surface);
            if (ground != null) {
                BlockState soil = level.getBlockState(ground.below());
                if (soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.DIRT)) {
                    level.setBlock(ground.below(), Blocks.COARSE_DIRT.defaultBlockState(), FLAGS);
                }
            }
        }
        GuavaTree.place(level, floor, warren.tree());
        // U2 has already placed fitted higher-level networks. U1 still builds its own level-1 den here.
        // Without a fitted den, retain the old holes and surface nests.
        boolean denBuilt = networkDen || den && surface && WarrenDen.build(level, floor, style, rng,lifeDetails);
        if (!denBuilt) {
            for (WarrenShape.Spot spot : warren.burrows()) {
                BlockPos ground = groundAt(level, floor.getX() + spot.dx(), floor.getY(), floor.getZ() + spot.dz(), surface);
                if (ground != null && dig(level, ground.below())) {
                    dig(level, ground.below(2));
                }
            }
            // H3 (docs/HOME_SPECS.md): a Nest beside each burrow hole, BEFORE the dogs, so that no dog is ever put where a nest goes.
            // It is a plain block placed the way a village's bed is, so it registers as a point of interest and a wild dog can claim it.
            for (WarrenShape.Spot spot : warren.nests()) {
                BlockPos ground = groundAt(level, floor.getX() + spot.dx(), floor.getY(), floor.getZ() + spot.dz(), surface);
                if (ground != null) {
                    level.setBlock(ground, ModBlocks.NEST.get().defaultBlockState(), FLAGS);
                }
            }
        }
        ServerLevel server = level.getLevel();
        int dogs = 0;
        for (WarrenShape.Spot spot : warren.dogs()) {
            if (spawnDog(level, server, floor, spot, rng, surface)) {
                dogs++;
            }
        }
        for (WarrenShape.Spot spot : warren.patch()) {
            if (dogs >= WarrenShape.MIN_DOGS) {
                break;
            }
            boolean trunk = spot.dx() == 0 && spot.dz() == 0;
            if (!trunk && !warren.dogs().contains(spot) && !warren.burrows().contains(spot) && !warren.nests().contains(spot) && spawnDog(level, server, floor, spot, rng, surface)) {
                dogs++;
            }
        }
        return dogs;
    }

    /** One wild bunnidog on the ground of the column at {@code spot} from the floor; false if that column has no ground. */
    private static boolean spawnDog(WorldGenLevel level, ServerLevel server, BlockPos floor, WarrenShape.Spot spot, Random rng, boolean surface) {
        BlockPos ground = groundAt(level, floor.getX() + spot.dx(), floor.getY(), floor.getZ() + spot.dz(), surface);
        if (ground == null) {
            return false;
        }
        Bunnidog dog = ModEntities.BUNNIDOG.get().create(server, EntitySpawnReason.STRUCTURE);
        if (dog == null) {
            return false;
        }
        dog.snapTo(ground.getX() + 0.5D, ground.getY(), ground.getZ() + 0.5D, rng.nextFloat() * 360.0F, 0.0F);
        dog.setPersistenceRequired();
        level.addFreshEntityWithPassengers(dog);
        return true;
    }

    /** The first of the {@code depth} positions at or below {@code origin} with two air blocks above solid ground, or null. */
    static BlockPos findFloor(WorldGenLevel level, BlockPos origin, int depth) {
        for (int i = 0; i < depth; i++) {
            BlockPos pos = origin.below(i);
            if (pos.getY() < LOWEST) {
                return null;
            }
            if (isStandable(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    /**
     * The ground of the column (x, z) near the height {@code y}: the position to stand on, or null (no ground here, or too steep).
     * SURFACE kind: the topmost block of the column between {@link #SURFACE_RISE} above and {@link #SURFACE_DROP} below {@code y} decides:
     * air and soft plants are looked through, and the first block that is not is the ground if it is solid and has two free blocks above it.
     * If that block is a fluid or the window ends inside rock, the column has no ground (so a lake never lets a warren dig into the cave
     * below it, the bug of session 8). CAVE kind: the highest standable position between {@link #CAVE_RISE} above and {@link #CAVE_DROP} below.
     */
    static BlockPos groundAt(WorldGenLevel level, int x, int y, int z, boolean surface) {
        if (surface) {
            for (int dy = SURFACE_RISE; dy >= -SURFACE_DROP; dy--) {
                BlockPos pos = new BlockPos(x, y + dy, z);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || (state.getFluidState().isEmpty() && state.canBeReplaced())) {
                    continue;
                }
                if (!state.getFluidState().isEmpty()) {
                    return null;
                }
                return isStandable(level, pos.above()) ? pos.above() : null;
            }
            return null;
        }
        for (int dy = CAVE_RISE; dy >= -CAVE_DROP; dy--) {
            BlockPos pos = new BlockPos(x, y + dy, z);
            if (isStandable(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    /** Two air blocks here and above, solid ground below (a block you can stand on), and no fluid. */
    private static boolean isStandable(WorldGenLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** Removes one block of a burrow if it is something soft and plain: not a fluid, not a container, nothing harder than stone. */
    private static boolean dig(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        if (state.isAir() || !state.getFluidState().isEmpty() || state.hasBlockEntity() || hardness < 0.0F || hardness > 3.0F) {
            return false;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        return true;
    }
}
