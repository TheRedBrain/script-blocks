package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
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

public class UpdateHousingBlockCreativePacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateHousingBlockCreativePacket> {
	@Override
	public void receive(UpdateHousingBlockCreativePacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		boolean showRestrictBlockBreakingArea = payload.showRestrictBlockBreakingArea();

		Vec3i restrictBlockBreakingAreaDimensions = payload.restrictBlockBreakingAreaDimensions();
		BlockPos restrictBlockBreakingAreaPositionOffset = payload.restrictBlockBreakingAreaPositionOffset();
		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.byName(payload.ownerMode()).orElse(HousingBlockEntity.OwnerMode.DIMENSION_OWNER);

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			if (!housingBlockEntity.setShowInfluenceArea(showRestrictBlockBreakingArea)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.showRestrictBlockBreakingArea.invalid"), false);
				updateSuccessful = false;
			}
			if (!housingBlockEntity.setInfluenceAreaDimensions(restrictBlockBreakingAreaDimensions)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.restrictBlockBreakingAreaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!housingBlockEntity.setRestrictBlockBreakingAreaPositionOffset(restrictBlockBreakingAreaPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.restrictBlockBreakingAreaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			housingBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			if (!housingBlockEntity.setOwnerMode(ownerMode)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.ownerMode.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			housingBlockEntity.markDirty();
			world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
