package com.bunnidogs.mod.block;

import java.util.Random;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Breeding;
import com.bunnidogs.mod.genetics.Seed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Called by native player removal after break events and permission checks have accepted the action. */
public final class SeedCropHarvest {
    private SeedCropHarvest() {}

    public static boolean remove(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack tool) {
        if (!state.equals(level.getBlockState(pos))) return false;
        if (!(level.getBlockEntity(pos) instanceof SeedCropBlockEntity crop) || crop.seed().isEmpty()) return level.removeBlock(pos,false);
        boolean ripe = SeedCropBlock.isRipe(state);
        if (ripe && level.getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(Breeding.CROWD_RADIUS)).size() >= Breeding.CROWD_LIMIT) {
            level.sendBlockUpdated(pos,state,state,3);
            if (player != null) player.sendOverlayMessage(Component.translatable("message.bunnidogs.crop.crowded"));
            return false;
        }
        var seed = crop.seed().orElseThrow();
        var reserved = crop.takeSeed(); // Removal hooks and reentrant reads must no longer see stock to refund.
        if (!level.removeBlock(pos,false)) {
            if (level.getBlockEntity(pos) == crop) crop.setSeedStack(reserved);
            else SeedCropBlock.dropSeed(level,pos,reserved);
            return false;
        }
        if (!ripe) {
            if (player == null || !player.preventsBlockDrops()) SeedCropBlock.dropSeed(level,pos,reserved);
            return true;
        }
        int fortune = tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE));
        var born = Bunnidog.hatch(level,Vec3.atBottomCenterOf(pos),seed,Seed.fortuneBonus(fortune),new Random(level.getRandom().nextLong()));
        if (born.isEmpty()) {
            SeedCropBlock.dropSeed(level,pos,reserved);
            if (player != null) player.sendOverlayMessage(Component.translatable("message.bunnidogs.crop.refunded"));
        }
        return true;
    }
}
