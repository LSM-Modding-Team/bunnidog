package com.bunnidogs.mod.client.screen;

import com.bunnidogs.mod.menu.CareView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/** Read-only stock: real item/count and hover contents, without textual inventory paragraphs. */
final class CareStockWidget extends AbstractWidget {
    private final Minecraft minecraft;
    private CareView.Stock stock;
    CareStockWidget(Minecraft minecraft,int width,CareView.Stock stock) {
        super(0,0,width,54,stock.label()); this.minecraft=minecraft; setStock(stock);
    }
    void setStock(CareView.Stock value) {
        stock=value; setMessage(value.label());
        setTooltip(value.item().isEmpty() ? Tooltip.create(Component.translatable("care.bunnidogs.empty")) : null);
    }
    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        var font=minecraft.font; int slotX=getX()+(width-18)/2, slotY=getY()+30;
        int line=0;
        for (var part:font.split(getMessage(),width)) {
            if (line==2) break;
            graphics.text(font,part,getX()+(width-font.width(part))/2,getY()+2+line++*font.lineHeight,0xFFE0D6C1,true);
        }
        if (isFocused()) graphics.outline(getX(),getY(),width,height,0xFFF4E7C9);
        if (!stock.item().isEmpty()) {
            graphics.item(stock.item(),slotX+1,slotY+1,0);
            graphics.itemDecorations(font,stock.item(),slotX+1,slotY+1,null);
            if (isHoveredOrFocused()) graphics.setTooltipForNextFrame(font,stock.item(),mouseX,mouseY);
        } else graphics.text(font,"—",slotX+4,slotY+4,0xFFB0ABA1,true);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE,stock.label());
        output.add(NarratedElementType.TITLE,stock.item().isEmpty() ? Component.translatable("care.bunnidogs.empty")
                : Component.translatable("care.bunnidogs.stock",stock.item().getCount(),stock.item().getHoverName()));
    }
}
