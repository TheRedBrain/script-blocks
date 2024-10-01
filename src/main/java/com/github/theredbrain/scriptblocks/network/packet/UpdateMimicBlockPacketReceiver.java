package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateMimicBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateMimicBlockPacket> {
	@Override
	public void receive(UpdateMimicBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos mimicBlockPosition = payload.mimicBlockPosition();

		BlockPos activeMimicBlockPositionOffset = payload.activeMimicBlockPositionOffset();
		BlockPos inactiveMimicBlockPositionOffset = payload.inactiveMimicBlockPositionOffset();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(mimicBlockPosition);
		BlockState blockState = world.getBlockState(mimicBlockPosition);

		if (blockEntity instanceof MimicBlockEntity mimicBlockEntity) {
			if (!mimicBlockEntity.setActiveMimicBlockPositionOffset(activeMimicBlockPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("mimic_block.activeMimicBlockPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!mimicBlockEntity.setInactiveMimicBlockPositionOffset(inactiveMimicBlockPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("mimic_block.inactiveMimicBlockPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			mimicBlockEntity.markDirty();
			world.updateListeners(mimicBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
