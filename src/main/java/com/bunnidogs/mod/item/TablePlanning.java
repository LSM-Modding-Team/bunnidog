package com.bunnidogs.mod.item;

import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.genetics.BreedingPlan;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;

/** Held items are reusable preview cues, sampled on the server when the table is read. */
public final class TablePlanning {
    private TablePlanning() {}
    public static BreedingPlan held(ItemStack main, ItemStack off, int fortune) {
        boolean a = main.getItem() instanceof GuavaItem, b = off.getItem() instanceof GuavaItem;
        int tierA = a ? GuavaItem.tierOf(main) : b ? GuavaItem.tierOf(off) : 0;
        int tierB = b ? GuavaItem.tierOf(off) : tierA;
        return new BreedingPlan(tierA,tierB,main.is(ModItems.MUTAGEN.get()) || off.is(ModItems.MUTAGEN.get()),fortune);
    }
    public static TableSheet snapshot(GeneticsTableBlockEntity table, Player player) {
        var main = player.getMainHandItem(); var off = player.getOffhandItem();
        int fortune = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FORTUNE)
                .map(enchantment -> Math.clamp(Math.max(main.getEnchantmentLevel(enchantment),off.getEnchantmentLevel(enchantment)),0,255)).orElse(0);
        return new TableSheet(TissueSampleItem.read(table.item(0)),TissueSampleItem.read(table.item(1)),
                held(main,off,fortune),table.goal());
    }
}
