package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Combat;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.item.TravelBasketItem;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.CombatPanel;
import com.bunnidogs.mod.menu.PartyPanel;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

/** Real goals, native damage/projectiles and whole-animal rescue, rather than source-string assertions. */
public final class CompanionCombatGameTest {
    private CompanionCombatGameTest() {}
    private static final int Y=200;
    private static void floor(GameTestHelper h) {
        for (int x=0;x<=18;x++) for (int z=0;z<=5;z++) {
            h.setBlock(x,Y,z,Blocks.STONE);
            for (int y=1;y<=4;y++) h.setBlock(x,Y+y,z,Blocks.AIR);
        }
    }
    private static Bunnidog dog(GameTestHelper h,int x,boolean walk) {
        var dog=walk ? h.spawn(ModEntities.BUNNIDOG.get(),x,Y+1,2) : h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),x,Y+1,2);
        dog.setGenome(Genome.DEFAULT); dog.setStats(Stats.ZERO); dog.setAge(0); dog.setHealth(dog.getMaxHealth()); dog.setOnGround(true);
        return dog;
    }
    private static ServerPlayer keeper(GameTestHelper h,Bunnidog dog) {
        var player=h.makeMockServerPlayerInLevel(); CareMenuGameTest.prepare(player); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(dog.position().add(1,0,0));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.TRAIL_WHISTLE.get()));
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GUAVA.get(),8));
        h.assertTrue(dog.trail().trainFromStock(player,player.getOffhandItem()),"An ordinary zero-point adult trains with actual stock");
        return player;
    }
    private static CompoundTag save(Bunnidog dog) {
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.registryAccess()); dog.saveWithoutId(out); return out.buildResult();
    }
    private static Genome trait(Genome genome,Gene gene,String name) {
        int allele=gene.alleleNamed(name).id(); return genome.with(gene,0,allele).with(gene,1,allele);
    }
    public static void fight(GameTestHelper h) {
        floor(h); var dog=dog(h,2,true); var keeper=keeper(h,dog);
        CareMenu.openCombat(keeper,dog); var menu=(CareMenu)keeper.containerMenu;
        h.assertTrue(menu.clickMenuButton(keeper,menu.buttonId(CombatPanel.ASSIST))&&dog.combat().selected(),"Actual orders explicitly choose one fighter");
        CohesionGameTest.preview("Combat orders",menu.view()); keeper.closeContainer();
        var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,6,Y+1,2); enemy.setTarget(keeper);
        keeper.attack(enemy);
        float afterKeeperHit=enemy.getHealth();
        double[] distance={0};
        h.runAtTickTime(60,()->{
            h.assertTrue(enemy.getHealth()<afterKeeperHit&&dog.getTarget()==enemy,"A real companion goal walks to and bites the keeper's target: health="+enemy.getHealth()+", intent="+dog.combat().intent()+", dog="+dog.position()+", enemy="+enemy.position()+", target="+dog.getTarget()+", keeper="+dog.trail().keeperPlayer()+", legal="+dog.combat().legal(enemy));
            h.assertTrue(enemy.getLastHurtByPlayer()==keeper,"Actual pet damage credits a nearby participating keeper through native memory");
            keeper.setPos(dog.position().add(-5,0,0)); distance[0]=dog.distanceToSqr(keeper);
            var chest=keeper.blockPosition().offset(0,0,1); h.getLevel().setBlock(chest,Blocks.CHEST.defaultBlockState(),3);
            keeper.gameMode.useItemOn(keeper,h.getLevel(),keeper.getMainHandItem(),InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(chest),Direction.UP,chest,false));
            h.assertTrue(dog.getTarget()==null&&dog.combat().intent().equals("regrouping"),"A heard normal Whistle clears an actual fighting target immediately");
            h.assertTrue(keeper.containerMenu==keeper.inventoryMenu,"Native Whistle use at a chest recalls instead of opening storage");
            dog.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),1);
        });
        h.runAtTickTime(95,()->{
            h.assertTrue(dog.getTarget()==null&&dog.distanceToSqr(keeper)<distance[0],"Incoming damage cannot cancel withdrawal or reacquire the old enemy");
            dog.discard(); enemy.discard(); h.succeed();
        });
    }
    public static void rescue(GameTestHelper h) {
        floor(h); var dog=dog(h,2,false); var keeper=keeper(h,dog); var level=h.getLevel();
        dog.setCustomName(Component.literal("Bracken")); dog.setStats(Stats.of(20,0,0,0,0));
        var bag=new ItemStack(Items.BUNDLE); bag.set(DataComponents.CUSTOM_NAME,Component.literal("Field ration"));
        keeper.setItemInHand(InteractionHand.MAIN_HAND,bag); dog.mobInteract(keeper,InteractionHand.MAIN_HAND);
        h.assertTrue(dog.carriedBundleCount()==1,"The rescued animal has actual named equipment");
        var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,5,Y+1,2);
        h.assertTrue(dog.hurtServer(level,level.damageSources().mobAttack(enemy),100)&&dog.isAlive()&&dog.combat().downed(),"A bonded non-party carrier survives a lethal native combat hit without dying");
        h.assertTrue(!dog.canBeSeenAsEnemy()&&!dog.canBeHitByProjectile()&&!dog.isPushable()&&!dog.readyForPen(),"A downed animal is no target, shield, pushable obstacle or breeder");
        CareMenu.openCombat(keeper,dog); CohesionGameTest.preview("Downed partner",((CareMenu)keeper.containerMenu).view()); keeper.closeContainer();
        h.assertTrue(!dog.hurtServer(level,level.damageSources().mobAttack(enemy),200),"Follow-up combat does not kill the downed animal");
        var identity=dog.getUUID(); var genome=dog.genome(); var stats=dog.stats(); var basket=new ItemStack(ModItems.TRAVEL_BASKET.get());
        h.assertTrue(TravelBasketItem.capture(level,basket,dog).success(),"Only the downed rescue exception bypasses recent-combat capture refusal");
        h.assertTrue(TravelBasketItem.release(level,h.absolutePos(new BlockPos(9,Y+1,2)),basket).success(),"Rescue releases the existing animal safely");
        var rescued=(Bunnidog)level.getEntity(identity);
        h.assertTrue(rescued!=null&&rescued.combat().downed()&&rescued.combat().recovering()&&rescued.genome().equals(genome)
                &&rescued.stats().equals(stats)&&rescued.trail().belongsTo(keeper)&&rescued.carriedBundleCount()==1
                &&rescued.getName().getString().equals("Bracken"),"Basket travel conserves identity, family, cargo, bond and injury");
        keeper.setPos(rescued.position().add(1,0,0)); rescued.setOnGround(true); var guavas=keeper.getOffhandItem(); int before=guavas.getCount();
        var quiet=save(rescued); quiet.putInt("CombatDanger",0);
        rescued.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),quiet)); rescued.setOnGround(true);
        CareMenu.openCombat(keeper,rescued); var aid=(CareMenu)keeper.containerMenu;
        boolean[] callback={false};
        Consumer<LivingHealEvent> reentry=event->{
            if (event.getEntity()!=rescued) return;
            callback[0]=true;
            h.assertTrue(guavas.getCount()==before-1&&!rescued.careFromStock(keeper,guavas)&&!rescued.combat().firstAid(keeper,guavas),
                    "First aid pays before actual healing hooks and cannot reenter ordinary care or buy aid twice");
        };
        NeoForge.EVENT_BUS.addListener(reentry);
        try { h.assertTrue(aid.clickMenuButton(keeper,aid.buttonId(CarePanels.CARE)),"The actual combat-orders first-aid button succeeds"); }
        finally { NeoForge.EVENT_BUS.unregister(reentry); }
        h.assertTrue(callback[0]&&guavas.getCount()==before-1&&!rescued.combat().downed()
                &&rescued.combat().recovering()&&!rescued.combat().assist(keeper)&&!rescued.trail().release(keeper),"One actual first-aid payment restores travel without resetting injury or releasing protection");
        keeper.closeContainer();
        CareMenu.openCombat(keeper,rescued); CohesionGameTest.preview("Recovering partner",((CareMenu)keeper.containerMenu).view()); keeper.closeContainer();
        var wounded=save(rescued); wounded.putInt("CombatRecovery",Combat.recoveryTicks(rescued.stats())-2); wounded.putInt("CombatDanger",0);
        rescued.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),wounded));
        rescued.setHealth(rescued.getMaxHealth()/2); rescued.setOnGround(true);
        h.runAfterDelay(5,()->{
            h.assertTrue(!rescued.combat().recovering(),"After fast-forwarding only the long wait, native safe ticks complete saved recovery at half health");
            rescued.discard(); enemy.discard(); h.succeed();
        });
    }
    public static void roster(GameTestHelper h) {
        floor(h); var one=dog(h,2,false); var keeper=keeper(h,one); var two=dog(h,5,false);
        var three=dog(h,4,false); var reserve=dog(h,6,false);
        for (var partner:new Bunnidog[]{two,three,reserve}) h.assertTrue(partner.trail().trainFromStock(keeper,keeper.getOffhandItem()),"The keeper can bond a household");
        h.assertTrue(one.combat().assist(keeper)&&one.combat().selected()&&!two.combat().selected(),"Training does not recruit the household");
        h.assertTrue(two.combat().assist(keeper)&&three.combat().assist(keeper)&&one.combat().partyCount()==3
                &&three.combat().commanded(),"Three chosen adults fill a bounded party and the latest choice receives directed orders");
        var roster=CombatRoster.get(h.getLevel());
        var stored=CombatRoster.CODEC.encodeStart(NbtOps.INSTANCE,roster).getOrThrow();
        var decoded=CombatRoster.CODEC.parse(NbtOps.INSTANCE,stored).getOrThrow();
        h.assertTrue(decoded.members(keeper.getUUID()).equals(roster.members(keeper.getUUID()))
                &&decoded.animal(keeper.getUUID()).equals(three.getUUID())
                &&decoded.sighting(keeper.getUUID(),one.getUUID()).pos().equals(one.blockPosition()),
                "The actual SavedData codec conserves ordered members, command recipient and a remembered location");
        var basket=new ItemStack(ModItems.TRAVEL_BASKET.get()); var id=one.getUUID();
        h.assertTrue(TravelBasketItem.capture(h.getLevel(),basket,one).success()&&!reserve.combat().assist(keeper),"A packed fighter still occupies its slot");
        h.assertTrue(TravelBasketItem.release(h.getLevel(),h.absolutePos(new BlockPos(9,Y+1,2)),basket).success(),"The old selected animal can be restored from its real basket");
        var restored=(Bunnidog)h.getLevel().getEntity(id);
        h.assertTrue(restored.combat().selected()&&two.combat().selected()&&two.isAlliedTo(restored)&&restored.combat().partyCount()==3,
                "Basket restoration retains exactly the same three party memberships and allies");
        CareMenu.openParty(keeper); var menu=(CareMenu)keeper.containerMenu;
        CohesionGameTest.preview("Combat party",menu.view());
        int staleThird=menu.buttonId(PartyPanel.REMOVE+2);
        h.assertTrue(menu.clickMenuButton(keeper,menu.buttonId(PartyPanel.REMOVE))&&!restored.combat().selected(),"The live party view removes one quiet member");
        h.assertTrue(!menu.clickMenuButton(keeper,staleThird)&&three.combat().selected(),"A stale row button cannot remove the next animal after a roster change");
        keeper.closeContainer();
        h.assertTrue(reserve.combat().assist(keeper),"A reserve can join once a quiet member is deliberately removed");
        h.assertTrue(TravelBasketItem.capture(h.getLevel(),basket,reserve).success(),"A full party can include a packed member");
        keeper.setPos(two.position().add(1,0,0)); var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,7,Y+1,2);
        two.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),1);
        h.assertTrue(!CompanionParty.remove(keeper,reserve.getUUID())&&two.combat().selected(),"Recent combat reserves even a packed party slot");
        two.combat().recall(keeper);
        h.runAtTickTime(220,()->{
            h.assertTrue(CompanionParty.remove(keeper,reserve.getUUID()),"Actual quiet loaded time allows removing a packed member without releasing it");
            keeper.setHealth(0);
            h.runAfterDelay(2,()->{
                h.assertTrue(two.trail().order()==Trail.Order.WAIT&&two.getTarget()==null,"Keeper loss ends offense and waits instead of seeking a remote home");
                restored.discard(); two.discard(); three.discard(); enemy.discard(); h.succeed();
            });
        });
    }
    public static void crossfire(GameTestHelper h) {
        floor(h); var dog=dog(h,4,false); var keeper=keeper(h,dog); var level=h.getLevel();
        keeper.setPos(dog.position().add(1,0,2));
        var friend=h.makeMockServerPlayerInLevel(); friend.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); friend.setPos(dog.position().add(-3,0,0));
        h.assertTrue(!dog.hurtServer(level,level.damageSources().playerAttack(keeper),4)
                &&!dog.hurtServer(level,level.damageSources().playerAttack(friend),4),"PvE routine crossfire protection does not need scoreboard teams");
        var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,8,Y+1,2);
        var arrow=h.spawn(EntityTypes.ARROW,1,Y+1,2); arrow.setOwner(friend); arrow.setPos(dog.getX()-3,dog.getY()+.4,dog.getZ());
        arrow.setNoGravity(true); arrow.setDeltaMovement(1,0,0); arrow.setBaseDamage(3);
        h.runAfterDelay(18,()->{
            h.assertTrue(dog.getHealth()==dog.getMaxHealth()&&enemy.getHealth()<enemy.getMaxHealth(),"A real friendly arrow passes the companion and can hit the enemy behind it: pet="+dog.getHealth()+", enemy="+enemy.getHealth()+", arrow="+arrow.position()+", alive="+arrow.isAlive());
            dog.discard(); enemy.discard(); arrow.discard(); h.succeed();
        });
    }
    public static void genetics(GameTestHelper h) {
        floor(h); var dog=dog(h,2,false);
        h.assertTrue(dog.getAttributeValue(Attributes.ATTACK_DAMAGE)==2,"An average zero-point loyal animal has a useful two-HP reference bite");
        var line=trait(trait(Genome.DEFAULT,Gene.BUILD,"stocky"),Gene.TEMPERAMENT,"feral");
        dog.setHealth(dog.getMaxHealth()/2); dog.setGenome(line); dog.setStats(Stats.of(20,0,0,0,0));
        h.assertTrue(Math.abs(dog.getAttributeValue(Attributes.ATTACK_DAMAGE)-3.168)<.001
                &&Math.abs(dog.getHealth()/dog.getMaxHealth()-.5)<.001,"Actual attributes use Build, temperament and Vitality while conserving the health fraction");
        var wild=dog(h,6,false); var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,9,Y+1,2);
        wild.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),100);
        h.assertTrue(!wild.isAlive()&&!wild.combat().downed(),"Unbonded animals retain native death rather than free rescue protection");
        var bonded=dog(h,12,false); var keeper=keeper(h,bonded);
        bonded.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),100);
        bonded.hurtServer(h.getLevel(),h.getLevel().damageSources().fellOutOfWorld(),1000);
        int food=keeper.getOffhandItem().getCount();
        h.assertTrue(!bonded.isAlive()&&!bonded.combat().firstAid(keeper,keeper.getOffhandItem())&&keeper.getOffhandItem().getCount()==food,
                "Downed protection never cancels the void, and first aid cannot spend food or heal a native corpse");
        dog.discard(); wild.discard(); bonded.discard(); enemy.discard();
    }
    public static void focus(GameTestHelper h) {
        floor(h); var dog=dog(h,2,true); dog.setGenome(trait(Genome.DEFAULT,Gene.TEMPERAMENT,"feral"));
        var keeper=keeper(h,dog);
        h.assertTrue(dog.combat().assist(keeper)&&dog.combat().staySafe(keeper),"An ordinary feral adult bonds and can be deliberately kept safe");
        var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,6,Y+1,2);
        var creeper=h.spawnWithNoFreeWill(EntityTypes.CREEPER,6,Y+1,4);
        h.assertTrue(!dog.combat().focus(keeper,creeper)&&!dog.combat().danger(),"An unsafe explicit focus refuses a Creeper without changing a safe policy");
        creeper.discard();
        keeper.lookAt(EntityAnchorArgument.Anchor.EYES,enemy.getEyePosition()); keeper.setShiftKeyDown(true);
        ModItems.TRAIL_WHISTLE.get().use(h.getLevel(),keeper,InteractionHand.MAIN_HAND); keeper.setShiftKeyDown(false);
        h.assertTrue(dog.getTarget()==enemy&&!dog.combat().assisting(),"The actual crouched Whistle view trace starts a single feral focus without enabling automatic Assist");
        h.runAtTickTime(45,()->{
            h.assertTrue(enemy.getHealth()<enemy.getMaxHealth()&&enemy.getLastHurtByPlayer()==keeper,
                    "The directed feral striker walks and lands real native hits with local keeper credit");
            ModItems.TRAIL_WHISTLE.get().use(h.getLevel(),keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.getTarget()==null&&!dog.combat().assisting()&&dog.combat().intent().equals("regrouping"),
                    "Recall interrupts an actual directed strike and retains the chosen safe policy");
            h.assertTrue(dog.stats().equals(Stats.ZERO),"Bonding and fighting never grant inherited points");
            dog.discard(); enemy.discard(); h.succeed();
        });
    }
    public static void lookout(GameTestHelper h) {
        floor(h); var dog=dog(h,2,true); dog.setGenome(trait(Genome.DEFAULT,Gene.TEMPERAMENT,"skittish"));
        var keeper=keeper(h,dog); h.assertTrue(dog.combat().assist(keeper),"An ordinary skittish adult can be a chosen lookout");
        keeper.setPos(dog.position().add(0,0,1));
        var enemy=h.spawnWithNoFreeWill(EntityTypes.HUSK,6,Y+1,2); enemy.setTarget(keeper);
        h.runAtTickTime(12,()->{
            h.assertTrue(dog.getTarget()==null&&dog.combatState()==6&&enemy.getHealth()==enemy.getMaxHealth(),
                    "A real skittish lookout warns about the visible attacker and does not automatically charge");
            h.assertTrue(dog.combat().focus(keeper,enemy),"A healthy lookout can accept a deliberate skirmish");
        });
        h.runAtTickTime(48,()->{
            h.assertTrue(enemy.getHealth()<enemy.getMaxHealth()&&enemy.getHealth()>=enemy.getMaxHealth()-1.81F
                    &&dog.getTarget()==null&&dog.combat().intent().equals("regrouping"),
                    "A skittish native skirmish lands one genetic bite then withdraws");
            dog.combat().assist(keeper); enemy.discard();
            var carrier=dog(h,3,true);
            h.assertTrue(carrier.trail().trainFromStock(keeper,keeper.getOffhandItem())&&carrier.trail().command(keeper,Trail.Order.WAIT)
                    &&!carrier.combat().selected(),"An unselected carrier remains an ordinary parked follower");
            var creeper=h.spawnWithNoFreeWill(EntityTypes.CREEPER,4,Y+1,2); creeper.setSwellDir(1);
            dog.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2,Y+1,2))));
            h.runAfterDelay(12,()->{
                h.assertTrue(dog.getTarget()==null&&dog.combat().intent().equals("regrouping"),
                        "A primed Creeper interrupts resumed assistance and causes withdrawal, never an automatic bite");
                h.assertTrue(carrier.trail().order()==Trail.Order.FOLLOW&&carrier.getTarget()==null&&!carrier.combat().selected(),
                        "A visible fuse frees a parked carrier to escape without recruiting it to combat");
                dog.discard(); carrier.discard(); creeper.discard(); h.succeed();
            });
        });
    }
    public static void hold(GameTestHelper h) {
        floor(h); var dog=dog(h,4,true); dog.setGenome(trait(Genome.DEFAULT,Gene.TEMPERAMENT,"stoic")); var keeper=keeper(h,dog);
        h.assertTrue(dog.combat().assist(keeper)&&dog.trail().command(keeper,Trail.Order.WAIT),"An ordinary stoic can Assist while holding a chosen place");
        var anchor=dog.combat().hold(); var stored=save(dog);
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),stored));
        h.assertTrue(anchor.equals(dog.combat().hold()),"Native animal save/load retains the actual held position");
        var far=h.spawnWithNoFreeWill(EntityTypes.HUSK,12,Y+1,2);
        h.assertTrue(!dog.combat().focus(keeper,far)&&dog.trail().order()==Trail.Order.WAIT,"A held defender refuses a legal distant hostile outside its small anchor area");
        var near=h.spawnWithNoFreeWill(EntityTypes.HUSK,5,Y+1,2);
        // Keep the first contact stable; exercise actual target departure explicitly after the recall.
        near.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        keeper.attack(near); float before=near.getHealth();
        h.runAtTickTime(25,()->{
            h.assertTrue(near.getHealth()<before&&dog.trail().order()==Trail.Order.WAIT&&dog.blockPosition().distSqr(anchor)<=16,
                    "The real held-defense goal bites a nearby target without abandoning its anchor: health="+near.getHealth()+", before="+before+", intent="+dog.combat().intent()+", order="+dog.trail().order()+", dog="+dog.position()+", enemy="+near.position()+", anchor="+anchor);
            ModItems.TRAIL_WHISTLE.get().use(h.getLevel(),keeper,InteractionHand.MAIN_HAND);
            h.assertTrue(dog.trail().order()==Trail.Order.FOLLOW&&dog.getTarget()==null&&dog.combat().hold()==null,
                    "Normal Whistle reliably calls an actively fighting held partner out of position");
            h.assertTrue(dog.combat().assist(keeper)&&dog.trail().command(keeper,Trail.Order.WAIT)&&dog.combat().focus(keeper,near),
                    "The keeper may explicitly resume a healthy defender at a new held position");
            near.setPos(Vec3.atBottomCenterOf(dog.combat().hold().offset(6,0,0)));
            h.runAfterDelay(2,()->{
                h.assertTrue(dog.getTarget()==null&&dog.trail().order()==Trail.Order.FOLLOW&&dog.combat().intent().equals("regrouping"),
                        "An actual target leaving the held area ends pursuit and yields the position");
                dog.discard(); near.discard(); far.discard(); h.succeed();
            });
        });
    }
    public static void party(GameTestHelper h) {
        floor(h); var one=dog(h,2,true); var keeper=keeper(h,one); var two=dog(h,4,true); var three=dog(h,5,true);
        keeper.setPos(h.absoluteVec(new Vec3(6.5,Y+1,3.5))); keeper.setYRot(0);
        for (var partner:new Bunnidog[]{two,three}) h.assertTrue(partner.trail().trainFromStock(keeper,keeper.getOffhandItem()),"An ordinary travelling group bonds from actual food");
        for (var partner:new Bunnidog[]{one,two,three}) h.assertTrue(partner.combat().assist(keeper),"Each fighter joins deliberately");
        h.runAtTickTime(75,()->{
            h.assertTrue(one.distanceToSqr(two)>.8&&one.distanceToSqr(three)>.8&&two.distanceToSqr(three)>.8
                    &&one.distanceToSqr(keeper)<25&&two.distanceToSqr(keeper)<25&&three.distanceToSqr(keeper)<25,
                    "Three real following goals settle into separate local positions");
            var first=h.spawnWithNoFreeWill(EntityTypes.HUSK,9,Y+1,2); var second=h.spawnWithNoFreeWill(EntityTypes.HUSK,9,Y+1,4);
            keeper.attack(first); two.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(second),.5F);
            h.runAfterDelay(23,()->{
                var targets=java.util.stream.Stream.of(one,two,three).map(Bunnidog::getTarget).toList();
                h.assertTrue(targets.contains(first)&&targets.contains(second)&&targets.stream().filter(t->t==first).count()<=2,
                        "Native party decisions cover two actual combat threats instead of sending all three at one victim");
                one.discard(); two.discard(); three.discard(); first.discard(); second.discard(); h.succeed();
            });
        });
    }
}
