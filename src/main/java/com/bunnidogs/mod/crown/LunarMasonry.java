package com.bunnidogs.mod.crown;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.neoforge.registries.*;

/** Player-craftable structural stone; entity terrain abilities cannot dismantle it. */
public final class LunarMasonry {
    public static final Map<String,DeferredBlock<? extends Block>> BLOCKS=new LinkedHashMap<>();
    public static void register(DeferredRegister.Blocks registry) {
        for(String name:List.of("lunar_rootstone","lunar_bricks","pale_lunar_tiles","lunar_tiles","cinderroot_beams","runic_lunar_glass","lunar_rootlight","cinderroot_fence")) {
            var base=registry.registerBlock(name,name.equals("cinderroot_fence") ? Fence::new : com.bunnidogs.mod.realm.RootstoneBlock::new,p->properties(name.equals("runic_lunar_glass") ? p.noOcclusion() : p).lightLevel(s->name.equals("lunar_rootlight") ? 13 : 0));BLOCKS.put(name,base);
            if(name.endsWith("bricks") || name.endsWith("tiles")) {
                BLOCKS.put(name+"_stairs",registry.registerBlock(name+"_stairs",p->new Stairs(base.get().defaultBlockState(),p),LunarMasonry::properties));
                BLOCKS.put(name+"_slab",registry.registerBlock(name+"_slab",Slab::new,LunarMasonry::properties));
                BLOCKS.put(name+"_wall",registry.registerBlock(name+"_wall",Wall::new,LunarMasonry::properties));
            }
        }
    }
    public static BlockState upgrade(BlockState state) {
        var key=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String out=CONVERSIONS.get(key);if(out==null)return state;
        BlockState result=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(out)).defaultBlockState();
        for(var property:state.getProperties())if(result.hasProperty(property))result=copyProperty(result,state,property);
        return result;
    }
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState to,BlockState from,net.minecraft.world.level.block.state.properties.Property<T> property){return to.setValue(property,from.getValue(property));}
    private static final Map<String,String> CONVERSIONS=conversions();
    private static Map<String,String> conversions() {
        try(var in=LunarMasonry.class.getResourceAsStream("/data/bunnidogs/lunar_masonry.json")) {
            var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();var map=new LinkedHashMap<String,String>();json.entrySet().forEach(e->map.put(e.getKey(),e.getValue().getAsString()));return Map.copyOf(map);
        }catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}
    }
    private static BlockBehaviour.Properties properties(BlockBehaviour.Properties p) {return p.strength(200,Float.MAX_VALUE).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK);}
    private static final class Fence extends FenceBlock {
        Fence(Properties p){super(p);}
        @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity e){return false;}
    }
    private static final class Stairs extends StairBlock {
        Stairs(BlockState base,Properties p){super(base,p);}
        @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity e){return false;}
    }
    private static final class Slab extends SlabBlock {
        Slab(Properties p){super(p);}
        @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity e){return false;}
    }
    private static final class Wall extends WallBlock {
        Wall(Properties p){super(p);}
        @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity e){return false;}
    }
    private LunarMasonry() {}
}
