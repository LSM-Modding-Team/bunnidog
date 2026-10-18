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
    public static final int PAGE_COUNT = 46;
    private static final int MORE_CONTENTS = 40;
    public FieldGuideItem(Properties properties) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, content()));
    }
    private static WrittenBookContent content() {
        var pages = new ArrayList<Filterable<Component>>(PAGE_COUNT);
        pages.add(Filterable.passThrough(contents("guide.bunnidogs.title",new int[]{2,4,41,8,11,45,15,17,19,20},
                "guide.bunnidogs.more_contents",MORE_CONTENTS)));
        for (int page = 2; page <= PAGE_COUNT; page++) {
            if (page == MORE_CONTENTS) {
                pages.add(Filterable.passThrough(contents("guide.bunnidogs.more_contents",new int[]{24,25,27,28,29,31,32,34,36,39},
                        "guide.bunnidogs.main_contents",1)));
                continue;
            }
            var text = Component.translatable("guide.bunnidogs.page." + page + ".title").withStyle(ChatFormatting.BOLD)
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(Component.translatable("guide.bunnidogs.page." + page + ".body").withStyle(style -> style.withBold(false)))
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(link("guide.bunnidogs.contents",page < 24 || page > MORE_CONTENTS ? 1 : MORE_CONTENTS));
            pages.add(Filterable.passThrough(text));
        }
        return new WrittenBookContent(Filterable.passThrough("Bunnidogs Guide"), "Warren Keepers", 0, pages, true);
    }
    private static Component contents(String title, int[] chapters, String footer, int destination) {
        var contents = Component.translatable(title).withStyle(ChatFormatting.BOLD)
                .append(Component.literal("\n\n").withStyle(ChatFormatting.RESET));
        for (int page : chapters) contents.append(link("guide.bunnidogs.index."+page,page)).append("\n");
        return contents.append("\n").append(link(footer,destination));
    }
    private static Component link(String key, int destination) {
        return Component.translatable(key).withStyle(style -> style.withBold(false).withColor(ChatFormatting.DARK_GREEN)
                .withUnderlined(true).withClickEvent(new ClickEvent.ChangePage(destination)));
    }
}
