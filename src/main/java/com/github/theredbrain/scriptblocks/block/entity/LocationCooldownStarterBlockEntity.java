package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LocationCooldownStarterBlockEntity extends BlockEntity implements HandlesUUIDList {
	public LocationCooldownStarterBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.LOCATION_COOLDOWN_STARTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return new ArrayList<>();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		if (this.getLevel() == null) {
			return;
		}

		Iterator<UUID> playerListIterator = list.iterator();

		Player playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = this.getLevel().getPlayerByUUID(uuid);

			if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
				DataAttachmentHelper.setActiveLocationCooldowns(serverPlayerEntity);
			}
		}
	}

	@Override
	public void reset() {
	}
}
