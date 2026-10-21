package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/** Deliberately enabled meals reserve actual native cargo/rest before reentrant healing or equipment changes. */
public final class TrailProvisionsGameTest {
    private TrailProvisionsGameTest() {}
    private static ItemStack bag(int guavas) {
        var food=new ItemStack(ModItems.GUAVA.get(),guavas); food.set(DataComponents.CUSTOM_NAME,Component.literal("Travel guavas"));
        var bag=new ItemStack(Items.DYED_BUNDLE.pick(DyeColor.BLUE));
        bag.set(DataComponents.CUSTOM_NAME,Component.literal("Native field provisions"));
        bag.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(List.of(new ItemStackTemplate(Items.DIAMOND,3),
                new ItemStackTemplate(ModItems.GOLDEN_GUAVA.get(),2),new ItemStackTemplate(Items.KELP,4),ItemStackTemplate.fromNonEmptyStack(food))));
        return bag;
    }
    private static long count(ItemStack bag,Item item) {
        return bag.getOrDefault(DataComponents.BUNDLE_CONTENTS,BundleContents.EMPTY).items().stream().map(ItemStackTemplate::create)
                .filter(stack->stack.is(item)).mapToLong(ItemStack::getCount).sum();
    }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); int floor=319;
        for(int x=0;x<=7;x++) for(int z=0;z<=6;z++) {
            h.setBlock(x,floor,z,Blocks.STONE); for(int y=1;y<=3;y++) h.setBlock(x,floor+y,z,Blocks.AIR);
        }
        var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,floor+1,3);
        dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.ZERO); dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        var player=h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setPos(dog.position().add(1,0,0));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),2));
        h.assertTrue(dog.trail().trainFromStock(player,player.getOffhandItem()),"Provisions use a deliberately trained partner");
        player.setItemInHand(InteractionHand.MAIN_HAND,bag(3)); player.interactOn(dog,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
        h.assertTrue(dog.carriedBundleCount()==1&&!dog.trail().provisions(),"Training and equipping cargo do not opt in to eating it");
        dog.setHealth(dog.getMaxHealth()-4); dog.trail().tick();
        h.assertTrue(ItemStack.matches(dog.carriedBundles().getFirst(),bag(3))&&dog.forageWait()==0,"Disabled provisions preserve all real cargo while hurt");
        dog.setHealth(dog.getMaxHealth()); var bowl=new ItemStack(Items.BOWL); player.setItemInHand(InteractionHand.MAIN_HAND,bowl);
        player.interactOn(dog,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO); dog.trail().tick();
        h.assertTrue(dog.trail().provisions()&&bowl.getCount()==1&&ItemStack.matches(dog.carriedBundles().getFirst(),bag(3)),
                "A reusable actual bowl opts in, while a healthy partner keeps all food");
        var stranger=h.makeMockServerPlayerInLevel(); stranger.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        stranger.setPos(player.position()); stranger.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BOWL));
        stranger.interactOn(dog,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
        h.assertTrue(dog.trail().provisions(),"Another keeper cannot change the provision preference");
        dog.setHealth(dog.getMaxHealth()-4); dog.setLeashedTo(player,false); dog.trail().tick();
        h.assertTrue(dog.forageWait()==0&&ItemStack.matches(dog.carriedBundles().getFirst(),bag(3)),"Native transport pauses automatic cargo meals"); dog.removeLeash();
        boolean[] observed={false};
        Consumer<LivingHealEvent> heal=event->{
            if(event.getEntity()!=dog)return;
            observed[0]=true;
            h.assertTrue(dog.forageWait()==600&&ItemStack.matches(dog.carriedBundles().getFirst(),bag(2)),
                    "Actual source food and shared recovery are reserved before native healing callbacks");
            dog.trail().tick();
            h.assertTrue(ItemStack.matches(dog.carriedBundles().getFirst(),bag(2)),"Reentrant care cannot buy another meal");
        };
        NeoForge.EVENT_BUS.addListener(heal);
        try { dog.trail().tick(); } finally { NeoForge.EVENT_BUS.unregister(heal); }
        h.assertTrue(observed[0]&&dog.getHealth()==dog.getMaxHealth()-2&&dog.forageWait()==600
                &&ItemStack.matches(dog.carriedBundles().getFirst(),bag(2))&&player.getOffhandItem().getCount()==1,
                "One exact named plain guava heals two HP while unrelated, wrong-Diet and upgraded cargo remain");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); dog.saveWithoutId(saved);
        var legacy=saved.buildResult(); legacy.remove("TrailProvisions");
        var old=ModEntities.BUNNIDOG.get().create(level,net.minecraft.world.entity.EntitySpawnReason.LOAD);
        old.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),legacy));
        h.assertTrue(old.trail().belongsTo(player)&&!old.trail().provisions(),"Missing legacy data retains training with provisions safely disabled");
        var basket=new ItemStack(ModItems.TRAVEL_BASKET.get()); var uuid=dog.getUUID();
        h.assertTrue(TravelBasketItem.capture(level,basket,dog)==TravelBasketItem.Result.CAPTURED,"A provisioned companion can travel in its ordinary basket");
        var arrival=h.absolutePos(new BlockPos(6,floor+1,3));
        h.assertTrue(TravelBasketItem.release(level,arrival,basket)==TravelBasketItem.Result.RELEASED,"Whole-native basket release succeeds");
        var restored=(Bunnidog)level.getEntity(uuid); restored.setOnGround(true); restored.trail().tick();
        h.assertTrue(restored.trail().provisions()&&restored.forageWait()==600&&ItemStack.matches(restored.carriedBundles().getFirst(),bag(2)),
                "Baskets preserve exact remaining cargo, opt-in and recovery without a second meal");
        var reset=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); restored.saveWithoutId(reset);
        var ready=reset.buildResult(); ready.remove("ForageWait");
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),ready)); restored.setOnGround(true); restored.setHealth(restored.getMaxHealth()-4);
        player.setPos(restored.position().add(-1,0,0));
        var refill=new ItemStack(Items.BUNDLE); refill.set(DataComponents.CUSTOM_NAME,Component.literal("Callback refill"));
        boolean[] replaced={false};
        Consumer<LivingHealEvent> replace=event->{
            if(event.getEntity()!=restored)return;
            replaced[0]=true;
            h.assertTrue(ItemStack.matches(restored.carriedBundles().getFirst(),bag(1)),"The second meal pays from the old bundle before equipment callbacks");
            restored.dropEquipment(level); player.setItemInHand(InteractionHand.MAIN_HAND,refill);
            player.interactOn(restored,InteractionHand.MAIN_HAND,net.minecraft.world.phys.Vec3.ZERO);
            restored.trail().tick();
        };
        NeoForge.EVENT_BUS.addListener(replace);
        try { restored.trail().tick(); } finally { NeoForge.EVENT_BUS.unregister(replace); }
        var drops=level.getEntitiesOfClass(ItemEntity.class,restored.getBoundingBox().inflate(3),e->ItemStack.matches(e.getItem(),bag(1)));
        h.assertTrue(replaced[0]&&restored.forageWait()==600&&restored.carriedBundleCount()==1
                &&restored.carriedBundles().getFirst().getHoverName().getString().equals("Callback refill")&&drops.size()==1
                &&count(drops.getFirst().getItem(),ModItems.GOLDEN_GUAVA.get())==2,
                "Dropped old equipment and callback-refilled cargo conserve the paid food and every untouched component");
        restored.discard();
    }
}
