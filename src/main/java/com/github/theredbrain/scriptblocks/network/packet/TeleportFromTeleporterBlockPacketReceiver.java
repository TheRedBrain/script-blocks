package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.LocationsRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeleportFromTeleporterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TeleportFromTeleporterBlockPacket> {
	@Override
	public void receive(TeleportFromTeleporterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos teleportBlockPosition = payload.teleportBlockPosition();

		String accessPositionDimension = payload.accessPositionDimension();
		BlockPos accessPositionOffset = payload.accessPositionOffset();
		boolean setAccessPosition = payload.setAccessPosition();

		boolean teleportTeam = payload.teleportTeam();

		TeleporterBlockEntity.TeleportationMode teleportationMode = TeleporterBlockEntity.TeleportationMode.byName(payload.teleportationMode()).orElse(TeleporterBlockEntity.TeleportationMode.DIRECT);

		BlockPos directTeleportPositionOffset = payload.directTeleportPositionOffset();
		double directTeleportOrientationYaw = payload.directTeleportOrientationYaw();
		double directTeleportOrientationPitch = payload.directTeleportOrientationPitch();

		TeleporterBlockEntity.SpawnPointType spawnPointType = TeleporterBlockEntity.SpawnPointType.byName(payload.spawnPointType()).orElse(TeleporterBlockEntity.SpawnPointType.WORLD_SPAWN);

		String targetDimensionOwnerName = payload.targetDimensionOwnerName();
		String targetLocation = payload.targetLocation();
		String targetLocationEntrance = payload.targetLocationEntrance();
		List<String> statusEffectsToDecrementLevelOnTeleport = payload.statusEffectsToDecrementLevelOnTeleport();
		String dataId = payload.dataId();
		int data = payload.data();

		ServerWorld serverWorld = serverPlayerEntity.getServerWorld();
		MinecraftServer server = serverPlayerEntity.server;

		ServerWorld targetWorld = null;
		BlockPos targetPos = null;
		double targetYaw = 0.0;
		double targetPitch = 0.0;

		boolean locationWasGeneratedByOwner = true;
		boolean playerHadKeyItem = true;
		boolean locationWasReset = false;

		if (teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
			targetWorld = serverWorld;
			targetPos = new BlockPos(teleportBlockPosition.getX() + directTeleportPositionOffset.getX(), teleportBlockPosition.getY() + directTeleportPositionOffset.getY(), teleportBlockPosition.getZ() + directTeleportPositionOffset.getZ());
			targetYaw = directTeleportOrientationYaw;
			targetPitch = directTeleportOrientationPitch;
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
		} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

			Location location = LocationsRegistry.registeredLocations.get(Identifier.tryParse(targetLocation));

			ServerPlayerEntity targetDimensionOwner = server.getPlayerManager().getPlayer(targetDimensionOwnerName);

//            ScriptBlocks.info("targetLocation: " + targetLocation);

			if (location != null) {

				if (location.isPublic()) {
					targetWorld = server.getOverworld();
				} else if (targetDimensionOwner != null) {
//                ScriptBlocks.info("targetDimensionOwner: " + targetDimensionOwner);
					Identifier targetDimensionId = ScriptBlocks.identifier(targetDimensionOwner.getUuidAsString());
//                ScriptBlocks.info("targetDimensionId: " + targetDimensionId);
					RegistryKey<World> dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
					targetWorld = server.getWorld(dimensionregistryKey);

					if (targetWorld == null) {
						if (targetDimensionOwner.getUuid() == serverPlayerEntity.getUuid()) {
//                        ScriptBlocks.info("targetDimensionOwner.getUuid() == serverPlayerEntity.getUuid()");
							DimensionsManager.addAndSaveDynamicDimension(targetDimensionId, server);
							dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
							targetWorld = server.getWorld(dimensionregistryKey);
						} else {
							locationWasGeneratedByOwner = false;
						}

					}
				}
//                ScriptBlocks.info("targetWorld: " + targetWorld);

				if (targetWorld != null) {
//                    ScriptBlocks.info("targetWorld != null && location != null");

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

//                        ScriptBlocks.info("!(blockEntity instanceof LocationControlBlockEntity)");

						String forceLoadAddCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload add " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadAddCommand);

//                        server.getCommandManager().executeWithPrefix(server.getCommandSource(), "forceload query");

						String placeStructureCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run place structure " + location.structureIdentifier() + " " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), placeStructureCommand);

						String forceLoadRemoveAllCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload remove " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
						server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadRemoveAllCommand);

						blockEntity = targetWorld.getBlockEntity(blockPos);
						initialise = true;
					}

//                    ScriptBlocks.info("controlBlockPos: " + blockPos);
//                    ScriptBlocks.info("block at controlBlockPos: " + targetWorld.getBlockState(blockPos).getBlock());

					if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {
						if (locationControlBlock.shouldReset() || initialise) {

							String forceLoadAddCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload add " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
							server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadAddCommand);

							BlockPos dataBlockPos = locationControlBlock.getDataProvidingBlockPosOffset();
							if (dataBlockPos != BlockPos.ORIGIN) {
//								ScriptBlocks.info("dataBlockPos != BlockPos.ORIGIN");
								BlockEntity blockEntity1 = targetWorld.getBlockEntity(locationControlBlock.getPos().add(dataBlockPos.getX(), dataBlockPos.getY(), dataBlockPos.getZ()));
								if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
//									ScriptBlocks.info("providesDataBlockEntity.getData before reset: " + providesDataBlockEntity.getData(dataId));
									providesDataBlockEntity.reset();
//									ScriptBlocks.info("providesDataBlockEntity.getData after reset: " + providesDataBlockEntity.getData(dataId));
									if (!dataId.isEmpty()) {
//										ScriptBlocks.info("!dataId.isEmpty()");
//										ScriptBlocks.info("dataId: " + dataId + ", data: " + data);
										providesDataBlockEntity.setData(dataId, data);
									}
//									ScriptBlocks.info("providesDataBlockEntity.getData after setData: " + providesDataBlockEntity.getData(dataId));
								}
							}

							locationControlBlock.trigger();

							String forceLoadRemoveAllCommand = "execute in " + targetWorld.getRegistryKey().getValue() + " run forceload remove " + (blockPos.getX() - 16) + " " + (blockPos.getZ() - 16) + " " + (blockPos.getX() + 31) + " " + (blockPos.getZ() + 31);
							server.getCommandManager().executeWithPrefix(server.getCommandSource(), forceLoadRemoveAllCommand);

							locationWasReset = true;
						}
					}

					if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {

						MutablePair<BlockPos, MutablePair<Double, Double>> entrance = locationControlBlock.getTargetEntrance(targetWorld, targetLocationEntrance);
						targetPos = entrance.getLeft();
						targetYaw = entrance.getRight().getLeft();
						targetPitch = entrance.getRight().getRight();

						boolean consumeKey = LocationUtils.consumeKeyAtEntrance(location, targetLocationEntrance);
						ItemStack keyStack = LocationUtils.getKeyForEntrance(location, targetLocationEntrance);
						if (!keyStack.isEmpty()) {
							int keyCount = keyStack.getCount();
							PlayerInventory playerInventory = serverPlayerEntity.getInventory();

							for (int i = 0; i < playerInventory.size(); i++) {
								ItemStack currentItemStack = playerInventory.getStack(i);
								if (ItemStack.areItemsAndComponentsEqual(keyStack, currentItemStack)) {
									ItemStack currentItemStackCopy = currentItemStack.copy();
									int currentItemStackCount = currentItemStackCopy.getCount();
									if (currentItemStackCount >= keyCount) {
										currentItemStackCopy.setCount(currentItemStackCount - keyCount);
										if (consumeKey) {
											playerInventory.setStack(i, currentItemStackCopy);
										}
										keyCount = 0;
										break;
									} else {
										if (consumeKey) {
											playerInventory.setStack(i, ItemStack.EMPTY);
										}
										keyCount = keyCount - currentItemStackCount;
									}
								}
							}
							playerHadKeyItem = keyCount <= 0;
						}

						if (setAccessPosition && Identifier.tryParse(accessPositionDimension) != null) {
							((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$setLocationAccessPosition(new MutablePair<>(accessPositionDimension, teleportBlockPosition.add(accessPositionOffset.getX(), accessPositionOffset.getY(), accessPositionOffset.getZ())));
						}
					}
				}
			}
		}

		if (targetWorld != null && targetPos != null && playerHadKeyItem) {

			List<RegistryEntry<StatusEffect>> statusEffectList = new ArrayList<>();
			for (String statusEffectString : statusEffectsToDecrementLevelOnTeleport) {
				Optional<RegistryEntry.Reference<StatusEffect>> optional_status_effect_entry = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(statusEffectString));
				if (optional_status_effect_entry.isPresent()) {
					statusEffectList.add(optional_status_effect_entry.get());
				}
			}
			RegistryEntry<StatusEffect> portal_resistance_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.PORTAL_RESISTANCE_EFFECT);
			statusEffectList.add(portal_resistance_status_effect);
			serverPlayerEntity.fallDistance = 0;
			serverPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), (float) targetYaw, (float) targetPitch);
			if (DebuggingHelper.isTeleporterLoggingEnabled()) {
				DebuggingHelper.sendDebuggingMessage("Teleport to world: " + targetWorld.getRegistryKey().getValue() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch, serverPlayerEntity);
				if (targetWorld != server.getOverworld()) {
					DebuggingHelper.sendDebuggingMessage("World owned by: " + targetDimensionOwnerName, serverPlayerEntity);
				}
			}
			serverPlayerEntity.closeHandledScreen();
			for (RegistryEntry<StatusEffect> statusEffectEntry : statusEffectList) {
				StatusEffectInstance statusEffectInstance = serverPlayerEntity.getStatusEffect(statusEffectEntry);
				if (statusEffectInstance != null) {
					int oldAmplifier = statusEffectInstance.getAmplifier();
					if (oldAmplifier > 0) {
						StatusEffectInstance newStatusEffectInstance = new StatusEffectInstance(statusEffectEntry, statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier() - 1, statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowIcon());
						serverPlayerEntity.removeStatusEffect(statusEffectEntry);
						serverPlayerEntity.addStatusEffect(newStatusEffectInstance);
					} else {
						serverPlayerEntity.removeStatusEffect(statusEffectEntry);
					}
				}
			}

			if (teleportTeam) {
				Team team = serverPlayerEntity.getScoreboardTeam();
				if (team != null) {
					for (String playerString : team.getPlayerList()) {
						ServerPlayerEntity teamServerPlayerEntity = server.getPlayerManager().getPlayer(playerString);
						if (teamServerPlayerEntity != null && teamServerPlayerEntity != serverPlayerEntity) {
							teamServerPlayerEntity.fallDistance = 0;
							teamServerPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), (float) targetYaw, (float) targetPitch);
							if (DebuggingHelper.isTeleporterLoggingEnabled()) {
								DebuggingHelper.sendDebuggingMessage("Teleport to world: " + targetWorld.getRegistryKey().getValue() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch, teamServerPlayerEntity);
								if (targetWorld != server.getOverworld()) {
									DebuggingHelper.sendDebuggingMessage("World owned by: " + targetDimensionOwnerName, teamServerPlayerEntity);
								}
							}
							serverPlayerEntity.closeHandledScreen();
							for (RegistryEntry<StatusEffect> statusEffectEntry : statusEffectList) {
								StatusEffectInstance statusEffectInstance = teamServerPlayerEntity.getStatusEffect(statusEffectEntry);
								if (statusEffectInstance != null) {
									int oldAmplifier = statusEffectInstance.getAmplifier();
									if (oldAmplifier > 0) {
										StatusEffectInstance newStatusEffectInstance = new StatusEffectInstance(statusEffectEntry, statusEffectInstance.getDuration(), statusEffectInstance.getAmplifier() - 1, statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowIcon());
										teamServerPlayerEntity.removeStatusEffect(statusEffectEntry);
										teamServerPlayerEntity.addStatusEffect(newStatusEffectInstance);
									} else {
										teamServerPlayerEntity.removeStatusEffect(statusEffectEntry);
									}
								}
							}
						}
					}
				}
			}
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
				} else {
					if (!locationWasGeneratedByOwner) {
						serverPlayerEntity.sendMessage(Text.translatable("gui.teleporter_block.location_not_visited_by_owner"));
					}
				}
			}
		}
	}
}
