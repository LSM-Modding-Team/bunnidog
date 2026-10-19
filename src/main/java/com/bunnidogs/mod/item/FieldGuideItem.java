package com.bunnidogs.mod.item;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;

/** Native book UI/packets, translated pages and local page links; no custom screen or payload. */
public final class FieldGuideItem extends WrittenBookItem {
    public static final int PAGE_COUNT = 65;
    private static final int MORE_CONTENTS = 40;
    private static final int NEW_CONTENTS = 50;
    private static final int HABITAT_CONTENTS = 56;
    public FieldGuideItem(Properties properties) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, content()));
    }
    private static WrittenBookContent content() {
        var pages = new ArrayList<Filterable<Component>>(PAGE_COUNT);
        pages.add(Filterable.passThrough(contents("guide.bunnidogs.title",new int[]{2,4,8,11,15,17,19,20},
                "guide.bunnidogs.more_contents",MORE_CONTENTS).append("\n").append(link("guide.bunnidogs.new_contents",NEW_CONTENTS))
                .append("\n").append(link("guide.bunnidogs.habitat_contents",HABITAT_CONTENTS))));
        for (int page = 2; page <= PAGE_COUNT; page++) {
            if (page == HABITAT_CONTENTS) {
                pages.add(Filterable.passThrough(contents("guide.bunnidogs.habitat_contents",new int[]{57,58,59,60,61,62,63,64,65},
                        "guide.bunnidogs.main_contents",1)));
                continue;
            }
            if (page == MORE_CONTENTS) {
                pages.add(Filterable.passThrough(contents("guide.bunnidogs.more_contents",new int[]{24,25,27,28,29,31,32,34,36,39},
                        "guide.bunnidogs.main_contents",1)));
                continue;
            }
            if (page == NEW_CONTENTS) {
                pages.add(Filterable.passThrough(contents("guide.bunnidogs.new_contents",new int[]{41,44,45,46,47,49,51,52,53,54},
                        "guide.bunnidogs.main_contents",1)));
                continue;
            }
            var text = Component.translatable("guide.bunnidogs.page." + page + ".title").withStyle(ChatFormatting.BOLD)
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(Component.translatable("guide.bunnidogs.page." + page + ".body").withStyle(style -> style.withBold(false)))
                    .append(Component.literal("\n\n").withStyle(style -> style.withBold(false)))
                    .append(link("guide.bunnidogs.contents",page > HABITAT_CONTENTS ? HABITAT_CONTENTS
                            : page > MORE_CONTENTS ? NEW_CONTENTS : page < 24 ? 1 : MORE_CONTENTS));
            pages.add(Filterable.passThrough(text));
        }
        return new WrittenBookContent(Filterable.passThrough("Bunnidogs Guide"), "Warren Keepers", 0, pages, true);
    }
    private static MutableComponent contents(String title, int[] chapters, String footer, int destination) {
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
