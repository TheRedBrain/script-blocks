package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataModificationBlockPacket;
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

@Environment(value = EnvType.CLIENT)
public class DataModificationBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Component ADDED_INTEGER_VALUE_LABEL_TEXT = Component.translatable("gui.data_modification_block.added_integer_value_label");
	private static final Component INTEGER_MODULUS_VALUE_LABEL_TEXT = Component.translatable("gui.data_modification_block.integer_modulus_value_label");
	private final DataModificationBlockEntity dataModificationBlockEntity;
	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;
	private CycleButton<DataModificationBlockEntity.DataModificationMode> cycleDataModificationModeButton;
	private EditBox addedIntegerValueField;
	private EditBox integerModulusValueField;
	private DataModificationBlockEntity.DataModificationMode dataModificationMode;

	public DataModificationBlockScreen(DataModificationBlockEntity dataModificationBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.dataModificationBlockEntity = dataModificationBlockEntity;
	}

	@Override
	protected void init() {

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 55, 50, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.dataModificationMode = this.dataModificationBlockEntity.getDataModificationMode();
		this.cycleDataModificationModeButton = this.addRenderableWidget(CycleButton.builder(DataModificationBlockEntity.DataModificationMode::asText).values((DataModificationBlockEntity.DataModificationMode[]) DataModificationBlockEntity.DataModificationMode.values()).initially(this.dataModificationMode).omitKeyText().build(this.width / 2 - 154, 90, 300, 20, Component.empty(), (button, dataModificationMode) -> {
			this.dataModificationMode = dataModificationMode;
			this.updateWidgets();
		}));

		this.addedIntegerValueField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.addedIntegerValueField.setMaxLength(128);
		this.addedIntegerValueField.setValue(Integer.toString(this.dataModificationBlockEntity.getAddedIntegerValue()));
		this.addWidget(this.addedIntegerValueField);

		this.integerModulusValueField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.integerModulusValueField.setMaxLength(128);
		this.integerModulusValueField.setValue(Integer.toString(this.dataModificationBlockEntity.getIntegerModulusValue()));
		this.addWidget(this.integerModulusValueField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.dataProvidingBlockPositionOffsetXField);
	}

	@Override
	protected void updateWidgets() {

		this.addedIntegerValueField.setVisible(false);
		this.integerModulusValueField.setVisible(false);

		if (this.dataModificationMode.equals(DataModificationBlockEntity.DataModificationMode.INTEGER_ADDITION)) {
			this.addedIntegerValueField.setVisible(true);
		} else if (this.dataModificationMode.equals(DataModificationBlockEntity.DataModificationMode.INTEGER_MODULO)) {
			this.integerModulusValueField.setVisible(true);
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		DataModificationBlockEntity.DataModificationMode var = this.dataModificationMode;
		String string = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getValue();
		String string3 = this.addedIntegerValueField.getValue();
		this.init(client, width, height);
		this.dataModificationMode = var;
		this.cycleDataModificationModeButton.setValue(this.dataModificationMode);
		this.dataProvidingBlockPositionOffsetXField.setValue(string);
		this.dataProvidingBlockPositionOffsetYField.setValue(string1);
		this.dataProvidingBlockPositionOffsetZField.setValue(string2);
		this.addedIntegerValueField.setValue(string3);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		if (this.dataModificationMode.equals(DataModificationBlockEntity.DataModificationMode.INTEGER_ADDITION)) {

			context.drawString(this.font, ADDED_INTEGER_VALUE_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.addedIntegerValueField.render(context, mouseX, mouseY, delta);

		} else if (this.dataModificationMode.equals(DataModificationBlockEntity.DataModificationMode.INTEGER_MODULO)) {

			context.drawString(this.font, INTEGER_MODULUS_VALUE_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.integerModulusValueField.render(context, mouseX, mouseY, delta);

		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDataModificationBlockPacket(
				this.dataModificationBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				),
				this.dataModificationMode.asString(),
				ItemUtils.parseInt(this.addedIntegerValueField.getValue()),
				ItemUtils.parseInt(this.integerModulusValueField.getValue())
		));
		return true;
	}
}
