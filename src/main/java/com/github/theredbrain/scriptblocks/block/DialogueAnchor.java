package com.github.theredbrain.scriptblocks.block;

import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public interface DialogueAnchor {
	List<MutablePair<String, BlockPos>> getDialogueUsedBlocks();

	List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks();
}
