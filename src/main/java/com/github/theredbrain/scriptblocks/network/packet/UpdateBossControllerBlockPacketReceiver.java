package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;

public class UpdateBossControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateBossControllerBlockPacket> {

	@Override
	public void receive(UpdateBossControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos bossControllerBlockPosition = payload.bossControllerBlockPosition();

		BlockPos uuidListHandlerBlockPositionOffset = payload.uuidListHandlerBlockPositionOffset();

		BlockPos noPlayersAroundTriggeredBlockPositionOffset = payload.noPlayersAroundTriggeredBlockPositionOffset();
		boolean noPlayersAroundTriggeredBlockResets = payload.noPlayersAroundTriggeredBlockResets();

		String bossIdentifier = payload.bossIdentifier();
		BlockPos entitySpawnPositionOffset = payload.entitySpawnPositionOffset();
		double entitySpawnOrientationPitch = payload.entitySpawnOrientationPitch();
		double entitySpawnOrientationYaw = payload.entitySpawnOrientationYaw();

		boolean isPersistent = payload.isPersistent();
		boolean enableSpawnEffects = payload.enableSpawnEffects();

		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = payload.bossTriggeredBlocksList();
		HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocksMap = new HashMap<>();
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> triggeredBlock : bossTriggeredBlocksList) {
			bossTriggeredBlocksMap.put(triggeredBlock.getLeft(), triggeredBlock.getRight());
		}

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(bossControllerBlockPosition);
		BlockState blockState = world.getBlockState(bossControllerBlockPosition);

		if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
			bossControllerBlockEntity.reset();
			bossControllerBlockEntity.setUUIDListHandlerBlockPositionOffset(uuidListHandlerBlockPositionOffset);
			bossControllerBlockEntity.setNoPlayersAroundTriggeredBlock(new MutablePair<>(noPlayersAroundTriggeredBlockPositionOffset, noPlayersAroundTriggeredBlockResets));
			bossControllerBlockEntity.setBossIdentifier(bossIdentifier);
			bossControllerBlockEntity.setBossSpawnPositionOffset(entitySpawnPositionOffset);
			bossControllerBlockEntity.setBossSpawnPositionPitch(entitySpawnOrientationPitch);
			bossControllerBlockEntity.setBossSpawnPositionYaw(entitySpawnOrientationYaw);
			bossControllerBlockEntity.setIsPersistent(isPersistent);
			bossControllerBlockEntity.setEnableSpawnEffects(enableSpawnEffects);
			bossControllerBlockEntity.setBossTriggeredBlocks(bossTriggeredBlocksMap);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			bossControllerBlockEntity.markDirty();
			world.updateListeners(bossControllerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}

	}
}
