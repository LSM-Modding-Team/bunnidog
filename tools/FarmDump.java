import com.bunnidogs.mod.genetics.Lineage;
import com.bunnidogs.mod.layout.RegionalFarms;

/** Actual regional cells for art/preview_farms.py; pure layout preview, not a Minecraft screenshot. */
public final class FarmDump {
    public static void main(String[] args) {
        for (var style : new Lineage[]{Lineage.MEADOW,Lineage.DESERT,Lineage.NETHER,Lineage.SWAMP,Lineage.CAVE,Lineage.END}) {
            System.out.println("FARM "+style);
            for (var c : RegionalFarms.cells(style,0,true,true))
                System.out.println(c.dx()+" "+c.dy()+" "+c.dz()+" "+c.mat());
        }
    }
}
