import java.util.Optional;
import java.util.UUID;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;

/** Compile with pure genetics/layout plus SheetDump, then pipe into art/gen_lab_preview.py. */
public final class LabDump {
    public static void main(String[] args) {
        var a = new Tissue(Genome.parse("01-0b-05-04-01-01-03").orElseThrow(), Stats.of(7,8,9,10,11));
        var b = new Tissue(Genome.parse("44-bb-55-44-11-11-33").orElseThrow(), Stats.ZERO);
        var table = new TableSheet(Optional.of(a),Optional.of(b));
        var registry = new Pedigree();
        for (int i = 0; i < 24; i++) {
            Genome g = Genome.DEFAULT.with(Gene.COAT,0,i%12).with(Gene.COAT,1,i%12)
                    .with(Gene.EARS,0,i/12).with(Gene.EARS,1,i/12);
            Purebreds.add(registry,new Pedigree.Entry(g,Optional.of(Genome.DEFAULT),Optional.of(Genome.DEFAULT),
                    Optional.of(new UUID(0,i+1)),Optional.empty(),Optional.of(Stats.ZERO),false));
        }
        Page.Spread[] spreads = {table.spread(2,Metrics.ESTIMATE),table.spread(3,Metrics.ESTIMATE),
                table.spread(4,Metrics.ESTIMATE),RegistryContents.spread(registry,0,Metrics.ESTIMATE)};
        for (int i = 0; i < spreads.length; i++) {
            System.out.println("SPREAD " + i);
            SheetDump.page("L",spreads[i].left(),Metrics.ESTIMATE);
            SheetDump.page("R",spreads[i].right(),Metrics.ESTIMATE);
        }
    }
}
