package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.components.BlockPosComponent;
import com.github.theredbrain.scriptblocks.components.PlayerLocationAccessPosComponent;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ComponentsRegistry implements EntityComponentInitializer {
	public static final ComponentKey<BlockPosComponent> CURRENT_HOUSING_BLOCK_POS =
			ComponentRegistry.getOrCreate(ScriptBlocks.identifier("current_housing_block_pos"), BlockPosComponent.class);
	public static final ComponentKey<PlayerLocationAccessPosComponent> PLAYER_LOCATION_ACCESS_POS =
			ComponentRegistry.getOrCreate(ScriptBlocks.identifier("player_location_access_pos"), PlayerLocationAccessPosComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(CURRENT_HOUSING_BLOCK_POS, e -> new BlockPosComponent(), RespawnCopyStrategy.INVENTORY);
		registry.registerForPlayers(PLAYER_LOCATION_ACCESS_POS, e -> new PlayerLocationAccessPosComponent(), RespawnCopyStrategy.INVENTORY);
	}
}
