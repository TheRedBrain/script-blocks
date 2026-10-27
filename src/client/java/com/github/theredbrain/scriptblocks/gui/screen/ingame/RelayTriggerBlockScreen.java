package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRelayTriggerBlockPacket;
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
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class RelayTriggerBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component HIDE_AREA_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.hide_area_label");
	private static final Component SHOW_AREA_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.show_area_label");
	private static final Component RESETS_AREA_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.resets_area_label");
	private static final Component TRIGGERS_AREA_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.triggers_area_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.area_position_offset_label");
	private static final Component NEW_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.triggered_block.new_triggered_block_position_offset");
	private static final Component NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Component NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Component NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Component NEW_POSITION_CHANCE_FIELD_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.new_triggered_block_chance_label");
	private static final Component ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Component DATA_IDENTIFIER_FIELD_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.data_identifier_label");
	private static final Component TRIGGER_AMOUNT_FIELD_LABEL_TEXT = Component.translatable("gui.relay_trigger_block.trigger_amount_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_68_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_68");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 3;

	private final RelayTriggerBlockEntity relayTriggerBlock;

	private CycleButton<RelayTriggerBlockEntity.SelectionMode> selectionModeButton;
	private RelayTriggerBlockEntity.SelectionMode selectionMode;

	private CycleButton<Boolean> toggleShowAreaButton;
	private CycleButton<Boolean> toggleResetsAreaButton;
	private EditBox areaDimensionsXField;
	private EditBox areaDimensionsYField;
	private EditBox areaDimensionsZField;
	private EditBox areaPositionOffsetXField;
	private EditBox areaPositionOffsetYField;
	private EditBox areaPositionOffsetZField;
	private boolean showArea;
	private boolean resetsArea;

	private Button removeListEntryButton0;
	private Button removeListEntryButton1;
	private Button removeListEntryButton2;
	private EditBox newTriggeredBlockPositionOffsetXField;
	private EditBox newTriggeredBlockPositionOffsetYField;
	private EditBox newTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleNewTriggeredBlockResetsButton;
	private boolean newTriggeredBlockResets;
	private EditBox newTriggeredBlockChanceField;
	private Button addNewTriggeredBlockPositionOffsetButton;
	private CycleButton<RelayTriggerBlockEntity.TriggerMode> cycleTriggerModeButton;
	private CycleButton<Boolean> toggleIsTriggerAmountDataDrivenButton;
	private EditBox dataProvidingBlockPosOffsetXField;
	private EditBox dataProvidingBlockPosOffsetYField;
	private EditBox dataProvidingBlockPosOffsetZField;
	private EditBox dataIdentifierField;
	private EditBox triggerAmountField;

	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());
	private RelayTriggerBlockEntity.TriggerMode triggerMode;
	private boolean isTriggerAmountDataDriven;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public RelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
		super(GameNarrator.NO_TITLE);
		this.relayTriggerBlock = relayTriggerBlock;
	}

	private void addNewTriggeredBlock() {
		int chance = ItemUtils.parseInt(this.newTriggeredBlockChanceField.getValue());
		if (chance < 0) {
			chance = 0;
		}
		MutablePair<MutablePair<BlockPos, Boolean>, Integer> newTriggeredBlock = new MutablePair<>(
				new MutablePair<>(
						new BlockPos(
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetZField.getValue())
						),
						this.newTriggeredBlockResets
				),
				chance
		);
		for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
			if (triggeredBlock.equals(newTriggeredBlock)) {
				return;
			}
		}
		this.triggeredBlocks.add(newTriggeredBlock);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void removeTriggeredBlock(int index) {
		if (index + this.scrollPosition < this.triggeredBlocks.size()) {
			this.triggeredBlocks.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {

		super.init();

		this.selectionMode = this.relayTriggerBlock.getSelectionMode();
		this.selectionModeButton = this.addRenderableWidget(CycleButton.builder(RelayTriggerBlockEntity.SelectionMode::asText).values((RelayTriggerBlockEntity.SelectionMode[]) RelayTriggerBlockEntity.SelectionMode.values()).initially(this.selectionMode).omitKeyText().build(this.width / 2 - 154, 10, 300, 20, Component.empty(), (button, selectionMode) -> {
			this.selectionMode = selectionMode;
			this.updateWidgets();
		}));

		// Area Selection

		this.showArea = this.relayTriggerBlock.getShowArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 34, 300, 20, Component.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 69, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 54, 69, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 46, 69, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 104, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 54, 104, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 46, 104, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);

		this.resetsArea = this.relayTriggerBlock.getResetsArea();
		this.toggleResetsAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(RESETS_AREA_LABEL_TEXT, TRIGGERS_AREA_LABEL_TEXT).initially(this.resetsArea).omitKeyText().build(this.width / 2 - 154, 128, 300, 20, Component.empty(), (button, resetsArea) -> {
			this.resetsArea = resetsArea;
		}));

		// List Selection

		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(this.relayTriggerBlock.getTriggeredBlocks());

		this.removeListEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 34, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(0)));
		this.removeListEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 58, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(1)));
		this.removeListEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 82, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(2)));

		this.newTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 117, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetXField.setHint(NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetXField);

		this.newTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 104, 117, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetYField.setHint(NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetYField);

		this.newTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 54, 117, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetZField.setHint(NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetZField);

		this.newTriggeredBlockResets = false;
		this.toggleNewTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newTriggeredBlockResets).omitKeyText().build(this.width / 2 + 4, 117, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.newTriggeredBlockResets = triggeredBlockResets;
		}));

		this.newTriggeredBlockChanceField = new EditBox(this.font, this.width / 2 - 154, 152, 70, 20, Component.empty());
		this.newTriggeredBlockChanceField.setValue(Integer.toString(100));
		this.addWidget(this.newTriggeredBlockChanceField);

		this.addNewTriggeredBlockPositionOffsetButton = this.addRenderableWidget(Button.builder(ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT, button -> this.addNewTriggeredBlock()).bounds(this.width / 2 - 80, 152, 100, 20).build());

		this.triggerMode = this.relayTriggerBlock.getTriggerMode();
		this.cycleTriggerModeButton = this.addRenderableWidget(CycleButton.builder(RelayTriggerBlockEntity.TriggerMode::asText).values((RelayTriggerBlockEntity.TriggerMode[]) RelayTriggerBlockEntity.TriggerMode.values()).initially(this.triggerMode).omitKeyText().build(this.width / 2 + 24, 152, 130, 20, Component.empty(), (button, triggerMode) -> {
			this.triggerMode = triggerMode;
			this.updateWidgets();
		}));

		this.isTriggerAmountDataDriven = this.relayTriggerBlock.isTriggerAmountDataDriven();
		this.toggleIsTriggerAmountDataDrivenButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.relay_trigger_block.toggle_is_trigger_amount_data_driven_button_label.on"), Component.translatable("gui.relay_trigger_block.toggle_is_trigger_amount_data_driven_button_label.off")).initially(this.isTriggerAmountDataDriven).omitKeyText().build(this.width / 2 - 154, 187, 100, 20, Component.empty(), (button, isTriggerAmountDataDriven) -> {
			this.isTriggerAmountDataDriven = isTriggerAmountDataDriven;
			this.updateWidgets();
		}));

		this.dataProvidingBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 50, 187, 40, 20, Component.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setValue(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 10, 187, 40, 20, Component.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setValue(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new EditBox(this.font, this.width / 2 + 30, 187, 40, 20, Component.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setValue(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPosOffsetZField);

		this.dataIdentifierField = new EditBox(this.font, this.width / 2 + 74, 187, 80, 20, Component.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setValue(this.relayTriggerBlock.getDataIdentifier());
		this.addWidget(this.dataIdentifierField);

		this.triggerAmountField = new EditBox(this.font, this.width / 2 + 74, 187, 80, 20, Component.empty());
		this.triggerAmountField.setValue(Integer.toString(this.relayTriggerBlock.getTriggerAmount()));
		this.addWidget(this.triggerAmountField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 154, 211, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 211, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.selectionModeButton);
	}

	@Override
	protected void updateWidgets() {
		this.toggleShowAreaButton.visible = false;

		this.areaDimensionsXField.setVisible(false);
		this.areaDimensionsYField.setVisible(false);
		this.areaDimensionsZField.setVisible(false);

		this.areaPositionOffsetXField.setVisible(false);
		this.areaPositionOffsetYField.setVisible(false);
		this.areaPositionOffsetZField.setVisible(false);

		this.toggleResetsAreaButton.visible = false;


		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;

		this.newTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newTriggeredBlockPositionOffsetZField.setVisible(false);

		this.toggleNewTriggeredBlockResetsButton.visible = false;

		this.newTriggeredBlockChanceField.setVisible(false);

		this.addNewTriggeredBlockPositionOffsetButton.visible = false;
		this.cycleTriggerModeButton.visible = false;

		this.toggleIsTriggerAmountDataDrivenButton.visible = false;

		this.dataProvidingBlockPosOffsetXField.setVisible(false);
		this.dataProvidingBlockPosOffsetYField.setVisible(false);
		this.dataProvidingBlockPosOffsetZField.setVisible(false);
		this.dataIdentifierField.setVisible(false);

		this.triggerAmountField.setVisible(false);

		if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.AREA) {
			this.toggleShowAreaButton.visible = true;

			this.areaDimensionsXField.setVisible(true);
			this.areaDimensionsYField.setVisible(true);
			this.areaDimensionsZField.setVisible(true);

			this.areaPositionOffsetXField.setVisible(true);
			this.areaPositionOffsetYField.setVisible(true);
			this.areaPositionOffsetZField.setVisible(true);

			this.toggleResetsAreaButton.visible = true;

		} else if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.LIST) {
			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				if (index == 0) {
					this.removeListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeListEntryButton2.visible = true;
				}
				index++;
			}

			this.newTriggeredBlockPositionOffsetXField.setVisible(true);
			this.newTriggeredBlockPositionOffsetYField.setVisible(true);
			this.newTriggeredBlockPositionOffsetZField.setVisible(true);

			this.toggleNewTriggeredBlockResetsButton.visible = true;

			this.newTriggeredBlockChanceField.setVisible(true);

			this.addNewTriggeredBlockPositionOffsetButton.visible = true;
			this.cycleTriggerModeButton.visible = true;

			if (this.triggerMode == RelayTriggerBlockEntity.TriggerMode.BINOMIAL || this.triggerMode == RelayTriggerBlockEntity.TriggerMode.HYPER_GEOMETRIC) {
				this.toggleIsTriggerAmountDataDrivenButton.visible = true;

				if (this.isTriggerAmountDataDriven) {

					this.dataProvidingBlockPosOffsetXField.setVisible(true);
					this.dataProvidingBlockPosOffsetYField.setVisible(true);
					this.dataProvidingBlockPosOffsetZField.setVisible(true);

					this.dataIdentifierField.setVisible(true);

				} else {

					this.triggerAmountField.setVisible(true);

				}
			}
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		RelayTriggerBlockEntity.SelectionMode var = this.selectionMode;
		RelayTriggerBlockEntity.TriggerMode var1 = this.triggerMode;
		boolean bool = this.showArea;
		boolean bool1 = this.resetsArea;
		boolean bool2 = this.newTriggeredBlockResets;
		boolean bool3 = this.isTriggerAmountDataDriven;
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> list = new ArrayList<>(this.triggeredBlocks);
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.newTriggeredBlockPositionOffsetXField.getValue();
		String string7 = this.newTriggeredBlockPositionOffsetYField.getValue();
		String string8 = this.newTriggeredBlockPositionOffsetZField.getValue();
		String string9 = this.newTriggeredBlockChanceField.getValue();
		String string10 = this.dataProvidingBlockPosOffsetXField.getValue();
		String string11 = this.dataProvidingBlockPosOffsetYField.getValue();
		String string12 = this.dataProvidingBlockPosOffsetZField.getValue();
		String string13 = this.dataIdentifierField.getValue();
		String string14 = this.triggerAmountField.getValue();
		this.init(client, width, height);
		this.selectionMode = var;
		this.triggerMode = var1;
		this.showArea = bool;
		this.resetsArea = bool1;
		this.newTriggeredBlockResets = bool2;
		this.isTriggerAmountDataDriven = bool3;
		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(list);
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.newTriggeredBlockPositionOffsetXField.setValue(string6);
		this.newTriggeredBlockPositionOffsetYField.setValue(string7);
		this.newTriggeredBlockPositionOffsetZField.setValue(string8);
		this.newTriggeredBlockChanceField.setValue(string9);
		this.dataProvidingBlockPosOffsetXField.setValue(string10);
		this.dataProvidingBlockPosOffsetYField.setValue(string11);
		this.dataProvidingBlockPosOffsetZField.setValue(string12);
		this.dataIdentifierField.setValue(string13);
		this.triggerAmountField.setValue(string14);
		this.selectionModeButton.setValue(this.selectionMode);
		this.cycleTriggerModeButton.setValue(this.triggerMode);
		this.toggleShowAreaButton.setValue(this.showArea);
		this.toggleResetsAreaButton.setValue(this.resetsArea);
		this.toggleNewTriggeredBlockResetsButton.setValue(this.newTriggeredBlockResets);
		this.toggleIsTriggerAmountDataDrivenButton.setValue(this.isTriggerAmountDataDriven);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 34;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 68)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.triggeredBlocks.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 34 && mouseY <= 102) {
			int i = this.triggeredBlocks.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.AREA) {

			context.drawString(this.font, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 59, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 94, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.LIST) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock = this.triggeredBlocks.get(i);
				BlockPos triggeredBlockPos = triggeredBlock.left.left;
				MutableComponent text = Component.translatable("gui.triggered_block.list.entry", triggeredBlockPos.getX(), triggeredBlockPos.getY(), triggeredBlockPos.getZ());
				if (this.triggerMode == RelayTriggerBlockEntity.TriggerMode.NORMAL) {
					text.append(triggeredBlock.left.right ? Component.translatable("gui.triggered_block.list.is_reset") : Component.translatable("gui.triggered_block.list.is_triggered"));
					context.drawString(this.font, text, this.width / 2 - 117, 40 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
				} else {
					Component text1;
					if (this.triggerMode == RelayTriggerBlockEntity.TriggerMode.RANDOM) {
						text1 = Component.translatable("gui.triggered_block.list.chance", triggeredBlock.right).append(triggeredBlock.left.right ? Component.translatable("gui.triggered_block.list.reset") : Component.translatable("gui.triggered_block.list.triggered"));
					} else {
						text1 = Component.translatable("gui.triggered_block.list.weight", triggeredBlock.right);
					}
					context.drawString(this.font, text, this.width / 2 - 117, 35 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
					context.drawString(this.font, text1, this.width / 2 - 117, 45 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
				}
			}
			if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_68_TEXTURE, this.width / 2 - 153, 34, 8, 68);
				int k = (int) (59.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_TEXTURE, this.width / 2 - 152, 34 + 1 + k, 6, 7);
			}
			context.drawString(this.font, NEW_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 107, 0xA0A0A0);
			this.newTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			if (this.triggerMode != RelayTriggerBlockEntity.TriggerMode.NORMAL) {
				context.drawString(this.font, NEW_POSITION_CHANCE_FIELD_LABEL_TEXT, this.width / 2 - 153, 142, 0xA0A0A0);
				this.newTriggeredBlockChanceField.render(context, mouseX, mouseY, delta);

				if (this.triggerMode != RelayTriggerBlockEntity.TriggerMode.RANDOM) {

					if (this.isTriggerAmountDataDriven) {

						context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 49, 177, 0xA0A0A0);
						this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
						this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
						this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

						context.drawString(this.font, DATA_IDENTIFIER_FIELD_LABEL_TEXT, this.width / 2 + 75, 177, 0xA0A0A0);
						this.dataIdentifierField.render(context, mouseX, mouseY, delta);

					} else {

						context.drawString(this.font, TRIGGER_AMOUNT_FIELD_LABEL_TEXT, this.width / 2 + 75, 177, 0xA0A0A0);
						this.triggerAmountField.render(context, mouseX, mouseY, delta);

					}
				}
			}
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(
				new UpdateRelayTriggerBlockPacket(
						this.relayTriggerBlock.getPos(),
						this.selectionMode.asString(),
						this.showArea,
						this.resetsArea,
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
						this.triggeredBlocks,
						this.triggerMode.asString(),
						this.isTriggerAmountDataDriven,
						new BlockPos(
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getValue()),
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getValue()),
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getValue())
						),
						this.dataIdentifierField.getValue(),
						ItemUtils.parseInt(this.triggerAmountField.getValue())
				)
		);
		return true;
	}
}
