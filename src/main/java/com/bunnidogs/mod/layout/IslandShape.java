package com.bunnidogs.mod.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/** Stable version-one island: a rooted shoal, sheltered homes, guava tree and contained native tide garden. */
public final class IslandShape {
    private IslandShape() {}
    public static final int RADIUS = IslandSite.RADIUS, ABOVE = 16;
    public enum Mat { AIR, STONE, DIRT, GRASS, COARSE_DIRT, SAND, SANDSTONE, SLAB, STAIRS, LOG, LEAVES, FRUIT, NEST, TROUGH, BELL, LANTERN, CHEST, WATER, KELP_PLANT, KELP, SEAGRASS, PATH }
    public record Cell(int dx, int dy, int dz, Mat material, int detail) {}
    public record Spot(int dx, int dy, int dz) {}
    public static final List<Spot> FOUNDERS = List.of(new Spot(-3,4,-4),new Spot(0,4,-4),new Spot(3,4,-4));
    public static boolean contains(int x, int z, long seed) {
        if (Math.abs(x) > RADIUS || Math.abs(z) > RADIUS) return false;
        double wave = .65*Math.sin(Math.atan2(z,x)*3+(seed&1023)*.01);
        return Math.hypot(x,z)+wave <= RADIUS;
    }
    public static int top(int x, int z) { return 3-(int)Math.floor(Math.max(0,Math.hypot(x,z)-11)*.42); }
    public static int bottom(int x, int z, int bedRelative) {
        return Math.max(bedRelative,-3-(int)Math.floor(Math.max(0,RADIUS-Math.hypot(x,z))*1.5));
    }
    public static Mat terrain(int x,int y,int z,long seed) {
        int top=top(x,z);
        if (y<top-3) return Mat.STONE;
        if (top<=1) return Mat.SAND;
        if (y<top) return Mat.DIRT;
        return Math.floorMod(x*31+z*17+(int)seed,19)<3 ? Mat.COARSE_DIRT : Mat.GRASS;
    }
    public static List<Cell> decorations(long seed) {
        var cells = new LinkedHashMap<Spot,Cell>();
        class Draw {
            void block(int x, int y, int z, Mat material) { block(x,y,z,material,0); }
            void block(int x, int y, int z, Mat material, int detail) { cells.put(new Spot(x,y,z),new Cell(x,y,z,material,detail)); }
            void area(int x1,int x2,int y1,int y2,int z1,int z2,Mat material) {
                for (int x=x1;x<=x2;x++) for (int y=y1;y<=y2;y++) for (int z=z1;z<=z2;z++) block(x,y,z,material);
            }
        }
        var p = new Draw();
        p.area(-5,5,4,6,-6,1,Mat.AIR);
        p.area(-5,5,4,6,-6,-6,Mat.SANDSTONE);
        for (int x : new int[]{-5,5}) p.area(x,x,4,6,-5,-1,Mat.SANDSTONE);
        for (int x : new int[]{-5,5}) for (int z : new int[]{-6,1}) p.area(x,x,4,6,z,z,Mat.LOG);
        p.area(-6,6,7,7,-7,2,Mat.SLAB);
        for (var spot : FOUNDERS) p.block(spot.dx(),spot.dy()-1,spot.dz(),Mat.NEST);
        p.block(-4,4,0,Mat.TROUGH); p.block(0,4,1,Mat.BELL); p.block(0,6,-2,Mat.LANTERN);
        p.block(8,4,4,Mat.TROUGH);
        p.block(4,4,-2,Mat.CHEST);
        for (int z=3;z<=20;z++) for (int x=-1;x<=1;x++) {
            boolean rise=top(x,z)<top(x,z-1);
            p.block(x,top(x,z),z,rise ? Mat.SANDSTONE : Mat.PATH);
            if (rise) p.block(x,top(x,z)+1,z,Mat.STAIRS);
        }
        // A normal decaying guava tree; exact native leaf distances belong to the saved layout.
        var tree = TreeShape.generate(new Random(seed^0x749AE44DL)); var distances = TreeShape.leafDistances(tree);
        for (var c : tree) p.block(c.dx()-7,c.dy()+4,c.dz()+7,
                switch(c.kind()) { case LOG -> Mat.LOG; case LEAVES -> Mat.LEAVES; case FRUIT -> Mat.FRUIT; },
                c.kind() == TreeShape.Kind.LEAVES ? distances.get(c) : 0);
        // The water is below a dry sand rim, so it remains a pool rather than flowing through the homes.
        p.area(9,13,3,3,2,6,Mat.SAND);
        p.area(10,12,1,1,3,5,Mat.SAND);
        p.area(10,12,2,3,3,5,Mat.WATER);
        p.block(11,2,4,Mat.KELP_PLANT); p.block(11,3,4,Mat.KELP);
        p.block(10,2,3,Mat.SEAGRASS); p.block(12,2,5,Mat.SEAGRASS);
        var out = new ArrayList<>(cells.values());
        out.sort(Comparator.comparingInt(Cell::dy).thenComparingInt(Cell::dz).thenComparingInt(Cell::dx));
        return List.copyOf(out);
    }
}
