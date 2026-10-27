package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TriggeredDamageDealingBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private SelectionMode selectionMode = SelectionMode.AREA;

	private boolean calculateAreaBox = true;
	private AABB area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = BlockPos.ZERO;

	private BlockPos uuidListHandlerPositionOffset = BlockPos.ZERO;

	private String exceptionTagIdentifierString = "";
	private String damageTypeIdentifierString = "";
	private float damageAmount = 0;

	public TriggeredDamageDealingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_DAMAGE_DEALING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("selection_mode", this.selectionMode.getSerializedName());

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		view.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.uuidListHandlerPositionOffset);

		view.putString("exception_tag_identifier_string", this.exceptionTagIdentifierString);

		view.putString("damage_type_identifier_string", this.damageTypeIdentifierString);

		view.putFloat("damage_amount", this.damageAmount);

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.selectionMode = SelectionMode.byName(view.getStringOr("selection_mode", "")).orElse(SelectionMode.AREA);

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

		this.uuidListHandlerPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.exceptionTagIdentifierString = view.getStringOr("exception_tag_identifier_string", "");

		this.damageTypeIdentifierString = view.getStringOr("damage_type_identifier_string", "");

		this.damageAmount = view.getFloatOr("damage_amount", 0);

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
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
			Vec3 areaStart = new Vec3(worldPosition.getX() + areaPositionOffset.getX(), worldPosition.getY() + areaPositionOffset.getY(), worldPosition.getZ() + areaPositionOffset.getZ());
			Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
			this.area = new AABB(areaStart, areaEnd);
			this.calculateAreaBox = false;
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}

		if (this.level != null) {
			List<LivingEntity> entityList = new ArrayList<>();
			if (this.selectionMode == SelectionMode.AREA) {
				entityList.addAll(this.level.getEntitiesOfClass(LivingEntity.class, this.area, entity -> !(entity.getType().is(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(this.exceptionTagIdentifierString))))));
			} else {
				this.getUUIDListFromHandler().forEach((uuid) -> {
					Player playerEntity = this.level.getPlayerByUUID(uuid);
					if (playerEntity != null) {
						entityList.add(playerEntity);
					}
				});
			}
			if (this.level instanceof ServerLevel serverWorld) {
				this.dealDamageToEntities(serverWorld, entityList);
			}
		}
	}

	private void dealDamageToEntities(ServerLevel serverWorld, List<LivingEntity> list) {

		Optional<Registry<DamageType>> optionalRegistry = serverWorld.registryAccess().lookup(Registries.DAMAGE_TYPE);

		if (!this.damageTypeIdentifierString.isEmpty() && optionalRegistry.isPresent()) {
			Identifier damageTypeIdentifier = Identifier.tryParse(this.damageTypeIdentifierString);
			if (damageTypeIdentifier != null) {
				ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, damageTypeIdentifier);

				DamageSource damageSource = new DamageSource(optionalRegistry.get().wrapAsHolder(optionalRegistry.get().getValue(key)));

				for (LivingEntity entity : list) {
					entity.hurtServer(serverWorld, damageSource, this.damageAmount);
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
		if (this.level != null) {
			List<LivingEntity> entityList = new ArrayList<>();
			list.forEach((uuid) -> {
				Player playerEntity = this.level.getPlayerByUUID(uuid);
				if (playerEntity != null) {
					entityList.add(playerEntity);
				}
			});
			if (this.level instanceof ServerLevel serverWorld) {
				this.dealDamageToEntities(serverWorld, entityList);
			}
		}
	}

	@Override
	public void reset() {
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum SelectionMode implements StringRepresentable {
		AREA("area"),
		UUID_LIST("uuid_list");

		private final String name;

		SelectionMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<SelectionMode> byName(String name) {
			return Arrays.stream(SelectionMode.values()).filter(selectionMode -> selectionMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.triggered_damage_dealing_block.selection_mode." + this.name);
		}
	}

}
