package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
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

public class TeleporterBlockEntity extends RotatedBlockEntity implements ExtendedScreenHandlerFactory<TeleporterBlockScreenHandler.TeleporterBlockData>, HandlesUUIDList, Triggerable {

	private boolean showAdventureScreen = true;
	private BlockPos uuidListHandlerPositionOffset = BlockPos.ORIGIN;

	private BlockPos accessPositionOffset = BlockPos.ORIGIN;
	private boolean setAccessPosition = false;

	private boolean onlyTeleportDimensionOwner = false;
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
	private boolean showRegenerateButton = true;
	private boolean showCancelButton = true;
	private boolean canOwnerBeChosen = true;
	private String teleportButtonLabel = "gui.teleporter_block.teleport_button.label";
	private String cancelTeleportButtonLabel = "gui.teleporter_block.cancel_teleport_button.label";

	public TeleporterBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TELEPORTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putString("screen_page", this.creativeScreenPage.asString());

		nbt.put("uuid_list_handler_position_offset", NbtHelper.fromBlockPos(this.uuidListHandlerPositionOffset));

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.accessPositionOffset));
		nbtCompound.putBoolean("is_enabled", this.setAccessPosition);
		nbt.put("location_access", nbtCompound.copy());

		nbt.putBoolean("only_teleport_dimension_owner", this.onlyTeleportDimensionOwner);

		nbt.putBoolean("teleport_team", this.teleportTeam);

		nbt.putString("teleportation_mode", this.teleportationMode.asString());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.directTeleportPositionOffset));
		nbtCompound.putDouble("yaw", this.directTeleportOrientationYaw);
		nbtCompound.putDouble("pitch", this.directTeleportOrientationPitch);
		nbt.put("direct_teleport", nbtCompound.copy());

		nbt.putString("spawn_point_type", this.spawnPointType.asString());

		NbtList nbtList = new NbtList();
		for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> location : this.locationsList) {
			nbtCompound = new NbtCompound();
			nbtCompound.putString("identifier", location.getLeft().getLeft());
			nbtCompound.putString("entrance", location.getLeft().getRight());
			nbtCompound.putString("sent_data_id", location.getRight().getLeft());
			nbtCompound.putString("sent_data", location.getRight().getRight());
			nbtList.add(nbtCompound.copy());
		}
		nbt.put("locations", nbtList);

		nbtCompound = new NbtCompound();
		nbtCompound.putString("identifier", this.location.getLeft().getLeft());
		nbtCompound.putString("entrance", this.location.getLeft().getRight());
		nbtCompound.putString("sent_data_id", this.location.getRight().getLeft());
		nbtCompound.putString("sent_data", this.location.getRight().getRight());
		nbtCompound.putString("identifier_appendix", this.locationAppendix);
		nbtCompound.putString("entrance_appendix", this.entranceAppendix);
		nbtCompound.putString("sent_data_id_appendix", this.sentDataIdentifierAppendix);
		nbtCompound.putString("sent_data_appendix", this.sentDataValueAppendix);
		nbt.put("location", nbtCompound.copy());

		nbt.put("data_providing_block_position_offset", NbtHelper.fromBlockPos(this.dataProvidingBlockPosOffset));

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.preTeleportTriggeredBlock.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.preTeleportTriggeredBlock.getRight());
		nbt.put("pre_teleport", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("uuid_list_handler_position_offset", NbtHelper.fromBlockPos(this.onTeleportUUIDListHandlerPositionOffset));
		nbt.put("on_teleport", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.postTeleportTriggeredBlock.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.postTeleportTriggeredBlock.getRight());
		nbt.put("post_teleport", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("show_adventure_screen", this.showAdventureScreen);
		nbtCompound.putString("teleporter_name", this.teleporterName);
		nbtCompound.putString("current_target_identifier_label", this.currentTargetIdentifierLabel);
		nbtCompound.putString("current_target_owner_label", this.currentTargetOwnerLabel);
		nbtCompound.putBoolean("show_regenerate_button", this.showRegenerateButton);
		nbtCompound.putBoolean("show_cancel_button", this.showCancelButton);
		nbtCompound.putBoolean("can_owner_be_chosen", this.canOwnerBeChosen);
		nbtCompound.putString("teleport_button_label", this.teleportButtonLabel);
		nbtCompound.putString("cancel_teleport_button_label", this.cancelTeleportButtonLabel);
		nbt.put("gameplay_screen", nbtCompound.copy());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("creativeScreenPage")) {
			this.creativeScreenPage = CreativeScreenPage.byName(nbt.getString("creativeScreenPage")).orElseGet(() -> CreativeScreenPage.ACTIVATION);
			nbt.remove("creativeScreenPage");
		} else {
			this.creativeScreenPage = CreativeScreenPage.byName(nbt.getString("screen_page")).orElseGet(() -> CreativeScreenPage.ACTIVATION);
		}

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						NbtHelper.toBlockPos(nbt, "uuid_list_handler_position_offset").orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		NbtCompound nbtCompound = nbt.getCompound("location_access");
		if (nbt.contains("accessPositionOffsetX") || nbt.contains("accessPositionOffsetY") || nbt.contains("accessPositionOffsetZ")) {
			this.accessPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("accessPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("accessPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("accessPositionOffsetZ"), -48, 48)
			);
		} else {
			this.accessPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("setAccessPosition")) {
			this.setAccessPosition = nbt.getBoolean("setAccessPosition");
			nbt.remove("setAccessPosition");
		} else {
			this.setAccessPosition = nbtCompound.getBoolean("is_enabled");
		}

		if (nbt.contains("onlyTeleportDimensionOwner")) {
			this.onlyTeleportDimensionOwner = nbt.getBoolean("onlyTeleportDimensionOwner");
			nbt.remove("onlyTeleportDimensionOwner");
		} else {
			this.onlyTeleportDimensionOwner = nbt.getBoolean("only_teleport_dimension_owner");
		}

		if (nbt.contains("teleportTeam")) {
			this.teleportTeam = nbt.getBoolean("teleportTeam");
			nbt.remove("teleportTeam");
		} else {
			this.teleportTeam = nbt.getBoolean("teleport_team");
		}

		if (nbt.contains("teleportationMode")) {
			this.teleportationMode = TeleportationMode.byName(nbt.getString("teleportationMode")).orElseGet(() -> TeleportationMode.DIRECT);
			nbt.remove("teleportationMode");
		} else {
			this.teleportationMode = TeleportationMode.byName(nbt.getString("teleportation_mode")).orElseGet(() -> TeleportationMode.DIRECT);
		}

		nbtCompound = nbt.getCompound("direct_teleport");
		if (nbt.contains("directTeleportPositionOffsetX") || nbt.contains("directTeleportPositionOffsetY") || nbt.contains("directTeleportPositionOffsetZ")) {
			this.directTeleportPositionOffset = new BlockPos(
					nbt.getInt("directTeleportPositionOffsetX"),
					nbt.getInt("directTeleportPositionOffsetY"),
					nbt.getInt("directTeleportPositionOffsetZ")
			);
			nbt.remove("directTeleportPositionOffsetX");
			nbt.remove("directTeleportPositionOffsetY");
			nbt.remove("directTeleportPositionOffsetZ");
		} else {
			this.directTeleportPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("directTeleportPositionOffsetYaw")) {
			this.directTeleportOrientationYaw = nbt.getDouble("directTeleportPositionOffsetYaw");
			nbt.remove("directTeleportPositionOffsetYaw");
		} else {
			this.directTeleportOrientationYaw = nbtCompound.getDouble("yaw");
		}

		if (nbt.contains("directTeleportPositionOffsetPitch")) {
			this.directTeleportOrientationPitch = nbt.getDouble("directTeleportPositionOffsetPitch");
			nbt.remove("directTeleportPositionOffsetPitch");
		} else {
			this.directTeleportOrientationPitch = nbtCompound.getDouble("pitch");
		}

		if (nbt.contains("spawnPointType")) {
			this.spawnPointType = SpawnPointType.byName(nbt.getString("spawnPointType")).orElseGet(() -> SpawnPointType.WORLD_SPAWN);
			nbt.remove("spawnPointType");
		} else {
			this.spawnPointType = SpawnPointType.byName(nbt.getString("spawn_point_type")).orElseGet(() -> SpawnPointType.WORLD_SPAWN);
		}

		this.locationsList.clear();
		if (nbt.contains("locationsListSize")) {
			int locationsListSize = nbt.getInt("locationsListSize");
			for (int p = 0; p < locationsListSize; p++) {
				this.locationsList.add(new MutablePair<>(new MutablePair<>(nbt.getString("locationsListIdentifier_" + p), nbt.getString("locationsListEntrance_" + p)), new MutablePair<>(nbt.getString("locationsListDataId_" + p), nbt.getString("locationsListData_" + p))));
				nbt.remove("locationsListIdentifier_" + p);
				nbt.remove("locationsListEntrance_" + p);
				nbt.remove("locationsListDataId_" + p);
				nbt.remove("locationsListData_" + p);
			}
			nbt.remove("locationsListSize");
		} else {
			NbtList nbtList = nbt.getList("triggered_blocks", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				nbtCompound = nbtList.getCompound(i);
				this.locationsList.add(
						new MutablePair<>(
								new MutablePair<>(
										nbtCompound.getString("identifier"),
										nbtCompound.getString("entrance")
								),
								new MutablePair<>(
										nbtCompound.getString("sent_data_id"),
										nbtCompound.getString("sent_data")
								)
						)
				);
			}
		}

		nbtCompound = nbt.getCompound("location");
		if (nbt.contains("locationIdentifier") || nbt.contains("locationEntrance") || nbt.contains("locationDataId") || nbt.contains("locationData")) {
			this.location = new MutablePair<>(new MutablePair<>(nbt.getString("locationIdentifier"), nbt.getString("locationEntrance")), new MutablePair<>(nbt.getString("locationDataId"), nbt.getString("locationData")));
			nbt.remove("locationIdentifier");
			nbt.remove("locationEntrance");
			nbt.remove("locationDataId");
			nbt.remove("locationData");
		} else {
			this.location = new MutablePair<>(
					new MutablePair<>(
							nbtCompound.getString("identifier"),
							nbtCompound.getString("entrance")
					),
					new MutablePair<>(
							nbtCompound.getString("sent_data_id"),
							nbtCompound.getString("sent_data")
					)
			);
		}

		if (nbt.contains("locationDataIdentifier")) {
			this.locationAppendix = nbt.getString("locationDataIdentifier");
			nbt.remove("directTeleportPositionOffsetZ");
		} else {
			this.sentDataValueAppendix = nbtCompound.getString("identifier_appendix");
		}

		if (nbt.contains("entranceDataIdentifier")) {
			this.entranceAppendix = nbt.getString("entranceDataIdentifier");
			nbt.remove("directTeleportPositionOffsetZ");
		} else {
			this.sentDataValueAppendix = nbtCompound.getString("entrance_appendix");
		}

		if (nbt.contains("sendDataIdentifierDataIdentifier")) {
			this.sentDataIdentifierAppendix = nbt.getString("sendDataIdentifierDataIdentifier");
			nbt.remove("directTeleportPositionOffsetZ");
		} else {
			this.sentDataValueAppendix = nbtCompound.getString("sent_data_id_appendix");
		}

		if (nbt.contains("sendDataValueDataIdentifier")) {
			this.sentDataValueAppendix = nbt.getString("sendDataValueDataIdentifier");
			nbt.remove("directTeleportPositionOffsetZ");
		} else {
			this.sentDataValueAppendix = nbtCompound.getString("sent_data_appendix");
		}

		if (nbt.contains("dataProvidingBlockPosOffsetX") || nbt.contains("dataProvidingBlockPosOffsetY") || nbt.contains("dataProvidingBlockPosOffsetZ")) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		} else {
			this.dataProvidingBlockPosOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "data_providing_block_pos_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("pre_teleport_triggered_block_position_offset_x") || nbt.contains("pre_teleport_triggered_block_position_offset_y") || nbt.contains("pre_teleport_triggered_block_position_offset_z") || nbt.contains("pre_teleport_triggered_block_resets")) {
			this.preTeleportTriggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("pre_teleport_triggered_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("pre_teleport_triggered_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("pre_teleport_triggered_block_position_offset_z"), -48, 48)
			), nbt.getBoolean("pre_teleport_triggered_block_resets"));
			nbt.remove("pre_teleport_triggered_block_position_offset_x");
			nbt.remove("pre_teleport_triggered_block_position_offset_y");
			nbt.remove("pre_teleport_triggered_block_position_offset_z");
			nbt.remove("pre_teleport_triggered_block_resets");
		} else {
			nbtCompound = nbt.getCompound("pre_teleport");
			this.preTeleportTriggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "triggered_block_position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("triggered_block_resets")
			);
		}

		if (nbt.contains("uuid_list_handler_position_offset_x") || nbt.contains("uuid_list_handler_position_offset_y") || nbt.contains("uuid_list_handler_position_offset_z")) {
			this.onTeleportUUIDListHandlerPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_z"), -48, 48)
			);
			nbt.remove("uuid_list_handler_position_offset_x");
			nbt.remove("uuid_list_handler_position_offset_y");
			nbt.remove("uuid_list_handler_position_offset_z");
		} else {
			nbtCompound = nbt.getCompound("on_teleport");
			this.onTeleportUUIDListHandlerPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "uuid_list_handler_position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("post_teleport_triggered_block_position_offset_x") || nbt.contains("post_teleport_triggered_block_position_offset_y") || nbt.contains("post_teleport_triggered_block_position_offset_z") || nbt.contains("post_teleport_triggered_block_resets")) {
			this.postTeleportTriggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("post_teleport_triggered_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("post_teleport_triggered_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("post_teleport_triggered_block_position_offset_z"), -48, 48)
			), nbt.getBoolean("post_teleport_triggered_block_resets"));
			nbt.remove("post_teleport_triggered_block_position_offset_x");
			nbt.remove("post_teleport_triggered_block_position_offset_y");
			nbt.remove("post_teleport_triggered_block_position_offset_z");
			nbt.remove("post_teleport_triggered_block_resets");
		} else {
			nbtCompound = nbt.getCompound("post_teleport");
			this.postTeleportTriggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "triggered_block_position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("triggered_block_resets")
			);
		}

		nbtCompound = nbt.getCompound("gameplay_screen");
		if (nbt.contains("showAdventureScreen")) {
			this.showAdventureScreen = nbt.getBoolean("showAdventureScreen");
			nbt.remove("showAdventureScreen");
		} else {
			this.showAdventureScreen = nbtCompound.getBoolean("show_adventure_screen");
		}

		if (nbt.contains("teleporterName")) {
			this.teleporterName = nbt.getString("teleporterName");
			nbt.remove("teleporterName");
		} else {
			this.teleporterName = nbtCompound.getString("teleporter_name");
		}

		if (nbt.contains("currentTargetIdentifierLabel")) {
			this.currentTargetIdentifierLabel = nbt.getString("currentTargetIdentifierLabel");
			nbt.remove("currentTargetIdentifierLabel");
		} else {
			this.currentTargetIdentifierLabel = nbtCompound.getString("current_target_identifier_label");
		}

		if (nbt.contains("currentTargetOwnerLabel")) {
			this.currentTargetOwnerLabel = nbt.getString("currentTargetOwnerLabel");
			nbt.remove("currentTargetOwnerLabel");
		} else {
			this.currentTargetOwnerLabel = nbtCompound.getString("current_target_owner_label");
		}

		if (nbt.contains("showRegenerateButton")) {
			this.showRegenerateButton = nbt.getBoolean("showRegenerateButton");
			nbt.remove("showRegenerateButton");
		} else {
			this.showRegenerateButton = nbtCompound.getBoolean("show_regenerate_button");
		}

		if (nbt.contains("showCancelButton")) {
			this.showCancelButton = nbt.getBoolean("showCancelButton");
			nbt.remove("showCancelButton");
		} else {
			this.showCancelButton = nbtCompound.getBoolean("show_cancel_button");
		}

		if (nbt.contains("canOwnerBeChosen")) {
			this.canOwnerBeChosen = nbt.getBoolean("canOwnerBeChosen");
			nbt.remove("canOwnerBeChosen");
		} else {
			this.canOwnerBeChosen = nbtCompound.getBoolean("can_owner_be_chosen");
		}

		if (nbt.contains("teleportButtonLabel")) {
			this.teleportButtonLabel = nbt.getString("teleportButtonLabel");
			nbt.remove("teleportButtonLabel");
		} else {
			this.teleportButtonLabel = nbtCompound.getString("teleport_button_label");
		}

		if (nbt.contains("cancelTeleportButtonLabel")) {
			this.cancelTeleportButtonLabel = nbt.getString("cancelTeleportButtonLabel");
			nbt.remove("cancelTeleportButtonLabel");
		} else {
			this.cancelTeleportButtonLabel = nbtCompound.getString("cancel_teleport_button_label");
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void openNonCreativeScreen(World world, PlayerEntity player, BlockPos pos, BlockState state, TeleporterBlockEntity teleporterBlockEntity) {

		if (!teleporterBlockEntity.onlyTeleportDimensionOwner || player.getUuid().toString().equals(world.getRegistryKey().getValue().getPath())) {
			// TODO check for "prevent teleport" items
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
		} else {
			player.sendMessage(Text.translatable("hud.message.only_dimension_owner_can_teleport"), true);
		}
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
				if (playerEntity != null && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.world, playerEntity, this.pos, this.world.getBlockState(this.pos), this);
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
				if (playerEntity != null && !playerEntity.isCreative()) {
					openNonCreativeScreen(this.world, playerEntity, this.pos, this.world.getBlockState(this.pos), this);
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
		return uuidListHandlerPositionOffset;
	}

	public void setUuidListHandlerPositionOffset(BlockPos uuidListHandlerPositionOffset) {
		this.uuidListHandlerPositionOffset = uuidListHandlerPositionOffset;
	}

	public BlockPos getAccessPositionOffset() {
		return accessPositionOffset;
	}

	public void setAccessPositionOffset(BlockPos accessPositionOffset) {
		this.accessPositionOffset = accessPositionOffset;
	}

	/**
	 * Determines whether the access position of the player should be updated.
	 * Has an effect only in some modes
	 */
	public boolean getSetAccessPosition() {
		return setAccessPosition;
	}

	public void setSetAccessPosition(boolean setAccessPosition) {
		this.setAccessPosition = setAccessPosition;
	}

	public boolean onlyTeleportDimensionOwner() {
		return onlyTeleportDimensionOwner;
	}

	public void setOnlyTeleportDimensionOwner(boolean onlyTeleportDimensionOwner) {
		this.onlyTeleportDimensionOwner = onlyTeleportDimensionOwner;
	}

	public boolean teleportTeam() {
		return teleportTeam;
	}

	public void setTeleportTeam(boolean teleportTeam) {
		this.teleportTeam = teleportTeam;
	}

	public TeleportationMode getTeleportationMode() {
		return teleportationMode;
	}

	public void setTeleportationMode(TeleportationMode teleportationMode) {
		this.teleportationMode = teleportationMode;
	}

	public BlockPos getDirectTeleportPositionOffset() {
		return directTeleportPositionOffset;
	}

	public void setDirectTeleportPositionOffset(BlockPos directTeleportPositionOffset) {
		this.directTeleportPositionOffset = directTeleportPositionOffset;
	}

	public double getDirectTeleportOrientationYaw() {
		return directTeleportOrientationYaw;
	}

	// TODO check if input is valid
	public boolean setDirectTeleportOrientationYaw(double directTeleportOrientationYaw) {
		this.directTeleportOrientationYaw = directTeleportOrientationYaw;
		return true;
	}

	public double getDirectTeleportOrientationPitch() {
		return directTeleportOrientationPitch;
	}

	// TODO check if input is valid
	public boolean setDirectTeleportOrientationPitch(double directTeleportOrientationPitch) {
		this.directTeleportOrientationPitch = directTeleportOrientationPitch;
		return true;
	}

	public SpawnPointType getSpawnPointType() {
		return spawnPointType;
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

	public boolean showRegenerateButton() {
		return this.showRegenerateButton;
	}

	public void setShowRegenerateButton(boolean showRegenerateButton) {
		this.showRegenerateButton = showRegenerateButton;
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
	public TeleporterBlockScreenHandler.TeleporterBlockData getScreenOpeningData(ServerPlayerEntity player) {
		return new TeleporterBlockScreenHandler.TeleporterBlockData(this.pos);
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new TeleporterBlockScreenHandler(syncId, playerInventory, player.isCreativeLevelTwoOp());
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable(this.teleporterName);
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);

				this.accessPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.accessPositionOffset, blockRotation);

				this.directTeleportPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.directTeleportPositionOffset, blockRotation);
				this.directTeleportOrientationYaw = BlockRotationUtils.rotateYaw(this.directTeleportOrientationYaw, blockRotation);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.FRONT_BACK);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.FRONT_BACK);

				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.FRONT_BACK);
				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.LEFT_RIGHT);

				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.LEFT_RIGHT);

				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.LEFT_RIGHT);
				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.LEFT_RIGHT);

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

