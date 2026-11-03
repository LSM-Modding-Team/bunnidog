package com.bunnidogs.mod.society;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;

/** Fresh-glade-only stone permissions and paid primitive infrastructure; older sites never acquire these plots. */
public final class WildWorks {
    private final HearthTreeBlockEntity home;
    private final List<BlockPos> stones;
    private List<HearthTreeBlockEntity.Cell> workCells;
    private boolean enabled;
    private int stoneTaken;
    public WildWorks(HearthTreeBlockEntity home) {
        this.home=home;var base=home.getBlockPos();
        stones=List.of(base.offset(-5,1,-7),base.offset(4,1,-7),
                base.offset(-6,0,-7),base.offset(-5,0,-7),base.offset(-4,0,-7),base.offset(-6,0,-6),base.offset(-5,0,-6),
                base.offset(3,0,-7),base.offset(4,0,-7),base.offset(5,0,-7),base.offset(4,0,-6),base.offset(5,0,-6));
    }
    void configure() { enabled=true;stoneTaken=0; }
    public boolean enabled() { return enabled && home.wild().natural(); }
    public List<BlockPos> stones() { return stones; }
    public BlockPos furnace() { return home.getBlockPos().offset(4,0,4); }
    public List<HearthTreeBlockEntity.Cell> cells() {
        if(!enabled())return List.of();
        if(workCells!=null)return workCells;
        var cells=new ArrayList<HearthTreeBlockEntity.Cell>();
        cells.add(new HearthTreeBlockEntity.Cell(furnace(),Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.WEST),Items.FURNACE));
        for(var at:List.of(home.getBlockPos().offset(-6,0,1),home.getBlockPos().offset(6,0,1),home.getBlockPos().offset(-4,0,5),home.getBlockPos().offset(4,0,5)))
            cells.add(new HearthTreeBlockEntity.Cell(at,Blocks.TORCH.defaultBlockState(),Items.TORCH));
        return workCells=List.copyOf(cells);
    }
    public boolean stoneAvailable(BlockPos at) {
        int index=stones().indexOf(at);return enabled() && index>=0 && (stoneTaken&(1<<index))==0;
    }
    public boolean canMine(BlockPos at) {
        if(!stoneAvailable(at) || !home.attached())return false;var level=home.getLevel();
        return level.hasChunksAt(at.offset(-1,-1,-1),at.offset(1,2,1)) && level.getWorldBorder().isWithinBounds(at)
                && level.getBlockState(at).is(Blocks.STONE) && level.getBlockState(at.above()).isAir()
                && level.getBlockState(at.above(2)).isAir() && !level.hasNeighborSignal(at);
    }
    public boolean reserveStone(BlockPos at) {
        if(!canMine(at))return false;stoneTaken|=1<<stones().indexOf(at);home.setChanged();return true;
    }
    public void unreserveStone(BlockPos at) {
        int index=stones().indexOf(at);
        if(index>=0 && home.attached() && home.getLevel().hasChunkAt(at) && home.getLevel().getBlockState(at).is(Blocks.STONE)) {stoneTaken&=~(1<<index);home.setChanged();}
    }
    public void observe() {
        if(!enabled() || !home.attached())return;boolean changed=false;
        for(int index=0;index<stones.size();index++) {
            var at=stones.get(index);
            if((stoneTaken&(1<<index))==0 && home.getLevel().hasChunkAt(at) && !home.getLevel().getBlockState(at).is(Blocks.STONE)) {stoneTaken|=1<<index;changed=true;}
        }
        if(changed)home.setChanged();
    }
    public boolean needsStone() {
        return enabled() && home.attached() && home.construction().canBuild(cells().getFirst())
                && CivicRecipes.count(home,Items.FURNACE)==0 && CivicRecipes.count(home,Items.COBBLESTONE)<8;
    }
    public boolean needsPickaxe() {
        return needsStone() && WildForaging.pickaxeSlot(home)<0
                && home.residents().stream().noneMatch(dog->dog.society().miningTool());
    }
    public boolean needsFurnace() {
        return enabled() && home.attached() && home.construction().canBuild(cells().getFirst()) && CivicRecipes.count(home,Items.FURNACE)==0;
    }
    public boolean needsTorches() {
        return enabled() && home.attached() && CivicRecipes.count(home,Items.TORCH)
                <cells().stream().filter(c->c.item()==Items.TORCH && home.construction().canBuild(c)).count();
    }
    public boolean workCell(BlockPos at) { return enabled() && cells().stream().anyMatch(c->c.pos().equals(at)); }
    public boolean pending() { return enabled() && home.attached() && cells().stream().anyMatch(home.construction()::canBuild); }
    public void placed(BlockPos at) {
        if(!enabled() || !at.equals(furnace()) || !home.attached() || !home.construction().owned(at))return;
        var cell=cells().getFirst();
        if(home.getLevel().getBlockState(at).equals(cell.state()) && home.getLevel().getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.FurnaceBlockEntity furnace && furnace.isEmpty())home.link(at);
    }
    public void save(ValueOutput out) { out.putBoolean("PrimitiveWorkshopPlots",enabled);out.putInt("GladeStoneUsed",stoneTaken); }
    public void load(ValueInput in,boolean natural) { enabled=natural && in.getBooleanOr("PrimitiveWorkshopPlots",false);stoneTaken=in.getIntOr("GladeStoneUsed",0)&4095; }
}
