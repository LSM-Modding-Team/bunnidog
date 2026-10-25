package com.bunnidogs.mod.realm;

import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.item.ApexCrestItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Saved paired endpoints survive reconnect/death and never route through an unrelated player's portal. */
public final class WarrenAnchorBlockEntity extends BlockEntity {
    private ResourceKey<Level> destination;
    private BlockPos endpoint;
    private boolean busy;
    public WarrenAnchorBlockEntity(BlockPos pos,BlockState state) {super(ModBlocks.WARREN_ANCHOR_BE.get(),pos,state);}
    public boolean linked() {return destination!=null && endpoint!=null;}
    public ResourceKey<Level> destination() {return destination;}
    public BlockPos endpoint() {return endpoint;}
    public void link(ResourceKey<Level> dimension,BlockPos pos) {destination=dimension;endpoint=pos.immutable();setChanged();}
    private void message(Player player,String id) {player.sendOverlayMessage(Component.translatable("message.bunnidogs.realm."+id));}
    public void attune(Player player,InteractionHand hand) {
        if(busy || !StationUse.editable(player,this) || linked())return;
        var eye=player.getItemInHand(hand);var crest=player.getItemInHand(hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if(!eye.is(ModItems.RESONANT_EYE.get()) || !crest.is(ModItems.APEX_CREST.get())) {message(player,"crest");return;}
        if(!(level instanceof ServerLevel source) || GreatWarren.isRealm(source)) {message(player,"entry");return;}
        var realm=source.getServer().getLevel(GreatWarren.LEVEL);if(realm==null) {message(player,"unavailable");return;}
        var expectedEye=eye.copy();var expectedCrest=crest.copy();
        busy=true;
        try {
            var refuge=buildRefuge(realm,worldPosition);
            if(refuge==null || !StationUse.editable(player,this) || !net.minecraft.world.item.ItemStack.matches(eye,expectedEye) || !net.minecraft.world.item.ItemStack.matches(crest,expectedCrest) || player.getItemInHand(hand)!=eye || player.getItemInHand(hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)!=crest) {message(player,"blocked");return;}
            if(!(realm.getBlockEntity(refuge) instanceof WarrenAnchorBlockEntity back)) {message(player,"blocked");return;}
            back.link(source.dimension(),worldPosition);link(realm.dimension(),refuge);
            source.setBlock(worldPosition,getBlockState().setValue(WarrenAnchorBlock.ACTIVE,true),3);
            eye.consume(1,player);cross(player);
        } finally {busy=false;}
    }
    /** Generate only the bounded arrival neighborhood, then build into air above existing terrain. */
    public static BlockPos buildRefuge(ServerLevel realm,BlockPos near) {
        if(realm.getChunkSource().getGenerator() instanceof RealmChunkGenerator islands) {var site=islands.terrain().nearest(near.getX(),near.getZ());near=new BlockPos(site.x()+165,80,site.z());}
        int cx=(near.getX()>>4)*16+8,cz=(near.getZ()>>4)*16+8;
        if(!realm.getWorldBorder().isWithinBounds(new BlockPos(cx,80,cz)))return null;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)realm.getChunk((cx>>4)+x,(cz>>4)+z);
        for(int[] shift:new int[][]{{0,0},{16,0},{-16,0},{0,16},{0,-16}}) {
            int x=cx+shift[0],z=cz+shift[1],y=realm.getMinY()+2;
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)y=Math.max(y,realm.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,x+dx,z+dz));
            if(y>realm.getMaxY()-6)continue;
            BlockPos center=new BlockPos(x,y,z);boolean clear=true;
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=0;dy<=4;dy++) {
                var at=center.offset(dx,dy,dz);
                if(!realm.getWorldBorder().isWithinBounds(at) || !realm.getBlockState(at).isAir())clear=false;
            }
            if(!clear)continue;
            var ladder=ladderRoute(realm,center);if(ladder==null)continue;
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)realm.setBlock(center.offset(dx,0,dz),ModBlocks.ROOTSTONE.get().defaultBlockState(),3);
            for(int dx:new int[]{-3,3})for(int dz:new int[]{-3,3}) {
                for(int dy=1;dy<=3;dy++)realm.setBlock(center.offset(dx,dy,dz),ModBlocks.ROOTSTONE.get().defaultBlockState(),3);
                realm.setBlock(center.offset(dx,4,dz),Blocks.SEA_LANTERN.defaultBlockState(),3);
            }
            for(var cell:ladder.entrySet())realm.setBlock(cell.getKey(),cell.getValue(),3);
            BlockPos anchor=center.above();
            realm.setBlock(anchor,ModBlocks.WARREN_ANCHOR.get().defaultBlockState().setValue(WarrenAnchorBlock.ACTIVE,true).setValue(WarrenAnchorBlock.RETURN,true),3);
            return anchor;
        }
        return null;
    }
    /** A real way down and back up; never clears leaves, terrain, liquids or a player's construction. */
    private static java.util.Map<BlockPos,BlockState> ladderRoute(ServerLevel level,BlockPos floor) {
        for(var dir:net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var plan=new java.util.LinkedHashMap<BlockPos,BlockState>();
            for(int depth=0;depth<48 && floor.getY()-depth>level.getMinY();depth++) {
                var support=floor.relative(dir,3).below(depth);var rung=floor.relative(dir,4).below(depth);
                if(!level.hasChunkAt(rung) || !level.getWorldBorder().isWithinBounds(rung) || !level.getBlockState(rung).isAir()) {break;}
                var existing=level.getBlockState(support);
                if(existing.isAir())plan.put(support,ModBlocks.ROOTSTONE.get().defaultBlockState());
                else if(!existing.isFaceSturdy(level,support,dir)) {break;}
                plan.put(rung,Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING,dir));
                var below=rung.below();
                var bottom=level.getBlockState(below);
                if((bottom.isFaceSturdy(level,below,net.minecraft.core.Direction.UP) || bottom.is(net.minecraft.tags.BlockTags.LEAVES)
                        || bottom.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) && !bottom.is(Blocks.MAGMA_BLOCK))return plan;
            }
        }
        return null;
    }
    public void cross(Player player) {
        if(!(player instanceof ServerPlayer server) || !linked() || player.isPassenger() || player.isVehicle() || !player.isAlive() || player.isSpectator() || player.level()!=level || isRemoved() || level.getBlockEntity(worldPosition)!=this
                || !level.mayInteract(player,worldPosition) || player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>36)return;
        long now=server.level().getGameTime();long until=server.getPersistentData().getLongOr("WarrenCrossUntil",0);
        if(until>now && until<=now+40)return;
        var target=server.level().getServer().getLevel(destination);if(target==null) {message(player,"unavailable");return;}
        // Explicit portal use may load its saved endpoint neighborhood, never a distant search.
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)target.getChunk((endpoint.getX()>>4)+x,(endpoint.getZ()>>4)+z);
        Vec3 landing=safeLanding(target,endpoint,server);
        if(landing==null) {message(player,"blocked");return;}
        if(server.teleport(new TeleportTransition(target,landing,Vec3.ZERO,server.getYRot(),server.getXRot(),TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)))!=null)
            server.getPersistentData().putLong("WarrenCrossUntil",target.getGameTime()+20);
    }
    public static Vec3 safeLanding(ServerLevel level,BlockPos near,Player player) {
        for(int radius=1;radius<=8;radius++)for(int dy:new int[]{0,1,-1,2,-2,3,-3,4,-4})for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
            var feet=near.offset(dx,dy,dz);if(!level.hasChunkAt(feet) || !level.getWorldBorder().isWithinBounds(feet) || !level.getBlockState(feet.below()).isFaceSturdy(level,feet.below(),net.minecraft.core.Direction.UP))continue;
            var position=Vec3.atBottomCenterOf(feet);var box=player.getDimensions(net.minecraft.world.entity.Pose.STANDING).makeBoundingBox(position);
            if(level.noCollision(player,box) && !level.containsAnyLiquid(box) && !level.getBlockState(feet.below()).is(Blocks.MAGMA_BLOCK) && !level.getBlockState(feet.below()).is(Blocks.CAMPFIRE))return position;
        }
        return null;
    }
    @Override protected void saveAdditional(ValueOutput out) {super.saveAdditional(out);if(linked()) {out.putString("Destination",destination.identifier().toString());out.store("Endpoint",BlockPos.CODEC,endpoint);}}
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);destination=null;endpoint=null;
        var id=Identifier.tryParse(in.getStringOr("Destination",""));var pos=in.read("Endpoint",BlockPos.CODEC);
        if(id!=null && pos.isPresent()) {destination=ResourceKey.create(Registries.DIMENSION,id);endpoint=pos.get();}
    }
}
