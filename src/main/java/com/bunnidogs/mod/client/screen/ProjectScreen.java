package com.bunnidogs.mod.client.screen;

import com.bunnidogs.mod.menu.ProjectMenu;
import com.bunnidogs.mod.registry.ModItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Ordinary Minecraft frame, real slots, three cost icons and one live state. */
public final class ProjectScreen extends AbstractContainerScreen<ProjectMenu> {
    private net.minecraft.client.gui.components.Button launch;
    public ProjectScreen(ProjectMenu menu,Inventory inventory,Component title){super(menu,inventory,title,ProjectMenu.WIDTH,ProjectMenu.HEIGHT);}
    @Override protected void init(){super.init();if(menu.kind.equals("launch_aerie"))launch=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable("project.bunnidogs.launch"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+68,topPos+44,76,20).build());updateButton();}
    private void updateButton(){if(launch!=null)launch.active=menu.state().equals("ready");}
    @Override protected void containerTick(){super.containerTick();updateButton();}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
        g.fill(0,0,width,height,0x60000000);ContainerBackdrop.draw(g,leftPos,topPos,imageWidth,imageHeight);
        for(var slot:menu.slots)ContainerBackdrop.slot(g,leftPos+slot.x,topPos+slot.y);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) {
        mouseX-=leftPos;mouseY-=topPos;
        g.text(font,ContainerBackdrop.text(title),8,6,ContainerBackdrop.INK,false);
        g.text(font,Component.translatable("container.inventory"),26,136,ContainerBackdrop.INK,false);
        var status=Component.translatable("project.bunnidogs."+menu.state());
        g.text(font,status,(imageWidth-font.width(status))/2,66,ContainerBackdrop.INK,false);
        if(menu.workshop()) {
            g.text(font,Component.literal("→"),112,28,menu.state().equals("working") || menu.state().equals("ready") ? 0xFF3B7133 : 0xFF666666,false);
            if(menu.binding())g.text(font,Component.literal("+"),57,28,ContainerBackdrop.INK,false);
            var costs=menu.job().costs();
            for(int i=0;i<costs.size();i++)icon(g,costs.get(i),28+i*58,46,mouseX,mouseY);
        } else if(menu.kind.equals("bastion_heart")) {
            meter(g,new ItemStack(ModItems.CINDERITE_INGOT.get()),menu.fuel(1),1200,27,29,mouseX,mouseY);
            meter(g,new ItemStack(ModItems.STARLIT_DUST.get()),menu.fuel(2),12000,119,29,mouseX,mouseY);
        } else {
            var costs=menu.kind.equals("launch_aerie") ? java.util.List.of(new ItemStack(ModItems.CINDERITE_INGOT.get(),2),new ItemStack(ModItems.STARLIT_DUST.get())) : java.util.List.of(new ItemStack(com.bunnidogs.mod.guild.GuildProjects.material("nursery_bedding")),new ItemStack(ModItems.GUAVA.get()));
            for(int i=0;i<costs.size();i++)icon(g,costs.get(i),47+i*76,menu.kind.equals("launch_aerie") ? 24 : 30,mouseX,mouseY);
            if(menu.kind.equals("nursery_hearth")) {g.fill(28,53,184,57,0xFF555555);g.fill(29,54,29+154*menu.fuel(3)/600,56,0xFF78A265);}
        }
    }
    private void icon(GuiGraphicsExtractor g,ItemStack stack,int x,int y,int mouseX,int mouseY) {
        g.item(stack,x,y,0);g.itemDecorations(font,stack,x,y,null);
        if(mouseX>=x && mouseX<x+32 && mouseY>=y && mouseY<y+16)g.setTooltipForNextFrame(font,stack,mouseX+leftPos,mouseY+topPos);
    }
    private void meter(GuiGraphicsExtractor g,ItemStack stack,int fuel,int max,int x,int y,int mouseX,int mouseY) {
        icon(g,stack,x,y,mouseX,mouseY);g.fill(x+21,y+5,x+67,y+11,0xFF555555);g.fill(x+22,y+6,x+22+44*Math.clamp(fuel,0,max)/max,y+10,0xFF78A265);
    }
}
