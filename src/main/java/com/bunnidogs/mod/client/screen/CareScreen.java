package com.bunnidogs.mod.client.screen;

import java.util.ArrayList;
import java.util.List;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CareView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Animal actions sit beside the visible world; stations retain their container inspection layout. */
public final class CareScreen extends AbstractContainerScreen<CareMenu> {
    private CareView shown;
    private ScrollableLayout body;
    private LinearLayout content;
    private MultiLineTextWidget next;
    private int panelWidth,panelHeight;
    private final List<MultiLineTextWidget> rows=new ArrayList<>();
    private final List<Button> controls=new ArrayList<>();
    private final List<Integer> actions=new ArrayList<>();
    private boolean choosing;
    private int pendingChoice=-1;
    public CareScreen(CareMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    @Override protected void init() {
        super.init();
        arrangePanel(); build(0);
    }
    private boolean animal() { return menu.view()!=null && menu.view().subjectId()>=0; }
    private void arrangePanel() {
        panelWidth=Math.min(animal() ? 214 : 420,this.width-24);
        int desired=360;
        if (animal()) {
            var value=menu.view(); desired=70+27*value.controls().size();
            for (var row:value.rows()) desired+=7+9*font.split(row,panelWidth-36).size();
            if (!value.next().getString().isEmpty()) desired+=7+9*font.split(value.next(),panelWidth-36).size();
            desired=Math.min(310,desired);
        }
        panelHeight=Math.min(desired,this.height-24);
        leftPos=animal() ? width-panelWidth-12 : (width-panelWidth)/2;
        topPos=(height-panelHeight)/2;
    }
    private void build(double scroll) {
        clearWidgets(); rows.clear(); controls.clear(); actions.clear();
        arrangePanel();
        content=LinearLayout.vertical().spacing(7);
        int textWidth=panelWidth-36;
        shown=menu.view();
        next=new MultiLineTextWidget(advice(shown),font).setMaxWidth(textWidth);
        if (!animal() || !advice(shown).getString().isEmpty()) content.addChild(next);
        if (shown!=null) {
            if (choosing) addControls(shown,CareView.Control.CHOICE,textWidth);
            else {
                if (animal()) {
                    addControls(shown,CareView.Control.PRIMARY,textWidth);
                    for (var row:shown.rows()) addRow(row,textWidth);
                } else {
                    for (int i=0;i<Math.min(2,shown.rows().size());i++) addRow(shown.rows().get(i),textWidth);
                    addControls(shown,CareView.Control.PRIMARY,textWidth);
                    for (int i=2;i<shown.rows().size();i++) addRow(shown.rows().get(i),textWidth);
                }
                addControls(shown,CareView.Control.DETAIL,textWidth);
            }
        }
        body=new ScrollableLayout(minecraft,content,panelHeight-70,ScrollableLayout.ReserveStrategy.RIGHT);
        body.setMinWidth(panelWidth-24); body.setScrollbarSpacing(6);
        position(); body.visitWidgets(this::addRenderableWidget);
        body.visitChildren(child->{ if (child instanceof AbstractScrollArea area) area.setScrollAmount(scroll); });
        boolean hasChoices=shown!=null && shown.controls().stream().anyMatch(c->c.section()==CareView.Control.CHOICE);
        int footerWidth=hasChoices ? (panelWidth-32)/2 : panelWidth-24;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(leftPos+12,topPos+panelHeight-28,footerWidth,20).build());
        if (hasChoices) addRenderableWidget(Button.builder(Component.translatable(choosing ? "care.bunnidogs.choose.back" : "care.bunnidogs.choose.open"),b->{
            choosing=!choosing; pendingChoice=-1; build(0);
        }).bounds(leftPos+20+footerWidth,topPos+panelHeight-28,footerWidth,20).build());
    }
    private Component advice(CareView value) {
        return value==null ? Component.translatable("care.bunnidogs.loading") : choosing ? Component.translatable("care.bunnidogs.choose.hint") : value.next();
    }
    private void addRow(Component text,int width) { rows.add(content.addChild(new MultiLineTextWidget(text,font).setMaxWidth(width))); }
    private void addControls(CareView value,int section,int width) {
        var group=value.controls().stream().filter(c->c.section()==section).toList();
        int columns=animal() ? 1 : 2;
        for (int i=0;i<group.size();i+=columns) {
            var line=content.addChild(LinearLayout.horizontal().spacing(8));
            for (int j=i;j<Math.min(i+columns,group.size());j++) {
                var control=group.get(j); int action=control.id()&255;
                var button=Button.builder(control.label(),pressed->{
                    if (minecraft.gameMode!=null && shown!=null) shown.controls().stream().filter(c->(c.id()&255)==action && c.enabled()).findFirst().ifPresent(c->{
                        if (c.section()==CareView.Control.CHOICE) pendingChoice=action;
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,c.id());
                    });
                }).width(columns==1 ? width : (width-8)/2).build();
                if (!control.hint().getString().isEmpty()) button.setTooltip(Tooltip.create(control.hint()));
                button.active=control.enabled(); controls.add(line.addChild(button)); actions.add(action);
            }
        }
    }
    private void position() { body.arrangeElements(); body.setX(leftPos+12); body.setY(topPos+30); }
    @Override protected void containerTick() {
        super.containerTick(); var value=menu.view();
        if (value==null || value==shown) return;
        if (value.subjectId()>=0) {
            double[] scroll={0}; if (body!=null) body.visitChildren(child->{if (child instanceof AbstractScrollArea area) scroll[0]=area.scrollAmount();});
            build(scroll[0]); return;
        }
        if (pendingChoice>=0 && value.controls().stream().anyMatch(c->(c.id()&255)==pendingChoice && !c.enabled())) {
            choosing=false; pendingChoice=-1; build(0); return;
        }
        var visible=value.controls().stream().filter(c->choosing ? c.section()==CareView.Control.CHOICE : c.section()!=CareView.Control.CHOICE)
                .sorted(java.util.Comparator.comparingInt(CareView.Control::section)).toList();
        boolean same=shown!=null && (choosing ? rows.isEmpty() : value.rows().size()==rows.size()) && visible.size()==controls.size();
        if (same) for (int i=0;i<controls.size();i++) if (actions.get(i)!=(visible.get(i).id()&255)) { same=false; break; }
        if (!same) {
            double[] scroll={0}; if (body!=null) body.visitChildren(child->{if (child instanceof AbstractScrollArea area) scroll[0]=area.scrollAmount();});
            build(scroll[0]); return;
        }
        shown=value; next.setMessage(advice(value));
        for (int i=0;i<rows.size();i++) rows.get(i).setMessage(value.rows().get(i));
        for (int i=0;i<controls.size();i++) { controls.get(i).setMessage(visible.get(i).label()); controls.get(i).active=visible.get(i).enabled(); }
        position();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        graphics.fill(leftPos-1,topPos-1,leftPos+panelWidth+1,topPos+panelHeight+1,0xFFE1C790);
        graphics.fill(leftPos,topPos,leftPos+panelWidth,topPos+panelHeight,0xF0221C16);
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        if (!animal()) super.extractBackground(graphics,mouseX,mouseY,partialTick);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int x,int y) {
        graphics.text(font,font.split(choosing ? Component.translatable("care.bunnidogs.choose.title") : shown==null ? title : shown.title(),panelWidth-24).getFirst(),12,10,0xFFF4E7C9,false);
    }
}
