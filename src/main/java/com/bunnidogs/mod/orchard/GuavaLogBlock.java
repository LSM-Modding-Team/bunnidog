package com.bunnidogs.mod.orchard;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
/** The first ordinary punch of a standing guava tree also shakes its ripe canopy. */
public final class GuavaLogBlock extends RotatedPillarBlock {
    public static final MapCodec<GuavaLogBlock> CODEC=simpleCodec(GuavaLogBlock::new);
    public GuavaLogBlock(Properties p){super(p);}
    @Override public MapCodec<? extends RotatedPillarBlock> codec(){return CODEC;}
    @Override protected void attack(BlockState state,Level level,BlockPos pos,Player player){if(level instanceof ServerLevel server)Orchard.shake(server,pos,player);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!player.getMainHandItem().isEmpty())return InteractionResult.PASS;
        if(level instanceof ServerLevel server)Orchard.shake(server,pos,player);return InteractionResult.SUCCESS;
    }
}
