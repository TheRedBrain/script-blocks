package com.github.theredbrain.scriptblocks.block;

import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;

public interface DialogueAnchor {
	HashMap<String, BlockPos> getDialogueUsedBlocks();
	HashMap<String, MutablePair<BlockPos, Boolean>> getDialogueTriggeredBlocks();
}
