package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	@Shadow
	public abstract boolean isCreative();

	@Shadow
	public abstract boolean isSpectator();

	@Unique
	private static final TrackedData<Optional<BlockPos>> CURRENT_HOUSING_BLOCK_POS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

	@Unique
	private static final TrackedData<Optional<BlockPos>> CURRENT_LOCATION_ACCESS_BLOCK_POS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

	@Unique
	private static final TrackedData<String> CURRENT_LOCATION_ACCESS_DIMENSION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

	@Unique
	private static final TrackedData<Optional<BlockPos>> CURRENT_PVP_CONTROLLER_BLOCK_POS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(method = "isBlockBreakingRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameMode;isBlockBreakingRestricted()Z"))
	public boolean scriptblocks$wrap_isBlockBreakingRestricted(GameMode instance, Operation<Boolean> original) {
		return original.call(instance) || (!this.isCreative() && this.hasStatusEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT));
	}

	@Inject(method = "initDataTracker", at = @At("RETURN"))
	protected void scriptblocks$initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(CURRENT_HOUSING_BLOCK_POS, Optional.empty());
		builder.add(CURRENT_LOCATION_ACCESS_BLOCK_POS, Optional.empty());
		builder.add(CURRENT_LOCATION_ACCESS_DIMENSION, "");
		builder.add(CURRENT_PVP_CONTROLLER_BLOCK_POS, Optional.empty());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void scriptblocks$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

		if (nbt.contains("currentHousingBlockPositionX", NbtElement.INT_TYPE) && nbt.contains("currentHousingBlockPositionY", NbtElement.INT_TYPE) && nbt.contains("currentHousingBlockPositionZ", NbtElement.INT_TYPE)) {
			this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, Optional.of(new BlockPos(
					nbt.getInt("currentHousingBlockPositionX"),
					nbt.getInt("currentHousingBlockPositionY"),
					nbt.getInt("currentHousingBlockPositionZ")
			)));
		} else {
			this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, Optional.empty());
		}

		if (nbt.contains("currentLocationAccessBlockPositionX", NbtElement.INT_TYPE) && nbt.contains("currentLocationAccessBlockPositionY", NbtElement.INT_TYPE) && nbt.contains("currentLocationAccessBlockPositionZ", NbtElement.INT_TYPE)) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, Optional.of(new BlockPos(
					nbt.getInt("currentLocationAccessBlockPositionX"),
					nbt.getInt("currentLocationAccessBlockPositionY"),
					nbt.getInt("currentLocationAccessBlockPositionZ")
			)));
		} else {
			this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, Optional.empty());
		}

		if (nbt.contains("currentLocationAccessDimension", NbtElement.STRING_TYPE)) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, nbt.getString("currentLocationAccessDimension"));
		}

		if (nbt.contains("currentPVPControllerBlockPositionX", NbtElement.INT_TYPE) && nbt.contains("currentPVPControllerBlockPositionY", NbtElement.INT_TYPE) && nbt.contains("currentPVPControllerBlockPositionZ", NbtElement.INT_TYPE)) {
			this.dataTracker.set(CURRENT_PVP_CONTROLLER_BLOCK_POS, Optional.of(new BlockPos(
					nbt.getInt("currentPVPControllerBlockPositionX"),
					nbt.getInt("currentPVPControllerBlockPositionY"),
					nbt.getInt("currentPVPControllerBlockPositionZ")
			)));
		} else {
			this.dataTracker.set(CURRENT_PVP_CONTROLLER_BLOCK_POS, Optional.empty());
		}

	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void scriptblocks$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {

		Optional<BlockPos> optionalCurrentHousingBlockPosition = this.dataTracker.get(CURRENT_HOUSING_BLOCK_POS);
		if (optionalCurrentHousingBlockPosition.isPresent()) {
			nbt.putInt("currentHousingBlockPositionX", optionalCurrentHousingBlockPosition.get().getX());
			nbt.putInt("currentHousingBlockPositionY", optionalCurrentHousingBlockPosition.get().getY());
			nbt.putInt("currentHousingBlockPositionZ", optionalCurrentHousingBlockPosition.get().getZ());
		} else {
			nbt.remove("currentHousingBlockPositionX");
			nbt.remove("currentHousingBlockPositionY");
			nbt.remove("currentHousingBlockPositionZ");
		}

		Optional<BlockPos> optionalCurrentLocationAccessBlockPosition = this.dataTracker.get(CURRENT_LOCATION_ACCESS_BLOCK_POS);
		if (optionalCurrentLocationAccessBlockPosition.isPresent()) {
			nbt.putInt("currentLocationAccessBlockPositionX", optionalCurrentLocationAccessBlockPosition.get().getX());
			nbt.putInt("currentLocationAccessBlockPositionY", optionalCurrentLocationAccessBlockPosition.get().getY());
			nbt.putInt("currentLocationAccessBlockPositionZ", optionalCurrentLocationAccessBlockPosition.get().getZ());
		} else {
			nbt.remove("currentLocationAccessBlockPositionX");
			nbt.remove("currentLocationAccessBlockPositionY");
			nbt.remove("currentLocationAccessBlockPositionZ");
		}

		String currentLocationAccessDimension = this.dataTracker.get(CURRENT_LOCATION_ACCESS_DIMENSION);
		if (!currentLocationAccessDimension.isEmpty()) {
			nbt.putString("currentLocationAccessDimension", currentLocationAccessDimension);
		} else {
			nbt.remove("currentLocationAccessDimension");
		}

		Optional<BlockPos> optionalCurrentPVPControllerBlockPosition = this.dataTracker.get(CURRENT_PVP_CONTROLLER_BLOCK_POS);
		if (optionalCurrentPVPControllerBlockPosition.isPresent()) {
			nbt.putInt("currentPVPControllerBlockPositionX", optionalCurrentPVPControllerBlockPosition.get().getX());
			nbt.putInt("currentPVPControllerBlockPositionY", optionalCurrentPVPControllerBlockPosition.get().getY());
			nbt.putInt("currentPVPControllerBlockPositionZ", optionalCurrentPVPControllerBlockPosition.get().getZ());
		} else {
			nbt.remove("currentPVPControllerBlockPositionX");
			nbt.remove("currentPVPControllerBlockPositionY");
			nbt.remove("currentPVPControllerBlockPositionZ");
		}

	}

	@WrapMethod(method = "canModifyBlocks")
	public boolean scriptblocks$wrap_canModifyBlocks(Operation<Boolean> original) {
		return original.call() && !(!(this.isCreative() || this.isSpectator()) && this.hasStatusEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasStatusEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

	@WrapMethod(method = "canPlaceOn")
	public boolean scriptblocks$wrap_canPlaceOn(BlockPos pos, Direction facing, ItemStack stack, Operation<Boolean> original) {
		return original.call(pos, facing, stack) && !(!(this.isCreative() || this.isSpectator()) && this.hasStatusEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasStatusEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

	@Override
	public Optional<BlockPos> scriptblocks$getCurrentHousingBlockPosition() {
		return this.dataTracker.get(CURRENT_HOUSING_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setCurrentHousingBlockPosition(Optional<BlockPos> currentHousingBlockPosition) {
		this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, currentHousingBlockPosition);
	}

	@Override
	@Nullable
	public MutablePair<String, BlockPos> scriptblocks$getLocationAccessPosition() {
		Optional<BlockPos> optionalBlockPos = this.dataTracker.get(CURRENT_LOCATION_ACCESS_BLOCK_POS);
		String string = this.dataTracker.get(CURRENT_LOCATION_ACCESS_DIMENSION);
		if (string.isEmpty() || optionalBlockPos.isEmpty()) {
			return null;
		} else {
			return new MutablePair<>(string, optionalBlockPos.get());
		}
	}

	@Override
	public void scriptblocks$setLocationAccessPosition(@Nullable MutablePair<String, BlockPos> locationAccessPosition) {
		if (locationAccessPosition == null) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, Optional.empty());
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, "");
		} else {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, Optional.of(locationAccessPosition.right));
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, locationAccessPosition.left);
		}
	}

	@Override
	public Optional<BlockPos> scriptblocks$getCurrentPVPControllerBlockPosition() {
		return this.dataTracker.get(CURRENT_PVP_CONTROLLER_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setCurrentPVPControllerBlockPosition(Optional<BlockPos> currentPVPControllerBlockPosition) {
		this.dataTracker.set(CURRENT_PVP_CONTROLLER_BLOCK_POS, currentPVPControllerBlockPosition);
	}

}
