package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.Map;

public class UpdateLocationControlBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateLocationControlBlockPacket> {
	@Override
	public void receive(UpdateLocationControlBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos locationControlBlockPosition = payload.locationControlBlockPosition();


		LevelData.RespawnData mainEntrance = payload.mainEntrance();

		HashMap<String, LevelData.RespawnData> sideEntrances = new HashMap<>(Map.of());

		for (MutablePair<String, LevelData.RespawnData> sideEntrance : payload.sideEntrancesList()) {
			sideEntrances.put(sideEntrance.getLeft(), sideEntrance.getRight());
		}

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		BlockPos dataSavingBlockPosOffset = payload.dataSavingBlockPosOffset();

		boolean shouldAlwaysReset = payload.shouldAlwaysReset();
		boolean manualReset = payload.manualReset();

		int resetAreaMinX = payload.resetAreaMinX();
		int resetAreaMinZ = payload.resetAreaMinZ();
		int resetAreaMaxX = payload.resetAreaMaxX();
		int resetAreaMaxZ = payload.resetAreaMaxZ();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(locationControlBlockPosition);
		BlockState blockState = serverLevel.getBlockState(locationControlBlockPosition);

		if (blockEntity instanceof LocationControlBlockEntity locationControlBlockEntity) {
			locationControlBlockEntity.reset();
			locationControlBlockEntity.setMainEntrance(mainEntrance);
			locationControlBlockEntity.setSideEntrances(sideEntrances);
			locationControlBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			locationControlBlockEntity.setDataProvidingBlockPosOffset(dataSavingBlockPosOffset);
			locationControlBlockEntity.setShouldAlwaysReset(shouldAlwaysReset);
			locationControlBlockEntity.setManualReset(manualReset);
			locationControlBlockEntity.setResetAreaMinX(resetAreaMinX);
			locationControlBlockEntity.setResetAreaMinZ(resetAreaMinZ);
			locationControlBlockEntity.setResetAreaMaxX(resetAreaMaxX);
			locationControlBlockEntity.setResetAreaMaxZ(resetAreaMaxZ);

			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			locationControlBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(locationControlBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
