package com.bunnidogs.mod.client;

import com.bunnidogs.mod.network.ApexDazePayload;
import com.bunnidogs.mod.network.ApexStrugglePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;

/** Only visible during the requested anvil escape mechanic; no ambient tutorial layer. */
@EventBusSubscriber(modid="bunnidogs",value=Dist.CLIENT)
public final class ApexDazeOverlay {
    private ApexDazeOverlay() {}
    private static int nonce,progress;
    private static float yaw,pitch,movement;
    public static void receive(ApexDazePayload packet) {
        var p=Minecraft.getInstance().player;
        if(nonce!=packet.nonce()) { movement=0;if(p!=null){yaw=p.getYRot();pitch=p.getXRot();} }
        nonce=packet.nonce();progress=Math.clamp(packet.progress(),0,360);
    }
    private static void sample() {
        var mc=Minecraft.getInstance();var p=mc.player;
        if(p==null || nonce==0)return;
        if(mc.gui.screen()==null)movement+=Math.abs(Mth.wrapDegrees(p.getYRot()-yaw))+Math.abs(p.getXRot()-pitch);
        yaw=p.getYRot();pitch=p.getXRot();
    }
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();if(mc.player==null || mc.level==null) {nonce=0;movement=0;return;}
        sample();if(nonce!=0 && movement>=1) { int amount=Math.min(72,(int)movement);movement=0;net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new ApexStrugglePayload(nonce,amount)); }
    }
    public static void render(GuiGraphicsExtractor graphics,DeltaTracker delta) {
        sample();if(nonce==0 || Minecraft.getInstance().player==null)return;
        int x=graphics.guiWidth()/2-55,y=graphics.guiHeight()/2+24;
        graphics.fill(x-1,y-1,x+111,y+7,0xFF202020);graphics.fill(x,y,x+110,y+6,0xFF5B5B5B);
        graphics.fill(x,y,x+110*progress/360,y+6,0xFFE0B775);
        var text=Component.translatable("effect.bunnidogs.anvil_daze.escape");var font=Minecraft.getInstance().font;
        graphics.text(font,text,graphics.guiWidth()/2-font.width(text)/2,y+11,0xFFFFFFFF,true);
    }
}
