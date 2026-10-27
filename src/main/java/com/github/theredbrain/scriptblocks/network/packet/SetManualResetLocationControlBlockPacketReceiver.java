package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

public class SetManualResetLocationControlBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<SetManualResetLocationControlBlockPacket> {
	@Override
	public void receive(SetManualResetLocationControlBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		String targetDimensionOwnerName = payload.targetDimensionOwnerName();
		String targetLocation = payload.targetLocation();
		String targetEntrance = payload.targetEntrance();
		boolean checkForResetCost = payload.checkForResetCost();
		boolean manualReset = payload.manualReset();

		MinecraftServer server = context.server();

		ServerLevel targetWorld = null;
		Location location = null;
		Optional<Holder.Reference<Location>> optionalLocationReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(targetLocation));
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}

		ServerPlayer targetDimensionOwner = server.getPlayerList().getPlayerByName(targetDimensionOwnerName);

		if (location == null) {
			serverPlayerEntity.closeContainer();
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.location_not_found"));
			return;
		}

		boolean playerOwnsLocation = true;
		if (location.isPublic()) {
			if (ScriptBlocksConfigs.SERVER_CONFIG.enable_public_locations_dimension) {
				ResourceKey<Level> dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER);
				targetWorld = server.getLevel(dimensionregistryKey);

				if (targetWorld == null) {
					DimensionsManager.addAndSavePublicDimension(DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER, server);
					dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER);
					targetWorld = server.getLevel(dimensionregistryKey);
				}
			} else {
				targetWorld = server.overworld();
			}
		} else if (targetDimensionOwner != null) {
			if (targetDimensionOwner.getUUID() == serverPlayerEntity.getUUID()) {
				Identifier targetDimensionId = ScriptBlocks.identifier(targetDimensionOwner.getStringUUID());
				ResourceKey<Level> dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, targetDimensionId);
				targetWorld = server.getLevel(dimensionregistryKey);

				if (targetWorld == null) {
					DimensionsManager.addAndSaveDynamicDimension(targetDimensionId, server);
					dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, targetDimensionId);
					targetWorld = server.getLevel(dimensionregistryKey);
				}
			} else {
				playerOwnsLocation = false;
			}
		}

		if (!playerOwnsLocation) {
			serverPlayerEntity.closeContainer();
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.player_location_not_owned"));
			return;
		}

		if (targetWorld == null) {
			serverPlayerEntity.closeContainer();
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.target_world_not_found"));
			return;
		}

		boolean resetCostPaid = true;
		if (checkForResetCost) {

			for (CommonDataStructures.ItemCost itemCost : LocationUtils.getResetCostForEntrance(location, targetEntrance)) {

				ItemStack keyStack = itemCost.itemStack();
				int keyCount = keyStack.getCount();
				Inventory playerInventory = serverPlayerEntity.getInventory();

				for (int i = 0; i < playerInventory.getContainerSize(); i++) {
					ItemStack currentItemStack = playerInventory.getItem(i);
					if (ItemStack.isSameItemSameComponents(keyStack, currentItemStack)) {
						ItemStack currentItemStackCopy = currentItemStack.copy();
						int currentItemStackCount = currentItemStackCopy.getCount();
						if (currentItemStackCount >= keyCount) {
							currentItemStackCopy.setCount(currentItemStackCount - keyCount);
							if (itemCost.consumeStack()) {
								playerInventory.setItem(i, currentItemStackCopy);
							}
							keyCount = 0;
							break;
						} else {
							if (itemCost.consumeStack()) {
								playerInventory.setItem(i, ItemStack.EMPTY);
							}
							keyCount = keyCount - currentItemStackCount;
						}
					}
				}
				if (keyCount > 0) {
					resetCostPaid = false;
					break;
				}
			}
		}

		if (!resetCostPaid) {
			serverPlayerEntity.closeContainer();
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.reset_cost_not_paid"));
			return;
		}

		BlockPos blockPos = LocationUtils.getControlBlockPosForLocation(location);
		BlockEntity blockEntity = targetWorld.getBlockEntity(blockPos);
		BlockState blockState = targetWorld.getBlockState(blockPos);

		if (blockEntity instanceof LocationControlBlockEntity locationControlBlockEntity) {
			locationControlBlockEntity.setManualReset(manualReset);

			locationControlBlockEntity.setChanged();
			targetWorld.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);

			if (ScriptBlocksConfigs.SERVER_CONFIG.confirm_successful_location_reset) {
				serverPlayerEntity.closeContainer();
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.success"));
				return;
			}
		}
		serverPlayerEntity.closeContainer();
		serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.location_reset.location_control_block_missing"));
	}
}
