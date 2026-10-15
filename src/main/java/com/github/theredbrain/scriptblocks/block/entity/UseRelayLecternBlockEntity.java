package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class UseRelayLecternBlockEntity extends UseRelayBlockEntity {

	public UseRelayLecternBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.USE_RELAY_LECTERN_BLOCK_ENTITY, pos, state);
	}
}
