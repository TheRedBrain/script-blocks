package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayChestBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class UseRelayChestBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component KEY_IDENTIFIER_STRING_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.key_identifier_string_label");
	private static final Component LOCKED_MESSAGE_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.locked_message_label");
	private static final Component LOCKED_SOUND_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.locked_sound_label");
	private static final Component UNLOCK_MESSAGE_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.unlock_message_label");
	private static final Component UNLOCK_SOUND_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.unlock_sound_label");
	private static final Component RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.use_relay_block.relay_block_position_offset");
	private final UseRelayChestBlockEntity useRelayChestBlockEntity;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private EditBox keyIdentifierStringField;
	private EditBox lockedMessageField;
	private EditBox lockedSoundField;
	private EditBox unlockedMessageField;
	private EditBox unlockedSoundField;
	private EditBox relayBlockPositionOffsetXField;
	private EditBox relayBlockPositionOffsetYField;
	private EditBox relayBlockPositionOffsetZField;

	public UseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.useRelayChestBlockEntity = useRelayChestBlockEntity;
	}

	@Override
	protected void init() {

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 40, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 40, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 40, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.useRelayChestBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 40, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.keyIdentifierStringField = new EditBox(this.font, this.width / 2 - 154, 75, 300, 20, Component.empty());
		this.keyIdentifierStringField.setMaxLength(128);
		this.keyIdentifierStringField.setValue(this.useRelayChestBlockEntity.getKeyIdentifierString());
		this.addWidget(this.keyIdentifierStringField);

		this.lockedMessageField = new EditBox(this.font, this.width / 2 - 154, 110, 150, 20, Component.empty());
		this.lockedMessageField.setMaxLength(128);
		this.lockedMessageField.setValue(this.useRelayChestBlockEntity.getLockedMessage());
		this.addWidget(this.lockedMessageField);

		this.lockedSoundField = new EditBox(this.font, this.width / 2 + 4, 110, 150, 20, Component.empty());
		this.lockedSoundField.setMaxLength(128);
		this.lockedSoundField.setValue(this.useRelayChestBlockEntity.getLockedSound());
		this.addWidget(this.lockedSoundField);

		this.unlockedMessageField = new EditBox(this.font, this.width / 2 - 154, 145, 150, 20, Component.empty());
		this.unlockedMessageField.setMaxLength(128);
		this.unlockedMessageField.setValue(this.useRelayChestBlockEntity.getUnlockedMessage());
		this.addWidget(this.unlockedMessageField);

		this.unlockedSoundField = new EditBox(this.font, this.width / 2 + 4, 145, 150, 20, Component.empty());
		this.unlockedSoundField.setMaxLength(128);
		this.unlockedSoundField.setValue(this.useRelayChestBlockEntity.getUnlockedSound());
		this.addWidget(this.unlockedSoundField);

		this.relayBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 185, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetXField.setMaxLength(128);
		this.relayBlockPositionOffsetXField.setValue(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getX()));
		this.addWidget(this.relayBlockPositionOffsetXField);
		this.relayBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 185, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetYField.setMaxLength(128);
		this.relayBlockPositionOffsetYField.setValue(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getY()));
		this.addWidget(this.relayBlockPositionOffsetYField);
		this.relayBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 185, 100, 20, Component.translatable(""));
		this.relayBlockPositionOffsetZField.setMaxLength(128);
		this.relayBlockPositionOffsetZField.setValue(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getZ()));
		this.addWidget(this.relayBlockPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 209, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 209, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string2 = this.triggeredBlockPositionOffsetXField.getValue();
		String string3 = this.triggeredBlockPositionOffsetYField.getValue();
		String string4 = this.triggeredBlockPositionOffsetZField.getValue();
		String string5 = this.keyIdentifierStringField.getValue();
		String string6 = this.lockedMessageField.getValue();
		String string7 = this.lockedSoundField.getValue();
		String string8 = this.unlockedMessageField.getValue();
		String string9 = this.unlockedSoundField.getValue();
		String string10 = this.relayBlockPositionOffsetXField.getValue();
		String string11 = this.relayBlockPositionOffsetYField.getValue();
		String string12 = this.relayBlockPositionOffsetZField.getValue();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setValue(string2);
		this.triggeredBlockPositionOffsetYField.setValue(string3);
		this.triggeredBlockPositionOffsetZField.setValue(string4);
		this.keyIdentifierStringField.setValue(string5);
		this.lockedMessageField.setValue(string6);
		this.lockedSoundField.setValue(string7);
		this.unlockedMessageField.setValue(string8);
		this.unlockedSoundField.setValue(string9);
		this.relayBlockPositionOffsetXField.setValue(string10);
		this.relayBlockPositionOffsetYField.setValue(string11);
		this.relayBlockPositionOffsetZField.setValue(string12);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 30, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, KEY_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 - 153, 65, 0xA0A0A0);
		this.keyIdentifierStringField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, LOCKED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 100, 0xA0A0A0);
		this.lockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, LOCKED_SOUND_LABEL_TEXT, this.width / 2 + 5, 100, 0xA0A0A0);
		this.lockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, UNLOCK_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 135, 0xA0A0A0);
		this.unlockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, UNLOCK_SOUND_LABEL_TEXT, this.width / 2 + 5, 135, 0xA0A0A0);
		this.unlockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 175, 0xA0A0A0);
		this.relayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateUseRelayChestBlockPacket(
				this.useRelayChestBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				this.keyIdentifierStringField.getValue(),
				this.lockedMessageField.getValue(),
				this.lockedSoundField.getValue(),
				this.unlockedMessageField.getValue(),
				this.unlockedSoundField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.relayBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetZField.getValue())
				)
		));
		return true;
	}
}
