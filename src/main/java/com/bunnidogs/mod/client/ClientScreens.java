package com.bunnidogs.mod.client;

import com.bunnidogs.mod.client.screen.PedigreeScreen;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.item.GeneSplicerItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * The one door from common code to the client's windows (Phase 8 S3). Items call it only inside {@code level.isClientSide()},
 * so this class (which imports {@code Minecraft}) is never loaded on a dedicated server. Opening a screen is
 * {@code Minecraft.getInstance().gui.setScreen(...)} in 26.2 (verified in NeoForge's own tests, see docs/API_NOTES.md).
 */
public final class ClientScreens {
    private ClientScreens() {}

    /** Opens the Pedigree Book window on this log (a snapshot of it). */
    public static void openBook(Pedigree book) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(book));
    }

    public static void openRegistry(Pedigree registry) {
        Minecraft.getInstance().gui.setScreen(new PedigreeScreen(registry, true));
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

    /** Opens the window of a Genetics Table (T2): sample A's sheet, then sample B's, turned with Previous / Next. */
    public static void openTable(TableSheet table) {
        var client = Minecraft.getInstance();
        boolean splicing = client.player != null && client.player.getMainHandItem().getItem() instanceof GeneSplicerItem;
        client.gui.setScreen(new PedigreeScreen(table, splicing));
    }
}
