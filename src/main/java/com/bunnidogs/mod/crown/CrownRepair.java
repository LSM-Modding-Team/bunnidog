package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit old-site repair: never reruns encounter generation, refills loot, or loads distant chunks. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class CrownRepair {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bunnidogs").then(Commands.literal("dungeon").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.literal("repair").executes(c->{
            var player=c.getSource().getPlayerOrException();var level=player.level();
            var structure=level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("bunnidogs","crown_warren"))).value();
            var start=level.structureManager().getStructureAt(player.blockPosition(),structure);
            if(!start.isValid()) {c.getSource().sendFailure(Component.translatable("message.bunnidogs.crown.no_dungeon"));return 0;}
            int count=0;
            for(var part:start.getPieces())if(part instanceof CrownWarrenPiece piece)count+=repair(level,piece);
            int changed=count;c.getSource().sendSuccess(()->Component.translatable("message.bunnidogs.crown.repaired",changed),false);return count;
        }))));
    }
    public static int repair(net.minecraft.server.level.ServerLevel level,CrownWarrenPiece piece) {
        if(piece.layoutVersion()==2)return CrownCitadelPlacement.repair(level,piece);
        var center=piece.center();int changed=0;
        for(int x=-90;x<=90;x++)for(int z=-90;z<=90;z++) {
            if(!level.hasChunkAt(center.offset(x,0,z)))continue;
            for(int y=0;y<=64 && center.getY()+y<level.getMaxY();y++) {
                var pos=center.offset(x,y,z);var old=level.getBlockState(pos);var planned=CrownLayout.cell(x,y,z);
                if(old.hasBlockEntity()) {
                    if(planned!=null && planned.is(ModBlocks.CROWN_SEAL.get()) && level.getBlockEntity(pos) instanceof CrownSealBlockEntity seal && seal.site()==null) {seal.configure(piece.site(),center);changed++;}
                    continue;
                }
                boolean marker=planned!=null && (planned.is(ModBlocks.CROWN_SEAL.get()) || planned.is(Blocks.GOLD_BLOCK) || planned.getBlock() instanceof net.minecraft.world.level.block.StainedGlassBlock || planned.is(Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE)) || planned.is(Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.LIME)) || planned.is(Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.RED)));
                if(marker && (old.isAir() || CrownWarrenPiece.natural(old) || old.is(ModBlocks.ROOTSTONE.get()) || old.is(Blocks.CHISELED_TUFF_BRICKS))) {
                    if(!old.equals(planned)) {level.setBlock(pos,planned,2);changed++;}
                    if(level.getBlockEntity(pos) instanceof CrownSealBlockEntity seal)seal.configure(piece.site(),center);
                } else if(y>0 && (planned==null || planned.isAir()) && !old.isAir() && CrownWarrenPiece.natural(old)) {level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);changed++;}
            }
        }
        return changed;
    }
    private CrownRepair() {}
}
