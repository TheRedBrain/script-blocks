package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TriggeredDamageDealingBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private SelectionMode selectionMode = SelectionMode.AREA;

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = BlockPos.ORIGIN;

	private BlockPos uuidListHandlerPositionOffset = BlockPos.ORIGIN;

	private String exceptionTagIdentifierString = "";
	private String damageTypeIdentifierString = "";
	private float damageAmount = 0;

	public TriggeredDamageDealingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_DAMAGE_DEALING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putString("selection_mode", this.selectionMode.asString());

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.areaPositionOffset));
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		nbt.put("area", nbtCompound);

		nbt.put("uuid_list_handler_position_offset", NbtHelper.fromBlockPos(this.uuidListHandlerPositionOffset));

		nbt.putString("exception_tag_identifier_string", this.exceptionTagIdentifierString);

		nbt.putString("damage_type_identifier_string", this.damageTypeIdentifierString);

		nbt.putFloat("damage_amount", this.damageAmount);

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.selectionMode = SelectionMode.byName(nbt.getString("selection_mode")).orElse(SelectionMode.AREA);

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

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						NbtHelper.toBlockPos(nbtCompound, "uuid_list_handler_position_offset").orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		if (nbt.contains("exceptionTagIdentifierString")) {
			this.exceptionTagIdentifierString = nbt.getString("exceptionTagIdentifierString");
			nbt.remove("exceptionTagIdentifierString");
		} else {
			this.exceptionTagIdentifierString = nbt.getString("exception_tag_identifier_string");
		}

		if (nbt.contains("damageTypeIdentifierString")) {
			this.damageTypeIdentifierString = nbt.getString("damageTypeIdentifierString");
			nbt.remove("damageTypeIdentifierString");
		} else {
			this.damageTypeIdentifierString = nbt.getString("damage_type_identifier_string");
		}

		if (nbt.contains("damageAmount")) {
			this.damageAmount = nbt.getFloat("damageAmount");
			nbt.remove("damageAmount");
		} else {
			this.damageAmount = nbt.getFloat("damage_amount");
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

	// region --- getter & setter ---
	public SelectionMode getSelectionMode() {
		return this.selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	public boolean showArea() {
		return this.showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return this.areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
	}

	public BlockPos getAreaPositionOffset() {
		return this.areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
	}

	public BlockPos getUuidListHandlerPositionOffset() {
		return this.uuidListHandlerPositionOffset;
	}

	public void setUuidListHandlerPositionOffset(BlockPos uuidListHandlerPositionOffset) {
		this.uuidListHandlerPositionOffset = uuidListHandlerPositionOffset;
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
	// endregion --- getter & setter ---

	@Override
	public void trigger() {

		if (this.selectionMode == SelectionMode.AREA && (this.calculateAreaBox || this.area == null)) {
			BlockPos areaPositionOffset = this.areaPositionOffset;
			Vec3i areaDimensions = this.areaDimensions;
			Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
			Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
			this.area = new Box(areaStart, areaEnd);
			this.calculateAreaBox = false;
			this.markDirty();
			if (this.world != null) {
				BlockState blockState = this.world.getBlockState(this.pos);
				this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
		}

		if (this.world != null) {
			List<LivingEntity> entityList = new ArrayList<>();
			if (this.selectionMode == SelectionMode.AREA) {
				entityList.addAll(this.world.getEntitiesByClass(LivingEntity.class, this.area, entity -> !(entity.getType().isIn(TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(this.exceptionTagIdentifierString))))));
			} else {
				this.getUUIDListFromHandler().forEach((uuid) -> {
					PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
					if (playerEntity != null) {
						entityList.add(playerEntity);
					}
				});
			}
			this.dealDamageToEntities(this.world, entityList);
		}
	}

	private void dealDamageToEntities(World world, List<LivingEntity> list) {

		Registry<DamageType> registry = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);

		if (!this.damageTypeIdentifierString.isEmpty()) {
			Identifier damageTypeIdentifier = Identifier.tryParse(this.damageTypeIdentifierString);
			if (damageTypeIdentifier != null) {
				RegistryKey<DamageType> key = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, damageTypeIdentifier);
				DamageSource damageSource = new DamageSource(registry.entryOf(key));

				for (LivingEntity entity : list) {
					entity.damage(damageSource, this.damageAmount);
				}
			}
		}
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.world != null) {
			List<LivingEntity> entityList = new ArrayList<>();
			list.forEach((uuid) -> {
				PlayerEntity playerEntity = this.world.getPlayerByUuid(uuid);
				if (playerEntity != null) {
					entityList.add(playerEntity);
				}
			});
			this.dealDamageToEntities(this.world, entityList);
		}
	}

	@Override
	public void reset() {
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum SelectionMode implements StringIdentifiable {
		AREA("area"),
		UUID_LIST("uuid_list");

		private final String name;

		SelectionMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<SelectionMode> byName(String name) {
			return Arrays.stream(SelectionMode.values()).filter(selectionMode -> selectionMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_damage_dealing_block.selection_mode." + this.name);
		}
	}

}
