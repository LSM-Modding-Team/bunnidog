package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.equipment.*;
import com.bunnidogs.mod.crown.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.ProblemReporter;

final class LunarUpdateGameTest {
    private static ItemStack item(String id) {return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id.contains(":") ? id : "bunnidogs:"+id)));}
    private static ItemStack craft(GameTestHelper h,String id,int width,int height,String... ingredients) {
        var recipe=(CraftingRecipe)h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs",id))).orElseThrow().value();
        var input=CraftingInput.of(width,height,Arrays.stream(ingredients).map(LunarUpdateGameTest::item).toList());
        h.assertTrue(recipe.matches(input,h.getLevel()),"Native lunar forge recipe accepts its physical stage: "+id);return recipe.assemble(input);
    }
    static void forge(GameTestHelper h) {
        for(var entry:Map.of("astralite_concentrate","astralite_shard","cinderite_billet","cinderite_ingot","compressed_moonstone","moonstone").entrySet()) {
            var materials=new String[9];Arrays.fill(materials,entry.getValue());h.assertTrue(craft(h,entry.getKey(),3,3,materials).getCount()==1,"Nine actual ingredients compact into one intermediate, not free extra metal");
        }
        craft(h,"lunar_binder",1,3,"prism_lens","apex_marrow","compressed_moonstone");
        var bloom=craft(h,"astralite_bloom",3,3,"astralite_concentrate","cinderite_billet","astralite_concentrate","cinderite_billet","lunar_binder","cinderite_billet","astralite_concentrate","cinderite_billet","astralite_concentrate");
        var recipe=(BlastingRecipe)h.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("bunnidogs","astralite_ingot"))).orElseThrow().value();
        h.assertTrue(recipe.matches(new SingleRecipeInput(bloom),h.getLevel()) && recipe.assemble(new SingleRecipeInput(bloom)).is(ModItems.ASTRALITE_INGOT.get()),"Only the mined bloom blast-smelts into the Astralite ingot");
        h.assertTrue(craft(h,"astralite_plating",3,3,"astralite_ingot","cinderite_billet","astralite_ingot","cinderite_billet","aerial_membrane","cinderite_billet","astralite_ingot","cinderite_billet","astralite_ingot").is(ModItems.LUNAR_PARTS.get("astralite_plating").get()),"Full armour plating is a real four-ingot/four-billet craft");
    }
    static void armour(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var level=h.getLevel();
        var dog=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.COMMAND);dog.snapTo(Vec3.atBottomCenterOf(c.above()));dog.setGenome(com.bunnidogs.mod.genetics.Genome.DEFAULT);dog.setAge(0);dog.setNoAi(true);dog.setOnGround(true);level.addFreshEntity(dog);
        var p=ApexTypesGameTest.player(h,dog.position().add(1,0,0));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get()));
        h.assertTrue(dog.trail().trainFromStock(p,p.getOffhandItem()),"A real paid bond authorises creature equipment");
        h.assertTrue(net.minecraft.world.entity.EquipmentSlotGroup.ARMOR.test(EquipmentSlot.BODY),"Native BODY armour participates in ordinary armour enchantment slot groups");
        float wounds=dog.getMaxHealth()/2;dog.setHealth(wounds);var genome=dog.genome();
        for(String tier:Barding.NAMES) {
            var armor=new ItemStack(ModItems.BARDING.get(tier).get());p.setItemInHand(InteractionHand.MAIN_HAND,armor);
            h.assertTrue(Barding.equip(p,dog,armor) && armor.isEmpty() && dog.getHealth()==wounds && dog.genome().equals(genome),"Actual gear swaps pay the held suit without healing or rewriting genes: "+tier);
        }
        h.assertTrue(dog.getAttributeValue(Attributes.ARMOR)>=14 && dog.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==5,"Astralite body equipment supplies real native protection");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());dog.saveWithoutId(out);
        var restored=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));Barding.refresh(restored);
        h.assertTrue(Barding.tier(restored)==4 && restored.getMaxHealth()==dog.getMaxHealth() && restored.getHealth()==wounds,"Native equipment and attributes preserve the exact armour and wounds across reload");
        h.assertTrue(Barding.remove(p,restored) && Barding.tier(restored)==-1 && restored.getHealth()<=wounds,"The keeper can reclaim the saved suit without healing or duplication");
        dog.discard();restored.discard();p.discard();
    }
    static void boundary(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var c=CrownDungeonGameTest.origin(h);var seal=CrownDungeonGameTest.seal(h,0,c.above(),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(2,1,0)));
        h.assertTrue(seal.begin(p,InteractionHand.MAIN_HAND),"A real room engagement creates the boundary fixture");
        var mob=(Apex)h.getLevel().getEntity(seal.enemies().iterator().next());mob.setNoAi(true);var box=seal.movementBounds();
        mob.move(MoverType.SELF,new Vec3(1000,0,0));
        h.assertTrue(mob.getBoundingBox().maxX<=box.maxX+.001 && mob.isAlive() && seal.enemies().contains(mob.getUUID()),"Physical room limits contain even extreme displacement without despawning or awarding a kill");
        p.setPos(box.maxX+5,mob.getY(),mob.getZ());mob.setTarget(p);
        CrownEncounter.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(mob));
        h.assertTrue(mob.getTarget()==null && !mob.canAttack(p),"Dungeon target selection and existing pursuit both reject a player outside the room");
        var aimed=CrownEncounter.destination(mob,new Vec3(1000,1000,1000));h.assertTrue(box.contains(aimed),"Flight, retreat and jump destinations are steered inside the same saved room");seal.reset();p.discard();
    }
    static void masonry(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var at=ApexTypesGameTest.center(h);var blast=ApexTypesGameTest.apex(h,ApexKind.BLAST,false,at);
        var positions=new ArrayList<BlockPos>();int i=0;
        for(var entry:LunarMasonry.BLOCKS.values()) {
            var state=entry.get().defaultBlockState();var pos=BlockPos.containing(at).offset(i%5-2,1,i/5-2);i++;level.setBlock(pos,state,3);positions.add(pos);
            h.assertTrue(state.getDestroySpeed(level,pos)>=200 && state.getBlock().getExplosionResistance()>=3600000 && !state.canEntityDestroy(level,pos,blast),"Each structural shape is hard, blast-proof and protected from terrain-clearing attacks");
            h.assertTrue(!state.is(com.bunnidogs.mod.expedition.MoonMining.STONE),"Fortress stone never inherits the Blastmaw lunar-rock mining exception");
        }
        blast.skills().blast(level);h.assertTrue(positions.stream().noneMatch(p->level.getBlockState(p).isAir()),"An actual Blastmaw self-explosion preserves the entire structural palette");blast.discard();
        var p=BlockPos.containing(at).offset(8,1,0);level.setBlock(p,Blocks.BARREL.defaultBlockState(),3);var barrel=(BarrelBlockEntity)level.getBlockEntity(p);barrel.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("bunnidogs","chests/crown_supplies")),771);barrel.unpackLootTable(null);
        int apples=0,xp=0,amethyst=0,rockets=0;for(int slot=0;slot<27;slot++){var stack=barrel.getItem(slot);if(stack.is(Items.ENCHANTED_GOLDEN_APPLE))apples+=stack.getCount();if(stack.is(Items.EXPERIENCE_BOTTLE))xp+=stack.getCount();if(stack.is(Items.AMETHYST_BLOCK))amethyst+=stack.getCount();if(stack.is(Items.FIREWORK_ROCKET)){rockets+=stack.getCount();h.assertTrue(stack.get(net.minecraft.core.component.DataComponents.FIREWORKS).flightDuration()==1,"Every bulk rocket stack retains Flight 1");}}
        h.assertTrue(apples>=16 && xp>=128 && amethyst>=64 && rockets>=64,"A real 27-slot dungeon barrel resolves expedition-scale supplies rather than the old handfuls");
    }
}
