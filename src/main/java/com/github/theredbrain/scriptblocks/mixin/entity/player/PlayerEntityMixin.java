package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	@Unique
	private static final TrackedData<BlockPos> CURRENT_HOUSING_BLOCK_POS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

	@Unique
	private static final TrackedData<BlockPos> CURRENT_LOCATION_ACCESS_BLOCK_POS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

	@Unique
	private static final TrackedData<String> CURRENT_LOCATION_ACCESS_DIMENSION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("RETURN"))
	protected void scriptblocks$initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(CURRENT_HOUSING_BLOCK_POS, BlockPos.ORIGIN);
		builder.add(CURRENT_LOCATION_ACCESS_BLOCK_POS, BlockPos.ORIGIN);
		builder.add(CURRENT_LOCATION_ACCESS_DIMENSION, "");
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void scriptblocks$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

		if (nbt.contains("currentHousingBlockPositionX", NbtElement.INT_TYPE) && nbt.contains("currentHousingBlockPositionY", NbtElement.INT_TYPE) && nbt.contains("currentHousingBlockPositionZ", NbtElement.INT_TYPE)) {
			this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, new BlockPos(
					nbt.getInt("currentHousingBlockPositionX"),
					nbt.getInt("currentHousingBlockPositionY"),
					nbt.getInt("currentHousingBlockPositionZ")
			));
		}

		if (nbt.contains("currentLocationAccessBlockPositionX", NbtElement.INT_TYPE) && nbt.contains("currentLocationAccessBlockPositionY", NbtElement.INT_TYPE) && nbt.contains("currentLocationAccessBlockPositionZ", NbtElement.INT_TYPE)) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, new BlockPos(
					nbt.getInt("currentLocationAccessBlockPositionX"),
					nbt.getInt("currentLocationAccessBlockPositionY"),
					nbt.getInt("currentLocationAccessBlockPositionZ")
			));
		}

		if (nbt.contains("currentLocationAccessDimension", NbtElement.STRING_TYPE)) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, nbt.getString("currentLocationAccessDimension"));
		}

	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	public void scriptblocks$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {

		BlockPos currentHousingBlockPosition = this.dataTracker.get(CURRENT_HOUSING_BLOCK_POS);
		if (currentHousingBlockPosition != BlockPos.ORIGIN) {
			nbt.putInt("currentHousingBlockPositionX", currentHousingBlockPosition.getX());
			nbt.putInt("currentHousingBlockPositionY", currentHousingBlockPosition.getY());
			nbt.putInt("currentHousingBlockPositionZ", currentHousingBlockPosition.getZ());
		} else {
			nbt.remove("currentHousingBlockPositionX");
			nbt.remove("currentHousingBlockPositionY");
			nbt.remove("currentHousingBlockPositionZ");
		}

		BlockPos currentLocationAccessBlockPosition = this.dataTracker.get(CURRENT_LOCATION_ACCESS_BLOCK_POS);
		if (currentLocationAccessBlockPosition != BlockPos.ORIGIN) {
			nbt.putInt("currentLocationAccessBlockPositionX", currentLocationAccessBlockPosition.getX());
			nbt.putInt("currentLocationAccessBlockPositionY", currentLocationAccessBlockPosition.getY());
			nbt.putInt("currentLocationAccessBlockPositionZ", currentLocationAccessBlockPosition.getZ());
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

	}

	@Override
	@Nullable
	public BlockPos scriptblocks$getCurrentHousingBlockPosition() {
		return this.dataTracker.get(CURRENT_HOUSING_BLOCK_POS);
	}

	@Override
	public void scriptblocks$setCurrentHousingBlockPosition(@Nullable BlockPos currentHousingBlockPosition) {
		this.dataTracker.set(CURRENT_HOUSING_BLOCK_POS, currentHousingBlockPosition);
	}

	@Override
	@Nullable
	public MutablePair<String, BlockPos> scriptblocks$getLocationAccessPosition() {
		BlockPos blockPos = this.dataTracker.get(CURRENT_LOCATION_ACCESS_BLOCK_POS);
		String string = this.dataTracker.get(CURRENT_LOCATION_ACCESS_DIMENSION);
		if (string.isEmpty()) {
			return null;
		} else {
			return new MutablePair<>(string, blockPos);
		}
	}

	@Override
	public void scriptblocks$setLocationAccessPosition(@Nullable MutablePair<String, BlockPos> locationAccessPosition) {
		if (locationAccessPosition == null) {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, BlockPos.ORIGIN);
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, "");
		} else {
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_BLOCK_POS, locationAccessPosition.right);
			this.dataTracker.set(CURRENT_LOCATION_ACCESS_DIMENSION, locationAccessPosition.left);
		}
	}

	@Override
	public void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity) {
	}

	@Override
	public void scriptblocks$openHousingScreen() {
	}

	@Override
	public void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
	}

	@Override
	public void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
	}

	@Override
	public void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
	}

	@Override
	public void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
	}

	@Override
	public void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity) {
	}

	@Override
	public void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
	}

	@Override
	public void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock) {
	}

	@Override
	public void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
	}

	@Override
	public void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
	}

	@Override
	public void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
	}

	@Override
	public void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
	}

	@Override
	public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
	}

	@Override
	public void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
	}
}
