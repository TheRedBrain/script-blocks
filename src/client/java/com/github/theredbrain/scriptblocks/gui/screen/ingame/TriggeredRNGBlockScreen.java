package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredRNGBlockPacket;
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
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class TriggeredRNGBlockScreen extends Screen {
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private static final Text OVERRIDE_DATA_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.override_data_identifier_label");
	private static final Text OVERRIDE_DATA_VALUE_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.override_data_value_label");
	private static final Text OVERRIDE_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.override_triggered_block_pos_offset");

	private static final Text INFLUENCING_ATTRIBUTE_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.influencing_attribute_label");
	private static final Text TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.triggered_rng_block.toggle_checks_team_attributes_button_label.on");
	private static final Text TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.triggered_rng_block.toggle_checks_team_attributes_button_label.off");
	private static final Text TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.triggered_rng_block.toggle_is_affected_by_luck_button_label.on");
	private static final Text TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.triggered_rng_block.toggle_is_affected_by_luck_button_label.off");
	private static final Text RANDOM_MIN_VALUE_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.random_min_value_label");
	private static final Text RANDOM_MAX_VALUE_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.random_max_value_label");
	private static final Text FALLBACK_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_rng_block.fallback_triggered_block_pos_offset");

	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.newTriggeredBlockPositionOffset");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetX.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetY.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetZ.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_THRESHOLD_LABEL_TEXT = Text.translatable("gui.triggered_block.triggeredBlockThreshold.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_THRESHOLD_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockThreshold.placeholder");
	private static final Text ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TriggeredRNGBlockEntity triggeredRNGBlock;

	private CyclingButtonWidget<ScreenPage> screenPageButton;
	private ScreenPage screenPage;

	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private TextFieldWidget overrideDataIdentifierField;
	private TextFieldWidget overrideDataValueField;
	private TextFieldWidget overrideTriggeredBlockPosOffsetXField;
	private TextFieldWidget overrideTriggeredBlockPosOffsetYField;
	private TextFieldWidget overrideTriggeredBlockPosOffsetZField;
	private CyclingButtonWidget<Boolean> toggleOverrideTriggeredBlockResetsButton;
	private boolean overrideTriggeredBlockResets;

	private TextFieldWidget influencingAttributeIdentifierField;
	private CyclingButtonWidget<Boolean> toggleChecksTeamAttributesButton;
	private boolean checksTeamAttributes;
	private CyclingButtonWidget<Boolean> toggleIsAffectedByLuckButton;
	private boolean isAffectedByLuck;
	private TextFieldWidget randomMinValueField;
	private TextFieldWidget randomMaxValueField;
	private TextFieldWidget fallbackTriggeredBlockPosOffsetXField;
	private TextFieldWidget fallbackTriggeredBlockPosOffsetYField;
	private TextFieldWidget fallbackTriggeredBlockPosOffsetZField;
	private CyclingButtonWidget<Boolean> toggleFallbackTriggeredBlockResetsButton;
	private boolean fallbackTriggeredBlockResets;

	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private ButtonWidget removeListEntryButton3;
	private TextFieldWidget newTriggeredBlockPositionOffsetXField;
	private TextFieldWidget newTriggeredBlockPositionOffsetYField;
	private TextFieldWidget newTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleNewTriggeredBlockResetsButton;
	private boolean newTriggeredBlockResets;
	private TextFieldWidget newTriggeredBlockThresholdField;
	private ButtonWidget addNewTriggeredBlockPositionOffsetButton;

	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());

	private static final int VISIBLE_LIST_ELEMENTS = 4;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
		super(NarratorManager.EMPTY);
		this.triggeredRNGBlock = triggeredRNGBlockEntity;
	}

	private void addNewTriggeredBlock() {
		MutablePair<MutablePair<BlockPos, Boolean>, Integer> newTriggeredBlock = new MutablePair<>(
				new MutablePair<>(
						new BlockPos(
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetZField.getText())
						),
						this.newTriggeredBlockResets),
				ItemUtils.parseInt(this.newTriggeredBlockThresholdField.getText())
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
		if (this.updateTriggeredRNGBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		super.init();

		this.screenPage = ScreenPage.MISC;
		this.screenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));
		
		// --- data driven override ---

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 55, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 55, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.overrideDataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 300, 20, Text.empty());
		this.overrideDataIdentifierField.setMaxLength(128);
		this.overrideDataIdentifierField.setText(this.triggeredRNGBlock.getOverrideDataIdentifier());
		this.addSelectableChild(this.overrideDataIdentifierField);

		this.overrideDataValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 300, 20, Text.empty());
		this.overrideDataValueField.setMaxLength(128);
		this.overrideDataValueField.setText(this.triggeredRNGBlock.getOverrideDataValue());
		this.addSelectableChild(this.overrideDataValueField);

		this.overrideTriggeredBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 160, 50, 20, Text.empty());
		this.overrideTriggeredBlockPosOffsetXField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetXField.setText(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.overrideTriggeredBlockPosOffsetXField);
		this.overrideTriggeredBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 160, 50, 20, Text.empty());
		this.overrideTriggeredBlockPosOffsetYField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetYField.setText(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.overrideTriggeredBlockPosOffsetYField);
		this.overrideTriggeredBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 160, 50, 20, Text.empty());
		this.overrideTriggeredBlockPosOffsetZField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetZField.setText(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.overrideTriggeredBlockPosOffsetZField);
		this.overrideTriggeredBlockResets = this.triggeredRNGBlock.getOverrideTriggeredBlock().getRight();
		this.toggleOverrideTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.overrideTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 160, 150, 20, Text.empty(), (button, overrideTriggeredBlockResets) -> {
			this.overrideTriggeredBlockResets = overrideTriggeredBlockResets;
		}));

		// --- misc ---
		
		this.influencingAttributeIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 300, 20, Text.empty());
		this.influencingAttributeIdentifierField.setMaxLength(128);
		this.influencingAttributeIdentifierField.setText(this.triggeredRNGBlock.getInfluencingAttributeIdentifierString());
		this.addSelectableChild(this.influencingAttributeIdentifierField);

		this.checksTeamAttributes = this.triggeredRNGBlock.checksTeamAttributes();
		this.toggleChecksTeamAttributesButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_ON, TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_OFF).initially(this.checksTeamAttributes).omitKeyText().build(this.width / 2 - 154, 79, 150, 20, Text.empty(), (button, checksTeamAttributes) -> {
			this.checksTeamAttributes = checksTeamAttributes;
		}));
		this.isAffectedByLuck = this.triggeredRNGBlock.isAffectedByLuck();
		this.toggleIsAffectedByLuckButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_ON, TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_OFF).initially(this.isAffectedByLuck).omitKeyText().build(this.width / 2 + 8, 79, 150, 20, Text.empty(), (button, isAffectedByLuck) -> {
			this.isAffectedByLuck = isAffectedByLuck;
		}));

		this.randomMinValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 114, 150, 20, Text.empty());
		this.randomMinValueField.setMaxLength(128);
		this.randomMinValueField.setText(Integer.toString(this.triggeredRNGBlock.getRandomMinValue()));
		this.addSelectableChild(this.randomMinValueField);
		this.randomMaxValueField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 114, 150, 20, Text.empty());
		this.randomMaxValueField.setMaxLength(128);
		this.randomMaxValueField.setText(Integer.toString(this.triggeredRNGBlock.getRandomMaxValue()));
		this.addSelectableChild(this.randomMaxValueField);

		this.fallbackTriggeredBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 149, 50, 20, Text.empty());
		this.fallbackTriggeredBlockPosOffsetXField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetXField.setText(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.fallbackTriggeredBlockPosOffsetXField);
		this.fallbackTriggeredBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 149, 50, 20, Text.empty());
		this.fallbackTriggeredBlockPosOffsetYField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetYField.setText(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.fallbackTriggeredBlockPosOffsetYField);
		this.fallbackTriggeredBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 149, 50, 20, Text.empty());
		this.fallbackTriggeredBlockPosOffsetZField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetZField.setText(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.fallbackTriggeredBlockPosOffsetZField);
		this.fallbackTriggeredBlockResets = this.triggeredRNGBlock.getFallbackTriggeredBlock().getRight();
		this.toggleFallbackTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.fallbackTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 149, 150, 20, Text.empty(), (button, fallbackTriggeredBlockResets) -> {
			this.fallbackTriggeredBlockResets = fallbackTriggeredBlockResets;
		}));

		// --- triggered blocks ---


		this.triggeredBlocks.addAll(this.triggeredRNGBlock.getTriggeredBlocks());

		this.removeListEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(0)));
		this.removeListEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(1)));
		this.removeListEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(2)));
		this.removeListEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(3)));

		this.newTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 151, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetXField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetXField);
		this.newTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 151, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetYField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetYField);
		this.newTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 151, 50, 20, Text.empty());
		this.newTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetZField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockPositionOffsetZField);

		this.newTriggeredBlockResets = false;
		this.toggleNewTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newTriggeredBlockResets).omitKeyText().build(this.width / 2 + 4, 151, 150, 20, Text.empty(), (button, newTriggeredBlockResets) -> {
			this.newTriggeredBlockResets = newTriggeredBlockResets;
		}));

		this.newTriggeredBlockThresholdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 186, 150, 20, Text.empty());
		this.newTriggeredBlockThresholdField.setMaxLength(128);
		this.newTriggeredBlockThresholdField.setPlaceholder(NEW_TRIGGERED_BLOCK_THRESHOLD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTriggeredBlockThresholdField);

		this.addNewTriggeredBlockPositionOffsetButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT, button -> this.addNewTriggeredBlock()).dimensions(this.width / 2 + 4, 186, 150, 20).build());

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.screenPageButton);
	}

	private void updateWidgets() {
		
		this.dataProvidingBlockPositionOffsetXField.setVisible(false);
		this.dataProvidingBlockPositionOffsetYField.setVisible(false);
		this.dataProvidingBlockPositionOffsetZField.setVisible(false);
		this.overrideDataIdentifierField.setVisible(false);
		this.overrideDataValueField.setVisible(false);
		this.overrideTriggeredBlockPosOffsetXField.setVisible(false);
		this.overrideTriggeredBlockPosOffsetYField.setVisible(false);
		this.overrideTriggeredBlockPosOffsetZField.setVisible(false);
		this.toggleOverrideTriggeredBlockResetsButton.visible = false;

		this.influencingAttributeIdentifierField.setVisible(false);
		this.toggleChecksTeamAttributesButton.visible = false;
		this.toggleIsAffectedByLuckButton.visible = false;
		this.randomMinValueField.setVisible(false);
		this.randomMaxValueField.setVisible(false);
		this.fallbackTriggeredBlockPosOffsetXField.setVisible(false);
		this.fallbackTriggeredBlockPosOffsetYField.setVisible(false);
		this.fallbackTriggeredBlockPosOffsetZField.setVisible(false);
		this.toggleFallbackTriggeredBlockResetsButton.visible = false;

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.removeListEntryButton3.visible = false;
		this.newTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleNewTriggeredBlockResetsButton.visible = false;
		this.newTriggeredBlockThresholdField.setVisible(false);
		this.addNewTriggeredBlockPositionOffsetButton.visible = false;

		if (this.screenPage == ScreenPage.DATA_DRIVEN_OVERRIDE) {

			this.dataProvidingBlockPositionOffsetXField.setVisible(true);
			this.dataProvidingBlockPositionOffsetYField.setVisible(true);
			this.dataProvidingBlockPositionOffsetZField.setVisible(true);
			this.overrideDataIdentifierField.setVisible(true);
			this.overrideDataValueField.setVisible(true);
			this.overrideTriggeredBlockPosOffsetXField.setVisible(true);
			this.overrideTriggeredBlockPosOffsetYField.setVisible(true);
			this.overrideTriggeredBlockPosOffsetZField.setVisible(true);
			this.toggleOverrideTriggeredBlockResetsButton.visible = true;

		} else if (this.screenPage == ScreenPage.MISC) {

			this.influencingAttributeIdentifierField.setVisible(true);
			this.toggleChecksTeamAttributesButton.visible = true;
			this.toggleIsAffectedByLuckButton.visible = true;
			this.randomMinValueField.setVisible(true);
			this.randomMaxValueField.setVisible(true);
			this.fallbackTriggeredBlockPosOffsetXField.setVisible(true);
			this.fallbackTriggeredBlockPosOffsetYField.setVisible(true);
			this.fallbackTriggeredBlockPosOffsetZField.setVisible(true);
			this.toggleFallbackTriggeredBlockResetsButton.visible = true;

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				if (index == 0) {
					this.removeListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeListEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeListEntryButton3.visible = true;
				}
				index++;
			}

			this.newTriggeredBlockPositionOffsetXField.setVisible(true);
			this.newTriggeredBlockPositionOffsetYField.setVisible(true);
			this.newTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleNewTriggeredBlockResetsButton.visible = true;
			this.newTriggeredBlockThresholdField.setVisible(true);
			this.addNewTriggeredBlockPositionOffsetButton.visible = true;

		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.overrideTriggeredBlockResets;
		boolean bool1 = this.checksTeamAttributes;
		boolean bool2 = this.isAffectedByLuck;
		boolean bool3 = this.fallbackTriggeredBlockResets;
		boolean bool4 = this.newTriggeredBlockResets;
		String string = this.dataProvidingBlockPositionOffsetXField.getText();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string3 = this.overrideDataIdentifierField.getText();
		String string4 = this.overrideDataValueField.getText();
		String string5 = this.overrideTriggeredBlockPosOffsetXField.getText();
		String string6 = this.overrideTriggeredBlockPosOffsetYField.getText();
		String string7 = this.overrideTriggeredBlockPosOffsetZField.getText();
		String string8 = this.influencingAttributeIdentifierField.getText();
		String string9 = this.randomMinValueField.getText();
		String string10 = this.randomMaxValueField.getText();
		String string11 = this.fallbackTriggeredBlockPosOffsetXField.getText();
		String string12 = this.fallbackTriggeredBlockPosOffsetYField.getText();
		String string13 = this.fallbackTriggeredBlockPosOffsetZField.getText();
		String string14 = this.newTriggeredBlockPositionOffsetXField.getText();
		String string15 = this.newTriggeredBlockPositionOffsetYField.getText();
		String string16 = this.newTriggeredBlockPositionOffsetZField.getText();
		String string17 = this.newTriggeredBlockThresholdField.getText();
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> list = new ArrayList<>(this.triggeredBlocks);
		this.init(client, width, height);
		this.screenPage = var;
		this.overrideTriggeredBlockResets = bool;
		this.checksTeamAttributes = bool1;
		this.isAffectedByLuck = bool2;
		this.fallbackTriggeredBlockResets = bool3;
		this.newTriggeredBlockResets = bool4;
		this.dataProvidingBlockPositionOffsetXField.setText(string);
		this.dataProvidingBlockPositionOffsetYField.setText(string1);
		this.dataProvidingBlockPositionOffsetZField.setText(string2);
		this.overrideDataIdentifierField.setText(string3);
		this.overrideDataValueField.setText(string4);
		this.overrideTriggeredBlockPosOffsetXField.setText(string5);
		this.overrideTriggeredBlockPosOffsetYField.setText(string6);
		this.overrideTriggeredBlockPosOffsetZField.setText(string7);
		this.influencingAttributeIdentifierField.setText(string8);
		this.randomMinValueField.setText(string9);
		this.randomMaxValueField.setText(string10);
		this.fallbackTriggeredBlockPosOffsetXField.setText(string11);
		this.fallbackTriggeredBlockPosOffsetYField.setText(string12);
		this.fallbackTriggeredBlockPosOffsetZField.setText(string13);
		this.newTriggeredBlockPositionOffsetXField.setText(string14);
		this.newTriggeredBlockPositionOffsetYField.setText(string15);
		this.newTriggeredBlockPositionOffsetZField.setText(string16);
		this.newTriggeredBlockThresholdField.setText(string17);
		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(list);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS
				&& this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 44;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 92)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS
				&& this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS
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
		if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS
				&& this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 136) {
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

	private boolean updateTriggeredRNGBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredRNGBlockPacket(
				this.triggeredRNGBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				this.overrideDataIdentifierField.getText(),
				this.overrideDataValueField.getText(),
				new BlockPos(
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetZField.getText())
				),
				this.overrideTriggeredBlockResets,
				this.influencingAttributeIdentifierField.getText(),
				this.checksTeamAttributes,
				this.isAffectedByLuck,
				ItemUtils.parseInt(this.randomMinValueField.getText()),
				ItemUtils.parseInt(this.randomMaxValueField.getText()),
				new BlockPos(
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetXField.getText()),
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetYField.getText()),
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetZField.getText())
				),
				this.fallbackTriggeredBlockResets,
				this.triggeredBlocks
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.DATA_DRIVEN_OVERRIDE) {

			context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, OVERRIDE_DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.overrideDataIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, OVERRIDE_DATA_VALUE_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.overrideDataValueField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, OVERRIDE_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			this.overrideTriggeredBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.overrideTriggeredBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.overrideTriggeredBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.MISC){

			context.drawTextWithShadow(this.textRenderer, INFLUENCING_ATTRIBUTE_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.influencingAttributeIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, RANDOM_MIN_VALUE_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.randomMinValueField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, RANDOM_MAX_VALUE_LABEL_TEXT, this.width / 2 + 5, 104, 0xA0A0A0);
			this.randomMaxValueField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, FALLBACK_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.fallbackTriggeredBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.fallbackTriggeredBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.fallbackTriggeredBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS){

			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock = this.triggeredBlocks.get(i);
				BlockPos triggeredBlockPos = triggeredBlock.left.left;
				MutableText text = Text.translatable("gui.triggered_block.list.entry", triggeredBlockPos.getX(), triggeredBlockPos.getY(), triggeredBlockPos.getZ()).append(triggeredBlock.left.right ? Text.translatable("gui.triggered_block.list.is_reset") : Text.translatable("gui.triggered_block.list.is_triggered")).append(Text.translatable("gui.triggered_block.list.threshold", triggeredBlock.right));
				context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (79.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			context.drawTextWithShadow(this.textRenderer, NEW_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
			this.newTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, NEW_TRIGGERED_BLOCK_THRESHOLD_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
			this.newTriggeredBlockThresholdField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public enum ScreenPage implements StringIdentifiable {
		DATA_DRIVEN_OVERRIDE("data_driven_override"),
		MISC("misc"),
		TRIGGERED_BLOCKS("triggered_blocks");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public Text asText() {
			return Text.translatable("gui.triggered_rng_block.screenPage." + this.name);
		}
	}
}
