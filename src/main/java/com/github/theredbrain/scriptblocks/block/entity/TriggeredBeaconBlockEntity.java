package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TriggeredBeaconBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private boolean calculateAreaBox = true;
	private AABB area = null;
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
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.putString("identifier", this.appliedStatusEffectIdentifier);
		nbtCompound.putInt("amplifier", this.appliedStatusEffectAmplifier);
		nbtCompound.putInt("duration", this.appliedStatusEffectDuration);
		nbtCompound.putBoolean("is_ambient", this.appliedStatusEffectAmbient);
		nbtCompound.putBoolean("show_particles", this.appliedStatusEffectShowParticles);
		nbtCompound.putBoolean("show_icon", this.appliedStatusEffectShowIcon);
		view.store("applied_status_effect", CompoundTag.CODEC, nbtCompound.copy());

		view.putBoolean("triggered", true);

		view.putString("triggered_mode", this.triggeredMode.getSerializedName());

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showArea = nbtCompound.getBooleanOr("visible", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("applied_status_effect", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.appliedStatusEffectIdentifier = nbtCompound.getStringOr("identifier", "");

		this.appliedStatusEffectAmplifier = nbtCompound.getIntOr("amplifier", 0);

		this.appliedStatusEffectDuration = nbtCompound.getIntOr("duration", 100);

		this.appliedStatusEffectAmbient = nbtCompound.getBooleanOr("ambient", false);

		this.appliedStatusEffectShowParticles = nbtCompound.getBooleanOr("show_particles", false);

		this.appliedStatusEffectShowIcon = nbtCompound.getBooleanOr("show_icon", false);

		this.triggered = view.getBooleanOr("triggered", false);

		this.triggeredMode = TriggeredMode.byName(view.getStringOr("triggered_mode", "")).orElse(TriggeredMode.ONCE);

		super.loadAdditional(view);

	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
		if (!world.isClientSide() && world.getGameTime() % 80L == 0L) {
			if (triggeredBeaconBlockEntity.calculateAreaBox || triggeredBeaconBlockEntity.area == null) {
				BlockPos areaPositionOffset = triggeredBeaconBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = triggeredBeaconBlockEntity.areaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				triggeredBeaconBlockEntity.area = new AABB(areaStart, areaEnd);
				triggeredBeaconBlockEntity.calculateAreaBox = false;
				triggeredBeaconBlockEntity.setChanged();
				world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
			}
			if (triggeredBeaconBlockEntity.triggeredMode == TriggeredMode.CONTINUOUS && triggeredBeaconBlockEntity.triggered) {
				triggeredBeaconBlockEntity.applyStatusEffect(world);
			}
		}
	}

	private void applyStatusEffect(Level world) {
		Optional<Holder.Reference<MobEffect>> statusEffect = BuiltInRegistries.MOB_EFFECT.get(Identifier.tryParse(this.appliedStatusEffectIdentifier));
		if (statusEffect.isEmpty()) {
			return;
		}

		List<Player> playerList = world.getEntitiesOfClass(Player.class, this.area);
		List<UUID> playerUuidList = new ArrayList<>();
		for (Player player : playerList) {
			playerUuidList.add(player.getUUID());
		}

		Iterator<UUID> playerListIterator = playerUuidList.iterator();
		Player playerEntity;
		UUID uuid;

		while (playerListIterator.hasNext()) {
			uuid = playerListIterator.next();
			playerEntity = world.getPlayerByUUID(uuid);

			if (playerEntity != null) {
				playerEntity.addEffect(
						new MobEffectInstance(
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
		if (BuiltInRegistries.MOB_EFFECT.getValue(Identifier.tryParse(appliedStatusEffectIdentifier)) != null || appliedStatusEffectIdentifier.equals("")) {
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
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}
	}

	@Override
	public void trigger() {
		if (this.triggeredMode == TriggeredMode.CONTINUOUS && !this.triggered) {
			this.triggered = true;
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		} else if (this.triggeredMode == TriggeredMode.ONCE && this.level != null) {
			this.applyStatusEffect(this.level);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum TriggeredMode implements StringRepresentable {
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		TriggeredMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<TriggeredMode> byName(String name) {
			return Arrays.stream(TriggeredMode.values()).filter(triggeredMode -> triggeredMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.triggered_beacon_block.triggered_mode." + this.name);
		}
	}
}
