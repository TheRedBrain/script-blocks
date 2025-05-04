package com.github.theredbrain.scriptblocks.network.packet;
//
//import com.github.theredbrain.scriptblocks.ScriptBlocks;
//import com.github.theredbrain.scriptblocks.block.LootableVaultBlock;
//import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
//import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
//import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.entity.BlockEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.text.Text;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//
//import java.util.Optional;
//
//public class UpdateLootableVaultBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateLootableVaultBlockPacket> {
//	@Override
//	public void receive(UpdateLootableVaultBlockPacket payload, ServerPlayNetworking.Context context) {
//
//		ServerPlayerEntity serverPlayerEntity = context.player();
//
//		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
//			return;
//		}
//
//		BlockPos lootableVaultBlockPosition = payload.lootableVaultBlockPosition();
//
//		LootableVaultConfig config = new LootableVaultConfig(
//				"",
//				3,
//				1,
//				true,
//				4.0,
//				4.5,
//				new ItemStack(Items.TRIAL_KEY),
//				Optional.empty()
//		);
//
//		World world = serverPlayerEntity.getWorld();
//
//		BlockEntity blockEntity = world.getBlockEntity(lootableVaultBlockPosition);
//		BlockState blockState = world.getBlockState(lootableVaultBlockPosition);
//
//		if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity && blockState.isOf(BlockRegistry.LOOTABLE_VAULT_BLOCK)) {
//
////			world.setBlockState(lootableVaultBlockPosition, blockState.with(LootableVaultBlock.OMINOUS, payload.isOminous()));
//
//			lootableVaultBlockEntity.setConfig(config);
//
//			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
//			lootableVaultBlockEntity.markDirty();
//			world.updateListeners(lootableVaultBlockPosition, blockState, blockState.with(LootableVaultBlock.OMINOUS, payload.isOminous()), Block.NOTIFY_ALL);
//		}
//	}
//}
