package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class SetHousingBlockOwnerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<SetHousingBlockOwnerPacket> {
	@Override
	public void receive(SetHousingBlockOwnerPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		String owner = payload.owner();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			if (!housingBlockEntity.setOwnerUuid(owner)) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.housing_block.owner_uuid_invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				if (Objects.equals(owner, "")) {
					housingBlockEntity.setIsOwnerSet(false);
					serverPlayerEntity.sendMessage(Text.translatable("hud.message.housing_block.unclaimed_successful"), true);
					RegistryEntry<StatusEffect> housing_owner_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_OWNER_EFFECT);
					serverPlayerEntity.removeStatusEffect(housing_owner_status_effect);

					RegistryEntry<StatusEffect> building_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.BUILDING_MODE);
					serverPlayerEntity.removeStatusEffect(building_status_effect);
				} else {
					housingBlockEntity.setIsOwnerSet(true);
					serverPlayerEntity.sendMessage(Text.translatable("hud.message.housing_block.claimed_successful"), true);
					RegistryEntry<StatusEffect> housing_owner_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_OWNER_EFFECT);
					serverPlayerEntity.addStatusEffect(new StatusEffectInstance(housing_owner_status_effect, 100, 0, true, false, false));
				}
			}
			housingBlockEntity.markDirty();
			world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
