package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.block.Block;
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
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BossControllerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private long globalTimer = 0;
	private long phaseTimer = 0;
	private int currentPhaseId = -1;
	private Boss.Phase currentPhase = null;

	private Boss boss = null;

	@Nullable
	private UUID bossEntityUuid = null;

	private NbtCompound entityTypeCompound = new NbtCompound();

	private BlockPos uuidListHandlerBlockPositionOffset = BlockPos.ORIGIN;

	private MutablePair<BlockPos, Boolean> noPlayersAroundTriggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	private String bossIdentifier = "";

	private BlockPos bossSpawnPositionOffset = BlockPos.ORIGIN;
	private double bossSpawnOrientationPitch = 0.0;
	private double bossSpawnOrientationYaw = 0.0;

	protected boolean isPersistent = false;
	protected boolean enableSpawnEffects = true;

	private final Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

	private final HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocks = new HashMap<>();

	public BossControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.BOSS_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.put("uuid_list_handler_position_offset", NbtHelper.fromBlockPos(this.uuidListHandlerBlockPositionOffset));

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.noPlayersAroundTriggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.noPlayersAroundTriggeredBlock.getRight());
		nbt.put("triggered_block_on_no_players", nbtCompound.copy());

		nbt.putString("boss_identifier", this.bossIdentifier);

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.bossSpawnPositionOffset));
		nbtCompound.putDouble("yaw", this.bossSpawnOrientationYaw);
		nbtCompound.putDouble("pitch", this.bossSpawnOrientationPitch);
		nbt.put("boss_spawn_point", nbtCompound.copy());

		nbt.putBoolean("is_persistent", this.isPersistent);

		nbt.putBoolean("enable_spawn_effects", this.enableSpawnEffects);

		NbtList nbtList = new NbtList();
		this.bossTriggeredBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound1 = new NbtCompound();
			nbtCompound1.putString("key", key);
			nbtCompound1.put("position_offset", NbtHelper.fromBlockPos(entry.getLeft()));
			nbtCompound1.putBoolean("resets", entry.getRight());
			nbtList.add(nbtCompound1.copy());
		});
		nbt.put("boss_triggered_blocks", nbtList);

		nbt.put("entity_type_compound", this.entityTypeCompound);

		if (this.bossEntityUuid != null) {
			nbt.putUuid("boss_entity_uuid", this.bossEntityUuid);
		} else {
			nbt.remove("boss_entity_uuid");
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.uuidListHandlerBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						NbtHelper.toBlockPos(nbt, "uuid_list_handler_position_offset").orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		if (nbt.contains("noPlayersAroundTriggeredBlockPositionOffsetX") || nbt.contains("noPlayersAroundTriggeredBlockPositionOffsetY") || nbt.contains("noPlayersAroundTriggeredBlockPositionOffsetZ") || nbt.contains("noPlayersAroundTriggeredBlockResets")) {
			this.noPlayersAroundTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("noPlayersAroundTriggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("noPlayersAroundTriggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("noPlayersAroundTriggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("noPlayersAroundTriggeredBlockResets")
			);
			nbt.remove("noPlayersAroundTriggeredBlockPositionOffsetX");
			nbt.remove("noPlayersAroundTriggeredBlockPositionOffsetY");
			nbt.remove("noPlayersAroundTriggeredBlockPositionOffsetZ");
			nbt.remove("noPlayersAroundTriggeredBlockResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("triggered_block_on_no_players");
			this.noPlayersAroundTriggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("bossIdentifier")) {
			this.bossIdentifier = nbt.getString("bossIdentifier");
			nbt.remove("bossIdentifier");
		} else {
			this.bossIdentifier = nbt.getString("boss_identifier");
		}

		NbtCompound nbtCompound = nbt.getCompound("boss_spawn_point");
		if (nbt.contains("bossSpawnPositionOffsetX") || nbt.contains("bossSpawnPositionOffsetY") || nbt.contains("bossSpawnPositionOffsetZ")) {
			this.bossSpawnPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetZ"), -48, 48)
			);
			nbt.remove("bossSpawnPositionOffsetX");
			nbt.remove("bossSpawnPositionOffsetY");
			nbt.remove("bossSpawnPositionOffsetZ");
		} else {
			this.bossSpawnPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		this.isPersistent = nbt.getBoolean("is_persistent");

		this.enableSpawnEffects = nbt.getBoolean("enable_spawn_effects");

		if (nbt.contains("bossSpawnOrientationPitch", NbtElement.DOUBLE_TYPE)) {
			this.bossSpawnOrientationPitch = nbt.getDouble("bossSpawnOrientationPitch");
			nbt.remove("bossSpawnOrientationPitch");
		} else {
			this.bossSpawnOrientationPitch = MathHelper.clamp(nbtCompound.getDouble("pitch"), -90, 90);
		}

		if (nbt.contains("bossSpawnOrientationYaw", NbtElement.DOUBLE_TYPE)) {
			this.bossSpawnOrientationYaw = nbt.getDouble("bossSpawnOrientationYaw");
			nbt.remove("bossSpawnOrientationYaw");
		} else {
			this.bossSpawnOrientationYaw = MathHelper.clamp(nbtCompound.getDouble("yaw"), -180, 180);
		}

		this.bossTriggeredBlocks.clear();
		if (nbt.contains("bossTriggeredBlocksKeysSize")) {
			int bossTriggeredBlocksKeysSize = nbt.getInt("bossTriggeredBlocksKeysSize");
			for (int b = 0; b < bossTriggeredBlocksKeysSize; b++) {
				this.bossTriggeredBlocks.put(nbt.getString("bossTriggeredBlocks_key_" + b), new MutablePair<>(
						new BlockPos(
								MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_X_" + b), -48, 48),
								MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_Y_" + b), -48, 48),
								MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_Z_" + b), -48, 48)
						), nbt.getBoolean("bossTriggeredBlocks_entry_resets_" + b)));
				nbt.remove("bossTriggeredBlocks_key_" + b);
				nbt.remove("bossTriggeredBlocks_entry_X_" + b);
				nbt.remove("bossTriggeredBlocks_entry_Y_" + b);
				nbt.remove("bossTriggeredBlocks_entry_Z_" + b);
				nbt.remove("bossTriggeredBlocks_entry_resets_" + b);
			}
			nbt.remove("bossTriggeredBlocksKeysSize");
		} else {
			NbtList nbtList = nbt.getList("boss_triggered_blocks", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				nbtCompound = nbtList.getCompound(i);
				this.bossTriggeredBlocks.put(
						nbtCompound.getString("key"),
						new MutablePair<>(
								NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
								nbtCompound.getBoolean("resets")
						)
				);
			}
		}

		if (nbt.contains("EntityTypeCompound", NbtElement.COMPOUND_TYPE)) {
			this.entityTypeCompound = nbt.getCompound("EntityTypeCompound");
		} else {
			this.entityTypeCompound = nbt.getCompound("entity_type_compound");
		}

		if (nbt.containsUuid("bossEntityUuid")) {
			this.bossEntityUuid = nbt.getUuid("bossEntityUuid");
			nbt.remove("bossEntityUuid");
		} else if (nbt.containsUuid("boss_entity_uuid")) {
			this.bossEntityUuid = nbt.getUuid("boss_entity_uuid");
		} else {
			this.bossEntityUuid = null;
			nbt.remove("bossEntityUuid");
			nbt.remove("boss_entity_uuid");
		}

		super.readNbt(nbt, registryLookup);

	}

	public static void tick(World world, BlockPos pos, BlockState state, BossControllerBlockEntity bC) {
		if (!world.isClient && bC.currentPhase != null && bC.boss != null) {
			bC.globalTimer++;
			bC.phaseTimer++;
			int globalTimerThreshold = bC.currentPhase.globalTimerThreshold();
			int phaseTimerThreshold = bC.currentPhase.phaseTimerThreshold();
			if (globalTimerThreshold > -1 && bC.globalTimer >= globalTimerThreshold) {
				advancePhase(bC);
			} else if (phaseTimerThreshold > -1 && bC.phaseTimer >= phaseTimerThreshold) {
				advancePhase(bC);
			}
			if (bC.boss.triggersBlockWhenNoPlayersAround() && bC.noPlayersPresentInArea()) {
				MutablePair<BlockPos, Boolean> noPlayersAroundTriggeredBlock = bC.getNoPlayersAroundTriggeredBlock();
				BlockEntity blockEntity = world.getBlockEntity(new BlockPos(bC.pos.getX() + noPlayersAroundTriggeredBlock.getLeft().getX(), bC.pos.getY() + noPlayersAroundTriggeredBlock.getLeft().getY(), bC.pos.getZ() + noPlayersAroundTriggeredBlock.getLeft().getZ()));
				if (blockEntity != bC) {
					boolean noPlayersAroundTriggeredBlockResets = noPlayersAroundTriggeredBlock.getRight();
					if (noPlayersAroundTriggeredBlockResets && blockEntity instanceof Resetable resetable) {
						resetable.reset();
					} else if (!noPlayersAroundTriggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
						triggerable.trigger();
					}
				}
			}
			bC.markDirty();
			if (bC.world != null) {
				BlockState blockState = bC.world.getBlockState(bC.pos);
				bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
		}
	}

	//region Boss Battle Logic
	private static void startBattle(BossControllerBlockEntity bC) {

		DebuggingHelper.sendBossControllerLogMessage("startBattle", null);

		String identifierString = bC.bossIdentifier;
		if (!identifierString.isEmpty() && bC.world != null) {
			Optional<RegistryEntry.Reference<Boss>> optionalBossReference = bC.world.getRegistryManager().get(ScriptBlocksDynamicRegistries.BOSS_REGISTRY_KEY).getEntry(Identifier.of(identifierString));
			if (optionalBossReference.isPresent()) {
				bC.boss = optionalBossReference.get().value();
			}
		}

		if (bC.boss != null) {

			DebuggingHelper.sendBossControllerLogMessage("bC.boss != null", null);

			if (bC.setEntityType(bC.boss.bossEntityTypeId())) {
				bC.currentPhaseId = 0;
				bC.currentPhase = bC.boss.phases().getFirst();
				if (spawnBossEntity(bC)) {

					DebuggingHelper.sendBossControllerLogMessage("boss spawned", null);

					startPhase(bC);
				}
			}
		}
		bC.markDirty();
		if (bC.world != null) {
			BlockState blockState = bC.world.getBlockState(bC.pos);
			bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	private static void advancePhase(BossControllerBlockEntity bC) {

		DebuggingHelper.sendBossControllerLogMessage("advancePhase", null);

		if (bC.boss != null) {
			if ((bC.currentPhaseId + 1) < bC.boss.phases().size()) {
				bC.phaseTimer = 0;
				endPhase(bC, true);
				bC.currentPhaseId++;
				bC.currentPhase = bC.boss.phases().get(bC.currentPhaseId);
				startPhase(bC);
			} else {
				endBattle(bC);
			}
		} else {

			DebuggingHelper.sendBossControllerLogMessage("A bossControllerBlock at " + bC.getPos().toString() + " tried to advance a non existing boss fight.", null);

		}
		bC.markDirty();
		if (bC.world != null) {
			BlockState blockState = bC.world.getBlockState(bC.pos);
			bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	private static void endBattle(BossControllerBlockEntity bC) {
		endPhase(bC, false);
		bC.currentPhase = null;
		if (bC.boss.discardEntityAtEnd() && bC.bossEntityUuid != null && bC.world instanceof ServerWorld serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (entity != null) {
				entity.discard();
			}
			bC.bossEntityUuid = null;
		}
		bC.markDirty();
		if (bC.world != null) {
			BlockState blockState = bC.world.getBlockState(bC.pos);
			bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	private static void startPhase(BossControllerBlockEntity bC) {
		Boss.Phase phase = bC.currentPhase;

		DebuggingHelper.sendBossControllerLogMessage("startPhase", null);

		bC.entityAttributeModifiers.clear();
		bC.entityAttributeModifiers.putAll(getEntityAttributeModifiers(bC.currentPhase));

		// trigger block
		String triggeredBlock = phase.triggeredBlockAtStart();
		if (triggeredBlock != null) {
			List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = new ArrayList<>(List.of());
			List<String> keyList = new ArrayList<>(bC.getBossTriggeredBlocks().keySet());
			for (String key : keyList) {
				bossTriggeredBlocksList.add(new MutablePair<>(key, bC.getBossTriggeredBlocks().get(key)));
			}
			for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : bossTriggeredBlocksList) {
				if (entry.getLeft().equals(triggeredBlock)) {

					World world = bC.getWorld();
					BlockEntity blockEntity = null;
					if (world != null) {
						blockEntity = world.getBlockEntity(entry.getRight().getLeft().add(bC.pos));
					}

					if (blockEntity != null && blockEntity != bC) {
						boolean triggeredBlockResets = entry.getRight().getRight();
						if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
							resetable.reset();
						} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
							triggerable.trigger();
						}
					}
					break;
				}
			}
		}

		// modify boss entity
		if (bC.bossEntityUuid != null && bC.world instanceof ServerWorld serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (entity instanceof MobEntity mobEntity) {
				((DuckMobEntityMixin) mobEntity).scriptblocks$setBossHealthThreshold(bC.currentPhase.bossHealthThreshold());
				((DuckMobEntityMixin) mobEntity).scriptblocks$setBossPhase(bC.currentPhaseId);
				if (!bC.entityAttributeModifiers.isEmpty()) {
					AttributeContainer attributeContainer = mobEntity.getAttributes();
					bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
						EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
						if (entityAttributeInstance != null) {
							entityAttributeInstance.addPersistentModifier((EntityAttributeModifier) attributeModifier);
						}
					});
				}
				if (bC.currentPhase.newHealthRatio() > 0.0) {
					mobEntity.setHealth(mobEntity.getMaxHealth() * Math.min(1.0F, bC.currentPhase.newHealthRatio()));
				}
			}
		}
		bC.markDirty();
		if (bC.world != null) {
			BlockState blockState = bC.world.getBlockState(bC.pos);
			bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	private static void endPhase(BossControllerBlockEntity bC, boolean removeAttributeModifiers) {
		Boss.Phase phase = bC.currentPhase;

		DebuggingHelper.sendBossControllerLogMessage("endPhase, removeAttributeModifiers: " + removeAttributeModifiers, null);

		// trigger block
		String triggeredBlock = phase.triggeredBlockAtEnd();
		if (triggeredBlock != null) {
			List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = new ArrayList<>(List.of());
			List<String> keyList = new ArrayList<>(bC.getBossTriggeredBlocks().keySet());
			for (String key : keyList) {
				bossTriggeredBlocksList.add(new MutablePair<>(key, bC.getBossTriggeredBlocks().get(key)));
			}
			for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : bossTriggeredBlocksList) {
				if (entry.getLeft().equals(triggeredBlock)) {

					World world = bC.getWorld();
					BlockEntity blockEntity = null;
					if (world != null) {
						blockEntity = world.getBlockEntity(entry.getRight().getLeft().add(bC.pos));
					}

					if (blockEntity != null && blockEntity != bC) {
						boolean triggeredBlockResets = entry.getRight().getRight();
						if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
							resetable.reset();
						} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
							triggerable.trigger();
						}
					}
					break;
				}
			}
		}

		// modify boss entity
		if (bC.bossEntityUuid != null && removeAttributeModifiers && bC.world instanceof ServerWorld serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (!bC.entityAttributeModifiers.isEmpty() && entity instanceof LivingEntity) {
				AttributeContainer attributeContainer = ((LivingEntity) entity).getAttributes();
				bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
					EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance(attribute);
					if (entityAttributeInstance != null) {
						entityAttributeInstance.removeModifier(attributeModifier);
					}
				});
			} else if (bC.entityAttributeModifiers.isEmpty()) {

				DebuggingHelper.sendBossControllerLogMessage("bC.entityAttributeModifiers.isEmpty()", null);

			}
		}
	}

	public static void bossReachedHealthThreshold(BossControllerBlockEntity bC, MobEntity mobEntity) {
		if (mobEntity.getUuid() != bC.bossEntityUuid) {
			mobEntity.discard();
		}
		advancePhase(bC);
	}

	private static Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getEntityAttributeModifiers(Boss.Phase phase) {
		Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

		for (Boss.Phase.AttributeModifierEntry entityAttributeModifier : phase.entityAttributeModifiers()) {
			Optional<EntityAttribute> optional = Registries.ATTRIBUTE
					.getOrEmpty(Identifier.of(entityAttributeModifier.attributeIdentifier()));
			if (optional.isPresent()) {
				RegistryEntry<EntityAttribute> key = Registries.ATTRIBUTE.getEntry(optional.get());
				entityAttributeModifiers.put(key, new EntityAttributeModifier(Identifier.of(entityAttributeModifier.id()), entityAttributeModifier.amount(), EntityAttributeModifier.Operation.valueOf(entityAttributeModifier.operation())));
			}
		}
		return entityAttributeModifiers;
	}

	private static boolean spawnBossEntity(BossControllerBlockEntity bC) {
		if (bC.world instanceof ServerWorld serverWorld) {
			if (bC.bossEntityUuid != null) {
				Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
				if (entity != null) {
					entity.discard();
				}
			}
			Optional<EntityType<?>> optional = EntityType.fromNbt(bC.entityTypeCompound);
			if (optional.isEmpty()) {
				return false;
			}
			BlockPos blockPos = bC.pos.add(bC.bossSpawnPositionOffset.getX(), bC.bossSpawnPositionOffset.getY(), bC.bossSpawnPositionOffset.getZ());
			double pitch = bC.bossSpawnOrientationPitch;
			double yaw = bC.bossSpawnOrientationYaw;

			if (serverWorld.getBlockEntity(blockPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
				MutablePair<BlockPos, MutablePair<Double, Double>> entrance = spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);

				blockPos = entrance.getLeft();
				yaw = entrance.getRight().getLeft();
				pitch = entrance.getRight().getRight();
			}
			double d = blockPos.getX() + 0.5;
			double e = blockPos.getY();
			double f = blockPos.getZ() + 0.5;

			if (!serverWorld.isSpaceEmpty(optional.get().getSpawnBox(d, e, f))) {

				DebuggingHelper.sendBossControllerLogMessage("not enough space for spawning boss entity", null);

				return false;
			}
			blockPos = BlockPos.ofFloored(d, e, f);
			Entity entity2 = EntityType.loadEntityWithPassengers(bC.entityTypeCompound, bC.world, entity -> {
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
				if (bC.entityTypeCompound.contains("id", NbtElement.STRING_TYPE)) {
					mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null);
				}
				if (!serverWorld.spawnNewEntityAndPassengers(entity2)) {
					return false;
				}
				if (bC.enableSpawnEffects) {
					serverWorld.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, bC.pos, 0);
				}
				serverWorld.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);

				bC.bossEntityUuid = entity2.getUuid();
				if (bC.isPersistent) {
					mobEntity.setPersistent();
				}
				if (bC.enableSpawnEffects) {
					mobEntity.playSpawnEffects();
				}

				mobEntity.setPersistent();
				((DuckMobEntityMixin) mobEntity).scriptblocks$setControllerBlockPos(bC.pos);
			}
			bC.markDirty();
			if (bC.world != null) {
				BlockState blockState = bC.world.getBlockState(bC.pos);
				bC.world.updateListeners(bC.pos, blockState, blockState, Block.NOTIFY_ALL);
			}
			return true;
		}
		return false;
	}
	//endregion Boss Battle Logic

	@Override
	public void reset() {
		this.globalTimer = 0;
		this.phaseTimer = 0;
		this.currentPhaseId = -1;
		this.currentPhase = null;
		this.boss = null;

		DebuggingHelper.sendBossControllerLogMessage("BossControllerBlock reset", null);

		if (this.world instanceof ServerWorld serverWorld) {
			DebuggingHelper.sendBossControllerLogMessage("world instanceof ServerWorld", null);

			if (this.bossEntityUuid != null) {

				DebuggingHelper.sendBossControllerLogMessage("bossEntityUuid != null", null);

				Entity entity = serverWorld.getEntity(this.bossEntityUuid);
				if (entity != null) {
					// TODO alternative: set flag in mobEntity, that is checked in tick and triggers the discard on the entity side

					DebuggingHelper.sendBossControllerLogMessage("bossEntity discarded", null);

					entity.discard();
				}
				this.bossEntityUuid = null;
			} else {
				DebuggingHelper.sendBossControllerLogMessage("bossEntityUuid == null", null);
			}
		}
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	public boolean noPlayersPresentInArea() {
		if (this.world != null) {
			BlockPos uuidListHandlerBlockPos = new BlockPos(this.pos.getX() + this.uuidListHandlerBlockPositionOffset.getX(), this.pos.getY() + this.uuidListHandlerBlockPositionOffset.getY(), this.pos.getZ() + this.uuidListHandlerBlockPositionOffset.getZ());

			BlockEntity blockEntity = world.getBlockEntity(uuidListHandlerBlockPos);
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				return handlesUUIDList.supplyUUIDList(false).isEmpty();
			}
		}
		return false;
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

	// endregion --- getter & setter ---
	public BlockPos getUUIDListHandlerBlockPositionOffset() {
		return this.uuidListHandlerBlockPositionOffset;
	}

	public void setUUIDListHandlerBlockPositionOffset(BlockPos uuidListHandlerBlockPositionOffset) {
		this.uuidListHandlerBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						uuidListHandlerBlockPositionOffset,
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);
	}

	public MutablePair<BlockPos, Boolean> getNoPlayersAroundTriggeredBlock() {
		return this.noPlayersAroundTriggeredBlock;
	}

	public void setNoPlayersAroundTriggeredBlock(MutablePair<BlockPos, Boolean> noPlayersAroundTriggeredBlock) {
		this.noPlayersAroundTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								noPlayersAroundTriggeredBlock.getLeft(),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				noPlayersAroundTriggeredBlock.getRight()
		);
	}

	public BlockPos getBossSpawnPositionOffset() {
		return this.bossSpawnPositionOffset;
	}

	public void setBossSpawnPositionOffset(BlockPos bossSpawnPositionOffset) {
		this.bossSpawnPositionOffset = BlockPos.max(
				BlockPos.min(
						bossSpawnPositionOffset,
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);
	}

	public double getBossSpawnOrientationPitch() {
		return bossSpawnOrientationPitch;
	}

	public void setBossSpawnPositionPitch(double bossSpawnPositionPitch) {
		this.bossSpawnOrientationPitch = MathHelper.clamp(bossSpawnPositionPitch, -90, 90);
		;
	}

	public double getBossSpawnOrientationYaw() {
		return bossSpawnOrientationYaw;
	}

	public void setBossSpawnPositionYaw(double bossSpawnPositionYaw) {
		this.bossSpawnOrientationYaw = MathHelper.clamp(bossSpawnPositionYaw, -180, 180);
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

	public String getBossIdentifier() {
		return this.bossIdentifier;
	}

	public void setBossIdentifier(String bossIdentifier) {
		this.bossIdentifier = bossIdentifier;
	}

	public HashMap<String, MutablePair<BlockPos, Boolean>> getBossTriggeredBlocks() {
		return this.bossTriggeredBlocks;
	}

	public void setBossTriggeredBlocks(HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocks) {
		this.bossTriggeredBlocks.clear();
		this.bossTriggeredBlocks.putAll(bossTriggeredBlocks);
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.boss != null && this.currentPhase != null && this.currentPhase.triggerEndsPhase()) {
			advancePhase(this);
		}
		if (this.currentPhase == null && this.currentPhaseId == -1) {
			startBattle(this);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, blockRotation);

				this.bossSpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.bossSpawnPositionOffset, blockRotation);
				this.bossSpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.bossSpawnOrientationYaw, blockRotation);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), blockRotation));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, BlockMirror.FRONT_BACK);

				this.bossSpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.bossSpawnPositionOffset, BlockMirror.FRONT_BACK);
				this.bossSpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.bossSpawnOrientationYaw, BlockMirror.FRONT_BACK);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, BlockMirror.LEFT_RIGHT);

				this.bossSpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.bossSpawnPositionOffset, BlockMirror.LEFT_RIGHT);
				this.bossSpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.bossSpawnOrientationYaw, BlockMirror.LEFT_RIGHT);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

}
