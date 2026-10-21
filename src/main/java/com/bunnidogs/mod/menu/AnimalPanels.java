package com.bunnidogs.mod.menu;

import com.bunnidogs.mod.entity.Bunnidog;
import com.bunnidogs.mod.entity.BunnidogCare;
import com.bunnidogs.mod.genetics.Trail;
import com.bunnidogs.mod.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Actions over the living animal. Measurements belong to the Calipers, not ordinary interaction. */
public final class AnimalPanels {
    private AnimalPanels() {}
    public static final int CARE_MENU=226, ORDERS=227;
    public static Component text(String key,Object... args) { return Component.translatable("interaction.bunnidogs."+key,args); }
    private static boolean has(Player player,Item item) {
        return player.getInventory().contains(stack->stack.is(item));
    }
    private static CareView.Control action(int id,String key,boolean enabled,Component reason) {
        return new CareView.Control(id,text(key),enabled,CareView.Control.PRIMARY,enabled ? Component.empty() : reason);
    }
    public static CareView.Control back() { return action(CarePanels.BACK,"back",true,Component.empty()); }
    public static Component condition(Bunnidog dog) {
        return dog.combat().downed() ? text("downed") : dog.combat().danger() ? text("alarmed")
                : dog.combat().recovering() ? text("recovering") : dog.getHealth()<dog.getMaxHealth() ? text("injured")
                : dog.isBaby() ? text("pup") : dog.isInLove() ? text("courting") : Component.empty();
    }
    public static CareView home(Player player,Bunnidog dog) {
        var buttons=new ArrayList<CareView.Control>();
        buttons.add(action(CARE_MENU,"care",true,Component.empty()));
        buttons.add(action(ORDERS,"orders",true,Component.empty()));
        buttons.add(action(CarePanels.ROLES,"work",true,Component.empty()));
        if (dog.carriedBundleCount()>0) buttons.add(action(CarePanels.CARGO,"cargo",true,Component.empty()));
        if (FamilyPanels.hasCalipers(player)) buttons.add(action(CarePanels.GENETICS,"measure",true,Component.empty()));
        return new CareView(dog.getDisplayName(),condition(dog),List.of(),buttons);
    }
    public static CareView care(Player player,Bunnidog dog) {
        var buttons=new ArrayList<CareView.Control>();
        boolean edit=player.mayBuild(), guava=has(player,ModItems.GUAVA.get());
        boolean feed=edit && guava && dog.canEat(new net.minecraft.world.item.ItemStack(ModItems.GUAVA.get()))
                && (dog.combat().downed() ? dog.combat().canFirstAid(player)
                : dog.handCareReady() && (dog.getHealth()<dog.getMaxHealth() || dog.canAgeUp()));
        Component careReason=!edit ? text("read_only") : !guava ? text("need_guava")
                : !dog.canEat(new net.minecraft.world.item.ItemStack(ModItems.GUAVA.get())) ? text("wrong_food")
                : dog.combat().downed() ? text("keeper_only") : !dog.handCareReady() ? text("resting") : text("not_hungry");
        buttons.add(action(CarePanels.CARE,dog.combat().downed() ? "first_aid" : "feed",feed,careReason));
        boolean court=edit && guava && dog.readyForCareCourtship();
        buttons.add(action(CarePanels.COURTSHIP,"courtship",court,!edit ? text("read_only") : !guava ? text("need_guava")
                : dog.isBaby() ? text("still_growing") : dog.isInLove() ? text("courting")
                : dog.beingTransported() || dog.isVehicle() ? text("transported") : dog.combat().danger() ? text("alarmed") : text("resting")));
        boolean brush=has(player,Items.BRUSH);
        buttons.add(action(CarePanels.GROOM,"brush",edit && brush && BunnidogCare.canGroom(dog),
                !edit ? text("read_only") : !brush ? text("need_brush") : dog.isBaby() ? text("still_growing")
                : BunnidogCare.groomingProducts(dog).isEmpty() ? text("no_grooming") : text("resting")));
        buttons.add(back());
        return new CareView(text("care"),condition(dog),List.of(),buttons);
    }
    public static CareView orders(Player player,Bunnidog dog) {
        var buttons=new ArrayList<CareView.Control>();
        boolean edit=player.mayBuild(), own=dog.trail().belongsTo(player);
        boolean calm=!dog.combat().danger() && !dog.combat().downed() && !dog.isInLove() && !dog.isBaby();
        Component reason=!edit ? text("read_only") : !own ? text("keeper_only") : dog.isBaby() ? text("still_growing") : text("busy");
        if (dog.trail().keeper()==null) {
            boolean whistle=has(player,ModItems.TRAIL_WHISTLE.get()), guava=has(player,ModItems.GUAVA.get());
            buttons.add(action(CarePanels.TRAIN,"befriend",edit && guava && dog.trail().canTrainFromStock(player),
                    !edit ? text("read_only") : !whistle ? text("need_whistle") : !guava ? text("need_guava")
                    : dog.isBaby() ? text("still_growing") : text("busy")));
        } else {
            boolean qualified=Trail.qualified(dog.genome());
            buttons.add(action(CarePanels.FOLLOW,"follow",edit && own && calm && qualified,reason));
            buttons.add(action(CarePanels.WAIT,"wait",edit && own && calm && qualified,reason));
            buttons.add(action(CarePanels.RESIDENT,dog.homeNest()==null ? "roam" : "home",edit && own && calm && qualified,reason));
            buttons.add(action(CarePanels.PROVISIONS,dog.trail().provisions() ? "meals_off" : "meals_on",edit && own,reason));
            if (own) buttons.add(action(CombatPanel.OPEN,"combat",true,Component.empty()));
            buttons.add(action(CarePanels.RELEASE,"release",edit && own && calm && dog.combat().canRelease(),reason));
        }
        buttons.add(back());
        return new CareView(text("orders"),condition(dog),List.of(),buttons);
    }
}
