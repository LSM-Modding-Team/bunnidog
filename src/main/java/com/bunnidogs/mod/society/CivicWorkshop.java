package com.bunnidogs.mod.society;

import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;

/** Explicit native furnace identities and finite practical processes. No remote transfer or simulated cooking. */
public final class CivicWorkshop {
    private static final String TOKEN = "BunnidogWorkshopToken", SITE = "BunnidogWorkshopSite";
    private record Link(BlockPos at, String token) {
        private static final Codec<Link> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("at").forGetter(Link::at), Codec.STRING.fieldOf("token").forGetter(Link::token)).apply(i, Link::new));
    }
    public record Supply(int sourceSlot, int furnaceSlot, ItemStack stack, String token) {}
    private final HearthTreeBlockEntity home;
    private final Map<BlockPos, String> links = new HashMap<>();
    public CivicWorkshop(HearthTreeBlockEntity home) { this.home = home; }

    public boolean link(BlockPos at) {
        if (!home.attached() || at.distSqr(home.getBlockPos()) > 24 * 24 || !home.getLevel().hasChunkAt(at)
                || !home.getLevel().getWorldBorder().isWithinBounds(at)
                || !(home.getLevel().getBlockEntity(at) instanceof AbstractFurnaceBlockEntity furnace)
                || type(furnace) == null || furnace.isLocked()) return false;
        var data = furnace.getPersistentData();
        String site = data.getStringOr(SITE, "");
        if (!site.isEmpty() && !site.equals(home.site().toString()) || !links.containsKey(at) && links.size() >= 12) return false;
        String token = data.getStringOr(TOKEN, "");
        if (token.isEmpty()) { token = UUID.randomUUID().toString(); data.putString(TOKEN, token); }
        data.putString(SITE, home.site().toString()); furnace.setChanged();
        links.put(at.immutable(), token); home.setChanged(); return true;
    }

    /** Release the original container without touching its native contents, cooking or XP. */
    void unlink(BlockPos at) {
        var furnace = resolve(at, token(at));
        if (furnace != null) {
            var data = furnace.getPersistentData();
            data.remove(TOKEN); data.remove(SITE); furnace.setChanged();
        }
        links.remove(at);
    }

    /** Identity only: a powered/locked but surviving furnace still owns its in-flight parcels. */
    public AbstractFurnaceBlockEntity resolve(BlockPos at, String token) {
        if (!home.attached() || at == null || token == null || !token.equals(links.get(at))
                || !home.sites().contains(at) || !home.getLevel().hasChunkAt(at)
                || !(home.getLevel().getBlockEntity(at) instanceof AbstractFurnaceBlockEntity furnace) || type(furnace) == null) return null;
        var data = furnace.getPersistentData();
        return token.equals(data.getStringOr(TOKEN, "")) && home.site().toString().equals(data.getStringOr(SITE, "")) ? furnace : null;
    }
    public String token(BlockPos at) { return links.get(at); }
    public AbstractFurnaceBlockEntity available(BlockPos at) {
        var furnace = resolve(at, token(at));
        return furnace != null && !furnace.isLocked() && !home.isLocked() && home.getLevel().getWorldBorder().isWithinBounds(at)
                && home.getLevel().hasChunksAt(at.offset(-1,-1,-1),at.offset(1,1,1))
                && home.getLevel().hasChunksAt(home.getBlockPos().offset(-1,-1,-1),home.getBlockPos().offset(1,1,1))
                && !home.getLevel().hasNeighborSignal(at) && !home.getLevel().hasNeighborSignal(home.getBlockPos()) ? furnace : null;
    }

    private static RecipeType<? extends AbstractCookingRecipe> type(AbstractFurnaceBlockEntity furnace) {
        return furnace instanceof FurnaceBlockEntity ? RecipeType.SMELTING
                : furnace instanceof SmokerBlockEntity ? RecipeType.SMOKING
                : furnace instanceof BlastFurnaceBlockEntity ? RecipeType.BLASTING : null;
    }
    public ItemStack result(AbstractFurnaceBlockEntity furnace, ItemStack input) {
        if (input.isEmpty() || !(home.getLevel() instanceof ServerLevel level) || type(furnace) == null) return ItemStack.EMPTY;
        var recipeInput = new SingleRecipeInput(input);
        return level.recipeAccess().getRecipeFor(type(furnace), recipeInput, level)
                .map(recipe -> recipe.value().assemble(recipeInput)).orElse(ItemStack.EMPTY);
    }
    private boolean outputRoom(AbstractFurnaceBlockEntity furnace, ItemStack result) {
        var output = furnace.getItem(2);
        return !result.isEmpty() && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= Math.min(result.getMaxStackSize(), furnace.getMaxStackSize()));
    }
    private boolean candidate(ItemStack input, boolean advanced) {
        if (!CivicRecipes.ordinary(input)) return false;
        return input.is(ItemTags.LOGS_THAT_BURN) || advanced && (input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER)
                || input.is(Items.RAW_GOLD) || input.is(ModItems.RAW_CINDERITE.get()) || input.is(Items.SAND)
                || input.is(Items.RED_SAND) || input.has(DataComponents.FOOD));
    }
    private boolean wanted(ItemStack input, ItemStack result, boolean advanced) {
        if (result.isEmpty() || !CivicRecipes.ordinary(input)) return false;
        int reserve;
        if (input.is(ItemTags.LOGS_THAT_BURN)) {
            if (!result.is(Items.CHARCOAL) || CivicRecipes.count(home, Items.CHARCOAL) >= 16) return false;
            reserve = 4;
        } else {
            if (!advanced) return false;
            boolean mineral = input.is(Items.RAW_IRON) || input.is(Items.RAW_COPPER) || input.is(Items.RAW_GOLD) || input.is(ModItems.RAW_CINDERITE.get());
            boolean glass = input.is(Items.SAND) || input.is(Items.RED_SAND);
            boolean food = input.has(DataComponents.FOOD) && result.has(DataComponents.FOOD);
            if (!mineral && !glass && !food || CivicRecipes.count(home, result.getItem()) >= (mineral ? 32 : 16)) return false;
            reserve = input.is(Items.POTATO) ? 4 : food ? 2 : 0;
        }
        return CivicRecipes.count(home, input.getItem()) > reserve;
    }
    private boolean fuel(AbstractFurnaceBlockEntity furnace, ItemStack stack) {
        return CivicRecipes.ordinary(stack) && (stack.is(Items.COAL) || stack.is(Items.CHARCOAL) || stack.is(ItemTags.PLANKS) || stack.is(Items.STICK))
                && stack.getBurnTime(type(furnace), home.getLevel().fuelValues()) > 0;
    }
    private static int reserve(ItemStack input, int slot) {
        return slot == 1 ? input.is(ItemTags.PLANKS) ? 8 : input.is(Items.STICK) ? 2 : 0
                : input.is(ItemTags.LOGS_THAT_BURN) ? 4 : input.is(Items.POTATO) ? 4 : input.has(DataComponents.FOOD) ? 2 : 0;
    }
    public Supply supply(BlockPos at, int slot, int limit, boolean advanced) {
        var furnace = available(at);
        if (furnace == null || slot < 0 || slot > 1) return null;
        if (slot == 0 && !furnace.getItem(0).isEmpty()) return null;
        if (slot == 1 && (furnace.getItem(0).isEmpty() || !advanced && !furnace.getItem(0).is(ItemTags.LOGS_THAT_BURN)
                || !outputRoom(furnace, result(furnace, furnace.getItem(0))) || furnace.getItem(1).getCount() >= 2)) return null;
        for (int source = 0; source < home.getContainerSize(); source++) {
            var stack = home.getItem(source);
            if (slot == 0 && !candidate(stack, advanced)) continue;
            if (slot == 0 ? !wanted(stack, result(furnace, stack), advanced) : !fuel(furnace, stack)) continue;
            int count = Math.min(Math.max(1, limit), Math.min(stack.getCount(), CivicRecipes.count(home, stack.getItem()) - reserve(stack, slot)));
            count = Math.min(count, slot == 1 ? 2 - furnace.getItem(1).getCount() : 8);
            if (count <= 0 || !canLoad(at, token(at), slot, stack.copyWithCount(count), advanced)) continue;
            return new Supply(source, slot, stack.copyWithCount(count), token(at));
        }
        return null;
    }
    public boolean canLoad(BlockPos at, String token, int slot, ItemStack stack, boolean advanced) {
        var furnace = available(at);
        if (furnace == null || !Objects.equals(token, token(at)) || slot < 0 || slot > 1 || stack.isEmpty()
                || !CivicRecipes.ordinary(stack) || !furnace.canPlaceItem(slot, stack)) return false;
        var current = furnace.getItem(slot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)
                || current.getCount() + stack.getCount() > Math.min(stack.getMaxStackSize(), furnace.getMaxStackSize())) return false;
        if (slot == 0) return (advanced || stack.is(ItemTags.LOGS_THAT_BURN)) && outputRoom(furnace, result(furnace, stack));
        return fuel(furnace, stack) && !furnace.getItem(0).isEmpty()
                && (advanced || furnace.getItem(0).is(ItemTags.LOGS_THAT_BURN)) && outputRoom(furnace, result(furnace, furnace.getItem(0)));
    }
    public ItemStack output(BlockPos at, int slot, int limit) {
        var furnace = available(at);
        if (furnace == null || slot != 2 && slot != 1) return ItemStack.EMPTY;
        var stack = furnace.getItem(slot);
        if (stack.isEmpty() || slot == 1 && stack.getBurnTime(type(furnace), home.getLevel().fuelValues()) > 0) return ItemStack.EMPTY;
        return stack.copyWithCount(Math.min(stack.getCount(), Math.max(1, limit)));
    }
    public void save(ValueOutput out) {
        out.store("FurnaceLinks", Link.CODEC.listOf(), links.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new Link(entry.getKey(), entry.getValue())).toList());
    }
    public void load(ValueInput in) {
        links.clear();
        in.read("FurnaceLinks", Link.CODEC.listOf()).orElse(List.of()).stream().limit(12)
                .filter(link -> !link.token().isEmpty() && link.at().distSqr(home.getBlockPos()) <= 24 * 24)
                .forEach(link -> links.put(link.at(), link.token()));
    }
}
