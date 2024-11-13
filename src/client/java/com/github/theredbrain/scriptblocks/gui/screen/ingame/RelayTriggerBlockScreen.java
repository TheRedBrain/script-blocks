package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRelayTriggerBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class RelayTriggerBlockScreen extends Screen {
	private static final Text HIDE_AREA_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.hide_area_label");
	private static final Text SHOW_AREA_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.show_area_label");
	private static final Text RESETS_AREA_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.resets_area_label");
	private static final Text TRIGGERS_AREA_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.triggers_area_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.area_position_offset_label");
	private static final Text NEW_TRIGGERED_BLOCK_POSITION_TEXT = Text.translatable("gui.triggered_block.newTriggeredBlockPositionOffset");
	private static final Text NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetX.placeholder");
	private static final Text NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetY.placeholder");
	private static final Text NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetZ.placeholder");
	private static final Text NEW_POSITION_CHANCE_FIELD_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.new_triggered_block_chance_label");
	private static final Text ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.data_block_pos_offset");
	private static final Text DATA_IDENTIFIER_FIELD_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.data_identifier_label");
	private static final Text TRIGGER_AMOUNT_FIELD_LABEL_TEXT = Text.translatable("gui.relay_trigger_block.trigger_amount_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_68_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_68");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final RelayTriggerBlockEntity relayTriggerBlock;

	private CyclingButtonWidget<RelayTriggerBlockEntity.SelectionMode> selectionModeButton;
	private RelayTriggerBlockEntity.SelectionMode selectionMode;

	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private CyclingButtonWidget<Boolean> toggleResetsAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private boolean showArea;
	private boolean resetsArea;

	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private TextFieldWidget newTriggeredBlockPositionOffsetXField;
	private TextFieldWidget newTriggeredBlockPositionOffsetYField;
	private TextFieldWidget newTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleNewTriggeredBlockResetsButton;
	private boolean newTriggeredBlockResets;
	private TextFieldWidget newTriggeredBlockChanceField;
	private ButtonWidget addNewTriggeredBlockPositionOffsetButton;
	private CyclingButtonWidget<RelayTriggerBlockEntity.TriggerMode> cycleTriggerModeButton;
	private CyclingButtonWidget<Boolean> toggleIsTriggerAmountDataDrivenButton;
	private TextFieldWidget dataProvidingBlockPosOffsetXField;
	private TextFieldWidget dataProvidingBlockPosOffsetYField;
	private TextFieldWidget dataProvidingBlockPosOffsetZField;
	private TextFieldWidget dataIdentifierField;
	private TextFieldWidget triggerAmountField;

	private static final int VISIBLE_LIST_ELEMENTS = 3;
	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());
	private RelayTriggerBlockEntity.TriggerMode triggerMode;
	private boolean isTriggerAmountDataDriven;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public RelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
		super(NarratorManager.EMPTY);
		this.relayTriggerBlock = relayTriggerBlock;
	}

	private void addNewTriggeredBlock() {
		int chance = ItemUtils.parseInt(this.newTriggeredBlockChanceField.getText());
//		if (chance > 100) {
//			chance = 100;
//		} else
		if (chance < 0) {
			chance = 0;
		}
		MutablePair<MutablePair<BlockPos, Boolean>, Integer> newTriggeredBlock = new MutablePair<>(
				new MutablePair<>(
						new BlockPos(
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetZField.getText())
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

	private void done() {
		this.updateRelayTriggerBlock();
		this.close();
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		super.init();

		this.selectionMode = this.relayTriggerBlock.getSelectionMode();
		this.selectionModeButton = this.addDrawableChild(CyclingButtonWidget.builder(RelayTriggerBlockEntity.SelectionMode::asText).values((RelayTriggerBlockEntity.SelectionMode[]) RelayTriggerBlockEntity.SelectionMode.values()).initially(this.selectionMode).omitKeyText().build(this.width / 2 - 154, 10, 300, 20, Text.empty(), (button, selectionMode) -> {
			this.selectionMode = selectionMode;
			this.updateWidgets();
		}));

		// Area Selection

		this.showArea = this.relayTriggerBlock.getShowArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 34, 300, 20, Text.empty(), (button, showArea) -> {
			this.showArea = showArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 69, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 69, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 46, 69, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.relayTriggerBlock.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 104, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 104, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 46, 104, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.relayTriggerBlock.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);

		this.resetsArea = this.relayTriggerBlock.getResetsArea();
		this.toggleResetsAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(RESETS_AREA_LABEL_TEXT, TRIGGERS_AREA_LABEL_TEXT).initially(this.resetsArea).omitKeyText().build(this.width / 2 - 154, 128, 300, 20, Text.empty(), (button, resetsArea) -> {
			this.resetsArea = resetsArea;
		}));

		// List Selection

		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(this.relayTriggerBlock.getTriggeredBlocks());

		this.removeListEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 34, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(0)));
		this.removeListEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 58, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(1)));
		this.removeListEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 82, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(2)));

		this.newTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 117, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetXField.setPlaceholder(NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetXField);

		this.newTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 117, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetYField.setPlaceholder(NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetYField);

		this.newTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 117, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetZField.setPlaceholder(NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetZField);

		this.newTriggeredBlockResets = false;
		this.toggleNewTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newTriggeredBlockResets).omitKeyText().build(this.width / 2 + 4, 117, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.newTriggeredBlockResets = triggeredBlockResets;
		}));

		this.newTriggeredBlockChanceField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 152, 70, 20, Text.empty());
		this.newTriggeredBlockChanceField.setText(Integer.toString(100));
		this.addSelectableChild(this.newTriggeredBlockChanceField);

		this.addNewTriggeredBlockPositionOffsetButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT, button -> this.addNewTriggeredBlock()).dimensions(this.width / 2 - 80, 152, 100, 20).build());

		this.triggerMode = this.relayTriggerBlock.getTriggerMode();
		this.cycleTriggerModeButton = this.addDrawableChild(CyclingButtonWidget.builder(RelayTriggerBlockEntity.TriggerMode::asText).values((RelayTriggerBlockEntity.TriggerMode[]) RelayTriggerBlockEntity.TriggerMode.values()).initially(this.triggerMode).omitKeyText().build(this.width / 2 + 24, 152, 130, 20, Text.empty(), (button, triggerMode) -> {
			this.triggerMode = triggerMode;
			this.updateWidgets();
		}));

		this.isTriggerAmountDataDriven = this.relayTriggerBlock.isTriggerAmountDataDriven();
		this.toggleIsTriggerAmountDataDrivenButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.relay_trigger_block.toggle_is_trigger_amount_data_driven_button_label.on"), Text.translatable("gui.relay_trigger_block.toggle_is_trigger_amount_data_driven_button_label.off")).initially(this.isTriggerAmountDataDriven).omitKeyText().build(this.width / 2 - 154, 187, 100, 20, Text.empty(), (button, isTriggerAmountDataDriven) -> {
			this.isTriggerAmountDataDriven = isTriggerAmountDataDriven;
			this.updateWidgets();
		}));

		this.dataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 187, 40, 20, Text.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setText(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 10, 187, 40, 20, Text.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setText(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 30, 187, 40, 20, Text.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setText(Integer.toString(this.relayTriggerBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPosOffsetZField);

		this.dataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 + 74, 187, 80, 20, Text.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setText(this.relayTriggerBlock.getDataIdentifier());
		this.addSelectableChild(this.dataIdentifierField);

		this.triggerAmountField = new TextFieldWidget(this.textRenderer, this.width / 2 + 74, 187, 80, 20, Text.empty());
		this.triggerAmountField.setText(Integer.toString(this.relayTriggerBlock.getTriggerAmount()));
		this.addSelectableChild(this.triggerAmountField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 154, 211, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 211, 150, 20).build());
		this.setInitialFocus(this.newTriggeredBlockPositionOffsetXField);
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.selectionModeButton);
	}

	private void updateWidgets() {
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
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> list = new ArrayList<>(this.triggeredBlocks);
		String string = this.newTriggeredBlockPositionOffsetXField.getText();
		String string1 = this.newTriggeredBlockPositionOffsetYField.getText();
		String string2 = this.newTriggeredBlockPositionOffsetZField.getText();
		this.init(client, width, height);
		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(list);
		this.newTriggeredBlockPositionOffsetXField.setText(string);
		this.newTriggeredBlockPositionOffsetYField.setText(string1);
		this.newTriggeredBlockPositionOffsetZField.setText(string2);
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
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
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

	private void updateRelayTriggerBlock() {
		ClientPlayNetworking.send(
				new UpdateRelayTriggerBlockPacket(
						this.relayTriggerBlock.getPos(),
						this.selectionMode.asString(),
						this.showArea,
						this.resetsArea,
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
						this.triggeredBlocks,
						this.triggerMode.asString(),
						this.isTriggerAmountDataDriven,
						new BlockPos(
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getText()),
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getText()),
								ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getText())
						),
						this.dataIdentifierField.getText(),
						ItemUtils.parseInt(this.triggerAmountField.getText())
				)
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.AREA) {

			context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 59, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 94, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.selectionMode == RelayTriggerBlockEntity.SelectionMode.LIST) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock = this.triggeredBlocks.get(i);
				BlockPos triggeredBlockPos = triggeredBlock.left.left;
				MutableText text = Text.translatable("gui.triggered_block.list.entry", triggeredBlockPos.getX(), triggeredBlockPos.getY(), triggeredBlockPos.getZ());
				if (this.triggerMode == RelayTriggerBlockEntity.TriggerMode.NORMAL) {
					context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 40 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
					text.append(Text.translatable("gui.triggered_block.list.chance", triggeredBlock.right));
				} else {
					Text text1;
					if (this.triggerMode == RelayTriggerBlockEntity.TriggerMode.RANDOM) {
						text1 = Text.translatable("gui.triggered_block.list.chance", triggeredBlock.right).append(triggeredBlock.left.right ? Text.translatable("gui.triggered_block.list.reset") : Text.translatable("gui.triggered_block.list.triggered"));
					} else {
						text1 = Text.translatable("gui.triggered_block.list.weight", triggeredBlock.right);
					}
					context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 35 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
					context.drawTextWithShadow(this.textRenderer, text1, this.width / 2 - 117, 45 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
				}
			}
			if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_68_TEXTURE, this.width / 2 - 153, 34, 8, 68);
				int k = (int) (59.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 34 + 1 + k, 6, 7);
			}
			context.drawTextWithShadow(this.textRenderer, NEW_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 107, 0xA0A0A0);
			this.newTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			if (this.triggerMode != RelayTriggerBlockEntity.TriggerMode.NORMAL) {
				context.drawTextWithShadow(this.textRenderer, NEW_POSITION_CHANCE_FIELD_LABEL_TEXT, this.width / 2 - 153, 142, 0xA0A0A0);
				this.newTriggeredBlockChanceField.render(context, mouseX, mouseY, delta);

				if (this.triggerMode != RelayTriggerBlockEntity.TriggerMode.RANDOM) {

					if (this.isTriggerAmountDataDriven) {

						context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 49, 177, 0xA0A0A0);
						this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
						this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
						this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

						context.drawTextWithShadow(this.textRenderer, DATA_IDENTIFIER_FIELD_LABEL_TEXT, this.width / 2 + 75, 177, 0xA0A0A0);
						this.dataIdentifierField.render(context, mouseX, mouseY, delta);

					} else {

						context.drawTextWithShadow(this.textRenderer, TRIGGER_AMOUNT_FIELD_LABEL_TEXT, this.width / 2 + 75, 177, 0xA0A0A0);
						this.triggerAmountField.render(context, mouseX, mouseY, delta);

					}
				}
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
