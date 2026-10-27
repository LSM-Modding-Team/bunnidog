package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class CivicFieldsGameTest {
    private static BlockPos field(SocietyGameTest.Context c,boolean ripe) {
        var l=c.h().getLevel();var at=c.tree().getBlockPos().east(5);l.setBlock(at.below().north(),Blocks.WATER.defaultBlockState(),3);l.setBlock(at.below(),Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE,7),3);if(ripe)l.setBlock(at,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),3);
        c.h().assertTrue(c.tree().link(at.below()),"The keeper explicitly donates this particular farmland plot");return at;
    }
    private static void at(Bunnidog d,BlockPos at) {SocietyGameTest.rest(d);d.snapTo(Vec3.atBottomCenterOf(at.south()));d.setOnGround(true);}
    private static boolean collectSeed(SocietyGameTest.Context c,BlockPos field) {at(c.worker(),c.tree().getBlockPos());int slot=CivicFields.seedSlot(c.tree());return c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.FETCH_SEED,c.tree().getBlockPos(),slot,c.tree().getBlockState(),null,field));}
    private static boolean work(SocietyGameTest.Context c,SocietyWork.Kind kind,BlockPos pos) {at(c.worker(),pos);return c.worker().society().perform(new SocietyWork.Job(kind,pos,-1,c.h().getLevel().getBlockState(pos)));}
    private static void finish(SocietyGameTest.Context c) {c.worker().discard();c.p().discard();c.h().succeed();}
    public static void journey(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var field=field(c,false);c.tree().put(new ItemStack(Items.WHEAT_SEEDS));h.assertTrue(!c.worker().canEat(new ItemStack(Items.WHEAT)),"This farmer is truly guava-only, not secretly a grain eater");
        h.assertTrue(collectSeed(c,field) && c.tree().count(Items.WHEAT_SEEDS)==0 && c.worker().society().workSupplies().is(Items.WHEAT_SEEDS),"An actual crop seed walks out in saved household cargo");SocietyGameTest.rest(c.worker());h.assertTrue(work(c,SocietyWork.Kind.SOW_FIELD,field) && h.getLevel().getBlockState(field).is(Blocks.WHEAT) && c.worker().society().cargoEmpty(),"One reloaded seed pays the actual empty authorized field");
        h.getLevel().setBlock(field,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),3);h.assertTrue(work(c,SocietyWork.Kind.HARVEST_FIELD,field),"A qualified farmer can grow building/nursery material independently of its personal diet");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(field).inflate(2),e->e.getItem().has(ModItems.FARM_ORIGIN.get()));h.assertTrue(drops.stream().anyMatch(e->e.getItem().is(Items.WHEAT)),"The actual native crop loot exists physically in the field before transport");
        int wheat=drops.stream().filter(e->e.getItem().is(Items.WHEAT)).mapToInt(e->e.getItem().getCount()).sum();for(var drop:drops) {drop.setNoPickUpDelay();at(c.worker(),drop.blockPosition());h.assertTrue(c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,drop.blockPosition(),drop.getId(),h.getLevel().getBlockState(drop.blockPosition()))) && !c.worker().society().workSupplies().has(ModItems.FARM_ORIGIN.get()),"A hauler collects an exact physical plot receipt into ordinary paid stock");h.assertTrue(work(c,SocietyWork.Kind.DELIVER,c.tree().getBlockPos()),"The actual harvest returns to the real common inventory");}
        var after=h.getLevel().getBlockState(field);h.assertTrue(c.tree().count(Items.WHEAT)==wheat && (after.isAir() || after.is(Blocks.WHEAT) && after.getValue(CropBlock.AGE)==0),"The world/store totals reflect a single native harvest, with no virtual grain reward");finish(c);
    }
    public static void coppice(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var crop=c.tree().orchard().getFirst();c.tree().put(new ItemStack(Items.WOODEN_AXE));h.getLevel().setBlock(crop,ModBlocks.GUAVA_COPPICE.get().defaultBlockState().setValue(CoppiceBlock.AGE,7),3);
        h.assertTrue(work(c,SocietyWork.Kind.PRUNE,crop) && c.tree().count(ModItems.GUAVA.get())==0 && c.tree().count(ModItems.GUAVA_LOG.get())==0,"Coppice pruning leaves actual food and timber at its worksite, never a remote stock credit");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(crop).inflate(2),item->item.getItem().has(ModItems.FARM_ORIGIN.get()));h.assertTrue(drops.size()==2,"Two real owned resource stacks await an actual hauler");
        for(var item:drops) {item.setNoPickUpDelay();at(c.worker(),item.blockPosition());h.assertTrue(c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,item.blockPosition(),item.getId(),h.getLevel().getBlockState(item.blockPosition()))) && work(c,SocietyWork.Kind.DELIVER,c.tree().getBlockPos()),"Food/timber cargo walks home through its original inventory transaction");}
        h.assertTrue(c.tree().count(ModItems.GUAVA.get())==4 && c.tree().count(ModItems.GUAVA_LOG.get())==2,"One actual paid pruning conserves the exact promised food and timber");finish(c);
    }
    public static void protection(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var field=field(c,false);c.tree().put(new ItemStack(Items.WHEAT_SEEDS));h.assertTrue(collectSeed(c,field),"An actual planting parcel precedes the protection test");
        Consumer<BlockEvent.EntityPlaceEvent> deny=e->{if(e.getEntity()==c.worker())e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(deny);
        try {h.assertTrue(!work(c,SocietyWork.Kind.SOW_FIELD,field) && h.getLevel().getBlockState(field).isAir() && c.worker().society().workSupplies().getCount()==1,"A native planting veto neither edits land nor consumes the saved seed");}finally{NeoForge.EVENT_BUS.unregister(deny);}
        h.getLevel().setBlock(field.below(),Blocks.DIRT.defaultBlockState(),3);SocietyGameTest.rest(c.worker());h.assertTrue(c.worker().society().plan().kind()==SocietyWork.Kind.DELIVER && work(c,SocietyWork.Kind.DELIVER,c.tree().getBlockPos()) && c.tree().count(Items.WHEAT_SEEDS)==1,"Changing the donated soil releases the work front and returns the real seed without re-tilling player land");finish(c);
    }
    public static void ownership(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var field=field(c,true);h.assertTrue(work(c,SocietyWork.Kind.HARVEST_FIELD,field),"One actual field harvest owns its own native output");
        var own=h.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(field).inflate(2),e->e.getItem().is(Items.WHEAT)).getFirst();own.setNoPickUpDelay();var otherStack=own.getItem().copy();otherStack.set(ModItems.FARM_ORIGIN.get(),UUID.randomUUID().toString());var other=new ItemEntity(h.getLevel(),own.getX(),own.getY(),own.getZ(),otherStack);other.setTarget(c.tree().owner());h.getLevel().addFreshEntity(other);
        var privateDrop=new ItemEntity(h.getLevel(),field.getX()+.5,field.getY()+.2,field.getZ()+.5,new ItemStack(Items.WHEAT,32));h.getLevel().addFreshEntity(privateDrop);at(c.worker(),field);
        h.assertTrue(!c.worker().society().perform(new SocietyWork.Job(SocietyWork.Kind.GATHER_FIELD,privateDrop.blockPosition(),privateDrop.getId(),h.getLevel().getBlockState(privateDrop.blockPosition()))) && privateDrop.getItem().getCount()==32 && !CivicFields.available(c.tree(),other),"A nearby player's loose materials and another site's crops are not communal permissions");
        h.runAfterDelay(45,()->{h.assertTrue(own.isAlive() && other.isAlive() && own.getItem().getCount()==1 && other.getItem().getCount()==1,"Native ground-item merging cannot collapse different household receipts even under the same keeper");finish(c);});
    }
    public static void ai(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var plot=field(c,true);
        // Cover the fixture's one-block-thick platform irrigation. Otherwise random crop velocity
        // can fall down its water shaft, testing an unreachable void drop instead of field hauling.
        h.getLevel().setBlock(plot.below().north(),Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP).setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED,true),3);
        c.worker().setNoAi(false);c.p().snapTo(c.p().position().add(18,0,0));
        h.succeedWhen(()->{h.assertTrue(c.tree().count(Items.WHEAT)>0,"Native farmer/hauler AI must harvest a real field and walk its actual grain home: job="+c.worker().society().active()+", cargo="+c.worker().society().workSupplies()+", at="+c.worker().position()+", ready="+c.worker().society().ready()+", offers="+c.worker().society().offers()+", field="+h.getLevel().getBlockState(plot)+", drops="+CivicFields.drops(c.tree()).stream().map(item->item.getItem()+" at "+item.position()+" seen="+CivicFields.visible(c.worker(),item)).toList());c.worker().discard();c.p().discard();});
    }
}
