package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
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
		return original.call(instance) || (!this.isCreative() && this.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT));
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
		return original.call() && !(!(this.isCreative() || this.isSpectator()) && this.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT) && !this.hasStatusEffect(ScriptBlocks.BUILDING_MODE));
	}

	@WrapMethod(method = "canPlaceOn")
	public boolean scriptblocks$wrap_canPlaceOn(BlockPos pos, Direction facing, ItemStack stack, Operation<Boolean> original) {
		return original.call(pos, facing, stack) && !(!(this.isCreative() || this.isSpectator()) && this.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT) && !this.hasStatusEffect(ScriptBlocks.BUILDING_MODE));
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

	@Override
	public void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity) {
	}

	@Override
	public void scriptblocks$openHousingScreen() {
	}

	@Override
	public void scriptblocks$openTeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredBeaconBlockScreen(TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
	}

	@Override
	public void scriptblocks$openShopBlockScreen(ShopBlockEntity shopBlockEntity) {
	}

	@Override
	public void scriptblocks$openDialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
	}

//	@Override
//	public void scriptblocks$openDialogueScreen(Dialogue dialogue, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks) {
//	}

	@Override
	public void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
	}

	@Override
	public void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
	}

	@Override
	public void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlockEntity) {
	}

	@Override
	public void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity) {
	}

	@Override
	public void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
	}

	@Override
	public void scriptblocks$openUseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredVillagerSpawnerBlockScreen(TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock) {
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
	public void scriptblocks$openAreaFillerBlockScreen(AreaFillerBlockEntity areaFillerBlockEntity) {
	}

	@Override
	public void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
	}

	@Override
	public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
	}

	@Override
	public void scriptblocks$openInteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
	}

//	@Override
//	public void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
//	}

	@Override
	public void scriptblocks$openCopyDataBlockScreen(CopyDataBlockEntity copyDataBlockEntity) {
	}

	@Override
	public void scriptblocks$openDataWritingBlockScreen(DataWritingBlockEntity dataWritingBlockEntity) {
	}

	@Override
	public void scriptblocks$openDataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
	}

	@Override
	public void scriptblocks$openDataSavingBlockScreen(DataSavingBlockEntity dataSavingBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredEntityRemoverBlockScreen(TriggeredEntityRemoverBlockEntity triggeredEntityRemoverBlockEntity) {
	}

	@Override
	public void scriptblocks$openPlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlockEntity) {
	}

	@Override
	public void scriptblocks$openPVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity) {
	}

//	@Override
//	public void scriptblocks$openLootableVaultBlockScreen(LootableVaultBlockEntity lootableVaultBlockEntity) {
//	}
}
