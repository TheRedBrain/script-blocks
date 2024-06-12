package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.entity.mob.BossEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.BossesRegistry;
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
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BossControllerBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {


    private long globalTimer;
    private long phaseTimer;
    private int currentPhaseId;
    private Boss.Phase currentPhase;

    private Boss boss;

    @Nullable
    private UUID bossEntityUuid = null;

    private NbtCompound entityTypeCompound = new NbtCompound();

    // set via GUI
    private boolean calculateAreaBox = true;
    private Box area = null;
    private boolean showArea = false;
    private Vec3i areaDimensions = Vec3i.ZERO;
    private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

    private String bossIdentifier = "";

    private BlockPos bossSpawnPositionOffset = new BlockPos(0, 1, 0);
    private double bossSpawnOrientationPitch = 0.0;
    private double bossSpawnOrientationYaw = 0.0;

    Multimap<EntityAttribute, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

    private HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocks = new HashMap<>();

    public BossControllerBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.BOSS_CONTROLLER_BLOCK_ENTITY, pos, state);
        this.globalTimer = 0;
        this.phaseTimer = 0;
        this.currentPhaseId = -1;
        this.currentPhase = null;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {

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

        if (this.areaDimensions.getX() != 0) {
            nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
        } else {
            nbt.remove("areaDimensionsX");
        }

        if (this.areaDimensions.getY() != 0) {
            nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
        } else {
            nbt.remove("areaDimensionsY");
        }

        if (this.areaDimensions.getZ() != 0) {
            nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
        } else {
            nbt.remove("areaDimensionsZ");
        }

        if (this.areaPositionOffset.getX() != 0) {
            nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
        } else {
            nbt.remove("areaPositionOffsetX");
        }

        if (this.areaPositionOffset.getY() != 0) {
            nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
        } else {
            nbt.remove("areaPositionOffsetY");
        }

        if (this.areaPositionOffset.getZ() != 0) {
            nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
        } else {
            nbt.remove("areaPositionOffsetZ");
        }

        if (!this.bossIdentifier.isEmpty()) {
            nbt.putString("bossIdentifier", this.bossIdentifier);
        }

        nbt.putInt("bossSpawnPositionOffsetX", this.bossSpawnPositionOffset.getX());
        nbt.putInt("bossSpawnPositionOffsetY", this.bossSpawnPositionOffset.getY());
        nbt.putInt("bossSpawnPositionOffsetZ", this.bossSpawnPositionOffset.getZ());

        nbt.putDouble("bossSpawnOrientationPitch", this.bossSpawnOrientationPitch);
        nbt.putDouble("bossSpawnOrientationYaw", this.bossSpawnOrientationYaw);

        List<String> bossTriggeredBlocksKeys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
        nbt.putInt("bossTriggeredBlocksKeysSize", bossTriggeredBlocksKeys.size());
        for (int i = 0; i < bossTriggeredBlocksKeys.size(); i++) {
            String key = bossTriggeredBlocksKeys.get(i);
            nbt.putString("bossTriggeredBlocks_key_" + i, key);
            nbt.putInt("bossTriggeredBlocks_entry_X_" + i, this.bossTriggeredBlocks.get(key).getLeft().getX());
            nbt.putInt("bossTriggeredBlocks_entry_Y_" + i, this.bossTriggeredBlocks.get(key).getLeft().getY());
            nbt.putInt("bossTriggeredBlocks_entry_Z_" + i, this.bossTriggeredBlocks.get(key).getLeft().getZ());
            nbt.putBoolean("bossTriggeredBlocks_entry_resets_" + i, this.bossTriggeredBlocks.get(key).getRight());
        }

        nbt.put("EntityTypeCompound", this.entityTypeCompound);

        if (this.bossEntityUuid != null) {
            nbt.putUuid("bossEntityUuid", this.bossEntityUuid);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {

        this.showArea = nbt.getBoolean("showArea");

        if (nbt.contains("areaMinX") && nbt.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
            this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
            this.calculateAreaBox = true;
        }

        int i = MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48);
        int j = MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48);
        int k = MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48);
        this.areaDimensions = new Vec3i(i, j, k);

        int l = MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48);
        int m = MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48);
        int n = MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48);
        this.areaPositionOffset = new BlockPos(l, m, n);

        if (nbt.contains("bossIdentifier")) {
            this.bossIdentifier = nbt.getString("bossIdentifier");
        }

        this.bossSpawnPositionOffset = new BlockPos(
                MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetX"), -48, 48),
                MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetY"), -48, 48),
                MathHelper.clamp(nbt.getInt("bossSpawnPositionOffsetZ"), -48, 48)
        );

        this.bossSpawnOrientationPitch = nbt.getDouble("bossSpawnOrientationPitch");
        this.bossSpawnOrientationYaw = nbt.getDouble("bossSpawnOrientationYaw");

        this.bossTriggeredBlocks.clear();
        int bossTriggeredBlocksKeysSize = nbt.getInt("bossTriggeredBlocksKeysSize");
        for (int b = 0; b < bossTriggeredBlocksKeysSize; b++) {
            this.bossTriggeredBlocks.put(nbt.getString("bossTriggeredBlocks_key_" + b), new MutablePair<>(
                    new BlockPos(
                            MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_X_" + b), -48, 48),
                            MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_Y_" + b), -48, 48),
                            MathHelper.clamp(nbt.getInt("bossTriggeredBlocks_entry_Z_" + b), -48, 48)
                    ), nbt.getBoolean("bossTriggeredBlocks_entry_resets_" + b)));
        }

        if (nbt.contains("EntityTypeCompound", NbtElement.COMPOUND_TYPE)) {
            this.entityTypeCompound = nbt.getCompound("EntityTypeCompound");
        }

        if (nbt.containsUuid("bossEntityUuid")) {
            this.bossEntityUuid = nbt.getUuid("bossEntityUuid");
        }
    }

    public boolean openScreen(PlayerEntity player) {
        if (!player.isCreativeLevelTwoOp()) {
            return false;
        }
        if (player.getEntityWorld().isClient) {
            ((DuckPlayerEntityMixin) player).scriptblocks$openBossControllerBlockScreen(this);
        }
        return true;
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
        }
    }

    //region Boss Battle Logic
    private static void startBattle(BossControllerBlockEntity bC) {

        ScriptBlocksMod.info("startBattle");
        if (Identifier.isValid(bC.bossIdentifier)) {
            bC.boss = BossesRegistry.getBoss(new Identifier(bC.bossIdentifier));
        }

        if (bC.boss != null) {
            ScriptBlocksMod.info("bC.boss != null");
            bC.setEntityType(bC.boss.bossEntityTypeId());
            bC.currentPhaseId = 0;
            bC.currentPhase = bC.boss.phases()[0];
            if (spawnBossEntity(bC)) {
                ScriptBlocksMod.info("boss spawned");
                startPhase(bC);
            }
        }
    }

    private static void advancePhase(BossControllerBlockEntity bC) {
        ScriptBlocksMod.info("advancePhase");
        if ((bC.currentPhaseId + 1) < bC.boss.phases().length) {
            bC.phaseTimer = 0;
            endPhase(bC);
            bC.currentPhaseId++;
            bC.currentPhase = bC.boss.phases()[bC.currentPhaseId];
            startPhase(bC);
        } else {
            endBattle(bC);
        }
    }

    private static void endBattle(BossControllerBlockEntity bC) {
        endPhase(bC);
        bC.currentPhase = null;
        if (bC.boss.discardEntityAtEnd() && bC.bossEntityUuid != null && bC.world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
            if (entity != null) {
                entity.discard();
            }
            bC.bossEntityUuid = null;
        }
    }

    private static void startPhase(BossControllerBlockEntity bC) {
        Boss.Phase phase = bC.currentPhase;

        ScriptBlocksMod.info("startPhase: " + phase.animationsIdentifierString());
        bC.entityAttributeModifiers = getEntityAttributeModifiers(bC.currentPhase);

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
            if (entity instanceof BossEntity) {
                ((BossEntity) entity).setAnimationIdentifierString(bC.currentPhase.animationsIdentifierString());
                ((BossEntity) entity).setModelIdentifierString(bC.currentPhase.modelIdentifierString());
                ((BossEntity) entity).setTextureIdentifierString(bC.currentPhase.textureIdentifierString());
                ((BossEntity) entity).setBossHealthThreshold(bC.currentPhase.bossHealthThreshold());
            }
            if (!bC.entityAttributeModifiers.isEmpty() && entity instanceof LivingEntity) {
                AttributeContainer attributeContainer = ((LivingEntity) entity).getAttributes();
                bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
                    EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance((EntityAttribute) attribute);
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.addPersistentModifier((EntityAttributeModifier) attributeModifier);
                    }
                });
            }
        }
    }

    private static void endPhase(BossControllerBlockEntity bC) {
        Boss.Phase phase = bC.currentPhase;

        ScriptBlocksMod.info("endPhase");
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
        if (bC.bossEntityUuid != null && bC.world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(bC.bossEntityUuid);
            if (!bC.entityAttributeModifiers.isEmpty() && entity instanceof LivingEntity) {
                AttributeContainer attributeContainer = ((LivingEntity) entity).getAttributes();
                bC.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
                    EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance((EntityAttribute) attribute);
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.removeModifier(attributeModifier.getId());
                    }
                });
            }
        }
    }

    public static void bossReachedHealthThreshold(BossControllerBlockEntity bC) {
        advancePhase(bC);
    }

    private static Multimap<EntityAttribute, EntityAttributeModifier> getEntityAttributeModifiers(Boss.Phase phase) {
        Multimap<EntityAttribute, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

        for (Boss.Phase.EntityAttributeModifier entityAttributeModifier : phase.entityAttributeModifiers()) {
            Optional<EntityAttribute> optional = Registries.ATTRIBUTE
                    .getOrEmpty(Identifier.tryParse(entityAttributeModifier.identifier()));
            if (optional.isPresent()) {
                EntityAttribute key = optional.get();
                entityAttributeModifiers.put(key, new EntityAttributeModifier(entityAttributeModifier.name(), entityAttributeModifier.value(), EntityAttributeModifier.Operation.fromId(entityAttributeModifier.operation())));
            }
        }
        return entityAttributeModifiers;
    }

    private static boolean spawnBossEntity(BossControllerBlockEntity bC) {
        if (bC.world instanceof ServerWorld serverWorld) {
            Optional<EntityType<?>> optional = EntityType.fromNbt(bC.entityTypeCompound);
            if (optional.isEmpty()) {
                ScriptBlocksMod.info("optional.isEmpty()");
                return false;
            }
            double d = (double) bC.pos.getX() + bC.bossSpawnPositionOffset.getX() + 0.5;
            double e = (double) bC.pos.getY() + bC.bossSpawnPositionOffset.getY();
            double f = (double) bC.pos.getZ() + bC.bossSpawnPositionOffset.getZ() + 0.5;
            if (!serverWorld.isSpaceEmpty(optional.get().createSimpleBoundingBox(d, e, f))) {
                ScriptBlocksMod.info("not enough space for spawning");
                return false;
            }
            BlockPos blockPos = BlockPos.ofFloored(d, e, f);
            Entity entity2 = EntityType.loadEntityWithPassengers(bC.entityTypeCompound, bC.world, entity -> {
                entity.refreshPositionAndAngles(d, e, f, entity.getYaw(), entity.getPitch());
                return entity;
            });
            if (entity2 == null) {
                ScriptBlocksMod.info("entity2 == null");
                return false;
            }
            entity2.setBodyYaw((float) bC.bossSpawnOrientationYaw);
            entity2.setHeadYaw((float) bC.bossSpawnOrientationYaw);
            entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), (float) bC.bossSpawnOrientationYaw, (float) bC.bossSpawnOrientationPitch);
            if (entity2 instanceof MobEntity) {
                if (bC.entityTypeCompound.contains("id", NbtElement.STRING_TYPE)) {
                    NbtCompound entityNbt = new NbtCompound();
                    ((MobEntity) entity2).initialize(serverWorld, serverWorld.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null, entityNbt);
                }
            }
            if (entity2 instanceof BossEntity) {
                ((BossEntity) entity2).setAnimationIdentifierString(bC.currentPhase.animationsIdentifierString());
                ((BossEntity) entity2).setBossControllerBlockPos(bC.pos);
                ((BossEntity) entity2).setModelIdentifierString(bC.currentPhase.modelIdentifierString());
                ((BossEntity) entity2).setTextureIdentifierString(bC.currentPhase.textureIdentifierString());
            }
            if (!serverWorld.spawnNewEntityAndPassengers(entity2)) {
                ScriptBlocksMod.info("spawnNewEntityAndPassengers not successful");
                return false;
            }
            serverWorld.syncWorldEvent(WorldEvents.SPAWNER_SPAWNS_MOB, bC.pos, 0);
            serverWorld.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);
            if (entity2 instanceof LivingEntity) {

                bC.bossEntityUuid = ((LivingEntity) entity2).getUuid();

//                if (!this.entityAttributeModifiers.isEmpty()) {
//                    AttributeContainer attributeContainer = ((LivingEntity)entity2).getAttributes();
//                    this.entityAttributeModifiers.forEach((attribute, attributeModifier) -> {
//                        EntityAttributeInstance entityAttributeInstance = attributeContainer.getCustomInstance((EntityAttribute) attribute);
//                        if (entityAttributeInstance != null) {
//                            entityAttributeInstance.removeModifier(attributeModifier.getId());
//                            entityAttributeInstance.addPersistentModifier((EntityAttributeModifier) attributeModifier);
//                        }
//                        if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
//                            ((LivingEntity)entity2).setHealth((float) ((LivingEntity)entity2).getAttributes().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
//                        }
//                    });
//                }
                if (entity2 instanceof MobEntity mobEntity) {
                    mobEntity.setPersistent();
                }
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
        if (this.bossEntityUuid != null && this.world instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(this.bossEntityUuid);
            if (entity != null) {
                entity.discard();
            }
            this.bossEntityUuid = null;
        }
    }

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
            if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
                BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

                this.bossSpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.bossSpawnPositionOffset, blockRotation);
                this.bossSpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.bossSpawnOrientationYaw, blockRotation);

                List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
                for (String key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
                    this.bossTriggeredBlocks.put(key, oldBlockPos);
                }

                MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
                this.areaPositionOffset = offsetArea.getLeft();
                this.areaDimensions = offsetArea.getRight();

                this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
            }
            if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
                this.bossSpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.bossSpawnPositionOffset, BlockMirror.FRONT_BACK);
                this.bossSpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.bossSpawnOrientationYaw, BlockMirror.FRONT_BACK);

                List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
                for (String key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
                    this.bossTriggeredBlocks.put(key, oldBlockPos);
                }

                MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
                this.areaPositionOffset = offsetArea.getLeft();
                this.areaDimensions = offsetArea.getRight();

                this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
            }
            if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
                this.bossSpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.bossSpawnPositionOffset, BlockMirror.LEFT_RIGHT);
                this.bossSpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.bossSpawnOrientationYaw, BlockMirror.LEFT_RIGHT);


                List<String> keys = new ArrayList<>(this.bossTriggeredBlocks.keySet());
                for (String key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.bossTriggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
                    this.bossTriggeredBlocks.put(key, oldBlockPos);
                }

                MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
                this.areaPositionOffset = offsetArea.getLeft();
                this.areaDimensions = offsetArea.getRight();

                this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
            }
        }
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

    //region Getter & Setter
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

    public BlockPos getBossSpawnPositionOffset() {
        return this.bossSpawnPositionOffset;
    }

    // TODO check if input is valid
    public boolean setBossSpawnPositionOffset(BlockPos bossSpawnPositionOffset) {
        this.bossSpawnPositionOffset = bossSpawnPositionOffset;
        return true;
    }

    public double getBossSpawnOrientationPitch() {
        return bossSpawnOrientationPitch;
    }

    // TODO check if input is valid
    public boolean setBossSpawnPositionPitch(double bossSpawnPositionPitch) {
        this.bossSpawnOrientationPitch = bossSpawnPositionPitch;
        return true;
    }

    public double getBossSpawnOrientationYaw() {
        return bossSpawnOrientationYaw;
    }

    // TODO check if input is valid
    public boolean setBossSpawnPositionYaw(double bossSpawnPositionYaw) {
        this.bossSpawnOrientationYaw = bossSpawnPositionYaw;
        return true;
    }


    public String getBossIdentifier() {
        return this.bossIdentifier;
    }

    public boolean setBossIdentifier(String newBossIdentifier) {
        Boss boss = null;
        if (Identifier.isValid(newBossIdentifier)) {
            boss = BossesRegistry.getBoss(new Identifier(newBossIdentifier));
        }
        if (newBossIdentifier.isEmpty() || boss != null) {
            this.bossIdentifier = newBossIdentifier;
            return true;
        }
        return false;
    }

    public HashMap<String, MutablePair<BlockPos, Boolean>> getBossTriggeredBlocks() {
        return this.bossTriggeredBlocks;
    }

    // TODO check if input is valid
    public boolean setBossTriggeredBlocks(HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocks) {
        this.bossTriggeredBlocks = bossTriggeredBlocks;
        return true;
    }
    //endregion Getter & Setter

}
