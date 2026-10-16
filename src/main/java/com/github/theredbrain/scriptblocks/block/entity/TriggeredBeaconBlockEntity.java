package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
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
	private int appliedStatusEffectDuration = 100;
	private boolean appliedStatusEffectAmbient = false;
	private boolean appliedStatusEffectShowParticles = false;
	private boolean appliedStatusEffectShowIcon = false;

	private boolean triggered = false;

	private TriggeredMode triggeredMode = TriggeredMode.ONCE;

	public TriggeredBeaconBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_BEACON_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.areaPositionOffset));
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		nbt.put("area", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.putString("identifier", this.appliedStatusEffectIdentifier);
		nbtCompound.putInt("amplifier", this.appliedStatusEffectAmplifier);
		nbtCompound.putInt("duration", this.appliedStatusEffectDuration);
		nbtCompound.putBoolean("is_ambient", this.appliedStatusEffectAmbient);
		nbtCompound.putBoolean("show_particles", this.appliedStatusEffectShowParticles);
		nbtCompound.putBoolean("show_icon", this.appliedStatusEffectShowIcon);
		nbt.put("applied_status_effect", nbtCompound);

		nbt.putBoolean("triggered", true);

		nbt.putString("triggered_mode", this.triggeredMode.asString());

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = nbt.getCompound("area");
		if (nbt.contains("showArea")) {
			this.showArea = nbt.getBoolean("showArea");
			nbt.remove("showArea");
		} else {
			this.showArea = nbtCompound.getBoolean("visible");
		}

		if (nbt.contains("areaMinX") || nbt.contains("areaMinY") || nbt.contains("areaMinZ") || nbt.contains("areaMaxX") || nbt.contains("areaMaxY") || nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
			nbt.remove("areaMinX");
			nbt.remove("areaMinY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxX");
			nbt.remove("areaMaxY");
			nbt.remove("areaMaxZ");
		}

		if (nbt.contains("areaDimensionsX") || nbt.contains("areaDimensionsY") || nbt.contains("areaDimensionsZ")) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48)
			);
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		} else {
			this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));
		}

		if (nbt.contains("areaPositionOffsetX") || nbt.contains("areaPositionOffsetY") || nbt.contains("areaPositionOffsetZ")) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48)
			);
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		} else {
			this.areaPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}


		nbtCompound = nbt.getCompound("applied_status_effect");
		if (nbt.contains("appliedStatusEffectIdentifier")) {
			this.appliedStatusEffectIdentifier = nbt.getString("appliedStatusEffectIdentifier");
			nbt.remove("appliedStatusEffectIdentifier");
		} else {
			this.appliedStatusEffectIdentifier = nbtCompound.getString("identifier");
		}

		if (nbt.contains("appliedStatusEffectIdentifier")) {
			this.appliedStatusEffectAmplifier = nbt.getInt("appliedStatusEffectAmplifier");
			nbt.remove("appliedStatusEffectAmplifier");
		} else {
			this.appliedStatusEffectAmplifier = nbtCompound.getInt("amplifier");
		}

		if (nbt.contains("appliedStatusEffectDuration")) {
			this.appliedStatusEffectDuration = nbt.getInt("appliedStatusEffectDuration");
			nbt.remove("appliedStatusEffectDuration");
		} else {
			this.appliedStatusEffectDuration = nbtCompound.getInt("duration");
		}

		if (nbt.contains("appliedStatusEffectAmbient")) {
			this.appliedStatusEffectAmbient = nbt.getBoolean("appliedStatusEffectAmbient");
			nbt.remove("appliedStatusEffectAmbient");
		} else {
			this.appliedStatusEffectAmbient = nbtCompound.getBoolean("ambient");
		}

		if (nbt.contains("appliedStatusEffectShowParticles")) {
			this.appliedStatusEffectShowParticles = nbt.getBoolean("appliedStatusEffectShowParticles");
			nbt.remove("appliedStatusEffectShowParticles");
		} else {
			this.appliedStatusEffectShowParticles = nbtCompound.getBoolean("show_particles");
		}

		if (nbt.contains("appliedStatusEffectShowIcon")) {
			this.appliedStatusEffectShowIcon = nbt.getBoolean("appliedStatusEffectShowIcon");
			nbt.remove("appliedStatusEffectShowIcon");
		} else {
			this.appliedStatusEffectShowIcon = nbtCompound.getBoolean("show_icon");
		}

		this.triggered = nbt.getBoolean("triggered");

		if (nbt.contains("triggeredMode")) {
			this.triggeredMode = TriggeredMode.byName(nbt.getString("triggeredMode")).orElse(TriggeredMode.ONCE);
			nbt.remove("triggeredMode");
		} else {
			this.triggeredMode = TriggeredMode.byName(nbt.getString("triggered_mode")).orElse(TriggeredMode.ONCE);
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

	public static void tick(World world, BlockPos pos, BlockState state, TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
		if (!world.isClient && world.getTime() % 80L == 0L) {
			if (triggeredBeaconBlockEntity.calculateAreaBox || triggeredBeaconBlockEntity.area == null) {
				BlockPos areaPositionOffset = triggeredBeaconBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = triggeredBeaconBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				triggeredBeaconBlockEntity.area = new Box(areaStart, areaEnd);
				triggeredBeaconBlockEntity.calculateAreaBox = false;
				triggeredBeaconBlockEntity.markDirty();
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
								this.appliedStatusEffectDuration,
								this.appliedStatusEffectAmplifier,
								this.appliedStatusEffectAmbient,
								this.appliedStatusEffectShowParticles,
								this.appliedStatusEffectShowIcon
						)
				);
			}
		}
	}

	// region --- getter & setter ---
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

	public int getAppliedStatusEffectDuration() {
		return appliedStatusEffectDuration;
	}

	public void setAppliedStatusEffectDuration(int appliedStatusEffectDuration) {
		if (appliedStatusEffectDuration < -1) {
			appliedStatusEffectDuration = 100;
		}
		this.appliedStatusEffectDuration = appliedStatusEffectDuration;
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
	// endregion --- getter & setter ---

	@Override
	public void reset() {
		if (this.triggered) {
			this.triggered = false;
			this.markDirty();
			if (this.world != null) {
				BlockState blockState = this.world.getBlockState(this.pos);
				this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
		}
	}

	@Override
	public void trigger() {
		if (this.triggeredMode == TriggeredMode.CONTINUOUS && !this.triggered) {
			this.triggered = true;
			this.markDirty();
			if (this.world != null) {
				BlockState blockState = this.world.getBlockState(this.pos);
				this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
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

	public enum TriggeredMode implements StringIdentifiable {
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		TriggeredMode(String name) {
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
