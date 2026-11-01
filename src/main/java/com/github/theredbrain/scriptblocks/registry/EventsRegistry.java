package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public class EventsRegistry {
	public static void initializeEvents() {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
			if (((!player.isSpectator() && !player.isCreative()) || player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
				return true;
			}
			return false;
		});
		BlockEvents.USE_ITEM_ON.register((itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult) -> {
			if (((!player.isSpectator() && !player.isCreative()) || player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
				Optional<BlockPos> optionalHousingBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(player);
				boolean bl = false;
				if (optionalHousingBlockPos.isPresent() && level.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
					bl = housingBlockEntity.influenceAreaContains(blockHitResult.getBlockPos().relative(blockHitResult.getDirection()));
				} else {
					DataAttachmentHelper.setCurrentHousingBlockPosition(player, Optional.empty());
				}
				if (!bl) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
		BlockEvents.USE_WITHOUT_ITEM.register((blockState, level, blockPos, player, blockHitResult) -> {
			if (((!player.isSpectator() && !player.isCreative()) || player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
				Optional<BlockPos> optionalHousingBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(player);
				boolean bl = false;
				if (optionalHousingBlockPos.isPresent() && level.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
					bl = housingBlockEntity.influenceAreaContains(blockHitResult.getBlockPos().relative(blockHitResult.getDirection()));
				} else {
					DataAttachmentHelper.setCurrentHousingBlockPosition(player, Optional.empty());
				}
				if (!bl) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			if (((!player.isSpectator() && !player.isCreative()) || player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
				Optional<BlockPos> optionalHousingBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(player);
				boolean bl = false;
				if (optionalHousingBlockPos.isPresent() && level.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
					bl = housingBlockEntity.influenceAreaContains(pos);
				} else {
					DataAttachmentHelper.setCurrentHousingBlockPosition(player, Optional.empty());
				}
				if (bl) {
					return InteractionResult.PASS;
				}
			}
			return InteractionResult.CONSUME;
		});
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (!player.isSpectator() && entity instanceof Mob mob) {
				Optional<BlockPos> optionalBlockPos = DataAttachmentHelper.getUseRelayBlockPosition(mob);
				if (optionalBlockPos.isPresent()) {
					if (player instanceof ServerPlayer serverPlayerEntity) {
						Level world = player.level();
						BlockHitResult blockHitResult = new BlockHitResult(player.position(), Direction.UP, optionalBlockPos.get(), false);
						ItemStack itemStack = player.getItemInHand(hand);
						serverPlayerEntity.gameMode.useItemOn(serverPlayerEntity, world, itemStack, hand, blockHitResult);
					}
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.PASS;
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (entity instanceof Mob mob) {
				float healthThreshold = DataAttachmentHelper.getBossHealthThreshold(mob);
				float currentHealth = mob.getHealth();
				if (healthThreshold >= 0.0F && currentHealth <= healthThreshold) {
					mob.setHealth(healthThreshold);
					Optional<BlockPos> optionalControllerBlockPos = DataAttachmentHelper.getControllerBlockPosition(mob);
					if (optionalControllerBlockPos.isPresent()) {
						BlockEntity blockEntity = mob.level().getBlockEntity(optionalControllerBlockPos.get());
						if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
							BossControllerBlockEntity.bossReachedHealthThreshold(bossControllerBlockEntity, mob);
						}
					}
				}
			}
		});
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if (entity instanceof Mob mob) {
				float healthThreshold = DataAttachmentHelper.getBossHealthThreshold(mob);
				float currentHealth = mob.getHealth();
				if (healthThreshold >= 0.0F && currentHealth <= healthThreshold) {
					mob.setHealth(healthThreshold);
					Optional<BlockPos> optionalControllerBlockPos = DataAttachmentHelper.getControllerBlockPosition(mob);
					if (optionalControllerBlockPos.isPresent()) {
						BlockEntity blockEntity = mob.level().getBlockEntity(optionalControllerBlockPos.get());
						if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
							BossControllerBlockEntity.bossReachedHealthThreshold(bossControllerBlockEntity, mob);
							return false;
						}
					}
				}
			}
			return true;
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof Mob mob) {
				Optional<BlockPos> optionalControllerBlockPos = DataAttachmentHelper.getControllerBlockPosition(mob);
				if (optionalControllerBlockPos.isPresent()) {
					BlockEntity blockEntity = mob.level().getBlockEntity(optionalControllerBlockPos.get());
					if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
						triggeredSpawnerBlockEntity.onBoundEntityKilled();
					}
				}
			}
			if (entity instanceof ServerPlayer serverPlayerEntity) {
				DataAttachmentHelper.setActiveLocationCooldowns(serverPlayerEntity);
			}
		});
	}
}
