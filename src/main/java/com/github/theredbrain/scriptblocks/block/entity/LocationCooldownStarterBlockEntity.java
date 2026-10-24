package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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

		if (this.getWorld() == null) {
			return;
		}

		Iterator<UUID> playerListIterator = list.iterator();

		PlayerEntity playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = this.getWorld().getPlayerByUuid(uuid);

			if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
				DataAttachmentHelper.setActiveLocationCooldowns(serverPlayerEntity);
			}
		}
	}

	@Override
	public void reset() {
	}
}
