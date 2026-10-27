package com.github.theredbrain.scriptblocks.util;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ScriptBlocksBlockProperties {
	public static final IntegerProperty ROTATED = IntegerProperty.create("rotated", 0, 3);
	public static final BooleanProperty TOGGLED = BooleanProperty.create("toggled");
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");
	public static final BooleanProperty X_MIRRORED = BooleanProperty.create("x_mirrored");
	public static final BooleanProperty Z_MIRRORED = BooleanProperty.create("z_mirrored");
}
