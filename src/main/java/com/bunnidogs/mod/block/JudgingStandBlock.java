package com.bunnidogs.mod.block;

import java.util.Comparator;
import java.util.Optional;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.Sampling;
import com.bunnidogs.mod.genetics.Show;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.item.ShowRibbonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
        return player.getMainHandItem().isEmpty() ? judge(level, pos, player) : InteractionResult.PASS;
    }
    private InteractionResult judge(Level level, BlockPos pos, Player player) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var candidates = level.getEntitiesOfClass(Bunnidog.class, new AABB(pos).inflate(Show.RANGE),
                dog -> dog.isAlive() && !dog.isBaby() && !dog.isPassenger() && dog.isLeashed() && dog.getLeashHolder() == player
                        && dog.distanceToSqr(pos.getX()+.5, pos.getY()+1, pos.getZ()+.5) <= Show.RANGE*Show.RANGE);
        var candidate = candidates.stream().min(Comparator.comparingDouble((Bunnidog dog) ->
                dog.distanceToSqr(pos.getX()+.5, pos.getY()+1, pos.getZ()+.5)).thenComparing(Bunnidog::getUUID));
        if (candidate.isEmpty()) return message(player, "lead");
        var dog = candidate.orElseThrow();
        if (player.isShiftKeyDown()) {
            int lowest = Show.minimum(dog.stats());
            Stat limiting = Stat.VITALITY;
            for (Stat stat : Stat.values()) if (dog.stats().get(stat) == lowest) { limiting = stat; break; }
            player.sendOverlayMessage(Component.translatable("message.bunnidogs.show.preview",
                    Component.translatable("show.bunnidogs.tier." + Show.rank(dog.stats()).key()),
                    Plain.statName(limiting), lowest, Purebreds.fixedCount(dog.genome())));
            return InteractionResult.SUCCESS;
        }
        var entry = new Pedigree.Entry(dog.genome(), dog.motherGenome(), dog.fatherGenome(), Optional.of(dog.getUUID()),
                dog.earTag(), Optional.of(dog.stats()), false);
        var eligibility = Purebreds.eligibility(entry);
        if (eligibility == Purebreds.Result.PARENTS) return message(player, "parents");
        if (eligibility == Purebreds.Result.MIXED) return message(player, "mixed");
        if (!Sampling.healthy(dog.getHealth(), dog.getMaxHealth())) return message(player, "health");
        Show.Tier tier = Show.rank(dog.stats());
        if (tier == Show.Tier.NONE) return message(player, "stats");
        if (!dog.recordShowAward(tier.ordinal())) return message(player, "already");
        ItemStack ribbon = ShowRibbonItem.create(tier, entry);
        if (!player.getInventory().add(ribbon)) player.drop(ribbon, false);
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.show.awarded",
                Component.translatable("item.bunnidogs." + tier.key() + "_show_ribbon")));
        level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, .4F, 1.4F);
        if (tier == Show.Tier.GOLD && player instanceof ServerPlayer serverPlayer) {
            var advancement = serverPlayer.level().getServer().getAdvancements()
                    .get(Identifier.fromNamespaceAndPath("bunnidogs", "champion_line"));
            if (advancement != null) serverPlayer.getAdvancements().award(advancement, "judged");
        }
        return InteractionResult.SUCCESS;
    }
    private static InteractionResult message(Player player, String key) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.show." + key));
        return InteractionResult.SUCCESS;
    }
}
