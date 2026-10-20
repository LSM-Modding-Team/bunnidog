package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.block.CommissionBoardBlock;
import com.bunnidogs.mod.block.CommissionBoardBlockEntity;
import com.bunnidogs.mod.genetics.Commissions;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModVillagers;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

/** Real career/candidate/payment/reward flows and reentrant native advancement callbacks. */
public final class CommissionGameTest {
    private CommissionGameTest() {}
    private record Fixture(CommissionBoardBlockEntity board,Bunnidog dog,Villager breeder,net.minecraft.server.level.ServerPlayer player,BlockHitResult hit) {}
    private static Genome fixed(Genome genome,Gene gene,String allele) {
        int id=gene.alleleNamed(allele).id(); return genome.with(gene,0,id).with(gene,1,id);
    }
    private static Genome genome(Commissions.Request request) {
        return switch(request) {
            case NIGHT_GUIDE -> fixed(fixed(Genome.DEFAULT,Gene.COAT,"pale"),Gene.EARS,"bat");
            case COASTAL_SCOUT -> fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"seal"),Gene.LEGS,"paddle"),Gene.DIET,"shore_forager");
            case MOUNTAIN_COURIER -> fixed(fixed(Genome.DEFAULT,Gene.LEGS,"hoofed"),Gene.BUILD,"stocky");
            case EMBER_GARDENER -> fixed(fixed(Genome.DEFAULT,Gene.COAT,"ember"),Gene.DIET,"nether_forager");
            case FROST_STEWARD -> fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"snow"),Gene.EARS,"tiny"),Gene.DIET,"fisher");
            case DESERT_MESSENGER -> fixed(fixed(fixed(Genome.DEFAULT,Gene.EARS,"fennec"),Gene.LEGS,"fleet"),Gene.BUILD,"lean");
            case SWAMP_HERBALIST -> fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"moss"),Gene.LEGS,"webbed"),Gene.DIET,"mushroom_eater");
            case END_NURSERY -> fixed(fixed(fixed(fixed(Genome.DEFAULT,Gene.COAT,"void"),Gene.LEGS,"phase"),Gene.FERTILITY,"starborn"),Gene.DIET,"chorus_eater");
            case CROSSING_BRANCHES -> fixed(fixed(fixed(fixed(Genome.DEFAULT,Gene.EARS,"tiny"),Gene.COAT,"pale"),Gene.LEGS,"hoofed"),Gene.DIET,"nether_forager");
        };
    }
    private static Fixture fixture(GameTestHelper h,int floor) {
        var level=h.getLevel();
        for(int x=0;x<=7;x++)for(int z=0;z<=7;z++) {
            h.setBlock(x,floor,z,Blocks.STONE);
            for(int y=1;y<=4;y++) h.setBlock(x,floor+y,z,Blocks.AIR);
        }
        var pos=h.absolutePos(new BlockPos(3,floor+1,4));
        level.setBlock(pos,ModBlocks.COMMISSION_BOARD.get().defaultBlockState(),3);
        var board=(CommissionBoardBlockEntity)level.getBlockEntity(pos);
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,floor+1,3); dog.setGenome(genome(Commissions.Request.NIGHT_GUIDE));
        dog.setAge(0); dog.setStats(Stats.of(0,10,20,20,10)); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        var breeder=h.spawnWithNoFreeWill(EntityTypes.VILLAGER,5,floor+1,4); breeder.setBaby(false);
        breeder.setVillagerData(breeder.getVillagerData().withProfession(ModVillagers.BREEDER));
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(pos.east(2))); player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.PAPER,16));
        return new Fixture(board,dog,breeder,player,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
    }
    private static long count(Fixture f,Item item) {
        return java.util.stream.IntStream.range(0,f.player.getInventory().getContainerSize()).mapToLong(i->{var stack=f.player.getInventory().getItem(i);return stack.is(item)?stack.getCount():0;}).sum();
    }
    private static void submit(Fixture f) { f.player.gameMode.useItemOn(f.player,f.player.level(),f.player.getMainHandItem(),InteractionHand.MAIN_HAND,f.hit); }
    public static void rules(GameTestHelper h) {
        var f=fixture(h,330); var board=f.board; var dog=f.dog; var level=h.getLevel(); var paper=f.player.getMainHandItem();
        h.assertTrue(board.read().status()==Commissions.Status.PARENTS,"A creative-looking founder without actual birth parents cannot claim a commission");
        submit(f); h.assertTrue(paper.getCount()==16,"Refusals keep the actual submission paper");
        dog.setParents(Genome.DEFAULT,Genome.DEFAULT); f.breeder.setVillagerData(f.breeder.getVillagerData().withProfession(
                level.registryAccess().lookupOrThrow(Registries.VILLAGER_PROFESSION).getOrThrow(net.minecraft.world.entity.npc.villager.VillagerProfession.NONE)));
        h.assertTrue(board.read().status()==Commissions.Status.BREEDER,"A native non-Breeder villager cannot issue the request");
        f.breeder.setVillagerData(f.breeder.getVillagerData().withProfession(ModVillagers.BREEDER));
        dog.setStats(Stats.of(0,10,20,19,10)); h.assertTrue(board.read().status()==Commissions.Status.STATS,"A near-miss bred stat remains unfinished");
        dog.setStats(Stats.of(0,10,20,20,10)); var pad=board.getBlockPos().north(); level.setBlock(pad.above(),Blocks.STONE.defaultBlockState(),3);
        h.assertTrue(board.read().status()==Commissions.Status.BLOCKED,"Actual headroom blocks submissions"); level.removeBlock(pad.above(),false);
        var extra=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,331,3); extra.setGenome(Genome.DEFAULT);
        h.assertTrue(board.read().status()==Commissions.Status.CROWDED,"Crowded pads never choose a convenient candidate"); extra.discard();
        f.player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); submit(f);
        h.assertTrue(paper.getCount()==16,"Actual Adventure interactions keep paper and do not award"); f.player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        board.refresh(); h.assertTrue(board.getBlockState().getValue(CommissionBoardBlock.READY)
                &&board.getBlockState().getAnalogOutputSignal(level,board.getBlockPos(),Direction.NORTH)==15,"Native block/comparator state shows live shared qualification");
        int initialXp=f.player.totalExperience;
        dog.setStats(Stats.of(20,20,20,20,20));
        for(var request:Commissions.Request.values()) {
            h.assertTrue(board.request()==request,"Saved request cycling has a stable order");
            dog.setGenome(genome(request)); dog.setHealth(dog.getMaxHealth());
            if(request==Commissions.Request.MOUNTAIN_COURIER) {
                h.assertTrue(board.read().status()==Commissions.Status.CARGO,"The courier needs actual equipment as well as inherited capacity");
                f.player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.BUNDLE));
                f.player.interactOn(dog,InteractionHand.OFF_HAND,Vec3.ZERO);
                h.assertTrue(dog.carriedBundleCount()==1,"The requested real bundle is equipped without surrendering the animal");
            }
            h.assertTrue(board.read().status()==Commissions.Status.READY,"The real functional target should qualify: "+request+" / "+board.read().status());
            long beforeEmeralds=count(f,Items.EMERALD); int beforePaper=paper.getCount();
            submit(f);
            var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","commissions/"+request.key()));
            h.assertTrue(f.player.getAdvancements().getOrStartProgress(advancement).isDone()&&paper.getCount()==beforePaper-1
                    &&count(f,Items.EMERALD)>beforeEmeralds&&dog.isAlive(),"Native submission spends one paper and grants actual one-time material rewards: "+request);
            int xp=f.player.totalExperience; long stock=count(f,Items.EMERALD); submit(f);
            h.assertTrue(paper.getCount()==beforePaper-1&&f.player.totalExperience==xp&&count(f,Items.EMERALD)==stock,
                    "Re-reading/submitting a completed native goal never spends paper or pays again");
            board.cycle();
        }
        h.assertTrue(f.player.totalExperience-initialXp==500&&count(f,ModItems.GOLDEN_GUAVA.get())==1
                &&dog.carriedBundleCount()==1,"All nine actual native rewards retain cargo and pay their exact XP plus the advanced fruit");
        board.cycle(); var snapshot=board.saveWithFullMetadata(level.registryAccess());
        var loaded=(CommissionBoardBlockEntity)BlockEntity.loadStatic(board.getBlockPos(),board.getBlockState(),snapshot,level.registryAccess());
        h.assertTrue(loaded.request()==board.request(),"Native board saves retain only the selected stable request key");
        var tool=new ItemStack(ModItems.CALIPERS.get()); f.player.setItemInHand(InteractionHand.OFF_HAND,tool);
        tool.useOn(new net.minecraft.world.item.context.UseOnContext(f.player,InteractionHand.OFF_HAND,f.hit));
        h.assertTrue(snapshot.equals(board.saveWithFullMetadata(level.registryAccess()))&&tool.getDamageValue()==0,"Calipers request inspection is read-only");
        var rotated=board.getBlockState().rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
        h.assertTrue(rotated.getValue(CommissionBoardBlock.FACING)==Direction.EAST,"The visible front inspection pad rotates with placement");
        dog.discard(); f.breeder.discard();
    }
    public static void callbacks(GameTestHelper h) {
        var f=fixture(h,315); var dog=f.dog; var board=f.board; var level=h.getLevel(); dog.setParents(Genome.DEFAULT,Genome.DEFAULT);
        var otherPos=board.getBlockPos().north().east();
        level.setBlock(otherPos,ModBlocks.COMMISSION_BOARD.get().defaultBlockState().setValue(CommissionBoardBlock.FACING,Direction.WEST),3);
        var other=(CommissionBoardBlockEntity)level.getBlockEntity(otherPos);
        var paper=new ItemStack(Items.PAPER,2); paper.set(DataComponents.CUSTOM_NAME,Component.literal("Exact commission forms"));
        f.player.setItemInHand(InteractionHand.MAIN_HAND,paper); boolean[] observed={false};
        Consumer<AdvancementEvent.AdvancementEarnEvent> callback=event->{
            if(event.getEntity()!=f.player || !event.getAdvancement().id().equals(Identifier.fromNamespaceAndPath("bunnidogs","commissions/night_guide"))) return;
            observed[0]=true;
            h.assertTrue(paper.getCount()==1,"The actual form is reserved before native reward callbacks");
            h.assertTrue(!board.submit(f.player,InteractionHand.MAIN_HAND)&&!other.submit(f.player,InteractionHand.MAIN_HAND),
                    "Reentry through either station cannot claim the same native progress again");
            board.cycle(); h.assertTrue(board.request()==Commissions.Request.NIGHT_GUIDE,"In-progress selection cannot be changed by a callback");
            f.player.getInventory().setItem(9,paper); f.player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.APPLE,3));
            level.removeBlock(board.getBlockPos(),false);
        };
        NeoForge.EVENT_BUS.addListener(callback);
        try { submit(f); } finally { NeoForge.EVENT_BUS.unregister(callback); }
        h.assertTrue(observed[0]&&paper.getCount()==1&&paper.getHoverName().getString().equals("Exact commission forms")
                &&f.player.getMainHandItem().is(Items.APPLE)&&f.player.getMainHandItem().getCount()==3
                &&count(f,Items.EMERALD)==4&&count(f,Items.GLOW_BERRIES)==8&&f.player.totalExperience==50,
                "Committed native rewards survive removed stations and replaced hands without duplicate paper or material payouts");
        dog.discard(); f.breeder.discard();
    }
}
