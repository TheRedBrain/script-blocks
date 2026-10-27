package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DialogueScreenHandler extends AbstractContainerMenu {

	public Level world;
	@Nullable
	public Dialogue dialogue;
	public BlockPos dataBlockPos;
	public List<MutablePair<String, BlockPos>> dialogueUsedBlocksList;
	public List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList;
	public List<String> unlockedAnswersList;
	public List<String> visibleAnswersList;
	public List<String> dialogueTextList = new ArrayList<>(List.of());
	public int dialogueTextsStartingIndex;
	public int answersStartingIndex;

	public DialogueScreenHandler(int syncId, Inventory playerInventory, DialogueData data) {
		this(syncId, playerInventory, data.dialogueIdentifierString, data.dataBlockPos, data.dialogueUsedBlocks, data.dialogueTriggeredBlocks, data.unlockedAnswersList, data.visibleAnswersList);
	}

	public DialogueScreenHandler(int syncId, Inventory playerInventory, String dialogueIdentifierString, BlockPos dataBlockPos, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks, List<String> unlockedAnswersList, List<String> visibleAnswersList) {
		super(ScriptBlocksScreenHandlerTypes.DIALOGUE_SCREEN_HANDLER, syncId);
		this.world = playerInventory.player.getWorld();
		this.dataBlockPos = dataBlockPos;
		this.dialogueUsedBlocksList = dialogueUsedBlocks;
		this.dialogueTriggeredBlocksList = dialogueTriggeredBlocks;
		this.unlockedAnswersList = unlockedAnswersList;
		this.visibleAnswersList = visibleAnswersList;
		Optional<Holder.Reference<Dialogue>> optionalDialogueReference = this.world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(dialogueIdentifierString));
		if (optionalDialogueReference.isPresent()) {
			this.dialogue = optionalDialogueReference.get().value();
			this.dialogueTextList = this.dialogue.dialogueTextList();
			this.dialogueTextsStartingIndex = this.dialogue.dialogueTextsStartingIndex();
			this.answersStartingIndex = this.dialogue.answersStartingIndex();
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public int getDialogueTextsStartingIndex() {
		return Math.max(0, Math.min(7, this.dialogueTextsStartingIndex));
	}

	public int getAnswersStartingIndex() {
		return Math.max(0, Math.min(7, this.answersStartingIndex));
	}

	public record DialogueData(
			String dialogueIdentifierString,
			BlockPos dataBlockPos,
			List<MutablePair<String, BlockPos>> dialogueUsedBlocks,
			List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks,
			List<String> unlockedAnswersList,
			List<String> visibleAnswersList
	) {

		public static final StreamCodec<RegistryFriendlyByteBuf, DialogueData> PACKET_CODEC = StreamCodec.ofMember(DialogueData::write, DialogueData::new);

		public DialogueData(RegistryFriendlyByteBuf registryByteBuf) {
			this(
					registryByteBuf.readUtf(),
					registryByteBuf.readBlockPos(),
					registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
					registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN),
					registryByteBuf.readList(ByteBufCodecs.STRING_UTF8),
					registryByteBuf.readList(ByteBufCodecs.STRING_UTF8)
			);
		}

		private void write(RegistryFriendlyByteBuf registryByteBuf) {
			registryByteBuf.writeUtf(this.dialogueIdentifierString);
			registryByteBuf.writeBlockPos(this.dataBlockPos);
			registryByteBuf.writeCollection(this.dialogueUsedBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
			registryByteBuf.writeCollection(this.dialogueTriggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
			registryByteBuf.writeCollection(this.unlockedAnswersList, ByteBufCodecs.STRING_UTF8);
			registryByteBuf.writeCollection(this.visibleAnswersList, ByteBufCodecs.STRING_UTF8);
		}
	}
}
