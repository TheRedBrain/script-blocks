package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveTriggerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class InteractiveTriggerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Text PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.prevent_interaction_identifier_string_label");
	private static final Text KEY_IDENTIFIER_STRING_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.key_identifier_string_label");
	private static final Text INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.interaction_prevented_message_label");
	private static final Text INTERACTION_PREVENTED_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.interaction_prevented_sound_label");
	private static final Text LOCKED_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_message_label");
	private static final Text LOCKED_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_sound_label");
	private static final Text UNLOCK_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_message_label");
	private static final Text UNLOCK_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_sound_label");
	private final InteractiveTriggerBlockEntity interactiveTriggerBlockEntity;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private TextFieldWidget preventInteractionIdentifierStringField;
	private TextFieldWidget keyIdentifierStringField;
	private TextFieldWidget interactionPreventedMessageField;
	private TextFieldWidget interactionPreventedSoundField;
	private TextFieldWidget lockedMessageField;
	private TextFieldWidget lockedSoundField;
	private TextFieldWidget unlockedMessageField;
	private TextFieldWidget unlockedSoundField;

	public InteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
		super(NarratorManager.EMPTY);
		this.interactiveTriggerBlockEntity = interactiveTriggerBlockEntity;
	}

	@Override
	protected void init() {

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 30, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 30, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 30, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.interactiveTriggerBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 30, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.preventInteractionIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 65, 150, 20, Text.empty());
		this.preventInteractionIdentifierStringField.setMaxLength(128);
		this.preventInteractionIdentifierStringField.setText(this.interactiveTriggerBlockEntity.getPreventInteractionIdentifierString());
		this.addSelectableChild(this.preventInteractionIdentifierStringField);

		this.keyIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 65, 150, 20, Text.empty());
		this.keyIdentifierStringField.setMaxLength(128);
		this.keyIdentifierStringField.setText(this.interactiveTriggerBlockEntity.getKeyIdentifierString());
		this.addSelectableChild(this.keyIdentifierStringField);

		this.interactionPreventedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 100, 150, 20, Text.empty());
		this.interactionPreventedMessageField.setMaxLength(128);
		this.interactionPreventedMessageField.setText(this.interactiveTriggerBlockEntity.getInteractionPreventedMessage());
		this.addSelectableChild(this.interactionPreventedMessageField);

		this.interactionPreventedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 100, 150, 20, Text.empty());
		this.interactionPreventedSoundField.setMaxLength(128);
		this.interactionPreventedSoundField.setText(this.interactiveTriggerBlockEntity.getInteractionPreventedSound());
		this.addSelectableChild(this.interactionPreventedSoundField);

		this.lockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 135, 150, 20, Text.empty());
		this.lockedMessageField.setMaxLength(128);
		this.lockedMessageField.setText(this.interactiveTriggerBlockEntity.getLockedMessage());
		this.addSelectableChild(this.lockedMessageField);

		this.lockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 135, 150, 20, Text.empty());
		this.lockedSoundField.setMaxLength(128);
		this.lockedSoundField.setText(this.interactiveTriggerBlockEntity.getLockedSound());
		this.addSelectableChild(this.lockedSoundField);

		this.unlockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 170, 150, 20, Text.empty());
		this.unlockedMessageField.setMaxLength(128);
		this.unlockedMessageField.setText(this.interactiveTriggerBlockEntity.getUnlockedMessage());
		this.addSelectableChild(this.unlockedMessageField);

		this.unlockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 170, 150, 20, Text.empty());
		this.unlockedSoundField.setMaxLength(128);
		this.unlockedSoundField.setText(this.interactiveTriggerBlockEntity.getUnlockedSound());
		this.addSelectableChild(this.unlockedSoundField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 205, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 205, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string0 = this.triggeredBlockPositionOffsetXField.getText();
		String string1 = this.triggeredBlockPositionOffsetYField.getText();
		String string2 = this.triggeredBlockPositionOffsetZField.getText();
		String string3 = this.preventInteractionIdentifierStringField.getText();
		String string4 = this.keyIdentifierStringField.getText();
		String string5 = this.interactionPreventedMessageField.getText();
		String string6 = this.interactionPreventedSoundField.getText();
		String string7 = this.lockedMessageField.getText();
		String string8 = this.lockedSoundField.getText();
		String string9 = this.unlockedMessageField.getText();
		String string10 = this.unlockedSoundField.getText();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setText(string0);
		this.triggeredBlockPositionOffsetYField.setText(string1);
		this.triggeredBlockPositionOffsetZField.setText(string2);
		this.preventInteractionIdentifierStringField.setText(string3);
		this.keyIdentifierStringField.setText(string4);
		this.interactionPreventedMessageField.setText(string5);
		this.interactionPreventedSoundField.setText(string6);
		this.lockedMessageField.setText(string7);
		this.lockedSoundField.setText(string8);
		this.unlockedMessageField.setText(string9);
		this.unlockedSoundField.setText(string10);
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
		this.preventInteractionIdentifierStringField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, KEY_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 + 5, 55, 0xA0A0A0);
		this.keyIdentifierStringField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 90, 0xA0A0A0);
		this.interactionPreventedMessageField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, INTERACTION_PREVENTED_SOUND_LABEL_TEXT, this.width / 2 + 5, 90, 0xA0A0A0);
		this.interactionPreventedSoundField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, LOCKED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 125, 0xA0A0A0);
		this.lockedMessageField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, LOCKED_SOUND_LABEL_TEXT, this.width / 2 + 5, 125, 0xA0A0A0);
		this.lockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UNLOCK_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 160, 0xA0A0A0);
		this.unlockedMessageField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, UNLOCK_SOUND_LABEL_TEXT, this.width / 2 + 5, 160, 0xA0A0A0);
		this.unlockedSoundField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateInteractiveTriggerBlockPacket(
				this.interactiveTriggerBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				this.preventInteractionIdentifierStringField.getText(),
				this.keyIdentifierStringField.getText(),
				this.interactionPreventedMessageField.getText(),
				this.interactionPreventedSoundField.getText(),
				this.lockedMessageField.getText(),
				this.lockedSoundField.getText(),
				this.unlockedMessageField.getText(),
				this.unlockedSoundField.getText()
		));
		return true;
	}
}
