package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

@Deprecated
public class UpdateEntranceDelegationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateEntranceDelegationBlockPacket> {
	@Override
	public void receive(UpdateEntranceDelegationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			return;
		}

		BlockPos entranceDelegationBlockPosition = payload.entranceDelegationBlockPosition();

		BlockPos delegatedEntrancePositionOffset = payload.delegatedEntrancePositionOffset();
		double delegatedEntranceYaw = payload.delegatedEntranceYaw();
		double delegatedEntrancePitch = payload.delegatedEntrancePitch();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(entranceDelegationBlockPosition);
		BlockState blockState = world.getBlockState(entranceDelegationBlockPosition);

		if (blockEntity instanceof EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
			if (!entranceDelegationBlockEntity.setDelegatedEntrance(new MutablePair<>(delegatedEntrancePositionOffset, new MutablePair<>(delegatedEntranceYaw, delegatedEntrancePitch)))) {
				serverPlayerEntity.displayClientMessage(Component.translatable("entrance_delegation_block.delegatedEntrance.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			entranceDelegationBlockEntity.setChanged();
			world.sendBlockUpdated(entranceDelegationBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
