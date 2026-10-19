package com.bunnidogs.mod.registry;

import com.bunnidogs.mod.BunnidogsMod;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Native profession/POI/26.2 trade sets; offers, XP, restocks, cures and saves remain vanilla. */
public final class ModVillagers {
    private ModVillagers() {}
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION,BunnidogsMod.MODID);
    public static final DeferredHolder<VillagerProfession,VillagerProfession> BREEDER = PROFESSIONS.register("breeder",() -> {
        var trades = new Int2ObjectOpenHashMap<ResourceKey<TradeSet>>();
        for (int level = 1; level <= 5; level++) trades.put(level,ResourceKey.create(Registries.TRADE_SET,
                Identifier.fromNamespaceAndPath(BunnidogsMod.MODID,"breeder/level_"+level)));
        return new VillagerProfession(Component.translatable("entity.bunnidogs.villager.breeder"),
                poi -> poi.is(ModPoi.BREEDING_PEN.getKey()),poi -> poi.is(ModPoi.BREEDING_PEN.getKey()),
                ImmutableSet.of(),ImmutableSet.of(),SoundEvents.VILLAGER_WORK_SHEPHERD,trades);
    });
}
