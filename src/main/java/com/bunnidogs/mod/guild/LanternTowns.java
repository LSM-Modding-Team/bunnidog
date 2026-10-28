package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.realm.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.*;

/** Actual generated guild halls form the network; only explicit travel reveals bounded seed-valid neighbours. */
public final class LanternTowns extends SavedData {
    private static final Codec<LanternTowns> CODEC=GlobalPos.CODEC.listOf().xmap(LanternTowns::new,n->List.copyOf(n.halls));
    private static final SavedDataType<LanternTowns> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("bunnidogs","lantern_towns"),LanternTowns::new,CODEC);
    private final Set<GlobalPos> halls=new LinkedHashSet<>();
    public LanternTowns() {}
    private LanternTowns(List<GlobalPos> values) {halls.addAll(values);}
    static LanternTowns get(ServerLevel level) {return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);}
    public static void register(ServerLevel level,BlockPos heart) {var data=get(level);if(data.halls.add(GlobalPos.of(level.dimension(),heart)))data.setDirty();}
    public static boolean sheltered(ServerLevel level,BlockPos pos) {
        var data=get(level);
        for(var home:data.halls)if(home.dimension().equals(level.dimension()) && level.hasChunkAt(home.pos()) && home.pos().distSqr(pos)<76*76
                && level.getBlockEntity(home.pos()) instanceof ProjectStation town && town.isTown())return true;
        // Ordinary Bastions keep their base protection without fuel; powered ones expand it.
        return level.getPoiManager().findAll(t->t.is(ModPoi.BASTION.getKey()),p->level.hasChunkAt(p) && p.distSqr(pos)<=48*48 && level.getBlockEntity(p) instanceof ProjectStation station
                && p.distSqr(pos)<=(station.powered() ? 48*48 : 24*24),pos,48,net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY).findAny().isPresent();
    }
    public static ItemStack totem(GlobalPos hall) {var stack=new ItemStack(ModItems.HOMEWARD_TOTEM.get());stack.set(ModItems.GUILD_HOME.get(),hall);return stack;}
    /** First portal use can reveal a seed-valid neighbor. Only this explicit action generates its bounded town footprint. */
    public static GlobalPos discover(ServerLevel level,BlockPos near) {
        if(!(level.getChunkSource().getGenerator() instanceof RealmChunkGenerator g))return null;
        var r=level.registryAccess();var sets=r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);
        var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)sets.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,Identifier.fromNamespaceAndPath("bunnidogs","lantern_warrens"))).value().placement();
        var structure=(LanternWarrenStructure)r.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,Identifier.fromNamespaceAndPath("bunnidogs","lantern_warren"))).value();
        int region=placement.spacing()*16,rx=Math.floorDiv(near.getX(),region),rz=Math.floorDiv(near.getZ(),region);var options=new ArrayList<LanternWarrenPiece>();
        for(int x=rx-6;x<=rx+6;x++)for(int z=rz-6;z<=rz+6;z++) {
            var cp=placement.getPotentialStructureChunk(level.getSeed(),x*placement.spacing(),z*placement.spacing());
            var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(r,g,g.getBiomeSource(),level.getChunkSource().randomState(),level.getServer().getStructureManager(),level.getSeed(),cp,level,b->true);
            var stub=structure.findGenerationPoint(context);if(stub.isEmpty())continue;var piece=(LanternWarrenPiece)stub.get().getPiecesBuilder().build().pieces().getFirst();
            if(piece.center().distSqr(near)>200*200)options.add(piece);
        }
        options.sort(Comparator.comparingDouble(p->p.center().distSqr(near)));
        for(var piece:options.stream().limit(2).toList()) {
            var b=piece.getBoundingBox();for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)level.getChunk(x,z);
            var heart=piece.center().offset(12,1,19);
            if(level.getBlockEntity(heart) instanceof ProjectStation town && town.isTown()) {register(level,heart);return GlobalPos.of(level.dimension(),heart);}
        }
        return null;
    }
    public static void gate(ServerLevel level,ProjectStation gate,ServerPlayer player) {gate(level,gate,player,true);}
    public static void gate(ServerLevel level,ProjectStation gate,ServerPlayer player,boolean direct) {
        var data=get(level);
        if(data.halls.removeIf(home->home.dimension().equals(level.dimension()) && level.hasChunkAt(home.pos()) && !(level.getBlockEntity(home.pos()) instanceof ProjectStation town && town.isTown())))data.setDirty();
        var nodes=get(level).halls.stream().filter(p->p.dimension().equals(level.dimension()) && p.pos().distSqr(gate.getBlockPos())>80*80).sorted(Comparator.comparingLong(p->p.pos().asLong())).toList();
        var held=player.getMainHandItem();
        var own=get(level).halls.stream().filter(p->p.dimension().equals(level.dimension()) && p.pos().distSqr(gate.getBlockPos())<80*80).findFirst().orElse(null);
        if(direct && held.is(ModItems.HOMEWARD_TOTEM.get()) && own!=null) {held.set(ModItems.GUILD_HOME.get(),own);player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.bound"));return;}
        if(nodes.isEmpty()) {
            var neighbor=discover(level,gate.getBlockPos());if(neighbor!=null)nodes=List.of(neighbor);
        }
        if(nodes.isEmpty()) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.no_route"));return;}
        int selected=gate.getPersistentData().getIntOr("Route",0);
        if(player.isShiftKeyDown()) {selected=Math.floorMod(selected+1,nodes.size());gate.getPersistentData().putInt("Route",selected);gate.setChanged();player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.destination",name(nodes.get(selected).pos())));return;}
        travel(player,nodes.get(Math.floorMod(selected,nodes.size())),false);
    }
    public static String name(BlockPos p) {String[] a={"Amber","Moss","Silver","Hearth","Moon","Cinder","Willow","Lantern"},b={"deep","hollow","rest","haven","root","grove","vault","burrow"};long seed=RealmTerrain.hash(p.asLong());return a[Math.floorMod(seed,a.length)]+b[Math.floorMod(seed>>>16,b.length)];}
    public static boolean travel(ServerPlayer player,GlobalPos hall,boolean rescue) {
        var target=player.level().getServer().getLevel(hall.dimension());if(target==null || !target.getWorldBorder().isWithinBounds(hall.pos()) || player.isPassenger() && !rescue || player.isVehicle() && !rescue)return false;
        // Explicit travel/pop may load only the known landing neighborhood, never search/generate another town.
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)target.getChunk((hall.pos().getX()>>4)+x,(hall.pos().getZ()>>4)+z);
        if(!(target.getBlockEntity(hall.pos()) instanceof ProjectStation town && town.isTown())) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.blocked"));return false;}
        var landing=WarrenAnchorBlockEntity.safeLanding(target,hall.pos().offset(-12,0,-8),player);
        if(landing==null) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.guild.blocked"));return false;}
        if(rescue) {player.stopRiding();player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,900,1));player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,800,0));}
        if(player.teleport(new TeleportTransition(target,landing,Vec3.ZERO,player.getYRot(),player.getXRot(),TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)))==null)return false;
        player.getPersistentData().putLong("GuildTransitUntil",target.getGameTime()+80);player.resetFallDistance();player.clearFire();return true;
    }
    static void resident(ServerLevel level,BlockPos heart,int slot) {
        var center=heart.offset(-12,-1,-19);int[][] pads={{-31,-6},{31,-6},{-31,6},{31,6},{-28,28},{29,29},{-8,12},{8,12}};
        var mob=ModEntities.WARREN_CITIZEN.get().create(level,EntitySpawnReason.STRUCTURE);if(mob==null)return;
        mob.initialize(heart,slot%4);mob.snapTo(Vec3.atBottomCenterOf(center.offset(pads[slot][0],1,pads[slot][1])));
        if(!level.noCollision(mob) || !level.addFreshEntity(mob))mob.discard();
    }
}
