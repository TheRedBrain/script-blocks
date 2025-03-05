package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TriggeredBeaconBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

	private String appliedStatusEffectIdentifier = "";
	private int appliedStatusEffectAmplifier = 0;
	private boolean appliedStatusEffectAmbient = false;
	private boolean appliedStatusEffectShowParticles = false;
	private boolean appliedStatusEffectShowIcon = false;

	private boolean triggered = false;

	private TriggeredMode triggeredMode = TriggeredMode.ONCE;

	public TriggeredBeaconBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_BEACON_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.showArea) {
			nbt.putBoolean("showArea", true);
		} else {
			nbt.remove("showArea");
		}

		if (this.area != null) {
			nbt.putDouble("areaMinX", this.area.minX);
			nbt.putDouble("areaMaxX", this.area.maxX);
			nbt.putDouble("areaMinY", this.area.minY);
			nbt.putDouble("areaMaxY", this.area.maxY);
			nbt.putDouble("areaMinZ", this.area.minZ);
			nbt.putDouble("areaMaxZ", this.area.maxZ);
		} else {
			nbt.remove("areaMinX");
			nbt.remove("areaMaxX");
			nbt.remove("areaMinY");
			nbt.remove("areaMaxY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxZ");
		}

		if (this.areaDimensions.getX() != 0) {
			nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
		} else {
			nbt.remove("areaDimensionsX");
		}

		if (this.areaDimensions.getY() != 0) {
			nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
		} else {
			nbt.remove("areaDimensionsY");
		}

		if (this.areaDimensions.getZ() != 0) {
			nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			nbt.remove("areaDimensionsZ");
		}

		if (this.areaPositionOffset.getX() != 0) {
			nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
		} else {
			nbt.remove("areaPositionOffsetX");
		}

		if (this.areaPositionOffset.getY() != 0) {
			nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
		} else {
			nbt.remove("areaPositionOffsetY");
		}

		if (this.areaPositionOffset.getZ() != 0) {
			nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			nbt.remove("areaPositionOffsetZ");
		}


		if (!this.appliedStatusEffectIdentifier.isEmpty()) {
			nbt.putString("appliedStatusEffectIdentifier", this.appliedStatusEffectIdentifier);
		} else {
			nbt.remove("appliedStatusEffectIdentifier");
		}

		if (this.appliedStatusEffectAmplifier != 0) {
			nbt.putInt("appliedStatusEffectAmplifier", this.appliedStatusEffectAmplifier);
		} else {
			nbt.remove("appliedStatusEffectAmplifier");
		}

		if (this.appliedStatusEffectAmbient) {
			nbt.putBoolean("appliedStatusEffectAmbient", true);
		} else {
			nbt.remove("appliedStatusEffectAmbient");
		}

		if (this.appliedStatusEffectShowParticles) {
			nbt.putBoolean("appliedStatusEffectShowParticles", true);
		} else {
			nbt.remove("appliedStatusEffectShowParticles");
		}

		if (this.appliedStatusEffectShowIcon) {
			nbt.putBoolean("appliedStatusEffectShowIcon", true);
		} else {
			nbt.remove("appliedStatusEffectShowIcon");
		}

		if (this.triggered) {
			nbt.putBoolean("triggered", true);
		} else {
			nbt.remove("triggered");
		}

		if (this.triggeredMode != TriggeredMode.ONCE) {
			nbt.putString("triggeredMode", this.triggeredMode.asString());
		} else {
			nbt.remove("triggeredMode");
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.showArea = nbt.getBoolean("showArea");

		if (nbt.contains("areaMinX") && nbt.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
		}

		int i = MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48);
		int j = MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48);
		int k = MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48);
		this.areaDimensions = new Vec3i(i, j, k);

		int l = MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48);
		int m = MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48);
		int n = MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48);
		this.areaPositionOffset = new BlockPos(l, m, n);


		this.appliedStatusEffectIdentifier = nbt.getString("appliedStatusEffectIdentifier");

		this.appliedStatusEffectAmplifier = nbt.getInt("appliedStatusEffectAmplifier");

		this.appliedStatusEffectAmbient = nbt.getBoolean("appliedStatusEffectAmbient");

		this.appliedStatusEffectShowParticles = nbt.getBoolean("appliedStatusEffectShowParticles");

		this.appliedStatusEffectShowIcon = nbt.getBoolean("appliedStatusEffectShowIcon");

		this.triggered = nbt.getBoolean("triggered");

		this.triggeredMode = TriggeredMode.byName(nbt.getString("triggeredMode")).orElse(TriggeredMode.ONCE);

		super.readNbt(nbt, registryLookup);

	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
		if (!world.isClient && world.getTime() % 80L == 0L) {
			if (triggeredBeaconBlockEntity.calculateAreaBox || triggeredBeaconBlockEntity.area == null) {
				BlockPos areaPositionOffset = triggeredBeaconBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = triggeredBeaconBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				triggeredBeaconBlockEntity.area = new Box(areaStart, areaEnd);
				triggeredBeaconBlockEntity.calculateAreaBox = false;
			}
			if (triggeredBeaconBlockEntity.triggeredMode == TriggeredMode.CONTINUOUS && triggeredBeaconBlockEntity.triggered) {
				triggeredBeaconBlockEntity.applyStatusEffect(world);
			}
		}
	}

	private void applyStatusEffect(World world) {
		Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(this.appliedStatusEffectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}

		List<PlayerEntity> playerList = world.getNonSpectatingEntities(PlayerEntity.class, this.area);
		List<UUID> playerUuidList = new ArrayList<>();
		for (PlayerEntity player : playerList) {
			playerUuidList.add(player.getUuid());
		}

		Iterator<UUID> playerListIterator = playerUuidList.iterator();
		PlayerEntity playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = world.getPlayerByUuid(uuid);

			if (playerEntity != null) {
				playerEntity.addStatusEffect(
						new StatusEffectInstance(
								statusEffect.get(),
								100,
								this.appliedStatusEffectAmplifier,
								this.appliedStatusEffectAmbient,
								this.appliedStatusEffectShowParticles,
								this.appliedStatusEffectShowIcon
						)
				);
			}
		}
	}

	//region --- getter & setter ---
	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	// TODO check if input is valid
	public boolean setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
		return true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	// TODO check if input is valid
	public boolean setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
		return true;
	}

	public String getAppliedStatusEffectIdentifier() {
		return this.appliedStatusEffectIdentifier;
	}

	public boolean setAppliedStatusEffectIdentifier(String appliedStatusEffectIdentifier) {
		if (Registries.STATUS_EFFECT.get(Identifier.tryParse(appliedStatusEffectIdentifier)) != null || appliedStatusEffectIdentifier.equals("")) {
			this.appliedStatusEffectIdentifier = appliedStatusEffectIdentifier;
			return true;
		}
		return false;
	}

	public int getAppliedStatusEffectAmplifier() {
		return appliedStatusEffectAmplifier;
	}

	public boolean setAppliedStatusEffectAmplifier(int appliedStatusEffectAmplifier) {
		if (appliedStatusEffectAmplifier >= 0 && appliedStatusEffectAmplifier < 127) {
			this.appliedStatusEffectAmplifier = appliedStatusEffectAmplifier;
			return true;
		}
		return false;
	}

	public boolean getAppliedStatusEffectAmbient() {
		return appliedStatusEffectAmbient;
	}

	public void setAppliedStatusEffectAmbient(boolean appliedStatusEffectAmbient) {
		this.appliedStatusEffectAmbient = appliedStatusEffectAmbient;
	}

	public boolean getAppliedStatusEffectShowParticles() {
		return appliedStatusEffectShowParticles;
	}

	public void setAppliedStatusEffectShowParticles(boolean appliedStatusEffectShowParticles) {
		this.appliedStatusEffectShowParticles = appliedStatusEffectShowParticles;
	}

	public boolean getAppliedStatusEffectShowIcon() {
		return appliedStatusEffectShowIcon;
	}

	public void setAppliedStatusEffectShowIcon(boolean appliedStatusEffectShowIcon) {
		this.appliedStatusEffectShowIcon = appliedStatusEffectShowIcon;
	}

	public boolean getTriggered() {
		return this.triggered;
	}

	public void setTriggered(boolean triggered) {
		this.triggered = triggered;
	}

	public TriggeredMode getTriggeredMode() {
		return this.triggeredMode;
	}

	public void setTriggeredMode(TriggeredMode triggeredMode) {
		this.triggeredMode = triggeredMode;
	}
	//endregion --- getter & setter ---

	@Override
	public void reset() {
		if (this.triggered) {
			this.triggered = false;
		}
	}

	@Override
	public void trigger() {
		if (this.triggeredMode == TriggeredMode.CONTINUOUS && !this.triggered) {
			this.triggered = true;
		} else if (this.triggeredMode == TriggeredMode.ONCE && this.world != null) {
			this.applyStatusEffect(this.world);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	public static enum TriggeredMode implements StringIdentifiable {
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		private TriggeredMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<TriggeredMode> byName(String name) {
			return Arrays.stream(TriggeredMode.values()).filter(triggeredMode -> triggeredMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_beacon_block.triggered_mode." + this.name);
		}
	}
}
