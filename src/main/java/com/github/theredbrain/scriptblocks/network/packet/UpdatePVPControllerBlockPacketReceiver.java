package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.Map;

public class UpdatePVPControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdatePVPControllerBlockPacket> {
	@Override
	public void receive(UpdatePVPControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos pvpControllerBlockPosition = payload.pvpControllerBlockPosition();

		String pvpArenaSettingsIdentifier = payload.pvpArenaSettingsIdentifier();

		HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());

		for (MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrance : payload.sideEntrancesList()) {
			sideEntrances.put(sideEntrance.getLeft(), sideEntrance.getRight());
		}

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		BlockPos dataProvidingBlockPositionOffset = payload.dataProvidingBlockPositionOffset();

		String matchDurationDataIdentifier = payload.matchDurationDataIdentifier();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(pvpControllerBlockPosition);
		BlockState blockState = world.getBlockState(pvpControllerBlockPosition);

		if (blockEntity instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
			pvpControllerBlockEntity.reset();
			if (!pvpControllerBlockEntity.setPVPArenaSettingsIdentifier(pvpArenaSettingsIdentifier)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("pvp_controller_block.pvpArenaSettingsIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			pvpControllerBlockEntity.setRespawnPositions(sideEntrances);
			pvpControllerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			pvpControllerBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPositionOffset);
			pvpControllerBlockEntity.setMatchDurationDataIdentifier(matchDurationDataIdentifier);

			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			pvpControllerBlockEntity.setChanged();
			world.sendBlockUpdated(pvpControllerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
