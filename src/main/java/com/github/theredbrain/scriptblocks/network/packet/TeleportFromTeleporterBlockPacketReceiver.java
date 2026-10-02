package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import com.github.theredbrain.scriptblocks.util.LocationCooldownsHelper;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos teleportBlockPosition = payload.teleportBlockPosition();

		String accessPositionDimension = payload.accessPositionDimension();

		String targetDimensionOwnerName = payload.targetDimensionOwnerName();
		String targetLocation = payload.targetLocation();
		String targetLocationEntrance = payload.targetLocationEntrance();

		String dataId = payload.dataId();
		String data = payload.data();

		ServerWorld serverWorld = serverPlayerEntity.getServerWorld();
		MinecraftServer server = serverPlayerEntity.server;

		ServerWorld targetWorld = null;
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
			MutablePair<String, BlockPos> location_access_pos = ((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$getLocationAccessPosition();
			if (spawnPointType == TeleporterBlockEntity.SpawnPointType.LOCATION_ACCESS_POSITION && location_access_pos != null) {
				targetWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(location_access_pos.getLeft())));
				targetPos = location_access_pos.getRight();
				if (targetWorld != null && targetPos != null) {
					((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$setLocationAccessPosition(null);
				}
			} else if (spawnPointType == TeleporterBlockEntity.SpawnPointType.PLAYER_SPAWN) {
				targetWorld = server.getWorld(serverPlayerEntity.getSpawnPointDimension());
				targetPos = serverPlayerEntity.getSpawnPointPosition();
				targetYaw = serverPlayerEntity.getSpawnAngle();
			} else {
				targetWorld = server.getOverworld();
				targetPos = server.getOverworld().getSpawnPos();
				targetYaw = server.getOverworld().getSpawnAngle();
			}
		} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS || teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {

			Location location = null;
			Optional<RegistryEntry.Reference<Location>> optionalLocationReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(targetLocation));
			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}

			ServerPlayerEntity targetDimensionOwner = server.getPlayerManager().getPlayer(targetDimensionOwnerName);

			if (location != null) {

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
					targetLocationIsPublic = true;
				} else if (targetDimensionOwner != null) {
					Identifier targetDimensionId = ScriptBlocks.identifier(targetDimensionOwner.getUuidAsString());
					RegistryKey<World> dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
					targetWorld = server.getWorld(dimensionregistryKey);

					if (targetWorld == null) {
						if (targetDimensionOwner.getUuid() == serverPlayerEntity.getUuid()) {
							DimensionsManager.addAndSaveDynamicDimension(targetDimensionId, server);
							dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
							targetWorld = server.getWorld(dimensionregistryKey);
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
						DebuggingHelper.sendDebuggingMessage("targetWorld: " + targetWorld.getRegistryKey().toString(), serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("location: " + location, serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("location.controlBlockPos: " + LocationUtils.getControlBlockPosForLocation(location), serverPlayerEntity);
						DebuggingHelper.sendDebuggingMessage("block at controlBlockPos: " + targetWorld.getBlockState(blockPos).getBlock().getTranslationKey(), serverPlayerEntity);
					}

					if (!(blockEntity instanceof LocationControlBlockEntity)) {

						if (DebuggingHelper.isTeleporterLoggingEnabled()) {
							DebuggingHelper.sendDebuggingMessage("manually place location structure", serverPlayerEntity);
						}

						// TODO don't execute commands, find a better way
						String forceLoadAddCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload add " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadAddCommand);

						String placeStructureCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run place structure " + location.structureIdentifier() + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), placeStructureCommand);

						String forceLoadRemoveAllCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload remove " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadRemoveAllCommand);

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
							String forceLoadAddCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload add " + resetAreaMinX + " " + resetAreaMinZ + " " + resetAreaMaxX + " " + resetAreaMaxZ;
							server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadAddCommand);

							BlockPos dataBlockPos = locationControlBlock.getDataProvidingBlockPosOffset();
							if (dataBlockPos != BlockPos.ORIGIN) {
								BlockEntity blockEntity1 = targetWorld.getBlockEntity(locationControlBlock.getPos().add(dataBlockPos.getX(), dataBlockPos.getY(), dataBlockPos.getZ()));
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
							String forceLoadRemoveAllCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload remove " + resetAreaMinX + " " + resetAreaMinZ + " " + resetAreaMaxX + " " + resetAreaMaxZ;
							server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadRemoveAllCommand);

							locationWasReset = true;
						}
					}

					if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {

						MutablePair<BlockPos, MutablePair<Double, Double>> entrance = locationControlBlock.getTargetEntrance(targetWorld, targetLocationEntrance);
						targetPos = entrance.getLeft();
						targetYaw = entrance.getRight().getLeft();
						targetPitch = entrance.getRight().getRight();

						long entranceCooldown = 0;
						long serverTime = server.getOverworld().getTime();

						long currentLocationCooldown = LocationCooldownsHelper.getCurrentLocationCooldown(serverPlayerEntity, targetLocation);
						if (currentLocationCooldown > 0 && serverTime - currentLocationCooldown < entranceCooldown) {
							locationCooldownWasUp = false;
							serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_on_cooldown"));
						}

						if (teleportTeam) {
							Team team = serverPlayerEntity.getScoreboardTeam();
							if (team != null) {
								for (String playerString : team.getPlayerList()) {
									ServerPlayerEntity teamServerPlayerEntity = server.getPlayerManager().getPlayer(playerString);
									if (teamServerPlayerEntity != null && teamServerPlayerEntity != serverPlayerEntity) {

										currentLocationCooldown = LocationCooldownsHelper.getCurrentLocationCooldown(serverPlayerEntity, targetLocation);
										if (currentLocationCooldown > 0 && serverTime - currentLocationCooldown < entranceCooldown) {
											locationCooldownWasUp = false;
											serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_on_cooldown_for_team_member", playerString));
											teamServerPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_on_cooldown"));
										}

									}
								}
							}
						}

						if (locationCooldownWasUp) {
							for (CommonDataStructures.ItemCost itemCost : LocationUtils.getKeyForEntrance(location, targetLocationEntrance)) {

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
				actualAccessPosition = teleportBlockPosition.add(accessPositionOffset.getX(), accessPositionOffset.getY(), accessPositionOffset.getZ());
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

			uuidList.add(serverPlayerEntity.getUuid());

			if (teleportTeam) {
				Team team = serverPlayerEntity.getScoreboardTeam();
				if (team != null) {
					for (String playerString : team.getPlayerList()) {
						ServerPlayerEntity teamServerPlayerEntity = server.getPlayerManager().getPlayer(playerString);
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

							uuidList.add(teamServerPlayerEntity.getUuid());

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
				serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_was_reset"));
			} else {
				if (!playerHadKeyItem) {
					serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.key_item_required"));
				} else if (!locationWasGeneratedByOwner) {
					serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_not_visited_by_owner"));
				}
			}
		}
	}

	private void handleTeleport(ServerPlayerEntity serverPlayerEntity, ServerWorld targetWorld, BlockPos targetPos, double targetYaw, double targetPitch, boolean targetLocationIsPublic, String targetDimensionOwnerName, String accessPositionDimension, @Nullable BlockPos accessPosition, String locationToCoolDown) {

		serverPlayerEntity.fallDistance = 0;
		serverPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), EnumSet.noneOf(PositionFlag.class), (float) targetYaw, (float) targetPitch);
		if (DebuggingHelper.isTeleporterLoggingEnabled()) {
			DebuggingHelper.sendDebuggingMessage("Teleport to world: " + targetWorld.getRegistryKey().getValue() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch, serverPlayerEntity);
			if (!targetLocationIsPublic) {
				DebuggingHelper.sendDebuggingMessage("World owned by: " + targetDimensionOwnerName, serverPlayerEntity);
			}
		}

		if (!accessPositionDimension.isEmpty() && accessPosition != null) {
			((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$setLocationAccessPosition(new MutablePair<>(accessPositionDimension, accessPosition));
		}

		if (!locationToCoolDown.isEmpty()) {
			LocationCooldownsHelper.playerEntersLocation(serverPlayerEntity, locationToCoolDown);
		}

		serverPlayerEntity.closeHandledScreen();

		serverPlayerEntity.removeStatusEffect(ScriptBlocksStatusEffects.PORTAL_RESISTANCE_EFFECT);

	}
}
