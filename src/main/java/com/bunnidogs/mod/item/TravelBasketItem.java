package com.bunnidogs.mod.item;

import java.util.Optional;
import java.util.function.Consumer;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** One existing animal in native bucket data. Failure keeps both the basket and its occupant intact. */
public final class TravelBasketItem extends Item {
    private static final String VERSION = "BunnidogsBasket";
    public TravelBasketItem(Properties props) { super(props); }
    public static boolean occupied(ItemStack stack) {
        return !stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA,CustomData.EMPTY).isEmpty();
    }
    public static Optional<Tissue> readLine(ItemStack stack) {
        if (!stack.is(ModItems.TRAVEL_BASKET.get()) || !occupied(stack)) return Optional.empty();
        return line(stack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag());
    }
    public static Optional<Pedigree.Entry> readEntry(ItemStack stack) {
        if (!stack.is(ModItems.TRAVEL_BASKET.get()) || !occupied(stack)) return Optional.empty();
        var saved = stack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag();
        var identity = saved.read(Entity.TAG_UUID,UUIDUtil.CODEC);
        if (identity.isEmpty()) return Optional.empty();
        return line(saved).map(tissue -> new Pedigree.Entry(tissue.genome(),Genome.parse(saved.getStringOr("Mother","")),
                Genome.parse(saved.getStringOr("Father","")),identity,
                Optional.of(saved.getStringOr("EarTag","")).filter(tag -> !tag.isEmpty()),Optional.of(tissue.stats()),saved.getIntOr("Age",0) < 0));
    }
    /** Shared deliberate record gesture; callbacks run only on the server and never unpack the animal. */
    public static boolean recordCarried(Player player, InteractionHand hand, Consumer<Pedigree.Entry> recorder) {
        if (!player.isShiftKeyDown()) return false;
        var other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!other.is(ModItems.TRAVEL_BASKET.get()) || !occupied(other)) return false;
        if (!player.level().isClientSide()) {
            var entry = readEntry(other);
            if (entry.isPresent()) recorder.accept(entry.get());
            else message(player,"invalid");
        }
        return true;
    }
    private static Optional<Tissue> line(CompoundTag tag) {
        if (tag.getIntOr(VERSION,0) != 1) return Optional.empty();
        return Genome.parse(tag.getStringOr("Genome","")).flatMap(genome ->
                Stats.parse(tag.getStringOr("Stats","")).map(stats -> new Tissue(genome,stats)));
    }
    @Override public boolean isFoil(ItemStack stack) { return occupied(stack) || super.isFoil(stack); }
    @Override public Component getName(ItemStack stack) {
        return occupied(stack) ? Component.translatable("item.bunnidogs.travel_basket.filled") : super.getName(stack);
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Bunnidog dog)) return InteractionResult.PASS;
        if (!(dog.level() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        ItemStack actual = player.getItemInHand(hand); // Creative native interaction may pass a temporary copy.
        if (!player.mayBuild() || actual.getCount() != 1) return message(player,"permission");
        if (occupied(actual)) return message(player,"full");
        if (!dog.isAlive() || !dog.acceptsCare() || dog.getType() != ModEntities.BUNNIDOG.get()) return message(player,"ordinary");
        if (dog.beingTransported() || dog.isVehicle()
                || !Leashable.leashableInArea(dog,leashed -> leashed.getLeashHolder() == dog).isEmpty()) return message(player,"transported");
        if (dog.getTarget() != null || dog.seekingRefuge()
                || dog.getLastHurtByMob() != null && dog.tickCount-dog.getLastHurtByMobTimestamp() < 200) return message(player,"danger");
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,server.registryAccess());
        dog.saveWithoutId(output);
        var saved = output.buildResult();
        saved.putInt(VERSION,1);
        saved.putString("BasketName",dog.getName().getString());
        saved.remove("NestX"); saved.remove("NestY"); saved.remove("NestZ");
        saved.remove(Entity.TAG_POS); saved.remove("Motion");
        actual.set(DataComponents.BUCKET_ENTITY_DATA,CustomData.of(saved));
        dog.releaseHomeForTravel(server);
        dog.discard();
        player.playSound(SoundEvents.BUNDLE_INSERT,.6F,1);
        return message(player,"captured");
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        ItemStack basket = context.getItemInHand();
        if (!occupied(basket)) return InteractionResult.PASS;
        var player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        if (!(context.getLevel() instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var spot = context.getClickedPos().relative(context.getClickedFace());
        if (!player.mayBuild() || basket.getCount() != 1 || !player.mayUseItemAt(spot,context.getClickedFace(),basket)) return message(player,"permission");
        if (!server.hasChunkAt(spot) || !server.getBlockState(spot.below()).isFaceSturdy(server,spot.below(),Direction.UP)) return message(player,"blocked");
        var saved = basket.get(DataComponents.BUCKET_ENTITY_DATA).copyTag();
        var identity = saved.read(Entity.TAG_UUID,UUIDUtil.CODEC);
        if (line(saved).isEmpty() || identity.isEmpty()) return message(player,"invalid");
        if (server.getEntityInAnyDimension(identity.get()) != null) return message(player,"duplicate");
        Bunnidog dog = ModEntities.BUNNIDOG.get().create(server,EntitySpawnReason.BUCKET);
        if (dog == null) return message(player,"blocked");
        saved.store(Entity.TAG_POS,Vec3.CODEC,Vec3.atBottomCenterOf(spot));
        saved.remove("NestX"); saved.remove("NestY"); saved.remove("NestZ");
        try {
            dog.load(TagValueInput.create(ProblemReporter.DISCARDING,server.registryAccess(),saved));
        } catch (RuntimeException invalid) { return message(player,"invalid"); }
        dog.setDeltaMovement(Vec3.ZERO); dog.resetFallDistance();
        var box = dog.getBoundingBox();
        if (!dog.isAlive() || !dog.getUUID().equals(identity.get())) return message(player,"invalid");
        if (!server.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))
                || !server.noCollision(dog,box,true) || !server.addFreshEntity(dog)) return message(player,"blocked");
        basket.remove(DataComponents.BUCKET_ENTITY_DATA);
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE,.6F,1);
        return message(player,"released");
    }
    private static InteractionResult message(Player player, String key) {
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.basket."+key));
        return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack,context,display,lines,flag);
        if (occupied(stack)) {
            var saved = stack.get(DataComponents.BUCKET_ENTITY_DATA).copyTag();
            lines.accept(Component.translatable("tooltip.bunnidogs.basket.occupant",saved.getStringOr("BasketName","Bunnidog")).withStyle(ChatFormatting.GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.basket.release").withStyle(ChatFormatting.GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.basket.calipers").withStyle(ChatFormatting.DARK_GRAY));
            lines.accept(Component.translatable("tooltip.bunnidogs.basket.records").withStyle(ChatFormatting.DARK_GRAY));
        } else lines.accept(Component.translatable("tooltip.bunnidogs.basket.capture").withStyle(ChatFormatting.GRAY));
    }
}
