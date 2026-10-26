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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

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

	protected final Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

	protected BlockPos useRelayBlockPositionOffset = POSITION_OFFSET_DEFAULT;

	protected NbtCompound entityTypeCompound = new NbtCompound();

	private boolean triggered = false;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(POSITION_OFFSET_DEFAULT, false);

	public TriggeredSpawnerBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.TRIGGERED_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	public TriggeredSpawnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.entitySpawnPositionOffset);
		nbtCompound.putDouble("yaw", this.entitySpawnOrientationYaw);
		nbtCompound.putDouble("pitch", this.entitySpawnOrientationPitch);
		view.put("entity_spawn", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		view.put("use_relay_block_position_offset", BlockPos.CODEC, this.useRelayBlockPositionOffset);

		NbtList nbtList = new NbtList();
		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("entity_attribute_modifiers", NbtCompound.CODEC);
		for (RegistryEntry<EntityAttribute> registryEntry : this.entityAttributeModifiers.keySet()) {
			nbtCompound = new NbtCompound();
			nbtCompound.putString("attribute", String.valueOf(Registries.ATTRIBUTE.getId(registryEntry.value())));
			NbtList modifiersList = new NbtList();
			WriteView.ListAppender<EntityAttributeModifier> listAppender1 = nbtCompound.put(.getListAppender("entity_attribute_modifiers", EntityAttributeModifier.CODEC);
			for (EntityAttributeModifier entityAttributeModifier : this.entityAttributeModifiers.get(registryEntry)) {
				listAppender1.add(entityAttributeModifier);
			}
			nbtCompound.put("modifiers", listAppender1);
			listAppender.add(nbtCompound.copy());
		}

		view.putString("spawning_mode", this.spawningMode.asString());

		view.putBoolean("is_persistent", this.isPersistent);

		view.putBoolean("enable_spawn_effects", this.enableSpawnEffects);

		view.putBoolean("triggered", this.triggered);

		view.put("entity_type_compound", NbtCompound.CODEC, this.entityTypeCompound);

		view.put("bound_entity_uuid", Uuids.CODEC, this.boundEntityUuid);

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		NbtCompound nbtCompound = view.read("entity_spawn", NbtCompound.CODEC).orElseGet(NbtCompound::new);
			this.entitySpawnPositionOffset = BlockPos.max(
					BlockPos.min(
							nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

			this.entitySpawnOrientationPitch = MathHelper.clamp(nbtCompound.getDouble("pitch", 0), -90.0, 90.0);

			this.entitySpawnOrientationYaw = MathHelper.clamp(nbtCompound.getDouble("yaw", 0), -180.0, 180.0);

			nbtCompound = view.read("triggered_block", NbtCompound.CODEC).orElseGet(NbtCompound::new);
			this.triggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets", false)
			);

			this.useRelayBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							view.read("use_relay_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);

		this.entityAttributeModifiers.clear();
				for (NbtCompound nbtCompound1 : view.getTypedListView("entity_attribute_modifiers", NbtCompound.CODEC)) {
				Optional<RegistryEntry.Reference<EntityAttribute>> optional = Registries.ATTRIBUTE.getEntry(Identifier.tryParse(nbtCompound1.getString("attribute", "")));
				if (optional.isPresent()) {
					RegistryEntry.Reference<EntityAttribute> key = optional.get();
					Optional<NbtList> optionalModifiersList = nbtCompound1.getList("modifiers");
					if (optionalModifiersList.isPresent()) {
						for (NbtElement nbtElement : optionalModifiersList.get()) {
//							nbtElement.
//					for (int j = 0; j < modifiersList.size(); j++) {
							if (entityAttributeModifier != null) {
								this.entityAttributeModifiers.put(key, entityAttributeModifier);
							}
						}
					}
				}
			}

		this.spawningMode = SpawningMode.byName(view.getString("spawning_mode", "")).orElseGet(() -> SpawningMode.ONCE);

		this.isPersistent = view.getBoolean("is_persistent", false);

		this.enableSpawnEffects = view.getBoolean("enable_spawn_effects", true);

		this.triggered = view.getBoolean("triggered", false);

		this.entityTypeCompound = view.read("entity_type_compound", NbtCompound.CODEC).orElseGet(NbtCompound::new);

		this.boundEntityUuid = view.read("bound_entity_uuid", Uuids.CODEC).orElse(null);

		super.readData(view);

	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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

	public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getEntityAttributeModifiers() {
		return this.entityAttributeModifiers;
	}

	public void setEntityAttributeModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers) {
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
		Optional<EntityType<?>> optional = EntityType.get(entityTypeId);
		if (optional.isPresent()) {
			EntityType<?> entityType = optional.get();
			this.entityTypeCompound.putString("id", Registries.ENTITY_TYPE.getId(entityType).toString());
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.entitySpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.entitySpawnPositionOffset, blockRotation);
				this.entitySpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.entitySpawnOrientationYaw, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.useRelayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.useRelayBlockPositionOffset, blockRotation);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.FRONT_BACK);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.LEFT_RIGHT);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.triggered) {
			this.triggered = false;
		}

		if (this.world instanceof ServerWorld serverWorld) {
			if (this.boundEntityUuid != null) {
				Entity entity = serverWorld.getEntity(this.boundEntityUuid);
				if (entity != null) {
					entity.discard();
				}
				this.boundEntityUuid = null;
			}
		}
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
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
			this.markDirty();
			if (this.world != null) {
				BlockState blockState = this.world.getBlockState(this.pos);
				this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
		}
	}

	public void onBoundEntityKilled() {
		if (this.getWorld() != null) {
			this.boundEntityUuid = null;
			BlockEntity blockEntity = this.getWorld().getBlockEntity(BlockRotationUtils.getActualOffsetBlockPosition(this.pos, this.triggeredBlock.getLeft()));
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
			this.markDirty();
			BlockState blockState = this.getWorld().getBlockState(this.pos);
			this.getWorld().updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	protected boolean spawnEntity() {
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			Optional<EntityType<?>> optional = EntityType.fromNbt(this.entityTypeCompound);
			if (optional.isEmpty()) {
				return false;
			}
			BlockPos blockPos = BlockRotationUtils.getActualOffsetBlockPosition(this.pos, this.entitySpawnPositionOffset);
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

			if (!serverWorld.isSpaceEmpty(optional.get().getSpawnBox(d, e, f))) {
				return false;
			}
			blockPos = BlockPos.ofFloored(d, e, f);
			Entity entity2 = EntityType.loadEntityWithPassengers(this.entityTypeCompound, world, entity -> {
				entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
				return entity;
			});
			if (entity2 == null) {
				return false;
			}
			entity2.setBodyYaw((float) yaw);
			entity2.setHeadYaw((float) yaw);
			entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), (float) yaw, (float) pitch);
			if (entity2 instanceof MobEntity mobEntity) {
				if (this.entityTypeCompound.contains("id", NbtElement.STRING_TYPE)) {
					mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null);
				}
				if (!serverWorld.spawnNewEntityAndPassengers(entity2)) {
					return false;
				}
				if (this.enableSpawnEffects) {
					serverWorld.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, this.pos, 0);
				}
				serverWorld.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);

				this.customInit(entity2);

				this.boundEntityUuid = entity2.getUuid();

				if (!this.entityAttributeModifiers.isEmpty()) {
					AttributeContainer attributeContainer = ((LivingEntity) entity2).getAttributes();
					this.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
						EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
						if (entityAttributeInstance != null) {
							entityAttributeInstance.removeModifier(attributeModifier);
							entityAttributeInstance.addPersistentModifier(attributeModifier);
						}
						if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
							((LivingEntity) entity2).setHealth((float) ((LivingEntity) entity2).getAttributes().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
						}
					});
				}
				if (this.isPersistent) {
					mobEntity.setPersistent();
				}
				if (this.enableSpawnEffects) {
					mobEntity.playSpawnEffects();
				}
				if (this.spawningMode == SpawningMode.BOUND || this.spawningMode == SpawningMode.BOUND_RESPAWN) {
					mobEntity.setPersistent();
					((DuckMobEntityMixin) mobEntity).scriptblocks$setControllerBlockPos(this.pos);
				}
				if (!this.useRelayBlockPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
					((DuckMobEntityMixin) mobEntity).scriptblocks$setUseRelayBlockPos(this.pos.add(this.useRelayBlockPositionOffset));
				}
				this.markDirty();
				if (this.world != null) {
					BlockState blockState = this.world.getBlockState(this.pos);
					this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
				}
				return true;
			}
		}
		return false;
	}

	protected void customInit(Entity entity) {
	}

	public enum SpawningMode implements StringIdentifiable {
		BOUND("bound"),
		BOUND_RESPAWN("bound_respawn"),
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		SpawningMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<SpawningMode> byName(String name) {
			return Arrays.stream(SpawningMode.values()).filter(spawningMode -> spawningMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_spawner_block.spawning_mode." + this.name);
		}
	}

	public enum VisualEntityAttributeModifierOperation implements StringIdentifiable {
		ADD_VALUE("add_value", EntityAttributeModifier.Operation.ADD_VALUE),
		ADD_MULTIPLIED_BASE("add_multiplied_base", EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
		ADD_MULTIPLIED_TOTAL("add_multiplied_total", EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

		private final String name;
		private final EntityAttributeModifier.Operation operation;

		VisualEntityAttributeModifierOperation(String name, EntityAttributeModifier.Operation operation) {
			this.name = name;
			this.operation = operation;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<VisualEntityAttributeModifierOperation> byName(String name) {
			return Arrays.stream(VisualEntityAttributeModifierOperation.values()).filter(visualEntityAttributeModifierOperation -> visualEntityAttributeModifierOperation.asString().equals(name)).findFirst();
		}

		public EntityAttributeModifier.Operation getOperation() {
			return this.operation;
		}

		public Text asText() {
			return Text.translatable("gui.entity_attribute_modifier.operation." + this.name);
		}
	}
}
