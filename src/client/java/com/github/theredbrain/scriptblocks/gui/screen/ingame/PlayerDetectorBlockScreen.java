package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdatePlayerDetectorBlockPacket;
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
public class PlayerDetectorBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component ENABLE_TICKING_TRUE_LABEL_TEXT = Component.translatable("gui.player_detector_block.enable_ticking_true_label");
	private static final Component ENABLE_TICKING_FALSE_LABEL_TEXT = Component.translatable("gui.player_detector_block.enable_ticking_false_label");
	private static final Component SHOW_AREA_LABEL_TRUE_TEXT = Component.translatable("gui.player_detector_block.show_area_true_label");
	private static final Component SHOW_AREA_LABEL_FALSE_TEXT = Component.translatable("gui.player_detector_block.show_area_false_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.player_detector_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.player_detector_block.area_position_offset_label");
	private static final Component TRIGGERED_BLOCK_ON_ENTERING_POSITION_TEXT = Component.translatable("gui.player_detector_block.triggered_block_on_entering_position_offset");
	private static final Component TRIGGERED_BLOCK_ON_LEAVING_POSITION_TEXT = Component.translatable("gui.player_detector_block.triggered_block_on_leaving_position_offset");
	private static final Component TRIGGERED_BLOCK_ON_TRIGGERING_POSITION_TEXT = Component.translatable("gui.player_detector_block.triggered_block_on_triggering_position_offset");
	private static final Component UUID_LIST_HANDLER_ON_ENTERING_POSITION_TEXT = Component.translatable("gui.player_detector_block.uuid_list_handler_on_entering_position_offset");
	private static final Component UUID_LIST_HANDLER_ON_LEAVING_POSITION_TEXT = Component.translatable("gui.player_detector_block.uuid_list_handler_on_leaving_position_offset");
	private static final Component UUID_LIST_HANDLER_ON_TRIGGERING_POSITION_TEXT = Component.translatable("gui.player_detector_block.uuid_list_handler_on_triggering_position_offset");
	private final PlayerDetectorBlockEntity playerDetectorBlock;
	private CycleButton<ScreenPage> cycleScreenPageButton;
	private CycleButton<Boolean> toggleEnableTickingButton;
	private CycleButton<Boolean> toggleShowAreaButton;
	private EditBox areaDimensionsXField;
	private EditBox areaDimensionsYField;
	private EditBox areaDimensionsZField;
	private EditBox areaPositionOffsetXField;
	private EditBox areaPositionOffsetYField;
	private EditBox areaPositionOffsetZField;
	private ScreenPage screenPage;
	private boolean enableTicking;
	private boolean showArea;

	private EditBox triggeredBlockOnEnteringPositionOffsetXField;
	private EditBox triggeredBlockOnEnteringPositionOffsetYField;
	private EditBox triggeredBlockOnEnteringPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockOnEnteringResetsButton;
	private boolean triggeredBlockOnEnteringResets;

	private EditBox triggeredBlockOnLeavingPositionOffsetXField;
	private EditBox triggeredBlockOnLeavingPositionOffsetYField;
	private EditBox triggeredBlockOnLeavingPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockOnLeavingResetsButton;
	private boolean triggeredBlockOnLeavingResets;

	private EditBox triggeredBlockOnTriggeringPositionOffsetXField;
	private EditBox triggeredBlockOnTriggeringPositionOffsetYField;
	private EditBox triggeredBlockOnTriggeringPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockOnTriggeringResetsButton;
	private boolean triggeredBlockOnTriggeringResets;

	private EditBox uuidListHandlerOnEnteringPositionOffsetXField;
	private EditBox uuidListHandlerOnEnteringPositionOffsetYField;
	private EditBox uuidListHandlerOnEnteringPositionOffsetZField;

	private EditBox uuidListHandlerOnLeavingPositionOffsetXField;
	private EditBox uuidListHandlerOnLeavingPositionOffsetYField;
	private EditBox uuidListHandlerOnLeavingPositionOffsetZField;

	private EditBox uuidListHandlerOnTriggeringPositionOffsetXField;
	private EditBox uuidListHandlerOnTriggeringPositionOffsetYField;
	private EditBox uuidListHandlerOnTriggeringPositionOffsetZField;

	public PlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlock) {
		super(GameNarrator.NO_TITLE);
		this.playerDetectorBlock = playerDetectorBlock;
		this.screenPage = ScreenPage.AREA;
	}

	@Override
	protected void init() {

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.enableTicking = this.playerDetectorBlock.enableTicking();
		this.toggleEnableTickingButton = this.addRenderableWidget(CycleButton.booleanBuilder(ENABLE_TICKING_TRUE_LABEL_TEXT, ENABLE_TICKING_FALSE_LABEL_TEXT).initially(this.enableTicking).omitKeyText().build(this.width / 2 - 154, 44, 150, 20, Component.empty(), (button, enableTicking) -> {
			this.enableTicking = enableTicking;
		}));

		this.showArea = this.playerDetectorBlock.showArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_AREA_LABEL_TRUE_TEXT, SHOW_AREA_LABEL_FALSE_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 + 4, 44, 150, 20, Component.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 79, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 79, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 79, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 114, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 114, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 114, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);


		this.triggeredBlockOnEnteringPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 50, 20, Component.empty());
		this.triggeredBlockOnEnteringPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockOnEnteringPositionOffsetXField);
		this.triggeredBlockOnEnteringPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 55, 50, 20, Component.empty());
		this.triggeredBlockOnEnteringPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockOnEnteringPositionOffsetYField);
		this.triggeredBlockOnEnteringPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 55, 50, 20, Component.empty());
		this.triggeredBlockOnEnteringPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockOnEnteringPositionOffsetZField);

		this.triggeredBlockOnEnteringResets = this.playerDetectorBlock.getOnEnteringTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnEnteringResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnEnteringResets).omitKeyText().build(this.width / 2 + 8, 55, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnEnteringResets = triggeredBlockResets;
		}));

		this.triggeredBlockOnLeavingPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 50, 20, Component.empty());
		this.triggeredBlockOnLeavingPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockOnLeavingPositionOffsetXField);
		this.triggeredBlockOnLeavingPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 90, 50, 20, Component.empty());
		this.triggeredBlockOnLeavingPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockOnLeavingPositionOffsetYField);
		this.triggeredBlockOnLeavingPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 90, 50, 20, Component.empty());
		this.triggeredBlockOnLeavingPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockOnLeavingPositionOffsetZField);

		this.triggeredBlockOnLeavingResets = this.playerDetectorBlock.getOnLeavingTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnLeavingResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnLeavingResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnLeavingResets = triggeredBlockResets;
		}));

		this.triggeredBlockOnTriggeringPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 50, 20, Component.empty());
		this.triggeredBlockOnTriggeringPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockOnTriggeringPositionOffsetXField);
		this.triggeredBlockOnTriggeringPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 125, 50, 20, Component.empty());
		this.triggeredBlockOnTriggeringPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockOnTriggeringPositionOffsetYField);
		this.triggeredBlockOnTriggeringPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 125, 50, 20, Component.empty());
		this.triggeredBlockOnTriggeringPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockOnTriggeringPositionOffsetZField);

		this.triggeredBlockOnTriggeringResets = this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnTriggeringResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnTriggeringResets).omitKeyText().build(this.width / 2 + 8, 125, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnTriggeringResets = triggeredBlockResets;
		}));

		this.uuidListHandlerOnEnteringPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 100, 20, Component.empty());
		this.uuidListHandlerOnEnteringPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getX()));
		this.addWidget(this.uuidListHandlerOnEnteringPositionOffsetXField);
		this.uuidListHandlerOnEnteringPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 55, 100, 20, Component.empty());
		this.uuidListHandlerOnEnteringPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getY()));
		this.addWidget(this.uuidListHandlerOnEnteringPositionOffsetYField);
		this.uuidListHandlerOnEnteringPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 55, 100, 20, Component.empty());
		this.uuidListHandlerOnEnteringPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getZ()));
		this.addWidget(this.uuidListHandlerOnEnteringPositionOffsetZField);

		this.uuidListHandlerOnLeavingPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 100, 20, Component.empty());
		this.uuidListHandlerOnLeavingPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getX()));
		this.addWidget(this.uuidListHandlerOnLeavingPositionOffsetXField);
		this.uuidListHandlerOnLeavingPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 90, 100, 20, Component.empty());
		this.uuidListHandlerOnLeavingPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getY()));
		this.addWidget(this.uuidListHandlerOnLeavingPositionOffsetYField);
		this.uuidListHandlerOnLeavingPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 90, 100, 20, Component.empty());
		this.uuidListHandlerOnLeavingPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getZ()));
		this.addWidget(this.uuidListHandlerOnLeavingPositionOffsetZField);

		this.uuidListHandlerOnTriggeringPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getX()));
		this.addWidget(this.uuidListHandlerOnTriggeringPositionOffsetXField);
		this.uuidListHandlerOnTriggeringPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 125, 100, 20, Component.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getY()));
		this.addWidget(this.uuidListHandlerOnTriggeringPositionOffsetYField);
		this.uuidListHandlerOnTriggeringPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 125, 100, 20, Component.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setValue(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getZ()));
		this.addWidget(this.uuidListHandlerOnTriggeringPositionOffsetZField);

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

		this.toggleEnableTickingButton.visible = false;
		this.toggleShowAreaButton.visible = false;

		this.areaDimensionsXField.setVisible(false);
		this.areaDimensionsYField.setVisible(false);
		this.areaDimensionsZField.setVisible(false);

		this.areaPositionOffsetXField.setVisible(false);
		this.areaPositionOffsetYField.setVisible(false);
		this.areaPositionOffsetZField.setVisible(false);


		this.triggeredBlockOnEnteringPositionOffsetXField.setVisible(false);
		this.triggeredBlockOnEnteringPositionOffsetYField.setVisible(false);
		this.triggeredBlockOnEnteringPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockOnEnteringResetsButton.visible = false;

		this.triggeredBlockOnLeavingPositionOffsetXField.setVisible(false);
		this.triggeredBlockOnLeavingPositionOffsetYField.setVisible(false);
		this.triggeredBlockOnLeavingPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockOnLeavingResetsButton.visible = false;

		this.triggeredBlockOnTriggeringPositionOffsetXField.setVisible(false);
		this.triggeredBlockOnTriggeringPositionOffsetYField.setVisible(false);
		this.triggeredBlockOnTriggeringPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockOnTriggeringResetsButton.visible = false;


		this.uuidListHandlerOnEnteringPositionOffsetXField.setVisible(false);
		this.uuidListHandlerOnEnteringPositionOffsetYField.setVisible(false);
		this.uuidListHandlerOnEnteringPositionOffsetZField.setVisible(false);

		this.uuidListHandlerOnLeavingPositionOffsetXField.setVisible(false);
		this.uuidListHandlerOnLeavingPositionOffsetYField.setVisible(false);
		this.uuidListHandlerOnLeavingPositionOffsetZField.setVisible(false);

		this.uuidListHandlerOnTriggeringPositionOffsetXField.setVisible(false);
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setVisible(false);
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setVisible(false);

		if (this.screenPage == ScreenPage.AREA) {

			this.toggleEnableTickingButton.visible = true;
			this.toggleShowAreaButton.visible = true;

			this.areaDimensionsXField.setVisible(true);
			this.areaDimensionsYField.setVisible(true);
			this.areaDimensionsZField.setVisible(true);

			this.areaPositionOffsetXField.setVisible(true);
			this.areaPositionOffsetYField.setVisible(true);
			this.areaPositionOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK_OFFSETS) {

			this.triggeredBlockOnEnteringPositionOffsetXField.setVisible(true);
			this.triggeredBlockOnEnteringPositionOffsetYField.setVisible(true);
			this.triggeredBlockOnEnteringPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockOnEnteringResetsButton.visible = true;

			this.triggeredBlockOnLeavingPositionOffsetXField.setVisible(true);
			this.triggeredBlockOnLeavingPositionOffsetYField.setVisible(true);
			this.triggeredBlockOnLeavingPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockOnLeavingResetsButton.visible = true;

			this.triggeredBlockOnTriggeringPositionOffsetXField.setVisible(true);
			this.triggeredBlockOnTriggeringPositionOffsetYField.setVisible(true);
			this.triggeredBlockOnTriggeringPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockOnTriggeringResetsButton.visible = true;

		} else if (this.screenPage == ScreenPage.UUID_LIST_HANDLER_OFFSETS) {

			this.uuidListHandlerOnEnteringPositionOffsetXField.setVisible(true);
			this.uuidListHandlerOnEnteringPositionOffsetYField.setVisible(true);
			this.uuidListHandlerOnEnteringPositionOffsetZField.setVisible(true);

			this.uuidListHandlerOnLeavingPositionOffsetXField.setVisible(true);
			this.uuidListHandlerOnLeavingPositionOffsetYField.setVisible(true);
			this.uuidListHandlerOnLeavingPositionOffsetZField.setVisible(true);

			this.uuidListHandlerOnTriggeringPositionOffsetXField.setVisible(true);
			this.uuidListHandlerOnTriggeringPositionOffsetYField.setVisible(true);
			this.uuidListHandlerOnTriggeringPositionOffsetZField.setVisible(true);

		}

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.enableTicking;
		boolean bool1 = this.showArea;
		boolean bool2 = this.triggeredBlockOnEnteringResets;
		boolean bool3 = this.triggeredBlockOnLeavingResets;
		boolean bool4 = this.triggeredBlockOnTriggeringResets;
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.triggeredBlockOnEnteringPositionOffsetXField.getValue();
		String string7 = this.triggeredBlockOnEnteringPositionOffsetYField.getValue();
		String string8 = this.triggeredBlockOnEnteringPositionOffsetZField.getValue();
		String string9 = this.triggeredBlockOnLeavingPositionOffsetXField.getValue();
		String string10 = this.triggeredBlockOnLeavingPositionOffsetYField.getValue();
		String string11 = this.triggeredBlockOnLeavingPositionOffsetZField.getValue();
		String string12 = this.triggeredBlockOnTriggeringPositionOffsetXField.getValue();
		String string13 = this.triggeredBlockOnTriggeringPositionOffsetYField.getValue();
		String string14 = this.triggeredBlockOnTriggeringPositionOffsetZField.getValue();
		String string15 = this.uuidListHandlerOnEnteringPositionOffsetXField.getValue();
		String string16 = this.uuidListHandlerOnEnteringPositionOffsetYField.getValue();
		String string17 = this.uuidListHandlerOnEnteringPositionOffsetZField.getValue();
		String string18 = this.uuidListHandlerOnLeavingPositionOffsetXField.getValue();
		String string19 = this.uuidListHandlerOnLeavingPositionOffsetYField.getValue();
		String string20 = this.uuidListHandlerOnLeavingPositionOffsetZField.getValue();
		String string21 = this.uuidListHandlerOnTriggeringPositionOffsetXField.getValue();
		String string22 = this.uuidListHandlerOnTriggeringPositionOffsetYField.getValue();
		String string23 = this.uuidListHandlerOnTriggeringPositionOffsetZField.getValue();
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.enableTicking = bool;
		this.toggleEnableTickingButton.setValue(this.enableTicking);
		this.showArea = bool1;
		this.toggleShowAreaButton.setValue(this.showArea);
		this.triggeredBlockOnEnteringResets = bool2;
		this.toggleTriggeredBlockOnEnteringResetsButton.setValue(this.triggeredBlockOnEnteringResets);
		this.triggeredBlockOnLeavingResets = bool3;
		this.toggleTriggeredBlockOnLeavingResetsButton.setValue(this.triggeredBlockOnLeavingResets);
		this.triggeredBlockOnTriggeringResets = bool4;
		this.toggleTriggeredBlockOnTriggeringResetsButton.setValue(this.triggeredBlockOnTriggeringResets);
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.triggeredBlockOnEnteringPositionOffsetXField.setValue(string6);
		this.triggeredBlockOnEnteringPositionOffsetYField.setValue(string7);
		this.triggeredBlockOnEnteringPositionOffsetZField.setValue(string8);
		this.triggeredBlockOnLeavingPositionOffsetXField.setValue(string9);
		this.triggeredBlockOnLeavingPositionOffsetYField.setValue(string10);
		this.triggeredBlockOnLeavingPositionOffsetZField.setValue(string11);
		this.triggeredBlockOnTriggeringPositionOffsetXField.setValue(string12);
		this.triggeredBlockOnTriggeringPositionOffsetYField.setValue(string13);
		this.triggeredBlockOnTriggeringPositionOffsetZField.setValue(string14);
		this.uuidListHandlerOnEnteringPositionOffsetXField.setValue(string15);
		this.uuidListHandlerOnEnteringPositionOffsetYField.setValue(string16);
		this.uuidListHandlerOnEnteringPositionOffsetZField.setValue(string17);
		this.uuidListHandlerOnLeavingPositionOffsetXField.setValue(string18);
		this.uuidListHandlerOnLeavingPositionOffsetYField.setValue(string19);
		this.uuidListHandlerOnLeavingPositionOffsetZField.setValue(string20);
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setValue(string21);
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setValue(string22);
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setValue(string23);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {

			context.drawString(this.font, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK_OFFSETS) {

			context.drawString(this.font, TRIGGERED_BLOCK_ON_ENTERING_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.triggeredBlockOnEnteringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnEnteringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnEnteringPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TRIGGERED_BLOCK_ON_LEAVING_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.triggeredBlockOnLeavingPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnLeavingPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnLeavingPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TRIGGERED_BLOCK_ON_TRIGGERING_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.triggeredBlockOnTriggeringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnTriggeringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnTriggeringPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.UUID_LIST_HANDLER_OFFSETS) {

			context.drawString(this.font, UUID_LIST_HANDLER_ON_ENTERING_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.uuidListHandlerOnEnteringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnEnteringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnEnteringPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, UUID_LIST_HANDLER_ON_LEAVING_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.uuidListHandlerOnLeavingPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnLeavingPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnLeavingPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, UUID_LIST_HANDLER_ON_TRIGGERING_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.uuidListHandlerOnTriggeringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnTriggeringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnTriggeringPositionOffsetZField.render(context, mouseX, mouseY, delta);

		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdatePlayerDetectorBlockPacket(
				this.playerDetectorBlock.getPos(),
				this.enableTicking,
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
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetZField.getValue())
				),
				this.triggeredBlockOnEnteringResets,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetZField.getValue())
				),
				this.triggeredBlockOnLeavingResets,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetZField.getValue())
				),
				this.triggeredBlockOnTriggeringResets,
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetZField.getValue())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		AREA("area"),
		TRIGGERED_BLOCK_OFFSETS("triggered_block_offsets"),
		UUID_LIST_HANDLER_OFFSETS("uuid_list_handler_offsets");

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
			return Component.translatable("gui.player_detector_block.screen_page." + this.name);
		}
	}
}
