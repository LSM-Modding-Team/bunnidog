package com.bunnidogs.mod.society;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.registry.ModItems;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.google.gson.JsonParser;
import java.util.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.WrittenBookContent;

/** A paid resident-authored recipe procedure, bound to its original household; never stored skill or stock. */
public record CraftNotes(UUID hearth,UUID owner,UUID author,CivicRecipes.RecipeId recipe,List<ItemStack> ingredients,ItemStack product) {
    public CraftNotes {ingredients=ingredients.stream().map(ItemStack::copy).toList();product=product.copy();}
    @Override public List<ItemStack> ingredients() {return ingredients.stream().map(ItemStack::copy).toList();}
    @Override public ItemStack product() {return product.copy();}
    private static final Codec<CivicRecipes.RecipeId> RECIPE=Codec.STRING.comapFlatMap(value->{
        try {return DataResult.success(CivicRecipes.RecipeId.valueOf(value));}
        catch(IllegalArgumentException ignored) {return DataResult.error(()->"Unknown civic recipe");}
    },Enum::name);
    public static final Codec<CraftNotes> CODEC=RecordCodecBuilder.<CraftNotes>create(i->i.group(
            Codec.intRange(1,1).fieldOf("version").forGetter(n->1),
            UUIDUtil.STRING_CODEC.fieldOf("hearth").forGetter(CraftNotes::hearth),
            UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(CraftNotes::owner),
            UUIDUtil.STRING_CODEC.fieldOf("author").forGetter(CraftNotes::author),
            RECIPE.fieldOf("recipe").forGetter(CraftNotes::recipe),
            ItemStack.CODEC.listOf().fieldOf("ingredients").forGetter(CraftNotes::ingredients),
            ItemStack.CODEC.fieldOf("product").forGetter(CraftNotes::product)).apply(i,(version,hearth,owner,author,recipe,ingredients,product)->new CraftNotes(hearth,owner,author,recipe,ingredients,product)))
            .validate(n->!n.ingredients.isEmpty() && n.ingredients.size()<=9 && n.ingredients.stream().allMatch(s->s.getCount()==1 && CivicRecipes.ordinary(s))
                    && CivicRecipes.ordinary(n.product) && n.product.getCount()<=1024 ? DataResult.success(n) : DataResult.error(()->"Not a bounded ordinary crafting example"));
    private record Key(HolderLookup.Provider registries,String data) {}
    private static final Map<Key,Optional<CraftNotes>> CACHE=new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<Key,Optional<CraftNotes>> entry) {return size()>64;}
    };
    public boolean available(HearthTreeBlockEntity home) {return home!=null && home.attached() && hearth.equals(home.site()) && owner.equals(home.owner());}
    public static Optional<CraftNotes> read(ItemStack book,ServerLevel level) {
        return !book.is(ModItems.CRAFT_NOTES.get()) ? Optional.empty() : readData(book.get(ModItems.CRAFT_NOTES_DATA.get()),level);
    }
    public static Optional<CraftNotes> readData(String data,ServerLevel level) {
        if(data==null || data.length()>4096)return Optional.empty();var key=new Key(level.registryAccess(),data);
        synchronized(CACHE) {
            return CACHE.computeIfAbsent(key,value->{try {return CODEC.parse(net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE,level.registryAccess()),JsonParser.parseString(data)).result();}catch(RuntimeException ignored){return Optional.empty();}});
        }
    }
    public Optional<String> data(ServerLevel level) {
        return CODEC.encodeStart(net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE,level.registryAccess()),this).result().map(Object::toString).filter(s->s.length()<=4096);
    }
    public static CraftNotes example(HearthTreeBlockEntity home,Bunnidog writer,CivicRecipes.Plan plan) {
        return new CraftNotes(home.site(),home.owner(),writer.getUUID(),plan.id(),plan.input().items().stream().filter(s->!s.isEmpty()).map(s->new ItemStack(s.getItem())).toList(),new ItemStack(plan.result().getItem(),plan.result().getCount()));
    }
    public ItemStack write(ItemStack blank,Bunnidog writer) {
        if(!(writer.level() instanceof ServerLevel level) || !RouteNotes.blank(blank))return ItemStack.EMPTY;
        var encoded=data(level);if(encoded.isEmpty())return ItemStack.EMPTY;
        var result=blank.transmuteCopy(ModItems.CRAFT_NOTES.get(),1);result.remove(DataComponents.WRITABLE_BOOK_CONTENT);result.set(ModItems.CRAFT_NOTES_DATA.get(),encoded.get());
        var name=product.getHoverName();result.set(DataComponents.ITEM_NAME,Component.translatable("item.bunnidogs.craft_notes.named",name));
        var counts=new LinkedHashMap<Item,Integer>();for(var item:ingredients)counts.merge(item.getItem(),1,Integer::sum);var costs=Component.empty();
        for(var entry:counts.entrySet()) {if(!costs.getSiblings().isEmpty())costs.append("\n");costs.append(Component.translatable("book.bunnidogs.craft_notes.quantity",entry.getValue(),new ItemStack(entry.getKey()).getHoverName()));}
        var page=Component.translatable("book.bunnidogs.craft_notes.recipe",product.getCount(),name,costs);
        result.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("Craft"),writer.getName().getString(),0,List.of(Filterable.passThrough(page)),true));return result;
    }
}
