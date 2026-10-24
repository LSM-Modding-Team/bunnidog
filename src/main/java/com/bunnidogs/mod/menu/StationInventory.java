package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.block.*;
import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.item.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Native station slots wrap the existing journals and saved items; there is no second inventory. */
public final class StationInventory {
    private StationInventory() {}
    public enum Kind {
        NONE(0,0), TROUGH(1,16), PEN(2,16), CRADLE(1,1), JUICER(3,16), TABLE(2,1), JAR(1,1), PEDESTAL(1,1);
        public final int slots, capacity;
        Kind(int slots,int capacity) { this.slots=slots; this.capacity=capacity; }
        public int x(int slot) { return slots==1 ? 80 : slots==2 ? 53+54*slot : 18+58*slot; }
        public boolean accepts(int slot,ItemStack stack,ItemStack filter) {
            if (stack.isEmpty()) return false;
            return switch (this) {
                case TROUGH -> FeedingTroughBlockEntity.accepts(stack) && (filter.isEmpty() || ItemStack.isSameItemSameComponents(filter,stack));
                case PEN -> stack.getItem() instanceof GuavaItem && Bunnidog.isBunnidogFood(stack);
                case CRADLE -> SeedItem.read(stack).isPresent();
                case JUICER -> slot==0 ? stack.getItem() instanceof GuavaItem || SeedItem.read(stack).isPresent()
                        : slot==1 && (stack.is(Items.GLASS_BOTTLE) || JuiceItem.carrier(stack).isPresent());
                case TABLE, JAR -> TissueSampleItem.read(stack).isPresent();
                case PEDESTAL -> true;
                case NONE -> false;
            };
        }
    }
    public record State(Kind kind,int progress) {
        public static final State NONE=new State(Kind.NONE,-1);
        public State { if (progress< -1 || progress>100) throw new IllegalArgumentException("Invalid station progress"); }
    }
    public static State state(BlockEntity be) {
        if (be instanceof FeedingTroughBlockEntity) return new State(Kind.TROUGH,-1);
        if (be instanceof BreedingPenBlockEntity) return new State(Kind.PEN,-1);
        if (be instanceof NurseryCradleBlockEntity cradle) return new State(Kind.CRADLE,cradle.storedSeed().isEmpty() ? -1 : cradle.age()*100/SeedCropBlock.MAX_AGE);
        if (be instanceof JuicerBlockEntity press) return new State(Kind.JUICER,press.item(0).isEmpty() || press.item(1).isEmpty() ? -1 : press.progress()*100/JuicerBlockEntity.PROCESS_TICKS);
        if (be instanceof GeneticsTableBlockEntity) return new State(Kind.TABLE,-1);
        if (be instanceof SpecimenJarBlockEntity) return new State(Kind.JAR,-1);
        if (be instanceof TrophyPedestalBlockEntity) return new State(Kind.PEDESTAL,-1);
        return State.NONE;
    }
    public static ResourceHandler<ItemResource> handler(BlockEntity be) {
        if (be instanceof FeedingTroughBlockEntity trough) return trough.automation();
        if (be instanceof BreedingPenBlockEntity pen) return pen.menuInventory();
        if (be instanceof NurseryCradleBlockEntity cradle) return cradle.automation();
        if (be instanceof JuicerBlockEntity press) return press.menuInventory();
        if (be instanceof GeneticsTableBlockEntity table) return table.automation();
        if (be instanceof SpecimenJarBlockEntity jar) return jar.menuInventory();
        if (be instanceof TrophyPedestalBlockEntity pedestal) return pedestal.ritualStock();
        throw new IllegalArgumentException("Station has no inventory");
    }
    /** StackCopySlot's direct setter, implemented through the original rollback/commit journal. */
    public static void replace(ResourceHandler<ItemResource> handler,int index,ItemStack next) {
        var resource=handler.getResource(index); int count=handler.getAmountAsInt(index);
        if (resource.matches(next) && count==next.getCount() || resource.isEmpty() && next.isEmpty()) return;
        try (var tx=Transaction.openRoot()) {
            if (resource.matches(next) && next.getCount()<count) {
                if (handler.extract(index,resource,count-next.getCount(),tx)!=count-next.getCount()) throw new IllegalStateException("Station extraction refused");
            } else {
                if (!resource.isEmpty() && handler.extract(index,resource,count,tx)!=count) throw new IllegalStateException("Station extraction refused");
                if (!next.isEmpty() && handler.insert(index,ItemResource.of(next),next.getCount(),tx)!=next.getCount()) throw new IllegalStateException("Station insertion refused");
            }
            tx.commit();
        }
    }
    /** Reserved old button IDs; stock now moves only through native container gestures. */
    public static boolean transferButton(int id) {
        return switch (id) {
            case CarePanels.ADD_FOOD,CarePanels.TAKE_FOOD,CarePanels.ADD_LEFT,CarePanels.ADD_RIGHT,CarePanels.TAKE_LEFT,CarePanels.TAKE_RIGHT,
                    CarePanels.ADD_SEED,CarePanels.TAKE_SEED,CarePanels.ADD_FRUIT,CarePanels.ADD_BOTTLE,CarePanels.TAKE_FRUIT,CarePanels.TAKE_CARRIER,CarePanels.TAKE_JUICE,
                    WorkshopPanels.ADD_SAMPLE,WorkshopPanels.TAKE_A,WorkshopPanels.TAKE_B,SignalPanels.JAR_ADD,SignalPanels.JAR_TAKE,SignalPanels.TROPHY_TAKE -> true;
            default -> false;
        };
    }
}
