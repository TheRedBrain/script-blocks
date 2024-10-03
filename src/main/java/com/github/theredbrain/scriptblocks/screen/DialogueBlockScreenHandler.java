package com.github.theredbrain.scriptblocks.screen;

import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class DialogueBlockScreenHandler extends ScreenHandler {
	private final BlockPos blockPos;
	private final String dialogueIdentifierString;
	private final PlayerInventory playerInventory;
	private final boolean showCreativeTab;

	public DialogueBlockScreenHandler(int syncId, PlayerInventory playerInventory, DialogueBlockData data) {
		this(syncId, playerInventory, playerInventory.player.isCreativeLevelTwoOp(), data.blockPos(), data.string());
	}

	public DialogueBlockScreenHandler(int syncId, PlayerInventory playerInventory, boolean showCreativeTab, BlockPos blockPos, String dialogueIdentifierString) {
		super(ScreenHandlerTypesRegistry.DIALOGUE_BLOCK_SCREEN_HANDLER, syncId);
		this.playerInventory = playerInventory;
		this.showCreativeTab = showCreativeTab;
		this.blockPos = blockPos;
		this.dialogueIdentifierString = dialogueIdentifierString;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public String getDialogueIdentifierString() {
		return dialogueIdentifierString;
	}

	public PlayerInventory getPlayerInventory() {
		return playerInventory;
	}

	public boolean getShowCreativeTab() {
		return this.showCreativeTab;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	public record DialogueBlockData(
			BlockPos blockPos,
			String string
	) {

		public static final PacketCodec<RegistryByteBuf, DialogueBlockData> PACKET_CODEC = PacketCodec.of(DialogueBlockData::write, DialogueBlockData::new);

		public DialogueBlockData(RegistryByteBuf registryByteBuf) {
			this(registryByteBuf.readBlockPos(), registryByteBuf.readString());
		}

		private void write(RegistryByteBuf registryByteBuf) {
			registryByteBuf.writeBlockPos(blockPos);
			registryByteBuf.writeString(string);
		}
	}
}
