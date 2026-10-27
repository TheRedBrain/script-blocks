package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListMessagingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateUUIDListMessagingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUUIDListMessagingBlockPacket> {
	@Override
	public void receive(UpdateUUIDListMessagingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos uuidListMessagingBlockPosition = payload.uuidListMessagingBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPosOffset();
		UUIDListMessagingBlockEntity.MessageMode messageMode = UUIDListMessagingBlockEntity.MessageMode.byName(payload.messageMode()).orElse(UUIDListMessagingBlockEntity.MessageMode.CHAT);
		String messageLocalisationString = payload.messageLocalisationString();
		String subTitleLocalisationString = payload.subTitleLocalisationString();
		int titleFadeIn = payload.titleFadeIn();
		int titleStay = payload.titleStay();
		int titleFadeOut = payload.titleFadeOut();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(uuidListMessagingBlockPosition);
		BlockState blockState = world.getBlockState(uuidListMessagingBlockPosition);

		if (blockEntity instanceof UUIDListMessagingBlockEntity uuidListMessagingBlockEntity) {
			uuidListMessagingBlockEntity.reset();
			uuidListMessagingBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			uuidListMessagingBlockEntity.setMessageMode(messageMode);
			uuidListMessagingBlockEntity.setMessageLocalisationString(messageLocalisationString);
			uuidListMessagingBlockEntity.setSubTitleLocalisationString(subTitleLocalisationString);
			uuidListMessagingBlockEntity.setTitleFadeIn(titleFadeIn);
			uuidListMessagingBlockEntity.setTitleStay(titleStay);
			uuidListMessagingBlockEntity.setTitleFadeOut(titleFadeOut);

			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			uuidListMessagingBlockEntity.setChanged();
			world.sendBlockUpdated(uuidListMessagingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
