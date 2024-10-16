package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeredAdvancementCheckerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredAdvancementCheckerBlockPacket> {
	@Override
	public void receive(UpdateTriggeredAdvancementCheckerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos triggeredAdvancementCheckerBlockPosition = payload.triggeredAdvancementCheckerBlockPosition();

		BlockPos firstTriggeredBlockPositionOffset = payload.firstTriggeredBlockPositionOffset();

		boolean firstTriggeredBlockResets = payload.firstTriggeredBlockResets();

		BlockPos secondTriggeredBlockPositionOffset = payload.secondTriggeredBlockPositionOffset();

		boolean secondTriggeredBlockResets = payload.secondTriggeredBlockResets();

		String checkedAdvancementIdentifier = payload.checkedAdvancementIdentifier();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredAdvancementCheckerBlockPosition);
		BlockState blockState = world.getBlockState(triggeredAdvancementCheckerBlockPosition);

		if (blockEntity instanceof TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlockEntity) {
			triggeredAdvancementCheckerBlockEntity.setFirstTriggeredBlock(new MutablePair<>(firstTriggeredBlockPositionOffset, firstTriggeredBlockResets));
			triggeredAdvancementCheckerBlockEntity.setSecondTriggeredBlock(new MutablePair<>(secondTriggeredBlockPositionOffset, secondTriggeredBlockResets));
			if (!triggeredAdvancementCheckerBlockEntity.setCheckedAdvancementIdentifier(checkedAdvancementIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_advancement_checker_block.checkedAdvancementIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredAdvancementCheckerBlockEntity.markDirty();
			world.updateListeners(triggeredAdvancementCheckerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
