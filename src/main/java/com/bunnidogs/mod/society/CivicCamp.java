package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.registry.ModItems;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** No inventory before the first real cache: ordinary marked ground inputs, carried results and paid installation. */
public final class CivicCamp {
    private final HearthTreeBlockEntity home;
    private boolean busy;
    CivicCamp(HearthTreeBlockEntity home) {this.home=home;}
    public boolean bare() {return home.wild().campStart() && !home.cached();}
    public BlockPos mat() {return home.getBlockPos().north();}
    private AABB bounds() {return new AABB(mat()).inflate(.15,.75,.15);}
    private boolean live() {
        if(!bare() || !home.attached() || home.owner()==null || home.isLocked())return false;
        var level=home.getLevel();var root=home.getBlockPos();var at=mat();
        return !level.restoringBlockSnapshots && level.hasChunksAt(root.offset(-1,-1,-1),root.offset(1,1,1))
                && level.hasChunksAt(at.offset(-1,-1,-1),at.offset(1,1,1)) && level.getWorldBorder().isWithinBounds(at)
                && level.getWorldBorder().isWithinBounds(root) && !level.hasNeighborSignal(root) && !level.hasNeighborSignal(at)
                && level.getBlockState(at).isAir() && level.getFluidState(at).isEmpty()
                && level.getBlockState(at.below()).isFaceSturdy(level,at.below(),Direction.UP);
    }
    public boolean available() {return live();}
    /** Actual paid cache only; looking at a recipe or stock never creates a movement request. */
    public boolean cachePending() {
        return live() && (stock().stream().anyMatch(item->item.getItem().is(ModItems.ROOT_CACHE.get()))
                || home.residents().stream().anyMatch(d->d.society().campCacheCarrier(home)));
    }
    private boolean sees(Entity actor,BlockPos pos) {
        var level=actor.level();var from=actor.blockPosition();
        if(!level.hasChunksAt(new BlockPos(Math.min(from.getX(),pos.getX()),Math.min(from.getY(),pos.getY()),Math.min(from.getZ(),pos.getZ())),new BlockPos(Math.max(from.getX(),pos.getX()),Math.max(from.getY(),pos.getY()),Math.max(from.getZ(),pos.getZ()))))return false;
        var hit=level.clip(new ClipContext(actor.getEyePosition(),Vec3.atCenterOf(pos),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
        return hit.getType()==HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }
    public boolean workspace(Bunnidog dog) {
        return live() && dog.society().hearth()==home && dog.society().ready()
                && dog.distanceToSqr(Vec3.atCenterOf(home.getBlockPos()))<=16 && dog.distanceToSqr(Vec3.atCenterOf(mat()))<=16
                && sees(dog,home.getBlockPos()) && sees(dog,mat()) && dog.learning().witnesses(home.getBlockPos(),mat());
    }
    /** Prospective goal position must also clear the larger installed cache, using this actual body. */
    public boolean cacheStand(Bunnidog dog,Vec3 feet) {
        if(!home.attached())return false;var pos=home.getBlockPos();
        var shape=home.getBlockState().setValue(HearthTreeBlock.CACHED,true).getCollisionShape(home.getLevel(),pos,CollisionContext.of(dog)).move(pos);
        return !Shapes.joinIsNotEmpty(shape,Shapes.create(dog.getBoundingBox().move(feet.subtract(dog.position()))),BooleanOp.AND);
    }
    private List<ItemEntity> stock() {
        return !live() ? List.of() : home.getLevel().getEntitiesOfClass(ItemEntity.class,bounds(),item->CivicFields.available(home,item)).stream()
                .sorted(Comparator.comparingInt(Entity::getId)).limit(16).toList();
    }
    private Optional<CivicRecipes.Plan> plan(CivicRecipes.RecipeId id,List<ItemEntity> sources) {
        if(id!=CivicRecipes.RecipeId.PLANKS && id!=CivicRecipes.RecipeId.CACHE)return Optional.empty();
        return CivicRecipes.prepare((ServerLevel)home.getLevel(),id,sources.stream().map(item->item.getItem().copy()).toList())
                .filter(p->CivicRecipes.ordinary(p.result()) && p.result().getCount()<=p.result().getMaxStackSize() && p.remainders().stream().allMatch(ItemStack::isEmpty));
    }
    public Optional<CivicRecipes.Plan> prepare(CivicRecipes.RecipeId id) {return live() ? plan(id,stock()) : Optional.empty();}
    public boolean needs(CivicRecipes.RecipeId id) {
        if(!live())return false;var ground=stock();
        boolean cache=ground.stream().anyMatch(item->item.getItem().is(ModItems.ROOT_CACHE.get()))
                || home.residents().stream().anyMatch(d->d.society().campCacheCarrier(home));
        if(id==CivicRecipes.RecipeId.CACHE)return !cache;
        if(id!=CivicRecipes.RecipeId.PLANKS)return false;
        // Enough loose planks do not imply that one of several beginners has personally learned assembly.
        // Further useful plank work still pays original logs; the resulting stock supplies the table/tools.
        return cache ? home.residents().stream().noneMatch(d->d.society().canInstallCache())
                : ground.stream().filter(item->item.getItem().is(ItemTags.PLANKS)).mapToInt(item->item.getItem().getCount()).sum()<3;
    }
    public boolean needsHaul(Bunnidog dog,ItemEntity item) {
        if(!live() || !CivicFields.available(home,item))return false;
        if(bounds().contains(item.position()))return item.getItem().is(ModItems.ROOT_CACHE.get()) && dog.society().canInstallCache();
        var stack=item.getItem();return stack.is(ModItems.GUAVA_LOG.get()) || stack.is(ItemTags.PLANKS) || GuavaItem.tierOf(stack)==0 && dog.canEat(stack)
                || stack.is(ModItems.ROOT_CACHE.get()) && dog.society().canInstallCache();
    }
    private ItemEntity meal(Bunnidog dog) {
        return stock().stream().filter(item->GuavaItem.tierOf(item.getItem())==0 && dog.canEat(item.getItem()))
                .sorted(Comparator.comparing(item->item.getItem().is(ModItems.GUAVA.get()))).findFirst().orElse(null);
    }
    public boolean hasMeal(Bunnidog dog) {return meal(dog)!=null;}
    private static final class Ground extends ItemStackResourceHandler {
        private final ItemEntity item;Ground(ItemEntity item) {this.item=item;}
        @Override protected ItemStack getStack() {return item.getItem();}
        @Override protected void setStack(ItemStack stack) {item.setItem(stack);}
    }
    public boolean eat(Bunnidog dog) {
        if(busy || !workspace(dog))return false;var level=(ServerLevel)dog.level();var owner=home.owner();busy=true;
        try {
            if(!EventHooks.canEntityGrief(level,dog) || !workspace(dog) || !Objects.equals(owner,home.owner()))return false;
            var food=meal(dog);if(food==null || !CivicFields.visible(dog,food))return false;var exact=food.getItem().copyWithCount(1);
            try(var tx=Transaction.openRoot()) {
                if(new Ground(food).extract(ItemResource.of(exact),1,tx)!=1 || !workspace(dog))return false;tx.commit();
            }
            if(food.getItem().isEmpty())food.discard();dog.heal(2);home.evidence("meal",1);return true;
        } finally {busy=false;}
    }
    public boolean craft(Bunnidog dog,CivicRecipes.RecipeId id) {
        if(busy || !workspace(dog) || !needs(id) || !dog.society().cargoEmpty())return false;var owner=home.owner();busy=true;
        try {
            var level=(ServerLevel)dog.level();
            if(!EventHooks.canEntityGrief(level,dog) || !workspace(dog) || !Objects.equals(owner,home.owner()) || !needs(id) || !dog.society().canCraft(id))return false;
            var sources=stock().stream().filter(item->CivicFields.visible(dog,item)).toList();var current=plan(id,sources).orElse(null);if(current==null)return false;
            var handlers=sources.stream().map(Ground::new).toList();
            try(var tx=Transaction.openRoot()) {
                for(var input:current.input().items())if(!input.isEmpty()) {
                    int left=1;for(var handler:handlers) {left-=handler.extract(ItemResource.of(input),left,tx);if(left==0)break;}if(left!=0)return false;
                }
                if(!workspace(dog) || !dog.society().canCraft(id) || !dog.society().takeCampProduct(home,current.result()))return false;tx.commit();
            }
            sources.stream().filter(item->item.getItem().isEmpty()).forEach(ItemEntity::discard);dog.workshopLore().completed(home,current);return true;
        } finally {busy=false;}
    }
    public boolean deposit(Bunnidog dog,ItemStack material,BooleanSupplier owned,Runnable pay,Runnable refund) {
        if(busy || !workspace(dog))return false;busy=true;
        try {
            var level=(ServerLevel)dog.level();var owner=home.owner();
            if(!EventHooks.canEntityGrief(level,dog) || !workspace(dog) || !Objects.equals(owner,home.owner()) || !owned.getAsBoolean())return false;
            var marked=material.copy();marked.set(ModItems.FARM_ORIGIN.get(),home.site().toString());
            var item=new ItemEntity(level,mat().getX()+.5,mat().getY()+.2,mat().getZ()+.5,marked);item.setDeltaMovement(Vec3.ZERO);item.setTarget(owner);item.setDefaultPickUpDelay();
            pay.run();boolean added=false;
            try {added=level.addFreshEntity(item);return added;}finally {if(!added)refund.run();}
        } finally {busy=false;}
    }
    private boolean installer(Entity actor) {
        if(actor instanceof Bunnidog dog)return workspace(dog) && dog.society().canInstallCache();
        return actor instanceof Player p && live() && p.mayBuild() && !p.isSpectator() && p.level()==home.getLevel()
                && p.distanceToSqr(Vec3.atCenterOf(home.getBlockPos()))<=36 && p.level().mayInteract(p,home.getBlockPos()) && sees(p,home.getBlockPos());
    }
    public boolean install(Entity actor,BooleanSupplier owned,Runnable pay,Runnable refund) {
        if(busy || !installer(actor))return false;busy=true;
        try {
            var level=(ServerLevel)home.getLevel();var owner=home.owner();var site=home.site();var state=home.getBlockState();var pos=home.getBlockPos();var next=state.setValue(HearthTreeBlock.CACHED,true);
            if(actor instanceof Bunnidog dog && (!EventHooks.canEntityGrief(level,dog) || !CommonHooks.canEntityDestroy(level,pos,dog))
                    || EventHooks.onBlockPlace(actor,BlockSnapshot.create(level.dimension(),level,pos),Direction.UP)
                    || !installer(actor) || !Objects.equals(owner,home.owner()) || !site.equals(home.site()) || !state.equals(home.getBlockState()) || !owned.getAsBoolean()
                    || !level.isUnobstructed(next,pos,CollisionContext.of(actor)))return false;
            pay.run();boolean placed=false;
            try {
                placed=level.setBlock(pos,next,3);
                if(placed) {level.invalidateCapabilities(pos);home.evidence("build",1);}return placed;
            } finally {if(!placed)refund.run();}
        } finally {busy=false;}
    }
}
