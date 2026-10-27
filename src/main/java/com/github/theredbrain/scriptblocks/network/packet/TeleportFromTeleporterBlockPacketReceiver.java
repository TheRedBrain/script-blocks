package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
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
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeleportFromTeleporterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TeleportFromTeleporterBlockPacket> {
	@Override
	public void receive(TeleportFromTeleporterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		BlockPos teleportBlockPosition = payload.teleportBlockPosition();

		String accessPositionDimension = payload.accessPositionDimension();

		String targetDimensionOwnerName = payload.targetDimensionOwnerName();
		String targetLocation = payload.targetLocation();
		String targetLocationEntrance = payload.targetLocationEntrance();

		String dataId = payload.dataId();
		String data = payload.data();

		ServerLevel serverWorld = serverPlayerEntity.getServerWorld();
		MinecraftServer server = serverPlayerEntity.server;

		ServerLevel targetWorld = null;
		BlockPos targetPos = null;
		double targetYaw = 0.0;
		double targetPitch = 0.0;

		boolean locationWasGeneratedByOwner = true;
		boolean playerHadKeyItem = true;
		boolean locationWasReset = false;
		boolean targetLocationIsPublic = false;
		boolean locationCooldownWasUp = true;
		String locationToCoolDown = "";

		BlockEntity blockEntity2 = serverWorld.getBlockEntity(teleportBlockPosition);

		if (!(blockEntity2 instanceof TeleporterBlockEntity teleporterBlockEntity)) {
			return;
		}

		// triggered as early as possible to allow the trigger results to influence the spawn point calculation
		teleporterBlockEntity.preTeleportTrigger();

		BlockPos accessPositionOffset = teleporterBlockEntity.getAccessPositionOffset();
		boolean setAccessPosition = teleporterBlockEntity.getSetAccessPosition();

		boolean teleportTeam = teleporterBlockEntity.teleportTeam();

		TeleporterBlockEntity.TeleportationMode teleportationMode = teleporterBlockEntity.getTeleportationMode();

		TeleporterBlockEntity.SpawnPointType spawnPointType = teleporterBlockEntity.getSpawnPointType();

		if (teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
			targetWorld = serverWorld;
			BlockPos directTeleportPositionOffset = teleporterBlockEntity.getDirectTeleportPositionOffset();
			targetPos = new BlockPos(teleportBlockPosition.getX() + directTeleportPositionOffset.getX(), teleportBlockPosition.getY() + directTeleportPositionOffset.getY(), teleportBlockPosition.getZ() + directTeleportPositionOffset.getZ());
			targetYaw = teleporterBlockEntity.getDirectTeleportOrientationYaw();
			targetPitch = teleporterBlockEntity.getDirectTeleportOrientationPitch();

			if (targetWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
				MutablePair<BlockPos, MutablePair<Double, Double>> entrance = spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);

				targetPos = entrance.getLeft();
				targetYaw = entrance.getRight().getLeft();
				targetPitch = entrance.getRight().getRight();
			}

		} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {
			MutablePair<String, BlockPos> location_access_pos = DataAttachmentHelper.getCurrentLocationAccess(serverPlayerEntity);
			if (spawnPointType == TeleporterBlockEntity.SpawnPointType.LOCATION_ACCESS_POSITION && location_access_pos != null) {
				targetWorld = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(location_access_pos.getLeft())));
				targetPos = location_access_pos.getRight();
				if (targetWorld != null && targetPos != null) {
					DataAttachmentHelper.setCurrentLocationAccess(serverPlayerEntity, null);
				}
			} else if (spawnPointType == TeleporterBlockEntity.SpawnPointType.PLAYER_SPAWN) {
				targetWorld = server.getLevel(serverPlayerEntity.getSpawnPointDimension());
				targetPos = serverPlayerEntity.getSpawnPointPosition();
				targetYaw = serverPlayerEntity.getSpawnAngle();
			} else {
				targetWorld = server.overworld();
				targetPos = server.overworld().getSpawnPos();
				targetYaw = server.overworld().getSpawnAngle();
			}
		} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS || teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {

			Location location = null;
			Optional<Holder.Reference<Location>> optionalLocationReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(targetLocation));
			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}

			ServerPlayer targetDimensionOwner = server.getPlayerList().getPlayerByName(targetDimensionOwnerName);

			if (location != null) {

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
					targetLocationIsPublic = true;
				} else if (targetDimensionOwner != null) {
					Identifier targetDimensionId = ScriptBlocks.identifier(targetDimensionOwner.getStringUUID());
					ResourceKey<Level> dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, targetDimensionId);
					targetWorld = server.getLevel(dimensionregistryKey);

					if (targetWorld == null) {
						if (targetDimensionOwner.getUUID() == serverPlayerEntity.getUUID()) {
							DimensionsManager.addAndSaveDynamicDimension(targetDimensionId, server);
							dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, targetDimensionId);
							targetWorld = server.getLevel(dimensionregistryKey);
						} else {
							locationWasGeneratedByOwner = false;
						}

					}
				}

				if (targetWorld != null) {

					BlockPos blockPos = LocationUtils.getControlBlockPosForLocation(location);
					BlockEntity blockEntity = targetWorld.getBlockEntity(blockPos);
					boolean initialise = false;

					if (DebuggingHelper.isTeleporterLoggingEnabled()) {
						DebuggingHelper.sendDebuggingMessage("targetLocation: " + targetLocation, serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("targetWorld: " + targetWorld.dimension().toString(), serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("location: " + location, serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("location.controlBlockPos: " + LocationUtils.getControlBlockPosForLocation(location), serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("block at controlBlockPos: " + targetWorld.getBlockState(blockPos).getBlock().getDescriptionId(), serverPlayerEntity);
					}

					if (!(blockEntity instanceof LocationControlBlockEntity)) {

						if (DebuggingHelper.isTeleporterLoggingEnabled()) {
							DebuggingHelper.sendDebuggingMessage("manually place location structure", serverPlayerEntity);
						}

						// TODO don't execute commands, find a better way
						String forceLoadAddCommand = "execute in " + targetWorld.dimension().identifier() + " run forceload add " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommands().executeWithPrefix(server.createCommandSourceStack(), forceLoadAddCommand);

						String placeStructureCommand = "execute in " + targetWorld.dimension().identifier() + " run place structure " + location.structureIdentifier() + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
						server.getCommands().executeWithPrefix(server.createCommandSourceStack(), placeStructureCommand);

						String forceLoadRemoveAllCommand = "execute in " + targetWorld.dimension().identifier() + " run forceload remove " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommands().executeWithPrefix(server.createCommandSourceStack(), forceLoadRemoveAllCommand);

						blockEntity = targetWorld.getBlockEntity(blockPos);
						initialise = true;
					}

					if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {
						if (locationControlBlock.shouldReset() || initialise) {

							int resetAreaMinX = blockPos.getX() + locationControlBlock.getResetAreaMinX();
							int resetAreaMinZ = blockPos.getZ() + locationControlBlock.getResetAreaMinZ();
							int resetAreaMaxX = blockPos.getX() + locationControlBlock.getResetAreaMaxX();
							int resetAreaMaxZ = blockPos.getZ() + locationControlBlock.getResetAreaMaxZ();

							// TODO don't execute a command, find a better way
							String forceLoadAddCommand = "execute in " + targetWorld.dimension().identifier() + " run forceload add " + resetAreaMinX + " " + resetAreaMinZ + " " + resetAreaMaxX + " " + resetAreaMaxZ;
							server.getCommands().executeWithPrefix(server.createCommandSourceStack(), forceLoadAddCommand);

							BlockPos dataBlockPos = locationControlBlock.getDataProvidingBlockPosOffset();
							if (dataBlockPos != BlockPos.ZERO) {
								BlockEntity blockEntity1 = targetWorld.getBlockEntity(locationControlBlock.getBlockPos().offset(dataBlockPos.getX(), dataBlockPos.getY(), dataBlockPos.getZ()));
								if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
									providesDataBlockEntity.reset();
									if (!dataId.isEmpty()) {
										providesDataBlockEntity.setData(dataId, data);
									}
								}
							}

							locationControlBlock.trigger();

//							locationControlBlock.setForceLoadRemoveTimer(5);

							// TODO don't execute a command, find a better way
							String forceLoadRemoveAllCommand = "execute in " + targetWorld.dimension().identifier() + " run forceload remove " + resetAreaMinX + " " + resetAreaMinZ + " " + resetAreaMaxX + " " + resetAreaMaxZ;
							server.getCommands().executeWithPrefix(server.createCommandSourceStack(), forceLoadRemoveAllCommand);

							locationWasReset = true;
						}
					}

					if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {

						MutablePair<BlockPos, MutablePair<Double, Double>> entrance = locationControlBlock.getTargetEntrance(targetWorld, targetLocationEntrance);
						targetPos = entrance.getLeft();
						targetYaw = entrance.getRight().getLeft();
						targetPitch = entrance.getRight().getRight();

						long remainingCooldown = LocationUtils.getRemainingCooldownForEntrance(serverPlayerEntity, targetLocation, targetLocationEntrance);
						if (remainingCooldown > 0) {
							locationCooldownWasUp = false;
							serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.location_on_cooldown", location.displayName()));
							serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.cooldown_message", LocationUtils.getCooldownText(remainingCooldown)));
						}

						if (teleportTeam) {
							PlayerTeam team = serverPlayerEntity.getTeam();
							if (team != null) {
								for (String playerString : team.getPlayers()) {
									ServerPlayer teamServerPlayerEntity = server.getPlayerList().getPlayerByName(playerString);
									if (teamServerPlayerEntity != null && teamServerPlayerEntity != serverPlayerEntity) {

										remainingCooldown = LocationUtils.getRemainingCooldownForEntrance(teamServerPlayerEntity, targetLocation, targetLocationEntrance);
										if (remainingCooldown > 0) {
											locationCooldownWasUp = false;
											serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.location_on_cooldown_for_team_member", location.displayName(), playerString));
											teamServerPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.location_on_cooldown", location.displayName()));
											teamServerPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.cooldown_message", LocationUtils.getCooldownText(remainingCooldown)));
										}

									}
								}
							}
						}

						if (locationCooldownWasUp) {
							for (CommonDataStructures.ItemCost itemCost : LocationUtils.getKeyForEntrance(location, targetLocationEntrance)) {

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
									playerHadKeyItem = false;
									break;
								}
							}
							if (playerHadKeyItem) {
								locationToCoolDown = targetLocation;
							}
						}
					}
				}
			}
		}

		if (targetWorld != null && targetPos != null && playerHadKeyItem && locationCooldownWasUp) {

			String validatedAccessPositionDimension = "";
			BlockPos actualAccessPosition = null;
			List<UUID> uuidList = new ArrayList<>();

			if (setAccessPosition && Identifier.tryParse(accessPositionDimension) != null) {
				validatedAccessPositionDimension = accessPositionDimension;
				actualAccessPosition = teleportBlockPosition.offset(accessPositionOffset.getX(), accessPositionOffset.getY(), accessPositionOffset.getZ());
			}

			this.handleTeleport(
					serverPlayerEntity,
					targetWorld,
					targetPos,
					targetYaw,
					targetPitch,
					targetLocationIsPublic,
					targetDimensionOwnerName,
					validatedAccessPositionDimension,
					actualAccessPosition,
					locationToCoolDown
			);

			uuidList.add(serverPlayerEntity.getUUID());

			if (teleportTeam) {
				PlayerTeam team = serverPlayerEntity.getTeam();
				if (team != null) {
					for (String playerString : team.getPlayers()) {
						ServerPlayer teamServerPlayerEntity = server.getPlayerList().getPlayerByName(playerString);
						if (teamServerPlayerEntity != null && teamServerPlayerEntity != serverPlayerEntity) {

							this.handleTeleport(
									teamServerPlayerEntity,
									targetWorld,
									targetPos,
									targetYaw,
									targetPitch,
									targetLocationIsPublic,
									targetDimensionOwnerName,
									validatedAccessPositionDimension,
									actualAccessPosition,
									locationToCoolDown
							);

							uuidList.add(teamServerPlayerEntity.getUUID());

						}
					}
				}
			}

			teleporterBlockEntity.sendPostTeleportUUIDList(uuidList);

			teleporterBlockEntity.postTeleportTrigger();

		} else {
			if (DebuggingHelper.isTeleporterLoggingEnabled()) {
				DebuggingHelper.sendDebuggingMessage("Teleport failed", serverPlayerEntity);
				if (targetWorld == null) {
					DebuggingHelper.sendDebuggingMessage("targetWorld == null", serverPlayerEntity);
				}
				if (targetPos == null) {
					DebuggingHelper.sendDebuggingMessage("targetPos == null", serverPlayerEntity);
				}
			}

			if (locationWasReset) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.location_was_reset"));
			} else {
				if (!playerHadKeyItem) {
					serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.key_item_required"));
				} else if (!locationWasGeneratedByOwner) {
					serverPlayerEntity.sendSystemMessage(Component.translatable("gui.teleporter_block.location_not_visited_by_owner"));
				}
			}
		}
	}

	private void handleTeleport(ServerPlayer serverPlayerEntity, ServerLevel targetWorld, BlockPos targetPos, double targetYaw, double targetPitch, boolean targetLocationIsPublic, String targetDimensionOwnerName, String accessPositionDimension, @Nullable BlockPos accessPosition, String locationToCoolDown) {

		serverPlayerEntity.fallDistance = 0;
		serverPlayerEntity.teleportTo(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), EnumSet.noneOf(Relative.class), (float) targetYaw, (float) targetPitch);
		if (DebuggingHelper.isTeleporterLoggingEnabled()) {
			DebuggingHelper.sendDebuggingMessage("Teleport to world: " + targetWorld.dimension().identifier() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch, serverPlayerEntity);
			if (!targetLocationIsPublic) {
				DebuggingHelper.sendDebuggingMessage("World owned by: " + targetDimensionOwnerName, serverPlayerEntity);
			}
		}

		if (!accessPositionDimension.isEmpty() && accessPosition != null) {
			DataAttachmentHelper.setCurrentLocationAccess(serverPlayerEntity, new MutablePair<>(accessPositionDimension, accessPosition));
		}

		if (!locationToCoolDown.isEmpty()) {
			DataAttachmentHelper.playerEntersLocation(serverPlayerEntity, locationToCoolDown);
		}

		serverPlayerEntity.closeContainer();

	}
}
