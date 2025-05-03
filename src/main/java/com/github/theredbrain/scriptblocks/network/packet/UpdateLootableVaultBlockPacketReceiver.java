package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.LootableVaultBlock;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateLootableVaultBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateLootableVaultBlockPacket> {
	@Override
	public void receive(UpdateLootableVaultBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos lootableVaultBlockPosition = payload.lootableVaultBlockPosition();

		String lootableVaultConfigIdentifier = payload.lootableVaultConfigIdentifier();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(lootableVaultBlockPosition);
		BlockState blockState = world.getBlockState(lootableVaultBlockPosition);

		if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity && blockState.isOf(BlockRegistry.LOOTABLE_VAULT_BLOCK)) {

			world.setBlockState(lootableVaultBlockPosition, blockState.with(LootableVaultBlock.OMINOUS, payload.isOminous()));

			lootableVaultBlockEntity.setConfigId(lootableVaultConfigIdentifier);

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			lootableVaultBlockEntity.markDirty();
			world.updateListeners(lootableVaultBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
