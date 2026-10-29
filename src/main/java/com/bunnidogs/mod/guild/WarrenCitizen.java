package com.bunnidogs.mod.guild;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.genetics.*;
import com.bunnidogs.mod.registry.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;

/** Peaceful guild adults: inherited strong minds, personal professions and finite physical trade stock. */
public final class WarrenCitizen extends Bunnidog implements Merchant {
    private BlockPos hall=BlockPos.ZERO;
    private int profession,workClock;
    private Player customer;
    private MerchantOffers offers=new MerchantOffers();
    private NonNullList<ItemStack> goods=NonNullList.withSize(27,ItemStack.EMPTY);
    public WarrenCitizen(EntityType<? extends WarrenCitizen> type,Level level) {super(type,level);setPersistenceRequired();setAge(0);}
    public static AttributeSupplier.Builder attributes() {return Bunnidog.createAttributes().add(Attributes.MAX_HEALTH,80).add(Attributes.SCALE,1.8).add(Attributes.MOVEMENT_SPEED,.27);}
    @Override protected void applyGenetics() {
        float fraction=getMaxHealth()>0 ? getHealth()/getMaxHealth() : 1;
        super.applyGenetics();getAttribute(Attributes.MAX_HEALTH).setBaseValue(180*genome().traits().healthMultiplier());
        getAttribute(Attributes.ARMOR).setBaseValue(8);getAttribute(Attributes.SCALE).setBaseValue(2.2);setHealth(getMaxHealth()*fraction);
    }
    public void initialize(BlockPos home,int job) {
        hall=home.immutable();profession=Math.clamp(job,0,3);var rng=new Random(getUUID().getLeastSignificantBits());var genes=Genome.randomFounder(Lineage.CAVE,rng);
        for(var gene:CognitiveGene.values())for(int slot=0;slot<2;slot++)genes=genes.with(gene,slot,gene.alleles().size()-1);
        for(int slot=0;slot<2;slot++)genes=genes.with(Gene.TEMPERAMENT,slot,0);
        setGenome(genes);setStats(Stats.of(45,35,40,40,20));setHealth(getMaxHealth());
        setCustomName(Component.translatable("entity.bunnidogs.guild_job_"+profession));
        add(new ItemStack(ModItems.STARLIT_DUST.get(),64));add(new ItemStack(ModItems.PRISM_CELL.get(),32));
        add(new ItemStack(GuildProjects.material("nursery_bedding"),24));add(new ItemStack(GuildProjects.material("field_dressing"),16));
        add(new ItemStack(ModItems.MOONSTONE.get(),64));add(new ItemStack(ModItems.SINEW.get(),32));
        add(new ItemStack(ModItems.CINDERITE_INGOT.get(),64));add(new ItemStack(GuildProjects.material("root_fibre"),64));
        if(profession==3) {add(LanternTowns.totem(GlobalPos.of(level().dimension(),hall)));add(LanternTowns.totem(GlobalPos.of(level().dimension(),hall)));}
        rebuild();
    }
    @Override protected boolean considersEntityAsAlly(Entity other) {
        if(other instanceof Player && getLastHurtByMob()!=other)return true;
        if(other instanceof WarrenCitizen)return true;
        return super.considersEntityAsAlly(other);
    }
    @Override public boolean acceptsCare() {return false;}
    @Override public boolean canBeLeashed() {return false;}
    @Override public boolean canEat(ItemStack s) {return false;}
    @Override public boolean isFood(ItemStack s) {return false;}
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new PanicGoal(this,1.2));goalSelector.addGoal(3,new LookAtPlayerGoal(this,Player.class,9));
        goalSelector.addGoal(4,new RandomLookAroundGoal(this));
    }
    private void add(ItemStack stack) {
        for(int i=0;i<goods.size() && !stack.isEmpty();i++) {
            var old=goods.get(i);if(old.isEmpty()) {goods.set(i,stack.copy());stack.setCount(0);return;}
            if(ItemStack.isSameItemSameComponents(old,stack)) {int n=Math.min(old.getMaxStackSize()-old.getCount(),stack.getCount());old.grow(n);stack.shrink(n);}
        }
        if(!stack.isEmpty() && level() instanceof ServerLevel server)spawnAtLocation(server,stack);
    }
    private int count(ItemStack type) {int n=0;for(var s:goods)if(ItemStack.isSameItemSameComponents(s,type))n+=s.getCount();return n;}
    private void take(ItemStack type) {int n=type.getCount();for(var s:goods)if(ItemStack.isSameItemSameComponents(s,type)) {int pay=Math.min(n,s.getCount());s.shrink(pay);n-=pay;if(n==0)return;}}
    private void offer(Item a,int amount,ItemStack out,Item b,int second) {
        int stock=count(out)/out.getCount();if(stock==0)return;
        offers.add(new MerchantOffer(new ItemCost(a,amount),b==null ? Optional.empty() : Optional.of(new ItemCost(b,second)),out,Math.min(12,stock),2,0));
    }
    private void rebuild() {
        offers=new MerchantOffers();
        switch(profession) {
            case 0 -> {offer(ModItems.CINDERITE_INGOT.get(),48,new ItemStack(GuildProjects.material("structural_frame")),GuildProjects.material("root_fibre"),32);offer(ModItems.MOONSTONE.get(),48,new ItemStack(GuildProjects.material("root_fibre"),32),null,0);offer(ModItems.RAW_CINDERITE.get(),12,new ItemStack(ModItems.CINDERITE_INGOT.get(),12),GuildProjects.material("root_fibre"),4);}
            case 1 -> {offer(ModItems.CINDERITE_INGOT.get(),24,new ItemStack(GuildProjects.material("field_dressing"),4),ModItems.SINEW.get(),8);offer(ModItems.CINDERITE_INGOT.get(),16,new ItemStack(ModItems.PRISM_CELL.get(),16),ModItems.STARLIT_DUST.get(),16);}
            case 2 -> {offer(ModItems.CINDERITE_INGOT.get(),8,new ItemStack(Items.BREAD,16),null,0);offer(ModItems.SINEW.get(),16,new ItemStack(ModItems.STARLIT_DUST.get(),16),GuildProjects.material("root_fibre"),16);offer(GuildProjects.material("root_fibre"),24,new ItemStack(GuildProjects.material("nursery_bedding"),8),ModItems.GUAVA.get(),8);offer(ModItems.CINDERITE_INGOT.get(),24,new ItemStack(ModItems.STARLIT_DUST.get(),16),null,0);}
            case 3 -> {offer(Items.TOTEM_OF_UNDYING,1,LanternTowns.totem(GlobalPos.of(level().dimension(),hall)),ModItems.ASTRALITE_SHARD.get(),48);offer(ModItems.CINDERITE_INGOT.get(),16,new ItemStack(ModItems.PRISM_CELL.get(),16),ModItems.STARLIT_DUST.get(),16);}
        }
    }
    @Override public void tick() {
        super.tick();if(!(level() instanceof ServerLevel server) || !isAlive())return;
        if(customer!=null) {if(!stillValid(customer) || !(customer.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu))customer=null;else {getNavigation().stop();getLookControl().setLookAt(customer,30,30);return;}}
        if(++workClock%80!=0 || isNoAi())return;
        var center=hall.offset(-12,-1,-19);long day=server.getGameTime()%24000;boolean resting=day>12500 && day<22500;
        BlockPos crop=profession==2 && !resting ? crop(server,center) : null;
        BlockPos target=crop!=null ? crop : resting ? center.offset(profession<2 ? -32 : 32,10,profession*6-9) : center.offset(profession%2==0 ? -31 : 31,1,profession<2 ? -9 : 12);
        if(!server.hasChunksAt(blockPosition(),target))return;
        if(distanceToSqr(net.minecraft.world.phys.Vec3.atBottomCenterOf(target))>5)getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,.75);
        else if(crop!=null)harvest(server,crop);
        else if(!resting && workClock%400==0) {manufacture();rebuild();server.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,getX(),getEyeY(),getZ(),2,.3,.2,.3,0);}
    }
    private BlockPos crop(ServerLevel server,BlockPos center) {
        if(!(server.getBlockEntity(hall) instanceof ProjectStation town) || !town.isTown())return null;
        BlockPos nearest=null;double best=Double.POSITIVE_INFINITY;
        for(int x=-40;x<=-20;x++)for(int z=26;z<=27;z++) {
            var p=center.offset(x,1,z);if(!server.hasChunkAt(p))continue;var s=server.getBlockState(p);
            if(!s.is(net.minecraft.world.level.block.Blocks.WHEAT) || s.getValue(net.minecraft.world.level.block.CropBlock.AGE)<7)continue;
            double d=distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p));if(d<best){best=d;nearest=p;}
        }
        return nearest;
    }
    private void harvest(ServerLevel server,BlockPos p) {
        if(distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(p))>5 || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(server,this))return;
        var state=server.getBlockState(p);if(!state.is(net.minecraft.world.level.block.Blocks.WHEAT) || state.getValue(net.minecraft.world.level.block.CropBlock.AGE)!=7 || !net.neoforged.neoforge.event.EventHooks.onEntityDestroyBlock(this,p,state) || server.getBlockState(p)!=state)return;
        var drops=net.minecraft.world.level.block.Block.getDrops(state,server,p,null,this,ItemStack.EMPTY);boolean seed=false;
        for(var stack:drops)if(stack.is(Items.WHEAT_SEEDS) && !stack.isEmpty()) {stack.shrink(1);seed=true;break;}
        if(!seed)return; // Leave the crop until a real replanting seed is available.
        if(server.setBlock(p,state.setValue(net.minecraft.world.level.block.CropBlock.AGE,0),3))drops.forEach(this::add);
    }
    private void craft(java.util.Map<Item,Integer> inputs,ItemStack output) {
        if(count(output)>=output.getMaxStackSize()*2)return;
        for(var e:inputs.entrySet())if(count(new ItemStack(e.getKey()))<e.getValue())return;
        inputs.forEach((item,n)->take(new ItemStack(item,n)));add(output);
    }
    private void manufacture() {
        if(profession==0)craft(java.util.Map.of(ModItems.CINDERITE_INGOT.get(),36,ModItems.MOONSTONE.get(),18,GuildProjects.material("root_fibre"),18),new ItemStack(GuildProjects.material("structural_frame")));
        if(profession==2) {
            craft(java.util.Map.of(GuildProjects.material("root_fibre"),4,ModItems.SINEW.get(),2,ModItems.STARLIT_DUST.get(),1),new ItemStack(GuildProjects.material("nursery_bedding"),4));
            craft(java.util.Map.of(Items.WHEAT,3),new ItemStack(Items.BREAD));
        }
        ItemStack cinder=new ItemStack(ModItems.CINDERITE_INGOT.get(),4),dust=new ItemStack(ModItems.STARLIT_DUST.get(),8);
        if(profession==1 && count(cinder)>=4 && count(dust)>=8) {take(cinder);take(dust);add(new ItemStack(ModItems.PRISM_CELL.get(),8));}
        if(profession==3) {
            var totem=new ItemStack(Items.TOTEM_OF_UNDYING);var charge=new ItemStack(ModItems.PRISM_CELL.get(),8);var d=new ItemStack(ModItems.STARLIT_DUST.get(),16);
            if(count(totem)>0 && count(charge)>=8 && count(d)>=16) {take(totem);take(charge);take(d);add(LanternTowns.totem(GlobalPos.of(level().dimension(),hall)));}
        }
    }
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand) {
        if(player.isSpectator() || !isAlive() || customer!=null && customer!=player)return InteractionResult.PASS;
        if(!level().isClientSide()) {setTradingPlayer(player);openTradingScreen(player,getDisplayName(),1);}return InteractionResult.SUCCESS;
    }
    @Override public void setTradingPlayer(Player p) {customer=p;}
    @Override public Player getTradingPlayer() {return customer;}
    @Override public MerchantOffers getOffers() {return offers;}
    @Override public void overrideOffers(MerchantOffers value) {offers=value;}
    @Override public void notifyTrade(MerchantOffer offer) {
        take(offer.getResult());add(offer.getCostA().copy());add(offer.getCostB().copy());offer.increaseUses();playSound(getNotifyTradeSound(),.7F,1);
    }
    @Override public void notifyTradeUpdated(ItemStack s) {}
    @Override public int getVillagerXp() {return 0;}
    @Override public void overrideXp(int n) {}
    @Override public boolean showProgressBar() {return false;}
    @Override public SoundEvent getNotifyTradeSound() {return SoundEvents.VILLAGER_YES;}
    @Override public boolean isClientSide() {return level().isClientSide();}
    @Override public boolean stillValid(Player p) {return isAlive() && !p.isSpectator() && level()==p.level() && distanceToSqr(p)<64;}
    @Override public void addAdditionalSaveData(ValueOutput out) {super.addAdditionalSaveData(out);out.store("GuildHall",BlockPos.CODEC,hall);out.putInt("GuildJob",profession);out.store("GuildOffers",MerchantOffers.CODEC,offers);ContainerHelper.saveAllItems(out,goods);}
    @Override public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);hall=in.read("GuildHall",BlockPos.CODEC).orElse(blockPosition());profession=Math.clamp(in.getIntOr("GuildJob",0),0,3);offers=in.read("GuildOffers",MerchantOffers.CODEC).orElseGet(MerchantOffers::new);
        // Retire the old wood-for-mineral shortcut without refilling goods or resetting other offers' uses.
        offers.removeIf(o->o.getResult().is(ModItems.CINDERITE_INGOT.get()) && o.getCostA().is(GuildProjects.material("root_fibre")) && o.getCostB().isEmpty());
        goods=NonNullList.withSize(27,ItemStack.EMPTY);ContainerHelper.loadAllItems(in,goods);customer=null;
    }
}
