package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTwoAreasPlayerDetectorBlockPacket;
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
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TwoAreasPlayerDetectorBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component SHOW_FIRST_AREA_LABEL_TRUE_TEXT = Component.translatable("gui.two_areas_player_detector_block.show_first_area_true_label");
	private static final Component SHOW_FIRST_AREA_LABEL_FALSE_TEXT = Component.translatable("gui.two_areas_player_detector_block.show_first_area_false_label");
	private static final Component SHOW_SECOND_AREA_LABEL_TRUE_TEXT = Component.translatable("gui.two_areas_player_detector_block.show_second_area_true_label");
	private static final Component SHOW_SECOND_AREA_LABEL_FALSE_TEXT = Component.translatable("gui.two_areas_player_detector_block.show_second_area_false_label");
	private static final Component FIRST_AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.two_areas_player_detector_block.first_area_dimensions_label");
	private static final Component FIRST_AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.two_areas_player_detector_block.first_area_position_offset_label");
	private static final Component SECOND_AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.two_areas_player_detector_block.second_area_dimensions_label");
	private static final Component SECOND_AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.two_areas_player_detector_block.second_area_position_offset_label");
	private static final Component ON_ENTERING_FIRST_AREA_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.two_areas_player_detector_block.on_entering_first_area_triggered_block_position_offset");
	private static final Component ON_ENTERING_SECOND_AREA_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.two_areas_player_detector_block.on_entering_second_area_triggered_block_position_offset");
	private static final Component ON_ENTERING_FIRST_AREA_UUID_LIST_HANDLER_POSITION_TEXT = Component.translatable("gui.two_areas_player_detector_block.on_entering_first_area_uuid_list_handler_position_offset");
	private static final Component ON_ENTERING_SECOND_AREA_UUID_LIST_HANDLER_POSITION_TEXT = Component.translatable("gui.two_areas_player_detector_block.on_entering_second_area_uuid_list_handler_position_offset");
	private final TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlock;
	private CycleButton<ScreenPage> cycleScreenPageButton;
	private CycleButton<Boolean> toggleShowFirstAreaButton;
	private CycleButton<Boolean> toggleShowSecondAreaButton;
	private EditBox firstAreaDimensionsXField;
	private EditBox firstAreaDimensionsYField;
	private EditBox firstAreaDimensionsZField;
	private EditBox firstAreaPositionOffsetXField;
	private EditBox firstAreaPositionOffsetYField;
	private EditBox firstAreaPositionOffsetZField;
	private EditBox secondAreaDimensionsXField;
	private EditBox secondAreaDimensionsYField;
	private EditBox secondAreaDimensionsZField;
	private EditBox secondAreaPositionOffsetXField;
	private EditBox secondAreaPositionOffsetYField;
	private EditBox secondAreaPositionOffsetZField;
	private ScreenPage screenPage;
	private boolean showFirstArea;
	private boolean showSecondArea;

	private EditBox onEnteringFirstAreaTriggeredBlockPositionOffsetXField;
	private EditBox onEnteringFirstAreaTriggeredBlockPositionOffsetYField;
	private EditBox onEnteringFirstAreaTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleOnEnteringFirstAreaTriggeredBlockResetsButton;
	private boolean onEnteringFirstAreaTriggeredBlockResets;

	private EditBox onEnteringSecondAreaTriggeredBlockPositionOffsetXField;
	private EditBox onEnteringSecondAreaTriggeredBlockPositionOffsetYField;
	private EditBox onEnteringSecondAreaTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleOnEnteringSecondAreaTriggeredBlockResetsButton;
	private boolean onEnteringSecondAreaTriggeredBlockResets;

	private EditBox onEnteringFirstAreaUUIDListHandlerPositionOffsetXField;
	private EditBox onEnteringFirstAreaUUIDListHandlerPositionOffsetYField;
	private EditBox onEnteringFirstAreaUUIDListHandlerPositionOffsetZField;

	private EditBox onEnteringSecondAreaUUIDListHandlerPositionOffsetXField;
	private EditBox onEnteringSecondAreaUUIDListHandlerPositionOffsetYField;
	private EditBox onEnteringSecondAreaUUIDListHandlerPositionOffsetZField;

	public TwoAreasPlayerDetectorBlockScreen(TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.twoAreasPlayerDetectorBlock = twoAreasPlayerDetectorBlockEntity;
		this.screenPage = ScreenPage.AREA;
	}

	@Override
	protected void init() {

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values(ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showFirstArea = this.twoAreasPlayerDetectorBlock.showFirstArea();
		this.toggleShowFirstAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_FIRST_AREA_LABEL_TRUE_TEXT, SHOW_FIRST_AREA_LABEL_FALSE_TEXT).initially(this.showFirstArea).omitKeyText().build(this.width / 2 - 154, 44, 150, 20, Component.empty(), (button, showFirstArea) -> {
			this.showFirstArea = showFirstArea;
		}));

		this.showSecondArea = this.twoAreasPlayerDetectorBlock.showSecondArea();
		this.toggleShowSecondAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_SECOND_AREA_LABEL_TRUE_TEXT, SHOW_SECOND_AREA_LABEL_FALSE_TEXT).initially(this.showSecondArea).omitKeyText().build(this.width / 2 + 4, 44, 150, 20, Component.empty(), (button, showSecondArea) -> {
			this.showSecondArea = showSecondArea;
		}));

		this.firstAreaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 79, 100, 20, Component.empty());
		this.firstAreaDimensionsXField.setMaxLength(128);
		this.firstAreaDimensionsXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getX()));
		this.addWidget(this.firstAreaDimensionsXField);

		this.firstAreaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 79, 100, 20, Component.empty());
		this.firstAreaDimensionsYField.setMaxLength(128);
		this.firstAreaDimensionsYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getY()));
		this.addWidget(this.firstAreaDimensionsYField);

		this.firstAreaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 79, 100, 20, Component.empty());
		this.firstAreaDimensionsZField.setMaxLength(128);
		this.firstAreaDimensionsZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaDimensions().getZ()));
		this.addWidget(this.firstAreaDimensionsZField);

		this.firstAreaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 114, 100, 20, Component.empty());
		this.firstAreaPositionOffsetXField.setMaxLength(128);
		this.firstAreaPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getX()));
		this.addWidget(this.firstAreaPositionOffsetXField);

		this.firstAreaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 114, 100, 20, Component.empty());
		this.firstAreaPositionOffsetYField.setMaxLength(128);
		this.firstAreaPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getY()));
		this.addWidget(this.firstAreaPositionOffsetYField);

		this.firstAreaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 114, 100, 20, Component.empty());
		this.firstAreaPositionOffsetZField.setMaxLength(128);
		this.firstAreaPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getFirstAreaPositionOffset().getZ()));
		this.addWidget(this.firstAreaPositionOffsetZField);


		this.secondAreaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 149, 100, 20, Component.empty());
		this.secondAreaDimensionsXField.setMaxLength(128);
		this.secondAreaDimensionsXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getX()));
		this.addWidget(this.secondAreaDimensionsXField);

		this.secondAreaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 149, 100, 20, Component.empty());
		this.secondAreaDimensionsYField.setMaxLength(128);
		this.secondAreaDimensionsYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getY()));
		this.addWidget(this.secondAreaDimensionsYField);

		this.secondAreaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 149, 100, 20, Component.empty());
		this.secondAreaDimensionsZField.setMaxLength(128);
		this.secondAreaDimensionsZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaDimensions().getZ()));
		this.addWidget(this.secondAreaDimensionsZField);

		this.secondAreaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 184, 100, 20, Component.empty());
		this.secondAreaPositionOffsetXField.setMaxLength(128);
		this.secondAreaPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getX()));
		this.addWidget(this.secondAreaPositionOffsetXField);

		this.secondAreaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 184, 100, 20, Component.empty());
		this.secondAreaPositionOffsetYField.setMaxLength(128);
		this.secondAreaPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getY()));
		this.addWidget(this.secondAreaPositionOffsetYField);

		this.secondAreaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 184, 100, 20, Component.empty());
		this.secondAreaPositionOffsetZField.setMaxLength(128);
		this.secondAreaPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getSecondAreaPositionOffset().getZ()));
		this.addWidget(this.secondAreaPositionOffsetZField);


		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 50, 20, Component.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getX()));
		this.addWidget(this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 55, 50, 20, Component.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getY()));
		this.addWidget(this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 55, 50, 20, Component.empty());
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField);

		this.onEnteringFirstAreaTriggeredBlockResets = this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaTriggeredBlock().getRight();
		this.toggleOnEnteringFirstAreaTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.onEnteringFirstAreaTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 55, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.onEnteringFirstAreaTriggeredBlockResets = triggeredBlockResets;
		}));

		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 50, 20, Component.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getX()));
		this.addWidget(this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 90, 50, 20, Component.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getY()));
		this.addWidget(this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 90, 50, 20, Component.empty());
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField);

		this.onEnteringSecondAreaTriggeredBlockResets = this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaTriggeredBlock().getRight();
		this.toggleOnEnteringSecondAreaTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.onEnteringSecondAreaTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.onEnteringSecondAreaTriggeredBlockResets = triggeredBlockResets;
		}));

		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getX()));
		this.addWidget(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 125, 100, 20, Component.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getY()));
		this.addWidget(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 125, 100, 20, Component.empty());
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setMaxLength(128);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringFirstAreaUUIDListHandler().getZ()));
		this.addWidget(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField);

		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 160, 100, 20, Component.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getX()));
		this.addWidget(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 160, 100, 20, Component.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getY()));
		this.addWidget(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 160, 100, 20, Component.empty());
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setMaxLength(128);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setValue(Integer.toString(this.twoAreasPlayerDetectorBlock.getOnEnteringSecondAreaUUIDListHandler().getZ()));
		this.addWidget(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 207, 150, 20).build());

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
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.showFirstArea;
		boolean bool1 = this.showFirstArea;
		boolean bool2 = this.onEnteringFirstAreaTriggeredBlockResets;
		boolean bool3 = this.onEnteringSecondAreaTriggeredBlockResets;
		String string = this.firstAreaDimensionsXField.getValue();
		String string1 = this.firstAreaDimensionsYField.getValue();
		String string2 = this.firstAreaDimensionsZField.getValue();
		String string3 = this.firstAreaPositionOffsetXField.getValue();
		String string4 = this.firstAreaPositionOffsetYField.getValue();
		String string5 = this.firstAreaPositionOffsetZField.getValue();
		String string6 = this.secondAreaDimensionsXField.getValue();
		String string7 = this.secondAreaDimensionsYField.getValue();
		String string8 = this.secondAreaDimensionsZField.getValue();
		String string9 = this.secondAreaPositionOffsetXField.getValue();
		String string10 = this.secondAreaPositionOffsetYField.getValue();
		String string11 = this.secondAreaPositionOffsetZField.getValue();
		String string12 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.getValue();
		String string13 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.getValue();
		String string14 = this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.getValue();
		String string15 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.getValue();
		String string16 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.getValue();
		String string17 = this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.getValue();
		String string18 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.getValue();
		String string19 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.getValue();
		String string20 = this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.getValue();
		String string21 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.getValue();
		String string22 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.getValue();
		String string23 = this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.getValue();
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
		this.firstAreaDimensionsXField.setValue(string);
		this.firstAreaDimensionsYField.setValue(string1);
		this.firstAreaDimensionsZField.setValue(string2);
		this.firstAreaPositionOffsetXField.setValue(string3);
		this.firstAreaPositionOffsetYField.setValue(string4);
		this.firstAreaPositionOffsetZField.setValue(string5);
		this.secondAreaDimensionsXField.setValue(string6);
		this.secondAreaDimensionsYField.setValue(string7);
		this.secondAreaDimensionsZField.setValue(string8);
		this.secondAreaPositionOffsetXField.setValue(string9);
		this.secondAreaPositionOffsetYField.setValue(string10);
		this.secondAreaPositionOffsetZField.setValue(string11);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.setValue(string12);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.setValue(string13);
		this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.setValue(string14);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.setValue(string15);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.setValue(string16);
		this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.setValue(string17);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.setValue(string18);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.setValue(string19);
		this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.setValue(string20);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.setValue(string21);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.setValue(string22);
		this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.setValue(string23);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {

			context.drawString(this.font, FIRST_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.firstAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.firstAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.firstAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, FIRST_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.firstAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.firstAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.firstAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, SECOND_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.secondAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.secondAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.secondAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, SECOND_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 174, 0xA0A0A0);
			this.secondAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.secondAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.secondAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.EVENTS) {

			context.drawString(this.font, ON_ENTERING_FIRST_AREA_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ON_ENTERING_SECOND_AREA_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ON_ENTERING_FIRST_AREA_UUID_LIST_HANDLER_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ON_ENTERING_SECOND_AREA_UUID_LIST_HANDLER_POSITION_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
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
						ItemUtils.parseInt(this.firstAreaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.firstAreaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.firstAreaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.firstAreaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.firstAreaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.firstAreaPositionOffsetZField.getValue())
				),
				new Vec3i(
						ItemUtils.parseInt(this.secondAreaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.secondAreaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.secondAreaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.secondAreaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.secondAreaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.secondAreaPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.onEnteringFirstAreaTriggeredBlockPositionOffsetZField.getValue())
				),
				this.onEnteringFirstAreaTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.onEnteringSecondAreaTriggeredBlockPositionOffsetZField.getValue())
				),
				this.onEnteringSecondAreaTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.onEnteringFirstAreaUUIDListHandlerPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.onEnteringSecondAreaUUIDListHandlerPositionOffsetZField.getValue())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		AREA("area"),
		EVENTS("events");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.two_areas_player_detector_block.screen_page." + this.name);
		}
	}
}
