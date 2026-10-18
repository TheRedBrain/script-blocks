package com.github.theredbrain.scriptblocks.util;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;

public class ScriptBlocksBlockProperties {
	public static final IntProperty ROTATED = IntProperty.of("rotated", 0, 3);
	public static final BooleanProperty TOGGLED = BooleanProperty.of("toggled");
	public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");
	public static final BooleanProperty X_MIRRORED = BooleanProperty.of("x_mirrored");
	public static final BooleanProperty Z_MIRRORED = BooleanProperty.of("z_mirrored");
}
