package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.data.VisibleLocation;
import com.github.theredbrain.scriptblocks.entity.player.PlayerEntityHelper;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeleporterBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private boolean showAdventureScreen = true;

	private String preventInteractionIdentifierString = "";
	private String interactionPreventedMessage = "";

	private BlockPos uuidListHandlerPositionOffset = BlockPos.ZERO;

	private LevelData.RespawnData locationAccessPoint = LevelData.RespawnData.DEFAULT;
	private BlockPos accessPositionOffset = BlockPos.ZERO;
	private boolean enableLocationAccessPoint = false;

	private boolean onlyTeleportDimensionOwner = false;
	private String onlyDimensionOwnerCanTeleportMessage = "hud.message.only_dimension_owner_can_teleport";

	private boolean teleportTeam = false;

	private CreativeScreenPage creativeScreenPage = CreativeScreenPage.ACTIVATION;
	private TeleportationMode teleportationMode = TeleportationMode.DIRECT;

	// direct teleportation mode
	private LevelData.RespawnData directTeleportTarget = LevelData.RespawnData.DEFAULT;

	// spawn point mode
	private SpawnPointType spawnPointType = SpawnPointType.WORLD_SPAWN;

	// locations mode
	private final List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> locationsList = new ArrayList<>(List.of());

	// location mode
	private MutablePair<MutablePair<String, String>, MutablePair<String, String>> location = new MutablePair<>(new MutablePair<>("", ""), new MutablePair<>("", ""));

	private String locationAppendix = "";
	private String entranceAppendix = "";
	private String sentDataIdentifierAppendix = "";
	private String sentDataValueAppendix = "";

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;

	private MutablePair<BlockPos, Boolean> preTeleportTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private BlockPos onTeleportUUIDListHandlerPositionOffset = BlockPos.ZERO;
	private MutablePair<BlockPos, Boolean> postTeleportTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);

	private String teleporterName = "gui.teleporter_block.teleporter_name_field.label";
	private String currentTargetOwnerLabel = "gui.teleporter_block.target_owner_field.label";
	private String currentTargetIdentifierLabel = "gui.teleporter_block.target_identifier_field.label";
	private boolean showCancelButton = true;
	private boolean canOwnerBeChosen = true;
	private String teleportButtonLabel = "gui.teleporter_block.teleport_button.label";
	private String cancelTeleportButtonLabel = "gui.teleporter_block.cancel_teleport_button.label";

	public TeleporterBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TELEPORTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("screen_page", this.creativeScreenPage.getSerializedName());

		view.putString("prevent_interaction_identifier_string", this.preventInteractionIdentifierString);

		view.putString("interaction_prevented_message", this.interactionPreventedMessage);

		view.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.uuidListHandlerPositionOffset);

		view.store("location_access_point", LevelData.RespawnData.CODEC, this.locationAccessPoint);

		view.putBoolean("set_location_access_point", this.enableLocationAccessPoint);

		view.putBoolean("only_teleport_dimension_owner", this.onlyTeleportDimensionOwner);

		view.putString("only_dimension_owner_can_teleport_message", this.onlyDimensionOwnerCanTeleportMessage);

		view.putBoolean("teleport_team", this.teleportTeam);

		view.putString("teleportation_mode", this.teleportationMode.getSerializedName());

		view.store("direct_teleport_target", LevelData.RespawnData.CODEC, this.directTeleportTarget);

		view.putString("spawn_point_type", this.spawnPointType.getSerializedName());

		CompoundTag nbtCompound;
		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("locations", CompoundTag.CODEC);
		for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> location : this.locationsList) {
			nbtCompound = new CompoundTag();
			nbtCompound.putString("identifier", location.getLeft().getLeft());
			nbtCompound.putString("entrance", location.getLeft().getRight());
			nbtCompound.putString("sent_data_id", location.getRight().getLeft());
			nbtCompound.putString("sent_data", location.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		nbtCompound = new CompoundTag();
		nbtCompound.putString("identifier", this.location.getLeft().getLeft());
		nbtCompound.putString("entrance", this.location.getLeft().getRight());
		nbtCompound.putString("sent_data_id", this.location.getRight().getLeft());
		nbtCompound.putString("sent_data", this.location.getRight().getRight());
		nbtCompound.putString("identifier_appendix", this.locationAppendix);
		nbtCompound.putString("entrance_appendix", this.entranceAppendix);
		nbtCompound.putString("sent_data_id_appendix", this.sentDataIdentifierAppendix);
		nbtCompound.putString("sent_data_appendix", this.sentDataValueAppendix);
		view.store("location", CompoundTag.CODEC, nbtCompound.copy());

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.preTeleportTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.preTeleportTriggeredBlock.getRight());
		view.store("pre_teleport", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onTeleportUUIDListHandlerPositionOffset);
		view.store("on_teleport", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.postTeleportTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.postTeleportTriggeredBlock.getRight());
		view.store("post_teleport", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("show_adventure_screen", this.showAdventureScreen);
		nbtCompound.putString("teleporter_name", this.teleporterName);
		nbtCompound.putString("current_target_identifier_label", this.currentTargetIdentifierLabel);
		nbtCompound.putString("current_target_owner_label", this.currentTargetOwnerLabel);
		nbtCompound.putBoolean("show_cancel_button", this.showCancelButton);
		nbtCompound.putBoolean("can_owner_be_chosen", this.canOwnerBeChosen);
		nbtCompound.putString("teleport_button_label", this.teleportButtonLabel);
		nbtCompound.putString("cancel_teleport_button_label", this.cancelTeleportButtonLabel);
		view.store("gameplay_screen", CompoundTag.CODEC, nbtCompound.copy());

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.creativeScreenPage = CreativeScreenPage.byName(view.getStringOr("screen_page", "")).orElseGet(() -> CreativeScreenPage.ACTIVATION);

		this.preventInteractionIdentifierString = view.getStringOr("prevent_interaction_identifier_string", "");

		this.interactionPreventedMessage = view.getStringOr("interaction_prevented_message", "");

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.locationAccessPoint = view.read("location_access_point", LevelData.RespawnData.CODEC).orElseGet(() -> {
					CompoundTag nbtCompound1 = view.read("location_access", CompoundTag.CODEC).orElseGet(CompoundTag::new);
					return LevelData.RespawnData.of(
							this.getLevel() != null ? this.getLevel().dimension() : ServerLevel.OVERWORLD,
							BlockPos.max(
									BlockPos.min(
											nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							0,
							0
					);
				}
		);

		this.enableLocationAccessPoint = view.getBooleanOr("set_location_access_point", false);

		this.onlyTeleportDimensionOwner = view.getBooleanOr("only_teleport_dimension_owner", false);

		this.onlyDimensionOwnerCanTeleportMessage = view.getStringOr("only_dimension_owner_can_teleport_message", "hud.message.only_dimension_owner_can_teleport");

		this.teleportTeam = view.getBooleanOr("teleport_team", false);

		this.teleportationMode = TeleportationMode.byName(view.getStringOr("teleportation_mode", "")).orElseGet(() -> TeleportationMode.DIRECT);

		this.directTeleportTarget = view.read("direct_teleport_target", LevelData.RespawnData.CODEC).orElseGet(() -> {
					CompoundTag nbtCompound1 = view.read("direct_teleport", CompoundTag.CODEC).orElseGet(CompoundTag::new);
					return LevelData.RespawnData.of(
							this.getLevel() != null ? this.getLevel().dimension() : ServerLevel.OVERWORLD,
							BlockPos.max(
									BlockPos.min(
											nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							nbtCompound1.getFloatOr("yaw", 0),
							nbtCompound1.getFloatOr("pitch", 0)
					);
				}
		);

		this.spawnPointType = SpawnPointType.byName(view.getStringOr("spawn_point_type", "")).orElseGet(() -> SpawnPointType.WORLD_SPAWN);

		this.locationsList.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("locations", CompoundTag.CODEC)) {
			this.locationsList.add(
					new MutablePair<>(
							new MutablePair<>(
									nbtCompound1.getStringOr("identifier", ""),
									nbtCompound1.getStringOr("entrance", "")
							),
							new MutablePair<>(
									nbtCompound1.getStringOr("sent_data_id", ""),
									nbtCompound1.getStringOr("sent_data", "")
							)
					)
			);
		}

		CompoundTag nbtCompound = view.read("location", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.location = new MutablePair<>(
				new MutablePair<>(
						nbtCompound.getStringOr("identifier", ""),
						nbtCompound.getStringOr("entrance", "")
				),
				new MutablePair<>(
						nbtCompound.getStringOr("sent_data_id", ""),
						nbtCompound.getStringOr("sent_data", "")
				)
		);

		this.sentDataValueAppendix = nbtCompound.getStringOr("identifier_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getStringOr("entrance_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getStringOr("sent_data_id_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getStringOr("sent_data_appendix", "");

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("pre_teleport", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.preTeleportTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		nbtCompound = view.read("on_teleport", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onTeleportUUIDListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("post_teleport", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.postTeleportTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		nbtCompound = view.read("gameplay_screen", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showAdventureScreen = nbtCompound.getBooleanOr("show_adventure_screen", true);

		this.teleporterName = nbtCompound.getStringOr("teleporter_name", "gui.teleporter_block.teleporter_name_field.label");

		this.currentTargetIdentifierLabel = nbtCompound.getStringOr("current_target_identifier_label", "gui.teleporter_block.target_identifier_field.label");

		this.currentTargetOwnerLabel = nbtCompound.getStringOr("current_target_owner_label", "gui.teleporter_block.target_owner_field.label");

		this.showCancelButton = nbtCompound.getBooleanOr("show_cancel_button", true);

		this.canOwnerBeChosen = nbtCompound.getBooleanOr("can_owner_be_chosen", true);

		this.teleportButtonLabel = nbtCompound.getStringOr("teleport_button_label", "gui.teleporter_block.teleport_button.label");

		this.cancelTeleportButtonLabel = nbtCompound.getStringOr("cancel_teleport_button_label", "gui.teleporter_block.cancel_teleport_button.label");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void openNonCreativeScreen(Level world, ServerPlayer serverPlayerEntity, BlockPos pos, BlockState state, TeleporterBlockEntity teleporterBlockEntity) {

		if (teleporterBlockEntity.onlyTeleportDimensionOwner && !serverPlayerEntity.getUUID().toString().equals(world.dimension().identifier().getPath())) {
			if (!teleporterBlockEntity.getOnlyDimensionOwnerCanTeleportMessage().isEmpty()) {
				serverPlayerEntity.sendSystemMessage(Component.translatable(teleporterBlockEntity.getOnlyDimensionOwnerCanTeleportMessage()), true);
			}
			return;
		}

		if (!teleporterBlockEntity.getPreventInteractionIdentifierString().isEmpty() && !PlayerEntityHelper.allowInteractions(serverPlayerEntity, teleporterBlockEntity.getPreventInteractionIdentifierString())) {
			if (!teleporterBlockEntity.getInteractionPreventedMessage().isEmpty()) {
				serverPlayerEntity.sendSystemMessage(Component.translatable(teleporterBlockEntity.getInteractionPreventedMessage()), true);
			}
			return;
		}

		List<VisibleLocation> visibleLocationsList = new ArrayList<>();

		// check locationsList for visible / unlocked locations
		if (teleporterBlockEntity.getTeleportationMode() == TeleportationMode.LOCATIONS || teleporterBlockEntity.getTeleportationMode() == TeleportationMode.LOCATION) {

			if (teleporterBlockEntity.getTeleportationMode() == TeleportationMode.LOCATIONS) {
				for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> entry : teleporterBlockEntity.locationsList) {

					Location location = null;
					if (world != null) {
						Optional<Holder.Reference<Location>> optionalLocationReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).get().value().get(Identifier.parse(entry.getLeft().getLeft()));
						if (optionalLocationReference.isPresent()) {
							location = optionalLocationReference.get().value();
						}
					}
					if (location != null) {
						String entranceDisplayName = LocationUtils.getEntranceDisplayName(location, entry.getLeft().getRight());
						boolean isEntranceNameShownFirst = LocationUtils.isEntranceNameShownFirst(location, entry.getLeft().getRight());
						boolean allowRegeneration = LocationUtils.allowRegeneration(location, entry.getLeft().getRight());
						if (LocationUtils.isLocationEntranceUnlocked(serverPlayerEntity, location, entry.getLeft().getRight())) {
							visibleLocationsList.add(new VisibleLocation(entry.getLeft().getLeft(), entry.getLeft().getRight(), location.displayName(), entranceDisplayName, isEntranceNameShownFirst, entry.getRight().getLeft(), entry.getRight().getRight(), true, allowRegeneration/*, Math.max(0L, cooldownStart), Math.max(0L, cooldownLength)*/));
						} else if (LocationUtils.showLockedLocationForEntrance(location, entry.getLeft().getRight())) {
							visibleLocationsList.add(new VisibleLocation(entry.getLeft().getLeft(), entry.getLeft().getRight(), location.displayName(), entranceDisplayName, isEntranceNameShownFirst, entry.getRight().getLeft(), entry.getRight().getRight(), false, allowRegeneration/*, Math.max(0L, cooldownStart), Math.max(0L, cooldownLength)*/));
						}
					}
				}
			} else if (teleporterBlockEntity.getTeleportationMode() == TeleportationMode.LOCATION) {
				MutablePair<MutablePair<String, String>, MutablePair<String, String>> dataDrivenLocation = teleporterBlockEntity.getDataDrivenLocation();
				Location location = null;
				if (world != null) {
					Optional<Holder.Reference<Location>> optionalLocationReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).get().value().get(Identifier.parse(dataDrivenLocation.getLeft().getLeft()));
					if (optionalLocationReference.isPresent()) {
						location = optionalLocationReference.get().value();
					}
				}
				if (location != null) {
					String entranceDisplayName = LocationUtils.getEntranceDisplayName(location, dataDrivenLocation.getLeft().getRight());
					boolean isEntranceNameShownFirst = LocationUtils.isEntranceNameShownFirst(location, dataDrivenLocation.getLeft().getRight());
					boolean allowRegeneration = LocationUtils.allowRegeneration(location, dataDrivenLocation.getLeft().getRight());
					if (LocationUtils.isLocationEntranceUnlocked(serverPlayerEntity, location, dataDrivenLocation.getLeft().getRight())) {
						visibleLocationsList.add(new VisibleLocation(dataDrivenLocation.getLeft().getLeft(), dataDrivenLocation.getLeft().getRight(), location.displayName(), entranceDisplayName, isEntranceNameShownFirst, dataDrivenLocation.getRight().getLeft(), dataDrivenLocation.getRight().getRight(), true, allowRegeneration/*, Math.max(0L, cooldownStart), Math.max(0L, cooldownLength)*/));
					} else if (LocationUtils.showLockedLocationForEntrance(location, dataDrivenLocation.getLeft().getRight())) {
						visibleLocationsList.add(new VisibleLocation(dataDrivenLocation.getLeft().getLeft(), dataDrivenLocation.getLeft().getRight(), location.displayName(), entranceDisplayName, isEntranceNameShownFirst, dataDrivenLocation.getRight().getLeft(), dataDrivenLocation.getRight().getRight(), false, allowRegeneration/*, Math.max(0L, cooldownStart), Math.max(0L, cooldownLength)*/));
					}
				}
			}
		}

		serverPlayerEntity.openMenu(createTeleporterMenuProvider(
				world,
				Component.translatable(teleporterBlockEntity.teleporterName),
				teleporterBlockEntity.getBlockPos(),
				visibleLocationsList
		));
	}

	static MenuProvider createTeleporterMenuProvider(
			Level level,
			Component title,
			BlockPos teleporterBlockPos,
			List<VisibleLocation> visibleLocationsList
	) {

		return new ExtendedMenuProvider<>() {
			@Override
			public ShopScreenHandler.ShopBlockData getScreenOpeningData(ServerPlayer player) {
				return new ShopScreenHandler.ShopBlockData(teleporterBlockPos);
			}

			@Override
			public Component getDisplayName() {
				return title;
			}

			@Nullable
			@Override
			public TeleporterBlockScreenHandler createMenu(int syncId, Inventory playerInventory, Player player) {
				return new TeleporterBlockScreenHandler(
						syncId,
						playerInventory,
						ContainerLevelAccess.create(level, teleporterBlockPos),
						teleporterBlockPos,
						visibleLocationsList
				);
			}
		};
	}

	public LevelData.RespawnData getActualDirectTeleportTarget(Level level) {
		LevelData.RespawnData spawnPoint = this.directTeleportTarget;

		if (!this.directTeleportTarget.pos().equals(BlockPos.ZERO) && level.getBlockEntity(BlockRotationUtils.getActualOffsetBlockPosition(this.getBlockPos(), this.directTeleportTarget.pos())) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			LevelData.RespawnData delegatedSpawnpoint = spawnPointDelegationBlockEntity.getTargetSpawnPoint(level);
			if (delegatedSpawnpoint != null) {
				spawnPoint = delegatedSpawnpoint;
			}
		}

		return spawnPoint;
	}

	public LevelData.RespawnData getActualLocationAccessPoint(Level level) {
		LevelData.RespawnData spawnPoint = this.directTeleportTarget;

		if (!this.directTeleportTarget.pos().equals(BlockPos.ZERO) && level.getBlockEntity(BlockRotationUtils.getActualOffsetBlockPosition(this.getBlockPos(), this.directTeleportTarget.pos())) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			LevelData.RespawnData delegatedSpawnpoint = spawnPointDelegationBlockEntity.getTargetSpawnPoint(level);
			if (delegatedSpawnpoint != null) {
				spawnPoint = delegatedSpawnpoint;
			}
		}

		return spawnPoint;
	}

	public void preTeleportTrigger() {
		if (this.level != null && !this.preTeleportTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.preTeleportTriggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.preTeleportTriggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.preTeleportTriggeredBlock.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.preTeleportTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	public void sendPostTeleportUUIDList(List<UUID> uuidList) {
		if (this.level != null && !this.onTeleportUUIDListHandlerPositionOffset.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.onTeleportUUIDListHandlerPositionOffset.getX(), this.worldPosition.getY() + this.onTeleportUUIDListHandlerPositionOffset.getY(), this.worldPosition.getZ() + this.onTeleportUUIDListHandlerPositionOffset.getZ()));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void postTeleportTrigger() {
		if (this.level != null && !this.postTeleportTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.postTeleportTriggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.postTeleportTriggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.postTeleportTriggeredBlock.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.postTeleportTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	private List<UUID> getUUIDListFromHandler() {
		if (this.level != null) {

			BlockEntity blockEntity = level.getBlockEntity(
					new BlockPos(
							this.worldPosition.getX() + this.uuidListHandlerPositionOffset.getX(),
							this.worldPosition.getY() + this.uuidListHandlerPositionOffset.getY(),
							this.worldPosition.getZ() + this.uuidListHandlerPositionOffset.getZ()
					)
			);
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				return handlesUUIDList.supplyUUIDList(false);
			}
		}
		return List.of();
	}

	@Override
	public void trigger() {
		if (this.level != null) {
			this.getUUIDListFromHandler().forEach((uuid) -> {
				Player playerEntity = this.level.getPlayerByUUID(uuid);
				if (playerEntity instanceof ServerPlayer serverPlayerEntity && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.level, serverPlayerEntity, this.worldPosition, this.level.getBlockState(this.worldPosition), this);
				}
			});
		}
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.level != null) {
			list.forEach((uuid) -> {
				Player playerEntity = this.level.getPlayerByUUID(uuid);
				if (playerEntity instanceof ServerPlayer serverPlayerEntity && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.level, serverPlayerEntity, this.worldPosition, this.level.getBlockState(this.worldPosition), this);
				}
			});
		}
	}

	@Override
	public void reset() {
	}

	public MutablePair<MutablePair<String, String>, MutablePair<String, String>> getDataDrivenLocation() {

		String locationString = this.location.getLeft().getLeft();
		String entranceString = this.location.getLeft().getRight();
		String dataIdString = this.location.getRight().getLeft() + this.sentDataIdentifierAppendix;
		String dataValueString = this.location.getRight().getRight() + this.sentDataValueAppendix;

		BlockPos dataProvidingBlockPosOffset = this.dataProvidingBlockPosOffset;
		if (dataProvidingBlockPosOffset != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity = this.level.getBlockEntity(this.getBlockPos().offset(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ()));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				locationString = locationString + providesDataBlockEntity.getData(this.locationAppendix);
				entranceString = entranceString + providesDataBlockEntity.getData(this.entranceAppendix);
				dataIdString = dataIdString + providesDataBlockEntity.getData(this.sentDataIdentifierAppendix);
				dataValueString = dataValueString + providesDataBlockEntity.getData(this.sentDataValueAppendix);
			}
		}

		return new MutablePair<>(new MutablePair<>(locationString, entranceString), new MutablePair<>(dataIdString, dataValueString));
	}

	// region --- getter & setter ---
	public String getTeleporterName() {
		return teleporterName;
	}

	public void setTeleporterName(String teleporterName) {
		this.teleporterName = teleporterName;
	}

	/**
	 * Determines whether a pop-up window is shown where the player can confirm or deny the teleport.
	 * Has an effect only in some modes
	 */
	public boolean getShowAdventureScreen() {
		return showAdventureScreen;
	}

	public void setShowAdventureScreen(boolean showAdventureScreen) {
		this.showAdventureScreen = showAdventureScreen;
	}

	public CreativeScreenPage getCreativeScreenPage() {
		return this.creativeScreenPage;
	}

	public void setCreativeScreenPage(CreativeScreenPage creativeScreenPage) {
		this.creativeScreenPage = creativeScreenPage;
	}

	public BlockPos getUuidListHandlerPositionOffset() {
		return this.uuidListHandlerPositionOffset;
	}

	public void setUuidListHandlerPositionOffset(BlockPos uuidListHandlerPositionOffset) {
		this.uuidListHandlerPositionOffset = uuidListHandlerPositionOffset;
	}

	public String getPreventInteractionIdentifierString() {
		return this.preventInteractionIdentifierString;
	}

	public void setPreventInteractionIdentifierString(String preventInteractionIdentifierString) {
		this.preventInteractionIdentifierString = preventInteractionIdentifierString;
	}

	public String getInteractionPreventedMessage() {
		return this.interactionPreventedMessage;
	}

	public void setInteractionPreventedMessage(String interactionPreventedMessage) {
		this.interactionPreventedMessage = interactionPreventedMessage;
	}

	public LevelData.RespawnData getLocationAccessPoint() {
		return this.locationAccessPoint;
	}

	public void setLocationAccessPoint(LevelData.RespawnData locationAccessPoint) {
		this.locationAccessPoint = LevelData.RespawnData.of(locationAccessPoint.dimension(), locationAccessPoint.pos(), locationAccessPoint.yaw(), locationAccessPoint.pitch());
	}

	/**
	 * Determines whether the access position of the player should be updated.
	 * Has an effect only in some modes
	 */
	public boolean getEnableLocationAccessPoint() {
		return this.enableLocationAccessPoint;
	}

	public void setEnableLocationAccessPoint(boolean enableLocationAccessPoint) {
		this.enableLocationAccessPoint = enableLocationAccessPoint;
	}

	public boolean onlyTeleportDimensionOwner() {
		return this.onlyTeleportDimensionOwner;
	}

	public void setOnlyTeleportDimensionOwner(boolean onlyTeleportDimensionOwner) {
		this.onlyTeleportDimensionOwner = onlyTeleportDimensionOwner;
	}

	public String getOnlyDimensionOwnerCanTeleportMessage() {
		return this.onlyDimensionOwnerCanTeleportMessage;
	}

	public void setOnlyDimensionOwnerCanTeleportMessage(String onlyDimensionOwnerCanTeleportMessage) {
		this.onlyDimensionOwnerCanTeleportMessage = onlyDimensionOwnerCanTeleportMessage;
	}

	public boolean teleportTeam() {
		return this.teleportTeam;
	}

	public void setTeleportTeam(boolean teleportTeam) {
		this.teleportTeam = teleportTeam;
	}

	public TeleportationMode getTeleportationMode() {
		return this.teleportationMode;
	}

	public void setTeleportationMode(TeleportationMode teleportationMode) {
		this.teleportationMode = teleportationMode;
	}

	public LevelData.RespawnData getDirectTeleportTarget() {
		return this.directTeleportTarget;
	}

	public void setDirectTeleportTarget(LevelData.RespawnData directTeleportTarget) {
		this.directTeleportTarget = LevelData.RespawnData.of(directTeleportTarget.dimension(), directTeleportTarget.pos(), directTeleportTarget.yaw(), directTeleportTarget.pitch());
	}

	public SpawnPointType getSpawnPointType() {
		return this.spawnPointType;
	}

	public void setSpawnPointType(SpawnPointType spawnPointType) {
		this.spawnPointType = spawnPointType;
	}

	public List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> getLocationsList() {
		return this.locationsList;
	}

	public void setLocationsList(List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> locationsList) {
		this.locationsList.clear();
		this.locationsList.addAll(locationsList);
	}

	public MutablePair<MutablePair<String, String>, MutablePair<String, String>> getLocation() {
		return this.location;
	}

	public void setLocation(MutablePair<MutablePair<String, String>, MutablePair<String, String>> location) {
		this.location = location;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public String getLocationAppendix() {
		return this.locationAppendix;
	}

	public void setLocationAppendix(String locationAppendix) {
		this.locationAppendix = locationAppendix;
	}

	public String getEntranceAppendix() {
		return this.entranceAppendix;
	}

	public void setEntranceAppendix(String entranceAppendix) {
		this.entranceAppendix = entranceAppendix;
	}

	public String getSentDataIdentifierAppendix() {
		return this.sentDataIdentifierAppendix;
	}

	public void setSentDataIdentifierAppendix(String sentDataIdentifierAppendix) {
		this.sentDataIdentifierAppendix = sentDataIdentifierAppendix;
	}

	public String getSentDataValueAppendix() {
		return this.sentDataValueAppendix;
	}

	public void setSentDataValueAppendix(String sentDataValueAppendix) {
		this.sentDataValueAppendix = sentDataValueAppendix;
	}

	public MutablePair<BlockPos, Boolean> getPreTeleportTriggeredBlock() {
		return this.preTeleportTriggeredBlock;
	}

	public void setPreTeleportTriggeredBlock(MutablePair<BlockPos, Boolean> preTeleportTriggeredBlock) {
		this.preTeleportTriggeredBlock = preTeleportTriggeredBlock;
	}

	public BlockPos getOnTeleportUUIDListHandlerPositionOffset() {
		return this.onTeleportUUIDListHandlerPositionOffset;
	}

	public void setOnTeleportUUIDListHandlerPositionOffset(BlockPos onTeleportUUIDListHandlerPositionOffset) {
		this.onTeleportUUIDListHandlerPositionOffset = onTeleportUUIDListHandlerPositionOffset;
	}

	public MutablePair<BlockPos, Boolean> getPostTeleportTriggeredBlock() {
		return this.postTeleportTriggeredBlock;
	}

	public void setPostTeleportTriggeredBlock(MutablePair<BlockPos, Boolean> postTeleportTriggeredBlock) {
		this.postTeleportTriggeredBlock = postTeleportTriggeredBlock;
	}

	public String getCurrentTargetIdentifierLabel() {
		return this.currentTargetIdentifierLabel;
	}

	public void setCurrentTargetIdentifierLabel(String currentTargetIdentifierLabel) {
		this.currentTargetIdentifierLabel = currentTargetIdentifierLabel;
	}

	public String getCurrentTargetOwnerLabel() {
		return this.currentTargetOwnerLabel;
	}

	public void setCurrentTargetOwnerLabel(String currentTargetOwnerLabel) {
		this.currentTargetOwnerLabel = currentTargetOwnerLabel;
	}

	public boolean showCancelButton() {
		return this.showCancelButton;
	}

	public void setShowCancelButton(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
	}

	public boolean canOwnerBeChosen() {
		return this.canOwnerBeChosen;
	}

	public void setCanOwnerBeChosen(boolean canOwnerBeChosen) {
		this.canOwnerBeChosen = canOwnerBeChosen;
	}

	public String getTeleportButtonLabel() {
		return this.teleportButtonLabel;
	}

	public void setTeleportButtonLabel(String teleportButtonLabel) {
		this.teleportButtonLabel = teleportButtonLabel;
	}

	public String getCancelTeleportButtonLabel() {
		return this.cancelTeleportButtonLabel;
	}

	public void setCancelTeleportButtonLabel(String cancelTeleportButtonLabel) {
		this.cancelTeleportButtonLabel = cancelTeleportButtonLabel;
	}
	// endregion --- getter & setter ---

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);

				this.accessPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.accessPositionOffset, blockRotation);

				this.locationAccessPoint = BlockRotationUtils.rotateSpawnPoint(this.locationAccessPoint, blockRotation);
				this.directTeleportTarget = BlockRotationUtils.rotateSpawnPoint(this.directTeleportTarget, blockRotation);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, blockRotation);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, Mirror.FRONT_BACK);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, Mirror.FRONT_BACK);

				this.locationAccessPoint = BlockRotationUtils.mirrorSpawnPoint(this.locationAccessPoint, Mirror.FRONT_BACK);
				this.directTeleportTarget = BlockRotationUtils.mirrorSpawnPoint(this.directTeleportTarget, Mirror.FRONT_BACK);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, Mirror.FRONT_BACK);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, Mirror.LEFT_RIGHT);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, Mirror.LEFT_RIGHT);

				this.locationAccessPoint = BlockRotationUtils.mirrorSpawnPoint(this.locationAccessPoint, Mirror.LEFT_RIGHT);
				this.directTeleportTarget = BlockRotationUtils.mirrorSpawnPoint(this.directTeleportTarget, Mirror.LEFT_RIGHT);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, Mirror.LEFT_RIGHT);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum TeleportationMode implements StringRepresentable {
		DIRECT("direct"),
		SPAWN_POINTS("spawn_points"),
		LOCATIONS("locations"),
		LOCATION("location");

		private final String name;

		TeleportationMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<TeleportationMode> byName(String name) {
			return Arrays.stream(TeleportationMode.values()).filter(teleportationMode -> teleportationMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.teleporter_block.teleportation_mode." + this.name);
		}
	}

	public enum SpawnPointType implements StringRepresentable {
		WORLD_SPAWN("world_spawn"),
		PLAYER_SPAWN("player_spawn"),
		LOCATION_ACCESS_POSITION("location_access_position");

		private final String name;

		SpawnPointType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<SpawnPointType> byName(String name) {
			return Arrays.stream(SpawnPointType.values()).filter(spawnPointType -> spawnPointType.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.teleporter_block.spawn_point_type." + this.name);
		}
	}

	public enum CreativeScreenPage implements StringRepresentable {
		ACTIVATION("activation"),
		TELEPORTATION_MODE("teleportation_mode"),
		ON_TELEPORT_EVENTS("on_teleport_events"),
		ADVENTURE_SCREEN_CUSTOMIZATION("adventure_screen_customization");

		private final String name;

		CreativeScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<CreativeScreenPage> byName(String name) {
			return Arrays.stream(CreativeScreenPage.values()).filter(creativeScreenPage -> creativeScreenPage.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.teleporter_block.creative_screen_page." + this.name);
		}
	}
}

