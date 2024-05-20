package com.github.theredbrain.scriptblocks.mixin.entity.mob;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Unique
    @Nullable
    private BlockPos scriptblocks$boundSpawnerBlockPos;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void scriptblocks$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.scriptblocks$boundSpawnerBlockPos != null) {
            nbt.putInt("scriptblocks$boundSpawnerBlockPosX", this.scriptblocks$boundSpawnerBlockPos.getX());
            nbt.putInt("scriptblocks$boundSpawnerBlockPosY", this.scriptblocks$boundSpawnerBlockPos.getY());
            nbt.putInt("scriptblocks$boundSpawnerBlockPosZ", this.scriptblocks$boundSpawnerBlockPos.getZ());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void scriptblocks$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("scriptblocks$boundSpawnerBlockPosX") && nbt.contains("scriptblocks$boundSpawnerBlockPosY") && nbt.contains("scriptblocks$boundSpawnerBlockPosZ")) {
            this.scriptblocks$boundSpawnerBlockPos = new BlockPos(
                    nbt.getInt("scriptblocks$boundSpawnerBlockPosX"),
                    nbt.getInt("scriptblocks$boundSpawnerBlockPosY"),
                    nbt.getInt("scriptblocks$boundSpawnerBlockPosZ")
            );
        }
    }

    @Inject(method = "initialize", at = @At("RETURN"))
    public void scriptblocks$initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
        if (entityNbt != null && entityNbt.contains("scriptblocks$boundSpawnerBlockPosX") && entityNbt.contains("scriptblocks$boundSpawnerBlockPosY") && entityNbt.contains("scriptblocks$boundSpawnerBlockPosZ")) {
            this.scriptblocks$boundSpawnerBlockPos = new BlockPos(
                    entityNbt.getInt("scriptblocks$boundSpawnerBlockPosX"),
                    entityNbt.getInt("scriptblocks$boundSpawnerBlockPosY"),
                    entityNbt.getInt("scriptblocks$boundSpawnerBlockPosZ")
            );
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        if (this.getWorld() instanceof ServerWorld serverWorld && this.scriptblocks$boundSpawnerBlockPos != null) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(this.scriptblocks$boundSpawnerBlockPos);
            if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
                triggeredSpawnerBlockEntity.onBoundEntityKilled();
            }
        }
    }
}
