package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class UpdateInteractiveLootBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateInteractiveLootBlockPacket> {
	@Override
	public void receive(UpdateInteractiveLootBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
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

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(interactiveLootBlockPosition);
		BlockState blockState = world.getBlockState(interactiveLootBlockPosition);

		if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
			interactiveLootBlockEntity.reset();
			interactiveLootBlockEntity.setLootTableIdentifierString(lootTableIdentifierString);
			interactiveLootBlockEntity.setMode(mode);
			interactiveLootBlockEntity.setRolls(rolls);
			interactiveLootBlockEntity.setChoices(choices);
			interactiveLootBlockEntity.setTrackPlayers(trackPlayers);
			interactiveLootBlockEntity.setLootAcquiredMessage(lootAcquiredMessage);
			interactiveLootBlockEntity.setLootAcquiredSoundId(lootAcquiredSoundId);
			interactiveLootBlockEntity.setAlreadyLootedMessage(alreadyLootedMessage);
			interactiveLootBlockEntity.setAlreadyLootedSoundId(alreadyLootedSoundId);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			interactiveLootBlockEntity.setChanged();
			world.sendBlockUpdated(interactiveLootBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
