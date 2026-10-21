package com.bunnidogs.mod.item;

import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.LineageNames;
import com.bunnidogs.mod.network.LineageEditPayload;
import com.bunnidogs.mod.network.LineageResultPayload;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/** Server authority for editable keeper labels, with no client-provided pedigree or item writes. */
public final class LineageEditing {
    private LineageEditing() {}
    public static LineageResultPayload apply(Player player, LineageEditPayload request) {
        if (request.hand() < 0 || request.hand() > 1) return refused(request);
        var stack = player.getItemInHand(request.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        boolean registry = stack.is(ModItems.PUREBRED_REGISTRY.get());
        if (!registry && !stack.is(ModItems.PEDIGREE_BOOK.get())) return refused(request);
        String records = stack.getOrDefault(ModItems.PEDIGREE_DATA.get(),"");
        String previous = stack.getOrDefault(ModItems.LINEAGE_NAMES.get(),"");
        if (Genome.containsUnsupportedSchema(records) || Genome.containsUnsupportedSchema(previous)) return refused(request);
        if (!records.equals(request.records()) || !previous.equals(request.previous())) return refused(request);
        var genome = Genome.parse(request.genome());
        var book = registry ? PurebredRegistryItem.read(stack) : PedigreeBookItem.read(stack);
        if (genome.isEmpty() || book.entries().stream().noneMatch(e -> e.genome().equals(genome.get()))) return refused(request);
        var names = LineageNames.parse(previous).retain(book);
        if (!names.rename(genome.get(),request.name())) return refused(request);
        String result = names.toString();
        if (result.isEmpty()) stack.remove(ModItems.LINEAGE_NAMES.get()); else stack.set(ModItems.LINEAGE_NAMES.get(),result);
        return new LineageResultPayload(records,previous,result,true);
    }
    private static LineageResultPayload refused(LineageEditPayload request) {
        return new LineageResultPayload(request.records(),request.previous(),"",false);
    }
}
