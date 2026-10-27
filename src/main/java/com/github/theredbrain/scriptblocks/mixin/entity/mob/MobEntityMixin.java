package com.github.theredbrain.scriptblocks.mixin.entity.mob;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity implements DuckMobEntityMixin {

	@Unique
	private static final float BOSS_HEALTH_THRESHOLD_DEFAULT = -1.0f;

	@Unique
	private static final int BOSS_PHASE_DEFAULT = -1;

	@Unique
	private static final BlockPos CONTROLLER_BLOCK_POS_DEFAULT = new BlockPos(0, -100, 0);

	@Unique
	private static final BlockPos USE_RELAY_BLOCK_POS_DEFAULT = new BlockPos(0, -100, 0);

	@Unique
	private static final EntityDataAccessor<Float> BOSS_HEALTH_THRESHOLD = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.FLOAT);

	@Unique
	private static final EntityDataAccessor<Integer> BOSS_PHASE = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.INT);

	@Unique
	private static final EntityDataAccessor<BlockPos> CONTROLLER_BLOCK_POS = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BLOCK_POS);

	@Unique
	private static final EntityDataAccessor<BlockPos> USE_RELAY_BLOCK_POS = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BLOCK_POS);

	protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("RETURN"))
	protected void overhauleddamage$initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(BOSS_HEALTH_THRESHOLD, BOSS_HEALTH_THRESHOLD_DEFAULT);
		builder.define(BOSS_PHASE, BOSS_PHASE_DEFAULT);
		builder.define(CONTROLLER_BLOCK_POS, CONTROLLER_BLOCK_POS_DEFAULT);
		builder.define(USE_RELAY_BLOCK_POS, USE_RELAY_BLOCK_POS_DEFAULT);

	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void scriptblocks$writeCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {

		float bossHealthThreshold = this.scriptblocks$getBossHealthThreshold();
		if (bossHealthThreshold != BOSS_HEALTH_THRESHOLD_DEFAULT) {
			nbt.putFloat("boss_health_threshold", bossHealthThreshold);
		} else {
			nbt.remove("boss_health_threshold");
		}

		int bossPhase = this.scriptblocks$getBossPhase();
		if (bossPhase != BOSS_PHASE_DEFAULT) {
			nbt.putInt("boss_phase", bossPhase);
		} else {
			nbt.remove("boss_phase");
		}

		BlockPos controllerBlockPos = this.scriptblocks$getControllerBlockPos();
		if (!controllerBlockPos.equals(CONTROLLER_BLOCK_POS_DEFAULT)) {
			nbt.putInt("controller_block_pos_x", controllerBlockPos.getX());
			nbt.putInt("controller_block_pos_y", controllerBlockPos.getY());
			nbt.putInt("controller_block_pos_z", controllerBlockPos.getZ());
		} else {
			nbt.remove("controller_block_pos_x");
			nbt.remove("controller_block_pos_y");
			nbt.remove("controller_block_pos_z");
		}

		BlockPos useRelayBlockPos = this.scriptblocks$getUseRelayBlockPos();
		if (!useRelayBlockPos.equals(USE_RELAY_BLOCK_POS_DEFAULT)) {
			nbt.putInt("use_relay_block_pos_x", useRelayBlockPos.getX());
			nbt.putInt("use_relay_block_pos_y", useRelayBlockPos.getY());
			nbt.putInt("use_relay_block_pos_z", useRelayBlockPos.getZ());
		} else {
			nbt.remove("use_relay_block_pos_x");
			nbt.remove("use_relay_block_pos_y");
			nbt.remove("use_relay_block_pos_z");
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void scriptblocks$readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {

		if (nbt.contains("boss_health_threshold")) {
			this.scriptblocks$setBossHealthThreshold(nbt.getFloat("boss_health_threshold"));
		}

		if (nbt.contains("boss_phase")) {
			this.scriptblocks$setBossPhase(nbt.getInt("boss_phase"));
		}

		if (nbt.contains("controller_block_pos_x") || nbt.contains("controller_block_pos_y") || nbt.contains("controller_block_pos_z")) {
			this.scriptblocks$setControllerBlockPos(new BlockPos(
					nbt.getInt("controller_block_pos_x"),
					nbt.getInt("controller_block_pos_y"),
					nbt.getInt("controller_block_pos_z")
			));
		}

		if (nbt.contains("use_relay_block_pos_x") || nbt.contains("use_relay_block_pos_y") || nbt.contains("use_relay_block_pos_z")) {
			this.scriptblocks$setUseRelayBlockPos(new BlockPos(
					nbt.getInt("use_relay_block_pos_x"),
					nbt.getInt("use_relay_block_pos_y"),
					nbt.getInt("use_relay_block_pos_z")
			));
		}
	}

	@Inject(method = "interactMob", at = @At("TAIL"), cancellable = true)
	protected void scriptblocks$interactMob(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {

		BlockPos useRelayBlockPos = this.scriptblocks$getUseRelayBlockPos();
		if (!Objects.equals(useRelayBlockPos, USE_RELAY_BLOCK_POS_DEFAULT)) {
			if (player instanceof ServerPlayer serverPlayerEntity) {
				Level world = player.getWorld();
				BlockHitResult blockHitResult = new BlockHitResult(player.getPos(), Direction.UP, useRelayBlockPos, false);
				ItemStack itemStack = player.getItemInHand(hand);
				serverPlayerEntity.gameMode.useItemOn(serverPlayerEntity, world, itemStack, hand, blockHitResult);
				cir.setReturnValue(InteractionResult.CONSUME);
				cir.cancel();
			} else {
				cir.setReturnValue(InteractionResult.SUCCESS);
				cir.cancel();
			}
		}
	}

	@Override
	public void setHealth(float health) {
		float healthThreshold = this.scriptblocks$getBossHealthThreshold();
		if (healthThreshold > BOSS_HEALTH_THRESHOLD_DEFAULT && health <= healthThreshold) {
			health = healthThreshold;
			BlockPos controllerBlockPos = this.scriptblocks$getControllerBlockPos();
			if (this.getWorld() instanceof ServerLevel serverWorld && !Objects.equals(controllerBlockPos, CONTROLLER_BLOCK_POS_DEFAULT)) {
				BlockEntity blockEntity = serverWorld.getBlockEntity(controllerBlockPos);
				if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
					BossControllerBlockEntity.bossReachedHealthThreshold(bossControllerBlockEntity, ((Mob) (Object) this));
				}
			}
		}
		super.setHealth(health);
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
		BlockPos controllerBlockPos = this.scriptblocks$getControllerBlockPos();
		if (this.getWorld() instanceof ServerLevel serverWorld && !Objects.equals(controllerBlockPos, CONTROLLER_BLOCK_POS_DEFAULT)) {
			BlockEntity blockEntity = serverWorld.getBlockEntity(controllerBlockPos);
			if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
				triggeredSpawnerBlockEntity.onBoundEntityKilled();
			}
		}
	}

	@Override
	public float scriptblocks$getBossHealthThreshold() {
		return this.entityData.get(BOSS_HEALTH_THRESHOLD);
	}

	@Override
	public void scriptblocks$setBossHealthThreshold(float bossHealthThreshold) {
		this.entityData.set(BOSS_HEALTH_THRESHOLD, bossHealthThreshold);
	}

	@Override
	public int scriptblocks$getBossPhase() {
		return this.entityData.get(BOSS_PHASE);
	}

	@Override
	public void scriptblocks$setBossPhase(int bossPhase) {
		this.entityData.set(BOSS_PHASE, bossPhase);
	}

	@Override
	public BlockPos scriptblocks$getControllerBlockPos() {
		return this.entityData.get(CONTROLLER_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setControllerBlockPos(BlockPos controllerBlockPos) {
		this.entityData.set(CONTROLLER_BLOCK_POS, controllerBlockPos);
	}

	@Override
	public BlockPos scriptblocks$getUseRelayBlockPos() {
		return this.entityData.get(USE_RELAY_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setUseRelayBlockPos(BlockPos useRelayBlockPos) {
		this.entityData.set(USE_RELAY_BLOCK_POS, useRelayBlockPos);
	}

}
