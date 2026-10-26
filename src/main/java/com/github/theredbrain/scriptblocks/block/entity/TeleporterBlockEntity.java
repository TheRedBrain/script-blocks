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
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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

	private BlockPos uuidListHandlerPositionOffset = BlockPos.ORIGIN;

	private BlockPos accessPositionOffset = BlockPos.ORIGIN;
	private boolean setAccessPosition = false;

	private boolean onlyTeleportDimensionOwner = false;
	private String onlyDimensionOwnerCanTeleportMessage = "hud.message.only_dimension_owner_can_teleport";

	private boolean teleportTeam = false;

	private CreativeScreenPage creativeScreenPage = CreativeScreenPage.ACTIVATION;
	private TeleportationMode teleportationMode = TeleportationMode.DIRECT;

	// direct teleportation mode
	private BlockPos directTeleportPositionOffset = BlockPos.ORIGIN;
	private double directTeleportOrientationYaw = 0.0;
	private double directTeleportOrientationPitch = 0.0;

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

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;

	private MutablePair<BlockPos, Boolean> preTeleportTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos onTeleportUUIDListHandlerPositionOffset = BlockPos.ORIGIN;
	private MutablePair<BlockPos, Boolean> postTeleportTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);

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
	protected void writeData(WriteView view) {

		view.putString("screen_page", this.creativeScreenPage.asString());

		view.putString("prevent_interaction_identifier_string", this.preventInteractionIdentifierString);

		view.putString("interaction_prevented_message", this.interactionPreventedMessage);

		view.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.uuidListHandlerPositionOffset);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.accessPositionOffset);
		nbtCompound.putBoolean("is_enabled", this.setAccessPosition);
		view.put("location_access", NbtCompound.CODEC, nbtCompound.copy());

		view.putBoolean("only_teleport_dimension_owner", this.onlyTeleportDimensionOwner);

		view.putString("only_dimension_owner_can_teleport_message", this.onlyDimensionOwnerCanTeleportMessage);

		view.putBoolean("teleport_team", this.teleportTeam);

		view.putString("teleportation_mode", this.teleportationMode.asString());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.directTeleportPositionOffset);
		nbtCompound.putDouble("yaw", this.directTeleportOrientationYaw);
		nbtCompound.putDouble("pitch", this.directTeleportOrientationPitch);
		view.put("direct_teleport", NbtCompound.CODEC, nbtCompound.copy());

		view.putString("spawn_point_type", this.spawnPointType.asString());

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("locations", NbtCompound.CODEC);
		for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> location : this.locationsList) {
			nbtCompound = new NbtCompound();
			nbtCompound.putString("identifier", location.getLeft().getLeft());
			nbtCompound.putString("entrance", location.getLeft().getRight());
			nbtCompound.putString("sent_data_id", location.getRight().getLeft());
			nbtCompound.putString("sent_data", location.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		nbtCompound = new NbtCompound();
		nbtCompound.putString("identifier", this.location.getLeft().getLeft());
		nbtCompound.putString("entrance", this.location.getLeft().getRight());
		nbtCompound.putString("sent_data_id", this.location.getRight().getLeft());
		nbtCompound.putString("sent_data", this.location.getRight().getRight());
		nbtCompound.putString("identifier_appendix", this.locationAppendix);
		nbtCompound.putString("entrance_appendix", this.entranceAppendix);
		nbtCompound.putString("sent_data_id_appendix", this.sentDataIdentifierAppendix);
		nbtCompound.putString("sent_data_appendix", this.sentDataValueAppendix);
		view.put("location", NbtCompound.CODEC, nbtCompound.copy());

		view.put("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.preTeleportTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.preTeleportTriggeredBlock.getRight());
		view.put("pre_teleport", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onTeleportUUIDListHandlerPositionOffset);
		view.put("on_teleport", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.postTeleportTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.postTeleportTriggeredBlock.getRight());
		view.put("post_teleport", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("show_adventure_screen", this.showAdventureScreen);
		nbtCompound.putString("teleporter_name", this.teleporterName);
		nbtCompound.putString("current_target_identifier_label", this.currentTargetIdentifierLabel);
		nbtCompound.putString("current_target_owner_label", this.currentTargetOwnerLabel);
		nbtCompound.putBoolean("show_cancel_button", this.showCancelButton);
		nbtCompound.putBoolean("can_owner_be_chosen", this.canOwnerBeChosen);
		nbtCompound.putString("teleport_button_label", this.teleportButtonLabel);
		nbtCompound.putString("cancel_teleport_button_label", this.cancelTeleportButtonLabel);
		view.put("gameplay_screen", NbtCompound.CODEC, nbtCompound.copy());

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.creativeScreenPage = CreativeScreenPage.byName(view.getString("screen_page", "")).orElseGet(() -> CreativeScreenPage.ACTIVATION);

		this.preventInteractionIdentifierString = view.getString("prevent_interaction_identifier_string", "");

		this.interactionPreventedMessage = view.getString("interaction_prevented_message", "");

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		NbtCompound nbtCompound = view.read("location_access", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.accessPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.setAccessPosition = nbtCompound.getBoolean("is_enabled", false);

		this.onlyTeleportDimensionOwner = view.getBoolean("only_teleport_dimension_owner", false);

		this.onlyDimensionOwnerCanTeleportMessage = view.getString("only_dimension_owner_can_teleport_message", "hud.message.only_dimension_owner_can_teleport");

		this.teleportTeam = view.getBoolean("teleport_team", false);

		this.teleportationMode = TeleportationMode.byName(view.getString("teleportation_mode", "")).orElseGet(() -> TeleportationMode.DIRECT);

		nbtCompound = view.read("direct_teleport", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.directTeleportPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.directTeleportOrientationYaw = nbtCompound.getDouble("yaw", 0);

		this.directTeleportOrientationPitch = nbtCompound.getDouble("pitch", 0);

		this.spawnPointType = SpawnPointType.byName(view.getString("spawn_point_type", "")).orElseGet(() -> SpawnPointType.WORLD_SPAWN);

		this.locationsList.clear();
				for (NbtCompound nbtCompound1 : view.getTypedListView("locations", NbtCompound.CODEC)) {
				this.locationsList.add(
						new MutablePair<>(
								new MutablePair<>(
										nbtCompound1.getString("identifier", ""),
										nbtCompound1.getString("entrance", "")
								),
								new MutablePair<>(
										nbtCompound1.getString("sent_data_id", ""),
										nbtCompound1.getString("sent_data", "")
								)
						)
				);
			}

		nbtCompound = view.read("location", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.location = new MutablePair<>(
				new MutablePair<>(
						nbtCompound.getString("identifier", ""),
						nbtCompound.getString("entrance", "")
				),
				new MutablePair<>(
						nbtCompound.getString("sent_data_id", ""),
						nbtCompound.getString("sent_data", "")
				)
		);

		this.sentDataValueAppendix = nbtCompound.getString("identifier_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getString("entrance_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getString("sent_data_id_appendix", "");

		this.sentDataValueAppendix = nbtCompound.getString("sent_data_appendix", "");

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("pre_teleport", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.preTeleportTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		nbtCompound = view.read("on_teleport", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onTeleportUUIDListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("post_teleport", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.postTeleportTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		nbtCompound = view.read("gameplay_screen", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showAdventureScreen = nbtCompound.getBoolean("show_adventure_screen", true);

		this.teleporterName = nbtCompound.getString("teleporter_name", "gui.teleporter_block.teleporter_name_field.label");

		this.currentTargetIdentifierLabel = nbtCompound.getString("current_target_identifier_label", "gui.teleporter_block.target_identifier_field.label");

		this.currentTargetOwnerLabel = nbtCompound.getString("current_target_owner_label", "gui.teleporter_block.target_owner_field.label");

		this.showCancelButton = nbtCompound.getBoolean("show_cancel_button", true);

		this.canOwnerBeChosen = nbtCompound.getBoolean("can_owner_be_chosen", true);

		this.teleportButtonLabel = nbtCompound.getString("teleport_button_label", "gui.teleporter_block.teleport_button.label");

		this.cancelTeleportButtonLabel = nbtCompound.getString("cancel_teleport_button_label", "gui.teleporter_block.cancel_teleport_button.label");

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void openNonCreativeScreen(World world, ServerPlayerEntity serverPlayerEntity, BlockPos pos, BlockState state, TeleporterBlockEntity teleporterBlockEntity) {

		if (teleporterBlockEntity.onlyTeleportDimensionOwner && !serverPlayerEntity.getUuid().toString().equals(world.getRegistryKey().getValue().getPath())) {
			if (!teleporterBlockEntity.getOnlyDimensionOwnerCanTeleportMessage().isEmpty()) {
				serverPlayerEntity.sendMessage(Text.translatable(teleporterBlockEntity.getOnlyDimensionOwnerCanTeleportMessage()), true);
			}
			return;
		}

		if (!teleporterBlockEntity.getPreventInteractionIdentifierString().isEmpty() && !PlayerEntityHelper.allowInteractions(serverPlayerEntity, teleporterBlockEntity.getPreventInteractionIdentifierString())) {
			if (!teleporterBlockEntity.getInteractionPreventedMessage().isEmpty()) {
				serverPlayerEntity.sendMessage(Text.translatable(teleporterBlockEntity.getInteractionPreventedMessage()), true);
			}
			return;
		}

		List<VisibleLocation> visibleLocationsList = new ArrayList<>();

		// check locationsList for visible / unlocked locations
		if (teleporterBlockEntity.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS || teleporterBlockEntity.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATION) {

			if (teleporterBlockEntity.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> entry : teleporterBlockEntity.locationsList) {

					Location location = null;
					if (world != null) {
						Optional<RegistryEntry.Reference<Location>> optionalLocationReference = world.getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.of(entry.getLeft().getLeft()));
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
			} else if (teleporterBlockEntity.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATION) {
				MutablePair<MutablePair<String, String>, MutablePair<String, String>> dataDrivenLocation = teleporterBlockEntity.getDataDrivenLocation();
				Location location = null;
				if (world != null) {
					Optional<RegistryEntry.Reference<Location>> optionalLocationReference = world.getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.of(dataDrivenLocation.getLeft().getLeft()));
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

		serverPlayerEntity.openHandledScreen(new ExtendedScreenHandlerFactory<>() {

			@Override
			public TeleporterBlockScreenHandler.TeleporterBlockData getScreenOpeningData(ServerPlayerEntity player) {
				return new TeleporterBlockScreenHandler.TeleporterBlockData(
						teleporterBlockEntity.pos,
						visibleLocationsList
				);
			}

			@Override
			public Text getDisplayName() {
				return Text.translatable(teleporterBlockEntity.teleporterName);
			}

			@Nullable
			@Override
			public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
				return new TeleporterBlockScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(teleporterBlockEntity.world, teleporterBlockEntity.pos), teleporterBlockEntity.pos, visibleLocationsList);
			}

		});
	}

	public void preTeleportTrigger() {
		if (this.world != null && !this.preTeleportTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.preTeleportTriggeredBlock.getLeft().getX(), this.pos.getY() + this.preTeleportTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.preTeleportTriggeredBlock.getLeft().getZ()));
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
		if (this.world != null && !this.onTeleportUUIDListHandlerPositionOffset.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.onTeleportUUIDListHandlerPositionOffset.getX(), this.pos.getY() + this.onTeleportUUIDListHandlerPositionOffset.getY(), this.pos.getZ() + this.onTeleportUUIDListHandlerPositionOffset.getZ()));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void postTeleportTrigger() {
		if (this.world != null && !this.postTeleportTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.postTeleportTriggeredBlock.getLeft().getX(), this.pos.getY() + this.postTeleportTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.postTeleportTriggeredBlock.getLeft().getZ()));
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
		if (this.world != null) {

			BlockEntity blockEntity = world.getBlockEntity(
					new BlockPos(
							this.pos.getX() + this.uuidListHandlerPositionOffset.getX(),
							this.pos.getY() + this.uuidListHandlerPositionOffset.getY(),
							this.pos.getZ() + this.uuidListHandlerPositionOffset.getZ()
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
		if (this.world != null) {
			this.getUUIDListFromHandler().forEach((uuid) -> {
				PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
				if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.world, serverPlayerEntity, this.pos, this.world.getBlockState(this.pos), this);
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
		if (this.world != null) {
			list.forEach((uuid) -> {
				PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
				if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.world, serverPlayerEntity, this.pos, this.world.getBlockState(this.pos), this);
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
		if (dataProvidingBlockPosOffset != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.getPos().add(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ()));
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

	public BlockPos getAccessPositionOffset() {
		return this.accessPositionOffset;
	}

	public void setAccessPositionOffset(BlockPos accessPositionOffset) {
		this.accessPositionOffset = accessPositionOffset;
	}

	/**
	 * Determines whether the access position of the player should be updated.
	 * Has an effect only in some modes
	 */
	public boolean getSetAccessPosition() {
		return this.setAccessPosition;
	}

	public void setSetAccessPosition(boolean setAccessPosition) {
		this.setAccessPosition = setAccessPosition;
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

	public BlockPos getDirectTeleportPositionOffset() {
		return this.directTeleportPositionOffset;
	}

	public void setDirectTeleportPositionOffset(BlockPos directTeleportPositionOffset) {
		this.directTeleportPositionOffset = directTeleportPositionOffset;
	}

	public double getDirectTeleportOrientationYaw() {
		return this.directTeleportOrientationYaw;
	}

	// TODO check if input is valid
	public boolean setDirectTeleportOrientationYaw(double directTeleportOrientationYaw) {
		this.directTeleportOrientationYaw = directTeleportOrientationYaw;
		return true;
	}

	public double getDirectTeleportOrientationPitch() {
		return this.directTeleportOrientationPitch;
	}

	// TODO check if input is valid
	public boolean setDirectTeleportOrientationPitch(double directTeleportOrientationPitch) {
		this.directTeleportOrientationPitch = directTeleportOrientationPitch;
		return true;
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);

				this.accessPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.accessPositionOffset, blockRotation);

				this.directTeleportPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.directTeleportPositionOffset, blockRotation);
				this.directTeleportOrientationYaw = BlockRotationUtils.rotateYaw(this.directTeleportOrientationYaw, blockRotation);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, blockRotation);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.FRONT_BACK);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.FRONT_BACK);

				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.FRONT_BACK);
				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.FRONT_BACK);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, BlockMirror.FRONT_BACK);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.LEFT_RIGHT);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.LEFT_RIGHT);

				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.LEFT_RIGHT);
				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.LEFT_RIGHT);

				this.preTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.preTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onTeleportUUIDListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.onTeleportUUIDListHandlerPositionOffset, BlockMirror.LEFT_RIGHT);
				this.postTeleportTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.postTeleportTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum TeleportationMode implements StringIdentifiable {
		DIRECT("direct"),
		SPAWN_POINTS("spawn_points"),
		LOCATIONS("locations"),
		LOCATION("location");

		private final String name;

		TeleportationMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<TeleportationMode> byName(String name) {
			return Arrays.stream(TeleportationMode.values()).filter(teleportationMode -> teleportationMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.teleporter_block.teleportation_mode." + this.name);
		}
	}

	public enum SpawnPointType implements StringIdentifiable {
		WORLD_SPAWN("world_spawn"),
		PLAYER_SPAWN("player_spawn"),
		LOCATION_ACCESS_POSITION("location_access_position");

		private final String name;

		SpawnPointType(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<SpawnPointType> byName(String name) {
			return Arrays.stream(SpawnPointType.values()).filter(spawnPointType -> spawnPointType.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.teleporter_block.spawn_point_type." + this.name);
		}
	}

	public enum CreativeScreenPage implements StringIdentifiable {
		ACTIVATION("activation"),
		TELEPORTATION_MODE("teleportation_mode"),
		ON_TELEPORT_EVENTS("on_teleport_events"),
		ADVENTURE_SCREEN_CUSTOMIZATION("adventure_screen_customization");

		private final String name;

		CreativeScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<CreativeScreenPage> byName(String name) {
			return Arrays.stream(CreativeScreenPage.values()).filter(creativeScreenPage -> creativeScreenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.teleporter_block.creative_screen_page." + this.name);
		}
	}
}

