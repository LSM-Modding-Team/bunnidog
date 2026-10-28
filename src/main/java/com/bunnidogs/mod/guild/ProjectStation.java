package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.entity.Bunnidog;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Shared native inventory, with deliberately different physical workshop jobs. */
public final class ProjectStation extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> stock=NonNullList.withSize(27,ItemStack.EMPTY);
    private UUID owner;
    private int cinderTicks,dustTicks,nurseryTicks;
    private boolean townStarted;
    private int residents;
    public ProjectStation(BlockPos pos,BlockState state) {super(GuildProjects.STATION.get(),pos,state);}
    public String kind() {return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
    public void owner(UUID id) {owner=id;setChanged();}
    public void generatedTown() {townStarted=true;setChanged();}
    public boolean isTown() {return townStarted && kind().equals("guild_heart");}
    public boolean powered() {return cinderTicks>0 && dustTicks>0 && level!=null && !level.hasNeighborSignal(worldPosition);}
    @Override protected NonNullList<ItemStack> getItems() {return stock;}
    @Override protected void setItems(NonNullList<ItemStack> items) {stock=items;}
    @Override public int getContainerSize() {return 27;}
    @Override protected Component getDefaultName() {return Component.translatable("block.bunnidogs."+kind());}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory) {
        return new com.bunnidogs.mod.menu.ProjectMenu(id,inventory,this,kind(),new ContainerData() {
            public int get(int index){return switch(index){case 0->java.util.Arrays.asList(com.bunnidogs.mod.menu.ProjectMenu.STATES).indexOf(state(inventory.player));case 1->cinderTicks;case 2->dustTicks;default->nurseryTicks;};}
            public void set(int i,int value){}
            public int getCount(){return 4;}
        });
    }
    @Override public void writeClientSideData(AbstractContainerMenu menu,net.minecraft.network.RegistryFriendlyByteBuf buffer) {((com.bunnidogs.mod.menu.ProjectMenu)menu).writeOpeningData(buffer);}
    public boolean workshop(){return kind().equals("pack_armoury") || kind().equals("resonance_bench");}
    public boolean binding(){return kind().equals("resonance_bench");}
    public int supplies(){return workshop() ? binding() ? 3 : 2 : 0;}
    public int output(){return binding() ? 2 : 1;}
    public WorkshopJob job(){return WorkshopJob.plan(kind(),stock.get(0),binding() ? stock.get(1) : ItemStack.EMPTY);}
    public String state(Player viewer) {
        if(workshop() && !stock.get(output()).isEmpty()) {var result=stock.get(output());return (binding() ? result.has(DataComponents.ENCHANTMENTS) && !result.is(Items.ENCHANTED_BOOK) : result.getItem() instanceof com.bunnidogs.mod.equipment.BardingItem) ? "ready" : "occupied";}
        if(level!=null && level.hasNeighborSignal(worldPosition))return "paused";
        if(workshop()) {var job=job();return !job.bill().isEmpty() && !has(job.bill(),supplies()) ? "materials" : job.state();}
        return switch(kind()) {
            case "bastion_heart" -> powered() ? "active" : "fuel";
            case "launch_aerie" -> !usableWings(viewer) ? "wings" : has(Map.of(ModItems.CINDERITE_INGOT.get(),2,ModItems.STARLIT_DUST.get(),1),0) ? "ready" : "materials";
            default -> nurseryTicks>0 ? "active" : has(Map.of(GuildProjects.material("nursery_bedding"),1,ModItems.GUAVA.get(),1),0) ? "shelter" : "materials";
        };
    }
    private static boolean usableWings(Player p) {var wings=p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);return wings.has(DataComponents.GLIDER) && wings.getDamageValue()<wings.getMaxDamage()-1;}
    public boolean launch(Player player) {
        if(!(level instanceof ServerLevel server) || !com.bunnidogs.mod.block.StationUse.editable(player,this) || !stillValid(player) || !kind().equals("launch_aerie") || server.hasNeighborSignal(worldPosition)
            || !usableWings(player) || !pay(Map.of(ModItems.CINDERITE_INGOT.get(),2,ModItems.STARLIT_DUST.get(),1),0))return false;
        player.setOnGround(false);player.setDeltaMovement(player.getDeltaMovement().add(0,2.2,0));player.hurtMarked=true;player.startFallFlying();
        server.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,player.getX(),player.getY(),player.getZ(),24,.6,.2,.6,.1);return true;
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) {
        if(!workshop())return true;
        if(slot==output())return false;
        if(slot==0)return binding() ? !stack.is(Items.ENCHANTED_BOOK) && stack.has(DataComponents.ENCHANTMENTS) : stack.getItem() instanceof com.bunnidogs.mod.equipment.BardingItem;
        return !binding() || slot!=1 || stack.is(Items.ENCHANTED_BOOK);
    }
    @Override public boolean stillValid(Player player) {return Container.stillValidBlockEntity(this,player) && canOpen(player);}
    public void use(Player player) {
        if(!(level instanceof ServerLevel server) || !com.bunnidogs.mod.block.StationUse.editable(player,this) || !stillValid(player))return;
        if(kind().equals("guild_gate")) {LanternTowns.gate(server,this,(ServerPlayer)player);return;}
        if(kind().equals("guild_heart")) {player.sendOverlayMessage(Component.literal(LanternTowns.name(worldPosition)));return;}
        if(kind().equals("launch_aerie") && !player.isShiftKeyDown() && launch(player))return;
        player.openMenu(this);
    }
    private boolean has(Map<Item,Integer> bill,int start) {
        for(var e:bill.entrySet()) {int n=0;for(int i=start;i<stock.size();i++)if(stock.get(i).is(e.getKey()))n+=stock.get(i).getCount();if(n<e.getValue())return false;}return true;
    }
    boolean pay(Map<Item,Integer> bill,int start) {
        if(!has(bill,start))return false;
        bill.forEach((item,count)->{int remaining=count;for(int i=start;i<stock.size() && remaining>0;i++)if(stock.get(i).is(item)) {int n=Math.min(remaining,stock.get(i).getCount());stock.get(i).shrink(n);remaining-=n;}});setChanged();return true;
    }
    public void operate(ServerLevel server) {
        if(server.hasNeighborSignal(worldPosition))return;
        switch(kind()) {
            case "pack_armoury" -> repair();
            case "resonance_bench" -> bind();
            case "bastion_heart" -> {
                if(cinderTicks<=0 && pay(Map.of(ModItems.CINDERITE_INGOT.get(),1),0))cinderTicks=1200;
                if(dustTicks<=0 && pay(Map.of(ModItems.STARLIT_DUST.get(),1),0))dustTicks=12000;
                if(powered()) {cinderTicks-=20;dustTicks-=20;setChanged();}
            }
            case "nursery_hearth" -> nurture(server);
            case "guild_heart" -> {
                if(!townStarted)return;
                LanternTowns.register(server,worldPosition);
                // Persist each finite attempt before calling the entity spawn hooks.
                if(residents<8 && server.hasChunksAt(worldPosition.offset(-58,-4,-72),worldPosition.offset(34,45,34))) {
                    int slot=residents++;setChanged();LanternTowns.resident(server,worldPosition,slot);
                }
            }
            default -> {}
        }
    }
    private void repair() {
        var job=job();if(!stock.get(output()).isEmpty() || job.bill().isEmpty() || !pay(job.bill(),supplies()))return;
        var suit=stock.get(0);suit.setDamageValue(Math.max(0,suit.getDamageValue()-120));stock.set(output(),suit);stock.set(0,ItemStack.EMPTY);setChanged();
    }
    private void bind() {
        var job=job();if(!stock.get(output()).isEmpty() || job.bill().isEmpty() || !pay(job.bill(),supplies()))return;
        var gear=stock.get(0);EnchantmentHelper.updateEnchantments(gear,mutable->job.upgrades().entrySet().forEach(e->mutable.set(e.getKey(),e.getIntValue())));
        stock.get(1).shrink(1);stock.set(output(),gear);stock.set(0,ItemStack.EMPTY);setChanged();
    }
    private void nurture(ServerLevel server) {
        var babies=server.getEntitiesOfClass(Bunnidog.class,new AABB(worldPosition).inflate(12),d->d.acceptsCare() && d.isAlive() && d.isBaby() && (owner==null || d.trail().keeper()==null || owner.equals(d.trail().keeper()))
            && server.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,d.getBlockX(),d.getBlockZ())>d.getY()+3
            && (d.getPersistentData().getLongOr("GuildNurseryUntil",0)<=server.getGameTime() || d.getPersistentData().getLongOr("GuildNurseryStation",0)==worldPosition.asLong()));
        if(babies.isEmpty())return;
        if(nurseryTicks<=0 && pay(Map.of(GuildProjects.material("nursery_bedding"),1,ModItems.GUAVA.get(),1),0))nurseryTicks=600;
        if(nurseryTicks<=0)return;
        for(var pup:babies) {pup.getPersistentData().putLong("GuildNurseryUntil",server.getGameTime()+80);pup.getPersistentData().putLong("GuildNurseryStation",worldPosition.asLong());}
        nurseryTicks-=20;if(nurseryTicks%60==0)for(var pup:babies) {
            long last=pup.getPersistentData().getLongOr("GuildNurseryAt",server.getGameTime()-60);
            if(server.getGameTime()>=last+60) {pup.setAge(Math.min(0,pup.getAge()+20));pup.getPersistentData().putLong("GuildNurseryAt",server.getGameTime());}
        }setChanged();
    }
    public static void tick(Level world,BlockPos pos,BlockState state,ProjectStation station) {if(world instanceof ServerLevel server && world.getGameTime()%20==0)station.operate(server);}
    @Override protected void saveAdditional(ValueOutput out) {super.saveAdditional(out);ContainerHelper.saveAllItems(out,stock);if(owner!=null)out.putString("Owner",owner.toString());out.putInt("CinderTicks",cinderTicks);out.putInt("DustTicks",dustTicks);out.putInt("NurseryTicks",nurseryTicks);out.putInt("Residents",residents);out.putBoolean("TownStarted",townStarted);}
    @Override protected void loadAdditional(ValueInput in) {super.loadAdditional(in);stock=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(in,stock);try{owner=UUID.fromString(in.getStringOr("Owner",""));}catch(IllegalArgumentException ignored){owner=null;}cinderTicks=Math.clamp(in.getIntOr("CinderTicks",0),0,1200);dustTicks=Math.clamp(in.getIntOr("DustTicks",0),0,12000);nurseryTicks=Math.clamp(in.getIntOr("NurseryTicks",0),0,600);residents=Math.clamp(in.getIntOr("Residents",0),0,8);townStarted=in.getBooleanOr("TownStarted",false);}
}
