package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Founders;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.GeneticDiscoveries;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.item.PedigreeBookItem;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Actual held-book logging, hidden-origin collection, native one-time rewards and an archived book's adoption. */
public final class DiscoveryGameTest {
    private DiscoveryGameTest() {}
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("bunnidogs",path); }
    public static void run(GameTestHelper h) {
        var level=h.getLevel(); var player=h.makeMockServerPlayerInLevel();
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,1,2))));
        var book=new ItemStack(ModItems.PEDIGREE_BOOK.get()); player.setItemInHand(InteractionHand.MAIN_HAND,book);
        var library=level.getServer().getAdvancements().get(id("wild_gene_library"));
        var mutation=level.getServer().getAdvancements().get(id("nature_finds_a_way"));
        h.assertTrue(library!=null&&mutation!=null,"Both original-plan/native catalogue advancements must parse");
        Bunnidog last=null;
        for (var lineage:Lineage.values()) if(lineage!=Lineage.MEADOW) {
            var signature=Founders.recipe(lineage).stream().filter(s->s.resolve().isWild()).findFirst().orElseThrow();
            var genome=Genome.DEFAULT.with(signature.gene(),0,signature.gene().alleles().stream().filter(a->a.isRare()).findFirst().orElseThrow().id())
                    .with(signature.gene(),1,signature.resolve().id());
            var dog=h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),3,1,3); dog.setGenome(genome); dog.setAge(0);
            player.interactOn(dog,InteractionHand.MAIN_HAND,Vec3.ZERO); last=dog;
            int origins=GeneticDiscoveries.wildLineages(PedigreeBookItem.read(book)).size();
            h.assertTrue(origins>0&&!player.getAdvancements().getOrStartProgress(mutation).isDone(),
                    "Actual unparented snapshots record hidden wild origins without claiming a bred mutation");
            if(lineage!=Lineage.ISLAND) h.assertTrue(!player.getAdvancements().getOrStartProgress(library).isDone(),"The new island origin is required for the full library");
        }
        h.assertTrue(GeneticDiscoveries.wildLineages(PedigreeBookItem.read(book)).size()==8
                &&player.getAdvancements().getOrStartProgress(library).isDone()&&book.getCount()==1,
                "Actual reusable held-book logging includes all eight hidden wild origins");
        last.setParents(Genome.DEFAULT,Genome.DEFAULT); player.interactOn(last,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.getAdvancements().getOrStartProgress(mutation).isDone(),"A recorded born representative carrying a mutation-only allele earns Nature Finds a Way");
        int experience=player.totalExperience;
        ModItems.PEDIGREE_BOOK.get().use(level,player,InteractionHand.MAIN_HAND); player.interactOn(last,InteractionHand.MAIN_HAND,Vec3.ZERO);
        h.assertTrue(player.totalExperience==experience,"Re-reading or updating the same book cannot duplicate native advancement XP");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); out.store("Book",ItemStack.CODEC,book);
        var restored=TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),out.buildResult()).read("Book",ItemStack.CODEC).orElseThrow();
        var keeper=h.makeMockServerPlayerInLevel(); keeper.setItemInHand(InteractionHand.OFF_HAND,restored);
        String before=restored.get(ModItems.PEDIGREE_DATA.get()); ModItems.PEDIGREE_BOOK.get().use(level,keeper,InteractionHand.OFF_HAND);
        h.assertTrue(keeper.getAdvancements().getOrStartProgress(library).isDone()&&keeper.getAdvancements().getOrStartProgress(mutation).isDone()
                &&before.equals(restored.get(ModItems.PEDIGREE_DATA.get())),"A saved book transfers readable knowledge without rewriting snapshots, allele order or formats");
    }
}
