package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeleporterBlockEntity extends RotatedBlockEntity implements ExtendedScreenHandlerFactory {

	private boolean calculateActivationBox = true;
	private Box activationArea = null;
	private boolean showAdventureScreen = true;
	private boolean showActivationArea = false;
	private Vec3i activationAreaDimensions = Vec3i.ZERO;
	private BlockPos activationAreaPositionOffset = new BlockPos(0, 1, 0);

	private BlockPos accessPositionOffset = new BlockPos(0, 0, 0);
	private boolean setAccessPosition = false;

	private List<String> statusEffectsToDecrementLevelOnTeleport = new ArrayList<>();

	private boolean onlyTeleportDimensionOwner = false;
	private boolean teleportTeam = false;

	private TeleportationMode teleportationMode = TeleportationMode.DIRECT;

	// direct teleportation mode
	private BlockPos directTeleportPositionOffset = new BlockPos(0, 0, 0);
	private double directTeleportOrientationYaw = 0.0;
	private double directTeleportOrientationPitch = 0.0;

	// specific location mode
	private SpawnPointType spawnPointType = SpawnPointType.WORLD_SPAWN;

	// location mode
	private List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList = new ArrayList<>(List.of());

	private String teleporterName = "gui.teleporter_block.teleporter_name_field.label";
	private String currentTargetOwnerLabel = "gui.teleporter_block.target_owner_field.label";
	private String currentTargetIdentifierLabel = "gui.teleporter_block.target_identifier_field.label";
	private boolean showRegenerateButton = true;
	private String teleportButtonLabel = "gui.teleporter_block.teleport_button.label";
	private String cancelTeleportButtonLabel = "gui.teleporter_block.cancel_teleport_button.label";

	public TeleporterBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TELEPORTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putString("teleporterName", this.teleporterName);

		nbt.putBoolean("showAdventureScreen", this.showAdventureScreen);

		nbt.putBoolean("showActivationArea", this.showActivationArea);

		nbt.putInt("activationAreaDimensionsX", this.activationAreaDimensions.getX());
		nbt.putInt("activationAreaDimensionsY", this.activationAreaDimensions.getY());
		nbt.putInt("activationAreaDimensionsZ", this.activationAreaDimensions.getZ());

		nbt.putInt("activationAreaPositionOffsetX", this.activationAreaPositionOffset.getX());
		nbt.putInt("activationAreaPositionOffsetY", this.activationAreaPositionOffset.getY());
		nbt.putInt("activationAreaPositionOffsetZ", this.activationAreaPositionOffset.getZ());

		nbt.putInt("accessPositionOffsetX", this.accessPositionOffset.getX());
		nbt.putInt("accessPositionOffsetY", this.accessPositionOffset.getY());
		nbt.putInt("accessPositionOffsetZ", this.accessPositionOffset.getZ());

		nbt.putBoolean("setAccessPosition", this.setAccessPosition);

		nbt.putInt("statusEffectsToDecrementLevelOnTeleportListSize", this.statusEffectsToDecrementLevelOnTeleport.size());

		for (int i = 0; i < this.statusEffectsToDecrementLevelOnTeleport.size(); i++) {
			nbt.putString("statusEffectsToDecrementLevelOnTeleport_" + i, this.statusEffectsToDecrementLevelOnTeleport.get(i));
		}

		nbt.putBoolean("onlyTeleportDimensionOwner", this.onlyTeleportDimensionOwner);

		nbt.putBoolean("teleportTeam", this.teleportTeam);

		nbt.putString("teleportationMode", this.teleportationMode.asString());

		nbt.putInt("directTeleportPositionOffsetX", this.directTeleportPositionOffset.getX());
		nbt.putInt("directTeleportPositionOffsetY", this.directTeleportPositionOffset.getY());
		nbt.putInt("directTeleportPositionOffsetZ", this.directTeleportPositionOffset.getZ());
		nbt.putDouble("directTeleportPositionOffsetYaw", this.directTeleportOrientationYaw);
		nbt.putDouble("directTeleportPositionOffsetPitch", this.directTeleportOrientationPitch);

		nbt.putString("spawnPointType", this.spawnPointType.asString());

		nbt.putInt("locationsListSize", this.locationsList.size());

		for (int i = 0; i < this.locationsList.size(); i++) {
			nbt.putString("locationsListIdentifier_" + i, this.locationsList.get(i).getLeft().getLeft());
			nbt.putString("locationsListEntrance_" + i, this.locationsList.get(i).getLeft().getRight());
			nbt.putString("locationsListDataId_" + i, this.locationsList.get(i).getRight().getLeft());
			nbt.putInt("locationsListData_" + i, this.locationsList.get(i).getRight().getRight());
		}

		nbt.putString("currentTargetIdentifierLabel", this.currentTargetIdentifierLabel);
		nbt.putString("currentTargetOwnerLabel", this.currentTargetOwnerLabel);
		nbt.putBoolean("showRegenerateButton", this.showRegenerateButton);
		nbt.putString("teleportButtonLabel", this.teleportButtonLabel);
		nbt.putString("cancelTeleportButtonLabel", this.cancelTeleportButtonLabel);

		if (this.activationArea != null) {
			nbt.putDouble("activationAreaMinX", this.activationArea.minX);
			nbt.putDouble("activationAreaMaxX", this.activationArea.maxX);
			nbt.putDouble("activationAreaMinY", this.activationArea.minY);
			nbt.putDouble("activationAreaMaxY", this.activationArea.maxY);
			nbt.putDouble("activationAreaMinZ", this.activationArea.minZ);
			nbt.putDouble("activationAreaMaxZ", this.activationArea.maxZ);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.teleporterName = nbt.getString("teleporterName");

		this.showAdventureScreen = nbt.getBoolean("showAdventureScreen");

		this.showActivationArea = nbt.getBoolean("showActivationArea");

		this.activationAreaDimensions = new Vec3i(
				MathHelper.clamp(nbt.getInt("activationAreaDimensionsX"), 0, 48),
				MathHelper.clamp(nbt.getInt("activationAreaDimensionsY"), 0, 48),
				MathHelper.clamp(nbt.getInt("activationAreaDimensionsZ"), 0, 48)
		);

		this.activationAreaPositionOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("activationAreaPositionOffsetX"), -48, 48),
				MathHelper.clamp(nbt.getInt("activationAreaPositionOffsetY"), -48, 48),
				MathHelper.clamp(nbt.getInt("activationAreaPositionOffsetZ"), -48, 48)
		);

		this.accessPositionOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("accessPositionOffsetX"), -48, 48),
				MathHelper.clamp(nbt.getInt("accessPositionOffsetY"), -48, 48),
				MathHelper.clamp(nbt.getInt("accessPositionOffsetZ"), -48, 48)
		);

		this.setAccessPosition = nbt.getBoolean("setAccessPosition");

		int listSize = nbt.getInt("statusEffectsToDecrementLevelOnTeleportListSize");
		this.statusEffectsToDecrementLevelOnTeleport.clear();
		for (int i = 0; i < listSize; i++) {
			this.statusEffectsToDecrementLevelOnTeleport.add(nbt.getString("statusEffectsToDecrementLevelOnTeleport_" + i));
		}

		this.onlyTeleportDimensionOwner = nbt.getBoolean("onlyTeleportDimensionOwner");

		this.teleportTeam = nbt.getBoolean("teleportTeam");

		this.teleportationMode = TeleportationMode.byName(nbt.getString("teleportationMode")).orElseGet(() -> TeleportationMode.DIRECT);

		this.directTeleportPositionOffset = new BlockPos(
				nbt.getInt("directTeleportPositionOffsetX"),
				nbt.getInt("directTeleportPositionOffsetY"),
				nbt.getInt("directTeleportPositionOffsetZ")
		);
		this.directTeleportOrientationYaw = nbt.getDouble("directTeleportPositionOffsetYaw");
		this.directTeleportOrientationPitch = nbt.getDouble("directTeleportPositionOffsetPitch");

		this.spawnPointType = SpawnPointType.byName(nbt.getString("spawnPointType")).orElseGet(() -> SpawnPointType.WORLD_SPAWN);

		int locationsListSize = nbt.getInt("locationsListSize");
		this.locationsList.clear();
		for (int p = 0; p < locationsListSize; p++) {
			this.locationsList.add(new MutablePair<>(new MutablePair<>(nbt.getString("locationsListIdentifier_" + p), nbt.getString("locationsListEntrance_" + p)), new MutablePair<>(nbt.getString("locationsListDataId_" + p), nbt.getInt("locationsListData_" + p))));
		}

		this.currentTargetIdentifierLabel = nbt.getString("currentTargetIdentifierLabel");
		this.currentTargetOwnerLabel = nbt.getString("currentTargetOwnerLabel");
		this.showRegenerateButton = nbt.getBoolean("showRegenerateButton");
		this.teleportButtonLabel = nbt.getString("teleportButtonLabel");
		this.cancelTeleportButtonLabel = nbt.getString("cancelTeleportButtonLabel");

		if (nbt.contains("activationAreaMinX") && nbt.contains("activationAreaMinY") && nbt.contains("activationAreaMinZ") && nbt.contains("activationAreaMaxX") && nbt.contains("activationAreaMaxY") && nbt.contains("activationAreaMaxZ")) {
			this.activationArea = new Box(nbt.getDouble("activationAreaMinX"), nbt.getDouble("activationAreaMinY"), nbt.getDouble("activationAreaMinZ"), nbt.getDouble("activationAreaMaxX"), nbt.getDouble("activationAreaMaxY"), nbt.getDouble("activationAreaMaxZ"));
			this.calculateActivationBox = true;
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

	public static void tick(World world, BlockPos pos, BlockState state, TeleporterBlockEntity blockEntity) {
		TeleporterBlockEntity.tryOpenScreenRemotely(world, pos, state, blockEntity);
	}

	private static void tryOpenScreenRemotely(World world, BlockPos pos, BlockState state, TeleporterBlockEntity teleporterBlockEntity) {
		if (world.isClient) {
			return;
		}
		if (state.isOf(BlockRegistry.TELEPORTER_BLOCK)) {
			if (teleporterBlockEntity.calculateActivationBox || teleporterBlockEntity.activationArea == null) {
				BlockPos activationAreaPositionOffset = teleporterBlockEntity.getActivationAreaPositionOffset();
				Vec3i activationAreaDimensions = teleporterBlockEntity.getActivationAreaDimensions();
				Vec3d activationAreaStart = new Vec3d(pos.getX() + activationAreaPositionOffset.getX(), pos.getY() + activationAreaPositionOffset.getY(), pos.getZ() + activationAreaPositionOffset.getZ());
				Vec3d activationAreaEnd = new Vec3d(activationAreaStart.getX() + activationAreaDimensions.getX(), activationAreaStart.getY() + activationAreaDimensions.getY(), activationAreaStart.getZ() + activationAreaDimensions.getZ());
				teleporterBlockEntity.activationArea = new Box(activationAreaStart, activationAreaEnd);
				teleporterBlockEntity.calculateActivationBox = false;
			}
			List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, teleporterBlockEntity.activationArea);
			String worldName = world.getRegistryKey().getValue().getPath();
			RegistryEntry<StatusEffect> portal_resistance_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.PORTAL_RESISTANCE_EFFECT);
			for (PlayerEntity playerEntity : list) {
				if (!playerEntity.hasStatusEffect(portal_resistance_status_effect) && !playerEntity.isCreative()) {
					if (!teleporterBlockEntity.onlyTeleportDimensionOwner || playerEntity.getUuid().toString().equals(worldName)) {
						// prevents continuous opening of a screen
						playerEntity.setStatusEffect(
								new StatusEffectInstance(
										portal_resistance_status_effect,
										-1,
										0,
										false,
										false,
										false
								),
								playerEntity
						);
						playerEntity.openHandledScreen(state.createScreenHandlerFactory(world, pos));
					} else {
						playerEntity.sendMessage(Text.translatable("hud.message.onlyDimensionOwnerCanTeleport"), true);
					}
				}
			}
		}
	}

	//region --- getter & setter ---
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

	public boolean getShowActivationArea() {
		return showActivationArea;
	}

	public void setShowActivationArea(boolean showActivationArea) {
		this.showActivationArea = showActivationArea;
	}

	public Vec3i getActivationAreaDimensions() {
		return activationAreaDimensions;
	}

	public void setActivationAreaDimensions(Vec3i activationAreaDimensions) {
		this.activationAreaDimensions = activationAreaDimensions;
		this.calculateActivationBox = true;
	}

	public BlockPos getActivationAreaPositionOffset() {
		return activationAreaPositionOffset;
	}

	public void setActivationAreaPositionOffset(BlockPos activationAreaPositionOffset) {
		this.activationAreaPositionOffset = activationAreaPositionOffset;
		this.calculateActivationBox = true;
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

	public List<String> getStatusEffectsToDecrementLevelOnTeleport() {
		return statusEffectsToDecrementLevelOnTeleport;
	}

	public void setStatusEffectsToDecrementLevelOnTeleport(List<String> statusEffectsToDecrementLevelOnTeleport) {
		this.statusEffectsToDecrementLevelOnTeleport = statusEffectsToDecrementLevelOnTeleport;
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

	public List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> getLocationsList() {
		return this.locationsList;
	}

	// TODO check if input is valid
	public boolean setLocationsList(List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList) {
		this.locationsList = locationsList;
		return true;
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
		return showRegenerateButton;
	}

	public void setShowRegenerateButton(boolean showRegenerateButton) {
		this.showRegenerateButton = showRegenerateButton;
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
	//endregion

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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.accessPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.accessPositionOffset, blockRotation);
				this.directTeleportPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.directTeleportPositionOffset, blockRotation);

				this.directTeleportOrientationYaw = BlockRotationUtils.rotateYaw(this.directTeleportOrientationYaw, blockRotation);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.activationAreaPositionOffset, this.activationAreaDimensions, blockRotation);
				this.activationAreaPositionOffset = offsetArea.getLeft();
				this.activationAreaDimensions = offsetArea.getRight();

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.FRONT_BACK);
				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.FRONT_BACK);

				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.FRONT_BACK);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.activationAreaPositionOffset, this.activationAreaDimensions, BlockMirror.FRONT_BACK);
				this.activationAreaPositionOffset = offsetArea.getLeft();
				this.activationAreaDimensions = offsetArea.getRight();

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.accessPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.accessPositionOffset, BlockMirror.LEFT_RIGHT);
				this.directTeleportPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.directTeleportPositionOffset, BlockMirror.LEFT_RIGHT);

				this.directTeleportOrientationYaw = BlockRotationUtils.mirrorYaw(this.directTeleportOrientationYaw, BlockMirror.LEFT_RIGHT);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.activationAreaPositionOffset, this.activationAreaDimensions, BlockMirror.LEFT_RIGHT);
				this.activationAreaPositionOffset = offsetArea.getLeft();
				this.activationAreaDimensions = offsetArea.getRight();

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	public static enum TeleportationMode implements StringIdentifiable {
		DIRECT("direct"),
		SPAWN_POINTS("spawn_points"),
		LOCATIONS("locations");

		private final String name;

		private TeleportationMode(String name) {
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

	public static enum SpawnPointType implements StringIdentifiable {
		WORLD_SPAWN("world_spawn"),
		PLAYER_SPAWN("player_spawn"),
		LOCATION_ACCESS_POSITION("location_access_position");

		private final String name;

		private SpawnPointType(String name) {
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
}

