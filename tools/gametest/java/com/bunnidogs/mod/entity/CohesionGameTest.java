package com.bunnidogs.mod.entity;

import java.util.List;
import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.CareView;
import com.bunnidogs.mod.network.CareViewPayload;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModVillagers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Real first-session operations and native menu lifetime; no GUI automation or claimed client playtest. */
public final class CohesionGameTest {
    private CohesionGameTest() {}
    private static Genome fixed(Genome genome,Gene gene,String name) {
        int id=gene.alleleNamed(name).id(); return genome.with(gene,0,id).with(gene,1,id);
    }
    private static void floor(GameTestHelper h,int y) {
        for (int x=0;x<=13;x++) for (int z=0;z<=10;z++) {
            h.setBlock(x,y,z,Blocks.STONE); for (int up=1;up<=4;up++) h.setBlock(x,y+up,z,Blocks.AIR);
        }
    }
    private static ServerPlayer player(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        CareMenuGameTest.prepare(player); return player;
    }
    private static CareMenu animal(GameTestHelper h,ServerPlayer player,Bunnidog dog) {
        player.closeContainer(); player.setShiftKeyDown(false); CareMenuGameTest.stow(player); player.snapTo(dog.position().add(1.5,0,0));
        player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.containerMenu instanceof CareMenu,"Empty-hand animal interaction opens its native care menu");
        return (CareMenu)player.containerMenu;
    }
    private static void press(GameTestHelper h,ServerPlayer player,CareMenu menu,int action) {
        if (menu.buttonId(action)<0 && java.util.Set.of(CarePanels.CARE,CarePanels.COURTSHIP,CarePanels.GROOM,
                CarePanels.TRAIN,CarePanels.FOLLOW,CarePanels.WAIT,CarePanels.RESIDENT,CarePanels.PROVISIONS,
                CarePanels.TAKE_BUNDLE,CarePanels.ROLES,CarePanels.GENETICS).contains(action)) {
            if (menu.buttonId(com.bunnidogs.mod.menu.AnimalPanels.CARE_MENU)<0)
                h.assertTrue(menu.clickMenuButton(player,menu.buttonId(CarePanels.BACK)),"Return to the animal's action menu");
            int group=java.util.Set.of(CarePanels.CARE,CarePanels.COURTSHIP,CarePanels.GROOM).contains(action)
                    ? com.bunnidogs.mod.menu.AnimalPanels.CARE_MENU : action==CarePanels.TAKE_BUNDLE ? CarePanels.CARGO
                    : java.util.Set.of(CarePanels.TRAIN,CarePanels.FOLLOW,CarePanels.WAIT,CarePanels.RESIDENT,CarePanels.PROVISIONS).contains(action)
                    ? com.bunnidogs.mod.menu.AnimalPanels.ORDERS : -1;
            if (group>=0) h.assertTrue(menu.clickMenuButton(player,menu.buttonId(group)),"Open the named action group");
        }
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(action)),"Live care action should succeed: "+action);
    }
    private static int count(ServerPlayer player,Item item) {
        int count=0; for (int i=0;i<player.getInventory().getContainerSize();i++) {
            var stack=player.getInventory().getItem(i); if (stack.is(item)) count+=stack.getCount();
        }
        return count;
    }
    private static boolean done(ServerPlayer player,String step) {
        var goal=player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","journey/"+step));
        return goal!=null && player.getAdvancements().getOrStartProgress(goal).isDone();
    }
    static void preview(String name,CareView view) {
        var out=new JsonObject(); out.addProperty("name",name); out.addProperty("subjectId",view.subjectId());
        out.add("title",ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,view.title()).getOrThrow());
        out.add("next",ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,view.next()).getOrThrow());
        var rows=new JsonArray(); for (var row:view.rows()) rows.add(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,row).getOrThrow()); out.add("rows",rows);
        var controls=new JsonArray(); for (var control:view.controls()) {
            var button=new JsonObject(); button.addProperty("action",control.id()&255); button.add("label",ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,control.label()).getOrThrow());
            button.add("hint",ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE,control.hint()).getOrThrow());
            button.addProperty("enabled",control.enabled()); button.addProperty("section",control.section()); controls.add(button);
        }
        out.add("controls",controls); BunnidogsMod.LOGGER.info("CARE_PREVIEW {}",out);
    }
    public static void animals(GameTestHelper h) {
        int y=347; floor(h,y); var level=h.getLevel();
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3.5F,y+1,3.5F);
        dog.setGenome(fixed(Genome.DEFAULT,Gene.TEMPERAMENT,"loyal")); dog.setAge(0); dog.setOnGround(true); dog.setHealth(dog.getMaxHealth()-4);
        dog.setStats(Stats.of(0,0,0,10,0)); dog.setHealth(dog.getMaxHealth()-4);
        var player=player(h); player.getInventory().setItem(9,new ItemStack(ModItems.GUAVA.get(),6));
        var calipers=new ItemStack(ModItems.CALIPERS.get()); var whistle=new ItemStack(ModItems.TRAIL_WHISTLE.get());
        player.getInventory().setItem(10,calipers); player.getInventory().setItem(11,whistle);
        var brush=new ItemStack(Items.BRUSH); brush.setDamageValue(brush.getMaxDamage()-1); player.getInventory().setItem(12,brush);
        var before=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(before);
        var menu=animal(h,player,dog);
        var after=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(after);
        h.assertTrue(before.buildResult().equals(after.buildResult()) && count(player,ModItems.GUAVA.get())==6 && done(player,"root"),
                "Opening care records discovery but leaves the complete animal and actual supplies unchanged");
        preview("Injured companion",menu.view());
        press(h,player,menu,com.bunnidogs.mod.menu.AnimalPanels.CARE_MENU);
        preview("Animal care",menu.view());
        var wire=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try {
            var snapshot=new CareViewPayload(menu.containerId,menu.view()); CareViewPayload.STREAM_CODEC.encode(wire,snapshot);
            h.assertTrue(CareViewPayload.STREAM_CODEC.decode(wire).equals(snapshot),"The native registry-aware care codec preserves translated text and bounded live controls");
        } finally { wire.release(); }
        float health=dog.getHealth();
        player.connection.handleContainerButtonClick(new ServerboundContainerButtonClickPacket(menu.containerId,menu.buttonId(CarePanels.CARE)));
        h.assertTrue(dog.getHealth()==health+Foraging.HEAL && count(player,ModItems.GUAVA.get())==5 && !dog.isInLove(),
                "The actual native button packet spends one inventory guava for care without courtship");
        press(h,player,menu,CarePanels.CARE);
        h.assertTrue(dog.getHealth()==dog.getMaxHealth() && count(player,ModItems.GUAVA.get())==4,"A second paid care meal restores full health");
        h.assertTrue(!menu.clickMenuButton(player,menu.buttonId(CarePanels.CARE)) && count(player,ModItems.GUAVA.get())==4,"Healthy adults keep unneeded care food");
        press(h,player,menu,CarePanels.ROLES);
        preview("Useful first roles",menu.view());
        press(h,player,menu,CarePanels.WORK); h.assertTrue(dog.workPaused(),"Work controls live beside the actual role readiness");
        press(h,player,menu,CarePanels.WORK);
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
        h.assertTrue(menu.buttonId(CarePanels.WORK)>=0 && !menu.clickMenuButton(player,menu.buttonId(CarePanels.WORK)) && !dog.workPaused(),"Adventure inspection cannot change work or spend supplies");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        press(h,player,menu,CarePanels.BACK);
        press(h,player,menu,CarePanels.TRAIN);
        h.assertTrue(dog.trail().belongsTo(player) && dog.trail().order()==Trail.Order.FOLLOW && count(player,ModItems.GUAVA.get())==3
                && player.getInventory().getItem(11)==whistle,"Menu training pays one real plain guava and retains the required reusable Whistle");
        preview("Trained partner",menu.view());
        var stranger=player(h); var strangerMenu=animal(h,stranger,dog);
        press(h,stranger,strangerMenu,com.bunnidogs.mod.menu.AnimalPanels.ORDERS);
        h.assertTrue(!strangerMenu.clickMenuButton(stranger,strangerMenu.buttonId(CarePanels.WAIT)) && dog.trail().order()==Trail.Order.FOLLOW,
                "Another keeper's panel cannot command or enable meals for this partner");
        h.assertTrue(!menu.clickMenuButton(stranger,menu.buttonId(CarePanels.ROLES)),"A container session belongs to its actual player");
        press(h,player,menu,CarePanels.WAIT); press(h,player,menu,CarePanels.PROVISIONS); press(h,player,menu,CarePanels.RESIDENT);
        h.assertTrue(dog.trail().order()==Trail.Order.RESIDENT && dog.trail().provisions() && count(player,ModItems.GUAVA.get())==3,
                "Named local orders and meal preference use the existing bond without another food cost");
        press(h,player,menu,CarePanels.GROOM);
        h.assertTrue(brush.isEmpty() && count(player,ModItems.WHISKER.get())==1 && dog.groomingWait()>0
                && !menu.clickMenuButton(player,menu.buttonId(CarePanels.GROOM)),"Grooming uses the actual unequipped Brush, reserves native recovery and delivers one real material exactly once");
        player.closeContainer(); CareMenuGameTest.stow(player); var contents=new net.minecraft.world.item.component.BundleContents.Mutable(net.minecraft.world.item.component.BundleContents.EMPTY);
        contents.tryInsert(new ItemStack(Items.DIAMOND,3)); var bundle=new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS,contents.toImmutable()); bundle.set(DataComponents.CUSTOM_NAME,Component.literal("Travel tools")); var expectedBundle=bundle.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,bundle); player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO);
        menu=animal(h,player,dog); var cargoBefore=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(cargoBefore);
        press(h,player,menu,CarePanels.CARGO); var cargoAfter=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(cargoAfter);
        h.assertTrue(menu.view().rows().size()==3 && cargoBefore.buildResult().equals(cargoAfter.buildResult()),"Cargo inspection exposes the actual bag and contents without changing any animal or item state");
        press(h,player,menu,CarePanels.BACK);
        var packedWire=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
        try {
            var payload=new CareViewPayload(menu.containerId,menu.view()); CareViewPayload.STREAM_CODEC.encode(packedWire,payload);
            h.assertTrue(CareViewPayload.STREAM_CODEC.decode(packedWire).equals(payload),"A fully equipped trained adult's complete controls fit the bounded native codec");
        } finally { packedWire.release(); }
        press(h,player,menu,CarePanels.TAKE_BUNDLE);
        h.assertTrue(dog.carriedBundleCount()==0 && java.util.stream.IntStream.range(0,player.getInventory().getContainerSize())
                .anyMatch(slot->ItemStack.matches(player.getInventory().getItem(slot),expectedBundle)) && count(player,ModItems.WHISKER.get())==1,"The cargo view returns the complete named native bundle exactly once while retaining earlier care products");
        press(h,player,menu,CarePanels.GENETICS);
        h.assertTrue(player.containerMenu!=menu && player.getInventory().getItem(10)==calipers && !menu.clickMenuButton(player,menu.buttonId(CarePanels.ROLES)),
                "Calipers open the existing genetics sheet and a closed care session cannot mutate the animal");
        var distant=animal(h,player,dog); player.snapTo(dog.position().add(20,0,0));
        h.assertTrue(!distant.stillValid(player) && !distant.clickMenuButton(player,distant.buttonId(CarePanels.ROLES)),"Leaving reach invalidates live care");
        var removed=animal(h,player,dog); dog.discard();
        h.assertTrue(!removed.stillValid(player) && !removed.clickMenuButton(player,removed.buttonId(CarePanels.ROLES)),"Removed animals invalidate their exact menu target");
    }
    public static void stations(GameTestHelper h) {
        int y=354; floor(h,y); var level=h.getLevel(); var player=player(h);
        var guavas=new ItemStack(ModItems.GUAVA.get(),8); guavas.set(DataComponents.CUSTOM_NAME,Component.literal("Actual pantry stock"));
        player.getInventory().setItem(9,guavas); player.getInventory().setItem(10,new ItemStack(Items.GLASS_BOTTLE,2));
        var pos=h.absolutePos(new BlockPos(2,y+1,2)); level.setBlock(pos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        var trough=(FeedingTroughBlockEntity)level.getBlockEntity(pos); var original=trough.saveWithFullMetadata(level.registryAccess());
        var menu=CareMenuGameTest.open(h,player,pos);
        h.assertTrue(original.equals(trough.saveWithFullMetadata(level.registryAccess())) && guavas.getCount()==8,"Opening an empty Trough cannot fill it or toggle its jobs");
        press(h,player,menu,CarePanels.ADD_FOOD); press(h,player,menu,CarePanels.MODE+TroughWork.GARDEN.ordinal()); press(h,player,menu,CarePanels.TENDING);
        h.assertTrue(guavas.getCount()==7 && trough.food().getCount()==1 && trough.workMode()==TroughWork.GARDEN
                && trough.getBlockState().getValue(FeedingTroughBlock.TENDING),"Visible Trough controls pay actual stock and choose the real saved garden policy");
        preview("Food and garden work",menu.view());
        int oldWithdraw=menu.buttonId(CarePanels.TAKE_FOOD); var reserved=trough.takeAll();
        var replacement=new ItemStack(ModItems.GUAVA.get()); replacement.set(DataComponents.CUSTOM_NAME,Component.literal("Another keeper's food")); trough.setInitialFood(replacement);
        h.assertTrue(!menu.clickMenuButton(player,oldWithdraw) && ItemStack.matches(trough.food(),replacement) && reserved.getCount()==1,
                "A stale withdrawal cannot take newly replaced shared stock");
        level.removeBlock(pos,false); level.setBlock(pos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);
        h.assertTrue(!menu.stillValid(player) && !menu.clickMenuButton(player,menu.buttonId(CarePanels.ADD_FOOD)) && guavas.getCount()==7,
                "Replacing a station with the same type cannot reuse the old block-entity session");
        var fresh=(FeedingTroughBlockEntity)level.getBlockEntity(pos);
        var legacy=fresh.saveWithFullMetadata(level.registryAccess()); var rich=new ItemStack(ModItems.GOLDEN_GUAVA.get(),2);
        rich.set(DataComponents.CUSTOM_NAME,Component.literal("Saved breeding guavas"));
        var encoded=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); encoded.store("Food",ItemStack.CODEC,rich);
        legacy.put("Food",encoded.buildResult().get("Food").copy());
        var restored=(FeedingTroughBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,fresh.getBlockState(),legacy,level.registryAccess()); level.setBlockEntity(restored);
        var hungry=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),2.5F,y+1,4.5F); hungry.setGenome(Genome.DEFAULT); hungry.setHealth(1);
        h.assertTrue(ItemStack.matches(restored.food(),rich) && !restored.canFeed(hungry),"Native legacy loading keeps whole upgraded stock while excluding it from ordinary meals");
        player.closeContainer(); var offeredRich=new ItemStack(ModItems.GOLDEN_GUAVA.get(),3); player.setItemInHand(InteractionHand.MAIN_HAND,offeredRich); player.setShiftKeyDown(true);
        player.gameMode.useItemOn(player,level,offeredRich,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        h.assertTrue(offeredRich.getCount()==3 && ItemStack.matches(restored.food(),rich),"A real crouched rich-guava offer keeps its hand and saved stock instead of feeding, filtering or consuming it");
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); var readonly=CareMenuGameTest.open(h,player,pos);
        h.assertTrue(!readonly.clickMenuButton(player,readonly.buttonId(CarePanels.TAKE_FOOD)) && ItemStack.matches(restored.food(),rich),
                "Actual empty-hand station opening permits Adventure inspection while keeping all mutation controls unavailable");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); var recovery=CareMenuGameTest.open(h,player,pos); press(h,player,recovery,CarePanels.TAKE_FOOD);
        h.assertTrue(restored.food().isEmpty() && count(player,ModItems.GOLDEN_GUAVA.get())==5,"The panel recovers all legacy and offered breeding-grade fruit without loss"); hungry.discard();
        var pressPos=h.absolutePos(new BlockPos(5,y+1,2)); level.setBlock(pressPos,ModBlocks.JUICER.get().defaultBlockState(),3);
        var juicer=(JuicerBlockEntity)level.getBlockEntity(pressPos); var press=CareMenuGameTest.open(h,player,pressPos);
        press(h,player,press,CarePanels.ADD_FRUIT); press(h,player,press,CarePanels.ADD_BOTTLE);
        h.assertTrue(guavas.getCount()==6 && count(player,Items.GLASS_BOTTLE)==1 && juicer.item(0).getCount()==1 && juicer.item(1).getCount()==1,
                "Press controls stock the actual native input slots and spend one of each real ingredient");
        preview("Juicer inputs",press.view());
        var cradlePos=h.absolutePos(new BlockPos(9,y+1,5)); level.setBlock(cradlePos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState().setValue(NurseryCradleBlock.FACING,Direction.NORTH),3);
        var cradle=(NurseryCradleBlockEntity)level.getBlockEntity(cradlePos);
        var seed=SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,1)); seed.set(DataComponents.CUSTOM_NAME,Component.literal("Chosen litter"));
        var expected=seed.copy(); player.getInventory().setItem(12,seed); player.getInventory().setItem(13,new ItemStack(Items.BONE_MEAL,4));
        var nursery=CareMenuGameTest.open(h,player,cradlePos); press(h,player,nursery,CarePanels.ADD_SEED);
        h.assertTrue(seed.isEmpty() && ItemStack.matches(cradle.storedSeed(),expected),"Insert seed moves one complete real litter, including its components");
        for (int i=0;i<4 && cradle.age()<SeedCropBlock.MAX_AGE;i++) press(h,player,nursery,CarePanels.GROW);
        h.assertTrue(cradle.age()==SeedCropBlock.MAX_AGE && count(player,Items.BONE_MEAL)<4,"Native paid bone meal grows the stored litter without replacing it");
        level.setBlock(cradlePos.north(2),Blocks.STONE.defaultBlockState(),3);
        h.assertTrue(!nursery.clickMenuButton(player,nursery.buttonId(CarePanels.HATCH)) && ItemStack.matches(cradle.storedSeed(),expected),
                "A newly obstructed landing pad refuses the hatch button and keeps the whole litter");
        preview("Blocked nursery",nursery.view());
        var markedBefore=cradle.saveWithFullMetadata(level.registryAccess());
        press(h,player,nursery,StationMarkers.SHOW_PAD);
        h.assertTrue(player.containerMenu!=nursery && markedBefore.equals(cradle.saveWithFullMetadata(level.registryAccess())),
                "Showing the real hatch area closes inspection without hatching, changing stock or spending materials");
        nursery=CareMenuGameTest.open(h,player,cradlePos); level.removeBlock(cradlePos.north(2),false);
        int oldHatch=nursery.buttonId(CarePanels.HATCH); var oldSeed=cradle.takeSeed();
        var other=SeedItem.create(new Seed(Genome.DEFAULT,Stats.of(1,2,3,4,5),Genome.DEFAULT,Stats.ZERO,2)); cradle.insert(other); cradle.grow(true);
        h.assertTrue(!nursery.clickMenuButton(player,oldHatch) && ItemStack.matches(cradle.storedSeed(),other) && ItemStack.matches(oldSeed,expected),
                "An old hatch click cannot hatch a replacement litter, even when both are ripe and safe");
        press(h,player,nursery,CarePanels.TAKE_SEED); press(h,player,nursery,CarePanels.AUTO_HATCH);
        h.assertTrue(cradle.storedSeed().isEmpty() && cradle.manualHatchOnly(),"Withdrawing and named redstone controls act independently");

        var boardPos=h.absolutePos(new BlockPos(6,y+1,8)); level.setBlock(boardPos,ModBlocks.COMMISSION_BOARD.get().defaultBlockState().setValue(CommissionBoardBlock.FACING,Direction.NORTH),3);
        var board=(CommissionBoardBlockEntity)level.getBlockEntity(boardPos);
        var candidate=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),6.5F,y+1,7.5F);
        var genes=fixed(Genome.DEFAULT,Gene.EARS,"bat").with(Gene.COAT,0,Gene.COAT.alleleNamed("pale").id());
        candidate.setGenome(genes); candidate.setStats(Stats.of(0,0,0,10,0)); candidate.setAge(0); candidate.setParents(Genome.DEFAULT,Genome.DEFAULT); candidate.setHealth(candidate.getMaxHealth()); candidate.setOnGround(true);
        var breeder=h.spawnWithNoFreeWill(EntityTypes.VILLAGER,4.5F,y+1,8.5F); breeder.setVillagerData(breeder.getVillagerData().withProfession(ModVillagers.BREEDER));
        player.getInventory().setItem(20,new ItemStack(Items.PAPER,3)); var commissions=CareMenuGameTest.open(h,player,boardPos);
        h.assertTrue(board.read().status()==Commissions.Status.GENES && !commissions.clickMenuButton(player,commissions.buttonId(CarePanels.SUBMIT)) && count(player,Items.PAPER)==3,
                "A partially inherited commission shows actual missing requirements and keeps its paper");
        preview("Commission progress",commissions.view());
        candidate.setGenome(fixed(fixed(fixed(genes,Gene.COAT,"pale"),Gene.LEGS,"hoofed"),Gene.BUILD,"stocky")); candidate.setStats(Stats.of(0,10,20,20,0));
        player.closeContainer(); player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BUNDLE)); player.snapTo(candidate.position().add(1,0,0));
        player.interactOn(candidate,InteractionHand.MAIN_HAND,Vec3.ZERO); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        commissions=CareMenuGameTest.open(h,player,boardPos);
        h.assertTrue(board.read().status()==Commissions.Status.READY && Commissions.Request.MOUNTAIN_COURIER.match(candidate.genome(),candidate.stats(),candidate.carriedBundleCount())==Commissions.Status.READY,
                "The real candidate qualifies for two different targets so shared-selection staleness matters");
        int oldSubmit=commissions.buttonId(CarePanels.SUBMIT); board.select(Commissions.Request.MOUNTAIN_COURIER);
        h.assertTrue(!commissions.clickMenuButton(player,oldSubmit) && count(player,Items.PAPER)==3 && board.completedCount(player)==0,
                "A stale request button cannot pay for a different qualifying commission");
        press(h,player,commissions,CarePanels.REQUEST+Commissions.Request.NIGHT_GUIDE.ordinal()); press(h,player,commissions,CarePanels.SUBMIT);
        h.assertTrue(count(player,Items.PAPER)==2 && board.completed(player,Commissions.Request.NIGHT_GUIDE) && board.completedCount(player)==1
                && candidate.carriedBundleCount()==1,"Submitting from the panel pays one actual paper, grants native progress, and retains the animal and cargo");
        h.assertTrue(!commissions.clickMenuButton(player,commissions.buttonId(CarePanels.SUBMIT)) && count(player,Items.PAPER)==2,"Completed requests cannot pay twice through the menu");
        candidate.discard(); breeder.discard();
    }
    public static void journey(GameTestHelper h) {
        int y=361; floor(h,y); var level=h.getLevel(); var player=player(h);
        var recipes=level.getServer().getRecipeManager().recipeMap();
        var bookInput=net.minecraft.world.item.crafting.CraftingInput.of(2,1,List.of(new ItemStack(Items.BOOK),new ItemStack(ModItems.GUAVA.get())));
        var books=recipes.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,bookInput,level).toList();
        h.assertTrue(books.size()==1 && books.getFirst().value().assemble(bookInput).is(ModItems.PEDIGREE_BOOK.get()),
                "The actual book + guava recipe unambiguously makes the reusable Pedigree Book");
        var guideInput=net.minecraft.world.item.crafting.CraftingInput.of(2,2,List.of(new ItemStack(Items.BOOK),new ItemStack(ModItems.GUAVA.get()),new ItemStack(Items.PAPER),ItemStack.EMPTY));
        var guides=recipes.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,guideInput,level).toList();
        h.assertTrue(guides.size()==1 && guides.getFirst().value().assemble(guideInput).is(ModItems.FIELD_GUIDE.get()),
                "Adding paper selects the optional reference without overlapping the Pedigree Book");
        var nestInput=net.minecraft.world.item.crafting.CraftingInput.of(3,2,List.of(new ItemStack(Items.WHEAT),new ItemStack(Items.WHEAT),new ItemStack(Items.WHEAT),
                new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS)));
        h.assertTrue(recipes.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,nestInput,level).anyMatch(r->r.value().assemble(nestInput).is(ModBlocks.NEST.asItem())),
                "Ordinary native wood can craft a starter home away from guava timber");
        var whistleInput=net.minecraft.world.item.crafting.CraftingInput.of(2,3,List.of(new ItemStack(Items.COPPER_INGOT),ItemStack.EMPTY,
                new ItemStack(Items.STRING),new ItemStack(Items.FEATHER),new ItemStack(Items.STRING),ItemStack.EMPTY));
        h.assertTrue(recipes.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,whistleInput,level).anyMatch(r->r.value().assemble(whistleInput).is(ModItems.TRAIL_WHISTLE.get())),
                "Copper, ordinary string and a feather craft the reusable starter Whistle without developed-line materials");
        for (var quill:List.of(Items.FEATHER,ModItems.WHISKER.get())) {
            var tableInput=net.minecraft.world.item.crafting.CraftingInput.of(3,3,List.of(new ItemStack(quill),new ItemStack(Items.COPPER_INGOT),new ItemStack(quill),
                    new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),ItemStack.EMPTY,new ItemStack(Items.OAK_PLANKS)));
            var boardInput=net.minecraft.world.item.crafting.CraftingInput.of(3,3,List.of(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),
                    ItemStack.EMPTY,new ItemStack(quill),ItemStack.EMPTY,new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT)));
            var penInput=net.minecraft.world.item.crafting.CraftingInput.of(3,3,List.of(new ItemStack(Items.OAK_FENCE),new ItemStack(Items.HAY_BLOCK),new ItemStack(Items.OAK_FENCE),
                    new ItemStack(Items.OAK_PLANKS),new ItemStack(quill),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.COPPER_INGOT)));
            for (var entry:List.of(java.util.Map.entry(tableInput,ModBlocks.GENETICS_TABLE.asItem()),java.util.Map.entry(boardInput,ModBlocks.COMMISSION_BOARD.asItem()),
                    java.util.Map.entry(penInput,ModBlocks.BREEDING_PEN.asItem())))
                h.assertTrue(recipes.getRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,entry.getKey(),level)
                        .anyMatch(r->r.value().assemble(entry.getKey()).is(entry.getValue())),"The actual workshop recipe accepts ordinary feathers and retains legacy Whiskers: "+entry.getValue());
        }
        var a=h.spawn(ModEntities.BUNNIDOG.get(),4.5F,y+1,4.5F); var b=h.spawn(ModEntities.BUNNIDOG.get(),6.5F,y+1,4.5F);
        a.setGenome(Genome.DEFAULT); b.setGenome(fixed(Genome.DEFAULT,Gene.COAT,"cream"));
        for (var dog:List.of(a,b)) { dog.setAge(0); dog.setNoAi(true); dog.setOnGround(true); dog.setHealth(dog.getMaxHealth()); }
        var nestA=h.absolutePos(new BlockPos(3,y,6)); var nestB=h.absolutePos(new BlockPos(7,y,6));
        level.setBlock(nestA,ModBlocks.NEST.get().defaultBlockState(),3); level.setBlock(nestB,ModBlocks.NEST.get().defaultBlockState(),3);
        player.getInventory().setItem(9,new ItemStack(ModItems.GUAVA.get(),6));
        animal(h,player,a); h.assertTrue(done(player,"root"),"Meeting an animal discovers the native first-session journey without a guide");
        for (var dog:List.of(a,b)) dog.setLeashedTo(player,false);
        h.startSequence().thenIdle(3).thenExecute(()->{
            CareMenuGameTest.assignHome(h,player,nestA); CareMenuGameTest.assignHome(h,player,nestB);
            CareMenuGameTest.assignHome(h,player,nestA);
            h.assertTrue(nestA.equals(a.homeNest()) && nestB.equals(b.homeNest()) && done(player,"home"),"Named Nest controls give two actual led adults separate native home claims");
            a.removeLeash(); b.removeLeash();
            var care=animal(h,player,a); press(h,player,care,CarePanels.COURTSHIP); player.closeContainer();
            var offered=player.getInventory().removeItemNoUpdate(9); player.setItemInHand(InteractionHand.MAIN_HAND,offered); player.snapTo(b.position().add(1,0,0));
            CareMenuGameTest.chooseGuava(h,player,b,InteractionHand.MAIN_HAND,CarePanels.COURTSHIP); var remainder=player.getMainHandItem(); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); player.getInventory().setItem(9,remainder);
            h.assertTrue(a.isInLove() && b.isInLove() && count(player,ModItems.GUAVA.get())==4 && done(player,"courtship"),"Menu and direct-hand courtship spend the two real guavas and join the same native breeding flow");
            a.setNoAi(false); b.setNoAi(false);
        }).thenWaitUntil(()->{
            h.assertTrue(!level.getEntitiesOfClass(ItemEntity.class,new AABB(a.blockPosition()).inflate(8),e->SeedItem.read(e.getItem()).isPresent()).isEmpty(),"The actual BreedGoal must produce a litter seed");
        }).thenExecute(()->{
            var drops=level.getEntitiesOfClass(ItemEntity.class,new AABB(a.blockPosition()).inflate(8),e->SeedItem.read(e.getItem()).isPresent());
            h.assertTrue(drops.size()==1 && a.getAge()>0 && b.getAge()>0 && done(player,"seed"),"One genuine mating makes one seed and starts native parental rest");
            var drop=drops.getFirst(); drop.setNoPickUpDelay(); drop.playerTouch(player);
            int slot=-1; for (int i=0;i<player.getInventory().getContainerSize();i++) if (SeedItem.read(player.getInventory().getItem(i)).isPresent()) { slot=i; break; }
            h.assertTrue(slot>=0,"The actual seed can be collected into the keeper's inventory");
            var seedStack=player.getInventory().removeItemNoUpdate(slot); var seed=SeedItem.read(seedStack).orElseThrow();
            var soil=h.absolutePos(new BlockPos(10,y,3)); level.setBlock(soil,Blocks.FARMLAND.defaultBlockState(),3);
            player.snapTo(Vec3.atCenterOf(soil).add(0,1,2)); player.setItemInHand(InteractionHand.MAIN_HAND,seedStack);
            player.gameMode.useItemOn(player,level,seedStack,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(soil),Direction.UP,soil,false));
            var crop=soil.above();
            h.assertTrue(seedStack.isEmpty() && level.getBlockEntity(crop) instanceof SeedCropBlockEntity && done(player,"plant"),"Native farmland planting spends the collected seed and records the real handoff");
            var meal=new ItemStack(Items.BONE_MEAL,4); player.setItemInHand(InteractionHand.MAIN_HAND,meal);
            for (int i=0;i<4 && !SeedCropBlock.isRipe(level.getBlockState(crop));i++) player.gameMode.useItemOn(player,level,meal,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(crop),Direction.UP,crop,false));
            h.assertTrue(SeedCropBlock.isRipe(level.getBlockState(crop)) && meal.getCount()<4,"Ordinary paid bone meal ripens the actual planted litter crop");
            h.assertTrue(player.gameMode.destroyBlock(crop),"A real native break harvests the ripe crop");
            var pups=level.getEntitiesOfClass(Bunnidog.class,new AABB(crop).inflate(3),Bunnidog::isBaby);
            h.assertTrue(!pups.isEmpty() && pups.stream().allMatch(p->p.motherGenome().orElseThrow().equals(seed.mother()) && p.fatherGenome().orElseThrow().equals(seed.father()))
                    && done(player,"litter") && count(player,ModItems.GUAVA.get())==4,"The complete starter path hatches actual parented pups, preserves remaining food, and completes the native journey without advanced stations or a manual");
        }).thenSucceed();
    }
}
