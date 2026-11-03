package com.bunnidogs.mod.genetics;

/** Shared native-weapon numbers. Charge plus recovery determines a repeated shot's cycle. */
public enum LanceProfile {
    PRISM(32,30,32,64), SOVEREIGN(24,24,48,56);
    private final int chargeTicks,recoveryTicks,range,damagePerCell;
    LanceProfile(int chargeTicks,int recoveryTicks,int range,int damagePerCell) {
        this.chargeTicks=chargeTicks;this.recoveryTicks=recoveryTicks;this.range=range;this.damagePerCell=damagePerCell;
    }
    public int chargeTicks() {return chargeTicks;}
    public int recoveryTicks() {return recoveryTicks;}
    public int range() {return range;}
    public int power(int selected) {return this==PRISM ? 1 : Math.clamp(selected,1,5);}
    public int damage(int selected) {return damagePerCell*power(selected);}
    public int cycleTicks() {return chargeTicks+recoveryTicks;}
}
