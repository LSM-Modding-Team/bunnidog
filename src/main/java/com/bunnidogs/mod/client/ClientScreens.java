package com.bunnidogs.mod.client;

import com.bunnidogs.mod.client.screen.PedigreeScreen;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.item.GeneSplicerItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.network.LineageResultPayload;
import com.bunnidogs.mod.client.screen.LineageNameScreen;

/**
 * The one door from common code to the client's windows (Phase 8 S3). Items call it only inside {@code level.isClientSide()},
 * so this class (which imports {@code Minecraft}) is never loaded on a dedicated server. Opening a screen is
 * {@code Minecraft.getInstance().gui.setScreen(...)} in 26.2 (verified in NeoForge's own tests, see docs/API_NOTES.md).
 */
public final class ClientScreens {
    private ClientScreens() {}

    public static void careView(com.bunnidogs.mod.network.CareViewPayload payload) {
        var player=Minecraft.getInstance().player;
        if (player!=null && player.containerMenu instanceof com.bunnidogs.mod.menu.CareMenu menu && menu.containerId==payload.containerId())
            menu.receive(payload.view());
    }

    public static void apexBeam(com.bunnidogs.mod.network.ApexBeamPayload payload) {
        var level=Minecraft.getInstance().level;
        var from=payload.from(); var to=payload.to();
        if (level==null || !Double.isFinite(from.lengthSqr()) || !Double.isFinite(to.lengthSqr()) || from.distanceToSqr(to)>4225) return;
        int color=switch (payload.style()) { case 1 -> 0xFFC65A; case 2 -> 0xE34A92; case 3 -> 0x63E89A; case 4 -> 0x8BD7DF; default -> 0xBF7DFF; };
        var dust=new net.minecraft.core.particles.DustParticleOptions(color,payload.style()==0 || payload.style()==4 ? 4 : .8F);
        int count=Math.min(128,Math.max(1,(int)(from.distanceTo(to)*4)));
        for (int i=0;i<=count;i++) {
            var p=from.lerp(to,(double)i/count);
            level.addParticle(dust,p.x,p.y,p.z,0,0,0);
            if (payload.style()==0 || payload.style()==4) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,0,0,0);
                // A broad cylinder, matching the damaging ray's increased radius.
                var axis=to.subtract(from).normalize();var side=axis.cross(new net.minecraft.world.phys.Vec3(0,1,0));
                if(side.lengthSqr()<.01)side=new net.minecraft.world.phys.Vec3(1,0,0);
                side=side.normalize();var up=axis.cross(side).normalize();
                double angle=i*2.4;var edge=p.add(side.scale(Math.cos(angle)*(payload.style()==4 ? .65 : 1.1))).add(up.scale(Math.sin(angle)*(payload.style()==4 ? .65 : 1.1)));
                level.addParticle(dust,edge.x,edge.y,edge.z,0,0,0);
            }
        }
    }

    /** Opens the Pedigree Book window on this log (a snapshot of it). */
    public static void openBook(Pedigree book, InteractionHand hand, ItemStack stack) {
        Minecraft.getInstance().gui.setScreen(editable(new PedigreeScreen(book),hand,stack));
    }

    public static void openRegistry(Pedigree registry, InteractionHand hand, ItemStack stack) {
        Minecraft.getInstance().gui.setScreen(editable(new PedigreeScreen(registry,true),hand,stack));
    }
    private static PedigreeScreen editable(PedigreeScreen screen, InteractionHand hand, ItemStack stack) {
        return screen.editable(hand,stack.getOrDefault(ModItems.PEDIGREE_DATA.get(),""),stack.getOrDefault(ModItems.LINEAGE_NAMES.get(),""));
    }
    public static void lineageResult(LineageResultPayload result) {
        var screen = Minecraft.getInstance().gui.screen();
        if (screen instanceof LineageNameScreen editor) editor.accept(result);
    }

    public static void openRibbon(Pedigree.Entry animal, Component title) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(animal, title));
    }

    /** Opens the sheet of ONE animal that is in no book (the Calipers window, Phase 8 S6): the same screen, no contents, no page turning. */
    public static void openSheet(Pedigree.Entry animal) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(animal));
    }

    /** Opens the window of a seed (Phase 8 S8): the seed, then the mother's and the father's sheets. */
    public static void openSeed(Seed seed) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(seed));
    }

    /** Opens the window of a Tissue Sample (Phase 8 S8): one sheet of the animal it was taken from. */
    public static void openSample(Tissue sample) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(sample));
    }

    public static void openCrest(Tissue source) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(source, Component.translatable("item.bunnidogs.apex_crest")));
    }

    /** Opens the window of a Genetics Table (T2): sample A's sheet, then sample B's, turned with Previous / Next. */
    public static void openTable(TableSheet table) {
        var client = Minecraft.getInstance();
        boolean splicing = client.player != null && client.player.getMainHandItem().getItem() instanceof GeneSplicerItem;
        client.gui.setScreen(new PedigreeScreen(table, splicing));
    }
}
