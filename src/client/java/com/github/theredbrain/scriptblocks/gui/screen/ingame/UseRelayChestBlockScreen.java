package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayChestBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class UseRelayChestBlockScreen extends Screen {
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	private static final Text KEY_IDENTIFIER_STRING_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.key_identifier_string_label");
	private static final Text LOCKED_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_message_label");
	private static final Text LOCKED_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_sound_label");
	private static final Text UNLOCK_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_message_label");
	private static final Text UNLOCK_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_sound_label");
	private static final Text RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.use_relay_block.relayBlockPositionOffset");
	private final UseRelayChestBlockEntity useRelayChestBlockEntity;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private boolean triggeredBlockResets;
	private TextFieldWidget keyIdentifierStringField;
	private TextFieldWidget lockedMessageField;
	private TextFieldWidget lockedSoundField;
	private TextFieldWidget unlockedMessageField;
	private TextFieldWidget unlockedSoundField;
	private TextFieldWidget relayBlockPositionOffsetXField;
	private TextFieldWidget relayBlockPositionOffsetYField;
	private TextFieldWidget relayBlockPositionOffsetZField;

	public UseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlockEntity) {
		super(NarratorManager.EMPTY);
		this.useRelayChestBlockEntity = useRelayChestBlockEntity;
	}

	private void done() {
		if (this.updateRedstoneTriggerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 40, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 40, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.useRelayChestBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.useRelayChestBlockEntity.getTriggeredBlock().getRight();
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 40, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.keyIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 75, 300, 20, Text.empty());
		this.keyIdentifierStringField.setMaxLength(128);
		this.keyIdentifierStringField.setText(this.useRelayChestBlockEntity.getKeyIdentifierString());
		this.addSelectableChild(this.keyIdentifierStringField);

		this.lockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 110, 150, 20, Text.empty());
		this.lockedMessageField.setMaxLength(128);
		this.lockedMessageField.setText(this.useRelayChestBlockEntity.getLockedMessage());
		this.addSelectableChild(this.lockedMessageField);

		this.lockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 110, 150, 20, Text.empty());
		this.lockedSoundField.setMaxLength(128);
		this.lockedSoundField.setText(this.useRelayChestBlockEntity.getLockedSound());
		this.addSelectableChild(this.lockedSoundField);

		this.unlockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 145, 150, 20, Text.empty());
		this.unlockedMessageField.setMaxLength(128);
		this.unlockedMessageField.setText(this.useRelayChestBlockEntity.getUnlockedMessage());
		this.addSelectableChild(this.unlockedMessageField);

		this.unlockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 145, 150, 20, Text.empty());
		this.unlockedSoundField.setMaxLength(128);
		this.unlockedSoundField.setText(this.useRelayChestBlockEntity.getUnlockedSound());
		this.addSelectableChild(this.unlockedSoundField);

		this.relayBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 185, 100, 20, Text.translatable(""));
		this.relayBlockPositionOffsetXField.setMaxLength(128);
		this.relayBlockPositionOffsetXField.setText(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getX()));
		this.addSelectableChild(this.relayBlockPositionOffsetXField);
		this.relayBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 185, 100, 20, Text.translatable(""));
		this.relayBlockPositionOffsetYField.setMaxLength(128);
		this.relayBlockPositionOffsetYField.setText(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getY()));
		this.addSelectableChild(this.relayBlockPositionOffsetYField);
		this.relayBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 185, 100, 20, Text.translatable(""));
		this.relayBlockPositionOffsetZField.setMaxLength(128);
		this.relayBlockPositionOffsetZField.setText(Integer.toString(this.useRelayChestBlockEntity.getRelayBlockPositionOffset().getZ()));
		this.addSelectableChild(this.relayBlockPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 209, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 209, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string2 = this.triggeredBlockPositionOffsetXField.getText();
		String string3 = this.triggeredBlockPositionOffsetYField.getText();
		String string4 = this.triggeredBlockPositionOffsetZField.getText();
		String string5 = this.keyIdentifierStringField.getText();
		String string6 = this.lockedMessageField.getText();
		String string7 = this.lockedSoundField.getText();
		String string8 = this.unlockedMessageField.getText();
		String string9 = this.unlockedSoundField.getText();
		String string10 = this.relayBlockPositionOffsetXField.getText();
		String string11 = this.relayBlockPositionOffsetYField.getText();
		String string12 = this.relayBlockPositionOffsetZField.getText();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setText(string2);
		this.triggeredBlockPositionOffsetYField.setText(string3);
		this.triggeredBlockPositionOffsetZField.setText(string4);
		this.keyIdentifierStringField.setText(string5);
		this.lockedMessageField.setText(string6);
		this.lockedSoundField.setText(string7);
		this.unlockedMessageField.setText(string8);
		this.unlockedSoundField.setText(string9);
		this.relayBlockPositionOffsetXField.setText(string10);
		this.relayBlockPositionOffsetYField.setText(string11);
		this.relayBlockPositionOffsetZField.setText(string12);
		this.triggeredBlockResets = bl;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private boolean updateRedstoneTriggerBlock() {
		ClientPlayNetworking.send(new UpdateUseRelayChestBlockPacket(
				this.useRelayChestBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				this.keyIdentifierStringField.getText(),
				this.lockedMessageField.getText(),
				this.lockedSoundField.getText(),
				this.unlockedMessageField.getText(),
				this.unlockedSoundField.getText(),
				new BlockPos(
						ItemUtils.parseInt(this.relayBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.relayBlockPositionOffsetZField.getText())
				)
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 30, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, KEY_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 - 153, 65, 0xA0A0A0);
		this.keyIdentifierStringField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, LOCKED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 100, 0xA0A0A0);
		this.lockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, LOCKED_SOUND_LABEL_TEXT, this.width / 2 + 5, 100, 0xA0A0A0);
		this.lockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UNLOCK_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 135, 0xA0A0A0);
		this.unlockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UNLOCK_SOUND_LABEL_TEXT, this.width / 2 + 5, 135, 0xA0A0A0);
		this.unlockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, RELAY_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 175, 0xA0A0A0);
		this.relayBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.relayBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
