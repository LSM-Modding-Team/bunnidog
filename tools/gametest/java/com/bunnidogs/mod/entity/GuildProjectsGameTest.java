package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.guild.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.expedition.PowerModes;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

final class GuildProjectsGameTest {
    static ProjectStation station(GameTestHelper h,String name,BlockPos pos) {h.getLevel().setBlock(pos,GuildProjects.BLOCKS.get(name).get().defaultBlockState(),3);return (ProjectStation)h.getLevel().getBlockEntity(pos);}
    static void ammo(GameTestHelper h) {
        var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));p.getInventory().clearContent();var battery=new ItemStack(GuildProjects.BATTERY.get());p.setItemInHand(InteractionHand.MAIN_HAND,battery);
        p.getInventory().setItem(9,new ItemStack(ModItems.CINDERITE_INGOT.get(),18));p.getInventory().setItem(10,new ItemStack(ModItems.STARLIT_DUST.get(),36));p.setShiftKeyDown(true);GuildProjects.BATTERY.get().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(GuildProjects.units(battery,ModItems.PRISM_CELL.get())==256 && p.getInventory().getItem(9).isEmpty() && p.getInventory().getItem(10).isEmpty(),"A real refill transfers paid materials into a single finite saved battery");
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.PRISM_CELL.get(),3));PowerModes.pay(p,ModItems.PRISM_CELL.get(),5);
        h.assertTrue(p.getOffhandItem().isEmpty() && GuildProjects.units(battery,ModItems.PRISM_CELL.get())==254,"Mixed plain/ammo-reservoir payment consumes exactly five units");
        battery.set(ModItems.AMMO_UNITS.get(),500);p.getInventory().setItem(9,new ItemStack(ModItems.CINDERITE_INGOT.get(),18));p.getInventory().setItem(10,new ItemStack(ModItems.STARLIT_DUST.get(),36));GuildProjects.BATTERY.get().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(battery.get(ModItems.AMMO_UNITS.get())==500 && p.getInventory().getItem(9).getCount()==18,"A refill cannot waste paid overflow or exceed capacity");
        var dressing=new ItemStack(GuildProjects.material("field_dressing"),4);p.getInventory().setItem(11,dressing);PowerModes.pay(p,ModItems.HEART_SPOOL.get(),3);
        h.assertTrue(dressing.getCount()==1 && !dressing.is(ModItems.HEART_SPOOL.get()),"Processed dressing pays Standard fuel without becoming a cloned role organ");p.discard();
    }
    static void workshop(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(16,180,16));var repair=station(h,"pack_armoury",pos);var suitItem=ModItems.BARDING.get("astralite").get();var armour=new ItemStack(suitItem);armour.setDamageValue(200);armour.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Travel suit"));
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(pos.east()));p.getInventory().clearContent();repair.use(p);
        h.assertTrue(p.containerMenu instanceof com.bunnidogs.mod.menu.ProjectMenu,"The workshop immediately opens its own native input/result layout");var menu=(com.bunnidogs.mod.menu.ProjectMenu)p.containerMenu;
        p.getInventory().setItem(9,armour);menu.clicked(27,0,ContainerInput.QUICK_MOVE,p);
        h.assertTrue(repair.getItem(0).is(suitItem) && repair.getItem(0).getDamageValue()==200 && repair.getItem(0).get(DataComponents.CUSTOM_NAME).getString().equals("Travel suit") && p.getInventory().getItem(9).isEmpty(),"Native shift-click selects the actual named, damaged suit as the workpiece");armour=repair.getItem(0);
        repair.setItem(2,new ItemStack(ModItems.ASTRALITE_SHARD.get()));repair.setItem(3,new ItemStack(ModItems.CINDERITE_INGOT.get(),2));repair.setItem(4,new ItemStack(ModItems.SINEW.get(),4));preview(menu,"armoury");
        repair.operate(level);
        h.assertTrue(armour.getDamageValue()==80 && repair.getItem(0).isEmpty() && repair.getItem(1)==armour && repair.getItem(2).isEmpty() && repair.getItem(4).isEmpty(),"One paid repair returns the original suit in a real result slot");
        var reserve=new ItemStack(ModItems.BARDING.get("iron").get());reserve.setDamageValue(150);repair.setItem(10,reserve);repair.operate(level);
        h.assertTrue(reserve.getDamageValue()==150,"Stored reserve equipment is never silently selected as a workpiece");preview(menu,"armoury_ready");
        var saved=repair.saveWithFullMetadata(level.registryAccess());var loaded=(ProjectStation)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,repair.getBlockState(),saved,level.registryAccess());
        h.assertTrue(loaded.getItem(1).getDamageValue()==80 && loaded.getItem(0).isEmpty() && loaded.getItem(10).getDamageValue()==150,"Native saves preserve the actual result and unselected stock");
        menu.clicked(1,0,ContainerInput.QUICK_MOVE,p);h.assertTrue(repair.getItem(1).isEmpty() && p.getInventory().countItem(suitItem)==1,"Taking the output transfers the actual suit exactly once");
        for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(suitItem)){repair.setItem(1,p.getInventory().removeItem(i,1));break;}
        var bind=station(h,"resonance_bench",pos.east(4));p.snapTo(Vec3.atBottomCenterOf(pos.east(3)));bind.use(p);var bindMenu=(com.bunnidogs.mod.menu.ProjectMenu)p.containerMenu;
        var enchants=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(enchants.getOrThrow(Enchantments.MENDING),1);book.enchant(enchants.getOrThrow(Enchantments.UNBREAKING),3);
        var pick=new ItemStack(ModItems.ASTRALITE_TOOLS.get("pickaxe").get());pick.enchant(enchants.getOrThrow(Enchantments.MENDING),1);pick.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My original pick"));pick.setDamageValue(71);
        bind.setItem(0,pick);bind.setItem(1,book);bind.setItem(3,new ItemStack(ModItems.CINDERITE_INGOT.get(),24));bind.setItem(4,new ItemStack(ModItems.STARLIT_DUST.get(),24));bind.setItem(5,new ItemStack(ModItems.ASTRALITE_SHARD.get(),3));preview(bindMenu,"binding");bind.operate(level);
        h.assertTrue(pick.getEnchantments().getLevel(enchants.getOrThrow(Enchantments.MENDING))==1 && pick.getEnchantments().getLevel(enchants.getOrThrow(Enchantments.UNBREAKING))==3 && pick.getDamageValue()==71 && book.isEmpty() && bind.getItem(2)==pick && bind.getItem(3).isEmpty(),"Binding applies the useful part of a multi-enchantment book, pays only applied levels and preserves the original equipment");
        p.snapTo(p.position().add(100,0,0));bindMenu.clicked(2,0,ContainerInput.PICKUP,p);h.assertTrue(bind.getItem(2)==pick && bindMenu.getCarried().isEmpty(),"A stale out-of-reach workshop cannot transfer its real output");p.closeContainer();p.discard();
        level.destroyBlock(pos,false);h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2),e->e.getItem().getDamageValue()==80).size()==1,"Destroying the real station releases exactly its owned suit");
    }
    static void outpost(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var at=ApexTypesGameTest.center(h);var pos=BlockPos.containing(at);var heart=station(h,"bastion_heart",pos);heart.setItem(0,new ItemStack(ModItems.CINDERITE_INGOT.get()));heart.setItem(1,new ItemStack(ModItems.STARLIT_DUST.get()));heart.operate(level);
        h.assertTrue(heart.powered() && heart.isEmpty(),"The Bastion runs on real paid inventory fuel");
        var p=ApexTypesGameTest.player(h,at.add(5,0,0));var aerie=station(h,"launch_aerie",pos.east(4));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));aerie.setItem(0,new ItemStack(ModItems.CINDERITE_INGOT.get(),2));aerie.setItem(1,new ItemStack(ModItems.STARLIT_DUST.get()));aerie.use(p);
        h.assertTrue(p.getDeltaMovement().y>2 && aerie.isEmpty(),"A paid aerie launches an actual Elytra instead of granting free flight");
        aerie.setItem(0,new ItemStack(ModItems.CINDERITE_INGOT.get(),2));aerie.setItem(1,new ItemStack(ModItems.STARLIT_DUST.get()));p.setShiftKeyDown(true);aerie.use(p);
        h.assertTrue(p.containerMenu instanceof com.bunnidogs.mod.menu.ProjectMenu && p.containerMenu.clickMenuButton(p,0) && p.containerMenu==p.inventoryMenu && aerie.isEmpty(),"The native Launch button pays once and closes the supply menu so flight can begin");p.setShiftKeyDown(false);
        var nursery=station(h,"nursery_hearth",pos.north(5));var pup=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.TRIGGERED);pup.setGenome(com.bunnidogs.mod.genetics.Genome.DEFAULT);pup.setAge(-1000);pup.setNoAi(true);pup.snapTo(at.add(0,0,-7));level.addFreshEntity(pup);level.setBlock(pup.blockPosition().above(5),Blocks.STONE.defaultBlockState(),3);nursery.setItem(0,new ItemStack(GuildProjects.material("nursery_bedding")));nursery.setItem(1,new ItemStack(ModItems.GUAVA.get()));
        for(int i=0;i<3;i++)nursery.operate(level);h.assertTrue(pup.getAge()==-980 && nursery.isEmpty(),"Sheltered pups receive real meal/bedding-supported growth");
        var second=station(h,"nursery_hearth",pos.north(6));second.setItem(0,new ItemStack(GuildProjects.material("nursery_bedding")));second.setItem(1,new ItemStack(ModItems.GUAVA.get()));for(int i=0;i<3;i++)second.operate(level);h.assertTrue(pup.getAge()==-980,"Multiple nursery buildings cannot stack the growth boost");
        for(var s:List.of(heart,aerie,nursery)) {p.snapTo(Vec3.atBottomCenterOf(s.getBlockPos().east()));p.setShiftKeyDown(true);s.use(p);h.assertTrue(p.containerMenu instanceof com.bunnidogs.mod.menu.ProjectMenu,"Physical supply stations expose live state without an instruction menu");preview((com.bunnidogs.mod.menu.ProjectMenu)p.containerMenu,s.kind());p.closeContainer();}pup.discard();p.discard();
    }
    static void towns(GameTestHelper h) {
        CrownDungeonGameTest.floor(h);var level=h.getLevel();var c=CrownDungeonGameTest.origin(h);var hall=c.offset(18,1,18);var heart=station(h,"guild_heart",hall);heart.generatedTown();LanternTowns.register(level,hall);
        var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(hall.east(3)));var citizen=ModEntities.WARREN_CITIZEN.get().create(level,EntitySpawnReason.STRUCTURE);citizen.initialize(hall,3);citizen.snapTo(p.position().add(1,0,0));level.addFreshEntity(citizen);citizen.setNoAi(true);
        citizen.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(p.containerMenu instanceof MerchantMenu,"An actual peaceful Elder opens Minecraft's trading interface");var menu=(MerchantMenu)p.containerMenu;var offer=citizen.getOffers().getFirst();menu.setSelectionHint(0);menu.getSlot(0).set(offer.getCostA().copy());menu.getSlot(1).set(offer.getCostB().copy());menu.clicked(2,0,ContainerInput.PICKUP,p);
        var token=menu.getCarried().copy();h.assertTrue(token.is(ModItems.HOMEWARD_TOTEM.get()) && token.get(ModItems.GUILD_HOME.get()).pos().equals(hall) && menu.getSlot(0).getItem().isEmpty() && offer.getUses()==1,"A real native purchase pays inputs once and creates the town-bound rescue totem");menu.setCarried(ItemStack.EMPTY);p.closeContainer();
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());citizen.saveWithoutId(out);var restored=ModEntities.WARREN_CITIZEN.get().create(level,EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));h.assertTrue(restored.getOffers().getFirst().getUses()==1,"Merchant saves preserve finite uses and original stock");restored.discard();
        p.snapTo(Vec3.atBottomCenterOf(c.offset(-20,1,-20)));p.setItemInHand(InteractionHand.OFF_HAND,token);p.setHealth(1);p.invulnerableTime=0;p.hurtServer(level,level.damageSources().generic(),10000);ApexPlayerEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
        h.assertTrue(p.isAlive() && p.getOffhandItem().isEmpty() && p.distanceToSqr(Vec3.atCenterOf(hall))<25*25,"A successful native totem pop saves the player and teleports to the bound real Warren");var secondHall=hall.east(160);
        for(int dx=-22;dx<=3;dx++)for(int dz=-14;dz<=3;dz++)level.setBlock(secondHall.offset(dx,-1,dz),ModBlocks.ROOTSTONE.get().defaultBlockState(),3);
        var second=station(h,"guild_heart",secondHall);second.generatedTown();LanternTowns.register(level,secondHall);
        var gate=station(h,"guild_gate",hall.offset(0,-1,3));p.snapTo(Vec3.atBottomCenterOf(gate.getBlockPos().above()));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);gate.use(p);
        h.assertTrue(p.distanceToSqr(Vec3.atCenterOf(secondHall))<25*25,"The native portal reaches another real registered Warren with a safe physical landing");
        var third=station(h,"guild_heart",hall.west(160));third.generatedTown();LanternTowns.register(level,third.getBlockPos());
        p.snapTo(Vec3.atBottomCenterOf(gate.getBlockPos().above()));gate.use(p);
        h.assertTrue(p.distanceToSqr(Vec3.atCenterOf(secondHall))<25*25,"Discovering another town cannot silently change a portal's saved destination");
        citizen.discard();p.discard();
    }
    static void habitation(GameTestHelper h) {
        var level=h.getLevel();var center=CrownDungeonGameTest.origin(h);var piece=new LanternWarrenPiece(center);var bounds=piece.getBoundingBox();
        for(int x=bounds.minX()>>4;x<=bounds.maxX()>>4;x++)for(int z=bounds.minZ()>>4;z<=bounds.maxZ()>>4;z++) {
            level.getChunk(x,z);var cp=new net.minecraft.world.level.ChunkPos(x,z);
            piece.postProcess(level,level.structureManager(),level.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(2),new net.minecraft.world.level.levelgen.structure.BoundingBox(cp.getMinBlockX(),level.getMinY(),cp.getMinBlockZ(),cp.getMaxBlockX(),level.getMaxY()-1,cp.getMaxBlockZ()),cp,center);
        }
        h.runAfterDelay(230,()->{
            var people=level.getEntitiesOfClass(WarrenCitizen.class,new net.minecraft.world.phys.AABB(center).inflate(75));
            h.assertTrue(people.size()==8 && people.stream().allMatch(p->!p.isNoAi() && !p.getOffers().isEmpty()),"A fully generated live town actually populates all eight intelligent merchants, with valid spawn clearance: "+people.size());
            for(int x=-40;x<=-20;x++)for(int z:new int[]{26,27}) {
                var soil=center.offset(x,0,z);for(int n=0;n<10;n++)level.getBlockState(soil).randomTick(level,soil,level.getRandom());
                h.assertTrue(level.getBlockState(soil).is(Blocks.FARMLAND) && level.getBlockState(soil).getValue(net.minecraft.world.level.block.FarmlandBlock.MOISTURE)==7,"Actual covered irrigation keeps the town fields hydrated through repeated native farmland ticks");
            }
            var heart=(ProjectStation)level.getBlockEntity(center.offset(12,1,19));var save=heart.saveWithFullMetadata(level.registryAccess());var loaded=(ProjectStation)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(heart.getBlockPos(),heart.getBlockState(),save,level.registryAccess());level.setBlockEntity(loaded);for(int i=0;i<20;i++)loaded.operate(level);
            h.assertTrue(level.getEntitiesOfClass(WarrenCitizen.class,new net.minecraft.world.phys.AABB(center).inflate(75)).size()==8,"Town reload cannot duplicate residents or reset finite spawn attempts");
            people.forEach(WarrenCitizen::discard);h.succeed();
        });
    }
    static void wagon(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var at=ApexTypesGameTest.center(h);var p=ApexTypesGameTest.player(h,at.add(0,0,1));var wagon=ModEntities.KENNEL_WAGON.get().create(level,EntitySpawnReason.TRIGGERED);wagon.owner(p.getUUID());wagon.snapTo(at);level.addFreshEntity(wagon);wagon.setItem(0,new ItemStack(Items.DIAMOND,37));
        var dogs=new ArrayList<Bunnidog>();p.getInventory().setItem(9,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        for(int i=0;i<2;i++) {var d=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.TRIGGERED);d.setGenome(com.bunnidogs.mod.genetics.Genome.DEFAULT.with(com.bunnidogs.mod.genetics.Gene.BUILD,0,1).with(com.bunnidogs.mod.genetics.Gene.BUILD,1,1));d.setAge(0);d.setOnGround(true);d.setNoAi(true);d.snapTo(at.add(i+2,0,1));level.addFreshEntity(d);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),2));h.assertTrue(d.trail().trainFromStock(p,p.getOffhandItem()),"Real trained adults form the wagon team");dogs.add(d);}
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.LEAD));wagon.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(p.getMainHandItem().isEmpty(),"Hitching pays one actual Lead");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));wagon.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(wagon.getPassengers().size()==1,"The other actual animal boards; its puller is not duplicated into a berth");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);wagon.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(wagon.getControllingPassenger()==p && wagon.getPassengers().size()==2,"The real owner can drive with an actual animal seated behind them");
        h.assertTrue(wagon.getLeashHolder()==dogs.getFirst(),"The paid hitch is a native visible lead to the real puller");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SHEARS));wagon.mobInteract(p,InteractionHand.MAIN_HAND);
        h.assertTrue(!wagon.mayBeLeashed() && p.getInventory().countItem(Items.LEAD)==1 && wagon.getItem(0).getCount()==37,"Cutting the hitch returns its one real Lead without disturbing freight or passengers");
        wagon.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(p.getInventory().countItem(Items.LEAD)==1,"A repeated cut cannot refund the same hitch twice");
        var offered=new ItemStack(Items.LEAD,2);p.setItemInHand(InteractionHand.MAIN_HAND,offered);offered.shrink(1);dogs.getFirst().setLeashedTo(p,true);
        wagon.mobInteract(p,InteractionHand.MAIN_HAND);h.assertTrue(offered.getCount()==1 && !dogs.getFirst().mayBeLeashed() && wagon.getLeashHolder()==dogs.getFirst(),"A keeper's already-paid lead transfers directly into the hitch without paying or dropping a second lead");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());wagon.saveWithoutId(out);var loaded=ModEntities.KENNEL_WAGON.get().create(level,EntitySpawnReason.LOAD);loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));h.assertTrue(loaded.getItem(0).getCount()==37 && loaded.mayBeLeashed(),"Native wagon saves retain exact freight and the actual paid hitch");loaded.discard();wagon.ejectPassengers();dogs.forEach(Bunnidog::discard);wagon.discard();p.discard();
    }
    private static void preview(com.bunnidogs.mod.menu.ProjectMenu menu,String name) {
        var row=new com.google.gson.JsonObject();row.addProperty("kind",menu.kind);row.addProperty("state",menu.state());row.addProperty("width",menu.WIDTH);row.addProperty("height",menu.HEIGHT);
        var slots=new com.google.gson.JsonArray();for(var slot:menu.slots) {var s=item(slot.getItem());s.addProperty("index",slot.index);s.addProperty("x",slot.x);s.addProperty("y",slot.y);slots.add(s);}row.add("slots",slots);
        var costs=new com.google.gson.JsonArray();if(menu.workshop())menu.job().costs().forEach(s->costs.add(item(s)));row.add("costs",costs);
        var fuel=new com.google.gson.JsonArray();for(int i=1;i<=3;i++)fuel.add(menu.fuel(i));row.add("fuel",fuel);
        var file=java.nio.file.Path.of("project_workshops_preview.json");try {var root=java.nio.file.Files.exists(file) ? com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(file)).getAsJsonObject() : new com.google.gson.JsonObject();root.add(name,row);java.nio.file.Files.writeString(file,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root));}catch(java.io.IOException e){throw new RuntimeException(e);}
    }
    private static com.google.gson.JsonObject item(ItemStack stack) {var row=new com.google.gson.JsonObject();row.addProperty("item",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());row.addProperty("count",stack.getCount());return row;}
}
