package com.bunnidogs.mod.client.screen;

import java.util.ArrayList;
import java.util.List;
import com.bunnidogs.mod.menu.CareMenu;
import com.bunnidogs.mod.menu.CareView;
import com.bunnidogs.mod.menu.CarePanels;
import com.bunnidogs.mod.menu.AnimalPanels;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Small action pages over the world, with actual stock and stable navigation. */
public final class CareScreen extends AbstractContainerScreen<CareMenu> {
    private CareView shown;
    private ScrollableLayout body;
    private LinearLayout content;
    private MultiLineTextWidget next;
    private int panelWidth,panelHeight,headerHeight;
    private final List<MultiLineTextWidget> rows=new ArrayList<>();
    private final List<Button> controls=new ArrayList<>();
    private final List<Integer> actions=new ArrayList<>();
    private final List<CareStockWidget> stocks=new ArrayList<>();
    private Button back;
    public CareScreen(CareMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    @Override protected void init() { super.init(); build(0,-1); }
    private boolean animal() { return shown!=null && shown.subjectId()>=0; }
    private boolean backControl(CareView.Control control) {
        return CarePanels.backAction(control.id());
    }
    private List<CareView.Control> visible(CareView value) {
        return value.controls().stream().filter(c->!backControl(c)).sorted(java.util.Comparator.comparingInt(CareView.Control::section)).toList();
    }
    private void build(double scroll,int focus) {
        clearWidgets(); rows.clear(); controls.clear(); actions.clear(); stocks.clear(); back=null;
        shown=menu.view(); if (shown==null) return;
        panelWidth=Math.min(animal() ? 214 : 300,this.width-24);
        panelHeight=Math.min(310,this.height-24);
        leftPos=animal() ? width-panelWidth-12 : (width-panelWidth)/2;
        topPos=animal() ? 12 : (height-panelHeight)/2;
        headerHeight=font.split(shown.title(),panelWidth-24).size()*font.lineHeight;
        content=LinearLayout.vertical().spacing(7);
        int textWidth=panelWidth-36;
        if (!shown.stock().isEmpty()) {
            int columns=Math.min(3,shown.stock().size()), stockWidth=(textWidth-8*(columns-1))/columns;
            for (int i=0;i<shown.stock().size();i+=columns) {
                var line=content.addChild(LinearLayout.horizontal().spacing(8));
                for (int j=i;j<Math.min(i+columns,shown.stock().size());j++) stocks.add(line.addChild(new CareStockWidget(minecraft,stockWidth,shown.stock().get(j))));
            }
        }
        next=new MultiLineTextWidget(shown.next(),font).setMaxWidth(textWidth);
        if (!shown.next().getString().isEmpty()) content.addChild(next);
        if (animal()) addControls(shown,CareView.Control.PRIMARY,textWidth);
        for (var row:shown.rows()) rows.add(content.addChild(new MultiLineTextWidget(row,font).setMaxWidth(textWidth)));
        if (!animal()) addControls(shown,CareView.Control.PRIMARY,textWidth);
        addControls(shown,CareView.Control.DETAIL,textWidth);
        addControls(shown,CareView.Control.CHOICE,textWidth);
        body=new ScrollableLayout(minecraft,content,Math.max(20,panelHeight-headerHeight-54),ScrollableLayout.ReserveStrategy.RIGHT);
        body.setMinWidth(panelWidth-24); body.setScrollbarSpacing(6);
        position(); body.visitWidgets(this::addRenderableWidget);
        body.visitChildren(child->{ if (child instanceof AbstractScrollArea area) area.setScrollAmount(scroll); });
        int footerWidth=(panelWidth-32)/2;
        shown.controls().stream().filter(this::backControl).findFirst().ifPresent(control->{
            back=Button.builder(Component.literal("← ").append(AnimalPanels.text("back")),b->send(control.id()&255))
                    .bounds(leftPos+12,topPos+panelHeight-28,footerWidth,20).build();
            back.active=control.enabled(); addRenderableWidget(back);
        });
        addRenderableWidget(Button.builder(AnimalPanels.text("close"),b->onClose())
                .bounds(leftPos+20+footerWidth,topPos+panelHeight-28,footerWidth,20).build());
        if (focus>=0) for (int i=0;i<controls.size();i++) if (actions.get(i)==focus) {
            var button=controls.get(i);
            body.visitChildren(child->{ if (child instanceof AbstractContainerWidget container) { container.setFocused(button); setFocused(container); } });
            break;
        }
    }
    private void send(int action) {
        // Always use the latest server stamp, including for footer buttons kept across live refreshes.
        if (minecraft.gameMode!=null && shown!=null) shown.controls().stream().filter(c->(c.id()&255)==action && c.enabled())
                .findFirst().ifPresent(c->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,c.id()));
    }
    private void addControls(CareView value,int section,int width) {
        for (var control:visible(value)) if (control.section()==section) {
            int action=control.id()&255;
            var button=Button.builder(control.label(),pressed->send(action)).width(width).build();
            updateButton(button,control); controls.add(content.addChild(button)); actions.add(action);
        }
    }
    private void updateButton(Button button,CareView.Control control) {
        button.setMessage(control.label()); button.active=control.enabled();
        button.setTooltip(control.hint().getString().isEmpty() ? null : Tooltip.create(control.hint()));
    }
    private void position() { body.arrangeElements(); body.setX(leftPos+12); body.setY(topPos+headerHeight+18); }
    @Override protected void containerTick() {
        super.containerTick(); var value=menu.view();
        if (value==null || value==shown) return;
        boolean page=shown!=null && shown.page()==value.page() && shown.subjectId()==value.subjectId();
        var visible=visible(value);
        boolean same=page && value.rows().size()==rows.size() && value.stock().size()==stocks.size() && visible.size()==controls.size()
                && shown.next().getString().isEmpty()==value.next().getString().isEmpty()
                && shown.controls().stream().anyMatch(this::backControl)==value.controls().stream().anyMatch(this::backControl)
                && font.split(value.title(),panelWidth-24).size()*font.lineHeight==headerHeight;
        if (same) for (int i=0;i<controls.size();i++) if (actions.get(i)!=(visible.get(i).id()&255)) { same=false; break; }
        if (!same) {
            double[] scroll={0}; int focus=-1;
            if (page && body!=null) {
                body.visitChildren(child->{if (child instanceof AbstractScrollArea area) scroll[0]=area.scrollAmount();});
                for (int i=0;i<controls.size();i++) if (controls.get(i).isFocused()) focus=actions.get(i);
            }
            build(scroll[0],focus); return;
        }
        shown=value; next.setMessage(value.next());
        for (int i=0;i<rows.size();i++) rows.get(i).setMessage(value.rows().get(i));
        for (int i=0;i<controls.size();i++) updateButton(controls.get(i),visible.get(i));
        for (int i=0;i<stocks.size();i++) stocks.get(i).setStock(value.stock().get(i));
        if (back!=null) value.controls().stream().filter(this::backControl).findFirst().ifPresent(c->back.active=c.enabled());
        position();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        if (menu.view()!=null) super.extractRenderState(graphics,mouseX,mouseY,partialTick);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        if (shown!=null && !animal()) graphics.fill(0,0,width,height,0x60000000);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int x,int y) {
        if (shown==null) return;
        int line=0;
        for (var part:font.split(shown.title(),panelWidth-24)) graphics.text(font,part,12,10+line++*font.lineHeight,0xFFF4E7C9,true);
    }
}
