package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.*;
import com.bunnidogs.mod.registry.ModItems;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Freeze real parent records once, rather than rerolling genes on inspection or granting a boss as a pet. */
public final class CrownArchives {
    public static void fill(ChestBlockEntity chest,long seed,UUID site) {
        var random=new Random(seed);
        var mother=Wings.winged(Genome.randomFounder(Lineage.END,random));
        var father=Wings.winged(Genome.randomFounder(Lineage.CAVE,random));
        var a=points(random);var b=points(random);
        var embryo=SeedItem.create(new Seed(mother,a,father,b,36+random.nextInt(12)));
        embryo.set(ModItems.CROWN_SITE.get(),site.toString());embryo.set(DataComponents.CUSTOM_NAME,Component.translatable("item.bunnidogs.crown_archive_seed"));
        chest.setItem(10,embryo);chest.setItem(12,TissueSampleItem.create(mother,a));chest.setItem(14,TissueSampleItem.create(father,b));
        chest.setItem(19,new ItemStack(ModItems.MUTAGEN.get(),16+random.nextInt(9)));chest.setItem(21,new ItemStack(ModItems.STARLIT_DUST.get(),64));
        chest.setChanged();
    }
    private static Stats points(Random random) {return Stats.of(70+random.nextInt(31),70+random.nextInt(31),70+random.nextInt(31),70+random.nextInt(31),70+random.nextInt(31));}
    private CrownArchives() {}
}
