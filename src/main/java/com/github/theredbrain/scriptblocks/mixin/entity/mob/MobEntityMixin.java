package com.github.theredbrain.scriptblocks.mixin.entity.mob;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.mob.DuckMobEntityMixin;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MobEntity.class)
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
	private static final TrackedData<Float> BOSS_HEALTH_THRESHOLD = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.FLOAT);

	@Unique
	private static final TrackedData<Integer> BOSS_PHASE = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.INTEGER);

	@Unique
	private static final TrackedData<BlockPos> CONTROLLER_BLOCK_POS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

	@Unique
	private static final TrackedData<BlockPos> USE_RELAY_BLOCK_POS = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

	protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("RETURN"))
	protected void overhauleddamage$initDataTracker(CallbackInfo ci) {
		this.dataTracker.startTracking(BOSS_HEALTH_THRESHOLD, BOSS_HEALTH_THRESHOLD_DEFAULT);
		this.dataTracker.startTracking(BOSS_PHASE, BOSS_PHASE_DEFAULT);
		this.dataTracker.startTracking(CONTROLLER_BLOCK_POS, CONTROLLER_BLOCK_POS_DEFAULT);
		this.dataTracker.startTracking(USE_RELAY_BLOCK_POS, USE_RELAY_BLOCK_POS_DEFAULT);

	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void scriptblocks$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {

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
	public void scriptblocks$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

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
	protected void scriptblocks$interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

		BlockPos useRelayBlockPos = this.scriptblocks$getUseRelayBlockPos();
		if (!Objects.equals(useRelayBlockPos, USE_RELAY_BLOCK_POS_DEFAULT) && player instanceof ServerPlayerEntity serverPlayerEntity) {
			World world = player.getWorld();
			BlockHitResult blockHitResult = new BlockHitResult(player.getPos(), Direction.UP, useRelayBlockPos, false);
			ItemStack itemStack = player.getStackInHand(hand);
			serverPlayerEntity.interactionManager.interactBlock(serverPlayerEntity, world, itemStack, hand, blockHitResult);
			cir.setReturnValue(ActionResult.SUCCESS);
			cir.cancel();
		}
	}

	@Override
	public void setHealth(float health) {
		float healthThreshold = this.scriptblocks$getBossHealthThreshold();
		if (healthThreshold > BOSS_HEALTH_THRESHOLD_DEFAULT && health <= healthThreshold) {
			health = healthThreshold;
			BlockPos controllerBlockPos = this.scriptblocks$getControllerBlockPos();
			if (this.getWorld() instanceof ServerWorld serverWorld && !Objects.equals(controllerBlockPos, CONTROLLER_BLOCK_POS_DEFAULT)) {
				BlockEntity blockEntity = serverWorld.getBlockEntity(controllerBlockPos);
				if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
					BossControllerBlockEntity.bossReachedHealthThreshold(bossControllerBlockEntity);
				}
			}
		}
		super.setHealth(health);
	}

	@Override
	public void onDeath(DamageSource damageSource) {
		super.onDeath(damageSource);
		BlockPos controllerBlockPos = this.scriptblocks$getControllerBlockPos();
		if (this.getWorld() instanceof ServerWorld serverWorld && !Objects.equals(controllerBlockPos, CONTROLLER_BLOCK_POS_DEFAULT)) {
			BlockEntity blockEntity = serverWorld.getBlockEntity(controllerBlockPos);
			if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
				triggeredSpawnerBlockEntity.onBoundEntityKilled();
			}
		}
	}

	@Override
	public float scriptblocks$getBossHealthThreshold() {
		return this.dataTracker.get(BOSS_HEALTH_THRESHOLD);
	}

	@Override
	public void scriptblocks$setBossHealthThreshold(float bossHealthThreshold) {
		this.dataTracker.set(BOSS_HEALTH_THRESHOLD, bossHealthThreshold);
	}

	@Override
	public int scriptblocks$getBossPhase() {
		return this.dataTracker.get(BOSS_PHASE);
	}

	@Override
	public void scriptblocks$setBossPhase(int bossPhase) {
		this.dataTracker.set(BOSS_PHASE, bossPhase);
	}

	@Override
	public BlockPos scriptblocks$getControllerBlockPos() {
		return this.dataTracker.get(CONTROLLER_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setControllerBlockPos(BlockPos controllerBlockPos) {
		this.dataTracker.set(CONTROLLER_BLOCK_POS, controllerBlockPos);
	}

	@Override
	public BlockPos scriptblocks$getUseRelayBlockPos() {
		return this.dataTracker.get(USE_RELAY_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setUseRelayBlockPos(BlockPos useRelayBlockPos) {
		this.dataTracker.set(USE_RELAY_BLOCK_POS, useRelayBlockPos);
	}

}
