package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.crown.LunarMasonry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Seeded span-local masonry: clear five-block carriageway, stone arches, parapets and ribbed tunnels. */
public final class BridgeArchitecture {
    private static double bay(double along) {return along%24;}
    private static boolean pier(double along) {double b=bay(along);return b<1.6 || b>22.4;}
    public static int top(double side,double along,boolean tunnel) {
        if(tunnel)return 0;
        if(pier(along))return Math.abs(side)<2.5 ? 6 : 7;
        return Math.abs(side)>2.5 ? 2 : 0;
    }
    public static int bottom(double side,double along,boolean tunnel) {
        if(tunnel || Math.abs(side)<2.5)return -2;
        return pier(along) ? -16 : -(4+(int)Math.round(9*(1-Math.sin(Math.PI*bay(along)/24))));
    }
    private static BlockState block(String name) {return LunarMasonry.BLOCKS.get(name).get().defaultBlockState();}
    public static BlockState state(RealmTerrain.Column c,int y) {
        double side=Math.abs(c.lateral()),bay=bay(c.along());boolean pier=pier(c.along());
        if(side<=2.5 && y>=1 && y<=5)return Blocks.AIR.defaultBlockState();
        if(y==0 && side<=2.5 && c.halfLanding())return block("pale_lunar_tiles_slab");
        if(y==0 && side<=2.5 && c.stair()!=null)return block("pale_lunar_tiles_stairs").setValue(net.minecraft.world.level.block.StairBlock.FACING,c.stair());
        if(y<=0 && y>=-2)return block(y==0 && side<1.4 ? "pale_lunar_tiles" : "lunar_bricks");
        if(c.tunnel()) {
            if(pier && side>2.5 && y>=1 && y<=6)return block(y==3 ? "lunar_rootlight" : "cinderroot_beams");
            if(pier && side<=2.5 && y==6)return block("cinderroot_beams");
            return null;
        }
        if(side>2.5) {
            if(y==1)return block("lunar_bricks");
            if(y==2)return block(pier ? "pale_lunar_tiles" : "lunar_bricks_slab");
            if(pier && y>=3 && y<=7)return block(y==4 ? "lunar_rootlight" : y==7 ? "pale_lunar_tiles" : "cinderroot_beams");
            int rib=bottom(side,c.along(),false);
            if(y>=rib && y<=rib+1 || pier && y<0 && y>=-15)return block(y==rib ? "pale_lunar_tiles" : "lunar_bricks");
            if(pier && y==-16)return block("lunar_rootstone");
        } else if(pier && y==6)return block("cinderroot_beams");
        return null;
    }
    private BridgeArchitecture() {}
}
