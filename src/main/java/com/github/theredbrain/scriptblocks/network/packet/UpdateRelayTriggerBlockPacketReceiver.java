package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateRelayTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateRelayTriggerBlockPacket> {

	@Override
	public void receive(UpdateRelayTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos relayTriggerBlockPos = payload.relayTriggerBlockPosition();

		RelayTriggerBlockEntity.SelectionMode selectionMode = RelayTriggerBlockEntity.SelectionMode.byName(payload.selectionMode()).orElse(RelayTriggerBlockEntity.SelectionMode.LIST);

		boolean showArea = payload.showArea();
		boolean resetsArea = payload.resetsArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = payload.triggeredBlocks();
		RelayTriggerBlockEntity.TriggerMode triggerMode = RelayTriggerBlockEntity.TriggerMode.byName(payload.triggerMode()).orElse(RelayTriggerBlockEntity.TriggerMode.NORMAL);
		int triggerAmount = payload.triggerAmount();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(relayTriggerBlockPos);
		BlockState blockState = world.getBlockState(relayTriggerBlockPos);

		if (blockEntity instanceof RelayTriggerBlockEntity relayTriggerBlockEntity) {
			relayTriggerBlockEntity.setSelectionMode(selectionMode);
			relayTriggerBlockEntity.setShowArea(showArea);
			relayTriggerBlockEntity.setResetsArea(resetsArea);
			relayTriggerBlockEntity.setAreaDimensions(areaDimensions);
			relayTriggerBlockEntity.setAreaPositionOffset(areaPositionOffset);
			relayTriggerBlockEntity.setTriggeredBlocks(triggeredBlocks);
			relayTriggerBlockEntity.setTriggerMode(triggerMode);
			relayTriggerBlockEntity.setTriggerAmount(triggerAmount);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);

			relayTriggerBlockEntity.markDirty();
			world.updateListeners(relayTriggerBlockPos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
