package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.equipment.BardingItem;
import com.bunnidogs.mod.registry.ModItems;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;

/** One deliberately selected workpiece and its real, component-preserving operation. */
public record WorkshopJob(String state,Map<Item,Integer> bill,ItemEnchantments upgrades) {
    private static WorkshopJob idle(String state) {return new WorkshopJob(state,Map.of(),ItemEnchantments.EMPTY);}
    public List<ItemStack> costs() {return bill.entrySet().stream().map(e->new ItemStack(e.getKey(),e.getValue())).toList();}
    public static WorkshopJob plan(String kind,ItemStack gear,ItemStack book) {
        if(gear.isEmpty())return idle(kind.equals("pack_armoury") ? "barding" : "equipment");
        var bill=new LinkedHashMap<Item,Integer>();
        if(kind.equals("pack_armoury")) {
            if(!(gear.getItem() instanceof BardingItem))return idle("barding");
            if(!gear.isDamaged())return idle("repaired");
            Item material=ModItems.ASTRALITE_SHARD.get();
            String[] tiers={"leather","iron","diamond","netherite"};Item[] inputs={Items.LEATHER,Items.IRON_INGOT,Items.DIAMOND,Items.NETHERITE_INGOT};
            for(int i=0;i<tiers.length;i++)if(gear.is(ModItems.BARDING.get(tiers[i]).get()))material=inputs[i];
            bill.put(material,1);bill.put(ModItems.CINDERITE_INGOT.get(),2);bill.put(ModItems.SINEW.get(),4);
            return new WorkshopJob("working",Collections.unmodifiableMap(bill),ItemEnchantments.EMPTY);
        }
        if(!gear.has(DataComponents.ENCHANTMENTS) || gear.is(Items.ENCHANTED_BOOK))return idle("equipment");
        if(!book.is(Items.ENCHANTED_BOOK))return idle("book");
        var old=EnchantmentHelper.getEnchantmentsForCrafting(gear);var accepted=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);int levels=0;
        for(var entry:EnchantmentHelper.getEnchantmentsForCrafting(book).entrySet().stream().sorted(Comparator.comparing(e->e.getKey().unwrapKey().map(k->k.identifier().toString()).orElse(""))).toList()) {
            var enchant=entry.getKey();int n=entry.getIntValue();
            if(n<=old.getLevel(enchant) || n>enchant.value().getMaxLevel() || !enchant.value().isSupportedItem(gear))continue;
            if(old.keySet().stream().anyMatch(e->!e.equals(enchant) && !Enchantment.areCompatible(enchant,e)))continue;
            if(accepted.keySet().stream().anyMatch(e->!e.equals(enchant) && !Enchantment.areCompatible(enchant,e)))continue;
            accepted.set(enchant,n);levels+=n;
        }
        if(levels==0)return idle("incompatible");
        bill.put(ModItems.CINDERITE_INGOT.get(),8*levels);bill.put(ModItems.STARLIT_DUST.get(),8*levels);bill.put(ModItems.ASTRALITE_SHARD.get(),levels);
        return new WorkshopJob("working",Collections.unmodifiableMap(bill),accepted.toImmutable());
    }
}
