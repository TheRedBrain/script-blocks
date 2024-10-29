package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeredDisplayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredDisplayBlockPacket> {
	@Override
	public void receive(UpdateTriggeredDisplayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos triggeredDisplayBlockPosition = payload.triggeredDisplayBlockPosition();

		// common
		TriggeredDisplayBlockEntity.BillboardMode billboardMode = TriggeredDisplayBlockEntity.BillboardMode.byName(payload.billboardModeString()).orElse(TriggeredDisplayBlockEntity.BillboardMode.FIXED);
		TriggeredDisplayBlockEntity.DisplayMode displayMode = TriggeredDisplayBlockEntity.DisplayMode.byName(payload.displayModeString()).orElse(TriggeredDisplayBlockEntity.DisplayMode.TEXT);
		boolean isTriggered = payload.isTriggered();
		Vec3d displayOffset = payload.displayOffset();
		float displayYaw = payload.displayYaw();
		float displayPitch = payload.displayPitch();
		// text mode
		String displayTextString = payload.displayTextString();
		int lineWidth = payload.lineWidth();
		Byte textOpacity = payload.textOpacity();
		int textBackground = payload.textBackground();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeredDisplayBlockPosition);
		BlockState blockState = world.getBlockState(triggeredDisplayBlockPosition);

		if (blockEntity instanceof TriggeredDisplayBlockEntity triggeredDisplayBlockEntity) {
			triggeredDisplayBlockEntity.setBillboardMode(billboardMode);
			triggeredDisplayBlockEntity.setDisplayMode(displayMode);
			triggeredDisplayBlockEntity.setIsTriggered(isTriggered);
			triggeredDisplayBlockEntity.setDisplayOffset(displayOffset);
			triggeredDisplayBlockEntity.setDisplayRotation(displayYaw, displayPitch);
			triggeredDisplayBlockEntity.setTextString(displayTextString);
			triggeredDisplayBlockEntity.setLineWidth(lineWidth);
			triggeredDisplayBlockEntity.setTextOpacity(textOpacity);
			triggeredDisplayBlockEntity.setBackground(textBackground);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			triggeredDisplayBlockEntity.markDirty();
			world.updateListeners(triggeredDisplayBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
