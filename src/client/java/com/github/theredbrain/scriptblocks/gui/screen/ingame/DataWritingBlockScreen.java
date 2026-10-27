package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataWritingBlockPacket;
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
public class DataWritingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private final DataWritingBlockEntity dataWritingBlock;
	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;
	private EditBox dataIdentifierField;
	private EditBox newDataValueField;

	public DataWritingBlockScreen(DataWritingBlockEntity dataWritingBlock) {
		super(GameNarrator.NO_TITLE);
		this.dataWritingBlock = dataWritingBlock;
	}

	@Override
	protected void init() {

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.dataWritingBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new EditBox(this.font, this.width / 2 - 154, 90, 300, 20, Component.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setValue(this.dataWritingBlock.getDataIdentifier());
		this.addWidget(this.dataIdentifierField);

		this.newDataValueField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.newDataValueField.setMaxLength(128);
		this.newDataValueField.setValue(this.dataWritingBlock.getNewDataValue());
		this.addWidget(this.newDataValueField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.dataProvidingBlockPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getValue();
		String string3 = this.dataIdentifierField.getValue();
		String string4 = this.newDataValueField.getValue();
		this.init(client, width, height);
		this.dataProvidingBlockPositionOffsetXField.setValue(string);
		this.dataProvidingBlockPositionOffsetYField.setValue(string1);
		this.dataProvidingBlockPositionOffsetZField.setValue(string2);
		this.dataIdentifierField.setValue(string3);
		this.newDataValueField.setValue(string4);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

		this.newDataValueField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDataWritingBlockPacket(
				this.dataWritingBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				),
				this.dataIdentifierField.getValue(),
				this.newDataValueField.getValue()
		));
		return true;
	}
}
