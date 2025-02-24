package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateInteractiveLootBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateInteractiveLootBlockPacket> {
	@Override
	public void receive(UpdateInteractiveLootBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos interactiveLootBlockPosition = payload.interactiveLootBlockPosition();

		String lootTableIdentifierString = payload.lootTableIdentifierString();

		InteractiveLootBlockEntity.Mode mode = InteractiveLootBlockEntity.Mode.byName(payload.mode()).orElse(InteractiveLootBlockEntity.Mode.VANILLA);

		int rolls = payload.rolls();

		int choices = payload.choices();

		boolean trackPlayers = payload.trackPlayers();

		String lootAcquiredMessage = payload.lootAcquiredMessage();

		String lootAcquiredSoundId = payload.lootAcquiredSoundId();

		String alreadyLootedMessage = payload.alreadyLootedMessage();

		String alreadyLootedSoundId = payload.alreadyLootedSoundId();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(interactiveLootBlockPosition);
		BlockState blockState = world.getBlockState(interactiveLootBlockPosition);

		if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
			interactiveLootBlockEntity.setLootTableIdentifierString(lootTableIdentifierString);
			interactiveLootBlockEntity.setMode(mode);
			interactiveLootBlockEntity.setRolls(rolls);
			interactiveLootBlockEntity.setChoices(choices);
			interactiveLootBlockEntity.setTrackPlayers(trackPlayers);
			interactiveLootBlockEntity.setLootAcquiredMessage(lootAcquiredMessage);
			interactiveLootBlockEntity.setLootAcquiredSoundId(lootAcquiredSoundId);
			interactiveLootBlockEntity.setAlreadyLootedMessage(alreadyLootedMessage);
			interactiveLootBlockEntity.setAlreadyLootedSoundId(alreadyLootedSoundId);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			interactiveLootBlockEntity.markDirty();
			world.updateListeners(interactiveLootBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
