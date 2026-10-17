package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListMessagingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateUUIDListMessagingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUUIDListMessagingBlockPacket> {
	@Override
	public void receive(UpdateUUIDListMessagingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
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

		World world = serverPlayerEntity.getWorld();

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

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			uuidListMessagingBlockEntity.markDirty();
			world.updateListeners(uuidListMessagingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
