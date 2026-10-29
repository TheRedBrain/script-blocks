package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateRelayTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateRelayTriggerBlockPacket> {

	@Override
	public void receive(UpdateRelayTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos relayTriggerBlockPos = payload.relayTriggerBlockPosition();

		RelayTriggerBlockEntity.SelectionMode selectionMode = RelayTriggerBlockEntity.SelectionMode.byName(payload.selectionMode()).orElse(RelayTriggerBlockEntity.SelectionMode.LIST);

		boolean showArea = payload.showArea();
		boolean resetsArea = payload.resetsArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = payload.triggeredBlocks();
		RelayTriggerBlockEntity.TriggerMode triggerMode = RelayTriggerBlockEntity.TriggerMode.byName(payload.triggerMode()).orElse(RelayTriggerBlockEntity.TriggerMode.NORMAL);

		boolean isTriggerAmountDataDriven = payload.isTriggerAmountDataDriven();
		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPosOffset();
		String dataIdentifier = payload.dataIdentifier();

		int triggerAmount = payload.triggerAmount();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(relayTriggerBlockPos);
		BlockState blockState = serverLevel.getBlockState(relayTriggerBlockPos);

		if (blockEntity instanceof RelayTriggerBlockEntity relayTriggerBlockEntity) {
			relayTriggerBlockEntity.setSelectionMode(selectionMode);
			relayTriggerBlockEntity.setShowArea(showArea);
			relayTriggerBlockEntity.setResetsArea(resetsArea);
			relayTriggerBlockEntity.setAreaDimensions(areaDimensions);
			relayTriggerBlockEntity.setAreaPositionOffset(areaPositionOffset);
			relayTriggerBlockEntity.setTriggeredBlocks(triggeredBlocks);
			relayTriggerBlockEntity.setTriggerMode(triggerMode);
			relayTriggerBlockEntity.setIsTriggerAmountDataDriven(isTriggerAmountDataDriven);
			relayTriggerBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			relayTriggerBlockEntity.setDataIdentifier(dataIdentifier);
			relayTriggerBlockEntity.setTriggerAmount(triggerAmount);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);

			relayTriggerBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(relayTriggerBlockPos, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
