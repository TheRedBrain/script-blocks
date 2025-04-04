package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

public class TriggeredVillagerSpawnerBlockEntity extends TriggeredSpawnerBlockEntity {

	private static final VillagerData DEFAULT_VILLAGER_DATA = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);
	private VillagerData villagerData = DEFAULT_VILLAGER_DATA;

	public TriggeredVillagerSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);

		VillagerData.CODEC
				.encodeStart(NbtOps.INSTANCE, this.villagerData)
				.resultOrPartial(ScriptBlocks.LOGGER::error)
				.ifPresent(nbtElement -> nbt.put("VillagerData", nbtElement));

	}

	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);

		if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
			VillagerData.CODEC
					.parse(NbtOps.INSTANCE, nbt.get("VillagerData"))
					.resultOrPartial(ScriptBlocks.LOGGER::error)
					.ifPresent(villagerData -> this.villagerData = villagerData);
		}

	}

	public VillagerData getVillagerData() {
		return this.villagerData;
	}

	public void setVillagerData(VillagerData villagerData) {
		this.villagerData = villagerData;
	}

	@Override
	protected boolean spawnEntity() {
		World var2 = this.world;
		if (var2 instanceof ServerWorld serverWorld) {
			Optional<EntityType<?>> optional = EntityType.fromNbt(this.entityTypeCompound);
			if (optional.isPresent()) {
				double d = (double) this.pos.getX() + (double) this.entitySpawnPositionOffset.getX() + 0.5;
				double e = (double) this.pos.getY() + (double) this.entitySpawnPositionOffset.getY();
				double f = (double) this.pos.getZ() + (double) this.entitySpawnPositionOffset.getZ() + 0.5;
				if (serverWorld.isSpaceEmpty(((EntityType) optional.get()).getSpawnBox(d, e, f))) {
					BlockPos blockPos = BlockPos.ofFloored(d, e, f);
					Entity entity2 = EntityType.loadEntityWithPassengers(this.entityTypeCompound, this.world, (entity) -> {
						entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
						return entity;
					});
					if (entity2 instanceof VillagerDataContainer villagerDataContainer) {
						entity2.setBodyYaw((float) this.entitySpawnOrientationYaw);
						entity2.setHeadYaw((float) this.entitySpawnOrientationYaw);
						entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), (float) this.entitySpawnOrientationYaw, (float) this.entitySpawnOrientationPitch);
						if (entity2 instanceof MobEntity && this.entityTypeCompound.contains("id", 8)) {
							((MobEntity) entity2).initialize(serverWorld, serverWorld.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, (EntityData) null);
						}

						if (serverWorld.spawnNewEntityAndPassengers(entity2)) {
							serverWorld.syncWorldEvent(2004, this.pos, 0);
							serverWorld.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);

							villagerDataContainer.setVillagerData(this.villagerData);

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

								if (entity2 instanceof MobEntity) {
									MobEntity mobEntity = (MobEntity) entity2;
									((MobEntity) entity2).playSpawnEffects();
									if (this.spawningMode == TriggeredSpawnerBlockEntity.SpawningMode.BOUND || this.spawningMode == TriggeredSpawnerBlockEntity.SpawningMode.BOUND_RESPAWN) {
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
					}
				}
			}
		}
		return false;
	}
}
