package com.bunnidogs.mod.block;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Seed;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * What breaking a {@link SeedCropBlock} gives (Phase 4d). The block has no loot table; a player breaking it is caught here,
 * on the server, before the block goes:
 * <ul>
 *   <li><b>Unripe:</b> the seed comes back as an item with its payload untouched (nothing is lost by digging it up early).
 *       In creative mode nothing drops, like any block.</li>
 *   <li><b>Ripe:</b> the litter is born ({@link Bunnidog#hatch}) and the seed is used up. Fortune on the tool adds
 *       {@link Seed#fortuneBonus} quality to this one hatching. Works in creative mode too (that is how it is tested there).
 *       If too many bunnidogs live nearby nothing hatches: the break is cancelled, the crop stays ripe and the player is told.</li>
 * </ul>
 * A crop that is removed any other way (water, piston, explosion) gives nothing; only the soil check in
 * {@link SeedCropBlock#randomTick} returns the seed.
 */
@EventBusSubscriber(modid = BunnidogsMod.MODID)
public final class SeedCropHarvest {
    private SeedCropHarvest() {}

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;                                                  // the event fires on the client too: act on the server only
        }
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof SeedCropBlock)) {
            return;
        }
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        if (!(level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop)) {
            return;
        }
        Optional<Seed> planted = crop.seed();
        if (planted.isEmpty()) {
            return;                                                  // an empty crop (from /setblock): nothing to give
        }
        Seed seed = planted.get();

        if (!SeedCropBlock.isRipe(state)) {
            if (!player.hasInfiniteMaterials()) {
                SeedCropBlock.dropSeed(level, pos, seed);
            }
            return;
        }

        ItemStack tool = player.getMainHandItem();
        int fortune = tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
        Vec3 at = new Vec3(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        List<Bunnidog> born = Bunnidog.hatch(level, at, seed, Seed.fortuneBonus(fortune), new Random(level.getRandom().nextLong()));
        if (born.isEmpty()) {
            event.setCanceled(true);
            event.setNotifyClient(true);                             // the client already predicted the break: tell it to undo
            player.sendSystemMessage(Component.literal("Too many bunnidogs live around here; the crop stays ripe until there is room."));
        }
    }
}
