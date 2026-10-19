package com.bunnidogs.mod.entity;

import java.util.Optional;
import java.util.UUID;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.LineageEditing;
import com.bunnidogs.mod.item.PedigreeBookItem;
import com.bunnidogs.mod.item.PurebredRegistryItem;
import com.bunnidogs.mod.network.LineageEditPayload;
import com.bunnidogs.mod.registry.ModItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public final class LineageEditingGameTest {
    private LineageEditingGameTest() {}
    public static void run(GameTestHelper h) {
        var player = h.makeMockServerPlayerInLevel();
        for (boolean registry : new boolean[]{false,true}) {
            var log = new Pedigree();
            var entry = new Pedigree.Entry(Genome.DEFAULT,Optional.of(Genome.DEFAULT),Optional.of(Genome.DEFAULT),
                    Optional.of(UUID.randomUUID()),Optional.empty(),Optional.of(Stats.of(1,2,3,4,5)),false);
            log.add(entry);
            var book = new ItemStack(registry ? ModItems.PUREBRED_REGISTRY.get() : ModItems.PEDIGREE_BOOK.get());
            book.set(ModItems.PEDIGREE_DATA.get(),log.toString());
            var hand = registry ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            int handId = registry ? 1 : 0;
            player.setItemInHand(hand,book);
            var request = new LineageEditPayload(handId,log.toString(),"",Genome.DEFAULT.toString(),"  Moss; #1 🐇  ");
            var wire = Unpooled.buffer();
            try {
                LineageEditPayload.STREAM_CODEC.encode(wire,request);
                h.assertTrue(LineageEditPayload.STREAM_CODEC.decode(wire).equals(request),"Bounded native packet codec preserves Unicode keeper text and exact preconditions");
            } finally { wire.release(); }
            var result = LineageEditing.apply(player,request);
            h.assertTrue(result.saved(),"A held recorded genotype can receive a keeper name");
            var read = registry ? PurebredRegistryItem.read(book) : PedigreeBookItem.read(book);
            h.assertTrue(read.entries().equals(log.entries()) && read.lineageName(Genome.DEFAULT).orElseThrow().equals("Moss; #1 🐇")
                    && book.getCount() == 1,"Naming keeps the entire original snapshot, identity, parents, stats and reusable book");
            h.assertTrue(!LineageEditing.apply(player,request).saved(),"A stale pre-edit component must not overwrite a newer name");
            String names = result.names();
            h.assertTrue(!LineageEditing.apply(player,new LineageEditPayload(handId,log.toString(),names,Genome.DEFAULT.toString(),"bad\nname")).saved()
                    && !LineageEditing.apply(player,new LineageEditPayload(handId,log.toString(),names,Genome.DEFAULT.toString(),"x".repeat(41))).saved(),
                    "Controls and overlong labels refuse without changing the component");
            h.assertTrue(!LineageEditing.apply(player,new LineageEditPayload(handId,log.toString(),names,Genome.DEFAULT.with(Gene.COAT,0,1).toString(),"Forged")).saved(),
                    "A client cannot introduce a name for a genotype absent from the actual book");
            h.assertTrue(RecordFilter.indices(read,"line:moss vitality<5").equals(java.util.List.of(0))
                    && RecordFilter.indices(read,"moss vitality>=100").isEmpty(),"Names join local queries without bypassing numeric record filters");
            if (!registry) {
                PedigreeBookItem.log(player,book,new Pedigree.Entry(entry.genome(),entry.mother(),entry.father(),entry.id(),entry.tag(),Optional.of(Stats.ZERO),false));
                h.assertTrue(PedigreeBookItem.read(book).lineageName(Genome.DEFAULT).orElseThrow().equals("Moss; #1 🐇"),"Refreshing an individual retains its genotype's keeper name");
            }
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess()); output.store("Book",ItemStack.CODEC,book);
            var loaded = TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),output.buildResult()).read("Book",ItemStack.CODEC).orElseThrow();
            h.assertTrue(loaded.get(ModItems.LINEAGE_NAMES.get()).equals(names),"Names round-trip in the native saved item independently of legacy pedigree text");
            String current = book.get(ModItems.PEDIGREE_DATA.get());
            player.setItemInHand(hand,new ItemStack(Items.BOOK));
            h.assertTrue(!LineageEditing.apply(player,new LineageEditPayload(handId,current,names,Genome.DEFAULT.toString(),"Wrong hand")).saved(),"Changing the held item invalidates a pending editor");
            player.setItemInHand(hand,book);
            var cleared = LineageEditing.apply(player,new LineageEditPayload(handId,current,names,Genome.DEFAULT.toString(),""));
            h.assertTrue(cleared.saved() && !book.has(ModItems.LINEAGE_NAMES.get()),"Clearing a keeper name removes only the optional names component");
        }
        h.assertTrue(LineageNames.parse("garbage;"+Genome.DEFAULT+"~%%%%").toString().isEmpty()
                && LineageNames.clean("é".repeat(40)).isPresent() && LineageNames.clean("§4red").isEmpty(),"Malformed legacy additions and formatting cannot poison book parsing");
        var guide = new ItemStack(ModItems.FIELD_GUIDE.get());
        var content = guide.get(DataComponents.WRITTEN_BOOK_CONTENT);
        h.assertTrue(content != null && content.pages().size() == 116,"Fresh native guides include venue chapters past page 100 without renumbering old pages");
        // Change the title so the item codec must serialize the pages rather than using the preset default component.
        guide.set(DataComponents.WRITTEN_BOOK_CONTENT,new net.minecraft.world.item.component.WrittenBookContent(
                net.minecraft.server.network.Filterable.passThrough("Archive copy"),content.author(),content.generation(),content.pages(),content.resolved()));
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess()); out.store("Guide",ItemStack.CODEC,guide);
        var loadedGuide = TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()).read("Guide",ItemStack.CODEC).orElseThrow();
        h.assertTrue(loadedGuide.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size() == 116,"A saved customized guide serializes every page through the native written-book item codec");
    }
}
