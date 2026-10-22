package com.bunnidogs.mod.menu;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A small, server-authored snapshot. Buttons use stable meanings, never row indices. */
public record CareView(Component title, Component next, List<Component> rows, List<Control> controls,
        int subjectId, int page, List<Stock> stock) {
    public CareView(Component title,Component next,List<Component> rows,List<Control> controls) { this(title,next,rows,controls,-1); }
    public CareView(Component title,Component next,List<Component> rows,List<Control> controls,int subjectId) {
        this(title,next,rows,controls,subjectId,0,List.of());
    }
    public CareView {
        rows=List.copyOf(rows); controls=List.copyOf(controls); stock=List.copyOf(stock);
        if (rows.size()>12 || controls.size()>12 || stock.size()>6) throw new IllegalArgumentException("Oversized care view");
    }
    public CareView withStock(Stock... stock) {
        return new CareView(title,next,rows,controls,subjectId,page,List.of(stock));
    }
    /** A display of actual stock, never a second client inventory or a source for payment. */
    public record Stock(Component label,ItemStack item) {
        public Stock { item=item.copy(); }
        @Override public boolean equals(Object other) {
            return other instanceof Stock value && label.equals(value.label) && ItemStack.matches(item,value.item);
        }
        @Override public int hashCode() { return 31*label.hashCode()+31*item.getCount()+ItemStack.hashItemAndComponents(item); }
    }
    public record Control(int id,Component label,boolean enabled,int section,Component hint) {
        public static final int PRIMARY=0, DETAIL=1, CHOICE=2;
        public Control { if (section<PRIMARY || section>CHOICE) throw new IllegalArgumentException("Unknown care section"); }
        public Control(int id,Component label,boolean enabled) { this(id,label,enabled,PRIMARY,Component.empty()); }
        public Control(int id,Component label,boolean enabled,int section) { this(id,label,enabled,section,Component.empty()); }
    }
    public boolean permits(int id) { return controls.stream().anyMatch(c->c.id()==id && c.enabled()); }
}
