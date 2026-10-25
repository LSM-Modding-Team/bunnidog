package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.block.NurseryCradleBlockEntity;
import com.bunnidogs.mod.genetics.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** One short-lived, witnessed native outcome per pupil. Attempts and previews are not experience. */
@EventBusSubscriber(modid=BunnidogsMod.MODID)
public final class WorkObservation {
    private record Example(UUID teacher,Learning.Routine routine,BlockPos pos,long tick,BooleanSupplier succeeded) {}
    private final BunnidogLearning learning;
    private Example pending;
    WorkObservation(BunnidogLearning learning) {this.learning=learning;}
    void clear() {pending=null;}
    private void watch(ServerPlayer player,Learning.Routine routine,BlockPos pos,BooleanSupplier succeeded) {
        if(pending==null && learning.acceptsExample(player,routine,pos)) {
            pending=new Example(player.getUUID(),routine,pos.immutable(),player.level().getGameTime(),succeeded);
            learning.observe(routine,BunnidogLearning.Activity.WATCHING);
        }
    }
    void tick() {
        var example=pending;if(example==null)return;
        var dog=learning.animal();long now=dog.level().getGameTime();if(now<=example.tick)return;
        pending=null;
        if(!(dog.level() instanceof net.minecraft.server.level.ServerLevel level) || now>example.tick+3 || learning.lesson()!=example.routine)return;
        var player=level.getPlayerByUUID(example.teacher);
        if(player!=null && level.hasChunkAt(example.pos) && learning.acceptsExample(player,example.routine,example.pos) && example.succeeded.getAsBoolean())
            learning.demonstrated(player,example.routine,example.pos);
        if(learning.lesson()==example.routine)learning.observe(example.routine,example.routine==Learning.Routine.SCOUT ? BunnidogLearning.Activity.WATCHING : BunnidogLearning.Activity.SEARCHING);
    }
    private static List<Bunnidog> pupils(ServerPlayer player) {
        return player.level().getEntitiesOfClass(Bunnidog.class,player.getBoundingBox().inflate(BunnidogLearning.GUIDE_RANGE),dog->player.getUUID().equals(dog.learning().teacher()) && dog.learning().guided()).stream().limit(4).toList();
    }
    private static int count(ServerPlayer p,ItemStack item) {
        int n=0;for(int i=0;i<p.getInventory().getContainerSize();i++) {var actual=p.getInventory().getItem(i);if(ItemStack.isSameItemSameComponents(actual,item))n+=actual.getCount();}return n;
    }
    private static boolean paid(ServerPlayer player,InteractionHand hand,ItemStack before) {
        var after=player.getItemInHand(hand);
        return player.hasInfiniteMaterials() || (after.isEmpty() || ItemStack.isSameItemSameComponents(before,after)) && before.getCount()-after.getCount()==1;
    }
    private static boolean grew(BlockState before,BlockState after) {
        if(!after.is(before.getBlock()))return false;
        for(var property:before.getProperties()) {
            var old=before.getValue(property);var next=after.getValue(property);
            if(property.getName().equals("age") && old instanceof Integer a && next instanceof Integer b && b>a)return true;
        }
        return before.hasProperty(CaveVines.BERRIES) && !before.getValue(CaveVines.BERRIES) && after.getValue(CaveVines.BERRIES);
    }
    private static BlockState plantingExample(ServerPlayer player,BlockPos at,ItemStack input) {
        var level=player.level();var planned=ResidentPlanting.plan(level,at,input);if(planned!=null)return planned;
        if(!level.getBlockState(at).isAir())return null;
        var seed=input.is(Items.WHEAT_SEEDS) ? Blocks.WHEAT : input.is(Items.BEETROOT_SEEDS) ? Blocks.BEETROOTS : input.is(Items.MELON_SEEDS) ? Blocks.MELON_STEM : null;
        return seed!=null && seed.defaultBlockState().canSurvive(level,at) ? seed.defaultBlockState() : null;
    }
    private static List<ItemEntity> nearby(ServerPlayer p,BlockPos pos) {return p.level().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2)).stream().limit(33).toList();}
    /** Real player-attributed drops are checked after native removal/spawning and later event cancellation. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void broken(BlockDropsEvent event) {
        if(!(event.getBreaker() instanceof ServerPlayer player) || event.getDrops().isEmpty())return;
        var level=player.level();var pos=event.getPos().immutable();var before=event.getState();
        var drops=event.getDrops().stream().limit(16).toList();
        for(var dog:pupils(player)) {
            var routine=dog.learning().lesson();
            if(routine==Learning.Routine.SCOUT) {if(!before.is(MineralScouting.ORES))continue;}
            else if(routine==Learning.Routine.HARVEST) {var food=GardenGoal.harvestExample(dog,pos,before);if(food.isEmpty() || drops.stream().noneMatch(d->ItemStack.isSameItemSameComponents(d.getItem(),food)))continue;}
            else continue;
            var keys=drops.stream().map(d->d.getItem().copyWithCount(1)).toList();var totals=keys.stream().mapToInt(s->count(player,s)).toArray();
            dog.learning().observation().watch(player,routine,pos,()->{
                if(level.getBlockState(pos).equals(before))return false;
                for(int i=0;i<drops.size();i++)if(level.getEntity(drops.get(i).getId())==drops.get(i) && !drops.get(i).getItem().isEmpty() && ItemStack.isSameItemSameComponents(drops.get(i).getItem(),keys.get(i)) || count(player,keys.get(i))>totals[i])return true;
                return false;
            });
        }
    }
    /** Capture actual use context, then wait for both the native world change and its payment/produce. */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void used(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        var level=player.level();var clicked=event.getPos().immutable();var before=level.getBlockState(clicked);var hand=event.getHand();var input=player.getItemInHand(hand).copy();
        for(var dog:pupils(player)) {
            var routine=dog.learning().lesson();
            if(routine==Learning.Routine.HARVEST) {
                var food=GardenGoal.harvestExample(dog,clicked,before);if(food.isEmpty())continue;
                var existing=nearby(player,clicked);if(existing.size()>32)continue;var ids=existing.stream().map(ItemEntity::getUUID).toList();int carried=count(player,food);
                dog.learning().observation().watch(player,routine,clicked,()->!level.getBlockState(clicked).equals(before) && (count(player,food)>carried || nearby(player,clicked).stream().anyMatch(d->!ids.contains(d.getUUID()) && ItemStack.isSameItemSameComponents(d.getItem(),food))));
            } else if(routine==Learning.Routine.GARDEN) {
                if(input.is(Items.BONE_MEAL) && GardenGoal.cultivationExample(dog,clicked,before)) {
                    dog.learning().observation().watch(player,routine,clicked,()->paid(player,hand,input) && grew(before,level.getBlockState(clicked)));
                } else if(before.is(Blocks.COMPOSTER) && before.getValue(ComposterBlock.LEVEL)<ComposterBlock.MAX_LEVEL && ComposterBlock.getValue(input)>0 && dog.canEat(input)) {
                    dog.learning().observation().watch(player,routine,clicked,()->paid(player,hand,input) && level.getBlockState(clicked).is(Blocks.COMPOSTER) && level.getBlockState(clicked).getValue(ComposterBlock.LEVEL)>before.getValue(ComposterBlock.LEVEL));
                } else if(!input.isEmpty()) {
                    var at=clicked.relative(event.getFace());
                    // Native planting may replace the interacted cell (water/replaceable vegetation).
                    if(before.canBeReplaced())at=clicked;
                    if(!level.hasChunkAt(at))continue;var target=at.immutable();var old=level.getBlockState(target);
                    var planned=plantingExample(player,target,input);var food=input.is(Items.WHEAT_SEEDS) ? new ItemStack(Items.WHEAT) : input.is(Items.BEETROOT_SEEDS) ? new ItemStack(Items.BEETROOT) : input.is(Items.MELON_SEEDS) ? new ItemStack(Items.MELON_SLICE) : input;
                    if(planned!=null && dog.canEat(food))dog.learning().observation().watch(player,routine,target,()->paid(player,hand,input) && !level.getBlockState(target).equals(old) && level.getBlockState(target).is(planned.getBlock()) && level.getBlockState(target).canSurvive(level,target));
                }
            }
        }
    }
    /** Both held-item and native-menu care call this only after real paid growth of the same litter. */
    public static void nurtured(net.minecraft.world.entity.player.Player player,NurseryCradleBlockEntity cradle,ItemStack litter,int age) {
        if(!(player instanceof ServerPlayer p) || cradle.age()<=age)return;
        var pos=cradle.getBlockPos().immutable();
        for(var dog:pupils(p))dog.learning().observation().watch(p,Learning.Routine.NURSERY,pos,()->p.level().getBlockEntity(pos)==cradle && cradle.age()>age && ItemStack.matches(cradle.storedSeed(),litter));
    }
}
