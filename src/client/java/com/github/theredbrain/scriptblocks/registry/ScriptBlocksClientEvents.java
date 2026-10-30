package com.github.theredbrain.scriptblocks.registry;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ScriptBlocksClientEvents {
	public static void initializeEvents() {
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
			if (stack.is(ScriptBlocksBlocks.TRIGGERING_TRIAL_SPAWNER_BLOCK_ITEM_KEY) && display.shows(DataComponents.BLOCK_ENTITY_DATA)) {
				TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
				Spawner.appendHoverText(blockEntityData, lines::add, "SpawnData");
			}
		});
	}
}
