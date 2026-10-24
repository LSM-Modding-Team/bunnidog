package com.bunnidogs.mod.client.screen;

import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/** Minecraft's actual container border and slots, with readable text on its light surface. */
final class ContainerBackdrop {
    private static final Identifier TEXTURE=Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
    static final int INK=0xFF404040;
    private ContainerBackdrop() {}
    static void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height) {
        // Nine-slice only the empty frame; never stretch the crafting grid baked into the vanilla texture.
        patch(graphics,x+4,y+4,width-8,height-8,4,4,1,1);
        patch(graphics,x,y,4,4,0,0,4,4);
        patch(graphics,x+4,y,width-8,4,4,0,168,4);
        patch(graphics,x+width-4,y,4,4,172,0,4,4);
        patch(graphics,x,y+4,4,height-8,0,4,4,158);
        patch(graphics,x+width-4,y+4,4,height-8,172,4,4,158);
        patch(graphics,x,y+height-4,4,4,0,162,4,4);
        patch(graphics,x+4,y+height-4,width-8,4,4,162,168,4);
        patch(graphics,x+width-4,y+height-4,4,4,172,162,4,4);
    }
    static void slot(GuiGraphicsExtractor graphics,int x,int y) { patch(graphics,x-1,y-1,18,18,7,83,18,18); }
    private static void patch(GuiGraphicsExtractor graphics,int x,int y,int width,int height,int u,int v,int sourceWidth,int sourceHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x,y,(float)u,(float)v,width,height,sourceWidth,sourceHeight,256,256);
    }
    /** Resolve on the client and retain hover/click/formatting styles while darkening bright text runs. */
    static Component text(Component source) {
        var result=Component.empty();
        source.visit((style,run)->{
            int color=style.getColor()==null ? INK&0xFFFFFF : readable(style.getColor().getValue());
            result.append(Component.literal(run).setStyle(style.withColor(color).withShadowColor(0)));
            return Optional.empty();
        },Style.EMPTY);
        return result;
    }
    private static int readable(int color) {
        int r=color>>16&255, g=color>>8&255, b=color&255;
        if (Math.max(r,Math.max(g,b))<=96) return color;
        double scale=96.0/Math.max(r,Math.max(g,b));
        return (int)(r*scale)<<16 | (int)(g*scale)<<8 | (int)(b*scale);
    }
}
