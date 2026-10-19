package com.bunnidogs.mod.layout;

import java.util.List;

import com.bunnidogs.mod.genetics.Pedigree;
import com.bunnidogs.mod.genetics.Plain;
import com.bunnidogs.mod.genetics.RecordFilter;

/**
 * Spread 0 of the Pedigree Book (Phase 8 S3, docs/SHEET_SPECS.md section 3): the title, how many animals are logged, one index
 * line per animal ({@code #7  spotted, short ears, loyal}) and a short "how to read a code". The left page holds the title and
 * the first {@link #LEFT_ROWS} animals, the right page the rest (a full book of {@link Pedigree#MAX_ENTRIES} fits) and the
 * guide. Also the paging of the whole book: spread 0 is this one, spread n (n >= 1) is the sheet of animal n.
 */
public final class BookContents {
    private BookContents() {}

    static final int LEFT_ROWS = 14;
    static final int LEFT_ROWS_Y = 27;
    static final int GUIDE_Y = 90;
    static final int GUIDE_LINES = 6;

    /** The short reading guide of the contents page (the full text, {@link Plain#HOW_TO_READ}, waits for the hover boxes of S5). */
    public static final String GUIDE = "Pairs: Ears, Coat, Legs, Build, Temper., Fertil., Diet. "
            + "The stronger copy shows. Either copy can pass to a pup.";

    /** How many spreads the open book has: the contents and one per logged animal. */
    public static int spreadCount(Pedigree book) {
        return 1 + book.size();
    }

    /** Spread {@code n} (0 = contents, then one sheet per animal in book order); {@code n} is clamped into range. */
    public static Page.Spread spread(Pedigree book, int n, Metrics m) {
        return spread(book,n,m,"");
    }
    public static Page.Spread spread(Pedigree book, int n, Metrics m, String query) {
        int clamped = Math.max(0, Math.min(n, book.size()));
        return clamped == 0 ? contents(book, m,query) : SheetLayout.sheet(book, clamped - 1, m);
    }

    public static Page.Spread contents(Pedigree book, Metrics m) {
        return contents(book,m,"");
    }
    public static Page.Spread contents(Pedigree book, Metrics m, String query) {
        Page left = new Page();
        Page right = new Page();
        var matches = RecordFilter.indices(book,query);
        left.text(0, 0, "Pedigree Book", Page.Tone.HEAD);
        left.text(0, Page.LINE, query == null || query.isBlank() ? book.size() + " of " + Pedigree.MAX_ENTRIES + " animals"
                : matches.size() + " of " + book.size() + " match", Page.Tone.MUTED);
        for (int row = 0; row < matches.size(); row++) {
            int i = matches.get(row);
            var entry = book.entries().get(i);
            String whole = "#" + (i + 1) + "  " + book.lineageName(entry.genome()).map(name -> name+"  ").orElse("")
                    + entry.tag().map(tag -> tag + "  ").orElse("") + Plain.look(entry.genome());
            String line = Page.fit(whole, Page.WIDTH, m);
            Page page = row < LEFT_ROWS ? left : right;
            int y = row < LEFT_ROWS ? LEFT_ROWS_Y + Page.LINE * row : Page.LINE * (row - LEFT_ROWS);
            page.text(0, y, line, Page.Tone.INK);
            page.link(0, y, Page.WIDTH, Page.LINE, i + 1);                    // click: open this animal's sheet
            if (!line.equals(whole)) {
                page.hotspot(0, y, Page.WIDTH, Page.LINE, whole, -1);        // a clipped look is shown whole on hover
            }
        }
        right.text(0, GUIDE_Y, "HOW TO READ A CODE", Page.Tone.HEAD);
        List<String> guide = Page.wrap(GUIDE, Page.WIDTH, m);
        for (int i = 0; i < Math.min(GUIDE_LINES, guide.size()); i++) {
            right.text(0, GUIDE_Y + Page.LINE * (i + 1), guide.get(i), Page.Tone.MUTED);
        }
        return new Page.Spread(left, right);
    }
}
