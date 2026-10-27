package com.bunnidogs.mod.crown;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

/** Authored V2 coordinates. Each controller persists its resolved world-space contract, not a lookup
 * into a future blueprint. Missing saved geometry is always the original V1 dungeon. */
public final class CrownSitePlan {
    private static final JsonObject BLUEPRINT=load();
    public record Region(BlockPos min,BlockPos max) {
        public static final Codec<Region> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("Min").forGetter(Region::min),BlockPos.CODEC.fieldOf("Max").forGetter(Region::max)).apply(i,Region::new));
        public BoundingBox box() {return new BoundingBox(min.getX(),min.getY(),min.getZ(),max.getX(),max.getY(),max.getZ());}
        public boolean contains(Vec3 p,int margin) {return p.x>=min.getX()-margin && p.x<=max.getX()+1+margin && p.y>=min.getY()-margin && p.y<=max.getY()+1+margin && p.z>=min.getZ()-margin && p.z<=max.getZ()+1+margin;}
    }
    public record Gate(int kind,Region region) {
        public static final Codec<Gate> CODEC=RecordCodecBuilder.create(i->i.group(Codec.INT.fieldOf("Kind").forGetter(Gate::kind),Region.CODEC.fieldOf("Region").forGetter(Gate::region)).apply(i,Gate::new));
    }
    public record Geometry(BlockPos floor,Region bounds,List<BlockPos> pads,List<BlockPos> rewards,List<BlockPos> archives,BlockPos patrol,Region patrolBounds,List<Gate> gates,List<BlockPos> sockets) {
        public static final Codec<Geometry> CODEC=RecordCodecBuilder.create(i->i.group(
            BlockPos.CODEC.fieldOf("Floor").forGetter(Geometry::floor),Region.CODEC.fieldOf("Bounds").forGetter(Geometry::bounds),BlockPos.CODEC.listOf().fieldOf("Pads").forGetter(Geometry::pads),
            BlockPos.CODEC.listOf().fieldOf("Rewards").forGetter(Geometry::rewards),BlockPos.CODEC.listOf().fieldOf("Archives").forGetter(Geometry::archives),BlockPos.CODEC.fieldOf("Patrol").forGetter(Geometry::patrol),
            Region.CODEC.fieldOf("PatrolBounds").forGetter(Geometry::patrolBounds),Gate.CODEC.listOf().fieldOf("Gates").forGetter(Geometry::gates),BlockPos.CODEC.listOf().fieldOf("Sockets").forGetter(Geometry::sockets)
        ).apply(i,Geometry::new));
        public Geometry {pads=List.copyOf(pads);rewards=List.copyOf(rewards);archives=List.copyOf(archives);gates=List.copyOf(gates);sockets=List.copyOf(sockets);}
    }
    public record Tile(BlockPos offset,String name) {}
    public static List<Tile> tiles() {return BLUEPRINT.getAsJsonArray("tiles").asList().stream().map(e->{var a=e.getAsJsonArray();return new Tile(new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt()),a.get(3).getAsString());}).toList();}
    public static BlockPos seal(BlockPos center,int wing) {return pos(BLUEPRINT.getAsJsonArray("encounters").get(wing).getAsJsonObject().getAsJsonArray("seal"),center);}
    public static Geometry geometry(BlockPos center,int wing) {
        var o=BLUEPRINT.getAsJsonArray("encounters").get(wing).getAsJsonObject();
        var gates=BLUEPRINT.getAsJsonArray("gates").asList().stream().map(v->{var g=v.getAsJsonObject();return new Gate(g.get("kind").getAsInt(),region(g.getAsJsonArray("box"),center));}).toList();
        return new Geometry(pos(o.getAsJsonArray("floor"),center),region(o.getAsJsonArray("bounds"),center),positions(o.getAsJsonArray("pads"),center),positions(o.getAsJsonArray("rewards"),center),positions(o.getAsJsonArray("archives"),center),pos(o.getAsJsonArray("patrol"),center),region(o.getAsJsonArray("patrolBounds"),center),gates,positions(BLUEPRINT.getAsJsonArray("sockets"),center));
    }
    private static Region region(JsonArray a,BlockPos c) {return new Region(c.offset(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt()),c.offset(a.get(3).getAsInt(),a.get(4).getAsInt(),a.get(5).getAsInt()));}
    private static List<BlockPos> positions(JsonArray a,BlockPos c) {return a.asList().stream().map(e->pos(e.getAsJsonArray(),c)).toList();}
    private static BlockPos pos(JsonArray a,BlockPos c) {return c.offset(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    private static JsonObject load() {
        try(var stream=CrownSitePlan.class.getResourceAsStream("/data/bunnidogs/structure/crown_citadel.json")) {
            if(stream==null)throw new IOException("Missing Crown citadel blueprint");
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        } catch(IOException e) {throw new ExceptionInInitializerError(e);}
    }
    private CrownSitePlan() {}
}
