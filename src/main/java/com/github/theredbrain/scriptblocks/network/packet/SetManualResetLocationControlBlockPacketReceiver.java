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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class SetManualResetLocationControlBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<SetManualResetLocationControlBlockPacket> {
	@Override
	public void receive(SetManualResetLocationControlBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		String targetDimensionOwnerName = payload.targetDimensionOwnerName();
		String targetLocation = payload.targetLocation();
		String targetEntrance = payload.targetEntrance();
		boolean checkForResetCost = payload.checkForResetCost();
		boolean manualReset = payload.manualReset();

		MinecraftServer server = context.server();

		ServerWorld targetWorld = null;
		Location location = null;
		Optional<RegistryEntry.Reference<Location>> optionalLocationReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(targetLocation));
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}

		ServerPlayerEntity targetDimensionOwner = server.getPlayerManager().getPlayer(targetDimensionOwnerName);

		if (location == null) {
			serverPlayerEntity.closeHandledScreen();
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.location_not_found"));
			return;
		}

		boolean playerOwnsLocation = true;
		if (location.isPublic()) {
			if (ScriptBlocksConfigs.SERVER_CONFIG.enable_public_locations_dimension) {
				RegistryKey<World> dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER);
				targetWorld = server.getWorld(dimensionregistryKey);

				if (targetWorld == null) {
					DimensionsManager.addAndSavePublicDimension(DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER, server);
					dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, DimensionsManager.PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER);
					targetWorld = server.getWorld(dimensionregistryKey);
				}
			} else {
				targetWorld = server.getOverworld();
			}
		} else if (targetDimensionOwner != null) {
			if (targetDimensionOwner.getUuid() == serverPlayerEntity.getUuid()) {
				Identifier targetDimensionId = ScriptBlocks.identifier(targetDimensionOwner.getUuidAsString());
				RegistryKey<World> dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
				targetWorld = server.getWorld(dimensionregistryKey);

				if (targetWorld == null) {
					DimensionsManager.addAndSaveDynamicDimension(targetDimensionId, server);
					dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
					targetWorld = server.getWorld(dimensionregistryKey);
				}
			} else {
				playerOwnsLocation = false;
			}
		}

		if (!playerOwnsLocation) {
			serverPlayerEntity.closeHandledScreen();
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.player_location_not_owned"));
			return;
		}

		if (targetWorld == null) {
			serverPlayerEntity.closeHandledScreen();
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.target_world_not_found"));
			return;
		}

		boolean resetCostPaid = true;
		if (checkForResetCost) {

			for (CommonDataStructures.ItemCost itemCost : LocationUtils.getResetCostForEntrance(location, targetEntrance)) {

				ItemStack keyStack = itemCost.itemStack();
				int keyCount = keyStack.getCount();
				PlayerInventory playerInventory = serverPlayerEntity.getInventory();

				for (int i = 0; i < playerInventory.size(); i++) {
					ItemStack currentItemStack = playerInventory.getStack(i);
					if (ItemStack.areItemsAndComponentsEqual(keyStack, currentItemStack)) {
						ItemStack currentItemStackCopy = currentItemStack.copy();
						int currentItemStackCount = currentItemStackCopy.getCount();
						if (currentItemStackCount >= keyCount) {
							currentItemStackCopy.setCount(currentItemStackCount - keyCount);
							if (itemCost.consumeStack()) {
								playerInventory.setStack(i, currentItemStackCopy);
							}
							keyCount = 0;
							break;
						} else {
							if (itemCost.consumeStack()) {
								playerInventory.setStack(i, ItemStack.EMPTY);
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
			serverPlayerEntity.closeHandledScreen();
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.reset_cost_not_paid"));
			return;
		}

		BlockPos blockPos = LocationUtils.getControlBlockPosForLocation(location);
		BlockEntity blockEntity = targetWorld.getBlockEntity(blockPos);
		BlockState blockState = targetWorld.getBlockState(blockPos);

		if (blockEntity instanceof LocationControlBlockEntity locationControlBlockEntity) {
			locationControlBlockEntity.setManualReset(manualReset);

			locationControlBlockEntity.markDirty();
			targetWorld.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL);

			if (ScriptBlocksConfigs.SERVER_CONFIG.confirm_successful_location_reset) {
				serverPlayerEntity.closeHandledScreen();
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.success"));
				return;
			}
		}
		serverPlayerEntity.closeHandledScreen();
		serverPlayerEntity.sendMessage(Text.translatable("hud.message.location_reset.location_control_block_missing"));
	}
}
