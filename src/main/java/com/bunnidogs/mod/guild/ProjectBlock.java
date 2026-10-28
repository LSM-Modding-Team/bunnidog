package com.bunnidogs.mod.guild;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Native container screens, physical stock and direct world use; no instruction menu. */
public final class ProjectBlock extends BaseEntityBlock {
    public static final MapCodec<ProjectBlock> CODEC=simpleCodec(ProjectBlock::new);
    public ProjectBlock(Properties p) {super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec() {return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s) {return new ProjectStation(p,s);}
    @Override public RenderShape getRenderShape(BlockState s) {return RenderShape.MODEL;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(level.getBlockEntity(pos) instanceof ProjectStation station)station.use(player);return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit) {
        return useWithoutItem(state,level,pos,player,hit);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t) {return l.isClientSide() ? null : createTickerHelper(t,GuildProjects.STATION.get(),ProjectStation::tick);}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(l,p,s,who,stack);if(l.getBlockEntity(p) instanceof ProjectStation station && who instanceof Player player)station.owner(player.getUUID());
    }
}
