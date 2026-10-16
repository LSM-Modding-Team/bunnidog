package com.bunnidogs.mod.item;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

/** Native book UI/packets, translated pages and local page links; no custom screen or payload. */
public final class FieldGuideItem extends WrittenBookItem {
    public static final int PAGE_COUNT = 33;
    public FieldGuideItem(Properties properties) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, content()));
    }
    private static WrittenBookContent content() {
        var pages = new ArrayList<Filterable<Component>>(PAGE_COUNT);
        var contents = Component.translatable("guide.bunnidogs.title").withStyle(ChatFormatting.BOLD)
                .append(Component.literal("\n\n").withStyle(ChatFormatting.RESET));
        for (int page : new int[]{2,4,8,11,15,17,19,20,24,25,27}) {
            contents.append(Component.translatable("guide.bunnidogs.index." + page).withStyle(style -> style
                    .withBold(false).withColor(ChatFormatting.DARK_GREEN).withUnderlined(true)
                    .withClickEvent(new ClickEvent.ChangePage(page))));
            contents.append("\n");
        }
        pages.add(Filterable.passThrough(contents));
        for (int page = 2; page <= PAGE_COUNT; page++) {
            var text = Component.translatable("guide.bunnidogs.page." + page + ".title").withStyle(ChatFormatting.BOLD)
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(Component.translatable("guide.bunnidogs.page." + page + ".body").withStyle(style -> style.withBold(false)))
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(Component.translatable("guide.bunnidogs.contents").withStyle(style -> style
                            .withBold(false).withUnderlined(true).withColor(ChatFormatting.DARK_GREEN)
                            .withClickEvent(new ClickEvent.ChangePage(1))));
            pages.add(Filterable.passThrough(text));
        }
        return new WrittenBookContent(Filterable.passThrough("Bunnidogs Guide"), "Warren Keepers", 0, pages, true);
    }
}
