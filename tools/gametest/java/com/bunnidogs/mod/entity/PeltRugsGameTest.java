package com.bunnidogs.mod.entity;

import java.util.List;
import com.bunnidogs.mod.block.PeltRugBlock;
import com.bunnidogs.mod.genetics.Gene;
import com.bunnidogs.mod.genetics.GuavaTier;
import com.bunnidogs.mod.registry.ModBlocks;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PeltRugsGameTest {
    private PeltRugsGameTest() {}
    public static void run(GameTestHelper h) {
        var level = h.getLevel(); var player = h.makeMockServerPlayerInLevel(); player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        for (var tier : GuavaTier.values()) {
            var input = CraftingInput.of(2,1,List.of(new ItemStack(ModItems.GUAVAS.get(tier.level()).get()),new ItemStack(Items.GLASS_BOTTLE)));
            var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow();
            h.assertTrue(recipe.value().assemble(input).is(ModItems.JUICES.get(tier.level()).get()),"Native recipe reload includes every tier carrier with the matching fruit");
        }
        for (var coat : Gene.COAT.alleles()) {
            var pelt = ModItems.PELTS.get(coat.name()).get(); var rug = ModItems.PELT_RUGS.get(coat.name()).get();
            var input = CraftingInput.of(3,1,List.of(new ItemStack(pelt),new ItemStack(pelt),new ItemStack(Items.STRING)));
            var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow();
            h.assertTrue(recipe.value().assemble(input).is(rug),"Each native rug recipe keeps the exact coat of its two pelts");
            var base = h.absolutePos(new BlockPos(2+coat.id()%4,0,2+coat.id()/4));
            level.setBlock(base,Blocks.STONE.defaultBlockState(),3);
            var held = new ItemStack(rug,2); player.setItemInHand(InteractionHand.MAIN_HAND,held);
            player.gameMode.useItemOn(player,level,held,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(base).add(0,.5,0),Direction.UP,base,false));
            var state = level.getBlockState(base.above());
            h.assertTrue(state.is(ModBlocks.PELT_RUGS.get(coat.name()).get()) && held.getCount() == 1
                    && state.getShape(level,base.above()).max(Direction.Axis.Y) == 1.0/16,"Real placement pays one rug and uses native carpet height");
            var facing = state.getValue(PeltRugBlock.FACING);
            h.assertTrue(state.rotate(Rotation.CLOCKWISE_90).getValue(PeltRugBlock.FACING) == facing.getClockWise(),"The hide keeps a native rotatable orientation");
            level.setBlock(base,Blocks.AIR.defaultBlockState(),3);
        }
        var mixed = CraftingInput.of(3,1,List.of(new ItemStack(ModItems.PELTS.get("tan").get()),new ItemStack(ModItems.PELTS.get("cream").get()),new ItemStack(Items.STRING)));
        h.assertTrue(level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,mixed,level).isEmpty(),"Mixed hides cannot silently repaint a coat rug");
        h.runAfterDelay(2,() -> {
            for (var coat : Gene.COAT.alleles()) {
                var pos = h.absolutePos(new BlockPos(2+coat.id()%4,1,2+coat.id()/4));
                int count = level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(.8),
                        e -> e.getItem().is(ModItems.PELT_RUGS.get(coat.name()).get())).stream().mapToInt(e -> e.getItem().getCount()).sum();
                h.assertTrue(level.getBlockState(pos).isAir() && count == 1,"Removing native carpet support drops exactly the placed coat rug");
            }
            h.succeed();
        });
    }
}
