package com.bunnidogs.mod.guild;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
public final class GuildVeilBlock extends Block {
    public GuildVeilBlock(Properties p){super(p);}
    private void enter(Level level,BlockPos pos,Player player,boolean direct) {
        if(!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer p) || player.isSpectator() || !player.isAlive())return;
        if(!direct && player.isShiftKeyDown())return;
        long until=p.getPersistentData().getLongOr("GuildTransitUntil",0);if(until>server.getGameTime())return;
        for(int y=1;y<=8;y++)for(int x=-3;x<=3;x++) {
            var at=pos.offset(x,-y,0);if(!server.hasChunkAt(at))continue;
            if(server.getBlockEntity(at) instanceof ProjectStation gate && gate.kind().equals("guild_gate")) {
                p.getPersistentData().putLong("GuildTransitUntil",server.getGameTime()+40);LanternTowns.gate(server,gate,p,direct);return;
            }
        }
    }
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e,InsideBlockEffectApplier effects,boolean precise) {if(e instanceof Player player)enter(l,p,player,false);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){enter(l,p,player,true);return InteractionResult.SUCCESS;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,net.minecraft.util.RandomSource random) {
        if(random.nextInt(3)==0)l.addParticle(net.minecraft.core.particles.ParticleTypes.PORTAL,p.getX()+random.nextDouble(),p.getY()+random.nextDouble(),p.getZ()+.5,(random.nextDouble()-.5)*.2,.1,0);
    }
}
