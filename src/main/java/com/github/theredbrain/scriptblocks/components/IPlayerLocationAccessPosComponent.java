package com.github.theredbrain.scriptblocks.components;

import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.Component;

public interface IPlayerLocationAccessPosComponent extends Component {
	Pair<Pair<String, BlockPos>, Boolean> getValue();

	void setValue(Pair<Pair<String, BlockPos>, Boolean> value);

	void deactivate();
}
