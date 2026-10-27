package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TriggeredSpawnerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	protected static final BlockPos POSITION_OFFSET_DEFAULT = new BlockPos(0, 0, 0);

	protected BlockPos entitySpawnPositionOffset = POSITION_OFFSET_DEFAULT;
	protected double entitySpawnOrientationPitch = 0.0;
	protected double entitySpawnOrientationYaw = 0.0;
	@Nullable
	protected UUID boundEntityUuid = null;

	protected SpawningMode spawningMode = SpawningMode.ONCE;
	protected boolean isPersistent = false;
	protected boolean enableSpawnEffects = true;

	protected final Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

	protected BlockPos useRelayBlockPositionOffset = POSITION_OFFSET_DEFAULT;

	protected CompoundTag entityTypeCompound = new CompoundTag();

	private boolean triggered = false;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(POSITION_OFFSET_DEFAULT, false);

	public TriggeredSpawnerBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.TRIGGERED_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	public TriggeredSpawnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.entitySpawnPositionOffset);
		nbtCompound.putDouble("yaw", this.entitySpawnOrientationYaw);
		nbtCompound.putDouble("pitch", this.entitySpawnOrientationPitch);
		view.store("entity_spawn", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		view.store("use_relay_block_position_offset", BlockPos.CODEC, this.useRelayBlockPositionOffset);

//		NbtList nbtList = new NbtList();
//		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("entity_attribute_modifiers", NbtCompound.CODEC);
//		for (RegistryEntry<EntityAttribute> registryEntry : this.entityAttributeModifiers.keySet()) {
//			nbtCompound = new NbtCompound();
//			nbtCompound.putString("attribute", String.valueOf(Registries.ATTRIBUTE.getId(registryEntry.value())));
//			NbtList modifiersList = new NbtList();
//			WriteView.ListAppender<EntityAttributeModifier> listAppender1 = nbtCompound.put(.getListAppender("entity_attribute_modifiers", EntityAttributeModifier.CODEC);
//			for (EntityAttributeModifier entityAttributeModifier : this.entityAttributeModifiers.get(registryEntry)) {
//				listAppender1.add(entityAttributeModifier);
//			}
//			nbtCompound.put("modifiers", listAppender1);
//			listAppender.add(nbtCompound.copy());
//		}

		view.putString("spawning_mode", this.spawningMode.getSerializedName());

		view.putBoolean("is_persistent", this.isPersistent);

		view.putBoolean("enable_spawn_effects", this.enableSpawnEffects);

		view.putBoolean("triggered", this.triggered);

		view.store("entity_type_compound", CompoundTag.CODEC, this.entityTypeCompound);

		view.store("bound_entity_uuid", UUIDUtil.AUTHLIB_CODEC, this.boundEntityUuid);

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("entity_spawn", CompoundTag.CODEC).orElseGet(CompoundTag::new);
			this.entitySpawnPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

			this.entitySpawnOrientationPitch = Mth.clamp(nbtCompound.getDoubleOr("pitch", 0), -90.0, 90.0);

			this.entitySpawnOrientationYaw = Mth.clamp(nbtCompound.getDoubleOr("yaw", 0), -180.0, 180.0);

			nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
			this.triggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBooleanOr("resets", false)
			);

			this.useRelayBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							view.read("use_relay_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

		this.entityAttributeModifiers.clear();
//				for (NbtCompound nbtCompound1 : view.getTypedListView("entity_attribute_modifiers", NbtCompound.CODEC)) {
//				Optional<RegistryEntry.Reference<EntityAttribute>> optional = Registries.ATTRIBUTE.getEntry(Identifier.tryParse(nbtCompound1.getString("attribute", "")));
//				if (optional.isPresent()) {
//					RegistryEntry.Reference<EntityAttribute> key = optional.get();
//					Optional<NbtList> optionalModifiersList = nbtCompound1.getList("modifiers");
//					if (optionalModifiersList.isPresent()) {
//						for (NbtElement nbtElement : optionalModifiersList.get()) {
////							nbtElement.
////					for (int j = 0; j < modifiersList.size(); j++) {
//							if (entityAttributeModifier != null) {
//								this.entityAttributeModifiers.put(key, entityAttributeModifier);
//							}
//						}
//					}
//				}
//			}

		this.spawningMode = SpawningMode.byName(view.getStringOr("spawning_mode", "")).orElseGet(() -> SpawningMode.ONCE);

		this.isPersistent = view.getBooleanOr("is_persistent", false);

		this.enableSpawnEffects = view.getBooleanOr("enable_spawn_effects", true);

		this.triggered = view.getBooleanOr("triggered", false);

		this.entityTypeCompound = view.read("entity_type_compound", CompoundTag.CODEC).orElseGet(CompoundTag::new);

		this.boundEntityUuid = view.read("bound_entity_uuid", UUIDUtil.AUTHLIB_CODEC).orElse(null);

		super.loadAdditional(view);

	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---
	public BlockPos getEntitySpawnPositionOffset() {
		return this.entitySpawnPositionOffset;
	}

	public void setEntitySpawnPositionOffset(BlockPos entitySpawnPositionOffset) {
		this.entitySpawnPositionOffset = new BlockPos(entitySpawnPositionOffset);
	}

	public double getEntitySpawnOrientationPitch() {
		return entitySpawnOrientationPitch;
	}

	// TODO check if input is valid
	public boolean setEntitySpawnPositionPitch(double entitySpawnPositionPitch) {
		this.entitySpawnOrientationPitch = entitySpawnPositionPitch;
		return true;
	}

	public double getEntitySpawnOrientationYaw() {
		return entitySpawnOrientationYaw;
	}

	// TODO check if input is valid
	public boolean setEntitySpawnPositionYaw(double entitySpawnPositionYaw) {
		this.entitySpawnOrientationYaw = entitySpawnPositionYaw;
		return true;
	}

	public Multimap<Holder<Attribute>, AttributeModifier> getEntityAttributeModifiers() {
		return this.entityAttributeModifiers;
	}

	public void setEntityAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers) {
		this.entityAttributeModifiers.clear();
		this.entityAttributeModifiers.putAll(entityAttributeModifiers);
	}

	public SpawningMode getSpawningMode() {
		return this.spawningMode;
	}

	public void setSpawningMode(SpawningMode spawningMode) {
		this.spawningMode = spawningMode;
	}

	public boolean isPersistent() {
		return this.isPersistent;
	}

	public void setIsPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
	}

	public boolean enableSpawnEffects() {
		return this.enableSpawnEffects;
	}

	public void setEnableSpawnEffects(boolean enableSpawnEffects) {
		this.enableSpawnEffects = enableSpawnEffects;
	}

	public String getEntityTypeId() {
		if (this.entityTypeCompound != null && this.entityTypeCompound.contains("id")) {
			return this.entityTypeCompound.getString("id");
		}
		return "";
	}

	public boolean setEntityType(String entityTypeId) {
		Optional<EntityType<?>> optional = EntityType.byString(entityTypeId);
		if (optional.isPresent()) {
			EntityType<?> entityType = optional.get();
			this.entityTypeCompound.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
			return true;
		}
		return entityTypeId.isEmpty();
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public BlockPos getUseRelayBlockPositionOffset() {
		return this.useRelayBlockPositionOffset;
	}

	public void setUseRelayBlockPositionOffset(BlockPos useRelayedBlockPositionOffset) {
		this.useRelayBlockPositionOffset = useRelayedBlockPositionOffset;
	}
	// endregion --- getter & setter ---

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.entitySpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.entitySpawnPositionOffset, blockRotation);
				this.entitySpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.entitySpawnOrientationYaw, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.useRelayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.useRelayBlockPositionOffset, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, Mirror.FRONT_BACK);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, Mirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, Mirror.LEFT_RIGHT);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, Mirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.triggered) {
			this.triggered = false;
		}

		if (this.level instanceof ServerLevel serverWorld) {
			if (this.boundEntityUuid != null) {
				Entity entity = serverWorld.getEntity(this.boundEntityUuid);
				if (entity != null) {
					entity.discard();
				}
				this.boundEntityUuid = null;
			}
		}
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void trigger() {
		if (
				((this.spawningMode == SpawningMode.BOUND || this.spawningMode == SpawningMode.ONCE) && !this.triggered)
						|| this.spawningMode == SpawningMode.CONTINUOUS
						|| (this.spawningMode == SpawningMode.BOUND_RESPAWN && this.boundEntityUuid == null)
		) {
			if (this.spawnEntity()) {
				this.triggered = true;
			}
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}
	}

	public void onBoundEntityKilled() {
		if (this.getLevel() != null) {
			this.boundEntityUuid = null;
			BlockEntity blockEntity = this.getLevel().getBlockEntity(BlockRotationUtils.getActualOffsetBlockPosition(this.worldPosition, this.triggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
			if (this.spawningMode == SpawningMode.BOUND_RESPAWN) {
				this.trigger();
			}
			this.setChanged();
			BlockState blockState = this.getLevel().getBlockState(this.worldPosition);
			this.getLevel().sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	protected boolean spawnEntity() {
		if (this.getLevel() instanceof ServerLevel serverWorld) {
			Optional<EntityType<?>> optional = EntityType.fromNbt(this.entityTypeCompound);
			if (optional.isEmpty()) {
				return false;
			}
			BlockPos blockPos = BlockRotationUtils.getActualOffsetBlockPosition(this.worldPosition, this.entitySpawnPositionOffset);
			double pitch = this.entitySpawnOrientationPitch;
			double yaw = this.entitySpawnOrientationYaw;

			if (serverWorld.getBlockEntity(blockPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
				MutablePair<BlockPos, MutablePair<Double, Double>> targetSpawnPoint = spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);

				blockPos = targetSpawnPoint.getLeft();
				yaw = targetSpawnPoint.getRight().getLeft();
				pitch = targetSpawnPoint.getRight().getRight();
			}
			double d = blockPos.getX() + 0.5;
			double e = blockPos.getY();
			double f = blockPos.getZ() + 0.5;

			if (!serverWorld.noCollision(optional.get().getSpawnAABB(d, e, f))) {
				return false;
			}
			blockPos = BlockPos.containing(d, e, f);
			Entity entity2 = EntityType.loadEntityRecursive(this.entityTypeCompound, level, entity -> {
				entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
				return entity;
			});
			if (entity2 == null) {
				return false;
			}
			entity2.setYBodyRot((float) yaw);
			entity2.setYHeadRot((float) yaw);
			entity2.snapTo(entity2.getX(), entity2.getY(), entity2.getZ(), (float) yaw, (float) pitch);
			if (entity2 instanceof Mob mobEntity) {
				if (this.entityTypeCompound.contains("id", Tag.TAG_STRING)) {
					mobEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(entity2.blockPosition()), EntitySpawnReason.SPAWNER, null);
				}
				if (!serverWorld.tryAddFreshEntityWithPassengers(entity2)) {
					return false;
				}
				if (this.enableSpawnEffects) {
					serverWorld.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, this.worldPosition, 0);
				}
				serverWorld.gameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);

				this.customInit(entity2);

				this.boundEntityUuid = entity2.getUUID();

				if (!this.entityAttributeModifiers.isEmpty()) {
					AttributeMap attributeContainer = ((LivingEntity) entity2).getAttributes();
					this.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
						AttributeInstance entityAttributeInstance = attributeContainer.getInstance(attribute);
						if (entityAttributeInstance != null) {
							entityAttributeInstance.removeModifier(attributeModifier);
							entityAttributeInstance.addPermanentModifier(attributeModifier);
						}
						if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
							((LivingEntity) entity2).setHealth((float) ((LivingEntity) entity2).getAttributes().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
						}
					});
				}
				if (this.isPersistent) {
					mobEntity.setPersistenceRequired();
				}
				if (this.enableSpawnEffects) {
					mobEntity.spawnAnim();
				}
				if (this.spawningMode == SpawningMode.BOUND || this.spawningMode == SpawningMode.BOUND_RESPAWN) {
					mobEntity.setPersistenceRequired();
					((DuckMobEntityMixin) mobEntity).scriptblocks$setControllerBlockPos(this.worldPosition);
				}
				if (!this.useRelayBlockPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
					((DuckMobEntityMixin) mobEntity).scriptblocks$setUseRelayBlockPos(this.worldPosition.offset(this.useRelayBlockPositionOffset));
				}
				this.setChanged();
				if (this.level != null) {
					BlockState blockState = this.level.getBlockState(this.worldPosition);
					this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
				}
				return true;
			}
		}
		return false;
	}

	protected void customInit(Entity entity) {
	}

	public enum SpawningMode implements StringRepresentable {
		BOUND("bound"),
		BOUND_RESPAWN("bound_respawn"),
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		SpawningMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<SpawningMode> byName(String name) {
			return Arrays.stream(SpawningMode.values()).filter(spawningMode -> spawningMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.triggered_spawner_block.spawning_mode." + this.name);
		}
	}

	public enum VisualEntityAttributeModifierOperation implements StringRepresentable {
		ADD_VALUE("add_value", AttributeModifier.Operation.ADD_VALUE),
		ADD_MULTIPLIED_BASE("add_multiplied_base", AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
		ADD_MULTIPLIED_TOTAL("add_multiplied_total", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

		private final String name;
		private final AttributeModifier.Operation operation;

		VisualEntityAttributeModifierOperation(String name, AttributeModifier.Operation operation) {
			this.name = name;
			this.operation = operation;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<VisualEntityAttributeModifierOperation> byName(String name) {
			return Arrays.stream(VisualEntityAttributeModifierOperation.values()).filter(visualEntityAttributeModifierOperation -> visualEntityAttributeModifierOperation.getSerializedName().equals(name)).findFirst();
		}

		public AttributeModifier.Operation getOperation() {
			return this.operation;
		}

		public Component asText() {
			return Component.translatable("gui.entity_attribute_modifier.operation." + this.name);
		}
	}
}
