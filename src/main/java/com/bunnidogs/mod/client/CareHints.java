package com.bunnidogs.mod.client;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.item.GuavaItem;
import com.bunnidogs.mod.item.SeedItem;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Quiet, local affordances. Never writes the action bar, chat or server state. */
public final class CareHints {
    private CareHints() {}
    public static void render(GuiGraphicsExtractor graphics,DeltaTracker delta) {
        var mc=Minecraft.getInstance(); var player=mc.player;
        if (player==null || mc.level==null || mc.gui.screen()!=null || mc.gui.hud.isHidden() || player.isSpectator()) return;
        String key=key(mc); if (key==null) return;
        var hint=Component.translatable("hint.bunnidogs."+key,mc.options.keyUse.getTranslatedKeyMessage(),
                mc.options.keyShift.getTranslatedKeyMessage(),mc.options.keyAttack.getTranslatedKeyMessage());
        var lines=mc.font.split(hint,Math.min(360,graphics.guiWidth()-24));
        int y=graphics.guiHeight()/2+20;
        for (int i=0;i<Math.min(2,lines.size());i++) {
            var line=lines.get(i); int w=mc.font.width(line), x=(graphics.guiWidth()-w)/2;
            graphics.fill(x-4,y-2,x+w+4,y+10,0xA0181510);
            graphics.text(mc.font,line,x,y,0xFFF0E1BD); y+=11;
        }
    }
    private static String key(Minecraft mc) {
        var player=mc.player; var held=player.getMainHandItem().isEmpty() ? player.getOffhandItem() : player.getMainHandItem();
        boolean empty=held.isEmpty() && player.getOffhandItem().isEmpty();
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Bunnidog dog && dog.acceptsCare() && dog.isAlive()) {
            if (empty) return dog.isLeashed() && dog.getLeashHolder()==player ? "unleash"
                    : player.isShiftKeyDown() && dog.carriedBundleCount()>0 ? "bundle" : "animal";
            if (held.getItem() instanceof GuavaItem) return dog.isBaby() ? "grow" : player.isShiftKeyDown() ? "heal" : "breed";
            if (held.is(ModItems.TRAIL_WHISTLE.get())) return "whistle";
            if (held.is(Items.BRUSH)) return "brush";
            if (held.is(ModItems.CALIPERS.get())) return "calipers";
            if (held.getItem() instanceof com.bunnidogs.mod.item.SyringeItem) return "syringe";
            if (held.is(ModItems.PEDIGREE_BOOK.get())) return "book";
            if (held.is(Items.BOWL)) return "bowl";
            if (held.is(Items.COMPASS)) return "compass";
            if (held.getItem() instanceof net.minecraft.world.item.BundleItem) return "pack";
            if (dog.canEat(held)) return "feed";
            return null;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit)) return null;
        var state=mc.level.getBlockState(hit.getBlockPos()); var block=state.getBlock();
        if (block instanceof GuavaFruitBlock) {
            if (GuavaFruitBlock.isRipe(state)) return player.isShiftKeyDown() && !empty ? "fruit_release" : "fruit_ripe";
            return held.is(Items.BONE_MEAL) ? "fruit_bonemeal" : "fruit_growing";
        }
        if (block instanceof SeedCropBlock) return SeedCropBlock.isRipe(state) ? "crop_ripe" : "crop_growing";
        if (held.getItem() instanceof SeedItem && state.is(Blocks.FARMLAND)) return "plant";
        if (block instanceof GeneticsTableBlock) {
            if (empty) return player.isShiftKeyDown() ? "table_take" : "table";
            if (com.bunnidogs.mod.item.TissueSampleItem.read(held).isPresent()) return "sample";
            if (mc.level.getBlockEntity(hit.getBlockPos()) instanceof GeneticsTableBlockEntity table && table.stockSignal()>0) return "table_read";
        }
        if (block instanceof JudgingStandBlock) {
            if (empty) return player.isShiftKeyDown() ? "show_preview" : "show";
            if (held.is(Items.LEAD)) return "judge";
        }
        if (block instanceof SpecimenJarBlock) {
            if (empty) return player.isShiftKeyDown() ? "jar_take" : "jar";
            if (com.bunnidogs.mod.item.TissueSampleItem.read(held).isPresent()) return "jar_sample";
        }
        if (block instanceof PedigreeComparatorBlock) {
            if (empty) return player.isShiftKeyDown() ? "detector_clear" : "detector";
            if (com.bunnidogs.mod.item.TissueSampleItem.read(held).isPresent()) return "detector_copy";
        }
        if (block instanceof TrophyPedestalBlock) return empty ? player.isShiftKeyDown() ? "trophy_take" : "trophy" : "trophy_place";
        if (block instanceof ApexCrucibleBlock && (empty || held.is(ModItems.TISSUE_SAMPLE.get()) || held.is(ModItems.CALIPERS.get()))) return "apex";
        if (block instanceof FeedingTroughBlock || block instanceof BreedingPenBlock || block instanceof NurseryCradleBlock
                || block instanceof JuicerBlock || block instanceof CommissionBoardBlock || block instanceof NestBlock) {
            if (empty) return player.isShiftKeyDown() ? "station_shortcut" : "station";
            if (block instanceof NestBlock && held.is(Items.LEAD)) return "nest";
            if (block instanceof CommissionBoardBlock && held.is(Items.PAPER)) return "submit";
            if (block instanceof FeedingTroughBlock && FeedingTroughBlockEntity.accepts(held)) return "stock";
            if (block instanceof FeedingTroughBlock && held.getItem() instanceof GuavaItem) return "rich";
            if (block instanceof BreedingPenBlock && held.getItem() instanceof GuavaItem) return "manger";
            if (block instanceof NurseryCradleBlock && held.getItem() instanceof SeedItem) return "cradle_seed";
        }
        return null;
    }
}
