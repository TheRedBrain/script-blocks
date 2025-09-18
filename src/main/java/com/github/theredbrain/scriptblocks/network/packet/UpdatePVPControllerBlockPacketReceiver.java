package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
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
import java.util.Map;

public class UpdatePVPControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdatePVPControllerBlockPacket> {
	@Override
	public void receive(UpdatePVPControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos pvpControllerBlockPosition = payload.pvpControllerBlockPosition();

		String pvpArenaSettingsIdentifier = payload.pvpArenaSettingsIdentifier();

		HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());

		for (MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrance : payload.sideEntrancesList()) {
			sideEntrances.put(sideEntrance.getLeft(), sideEntrance.getRight());
		}

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(pvpControllerBlockPosition);
		BlockState blockState = world.getBlockState(pvpControllerBlockPosition);

		if (blockEntity instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
			pvpControllerBlockEntity.reset();
			pvpControllerBlockEntity.setPVPArenaSettingsIdentifier(pvpArenaSettingsIdentifier);
			pvpControllerBlockEntity.setRespawnPositions(sideEntrances);
			pvpControllerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			pvpControllerBlockEntity.markDirty();
			world.updateListeners(pvpControllerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
