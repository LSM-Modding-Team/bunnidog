package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import com.bunnidogs.mod.registry.ModPoi;
import com.bunnidogs.mod.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Exercise the native profession behavior, datapack offers, two-input payment and saved merchant state. */
public final class BreederGameTest {
    private BreederGameTest() {}
    private static Villager breeder(GameTestHelper h, int level) {
        var villager = h.spawnWithNoFreeWill(EntityTypes.VILLAGER,2,151,2);
        villager.setVillagerData(villager.getVillagerData().withProfession(ModVillagers.BREEDER).withLevel(level));
        return villager;
    }
    public static void run(GameTestHelper h) {
        var level = h.getLevel();
        for (int x = 0; x < 7; x++) for (int z = 0; z < 6; z++) h.setBlock(new BlockPos(x,150,z),Blocks.STONE);
        var job = h.absolutePos(new BlockPos(3,151,2));
        level.setBlockAndUpdate(job,ModBlocks.BREEDING_PEN.get().defaultBlockState());
        var recruit = h.spawnWithNoFreeWill(EntityTypes.VILLAGER,2,151,2);
        h.runAtTickTime(3,() -> {
            var poi = level.getPoiManager();
            h.assertTrue(poi.getType(job).orElseThrow().is(ModPoi.BREEDING_PEN.getKey())
                    && ModVillagers.BREEDER.get().acquirableJobSite().test(ModPoi.BREEDING_PEN)
                    && !ModVillagers.BREEDER.get().heldJobSite().test(ModPoi.NEST),"All Pen states are a native villager job site, independent of animal Nests");
            var claimed = poi.take(type -> type.is(ModPoi.BREEDING_PEN.getKey()),(type,pos) -> pos.equals(job),job,1);
            h.assertTrue(claimed.orElseThrow().equals(job),"A new breeder can claim the actual free Pen ticket");
            recruit.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE,GlobalPos.of(level.dimension(),job));
            h.assertTrue(AssignProfessionFromJobSite.create().tryStart(level,recruit,level.getGameTime())
                    && recruit.getVillagerData().profession().is(ModVillagers.BREEDER.getKey())
                    && recruit.getBrain().getMemory(MemoryModuleType.JOB_SITE).orElseThrow().pos().equals(job),
                    "Native villager job assignment must choose the mod's Breeder profession at a Pen");
            var originalOffers = recruit.getOffers();
            h.assertTrue(originalOffers.size() == 3 && originalOffers.stream().anyMatch(o -> o.getResult().is(ModItems.FIELD_GUIDE.get()))
                    && originalOffers.stream().anyMatch(o -> o.getBaseCostA().is(ModItems.GUAVA.get())),"Novices expose their three actual data-driven offers");
            var guava = originalOffers.stream().filter(o -> o.getBaseCostA().is(ModItems.GUAVA.get())).findFirst().orElseThrow();
            var payment = new ItemStack(ModItems.GUAVA.get(),21);
            h.assertTrue(guava.take(payment,ItemStack.EMPTY) && payment.getCount() == 1 && guava.assemble().is(Items.EMERALD),
                    "The native guava trade consumes its actual twenty fruit");
            recruit.notifyTrade(guava);
            h.assertTrue(recruit.getVillagerXp() == guava.getXp() && guava.getUses() == 1,"Successful trades gain native merchant XP and uses");
            for (int rank = 2; rank <= 5; rank++) {
                var vendor = breeder(h,rank);
                h.assertTrue(vendor.getOffers().size() == 2,"Every later rank must generate two valid native offers: "+rank);
                vendor.discard();
            }
            var types = java.util.List.of(VillagerType.PLAINS,VillagerType.DESERT,VillagerType.SAVANNA,VillagerType.TAIGA,VillagerType.SNOW,VillagerType.SWAMP,VillagerType.JUNGLE);
            String[] coats = {"tan","sand","cream","brown","snow","moss","spotted"};
            for (int i = 0; i < types.size(); i++) {
                var vendor = breeder(h,2);
                vendor.setVillagerData(vendor.getVillagerData().withType(level.registryAccess(),types.get(i)));
                var pelt = ModItems.PELTS.get(coats[i]).get();
                h.assertTrue(vendor.getOffers().size() == 2 && vendor.getOffers().stream().anyMatch(o -> o.getBaseCostA().is(pelt))
                        && vendor.getOffers().stream().anyMatch(o -> o.getResult().is(ModItems.SYRINGE.get())),
                        "Merchant predicates must pick exactly the local pelt contract and syringe: "+coats[i]);
                vendor.discard();
            }
            var master = breeder(h,5);
            var diamond = master.getOffers().stream().filter(o -> o.getResult().is(ModItems.DIAMOND_GUAVA.get())).findFirst().orElseThrow();
            var emeralds = diamond.getCostA().copy(); var shortDiamonds = new ItemStack(Items.DIAMOND,3);
            int coins = emeralds.getCount();
            h.assertTrue(!diamond.take(emeralds,shortDiamonds) && emeralds.getCount() == coins && shortDiamonds.getCount() == 3,
                    "An incomplete second payment must keep both native inputs");
            var diamonds = new ItemStack(Items.DIAMOND,5);
            h.assertTrue(diamond.take(emeralds,diamonds) && emeralds.isEmpty() && diamonds.getCount() == 1,
                    "Master fruit still consumes four actual diamonds alongside its emerald price");
            master.notifyTrade(diamond);
            var splicer = master.getOffers().stream().filter(o -> o.getResult().is(ModItems.GENE_SPLICER.get())).findFirst().orElseThrow();
            h.assertTrue(splicer.getCostB().is(ModItems.MUTANT_MARROW.get()) && splicer.getCostB().getCount() == 1,
                    "A bought Splicer retains the rare Mutant Marrow gate");
            var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); master.saveWithoutId(saved);
            var loaded = EntityTypes.VILLAGER.create(level,EntitySpawnReason.LOAD);
            loaded.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            h.assertTrue(loaded.getVillagerData().profession().is(ModVillagers.BREEDER.getKey()) && loaded.getVillagerData().level() == 5
                    && loaded.getVillagerXp() == master.getVillagerXp() && loaded.getOffers().stream().anyMatch(o -> o.getResult().is(ModItems.DIAMOND_GUAVA.get()) && o.getUses() == 1),
                    "Native merchant reload must keep the profession, career, exact offers and paid uses");
            for (int i = diamond.getUses(); i < diamond.getMaxUses(); i++) diamond.increaseUses();
            h.assertTrue(diamond.isOutOfStock(),"Native trade stock must exhaust normally"); master.restock();
            h.assertTrue(!diamond.isOutOfStock() && diamond.getUses() == 0,"Native working restocks replenish offers without custom inventory logic");
            level.removeBlock(job,false); recruit.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
            ResetProfession.create().tryStart(level,recruit,level.getGameTime());
            h.assertTrue(recruit.getVillagerData().profession().is(ModVillagers.BREEDER.getKey()),"Trading locks the career even after its Pen is lost");
            var untraded = breeder(h,1); untraded.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
            ResetProfession.create().tryStart(level,untraded,level.getGameTime());
            h.assertTrue(untraded.getVillagerData().profession().is(net.minecraft.world.entity.npc.villager.VillagerProfession.NONE),
                    "An untraded unemployed adult can leave the native career normally");
            master.discard(); recruit.discard(); untraded.discard(); h.succeed();
        });
    }
}
