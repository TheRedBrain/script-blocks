package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class UpdateTriggeredDamageDealingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredDamageDealingBlockPacket> {

	@Override
	public void receive(UpdateTriggeredDamageDealingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos triggeredDamageDealingBlockPosition = payload.triggeredDamageDealingBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeredDamageDealingBlockPosition);
		BlockState blockState = world.getBlockState(triggeredDamageDealingBlockPosition);

		if (blockEntity instanceof TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
			triggeredDamageDealingBlockEntity.setShowArea(showArea);
			triggeredDamageDealingBlockEntity.setAreaDimensions(areaDimensions);
			triggeredDamageDealingBlockEntity.setAreaPositionOffset(areaPositionOffset);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			triggeredDamageDealingBlockEntity.markDirty();
			world.updateListeners(triggeredDamageDealingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}

	}
}
