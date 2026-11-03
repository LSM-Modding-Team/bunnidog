package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.society.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Native Staff dispatch and real parcels when household permissions are withdrawn. */
final class WorksiteLinksGameTest {
    private static void use(SocietyGameTest.Context c,BlockPos clicked,boolean crouch) {
        var p=c.p();p.setShiftKeyDown(crouch);
        p.snapTo(Vec3.atBottomCenterOf(new BlockPos(clicked.getX()+1,c.tree().getBlockPos().getY(),clicked.getZ())));
        p.gameMode.useItemOn(p,c.h().getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(clicked),Direction.UP,clicked,false));
    }
    private static void staff(SocietyGameTest.Context c) {
        c.p().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.STEWARD_STAFF.get()));use(c,c.tree().getBlockPos(),false);
        c.h().assertTrue(StewardStaffItem.hearth(c.p().getMainHandItem(),c.p())==c.tree(),"The real Staff binds through native block interaction");
    }
    private static boolean act(SocietyGameTest.Context c,SocietyWork.Kind kind,BlockPos target,int index,BlockPos destination) {
        SocietyGameTest.rest(c.worker());c.worker().snapTo(Vec3.atBottomCenterOf(target.east()));c.worker().setOnGround(true);
        return c.worker().society().perform(new SocietyWork.Job(kind,target,index,c.h().getLevel().getBlockState(target),null,destination));
    }
    private static void returns(SocietyGameTest.Context c,ItemStack expected) {
        c.h().assertTrue(ItemStack.matches(expected,c.worker().society().workSupplies()),"The original parcel survives revocation exactly");
        SocietyGameTest.rest(c.worker());var offer=c.worker().society().offers();
        c.h().assertTrue(offer.size()==1 && offer.getFirst().kind()==SocietyWork.Kind.DELIVER && offer.getFirst().target().equals(c.tree().getBlockPos()),"The next walking destination is the original Hearth");
        c.h().assertTrue(act(c,SocietyWork.Kind.DELIVER,c.tree().getBlockPos(),-1,null) && c.worker().society().workSupplies().isEmpty(),"The carrier walks the rejected parcel home once");
    }
    private static void finish(SocietyGameTest.Context c) {c.worker().discard();c.p().discard();c.h().succeed();}

    static void capacity(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var l=h.getLevel();staff(c);var sites=new ArrayList<BlockPos>();
        for(int i=0;i<13;i++) {
            var pos=t.getBlockPos().offset(-8+(i%5)*3,0,-6+(i/5)*3);sites.add(pos);
            l.setBlock(pos,Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,4),3);use(c,pos,false);
        }
        h.assertTrue(t.sites().size()==12 && !t.sites().contains(sites.get(12)),"Native Staff linking still respects the twelve-site bound");
        var saved=t.saveWithFullMetadata(l.registryAccess());t.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved));
        var missing=sites.getFirst();l.setBlock(missing,Blocks.AIR.defaultBlockState(),3);use(c,missing.below(),true);
        h.assertTrue(t.sites().size()==11 && !t.sites().contains(missing) && l.getBlockState(missing).isAir(),"Crouch-clicking the surviving floor releases a saved missing station without rebuilding it");
        use(c,sites.get(12),false);h.assertTrue(t.sites().size()==12 && t.sites().contains(sites.get(12)),"The freed allowance immediately accepts a real new work site");
        var replacement=sites.get(1);l.setBlock(replacement,Blocks.STONE.defaultBlockState(),3);use(c,replacement.below(),true);
        h.assertTrue(t.sites().contains(replacement),"The missing-site floor shortcut cannot silently choose an occupied cell");use(c,replacement,true);
        h.assertTrue(!t.sites().contains(replacement) && l.getBlockState(replacement).is(Blocks.STONE),"Directly withdrawing a stale permission preserves the player's replacement block");
        var field=sites.get(2);l.setBlock(field,Blocks.FARMLAND.defaultBlockState(),3);l.setBlock(field.above(),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7),3);use(c,field.above(),true);
        h.assertTrue(!t.sites().contains(field) && l.getBlockState(field.above()).getValue(CropBlock.AGE)==7,"Using a crop releases its linked soil without harvesting it");
        var kept=sites.get(3);Consumer<PlayerInteractEvent.RightClickBlock> veto=e->{if(e.getEntity()==c.p() && e.getPos().equals(kept))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(veto);try {use(c,kept,true);}finally {NeoForge.EVENT_BUS.unregister(veto);}
        h.assertTrue(t.sites().contains(kept),"Native protection runs before Staff unlinking");
        c.p().setGameMode(GameType.ADVENTURE);use(c,kept,true);h.assertTrue(t.sites().contains(kept),"Adventure players cannot withdraw permissions");
        c.p().setGameMode(GameType.SPECTATOR);use(c,kept,true);h.assertTrue(t.sites().contains(kept),"Spectators cannot withdraw permissions");c.p().setGameMode(GameType.SURVIVAL);
        var stranger=ApexTypesGameTest.player(h,c.p().position());h.assertTrue(!t.unlink(stranger,kept) && t.sites().contains(kept),"Another keeper cannot release this household's permission");stranger.discard();
        int remaining=t.sites().size();use(c,t.getBlockPos(),true);
        h.assertTrue(t.authorized() && t.sites().size()==remaining && StewardStaffItem.hearth(c.p().getMainHandItem(),c.p())==t,"Crouch-use on the Hearth retains its existing survey action and binding");
        h.assertTrue(l.getBlockState(kept).getValue(ComposterBlock.LEVEL)==4,"Permission changes do not consume native composter contents");finish(c);
    }

    static void parcels(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var l=h.getLevel();staff(c);
        var troughPos=t.getBlockPos().west(4);l.setBlock(troughPos,ModBlocks.FEEDING_TROUGH.get().defaultBlockState(),3);use(c,troughPos,false);
        var trough=(FeedingTroughBlockEntity)l.getBlockEntity(troughPos);t.put(new ItemStack(ModItems.GUAVA.get(),32));
        h.assertTrue(act(c,SocietyWork.Kind.SUPPLY,t.getBlockPos(),0,null),"A worker withdraws real supplies for the linked trough");var food=c.worker().society().workSupplies();use(c,troughPos,true);
        h.assertTrue(!act(c,SocietyWork.Kind.DELIVER,troughPos,-1,null) && trough.food().isEmpty(),"An already offered trough delivery cannot finish after unlinking");returns(c,food);
        h.assertTrue(t.count(ModItems.GUAVA.get())==32 && l.getBlockState(troughPos).getValue(FeedingTroughBlock.TENDING),"Returned supplies are exact; unlinking does not change shared Tending settings");

        var cradlePos=t.getBlockPos().east(4);l.setBlock(cradlePos,ModBlocks.NURSERY_CRADLE.get().defaultBlockState(),3);use(c,cradlePos,false);var cradle=(NurseryCradleBlockEntity)l.getBlockEntity(cradlePos);
        var seed=SeedItem.create(new Seed(Genome.DEFAULT,Stats.ZERO,Genome.DEFAULT,Stats.ZERO,17));seed.set(ModItems.HEARTH_SITE.get(),t.site().toString());seed.set(DataComponents.CUSTOM_NAME,Component.literal("Exact unborn litter"));t.setItem(4,seed.copy());
        h.assertTrue(act(c,SocietyWork.Kind.SEED,t.getBlockPos(),4,null),"The nurse carries the actual saved litter");use(c,cradlePos,true);
        h.assertTrue(!act(c,SocietyWork.Kind.DELIVER,cradlePos,-1,null) && cradle.seed().isEmpty(),"An unlinked Cradle rejects stale seed deliveries");returns(c,seed);
        h.assertTrue(ItemStack.matches(seed,t.getItem(1)),"Returning the litter preserves every item component");
        use(c,cradlePos,false);h.assertTrue(act(c,SocietyWork.Kind.SEED,t.getBlockPos(),1,null) && cradle.setManualHatchOnly(true),"The nurse can depart before the player switches the linked Cradle to manual care");
        h.assertTrue(!act(c,SocietyWork.Kind.DELIVER,cradlePos,-1,null) && cradle.seed().isEmpty(),"Manual care also blocks an in-flight autonomous seed delivery");returns(c,seed);

        var soil=t.getBlockPos().north(4);l.setBlock(soil,Blocks.FARMLAND.defaultBlockState(),3);use(c,soil,false);t.setItem(5,new ItemStack(Items.WHEAT_SEEDS,3));
        h.assertTrue(act(c,SocietyWork.Kind.FETCH_SEED,t.getBlockPos(),5,soil.above()),"A grower carries one real planting seed");var grain=c.worker().society().workSupplies();use(c,soil,true);
        h.assertTrue(!act(c,SocietyWork.Kind.SOW_FIELD,soil.above(),-1,null) && l.getBlockState(soil.above()).isAir(),"Revoking the field also blocks a stale planting job");returns(c,grain);
        h.assertTrue(t.count(Items.WHEAT_SEEDS)==3,"Field revocation neither plants nor loses a seed");finish(c);
    }

    static void furnace(GameTestHelper h) {
        var c=SocietyGameTest.setup(h);var t=c.tree();var l=h.getLevel();staff(c);var pos=t.getBlockPos().east(5);
        l.setBlock(pos,Blocks.FURNACE.defaultBlockState(),3);use(c,pos,false);var furnace=(AbstractFurnaceBlockEntity)l.getBlockEntity(pos);
        furnace.setItem(0,new ItemStack(ModItems.GUAVA_LOG.get()));furnace.setItem(1,new ItemStack(Items.COAL));
        for(int tick=0;tick<201;tick++)AbstractFurnaceBlockEntity.serverTick(l,pos,l.getBlockState(pos),furnace);
        h.assertTrue(furnace.getItem(2).is(Items.CHARCOAL),"A native recipe has already consumed real fuel and banked XP");
        t.put(new ItemStack(ModItems.GUAVA_LOG.get(),12));h.assertTrue(act(c,SocietyWork.Kind.FETCH_FURNACE,t.getBlockPos(),0,pos),"The worker is en route with a real furnace parcel");var parcel=c.worker().society().workSupplies();String token=t.workshop().token(pos);
        var before=furnace.saveWithFullMetadata(l.registryAccess());var unrelated=furnace.getPersistentData();unrelated.putString("OtherModExample","preserve");
        use(c,pos,true);
        h.assertTrue(!t.sites().contains(pos) && t.workshop().token(pos)==null && t.workshop().resolve(pos,token)==null,"Unlinking releases the original home and native furnace contracts");
        h.assertTrue(furnace.getPersistentData().getStringOr("OtherModExample","").equals("preserve"),"Unlinking preserves unrelated native extension data");
        var after=furnace.saveWithFullMetadata(l.registryAccess());before.remove("NeoForgeData");after.remove("NeoForgeData");
        h.assertTrue(before.equals(after),"Native items, cooking timers, used recipes and banked XP remain byte-for-byte equivalent");
        h.assertTrue(!act(c,SocietyWork.Kind.LOAD_FURNACE,pos,-1,null),"Revocation rejects a stale original input delivery");
        use(c,pos,false);h.assertTrue(!token.equals(t.workshop().token(pos)) && !act(c,SocietyWork.Kind.LOAD_FURNACE,pos,-1,null),"Relinking the same native furnace gets a new identity; old cargo cannot resume against it");returns(c,parcel);
        h.assertTrue(t.count(ModItems.GUAVA_LOG.get())==12,"The old parcel reaches its original stock exactly once");use(c,pos,true);
        var otherPos=t.getBlockPos().south(5);l.setBlock(otherPos,ModBlocks.HEARTH_TREE.get().defaultBlockState(),3);var other=(HearthTreeBlockEntity)l.getBlockEntity(otherPos);other.found(c.p());
        h.assertTrue(other.link(pos) && t.workshop().available(pos)==null && other.workshop().available(pos)==furnace,"A deliberately released furnace can be explicitly donated to another household");
        finish(c);
    }
}
