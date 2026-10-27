package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateHousingBlockCreativePacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateHousingBlockCreativePacket> {
	@Override
	public void receive(UpdateHousingBlockCreativePacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		boolean showRestrictBlockBreakingArea = payload.showRestrictBlockBreakingArea();

		Vec3i restrictBlockBreakingAreaDimensions = payload.restrictBlockBreakingAreaDimensions();
		BlockPos restrictBlockBreakingAreaPositionOffset = payload.restrictBlockBreakingAreaPositionOffset();
		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.byName(payload.ownerMode()).orElse(HousingBlockEntity.OwnerMode.DIMENSION_OWNER);

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			housingBlockEntity.setShowInfluenceArea(showRestrictBlockBreakingArea);
			housingBlockEntity.setInfluenceAreaDimensions(restrictBlockBreakingAreaDimensions);
			housingBlockEntity.setRestrictBlockBreakingAreaPositionOffset(restrictBlockBreakingAreaPositionOffset);
			housingBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			housingBlockEntity.setOwnerMode(ownerMode);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			housingBlockEntity.setChanged();
			world.sendBlockUpdated(housingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
