package com.bunnidogs.mod.client.screen;

import com.bunnidogs.mod.genetics.LineageNames;
import com.bunnidogs.mod.network.LineageEditPayload;
import com.bunnidogs.mod.network.LineageResultPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Deliberate, acknowledged player text input; Escape cancels and stale books cannot be overwritten. */
public final class LineageNameScreen extends Screen {
    private final PedigreeScreen parent;
    private final LineageEditPayload source;
    private EditBox name;
    private Button save;
    private String draft;
    private boolean pending, refused;
    public LineageNameScreen(PedigreeScreen parent, LineageEditPayload source) {
        super(Component.translatable("screen.bunnidogs.lineage.name"));
        this.parent = parent; this.source = source; this.draft = source.name();
    }
    @Override protected void init() {
        int left = (this.width-260)/2, top = Math.max(12,(this.height-150)/2);
        this.name = new EditBox(this.font,left,top+42,260,20,this.title);
        this.name.setMaxLength(LineageNames.MAX_CODE_POINTS*2); this.name.setValue(this.draft);
        this.name.setResponder(value -> { this.draft = value; if (this.save != null) this.save.active = !this.pending && !this.refused && LineageNames.clean(value).isPresent(); });
        addRenderableWidget(this.name);
        this.save = Button.builder(Component.translatable("gui.done"),button -> send()).bounds(left,top+109,124,20).build();
        this.save.active = !this.pending && !this.refused && LineageNames.clean(this.draft).isPresent(); addRenderableWidget(this.save);
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),button -> onClose()).bounds(left+136,top+109,124,20).build());
        setInitialFocus(this.name);
    }
    private void send() {
        if (this.pending || this.refused || LineageNames.clean(this.name.getValue()).isEmpty()) return;
        this.pending = true; this.save.active = false;
        ClientPacketDistributor.sendToServer(new LineageEditPayload(this.source.hand(),this.source.records(),this.source.previous(),this.source.genome(),this.name.getValue()));
    }
    public void accept(LineageResultPayload result) {
        if (!this.pending || !result.records().equals(this.source.records()) || !result.previous().equals(this.source.previous())) return;
        this.pending = false;
        if (result.saved()) { this.parent.applyNames(result.names()); onClose(); }
        else { this.refused = true; this.save.active = false; }
    }
    @Override public void onClose() { Minecraft.getInstance().gui.setScreen(this.parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width-260)/2, top = Math.max(12,(this.height-150)/2);
        graphics.fill(left-12,top-12,left+272,top+142,0xF0221C16);
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
        graphics.text(this.font,this.title,left,top,0xFFF4E7C9);
        graphics.text(this.font,Component.translatable("screen.bunnidogs.lineage.hint"),left,top+23,0xFFD2C5A8);
        graphics.text(this.font,Component.translatable("screen.bunnidogs.lineage.local"),left,top+72,0xFFD2C5A8);
        if (this.pending || this.refused || LineageNames.clean(this.draft).isEmpty()) graphics.text(this.font,
                Component.translatable(this.refused ? "screen.bunnidogs.lineage.stale" : this.pending ? "screen.bunnidogs.lineage.saving" : "screen.bunnidogs.lineage.invalid"),left,top+89,0xFFFFC98B);
    }
}
