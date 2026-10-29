package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateStatusEffectManipulationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateStatusEffectManipulationBlockPacket> {
	@Override
	public void receive(UpdateStatusEffectManipulationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos statusEffectManipulationBlockPosition = payload.statusEffectManipulationBlockPosition();

		StatusEffectManipulationBlockEntity.ManipulationMode manipulationMode = StatusEffectManipulationBlockEntity.ManipulationMode.byName(payload.manipulationMode()).orElse(StatusEffectManipulationBlockEntity.ManipulationMode.APPLY);
		String effectIdentifier = payload.effectIdentifier();
		int effectAmplifier = payload.effectAmplifier();
		int effectDuration = payload.effectDuration();
		boolean effectIsAmbient = payload.effectIsAmbient();
		boolean effectShowsParticles = payload.effectShowsParticles();
		boolean effectShowsIcon = payload.effectShowsIcon();
		String manipulatedEffectTagIdentifierString = payload.manipulatedEffectTagIdentifierString();
		int amplifierModification = payload.amplifierModification();
		BlockPos uuidListHandlerPositionOffset = payload.uuidListHandlerPositionOffset();

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(statusEffectManipulationBlockPosition);
		BlockState blockState = serverLevel.getBlockState(statusEffectManipulationBlockPosition);

		if (blockEntity instanceof StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
			statusEffectManipulationBlockEntity.setManipulationMode(manipulationMode);
			if (!statusEffectManipulationBlockEntity.setEffectIdentifier(effectIdentifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("status_effect_manipulation_block.effect_identifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!statusEffectManipulationBlockEntity.setEffectAmplifier(effectAmplifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("status_effect_manipulation_block.effect_amplifier.invalid"), false);
				updateSuccessful = false;
			}
			statusEffectManipulationBlockEntity.setEffectDuration(effectDuration);
			statusEffectManipulationBlockEntity.setEffectIsAmbient(effectIsAmbient);
			statusEffectManipulationBlockEntity.setEffectShowsParticles(effectShowsParticles);
			statusEffectManipulationBlockEntity.setEffectShowsIcon(effectShowsIcon);
			statusEffectManipulationBlockEntity.setManipulatedEffectTagIdentifierString(manipulatedEffectTagIdentifierString);
			statusEffectManipulationBlockEntity.setAmplifierModification(amplifierModification);
			statusEffectManipulationBlockEntity.setUuidListHandlerPositionOffset(uuidListHandlerPositionOffset);

			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			statusEffectManipulationBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(statusEffectManipulationBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
