package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDelayTriggerBlockPacket;
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
public class DelayTriggerBlockScreen extends Screen {
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffset");
	private static final Text TRIGGER_DELAY_LABEL_TEXT = Text.translatable("gui.delay_trigger_block.triggeredBlockTriggerDelay");
	private final DelayTriggerBlockEntity delayTriggerBlock;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private TextFieldWidget triggerDelayField;

	public DelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
		super(NarratorManager.EMPTY);
		this.delayTriggerBlock = delayTriggerBlock;
	}

	private void done() {
		if (this.updateDelayTriggerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.delayTriggerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 80, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.triggerDelayField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.triggerDelayField.setMaxLength(128);
		this.triggerDelayField.setText(Integer.toString(this.delayTriggerBlock.getTriggerDelay()));
		this.addSelectableChild(this.triggerDelayField);
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 145, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 145, 150, 20).build());
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.triggeredBlockPositionOffsetXField.getText();
		String string1 = this.triggeredBlockPositionOffsetYField.getText();
		String string2 = this.triggeredBlockPositionOffsetZField.getText();
		boolean bl = this.triggeredBlockResets;
		String string3 = this.triggerDelayField.getText();
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setText(string);
		this.triggeredBlockPositionOffsetYField.setText(string1);
		this.triggeredBlockPositionOffsetZField.setText(string2);
		this.triggeredBlockResets = bl;
		this.triggerDelayField.setText(string3);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private boolean updateDelayTriggerBlock() {
		ClientPlayNetworking.send(new UpdateDelayTriggerBlockPacket(
				this.delayTriggerBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				ItemUtils.parseInt(this.triggerDelayField.getText())
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		this.renderBackground(context);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, TRIGGER_DELAY_LABEL_TEXT, this.width / 2 - 49, 105, 0xA0A0A0);
		this.triggerDelayField.render(context, mouseX, mouseY, delta);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
