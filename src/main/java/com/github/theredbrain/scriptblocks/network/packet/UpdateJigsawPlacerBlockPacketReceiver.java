package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateJigsawPlacerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateJigsawPlacerBlockPacket> {
	@Override
	public void receive(UpdateJigsawPlacerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos jigsawPlacerBlockPosition = payload.jigsawPlacerBlockPosition();

		String target = payload.target();
		String pool = payload.pool();

		JigsawBlockEntity.Joint joint = payload.joint();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(jigsawPlacerBlockPosition);
		BlockState blockState = world.getBlockState(jigsawPlacerBlockPosition);

		if (blockEntity instanceof JigsawPlacerBlockEntity jigsawPlacerBlockEntity) {
			if (!jigsawPlacerBlockEntity.setTarget(target)) {
				serverPlayerEntity.sendMessage(Text.translatable("jigsaw_placer_block.target.invalid"), false);
				updateSuccessful = false;
			}
			if (!jigsawPlacerBlockEntity.setPool(pool)) {
				serverPlayerEntity.sendMessage(Text.translatable("jigsaw_placer_block.pool.invalid"), false);
				updateSuccessful = false;
			}
			jigsawPlacerBlockEntity.setJoint(joint);
			jigsawPlacerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			jigsawPlacerBlockEntity.markDirty();
			world.updateListeners(jigsawPlacerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
