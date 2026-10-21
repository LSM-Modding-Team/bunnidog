package com.bunnidogs.mod.block;

import com.bunnidogs.mod.network.TextComponents;
import com.bunnidogs.mod.entity.ShowJudging;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.genetics.Show;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.menu.CareMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A marker for a player-built show area. Uses the existing lead/nearest-animal interaction. */
public final class JudgingStandBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(Shapes.box(.125, 0, .125, .875, .125, .875),
            Shapes.box(.375, .125, .375, .625, .75, .625), Shapes.box(.0625, .75, .0625, .9375, .9375, .9375),
            Shapes.box(.25, .9375, .3125, .75, 1, .6875));
    public JudgingStandBlock(Properties properties) { super(properties); }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.is(Items.LEAD) ? judge(level, pos, player) : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return preview(level,pos,player);
        CareMenu.open(player,pos);
        return InteractionResult.SUCCESS;
    }
    private InteractionResult judge(Level level, BlockPos pos, Player player) {
        if (!player.mayBuild() || !level.mayInteract(player,pos)) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return preview(level,pos,player);
        if (!level.isClientSide()) ShowJudging.award(level,pos,player);
        return InteractionResult.SUCCESS;
    }
    private InteractionResult preview(Level level,BlockPos pos,Player player) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var reading=ShowJudging.inspect(level,pos,player);
        if (reading.candidate().isEmpty()) {
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.show.lead"));
            return InteractionResult.SUCCESS;
        }
        var dog=reading.candidate().orElseThrow(); int lowest=Show.minimum(dog.stats());
        Stat limiting=Stat.VITALITY;
        for (Stat stat:Stat.values()) if (dog.stats().get(stat)==lowest) { limiting=stat; break; }
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.show.preview",
                Component.translatable("show.bunnidogs.tier."+reading.tier().key()),TextComponents.stat(limiting),lowest,Purebreds.fixedCount(dog.genome())));
        return InteractionResult.SUCCESS;
    }
}
