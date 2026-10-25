package com.bunnidogs.mod.crown;

import com.bunnidogs.mod.block.StationUse;
import com.bunnidogs.mod.entity.*;
import com.bunnidogs.mod.registry.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Saved encounter ledger. Only confirmed deaths advance waves; unload/removal never masquerades as victory. */
public final class CrownSealBlockEntity extends BlockEntity {
    private UUID site,run;
    private BlockPos center=BlockPos.ZERO;
    private int sockets,wave,absent,missing;
    private boolean cleared,active,completed,busy,victoryPending,spawning,claiming;
    private final Set<UUID> enemies=new HashSet<>();
    private final Map<UUID,Float> contributions=new LinkedHashMap<>();
    private final Map<UUID,Integer> pending=new LinkedHashMap<>();
    private final Set<UUID> trophies=new HashSet<>();
    private final Map<UUID,UUID> trophyRuns=new HashMap<>();
    private int vaults,archives;
    public CrownSealBlockEntity(BlockPos p,BlockState s) {super(ModBlocks.CROWN_SEAL_BE.get(),p,s);}
    /** Safe on unfinished ProtoChunks: no live block updates or level dependency. */
    public void configure(UUID id,BlockPos origin) {if(site==null) {site=id;center=origin.immutable();setChanged();}}
    public UUID site() {return site;}
    public UUID run() {return run;}
    public boolean active() {return active;}
    public boolean spawning() {return spawning;}
    public boolean cleared() {return cleared;}
    public int sockets() {return sockets;}
    public int wave() {return wave;}
    public Set<UUID> enemies() {return Set.copyOf(enemies);}
    public float contribution(UUID player) {return contributions.getOrDefault(player,0F);}
    public boolean participant(UUID player) {return contributions.containsKey(player);}
    public int pending(UUID player) {return pending.getOrDefault(player,0);}
    public boolean hasRewards(UUID player) {return pending(player)>0 || trophies.contains(player);}
    public int wing() {return getBlockState().getValue(CrownSealBlock.WING);}
    public BlockPos center() {return center;}
    public Vec3 encounterCenter() {return Vec3.atBottomCenterOf(wing()==3 ? center.above() : worldPosition);}
    public boolean contains(Vec3 p,int margin) {
        var c=encounterCenter();int radius=wing()==3 ? 41 : 17;
        return Math.abs(p.x-c.x)<=radius+margin && Math.abs(p.z-c.z)<=radius+margin && p.y>=center.getY()-4 && p.y<=center.getY()+(wing()==3 ? 31 : 16)+margin;
    }
    private void message(Player p,String id,Object...args) {p.sendOverlayMessage(Component.translatable("message.bunnidogs.crown."+id,args));}
    public void use(Player p,InteractionHand hand) {
        if(busy || !(p instanceof ServerPlayer player) || !StationUse.editable(p,this) || site==null || player.isSpectator() || !p.isAlive())return;
        busy=true;
        try {
            var held=p.getItemInHand(hand);
            // Shown offhand keys/sigils retain priority over an ordinary held weapon.
            if(wing()==3 && hand==InteractionHand.MAIN_HAND && keyIndex(held)<0 && !held.is(ModItems.CROWN_SIGIL.get())
                    && (keyIndex(p.getOffhandItem())>=0 || p.getOffhandItem().is(ModItems.CROWN_SIGIL.get()))) {hand=InteractionHand.OFF_HAND;held=p.getOffhandItem();}
            if(wing()<3) {
                if(cleared) {giveKey(player);return;}
                if(active) {message(p,"active");return;}
                begin(player,hand);return;
            }
            int key=keyIndex(held);
            if(key>=0) {
                if(active) {message(p,"active");return;}
                if(!site.toString().equals(held.get(ModItems.CROWN_SITE.get()))) {message(p,"wrong_site");return;}
                int bit=1<<key;if((sockets&bit)!=0) {message(p,"socket_filled");return;}
                sockets|=bit;held.consume(1,p);
                if(Integer.bitCount(sockets)==3) {archives++;flushVaults();}
                state();openGate();message(p,"sockets",Math.min(2,Integer.bitCount(sockets)));return;
            }
            if(!held.is(ModItems.CROWN_SIGIL.get()) && hasRewards(player.getUUID())) {claim(player);return;}
            if(active) {message(p,"active");return;}
            if(Integer.bitCount(sockets)<2) {message(p,"sockets",Integer.bitCount(sockets));return;}
            if(completed && !held.is(ModItems.CROWN_SIGIL.get())) {message(p,"rematch");return;}
            begin(player,hand);
        } finally {busy=false;}
    }
    public static int keyIndex(ItemStack s) {for(int i=0;i<3;i++)if(s.is(ModItems.CROWN_KEYS.get(i).get()))return i;return -1;}
    private void giveKey(ServerPlayer p) {
        // Lost keys can be replaced, but the one-time wing cache never refills.
        var key=new ItemStack(ModItems.CROWN_KEYS.get(wing()).get());key.set(ModItems.CROWN_SITE.get(),site.toString());
        if(!p.getInventory().add(key))p.drop(key,false);message(p,"key");
    }
    public boolean begin(ServerPlayer player,InteractionHand hand) {
        boolean paid=wing()==3 && completed;
        if(active || site==null || level!=player.level() || !StationUse.editable(player,this) || wing()<3 && cleared || wing()==3 && Integer.bitCount(sockets)<2)return false;
        var offered=player.getItemInHand(hand);var expected=offered.copy();
        if(paid && !offered.is(ModItems.CROWN_SIGIL.get()))return false;
        var server=player.level();
        if(server.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL) {message(player,"peaceful");return false;}
        var at=BlockPos.containing(encounterCenter());int radius=wing()==3 ? 43 : 19;
        // Only an explicit encounter start may load this finite known neighborhood. Tickers never fetch chunks.
        for(int x=(at.getX()-radius)>>4;x<=((at.getX()+radius)>>4);x++)for(int z=(at.getZ()-radius)>>4;z<=((at.getZ()+radius)>>4);z++)server.getChunk(x,z);
        if(isRemoved() || server.getBlockEntity(worldPosition)!=this || !StationUse.editable(player,this))return false;

        run=UUID.randomUUID();wave=0;active=true;absent=missing=0;enemies.clear();contributions.clear();
        if(wing()==3) {
            // Freeze a small nearby roster at engagement; spectators/late arrivals cannot create free cores.
            contributions.put(player.getUUID(),0F);
            server.players().stream().filter(p->p!=player && p.isAlive() && !p.isSpectator() && !p.hasInfiniteMaterials() && contains(p.position(),7)).sorted(Comparator.comparingDouble(p->p.distanceToSqr(encounterCenter()))).limit(3).forEach(p->contributions.put(p.getUUID(),0F));
        }
        boolean spawned=spawnWave();
        if(!spawned || isRemoved() || server.getBlockEntity(worldPosition)!=this || !StationUse.editable(player,this) || player.getItemInHand(hand)!=offered || !ItemStack.matches(expected,offered)) {reset();message(player,"blocked");return false;}
        CrownTickets.hold(this);
        if(paid)offered.consume(1,player);
        state();message(player,wing()==3 ? "awakened" : "wing_name_"+wing());return true;
    }
    private boolean spawnWave() {
        if(spawning)return false;spawning=true;
        try {return spawnActors();}finally {spawning=false;}
    }
    private boolean spawnActors() {
        if(!(level instanceof ServerLevel server))return false;
        List<ApexKind> kinds;
        if(wing()==3)kinds=List.of(ApexKind.BRUTE);
        else if(wing()==0)kinds=wave==0 ? List.of(ApexKind.PRISM,ApexKind.BRUTE) : List.of(ApexKind.PRISM,ApexKind.PRISM);
        else if(wing()==1)kinds=wave==0 ? List.of(ApexKind.BROOD) : List.of(ApexKind.BROOD,ApexKind.BRUTE);
        else kinds=wave==0 ? List.of(ApexKind.SUPPORT,ApexKind.BRUTE) : List.of(ApexKind.BLAST);
        var made=new ArrayList<Apex>();
        for(int i=0;i<kinds.size();i++) {
            Apex mob=wing()==3 ? ModEntities.CROWNED_APEX.get().create(server,EntitySpawnReason.TRIGGERED)
                    : ModEntities.apex(kinds.get(i),wing()==0 && wave==1 && i==1).create(server,EntitySpawnReason.TRIGGERED);
            if(mob==null) {made.forEach(Entity::discard);return false;}
            boolean placed=false;
            for(int[] d:new int[][]{{-7,7},{7,-7},{7,7},{-7,-7},{0,-10},{10,0}}) {
                var pos=encounterCenter().add(d[0],mob.flying() ? 4 : 0,d[1]);mob.snapTo(pos);
                var bounds=mob.getBoundingBox();
                if(!server.hasChunksAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ)))continue;
                if(!server.getBlockState(BlockPos.containing(pos).below(mob.flying() ? 4 : 0).below()).isFaceSturdy(server,BlockPos.containing(pos).below(mob.flying() ? 4 : 0).below(),net.minecraft.core.Direction.UP))continue;
                if(server.noCollision(mob) && !server.containsAnyLiquid(mob.getBoundingBox())) {placed=true;break;}
            }
            if(!placed) {mob.discard();made.forEach(Entity::discard);return false;}
            CrownEncounter.bind(mob,this);if(mob instanceof CrownedApex crowned)crowned.prepare(center,contributions.size());
            // Reserve identity before native callbacks can re-enter the controller.
            enemies.add(mob.getUUID());
            if(!server.addFreshEntity(mob) || !mob.isAlive() || isRemoved() || server.getBlockEntity(worldPosition)!=this) {mob.discard();made.forEach(Entity::discard);return false;}
            made.add(mob);
        }
        setChanged();return true;
    }
    public void credit(UUID player,float paid) {
        if(active && contributions.containsKey(player) && Float.isFinite(paid) && paid>0) {contributions.computeIfPresent(player,(id,value)->Math.min(100000,value+paid));setChanged();}
    }
    public void defeated(Apex mob) {
        if(!active || spawning || !mob.isDeadOrDying() || !enemies.remove(mob.getUUID()))return;
        if(wing()==3) {victoryPending=true;setChanged();return;}
        if(enemies.isEmpty()) {
            wave++;
            if(wave<2) {if(!spawnWave())reset();}
            else {active=false;cleared=true;CrownTickets.release(this);if(((ServerLevel)level).getGameRules().get(GameRules.MOB_DROPS))vaults++;state();flushVaults();}
        }
        setChanged();
    }
    /** Native Post damage is later than die(); defer resolution so the killing hit participates too. */
    public void resolveVictory() {if(active && victoryPending) {victoryPending=false;victory();}}
    private void victory() {
        active=false;completed=true;cleared=true;CrownTickets.release(this);
        boolean rewards=((ServerLevel)level).getGameRules().get(GameRules.MOB_DROPS);
        if(rewards)for(var entry:contributions.entrySet())if(entry.getValue()>=30) {
            pending.merge(entry.getKey(),1,Integer::sum);trophies.add(entry.getKey());trophyRuns.putIfAbsent(entry.getKey(),run);
            var p=((ServerLevel)level).getServer().getPlayerList().getPlayer(entry.getKey());
            if(p!=null) {message(p,"victory");var a=((ServerLevel)level).getServer().getAdvancements().get(net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs","crown_of_the_warren"));if(a!=null)p.getAdvancements().award(a,"defeated");}
        }
        if(rewards) {vaults++;archives++;}
        CrownedApex.cleanup((ServerLevel)level,run,encounterCenter());state();flushVaults();
    }
    public void claim(ServerPlayer player) {
        if(claiming || !StationUse.editable(player,this))return;claiming=true;
        try {
        UUID id=player.getUUID();int count=pending(id);if(!hasRewards(id))return;
        // Entitlement is saved before inventory/drop callbacks; failure returns it.
        if(count>0) {
            if(count==1)pending.remove(id);else pending.put(id,count-1);setChanged();
            if(!deliver(player,new ItemStack(ModItems.SOVEREIGN_CORE.get()))) {pending.put(id,count);setChanged();return;}
        }
        if(trophies.remove(id)) {
            var trophy=new ItemStack(ModItems.CROWN_TROPHY.get());trophy.set(ModItems.CROWN_SITE.get(),site.toString());
            var provenance=new net.minecraft.nbt.CompoundTag();provenance.putString("CrownVictory",trophyRuns.getOrDefault(id,run).toString());trophy.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(provenance));
            if(!deliver(player,trophy))trophies.add(id);else trophyRuns.remove(id);
        }
        setChanged();
        } finally {claiming=false;}
    }
    private boolean deliver(ServerPlayer player,ItemStack stack) {
        if(player.getInventory().add(stack) || stack.isEmpty())return true;
        var dropped=player.drop(stack,false);return dropped!=null && player.level().getEntity(dropped.getUUID())==dropped;
    }
    private void state() {
        setChanged();if(level!=null && !isRemoved() && level.getBlockEntity(worldPosition)==this)level.setBlock(worldPosition,getBlockState().setValue(CrownSealBlock.STATE,active ? 1 : cleared ? 2 : 0).setValue(CrownSealBlock.SOCKETS,sockets),3);
    }
    private void openGate() {
        if(wing()!=3 || Integer.bitCount(sockets)<2 || !(level instanceof ServerLevel server))return;
        for(int x=-3;x<=3;x++)for(int y=1;y<=6;y++) {var p=center.offset(x,y,42);if(server.hasChunkAt(p) && server.getBlockState(p).is(ModBlocks.CROWN_GATE.get()))server.setBlock(p,Blocks.AIR.defaultBlockState(),3);}
    }
    public void reset() {
        CrownTickets.release(this);
        if(level instanceof ServerLevel server && run!=null) {
            for(UUID id:Set.copyOf(enemies)) {var e=server.getEntity(id);if(e!=null)e.discard();}
            CrownedApex.cleanup(server,run,encounterCenter());
        }
        active=false;victoryPending=false;enemies.clear();contributions.clear();wave=0;absent=missing=0;state();
    }
    public void tick() {
        if(!(level instanceof ServerLevel server) || server.getGameTime()%10!=0 || site==null)return;
        if(wing()==3)openGate();if((vaults>0 || archives>0) && server.getGameTime()%40==0)flushVaults();
        if(victoryPending) {resolveVictory();return;}
        if(!active)return;
        if(server.getGameTime()%20==0)CrownTickets.hold(this);
        boolean players=server.players().stream().anyMatch(p->p.isAlive() && !p.isSpectator() && !p.hasInfiniteMaterials() && contains(p.position(),7));
        absent=players ? 0 : absent+10;
        boolean lost=enemies.stream().anyMatch(id->{var e=server.getEntity(id);return e==null || e.isRemoved();});
        missing=lost ? missing+10 : 0;
        if(absent>=200 || missing>=200 || server.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)reset();
    }
    private void flushVaults() {
        if(!(level instanceof ServerLevel server) || vaults<=0 && archives<=0)return;
        // Finished rooms pay into empty plinths only. Player containers are never replaced/refilled.
        var root=wing()==3 ? center.offset(0,1,31) : worldPosition;
        for(int i=0;i<6 && (vaults>0 || archives>0);i++) {
            var pos=root.offset(-6+i*2,0,wing()==3 ? 2 : -12);
            if(!server.hasChunkAt(pos) || !server.getBlockState(pos).isAir())continue;
            boolean archive=vaults==0;
            if(archive)archives--;else vaults--;setChanged();
            if(!server.setBlock(pos,Blocks.CHEST.defaultBlockState(),3) || !(server.getBlockEntity(pos) instanceof ChestBlockEntity chest)) {if(archive)archives++;else vaults++;setChanged();continue;}
            if(archive)CrownArchives.fill(chest,server.getRandom().nextLong(),site);
            else {
                chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs",wing()==3 ? "chests/crown_vault" : "chests/crown_wing")));
                chest.setLootTableSeed(server.getRandom().nextLong());chest.setChanged();
            }
        }
    }
    private static UUID uuid(String s) {try {return UUID.fromString(s);}catch(IllegalArgumentException e) {return null;}}
    private static List<String> ids(Collection<UUID> values) {return values.stream().map(UUID::toString).toList();}
    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);if(site==null)return;
        out.putString("Site",site.toString());out.store("Center",BlockPos.CODEC,center);if(run!=null)out.putString("Run",run.toString());
        out.putInt("Sockets",sockets);out.putInt("Wave",wave);out.putBoolean("Cleared",cleared);out.putBoolean("Completed",completed);out.putBoolean("Active",active);out.putInt("Vaults",vaults);out.putInt("Archives",archives);out.putBoolean("VictoryPending",victoryPending);
        var provenance=new LinkedHashMap<String,String>();trophyRuns.forEach((id,v)->provenance.put(id.toString(),v.toString()));out.store("TrophyRuns",Codec.unboundedMap(Codec.STRING,Codec.STRING),provenance);
        out.store("Enemies",Codec.STRING.listOf(),ids(enemies));out.store("Trophies",Codec.STRING.listOf(),ids(trophies));
        var credits=new LinkedHashMap<String,Float>();contributions.forEach((id,n)->credits.put(id.toString(),n));out.store("Credits",Codec.unboundedMap(Codec.STRING,Codec.FLOAT),credits);
        var rewards=new LinkedHashMap<String,Integer>();pending.forEach((id,n)->rewards.put(id.toString(),n));out.store("Rewards",Codec.unboundedMap(Codec.STRING,Codec.INT),rewards);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);site=uuid(in.getStringOr("Site",""));run=uuid(in.getStringOr("Run",""));center=in.read("Center",BlockPos.CODEC).orElse(BlockPos.ZERO);
        sockets=in.getIntOr("Sockets",0)&7;wave=Math.clamp(in.getIntOr("Wave",0),0,2);cleared=in.getBooleanOr("Cleared",false);completed=in.getBooleanOr("Completed",false);active=in.getBooleanOr("Active",false)&&run!=null;vaults=Math.max(0,in.getIntOr("Vaults",0));
        archives=Math.max(0,in.getIntOr("Archives",0));
        victoryPending=in.getBooleanOr("VictoryPending",false) && active && wing()==3;
        enemies.clear();for(String s:in.read("Enemies",Codec.STRING.listOf()).orElse(List.of())) {var id=uuid(s);if(id!=null)enemies.add(id);}
        trophies.clear();for(String s:in.read("Trophies",Codec.STRING.listOf()).orElse(List.of())) {var id=uuid(s);if(id!=null)trophies.add(id);}
        trophyRuns.clear();in.read("TrophyRuns",Codec.unboundedMap(Codec.STRING,Codec.STRING)).orElse(Map.of()).forEach((s,v)->{var id=uuid(s);var encounter=uuid(v);if(id!=null && encounter!=null)trophyRuns.put(id,encounter);});
        contributions.clear();in.read("Credits",Codec.unboundedMap(Codec.STRING,Codec.FLOAT)).orElse(Map.of()).forEach((s,n)->{var id=uuid(s);if(id!=null && Float.isFinite(n) && contributions.size()<4)contributions.put(id,Math.clamp(n,0,100000));});
        pending.clear();in.read("Rewards",Codec.unboundedMap(Codec.STRING,Codec.INT)).orElse(Map.of()).forEach((s,n)->{var id=uuid(s);if(id!=null && n>0)pending.put(id,Math.min(n,10000));});
    }
}
