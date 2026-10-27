package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

	@Shadow
	public abstract void sendMessage(Component message, boolean overlay);

	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);

	@Shadow
	public abstract boolean isCreative();

	@Shadow
	public abstract boolean isSpectator();

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@WrapOperation(method = "isBlockBreakingRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameMode;isBlockBreakingRestricted()Z"))
	public boolean scriptblocks$wrap_isBlockBreakingRestricted(GameType instance, Operation<Boolean> original) {
		return original.call(instance) || (!this.isCreative() && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT));
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	public void scriptblocks$readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {

		if (nbt.contains("currentHousingBlockPositionX", Tag.TAG_INT) || nbt.contains("currentHousingBlockPositionY", Tag.TAG_INT) || nbt.contains("currentHousingBlockPositionZ", Tag.TAG_INT)) {
			DataAttachmentHelper.setCurrentHousingBlockPosition(((Player) (Object) this), Optional.of(new BlockPos(
					nbt.getInt("currentHousingBlockPositionX"),
					nbt.getInt("currentHousingBlockPositionY"),
					nbt.getInt("currentHousingBlockPositionZ")
			)));
			nbt.remove("currentHousingBlockPositionX");
			nbt.remove("currentHousingBlockPositionY");
			nbt.remove("currentHousingBlockPositionZ");
		}

		if (nbt.contains("currentLocationAccessBlockPositionX", Tag.TAG_INT) || nbt.contains("currentLocationAccessBlockPositionY", Tag.TAG_INT) || nbt.contains("currentLocationAccessBlockPositionZ", Tag.TAG_INT)) {
			DataAttachmentHelper.setCurrentLocationAccessBlockPosition(((Player) (Object) this), Optional.of(new BlockPos(
					nbt.getInt("currentLocationAccessBlockPositionX"),
					nbt.getInt("currentLocationAccessBlockPositionY"),
					nbt.getInt("currentLocationAccessBlockPositionZ")
			)));
			nbt.remove("currentLocationAccessBlockPositionX");
			nbt.remove("currentLocationAccessBlockPositionY");
			nbt.remove("currentLocationAccessBlockPositionZ");
		}

		if (nbt.contains("currentLocationAccessDimension", Tag.TAG_STRING)) {
			DataAttachmentHelper.setCurrentLocationAccessDimension(((Player) (Object) this),
					nbt.getString("currentLocationAccessDimension")
			);
			nbt.remove("currentLocationAccessDimension");
		}

		if (nbt.contains("currentPVPControllerBlockPositionX", Tag.TAG_INT) || nbt.contains("currentPVPControllerBlockPositionY", Tag.TAG_INT) || nbt.contains("currentPVPControllerBlockPositionZ", Tag.TAG_INT)) {
			DataAttachmentHelper.setCurrentPVPControllerBlockPosition(((Player) (Object) this), Optional.of(new BlockPos(
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
		return original.call() && !(!(this.isCreative() || this.isSpectator()) && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

	@WrapMethod(method = "canPlaceOn")
	public boolean scriptblocks$wrap_canPlaceOn(BlockPos pos, Direction facing, ItemStack stack, Operation<Boolean> original) {
		return original.call(pos, facing, stack) && !(!(this.isCreative() || this.isSpectator()) && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

}
