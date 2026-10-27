package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class UpdateUUIDListRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUUIDListRelayBlockPacket> {
	@Override
	public void receive(UpdateUUIDListRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos uuidListRelayBlockPosition = payload.uuidListRelayBlockPosition();

		List<BlockPos> uuidListProviderPositionOffsets = payload.uuidListProviderPositionOffsets();

		Level world = serverPlayerEntity.level();

		BlockEntity blockEntity = world.getBlockEntity(uuidListRelayBlockPosition);
		BlockState blockState = world.getBlockState(uuidListRelayBlockPosition);

		if (blockEntity instanceof UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
			uuidListRelayBlockEntity.setUuidListHandlers(uuidListProviderPositionOffsets);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			uuidListRelayBlockEntity.setChanged();
			world.sendBlockUpdated(uuidListRelayBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
