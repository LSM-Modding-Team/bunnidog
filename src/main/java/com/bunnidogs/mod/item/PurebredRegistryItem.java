package com.bunnidogs.mod.item;

import java.util.Optional;
import java.util.function.Consumer;

import com.bunnidogs.mod.client.ClientScreens;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Purebreds;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** A portable collection of stable bred lines, rather than the ordinary book's individual animal log. */
public final class PurebredRegistryItem extends Item {
    public PurebredRegistryItem(Properties properties) { super(properties); }

    public static Pedigree read(ItemStack stack) {
        return Purebreds.read(stack.get(ModItems.PEDIGREE_DATA.get()));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof Bunnidog dog) || !dog.isAlive()) return InteractionResult.PASS;
        if (!dog.acceptsCare()) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        Pedigree.Entry entry = new Pedigree.Entry(dog.genome(), dog.motherGenome(), dog.fatherGenome(),
                Optional.of(dog.getUUID()), dog.earTag(), Optional.of(dog.stats()), dog.isBaby());
        // Native creative interaction supplies a temporary copy; records belong on the actual held book.
        register(player,player.getItemInHand(hand),entry,dog.blockPosition());
        return InteractionResult.SUCCESS;
    }
    private static void register(Player player, ItemStack stack, Pedigree.Entry entry, BlockPos soundAt) {
        Pedigree registry = read(stack);
        Purebreds.Result result = Purebreds.add(registry, entry);
        Component message = switch (result) {
            case ADDED -> Component.translatable("message.bunnidogs.registry.added", registry.size());
            case ALREADY -> Component.translatable("message.bunnidogs.registry.already");
            case FULL -> Component.translatable("message.bunnidogs.registry.full", Pedigree.MAX_ENTRIES);
            case PUP -> Component.translatable("message.bunnidogs.registry.pup");
            case PARENTS -> Component.translatable("message.bunnidogs.registry.parents");
            case MIXED -> Component.translatable("message.bunnidogs.registry.mixed", Purebreds.fixedCount(entry.genome()), Gene.COUNT);
        };
        if (result == Purebreds.Result.ADDED) {
            stack.set(ModItems.PEDIGREE_DATA.get(), registry.toString());
            player.level().playSound(null, soundAt, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, .35F, 1.1F);
            if (player instanceof ServerPlayer serverPlayer) {
                var advancement = serverPlayer.level().getServer().getAdvancements()
                        .get(Identifier.fromNamespaceAndPath("bunnidogs", "breeds_true"));
                if (advancement != null) serverPlayer.getAdvancements().award(advancement, "registered");
            }
        }
        player.sendOverlayMessage(message);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (recordCarried(player,hand)) return InteractionResult.SUCCESS;
        if (level.isClientSide()) ClientScreens.openRegistry(read(player.getItemInHand(hand)));
        return InteractionResult.SUCCESS;
    }
    private boolean recordCarried(Player player, InteractionHand hand) {
        return TravelBasketItem.recordCarried(player,hand,entry -> register(player,player.getItemInHand(hand),entry,player.blockPosition()));
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && recordCarried(context.getPlayer(),context.getHand())
                ? InteractionResult.SUCCESS : super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        lines.accept(Component.translatable("tooltip.bunnidogs.registry.count", read(stack).size(), Pedigree.MAX_ENTRIES).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.registry.use").withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.registry.goal").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.registry.snapshot").withStyle(ChatFormatting.DARK_GRAY));
        lines.accept(Component.translatable("tooltip.bunnidogs.records.carried").withStyle(ChatFormatting.DARK_GRAY));
    }
}
