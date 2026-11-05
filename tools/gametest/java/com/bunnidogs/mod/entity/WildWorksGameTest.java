package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.society.*;
import com.google.gson.*;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

/** Empty-stock primitive workshop proof, native tool custody and legacy-world isolation. */
public final class WildWorksGameTest {
    private static boolean act(WildFamilyGameTest.Site c,Bunnidog dog,SocietyWork.Kind kind,BlockPos target,int index,BlockPos destination) {
        WildFamilyGameTest.at(dog,target);
        return dog.society().perform(new SocietyWork.Job(kind,target,index,c.h().getLevel().getBlockState(target),null,destination));
    }
    private static boolean craft(WildFamilyGameTest.Site c,Bunnidog dog,CivicRecipes.RecipeId id) {
        return act(c,dog,SocietyWork.Kind.CRAFT,c.home().getBlockPos(),id.ordinal(),null);
    }
    private static boolean build(WildFamilyGameTest.Site c,Bunnidog dog,HearthTreeBlockEntity.Cell cell) {
        return act(c,dog,SocietyWork.Kind.FETCH_BUILD,c.home().getBlockPos(),-1,cell.pos())
                && act(c,dog,SocietyWork.Kind.BUILD,cell.pos(),-1,null);
    }
    private static boolean complete(HearthTreeBlockEntity home) {
        return home.wild().works().cells().stream().filter(cell->cell.item()!=ModItems.NEST.get()).allMatch(cell->{
            var actual=home.getLevel().getBlockState(cell.pos());
            if(actual.hasProperty(AbstractFurnaceBlock.LIT))actual=actual.setValue(AbstractFurnaceBlock.LIT,false);
            return actual.equals(cell.state());
        });
    }
    public static void preview(WorldGenLevel level,HearthTreeBlockEntity home,String path,String evidence) {
        var cells=new JsonArray();var origin=home.getBlockPos();
        for(int x=-7;x<=7;x++)for(int z=-8;z<=10;z++)for(int y=-1;y<4;y++) {
            var state=level.getBlockState(origin.offset(x,y,z));if(state.isAir())continue;
            var cell=new JsonObject();cell.addProperty("x",x);cell.addProperty("y",y);cell.addProperty("z",z);
            cell.addProperty("block",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());if(state.hasProperty(HearthTreeBlock.CACHED))cell.addProperty("cached",state.getValue(HearthTreeBlock.CACHED));cells.add(cell);
        }
        var out=new JsonObject();out.addProperty("evidence",evidence);out.addProperty("primitive_camp",home.wild().campStart());out.addProperty("cached",home.cached());out.add("cells",cells);var rest=new JsonArray();
        if(home.wild().works().restPlots())for(var at:home.wild().beds()) {var plot=new JsonObject();plot.addProperty("x",at.getX()-origin.getX());plot.addProperty("y",at.getY()-origin.getY());plot.addProperty("z",at.getZ()-origin.getZ());rest.add(plot);}out.add("rest_plots",rest);
        try {java.nio.file.Files.writeString(java.nio.file.Path.of(path),new GsonBuilder().setPrettyPrinting().create().toJson(out)+"\n");}
        catch(java.io.IOException error) {throw new RuntimeException(error);}
    }
    static void chain(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var worker=c.dogs().getFirst();var hauler=c.dogs().get(1);var works=home.wild().works();
        h.assertTrue(home.isEmpty() && works.enabled() && home.authorized(),"A fresh glade starts with empty stock and a genuinely clear original building approach");
        preview(h.getLevel(),home,"wild_works_start.json","Controlled native site before work: empty inventory, selected capable zero-point founders and no supplied tools/materials.");
        for(var log:home.wild().timber())h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.CLIP_WOOD,log),"The original finite logs are real harvested inputs");
        WildFamilyGameTest.collectAll(c,hauler);
        for(var recipe:List.of(CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.PLANKS,CivicRecipes.RecipeId.STICKS,CivicRecipes.RecipeId.WORKBENCH))
            h.assertTrue(craft(c,worker,recipe),"Hand-sized native recipes pay for the first physical workbench from the original three logs: "+recipe);
        h.assertTrue(!craft(c,worker,CivicRecipes.RecipeId.AXE) && build(c,worker,home.construction().cell(works.workbench())) && home.construction().owned(works.workbench()),"Merely crafting a table supplies no workspace until its actual original parcel is placed");
        for(var recipe:List.of(CivicRecipes.RecipeId.AXE,CivicRecipes.RecipeId.PICKAXE))h.assertTrue(craft(c,worker,recipe),"The actual reachable workbench now supports native larger tool crafting: "+recipe);
        var first=works.stones().stream().filter(works::canMine).findFirst().orElseThrow();
        h.assertTrue(act(c,worker,SocietyWork.Kind.FETCH_PICKAXE,home.getBlockPos(),-1,first) && home.count(Items.WOODEN_PICKAXE)==0,"A miner withdraws and physically owns the sole actual shared pickaxe");
        for(int i=0;i<8;i++) {
            var stone=works.stones().stream().filter(works::canMine).findFirst().orElseThrow();
            h.assertTrue(act(c,worker,SocietyWork.Kind.MINE_STONE,stone,-1,null),"A real wooden pick mines one original deposit");
        }
        h.assertTrue(worker.society().workSupplies().getDamageValue()==8 && home.count(Items.COBBLESTONE)==0,"The actual carried tool wears; native stone drops wait on the ground");
        h.assertTrue(act(c,worker,SocietyWork.Kind.DELIVER,home.getBlockPos(),-1,null),"The worn shared tool returns intact");WildFamilyGameTest.collectAll(c,hauler);
        h.assertTrue(home.count(Items.COBBLESTONE)==8 && craft(c,worker,CivicRecipes.RecipeId.FURNACE),"Eight actual recovered cobblestone pay for the native furnace recipe");
        h.assertTrue(build(c,worker,works.cells().getFirst()) && home.workshop().available(works.furnace())!=null,"The crew carries and builds its paid furnace, then links that exact new native container");
        for(var coppice:home.orchard())h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.PRUNE,coppice),"The paid axe harvests actual renewable fuel/feed stock");WildFamilyGameTest.collectAll(c,hauler);
        for(int i=0;i<3;i++)h.assertTrue(craft(c,worker,CivicRecipes.RecipeId.PLANKS),"Real logs provide the next fuel/building planks");
        h.assertTrue(act(c,worker,SocietyWork.Kind.FETCH_FURNACE,home.getBlockPos(),0,works.furnace())
                && act(c,worker,SocietyWork.Kind.LOAD_FURNACE,works.furnace(),-1,null)
                && act(c,worker,SocietyWork.Kind.FETCH_FURNACE,home.getBlockPos(),1,works.furnace())
                && act(c,worker,SocietyWork.Kind.LOAD_FURNACE,works.furnace(),-1,null),"Primitive craftspeople carry one real log and one real wood fuel unit to their own furnace");
        var furnace=(AbstractFurnaceBlockEntity)h.getLevel().getBlockEntity(works.furnace());
        for(int tick=0;tick<201;tick++)AbstractFurnaceBlockEntity.serverTick(h.getLevel(),works.furnace(),h.getLevel().getBlockState(works.furnace()),furnace);
        h.assertTrue(act(c,hauler,SocietyWork.Kind.EMPTY_FURNACE,works.furnace(),2,home.getBlockPos())
                && act(c,hauler,SocietyWork.Kind.DELIVER,home.getBlockPos(),-1,null) && home.count(Items.CHARCOAL)==1,"Native cooking produces the actual charcoal carried home");
        h.assertTrue(craft(c,worker,CivicRecipes.RecipeId.STICKS) && craft(c,worker,CivicRecipes.RecipeId.TORCH) && home.count(Items.TORCH)==4,"One actual charcoal and stick make four native torches");
        for(var cell:works.cells())if(cell.item()==Items.TORCH)h.assertTrue(build(c,worker,cell),"Each real light is carried and placed on its supported original plot");
        for(var cell:works.cells())if(cell.item()==ModItems.NEST.get()) {
            // Advance crop age only in this controlled transaction chain; the AI scenario uses native growth.
            for(var field:home.wild().fields()) {
                h.getLevel().setBlock(field,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),2);
                h.assertTrue(WildFamilyGameTest.act(c,worker,SocietyWork.Kind.HARVEST_FIELD,field),"Real native grain drops and paid replanting supply the next Nest");
            }
            WildFamilyGameTest.collectAll(c,hauler);int wheat=home.count(Items.WHEAT),planks=home.count(ModItems.GUAVA_PLANKS.get());
            h.assertTrue(craft(c,worker,CivicRecipes.RecipeId.NEST) && home.count(Items.WHEAT)==wheat-3 && home.count(ModItems.GUAVA_PLANKS.get())==planks-3
                    && build(c,worker,cell) && home.construction().owned(cell.pos()),"Every initial Nest costs its actual three wheat/three planks, travels in an original parcel and earns a real placement receipt");
        }
        h.assertTrue(complete(home) && home.wild().beds().stream().allMatch(home.construction()::owned) && !home.ripe(),"A real furnace, four supported lights and three resident-made Nests finish without granting a Keystone or inherited stats");
        preview(h.getLevel(),home,"wild_works_completed.json","Controlled native work chain from empty stores; selected capable zero-point founders, actual native stock and world actions; not natural timing.");
        WildFamilyGameTest.clean(c);h.succeed();
    }
    static void safety(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var worker=c.dogs().getFirst();var home=c.home();var works=home.wild().works();var stone=works.stones().getFirst();
        home.put(new ItemStack(Items.WOODEN_PICKAXE));h.assertTrue(act(c,worker,SocietyWork.Kind.FETCH_PICKAXE,home.getBlockPos(),-1,stone),"A real donated tool is reserved for this focused safety fixture");
        var exact=worker.society().workSupplies();WildFamilyGameTest.at(worker,stone);h.assertTrue(ItemStack.matches(exact,worker.society().workSupplies()),"Native whole-animal reload preserves the actual tool parcel");
        Consumer<EntityMobGriefingEvent> veto=e->{if(e.getEntity()==worker)e.setCanGrief(false);};NeoForge.EVENT_BUS.addListener(veto);
        try {h.assertTrue(!act(c,worker,SocietyWork.Kind.MINE_STONE,stone,-1,null) && works.stoneAvailable(stone) && ItemStack.matches(exact,worker.society().workSupplies()),"A native protection veto spends neither deposit permission nor tool durability");}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(act(c,worker,SocietyWork.Kind.MINE_STONE,stone,-1,null) && worker.society().workSupplies().getDamageValue()==1,"A valid mine spends the original deposit and one real tool wear");
        var saved=home.saveWithFullMetadata(h.getLevel().registryAccess());home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));h.getLevel().setBlock(stone,Blocks.STONE.defaultBlockState(),3);
        h.assertTrue(!works.stoneAvailable(stone) && !act(c,worker,SocietyWork.Kind.MINE_STONE,stone,-1,null) && worker.society().workSupplies().getDamageValue()==1,"Native home reload and a player-placed replacement never renew a spent deposit");
        var next=works.stones().stream().filter(works::canMine).findFirst().orElseThrow();Consumer<EntityMobGriefingEvent> replace=e->{if(e.getEntity()==worker)h.getLevel().setBlock(next,Blocks.DIRT.defaultBlockState(),3);};NeoForge.EVENT_BUS.addListener(replace);
        try {h.assertTrue(!act(c,worker,SocietyWork.Kind.MINE_STONE,next,-1,null) && worker.society().workSupplies().getDamageValue()==1,"A callback replacing the rock preserves the exact tool");}finally {NeoForge.EVENT_BUS.unregister(replace);}
        h.getLevel().setBlock(next,Blocks.STONE.defaultBlockState(),3);h.assertTrue(!works.stoneAvailable(next),"An observed lost original cannot authorize later replacement material");
        var dropped=worker.society().deathCargo();h.assertTrue(dropped.is(Items.WOODEN_PICKAXE) && dropped.getDamageValue()==1 && worker.society().deathCargo().isEmpty(),"A stranded miner releases its exact worn tool only once");
        WildFamilyGameTest.clean(c);h.succeed();
    }
    static void legacy(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true);var home=c.home();var data=home.saveWithFullMetadata(h.getLevel().registryAccess());data.remove("PrimitiveWorkshopPlots");data.remove("GladeStoneUsed");
        data.remove("PrimitiveRestPlots");data.putBoolean("Authorized",false);data.putInt("AnnexPlots",0);
        h.getLevel().setBlock(home.getBlockPos().offset(-2,0,3),Blocks.AIR.defaultBlockState(),3);
        h.getLevel().setBlock(home.getBlockPos().offset(0,0,3),com.bunnidogs.mod.registry.ModBlocks.NEST.get().defaultBlockState(),3);
        home.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),data));
        home.wild().tick();
        h.assertTrue(home.wild().natural() && !home.wild().works().enabled() && home.wild().works().cells().isEmpty()
                && home.authorized() && home.construction().cells().equals(home.construction().base())
                && h.getLevel().getBlockState(home.wild().beds().get(2)).is(com.bunnidogs.mod.registry.ModBlocks.NEST.get()),"Legacy families can walk around their original Nest to use the original room; no mining/workshop plots or nest relocation are granted");
        home.put(new ItemStack(Items.COBBLESTONE,8));h.assertTrue(!craft(c,c.dogs().getFirst(),CivicRecipes.RecipeId.FURNACE)
                && c.dogs().stream().allMatch(dog->dog.society().offers().stream().noneMatch(job->job.kind()==SocietyWork.Kind.FETCH_PICKAXE || job.kind()==SocietyWork.Kind.MINE_STONE)),"Stock and nearby stone cannot activate the new work chain in an old site");
        WildFamilyGameTest.clean(c);h.succeed();
    }
    static void ai(GameTestHelper h) {
        var c=WildFamilyGameTest.setup(h,true,true,true);var home=c.home();h.assertTrue(home.camp().bare() && home.getContainerSize()==0,"The workshop reference begins before its actual first cache");c.dogs().forEach(dog->dog.setNoAi(false));
        preview(h.getLevel(),home,"wild_camp_start.json","Native autonomous new-camp reference before work: four original logs, no usable cache, selected capable zero-point founders, empty education and no supplied stock/tools/food.");
        long started=h.getLevel().getGameTime();
        for(int checkpoint:new int[]{10000,40000,80000,110000,180000,260000})h.runAfterDelay(checkpoint,()->{
            if(home.residents().isEmpty())return;
            com.bunnidogs.mod.BunnidogsMod.LOGGER.info("WILD_WORKSHOP_WAIT tick={} guavas={} cobble={} charcoal={} paid_nests={} housed_ready_parents={} fruit={} coppices={}",h.getLevel().getGameTime()-started,home.count(ModItems.GUAVA.get()),home.count(Items.COBBLESTONE),home.count(Items.CHARCOAL),home.wild().beds().stream().filter(home.construction()::owned).count(),home.residents().stream().filter(d->d.readyForPen() && d.homeNest()!=null).count(),home.wild().fruit().stream().map(pos->h.getLevel().getBlockState(pos).toString()).toList(),home.orchard().stream().map(pos->h.getLevel().getBlockState(pos).toString()).toList());
        });
        h.succeedWhen(()->{
            h.assertTrue(home.workshop().available(home.wild().works().furnace())!=null && complete(home),
                    "Actual unsupervised empty-store residents must make their workbench/stone/furnace/charcoal/lights and first Nests; guavas="+home.count(ModItems.GUAVA.get())+", logs="+home.count(ModItems.GUAVA_LOG.get())+", planks="+home.count(ModItems.GUAVA_PLANKS.get())+", sticks="+home.count(Items.STICK)+", tables="+home.count(Items.CRAFTING_TABLE)+", axes="+home.count(Items.WOODEN_AXE)+", cobble="+home.count(Items.COBBLESTONE)+", charcoal="+home.count(Items.CHARCOAL)+", torches="+home.count(Items.TORCH)+", paid_nests="+home.wild().beds().stream().filter(home.construction()::owned).count()+", plan="+home.agenda().need()+" "+home.agenda().steps()+", jobs="+c.dogs().stream().map(dog->String.valueOf(dog.society().active())+" cargo="+dog.society().workSupplies()+" ready="+dog.society().ready()+" night="+dog.homeNight()+" at="+dog.position()).toList());
            h.assertTrue(home.construction().owned(home.wild().works().workbench()) && home.wild().beds().stream().anyMatch(home.construction()::owned) && c.dogs().stream().anyMatch(dog->dog.homeNest()!=null && home.wild().beds().contains(dog.homeNest()) && home.construction().owned(dog.homeNest())),"Actual native residents build their workbench and claim their first paid Nest without a player supplying either");
            com.bunnidogs.mod.BunnidogsMod.LOGGER.info("WILD_WORKSHOP_COMPLETE loaded_ticks={} paid_initial_nests={} guavas_left={} logs_left={} planks_left={}",h.getLevel().getGameTime()-started,home.wild().beds().stream().filter(home.construction()::owned).count(),home.count(ModItems.GUAVA.get()),home.count(ModItems.GUAVA_LOG.get()),home.count(ModItems.GUAVA_PLANKS.get()));
            preview(h.getLevel(),home,"wild_camp_completed.json","Same native autonomous site after its real ground-paid first cache, walked table/tools/stone/furnace/charcoal/lights and first claimed paid Nest. Normal growth, rations and night rest; no supplied materials or taught skills. Selected capable founder profile is separate from natural staffing/Survival/client acceptance.");
            WildFamilyGameTest.clean(c);
        });
    }
}
