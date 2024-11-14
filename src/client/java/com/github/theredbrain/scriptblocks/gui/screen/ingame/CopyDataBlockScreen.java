package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateCopyDataBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataWritingBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@Environment(value = EnvType.CLIENT)
public class CopyDataBlockScreen extends Screen {
	private static final Text FIRST_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.copy_data_block.first_data_providing_block_position_offset");
	private static final Text SECOND_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.copy_data_block.second_data_providing_block_position_offset");
	private static final Text DATA_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.data_provider_block.data_identifier");
	private final CopyDataBlockEntity copyDataBlock;
	private TextFieldWidget firstDataProvidingBlockPositionOffsetXField;
	private TextFieldWidget firstDataProvidingBlockPositionOffsetYField;
	private TextFieldWidget firstDataProvidingBlockPositionOffsetZField;
	private TextFieldWidget secondDataProvidingBlockPositionOffsetXField;
	private TextFieldWidget secondDataProvidingBlockPositionOffsetYField;
	private TextFieldWidget secondDataProvidingBlockPositionOffsetZField;
	private TextFieldWidget dataIdentifierField;

	public CopyDataBlockScreen(CopyDataBlockEntity copyDataBlock) {
		super(NarratorManager.EMPTY);
		this.copyDataBlock = copyDataBlock;
	}

	private void done() {
		if (this.updateDataWritingBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.firstDataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 100, 20, Text.empty());
		this.firstDataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.firstDataProvidingBlockPositionOffsetXField);
		this.firstDataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 55, 100, 20, Text.empty());
		this.firstDataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.firstDataProvidingBlockPositionOffsetYField);
		this.firstDataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 46, 55, 100, 20, Text.empty());
		this.firstDataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.firstDataProvidingBlockPositionOffsetZField);

		this.secondDataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 100, 20, Text.empty());
		this.secondDataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.secondDataProvidingBlockPositionOffsetXField);
		this.secondDataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 90, 100, 20, Text.empty());
		this.secondDataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.secondDataProvidingBlockPositionOffsetYField);
		this.secondDataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 46, 90, 100, 20, Text.empty());
		this.secondDataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.secondDataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 300, 20, Text.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setText(this.copyDataBlock.getDataIdentifier());
		this.addSelectableChild(this.dataIdentifierField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.firstDataProvidingBlockPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.firstDataProvidingBlockPositionOffsetXField.getText();
		String string1 = this.firstDataProvidingBlockPositionOffsetYField.getText();
		String string2 = this.firstDataProvidingBlockPositionOffsetZField.getText();
		String string3 = this.secondDataProvidingBlockPositionOffsetXField.getText();
		String string4 = this.secondDataProvidingBlockPositionOffsetYField.getText();
		String string5 = this.secondDataProvidingBlockPositionOffsetZField.getText();
		String string6 = this.dataIdentifierField.getText();
		this.init(client, width, height);
		this.firstDataProvidingBlockPositionOffsetXField.setText(string);
		this.firstDataProvidingBlockPositionOffsetYField.setText(string1);
		this.firstDataProvidingBlockPositionOffsetZField.setText(string2);
		this.secondDataProvidingBlockPositionOffsetXField.setText(string3);
		this.secondDataProvidingBlockPositionOffsetYField.setText(string4);
		this.secondDataProvidingBlockPositionOffsetZField.setText(string5);
		this.dataIdentifierField.setText(string6);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, FIRST_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.firstDataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, SECOND_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
		this.secondDataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataWritingBlock() {
		ClientPlayNetworking.send(new UpdateCopyDataBlockPacket(
				this.copyDataBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetZField.getText())
				),
				this.dataIdentifierField.getText()
		));
		return true;
	}
}
