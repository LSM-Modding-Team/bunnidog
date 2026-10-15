package com.bunnidogs.mod.block;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Exposes vanilla's protected wood constructors. Guava shares oak's interaction, redstone and sound rules. */
public final class GuavaWoodBlocks {
    private GuavaWoodBlocks() {}
    public static final class Door extends DoorBlock {
        public Door(Properties properties) { super(BlockSetType.OAK,properties); }
    }
    public static final class Trapdoor extends TrapDoorBlock {
        public Trapdoor(Properties properties) { super(BlockSetType.OAK,properties); }
    }
    public static final class Button extends ButtonBlock {
        public Button(Properties properties) { super(BlockSetType.OAK,30,properties); }
    }
    public static final class PressurePlate extends PressurePlateBlock {
        public PressurePlate(Properties properties) { super(BlockSetType.OAK,properties); }
    }
}
