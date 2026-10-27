package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateCopyDataBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value = EnvType.CLIENT)
public class CopyDataBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component FIRST_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.copy_data_block.first_data_providing_block_position_offset");
	private static final Component SECOND_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.copy_data_block.second_data_providing_block_position_offset");
	private static final Component DATA_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_identifier");
	private final CopyDataBlockEntity copyDataBlock;
	private EditBox firstDataProvidingBlockPositionOffsetXField;
	private EditBox firstDataProvidingBlockPositionOffsetYField;
	private EditBox firstDataProvidingBlockPositionOffsetZField;
	private EditBox secondDataProvidingBlockPositionOffsetXField;
	private EditBox secondDataProvidingBlockPositionOffsetYField;
	private EditBox secondDataProvidingBlockPositionOffsetZField;
	private EditBox dataIdentifierField;

	public CopyDataBlockScreen(CopyDataBlockEntity copyDataBlock) {
		super(GameNarrator.NO_TITLE);
		this.copyDataBlock = copyDataBlock;
	}

	@Override
	protected void init() {

		this.firstDataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 100, 20, Component.empty());
		this.firstDataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.firstDataProvidingBlockPositionOffsetXField);
		this.firstDataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 54, 55, 100, 20, Component.empty());
		this.firstDataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.firstDataProvidingBlockPositionOffsetYField);
		this.firstDataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 46, 55, 100, 20, Component.empty());
		this.firstDataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.firstDataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.copyDataBlock.getFirstDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.firstDataProvidingBlockPositionOffsetZField);

		this.secondDataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 100, 20, Component.empty());
		this.secondDataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.secondDataProvidingBlockPositionOffsetXField);
		this.secondDataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 54, 90, 100, 20, Component.empty());
		this.secondDataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.secondDataProvidingBlockPositionOffsetYField);
		this.secondDataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 46, 90, 100, 20, Component.empty());
		this.secondDataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.secondDataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.copyDataBlock.getSecondDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.secondDataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new EditBox(this.font, this.width / 2 - 154, 125, 300, 20, Component.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setValue(this.copyDataBlock.getDataIdentifier());
		this.addWidget(this.dataIdentifierField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.firstDataProvidingBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.firstDataProvidingBlockPositionOffsetXField.getValue();
		String string1 = this.firstDataProvidingBlockPositionOffsetYField.getValue();
		String string2 = this.firstDataProvidingBlockPositionOffsetZField.getValue();
		String string3 = this.secondDataProvidingBlockPositionOffsetXField.getValue();
		String string4 = this.secondDataProvidingBlockPositionOffsetYField.getValue();
		String string5 = this.secondDataProvidingBlockPositionOffsetZField.getValue();
		String string6 = this.dataIdentifierField.getValue();
		this.init(client, width, height);
		this.firstDataProvidingBlockPositionOffsetXField.setValue(string);
		this.firstDataProvidingBlockPositionOffsetYField.setValue(string1);
		this.firstDataProvidingBlockPositionOffsetZField.setValue(string2);
		this.secondDataProvidingBlockPositionOffsetXField.setValue(string3);
		this.secondDataProvidingBlockPositionOffsetYField.setValue(string4);
		this.secondDataProvidingBlockPositionOffsetZField.setValue(string5);
		this.dataIdentifierField.setValue(string6);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, FIRST_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.firstDataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.firstDataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, SECOND_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
		this.secondDataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.secondDataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateCopyDataBlockPacket(
				this.copyDataBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.firstDataProvidingBlockPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.secondDataProvidingBlockPositionOffsetZField.getValue())
				),
				this.dataIdentifierField.getValue()
		));
		return true;
	}
}
