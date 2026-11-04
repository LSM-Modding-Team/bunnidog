package com.bunnidogs.mod.society;

import java.util.*;
import com.bunnidogs.mod.registry.*;
import net.minecraft.core.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;

/** Fresh-glade-only stone permissions and paid primitive infrastructure; older sites never acquire these plots. */
public final class WildWorks {
    private final HearthTreeBlockEntity home;
    private final List<BlockPos> stones;
    private List<HearthTreeBlockEntity.Cell> workCells;
    private boolean enabled,restPlots,workbenchPlot;
    private int stoneTaken;
    public WildWorks(HearthTreeBlockEntity home) {
        this.home=home;var base=home.getBlockPos();
        stones=List.of(base.offset(-5,1,-7),base.offset(4,1,-7),
                base.offset(-6,0,-7),base.offset(-5,0,-7),base.offset(-4,0,-7),base.offset(-6,0,-6),base.offset(-5,0,-6),
                base.offset(3,0,-7),base.offset(4,0,-7),base.offset(5,0,-7),base.offset(4,0,-6),base.offset(5,0,-6));
    }
    void configure() { enabled=restPlots=workbenchPlot=true;stoneTaken=0;workCells=null; }
    public boolean enabled() { return enabled && home.wild().natural(); }
    public boolean restPlots() { return enabled() && restPlots; }
    public boolean workbenchPlot() { return enabled() && workbenchPlot; }
    public List<BlockPos> stones() { return stones; }
    public BlockPos furnace() { return home.getBlockPos().offset(4,0,4); }
    public BlockPos workbench() { return home.getBlockPos().west(2); }
    private HearthTreeBlockEntity.Cell benchCell() {return new HearthTreeBlockEntity.Cell(workbench(),Blocks.CRAFTING_TABLE.defaultBlockState(),Items.CRAFTING_TABLE);}
    public boolean requiresWorkbench(CivicRecipes.RecipeId recipe) {return workbenchPlot() && CivicRecipes.needsWorkbench(recipe);}
    public boolean craftingAvailable(CivicRecipes.RecipeId recipe) {
        if(!requiresWorkbench(recipe))return true;var at=workbench();
        return home.attached() && home.getLevel().hasChunksAt(at.offset(-1,-1,-1),at.offset(1,1,1))
                && home.getLevel().getWorldBorder().isWithinBounds(at) && home.getLevel().getBlockState(at).is(Blocks.CRAFTING_TABLE) && !home.getLevel().hasNeighborSignal(at);
    }
    private boolean onWay(BlockPos at) {return home.residents().stream().anyMatch(dog->at.equals(dog.society().buildTarget()));}
    public boolean needsWorkbench() {return workbenchPlot() && home.attached() && home.construction().canBuild(benchCell()) && CivicRecipes.count(home,Items.CRAFTING_TABLE)==0 && !onWay(workbench());}
    public List<HearthTreeBlockEntity.Cell> cells() {
        if(!enabled())return List.of();
        if(workCells!=null)return workCells;
        var cells=new ArrayList<HearthTreeBlockEntity.Cell>();
        cells.add(new HearthTreeBlockEntity.Cell(furnace(),Blocks.FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.WEST),Items.FURNACE));
        for(var at:List.of(home.getBlockPos().offset(-6,0,1),home.getBlockPos().offset(6,0,1),home.getBlockPos().offset(-4,0,5),home.getBlockPos().offset(4,0,5)))
            cells.add(new HearthTreeBlockEntity.Cell(at,Blocks.TORCH.defaultBlockState(),Items.TORCH));
        if(restPlots())for(var at:home.wild().beds())cells.add(new HearthTreeBlockEntity.Cell(at,ModBlocks.NEST.get().defaultBlockState(),ModItems.NEST.get()));
        if(workbenchPlot())cells.add(benchCell());
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
                && CivicRecipes.count(home,Items.FURNACE)==0 && !onWay(furnace()) && CivicRecipes.count(home,Items.COBBLESTONE)<8;
    }
    public boolean needsPickaxe() {
        return needsStone() && WildForaging.pickaxeSlot(home)<0
                && home.residents().stream().noneMatch(dog->dog.society().miningTool());
    }
    public boolean needsFurnace() {
        return enabled() && home.attached() && home.construction().canBuild(cells().getFirst()) && CivicRecipes.count(home,Items.FURNACE)==0 && !onWay(furnace());
    }
    public boolean needsTorches() {
        if(!enabled() || !home.attached())return false;
        var missing=cells().stream().filter(c->c.item()==Items.TORCH && home.construction().canBuild(c)).toList();
        var moving=home.residents().stream().map(dog->dog.society().buildTarget()).filter(Objects::nonNull).toList();
        return CivicRecipes.count(home,Items.TORCH)+missing.stream().filter(c->moving.contains(c.pos())).count()<missing.size();
    }
    public boolean workCell(BlockPos at) { return enabled() && cells().stream().anyMatch(c->c.pos().equals(at)); }
    public boolean pending() { return enabled() && home.attached() && cells().stream().anyMatch(home.construction()::canBuild); }
    public void placed(BlockPos at) {
        if(!enabled() || !at.equals(furnace()) || !home.attached() || !home.construction().owned(at))return;
        var cell=cells().getFirst();
        if(home.getLevel().getBlockState(at).equals(cell.state()) && home.getLevel().getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.FurnaceBlockEntity furnace && furnace.isEmpty())home.link(at);
    }
    public void save(ValueOutput out) { out.putBoolean("PrimitiveWorkshopPlots",enabled);out.putBoolean("PrimitiveRestPlots",restPlots);out.putBoolean("PrimitiveWorkbenchPlot",workbenchPlot);out.putInt("GladeStoneUsed",stoneTaken); }
    public void load(ValueInput in,boolean natural) { enabled=natural && in.getBooleanOr("PrimitiveWorkshopPlots",false);restPlots=enabled && in.getBooleanOr("PrimitiveRestPlots",false);workbenchPlot=enabled && in.getBooleanOr("PrimitiveWorkbenchPlot",false);stoneTaken=in.getIntOr("GladeStoneUsed",0)&4095;workCells=null; }
}
