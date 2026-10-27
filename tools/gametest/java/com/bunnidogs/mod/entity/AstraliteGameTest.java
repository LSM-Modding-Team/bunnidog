package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;

final class AstraliteGameTest {
    static void run(GameTestHelper h) {
        var level=h.getLevel();var enchants=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);var registry=net.minecraft.core.registries.BuiltInRegistries.ITEM;
        for(String part:new String[]{"helmet","chestplate","leggings","boots","sword","pickaxe","axe","shovel","hoe"}) {
            var base=new ItemStack(registry.getValue(Identifier.withDefaultNamespace("netherite_"+part)));base.setDamageValue(47);base.enchant(enchants.getOrThrow(Enchantments.UNBREAKING),3);base.enchant(enchants.getOrThrow(Enchantments.MENDING),1);base.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My expedition gear"));
            var recipe=(SmithingTransformRecipe)level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs","astralite_"+part))).orElseThrow().value();
            var input=new SmithingRecipeInput(new ItemStack(ModItems.ASTRALITE_UPGRADE.get()),base,new ItemStack(ModItems.ASTRALITE_ARMOR.containsKey(part) ? ModItems.LUNAR_PARTS.get("astralite_plating").get() : ModItems.ASTRALITE_INGOT.get()));
            h.assertTrue(recipe.matches(input,level),"Real smithing accepts the Netherite base, Astralite template and ingot");
            var output=recipe.assemble(input);var expected=(ModItems.ASTRALITE_ARMOR.containsKey(part) ? ModItems.ASTRALITE_ARMOR : ModItems.ASTRALITE_TOOLS).get(part).get();
            h.assertTrue(output.is(expected) && output.getDamageValue()==47 && output.get(DataComponents.ENCHANTMENTS).equals(base.get(DataComponents.ENCHANTMENTS)) && output.getHoverName().equals(base.getHoverName()),"Astralite smithing preserves wear, Mending, Unbreaking and identity: "+part);
            h.assertTrue(enchants.getOrThrow(Enchantments.UNBREAKING).value().isSupportedItem(output) && enchants.getOrThrow(Enchantments.MENDING).value().isSupportedItem(output),"New gear supports real native durability enchantments");
            if(ModItems.ASTRALITE_ARMOR.containsKey(part)) {
                h.assertTrue(output.getMaxDamage()==base.getMaxDamage()*3,"Every armor slot triples native Netherite durability");
                var artifact=(SmithingTransformRecipe)level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs","heartbound_"+part))).orElseThrow().value();
                h.assertTrue(!artifact.matches(new SmithingRecipeInput(ItemStack.EMPTY,base,new ItemStack(ModItems.VITALITY_ARTIFACT.get())),level) && artifact.matches(new SmithingRecipeInput(ItemStack.EMPTY,output,new ItemStack(ModItems.VITALITY_ARTIFACT.get())),level),"Only Astralite, not Netherite, accepts the armor artifact");
                h.assertTrue(artifact.assemble(new SmithingRecipeInput(ItemStack.EMPTY,output,new ItemStack(ModItems.VITALITY_ARTIFACT.get()))).getMaxDamage()==output.getMaxDamage(),"Artifact armor retains its Astralite durability foundation");
            } else h.assertTrue(output.getMaxDamage()>base.getMaxDamage() && output.getMaxDamage()<base.getMaxDamage()*1.3,"Tools improve modestly instead of tripling durability");
        }
        for(var ore:java.util.List.of(com.bunnidogs.mod.registry.ModBlocks.ASTRALITE_ORE.get(),com.bunnidogs.mod.registry.ModBlocks.CINDERITE_ORE.get())) {
            var pick=new ItemStack(ModItems.ASTRALITE_TOOLS.get("pickaxe").get());var state=ore.defaultBlockState();
            h.assertTrue(pick.isCorrectToolForDrops(state) && !new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(state),"Realm ores require a proper high-tier pickaxe");
            var pos=h.absolutePos(new net.minecraft.core.BlockPos(3,2,3));
            var drops=net.minecraft.world.level.block.Block.getDrops(state,level,pos,null,null,pick);
            h.assertTrue(!drops.isEmpty() && drops.stream().anyMatch(d->d.is(ore==com.bunnidogs.mod.registry.ModBlocks.ASTRALITE_ORE.get() ? ModItems.ASTRALITE_SHARD.get() : ModItems.RAW_CINDERITE.get())),"Native ore loot yields the actual progression ingredient");
            pick.enchant(enchants.getOrThrow(Enchantments.SILK_TOUCH),1);
            h.assertTrue(net.minecraft.world.level.block.Block.getDrops(state,level,pos,null,null,pick).stream().anyMatch(d->d.is(ore.asItem())),"Native Silk Touch retains the ore instead of its ingredient");
        }
    }
}
