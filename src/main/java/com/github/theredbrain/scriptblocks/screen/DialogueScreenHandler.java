package com.github.theredbrain.scriptblocks.screen;
//
//import com.github.theredbrain.scriptblocks.data.Dialogue;
//import com.github.theredbrain.scriptblocks.registry.DialoguesRegistry;
//import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.item.ItemStack;
//import net.minecraft.network.RegistryByteBuf;
//import net.minecraft.network.codec.PacketCodec;
//import net.minecraft.screen.ScreenHandler;
//import net.minecraft.util.Identifier;
//import org.jetbrains.annotations.Nullable;
//
//public class DialogueScreenHandler extends ScreenHandler {
//	private final Dialogue dialogue;
//	private final PlayerInventory playerInventory;
//
//	public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, DialogueBlockData data) {
//		this(syncId, playerInventory, data.string());
//	}
//
//	public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, String dialogueIdentifierString) {
//		super(ScreenHandlerTypesRegistry.DIALOGUE_SCREEN_HANDLER, syncId);
//		this.playerInventory = playerInventory;
//		this.dialogue = DialoguesRegistry.registeredDialogues.get(Identifier.of(dialogueIdentifierString));
//	}
//
//	@Nullable
//	public Dialogue getDialogue() {
//		return dialogue;
//	}
//
//	public PlayerInventory getPlayerInventory() {
//		return playerInventory;
//	}
//
//	@Override
//	public ItemStack quickMove(PlayerEntity player, int slot) {
//		return ItemStack.EMPTY;
//	}
//
//	@Override
//	public boolean canUse(PlayerEntity player) {
//		return true;
//	}
//
//	public record DialogueBlockData(
//			String string
//	) {
//
//		public static final PacketCodec<RegistryByteBuf, DialogueBlockData> PACKET_CODEC = PacketCodec.of(DialogueBlockData::write, DialogueBlockData::new);
//
//		public DialogueBlockData(RegistryByteBuf registryByteBuf) {
//			this(registryByteBuf.readString());
//		}
//
//		private void write(RegistryByteBuf registryByteBuf) {
//			registryByteBuf.writeString(string);
//		}
//	}
//}
