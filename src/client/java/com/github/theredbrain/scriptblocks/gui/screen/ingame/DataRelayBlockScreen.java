package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataRelayBlockPacket;
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
public class DataRelayBlockScreen extends Screen {
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private final DataRelayBlockEntity dataRelayBlockEntity;
	private TextFieldWidget dataProvidingBlockPosOffsetXField;
	private TextFieldWidget dataProvidingBlockPosOffsetYField;
	private TextFieldWidget dataProvidingBlockPosOffsetZField;

	public DataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
		super(NarratorManager.EMPTY);
		this.dataRelayBlockEntity = dataRelayBlockEntity;
	}

	private void done() {
		if (this.updateDataRelayBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.dataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setText(Integer.toString(this.dataRelayBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setText(Integer.toString(this.dataRelayBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setText(Integer.toString(this.dataRelayBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.dataProvidingBlockPosOffsetXField.getText();
		String string1 = this.dataProvidingBlockPosOffsetYField.getText();
		String string2 = this.dataProvidingBlockPosOffsetZField.getText();
		this.init(client, width, height);
		this.dataProvidingBlockPosOffsetXField.setText(string);
		this.dataProvidingBlockPosOffsetYField.setText(string1);
		this.dataProvidingBlockPosOffsetZField.setText(string2);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
		this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataRelayBlock() {
		ClientPlayNetworking.send(new UpdateDataRelayBlockPacket(
				this.dataRelayBlockEntity.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getText())
				)
		));
		return true;
	}
}
