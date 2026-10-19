package com.bunnidogs.mod.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.genetics.Infusion;
import com.bunnidogs.mod.genetics.Juice;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Guava juice. Eight uninfused carriers preserve their guava tier (drinking only restores a little hunger);
 * {@code infused_guava_juice} holds an {@link Infusion} (a
 * frozen "liquid pup" made from a seed, see {@link SeedItem#use}) in the data component {@code bunnidogs:infusion_data} and
 * gives the effects its genes call for ({@link Juice}).
 *
 * <p>Drinking is the vanilla drink animation with our own finish: every {@link Juice.Spec} is applied by looking its
 * effect up by registry id (an id that does not exist is skipped with a log line, never a crash). Effects aimed at
 * {@link Juice.Target#NEARBY} (the Glowing of the ears) go to every other living thing within the infusion's hearing radius.
 * The empty bottle comes back (survival).
 */
public class JuiceItem extends Item {
    /** Ticks of the drinking animation (a vanilla potion takes 32). */
    public static final int DRINK_TICKS = 32;
    private final GuavaTier tier;

    public JuiceItem(Properties properties) {
        this(properties,GuavaTier.PLAIN);
    }

    public JuiceItem(Properties properties, GuavaTier tier) {
        super(properties);
        this.tier = tier;
    }

    public static Optional<GuavaTier> carrier(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof JuiceItem juice && !stack.is(ModItems.INFUSED_JUICE.get())
                ? Optional.of(juice.tier) : Optional.empty();
    }

    /** A bottle of infused juice holding this liquid pup. */
    public static ItemStack create(Infusion infusion) {
        ItemStack stack = new ItemStack(ModItems.INFUSED_JUICE.get());
        stack.set(ModItems.INFUSION_DATA.get(), infusion.toString());
        return stack;
    }

    /** The liquid pup in this stack, or empty (a plain juice, or an infused one whose payload is missing or malformed). */
    public static Optional<Infusion> read(ItemStack stack) {
        if (!stack.is(ModItems.INFUSED_JUICE.get())) {
            return Optional.empty();
        }
        String text = stack.get(ModItems.INFUSION_DATA.get());
        return text == null ? Optional.empty() : Infusion.parse(text);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!level.isClientSide()) {
            Optional<Infusion> infusion = read(stack);
            List<Juice.Spec> specs = infusion.isPresent() ? infusion.get().specs() : List.of(Juice.plainSaturation());
            double radius = infusion.map(Infusion::areaRadius).orElse(Juice.AREA_RADIUS);
            for (Juice.Spec spec : specs) {
                apply(level, living, spec, radius);
            }
        }
        if (living instanceof Player player && player.hasInfiniteMaterials()) {
            return stack;
        }
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) {
            return bottle;
        }
        if (living instanceof Player player && !player.getInventory().add(bottle)) {
            player.drop(bottle, false);
        }
        return stack;
    }

    /** Gives one effect to the drinker or to everything around them. A nonexistent effect id is logged and skipped. */
    private static void apply(Level level, LivingEntity drinker, Juice.Spec spec, double radius) {
        Holder<MobEffect> effect;
        try {
            effect = level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT)
                    .getOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.parse(spec.id())));
        } catch (RuntimeException e) {
            BunnidogsMod.LOGGER.warn("Juice: no such effect '{}', skipped", spec.id());
            return;
        }
        if (spec.target() == Juice.Target.SELF) {
            drinker.addEffect(new MobEffectInstance(effect, spec.ticks(), spec.amplifier()));
            return;
        }
        AABB box = new AABB(drinker.blockPosition()).inflate(radius);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (other != drinker) {
                other.addEffect(new MobEffectInstance(effect, spec.ticks(), spec.amplifier()));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        Optional<Infusion> infusion = read(stack);
        if (infusion.isEmpty()) {
            lines.accept(Component.literal("Restores a little hunger").withStyle(ChatFormatting.GRAY));
            carrier(stack).ifPresent(t -> {
                lines.accept(Component.translatable("tooltip.bunnidogs.juice.carrier",t.qualityText()).withStyle(ChatFormatting.GRAY));
                lines.accept(Component.translatable("tooltip.bunnidogs.juice.infuse").withStyle(ChatFormatting.DARK_GRAY));
            });
            return;
        }
        for (String line : infusion.get().summary()) {
            lines.accept(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        if (flag.isAdvanced()) {
            lines.accept(Component.literal("Liquid pup " + infusion.get()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
