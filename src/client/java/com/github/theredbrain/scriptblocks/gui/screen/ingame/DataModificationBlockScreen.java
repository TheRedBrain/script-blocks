package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataModificationBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataWritingBlockPacket;
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

@Environment(value = EnvType.CLIENT)
public class DataModificationBlockScreen extends Screen {
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private static final Text ADDED_INTEGER_VALUE_LABEL_TEXT = Text.translatable("gui.data_modification_block.added_integer_value_label");
	private final DataModificationBlockEntity dataModificationBlockEntity;
	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private CyclingButtonWidget<DataModificationBlockEntity.DataModificationMode> cycleDataModificationModeButton;
	private TextFieldWidget addedIntegerValueField;
	private DataModificationBlockEntity.DataModificationMode dataModificationMode;

	public DataModificationBlockScreen(DataModificationBlockEntity dataModificationBlockEntity) {
		super(NarratorManager.EMPTY);
		this.dataModificationBlockEntity = dataModificationBlockEntity;
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
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.dataModificationBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.dataModificationMode = this.dataModificationBlockEntity.getDataModificationMode();
		this.cycleDataModificationModeButton = this.addDrawableChild(CyclingButtonWidget.builder(DataModificationBlockEntity.DataModificationMode::asText).values((DataModificationBlockEntity.DataModificationMode[]) DataModificationBlockEntity.DataModificationMode.values()).initially(this.dataModificationMode).omitKeyText().build(this.width / 2 - 154, 90, 300, 20, Text.empty(), (button, dataModificationMode) -> {
			this.dataModificationMode = dataModificationMode;
			this.updateWidgets();
		}));

		this.addedIntegerValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.addedIntegerValueField.setMaxLength(128);
		this.addedIntegerValueField.setText(Integer.toString(this.dataModificationBlockEntity.getAddedIntegerValue()));
		this.addSelectableChild(this.addedIntegerValueField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.dataProvidingBlockPositionOffsetXField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		DataModificationBlockEntity.DataModificationMode var = this.dataModificationMode;
		String string = this.dataProvidingBlockPositionOffsetXField.getText();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string3 = this.addedIntegerValueField.getText();
		this.init(client, width, height);
		this.dataProvidingBlockPositionOffsetXField.setText(string);
		this.dataProvidingBlockPositionOffsetYField.setText(string1);
		this.dataProvidingBlockPositionOffsetZField.setText(string2);
		this.addedIntegerValueField.setText(string3);
		this.dataModificationMode = var;
		this.cycleDataModificationModeButton.setValue(this.dataModificationMode);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, ADDED_INTEGER_VALUE_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
		this.addedIntegerValueField.render(context, mouseX, mouseY, delta);

	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataWritingBlock() {
		ClientPlayNetworking.send(new UpdateDataModificationBlockPacket(
				this.dataModificationBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				this.dataModificationMode.asString(),
				ItemUtils.parseInt(this.addedIntegerValueField.getText())
		));
		return true;
	}
}
