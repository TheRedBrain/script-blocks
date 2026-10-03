package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdatePlayerDetectorBlockPacket;
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
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class PlayerDetectorBlockScreen extends Screen {
	private static final Text ENABLE_TICKING_TRUE_LABEL_TEXT = Text.translatable("gui.player_detector_block.enable_ticking_true_label");
	private static final Text ENABLE_TICKING_FALSE_LABEL_TEXT = Text.translatable("gui.player_detector_block.enable_ticking_false_label");
	private static final Text SHOW_AREA_LABEL_TRUE_TEXT = Text.translatable("gui.player_detector_block.show_area_true_label");
	private static final Text SHOW_AREA_LABEL_FALSE_TEXT = Text.translatable("gui.player_detector_block.show_area_false_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.player_detector_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.player_detector_block.area_position_offset_label");
	private static final Text TRIGGERED_BLOCK_ON_ENTERING_POSITION_TEXT = Text.translatable("gui.player_detector_block.triggered_block_on_entering_position_offset");
	private static final Text TRIGGERED_BLOCK_ON_LEAVING_POSITION_TEXT = Text.translatable("gui.player_detector_block.triggered_block_on_leaving_position_offset");
	private static final Text TRIGGERED_BLOCK_ON_TRIGGERING_POSITION_TEXT = Text.translatable("gui.player_detector_block.triggered_block_on_triggering_position_offset");
	private static final Text UUID_LIST_HANDLER_ON_ENTERING_POSITION_TEXT = Text.translatable("gui.player_detector_block.uuid_list_handler_on_entering_position_offset");
	private static final Text UUID_LIST_HANDLER_ON_LEAVING_POSITION_TEXT = Text.translatable("gui.player_detector_block.uuid_list_handler_on_leaving_position_offset");
	private static final Text UUID_LIST_HANDLER_ON_TRIGGERING_POSITION_TEXT = Text.translatable("gui.player_detector_block.uuid_list_handler_on_triggering_position_offset");
	private final PlayerDetectorBlockEntity playerDetectorBlock;
	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private CyclingButtonWidget<Boolean> toggleEnableTickingButton;
	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private ScreenPage screenPage;
	private boolean enableTicking;
	private boolean showArea;

	private TextFieldWidget triggeredBlockOnEnteringPositionOffsetXField;
	private TextFieldWidget triggeredBlockOnEnteringPositionOffsetYField;
	private TextFieldWidget triggeredBlockOnEnteringPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockOnEnteringResetsButton;
	private boolean triggeredBlockOnEnteringResets;

	private TextFieldWidget triggeredBlockOnLeavingPositionOffsetXField;
	private TextFieldWidget triggeredBlockOnLeavingPositionOffsetYField;
	private TextFieldWidget triggeredBlockOnLeavingPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockOnLeavingResetsButton;
	private boolean triggeredBlockOnLeavingResets;

	private TextFieldWidget triggeredBlockOnTriggeringPositionOffsetXField;
	private TextFieldWidget triggeredBlockOnTriggeringPositionOffsetYField;
	private TextFieldWidget triggeredBlockOnTriggeringPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockOnTriggeringResetsButton;
	private boolean triggeredBlockOnTriggeringResets;

	private TextFieldWidget uuidListHandlerOnEnteringPositionOffsetXField;
	private TextFieldWidget uuidListHandlerOnEnteringPositionOffsetYField;
	private TextFieldWidget uuidListHandlerOnEnteringPositionOffsetZField;

	private TextFieldWidget uuidListHandlerOnLeavingPositionOffsetXField;
	private TextFieldWidget uuidListHandlerOnLeavingPositionOffsetYField;
	private TextFieldWidget uuidListHandlerOnLeavingPositionOffsetZField;

	private TextFieldWidget uuidListHandlerOnTriggeringPositionOffsetXField;
	private TextFieldWidget uuidListHandlerOnTriggeringPositionOffsetYField;
	private TextFieldWidget uuidListHandlerOnTriggeringPositionOffsetZField;

	public PlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlock) {
		super(NarratorManager.EMPTY);
		this.playerDetectorBlock = playerDetectorBlock;
		this.screenPage = ScreenPage.AREA;
	}

	private void done() {
		if (this.updateAreaBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.enableTicking = this.playerDetectorBlock.enableTicking();
		this.toggleEnableTickingButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(ENABLE_TICKING_TRUE_LABEL_TEXT, ENABLE_TICKING_FALSE_LABEL_TEXT).initially(this.enableTicking).omitKeyText().build(this.width / 2 - 154, 44, 150, 20, Text.empty(), (button, enableTicking) -> {
			this.enableTicking = enableTicking;
		}));

		this.showArea = this.playerDetectorBlock.showArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(SHOW_AREA_LABEL_TRUE_TEXT, SHOW_AREA_LABEL_FALSE_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 + 4, 44, 150, 20, Text.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 79, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 79, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 79, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.playerDetectorBlock.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 114, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 114, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);


		this.triggeredBlockOnEnteringPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 50, 20, Text.empty());
		this.triggeredBlockOnEnteringPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockOnEnteringPositionOffsetXField);
		this.triggeredBlockOnEnteringPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 55, 50, 20, Text.empty());
		this.triggeredBlockOnEnteringPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockOnEnteringPositionOffsetYField);
		this.triggeredBlockOnEnteringPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 55, 50, 20, Text.empty());
		this.triggeredBlockOnEnteringPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnEnteringPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockOnEnteringPositionOffsetZField);

		this.triggeredBlockOnEnteringResets = this.playerDetectorBlock.getOnEnteringTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnEnteringResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnEnteringResets).omitKeyText().build(this.width / 2 + 8, 55, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnEnteringResets = triggeredBlockResets;
		}));

		this.triggeredBlockOnLeavingPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 50, 20, Text.empty());
		this.triggeredBlockOnLeavingPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockOnLeavingPositionOffsetXField);
		this.triggeredBlockOnLeavingPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 90, 50, 20, Text.empty());
		this.triggeredBlockOnLeavingPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockOnLeavingPositionOffsetYField);
		this.triggeredBlockOnLeavingPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 90, 50, 20, Text.empty());
		this.triggeredBlockOnLeavingPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnLeavingPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockOnLeavingPositionOffsetZField);

		this.triggeredBlockOnLeavingResets = this.playerDetectorBlock.getOnLeavingTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnLeavingResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnLeavingResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnLeavingResets = triggeredBlockResets;
		}));

		this.triggeredBlockOnTriggeringPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 50, 20, Text.empty());
		this.triggeredBlockOnTriggeringPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockOnTriggeringPositionOffsetXField);
		this.triggeredBlockOnTriggeringPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 125, 50, 20, Text.empty());
		this.triggeredBlockOnTriggeringPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockOnTriggeringPositionOffsetYField);
		this.triggeredBlockOnTriggeringPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 125, 50, 20, Text.empty());
		this.triggeredBlockOnTriggeringPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockOnTriggeringPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockOnTriggeringPositionOffsetZField);

		this.triggeredBlockOnTriggeringResets = this.playerDetectorBlock.getOnTriggeringTriggeredBlock().getRight();
		this.toggleTriggeredBlockOnTriggeringResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockOnTriggeringResets).omitKeyText().build(this.width / 2 + 8, 125, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockOnTriggeringResets = triggeredBlockResets;
		}));

		this.uuidListHandlerOnEnteringPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 100, 20, Text.empty());
		this.uuidListHandlerOnEnteringPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getX()));
		this.addSelectableChild(this.uuidListHandlerOnEnteringPositionOffsetXField);
		this.uuidListHandlerOnEnteringPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 55, 100, 20, Text.empty());
		this.uuidListHandlerOnEnteringPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getY()));
		this.addSelectableChild(this.uuidListHandlerOnEnteringPositionOffsetYField);
		this.uuidListHandlerOnEnteringPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 55, 100, 20, Text.empty());
		this.uuidListHandlerOnEnteringPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnEnteringPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnEnteringUUIDListHandler().getZ()));
		this.addSelectableChild(this.uuidListHandlerOnEnteringPositionOffsetZField);

		this.uuidListHandlerOnLeavingPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 100, 20, Text.empty());
		this.uuidListHandlerOnLeavingPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getX()));
		this.addSelectableChild(this.uuidListHandlerOnLeavingPositionOffsetXField);
		this.uuidListHandlerOnLeavingPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 90, 100, 20, Text.empty());
		this.uuidListHandlerOnLeavingPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getY()));
		this.addSelectableChild(this.uuidListHandlerOnLeavingPositionOffsetYField);
		this.uuidListHandlerOnLeavingPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 90, 100, 20, Text.empty());
		this.uuidListHandlerOnLeavingPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnLeavingPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnLeavingUUIDListHandler().getZ()));
		this.addSelectableChild(this.uuidListHandlerOnLeavingPositionOffsetZField);

		this.uuidListHandlerOnTriggeringPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getX()));
		this.addSelectableChild(this.uuidListHandlerOnTriggeringPositionOffsetXField);
		this.uuidListHandlerOnTriggeringPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 125, 100, 20, Text.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getY()));
		this.addSelectableChild(this.uuidListHandlerOnTriggeringPositionOffsetYField);
		this.uuidListHandlerOnTriggeringPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 125, 100, 20, Text.empty());
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setText(Integer.toString(this.playerDetectorBlock.getOnTriggeringUUIDListHandler().getZ()));
		this.addSelectableChild(this.uuidListHandlerOnTriggeringPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 207, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

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
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.enableTicking;
		boolean bool1 = this.showArea;
		boolean bool2 = this.triggeredBlockOnEnteringResets;
		boolean bool3 = this.triggeredBlockOnLeavingResets;
		boolean bool4 = this.triggeredBlockOnTriggeringResets;
		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();
		String string6 = this.triggeredBlockOnEnteringPositionOffsetXField.getText();
		String string7 = this.triggeredBlockOnEnteringPositionOffsetYField.getText();
		String string8 = this.triggeredBlockOnEnteringPositionOffsetZField.getText();
		String string9 = this.triggeredBlockOnLeavingPositionOffsetXField.getText();
		String string10 = this.triggeredBlockOnLeavingPositionOffsetYField.getText();
		String string11 = this.triggeredBlockOnLeavingPositionOffsetZField.getText();
		String string12 = this.triggeredBlockOnTriggeringPositionOffsetXField.getText();
		String string13 = this.triggeredBlockOnTriggeringPositionOffsetYField.getText();
		String string14 = this.triggeredBlockOnTriggeringPositionOffsetZField.getText();
		String string15 = this.uuidListHandlerOnEnteringPositionOffsetXField.getText();
		String string16 = this.uuidListHandlerOnEnteringPositionOffsetYField.getText();
		String string17 = this.uuidListHandlerOnEnteringPositionOffsetZField.getText();
		String string18 = this.uuidListHandlerOnLeavingPositionOffsetXField.getText();
		String string19 = this.uuidListHandlerOnLeavingPositionOffsetYField.getText();
		String string20 = this.uuidListHandlerOnLeavingPositionOffsetZField.getText();
		String string21 = this.uuidListHandlerOnTriggeringPositionOffsetXField.getText();
		String string22 = this.uuidListHandlerOnTriggeringPositionOffsetYField.getText();
		String string23 = this.uuidListHandlerOnTriggeringPositionOffsetZField.getText();
		this.init(client, width, height);
		this.screenPage = var;
		this.enableTicking = bool;
		this.showArea = bool1;
		this.triggeredBlockOnEnteringResets = bool2;
		this.triggeredBlockOnLeavingResets = bool3;
		this.triggeredBlockOnTriggeringResets = bool4;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.toggleEnableTickingButton.setValue(this.enableTicking);
		this.toggleShowAreaButton.setValue(this.showArea);
		this.toggleTriggeredBlockOnEnteringResetsButton.setValue(this.triggeredBlockOnEnteringResets);
		this.toggleTriggeredBlockOnLeavingResetsButton.setValue(this.triggeredBlockOnLeavingResets);
		this.toggleTriggeredBlockOnTriggeringResetsButton.setValue(this.triggeredBlockOnTriggeringResets);
		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);
		this.triggeredBlockOnEnteringPositionOffsetXField.setText(string6);
		this.triggeredBlockOnEnteringPositionOffsetYField.setText(string7);
		this.triggeredBlockOnEnteringPositionOffsetZField.setText(string8);
		this.triggeredBlockOnLeavingPositionOffsetXField.setText(string9);
		this.triggeredBlockOnLeavingPositionOffsetYField.setText(string10);
		this.triggeredBlockOnLeavingPositionOffsetZField.setText(string11);
		this.triggeredBlockOnTriggeringPositionOffsetXField.setText(string12);
		this.triggeredBlockOnTriggeringPositionOffsetYField.setText(string13);
		this.triggeredBlockOnTriggeringPositionOffsetZField.setText(string14);
		this.uuidListHandlerOnEnteringPositionOffsetXField.setText(string15);
		this.uuidListHandlerOnEnteringPositionOffsetYField.setText(string16);
		this.uuidListHandlerOnEnteringPositionOffsetZField.setText(string17);
		this.uuidListHandlerOnLeavingPositionOffsetXField.setText(string18);
		this.uuidListHandlerOnLeavingPositionOffsetYField.setText(string19);
		this.uuidListHandlerOnLeavingPositionOffsetZField.setText(string20);
		this.uuidListHandlerOnTriggeringPositionOffsetXField.setText(string21);
		this.uuidListHandlerOnTriggeringPositionOffsetYField.setText(string22);
		this.uuidListHandlerOnTriggeringPositionOffsetZField.setText(string23);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {

			context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 69, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK_OFFSETS) {

			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_ON_ENTERING_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.triggeredBlockOnEnteringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnEnteringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnEnteringPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_ON_LEAVING_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.triggeredBlockOnLeavingPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnLeavingPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnLeavingPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_ON_TRIGGERING_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.triggeredBlockOnTriggeringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnTriggeringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockOnTriggeringPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.UUID_LIST_HANDLER_OFFSETS) {

			context.drawTextWithShadow(this.textRenderer, UUID_LIST_HANDLER_ON_ENTERING_POSITION_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.uuidListHandlerOnEnteringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnEnteringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnEnteringPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, UUID_LIST_HANDLER_ON_LEAVING_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.uuidListHandlerOnLeavingPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnLeavingPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnLeavingPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, UUID_LIST_HANDLER_ON_TRIGGERING_POSITION_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.uuidListHandlerOnTriggeringPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnTriggeringPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListHandlerOnTriggeringPositionOffsetZField.render(context, mouseX, mouseY, delta);

		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateAreaBlock() {
		ClientPlayNetworking.send(new UpdatePlayerDetectorBlockPacket(
				this.playerDetectorBlock.getPos(),
				this.enableTicking,
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
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnEnteringPositionOffsetZField.getText())
				),
				this.triggeredBlockOnEnteringResets,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnLeavingPositionOffsetZField.getText())
				),
				this.triggeredBlockOnLeavingResets,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockOnTriggeringPositionOffsetZField.getText())
				),
				this.triggeredBlockOnTriggeringResets,
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnEnteringPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnLeavingPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerOnTriggeringPositionOffsetZField.getText())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		AREA("area"),
		TRIGGERED_BLOCK_OFFSETS("triggered_block_offsets"),
		UUID_LIST_HANDLER_OFFSETS("uuid_list_handler_offsets");

		private final String name;

		private ScreenPage(String name) {
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
			return Text.translatable("gui.player_detector_block.screenPage." + this.name);
		}
	}
}
