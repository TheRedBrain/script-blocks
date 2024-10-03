package com.github.theredbrain.scriptblocks.components;

import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.Component;

public interface IBlockPosComponent extends Component {
	BlockPos getValue();

	void setValue(BlockPos value);
}
