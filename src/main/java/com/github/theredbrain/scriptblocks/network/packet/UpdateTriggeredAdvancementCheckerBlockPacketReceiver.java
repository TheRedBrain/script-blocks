package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeredAdvancementCheckerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredAdvancementCheckerBlockPacket> {
	@Override
	public void receive(UpdateTriggeredAdvancementCheckerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredAdvancementCheckerBlockPosition = payload.triggeredAdvancementCheckerBlockPosition();

		BlockPos firstTriggeredBlockPositionOffset = payload.firstTriggeredBlockPositionOffset();

		boolean firstTriggeredBlockResets = payload.firstTriggeredBlockResets();

		BlockPos secondTriggeredBlockPositionOffset = payload.secondTriggeredBlockPositionOffset();

		boolean secondTriggeredBlockResets = payload.secondTriggeredBlockResets();

		String checkedAdvancementIdentifier = payload.checkedAdvancementIdentifier();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredAdvancementCheckerBlockPosition);
		BlockState blockState = world.getBlockState(triggeredAdvancementCheckerBlockPosition);

		if (blockEntity instanceof TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlockEntity) {
			triggeredAdvancementCheckerBlockEntity.setFirstTriggeredBlock(new MutablePair<>(firstTriggeredBlockPositionOffset, firstTriggeredBlockResets));
			triggeredAdvancementCheckerBlockEntity.setSecondTriggeredBlock(new MutablePair<>(secondTriggeredBlockPositionOffset, secondTriggeredBlockResets));
			if (!triggeredAdvancementCheckerBlockEntity.setCheckedAdvancementIdentifier(checkedAdvancementIdentifier)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("triggered_advancement_checker_block.checkedAdvancementIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredAdvancementCheckerBlockEntity.setChanged();
			world.sendBlockUpdated(triggeredAdvancementCheckerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
