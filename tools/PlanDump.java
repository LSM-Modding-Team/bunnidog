import java.util.Optional;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.layout.*;

/** Compare the real plain and planned forecast layouts; pipe to art/gen_book_preview.py. */
public final class PlanDump {
    public static void main(String[] args) {
        var genome = Genome.parse("11-00-11-00-00-00-00").orElseThrow();
        var a = new Tissue(genome,Stats.of(7,8,9,10,11));
        var b = new Tissue(genome,Stats.ZERO);
        var ordinary = new TableSheet(Optional.of(a),Optional.of(b));
        var planned = new TableSheet(Optional.of(a),Optional.of(b),new BreedingPlan(1,2,true));
        var harvest = new TableSheet(Optional.of(a),Optional.of(b),new BreedingPlan(2,2,false,3));
        for (var table : new TableSheet[]{ordinary,planned,harvest}) {
            System.out.println("SPREAD 0");
            var spread = table.spread(TableSheet.FORECAST,Metrics.ESTIMATE);
            SheetDump.page("L",spread.left(),Metrics.ESTIMATE);
            SheetDump.page("R",spread.right(),Metrics.ESTIMATE);
        }
    }
}
