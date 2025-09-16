package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import java.util.function.Predicate;

public class TriggeredDamageDealingBlockEntity extends RotatedBlockEntity implements Triggerable {

	private static final BlockPos AREA_POSITION_OFFSET_DEFAULT = new BlockPos(0, 0, 0);

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 0, 0);

	private String exceptionTagIdentifierString;
	private String damageTypeIdentifierString;
	private float damageAmount;

	public TriggeredDamageDealingBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY, pos, state);
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

		if (this.areaDimensions != Vec3i.ZERO) {
			nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
			nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
			nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		}

		BlockPos areaPositionOffset = this.areaPositionOffset;
		if (!areaPositionOffset.equals(AREA_POSITION_OFFSET_DEFAULT)) {
			nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
			nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
			nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		}

		if (!this.exceptionTagIdentifierString.isEmpty()) {
			nbt.putString("exceptionTagIdentifierString", this.exceptionTagIdentifierString);
		} else {
			nbt.remove("exceptionTagIdentifierString");
		}

		if (!this.damageTypeIdentifierString.isEmpty()) {
			nbt.putString("damageTypeIdentifierString", this.damageTypeIdentifierString);
		} else {
			nbt.remove("damageTypeIdentifierString");
		}

		if (this.damageAmount != 0) {
			nbt.putFloat("damageAmount", this.damageAmount);
		} else {
			nbt.remove("damageAmount");
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("showArea", NbtElement.BYTE_TYPE)) {
			this.showArea = nbt.getBoolean("showArea");
		} else {
			this.showArea = false;
		}

		if (nbt.contains("areaMinX") && nbt.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
		} else {
			this.area = null;
		}

		if (nbt.contains("areaDimensionsX", NbtElement.INT_TYPE) || nbt.contains("areaDimensionsY", NbtElement.INT_TYPE) || nbt.contains("areaDimensionsZ", NbtElement.INT_TYPE)) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48)
			);
		} else {
			this.areaDimensions = Vec3i.ZERO;
		}

		if (nbt.contains("areaPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("areaPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("areaPositionOffsetZ", NbtElement.INT_TYPE)) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48)
			);
		} else {
			this.areaPositionOffset = AREA_POSITION_OFFSET_DEFAULT;
		}

		this.exceptionTagIdentifierString = nbt.getString("exceptionTagIdentifierString");

		this.damageTypeIdentifierString = nbt.getString("damageTypeIdentifierString");

		this.damageAmount = nbt.getFloat("damageAmount");

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
	}

	public String getExceptionTagIdentifierString() {
		return this.exceptionTagIdentifierString;
	}

	public void setExceptionTagIdentifierString(String exceptionTagIdentifierString) {
		this.exceptionTagIdentifierString = exceptionTagIdentifierString;
	}

	public String getDamageTypeIdentifierString() {
		return this.damageTypeIdentifierString;
	}

	public void setDamageTypeIdentifierString(String damageTypeIdentifierString) {
		this.damageTypeIdentifierString = damageTypeIdentifierString;
	}

	public float getDamageAmount() {
		return this.damageAmount;
	}

	public void setDamageAmount(float damageAmount) {
		this.damageAmount = damageAmount;
	}

	@Override
	public void trigger() {

		if (this.calculateAreaBox || this.area == null) {
			BlockPos areaPositionOffset = this.areaPositionOffset;
			Vec3i areaDimensions = this.areaDimensions;
			Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
			Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
			this.area = new Box(areaStart, areaEnd);
			this.calculateAreaBox = false;
		}

		if (this.world != null) {
			List<LivingEntity> entityList = this.world.getEntitiesByClass(LivingEntity.class, this.area, entity -> !(entity.getType().isIn(TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(this.exceptionTagIdentifierString)))));

			Registry<DamageType> registry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);

			if (!this.damageTypeIdentifierString.isEmpty()) {
				Identifier damageTypeIdentifier = Identifier.tryParse(this.damageTypeIdentifierString);
				if (damageTypeIdentifier != null) {
					RegistryKey<DamageType> key = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, damageTypeIdentifier);
					DamageSource damageSource = new DamageSource(registry.entryOf(key));

					for (LivingEntity entity : entityList) {
						entity.damage(damageSource, this.damageAmount);
					}
				}
			}
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
}
