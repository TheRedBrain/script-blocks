package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateUUIDListRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUUIDListRelayBlockPacket> {
	@Override
	public void receive(UpdateUUIDListRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos uuidListRelayBlockPosition = payload.uuidListRelayBlockPosition();

		BlockPos uuidListProviderPositionOffset = payload.uuidListProviderPositionOffset();

		World world = serverPlayerEntity.getEntityWorld();

		BlockEntity blockEntity = world.getBlockEntity(uuidListRelayBlockPosition);
		BlockState blockState = world.getBlockState(uuidListRelayBlockPosition);

		if (blockEntity instanceof UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
			uuidListRelayBlockEntity.setUuidListProviderPositionOffset(uuidListProviderPositionOffset);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			uuidListRelayBlockEntity.markDirty();
			world.updateListeners(uuidListRelayBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
