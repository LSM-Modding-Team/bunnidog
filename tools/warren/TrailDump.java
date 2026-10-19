import com.bunnidogs.mod.layout.DenShape;
import com.bunnidogs.mod.layout.EntranceTrail;

/** Development preview data from the exact pure layout; pipe to art/preview_trails.py. */
public final class TrailDump {
    public static void main(String[] args) {
        for (var dir : DenShape.Dir.values()) {
            int radial = dir == DenShape.Dir.EAST || dir == DenShape.Dir.WEST ? 7 : 9;
            System.out.println("CASE "+dir.name()+" "+radial);
            for (var c : DenShape.mound(dir,radial,0)) System.out.println("MOUND "+c.dx()+" "+c.dy()+" "+c.dz()+" "+c.kind());
            for (var p : EntranceTrail.route(dir,radial,1)) System.out.println("TRAIL "+p.dx()+" "+p.dz());
        }
    }
}
