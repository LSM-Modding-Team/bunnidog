package com.bunnidogs.mod.menu;

import java.util.List;
import net.minecraft.network.chat.Component;

/** A small, server-authored snapshot. Buttons use stable meanings, never row indices. */
public record CareView(Component title, Component next, List<Component> rows, List<Control> controls) {
    public CareView {
        rows=List.copyOf(rows); controls=List.copyOf(controls);
        if (rows.size()>12 || controls.size()>12) throw new IllegalArgumentException("Oversized care view");
    }
    public record Control(int id,Component label,boolean enabled,int section) {
        public static final int PRIMARY=0, DETAIL=1, CHOICE=2;
        public Control { if (section<PRIMARY || section>CHOICE) throw new IllegalArgumentException("Unknown care section"); }
        public Control(int id,Component label,boolean enabled) { this(id,label,enabled,PRIMARY); }
    }
    public boolean permits(int id) { return controls.stream().anyMatch(c->c.id()==id && c.enabled()); }
}
