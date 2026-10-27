package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateTriggeredDamageDealingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredDamageDealingBlockPacket> {

	@Override
	public void receive(UpdateTriggeredDamageDealingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredDamageDealingBlockPosition = payload.triggeredDamageDealingBlockPosition();

		TriggeredDamageDealingBlockEntity.SelectionMode selectionMode = TriggeredDamageDealingBlockEntity.SelectionMode.byName(payload.selectionMode()).orElse(TriggeredDamageDealingBlockEntity.SelectionMode.AREA);

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		BlockPos uuidListHandlerPositionOffset = payload.uuidListHandlerPositionOffset();

		String exceptionTagIdentifierString = payload.exceptionTagIdentifierString();
		String damageTypeIdentifierString = payload.damageTypeIdentifierString();
		float damageAmount = payload.damageAmount();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeredDamageDealingBlockPosition);
		BlockState blockState = world.getBlockState(triggeredDamageDealingBlockPosition);

		if (blockEntity instanceof TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
			triggeredDamageDealingBlockEntity.setSelectionMode(selectionMode);
			triggeredDamageDealingBlockEntity.setShowArea(showArea);
			triggeredDamageDealingBlockEntity.setAreaDimensions(areaDimensions);
			triggeredDamageDealingBlockEntity.setAreaPositionOffset(areaPositionOffset);
			triggeredDamageDealingBlockEntity.setUuidListHandlerPositionOffset(uuidListHandlerPositionOffset);
			triggeredDamageDealingBlockEntity.setExceptionTagIdentifierString(exceptionTagIdentifierString);
			triggeredDamageDealingBlockEntity.setDamageTypeIdentifierString(damageTypeIdentifierString);
			triggeredDamageDealingBlockEntity.setDamageAmount(damageAmount);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			triggeredDamageDealingBlockEntity.setChanged();
			world.sendBlockUpdated(triggeredDamageDealingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}

	}
}
