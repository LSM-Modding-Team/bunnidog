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
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(16,180,16));var repair=station(h,"pack_armoury",pos);var armour=new ItemStack(ModItems.BARDING.get("astralite").get());armour.setDamageValue(200);repair.setItem(0,armour);repair.setItem(1,new ItemStack(ModItems.ASTRALITE_SHARD.get()));repair.setItem(2,new ItemStack(ModItems.CINDERITE_INGOT.get(),2));repair.setItem(3,new ItemStack(ModItems.SINEW.get(),4));repair.operate(level);
        h.assertTrue(armour.getDamageValue()==80 && repair.getItem(1).isEmpty() && repair.getItem(3).isEmpty(),"Real barding repairs consume exact native stock rather than copying a suit");repair.operate(level);h.assertTrue(armour.getDamageValue()==80,"An empty armoury cannot repair for free");
        var bind=station(h,"resonance_bench",pos.east(4));var enchants=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(enchants.getOrThrow(Enchantments.MENDING),1);var pick=new ItemStack(ModItems.ASTRALITE_TOOLS.get("pickaxe").get());pick.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My original pick"));pick.setDamageValue(71);bind.setItem(0,pick);bind.setItem(1,book);bind.setItem(2,new ItemStack(ModItems.CINDERITE_INGOT.get(),8));bind.setItem(3,new ItemStack(ModItems.STARLIT_DUST.get(),8));bind.setItem(4,new ItemStack(ModItems.ASTRALITE_SHARD.get()));bind.operate(level);
        h.assertTrue(pick.getEnchantments().getLevel(enchants.getOrThrow(Enchantments.MENDING))==1 && pick.getDamageValue()==71 && book.isEmpty() && bind.getItem(2).isEmpty(),"Binding consumes the actual book/materials and preserves the exact original gear/wounds");
        var saved=repair.saveWithFullMetadata(level.registryAccess());var loaded=(ProjectStation)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,repair.getBlockState(),saved,level.registryAccess());
        h.assertTrue(loaded.getItem(0).getDamageValue()==80 && loaded.getItem(1).isEmpty(),"Native station saves do not repair/refill inventory on load");
        level.destroyBlock(pos,false);h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2),e->e.getItem().getDamageValue()==80).size()==1,"Destroying the real station releases exactly its owned suit");
    }
    static void outpost(GameTestHelper h) {
        ApexTypesGameTest.floor(h);var level=h.getLevel();var at=ApexTypesGameTest.center(h);var pos=BlockPos.containing(at);var heart=station(h,"bastion_heart",pos);heart.setItem(0,new ItemStack(ModItems.CINDERITE_INGOT.get()));heart.setItem(1,new ItemStack(ModItems.STARLIT_DUST.get()));heart.operate(level);
        h.assertTrue(heart.powered() && heart.isEmpty(),"The Bastion runs on real paid inventory fuel");
        var p=ApexTypesGameTest.player(h,at.add(5,0,0));var aerie=station(h,"launch_aerie",pos.east(4));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));aerie.setItem(0,new ItemStack(ModItems.CINDERITE_INGOT.get(),2));aerie.setItem(1,new ItemStack(ModItems.STARLIT_DUST.get()));aerie.use(p);
        h.assertTrue(p.getDeltaMovement().y>2 && aerie.isEmpty(),"A paid aerie launches an actual Elytra instead of granting free flight");
        var nursery=station(h,"nursery_hearth",pos.north(5));var pup=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.TRIGGERED);pup.setGenome(com.bunnidogs.mod.genetics.Genome.DEFAULT);pup.setAge(-1000);pup.setNoAi(true);pup.snapTo(at.add(0,0,-7));level.addFreshEntity(pup);level.setBlock(pup.blockPosition().above(5),Blocks.STONE.defaultBlockState(),3);nursery.setItem(0,new ItemStack(GuildProjects.material("nursery_bedding")));nursery.setItem(1,new ItemStack(ModItems.GUAVA.get()));
        for(int i=0;i<3;i++)nursery.operate(level);h.assertTrue(pup.getAge()==-980 && nursery.isEmpty(),"Sheltered pups receive real meal/bedding-supported growth");
        var second=station(h,"nursery_hearth",pos.north(6));second.setItem(0,new ItemStack(GuildProjects.material("nursery_bedding")));second.setItem(1,new ItemStack(ModItems.GUAVA.get()));for(int i=0;i<3;i++)second.operate(level);h.assertTrue(pup.getAge()==-980,"Multiple nursery buildings cannot stack the growth boost");pup.discard();p.discard();
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
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());wagon.saveWithoutId(out);var loaded=ModEntities.KENNEL_WAGON.get().create(level,EntitySpawnReason.LOAD);loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()));h.assertTrue(loaded.getItem(0).getCount()==37,"Native wagon saves retain exact physical freight");loaded.discard();wagon.ejectPassengers();dogs.forEach(Bunnidog::discard);wagon.discard();p.discard();
    }
}
