package com.github.theredbrain.scriptblocks.compatibility;

import com.github.theredbrain.scriptblocks.block.InteractiveLootBlock;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LootableCompat {

	@Deprecated
	public static void supplyLootableLoot(Identifier identifier, ServerLevel world, ServerPlayer serverPlayerEntity, Vec3 pos, int rolls, int choices, boolean withChoice, @Nullable ItemStack itemStack) {
//		if (withChoice) {
//			LootablesApi.supplyLootWithChoices(
//					identifier,
//					serverPlayerEntity,
//					pos,
//					(serverPlayerEntity1, vec3d) -> {
//
//						BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
//						BlockEntity blockEntity = world.getBlockEntity(blockPos);
//						if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
//							InteractiveLootBlock.lootWasSupplied(serverPlayerEntity, interactiveLootBlockEntity);
//						}
//						if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
//							lootableVaultBlockEntity.markAsRewarded(serverPlayerEntity, itemStack);
//						}
//					},
//					(serverPlayerEntity2, vec3d) -> {
//						// gets called when player leaves choices screen without making a choice
////							BlockEntity blockEntity = world.getBlockEntity(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
////							if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
////								interactiveLootBlockEntity.removePlayerFromSet(serverPlayerEntity);
////							}
////							if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
////								lootableVaultBlockEntity.unmarkAsRewarded(serverPlayerEntity);
////							}
//					},
//					null,
//					rolls,
//					choices
//			);
//		} else {
//			LootablesApi.supplyLootRandomly(
//					identifier,
//					serverPlayerEntity,
//					pos,
//					null,
//					rolls
//			);
//			BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
//			BlockEntity blockEntity = world.getBlockEntity(blockPos);
//			if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
//				InteractiveLootBlock.lootWasSupplied(serverPlayerEntity, interactiveLootBlockEntity);
//			}
//			if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
//				lootableVaultBlockEntity.markAsRewarded(serverPlayerEntity, itemStack);
//			}
//		}
	}

}
