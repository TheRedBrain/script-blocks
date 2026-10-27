package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Objects;

public class SetHousingBlockOwnerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<SetHousingBlockOwnerPacket> {
	@Override
	public void receive(SetHousingBlockOwnerPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		String owner = payload.owner();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			if (!housingBlockEntity.setOwnerUuid(owner)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.housing_block.owner_uuid_invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				if (Objects.equals(owner, "")) {
					housingBlockEntity.setIsOwnerSet(false);
					serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.housing_block.unclaimed_successful"), true);
					serverPlayerEntity.removeEffect(ScriptBlocksStatusEffects.HOUSING_OWNER_EFFECT);

					serverPlayerEntity.removeEffect(ScriptBlocksStatusEffects.BUILDING_MODE);
				} else {
					housingBlockEntity.setIsOwnerSet(true);
					serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.housing_block.claimed_successful"), true);
					serverPlayerEntity.addEffect(new MobEffectInstance(ScriptBlocksStatusEffects.HOUSING_OWNER_EFFECT, 100, 0, true, false, false));
				}
			}
			housingBlockEntity.setChanged();
			world.sendBlockUpdated(housingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
