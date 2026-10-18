package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTwoAreasPlayerDetectorBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TwoAreasPlayerDetectorBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text SHOW_FIRST_AREA_LABEL_TRUE_TEXT = Text.translatable("gui.two_areas_player_detector_block.show_first_area_true_label");
	private static final Text SHOW_FIRST_AREA_LABEL_FALSE_TEXT = Text.translatable("gui.two_areas_player_detector_block.show_first_area_false_label");
	private static final Text SHOW_SECOND_AREA_LABEL_TRUE_TEXT = Text.translatable("gui.two_areas_player_detector_block.show_second_area_true_label");
	private static final Text SHOW_SECOND_AREA_LABEL_FALSE_TEXT = Text.translatable("gui.two_areas_player_detector_block.show_second_area_false_label");
	private static final Text FIRST_AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.two_areas_player_detector_block.first_area_dimensions_label");
	private static final Text FIRST_AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.two_areas_player_detector_block.first_area_position_offset_label");
	private static final Text SECOND_AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.two_areas_player_detector_block.second_area_dimensions_label");
	private static final Text SECOND_AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.two_areas_player_detector_block.second_area_position_offset_label");
	private static final Text ON_ENTERING_FIRST_AREA_TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.two_areas_player_detector_block.on_entering_first_area_triggered_block_position_offset");
	private static final Text ON_ENTERING_SECOND_AREA_TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.two_areas_player_detector_block.on_entering_second_area_triggered_block_position_offset");
	private static final Text ON_ENTERING_FIRST_AREA_UUID_LIST_HANDLER_POSITION_TEXT = Text.translatable("gui.two_areas_player_detector_block.on_entering_first_area_uuid_list_handler_position_offset");
	private static final Text ON_ENTERING_SECOND_AREA_UUID_LIST_HANDLER_POSITION_TEXT = Text.translatable("gui.two_areas_player_detector_block.on_entering_second_area_uuid_list_handler_position_offset");
	private final TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlock;
	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private CyclingButtonWidget<Boolean> toggleShowFirstAreaButton;
	private CyclingButtonWidget<Boolean> toggleShowSecondAreaButton;
	private TextFieldWidget firstAreaDimensionsXField;
	private TextFieldWidget firstAreaDimensionsYField;
	private TextFieldWidget firstAreaDimensionsZField;
	private TextFieldWidget firstAreaPositionOffsetXField;
	private TextFieldWidget firstAreaPositionOffsetYField;
	private TextFieldWidget firstAreaPositionOffsetZField;
	private TextFieldWidget secondAreaDimensionsXField;
	private TextFieldWidget secondAreaDimensionsYField;
	private TextFieldWidget secondAreaDimensionsZField;
	private TextFieldWidget secondAreaPositionOffsetXField;
	private TextFieldWidget secondAreaPositionOffsetYField;
	private TextFieldWidget secondAreaPositionOffsetZField;
	private ScreenPage screenPage;
	private boolean showFirstArea;
	private boolean showSecondArea;

	private TextFieldWidget onEnteringFirstAreaTriggeredBlockPositionOffsetXField;
	private TextFieldWidget onEnteringFirstAreaTriggeredBlockPositionOffsetYField;
	private TextFieldWidget onEnteringFirstAreaTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleOnEnteringFirstAreaTriggeredBlockResetsButton;
	private boolean onEnteringFirstAreaTriggeredBlockResets;

	private TextFieldWidget onEnteringSecondAreaTriggeredBlockPositionOffsetXField;
	private TextFieldWidget onEnteringSecondAreaTriggeredBlockPositionOffsetYField;
	private TextFieldWidget onEnteringSecondAreaTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleOnEnteringSecondAreaTriggeredBlockResetsButton;
	private boolean onEnteringSecondAreaTriggeredBlockResets;

	private TextFieldWidget onEnteringFirstAreaUUIDListHandlerPositionOffsetXField;
	private TextFieldWidget onEnteringFirstAreaUUIDListHandlerPositionOffsetYField;
	private TextFieldWidget onEnteringFirstAreaUUIDListHandlerPositionOffsetZField;

	private TextFieldWidget onEnteringSecondAreaUUIDListHandlerPositionOffsetXField;
	private TextFieldWidget onEnteringSecondAreaUUIDListHandlerPositionOffsetYField;
	private TextFieldWidget onEnteringSecondAreaUUIDListHandlerPositionOffsetZField;

	public TwoAreasPlayerDetectorBlockScreen(TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		super(NarratorManager.EMPTY);
		this.twoAreasPlayerDetectorBlock = twoAreasPlayerDetectorBlockEntity;
		this.screenPage = ScreenPage.AREA;
	}

	@Override
	protected void init() {

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values(ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showFirstArea = this.twoAreasPlayerDetectorBlock.showFirstArea();
		this.toggleShowFirstAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(SHOW_FIRST_AREA_LABEL_TRUE_TEXT, SHOW_FIRST_AREA_LABEL_FALSE_TEXT).initially(this.showFirstArea).omitKeyText().build(this.width / 2 - 154, 44, 150, 20, Text.empty(), (button, showFirstArea) -> {
			this.showFirstArea = showFirstArea;
		}));

		this.showSecondArea = this.twoAreasPlayerDetectorBlock.showSecondArea();
		this.toggleShowSecondAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(SHOW_SECOND_AREA_LABEL_TRUE_TEXT, SHOW_SECOND_AREA_LABEL_FALSE_TEXT).initially(this.showSecondArea).omitKeyText().build(this.width / 2 + 4, 44, 150, 20, Text.empty(), (button, showSecondArea) -> {
			this.showSecondArea = showSecondArea;
		}));

		this.firstAreaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 79, 100, 20, Text.empty());
		this.firstAreaDimensionsXField.setMaxLength(128);
		this.firstAreaDimensionsXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getX()));
		this.addSelectableChild(this.firstAreaDimensionsXField);

		this.firstAreaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 79, 100, 20, Text.empty());
		this.firstAreaDimensionsYField.setMaxLength(128);
		this.firstAreaDimensionsYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getY()));
		this.addSelectableChild(this.firstAreaDimensionsYField);

		this.firstAreaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 79, 100, 20, Text.empty());
		this.firstAreaDimensionsZField.setMaxLength(128);
		this.firstAreaDimensionsZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getZ()));
		this.addSelectableChild(this.firstAreaDimensionsZField);

		this.firstAreaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 100, 20, Text.empty());
		this.firstAreaPositionOffsetXField.setMaxLength(128);
		this.firstAreaPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getX()));
		this.addSelectableChild(this.firstAreaPositionOffsetXField);

		this.firstAreaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 114, 100, 20, Text.empty());
		this.firstAreaPositionOffsetYField.setMaxLength(128);
		this.firstAreaPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getY()));
		this.addSelectableChild(this.firstAreaPositionOffsetYField);

		this.firstAreaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 114, 100, 20, Text.empty());
		this.firstAreaPositionOffsetZField.setMaxLength(128);
		this.firstAreaPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getZ()));
		this.addSelectableChild(this.firstAreaPositionOffsetZField);


		this.secondAreaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 149, 100, 20, Text.empty());
		this.secondAreaDimensionsXField.setMaxLength(128);
		this.secondAreaDimensionsXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getX()));
		this.addSelectableChild(this.secondAreaDimensionsXField);

		this.secondAreaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 149, 100, 20, Text.empty());
		this.secondAreaDimensionsYField.setMaxLength(128);
		this.secondAreaDimensionsYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getY()));
		this.addSelectableChild(this.secondAreaDimensionsYField);

		this.secondAreaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 149, 100, 20, Text.empty());
		this.secondAreaDimensionsZField.setMaxLength(128);
		this.secondAreaDimensionsZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getZ()));
		this.addSelectableChild(this.secondAreaDimensionsZField);

		this.secondAreaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 184, 100, 20, Text.empty());
		this.secondAreaPositionOffsetXField.setMaxLength(128);
		this.secondAreaPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getX()));
		this.addSelectableChild(this.secondAreaPositionOffsetXField);

		this.secondAreaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 184, 100, 20, Text.empty());
		this.secondAreaPositionOffsetYField.setMaxLength(128);
		this.secondAreaPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getY()));
		this.addSelectableChild(this.secondAreaPositionOffsetYField);

		this.secondAreaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 184, 100, 20, Text.empty());
		this.secondAreaPositionOffsetZField.setMaxLength(128);
		this.secondAreaPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getZ()));
		this.addSelectableChild(this.secondAreaPositionOffsetZField);


		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 50, 20, Text.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 55, 50, 20, Text.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 55, 50, 20, Text.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField);

		this.onEnteringFirstAreaTriggeredBlockResets = this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getRight();
		this.toggleOnEnteringFirstAreaTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.onEnteringFirstAreaTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 55, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.onEnteringFirstAreaTriggeredBlockResets = triggeredBlockResets;
		}));

		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 50, 20, Text.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 90, 50, 20, Text.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 90, 50, 20, Text.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField);

		this.onEnteringSecondAreaTriggeredBlockResets = this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getRight();
		this.toggleOnEnteringSecondAreaTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.onEnteringSecondAreaTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.onEnteringSecondAreaTriggeredBlockResets = triggeredBlockResets;
		}));

		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getX()));
		this.addSelectableChild(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 125, 100, 20, Text.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getY()));
		this.addSelectableChild(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 125, 100, 20, Text.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getZ()));
		this.addSelectableChild(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField);

		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 160, 100, 20, Text.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getX()));
		this.addSelectableChild(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 160, 100, 20, Text.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getY()));
		this.addSelectableChild(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 160, 100, 20, Text.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setText(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getZ()));
		this.addSelectableChild(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 207, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.toggleShowFirstAreaButton.visible = false;
		this.toggleShowSecondAreaButton.visible = false;

		this.firstAreaDimensionsXField.setVisible(false);
		this.firstAreaDimensionsYField.setVisible(false);
		this.firstAreaDimensionsZField.setVisible(false);

		this.firstAreaPositionOffsetXField.setVisible(false);
		this.firstAreaPositionOffsetYField.setVisible(false);
		this.firstAreaPositionOffsetZField.setVisible(false);

		this.secondAreaDimensionsXField.setVisible(false);
		this.secondAreaDimensionsYField.setVisible(false);
		this.secondAreaDimensionsZField.setVisible(false);

		this.secondAreaPositionOffsetXField.setVisible(false);
		this.secondAreaPositionOffsetYField.setVisible(false);
		this.secondAreaPositionOffsetZField.setVisible(false);


		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setVisible(false);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setVisible(false);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleOnEnteringFirstAreaTriggeredBlockResetsButton.visible = false;

		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setVisible(false);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setVisible(false);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleOnEnteringSecondAreaTriggeredBlockResetsButton.visible = false;


		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setVisible(false);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setVisible(false);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setVisible(false);

		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setVisible(false);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setVisible(false);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setVisible(false);

		if (this.screenPage == ScreenPage.AREA) {

			this.toggleShowFirstAreaButton.visible = true;
			this.toggleShowSecondAreaButton.visible = true;

			this.firstAreaDimensionsXField.setVisible(true);
			this.firstAreaDimensionsYField.setVisible(true);
			this.firstAreaDimensionsZField.setVisible(true);

			this.firstAreaPositionOffsetXField.setVisible(true);
			this.firstAreaPositionOffsetYField.setVisible(true);
			this.firstAreaPositionOffsetZField.setVisible(true);

			this.secondAreaDimensionsXField.setVisible(true);
			this.secondAreaDimensionsYField.setVisible(true);
			this.secondAreaDimensionsZField.setVisible(true);

			this.secondAreaPositionOffsetXField.setVisible(true);
			this.secondAreaPositionOffsetYField.setVisible(true);
			this.secondAreaPositionOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.EVENTS) {

			this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setVisible(true);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setVisible(true);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleOnEnteringFirstAreaTriggeredBlockResetsButton.visible = true;

			this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setVisible(true);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setVisible(true);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleOnEnteringSecondAreaTriggeredBlockResetsButton.visible = true;

			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setVisible(true);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setVisible(true);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setVisible(true);

			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setVisible(true);
			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setVisible(true);
			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setVisible(true);

		}

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.showFirstArea;
		boolean bool1 = this.showFirstArea;
		boolean bool2 = this.onEnteringFirstAreaTriggeredBlockResets;
		boolean bool3 = this.onEnteringSecondAreaTriggeredBlockResets;
		String string = this.firstAreaDimensionsXField.getText();
		String string1 = this.firstAreaDimensionsYField.getText();
		String string2 = this.firstAreaDimensionsZField.getText();
		String string3 = this.firstAreaPositionOffsetXField.getText();
		String string4 = this.firstAreaPositionOffsetYField.getText();
		String string5 = this.firstAreaPositionOffsetZField.getText();
		String string6 = this.secondAreaDimensionsXField.getText();
		String string7 = this.secondAreaDimensionsYField.getText();
		String string8 = this.secondAreaDimensionsZField.getText();
		String string9 = this.secondAreaPositionOffsetXField.getText();
		String string10 = this.secondAreaPositionOffsetYField.getText();
		String string11 = this.secondAreaPositionOffsetZField.getText();
		String string12 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.getText();
		String string13 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.getText();
		String string14 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.getText();
		String string15 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.getText();
		String string16 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.getText();
		String string17 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.getText();
		String string18 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.getText();
		String string19 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.getText();
		String string20 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.getText();
		String string21 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.getText();
		String string22 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.getText();
		String string23 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.getText();
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.showFirstArea = bool;
		this.toggleShowFirstAreaButton.setValue(this.showFirstArea);
		this.showSecondArea = bool1;
		this.toggleShowSecondAreaButton.setValue(this.showSecondArea);
		this.onEnteringFirstAreaTriggeredBlockResets = bool2;
		this.toggleOnEnteringFirstAreaTriggeredBlockResetsButton.setValue(this.onEnteringFirstAreaTriggeredBlockResets);
		this.onEnteringSecondAreaTriggeredBlockResets = bool3;
		this.toggleOnEnteringSecondAreaTriggeredBlockResetsButton.setValue(this.onEnteringSecondAreaTriggeredBlockResets);
		this.firstAreaDimensionsXField.setText(string);
		this.firstAreaDimensionsYField.setText(string1);
		this.firstAreaDimensionsZField.setText(string2);
		this.firstAreaPositionOffsetXField.setText(string3);
		this.firstAreaPositionOffsetYField.setText(string4);
		this.firstAreaPositionOffsetZField.setText(string5);
		this.secondAreaDimensionsXField.setText(string6);
		this.secondAreaDimensionsYField.setText(string7);
		this.secondAreaDimensionsZField.setText(string8);
		this.secondAreaPositionOffsetXField.setText(string9);
		this.secondAreaPositionOffsetYField.setText(string10);
		this.secondAreaPositionOffsetZField.setText(string11);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setText(string12);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setText(string13);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setText(string14);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setText(string15);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setText(string16);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setText(string17);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setText(string18);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setText(string19);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setText(string20);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setText(string21);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setText(string22);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setText(string23);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {

			context.drawTextWithShadow(this.textRenderer, FIRST_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.firstAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.firstAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.firstAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, FIRST_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.firstAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.firstAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.firstAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, SECOND_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.secondAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.secondAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.secondAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, SECOND_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 174, 0xA0A0A0);
			this.secondAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.secondAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.secondAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.EVENTS) {

			context.drawTextWithShadow(this.textRenderer, ON_ENTERING_FIRST_AREA_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ON_ENTERING_SECOND_AREA_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ON_ENTERING_FIRST_AREA_UUID_LIST_HANDLER_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ON_ENTERING_SECOND_AREA_UUID_LIST_HANDLER_POSITION_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);

		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTwoAreasPlayerDetectorBlockPacket(
				this.twoAreasPlayerDetectorBlock.getPos(),
				this.showFirstArea,
				this.showSecondArea,
				new Vec3i(
						ItemUtils.parseInt(this.firstAreaDimensionsXField.getText()),
						ItemUtils.parseInt(this.firstAreaDimensionsYField.getText()),
						ItemUtils.parseInt(this.firstAreaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.firstAreaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.firstAreaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.firstAreaPositionOffsetZField.getText())
				),
				new Vec3i(
						ItemUtils.parseInt(this.secondAreaDimensionsXField.getText()),
						ItemUtils.parseInt(this.secondAreaDimensionsYField.getText()),
						ItemUtils.parseInt(this.secondAreaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.secondAreaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.secondAreaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.secondAreaPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.getText())
				),
				this.onEnteringFirstAreaTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.getText())
				),
				this.onEnteringSecondAreaTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.getText())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		AREA("area"),
		EVENTS("events");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.two_areas_player_detector_block.screen_page." + this.name);
		}
	}
}
