package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateLocationControlBlockPacket;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class LocationControlBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component MAIN_ENTRANCE_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.location_controller_block.main_entrance.position_offset");
	private static final Component MAIN_ENTRANCE_ORIENTATION_LABEL_TEXT = Component.translatable("gui.location_controller_block.main_entrance.orientation");
	private static final Component REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.housing_screen.remove_list_entry_button_label");
	private static final Component NEW_SIDE_ENTRANCE_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.location_controller_block.new_side_entrance.position_offset");
	private static final Component NEW_SIDE_ENTRANCE_NAME_LABEL_TEXT = Component.translatable("gui.location_controller_block.new_side_entrance.name");
	private static final Component NEW_SIDE_ENTRANCE_ORIENTATION_LABEL_TEXT = Component.translatable("gui.location_controller_block.new_side_entrance.orientation");
	private static final Component TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Component RESET_AREA_MIN_X_LABEL_TEXT = Component.translatable("gui.location_controller_block.reset_area_min_x");
	private static final Component RESET_AREA_MIN_Z_LABEL_TEXT = Component.translatable("gui.location_controller_block.reset_area_min_z");
	private static final Component RESET_AREA_MAX_X_LABEL_TEXT = Component.translatable("gui.location_controller_block.reset_area_max_x");
	private static final Component RESET_AREA_MAX_Z_LABEL_TEXT = Component.translatable("gui.location_controller_block.reset_area_max_z");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private final LocationControlBlockEntity locationControlBlock;
	private CycleButton<ScreenPage> cycleScreenPageButton;
	private EditBox mainEntrancePositionOffsetXField;
	private EditBox mainEntrancePositionOffsetYField;
	private EditBox mainEntrancePositionOffsetZField;
	private EditBox mainEntranceOrientationYawField;
	private EditBox mainEntranceOrientationPitchField;
	private CycleButton<Boolean> toggleShouldAlwaysResetButton;
	private CycleButton<Boolean> toggleManualResetButton;
	private Button removeSideEntranceButton0;
	private Button removeSideEntranceButton1;
	private Button removeSideEntranceButton2;
	private EditBox newSideEntrancePositionOffsetXField;
	private EditBox newSideEntrancePositionOffsetYField;
	private EditBox newSideEntrancePositionOffsetZField;
	private EditBox newSideEntranceOrientationYawField;
	private EditBox newSideEntranceOrientationPitchField;
	private EditBox newSideEntranceNameField;
	private Button addNewSideEntranceButton;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private EditBox dataProvidingBlockPosOffsetXField;
	private EditBox dataProvidingBlockPosOffsetYField;
	private EditBox dataProvidingBlockPosOffsetZField;
	private EditBox resetAreaMinXField;
	private EditBox resetAreaMinZField;
	private EditBox resetAreaMaxXField;
	private EditBox resetAreaMaxZField;
	private ScreenPage screenPage;
	private final List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> sideEntranceList = new ArrayList<>();
	private boolean shouldAlwaysReset;
	private boolean manualReset;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public LocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
		super(GameNarrator.NO_TITLE);
		this.locationControlBlock = locationControlBlock;
		this.screenPage = ScreenPage.MAIN_ENTRANCE;
	}

	private void addNewSideEntrance() {
		String newEntranceName = this.newSideEntranceNameField.getValue();
		if (newEntranceName.isEmpty()) {
			return;
		}
		boolean bl = true;
		for (MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> stringMutablePairMutablePair : this.sideEntranceList) {
			if (stringMutablePairMutablePair.getLeft().equals(newEntranceName)) {
				bl = false;
				break;
			}
		}
		if (bl) {
			this.sideEntranceList.add(new MutablePair<>(
					this.newSideEntranceNameField.getValue(),
					new MutablePair<>(
							new BlockPos(
									ItemUtils.parseInt(this.newSideEntrancePositionOffsetXField.getValue()),
									ItemUtils.parseInt(this.newSideEntrancePositionOffsetYField.getValue()),
									ItemUtils.parseInt(this.newSideEntrancePositionOffsetZField.getValue())
							),
							new MutablePair<>(
									ItemUtils.parseDouble(this.newSideEntranceOrientationYawField.getValue()),
									ItemUtils.parseDouble(this.newSideEntranceOrientationPitchField.getValue())
							)
					)
			));
			this.scrollPosition = 0;
			this.scrollAmount = 0.0f;
			this.updateWidgets();
		} else if (this.minecraft != null && this.minecraft.player != null) {
			this.minecraft.player.displayClientMessage(Component.translatable("gui.teleporter_block.location_already_in_list"));
		}
	}

	private void removeSideEntrance(int index) {
		this.sideEntranceList.remove(index + this.scrollPosition);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.sideEntranceList.clear();
		List<String> keyList = new ArrayList<>(this.locationControlBlock.getSideEntrances().keySet());
		for (String key : keyList) {
			this.sideEntranceList.add(new MutablePair<>(key, this.locationControlBlock.getSideEntrances().get(key)));
		}
		super.init();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 18, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- main entrance page ---

		this.mainEntrancePositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetXField.setMaxLength(128);
		this.mainEntrancePositionOffsetXField.setValue(Integer.toString(this.locationControlBlock.getMainEntrance().getLeft().getX()));
		this.addWidget(this.mainEntrancePositionOffsetXField);

		this.mainEntrancePositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetYField.setMaxLength(128);
		this.mainEntrancePositionOffsetYField.setValue(Integer.toString(this.locationControlBlock.getMainEntrance().getLeft().getY()));
		this.addWidget(this.mainEntrancePositionOffsetYField);

		this.mainEntrancePositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.empty());
		this.mainEntrancePositionOffsetZField.setMaxLength(128);
		this.mainEntrancePositionOffsetZField.setValue(Integer.toString(this.locationControlBlock.getMainEntrance().getLeft().getZ()));
		this.addWidget(this.mainEntrancePositionOffsetZField);

		this.mainEntranceOrientationYawField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.empty());
		this.mainEntranceOrientationYawField.setMaxLength(128);
		this.mainEntranceOrientationYawField.setValue(Double.toString(this.locationControlBlock.getMainEntrance().getRight().getLeft()));
		this.addWidget(this.mainEntranceOrientationYawField);

		this.mainEntranceOrientationPitchField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.mainEntranceOrientationPitchField.setMaxLength(128);
		this.mainEntranceOrientationPitchField.setValue(Double.toString(this.locationControlBlock.getMainEntrance().getRight().getRight()));
		this.addWidget(this.mainEntranceOrientationPitchField);

		this.shouldAlwaysReset = this.locationControlBlock.shouldAlwaysReset();
		this.toggleShouldAlwaysResetButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.location_controller_block.toggle_should_always_reset_button_label.on"), Component.translatable("gui.location_controller_block.toggle_should_always_reset_button_label.off")).initially(this.shouldAlwaysReset).omitKeyText().build(this.width / 2 - 154, 150, 150, 20, Component.empty(), (button, shouldAlwaysReset) -> {
			this.shouldAlwaysReset = shouldAlwaysReset;
		}));

		this.manualReset = this.locationControlBlock.getManualReset();
		this.toggleManualResetButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.location_controller_block.toggle_manual_reset_button_label.on"), Component.translatable("gui.location_controller_block.toggle_manual_reset_button_label.off")).initially(this.manualReset).omitKeyText().build(this.width / 2 + 4, 150, 150, 20, Component.empty(), (button, manualReset) -> {
			this.manualReset = manualReset;
		}));

		// --- side entrances page ---

		this.removeSideEntranceButton0 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(0)).bounds(this.width / 2 + 104, 44, 50, 20).build());
		this.removeSideEntranceButton1 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(1)).bounds(this.width / 2 + 104, 68, 50, 20).build());
		this.removeSideEntranceButton2 = this.addRenderableWidget(Button.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeSideEntrance(2)).bounds(this.width / 2 + 104, 92, 50, 20).build());

		this.newSideEntrancePositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 127, 100, 20, Component.empty());
		this.newSideEntrancePositionOffsetXField.setMaxLength(128);
		this.addWidget(this.newSideEntrancePositionOffsetXField);

		this.newSideEntrancePositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 127, 100, 20, Component.empty());
		this.newSideEntrancePositionOffsetYField.setMaxLength(128);
		this.addWidget(this.newSideEntrancePositionOffsetYField);

		this.newSideEntrancePositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 127, 100, 20, Component.empty());
		this.newSideEntrancePositionOffsetZField.setMaxLength(128);
		this.addWidget(this.newSideEntrancePositionOffsetZField);

		this.newSideEntranceNameField = new EditBox(this.font, this.width / 2 - 154, 162, 204, 20, Component.empty());
		this.newSideEntranceNameField.setMaxLength(128);
		this.addWidget(this.newSideEntranceNameField);

		this.newSideEntranceOrientationYawField = new EditBox(this.font, this.width / 2 + 54, 162, 48, 20, Component.empty());
		this.newSideEntranceOrientationYawField.setMaxLength(128);
		this.addWidget(this.newSideEntranceOrientationYawField);

		this.newSideEntranceOrientationPitchField = new EditBox(this.font, this.width / 2 + 106, 162, 48, 20, Component.empty());
		this.newSideEntranceOrientationPitchField.setMaxLength(128);
		this.addWidget(this.newSideEntranceOrientationPitchField);

		this.addNewSideEntranceButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.addNewSideEntrance()).bounds(this.width / 2 - 4 - 150, 186, 308, 20).build());

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.locationControlBlock.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);

		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.locationControlBlock.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);

		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.locationControlBlock.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.locationControlBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 80, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.dataProvidingBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 115, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setValue(Integer.toString(this.locationControlBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 100, 115, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setValue(Integer.toString(this.locationControlBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 46, 115, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setValue(Integer.toString(this.locationControlBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPosOffsetZField);


		// --- reset area page ---

		this.resetAreaMinXField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.resetAreaMinXField.setMaxLength(128);
		this.resetAreaMinXField.setValue(Integer.toString(this.locationControlBlock.getResetAreaMinX()));
		this.addWidget(this.resetAreaMinXField);

		this.resetAreaMinZField = new EditBox(this.font, this.width / 2 - 154, 100, 300, 20, Component.empty());
		this.resetAreaMinZField.setMaxLength(128);
		this.resetAreaMinZField.setValue(Integer.toString(this.locationControlBlock.getResetAreaMinZ()));
		this.addWidget(this.resetAreaMinZField);

		this.resetAreaMaxXField = new EditBox(this.font, this.width / 2 - 154, 135, 300, 20, Component.empty());
		this.resetAreaMaxXField.setMaxLength(128);
		this.resetAreaMaxXField.setValue(Integer.toString(this.locationControlBlock.getResetAreaMaxX()));
		this.addWidget(this.resetAreaMaxXField);

		this.resetAreaMaxZField = new EditBox(this.font, this.width / 2 - 154, 170, 300, 20, Component.empty());
		this.resetAreaMaxZField.setMaxLength(128);
		this.resetAreaMaxZField.setValue(Integer.toString(this.locationControlBlock.getResetAreaMaxZ()));
		this.addWidget(this.resetAreaMaxZField);


		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.mainEntrancePositionOffsetXField.setVisible(false);
		this.mainEntrancePositionOffsetYField.setVisible(false);
		this.mainEntrancePositionOffsetZField.setVisible(false);
		this.mainEntranceOrientationYawField.setVisible(false);
		this.mainEntranceOrientationPitchField.setVisible(false);

		this.toggleShouldAlwaysResetButton.visible = false;
		this.toggleManualResetButton.visible = false;

		this.removeSideEntranceButton0.visible = false;
		this.removeSideEntranceButton1.visible = false;
		this.removeSideEntranceButton2.visible = false;

		this.newSideEntrancePositionOffsetXField.setVisible(false);
		this.newSideEntrancePositionOffsetYField.setVisible(false);
		this.newSideEntrancePositionOffsetZField.setVisible(false);
		this.newSideEntranceOrientationYawField.setVisible(false);
		this.newSideEntranceOrientationPitchField.setVisible(false);
		this.newSideEntranceNameField.setVisible(false);
		this.addNewSideEntranceButton.visible = false;

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		this.dataProvidingBlockPosOffsetXField.setVisible(false);
		this.dataProvidingBlockPosOffsetYField.setVisible(false);
		this.dataProvidingBlockPosOffsetZField.setVisible(false);

		this.resetAreaMinXField.setVisible(false);
		this.resetAreaMinZField.setVisible(false);
		this.resetAreaMaxXField.setVisible(false);
		this.resetAreaMaxZField.setVisible(false);

		if (this.screenPage == ScreenPage.MAIN_ENTRANCE) {

			this.mainEntrancePositionOffsetXField.setVisible(true);
			this.mainEntrancePositionOffsetYField.setVisible(true);
			this.mainEntrancePositionOffsetZField.setVisible(true);
			this.mainEntranceOrientationYawField.setVisible(true);
			this.mainEntranceOrientationPitchField.setVisible(true);

			this.toggleShouldAlwaysResetButton.visible = true;
			this.toggleManualResetButton.visible = true;

		} else if (this.screenPage == ScreenPage.SIDE_ENTRANCES) {

			int index = 0;
			for (int i = 0; i < Math.min(3, this.sideEntranceList.size()); i++) {
				if (index == 0) {
					this.removeSideEntranceButton0.visible = true;
				} else if (index == 1) {
					this.removeSideEntranceButton1.visible = true;
				} else if (index == 2) {
					this.removeSideEntranceButton2.visible = true;
				}
				index++;
			}

			this.newSideEntrancePositionOffsetXField.setVisible(true);
			this.newSideEntrancePositionOffsetYField.setVisible(true);
			this.newSideEntrancePositionOffsetZField.setVisible(true);
			this.newSideEntranceOrientationYawField.setVisible(true);
			this.newSideEntranceOrientationPitchField.setVisible(true);
			this.newSideEntranceNameField.setVisible(true);
			this.addNewSideEntranceButton.visible = true;

		} else if (this.screenPage == ScreenPage.INTERACTED_BLOCKS) {

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockResetsButton.visible = true;

			this.dataProvidingBlockPosOffsetXField.setVisible(true);
			this.dataProvidingBlockPosOffsetYField.setVisible(true);
			this.dataProvidingBlockPosOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.RESET_AREA) {

			this.resetAreaMinXField.setVisible(true);
			this.resetAreaMinZField.setVisible(true);
			this.resetAreaMaxXField.setVisible(true);
			this.resetAreaMaxZField.setVisible(true);

		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> list = new ArrayList<>(this.sideEntranceList);
		ScreenPage var = this.screenPage;
		int number = this.scrollPosition;
		float number1 = this.scrollAmount;
		boolean bl = this.shouldAlwaysReset;
		boolean bl1 = this.manualReset;
		boolean bl2 = this.triggeredBlockResets;
		String string = this.mainEntrancePositionOffsetXField.getValue();
		String string1 = this.mainEntrancePositionOffsetYField.getValue();
		String string2 = this.mainEntrancePositionOffsetZField.getValue();
		String string3 = this.mainEntranceOrientationYawField.getValue();
		String string4 = this.mainEntranceOrientationPitchField.getValue();
		String string5 = this.newSideEntrancePositionOffsetXField.getValue();
		String string6 = this.newSideEntrancePositionOffsetYField.getValue();
		String string7 = this.newSideEntrancePositionOffsetZField.getValue();
		String string8 = this.newSideEntranceOrientationYawField.getValue();
		String string9 = this.newSideEntranceOrientationPitchField.getValue();
		String string10 = this.newSideEntranceNameField.getValue();
		String string11 = this.triggeredBlockPositionOffsetXField.getValue();
		String string12 = this.triggeredBlockPositionOffsetYField.getValue();
		String string13 = this.triggeredBlockPositionOffsetZField.getValue();
		String string14 = this.dataProvidingBlockPosOffsetXField.getValue();
		String string15 = this.dataProvidingBlockPosOffsetYField.getValue();
		String string16 = this.dataProvidingBlockPosOffsetZField.getValue();
		String string17 = this.resetAreaMinXField.getValue();
		String string18 = this.resetAreaMinZField.getValue();
		String string19 = this.resetAreaMaxXField.getValue();
		String string20 = this.resetAreaMaxZField.getValue();
		this.init(client, width, height);
		this.sideEntranceList.clear();
		this.sideEntranceList.addAll(list);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.scrollPosition = number;
		this.scrollAmount = number1;
		this.shouldAlwaysReset = bl;
		this.toggleShouldAlwaysResetButton.setValue(this.shouldAlwaysReset);
		this.manualReset = bl1;
		this.toggleManualResetButton.setValue(this.manualReset);
		this.triggeredBlockResets = bl2;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.mainEntrancePositionOffsetXField.setValue(string);
		this.mainEntrancePositionOffsetYField.setValue(string1);
		this.mainEntrancePositionOffsetZField.setValue(string2);
		this.mainEntranceOrientationYawField.setValue(string3);
		this.mainEntranceOrientationPitchField.setValue(string4);
		this.newSideEntrancePositionOffsetXField.setValue(string5);
		this.newSideEntrancePositionOffsetYField.setValue(string6);
		this.newSideEntrancePositionOffsetZField.setValue(string7);
		this.newSideEntranceOrientationYawField.setValue(string8);
		this.newSideEntranceOrientationPitchField.setValue(string9);
		this.newSideEntranceNameField.setValue(string10);
		this.triggeredBlockPositionOffsetXField.setValue(string11);
		this.triggeredBlockPositionOffsetYField.setValue(string12);
		this.triggeredBlockPositionOffsetZField.setValue(string13);
		this.dataProvidingBlockPosOffsetXField.setValue(string14);
		this.dataProvidingBlockPosOffsetYField.setValue(string15);
		this.dataProvidingBlockPosOffsetZField.setValue(string16);
		this.resetAreaMinXField.setValue(string17);
		this.resetAreaMinZField.setValue(string18);
		this.resetAreaMaxXField.setValue(string19);
		this.resetAreaMaxZField.setValue(string20);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.SIDE_ENTRANCES
				&& this.sideEntranceList.size() > 3) {
			int i = this.width / 2 - 152;
			int j = 45;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 68)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.SIDE_ENTRANCES
				&& this.sideEntranceList.size() > 3
				&& this.mouseClicked) {
			int i = this.sideEntranceList.size() - 3;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.SIDE_ENTRANCES
				&& this.sideEntranceList.size() > 3
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 100)
				&& mouseY >= 44 && mouseY <= 114) {
			int i = this.sideEntranceList.size() - 3;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.MAIN_ENTRANCE) {
			context.drawString(this.font, MAIN_ENTRANCE_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.mainEntrancePositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.mainEntrancePositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.mainEntrancePositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, MAIN_ENTRANCE_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.mainEntranceOrientationYawField.render(context, mouseX, mouseY, delta);
			this.mainEntranceOrientationPitchField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.SIDE_ENTRANCES) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 3, this.sideEntranceList.size()); i++) {
				String text = this.sideEntranceList.get(i).getLeft();
				if (!this.sideEntranceList.get(i).getLeft().equals("")) {
					text = this.sideEntranceList.get(i).getLeft() + ", " + this.sideEntranceList.get(i).getRight();
				}
				context.drawString(this.font, text, this.width / 2 - 141, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.sideEntranceList.size() > 3) {
//                context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 44, 8, 70);
				context.blit(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 44, 0, 0, 8, 70);
				int k = (int) (61.0f * this.scrollAmount);
//                context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
				context.blit(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 0, 0, 6, 7);
			}
			context.drawString(this.font, NEW_SIDE_ENTRANCE_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 116, 0xA0A0A0);
			this.newSideEntrancePositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newSideEntrancePositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newSideEntrancePositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, NEW_SIDE_ENTRANCE_NAME_LABEL_TEXT, this.width / 2 - 153, 151, 0xA0A0A0);
			context.drawString(this.font, NEW_SIDE_ENTRANCE_ORIENTATION_LABEL_TEXT, this.width / 2 + 55, 151, 0xA0A0A0);
			this.newSideEntranceOrientationYawField.render(context, mouseX, mouseY, delta);
			this.newSideEntranceOrientationPitchField.render(context, mouseX, mouseY, delta);
			this.newSideEntranceNameField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.INTERACTED_BLOCKS) {
			context.drawString(this.font, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.RESET_AREA) {
			context.drawString(this.font, RESET_AREA_MIN_X_LABEL_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
			this.resetAreaMinXField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, RESET_AREA_MIN_Z_LABEL_TEXT, this.width / 2 - 153, 90, 0xA0A0A0);
			this.resetAreaMinZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, RESET_AREA_MAX_X_LABEL_TEXT, this.width / 2 - 153, 125, 0xA0A0A0);
			this.resetAreaMaxXField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, RESET_AREA_MAX_Z_LABEL_TEXT, this.width / 2 - 153, 160, 0xA0A0A0);
			this.resetAreaMaxZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateLocationControlBlockPacket(
				this.locationControlBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.mainEntrancePositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.mainEntrancePositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.mainEntrancePositionOffsetZField.getValue())
				),
				ItemUtils.parseDouble(this.mainEntranceOrientationYawField.getValue()),
				ItemUtils.parseDouble(this.mainEntranceOrientationPitchField.getValue()),
				this.sideEntranceList,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getValue())
				),
				this.shouldAlwaysReset,
				this.manualReset,
				ItemUtils.parseInt(this.resetAreaMinXField.getValue()),
				ItemUtils.parseInt(this.resetAreaMinZField.getValue()),
				ItemUtils.parseInt(this.resetAreaMaxXField.getValue()),
				ItemUtils.parseInt(this.resetAreaMaxZField.getValue())
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		MAIN_ENTRANCE("main_entrance"),
		SIDE_ENTRANCES("side_entrances"),
		INTERACTED_BLOCKS("interacted_blocks"),
		RESET_AREA("reset_area");

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
			return Component.translatable("gui.location_controller_block.screen_page." + this.name);
		}
	}
}
