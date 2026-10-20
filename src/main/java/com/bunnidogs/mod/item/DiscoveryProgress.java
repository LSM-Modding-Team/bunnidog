package com.bunnidogs.mod.item;

import com.bunnidogs.mod.genetics.GeneticDiscoveries;
import com.bunnidogs.mod.genetics.Pedigree;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Native one-time rewards for an ordinary saved book; sharing or re-reading knowledge never duplicates XP. */
final class DiscoveryProgress {
    private DiscoveryProgress() {}
    static void observe(Player player,Pedigree book) {
        if (!(player instanceof ServerPlayer server)) return;
        if (GeneticDiscoveries.hasBredMutation(book)) award(server,"nature_finds_a_way","observed");
        if (GeneticDiscoveries.wildLineages(book).size()==GeneticDiscoveries.wildLineageCount()) award(server,"wild_gene_library","catalogued");
    }
    private static void award(ServerPlayer player,String path,String criterion) {
        var advancement=player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs",path));
        if (advancement!=null) player.getAdvancements().award(advancement,criterion);
    }
}
