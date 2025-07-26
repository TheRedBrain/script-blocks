package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaFillerBlockPacket;
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
import net.minecraft.util.math.Vec3i;

@Environment(value = EnvType.CLIENT)
public class AreaFillerBlockScreen extends Screen {
	private static final Text HIDE_AREA_LABEL_TEXT = Text.translatable("gui.area_block.hide_area_label");
	private static final Text SHOW_AREA_LABEL_TEXT = Text.translatable("gui.area_block.show_area_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.area_filler_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.area_filler_block.area_position_offset_label");
	private static final Text BLOCK_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.area_filler_block.block_identifier_label");
	private final AreaFillerBlockEntity areaFillerBlock;

	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private boolean showArea;

	private TextFieldWidget blockIdentifierField;

	public AreaFillerBlockScreen(AreaFillerBlockEntity areaFillerBlock) {
		super(NarratorManager.EMPTY);
		this.areaFillerBlock = areaFillerBlock;
	}

	private void done() {
		if (this.updateAreaFillerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.showArea = this.areaFillerBlock.showArea();
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 54, 300, 20, Text.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 89, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.areaFillerBlock.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 89, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.areaFillerBlock.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 89, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.areaFillerBlock.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 124, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.areaFillerBlock.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 124, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.areaFillerBlock.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 124, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.areaFillerBlock.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);


		this.blockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 159, 300, 20, Text.empty());
		this.blockIdentifierField.setMaxLength(128);
		this.blockIdentifierField.setText(this.areaFillerBlock.getBlockIdentifierString());
		this.addSelectableChild(this.blockIdentifierField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 207, 150, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		boolean bool = this.showArea;
		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();
		String string6 = this.blockIdentifierField.getText();
		this.init(client, width, height);
		this.showArea = bool;
		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);
		this.blockIdentifierField.setText(string6);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 44, 0xA0A0A0);
		this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
		this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, BLOCK_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 114, 0xA0A0A0);
		this.blockIdentifierField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateAreaFillerBlock() {
		ClientPlayNetworking.send(new UpdateAreaFillerBlockPacket(
				this.areaFillerBlock.getPos(),
				this.showArea,
				new Vec3i(
						ItemUtils.parseInt(this.areaDimensionsXField.getText()),
						ItemUtils.parseInt(this.areaDimensionsYField.getText()),
						ItemUtils.parseInt(this.areaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.areaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.areaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.areaPositionOffsetZField.getText())
				),
				this.blockIdentifierField.getText()
		));
		return true;
	}
}
