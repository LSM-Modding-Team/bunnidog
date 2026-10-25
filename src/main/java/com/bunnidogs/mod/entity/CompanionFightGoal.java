package com.bunnidogs.mod.entity;

import com.bunnidogs.mod.genetics.Combat;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

/** One bounded, visible melee approach; never bypasses native hit immunity or terrain. */
final class CompanionFightGoal extends Goal {
    private final Bunnidog dog;
    private int repath,bite,blocked;
    CompanionFightGoal(Bunnidog dog) { this.dog=dog; setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    @Override public boolean canUse() { return dog.combat().fighting(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { repath=0; bite=10; blocked=0; dog.getNavigation().stop(); }
    @Override public void stop() { dog.getNavigation().stop(); }
    @Override public void tick() {
        var enemy=dog.combat().target();
        if (enemy==null || !dog.combat().legal(enemy)) return;
        dog.getLookControl().setLookAt(enemy,30,30);
        if (bite>0) bite--;
        if (dog.isWithinMeleeAttackRange(enemy) && HomeRoutes.visible(dog,enemy)) {
            dog.getNavigation().stop();
            if (bite==0) {
                dog.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                float before=enemy.getHealth();
                if (dog.doHurtTarget((ServerLevel)dog.level(),enemy)) {if(enemy.getHealth()<before || !enemy.isAlive())dog.combat().tactics().practiced(enemy);dog.combat().skirmishDone();}
                bite=Combat.profile(dog.genome(),dog.stats()).interval();
            }
        } else if (--repath<=0) {
            repath=20;
            var route=dog.getNavigation().createPath(enemy,0);
            if (route==null || !route.canReach() || !HomeRoutes.trailSafe(dog,route)) {
                dog.getNavigation().stop();
                if (++blocked<3) { repath=5; return; }
                var keeper=dog.trail().keeperPlayer();
                if (keeper!=null) keeper.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.bunnidogs.combat.blocked"));
                dog.combat().recall(dog.trail().keeperPlayer()); return;
            }
            blocked=0;
            dog.getNavigation().moveTo(route,1.2);
        } else if (!HomeRoutes.trailSafe(dog,dog.getNavigation().getPath())) dog.getNavigation().stop();
    }
}
