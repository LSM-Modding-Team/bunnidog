package com.bunnidogs.mod.layout;

import java.util.*;

/** Pure finite room plans: open entrance, usable interior, walls before roof, and actual sleeping places. */
public final class CivicAnnex {
    public enum Material { PLANK,NEST }
    public record Cell(int x,int y,int z,Material material) {}
    private CivicAnnex() {}
    public static List<Cell> cells(int rotation,int radius) {return cells(rotation,radius,0);}
    public static List<Cell> cells(int rotation,int radius,int doorwayHalf) {
        if(rotation<0 || rotation>3 || radius<2 || radius>3 || doorwayHalf<0 || doorwayHalf>1)throw new IllegalArgumentException("Unknown annex shape");
        var cells=new ArrayList<Cell>();
        for(int y=0;y<=2;y++)for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)
            if((Math.abs(x)==radius || Math.abs(z)==radius) && !(Math.abs(x)<=doorwayHalf && z==-radius && y<2))cells.add(turn(x,y,z+8,Material.PLANK,rotation));
        for(int x:new int[]{-1,1})for(int z=0;z<radius;z++)cells.add(turn(x,0,z+8,Material.NEST,rotation));
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)cells.add(turn(x,3,z+8,Material.PLANK,rotation));
        return List.copyOf(cells);
    }
    public static Cell turn(int x,int y,int z,Material material,int rotation) {
        return switch(rotation) {case 0->new Cell(x,y,z,material);case 1->new Cell(-z,y,x,material);case 2->new Cell(-x,y,-z,material);case 3->new Cell(z,y,-x,material);default->throw new IllegalArgumentException();};
    }
}
