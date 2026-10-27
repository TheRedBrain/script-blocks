package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredAdvancementCheckerBlockPacket;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value = EnvType.CLIENT)
public class TriggeredAdvancementCheckerBlockScreen extends Screen {
	private static final Component FIRST_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_advancement_checker_block.first_triggered_block_position_offset_label");
	private static final Component SECOND_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_advancement_checker_block.second_triggered_block_position_offset_label");
	private static final Component CHECKED_ADVANCEMENT_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.triggered_advancement_checker_block.checked_advancement_identifier_label");
	private final TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock;
	private EditBox firstTriggeredBlockPositionOffsetXField;
	private EditBox firstTriggeredBlockPositionOffsetYField;
	private EditBox firstTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleFirstTriggeredBlockResetsButton;
	private boolean firstTriggeredBlockResets;
	private EditBox secondTriggeredBlockPositionOffsetXField;
	private EditBox secondTriggeredBlockPositionOffsetYField;
	private EditBox secondTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleSecondTriggeredBlockResetsButton;
	private boolean secondTriggeredBlockResets;
	private EditBox checkedAdvancementIdentifierField;

	public TriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
		super(GameNarrator.NO_TITLE);
		this.triggeredAdvancementCheckerBlock = triggeredAdvancementCheckerBlock;
	}

	private void done() {
		if (this.updateTriggeredAdvancementCheckerBlock()) {
			this.onClose();
		}
	}

	private void cancel() {
		this.onClose();
	}

	@Override
	protected void init() {
		this.firstTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 70, 50, 20, Component.empty());
		this.firstTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getFirstTriggeredBlock().getLeft().getX()));
		this.addWidget(this.firstTriggeredBlockPositionOffsetXField);
		this.firstTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 70, 50, 20, Component.empty());
		this.firstTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getFirstTriggeredBlock().getLeft().getY()));
		this.addWidget(this.firstTriggeredBlockPositionOffsetYField);
		this.firstTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 70, 50, 20, Component.empty());
		this.firstTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getFirstTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.firstTriggeredBlockPositionOffsetZField);
		this.firstTriggeredBlockResets = this.triggeredAdvancementCheckerBlock.getFirstTriggeredBlock().getRight();
		this.toggleFirstTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.firstTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 70, 150, 20, Component.empty(), (button, firstTriggeredBlockResets) -> {
			this.firstTriggeredBlockResets = firstTriggeredBlockResets;
		}));

		this.secondTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 105, 50, 20, Component.empty());
		this.secondTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getSecondTriggeredBlock().getLeft().getX()));
		this.addWidget(this.secondTriggeredBlockPositionOffsetXField);
		this.secondTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 105, 50, 20, Component.empty());
		this.secondTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getSecondTriggeredBlock().getLeft().getY()));
		this.addWidget(this.secondTriggeredBlockPositionOffsetYField);
		this.secondTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 105, 50, 20, Component.empty());
		this.secondTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.triggeredAdvancementCheckerBlock.getSecondTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.secondTriggeredBlockPositionOffsetZField);
		this.secondTriggeredBlockResets = this.triggeredAdvancementCheckerBlock.getSecondTriggeredBlock().getRight();
		this.toggleSecondTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.secondTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 105, 150, 20, Component.empty(), (button, secondTriggeredBlockResets) -> {
			this.secondTriggeredBlockResets = secondTriggeredBlockResets;
		}));

		this.checkedAdvancementIdentifierField = new EditBox(this.font, this.width / 2 - 154, 140, 300, 20, Component.empty());
		this.checkedAdvancementIdentifierField.setMaxLength(128);
		this.checkedAdvancementIdentifierField.setValue(this.triggeredAdvancementCheckerBlock.getCheckedAdvancementIdentifier());
		this.addWidget(this.checkedAdvancementIdentifierField);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 164, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 164, 150, 20).build());
		this.setInitialFocus(this.firstTriggeredBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.firstTriggeredBlockPositionOffsetXField.getValue();
		String string1 = this.firstTriggeredBlockPositionOffsetYField.getValue();
		String string2 = this.firstTriggeredBlockPositionOffsetZField.getValue();
		String string3 = this.secondTriggeredBlockPositionOffsetXField.getValue();
		String string4 = this.secondTriggeredBlockPositionOffsetYField.getValue();
		String string5 = this.secondTriggeredBlockPositionOffsetZField.getValue();
		String string6 = this.checkedAdvancementIdentifierField.getValue();
		boolean bl = this.firstTriggeredBlockResets;
		boolean bl2 = this.secondTriggeredBlockResets;
		this.init(client, width, height);
		this.firstTriggeredBlockPositionOffsetXField.setValue(string);
		this.firstTriggeredBlockPositionOffsetYField.setValue(string1);
		this.firstTriggeredBlockPositionOffsetZField.setValue(string2);
		this.secondTriggeredBlockPositionOffsetXField.setValue(string3);
		this.secondTriggeredBlockPositionOffsetYField.setValue(string4);
		this.secondTriggeredBlockPositionOffsetZField.setValue(string5);
		this.checkedAdvancementIdentifierField.setValue(string6);
		this.firstTriggeredBlockResets = bl;
		this.secondTriggeredBlockResets = bl2;
	}

	private boolean updateTriggeredAdvancementCheckerBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredAdvancementCheckerBlockPacket(
				this.triggeredAdvancementCheckerBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetZField.getValue())
				),
				this.firstTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetZField.getValue())
				),
				this.secondTriggeredBlockResets,
				this.checkedAdvancementIdentifierField.getValue()
		));
		return true;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, FIRST_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 60, 0xA0A0A0);
		this.firstTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.firstTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.firstTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, SECOND_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 95, 0xA0A0A0);
		this.secondTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.secondTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.secondTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, CHECKED_ADVANCEMENT_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 130, 0xA0A0A0);
		this.checkedAdvancementIdentifierField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
