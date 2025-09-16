package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredEntityRemoverBlockPacket;
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
public class TriggeredDamageDealingBlockScreen extends Screen {
	private static final Text HIDE_AREA_LABEL_TEXT = Text.translatable("gui.triggered_entity_remover_block.hide_area_label");
	private static final Text SHOW_AREA_LABEL_TEXT = Text.translatable("gui.triggered_entity_remover_block.show_area_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.triggered_entity_remover_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.triggered_entity_remover_block.area_position_offset_label");

	private final TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity;

	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private boolean showArea;

	private ButtonWidget doneButton;
	private ButtonWidget cancelButton;

	public TriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
		super(NarratorManager.EMPTY);
		this.triggeredDamageDealingBlockEntity = triggeredDamageDealingBlockEntity;
	}

	private void done() {
		this.updateBossControllerBlock();
		this.close();
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		super.init();

		this.showArea = this.triggeredDamageDealingBlockEntity.showArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 54, 300, 20, Text.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 89, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 89, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 89, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 124, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 124, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 124, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {

		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();

		this.init(client, width, height);

		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
		this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 114, 0xA0A0A0);
		this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

	}

	private void updateBossControllerBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredEntityRemoverBlockPacket(
				this.triggeredDamageDealingBlockEntity.getPos(),
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
				)
		));
	}
}
