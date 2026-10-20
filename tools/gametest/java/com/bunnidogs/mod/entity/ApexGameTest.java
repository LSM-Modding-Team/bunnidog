package com.bunnidogs.mod.entity;

import java.util.ArrayList;
import io.netty.channel.embedded.EmbeddedChannel;
import com.bunnidogs.mod.block.ApexCrucibleBlock;
import com.bunnidogs.mod.menu.ApexPanel;
import com.bunnidogs.mod.block.TrophyPedestalBlockEntity;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.genetics.Tissue;
import com.bunnidogs.mod.genetics.ApexRules;
import com.bunnidogs.mod.item.ApexCrestItem;
import com.bunnidogs.mod.item.TissueSampleItem;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModEntities;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** One native end-to-end encounter; no client payload or world-generation dependency. */
public final class ApexGameTest {
    private ApexGameTest() {}
    private static void ritual(GameTestHelper h,ServerPlayer player,BlockPos pos) {
        var menu=CareMenuGameTest.open(h,player,pos);
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(ApexPanel.PREPARE)),"A complete ring permits deliberate preparation");
        h.assertTrue(menu.clickMenuButton(player,menu.buttonId(ApexPanel.SUMMON)),"The explicit summon action reaches native spawning");
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        var difficulty = level.getDifficulty();
        level.getServer().setDifficulty(Difficulty.NORMAL, true);
        try {
            var center = new BlockPos(4,1,4);
            var absolute = h.absolutePos(center);
            for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(new BlockPos(x,0,z), Blocks.STONE);
            h.setBlock(center, ModBlocks.APEX_CRUCIBLE.get());
            // The deprecated in-level helper always reports Creative, even after ability changes.
            var player = (ServerPlayer) h.makeMockServerPlayer(GameType.SURVIVAL);
            var connection = new Connection(PacketFlow.SERVERBOUND);
            new EmbeddedChannel(connection);
            level.getServer().getPlayerList().placeNewPlayer(connection, player,
                    CommonListenerCookie.createInitial(player.getGameProfile(), false));
            player.connection.markClientLoaded();
            GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
            CareMenuGameTest.prepare(player);
            player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(0,1,0))));
            var genome = Genome.DEFAULT.with(Gene.COAT,0,4).with(Gene.COAT,1,4).with(Gene.DIET,1,11);
            var source = new Tissue(genome, Stats.of(1000000,1000000,1000000,1000000,1000000));
            var sample = TissueSampleItem.create(source.genome(),source.stats());
            sample.setCount(2);
            player.setItemInHand(InteractionHand.MAIN_HAND,sample);
            var legends = new ItemStack[]{new ItemStack(ModItems.TITAN_HAUNCH.get()),new ItemStack(ModItems.GILDED_FOOT.get()),
                    new ItemStack(ModItems.MOON_SINEW.get()),new ItemStack(ModItems.LISTENING_SHELL.get()),new ItemStack(ModItems.EVERBLOOM.get())};
            var pedestals = new ArrayList<TrophyPedestalBlockEntity>();
            for (int i = 0; i < 5; i++) {
                var position = center.offset(ApexCrucibleBlock.RING.get(i));
                h.setBlock(position,ModBlocks.TROPHY_PEDESTAL.get());
                var pedestal = (TrophyPedestalBlockEntity) level.getBlockEntity(h.absolutePos(position));
                if (i < 4) pedestal.setItem(legends[i].copy());
                pedestals.add(pedestal);
            }
            h.useBlock(center,player);
            h.assertTrue(sample.getCount() == 2 && pedestals.getFirst().item().is(legends[0].getItem()),
                    "Missing offerings must preserve the sample and completed displays");
            pedestals.get(4).setItem(legends[4].copy());
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND,sample);
            h.useBlock(center,player);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.CALIPERS.get()));
            h.useBlock(center,player);
            h.assertTrue(sample.getCount() == 2 && pedestals.stream().noneMatch(TrophyPedestalBlockEntity::isEmpty)
                    && level.getEntitiesOfClass(Apex.class,new AABB(absolute).inflate(5)).isEmpty(),
                    "Native empty-hand inspection and Calipers forecasts must preserve all materials and spawn nothing");
            player.setItemInHand(InteractionHand.MAIN_HAND,sample);
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            h.setBlock(center.above(2),Blocks.STONE);
            ritual(h,player,absolute);
            h.assertTrue(sample.getCount() == 2 && pedestals.stream().noneMatch(TrophyPedestalBlockEntity::isEmpty),
                    "Blocked spawning must spend no offerings or sample");
            h.setBlock(center.above(2),Blocks.AIR);
            ritual(h,player,absolute);
            var bosses = level.getEntitiesOfClass(Apex.class,new AABB(absolute).inflate(5));
            h.assertTrue(bosses.size() == 1 && sample.getCount() == 1
                    && pedestals.stream().allMatch(TrophyPedestalBlockEntity::isEmpty),
                    "A valid native ritual must create one boss and spend exactly five legends and one sample");
            var apex = bosses.getFirst();
            var forecast = ApexRules.forecast(source);
            h.assertTrue(Math.abs(apex.getMaxHealth()-forecast.normal().health()) < .001
                    && Math.abs(apex.getAttributeValue(Attributes.ATTACK_DAMAGE)-forecast.normal().damage()) < .001
                    && apex.getAttributeValue(Attributes.ARMOR) == forecast.armor()
                    && apex.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == forecast.knockbackResistance(),
                    "The actual summoned boss must match the source forecast's health, damage and defenses");
            apex.setNoAi(true);
            h.assertTrue(apex.genome().equals(source.genome()) && apex.stats().equals(source.stats())
                    && apex.getMaxHealth() >= 180 && apex.getMaxHealth() <= 720
                    && apex.getAttributeValue(Attributes.MOVEMENT_SPEED) <= .42
                    && apex.getBbHeight() > 1.6 && apex.isPersistenceRequired(),
                    "Source data must stay exact while combat attributes, scaled dimensions and persistence apply immediately");
            for (int i = 0; i < 5; i++) pedestals.get(i).setItem(legends[i].copy());
            h.useBlock(center,player);
            h.assertTrue(sample.getCount() == 1 && pedestals.stream().noneMatch(TrophyPedestalBlockEntity::isEmpty),
                    "An active nearby Apex must prevent another ritual without cost");
            var boat = ModEntities.GUAVA_BOAT.get().create(level,EntitySpawnReason.COMMAND);
            h.assertTrue(!apex.canBeLeashed() && !apex.startRiding(boat) && !apex.acceptsCare()
                    && !apex.canEat(new ItemStack(ModItems.GUAVA.get())), "The boss must reject domestic transport and food");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.SYRINGE.get()));
            player.interactOn(apex,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(player.getMainHandItem().getDamageValue() == 0 && apex.samplingWait() == 0,
                    "Native sampling must refuse the boss without tool wear or a new sample");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.GENE_SPLICER.get()));
            player.setItemInHand(InteractionHand.OFF_HAND,TissueSampleItem.create(Genome.DEFAULT,Stats.ZERO));
            player.getInventory().add(new ItemStack(ModItems.MUTAGEN.get()));
            player.interactOn(apex,InteractionHand.MAIN_HAND,Vec3.ZERO);
            h.assertTrue(apex.genome().equals(source.genome()) && player.getMainHandItem().getDamageValue() == 0,
                    "Native splicing must leave the boss and tool intact");
            h.setBlock(new BlockPos(2,1,4),ModBlocks.NEST.get());
            h.assertTrue(apex.moveHomeTo(level,h.absolutePos(new BlockPos(2,1,4))) == NestHome.MoveResult.INVALID,
                    "An Apex cannot acquire a resident's home");
            apex.setHealth(apex.getMaxHealth() * .4F);
            apex.tick();
            h.assertTrue(apex.enraged() && apex.homeNest() == null
                    && Math.abs(apex.getAttributeValue(Attributes.ATTACK_DAMAGE)-forecast.enraged().damage()) < .001,
                    "Half-health enrage must respect combat bounds and never claim a Nest");
            apex.beginAbility();
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            apex.saveWithoutId(output);
            var saved = output.buildResult();
            var restored = ModEntities.APEX.get().create(level,EntitySpawnReason.LOAD);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            h.assertTrue(restored.genome().equals(source.genome()) && restored.stats().equals(source.stats())
                    && restored.abilityWait() == 120 && restored.enraged()
                    && Math.abs(restored.getHealth()/restored.getMaxHealth() - .4) < .001,
                    "Native saves must retain source, health fraction, enrage and remaining ability recovery");
            apex.startSeenByPlayer(player); apex.stopSeenByPlayer(player);
            saved.putInt("ApexAbilityWait",0);
            apex.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            apex.setTarget(player); apex.setOnGround(true);
            player.setPos(apex.position().add(2,0,0));
            player.setHealth(player.getMaxHealth());
            float before = player.getHealth();
            var stomp = new ApexAbilityGoal(apex);
            h.assertTrue(stomp.canUse(), "A rested boss must be able to charge: wait=" + apex.abilityWait()
                    + ", ground=" + apex.onGround() + ", target=" + (apex.getTarget() != null)
                    + ", distance=" + apex.distanceToSqr(player) + ", sight=" + apex.hasLineOfSight(player));
            stomp.start();
            h.assertTrue(player.getHealth() == before, "Starting a charge must give warning before damage");
            while (stomp.canContinueToUse()) stomp.tick();
            stomp.stop();
            h.assertTrue(player.getHealth() < before, "The completed native stomp must damage a visible nearby survival player");
            apex.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            apex.setTarget(player); apex.setOnGround(true);
            // Keep both ends of the sight ray inside the eight-block fixture's barrier walls.
            player.setPos(apex.position().add(-3,0,2));
            var pounce = new ApexAbilityGoal(apex);
            h.assertTrue(pounce.canUse(), "A rested boss must be able to charge a ranged pounce");
            pounce.start();
            while (pounce.canContinueToUse()) pounce.tick();
            pounce.stop();
            h.assertTrue(apex.getDeltaMovement().horizontalDistanceSqr() > .1 && apex.getDeltaMovement().y > 0,
                    "The warned pounce must produce bounded forward and upward motion");
            apex.hurtServer(level,level.damageSources().playerAttack(player),10000);
            var drops = level.getEntitiesOfClass(ItemEntity.class,new AABB(absolute).inflate(5));
            var crests = drops.stream().filter(e -> e.getItem().is(ModItems.APEX_CREST.get())).toList();
            h.assertTrue(!apex.isAlive() && crests.size() == 1 && drops.size() == 1,
                    "A credited player kill must drop one crest without ordinary bunnidog materials or legend refunds");
            var crest = crests.getFirst().getItem();
            h.assertTrue(ApexCrestItem.read(crest).orElseThrow().equals(source), "The reward must preserve the exact source line");
            var achievement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","apex_of_the_line"));
            h.assertTrue(achievement != null && player.getAdvancements().getOrStartProgress(achievement).isDone(),
                    "A credited victory must award the native advancement");
            var display = pedestals.getFirst();
            display.setItem(crest.copy());
            var reloaded = (TrophyPedestalBlockEntity) BlockEntity.loadStatic(display.getBlockPos(),display.getBlockState(),
                    display.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
            h.assertTrue(ApexCrestItem.read(reloaded.item()).orElseThrow().equals(source), "Pedestal saves must retain the crest's source data");
        } finally {
            level.getServer().setDifficulty(difficulty,true);
        }
    }
}
