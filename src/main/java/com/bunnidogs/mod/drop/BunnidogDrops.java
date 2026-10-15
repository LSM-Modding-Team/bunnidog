package com.bunnidogs.mod.drop;

import java.util.Random;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Drops;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.WornEarTagItem;
import com.bunnidogs.mod.registry.ModItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * What a dying bunnidog drops (docs/DROPS_SPECS.md, chunk D1). A bunnidog has no loot table, so this is the only source:
 * NeoForge fires {@link LivingDropsEvent} with the drops captured so far, and the stacks rolled by {@link Drops#roll}
 * are added to them (nothing is cancelled). The same pattern as {@code SeedCropHarvest}: a static subscriber, server side only.
 *
 * <p>The glue only gathers the circumstances (the animal's age, whether a player did it, fire, the killer's Looting) and
 * turns item ids into stacks; every number lives in the pure class {@link Drops}.
 */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class BunnidogDrops {
    private BunnidogDrops() {}

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Bunnidog dog) || !(dog.level() instanceof ServerLevel level)) {
            return;                                                  // only bunnidogs, and only on the server
        }
        if (!level.getGameRules().get(GameRules.MOB_DROPS)) {
            return;                                                  // our drops come from this event, not vanilla's loot step: honour the game rule here
        }
        DamageSource source = event.getSource();
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            // the cause of the damage (an arrow's shooter, not the arrow), read like Fortune in SeedCropHarvest
            looting = killer.getMainHandItem().getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING));
        }
        // D7a: the Calipers in the killer's OFF hand make the Tissue Sample certain (read like SeedItem reads its off hand: COMPILED)
        boolean calipersOffHand = source.getEntity() instanceof Player player
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CALIPERS.get());
        Drops.Context ctx = new Drops.Context(Drops.Source.DEATH, dog.isBaby(), event.isRecentlyHit(), dog.isOnFire(), looting, calipersOffHand);
        for (Drops.Drop drop : Drops.roll(dog.genome(), dog.stats(), ctx, new Random(dog.getRandom().nextLong()))) {
            if (drop.itemId().equals(Drops.TISSUE_ID)) {
                // the one drop that carries data: a vial with THIS animal's genes (the engine only says "a sample")
                event.getDrops().add(new ItemEntity(level, dog.getX(), dog.getY(), dog.getZ(), TissueSampleItem.create(dog.genome(), dog.stats())));
                continue;
            }
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(drop.itemId()));
            if (item == Items.AIR) {
                BunnidogsMod.LOGGER.warn("Drops: no such item '{}', skipped", drop.itemId());
                continue;
            }
            event.getDrops().add(new ItemEntity(level, dog.getX(), dog.getY(), dog.getZ(), new ItemStack(item, drop.count())));
        }
        // D8e: an animal that wears an Ear Tag leaves it behind, with its code. Not a roll (so not in Drops): a keepsake, certain, any age, any cause.
        dog.earTag().ifPresent(code -> event.getDrops().add(new ItemEntity(level, dog.getX(), dog.getY(), dog.getZ(), WornEarTagItem.create(code))));
    }
}
