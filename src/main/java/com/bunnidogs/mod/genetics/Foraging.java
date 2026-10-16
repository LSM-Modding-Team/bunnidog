package com.bunnidogs.mod.genetics;

/** Pure decisions shared by feeding and autonomous food goals. No starvation/hunger meter is added. */
public final class Foraging {
    private Foraging() {}
    public static final float HEAL = 2.0F;
    public static final float HUNT_HEAL = 4.0F;
    public static final int SEARCH_INTERVAL = 100;
    public static final int MEAL_COOLDOWN = 600;
    public static final int GRAZE_TICKS = 40;
    public static final int SEARCH_RADIUS = 8;
    public static final int HUNT_TICKS = 400;

    public enum Meal { NONE, HAY, GRASS, WHEAT, PRODUCE }
    public static boolean handFood(boolean edible, boolean breedingFood, boolean hurt, boolean canGrow) {
        return edible && !breedingFood && (hurt || canGrow);
    }
    public static boolean canForage(boolean adult, boolean hurt, boolean hasNest, boolean night, boolean hasTarget) {
        return adult && hurt && hasNest && !night && !hasTarget;
    }
    public static boolean insideHome(double distanceSquared) {
        return distanceSquared >= 0 && distanceSquared <= (double) Home.HOME_RADIUS * Home.HOME_RADIUS;
    }
    public static boolean canGraze(Diet diet, Meal meal, boolean mobGriefing) {
        return switch (meal) {
            case HAY -> diet.grazesHay();
            case GRASS, WHEAT -> mobGriefing && diet.grazesHay();
            case PRODUCE -> mobGriefing && diet.tags().contains("forager");
            case NONE -> false;
        };
    }
    public static boolean prey(boolean tagged, boolean baby, boolean player, boolean bunnidog, boolean tame, boolean allied) {
        return tagged && !baby && !player && !bunnidog && !tame && !allied;
    }
}
