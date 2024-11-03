package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
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
public class DataWritingBlockScreen extends Screen {
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private final DataWritingBlockEntity dataWritingBlock;
	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private TextFieldWidget dataIdentifierField;
	private TextFieldWidget newDataValueField;

	public DataWritingBlockScreen(DataWritingBlockEntity dataWritingBlock) {
		super(NarratorManager.EMPTY);
		this.dataWritingBlock = dataWritingBlock;
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

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 300, 20, Text.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setText(this.dataWritingBlock.getDataIdentifier());
		this.addSelectableChild(this.dataIdentifierField);

		this.newDataValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.newDataValueField.setMaxLength(128);
		this.newDataValueField.setText(this.dataWritingBlock.getNewDataValue());
		this.addSelectableChild(this.newDataValueField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.dataProvidingBlockPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.dataProvidingBlockPositionOffsetXField.getText();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string3 = this.dataIdentifierField.getText();
		String string4 = this.newDataValueField.getText();
		this.init(client, width, height);
		this.dataProvidingBlockPositionOffsetXField.setText(string);
		this.dataProvidingBlockPositionOffsetYField.setText(string1);
		this.dataProvidingBlockPositionOffsetZField.setText(string2);
		this.dataIdentifierField.setText(string3);
		this.newDataValueField.setText(string4);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

		this.newDataValueField.render(context, mouseX, mouseY, delta);

	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataWritingBlock() {
		ClientPlayNetworking.send(new UpdateDataWritingBlockPacket(
				this.dataWritingBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				this.dataIdentifierField.getText(),
				this.newDataValueField.getText()
		));
		return true;
	}
}
