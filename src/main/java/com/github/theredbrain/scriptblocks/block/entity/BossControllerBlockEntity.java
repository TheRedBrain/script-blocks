package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

	private CompoundTag entityTypeCompound = new CompoundTag();

	private BlockPos uuidListHandlerBlockPositionOffset = BlockPos.ZERO;

	private MutablePair<BlockPos, Boolean> noPlayersAroundTriggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	private String bossIdentifier = "";

	private LevelData.RespawnData spawnPoint = LevelData.RespawnData.DEFAULT;

	protected boolean isPersistent = false;
	protected boolean enableSpawnEffects = true;

	private final Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

	private final HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocks = new HashMap<>();

	public BossControllerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.BOSS_CONTROLLER_BLOCK_ENTITY, pos, state);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.uuidListHandlerBlockPositionOffset);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.noPlayersAroundTriggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.noPlayersAroundTriggeredBlock.getRight());
		view.store("triggered_block_on_no_players", CompoundTag.CODEC, nbtCompound.copy());

		view.putString("boss_identifier", this.bossIdentifier);

		view.store("spawn_point", LevelData.RespawnData.CODEC, this.spawnPoint);

		view.putBoolean("is_persistent", this.isPersistent);

		view.putBoolean("enable_spawn_effects", this.enableSpawnEffects);

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("boss_triggered_blocks", CompoundTag.CODEC);
		this.bossTriggeredBlocks.forEach((key, entry) -> {
			CompoundTag nbtCompound1 = new CompoundTag();
			nbtCompound1.putString("key", key);
			nbtCompound1.store("position_offset", BlockPos.CODEC, entry.getLeft());
			nbtCompound1.putBoolean("resets", entry.getRight());
			listAppender.add(nbtCompound1.copy());
		});

		view.store("entity_type_compound", CompoundTag.CODEC, this.entityTypeCompound);

		view.store("boss_entity_uuid", UUIDUtil.AUTHLIB_CODEC, this.bossEntityUuid);

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.uuidListHandlerBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		CompoundTag nbtCompound = view.read("triggered_block_on_no_players", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.noPlayersAroundTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.bossIdentifier = view.getStringOr("boss_identifier", "");

		this.spawnPoint = view.read("spawn_point", LevelData.RespawnData.CODEC).orElseGet(() -> {
					CompoundTag nbtCompound1 = view.read("boss_spawn_point", CompoundTag.CODEC).orElseGet(CompoundTag::new);
					return LevelData.RespawnData.of(
							this.getLevel() != null ? this.getLevel().dimension() : ServerLevel.OVERWORLD,
							nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							(float) nbtCompound1.getDoubleOr("yaw", 0),
							(float) nbtCompound1.getDoubleOr("pitch", 0)
					);
				}
		);

		this.isPersistent = view.getBooleanOr("is_persistent", false);

		this.enableSpawnEffects = view.getBooleanOr("enable_spawn_effects", true);

		this.bossTriggeredBlocks.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("boss_triggered_blocks", CompoundTag.CODEC)) {
			this.bossTriggeredBlocks.put(
					nbtCompound1.getStringOr("key", ""),
					new MutablePair<>(
							nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							nbtCompound1.getBooleanOr("resets", false)
					)
			);
		}

		this.entityTypeCompound = view.read("entity_type_compound", CompoundTag.CODEC).orElseGet(CompoundTag::new);

		this.bossEntityUuid = view.read("boss_entity_uuid", UUIDUtil.AUTHLIB_CODEC).orElse(null);

		super.loadAdditional(view);

	}

	public static void tick(Level world, BlockPos pos, BlockState state, BossControllerBlockEntity bC) {
		if (!world.isClientSide() && bC.currentPhase != null && bC.boss != null) {
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
				BlockEntity blockEntity = world.getBlockEntity(new BlockPos(bC.worldPosition.getX() + noPlayersAroundTriggeredBlock.getLeft().getX(), bC.worldPosition.getY() + noPlayersAroundTriggeredBlock.getLeft().getY(), bC.worldPosition.getZ() + noPlayersAroundTriggeredBlock.getLeft().getZ()));
				if (blockEntity != bC) {
					boolean noPlayersAroundTriggeredBlockResets = noPlayersAroundTriggeredBlock.getRight();
					if (noPlayersAroundTriggeredBlockResets && blockEntity instanceof Resetable resetable) {
						resetable.reset();
					} else if (!noPlayersAroundTriggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
						triggerable.trigger();
					}
				}
			}
			bC.setChanged();
			if (bC.level != null) {
				BlockState blockState = bC.level.getBlockState(bC.worldPosition);
				bC.level.sendBlockUpdated(bC.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}
	}

	//region Boss Battle Logic
	private static void startBattle(BossControllerBlockEntity bC) {

		DebuggingHelper.sendBossControllerLogMessage("startBattle", null);

		String identifierString = bC.bossIdentifier;
		if (!identifierString.isEmpty() && bC.level != null) {
			Optional<Holder.Reference<Boss>> optionalBossReference = bC.level.registryAccess().get(ScriptBlocksDynamicRegistries.BOSS_REGISTRY_KEY).get().value().get(Identifier.parse(identifierString));
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
		bC.setChanged();
		if (bC.level != null) {
			BlockState blockState = bC.level.getBlockState(bC.worldPosition);
			bC.level.sendBlockUpdated(bC.worldPosition, blockState, blockState, Block.UPDATE_ALL);
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

			DebuggingHelper.sendBossControllerLogMessage("A bossControllerBlock at " + bC.getBlockPos().toString() + " tried to advance a non existing boss fight.", null);

		}
		bC.setChanged();
		if (bC.level != null) {
			BlockState blockState = bC.level.getBlockState(bC.worldPosition);
			bC.level.sendBlockUpdated(bC.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	private static void endBattle(BossControllerBlockEntity bC) {
		endPhase(bC, false);
		bC.currentPhase = null;
		if (bC.boss.discardEntityAtEnd() && bC.bossEntityUuid != null && bC.level instanceof ServerLevel serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (entity != null) {
				entity.discard();
			}
			bC.bossEntityUuid = null;
		}
		bC.setChanged();
		if (bC.level != null) {
			BlockState blockState = bC.level.getBlockState(bC.worldPosition);
			bC.level.sendBlockUpdated(bC.worldPosition, blockState, blockState, Block.UPDATE_ALL);
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

					Level world = bC.getLevel();
					BlockEntity blockEntity = null;
					if (world != null) {
						blockEntity = world.getBlockEntity(entry.getRight().getLeft().offset(bC.worldPosition));
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
		if (bC.bossEntityUuid != null && bC.level instanceof ServerLevel serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (entity instanceof Mob mobEntity) {
				((DuckMobEntityMixin) mobEntity).scriptblocks$setBossHealthThreshold(bC.currentPhase.bossHealthThreshold());
				((DuckMobEntityMixin) mobEntity).scriptblocks$setBossPhase(bC.currentPhaseId);
				if (!bC.entityAttributeModifiers.isEmpty()) {
					AttributeMap attributeContainer = mobEntity.getAttributes();
					bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
						AttributeInstance entityAttributeInstance = attributeContainer.getInstance(attribute);
						if (entityAttributeInstance != null) {
							entityAttributeInstance.addPermanentModifier((AttributeModifier) attributeModifier);
						}
					});
				}
				if (bC.currentPhase.newHealthRatio() > 0.0) {
					mobEntity.setHealth(mobEntity.getMaxHealth() * Math.min(1.0F, bC.currentPhase.newHealthRatio()));
				}
			}
		}
		bC.setChanged();
		if (bC.level != null) {
			BlockState blockState = bC.level.getBlockState(bC.worldPosition);
			bC.level.sendBlockUpdated(bC.worldPosition, blockState, blockState, Block.UPDATE_ALL);
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

					Level world = bC.getLevel();
					BlockEntity blockEntity = null;
					if (world != null) {
						blockEntity = world.getBlockEntity(entry.getRight().getLeft().offset(bC.worldPosition));
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
		if (bC.bossEntityUuid != null && removeAttributeModifiers && bC.level instanceof ServerLevel serverWorld) {
			Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
			if (!bC.entityAttributeModifiers.isEmpty() && entity instanceof LivingEntity) {
				AttributeMap attributeContainer = ((LivingEntity) entity).getAttributes();
				bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
					AttributeInstance entityAttributeInstance = attributeContainer.getInstance(attribute);
					if (entityAttributeInstance != null) {
						entityAttributeInstance.removeModifier(attributeModifier);
					}
				});
			} else if (bC.entityAttributeModifiers.isEmpty()) {

				DebuggingHelper.sendBossControllerLogMessage("bC.entityAttributeModifiers.isEmpty()", null);

			}
		}
	}

	public static void bossReachedHealthThreshold(BossControllerBlockEntity bC, Mob mobEntity) {
		if (mobEntity.getUUID() != bC.bossEntityUuid) {
			mobEntity.discard();
		}
		advancePhase(bC);
	}

	private static Multimap<Holder<Attribute>, AttributeModifier> getEntityAttributeModifiers(Boss.Phase phase) {
		Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

		for (Boss.Phase.AttributeModifierEntry entityAttributeModifier : phase.entityAttributeModifiers()) {
			Optional<Attribute> optional = BuiltInRegistries.ATTRIBUTE
					.getOptional(Identifier.parse(entityAttributeModifier.attributeIdentifier()));
			if (optional.isPresent()) {
				Holder<Attribute> key = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(optional.get());
				entityAttributeModifiers.put(key, new AttributeModifier(Identifier.parse(entityAttributeModifier.id()), entityAttributeModifier.amount(), AttributeModifier.Operation.valueOf(entityAttributeModifier.operation())));
			}
		}
		return entityAttributeModifiers;
	}

	public LevelData.RespawnData getActualSpawnPoint(ServerLevel serverWorld) {

		BlockPos targetPos = BlockRotationUtils.getActualOffsetBlockPosition(this.getBlockPos(), this.spawnPoint.pos());

		if (!this.spawnPoint.pos().equals(BlockPos.ZERO) && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return this.spawnPoint;
	}

	private static boolean spawnBossEntity(BossControllerBlockEntity bC) {
		if (bC.level instanceof ServerLevel serverWorld) {
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(bC::toString, ScriptBlocks.LOGGER)) {
				if (bC.bossEntityUuid != null) {
					Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
					if (entity != null) {
						entity.discard();
					}
				}
				ValueInput input = TagValueInput.create(reporter, serverWorld.registryAccess(), bC.entityTypeCompound);
				Optional<EntityType<?>> entityType = EntityType.by(input);
				if (entityType.isEmpty()) {
					return false;
				}
				LevelData.RespawnData spawnPoint = bC.getActualSpawnPoint(serverWorld);
				BlockPos blockPos = spawnPoint.pos();
				float yaw = spawnPoint.yaw();
				float pitch = spawnPoint.pitch();
				double d = blockPos.getX() + 0.5;
				double e = blockPos.getY();
				double f = blockPos.getZ() + 0.5;

				if (!serverWorld.noCollision(entityType.get().getSpawnAABB(d, e, f))) {

					DebuggingHelper.sendBossControllerLogMessage("not enough space for spawning boss entity", null);

					return false;
				}
				blockPos = BlockPos.containing(d, e, f);
				Entity entity2 = EntityType.loadEntityRecursive(input, bC.level, EntitySpawnReason.SPAWNER, entity -> {
					entity.snapTo(d, e, f, entity.getYRot(), entity.getXRot());
					return entity;
				});
				if (entity2 == null) {
					return false;
				}
				entity2.setYBodyRot(yaw);
				entity2.setYHeadRot(yaw);
				entity2.snapTo(entity2.getX(), entity2.getY(), entity2.getZ(), yaw, pitch);
				if (entity2 instanceof Mob mobEntity) {
					if (bC.entityTypeCompound.getString("id").isPresent()) {
						mobEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(entity2.blockPosition()), EntitySpawnReason.SPAWNER, null);
					}
					if (!serverWorld.tryAddFreshEntityWithPassengers(entity2)) {
						return false;
					}
					if (bC.enableSpawnEffects) {
						serverWorld.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, bC.getBlockPos(), 0);
					}
					serverWorld.gameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);

					bC.bossEntityUuid = entity2.getUUID();
					if (bC.isPersistent) {
						mobEntity.setPersistenceRequired();
					}
					if (bC.enableSpawnEffects) {
						mobEntity.spawnAnim();
					}

					((DuckMobEntityMixin) mobEntity).scriptblocks$setControllerBlockPos(bC.getBlockPos());
				}
				bC.setChanged();
				if (bC.level != null) {
					BlockState blockState = bC.level.getBlockState(bC.getBlockPos());
					bC.level.sendBlockUpdated(bC.getBlockPos(), blockState, blockState, Block.UPDATE_ALL);
				}
				return true;
			}
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

		if (this.level instanceof ServerLevel serverWorld) {
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
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	public boolean noPlayersPresentInArea() {
		if (this.level != null) {
			BlockPos uuidListHandlerBlockPos = new BlockPos(this.worldPosition.getX() + this.uuidListHandlerBlockPositionOffset.getX(), this.worldPosition.getY() + this.uuidListHandlerBlockPositionOffset.getY(), this.worldPosition.getZ() + this.uuidListHandlerBlockPositionOffset.getZ());

			BlockEntity blockEntity = level.getBlockEntity(uuidListHandlerBlockPos);
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				return handlesUUIDList.supplyUUIDList(false).isEmpty();
			}
		}
		return false;
	}

	public String getEntityTypeId() {
		if (this.entityTypeCompound != null) {
			return this.entityTypeCompound.getString("id").orElse("");
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

	// region --- getter & setter ---
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

	public LevelData.RespawnData getSpawnPoint() {
		return this.spawnPoint;
	}

	public void setSpawnPoint(LevelData.RespawnData spawnPoint) {
		this.spawnPoint = LevelData.RespawnData.of(spawnPoint.dimension(), spawnPoint.pos(), spawnPoint.yaw(), spawnPoint.pitch());
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, blockRotation);

				this.spawnPoint = BlockRotationUtils.rotateSpawnPoint(this.spawnPoint, blockRotation);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), blockRotation));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, Mirror.FRONT_BACK);

				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, Mirror.FRONT_BACK);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.FRONT_BACK));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.uuidListHandlerBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerBlockPositionOffset, Mirror.LEFT_RIGHT);

				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, Mirror.LEFT_RIGHT);

				this.noPlayersAroundTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.noPlayersAroundTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.LEFT_RIGHT));
					this.bossTriggeredBlocks.put(key, oldBlockPos);
				}

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

}
