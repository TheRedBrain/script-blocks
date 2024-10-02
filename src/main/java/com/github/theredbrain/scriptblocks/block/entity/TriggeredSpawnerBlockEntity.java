package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class TriggeredSpawnerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private static final BlockPos POSITION_OFFSET_DEFAULT = new BlockPos(0, 0, 0);

	private BlockPos entitySpawnPositionOffset = POSITION_OFFSET_DEFAULT;
	private double entitySpawnOrientationPitch = 0.0;
	private double entitySpawnOrientationYaw = 0.0;
	@Nullable
	private UUID boundEntityUuid = null;

	private SpawningMode spawningMode = SpawningMode.ONCE;

	private Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(POSITION_OFFSET_DEFAULT, false);
	private BlockPos useRelayBlockPositionOffset = POSITION_OFFSET_DEFAULT;

	private boolean triggered = false;
	private NbtCompound entityTypeCompound = new NbtCompound();

	public TriggeredSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		super.writeNbt(nbt, registryLookup);

		BlockPos entitySpawnPositionOffset = this.entitySpawnPositionOffset;
		if (!entitySpawnPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
			nbt.putInt("entitySpawnPositionOffsetX", entitySpawnPositionOffset.getX());
			nbt.putInt("entitySpawnPositionOffsetY", entitySpawnPositionOffset.getY());
			nbt.putInt("entitySpawnPositionOffsetZ", entitySpawnPositionOffset.getZ());
		} else {
			nbt.remove("entitySpawnPositionOffsetX");
			nbt.remove("entitySpawnPositionOffsetY");
			nbt.remove("entitySpawnPositionOffsetZ");
		}

		if (this.entitySpawnOrientationPitch != 0.0) {
			nbt.putDouble("entitySpawnOrientationPitch", this.entitySpawnOrientationPitch);
		} else {
			nbt.remove("entitySpawnOrientationPitch");
		}

		if (this.entitySpawnOrientationYaw != 0.0) {
			nbt.putDouble("entitySpawnOrientationYaw", this.entitySpawnOrientationYaw);
		} else {
			nbt.remove("entitySpawnOrientationYaw");
		}

		BlockPos triggeredBlockPositionOffset = this.triggeredBlock.getLeft();
		if (!triggeredBlockPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
			nbt.putInt("triggeredBlockPositionOffsetX", triggeredBlockPositionOffset.getX());
			nbt.putInt("triggeredBlockPositionOffsetY", triggeredBlockPositionOffset.getY());
			nbt.putInt("triggeredBlockPositionOffsetZ", triggeredBlockPositionOffset.getZ());
			nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());
		} else {
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		}

		BlockPos useRelayBlockPositionOffset = this.useRelayBlockPositionOffset;
		if (!useRelayBlockPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
			nbt.putInt("useRelayedBlockPositionOffsetX", useRelayBlockPositionOffset.getX());
			nbt.putInt("useRelayedBlockPositionOffsetY", useRelayBlockPositionOffset.getY());
			nbt.putInt("useRelayedBlockPositionOffsetZ", useRelayBlockPositionOffset.getZ());
		} else {
			nbt.remove("useRelayedBlockPositionOffsetX");
			nbt.remove("useRelayedBlockPositionOffsetY");
			nbt.remove("useRelayedBlockPositionOffsetZ");
		}

		// TODO convert to an nbt element
		List<RegistryEntry<EntityAttribute>> entityAttributeModifiersKeys = new ArrayList<>(this.entityAttributeModifiers.keySet());
		nbt.putInt("entityAttributeModifiersKeysSize", entityAttributeModifiersKeys.size());
		for (int i = 0; i < entityAttributeModifiersKeys.size(); i++) {
			RegistryEntry<EntityAttribute> key = entityAttributeModifiersKeys.get(i);
			Collection<EntityAttributeModifier> modifierCollection = this.entityAttributeModifiers.get(key);
			nbt.putString("entityAttributeModifiers_key" + i, String.valueOf(Registries.ATTRIBUTE.getId(key.value())));
			List<EntityAttributeModifier> modifierList = modifierCollection.stream().toList();
			nbt.putInt("entityAttributeModifiers_modifierListSize_" + i, modifierList.size());
			for (int j = 0; j < modifierList.size(); j++) {
				nbt.put("entityAttributeModifiers_" + i + "_" + j, modifierList.get(j).toNbt());
			}
		}

		if (this.spawningMode != SpawningMode.ONCE) {
			nbt.putString("spawningMode", this.spawningMode.asString());
		} else {
			nbt.remove("spawningMode");
		}

		if (this.triggered) {
			nbt.putBoolean("triggered", true);
		} else {
			nbt.remove("triggered");
		}

		if (!Objects.equals(this.entityTypeCompound, new NbtCompound())) {
			nbt.put("EntityTypeCompound", this.entityTypeCompound);
		} else {
			nbt.remove("EntityTypeCompound");
		}

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		super.readNbt(nbt, registryLookup);

		if (nbt.contains("entitySpawnPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("entitySpawnPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("entitySpawnPositionOffsetZ", NbtElement.INT_TYPE)) {
			this.entitySpawnPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("entitySpawnPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("entitySpawnPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("entitySpawnPositionOffsetZ"), -48, 48)
			);
		}

		if (nbt.contains("entitySpawnOrientationPitch", NbtElement.DOUBLE_TYPE)) {
			this.entitySpawnOrientationPitch = nbt.getDouble("entitySpawnOrientationPitch");
		}

		if (nbt.contains("entitySpawnOrientationYaw", NbtElement.DOUBLE_TYPE)) {
			this.entitySpawnOrientationYaw = nbt.getDouble("entitySpawnOrientationYaw");
		}

		if (nbt.contains("triggeredBlockPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("triggeredBlockPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("triggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) || nbt.contains("triggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockResets")
			);
		}

		if (nbt.contains("useRelayedBlockPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("useRelayedBlockPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("useRelayedBlockPositionOffsetZ", NbtElement.INT_TYPE)) {
			this.useRelayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("useRelayedBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("useRelayedBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("useRelayedBlockPositionOffsetZ"), -48, 48)
			);
		}

		this.entityAttributeModifiers.clear();
		int entityAttributeModifiersKeysSize = nbt.getInt("entityAttributeModifiersKeysSize");
		for (int i = 0; i < entityAttributeModifiersKeysSize; i++) {
			Optional<EntityAttribute> optional = Registries.ATTRIBUTE
					.getOrEmpty(Identifier.tryParse(nbt.getString("entityAttributeModifiers_key" + i)));
			if (optional.isPresent()) {
				EntityAttribute key = optional.get();
				int modifierListSize = nbt.getInt("entityAttributeModifiers_modifierListSize_" + i);
				for (int j = 0; j < modifierListSize; j++) {
					this.entityAttributeModifiers.put(key, EntityAttributeModifier.fromNbt(nbt.getCompound("entityAttributeModifiers_" + i + "_" + j)));
				}
			}
		}

		if (nbt.contains("spawningMode", NbtElement.STRING_TYPE)) {
			this.spawningMode = SpawningMode.byName(nbt.getString("spawningMode")).orElseGet(() -> SpawningMode.ONCE);
		}

		if (nbt.contains("triggered", NbtElement.BYTE_TYPE)) {
			this.triggered = nbt.getBoolean("triggered");
		}

		if (nbt.contains("EntityTypeCompound", NbtElement.COMPOUND_TYPE)) {
			this.entityTypeCompound = nbt.getCompound("EntityTypeCompound");
		}

		if (nbt.containsUuid("boundEntityUuid")) {
			this.boundEntityUuid = nbt.getUuid("boundEntityUuid");
		}
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	//region getter/setter
	public BlockPos getEntitySpawnPositionOffset() {
		return this.entitySpawnPositionOffset;
	}

	// TODO check if input is valid
	public boolean setEntitySpawnPositionOffset(BlockPos entitySpawnPositionOffset) {
		this.entitySpawnPositionOffset = entitySpawnPositionOffset;
		return true;
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

	public boolean setEntityAttributeModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers) {
		this.entityAttributeModifiers = entityAttributeModifiers;
		return true;
	}

	public SpawningMode getSpawningMode() {
		return this.spawningMode;
	}

	public boolean setSpawningMode(SpawningMode spawningMode) {
		this.spawningMode = spawningMode;
		return true;
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

	//endregion getter/setter

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.entitySpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.entitySpawnPositionOffset, blockRotation);
				this.entitySpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.entitySpawnOrientationYaw, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.useRelayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.useRelayBlockPositionOffset, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.FRONT_BACK);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.LEFT_RIGHT);
				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.triggered) {
			this.triggered = false;
		}
		if (this.boundEntityUuid != null && this.world instanceof ServerWorld serverWorld) {
			Entity entity = serverWorld.getEntity(this.boundEntityUuid);
			if (entity != null) {
				entity.discard();
			}
			this.boundEntityUuid = null;
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
		}
	}

	public void onBoundEntityKilled() {
		if (this.world != null) {
			this.boundEntityUuid = null;
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlock.getLeft().getX(), this.pos.getY() + this.triggeredBlock.getLeft().getY(), this.pos.getZ() + this.triggeredBlock.getLeft().getZ()));
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
		}
	}

	private boolean spawnEntity() {
		if (this.world instanceof ServerWorld serverWorld) {
			Optional<EntityType<?>> optional = EntityType.fromNbt(this.entityTypeCompound);
			if (optional.isEmpty()) {
				return false;
			}
			double d = (double) this.pos.getX() + this.entitySpawnPositionOffset.getX() + 0.5;
			double e = (double) this.pos.getY() + this.entitySpawnPositionOffset.getY();
			double f = (double) this.pos.getZ() + this.entitySpawnPositionOffset.getZ() + 0.5;
			if (!serverWorld.isSpaceEmpty(optional.get().getSpawnBox(d, e, f))) return false;
			BlockPos blockPos = BlockPos.ofFloored(d, e, f);
			Entity entity2 = EntityType.loadEntityWithPassengers(this.entityTypeCompound, world, entity -> {
				entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
				return entity;
			});
			if (entity2 == null) {
				return false;
			}
			entity2.setBodyYaw((float) this.entitySpawnOrientationYaw);
			entity2.setHeadYaw((float) this.entitySpawnOrientationYaw);
			entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), (float) this.entitySpawnOrientationYaw, (float) this.entitySpawnOrientationPitch);
			if (entity2 instanceof MobEntity) {
				if (this.entityTypeCompound.contains("id", NbtElement.STRING_TYPE)) {
					((MobEntity) entity2).initialize(serverWorld, serverWorld.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null);
				}
			}
			if (!serverWorld.spawnNewEntityAndPassengers(entity2)) {
				return false;
			}
			serverWorld.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, this.pos, 0);
			serverWorld.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);
			if (entity2 instanceof LivingEntity) {

				this.boundEntityUuid = ((LivingEntity) entity2).getUuid();

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
				if (entity2 instanceof MobEntity mobEntity) {
					((MobEntity) entity2).playSpawnEffects(); // TODO make optional

					if (this.spawningMode == SpawningMode.BOUND || this.spawningMode == SpawningMode.BOUND_RESPAWN) {
						mobEntity.setPersistent();
						((DuckMobEntityMixin) mobEntity).scriptblocks$setControllerBlockPos(this.pos);
					}

					if (!this.useRelayBlockPositionOffset.equals(POSITION_OFFSET_DEFAULT)) {
						((DuckMobEntityMixin) mobEntity).scriptblocks$setUseRelayBlockPos(this.pos.add(this.useRelayBlockPositionOffset));
					}
				}
			}
			return true;
		}
		return false;
	}

	public static enum SpawningMode implements StringIdentifiable {
		BOUND("bound"),
		BOUND_RESPAWN("bound_respawn"),
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		private SpawningMode(String name) {
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
}
