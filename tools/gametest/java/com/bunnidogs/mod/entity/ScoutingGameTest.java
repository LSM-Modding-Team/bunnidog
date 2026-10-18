package com.bunnidogs.mod.entity;

import java.util.Optional;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.Genome;
import com.bunnidogs.mod.genetics.Scouting;
import com.bunnidogs.mod.genetics.Stat;
import com.bunnidogs.mod.genetics.Stats;
import com.bunnidogs.mod.item.CalipersItem;
import com.bunnidogs.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public final class ScoutingGameTest {
    private ScoutingGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) h.setBlock(x,0,z,Blocks.STONE);
        var dog = h.spawnWithNoFreeWill(ModEntities.BUNNIDOG.get(),1.5F,1,3.5F);
        int bat = Gene.EARS.alleleNamed("bat").id(), huge = Gene.EARS.alleleNamed("huge").id();
        var genome = Genome.DEFAULT.with(Gene.EARS,0,bat).with(Gene.EARS,1,bat);
        dog.setGenome(genome); dog.setAge(0); dog.setStats(Stats.ZERO.with(Stat.SENSES,9));
        var player = h.makeMockServerPlayerInLevel();
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.setShiftKeyDown(true);
        var compasses = new ItemStack(Items.COMPASS,3);
        compasses.set(DataComponents.CUSTOM_NAME,Component.literal("Cave route"));
        player.setItemInHand(InteractionHand.OFF_HAND,compasses);
        var near = h.absolutePos(new BlockPos(3,0,3));
        var far = h.absolutePos(new BlockPos(6,1,3));
        level.setBlock(near,Blocks.IRON_ORE.defaultBlockState(),2);
        level.setBlock(far,Blocks.DIAMOND_ORE.defaultBlockState(),2);
        dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingStatus() == Scouting.Status.TRAITS && dog.scoutingWait() == 0 && compasses.getCount() == 3,
                "Senses +9 must refuse without a marker or recovery");
        dog.setStats(Stats.ZERO.with(Stat.SENSES,10)); dog.setGenome(genome.with(Gene.EARS,1,huge));
        dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingWait() == 0 && !compasses.has(DataComponents.LODESTONE_TRACKER),
                "Hidden bat ears must not scout");
        dog.setGenome(genome); dog.setAge(-100); dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingStatus() == Scouting.Status.ADULT && dog.scoutingWait() == 0,"Pups must refuse mineral surveys");
        dog.setAge(0); dog.setHealth(dog.getMaxHealth()/2-.25F); dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingStatus() == Scouting.Status.HEALTH && dog.scoutingWait() == 0,"Surveying needs half health");
        dog.setHealth(dog.getMaxHealth());
        player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE); dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingWait() == 0 && compasses.getCount() == 3,"Adventure permissions must prevent compass changes");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var other = h.spawnWithNoFreeWill(EntityTypes.COW,2,1,5);
        dog.setTarget(other); dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(dog.scoutingStatus() == Scouting.Status.COMBAT && dog.scoutingWait() == 0,"Combat must interrupt surveying");
        dog.setTarget(null); other.discard();
        var listener = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","quiet_listener"));
        var discerning = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("bunnidogs","discerning_ear"));
        h.assertTrue(listener != null && discerning != null && !player.getAdvancements().getOrStartProgress(listener).isDone(),
                "Refused surveys must not grant an exploration milestone");
        // A player may keep a scout safely on a lead; measuring does not request a journey.
        dog.setLeashedTo(player,false);
        var originalStats = dog.stats(); float health = dog.getHealth();
        dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(player.getAdvancements().getOrStartProgress(listener).isDone()
                && !player.getAdvancements().getOrStartProgress(discerning).isDone(),
                "A successful general survey grants its native milestone without granting focused work");
        var expected = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),near)),false);
        int slot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (expected.equals(player.getInventory().getItem(i).get(DataComponents.LODESTONE_TRACKER))) slot = i;
        h.assertTrue(slot >= 0 && compasses.getCount() == 2 && !compasses.has(DataComponents.LODESTONE_TRACKER),
                "A real survey must split exactly one named off-hand compass for the nearest ore");
        var marked = player.getInventory().removeItem(slot,1);
        h.assertTrue(marked.getHoverName().getString().equals("Cave route") && dog.scoutingWait() == Scouting.RECOVERY_TICKS
                && dog.genome().equals(genome) && dog.stats().equals(originalStats) && dog.getHealth() == health
                && level.getBlockState(near).is(Blocks.IRON_ORE) && level.getBlockState(far).is(Blocks.DIAMOND_ORE),
                "Surveying preserves whole compass details and animal/ore state");
        player.setItemInHand(InteractionHand.OFF_HAND,marked);
        level.setBlock(near,Blocks.STONE.defaultBlockState(),2);
        marked.getItem().inventoryTick(marked,level,player,EquipmentSlot.OFFHAND);
        dog.setGenome(Genome.DEFAULT); dog.setGenome(genome); dog.setStats(Stats.ZERO.with(Stat.SENSES,20));
        dog.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(expected.equals(marked.get(DataComponents.LODESTONE_TRACKER)) && dog.scoutingWait() == Scouting.RECOVERY_TICKS,
                "Mining, gene/stat edits and repeated use must retain the place and shared recovery");
        CalipersItem.showCare(player,dog);
        h.assertTrue(dog.scoutingWait() == Scouting.RECOVERY_TICKS && expected.equals(marked.get(DataComponents.LODESTONE_TRACKER)),
                "Calipers must not scan or restart scouting recovery");
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        dog.saveWithoutId(saved);
        var loaded = ModEntities.BUNNIDOG.get().create(level,EntitySpawnReason.COMMAND);
        var tag = saved.buildResult();
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
        h.assertTrue(loaded.scoutingWait() == Scouting.RECOVERY_TICKS,"Native saves must retain remaining survey recovery");
        tag.putInt("ScoutWait",99999); loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
        h.assertTrue(loaded.scoutingWait() == Scouting.RECOVERY_TICKS,"Malformed excessive survey recovery must clamp");
        tag.remove("ScoutWait"); loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));
        h.assertTrue(loaded.scoutingWait() == 0,"Legacy animals must begin ready");
        loaded.setPos(dog.position()); loaded.setHealth(loaded.getMaxHealth()); loaded.setTarget(null);
        level.setBlock(far,Blocks.STONE.defaultBlockState(),2);
        var outside = h.absolutePos(new BlockPos(7,1,4));
        level.setBlock(outside,Blocks.ANCIENT_DEBRIS.defaultBlockState(),2);
        h.assertTrue(MineralScouting.nearest(level,dog.blockPosition()) == null,
                "Diagonal ore outside the six-block sphere must be excluded");
        var edge = h.absolutePos(new BlockPos(7,1,3));
        level.setBlock(edge,Blocks.GOLD_ORE.defaultBlockState(),2);
        h.assertTrue(edge.equals(MineralScouting.nearest(level,dog.blockPosition())),"Exactly six blocks must remain surveyable");
        level.setBlock(edge,Blocks.RAW_IRON_BLOCK.defaultBlockState(),2);
        h.assertTrue(MineralScouting.nearest(level,dog.blockPosition()) == null,"Mineral storage blocks must not count as ore");
        var remote = dog.blockPosition().offset(4096,0,4096);
        h.assertTrue(!level.hasChunkAt(remote) && MineralScouting.nearest(level,remote) == null && !level.hasChunkAt(remote),
                "An entirely unloaded survey must never request a chunk");
        var coal = h.absolutePos(new BlockPos(2,1,3));
        level.setBlock(coal,Blocks.COAL_ORE.defaultBlockState(),2);
        level.setBlock(far,Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(),2);
        var mineral = new ItemStack(Items.DIAMOND,2);
        mineral.set(DataComponents.CUSTOM_NAME,Component.literal("Focus gem"));
        player.setItemInHand(InteractionHand.MAIN_HAND,mineral);
        loaded.setStats(Stats.ZERO.with(Stat.SENSES,19));
        loaded.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(loaded.scoutingWait() == 0 && expected.equals(marked.get(DataComponents.LODESTONE_TRACKER)),
                "A requested focus at Senses +19 must refuse without spending the general survey");
        h.assertTrue(!player.getAdvancements().getOrStartProgress(discerning).isDone(),"Refused focus must not grant advanced work");
        loaded.setStats(Stats.ZERO.with(Stat.SENSES,20));
        loaded.mobInteract(player,InteractionHand.OFF_HAND);
        var diamond = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(),far)),false);
        h.assertTrue(diamond.equals(marked.get(DataComponents.LODESTONE_TRACKER)) && mineral.getCount() == 2
                && mineral.getHoverName().getString().equals("Focus gem") && loaded.scoutingWait() == Scouting.RECOVERY_TICKS,
                "A focused native survey must skip nearer coal, include deepslate ores and preserve its named mineral");
        h.assertTrue(player.getAdvancements().getOrStartProgress(discerning).isDone(),
                "A successful focused survey must grant the advanced native milestone");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.RAW_IRON,2));
        loaded.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(loaded.scoutingWait() == Scouting.RECOVERY_TICKS && diamond.equals(marked.get(DataComponents.LODESTONE_TRACKER)),
                "Changing the requested ore family must not bypass shared recovery");
        resetRecovery(loaded);
        loaded.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(loaded.scoutingWait() == Scouting.RECOVERY_TICKS && diamond.equals(marked.get(DataComponents.LODESTONE_TRACKER))
                && player.getMainHandItem().getCount() == 2,
                "An empty focused iron search must not fall back to nearby coal/diamond or consume its mineral");
        resetRecovery(loaded); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        level.setBlock(coal,Blocks.STONE.defaultBlockState(),2); level.setBlock(far,Blocks.STONE.defaultBlockState(),2);
        loaded.mobInteract(player,InteractionHand.OFF_HAND);
        h.assertTrue(loaded.scoutingWait() == Scouting.RECOVERY_TICKS && diamond.equals(marked.get(DataComponents.LODESTONE_TRACKER)),
                "An empty survey must rest without changing an old compass marker");
    }
    private static void resetRecovery(Bunnidog dog) {
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,dog.level().registryAccess());
        dog.saveWithoutId(saved);
        var tag = saved.buildResult(); tag.remove("ScoutWait");
        dog.load(TagValueInput.create(ProblemReporter.DISCARDING,dog.level().registryAccess(),tag));
    }
}
