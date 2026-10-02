package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateStatusEffectManipulationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateStatusEffectManipulationBlockPacket> {
	@Override
	public void receive(UpdateStatusEffectManipulationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
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

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(statusEffectManipulationBlockPosition);
		BlockState blockState = world.getBlockState(statusEffectManipulationBlockPosition);

		if (blockEntity instanceof StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
			statusEffectManipulationBlockEntity.setManipulationMode(manipulationMode);
			if (!statusEffectManipulationBlockEntity.setEffectIdentifier(effectIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("status_effect_manipulation_block.effect_identifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!statusEffectManipulationBlockEntity.setEffectAmplifier(effectAmplifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("status_effect_manipulation_block.effect_amplifier.invalid"), false);
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
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			statusEffectManipulationBlockEntity.markDirty();
			world.updateListeners(statusEffectManipulationBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
