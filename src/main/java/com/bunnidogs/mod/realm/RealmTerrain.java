package com.bunnidogs.mod.realm;

import java.util.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** Seeded, chunk-order-independent islands and straight nearest-site causeways. No world access. */
public final class RealmTerrain {
    public static final int LAND_Y=80, MOON_Y=244;
    public static final RandomSpreadStructurePlacement CROWNS=new RandomSpreadStructurePlacement(64,24,RandomSpreadType.LINEAR,926481503);
    public record Site(int x,int z) {}
    public record Edge(Site a,Site b) {}
    public record Moon(int x,int y,int z,int radius) {}
    public record Column(int bottom,int top,int moonBottom,int moonTop,boolean bridge) {}
    public record Tile(List<Site> sites,List<Edge> edges,List<Moon> moons,List<Site> outliers) {}
    private final long seed;
    private final Map<Long,Tile> tiles=Collections.synchronizedMap(new LinkedHashMap<>(128,.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long,Tile> e) {return size()>256;}
    });
    public RealmTerrain(long seed) {this.seed=seed;}
    public Site site(int rx,int rz) {var c=CROWNS.getPotentialStructureChunk(seed,rx*64,rz*64);return new Site(c.getMiddleBlockX(),c.getMiddleBlockZ());}
    public static long hash(long n) {n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
    public long hash(int x,int y,int z) {return hash(seed ^ x*341873128712L ^ z*132897987541L ^ y*42317861L);}
    private static double distance(Site a,Site b) {return Math.hypot(a.x-b.x,a.z-b.z);}
    public Tile tile(int x,int z) {
        int rx=Math.floorDiv(x,1024),rz=Math.floorDiv(z,1024);long key=new ChunkPos(rx,rz).pack();var t=tiles.get(key);if(t!=null)return t;
        var sites=new ArrayList<Site>();var edges=new ArrayList<Edge>();var moons=new ArrayList<Moon>();var outliers=new ArrayList<Site>();
        for(int a=rx-2;a<=rx+2;a++)for(int b=rz-2;b<=rz+2;b++)sites.add(site(a,b));
        // Each neighboring group of four dungeons uses its Euclidean minimum spanning tree.
        // Their union stays connected without adding every long diagonal or a pathfinding/world query.
        for(int a=rx-1;a<=rx+1;a++)for(int b=rz-1;b<=rz+1;b++) {
            Site from=site(a,b);long h=hash(a,0,b);
            moons.add(new Moon(from.x+210+(int)Math.floorMod(h,100),MOON_Y+(int)Math.floorMod(h>>>12,15)-7,from.z-230,34+(int)Math.floorMod(h>>>24,9)));
            outliers.add(new Site(from.x-340,from.z+240));outliers.add(new Site(from.x+340,from.z+330));
            var corners=List.of(from,site(a+1,b),site(a,b+1),site(a+1,b+1));
            var possible=new ArrayList<Edge>();for(int i=0;i<4;i++)for(int j=i+1;j<4;j++)possible.add(new Edge(corners.get(i),corners.get(j)));
            possible.sort(Comparator.comparingDouble(e->distance(e.a,e.b)));int[] parent={0,1,2,3};
            for(var e:possible) {int i=corners.indexOf(e.a),j=corners.indexOf(e.b);while(parent[i]!=i)i=parent[i];while(parent[j]!=j)j=parent[j];if(i!=j) {parent[i]=j;edges.add(e);}}
        }
        t=new Tile(List.copyOf(sites),List.copyOf(edges),List.copyOf(moons),List.copyOf(outliers));tiles.put(key,t);return t;
    }
    public Site nearest(int x,int z) {return tile(x,z).sites.stream().min(Comparator.comparingDouble(s->Math.hypot(s.x-x,s.z-z))).orElseThrow();}
    private static double edgeDistance(int x,int z,Edge e) {
        double dx=e.b.x-e.a.x,dz=e.b.z-e.a.z,t=Math.clamp(((x-e.a.x)*dx+(z-e.a.z)*dz)/(dx*dx+dz*dz),0,1);
        return Math.hypot(x-e.a.x-t*dx,z-e.a.z-t*dz);
    }
    public Column column(int x,int z) {
        var t=tile(x,z);int bottom=320,top=-64,mb=320,mt=-64;boolean bridge=false;
        for(var s:t.sites) {
            double d=Math.hypot(x-s.x,z-s.z),angle=Math.atan2(z-s.z,x-s.x);
            double radius=215+Math.sin(angle*5+s.x)*14+Math.sin(angle*9+s.z)*9;
            if(d>radius)continue;
            double edge=Math.sqrt(Math.max(0,1-d*d/(radius*radius)));
            int surface=d<125 ? LAND_Y : LAND_Y+(int)(Math.sin(x*.025)*Math.cos(z*.023)*7*Math.min(1,(d-125)/25));
            top=Math.max(top,surface);bottom=Math.min(bottom,LAND_Y-(int)(edge*57)-3);
        }
        for(var s:t.outliers) {
            double d=Math.hypot(x-s.x,z-s.z);if(d>65)continue;double e=Math.sqrt(1-d*d/(65*65));top=Math.max(top,66+(int)(e*10));bottom=Math.min(bottom,65-(int)(e*28));
        }
        for(var e:t.edges)if(edgeDistance(x,z,e)<=3.3) {if(top<LAND_Y)bridge=true;top=Math.max(top,LAND_Y);bottom=Math.min(bottom,LAND_Y-2);break;}
        for(var m:t.moons) {
            double d=((x-m.x)*(double)(x-m.x)+(z-m.z)*(double)(z-m.z))/(m.radius*m.radius);
            if(d<1) {int height=(int)(Math.sqrt(1-d)*m.radius*.82);mb=m.y-height;mt=m.y+height;}
        }
        return new Column(bottom,top,mb,mt,bridge);
    }
}
