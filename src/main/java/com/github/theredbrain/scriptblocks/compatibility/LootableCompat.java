package com.github.theredbrain.scriptblocks.compatibility;

import com.github.theredbrain.scriptblocks.block.InteractiveLootBlock;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import me.fzzyhmstrs.lootables.api.LootablesApi;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LootableCompat {

	@Deprecated
	public static void supplyLootableLoot(Identifier identifier, ServerWorld world, ServerPlayerEntity serverPlayerEntity, Vec3d pos, int rolls, int choices, boolean withChoice, @Nullable ItemStack itemStack) {
		if (withChoice) {
			LootablesApi.supplyLootWithChoices(
					identifier,
					serverPlayerEntity,
					pos,
					(serverPlayerEntity1, vec3d) -> {

						BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
						BlockEntity blockEntity = world.getBlockEntity(blockPos);
						if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
							InteractiveLootBlock.lootWasSupplied(serverPlayerEntity, interactiveLootBlockEntity);
						}
						if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
							lootableVaultBlockEntity.markAsRewarded(serverPlayerEntity, itemStack);
						}
					},
					(serverPlayerEntity2, vec3d) -> {
						// gets called when player leaves choices screen without making a choice
//							BlockEntity blockEntity = world.getBlockEntity(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
//							if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
//								interactiveLootBlockEntity.removePlayerFromSet(serverPlayerEntity);
//							}
//							if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
//								lootableVaultBlockEntity.unmarkAsRewarded(serverPlayerEntity);
//							}
					},
					null,
					rolls,
					choices
			);
		} else {
			LootablesApi.supplyLootRandomly(
					identifier,
					serverPlayerEntity,
					pos,
					null,
					rolls
			);
			BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
				InteractiveLootBlock.lootWasSupplied(serverPlayerEntity, interactiveLootBlockEntity);
			}
			if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
				lootableVaultBlockEntity.markAsRewarded(serverPlayerEntity, itemStack);
			}
		}
	}

}
