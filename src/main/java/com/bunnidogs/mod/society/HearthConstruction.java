package com.bunnidogs.mod.society;

import com.bunnidogs.mod.layout.CivicAnnex;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;

/** Saved public work fronts, exact placed-cell receipts and deliberate, bounded land permissions. */
public final class HearthConstruction {
    private final HearthTreeBlockEntity home;
    private int base,approved;
    private final int[] radii={2,2,2,2};
    private final Set<BlockPos> seen=new HashSet<>(),owned=new HashSet<>(),repairs=new HashSet<>();
    private final Map<BlockPos,Long> blocked=new HashMap<>();
    private UUID surveyor;private int pending=-1,pendingRadius=2;private boolean repairSurvey;private long until;
    public HearthConstruction(HearthTreeBlockEntity home) {this.home=home;}
    public List<HearthTreeBlockEntity.Cell> room(int rotation) {
        return CivicAnnex.cells(rotation,radii[rotation]).stream().map(c->{boolean nest=c.material()==CivicAnnex.Material.NEST;return new HearthTreeBlockEntity.Cell(home.getBlockPos().offset(c.x(),c.y(),c.z()),nest ? ModBlocks.NEST.get().defaultBlockState() : ModBlocks.GUAVA_PLANKS.get().defaultBlockState(),nest ? ModItems.NEST.get() : ModItems.GUAVA_PLANKS.get());}).toList();
    }
    public List<HearthTreeBlockEntity.Cell> base() {return room(base);}
    public List<HearthTreeBlockEntity.Cell> cells() {var result=new ArrayList<HearthTreeBlockEntity.Cell>();for(int r=0;r<4;r++)if((approved&(1<<r))!=0)result.addAll(room(r));return List.copyOf(result);}
    public HearthTreeBlockEntity.Cell cell(BlockPos at) {return cells().stream().filter(c->c.pos().equals(at)).findFirst().orElse(null);}
    public boolean fits(int rotation,int radius) {
        if(!home.attached())return false;var level=home.getLevel();var center=CivicAnnex.turn(0,0,8,CivicAnnex.Material.PLANK,rotation);var at=home.getBlockPos().offset(center.x(),0,center.z());
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
            var floor=at.offset(x,-1,z);if(!level.hasChunksAt(floor,floor.above(5)) || !level.getWorldBorder().isWithinBounds(floor) || !level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP) || !level.getFluidState(floor).isEmpty())return false;
            for(int y=0;y<4;y++) {var p=at.offset(x,y,z);if(!level.getBlockState(p).isAir())return false;}
        }
        // The two-block doorway must have a real clear approach back toward the common store.
        for(int step=radius+1;step<=7;step++) {var p=CivicAnnex.turn(0,0,8-step,CivicAnnex.Material.PLANK,rotation);var stand=home.getBlockPos().offset(p.x(),0,p.z());
            if(!level.hasChunkAt(stand) || !level.getWorldBorder().isWithinBounds(stand) || !level.getBlockState(stand).isAir() || !level.getBlockState(stand.above()).isAir() || !level.getBlockState(stand.below()).isFaceSturdy(level,stand.below(),Direction.UP))return false;
        }return true;
    }
    public boolean chooseBase(Player player) {
        if(!home.attached() || !home.permitted(player))return false;long now=home.getLevel().getGameTime();
        if(player.getUUID().equals(surveyor) && pending>=0 && !repairSurvey && now<until && fits(pending,2) && home.orchardSafe()) {base=pending;approved=1<<base;radii[base]=2;surveyor=null;home.authorize();return true;}
        if(!home.orchardSafe())return false;
        for(int r=0;r<4;r++)if(fits(r,2)) {preview(player,r,2,false);return true;}return false;
    }
    public void survey(Player player) {
        if(!home.attached() || !home.permitted(player))return;long now=home.getLevel().getGameTime();
        var missing=cells().stream().filter(c->seen.contains(c.pos()) && home.getLevel().hasChunkAt(c.pos()) && home.getLevel().getBlockState(c.pos()).isAir() && !repairs.contains(c.pos())).toList();
        if(!missing.isEmpty()) {
            if(repairSurvey && player.getUUID().equals(surveyor) && now<until) {missing.forEach(c->repairs.add(c.pos()));surveyor=null;home.setChanged();player.sendOverlayMessage(Component.translatable("message.bunnidogs.construction.repair_allowed"));return;}
            repairSurvey=true;surveyor=player.getUUID();until=now+200;pending=-1;for(var c:missing)mark(c.pos(),ParticleTypes.END_ROD);player.sendOverlayMessage(Component.translatable("message.bunnidogs.construction.confirm_repair",missing.size()));return;
        }
        if(!home.shelterComplete()) {player.sendOverlayMessage(home.need());return;}
        if(!repairSurvey && player.getUUID().equals(surveyor) && pending>=0 && now<until && (approved&(1<<pending))==0 && fits(pending,pendingRadius)) {
            approved|=1<<pending;radii[pending]=pendingRadius;surveyor=null;home.setChanged();player.sendOverlayMessage(Component.translatable("message.bunnidogs.construction.annex_allowed"));return;
        }
        int radius=home.residents().size()>4 ? 3 : 2;
        for(int r=0;r<4;r++)if((approved&(1<<r))==0 && fits(r,radius)) {preview(player,r,radius,true);return;}
        player.sendOverlayMessage(Component.translatable("message.bunnidogs.construction.no_plot"));
    }
    private void preview(Player player,int rotation,int radius,boolean extension) {
        pending=rotation;pendingRadius=radius;surveyor=player.getUUID();until=home.getLevel().getGameTime()+200;repairSurvey=false;
        for(var c:CivicAnnex.cells(rotation,radius))if(c.y()==0)mark(home.getBlockPos().offset(c.x(),0,c.z()),ParticleTypes.END_ROD);
        if(!extension)for(var at:home.orchard())mark(at,ParticleTypes.HAPPY_VILLAGER);
        player.sendOverlayMessage(Component.translatable(extension ? "message.bunnidogs.construction.confirm_annex" : "message.bunnidogs.hearth.confirm_plot"));
    }
    private void mark(BlockPos at,net.minecraft.core.particles.SimpleParticleType type) {((ServerLevel)home.getLevel()).sendParticles(type,at.getX()+.5,at.getY()+.1,at.getZ()+.5,3,.05,.05,.05,0);}
    public boolean canBuild(HearthTreeBlockEntity.Cell cell) {
        if(!home.attached() || cell==null || !home.authorized() || !home.getLevel().hasChunkAt(cell.pos()) || !home.getLevel().getWorldBorder().isWithinBounds(cell.pos()) || blocked.getOrDefault(cell.pos(),0L)>home.getLevel().getGameTime())return false;
        var state=home.getLevel().getBlockState(cell.pos());if(!state.isAir() || !home.getLevel().getFluidState(cell.pos()).isEmpty())return false;
        return (!seen.contains(cell.pos()) || repairs.contains(cell.pos())) && cell.state().canSurvive(home.getLevel(),cell.pos());
    }
    public void placed(BlockPos at) {seen.add(at.immutable());owned.add(at.immutable());repairs.remove(at);blocked.remove(at);home.setChanged();}
    public void failed(BlockPos at) {blocked.put(at.immutable(),home.getLevel().getGameTime()+200);if(blocked.size()>512)blocked.entrySet().removeIf(e->e.getValue()<home.getLevel().getGameTime());}
    public boolean owned(BlockPos at) {return owned.contains(at);}
    public void explosionDamage(BlockPos at) {if(owned.contains(at) && home.getLevel().hasChunkAt(at) && home.getLevel().getBlockState(at).isAir()) {repairs.add(at.immutable());home.setChanged();}}
    public boolean needsRepair(BlockPos at) {return repairs.contains(at);}
    public void observe() {
        if(!home.attached())return;boolean changed=false;
        for(var cell:cells())if(home.getLevel().hasChunkAt(cell.pos()) && home.getLevel().getBlockState(cell.pos()).equals(cell.state()))changed|=seen.add(cell.pos());
        if(changed)home.setChanged();
    }
    public void save(ValueOutput out) {out.putInt("AnnexBase",base);out.putInt("AnnexPlots",approved);out.putIntArray("AnnexRadii",radii);out.store("AnnexSeen",BlockPos.CODEC.listOf(),seen.stream().sorted().toList());out.store("AnnexOwned",BlockPos.CODEC.listOf(),owned.stream().sorted().toList());out.store("AnnexRepairs",BlockPos.CODEC.listOf(),repairs.stream().sorted().toList());}
    public void load(ValueInput in,boolean legacyAuthorized) {
        base=Math.clamp(in.getIntOr("AnnexBase",0),0,3);approved=in.getIntOr("AnnexPlots",legacyAuthorized ? 1<<base : 0)&15;var values=in.getIntArray("AnnexRadii").orElse(new int[0]);for(int i=0;i<4;i++)radii[i]=i<values.length ? Math.clamp(values[i],2,3) : 2;
        for(var set:List.of(seen,owned,repairs))set.clear();seen.addAll(in.read("AnnexSeen",BlockPos.CODEC.listOf()).orElse(List.of()).stream().limit(512).toList());owned.addAll(in.read("AnnexOwned",BlockPos.CODEC.listOf()).orElse(List.of()).stream().filter(seen::contains).limit(512).toList());repairs.addAll(in.read("AnnexRepairs",BlockPos.CODEC.listOf()).orElse(List.of()).stream().filter(seen::contains).limit(512).toList());blocked.clear();surveyor=null;
    }
}
