package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** A cathedral arena, three distinct wings and a sheltered preparation court. All routes share one floor. */
public final class CrownLayout {
    public static final int RADIUS=90,HEIGHT=33;
    private static final BlockPos[] WINGS={new BlockPos(-70,0,0),new BlockPos(70,0,0),new BlockPos(0,0,-70),new BlockPos(0,0,46)};
    public static BlockPos wing(int index) {return WINGS[Math.clamp(index,0,3)];}
    public static BlockState cell(int x,int y,int z) {
        int room=-1,rx=x,rz=z;
        for(int i=0;i<3;i++) {var p=wing(i);if(Math.abs(x-p.getX())<=18 && Math.abs(z-p.getZ())<=18) {room=i;rx=x-p.getX();rz=z-p.getZ();break;}}
        boolean arena=Math.abs(x)<=42 && Math.abs(z)<=42;
        boolean hub=Math.abs(x)<=18 && z>=48 && z<=86;
        boolean path=(Math.abs(z)<=4 && Math.abs(x)<=70) || (Math.abs(x)<=4 && z>=-70 && z<=70)
                || (Math.abs(x)<=70 && z>=64 && z<=70) || (Math.abs(Math.abs(x)-70)<=3 && z>=0 && z<=70);
        if(!arena && room<0 && !hub && !path)return null;
        if(y<-2) return null;
        if(y<0)return ModBlocks.ROOTSTONE.get().defaultBlockState();
        if(y==0) {
            if(arena && (Math.abs(x)%10==0 || Math.abs(z)%10==0))return Blocks.CHISELED_TUFF_BRICKS.defaultBlockState();
            if(room==2 && Math.abs(rx)==11 && Math.abs(rz)>3 && Math.abs(rz)<13)return Blocks.MAGMA_BLOCK.defaultBlockState();
            if(room>=0)return (room==0 ? Blocks.CALCITE : room==1 ? Blocks.MOSS_BLOCK : Blocks.POLISHED_BASALT).defaultBlockState();
            return ModBlocks.ROOTSTONE.get().defaultBlockState();
        }
        if(y==1) {
            for(int i=0;i<4;i++) {var p=wing(i);if(x==p.getX() && z==p.getZ())return ModBlocks.CROWN_SEAL.get().defaultBlockState().setValue(CrownSealBlock.WING,i);}
            if(hub && x==0 && z==76)return ModBlocks.WARREN_WARD.get().defaultBlockState();
        }
        if(arena) {
            boolean door=(Math.abs(x)<=4 && Math.abs(z)==42) || (Math.abs(z)<=4 && Math.abs(x)==42);
            if(z==42 && Math.abs(x)<=3 && y<=6)return ModBlocks.CROWN_GATE.get().defaultBlockState();
            if((Math.abs(x)==42 || Math.abs(z)==42) && !door && y<=30)return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if(y==31)return Math.abs(x)%10==0 || Math.abs(z)%10==0 ? ModBlocks.ROOTSTONE.get().defaultBlockState() : Blocks.TINTED_GLASS.defaultBlockState();
            // Real cover with gaps and raised firing positions. Breakable outer ribs leave the safe core intact.
            if((Math.abs(x)==23 && Math.abs(z)==23) && y<=8)return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if((Math.abs(x)>=22 && Math.abs(x)<=24 && Math.abs(z)>=22 && Math.abs(z)<=24) && y<=4)return Blocks.TUFF_BRICKS.defaultBlockState();
            if((Math.abs(x)==34 && Math.abs(z)<=8 || Math.abs(z)==34 && Math.abs(x)<=8) && y<=2)return Blocks.TUFF_BRICKS.defaultBlockState();
            if(Math.abs(x)<=2 && Math.abs(z)<=2 && y<=1)return Blocks.CHISELED_TUFF_BRICKS.defaultBlockState();
        }
        if(room>=0) {
            boolean opening=(Math.abs(rx)<=4 && Math.abs(rz)==18) || (Math.abs(rz)<=4 && Math.abs(rx)==18);
            if((Math.abs(rx)==18 || Math.abs(rz)==18) && !opening && y<=14)return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if(room==0 && Math.abs(rx)>=13 && Math.abs(rx)<=16 && Math.abs(rz)<=11 && y<=3)return Blocks.QUARTZ_BLOCK.defaultBlockState();
            if(room==0 && Math.abs(rx)>=10 && Math.abs(rx)<=12 && Math.abs(rz)<=3 && y<=Math.abs(rx)-9)return Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING,rx<0 ? net.minecraft.core.Direction.WEST : net.minecraft.core.Direction.EAST);
            if(room==1 && Math.abs(rx)>=7 && Math.abs(rx)<=13 && Math.abs(rz)>=4 && Math.abs(rz)<=9 && y<=2
                    && (Math.abs(rx)==7 || Math.abs(rx)==13 || Math.abs(rz)==4 || Math.abs(rz)==9) && !(Math.abs(rz)==6 || Math.abs(rz)==7))return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            if(room==2 && Math.abs(rx)==14 && Math.abs(rz)<=6 && y<=5)return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
            if(Math.abs(rx)==18 && Math.abs(rz)==18 && y<=24)return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if(y==15)return (room==0 ? Blocks.TINTED_GLASS : room==1 ? Blocks.DARK_OAK_PLANKS : Blocks.POLISHED_BASALT).defaultBlockState();
            if((Math.abs(rx)==10 && Math.abs(rz)==10) && y<=6)return (room==0 ? Blocks.AMETHYST_BLOCK : room==1 ? Blocks.TUFF_BRICKS : Blocks.BASALT).defaultBlockState();
            if(y==14 && (Math.abs(rx)==8 && Math.abs(rz)==8))return Blocks.SHROOMLIGHT.defaultBlockState();
        }
        if(hub) {
            if((Math.abs(x)==18 || z==86) && y<=8 && !(z==86 && Math.abs(x)<=4))return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if(y==9)return ModBlocks.ROOTSTONE.get().defaultBlockState();
            if(y==8 && Math.abs(x)==12 && z%12==4)return Blocks.SEA_LANTERN.defaultBlockState();
            if(y==1 && z==66 && x==-8)return Blocks.CRAFTING_TABLE.defaultBlockState();
            if(y==1 && z==66 && x==-6)return Blocks.SMITHING_TABLE.defaultBlockState();
            if(y==1 && z==66 && x==8)return Blocks.ENDER_CHEST.defaultBlockState();
        }
        if(y==1 && ((arena && Math.abs(x)==40 && Math.abs(z)%12==0) || (path && y==1 && (Math.abs(x)+Math.abs(z))%16==0)))return Blocks.SEA_LANTERN.defaultBlockState();
        int ceiling=arena ? 31 : room>=0 ? 15 : hub ? 9 : 7;
        return y<ceiling ? Blocks.AIR.defaultBlockState() : null;
    }
    private CrownLayout() {}
}
