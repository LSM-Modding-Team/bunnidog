package com.bunnidogs.mod.society;

import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.*;

/** Empty natural stores, finite founder attempts and real genes, without gifted education. */
public final class WildHearth {
    private final HearthTreeBlockEntity home;
    private List<Genome> founders=List.of();
    private boolean natural;
    private int attempts,timberTaken;
    private boolean hatching;
    public WildHearth(HearthTreeBlockEntity home) {this.home=home;}
    public boolean natural() {return natural;}
    public List<Genome> founders() {return founders;}
    public int doorHalf() {return founders.stream().anyMatch(g->Carrying.capacity(g)>=3) ? 1 : 0;}
    public BlockPos nursery() {return home.getBlockPos().north(5);}
    public List<BlockPos> timber() {return List.of(home.getBlockPos().offset(-3,0,0),home.getBlockPos().offset(-3,0,1),home.getBlockPos().offset(-3,0,2));}
    public List<BlockPos> fruit() {return List.of(home.getBlockPos().offset(-3,0,-2),home.getBlockPos().offset(0,0,-2),home.getBlockPos().offset(3,0,-2));}
    public List<BlockPos> beds() {return List.of(home.getBlockPos().offset(-1,0,2),home.getBlockPos().offset(1,0,2),home.getBlockPos().offset(0,0,3));}
    public List<BlockPos> fields() {return List.of(home.getBlockPos().offset(5,0,0),home.getBlockPos().offset(5,0,1),home.getBlockPos().offset(5,0,2));}
    public boolean timberAvailable(BlockPos pos) {int i=timber().indexOf(pos);return natural && i>=0 && (timberTaken&(1<<i))==0;}
    public void timberUsed(BlockPos pos) {int i=timber().indexOf(pos);if(i>=0) {timberTaken|=1<<i;home.setChanged();}}
    public boolean emptyNursery() {var p=nursery();return natural && home.attached() && home.getLevel().hasChunksAt(p.offset(-1,-1,-1),p.offset(1,1,1)) && home.getLevel().getWorldBorder().isWithinBounds(p) && !home.getLevel().hasNeighborSignal(p) && home.getLevel().getBlockState(p).isAir() && home.getLevel().getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.FARMLAND);}
    public boolean hatch(com.bunnidogs.mod.entity.Bunnidog worker,BlockPos at) {
        if(hatching || !natural || !at.equals(nursery()) || !home.attached() || worker.society().hearth()!=home || !worker.society().ready() || !(home.getLevel() instanceof ServerLevel level) || !level.hasChunksAt(at.offset(-1,-1,-1),at.offset(1,1,1)) || level.hasNeighborSignal(at))return false;
        if(!(level.getBlockEntity(at) instanceof com.bunnidogs.mod.block.SeedCropBlockEntity crop) || !com.bunnidogs.mod.block.SeedCropBlock.isRipe(level.getBlockState(at)) || !com.bunnidogs.mod.entity.SocietyWork.communitySeed(crop.seedStack(),home))return false;
        hatching=true;
        try {
            var state=level.getBlockState(at);var original=crop.seedStack();
            if(!net.neoforged.neoforge.event.EventHooks.canEntityGrief(level,worker) || !net.neoforged.neoforge.common.CommonHooks.canEntityDestroy(level,at,worker) || net.neoforged.neoforge.event.EventHooks.onBlockPlace(worker,net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,at),Direction.UP)
                    || !home.attached() || worker.society().hearth()!=home || !worker.society().ready() || level.getBlockEntity(at)!=crop || !net.minecraft.world.item.ItemStack.matches(original,crop.seedStack()) || !level.getBlockState(at).equals(state) || level.hasNeighborSignal(at))return false;
            var exact=crop.takeSeed();var seed=com.bunnidogs.mod.item.SeedItem.read(exact);if(seed.isEmpty())return false;
            var pups=new ArrayList<com.bunnidogs.mod.entity.Bunnidog>();boolean hatched=false;
            try {
                var outcome=com.bunnidogs.mod.entity.NurseryHatching.hatch(level,at,Direction.NORTH,seed.get(),p->{p.society().born(home);pups.add(p);});
                hatched=outcome.status()==com.bunnidogs.mod.entity.NurseryHatching.Status.HATCHED && home.attached();
                if(!hatched) {pups.forEach(com.bunnidogs.mod.entity.Bunnidog::discard);return false;}
                if(level.getBlockEntity(at)==crop)level.removeBlock(at,false);home.evidence("birth",outcome.count());return true;
            } finally {if(!hatched) {if(level.getBlockEntity(at)==crop && crop.seed().isEmpty())crop.setSeedStack(exact);else net.minecraft.world.level.block.Block.popResource(level,at,exact);}}
        } finally {hatching=false;}
    }
    /** Configure only data: worldgen block entities may have no attached Level. */
    public boolean configure(long seed) {var rolled=new ArrayList<Genome>();for(int i=0;i<3;i++)rolled.add(Genome.randomFounder(Lineage.MEADOW,new Random(seed+i*7919L)));return configure(rolled);}
    public boolean configure(List<Genome> genomes) {
        if(natural || home.owner()!=null || genomes.size()!=3)return false;natural=true;founders=List.copyOf(genomes);home.wildOwner();home.setChanged();return true;
    }
    public void tick() {
        if(!natural || !home.attached() || !(home.getLevel() instanceof ServerLevel level))return;
        if(!level.hasChunksAt(home.getBlockPos().offset(-8,-1,-8),home.getBlockPos().offset(8,4,11)))return;
        for(var field:fields())if(!home.sites().contains(field.below()))home.link(field.below());
        for(int i=0;i<founders.size();i++)if((attempts&(1<<i))==0) {
            var at=beds().get(i).south();if(!level.getBlockState(at).isAir() || !level.getBlockState(at.above()).isAir() || !level.getBlockState(at.below()).isFaceSturdy(level,at.below(),Direction.UP))continue;
            UUID id=UUID.nameUUIDFromBytes((home.site()+":founder:"+i).getBytes(StandardCharsets.UTF_8));attempts|=1<<i;home.setChanged();if(level.getEntity(id)!=null)continue;
            var dog=ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.STRUCTURE);if(dog==null)continue;dog.setUUID(id);dog.setGenome(founders.get(i));dog.setStats(Stats.ZERO);dog.setAge(0);dog.setHealth(dog.getMaxHealth());dog.snapTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);dog.society().settle(home);dog.setPersistenceRequired();
            if(!level.addFreshEntity(dog) || !home.attached())dog.discard();break;
        }
        if(!home.authorized())home.construction().wildSurvey();
    }
    public void save(ValueOutput out) {if(natural) {out.putBoolean("NaturalHousehold",true);out.putInt("FounderAttempts",attempts);out.putInt("FallenWoodUsed",timberTaken);out.store("WildFounders",com.mojang.serialization.Codec.STRING.listOf(),founders.stream().map(Genome::toString).toList());}}
    public void load(ValueInput in) {
        natural=in.getBooleanOr("NaturalHousehold",false);attempts=in.getIntOr("FounderAttempts",0)&7;timberTaken=in.getIntOr("FallenWoodUsed",0)&7;hatching=false;
        founders=in.read("WildFounders",com.mojang.serialization.Codec.STRING.listOf()).orElse(List.of()).stream().map(Genome::parse).flatMap(Optional::stream).limit(3).toList();if(founders.size()!=3) {natural=false;founders=List.of();}
    }
}
