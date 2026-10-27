package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDamageDealingBlockPacket;
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
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value = EnvType.CLIENT)
public class TriggeredDamageDealingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component CYCLE_SELECTION_MODE_TOOLTIP_TEXT = Component.translatable("gui.triggered_damage_dealing_block.cycle_selection_mode_button.tooltip");
	private static final Component SHOW_AREA_TRUE_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.show_area_true_label");
	private static final Component SHOW_AREA_FALSE_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.show_area_false_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.area_position_offset_label");
	private static final Component UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.uuid_list_provider.uuid_list_provider_position_offset");
	private static final Component EXCEPTION_TAG_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.exception_tag_identifier_label");
	private static final Component DAMAGE_TYPE_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.damage_type_identifier_label");
	private static final Component DAMAGE_AMOUNT_LABEL_TEXT = Component.translatable("gui.triggered_damage_dealing_block.damage_amount_label");

	private final TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity;

	private CycleButton<TriggeredDamageDealingBlockEntity.SelectionMode> cycleSelectionModeButton;
	private TriggeredDamageDealingBlockEntity.SelectionMode selectionMode = TriggeredDamageDealingBlockEntity.SelectionMode.AREA;

	private CycleButton<Boolean> toggleShowAreaButton;
	private EditBox areaDimensionsXField;
	private EditBox areaDimensionsYField;
	private EditBox areaDimensionsZField;
	private EditBox areaPositionOffsetXField;
	private EditBox areaPositionOffsetYField;
	private EditBox areaPositionOffsetZField;

	private EditBox uuidListHandlerPositionOffsetXField;
	private EditBox uuidListHandlerPositionOffsetYField;
	private EditBox uuidListHandlerPositionOffsetZField;

	private EditBox exceptionTagIdentifierField;
	private EditBox damageTypeIdentifierField;
	private EditBox damageAmountField;

	private boolean showArea;

	public TriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.triggeredDamageDealingBlockEntity = triggeredDamageDealingBlockEntity;
	}

	@Override
	protected void init() {

		super.init();

		this.selectionMode = this.triggeredDamageDealingBlockEntity.getSelectionMode();
		this.cycleSelectionModeButton = CycleButton.builder(TriggeredDamageDealingBlockEntity.SelectionMode::asText).values(TriggeredDamageDealingBlockEntity.SelectionMode.values()).initially(this.selectionMode).omitKeyText().build(this.width / 2 - 154, 20, 150, 20, Component.empty(), (button, selectionMode) -> {
			this.selectionMode = selectionMode;
			button.setTooltip(Tooltip.of(CYCLE_SELECTION_MODE_TOOLTIP_TEXT));
		});
		this.cycleSelectionModeButton.setTooltip(Tooltip.create(CYCLE_SELECTION_MODE_TOOLTIP_TEXT));
		this.addRenderableWidget(this.cycleSelectionModeButton);

		this.showArea = this.triggeredDamageDealingBlockEntity.showArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_AREA_TRUE_LABEL_TEXT, SHOW_AREA_FALSE_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 + 4, 20, 150, 20, Component.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 55, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 55, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 55, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 90, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 90, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);

		this.uuidListHandlerPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetXField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getX()));
		this.addWidget(this.uuidListHandlerPositionOffsetXField);

		this.uuidListHandlerPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 125, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetYField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getY()));
		this.addWidget(this.uuidListHandlerPositionOffsetYField);

		this.uuidListHandlerPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 125, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetZField.setValue(Integer.toString(this.triggeredDamageDealingBlockEntity.getUuidListHandlerPositionOffset().getZ()));
		this.addWidget(this.uuidListHandlerPositionOffsetZField);

		this.exceptionTagIdentifierField = new EditBox(this.font, this.width / 2 - 154, 160, 300, 20, Component.empty());
		this.exceptionTagIdentifierField.setMaxLength(128);
		this.exceptionTagIdentifierField.setValue(this.triggeredDamageDealingBlockEntity.getExceptionTagIdentifierString());
		this.addWidget(this.exceptionTagIdentifierField);

		this.damageTypeIdentifierField = new EditBox(this.font, this.width / 2 - 154, 195, 250, 20, Component.empty());
		this.damageTypeIdentifierField.setMaxLength(128);
		this.damageTypeIdentifierField.setValue(this.triggeredDamageDealingBlockEntity.getDamageTypeIdentifierString());
		this.addWidget(this.damageTypeIdentifierField);

		this.damageAmountField = new EditBox(this.font, this.width / 2 + 104, 195, 50, 20, Component.empty());
		this.damageAmountField.setMaxLength(128);
		this.damageAmountField.setValue(Float.toString(this.triggeredDamageDealingBlockEntity.getDamageAmount()));
		this.addWidget(this.damageAmountField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 219, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 219, 150, 20).build());
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleSelectionModeButton);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		boolean bool = this.showArea;
		TriggeredDamageDealingBlockEntity.SelectionMode var = this.selectionMode;
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.uuidListHandlerPositionOffsetXField.getValue();
		String string7 = this.uuidListHandlerPositionOffsetYField.getValue();
		String string8 = this.uuidListHandlerPositionOffsetZField.getValue();
		String string9 = this.exceptionTagIdentifierField.getValue();
		String string10 = this.damageTypeIdentifierField.getValue();
		String string11 = this.damageAmountField.getValue();
		this.init(client, width, height);
		this.showArea = bool;
		this.toggleShowAreaButton.setValue(this.showArea);
		this.selectionMode = var;
		this.cycleSelectionModeButton.setValue(this.selectionMode);
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.uuidListHandlerPositionOffsetXField.setValue(string6);
		this.uuidListHandlerPositionOffsetYField.setValue(string7);
		this.uuidListHandlerPositionOffsetZField.setValue(string8);
		this.exceptionTagIdentifierField.setValue(string9);
		this.damageTypeIdentifierField.setValue(string10);
		this.damageAmountField.setValue(string11);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
		this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
		this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
		this.uuidListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, EXCEPTION_TAG_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
		this.exceptionTagIdentifierField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, DAMAGE_TYPE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
		this.damageTypeIdentifierField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, DAMAGE_AMOUNT_LABEL_TEXT, this.width / 2 + 105, 185, 0xA0A0A0);
		this.damageAmountField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredDamageDealingBlockPacket(
				this.triggeredDamageDealingBlockEntity.getPos(),
				this.selectionMode.asString(),
				this.showArea,
				new Vec3i(
						ItemUtils.parseInt(this.areaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.areaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetZField.getValue())
				),
				this.exceptionTagIdentifierField.getValue(),
				this.damageTypeIdentifierField.getValue(),
				ItemUtils.parseFloat(this.damageAmountField.getValue())
		));
		return true;
	}
}
