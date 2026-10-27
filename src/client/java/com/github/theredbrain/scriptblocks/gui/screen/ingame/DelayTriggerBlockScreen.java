package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDelayTriggerBlockPacket;
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
public class DelayTriggerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component TRIGGER_DELAY_LABEL_TEXT = Component.translatable("gui.delay_trigger_block.triggered_block_trigger_delay");
	private final DelayTriggerBlockEntity delayTriggerBlock;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private EditBox triggerDelayField;

	public DelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
		super(GameNarrator.NO_TITLE);
		this.delayTriggerBlock = delayTriggerBlock;
	}

	@Override
	protected void init() {
		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.delayTriggerBlock.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.delayTriggerBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 80, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.triggerDelayField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.triggerDelayField.setMaxLength(128);
		this.triggerDelayField.setValue(Integer.toString(this.delayTriggerBlock.getTriggerDelay()));
		this.addWidget(this.triggerDelayField);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 145, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 145, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.triggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.triggeredBlockPositionOffsetXField.getValue();
		String string1 = this.triggeredBlockPositionOffsetYField.getValue();
		String string2 = this.triggeredBlockPositionOffsetZField.getValue();
		String string3 = this.triggerDelayField.getValue();
		boolean bl = this.triggeredBlockResets;
		this.init(client, width, height);
		this.triggeredBlockPositionOffsetXField.setValue(string);
		this.triggeredBlockPositionOffsetYField.setValue(string1);
		this.triggeredBlockPositionOffsetZField.setValue(string2);
		this.triggerDelayField.setValue(string3);
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

		context.drawString(this.font, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, TRIGGER_DELAY_LABEL_TEXT, this.width / 2 - 49, 105, 0xA0A0A0);
		this.triggerDelayField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDelayTriggerBlockPacket(
				this.delayTriggerBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				ItemUtils.parseInt(this.triggerDelayField.getValue())
		));
		return true;
	}
}
