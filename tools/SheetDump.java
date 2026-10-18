import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Prints the pages of a small sample book exactly as the layout lists them (one line per thing to draw), for
 * art/gen_book_preview.py. Needs no Minecraft:
 *   java -m jdk.compiler/com.sun.tools.javac.Main -d /tmp/gcheck src/main/java/com/bunnidogs/mod/genetics/*.java src/main/java/com/bunnidogs/mod/layout/*.java tools/SheetDump.java
 *   java -cp /tmp/gcheck SheetDump | python3 art/gen_book_preview.py
 */
public class SheetDump {
    static Genome g(String s) {
        return Genome.parse(s).orElseThrow();
    }

    public static void main(String[] args) {
        Optional<Genome> none = Optional.empty();
        Genome mom = g("11-00-00-00-00-00"), dad = g("22-02-10-00-00-00");
        Pedigree book = new Pedigree();
        book.add(new Pedigree.Entry(mom, none, none, Optional.of(UUID.randomUUID()), Optional.of("BD-1A2B3C"), Optional.of(Stats.of(1, 0, 2, 0, 3)), false));
        book.add(new Pedigree.Entry(dad, none, none, Optional.of(UUID.randomUUID()), Optional.of("BD-77E0F1"), Optional.of(Stats.of(0, 4, 0, 1, 0)), false));
        book.add(new Pedigree.Entry(g("22-32-00-10-00-01"), Optional.of(mom), Optional.of(dad), Optional.of(UUID.randomUUID()), Optional.of("BD-3F7A2C"), Optional.of(Stats.of(3, -1, 0, 2, 12)), false));
        book.add(new Pedigree.Entry(g("33-b3-00-33-00-44"), none, none, Optional.of(UUID.randomUUID()), Optional.of("BD-9C04D2"), Optional.of(Stats.of(60, 22, -3, 8, 30)), true));
        book.add(new Pedigree.Entry(g("00-00-00-00-00-00"), none, none));
        Metrics m = Metrics.ESTIMATE;
        if (args.length > 0 && args[0].equals("filters")) {
            for (String query : new String[]{"adult bloom>=10","shown:bat senses>=20"}) {
                System.out.println("SPREAD filtered");
                System.out.println("FILTER " + query);
                var spread = BookContents.spread(book,0,m,query);
                page("L",spread.left(),m); page("R",spread.right(),m);
            }
            return;
        }
        for (int n = 0; n < BookContents.spreadCount(book); n++) {
            Page.Spread s = BookContents.spread(book, n, m);
            System.out.println("SPREAD " + n);
            page("L", s.left(), m);
            page("R", s.right(), m);
        }
    }

    static void page(String side, Page p, Metrics m) {
        System.out.println("PAGE " + side);
        if (p.portrait() != null) {
            Page.Portrait q = p.portrait();
            System.out.println("P " + q.x() + " " + q.y() + " " + q.w() + " " + q.h() + " " + SheetLayout.coatColor(q.coat()));
        }
        for (Page.Hearts h : p.hearts()) {
            System.out.println("H " + h.x() + " " + h.y() + " " + h.full() + " " + h.half() + " " + h.times());
        }
        for (Page.Text t : p.texts()) {
            System.out.println("T " + t.x() + " " + t.y() + " " + t.tone() + " " + t.right() + " " + t.text());
        }
    }
}
