package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveTriggerBlockPacket;
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
public class InteractiveTriggerBlockScreen extends Screen {
	private static final Text TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	private static final Text KEY_ITEM_TAG_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.key_item_tag_label");
	private static final Text LOCKED_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_message_label");
	private static final Text LOCKED_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.locked_sound_label");
	private static final Text UNLOCK_MESSAGE_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_message_label");
	private static final Text UNLOCK_SOUND_LABEL_TEXT = Text.translatable("gui.interactive_trigger_block.unlock_sound_label");
	private final InteractiveTriggerBlockEntity interactiveTriggerBlockEntity;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private boolean triggeredBlockResets;
	private TextFieldWidget keyItemTagField;
	private TextFieldWidget lockedMessageField;
	private TextFieldWidget lockedSoundField;
	private TextFieldWidget unlockedMessageField;
	private TextFieldWidget unlockedSoundField;

	public InteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
		super(NarratorManager.EMPTY);
		this.interactiveTriggerBlockEntity = interactiveTriggerBlockEntity;
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
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 40, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.interactiveTriggerBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.interactiveTriggerBlockEntity.getTriggeredBlock().getRight();
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 40, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.keyItemTagField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 75, 300, 20, Text.empty());
		this.keyItemTagField.setMaxLength(128);
		this.keyItemTagField.setText(this.interactiveTriggerBlockEntity.getKeyItemTag());
		this.addSelectableChild(this.keyItemTagField);

		this.lockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 110, 150, 20, Text.empty());
		this.lockedMessageField.setMaxLength(128);
		this.lockedMessageField.setText(this.interactiveTriggerBlockEntity.getLockedMessage());
		this.addSelectableChild(this.lockedMessageField);

		this.lockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 110, 150, 20, Text.empty());
		this.lockedSoundField.setMaxLength(128);
		this.lockedSoundField.setText(this.interactiveTriggerBlockEntity.getLockedSound());
		this.addSelectableChild(this.lockedSoundField);

		this.unlockedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 145, 150, 20, Text.empty());
		this.unlockedMessageField.setMaxLength(128);
		this.unlockedMessageField.setText(this.interactiveTriggerBlockEntity.getUnlockedMessage());
		this.addSelectableChild(this.unlockedMessageField);

		this.unlockedSoundField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 145, 150, 20, Text.empty());
		this.unlockedSoundField.setMaxLength(128);
		this.unlockedSoundField.setText(this.interactiveTriggerBlockEntity.getUnlockedSound());
		this.addSelectableChild(this.unlockedSoundField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 169, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 169, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string2 = this.triggeredBlockPositionOffsetXField.getText();
		String string3 = this.triggeredBlockPositionOffsetYField.getText();
		String string4 = this.triggeredBlockPositionOffsetZField.getText();
		String string5 = this.keyItemTagField.getText();
		String string6 = this.lockedMessageField.getText();
		String string7 = this.lockedSoundField.getText();
		String string8 = this.unlockedMessageField.getText();
		String string9 = this.unlockedSoundField.getText();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setText(string2);
		this.triggeredBlockPositionOffsetYField.setText(string3);
		this.triggeredBlockPositionOffsetZField.setText(string4);
		this.keyItemTagField.setText(string5);
		this.lockedMessageField.setText(string6);
		this.lockedSoundField.setText(string7);
		this.unlockedMessageField.setText(string8);
		this.unlockedSoundField.setText(string9);
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
		ClientPlayNetworking.send(new UpdateInteractiveTriggerBlockPacket(
				this.interactiveTriggerBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				this.keyItemTagField.getText(),
				this.lockedMessageField.getText(),
				this.lockedSoundField.getText(),
				this.unlockedMessageField.getText(),
				this.unlockedSoundField.getText()
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

		context.drawTextWithShadow(this.textRenderer, KEY_ITEM_TAG_LABEL_TEXT, this.width / 2 - 153, 65, 0xA0A0A0);
		this.keyItemTagField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, LOCKED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 100, 0xA0A0A0);
		this.lockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, LOCKED_SOUND_LABEL_TEXT, this.width / 2 + 5, 100, 0xA0A0A0);
		this.lockedSoundField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UNLOCK_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 135, 0xA0A0A0);
		this.unlockedMessageField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, UNLOCK_SOUND_LABEL_TEXT, this.width / 2 + 5, 135, 0xA0A0A0);
		this.unlockedSoundField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
