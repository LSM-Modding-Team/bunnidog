package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;

/** Physical capital projects and real, finite ammunition. All recipes are ordinary native recipes. */
public final class GuildProjects {
    public static final Map<String,DeferredItem<Item>> MATERIALS=new LinkedHashMap<>();
    public static final Map<String,DeferredBlock<ProjectBlock>> BLOCKS=new LinkedHashMap<>();
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<ProjectStation>> STATION;
    public static DeferredItem<ReservoirItem> BATTERY,MAGAZINE;
    public static void blocks(DeferredRegister.Blocks registry,DeferredRegister<BlockEntityType<?>> entities) {
        for(String name:List.of("pack_armoury","resonance_bench","nursery_hearth","bastion_heart","launch_aerie","guild_heart","guild_gate"))
            BLOCKS.put(name,registry.registerBlock(name,ProjectBlock::new,p->p.strength(6,3600000).lightLevel(s->10)));
        STATION=entities.register("guild_projects",()->new BlockEntityType<>(ProjectStation::new,BLOCKS.values().stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)));
    }
    public static void items(DeferredRegister.Items registry) {
        for(String name:List.of("root_fibre","fibre_roll","dust_pouch","sinew_bale","moonstone_crate","structural_frame","field_dressing","nursery_bedding","kennel_wagon"))
            MATERIALS.put(name,name.equals("kennel_wagon") ? registry.registerItem(name,WagonItem::new,p->p.stacksTo(1)) : registry.registerSimpleItem(name));
        BLOCKS.forEach((name,block)->registry.registerSimpleBlockItem(block));
        BATTERY=registry.registerItem("prism_battery",p->new ReservoirItem(p,true),p->p.stacksTo(1).fireResistant().component(ModItems.AMMO_UNITS.get(),0));
        MAGAZINE=registry.registerItem("volatile_magazine",p->new ReservoirItem(p,false),p->p.stacksTo(1).fireResistant().component(ModItems.AMMO_UNITS.get(),0));
    }
    public static Item material(String name) {return MATERIALS.get(name).get();}
    public static int units(ItemStack stack,Item ammo) {
        if(stack.getItem() instanceof ReservoirItem r && r.ammo()==ammo)return Math.clamp(stack.getOrDefault(ModItems.AMMO_UNITS.get(),0),0,r.capacity());
        return ammo==ModItems.HEART_SPOOL.get() && stack.is(material("field_dressing")) ? stack.getCount() : 0;
    }
    public static int spend(ItemStack stack,Item ammo,int wanted) {
        int take=Math.min(wanted,units(stack,ammo));
        if(take>0) {if(stack.getItem() instanceof ReservoirItem)stack.set(ModItems.AMMO_UNITS.get(),units(stack,ammo)-take);else stack.shrink(take);}
        return take;
    }
    private GuildProjects() {}
}
