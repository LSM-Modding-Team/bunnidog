package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.crown.*;
import com.bunnidogs.mod.registry.*;
import com.bunnidogs.mod.expedition.ExpeditionGear;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Gameplay-level encounter transactions, native boss health/attacks and component-preserving rewards. */
final class CrownDungeonGameTest {
    static BlockPos origin(GameTestHelper h) {return h.absolutePos(new BlockPos(56,179,56));}
    static void floor(GameTestHelper h) {for(int x=8;x<=104;x++)for(int z=8;z<=104;z++)h.setBlock(x,179,z,ModBlocks.ROOTSTONE.get());}
    static CrownSealBlockEntity seal(GameTestHelper h,int wing,BlockPos pos,BlockPos center,UUID site) {
        h.getLevel().setBlock(pos,ModBlocks.CROWN_SEAL.get().defaultBlockState().setValue(CrownSealBlock.WING,wing),3);
        var be=(CrownSealBlockEntity)h.getLevel().getBlockEntity(pos);be.configure(site,center);return be;
    }
    static ItemStack key(int wing,UUID site) {var s=new ItemStack(ModItems.CROWN_KEYS.get(wing).get());s.set(ModItems.CROWN_SITE.get(),site.toString());return s;}
    static void unlock(GameTestHelper h,CrownSealBlockEntity seal,ServerPlayer p) {
        for(int i=0;i<2;i++) {p.setItemInHand(InteractionHand.MAIN_HAND,key(i,seal.site()));seal.use(p,InteractionHand.MAIN_HAND);}
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
    }
    static CrownSealBlockEntity copy(GameTestHelper h,CrownSealBlockEntity be) {
        var copy=(CrownSealBlockEntity)BlockEntity.loadStatic(be.getBlockPos(),be.getBlockState(),be.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());h.getLevel().setBlockEntity(copy);return copy;
    }
    static void kill(GameTestHelper h,LivingEntity mob,ServerPlayer p) {mob.setInvulnerable(false);mob.invulnerableTime=0;mob.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),20000);h.assertTrue(mob.isDeadOrDying(),"The actual accepted lethal hit must kill its encounter member");}
    static void wings(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,0,c.above(),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(2,1,0)));
        h.assertTrue(be.begin(p,InteractionHand.MAIN_HAND) && be.enemies().size()==4,"A physical wing seal starts its finite first encounter");
        for(var id:be.enemies()) {var e=(Apex)h.getLevel().getEntity(id);e.setNoAi(true);}
        var champion=(Apex)h.getLevel().getEntity(be.enemies().iterator().next());
        var ordinary=ModEntities.apex(champion.kind(),champion.flying()).create(h.getLevel(),EntitySpawnReason.COMMAND);
        ordinary.setGenome(champion.genome());ordinary.setStats(champion.stats());ordinary.setNoAi(true);
        h.assertTrue(Math.abs(champion.getMaxHealth()-Math.min(960,ordinary.getMaxHealth()*2))<.1 && Math.abs(champion.getAttributeValue(Attributes.ATTACK_DAMAGE)-ordinary.getAttributeValue(Attributes.ATTACK_DAMAGE)*1.6)<.001,"A dungeon champion is stronger than the same ordinary genetic line, without changing wild rules");
        float maximum=champion.getMaxHealth();champion.setHealth(maximum/3);champion.tick();
        var savedActor=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());champion.saveWithoutId(savedActor);
        var restoredActor=ModEntities.apex(champion.kind(),champion.flying()).create(h.getLevel(),EntitySpawnReason.LOAD);restoredActor.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),savedActor.buildResult()));restoredActor.tick();
        h.assertTrue(Math.abs(restoredActor.getMaxHealth()-maximum)<.1 && Math.abs(restoredActor.getHealth()-maximum/3)<.1 && com.bunnidogs.mod.equipment.Barding.tier(restoredActor)==4 && !restoredActor.getAttribute(Attributes.ATTACK_DAMAGE).hasModifier(CrownBalance.DAMAGE),"Enrage and native actor reload retain champion bonuses and wounds without stacking or refilling health");
        ordinary.discard();restoredActor.discard();
        var first=h.getLevel().getEntity(be.enemies().iterator().next());first.discard();
        h.assertTrue(be.active() && be.wave()==0 && !be.cleared(),"Discarding/unloading an actor never counts as a confirmed kill");be.reset();
        be.begin(p,InteractionHand.MAIN_HAND);
        var saved=copy(h,be);h.assertTrue(saved.active() && saved.enemies().size()==4,"A native controller save retains exact encounter actor identities");
        for(int wave=0;wave<2;wave++)for(var id:saved.enemies())kill(h,(LivingEntity)h.getLevel().getEntity(id),p);
        h.assertTrue(saved.cleared() && !saved.active() && saved.wave()==2,"Only both finite waves unlock their keystone");
        int chests=h.getLevel().getBlockState(c.offset(-6,1,-12)).is(Blocks.CHEST) ? 1 : 0;h.assertTrue(chests==1,"Wing completion pays a physical generic-supply cache");
        saved.use(p,InteractionHand.MAIN_HAND);
        boolean found=false;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModItems.CROWN_KEYS.getFirst().get()))found=saved.site().toString().equals(p.getInventory().getItem(i).get(ModItems.CROWN_SITE.get()));
        h.assertTrue(found,"The recovered physical key carries the exact dungeon identity");
        var chest=(ChestBlockEntity)h.getLevel().getBlockEntity(c.offset(-6,1,-12));chest.unpackLootTable(null);chest.clearContent();
        saved=copy(h,saved);saved.use(p,InteractionHand.MAIN_HAND);h.assertTrue(chest.isEmpty() && saved.cleared(),"Replacement keys and native reload cannot refill the one-time cache");saved.reset();p.discard();
    }
    static void balance(GameTestHelper h) {
        floor(h);var c=origin(h);var seal=seal(h,1,c.above(),c,UUID.randomUUID());
        var party=new ArrayList<ServerPlayer>();for(int i=0;i<4;i++)party.add(ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(2+i,1,2))));
        h.assertTrue(seal.begin(party.getFirst(),InteractionHand.MAIN_HAND) && seal.enemies().size()==4,"The initial Brood trial is a four-Apex combined-arms fight");
        var late=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(-2,1,2)));seal=copy(h,seal);
        h.assertTrue(!seal.participant(late.getUUID()) && party.stream().allMatch(p->sealParticipant(h,c,p)),"Wing saves freeze the nearby party; late visitors cannot rescale the fight");
        for(int i=1;i<party.size();i++)party.get(i).discard();
        for(var id:seal.enemies())kill(h,(LivingEntity)h.getLevel().getEntity(id),party.getFirst());
        h.assertTrue(seal.wave()==1 && seal.enemies().size()==5,"Confirmed first-wave deaths lead to five complementary champions");
        boolean brood=false,support=false,air=false;
        for(var id:seal.enemies()) {
            var mob=(Apex)h.getLevel().getEntity(id);var hp=mob.getAttribute(Attributes.MAX_HEALTH);
            double expected=Math.min(960,hp.getBaseValue()*2*1.45);
            h.assertTrue(Math.abs(mob.getMaxHealth()-expected)<.1 && mob.getMaxHealth()<=960,"The next wave retains the original four-player scaling within native health bounds");
            brood|=mob.kind()==ApexKind.BROOD;support|=mob.kind()==ApexKind.SUPPORT;air|=mob.flying();
        }
        h.assertTrue(brood && support && air,"The second Brood wave combines summons, healing and aerial pressure");
        var four=ModEntities.CROWNED_APEX.get().create(h.getLevel(),EntitySpawnReason.COMMAND);four.prepare(c,4);four.setHealth(13000);
        h.assertTrue(four.getMaxHealth()==15900 && four.getHealth()==13000,"Four-player Crown health remains a truthful native pool below its registered maximum");four.discard();
        seal.reset();for(var p:party)p.discard();late.discard();
    }
    private static boolean sealParticipant(GameTestHelper h,BlockPos c,ServerPlayer p) {return ((CrownSealBlockEntity)h.getLevel().getBlockEntity(c.above())).participant(p.getUUID());}
    static void gate(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(be.getBlockPos().east()));
        for(int x=-3;x<=3;x++)for(int y=1;y<=6;y++)h.getLevel().setBlock(c.offset(x,y,42),ModBlocks.CROWN_GATE.get().defaultBlockState(),3);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));
        p.setItemInHand(InteractionHand.OFF_HAND,key(0,UUID.randomUUID()));be.use(p,InteractionHand.MAIN_HAND);
        h.assertTrue(be.sockets()==0 && p.getOffhandItem().getCount()==1,"Keys from another site remain in the actual offered hand");
        p.setItemInHand(InteractionHand.OFF_HAND,key(0,be.site()));p.snapTo(Vec3.atBottomCenterOf(c.offset(0,1,41)));
        var gate=c.offset(0,2,42);p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(gate),net.minecraft.core.Direction.NORTH,gate,false));
        p.snapTo(Vec3.atBottomCenterOf(be.getBlockPos().east()));
        h.assertTrue(be.sockets()==1 && p.getOffhandItem().isEmpty() && h.getLevel().getBlockState(c.offset(0,2,42)).is(ModBlocks.CROWN_GATE.get()),"One real key leaves the protected gate shut");
        be=copy(h,be);p.setItemInHand(InteractionHand.OFF_HAND,key(0,be.site()));be.use(p,InteractionHand.MAIN_HAND);
        h.assertTrue(p.getOffhandItem().getCount()==1 && be.sockets()==1,"Repeated identical keys cannot substitute for a different wing");
        p.setItemInHand(InteractionHand.OFF_HAND,key(2,be.site()));be.use(p,InteractionHand.OFF_HAND);
        h.assertTrue(Integer.bitCount(be.sockets())==2 && h.getLevel().getBlockState(c.offset(0,2,42)).isAir(),"Any two distinct matching-site keys open the actual saved central gate");
        p.setItemInHand(InteractionHand.OFF_HAND,key(1,be.site()));be.use(p,InteractionHand.MAIN_HAND);
        var optional=(ChestBlockEntity)h.getLevel().getBlockEntity(c.offset(-6,1,50));
        h.assertTrue(Integer.bitCount(be.sockets())==3 && optional!=null && com.bunnidogs.mod.item.SeedItem.read(optional.getItem(10)).orElseThrow().quality()>=36,"The optional third matching keystone pays a frozen genetic archive without being required by the gate");
        var frozen=optional.getItem(10).copy();be=copy(h,be);p.setItemInHand(InteractionHand.OFF_HAND,key(1,be.site()));be.use(p,InteractionHand.MAIN_HAND);
        h.assertTrue(ItemStack.matches(optional.getItem(10),frozen) && !h.getLevel().getBlockState(c.offset(-4,1,50)).is(Blocks.CHEST),"Saved optional-key progress cannot duplicate or reroll an archive");
        h.assertTrue(be.getBlockState().getDestroyProgress(p,h.getLevel(),be.getBlockPos())==0 && !be.getBlockState().canEntityDestroy(h.getLevel(),be.getBlockPos(),p),"Critical encounter machinery survives mining and Apex terrain attacks");p.discard();
    }
    static void boss(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(be.getBlockPos().east()));
        var idle=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(15,1,15)));unlock(h,be,p);
        h.assertTrue(be.begin(p,InteractionHand.MAIN_HAND),"Two paid sockets authorize the first real Crowned encounter");
        var boss=(CrownedApex)h.getLevel().getEntity(be.enemies().iterator().next());boss.setNoAi(true);
        h.assertTrue(boss.getMaxHealth()==9300 && boss.roster()==2 && boss.getAttribute(Attributes.MAX_HEALTH)==boss.getAttribute(ModAttributes.CROWN_HEALTH),"Frozen two-player scaling uses a real native pool above vanilla's 1024 cap");
        boss.setHealth(3200);h.assertTrue(boss.getHealth()==3200,"Native setHealth cannot silently clamp the boss to vanilla max health");
        boss.setHealth(5400);boss.setNoAi(false);boss.tick();boss.setNoAi(true);h.assertTrue(boss.phase()==2 && boss.getHealth()>=5399.9F && boss.getHealth()<=5412.1F,"Phase transitions preserve damage; only the finite visible vitality link can heal");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ApexMinion.class,boss.getBoundingBox().inflate(50),e->CrownEncounter.member(e)).size()==3,"The Living Crown has a finite native pack");
        h.assertTrue(h.getLevel().getEntitiesOfClass(Apex.class,boss.getBoundingBox().inflate(60),e->e!=boss && CrownEncounter.member(e)).size()==4,"The middle phase adds four champions, with a melee threat and two directed ranged casters");
        float savedHealth=boss.getHealth();
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());boss.saveWithoutId(out);var restored=ModEntities.CROWNED_APEX.get().create(h.getLevel(),EntitySpawnReason.LOAD);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
        h.assertTrue(restored.roster()==2 && restored.getMaxHealth()==9300 && Math.abs(restored.getHealth()-savedHealth)<.1 && restored.phase()==2,"Native entity saves preserve full health, scaling and phase without resetting the fight");restored.discard();
        boss.setHealth(1000);boss.setNoAi(false);boss.tick();boss.setNoAi(true);h.assertTrue(boss.phase()==3 && boss.flying(),"The last third becomes an actual aerial finale");
        var escorts=h.getLevel().getEntitiesOfClass(Apex.class,boss.getBoundingBox().inflate(60),e->e!=boss && CrownEncounter.member(e));
        h.assertTrue(escorts.size()==2 && escorts.stream().allMatch(e->e.flying() && e.kind()==ApexKind.PRISM),"The final phase replaces its ground pack with two finite flying Prismmaws");
        var finalOut=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());boss.saveWithoutId(finalOut);
        var finalCopy=ModEntities.CROWNED_APEX.get().create(h.getLevel(),EntitySpawnReason.LOAD);finalCopy.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),finalOut.buildResult()));finalCopy.setNoAi(false);finalCopy.tick();
        h.assertTrue(h.getLevel().getEntitiesOfClass(Apex.class,boss.getBoundingBox().inflate(60),e->e!=boss && CrownEncounter.member(e)).size()==2,"Reloading a wounded final-phase boss cannot create another escort pack");finalCopy.discard();
        kill(h,boss,p);
        h.assertTrue(!be.active() && be.pending(p.getUUID())==1 && be.pending(idle.getUUID())==0,"Accepted lethal participation earns one saved core; an idle scaling visitor earns none");
        var cache=(ChestBlockEntity)h.getLevel().getBlockEntity(c.offset(-6,1,50));var archive=(ChestBlockEntity)h.getLevel().getBlockEntity(c.offset(-4,1,50));
        h.assertTrue(ApexSkills.explosion(h.getLevel(),boss,net.minecraft.world.phys.Vec3.atBottomCenterOf(c.offset(0,1,22)),9,false),"A real full-power finale blast must execute in the loaded native arena");
        h.assertTrue(h.getLevel().getBlockEntity(cache.getBlockPos())==cache && h.getLevel().getBlockEntity(archive.getBlockPos())==archive,"Earned vault/archive chests sit outside the next rematch's actual explosion reach");
        var seed=com.bunnidogs.mod.item.SeedItem.read(archive.getItem(10)).orElseThrow();
        h.assertTrue(cache.getLootTable()!=null && archive.getLootTable()==null && com.bunnidogs.mod.genetics.Wings.expressed(seed.mother()) && com.bunnidogs.mod.genetics.Wings.expressed(seed.father()),"An actual boss victory pays generic wealth and a separately frozen winged breeding archive");
        var tissue=com.bunnidogs.mod.item.TissueSampleItem.read(archive.getItem(12)).orElseThrow();h.assertTrue(tissue.genome().equals(seed.mother()) && tissue.stats().equals(seed.motherStats()),"The archive's reusable tissue is the exact saved parent, not a separate roll");
        be=copy(h,be);
        for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        Consumer<EntityJoinLevelEvent> noCore=e->{if(e.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item && item.getItem().is(ModItems.SOVEREIGN_CORE.get()))e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(noCore);
        try {be.claim(p);h.assertTrue(be.pending(p.getUUID())==1,"A full inventory and cancelled native item spawn retain the earned core entitlement");}finally {NeoForge.EVENT_BUS.unregister(noCore);}
        p.getInventory().setItem(0,ItemStack.EMPTY);
        Consumer<net.neoforged.neoforge.event.entity.item.ItemTossEvent> noTrophy=e->{if(e.getPlayer()==p && e.getEntity().getItem().is(ModItems.CROWN_TROPHY.get()))e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(noTrophy);
        try {be.claim(p);h.assertTrue(be.pending(p.getUUID())==0 && be.hasRewards(p.getUUID()),"A failed trophy delivery stays claimable after its core has already been delivered");}finally {NeoForge.EVENT_BUS.unregister(noTrophy);}
        p.getInventory().setItem(1,ItemStack.EMPTY);be.claim(p);be.claim(p);
        h.assertTrue(p.getInventory().getItem(1).is(ModItems.CROWN_TROPHY.get()) && p.getInventory().getItem(1).get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("CrownVictory","").equals(be.run().toString()),"The separately retried trophy records its real encounter identity");
        int cores=0;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(ModItems.SOVEREIGN_CORE.get()))cores+=p.getInventory().getItem(i).getCount();
        h.assertTrue(cores==1 && be.pending(p.getUUID())==0,"Reconnection and repeated claims cannot duplicate the same victory");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ApexMinion.class,new AABB(Vec3.atCenterOf(c),Vec3.atCenterOf(c)).inflate(60),e->CrownEncounter.member(e)).isEmpty(),"Phase/final cleanup removes temporary actors without touching player drops");
        be.reset();p.discard();idle.discard();
    }
    static void rematch(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(be.getBlockPos().east()));unlock(h,be,p);be.begin(p,InteractionHand.MAIN_HAND);
        var boss=(CrownedApex)h.getLevel().getEntity(be.enemies().iterator().next());boss.setNoAi(true);kill(h,boss,p);be.claim(p);
        var offered=new ItemStack(ModItems.CROWN_SIGIL.get(),2);offered.set(DataComponents.CUSTOM_NAME,Component.literal("Offhand payment"));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.CROWN_SIGIL.get(),3));p.setItemInHand(InteractionHand.OFF_HAND,offered);
        Consumer<EntityJoinLevelEvent> reject=e->{if(e.getEntity() instanceof CrownedApex && e.getLevel()==h.getLevel())e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(reject);
        try {h.assertTrue(!be.begin(p,InteractionHand.OFF_HAND) && offered.getCount()==2 && !be.active(),"Native spawn refusal pays no rematch token and leaves no live encounter");}finally {NeoForge.EVENT_BUS.unregister(reject);}
        h.assertTrue(be.begin(p,InteractionHand.OFF_HAND) && offered.getCount()==1 && p.getMainHandItem().getCount()==3,"A successful rematch spends exactly the actually offered offhand token");
        var run=be.run();be=copy(h,be);h.assertTrue(be.active() && be.run().equals(run) && offered.getCount()==1,"Saving an active rematch retains its already-paid encounter without paying again");
        be.reset();h.assertTrue(!be.active() && be.pending(p.getUUID())==0,"Abandonment cleanup grants no fabricated reward");p.discard();
    }
    static ItemStack smith(GameTestHelper h,String name,ItemStack base) {
        var recipe=h.getLevel().recipeAccess().byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,net.minecraft.resources.Identifier.fromNamespaceAndPath("bunnidogs",name))).orElseThrow();
        return ((net.minecraft.world.item.crafting.SmithingRecipe)recipe.value()).assemble(new net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack.EMPTY,base,new ItemStack(ModItems.SOVEREIGN_CORE.get())));
    }
    static void gear(GameTestHelper h) {
        var p=ApexTypesGameTest.player(h,ApexTypesGameTest.center(h));var base=new ItemStack(ModItems.HEARTBOUND_ARMOR.get("chestplate").get());base.setDamageValue(71);base.set(DataComponents.CUSTOM_NAME,Component.literal("Kept chestplate"));
        var protection=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION);base.enchant(protection,4);
        var upgrade=smith(h,"sovereign_chestplate",base);h.assertTrue(upgrade.is(ModItems.SOVEREIGN_ARMOR.get("chestplate").get()) && upgrade.getDamageValue()==71 && upgrade.getHoverName().getString().equals("Kept chestplate") && upgrade.getEnchantmentLevel(protection)==4,"Native Sovereign smithing retains actual wear, name and enchantments");
        p.setItemSlot(EquipmentSlot.CHEST,upgrade);p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.HEARTBOUND_ARMOR.get("helmet").get()));ExpeditionGear.armor(p);
        h.assertTrue(p.getMaxHealth()==29 && ExpeditionGear.pieces(p)==2,"Mixed old/new armor totals three plus six HP while keeping both recovery pieces");
        var elytra=smith(h,"sovereign_elytra",new ItemStack(ModItems.APEX_WING_HARNESS.get()));h.assertTrue(elytra.has(DataComponents.GLIDER) && protection.value().isSupportedItem(elytra),"The higher Elytra remains a native glider with actual Protection compatibility");
        p.setItemSlot(EquipmentSlot.CHEST,elytra);p.startFallFlying();var ammo=new ItemStack(ModItems.PRISM_CELL.get(),2);p.setItemInHand(InteractionHand.MAIN_HAND,ammo);ModItems.PRISM_CELL.get().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(ammo.getCount()==1 && elytra.getDamageValue()==3 && p.getCooldowns().isOnCooldown(ammo),"A real Sovereign flight burst pays its efficient but nonzero cell/wear costs");
        h.assertTrue(ModItems.SOVEREIGN_LANCE.get().getUseDuration(new ItemStack(ModItems.SOVEREIGN_LANCE.get()),p)==24,"The upgraded piercing weapon has the advertised shorter real charge");p.discard();
    }
    static void attacks(GameTestHelper h) {
        floor(h);var c=origin(h);var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(15,1,0)));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);p.setHealth(1024);
        var boss=ModEntities.CROWNED_APEX.get().create(h.getLevel(),EntitySpawnReason.TRIGGERED);boss.prepare(c,1);boss.snapTo(Vec3.atBottomCenterOf(c.above()));h.getLevel().addFreshEntity(boss);boss.setTarget(p);boss.beginCast(2,p);
        float hp=p.getHealth();for(int t=0;t<34;t++)boss.tick();h.assertTrue(p.getHealth()==hp && boss.warning()==2,"A committed beam has a full visible, harmless warning interval");
        for(int t=0;t<11;t++)boss.tick();h.assertTrue(p.getHealth()<hp,"The warned line resolves into real native damage");boss.cancel();boss.setHealth(800);boss.tick();
        h.assertTrue(boss.phase()==3 && boss.isNoGravity() && boss.getDeltaMovement().y>0,"The Broken Crown rises and remains an actual aerial creature between attacks");
        h.runAfterDelay(80,()->{h.assertTrue(boss.isAlive() && boss.isNoGravity() && boss.getY()>c.getY()+5,"Real native physics sustains the aerial finale between committed attacks: alive="+boss.isAlive()+", gravity="+boss.isNoGravity()+", height="+(boss.getY()-c.getY())+", phase="+boss.phase()+", target="+boss.getTarget()+", velocity="+boss.getDeltaMovement());boss.discard();p.discard();h.succeed();});
    }
    static void abandon(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(be.getBlockPos().east()));unlock(h,be,p);be.begin(p,InteractionHand.MAIN_HAND);
        var boss=(CrownedApex)h.getLevel().getEntity(be.enemies().iterator().next());boss.setNoAi(true);
        var storage=h.getLevel().getDataStorage().computeIfAbsent(net.minecraft.world.level.TicketStorage.TYPE);long home=net.minecraft.world.level.ChunkPos.containing(be.getBlockPos()).pack();
        h.assertTrue(storage.getTickets(home).stream().anyMatch(t->t.getType().equals(CrownTickets.loading(3))) && !CrownTickets.SIMULATION.get().doesLoad() && !CrownTickets.loading(3).persist(),"Native nonpersistent tickets retain the actual controller while simulation cannot generate an outer ring: home="+be.getBlockPos()+", tickets="+storage.getTickets(home));
        h.assertTrue(!CrownTickets.loading(0).equals(CrownTickets.loading(3)),"Different wing leases must remain distinct native TicketType values despite identical loading flags");
        var overlapping=seal(h,0,c.offset(0,1,44),c,UUID.randomUUID());CrownTickets.hold(overlapping);CrownTickets.release(overlapping);
        h.assertTrue(storage.getTickets(home).stream().anyMatch(t->t.getType().equals(CrownTickets.loading(3))),"Releasing an overlapping wing cannot cancel the central controller's chunk lease");
        var dropped=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),c.getX(),c.getY()+1,c.getZ(),new ItemStack(Items.DIAMOND_SWORD));h.getLevel().addFreshEntity(dropped);p.discard();
        h.runAfterDelay(220,()->{h.assertTrue(!be.active() && boss.isRemoved() && be.pending(p.getUUID())==0,"Ten seconds without a valid participant resets and cleans the abandoned encounter");h.assertTrue(dropped.isAlive(),"Encounter reset preserves actual dropped player equipment");h.assertTrue(storage.getTickets(home).stream().noneMatch(t->t.getType().equals(CrownTickets.loading(3))),"Abandonment releases its native loading lease rather than permanently forcing dungeon chunks");dropped.discard();h.succeed();});
    }
    static void support(GameTestHelper h) {
        floor(h);var c=origin(h);var be=seal(h,3,c.offset(0,1,46),c,UUID.randomUUID());var p=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(be.getBlockPos().east()));var ally=ApexTypesGameTest.player(h,Vec3.atBottomCenterOf(c.offset(15,1,15)));unlock(h,be,p);be.begin(p,InteractionHand.MAIN_HAND);
        ((Apex)h.getLevel().getEntity(be.enemies().iterator().next())).setNoAi(true);p.snapTo(ally.position().add(2,0,0));ally.setHealth(4);
        var tool=new ItemStack(ModItems.CONCORD_STANDARD.get());p.setItemInHand(InteractionHand.MAIN_HAND,tool);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.HEART_SPOOL.get(),2));ModItems.CONCORD_STANDARD.get().use(h.getLevel(),p,InteractionHand.MAIN_HAND);ModItems.CONCORD_STANDARD.get().finishUsingItem(tool,h.getLevel(),p);
        h.assertTrue(ally.getHealth()==14 && p.getOffhandItem().getCount()==1 && tool.getDamageValue()==1 && ally.hasEffect(net.minecraft.world.effect.MobEffects.RESISTANCE),"The Standard heals another actual engaged player and pays real spool/wear costs");
        h.assertTrue(be.contribution(p.getUUID())==10 && be.contribution(ally.getUUID())==0,"Only accepted other-player healing earns support contribution; receiving healing does not");be.reset();p.discard();ally.discard();
    }
}
