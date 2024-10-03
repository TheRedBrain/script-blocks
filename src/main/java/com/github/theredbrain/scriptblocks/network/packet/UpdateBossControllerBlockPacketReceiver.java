package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;

public class UpdateBossControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateBossControllerBlockPacket> {

	@Override
	public void receive(UpdateBossControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos bossControllerBlockPosition = payload.bossControllerBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.applicationAreaDimensions();
		BlockPos areaPositionOffset = payload.applicationAreaPositionOffset();

		Identifier bossIdentifier = payload.bossIdentifier();
		BlockPos entitySpawnPositionOffset = payload.entitySpawnPositionOffset();
		double entitySpawnOrientationPitch = payload.entitySpawnOrientationPitch();
		double entitySpawnOrientationYaw = payload.entitySpawnOrientationYaw();

		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = payload.bossTriggeredBlocksList();
		HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocksMap = new HashMap<>();
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> triggeredBlock : bossTriggeredBlocksList) {
			bossTriggeredBlocksMap.put(triggeredBlock.getLeft(), triggeredBlock.getRight());
		}

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(bossControllerBlockPosition);
		BlockState blockState = world.getBlockState(bossControllerBlockPosition);

		if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
			bossControllerBlockEntity.reset();
			bossControllerBlockEntity.setShowArea(showArea);
			if (!bossControllerBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setBossIdentifier(bossIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("shop_block.bossIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setBossSpawnPositionOffset(entitySpawnPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setBossSpawnPositionPitch(entitySpawnOrientationPitch)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationPitch.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setBossSpawnPositionYaw(entitySpawnOrientationYaw)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationYaw.invalid"), false);
				updateSuccessful = false;
			}
			if (!bossControllerBlockEntity.setBossTriggeredBlocks(bossTriggeredBlocksMap)) {
				serverPlayerEntity.sendMessage(Text.translatable("dialogue_block.bossTriggeredBlocksList.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			bossControllerBlockEntity.markDirty();
			world.updateListeners(bossControllerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}

	}
}
