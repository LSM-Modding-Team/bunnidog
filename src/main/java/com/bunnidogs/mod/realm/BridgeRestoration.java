package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.BunnidogsMod;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit loaded-only upgrade for the former bare road. Never repastes terrain or player construction. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class BridgeRestoration {
    @SubscribeEvent public static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("bunnidogs").then(Commands.literal("bridges").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("renovate").executes(c->{
            int changed=renovate(c.getSource().getLevel(),BlockPos.containing(c.getSource().getPosition()),64);c.getSource().sendSuccess(()->Component.translatable("message.bunnidogs.guild.renovated",changed),false);return changed;
        }))));
    }
    private static boolean oldRoad(net.minecraft.world.level.block.state.BlockState s){return s.is(Blocks.END_STONE_BRICKS) || s.is(Blocks.SEA_LANTERN);}
    public static int renovate(ServerLevel level,BlockPos near,int radius) {
        if(!(level.getChunkSource().getGenerator() instanceof RealmChunkGenerator g))return 0;int changed=0;
        record Segment(BlockPos floor,RealmTerrain.Column column) {}
        var anchoredSegments=new java.util.ArrayList<Segment>();
        // Snapshot surviving old roads before replacing them: one side of a span must
        // not lose its anchor merely because the scan already renovated the other.
        for(int x=near.getX()-radius;x<=near.getX()+radius;x++)for(int z=near.getZ()-radius;z<=near.getZ()+radius;z++) {
            var c=g.terrain().column(x,z);if(!c.bridge())continue;var floor=new BlockPos(x,c.roadY(),z);if(!level.hasChunkAt(floor))continue;
            boolean anchored=oldRoad(level.getBlockState(floor));
            if(!anchored)for(int dx=-2;dx<=2 && !anchored;dx++)for(int dz=-2;dz<=2;dz++) {
                var at=floor.offset(dx,0,dz);if(level.hasChunkAt(at) && oldRoad(level.getBlockState(at))) {anchored=true;break;}
            }
            if(!anchored)continue;
            anchoredSegments.add(new Segment(floor,c));
        }
        for(var segment:anchoredSegments) {
            var floor=segment.floor();var c=segment.column();
            for(int y=-17;y<=7;y++) {
                var at=floor.above(y);var old=level.getBlockState(at);if(old.hasBlockEntity() || !old.isAir() && !oldRoad(old))continue;
                var replacement=BridgeArchitecture.state(c,y);if(replacement==null || replacement.isAir() || replacement==old)continue;
                if(level.setBlock(at,replacement,3))changed++;
            }
        }
        return changed;
    }
    private BridgeRestoration(){}
}
