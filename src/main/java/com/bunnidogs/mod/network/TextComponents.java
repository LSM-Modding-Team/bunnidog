package com.bunnidogs.mod.network;

import com.bunnidogs.mod.genetics.*;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

/** Keep server messages translatable for each recipient; scope pure layouts to the current client language. */
public final class TextComponents {
    private TextComponents() {}
    public static MutableComponent t(String english,Object... args) {
        return Component.translatableWithFallback(Texts.key(english),english,args);
    }
    public static <T> T localized(Supplier<T> work) { return Texts.using(Language.getInstance()::getOrDefault,work); }
    public static Component gene(Gene gene) { return t(gene.displayName()); }
    public static Component stat(Stat stat) { return t(stat==Stat.SWIFTNESS ? "Speed" : stat.displayName()); }
    public static Component allele(Allele allele) { return t(allele.name().replace('_',' ')); }
    public static Component label(Allele allele) {
        return allele.isWild() ? t("%s [%s]",allele(allele),t(allele.lineage().key())) : allele(allele);
    }
    public static Component diet(Diet diet) { return t(diet.foods()); }
    private static Component phrase(Gene gene,Allele allele) {
        return switch (gene) {
            case EARS -> t("%s ears",allele(allele)); case COAT -> t("%s coat",allele(allele));
            case LEGS -> t("%s legs",allele(allele)); case BUILD -> t("%s build",allele(allele));
            case FERTILITY -> t("%s fertility",allele(allele)); case DIET -> t("%s diet",allele(allele));
            case TEMPERAMENT, WINGS -> allele(allele);
        };
    }
    public static MutableComponent look(Genome genome) {
        var parts=new ArrayList<Component>(); parts.add(allele(genome.expressed(Gene.COAT)));
        for (var gene:Gene.values()) if (gene!=Gene.COAT && gene!=Gene.TEMPERAMENT && genome.expressed(gene).id()!=0)
            parts.add(phrase(gene,genome.expressed(gene)));
        parts.add(allele(genome.expressed(Gene.TEMPERAMENT)));
        return ComponentUtils.formatList(parts,Component.literal(", ")).copy();
    }
}
