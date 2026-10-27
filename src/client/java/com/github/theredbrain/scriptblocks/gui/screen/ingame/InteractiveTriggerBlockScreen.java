package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveTriggerBlockPacket;
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
public class InteractiveTriggerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.prevent_interaction_identifier_string_label");
	private static final Component KEY_IDENTIFIER_STRING_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.key_identifier_string_label");
	private static final Component INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.interaction_prevented_message_label");
	private static final Component INTERACTION_PREVENTED_SOUND_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.interaction_prevented_sound_label");
	private static final Component LOCKED_MESSAGE_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.locked_message_label");
	private static final Component LOCKED_SOUND_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.locked_sound_label");
	private static final Component UNLOCK_MESSAGE_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.unlock_message_label");
	private static final Component UNLOCK_SOUND_LABEL_TEXT = Component.translatable("gui.interactive_trigger_block.unlock_sound_label");
	private final InteractiveTriggerBlockEntity interactiveTriggerBlockEntity;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private EditBox preventInteractionIdentifierStringField;
	private EditBox keyIdentifierStringField;
	private EditBox interactionPreventedMessageField;
	private EditBox interactionPreventedSoundField;
	private EditBox lockedMessageField;
	private EditBox lockedSoundField;
	private EditBox unlockedMessageField;
	private EditBox unlockedSoundField;

	public InteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.interactiveTriggerBlockEntity = interactiveTriggerBlockEntity;
	}

	@Override
	protected void init() {

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 30, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 30, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 30, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.interactiveTriggerBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 30, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.preventInteractionIdentifierStringField = new EditBox(this.font, this.width / 2 - 154, 65, 150, 20, Component.empty());
		this.preventInteractionIdentifierStringField.setMaxLength(128);
		this.preventInteractionIdentifierStringField.setValue(this.interactiveTriggerBlockEntity.getPreventInteractionIdentifierString());
		this.addWidget(this.preventInteractionIdentifierStringField);

		this.keyIdentifierStringField = new EditBox(this.font, this.width / 2 + 4, 65, 150, 20, Component.empty());
		this.keyIdentifierStringField.setMaxLength(128);
		this.keyIdentifierStringField.setValue(this.interactiveTriggerBlockEntity.getKeyIdentifierString());
		this.addWidget(this.keyIdentifierStringField);

		this.interactionPreventedMessageField = new EditBox(this.font, this.width / 2 - 154, 100, 150, 20, Component.empty());
		this.interactionPreventedMessageField.setMaxLength(128);
		this.interactionPreventedMessageField.setValue(this.interactiveTriggerBlockEntity.getInteractionPreventedMessage());
		this.addWidget(this.interactionPreventedMessageField);

		this.interactionPreventedSoundField = new EditBox(this.font, this.width / 2 + 4, 100, 150, 20, Component.empty());
		this.interactionPreventedSoundField.setMaxLength(128);
		this.interactionPreventedSoundField.setValue(this.interactiveTriggerBlockEntity.getInteractionPreventedSound());
		this.addWidget(this.interactionPreventedSoundField);

		this.lockedMessageField = new EditBox(this.font, this.width / 2 - 154, 135, 150, 20, Component.empty());
		this.lockedMessageField.setMaxLength(128);
		this.lockedMessageField.setValue(this.interactiveTriggerBlockEntity.getLockedMessage());
		this.addWidget(this.lockedMessageField);

		this.lockedSoundField = new EditBox(this.font, this.width / 2 + 4, 135, 150, 20, Component.empty());
		this.lockedSoundField.setMaxLength(128);
		this.lockedSoundField.setValue(this.interactiveTriggerBlockEntity.getLockedSound());
		this.addWidget(this.lockedSoundField);

		this.unlockedMessageField = new EditBox(this.font, this.width / 2 - 154, 170, 150, 20, Component.empty());
		this.unlockedMessageField.setMaxLength(128);
		this.unlockedMessageField.setValue(this.interactiveTriggerBlockEntity.getUnlockedMessage());
		this.addWidget(this.unlockedMessageField);

		this.unlockedSoundField = new EditBox(this.font, this.width / 2 + 4, 170, 150, 20, Component.empty());
		this.unlockedSoundField.setMaxLength(128);
		this.unlockedSoundField.setValue(this.interactiveTriggerBlockEntity.getUnlockedSound());
		this.addWidget(this.unlockedSoundField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 205, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 205, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string0 = this.triggeredBlockPositionOffsetXField.getValue();
		String string1 = this.triggeredBlockPositionOffsetYField.getValue();
		String string2 = this.triggeredBlockPositionOffsetZField.getValue();
		String string3 = this.preventInteractionIdentifierStringField.getValue();
		String string4 = this.keyIdentifierStringField.getValue();
		String string5 = this.interactionPreventedMessageField.getValue();
		String string6 = this.interactionPreventedSoundField.getValue();
		String string7 = this.lockedMessageField.getValue();
		String string8 = this.lockedSoundField.getValue();
		String string9 = this.unlockedMessageField.getValue();
		String string10 = this.unlockedSoundField.getValue();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setValue(string0);
		this.triggeredBlockPositionOffsetYField.setValue(string1);
		this.triggeredBlockPositionOffsetZField.setValue(string2);
		this.preventInteractionIdentifierStringField.setValue(string3);
		this.keyIdentifierStringField.setValue(string4);
		this.interactionPreventedMessageField.setValue(string5);
		this.interactionPreventedSoundField.setValue(string6);
		this.lockedMessageField.setValue(string7);
		this.lockedSoundField.setValue(string8);
		this.unlockedMessageField.setValue(string9);
		this.unlockedSoundField.setValue(string10);
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

		context.drawString(this.font, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
		this.preventInteractionIdentifierStringField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, KEY_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 + 5, 55, 0xA0A0A0);
		this.keyIdentifierStringField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 90, 0xA0A0A0);
		this.interactionPreventedMessageField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, INTERACTION_PREVENTED_SOUND_LABEL_TEXT, this.width / 2 + 5, 90, 0xA0A0A0);
		this.interactionPreventedSoundField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, LOCKED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 125, 0xA0A0A0);
		this.lockedMessageField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, LOCKED_SOUND_LABEL_TEXT, this.width / 2 + 5, 125, 0xA0A0A0);
		this.lockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, UNLOCK_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 160, 0xA0A0A0);
		this.unlockedMessageField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, UNLOCK_SOUND_LABEL_TEXT, this.width / 2 + 5, 160, 0xA0A0A0);
		this.unlockedSoundField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateInteractiveTriggerBlockPacket(
				this.interactiveTriggerBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				this.preventInteractionIdentifierStringField.getValue(),
				this.keyIdentifierStringField.getValue(),
				this.interactionPreventedMessageField.getValue(),
				this.interactionPreventedSoundField.getValue(),
				this.lockedMessageField.getValue(),
				this.lockedSoundField.getValue(),
				this.unlockedMessageField.getValue(),
				this.unlockedSoundField.getValue()
		));
		return true;
	}
}
