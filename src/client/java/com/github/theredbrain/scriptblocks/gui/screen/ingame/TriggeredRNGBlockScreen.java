package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredRNGBlockPacket;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class TriggeredRNGBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Component OVERRIDE_DATA_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.override_data_identifier_label");
	private static final Component OVERRIDE_DATA_VALUE_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.override_data_value_label");
	private static final Component OVERRIDE_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.override_triggered_block_pos_offset");

	private static final Component INFLUENCING_ATTRIBUTE_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.influencing_attribute_label");
	private static final Component TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.triggered_rng_block.toggle_checks_team_attributes_button_label.on");
	private static final Component TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.triggered_rng_block.toggle_checks_team_attributes_button_label.off");
	private static final Component TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.triggered_rng_block.toggle_is_affected_by_luck_button_label.on");
	private static final Component TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.triggered_rng_block.toggle_is_affected_by_luck_button_label.off");
	private static final Component RANDOM_MIN_VALUE_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.random_min_value_label");
	private static final Component RANDOM_MAX_VALUE_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.random_max_value_label");
	private static final Component FALLBACK_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_rng_block.fallback_triggered_block_pos_offset");

	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.new_triggered_block_position_offset");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_THRESHOLD_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_threshold.placeholder");
	private static final Component ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TriggeredRNGBlockEntity triggeredRNGBlock;

	private CycleButton<ScreenPage> cycleScreenPageButton;
	private ScreenPage screenPage;

	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;
	private EditBox overrideDataIdentifierField;
	private EditBox overrideDataValueField;
	private EditBox overrideTriggeredBlockPosOffsetXField;
	private EditBox overrideTriggeredBlockPosOffsetYField;
	private EditBox overrideTriggeredBlockPosOffsetZField;
	private CycleButton<Boolean> toggleOverrideTriggeredBlockResetsButton;
	private boolean overrideTriggeredBlockResets;

	private EditBox influencingAttributeIdentifierField;
	private CycleButton<Boolean> toggleChecksTeamAttributesButton;
	private boolean checksTeamAttributes;
	private CycleButton<Boolean> toggleIsAffectedByLuckButton;
	private boolean isAffectedByLuck;
	private EditBox randomMinValueField;
	private EditBox randomMaxValueField;
	private EditBox fallbackTriggeredBlockPosOffsetXField;
	private EditBox fallbackTriggeredBlockPosOffsetYField;
	private EditBox fallbackTriggeredBlockPosOffsetZField;
	private CycleButton<Boolean> toggleFallbackTriggeredBlockResetsButton;
	private boolean fallbackTriggeredBlockResets;

	private Button removeListEntryButton0;
	private Button removeListEntryButton1;
	private Button removeListEntryButton2;
	private Button removeListEntryButton3;
	private EditBox newTriggeredBlockPositionOffsetXField;
	private EditBox newTriggeredBlockPositionOffsetYField;
	private EditBox newTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleNewTriggeredBlockResetsButton;
	private boolean newTriggeredBlockResets;
	private EditBox newTriggeredBlockThresholdField;
	private Button addNewTriggeredBlockPositionOffsetButton;

	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());

	private static final int VISIBLE_LIST_ELEMENTS = 4;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public TriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.triggeredRNGBlock = triggeredRNGBlockEntity;
	}

	private void addNewTriggeredBlock() {
		MutablePair<MutablePair<BlockPos, Boolean>, Integer> newTriggeredBlock = new MutablePair<>(
				new MutablePair<>(
						new BlockPos(
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.newTriggeredBlockPositionOffsetZField.getValue())
						),
						this.newTriggeredBlockResets),
				ItemUtils.parseInt(this.newTriggeredBlockThresholdField.getValue())
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

		this.screenPage = ScreenPage.MISC;
		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 308, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- data driven override ---

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 55, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 55, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 55, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.triggeredRNGBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.overrideDataIdentifierField = new EditBox(this.font, this.width / 2 - 154, 90, 300, 20, Component.empty());
		this.overrideDataIdentifierField.setMaxLength(128);
		this.overrideDataIdentifierField.setValue(this.triggeredRNGBlock.getOverrideDataIdentifier());
		this.addWidget(this.overrideDataIdentifierField);

		this.overrideDataValueField = new EditBox(this.font, this.width / 2 - 154, 125, 300, 20, Component.empty());
		this.overrideDataValueField.setMaxLength(128);
		this.overrideDataValueField.setValue(this.triggeredRNGBlock.getOverrideDataValue());
		this.addWidget(this.overrideDataValueField);

		this.overrideTriggeredBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 160, 50, 20, Component.empty());
		this.overrideTriggeredBlockPosOffsetXField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetXField.setValue(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getX()));
		this.addWidget(this.overrideTriggeredBlockPosOffsetXField);
		this.overrideTriggeredBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 100, 160, 50, 20, Component.empty());
		this.overrideTriggeredBlockPosOffsetYField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetYField.setValue(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getY()));
		this.addWidget(this.overrideTriggeredBlockPosOffsetYField);
		this.overrideTriggeredBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 46, 160, 50, 20, Component.empty());
		this.overrideTriggeredBlockPosOffsetZField.setMaxLength(128);
		this.overrideTriggeredBlockPosOffsetZField.setValue(Integer.toString(this.triggeredRNGBlock.getOverrideTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.overrideTriggeredBlockPosOffsetZField);
		this.overrideTriggeredBlockResets = this.triggeredRNGBlock.getOverrideTriggeredBlock().getRight();
		this.toggleOverrideTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.overrideTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 160, 150, 20, Component.empty(), (button, overrideTriggeredBlockResets) -> {
			this.overrideTriggeredBlockResets = overrideTriggeredBlockResets;
		}));

		// --- misc ---

		this.influencingAttributeIdentifierField = new EditBox(this.font, this.width / 2 - 154, 55, 300, 20, Component.empty());
		this.influencingAttributeIdentifierField.setMaxLength(128);
		this.influencingAttributeIdentifierField.setValue(this.triggeredRNGBlock.getInfluencingAttributeIdentifierString());
		this.addWidget(this.influencingAttributeIdentifierField);

		this.checksTeamAttributes = this.triggeredRNGBlock.checksTeamAttributes();
		this.toggleChecksTeamAttributesButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_ON, TOGGLE_CHECKS_TEAM_ATTRIBUTES_BUTTON_LABEL_TEXT_OFF).initially(this.checksTeamAttributes).omitKeyText().build(this.width / 2 - 154, 79, 150, 20, Component.empty(), (button, checksTeamAttributes) -> {
			this.checksTeamAttributes = checksTeamAttributes;
		}));
		this.isAffectedByLuck = this.triggeredRNGBlock.isAffectedByLuck();
		this.toggleIsAffectedByLuckButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_ON, TOGGLE_IS_AFFECTED_BY_LUCK_BUTTON_LABEL_TEXT_OFF).initially(this.isAffectedByLuck).omitKeyText().build(this.width / 2 + 8, 79, 150, 20, Component.empty(), (button, isAffectedByLuck) -> {
			this.isAffectedByLuck = isAffectedByLuck;
		}));

		this.randomMinValueField = new EditBox(this.font, this.width / 2 - 154, 114, 150, 20, Component.empty());
		this.randomMinValueField.setMaxLength(128);
		this.randomMinValueField.setValue(Integer.toString(this.triggeredRNGBlock.getRandomMinValue()));
		this.addWidget(this.randomMinValueField);
		this.randomMaxValueField = new EditBox(this.font, this.width / 2 + 4, 114, 150, 20, Component.empty());
		this.randomMaxValueField.setMaxLength(128);
		this.randomMaxValueField.setValue(Integer.toString(this.triggeredRNGBlock.getRandomMaxValue()));
		this.addWidget(this.randomMaxValueField);

		this.fallbackTriggeredBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 149, 50, 20, Component.empty());
		this.fallbackTriggeredBlockPosOffsetXField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetXField.setValue(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getX()));
		this.addWidget(this.fallbackTriggeredBlockPosOffsetXField);
		this.fallbackTriggeredBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 100, 149, 50, 20, Component.empty());
		this.fallbackTriggeredBlockPosOffsetYField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetYField.setValue(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getY()));
		this.addWidget(this.fallbackTriggeredBlockPosOffsetYField);
		this.fallbackTriggeredBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 46, 149, 50, 20, Component.empty());
		this.fallbackTriggeredBlockPosOffsetZField.setMaxLength(128);
		this.fallbackTriggeredBlockPosOffsetZField.setValue(Integer.toString(this.triggeredRNGBlock.getFallbackTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.fallbackTriggeredBlockPosOffsetZField);
		this.fallbackTriggeredBlockResets = this.triggeredRNGBlock.getFallbackTriggeredBlock().getRight();
		this.toggleFallbackTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.fallbackTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 149, 150, 20, Component.empty(), (button, fallbackTriggeredBlockResets) -> {
			this.fallbackTriggeredBlockResets = fallbackTriggeredBlockResets;
		}));

		// --- triggered blocks ---


		this.triggeredBlocks.addAll(this.triggeredRNGBlock.getTriggeredBlocks());

		this.removeListEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(0)));
		this.removeListEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(1)));
		this.removeListEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(2)));
		this.removeListEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeTriggeredBlock(3)));

		this.newTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 151, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetXField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetXField);
		this.newTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 104, 151, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetYField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetYField);
		this.newTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 54, 151, 50, 20, Component.empty());
		this.newTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.newTriggeredBlockPositionOffsetZField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addWidget(this.newTriggeredBlockPositionOffsetZField);

		this.newTriggeredBlockResets = false;
		this.toggleNewTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newTriggeredBlockResets).omitKeyText().build(this.width / 2 + 4, 151, 150, 20, Component.empty(), (button, newTriggeredBlockResets) -> {
			this.newTriggeredBlockResets = newTriggeredBlockResets;
		}));

		this.newTriggeredBlockThresholdField = new EditBox(this.font, this.width / 2 - 154, 186, 150, 20, Component.empty());
		this.newTriggeredBlockThresholdField.setMaxLength(128);
		this.addWidget(this.newTriggeredBlockThresholdField);

		this.addNewTriggeredBlockPositionOffsetButton = this.addRenderableWidget(Button.builder(ADD_NEW_TRIGGERED_BLOCK_BUTTON_LABEL_TEXT, button -> this.addNewTriggeredBlock()).bounds(this.width / 2 + 4, 186, 150, 20).build());

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
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		boolean bool = this.overrideTriggeredBlockResets;
		boolean bool1 = this.checksTeamAttributes;
		boolean bool2 = this.isAffectedByLuck;
		boolean bool3 = this.fallbackTriggeredBlockResets;
		boolean bool4 = this.newTriggeredBlockResets;
		String string = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getValue();
		String string3 = this.overrideDataIdentifierField.getValue();
		String string4 = this.overrideDataValueField.getValue();
		String string5 = this.overrideTriggeredBlockPosOffsetXField.getValue();
		String string6 = this.overrideTriggeredBlockPosOffsetYField.getValue();
		String string7 = this.overrideTriggeredBlockPosOffsetZField.getValue();
		String string8 = this.influencingAttributeIdentifierField.getValue();
		String string9 = this.randomMinValueField.getValue();
		String string10 = this.randomMaxValueField.getValue();
		String string11 = this.fallbackTriggeredBlockPosOffsetXField.getValue();
		String string12 = this.fallbackTriggeredBlockPosOffsetYField.getValue();
		String string13 = this.fallbackTriggeredBlockPosOffsetZField.getValue();
		String string14 = this.newTriggeredBlockPositionOffsetXField.getValue();
		String string15 = this.newTriggeredBlockPositionOffsetYField.getValue();
		String string16 = this.newTriggeredBlockPositionOffsetZField.getValue();
		String string17 = this.newTriggeredBlockThresholdField.getValue();
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> list = new ArrayList<>(this.triggeredBlocks);
		this.init(client, width, height);
		this.screenPage = var;
		this.cycleScreenPageButton.setValue(this.screenPage);
		this.overrideTriggeredBlockResets = bool;
		this.toggleOverrideTriggeredBlockResetsButton.setValue(this.overrideTriggeredBlockResets);
		this.checksTeamAttributes = bool1;
		this.toggleChecksTeamAttributesButton.setValue(this.checksTeamAttributes);
		this.isAffectedByLuck = bool2;
		this.toggleIsAffectedByLuckButton.setValue(this.isAffectedByLuck);
		this.fallbackTriggeredBlockResets = bool3;
		this.toggleFallbackTriggeredBlockResetsButton.setValue(this.fallbackTriggeredBlockResets);
		this.newTriggeredBlockResets = bool4;
		this.toggleNewTriggeredBlockResetsButton.setValue(this.newTriggeredBlockResets);
		this.dataProvidingBlockPositionOffsetXField.setValue(string);
		this.dataProvidingBlockPositionOffsetYField.setValue(string1);
		this.dataProvidingBlockPositionOffsetZField.setValue(string2);
		this.overrideDataIdentifierField.setValue(string3);
		this.overrideDataValueField.setValue(string4);
		this.overrideTriggeredBlockPosOffsetXField.setValue(string5);
		this.overrideTriggeredBlockPosOffsetYField.setValue(string6);
		this.overrideTriggeredBlockPosOffsetZField.setValue(string7);
		this.influencingAttributeIdentifierField.setValue(string8);
		this.randomMinValueField.setValue(string9);
		this.randomMaxValueField.setValue(string10);
		this.fallbackTriggeredBlockPosOffsetXField.setValue(string11);
		this.fallbackTriggeredBlockPosOffsetYField.setValue(string12);
		this.fallbackTriggeredBlockPosOffsetZField.setValue(string13);
		this.newTriggeredBlockPositionOffsetXField.setValue(string14);
		this.newTriggeredBlockPositionOffsetYField.setValue(string15);
		this.newTriggeredBlockPositionOffsetZField.setValue(string16);
		this.newTriggeredBlockThresholdField.setValue(string17);
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
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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

		if (this.screenPage == ScreenPage.DATA_DRIVEN_OVERRIDE) {

			context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, OVERRIDE_DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.overrideDataIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, OVERRIDE_DATA_VALUE_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.overrideDataValueField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, OVERRIDE_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			this.overrideTriggeredBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.overrideTriggeredBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.overrideTriggeredBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.MISC) {

			context.drawString(this.font, INFLUENCING_ATTRIBUTE_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.influencingAttributeIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, RANDOM_MIN_VALUE_LABEL_TEXT, this.width / 2 - 153, 104, 0xA0A0A0);
			this.randomMinValueField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, RANDOM_MAX_VALUE_LABEL_TEXT, this.width / 2 + 5, 104, 0xA0A0A0);
			this.randomMaxValueField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, FALLBACK_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 139, 0xA0A0A0);
			this.fallbackTriggeredBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.fallbackTriggeredBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.fallbackTriggeredBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCKS) {

			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.triggeredBlocks.size()); i++) {
				MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock = this.triggeredBlocks.get(i);
				BlockPos triggeredBlockPos = triggeredBlock.left.left;
				MutableComponent text = Component.translatable("gui.triggered_block.list.entry", triggeredBlockPos.getX(), triggeredBlockPos.getY(), triggeredBlockPos.getZ()).append(triggeredBlock.left.right ? Component.translatable("gui.triggered_block.list.is_reset") : Component.translatable("gui.triggered_block.list.is_triggered")).append(Component.translatable("gui.triggered_block.list.threshold", triggeredBlock.right));
				context.drawString(this.font, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.triggeredBlocks.size() > VISIBLE_LIST_ELEMENTS) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (79.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			context.drawString(this.font, NEW_TRIGGERED_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
			this.newTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, NEW_TRIGGERED_BLOCK_THRESHOLD_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
			this.newTriggeredBlockThresholdField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredRNGBlockPacket(
				this.triggeredRNGBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				),
				this.overrideDataIdentifierField.getValue(),
				this.overrideDataValueField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.overrideTriggeredBlockPosOffsetZField.getValue())
				),
				this.overrideTriggeredBlockResets,
				this.influencingAttributeIdentifierField.getValue(),
				this.checksTeamAttributes,
				this.isAffectedByLuck,
				ItemUtils.parseInt(this.randomMinValueField.getValue()),
				ItemUtils.parseInt(this.randomMaxValueField.getValue()),
				new BlockPos(
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.fallbackTriggeredBlockPosOffsetZField.getValue())
				),
				this.fallbackTriggeredBlockResets,
				this.triggeredBlocks
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		DATA_DRIVEN_OVERRIDE("data_driven_override"),
		MISC("misc"),
		TRIGGERED_BLOCKS("triggered_blocks");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public Component asText() {
			return Component.translatable("gui.triggered_rng_block.screen_page." + this.name);
		}
	}
}
