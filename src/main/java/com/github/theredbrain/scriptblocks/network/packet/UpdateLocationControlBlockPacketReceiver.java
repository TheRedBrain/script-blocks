package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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

public class UpdateLocationControlBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateLocationControlBlockPacket> {
	@Override
	public void receive(UpdateLocationControlBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos locationControlBlockPosition = payload.locationControlBlockPosition();

		BlockPos mainEntrancePositionOffset = payload.mainEntrancePositionOffset();
		double mainEntranceYaw = payload.mainEntranceYaw();
		double mainEntrancePitch = payload.mainEntrancePitch();

		HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());

		for (MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrance : payload.sideEntrancesList()) {
			sideEntrances.put(sideEntrance.getLeft(), sideEntrance.getRight());
		}

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		BlockPos dataSavingBlockPosOffset = payload.dataSavingBlockPosOffset();

		boolean shouldAlwaysReset = payload.shouldAlwaysReset();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(locationControlBlockPosition);
		BlockState blockState = world.getBlockState(locationControlBlockPosition);

		if (blockEntity instanceof LocationControlBlockEntity locationControlBlockEntity) {

			locationControlBlockEntity.setMainEntrance(new MutablePair<>(mainEntrancePositionOffset, new MutablePair<>(mainEntranceYaw, mainEntrancePitch)));
			locationControlBlockEntity.setSideEntrances(sideEntrances);
			locationControlBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			locationControlBlockEntity.setDataSavingBlockPosOffset(dataSavingBlockPosOffset);
			locationControlBlockEntity.setShouldAlwaysReset(shouldAlwaysReset);

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			locationControlBlockEntity.markDirty();
			world.updateListeners(locationControlBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
