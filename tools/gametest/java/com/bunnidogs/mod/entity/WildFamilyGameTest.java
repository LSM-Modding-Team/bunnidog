package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import com.bunnidogs.mod.worldgen.WildFamilyFeature;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class WildFamilyGameTest {
    record Site(GameTestHelper h,HearthTreeBlockEntity home,List<Bunnidog> dogs) {}
    static Site setup(GameTestHelper h,boolean bright) {return setup(h,bright,true);}
    static Site setup(GameTestHelper h,boolean bright,boolean spawn) {
        var level=h.getLevel();var at=h.absolutePos(new BlockPos(32,180,32));
        for(int x=-9;x<=9;x++)for(int z=-10;z<=12;z++) {level.setBlock(at.offset(x,-1,z),Blocks.DIRT.defaultBlockState(),2);for(int y=0;y<6;y++)level.setBlock(at.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
        h.assertTrue(WildFamilyFeature.generate(level,at,591L),"A clear natural glade generates through the registered feature");var home=(HearthTreeBlockEntity)level.getBlockEntity(at);
        if(bright) {var tag=home.saveWithFullMetadata(level.registryAccess());var genes=new net.minecraft.nbt.ListTag();for(int i=0;i<3;i++)genes.add(net.minecraft.nbt.StringTag.valueOf(SocietyGameTest.bright().toString()));tag.put("WildFounders",genes);home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));}
        if(spawn)for(int i=0;i<3;i++)home.wild().tick();var dogs=home.residents();dogs.forEach(d->{d.setNoAi(true);d.setOnGround(true);});h.assertTrue(!spawn || dogs.size()==3,"Three finite natural founder attempts produce actual residents");return new Site(h,home,dogs);
    }
    static void clean(Site c) {c.home.residents().forEach(Bunnidog::discard);c.h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(c.home.getBlockPos()).inflate(24)).forEach(ItemEntity::discard);}
    static void at(Bunnidog dog,BlockPos pos) {SocietyGameTest.rest(dog);dog.snapTo(Vec3.atBottomCenterOf(pos.east()));dog.setOnGround(true);}
    static boolean act(Site c,Bunnidog dog,SocietyWork.Kind kind,BlockPos target,int index) {at(dog,target);return dog.society().perform(new SocietyWork.Job(kind,target,index,c.h.getLevel().getBlockState(target)));}
    static boolean act(Site c,Bunnidog dog,SocietyWork.Kind kind,BlockPos target) {return act(c,dog,kind,target,-1);}
    static void collectAll(Site c,Bunnidog dog) {
        var level=c.h.getLevel();var all=level.getEntitiesOfClass(ItemEntity.class,new AABB(c.home.getBlockPos()).inflate(24));
        for(var item:all)item.setNoPickUpDelay();
        for(int n=0;n<40;n++) {var items=CivicFields.drops(c.home);if(items.isEmpty())break;var item=items.getFirst();at(dog,item.blockPosition());dog.snapTo(item.position().add(0,0,1));
            c.h.assertTrue(dog.society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,item.blockPosition(),item.getId(),level.getBlockState(item.blockPosition()))),"The primitive hauler picks a real marked item");
            c.h.assertTrue(dog.society().workSupplies().getCount()==1,"Zero Senses points allow one item, never advanced bulk hauling");
            c.h.assertTrue(act(c,dog,SocietyWork.Kind.DELIVER,c.home.getBlockPos()),"The same physical parcel reaches its original cache");
        }
    }
    public static void genesis(GameTestHelper h) {
        var c=setup(h,false);var home=c.home;
        h.assertTrue(home.isEmpty() && home.progress()==0 && !home.ripe(),"Natural infrastructure gives no stored stock or progression receipts");
        for(var dog:c.dogs)h.assertTrue(dog.stats().equals(Stats.ZERO) && dog.learning().knowledge().equals(Learning.EMPTY) && home.wild().founders().contains(dog.genome()),"Founders keep their ordinary frozen genes and start without bred points or education");
        var tag=home.saveWithFullMetadata(h.getLevel().registryAccess());var copy=(HearthTreeBlockEntity)BlockEntity.loadStatic(home.getBlockPos(),home.getBlockState(),tag,h.getLevel().registryAccess());
        h.assertTrue(copy!=null && copy.wild().founders().equals(home.wild().founders()) && copy.owner().equals(home.owner()),"Exact founders and the community's own identity survive native saves");
        var player=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(home.getBlockPos().west(2)));home.found(player);
        h.assertTrue(!home.permitted(player) && !c.dogs.getFirst().society().invite(player,home,true),"A visitor cannot claim the community cache or dismiss its residents");player.discard();
        clean(c);for(int i=0;i<4;i++)home.wild().tick();h.assertTrue(home.residents().isEmpty(),"Dead/removed founders are not respawned on subsequent ticks");h.succeed();
    }
    public static void founderVeto(GameTestHelper h) {
        var c=setup(h,false,false);var t=c.home;
        java.util.function.Consumer<EntityJoinLevelEvent> cancel=e->{if(e.getEntity() instanceof Bunnidog d && d.society().belongs(t))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(cancel);try {t.wild().tick();}finally {NeoForge.EVENT_BUS.unregister(cancel);}
        for(int i=0;i<4;i++)t.wild().tick();h.assertTrue(t.residents().size()==2,"A canceled founder consumes its one attempt instead of retrying until a genius appears");
        var data=t.saveWithFullMetadata(h.getLevel().registryAccess());t.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),data));
        for(int i=0;i<4;i++)t.wild().tick();h.assertTrue(t.residents().size()==2,"Native home reload preserves spent attempts and cannot duplicate the surviving founders");clean(c);h.succeed();
    }
    public static void materials(GameTestHelper h) {
        var c=setup(h,true);var d=c.dogs.getFirst();var t=c.home;var l=h.getLevel();
        for(var p:t.wild().timber())h.assertTrue(act(c,d,SocietyWork.Kind.CLIP_WOOD,p),"Primitive harvesting removes only known fallen wood");
        h.assertTrue(t.isEmpty() && !l.getEntitiesOfClass(ItemEntity.class,new AABB(t.getBlockPos()).inflate(12)).isEmpty(),"Wood remains in actual ground entities until walked home");collectAll(c,d);h.assertTrue(t.count(ModItems.GUAVA_LOG.get())==3,"Every original log arrives exactly once");
        for(var recipe:List.of(CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.STICKS,CivicRecipes.RecipeId.AXE))h.assertTrue(act(c,d,SocietyWork.Kind.CRAFT,t.getBlockPos(),recipe.ordinal()),"An untrained capable founder learns from real paid native recipes: "+recipe);
        h.assertTrue(t.count(Items.WOODEN_AXE)==1 && d.stats().equals(Stats.ZERO) && d.learning().knowledge().knows(Learning.Routine.GARDEN),"Practice earns personal knowledge without invented body points");
        h.assertTrue(!act(c,d,SocietyWork.Kind.CRAFT,t.getBlockPos(),CivicRecipes.RecipeId.FRAME.ordinal()),"Primitive discovery cannot manufacture advanced grafting equipment");
        var field=t.wild().fields().getFirst();h.assertTrue(act(c,d,SocietyWork.Kind.HARVEST_FIELD,field) && l.getBlockState(field).getValue(CropBlock.AGE)==0,"The original native wheat crop yields real materials and consumes its returned replant seed");collectAll(c,d);
        h.assertTrue(t.count(Items.WHEAT)>=1,"Real wheat, not the farmer's personal Diet, supplies nest making");
        h.assertTrue(act(c,d,SocietyWork.Kind.PRUNE,t.orchard().getFirst()),"The crafted axe unlocks actual renewable Coppice output");collectAll(c,d);
        var old=t.wild().timber().getFirst();l.setBlock(old,ModBlocks.GUAVA_LOG.get().defaultBlockState(),3);h.assertTrue(!act(c,d,SocietyWork.Kind.CLIP_WOOD,old),"A newly placed log never renews the original finite wood permission");
        h.assertTrue(!t.ripe(),"A wild bootstrap cannot award the player progression Keystone");clean(c);h.succeed();
    }
    static ItemStack plantFamily(Site c) {
        var h=c.h;var t=c.home;var l=h.getLevel();var first=c.dogs.get(0);var second=c.dogs.get(1);
        for(int i=0;i<2;i++) {var d=c.dogs.get(i);var bed=t.wild().beds().get(i);l.getPoiManager().add(bed,ModPoi.NEST);at(d,bed);h.assertTrue(d.moveHomeTo(l,bed)==NestHome.MoveResult.MOVED,"Both actual parents claim reachable Nests");}
        // Food is a fixture input here; the separate materials scenario covers paid harvesting/transport.
        t.put(new ItemStack(ModItems.GUAVA.get(),24));h.assertTrue(act(c,first,SocietyWork.Kind.COURT,t.getBlockPos()) && t.count(ModItems.GUAVA.get())==22,"A primitive nurse pays two real guavas for native courtship");first.spawnChildFromBreeding(l,second);
        int slot=-1;for(int i=0;i<27;i++)if(SocietyWork.communitySeed(t.getItem(i),t))slot=i;h.assertTrue(slot>=0,"Native breeding preserves the actual parental seed at the cache");
        var exact=t.getItem(slot).copy();exact.set(DataComponents.CUSTOM_NAME,Component.literal("Wild family litter"));t.setItem(slot,exact.copy());first.setAge(0);second.setAge(0);
        h.assertTrue(act(c,first,SocietyWork.Kind.SEED,t.getBlockPos(),slot),"The caretaker takes the exact parental seed");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());first.saveWithoutId(saved);first.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved.buildResult()));
        h.assertTrue(ItemStack.matches(first.society().workSupplies(),exact),"The seed parcel retains its name, pedigree and destination through native save/load");
        h.assertTrue(act(c,first,SocietyWork.Kind.DELIVER,t.wild().nursery()),"The seed is physically planted in reserved soil without a Cradle");var crop=(SeedCropBlockEntity)l.getBlockEntity(t.wild().nursery());h.assertTrue(ItemStack.matches(crop.seedStack(),exact),"The actual crop stores the exact seed");
        l.setBlock(t.wild().nursery(),l.getBlockState(t.wild().nursery()).setValue(SeedCropBlock.AGE,SeedCropBlock.MAX_AGE),2);return exact;
    }
    public static void nursery(GameTestHelper h,boolean veto) {
        var c=setup(h,true);var exact=plantFamily(c);var t=c.home;var d=c.dogs.getFirst();var l=h.getLevel();
        c.dogs.forEach(dog->dog.snapTo(Vec3.atBottomCenterOf(t.getBlockPos().south(4))));
        if(veto) {
            java.util.function.Consumer<EntityJoinLevelEvent> cancel=e->{if(e.getEntity() instanceof Bunnidog pup && pup.isBaby() && pup.society().belongs(t))e.setCanceled(true);};
            NeoForge.EVENT_BUS.addListener(cancel);try {h.assertTrue(!act(c,d,SocietyWork.Kind.HATCH_SOIL,t.wild().nursery()),"A native spawn veto refuses the entire soil litter");}finally {NeoForge.EVENT_BUS.unregister(cancel);}
            h.assertTrue(l.getBlockEntity(t.wild().nursery()) instanceof SeedCropBlockEntity crop && ItemStack.matches(crop.seedStack(),exact) && t.residents().stream().noneMatch(Bunnidog::isBaby),"A refused hatch retains exactly the same ripe seed and no siblings");
        }
        h.assertTrue(act(c,d,SocietyWork.Kind.HATCH_SOIL,t.wild().nursery()),"A clear native soil nursery hatches without player action or machine");
        var pups=t.residents().stream().filter(Bunnidog::isBaby).toList();h.assertTrue(!pups.isEmpty() && pups.stream().allMatch(p->p.society().child() && p.learning().knowledge().equals(Learning.EMPTY)) && l.getBlockState(t.wild().nursery()).isAir(),"Real pups inherit membership and DNA, never their parents' education; the seed is spent once");
        clean(c);h.succeed();
    }
    public static void limits(GameTestHelper h) {
        var c=setup(h,true);var d=c.dogs.getFirst();d.setGenome(new Genome(d.genome().packed(),0));
        h.assertTrue(!act(c,d,SocietyWork.Kind.CRAFT,c.home.getBlockPos(),CivicRecipes.RecipeId.PLANKS.ordinal()) && !d.society().pioneer(Learning.Routine.DELIVERY),"Natural membership never upgrades a genetically limited mind");
        var playerHome=c.home.getBlockPos().east(7);h.getLevel().setBlock(playerHome,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var ordinary=(HearthTreeBlockEntity)h.getLevel().getBlockEntity(playerHome);var visitor=ModEntities.BUNNIDOG.get().create(h.getLevel(),EntitySpawnReason.COMMAND);visitor.setGenome(SocietyGameTest.bright());visitor.society().settle(ordinary);h.assertTrue(!visitor.society().belongs(ordinary) && !ordinary.wild().natural(),"Old/player Hearths never acquire wild permissions by proximity");visitor.discard();
        int narrow=com.bunnidogs.mod.layout.CivicAnnex.cells(0,2,0).size(),wide=com.bunnidogs.mod.layout.CivicAnnex.cells(0,2,1).size();h.assertTrue(narrow-wide==4,"Broad bodies lead to a genuinely wider two-high entrance");clean(c);h.succeed();
    }
    public static void ai(GameTestHelper h) {
        var c=setup(h,true);c.dogs.forEach(d->d.setNoAi(false));
        h.runAfterDelay(2200,()->{var t=c.home;h.assertTrue(t.count(Items.WOODEN_AXE)==1 && c.dogs.stream().anyMatch(d->d.learning().knowledge().knows(Learning.Routine.GARDEN)),"Actual autonomous goals bootstrap a paid axe from empty stores and zero learned skills; stock="+t.count(ModItems.GUAVA_LOG.get())+" logs, "+t.count(ModItems.GUAVA_PLANKS.get())+" planks, "+t.count(Items.STICK)+" sticks; jobs="+c.dogs.stream().map(d->String.valueOf(d.society().active())).toList());clean(c);h.succeed();});
    }
}
