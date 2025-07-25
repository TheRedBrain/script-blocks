package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogueScreenHandler extends ScreenHandler {

	public World world;
	@Nullable
	public Dialogue dialogue;
	public BlockPos dataBlockPos;
	public List<MutablePair<String, BlockPos>> dialogueUsedBlocksList;
	public List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList;
	public List<String> unlockedAnswersList;
	public List<String> visibleAnswersList;
	public List<String> dialogueTextList = new ArrayList<>(List.of());

	public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, DialogueData data) {
		this(syncId, playerInventory, data.dialogueIdentifierString, data.dataBlockPos, data.dialogueUsedBlocks, data.dialogueTriggeredBlocks, data.unlockedAnswersList, data.visibleAnswersList);
	}

	public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, String dialogueIdentifierString, BlockPos dataBlockPos, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks, List<String> unlockedAnswersList, List<String> visibleAnswersList) {
		super(ScreenHandlerTypesRegistry.DIALOGUE_SCREEN_HANDLER, syncId);
		this.world = playerInventory.player.getWorld();
		this.dataBlockPos = dataBlockPos;
		this.dialogueUsedBlocksList = dialogueUsedBlocks;
		this.dialogueTriggeredBlocksList = dialogueTriggeredBlocks;
		this.unlockedAnswersList = unlockedAnswersList;
		this.visibleAnswersList = visibleAnswersList;
		Optional<RegistryEntry.Reference<Dialogue>> optionalDialogueReference = this.world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(dialogueIdentifierString));
		if (optionalDialogueReference.isPresent()) {
			this.dialogue = optionalDialogueReference.get().value();
			this.dialogueTextList = this.dialogue.dialogueTextList();
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	public record DialogueData(
			String dialogueIdentifierString,
			BlockPos dataBlockPos,
			List<MutablePair<String, BlockPos>> dialogueUsedBlocks,
			List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks,
			List<String> unlockedAnswersList,
			List<String> visibleAnswersList
	) {

		public static final PacketCodec<RegistryByteBuf, DialogueData> PACKET_CODEC = PacketCodec.of(DialogueData::write, DialogueData::new);

		public DialogueData(RegistryByteBuf registryByteBuf) {
			this(
					registryByteBuf.readString(),
					registryByteBuf.readBlockPos(),
					registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
					registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN),
					registryByteBuf.readList(PacketCodecs.STRING),
					registryByteBuf.readList(PacketCodecs.STRING)
			);
		}

		private void write(RegistryByteBuf registryByteBuf) {
			registryByteBuf.writeString(this.dialogueIdentifierString);
			registryByteBuf.writeBlockPos(this.dataBlockPos);
			registryByteBuf.writeCollection(this.dialogueUsedBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
			registryByteBuf.writeCollection(this.dialogueTriggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
			registryByteBuf.writeCollection(this.unlockedAnswersList, PacketCodecs.STRING);
			registryByteBuf.writeCollection(this.visibleAnswersList, PacketCodecs.STRING);
		}
	}
}
