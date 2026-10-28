package com.bunnidogs.mod.realm;

import java.util.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** End-inspired seeded island fields, mountain relief and shortest routes through intermediate islands. No world queries. */
public final class RealmTerrain {
    public static final int LAND_Y=80,MOON_Y=244,CROWN_SPACING=512,CROWN_SEPARATION=256;
    public static final int REGION=CROWN_SPACING*16,GRID=256,TILE=256;
    public static final RandomSpreadStructurePlacement CROWNS=new RandomSpreadStructurePlacement(CROWN_SPACING,CROWN_SEPARATION,RandomSpreadType.LINEAR,926481503);
    public record Site(int x,int z) {}
    public record Edge(Site a,Site b) {}
    private static final Comparator<Edge> EDGE_ORDER=Comparator.comparingInt((Edge e)->e.a.x).thenComparingInt(e->e.a.z).thenComparingInt(e->e.b.x).thenComparingInt(e->e.b.z);
    public record Moon(int x,int y,int z,int radius) {}
    public record Column(int bottom,int top,int moonBottom,int moonTop,boolean bridge) {}
    public record Island(Site at,boolean crown,double rx,double rz,double sin,double cos,int base,double phase,double warpX,double warpZ) {}
    public record Tile(List<Site> sites,List<Edge> edges,List<Moon> moons,List<Site> outliers,List<Island> islands) {}
    private record Route(List<Site> points,List<Edge> spans) {}
    private record Step(int index,double cost) {}
    private final long seed;
    private final SimplexNoise islandNoise,relief,detail,caves;
    private final Map<Long,Tile> tiles=lru(256);
    private final Map<Edge,Route> routes=lru(192);
    private static <K,V> Map<K,V> lru(int size) {return Collections.synchronizedMap(new LinkedHashMap<K,V>(128,.75f,true) {@Override protected boolean removeEldestEntry(Map.Entry<K,V> e) {return size()>size;}});}
    public RealmTerrain(long seed) {this.seed=seed;var random=RandomSource.create(seed);random.consumeCount(17292);islandNoise=new SimplexNoise(random);relief=new SimplexNoise(random);detail=new SimplexNoise(random);caves=new SimplexNoise(random);}
    public Site site(int rx,int rz) {var c=CROWNS.getPotentialStructureChunk(seed,rx*CROWN_SPACING,rz*CROWN_SPACING);return new Site(c.getMiddleBlockX(),c.getMiddleBlockZ());}
    public static long hash(long n) {n=(n^(n>>>30))*0xbf58476d1ce4e5b9L;n=(n^(n>>>27))*0x94d049bb133111ebL;return n^(n>>>31);}
    public long hash(int x,int y,int z) {return hash(seed ^ x*341873128712L ^ z*132897987541L ^ y*42317861L);}
    private static double distance(Site a,Site b) {return Math.hypot(a.x-b.x,a.z-b.z);}
    private static Edge edge(Site a,Site b) {return a.x<b.x || a.x==b.x && a.z<b.z ? new Edge(a,b) : new Edge(b,a);}
    private Site node(int x,int z) {long h=hash(x,419,z);return new Site(x*GRID+128+(int)Math.floorMod(h,121)-60,z*GRID+128+(int)Math.floorMod(h>>>13,121)-60);}
    private boolean present(int x,int z) {return islandNoise.getValue(x*.61,z*.61)<.10 || Math.floorMod(x,8)==0 && Math.floorMod(z,8)==0;}
    private Island island(Site at,boolean crown) {
        long h=hash(at.x,73,at.z);double angle=Math.floorMod(h,6283)/1000.0;
        boolean fragment=!crown && Math.floorMod(h>>>26,7)==0;
        double rx=crown ? 270+Math.floorMod(h,55) : fragment ? 24+Math.floorMod(h,40) : 64+Math.floorMod(h,127);
        double rz=crown ? 240+Math.floorMod(h>>>8,75) : fragment ? 22+Math.floorMod(h>>>8,35) : 48+Math.floorMod(h>>>8,125);
        return new Island(at,crown,rx,rz,Math.sin(angle),Math.cos(angle),crown ? LAND_Y : 60+(int)Math.floorMod(h>>>20,44),angle,detail.getValue(at.x*.009,at.z*.009),detail.getValue(at.x*.009+91,at.z*.009-31));
    }
    private Moon moon(int x,int z) {long h=hash(x,984,z);return new Moon(x*1024+512+(int)Math.floorMod(h,501)-250,MOON_Y+(int)Math.floorMod(h>>>12,19)-9,z*1024+512+(int)Math.floorMod(h>>>22,501)-250,42+(int)Math.floorMod(h>>>32,23));}
    public Site nearest(int x,int z) {
        int rx=Math.floorDiv(x,REGION),rz=Math.floorDiv(z,REGION);Site found=null;double best=Double.POSITIVE_INFINITY;
        for(int a=rx-1;a<=rx+1;a++)for(int b=rz-1;b<=rz+1;b++) {var s=site(a,b);double d=Math.hypot(s.x-x,s.z-z);if(d<best) {best=d;found=s;}}return found;
    }
    /** A fresh expedition starts on ordinary land, away from Crown clearings and overhead moons. */
    public Site arrival(int x,int z) {
        int cx=Math.floorDiv(x,GRID),cz=Math.floorDiv(z,GRID);Site found=null;double best=Double.POSITIVE_INFINITY;
        for(int a=cx-4;a<=cx+4;a++)for(int b=cz-4;b<=cz+4;b++)if(present(a,b)) {
            var s=node(a,b);double d=Math.hypot(s.x-x,s.z-z);if(d>=best || distance(s,nearest(s.x,s.z))<700)continue;
            var c=column(s.x,s.z);if(c.top<48 || c.moonTop>=0)continue;found=s;best=d;
        }return found;
    }
    public List<Edge> crownLinks(int rx,int rz) {
        var result=new LinkedHashSet<Edge>();
        // Four-site local minimum trees retain a connected large-scale network without a complete mesh.
        for(int a=rx-1;a<=rx+1;a++)for(int b=rz-1;b<=rz+1;b++) {
            var corners=List.of(site(a,b),site(a+1,b),site(a,b+1),site(a+1,b+1));var possible=new ArrayList<Edge>();
            for(int i=0;i<4;i++)for(int j=i+1;j<4;j++)possible.add(edge(corners.get(i),corners.get(j)));
            possible.sort(Comparator.comparingDouble((Edge e)->distance(e.a,e.b)).thenComparing(EDGE_ORDER));int[] parent={0,1,2,3};
            for(var e:possible) {int i=corners.indexOf(e.a),j=corners.indexOf(e.b);while(parent[i]!=i)i=parent[i];while(parent[j]!=j)j=parent[j];if(i!=j) {parent[i]=j;result.add(e);}}
        }return result.stream().sorted(EDGE_ORDER).toList();
    }
    /** Immutable seeded plan, also used by native path audits and terrain previews. */
    public List<Site> islandPath(Edge crownLink) {return route(edge(crownLink.a,crownLink.b)).points;}
    private int centerHeight(Site s) {return surface(island(s,false),s.x,s.z,1);}
    /** Bounded Dijkstra over neighboring island candidates; gaps and climbs cost more than existing land. */
    private Route route(Edge link) {
        var cached=routes.get(link);if(cached!=null)return cached;
        int x0=Math.floorDiv(Math.min(link.a.x,link.b.x),GRID)-1,z0=Math.floorDiv(Math.min(link.a.z,link.b.z),GRID)-1;
        int nx=Math.floorDiv(Math.max(link.a.x,link.b.x),GRID)-x0+2,nz=Math.floorDiv(Math.max(link.a.z,link.b.z),GRID)-z0+2,n=nx*nz,start=n,end=n+1;
        var points=new Site[n+2];var heights=new int[n+2];var exists=new boolean[n];
        for(int i=0;i<n;i++) {int a=x0+i/nz,b=z0+i%nz;points[i]=node(a,b);exists[i]=present(a,b);heights[i]=centerHeight(points[i]);}points[start]=link.a;points[end]=link.b;heights[start]=heights[end]=LAND_Y;
        double[] cost=new double[n+2];Arrays.fill(cost,Double.POSITIVE_INFINITY);int[] parent=new int[n+2];Arrays.fill(parent,-1);var queue=new PriorityQueue<Step>(Comparator.comparingDouble(Step::cost).thenComparingInt(Step::index));
        cost[start]=0;queue.add(new Step(start,0));
        while(!queue.isEmpty()) {
            var current=queue.remove();int i=current.index;if(current.cost!=cost[i])continue;if(i==end)break;
            var next=new ArrayList<Integer>(10);
            if(i==start) {int gx=Math.clamp(Math.floorDiv(link.a.x,GRID)-x0,0,nx-1),gz=Math.clamp(Math.floorDiv(link.a.z,GRID)-z0,0,nz-1);for(int a=Math.max(0,gx-1);a<=Math.min(nx-1,gx+1);a++)for(int b=Math.max(0,gz-1);b<=Math.min(nz-1,gz+1);b++)next.add(a*nz+b);}
            else {int a=i/nz,b=i%nz;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if((dx!=0 || dz!=0) && (dx==0 || dz==0 || ((hash(x0+Math.min(a,a+dx),7,z0+Math.min(b,b+dz))&1)==0)==(dx==dz)) && a+dx>=0 && a+dx<nx && b+dz>=0 && b+dz<nz)next.add((a+dx)*nz+b+dz);if(distance(points[i],link.b)<GRID*1.6)next.add(end);}
            for(int j:next) {double paid=cost[i]+distance(points[i],points[j])+Math.abs(heights[i]-heights[j])*3+(j<n && !exists[j] ? 165 : 0);if(paid<cost[j]) {cost[j]=paid;parent[j]=i;queue.add(new Step(j,paid));}}
        }
        if(parent[end]<0)throw new IllegalStateException("No bounded island route between "+link.a+" and "+link.b);
        var path=new ArrayList<Site>();for(int i=end;i>=0;i=parent[i]) {path.add(points[i]);if(i==start)break;}Collections.reverse(path);
        var spans=new ArrayList<Edge>();for(int i=1;i<path.size();i++)spans.add(edge(path.get(i-1),path.get(i)));
        var result=new Route(List.copyOf(path),List.copyOf(spans));routes.put(link,result);return result;
    }
    private static boolean near(Edge e,int x,int z,int margin) {return Math.max(e.a.x,e.b.x)>=x-margin && Math.min(e.a.x,e.b.x)<=x+TILE+margin && Math.max(e.a.z,e.b.z)>=z-margin && Math.min(e.a.z,e.b.z)<=z+TILE+margin;}
    public Tile tile(int x,int z) {
        int tx=Math.floorDiv(x,TILE),tz=Math.floorDiv(z,TILE);long key=new ChunkPos(tx,tz).pack();var cached=tiles.get(key);if(cached!=null)return cached;int ox=tx*TILE,oz=tz*TILE;
        int rx=Math.floorDiv(x,REGION),rz=Math.floorDiv(z,REGION);var crowns=new ArrayList<Site>();var natural=new LinkedHashSet<Site>();var spans=new LinkedHashSet<Edge>();var moons=new ArrayList<Moon>();
        for(int a=rx-1;a<=rx+1;a++)for(int b=rz-1;b<=rz+1;b++)crowns.add(site(a,b));
        for(int a=tx-2;a<=tx+2;a++)for(int b=tz-2;b<=tz+2;b++)if(present(a,b))natural.add(node(a,b));
        for(var link:crownLinks(rx,rz))if(near(link,ox,oz,GRID*2)) {
            var route=route(link);for(var s:route.points)if(s.x>=ox-400 && s.x<=ox+TILE+400 && s.z>=oz-400 && s.z<=oz+TILE+400 && !s.equals(link.a) && !s.equals(link.b))natural.add(s);
            for(var e:route.spans)if(near(e,ox,oz,16))spans.add(e);
        }
        var islands=new ArrayList<Island>();for(var s:crowns)if(s.x>=ox-512 && s.x<=ox+TILE+512 && s.z>=oz-512 && s.z<=oz+TILE+512)islands.add(island(s,true));
        for(var s:natural)if(crowns.stream().noneMatch(c->distance(c,s)<400))islands.add(island(s,false));
        int mx=Math.floorDiv(x,1024),mz=Math.floorDiv(z,1024);for(int a=mx-1;a<=mx+1;a++)for(int b=mz-1;b<=mz+1;b++)moons.add(moon(a,b));
        var result=new Tile(List.copyOf(crowns),spans.stream().sorted(EDGE_ORDER).toList(),List.copyOf(moons),List.copyOf(natural),List.copyOf(islands));tiles.put(key,result);return result;
    }
    private double shape(Island s,int x,int z) {
        double dx=x-s.at.x,dz=z-s.at.z;if(Math.abs(dx)>512 || Math.abs(dz)>512)return -1;
        double wx=dx+(detail.getValue(x*.009,z*.009)-s.warpX)*30,wz=dz+(detail.getValue(x*.009+91,z*.009-31)-s.warpZ)*30;
        double a=(wx*s.cos+wz*s.sin)/s.rx,b=(-wx*s.sin+wz*s.cos)/s.rz;
        double contour=1+.14*Math.sin(Math.atan2(b,a)*3+s.phase)+detail.getValue(x*.025,z*.025)*.12;
        double value=1-(a*a+b*b)/(contour*contour);
        // The dungeon itself is the only guaranteed flat square; its island's coast and hills remain varied.
        if(s.crown && Math.max(Math.abs(dx),Math.abs(dz))<=100)return 1;return value;
    }
    private int surface(Island s,int x,int z,double shape) {
        double foothill=Math.sqrt(Math.clamp(shape,0,1));
        double hills=relief.getValue(x*.004,z*.004)*29+(1-Math.abs(relief.getValue(x*.009+731,z*.009-821)))*25+detail.getValue(x*.035,z*.035)*5;
        int height=s.base+(int)(hills*foothill);
        if(s.crown) {double distance=Math.max(Math.abs(x-s.at.x),Math.abs(z-s.at.z));double blend=Math.clamp((distance-96)/65,0,1);height=(int)Math.round(LAND_Y*(1-blend)+height*blend);}return height;
    }
    private int roadHeight(Site s) {return site(Math.floorDiv(s.x,REGION),Math.floorDiv(s.z,REGION)).equals(s) ? LAND_Y : centerHeight(s);}
    private static double along(int x,int z,Edge e) {double dx=e.b.x-e.a.x,dz=e.b.z-e.a.z;return Math.clamp(((x-e.a.x)*dx+(z-e.a.z)*dz)/(dx*dx+dz*dz),0,1);}
    public Column column(int x,int z) {
        var t=tile(x,z);int bottom=320,top=-64,mb=320,mt=-64;boolean bridge=false;
        for(var s:t.islands) {double q=shape(s,x,z);if(q<0)continue;int surface=surface(s,x,z,q);top=Math.max(top,surface);bottom=Math.min(bottom,Math.min(surface-5,s.base-10-(int)(Math.sqrt(Math.min(1,q))*52+detail.getValue(x*.022,z*.022)*7)));}
        for(var e:t.edges) {
            double a=along(x,z,e);if(Math.hypot(x-e.a.x-a*(e.b.x-e.a.x),z-e.a.z-a*(e.b.z-e.a.z))>3.2)continue;
            // Never grade through an existing Crown footprint. Its approaches join the edge of the plateau.
            if(t.sites.stream().anyMatch(s->Math.max(Math.abs(x-s.x),Math.abs(z-s.z))<98))break;
            double height=roadHeight(e.a)*(1-a)+roadHeight(e.b)*a;
            // Round only the final grade: two consecutive roundings can create a two-block step.
            double shore=t.sites.stream().mapToDouble(s->Math.max(Math.abs(x-s.x),Math.abs(z-s.z))).min().orElse(1000);double blend=Math.clamp((shore-96)/96,0,1);bridge=true;top=(int)Math.round(LAND_Y*(1-blend)+height*blend);bottom=Math.min(bottom,top-2);break;
        }
        for(var m:t.moons) {double q=((x-m.x)*(double)(x-m.x)+(z-m.z)*(double)(z-m.z))/(m.radius*m.radius);if(q<1) {int h=(int)(Math.sqrt(1-q)*m.radius*.82);mb=m.y-h;mt=m.y+h;break;}}
        return new Column(bottom,top,mb,mt,bridge);
    }
    public boolean hollow(int x,int y,int z,Column c) {return y>c.bottom+3 && y<c.top-4 && caves.getValue(x*.035,y*.045,z*.035)>.57 && nearestCrownDistance(x,z)>150;}
    private double nearestCrownDistance(int x,int z) {return tile(x,z).islands.stream().filter(Island::crown).mapToDouble(s->Math.max(Math.abs(x-s.at.x),Math.abs(z-s.at.z))).min().orElse(1000);}
}
