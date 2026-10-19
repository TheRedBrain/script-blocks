package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDamageDealingBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@Environment(value = EnvType.CLIENT)
public class TriggeredDamageDealingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text CYCLE_SELECTION_MODE_TOOLTIP_TEXT = Text.translatable("gui.triggered_damage_dealing_block.cycle_selection_mode_button.tooltip");
	private static final Text SHOW_AREA_TRUE_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.show_area_true_label");
	private static final Text SHOW_AREA_FALSE_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.show_area_false_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.area_position_offset_label");
	private static final Text UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.uuid_list_provider.uuid_list_provider_position_offset");
	private static final Text EXCEPTION_TAG_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.exception_tag_identifier_label");
	private static final Text DAMAGE_TYPE_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.damage_type_identifier_label");
	private static final Text DAMAGE_AMOUNT_LABEL_TEXT = Text.translatable("gui.triggered_damage_dealing_block.damage_amount_label");

	private final TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity;

	private CyclingButtonWidget<TriggeredDamageDealingBlockEntity.SelectionMode> cycleSelectionModeButton;
	private TriggeredDamageDealingBlockEntity.SelectionMode selectionMode = TriggeredDamageDealingBlockEntity.SelectionMode.AREA;

	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;

	private TextFieldWidget uuidListHandlerPositionOffsetXField;
	private TextFieldWidget uuidListHandlerPositionOffsetYField;
	private TextFieldWidget uuidListHandlerPositionOffsetZField;

	private TextFieldWidget exceptionTagIdentifierField;
	private TextFieldWidget damageTypeIdentifierField;
	private TextFieldWidget damageAmountField;

	private boolean showArea;

	public TriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
		super(NarratorManager.EMPTY);
		this.triggeredDamageDealingBlockEntity = triggeredDamageDealingBlockEntity;
	}

	@Override
	protected void init() {

		super.init();

		this.selectionMode = this.triggeredDamageDealingBlockEntity.getSelectionMode();
		this.cycleSelectionModeButton = CyclingButtonWidget.builder(TriggeredDamageDealingBlockEntity.SelectionMode::asText).values(TriggeredDamageDealingBlockEntity.SelectionMode.values()).initially(this.selectionMode).omitKeyText().build(this.width / 2 - 154, 20, 150, 20, Text.empty(), (button, selectionMode) -> {
			this.selectionMode = selectionMode;
			button.setTooltip(Tooltip.of(CYCLE_SELECTION_MODE_TOOLTIP_TEXT));
		});
		this.cycleSelectionModeButton.setTooltip(Tooltip.of(CYCLE_SELECTION_MODE_TOOLTIP_TEXT));
		this.addDrawableChild(this.cycleSelectionModeButton);

		this.showArea = this.triggeredDamageDealingBlockEntity.showArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(SHOW_AREA_TRUE_LABEL_TEXT, SHOW_AREA_FALSE_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 + 4, 20, 150, 20, Text.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 55, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 55, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 90, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 90, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);

		this.uuidListHandlerPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetXField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getX()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetXField);

		this.uuidListHandlerPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 125, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetYField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getY()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetYField);

		this.uuidListHandlerPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 125, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetZField.setText(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getZ()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetZField);

		this.exceptionTagIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 160, 300, 20, Text.empty());
		this.exceptionTagIdentifierField.setMaxLength(128);
		this.exceptionTagIdentifierField.setText(this.triggeredDamageDealingBlockEntity.getExceptionTagIdentifierString());
		this.addSelectableChild(this.exceptionTagIdentifierField);

		this.damageTypeIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 195, 250, 20, Text.empty());
		this.damageTypeIdentifierField.setMaxLength(128);
		this.damageTypeIdentifierField.setText(this.triggeredDamageDealingBlockEntity.getDamageTypeIdentifierString());
		this.addSelectableChild(this.damageTypeIdentifierField);

		this.damageAmountField = new TextFieldWidget(this.textRenderer, this.width / 2 + 104, 195, 50, 20, Text.empty());
		this.damageAmountField.setMaxLength(128);
		this.damageAmountField.setText(Float.toString(this.triggeredDamageDealingBlockEntity.getDamageAmount()));
		this.addSelectableChild(this.damageAmountField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 219, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 219, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleSelectionModeButton);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		boolean bool = this.showArea;
		TriggeredDamageDealingBlockEntity.SelectionMode var = this.selectionMode;
		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();
		String string6 = this.uuidListHandlerPositionOffsetXField.getText();
		String string7 = this.uuidListHandlerPositionOffsetYField.getText();
		String string8 = this.uuidListHandlerPositionOffsetZField.getText();
		String string9 = this.exceptionTagIdentifierField.getText();
		String string10 = this.damageTypeIdentifierField.getText();
		String string11 = this.damageAmountField.getText();
		this.init(client, width, height);
		this.showArea = bool;
		this.toggleShowAreaButton.setValue(this.showArea);
		this.selectionMode = var;
		this.cycleSelectionModeButton.setValue(this.selectionMode);
		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);
		this.uuidListHandlerPositionOffsetXField.setText(string6);
		this.uuidListHandlerPositionOffsetYField.setText(string7);
		this.uuidListHandlerPositionOffsetZField.setText(string8);
		this.exceptionTagIdentifierField.setText(string9);
		this.damageTypeIdentifierField.setText(string10);
		this.damageAmountField.setText(string11);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
		this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
		this.uuidListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, EXCEPTION_TAG_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
		this.exceptionTagIdentifierField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, DAMAGE_TYPE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
		this.damageTypeIdentifierField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, DAMAGE_AMOUNT_LABEL_TEXT, this.width / 2 + 105, 185, 0xA0A0A0);
		this.damageAmountField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredDamageDealingBlockPacket(
				this.triggeredDamageDealingBlockEntity.getPos(),
				this.selectionMode.asString(),
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
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetZField.getText())
				),
				this.exceptionTagIdentifierField.getText(),
				this.damageTypeIdentifierField.getText(),
				ItemUtils.parseFloat(this.damageAmountField.getText())
		));
		return true;
	}
}
