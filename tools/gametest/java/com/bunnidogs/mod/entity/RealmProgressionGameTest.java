package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.realm.RealmLoot;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

final class RealmProgressionGameTest {
    static void recipe(GameTestHelper h) {
        var level=h.getLevel();var at=ApexTypesGameTest.center(h);var parts=new EnumMap<ApexKind,ItemStack>(ApexKind.class);ItemStack wing=ItemStack.EMPTY;
        for(var kind:ApexKind.values())for(boolean flying:new boolean[]{false,true}) {
            var dog=ApexTypesGameTest.apex(h,kind,flying,at);var event=new LivingDropsEvent(dog,level.damageSources().generic(),new ArrayList<>(),true);RealmLoot.parts(event,dog,level);var remnant=event.getDrops().iterator().next().getItem();
            h.assertTrue(remnant.getOrDefault(ModItems.GROUND_REMNANT.get(),false)==!flying,"Only real ground-role loot carries the visible ground-remnant component");
            if(!flying)parts.put(kind,remnant.copyWithCount(1));else for(var drop:event.getDrops())if(drop.getItem().is(ModItems.AERIAL_MEMBRANE.get()))wing=drop.getItem().copyWithCount(1);dog.discard();
        }
        var recipe=(CraftingRecipe)level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs","resonant_eye"))).orElseThrow().value();
        var grid=new ArrayList<>(List.of(parts.get(ApexKind.BRUTE),parts.get(ApexKind.PRISM),parts.get(ApexKind.BROOD),parts.get(ApexKind.BLAST),new ItemStack(Items.ENDER_EYE),parts.get(ApexKind.SUPPORT),wing,new ItemStack(ModItems.STARLIT_DUST.get()),new ItemStack(Items.NETHERITE_SCRAP)));
        var input=CraftingInput.of(3,3,grid);h.assertTrue(recipe.matches(input,level) && recipe.assemble(input).is(ModItems.RESONANT_EYE.get()),"The real recipe combines all five physical ground remnants and an aerial drop, with ordinary components");
        for(int slot:new int[]{0,1,2,3,5}) {var old=grid.get(slot);var absent=old.copy();absent.remove(ModItems.GROUND_REMNANT.get());grid.set(slot,absent);h.assertTrue(!recipe.matches(CraftingInput.of(3,3,grid),level),"A flying/unmarked substitute cannot bypass a missing ground ingredient");grid.set(slot,old);}
        grid.set(6,ItemStack.EMPTY);h.assertTrue(!recipe.matches(CraftingInput.of(3,3,grid),level),"Missing aerial material refuses the same native recipe");
    }
    static List<ItemStack> contents(net.minecraft.server.level.ServerPlayer p) {
        var result=new ArrayList<ItemStack>();for(int i=0;i<p.getInventory().getContainerSize();i++) {var stack=p.getInventory().getItem(i);if(stack.isEmpty())continue;result.add(stack);var inside=stack.get(DataComponents.CONTAINER);if(inside!=null)inside.nonEmptyItems().forEach(t->result.add(t.create()));}return result;
    }
    static void kits(GameTestHelper h) {
        var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));
        for(String family:new String[]{"vanilla","bunnidog"})for(String tier:new String[]{"early","mid","late","endgame"}) {
            com.bunnidogs.mod.command.KitCommands.apply(p,family,tier);var all=contents(p);boolean end=tier.equals("endgame");
            boolean late=end || tier.equals("late");var ench=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            if(!late) {
                h.assertTrue(count(all,Items.COOKED_BEEF)==64 && count(all,Items.END_CRYSTAL)==0 && count(all,Items.LAVA_BUCKET)==1,"Earlier kits keep normal food/building supplies and gain a real lava bucket");
                h.assertTrue(count(all,Items.COBWEB)==(tier.equals("mid") ? 8 : 0),"Diamond progression adds a modest web supply, without giving early kits endgame stock");continue;
            }
            h.assertTrue(all.stream().filter(s->s.is(Items.GOLDEN_APPLE)).mapToInt(ItemStack::getCount).sum()==(end ? 256 : 64) && all.stream().filter(s->s.is(Items.ENCHANTED_GOLDEN_APPLE)).mapToInt(ItemStack::getCount).sum()==(end ? 20 : 5),"The actual inventory and Shulker together have exact requested apple totals: "+family+" "+tier);
            h.assertTrue(all.stream().anyMatch(s->s.is(Items.FIREWORK_ROCKET)) && all.stream().filter(s->s.is(Items.FIREWORK_ROCKET)).allMatch(s->s.get(DataComponents.FIREWORKS).flightDuration()==1),"Every carried and boxed rocket uses native Flight 1");
            h.assertTrue(count(all,Items.FIREWORK_ROCKET)==192 && count(all,Items.ARROW)==64,"Late/endgame kits carry three rocket stacks and one shared arrow stack");
            var bow=all.stream().filter(s->s.is(Items.BOW)).findFirst().orElseThrow();
            h.assertTrue(bow.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.INFINITY))==1 && bow.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING))==0,"Infinity bows never also carry incompatible Mending");
            if(end) {
                h.assertTrue(count(all,Items.COOKED_BEEF)==0 && count(all,Items.EXPERIENCE_BOTTLE)==128 && count(all,Items.END_CRYSTAL)==128 && count(all,Items.WIND_CHARGE)==128,"Both endgame families have exact XP/crystal/wind supplies and no steak");
                h.assertTrue(count(all,Items.COBBLESTONE)==0 && count(all,Items.OAK_PLANKS)==0 && count(all,Items.OBSIDIAN)==192,"Endgame replaces ordinary blocks but retains all three stacks of crystal bases");
                var main=new ArrayList<ItemStack>();for(int i=0;i<36;i++)main.add(p.getInventory().getItem(i));
                var trident=all.stream().filter(s->s.is(Items.TRIDENT)).findFirst().orElseThrow();
                h.assertTrue(trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.RIPTIDE))==3 && trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.CHANNELING))==0 && trident.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOYALTY))==0,"Riptide III tridents exclude both incompatible throwing enchantments");
                for(String tool:List.of("axe","shovel","hoe")) {
                    var item=family.equals("bunnidog") ? ModItems.ASTRALITE_TOOLS.get(tool).get() : net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("netherite_"+tool));
                    h.assertTrue(count(main,item)==0 && count(all,item)==1,"Secondary tools are preserved exactly once inside the real Shulker");
                }
                h.assertTrue(count(main,Items.TRIDENT)==0 && count(main,Items.CROSSBOW)==0 && count(all,Items.TRIDENT)==1 && count(all,Items.CROSSBOW)==1 && count(main,Items.OBSIDIAN)==64,"Optional weapons are boxed and crystal bases are directly accessible");
            }
            h.assertTrue(all.stream().filter(ItemStack::isDamageableItem).allMatch(s->s.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING))==3 && (s.is(Items.BOW) || s.getEnchantmentLevel(ench.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING))==1)),"Every durable carried or boxed late-game item retains Unbreaking III and Mending, except the deliberate Infinity bow");
        }p.discard();
    }
    private static int count(List<ItemStack> stacks,Item item) {return stacks.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    static void mining(GameTestHelper h) {
        var level=h.getLevel();var center=ApexTypesGameTest.center(h);var origin=BlockPos.containing(center);var blast=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,center);
        var ore=ModBlocks.ASTRALITE_ORE.get().defaultBlockState();var stone=ModBlocks.MOONSTONE.get().defaultBlockState();var cinder=ModBlocks.CINDERITE_ORE.get().defaultBlockState();
        for(var state:List.of(ore,stone,cinder))h.assertTrue(!new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(state) && new ItemStack(ModItems.ASTRALITE_TOOLS.get("pickaxe").get()).isCorrectToolForDrops(state),"All lunar blocks require the actual Astralite tool tier");
        var shell=new ArrayList<BlockPos>();for(int x=-4;x<=4;x++)for(int y=-2;y<=2;y++)for(int z=-4;z<=4;z++)if(Math.max(Math.abs(x),Math.abs(z))>=2 || y<0) {var pos=origin.offset(x,y,z);level.setBlock(pos,(x+z)%3==0 ? ore : (x+z)%3==1 ? cinder : stone,3);shell.add(pos);}
        var protectedItems=new ArrayList<ItemEntity>();for(var item:List.of(ModItems.MOONSTONE.get(),ModItems.ASTRALITE_ORE.get(),ModItems.CINDERITE_ORE.get(),ModItems.ASTRALITE_SHARD.get(),ModItems.RAW_CINDERITE.get(),ModItems.CINDERITE_INGOT.get(),ModItems.ASTRALITE_INGOT.get())) {
            var drop=new ItemEntity(level,center.x(),center.y()+1,center.z(),new ItemStack(item));level.addFreshEntity(drop);protectedItems.add(drop);
            h.assertTrue(!drop.hurtServer(level,level.damageSources().lava(),100) && !drop.hurtServer(level,level.damageSources().explosion(blast,blast),100) && drop.isAlive(),"Actual lunar material entities survive native fire and explosion damage");
        }
        level.explode(null,null,new ExplosionDamageCalculator(),center,4,false,Level.ExplosionInteraction.TNT);
        h.assertTrue(shell.stream().allMatch(p->!level.getBlockState(p).isAir()),"A native TNT-strength explosion cannot mine lunar rock/ores");
        h.assertTrue(blast.skills().blast(level) && shell.stream().filter(p->level.getBlockState(p).isAir()).count()>100,"The actual Blastmaw self-explosion breaks its surrounding lunar shell");
        h.assertTrue(protectedItems.stream().allMatch(ItemEntity::isAlive),"The resulting Blastmaw blast preserves physical ore items");protectedItems.forEach(ItemEntity::discard);blast.discard();
    }
}
