package com.bunnidogs.mod.client.screen;

import java.util.List;

import com.bunnidogs.mod.BunnidogsMod;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.RecordFilter;
import com.bunnidogs.mod.genetics.Seed;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.layout.BookContents;
import com.bunnidogs.mod.layout.Page;
import com.bunnidogs.mod.layout.RegistryContents;
import com.bunnidogs.mod.layout.SeedSheet;
import com.bunnidogs.mod.layout.TableSheet;
import com.bunnidogs.mod.layout.SheetLayout;
import com.bunnidogs.mod.registry.ModEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

/**
 * The open Pedigree Book (Phase 8 S3; S6 gave it the one-sheet Calipers window, S8 the Tissue Sample window and the seed window): a contents spread, then one spread per logged animal (docs/SHEET_SPECS.md). It draws
 * ONLY what {@link BookContents#spread} lists (text, hearts, the portrait box), with plain {@code fill} and {@code text}
 * calls: no texture, no sprite name, no vanilla tooltip method. Previous / Next turn the spread (greyed out at the ends);
 * Escape closes it. A snapshot of the book is shown: it does not follow the item while open.
 *
 * <p>Phase 8 S4: the portrait box shows the animal itself. For each sheet the screen builds a THROWAWAY client-side
 * {@link Bunnidog} (never added to the world) from the logged genome and pup flag, and draws it with vanilla's inventory
 * method {@code InventoryScreen.renderEntityInInventoryFollowsAngle}, swaying slowly. If the entity cannot be built or drawn,
 * the box falls back to the flat coat swatch of S3.
 */
public final class PedigreeScreen extends Screen {
    private static final int PARCHMENT = 0xFFE8D8AE;
    private static final int EDGE = 0xFF5B3A1E;
    private static final int SPINE = 0xFFB59B68;
    private static final int MAT = 0xFFD8C79A;
    private static final int HEART_FULL = 0xFFD02828;
    private static final int HEART_EMPTY = 0xFF8A7360;
    private static final int HEART_STEP = 8;
    private static final int PAIR_GLOW = 0xFFF0C040;
    private static final int TIP_BORDER = 0xFF5B3A1E;
    private static final int TIP_BACK = 0xF0231710;
    private static final int TIP_TEXT = 0xFFF4EBD2;
    /** Widest hover box text line, in px; the box is placed 10 px right of and below the mouse and kept on screen. */
    private static final int TIP_WIDTH = 150;

    /** Portrait tuning (one line each, to be adjusted by eye in game): pixels per block, and how far down the animal sits in the box. */
    private static final int PORTRAIT_SIZE = 32;
    private static final float PORTRAIT_OFFSET_Y = 0.15F;
    /** The sway: a full turn left-right-left takes this many milliseconds; the view is 3/4 from the side, swinging around it. */
    private static final long SWAY_MILLIS = 8800L;
    private static final float SWAY_CENTER = 0.5F;
    private static final float SWAY_RANGE = 0.9F;
    /**
     * F3b: vanilla's entity-in-GUI call reads {@code Entity#getId()} (the held-item model resolver uses it as a seed), and in 26.2 that throws
     * "Tried to access entity ID before ID assignment" for an entity that was never added to a level. The portrait animal is never added, so it
     * gets this fixed id by hand. Nothing looks it up in the level, so it cannot clash with a real entity.
     */
    private static final int PORTRAIT_ENTITY_ID = 0x7FFFFF00;

    private final Pedigree book;
    /** Phase 8 S6: the one animal of a Calipers window, or null for the book. A single sheet has no contents, no buttons and no page turning. */
    private final Pedigree.Entry single;
    /** Phase 8 S8: the Tissue Sample of a sample window (one sheet, like {@link #single}), or null. */
    private final Tissue sample;
    /** Phase 8 S8: the seed of a seed window (three spreads: the seed, the mother, the father), or null. */
    private final Seed seed;
    /** Genetics Table: A/B samples followed by the breeding and splicing analysis, or null. */
    private final TableSheet table;
    private final boolean registry;
    private int spread = 0;
    private Page.Spread current;
    private Button previous;
    private Button next;
    private EditBox filter;
    private String query = "";
    /** The throwaway animal drawn in the portrait box of the current sheet; null on the contents spread or when it could not be built. */
    private Bunnidog animal;
    /** Set when building or drawing the animal failed once: the rest of this window uses the flat swatch. */
    private boolean portraitFailed;
    /** F3a: the first successful build is logged once per window, so the log tells "built but not drawn" from "never built". */
    private boolean portraitLogged;

    public PedigreeScreen(Pedigree book) {
        this(book, false);
    }

    public PedigreeScreen(Pedigree book, boolean registry) {
        super(Component.literal(registry ? "Purebred Registry" : "Pedigree Book"));
        this.book = book;
        this.single = null;
        this.sample = null;
        this.seed = null;
        this.table = null;
        this.registry = registry;
    }

    /** The Calipers window (Phase 8 S6): the sheet of one animal that is in no book. */
    public PedigreeScreen(Pedigree.Entry animal) {
        this(animal, Component.literal("Calipers"));
    }

    public PedigreeScreen(Pedigree.Entry animal, Component title) {
        super(title);
        this.book = new Pedigree();
        this.single = animal;
        this.sample = null;
        this.seed = null;
        this.table = null;
        this.registry = false;
    }

    /** The Tissue Sample window (Phase 8 S8): one sheet of the animal the sample was taken from. */
    public PedigreeScreen(Tissue sample) {
        this(sample, Component.literal("Tissue Sample"));
    }

    public PedigreeScreen(Tissue sample, Component title) {
        super(title);
        this.book = new Pedigree();
        this.single = null;
        this.sample = sample;
        this.seed = null;
        this.table = null;
        this.registry = false;
    }

    /** The seed window (Phase 8 S8): the seed, then the mother's and the father's sheets, turned with Previous / Next. */
    public PedigreeScreen(Seed seed) {
        super(Component.literal("Bunnidog Seed"));
        this.book = new Pedigree();
        this.single = null;
        this.sample = null;
        this.seed = seed;
        this.table = null;
        this.registry = false;
    }

    /** The Genetics Table window (T2, T3): the sheet of the sample on slot A, then the one on slot B, then the forecast of their litter, turned with Previous / Next. */
    public PedigreeScreen(TableSheet table) {
        this(table, false);
    }

    public PedigreeScreen(TableSheet table, boolean splicing) {
        super(Component.literal("Genetics Table"));
        this.book = new Pedigree();
        this.single = null;
        this.sample = null;
        this.seed = null;
        this.table = table;
        this.registry = false;
        if (splicing && table.isAnalysis(TableSheet.SPLICING)) this.spread = TableSheet.SPLICING;
    }

    /** True for the windows of ONE sheet (the Calipers on an animal, a Tissue Sample): no buttons, no page turning, no links. */
    private boolean oneSheet() {
        return this.single != null || this.sample != null;
    }

    /** Book contents/sheets, three seed spreads, one/five table spreads, or one standalone sheet. */
    private int spreadCount() {
        if (oneSheet()) {
            return 1;
        }
        if (this.table != null) {
            return this.table.spreadCount();
        }
        return this.seed != null ? SeedSheet.spreadCount() : BookContents.spreadCount(this.book);
    }

    @Override
    protected void init() {
        if (oneSheet()) {
            refresh();          // no buttons: one sheet, nothing to turn to (Escape closes the window)
            return;
        }
        int y = bookTop() + Page.BOOK_H + 4;
        this.previous = Button.builder(Component.literal("< Previous"), button -> turn(-1))
                .bounds(bookLeft(), y, 80, 20).build();
        this.next = Button.builder(Component.literal("Next >"), button -> turn(1))
                .bounds(bookLeft() + Page.BOOK_W - 80, y, 80, 20).build();
        addRenderableWidget(this.previous);
        addRenderableWidget(this.next);
        if (this.seed == null && this.table == null) {
            this.filter = new EditBox(this.font,bookLeft()+84,y,Page.BOOK_W-168,20,Component.translatable("screen.bunnidogs.records.filter"));
            this.filter.setMaxLength(80);
            this.filter.setHint(Component.translatable("screen.bunnidogs.records.filter"));
            this.filter.setTooltip(Tooltip.create(Component.translatable("screen.bunnidogs.records.filter_help")));
            this.filter.setValue(this.query);
            this.filter.setResponder(value -> { this.query = value; this.spread = 0; refresh(); });
            addRenderableWidget(this.filter);
        }
        refresh();
    }

    private int bookLeft() {
        return (this.width - Page.BOOK_W) / 2;
    }

    private int bookTop() {
        return Math.max(2, (this.height - Page.BOOK_H - 24) / 2);
    }

    private void turn(int by) {
        if (oneSheet()) {
            return;
        }
        int target = this.filter != null ? RecordFilter.turn(this.book,this.query,this.spread,by)
                : Math.max(0, Math.min(this.spread + by, spreadCount() - 1));
        if (target != this.spread) {
            pageSound();
        }
        this.spread = target;
        refresh();
    }

    /** The page-turn sound of vanilla's own book (Phase 8 S7, left for later until now); played when the spread really changes. */
    private static void pageSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    /** Left / Right arrow turn the spread like the buttons; any other key (Escape closes the window) goes to the default handling. */
    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.filter != null && this.filter.visible && this.filter.isFocused()) return super.keyPressed(keyEvent);
        if (keyEvent.isLeft()) {
            turn(-1);
            return true;
        }
        if (keyEvent.isRight()) {
            turn(1);
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    /** A left click on a link of the layout (an index line, a parent line) jumps to that spread; the buttons get the click first. */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (!oneSheet() && event.button() == 0 && this.current != null) {
            Page.Link link = linkAt(this.current.left(), bookLeft() + Page.LEFT_X, bookTop() + Page.TOP, event.x(), event.y());
            if (link == null) {
                link = linkAt(this.current.right(), bookLeft() + Page.RIGHT_X, bookTop() + Page.TOP, event.x(), event.y());
            }
            if (link != null) {
                int target = Math.max(0, Math.min(link.spread(), spreadCount() - 1));
                if (target != this.spread) {
                    pageSound();
                }
                this.spread = target;
                refresh();
                return true;
            }
        }
        return false;
    }

    private static Page.Link linkAt(Page page, int ox, int oy, double mouseX, double mouseY) {
        for (Page.Link link : page.links()) {
            if (mouseX >= ox + link.x() && mouseX < ox + link.x() + link.w() && mouseY >= oy + link.y() && mouseY < oy + link.y() + link.h()) {
                return link;
            }
        }
        return null;
    }

    /** Lays the current spread out with the real font's widths and greys out the buttons at the ends. */
    private void refresh() {
        if (this.single != null) {
            this.current = SheetLayout.single(this.single, text -> this.font.width(text));
            this.animal = buildPortrait(this.single);
            return;
        }
        if (this.sample != null) {
            this.current = SheetLayout.sample(this.sample, text -> this.font.width(text));
            this.animal = buildPortrait(SheetLayout.sampleEntry(this.sample));
            return;
        }
        this.previous.active = this.filter != null ? RecordFilter.turn(this.book,this.query,this.spread,-1) != this.spread : this.spread > 0;
        this.next.active = this.filter != null ? RecordFilter.turn(this.book,this.query,this.spread,1) != this.spread : this.spread < spreadCount() - 1;
        if (this.filter != null) {
            this.filter.visible = this.spread == 0;
            if (!this.filter.visible) this.filter.setFocused(false);
        }
        if (this.table != null) {
            this.current = this.table.spread(this.spread, text -> this.font.width(text));
            this.animal = this.table.isAnalysis(this.spread) ? null : buildPortrait(this.table.entry(this.spread));
            return;
        }
        if (this.seed != null) {
            this.current = SeedSheet.spread(this.seed, this.spread, text -> this.font.width(text));
            this.animal = this.spread >= 1 ? buildPortrait(SeedSheet.parentEntry(this.seed, this.spread == 1)) : null;
            return;
        }
        this.current = this.registry ? RegistryContents.spread(this.book, this.spread, text -> this.font.width(text),this.query)
                : BookContents.spread(this.book, this.spread, text -> this.font.width(text),this.query);
        this.animal = this.spread >= 1 && this.spread <= this.book.size() ? buildPortrait(this.book.entries().get(this.spread - 1)) : null;
    }

    /**
     * Builds a client-side Bunnidog from the snapshot of one logged animal (genome, pup flag; spread n >= 1 of the book is the n-th animal's sheet, the Calipers window has its one):
     * on the client the constructor and {@code setGenome} only touch synced data, so nothing needs a server or a world tick.
     */
    private Bunnidog buildPortrait(Pedigree.Entry entry) {
        if (this.portraitFailed) {
            return null;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            BunnidogsMod.LOGGER.warn("Portrait: Minecraft.getInstance().level is null, so no animal is built (the flat swatch is shown)");
            return null;
        }
        try {
            Bunnidog animal = new Bunnidog(ModEntities.BUNNIDOG.get(), level);
            animal.setId(PORTRAIT_ENTITY_ID);
            animal.setGenome(entry.genome());
            if (entry.pup()) {
                animal.setBaby(true);
            }
            if (!this.portraitLogged) {
                this.portraitLogged = true;
                BunnidogsMod.LOGGER.info("Portrait: built a client-side bunnidog for the sheet (genome {})", entry.genome());
            }
            return animal;
        } catch (RuntimeException e) {
            BunnidogsMod.LOGGER.warn("Portrait: building the animal failed, the flat swatch is shown from now on", e);
            this.portraitFailed = true;
            return null;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = bookLeft();
        int y = bookTop();
        graphics.fill(x - 2, y - 2, x + Page.BOOK_W + 2, y + Page.BOOK_H + 2, EDGE);
        graphics.fill(x, y, x + Page.BOOK_W, y + Page.BOOK_H, PARCHMENT);
        graphics.fill(x + Page.BOOK_W / 2 - 1, y, x + Page.BOOK_W / 2 + 1, y + Page.BOOK_H, SPINE);
        if (this.current != null) {
            Page.Hotspot hot = hotspotAt(this.current.left(), x + Page.LEFT_X, y + Page.TOP, mouseX, mouseY);
            int glow = hot != null ? hot.pair() : -1;
            if (hot == null) {
                hot = hotspotAt(this.current.right(), x + Page.RIGHT_X, y + Page.TOP, mouseX, mouseY);
            }
            drawPage(graphics, this.current.left(), x + Page.LEFT_X, y + Page.TOP, glow);
            drawPage(graphics, this.current.right(), x + Page.RIGHT_X, y + Page.TOP, -1);
            if (hot != null) {
                drawTip(graphics, hot.text(), mouseX, mouseY);
            }
        }
    }

    /** The hover region of {@code page} (drawn with its corner at ox, oy) under the mouse, or null. */
    private static Page.Hotspot hotspotAt(Page page, int ox, int oy, int mouseX, int mouseY) {
        for (Page.Hotspot hot : page.hotspots()) {
            if (mouseX >= ox + hot.x() && mouseX < ox + hot.x() + hot.w() && mouseY >= oy + hot.y() && mouseY < oy + hot.y() + hot.h()) {
                return hot;
            }
        }
        return null;
    }

    /** A plain hover box (fills + text, no vanilla tooltip method): the text wrapped to TIP_WIDTH, near the mouse, inside the window. */
    private void drawTip(GuiGraphicsExtractor graphics, String text, int mouseX, int mouseY) {
        List<String> lines = Page.wrap(text, TIP_WIDTH, line -> this.font.width(line));
        int widest = 0;
        for (String line : lines) {
            widest = Math.max(widest, this.font.width(line));
        }
        int w = widest + 8;
        int h = lines.size() * Page.LINE + 6;
        int bx = mouseX + 10 + w > this.width - 2 ? Math.max(2, mouseX - w - 6) : mouseX + 10;
        int by = Math.max(2, Math.min(mouseY + 10, this.height - h - 2));
        graphics.fill(bx - 1, by - 1, bx + w + 1, by + h + 1, TIP_BORDER);
        graphics.fill(bx, by, bx + w, by + h, TIP_BACK);
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(this.font, lines.get(i), bx + 4, by + 3 + i * Page.LINE, TIP_TEXT);
        }
    }

    private void drawPage(GuiGraphicsExtractor graphics, Page page, int ox, int oy, int glowPair) {
        if (glowPair >= 0 && glowPair < page.pairs().size()) {
            Page.Pair pair = page.pairs().get(glowPair);
            graphics.fill(ox + pair.x() - 1, oy + pair.y() - 1, ox + pair.x() + pair.w() + 1, oy + pair.y() + Page.LINE - 1, PAIR_GLOW);
        }
        Page.Portrait portrait = page.portrait();
        if (portrait != null) {
            int px = ox + portrait.x();
            int py = oy + portrait.y();
            graphics.fill(px, py, px + portrait.w(), py + portrait.h(), EDGE);
            graphics.fill(px + 1, py + 1, px + portrait.w() - 1, py + portrait.h() - 1, MAT);
            if (this.animal != null) {
                drawAnimal(graphics, px + 3, py + 3, px + portrait.w() - 3, py + portrait.h() - 3);
            } else {
                graphics.fill(px + 5, py + 5, px + portrait.w() - 5, py + portrait.h() - 5, SheetLayout.coatColor(portrait.coat()));
            }
        }
        for (Page.Hearts hearts : page.hearts()) {
            int icons = hearts.times() > 0 ? 1 : hearts.full() + (hearts.half() ? 1 : 0);
            for (int i = 0; i < icons; i++) {
                boolean half = hearts.times() == 0 && hearts.half() && i == icons - 1;
                drawHeart(graphics, ox + hearts.x() + i * HEART_STEP, oy + hearts.y() + 1, half);
            }
        }
        for (Page.Text text : page.texts()) {
            int tx = ox + text.x() - (text.right() ? this.font.width(text.text()) : 0);
            graphics.text(this.font, text.text(), tx, oy + text.y(), color(text.tone()), false);
        }
    }

    /** The animal in the box (x0, y0 .. x1, y1), turning slowly. If vanilla's call throws, the next frames use the swatch. */
    private void drawAnimal(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1) {
        double turn = (System.currentTimeMillis() % SWAY_MILLIS) / (double) SWAY_MILLIS * 2.0 * Math.PI;
        float xAngle = SWAY_CENTER + SWAY_RANGE * (float) Math.sin(turn);
        try {
            InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, x0, y0, x1, y1, PORTRAIT_SIZE, PORTRAIT_OFFSET_Y, xAngle, 0.0F, this.animal);
        } catch (RuntimeException e) {
            BunnidogsMod.LOGGER.warn("Portrait: drawing the animal failed, the flat swatch is shown from now on", e);
            this.portraitFailed = true;
            this.animal = null;
        }
    }

    /** One heart from the 7 x 6 bitmap of the layout, pixel by pixel; a half heart has its right side grey. */
    private static void drawHeart(GuiGraphicsExtractor graphics, int x, int y, boolean half) {
        String[] rows = SheetLayout.HEART;
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == 'X') {
                    graphics.fill(x + col, y + row, x + col + 1, y + row + 1, half && col >= 4 ? HEART_EMPTY : HEART_FULL);
                }
            }
        }
    }

    private static int color(Page.Tone tone) {
        return switch (tone) {
            case INK -> 0xFF2B1D0E;
            case HEAD -> 0xFF5B3A1E;
            case MUTED -> 0xFF65563A;
            case GOOD -> 0xFF246B2E;
            case BAD -> 0xFFB3261E;
        };
    }
}
