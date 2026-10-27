package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateJigsawPlacerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateJigsawPlacerBlockPacket> {
	@Override
	public void receive(UpdateJigsawPlacerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos jigsawPlacerBlockPosition = payload.jigsawPlacerBlockPosition();

		String structurePoolString = payload.structurePoolString();

		List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices = payload.structurePoolStringAppendices();

		String target = payload.target();

		JigsawBlockEntity.JointType joint = payload.joint();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(jigsawPlacerBlockPosition);
		BlockState blockState = world.getBlockState(jigsawPlacerBlockPosition);

		if (blockEntity instanceof JigsawPlacerBlockEntity jigsawPlacerBlockEntity) {
			jigsawPlacerBlockEntity.setStructurePoolString(structurePoolString);
			jigsawPlacerBlockEntity.setStructurePoolStringAppendices(structurePoolStringAppendices);
			if (!jigsawPlacerBlockEntity.setTarget(target)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("jigsaw_placer_block.target.invalid"), false);
				updateSuccessful = false;
			}
			jigsawPlacerBlockEntity.setJoint(joint);
			jigsawPlacerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			jigsawPlacerBlockEntity.setChanged();
			world.sendBlockUpdated(jigsawPlacerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
