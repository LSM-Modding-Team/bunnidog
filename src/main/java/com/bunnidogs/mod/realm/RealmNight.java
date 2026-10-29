package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Apex;
import com.bunnidogs.mod.equipment.Barding;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Night changes native spawn composition, never bypassing caps, placement, wards or loaded terrain. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class RealmNight {
    public static boolean active(ServerLevel level) {
        return GreatWarren.isRealm(level) && level.isDarkOutside();
    }
    @SubscribeEvent public static void spawns(LevelEvent.PotentialSpawns event) {
        if(!(event.getLevel() instanceof ServerLevel level) || !active(level) || event.getMobCategory()!=MobCategory.MONSTER)return;
        for(var entry:java.util.List.copyOf(event.getSpawnerDataList())) {
            var data=entry.value();
            if(!com.bunnidogs.mod.registry.ModEntities.FLYING_APEX.stream().anyMatch(type->type.get()==data.type()))continue;
            event.removeSpawnerData(entry);
            // Four times the selection weight, plus actual small aerial packs instead of lone flyers.
            event.addSpawnerData(new Weighted<>(new MobSpawnSettings.SpawnerData(data.type(),data.minCount(),Math.max(2,data.maxCount())),entry.weight()*4));
        }
    }
    public static void equip(Apex mob,ServerLevel level) {
        if(!active(level) || Barding.tier(mob)>=0 || mob.getRandom().nextFloat()>=.35F)return;
        int roll=mob.getRandom().nextInt(100);String tier=roll<50 ? "iron" : roll<85 ? "diamond" : "netherite";
        mob.setItemSlot(EquipmentSlot.BODY,new ItemStack(ModItems.BARDING.get(tier).get()));
        mob.setDropChance(EquipmentSlot.BODY,.085F);Barding.refresh(mob);mob.setHealth(mob.getMaxHealth());
    }
    private RealmNight() {}
}
