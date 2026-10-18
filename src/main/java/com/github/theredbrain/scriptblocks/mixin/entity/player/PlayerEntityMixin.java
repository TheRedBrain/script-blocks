package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(method = "isBlockBreakingRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameMode;isBlockBreakingRestricted()Z"))
	public boolean scriptblocks$wrap_isBlockBreakingRestricted(GameMode instance, Operation<Boolean> original) {
		return original.call(instance) || (!this.isCreative() && this.hasStatusEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT));
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void scriptblocks$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {

		if (nbt.contains("currentHousingBlockPositionX", NbtElement.INT_TYPE) || nbt.contains("currentHousingBlockPositionY", NbtElement.INT_TYPE) || nbt.contains("currentHousingBlockPositionZ", NbtElement.INT_TYPE)) {
			DataAttachmentHelper.setCurrentHousingBlockPosition(((PlayerEntity) (Object) this), Optional.of(new BlockPos(
					nbt.getInt("currentHousingBlockPositionX"),
					nbt.getInt("currentHousingBlockPositionY"),
					nbt.getInt("currentHousingBlockPositionZ")
			)));
			nbt.remove("currentHousingBlockPositionX");
			nbt.remove("currentHousingBlockPositionY");
			nbt.remove("currentHousingBlockPositionZ");
		}

		if (nbt.contains("currentLocationAccessBlockPositionX", NbtElement.INT_TYPE) || nbt.contains("currentLocationAccessBlockPositionY", NbtElement.INT_TYPE) || nbt.contains("currentLocationAccessBlockPositionZ", NbtElement.INT_TYPE)) {
			DataAttachmentHelper.setCurrentLocationAccessBlockPosition(((PlayerEntity) (Object) this), Optional.of(new BlockPos(
					nbt.getInt("currentLocationAccessBlockPositionX"),
					nbt.getInt("currentLocationAccessBlockPositionY"),
					nbt.getInt("currentLocationAccessBlockPositionZ")
			)));
			nbt.remove("currentLocationAccessBlockPositionX");
			nbt.remove("currentLocationAccessBlockPositionY");
			nbt.remove("currentLocationAccessBlockPositionZ");
		}

		if (nbt.contains("currentLocationAccessDimension", NbtElement.STRING_TYPE)) {
			DataAttachmentHelper.setCurrentLocationAccessDimension(((PlayerEntity) (Object) this),
					nbt.getString("currentLocationAccessDimension")
			);
			nbt.remove("currentLocationAccessDimension");
		}

		if (nbt.contains("currentPVPControllerBlockPositionX", NbtElement.INT_TYPE) || nbt.contains("currentPVPControllerBlockPositionY", NbtElement.INT_TYPE) || nbt.contains("currentPVPControllerBlockPositionZ", NbtElement.INT_TYPE)) {
			DataAttachmentHelper.setCurrentPVPControllerBlockPosition(((PlayerEntity) (Object) this), Optional.of(new BlockPos(
					nbt.getInt("currentPVPControllerBlockPositionX"),
					nbt.getInt("currentPVPControllerBlockPositionY"),
					nbt.getInt("currentPVPControllerBlockPositionZ")
			)));
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

}
