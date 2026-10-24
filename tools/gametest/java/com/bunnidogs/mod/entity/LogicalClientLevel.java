package com.bunnidogs.mod.entity;

import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;

/** Logical-client world for real entity notification/event dispatch, without loading a graphical client. */
final class LogicalClientLevel extends Level {
    private final ServerLevel source;
    LogicalClientLevel(ServerLevel source) {
        super((net.minecraft.world.level.storage.WritableLevelData)source.getLevelData(),source.dimension(),source.registryAccess(),source.dimensionTypeRegistration(),true,false,0,512);
        this.source=source;
    }
    @Override public void sendBlockUpdated(BlockPos pos,BlockState old,BlockState state,int flags) {}
    @Override public void playSeededSound(Entity except,double x,double y,double z,Holder<SoundEvent> sound,SoundSource category,float volume,float pitch,long seed) {}
    @Override public void playSeededSound(Entity except,Entity entity,Holder<SoundEvent> sound,SoundSource category,float volume,float pitch,long seed) {}
    @Override public void explode(Entity entity,net.minecraft.world.damagesource.DamageSource damage,net.minecraft.world.level.ExplosionDamageCalculator calculator,
            double x,double y,double z,float radius,boolean fire,ExplosionInteraction interaction,ParticleOptions small,ParticleOptions large,WeightedList<ExplosionParticleInfo> particles,Holder<SoundEvent> sound) {}
    @Override public String gatherChunkSourceStats() {return "logical client notification fixture";}
    @Override public void setRespawnData(LevelData.RespawnData data) {}
    @Override public LevelData.RespawnData getRespawnData() {return source.getRespawnData();}
    @Override public Entity getEntity(int id) {return null;}
    @Override public Collection<? extends net.neoforged.neoforge.entity.PartEntity<?>> dragonParts() {return List.of();}
    @Override public net.minecraft.world.TickRateManager tickRateManager() {return source.tickRateManager();}
    @Override public net.minecraft.world.level.saveddata.maps.MapItemSavedData getMapData(net.minecraft.world.level.saveddata.maps.MapId id) {return null;}
    @Override public void destroyBlockProgress(int id,BlockPos pos,int progress) {}
    @Override public net.minecraft.world.scores.Scoreboard getScoreboard() {return source.getScoreboard();}
    @Override public net.minecraft.world.item.crafting.RecipeAccess recipeAccess() {return source.recipeAccess();}
    @Override protected net.minecraft.world.level.entity.LevelEntityGetter<Entity> getEntities() {throw new AssertionError("Death notifications must not query server entities");}
    @Override public net.minecraft.world.clock.ClockManager clockManager() {return source.clockManager();}
    @Override public net.minecraft.world.attribute.EnvironmentAttributeSystem environmentAttributes() {return source.environmentAttributes();}
    @Override public net.minecraft.world.item.alchemy.PotionBrewing potionBrewing() {return source.potionBrewing();}
    @Override public net.minecraft.world.level.block.entity.FuelValues fuelValues() {return source.fuelValues();}
    @Override public net.minecraft.world.level.chunk.ChunkSource getChunkSource() {return source.getChunkSource();}
    @Override public List<net.minecraft.world.entity.player.Player> players() {return List.of();}
    @Override public net.minecraft.world.ticks.LevelTickAccess<net.minecraft.world.level.block.Block> getBlockTicks() {return source.getBlockTicks();}
    @Override public net.minecraft.world.ticks.LevelTickAccess<net.minecraft.world.level.material.Fluid> getFluidTicks() {return source.getFluidTicks();}
    @Override public Holder<net.minecraft.world.level.biome.Biome> getUncachedNoiseBiome(int x,int y,int z) {return source.getUncachedNoiseBiome(x,y,z);}
    @Override public void levelEvent(Entity entity,int event,BlockPos pos,int data) {}
    @Override public void gameEvent(Holder<net.minecraft.world.level.gameevent.GameEvent> event,net.minecraft.world.phys.Vec3 pos,net.minecraft.world.level.gameevent.GameEvent.Context context) {}
    @Override public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() {return source.enabledFeatures();}
    @Override public int getSeaLevel() {return source.getSeaLevel();}
    @Override public long nextSubTickCount() {return source.nextSubTickCount();}
    @Override public net.minecraft.world.level.border.WorldBorder getWorldBorder() {return source.getWorldBorder();}
    @Override public void close() {}
}
