package com.bunnidogs.mod.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.bunnidogs.mod.block.GeneticsTableBlockEntity;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.CommissionCandidate;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Read-only handoff from sampled parents to their actual living family and the existing Board targets. */
public final class FamilyPanels {
    private FamilyPanels() {}
    public static final int COMPARE=180, CHOOSE_GOAL=181, BACK_FIT=182, FAMILY=183, PREV=184, NEXT=185, SHOW_FAMILY=186;
    public static final int SEED_FAMILY=187;
    public static final int SELECT=190, SELECT_GOAL=200, PAGE_SIZE=4, RANGE=12;
    private static Component t(String key,Object... args) { return Component.translatable("care.bunnidogs."+key,args); }
    private static Component title(Commissions.Request goal) { return Component.translatable("commission.bunnidogs."+goal.key()+".title"); }
    private static CareView.Control button(int id,String key,boolean enabled) { return new CareView.Control(id,t("button."+key),enabled); }
    public static boolean readOnly(int id) {
        return id>=COMPARE && id<=SEED_FAMILY || id>=SELECT && id<SELECT+PAGE_SIZE
                || id>=SELECT_GOAL && id<SELECT_GOAL+Commissions.Request.values().length;
    }
    public static boolean hasCalipers(Player player) {
        for (int i=0;i<player.getInventory().getContainerSize();i++) if (player.getInventory().getItem(i).is(ModItems.CALIPERS.get())) return true;
        return false;
    }
    public static Component growth(Bunnidog dog) {
        if (dog.isAgeLocked()) return t("family.growth_locked");
        int seconds=Growth.secondsRemaining(dog.getAge(),dog.stats().growthMultiplier());
        return t("family.growth",seconds/60,seconds%60);
    }
    public static CareView choices(Player player,Bunnidog dog) {
        var controls=new ArrayList<CareView.Control>();
        for (var goal:Commissions.Request.values()) controls.add(new CareView.Control(SELECT_GOAL+goal.ordinal(),title(goal),hasCalipers(player)));
        controls.add(button(BACK_FIT,"back",true));
        return new CareView(t("family.fit_title",dog.getDisplayName()),t("family.choose"),List.of(t("family.measure")),controls);
    }
    public static CareView fit(Player player,Bunnidog dog,Commissions.Request goal) {
        if (!hasCalipers(player)) return new CareView(t("family.fit_title",dog.getDisplayName()),t("family.need_calipers"),List.of(),
                List.of(button(CarePanels.BACK,"back",true)));
        var rows=new ArrayList<Component>(); var checks=goal.checks(dog.genome(),dog.stats(),dog.carriedBundleCount());
        rows.add(t("board.request",title(goal)));
        rows.add(t(dog.isBaby() ? dog.isAgeLocked() ? "family.pup_locked" : "family.pup" : "family.adult",dog.isBaby() ? growth(dog) : Component.empty()));
        rows.add(t("animal.health",Math.round(dog.getHealth()),Math.round(dog.getMaxHealth())));
        rows.add(t("family.parents",t(dog.motherGenome().isPresent() && dog.fatherGenome().isPresent() ? "show.recorded" : "show.missing")));
        for (var check:checks) rows.add(t("board.check",t("check."+check.key(),check.detail().replace('_',' ')),check.current(),check.required())
                .copy().withStyle(check.met() ? ChatFormatting.GREEN : ChatFormatting.RED));
        rows.add(t("family.inherited")); rows.add(t("family.at_board"));
        var failure=checks.stream().filter(c->!c.met()).map(Commissions.Check::failure).findFirst().orElse(Commissions.Status.READY);
        var condition=CommissionCandidate.condition(dog);
        String next=failure==Commissions.Status.GENES ? "genes" : failure==Commissions.Status.STATS ? "stats"
                : dog.motherGenome().isEmpty() || dog.fatherGenome().isEmpty() ? "parents"
                : condition==Commissions.Status.ADULT && dog.isAgeLocked() ? "age_locked"
                : condition!=Commissions.Status.READY ? condition.name().toLowerCase(Locale.ROOT)
                : failure==Commissions.Status.CARGO ? "cargo" : "ready";
        return new CareView(t("family.fit_title",dog.getDisplayName()),t("family.next."+next),rows,
                List.of(button(CarePanels.BACK,"back",true),button(CHOOSE_GOAL,"other_goal",true)));
    }
    /** Birth records contain parental genomes, not identities; either parental order is a genuine match. */
    public static List<Bunnidog> relatives(Player player,BlockPos pos,GeneticsTableBlockEntity table) {
        return relatives(player,pos,table,false);
    }
    private record Parents(Genome a,Genome b) {}
    private static Optional<Parents> parents(Player player,GeneticsTableBlockEntity table,boolean fromSeed) {
        if (fromSeed) return SeedItem.read(LitterPanel.nextSeed(player)).map(seed->new Parents(seed.mother(),seed.father()));
        var a=TissueSampleItem.read(table.item(0)); var b=TissueSampleItem.read(table.item(1));
        return a.isPresent() && b.isPresent() ? Optional.of(new Parents(a.orElseThrow().genome(),b.orElseThrow().genome())) : Optional.empty();
    }
    public static List<Bunnidog> relatives(Player player,BlockPos pos,GeneticsTableBlockEntity table,boolean fromSeed) {
        var source=parents(player,table,fromSeed);
        if (!hasCalipers(player) || table.goal().isEmpty() || source.isEmpty()) return List.of();
        var first=source.orElseThrow().a(); var second=source.orElseThrow().b();
        return player.level().getEntitiesOfClass(Bunnidog.class,new AABB(pos).inflate(RANGE),dog->dog.isAlive() && dog.acceptsCare()
                && dog.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=RANGE*RANGE
                && dog.motherGenome().isPresent() && dog.fatherGenome().isPresent()
                && (dog.motherGenome().orElseThrow().equals(first) && dog.fatherGenome().orElseThrow().equals(second)
                || dog.motherGenome().orElseThrow().equals(second) && dog.fatherGenome().orElseThrow().equals(first)))
                .stream().sorted(Comparator.comparing(Bunnidog::getUUID)).toList();
    }
    public static int page(List<Bunnidog> family,int requested) { return Math.clamp(requested,0,Math.max(0,(family.size()-1)/PAGE_SIZE)); }
    public static List<Bunnidog> visible(List<Bunnidog> family,int requested) {
        int start=page(family,requested)*PAGE_SIZE; return family.subList(start,Math.min(family.size(),start+PAGE_SIZE));
    }
    public static CareView family(Player player,BlockPos pos,GeneticsTableBlockEntity table,int requested) {
        return family(player,pos,table,requested,false);
    }
    public static CareView family(Player player,BlockPos pos,GeneticsTableBlockEntity table,int requested,boolean fromSeed) {
        var family=relatives(player,pos,table,fromSeed); int page=page(family,requested);
        var rows=new ArrayList<Component>(); var controls=new ArrayList<CareView.Control>();
        rows.add(table.goal().map(goal->t("board.request",title(goal))).orElse(t("family.no_goal")));
        var goal=table.goal().orElse(Commissions.Request.NIGHT_GUIDE); var shown=visible(family,page);
        var summary=Component.empty();
        for (int i=0;i<shown.size();i++) {
            var dog=shown.get(i); var checks=goal.checks(dog.genome(),dog.stats(),0).stream().filter(check->check.failure()!=Commissions.Status.CARGO).toList();
            int number=page*PAGE_SIZE+i+1;
            if (i>0) summary.append("\n");
            summary.append(t("family.entry",number,dog.getDisplayName(),t(dog.isBaby() ? "animal.young" : "family.adult_short"),checks.stream().filter(Commissions.Check::met).count(),checks.size()))
                    .append(" ").append(t("family.position",dog.getBlockX(),dog.getBlockY(),dog.getBlockZ()));
            controls.add(new CareView.Control(SELECT+i,t("family.inspect",number),dog.distanceToSqr(player)<=36));
        }
        // The actual entries precede their buttons in the client's primary-control layout.
        rows.add(shown.isEmpty() ? t("family.none") : summary);
        rows.add(t(fromSeed ? "family.seed_source" : "family.source",RANGE));
        if (!hasCalipers(player)) rows.add(t("family.need_calipers"));
        rows.add(t("family.page",page+1,Math.max(1,(family.size()+PAGE_SIZE-1)/PAGE_SIZE),family.size()));
        controls.add(button(fromSeed ? LitterPanel.LITTER : WorkshopPanels.BACK_TABLE,fromSeed ? "back_litter" : "back_table",true));
        controls.add(button(PREV,"family_prev",page>0)); controls.add(button(NEXT,"family_next",(page+1)*PAGE_SIZE<family.size()));
        controls.add(button(SHOW_FAMILY,"show_family",!shown.isEmpty()));
        return new CareView(t("family.title"),t("family.next"),rows,controls);
    }
    public static void show(Player player,List<Bunnidog> family) {
        if (!(player instanceof ServerPlayer server)) return;
        ServerLevel level=server.level();
        for (var dog:family) if (dog.isAlive() && dog.level()==level && level.hasChunkAt(dog.blockPosition()))
            level.sendParticles(ParticleTypes.END_ROD,dog.getX(),dog.getY()+dog.getBbHeight()+.2,dog.getZ(),12,.1,.2,.1,.015);
    }
}
