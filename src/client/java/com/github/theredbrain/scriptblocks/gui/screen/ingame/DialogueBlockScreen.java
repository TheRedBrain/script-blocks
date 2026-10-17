package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDialogueBlockPacket;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DialogueBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final int VISIBLE_SMALL_LIST_ENTRIES = 4;
	private static final int VISIBLE_LARGE_LIST_ENTRIES = 5;
	private static final Text ADD_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Text ENTRY_ALREADY_IN_LIST_TEXT = Text.translatable("gui.dialogue_block.entry_already_in_list");
	private static final Text EMPTY_IDENTIFIER_TEXT = Text.translatable("gui.dialogue_block.empty_identifier");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final Text IDENTIFIER_PLACEHOLDER_TEXT = Text.translatable("gui.dialogue_block.identifier_placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private final DialogueBlockEntity dialogueBlockEntity;

	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;

	private ButtonWidget removeDialogueUsedBlockEntryButton0;
	private ButtonWidget removeDialogueUsedBlockEntryButton1;
	private ButtonWidget removeDialogueUsedBlockEntryButton2;
	private ButtonWidget removeDialogueUsedBlockEntryButton3;
	private TextFieldWidget newDialogueUsedBlockIdentifierField;
	private TextFieldWidget newDialogueUsedBlockPositionOffsetXField;
	private TextFieldWidget newDialogueUsedBlockPositionOffsetYField;
	private TextFieldWidget newDialogueUsedBlockPositionOffsetZField;
	private ButtonWidget addDialogueUsedBlockButton;

	private ButtonWidget removeDialogueTriggeredBlockEntryButton0;
	private ButtonWidget removeDialogueTriggeredBlockEntryButton1;
	private ButtonWidget removeDialogueTriggeredBlockEntryButton2;
	private ButtonWidget removeDialogueTriggeredBlockEntryButton3;
	private TextFieldWidget newDialogueTriggeredBlockIdentifierField;
	private TextFieldWidget newDialogueTriggeredBlockPositionOffsetXField;
	private TextFieldWidget newDialogueTriggeredBlockPositionOffsetYField;
	private TextFieldWidget newDialogueTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleNewDialogueTriggeredBlockResetsButton;
	private boolean newDialogueTriggeredBlockResets;
	private ButtonWidget addDialogueTriggeredBlockButton;

	private ButtonWidget removeStartingDialogueEntryButton0;
	private ButtonWidget removeStartingDialogueEntryButton1;
	private ButtonWidget removeStartingDialogueEntryButton2;
	private ButtonWidget removeStartingDialogueEntryButton3;
	private ButtonWidget removeStartingDialogueEntryButton4;
	private TextFieldWidget newStartingDialogueIdentifierField;
	private ButtonWidget addStartingDialogueButton;

	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;

	private ScreenPage screenPage;
	private final List<MutablePair<String, BlockPos>> dialogueUsedBlocksList = new ArrayList<>(List.of());
	private final List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList = new ArrayList<>(List.of());
	private final List<String> startingDialogueList = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
		super(NarratorManager.EMPTY);
		this.dialogueBlockEntity = dialogueBlockEntity;
		this.screenPage = ScreenPage.STARTING_DIALOGUES;
	}

	private void removeDialogueUsedBlockEntry(int index) {
		if (index + this.scrollPosition < this.dialogueUsedBlocksList.size()) {
			this.dialogueUsedBlocksList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addDialogueUsedBlockEntry() {
		String newDialogueUsedBlockIdentifier = this.newDialogueUsedBlockIdentifierField.getText();
		if (newDialogueUsedBlockIdentifier.isEmpty()) {
			if (this.client != null && this.client.player != null) {
				this.client.player.sendMessage(EMPTY_IDENTIFIER_TEXT);
			}
			return;
		}
		for (MutablePair<String, BlockPos> entry : this.dialogueUsedBlocksList) {
			if (entry.getLeft().equals(newDialogueUsedBlockIdentifier)) {
				if (this.client != null && this.client.player != null) {
					this.client.player.sendMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.dialogueUsedBlocksList.add(
				new MutablePair<>(newDialogueUsedBlockIdentifier,
						new BlockPos(
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetZField.getText())
						)
				)
		);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void removeDialogueTriggeredBlockEntry(int index) {
		if (index + this.scrollPosition < this.dialogueTriggeredBlocksList.size()) {
			this.dialogueTriggeredBlocksList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addDialogueTriggeredBlockEntry() {
		String newDialogueTriggeredBlockIdentifier = this.newDialogueTriggeredBlockIdentifierField.getText();
		if (newDialogueTriggeredBlockIdentifier.isEmpty()) {
			if (this.client != null && this.client.player != null) {
				this.client.player.sendMessage(EMPTY_IDENTIFIER_TEXT);
			}
			return;
		}
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : this.dialogueTriggeredBlocksList) {
			if (entry.getLeft().equals(newDialogueTriggeredBlockIdentifier)) {
				if (this.client != null && this.client.player != null) {
					this.client.player.sendMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.dialogueTriggeredBlocksList.add(
				new MutablePair<>(newDialogueTriggeredBlockIdentifier,
						new MutablePair<>(
								new BlockPos(
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetXField.getText()),
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetYField.getText()),
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetZField.getText())
								),
								this.newDialogueTriggeredBlockResets
						)
				)
		);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void removeStartingDialogueEntry(int index) {
		if (index + this.scrollPosition < this.startingDialogueList.size()) {
			this.startingDialogueList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addStartingDialogueEntry() {
		String message = "";
		String newStartingDialogueIdentifier = this.newStartingDialogueIdentifierField.getText();

		Dialogue newStartingDialogue = null;
		World world = this.dialogueBlockEntity.getWorld();
		if (world != null) {
			Optional<RegistryEntry.Reference<Dialogue>> optionalDialogueReference = world.getRegistryManager().get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(newStartingDialogueIdentifier));
			if (optionalDialogueReference.isPresent()) {
				newStartingDialogue = optionalDialogueReference.get().value();
			}
		}

		if (newStartingDialogue == null) {
			message = "gui.dialogue_block.invalid_dialogue_identifier";
		}
		if (message.isEmpty()) {
			for (String entry : this.startingDialogueList) {
				if (entry.equals(newStartingDialogueIdentifier)) {
					message = "gui.dialogue_block.entry_already_in_list";
					break;
				}
			}
		}
		if (this.client != null && this.client.player != null && !message.isEmpty()) {
			this.client.player.sendMessage(Text.translatable(message));
			return;
		}
		this.startingDialogueList.add(newStartingDialogueIdentifier);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.dialogueUsedBlocksList.clear();
		this.dialogueTriggeredBlocksList.clear();
		this.startingDialogueList.clear();
		List<String> keyList = new ArrayList<>(this.dialogueBlockEntity.getUseRelayBlocks().keySet());
		for (String key : keyList) {
			this.dialogueUsedBlocksList.add(new MutablePair<>(key, this.dialogueBlockEntity.getUseRelayBlocks().get(key)));
		}
		keyList = new ArrayList<>(this.dialogueBlockEntity.getTriggeredBlocks().keySet());
		for (String key : keyList) {
			this.dialogueTriggeredBlocksList.add(new MutablePair<>(key, this.dialogueBlockEntity.getTriggeredBlocks().get(key)));
		}
		this.startingDialogueList.addAll(this.dialogueBlockEntity.getStartingDialogueList());

		super.init();

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- dialogue used blocks page ---

		this.removeDialogueUsedBlockEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(0)));
		this.removeDialogueUsedBlockEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(1)));
		this.removeDialogueUsedBlockEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(2)));
		this.removeDialogueUsedBlockEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(3)));

		this.addDialogueUsedBlockButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueUsedBlockEntry()).dimensions(this.width / 2 - 4 - 150, 140, 300, 20).build());

		this.newDialogueUsedBlockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 300, 20, Text.empty());
		this.newDialogueUsedBlockIdentifierField.setMaxLength(128);
		this.newDialogueUsedBlockIdentifierField.setPlaceholder(IDENTIFIER_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueUsedBlockIdentifierField);

		this.newDialogueUsedBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 188, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetXField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetXField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetXField);

		this.newDialogueUsedBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 188, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetYField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetYField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetYField);

		this.newDialogueUsedBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 188, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetZField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetZField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetZField);

		// --- dialogue used blocks page ---

		this.removeDialogueTriggeredBlockEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(0)));
		this.removeDialogueTriggeredBlockEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(1)));
		this.removeDialogueTriggeredBlockEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(2)));
		this.removeDialogueTriggeredBlockEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(3)));

		this.addDialogueTriggeredBlockButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueTriggeredBlockEntry()).dimensions(this.width / 2 - 4 - 150, 140, 300, 20).build());

		this.newDialogueTriggeredBlockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 300, 20, Text.empty());
		this.newDialogueTriggeredBlockIdentifierField.setMaxLength(128);
		this.newDialogueTriggeredBlockIdentifierField.setPlaceholder(IDENTIFIER_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueTriggeredBlockIdentifierField);

		this.newDialogueTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 188, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetXField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetXField);

		this.newDialogueTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 188, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetYField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetYField);

		this.newDialogueTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 188, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetZField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetZField);

		this.newDialogueTriggeredBlockResets = false;
		this.toggleNewDialogueTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newDialogueTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 188, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.newDialogueTriggeredBlockResets = triggeredBlockResets;
		}));

		// --- starting dialogues page ---

		this.removeStartingDialogueEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(0)));
		this.removeStartingDialogueEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(1)));
		this.removeStartingDialogueEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(2)));
		this.removeStartingDialogueEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(3)));
		this.removeStartingDialogueEntryButton4 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 140, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(4)));

		this.addStartingDialogueButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addStartingDialogueEntry()).dimensions(this.width / 2 - 4 - 150, 164, 300, 20).build());

		this.newStartingDialogueIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 188, 300, 20, Text.empty());
		this.newStartingDialogueIdentifierField.setMaxLength(128);
		this.addSelectableChild(this.newStartingDialogueIdentifierField);

		// --- data providing block page ---

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 116, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);

		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 116, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);

		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 116, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.removeDialogueUsedBlockEntryButton0.visible = false;
		this.removeDialogueUsedBlockEntryButton1.visible = false;
		this.removeDialogueUsedBlockEntryButton2.visible = false;
		this.removeDialogueUsedBlockEntryButton3.visible = false;

		this.newDialogueUsedBlockIdentifierField.setVisible(false);
		this.newDialogueUsedBlockPositionOffsetXField.setVisible(false);
		this.newDialogueUsedBlockPositionOffsetYField.setVisible(false);
		this.newDialogueUsedBlockPositionOffsetZField.setVisible(false);

		this.addDialogueUsedBlockButton.visible = false;

		this.removeDialogueTriggeredBlockEntryButton0.visible = false;
		this.removeDialogueTriggeredBlockEntryButton1.visible = false;
		this.removeDialogueTriggeredBlockEntryButton2.visible = false;
		this.removeDialogueTriggeredBlockEntryButton3.visible = false;

		this.newDialogueTriggeredBlockIdentifierField.setVisible(false);
		this.newDialogueTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newDialogueTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newDialogueTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleNewDialogueTriggeredBlockResetsButton.visible = false;

		this.addDialogueTriggeredBlockButton.visible = false;

		this.removeStartingDialogueEntryButton0.visible = false;
		this.removeStartingDialogueEntryButton1.visible = false;
		this.removeStartingDialogueEntryButton2.visible = false;
		this.removeStartingDialogueEntryButton3.visible = false;
		this.removeStartingDialogueEntryButton4.visible = false;

		this.newStartingDialogueIdentifierField.setVisible(false);

		this.addStartingDialogueButton.visible = false;

		this.dataProvidingBlockPositionOffsetXField.setVisible(false);
		this.dataProvidingBlockPositionOffsetYField.setVisible(false);
		this.dataProvidingBlockPositionOffsetZField.setVisible(false);

		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_SMALL_LIST_ENTRIES, this.dialogueUsedBlocksList.size()); i++) {
				if (index == 0) {
					this.removeDialogueUsedBlockEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeDialogueUsedBlockEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeDialogueUsedBlockEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeDialogueUsedBlockEntryButton3.visible = true;
				}
				index++;
			}

			this.newDialogueUsedBlockIdentifierField.setVisible(true);
			this.newDialogueUsedBlockPositionOffsetXField.setVisible(true);
			this.newDialogueUsedBlockPositionOffsetYField.setVisible(true);
			this.newDialogueUsedBlockPositionOffsetZField.setVisible(true);

			this.addDialogueUsedBlockButton.visible = true;

		} else if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_SMALL_LIST_ENTRIES, this.dialogueTriggeredBlocksList.size()); i++) {
				if (index == 0) {
					this.removeDialogueTriggeredBlockEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeDialogueTriggeredBlockEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeDialogueTriggeredBlockEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeDialogueTriggeredBlockEntryButton3.visible = true;
				}
				index++;
			}

			this.newDialogueTriggeredBlockIdentifierField.setVisible(true);
			this.newDialogueTriggeredBlockPositionOffsetXField.setVisible(true);
			this.newDialogueTriggeredBlockPositionOffsetYField.setVisible(true);
			this.newDialogueTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleNewDialogueTriggeredBlockResetsButton.visible = true;

			this.addDialogueTriggeredBlockButton.visible = true;

		} else if (this.screenPage == ScreenPage.STARTING_DIALOGUES) {

			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_LARGE_LIST_ENTRIES, this.startingDialogueList.size()); i++) {
				if (index == 0) {
					this.removeStartingDialogueEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeStartingDialogueEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeStartingDialogueEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeStartingDialogueEntryButton3.visible = true;
				} else if (index == 4) {
					this.removeStartingDialogueEntryButton4.visible = true;
				}
				index++;
			}

			this.newStartingDialogueIdentifierField.setVisible(true);

			this.addStartingDialogueButton.visible = true;

		} else if (this.screenPage == ScreenPage.DATA_BLOCK_OFFSET) {

			this.dataProvidingBlockPositionOffsetXField.setVisible(true);
			this.dataProvidingBlockPositionOffsetYField.setVisible(true);
			this.dataProvidingBlockPositionOffsetZField.setVisible(true);

		}

		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<String, BlockPos>> list = new ArrayList<>(this.dialogueUsedBlocksList);
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> list1 = new ArrayList<>(this.dialogueTriggeredBlocksList);
		List<String> list2 = new ArrayList<>(this.startingDialogueList);
		int number4 = this.scrollPosition;
		float number5 = this.scrollAmount;
		ScreenPage var = this.screenPage;
		String string = this.newDialogueUsedBlockIdentifierField.getText();
		String string1 = this.newDialogueUsedBlockPositionOffsetXField.getText();
		String string2 = this.newDialogueUsedBlockPositionOffsetYField.getText();
		String string3 = this.newDialogueUsedBlockPositionOffsetZField.getText();
		String string4 = this.newDialogueTriggeredBlockIdentifierField.getText();
		String string5 = this.newDialogueTriggeredBlockPositionOffsetXField.getText();
		String string6 = this.newDialogueTriggeredBlockPositionOffsetYField.getText();
		String string7 = this.newDialogueTriggeredBlockPositionOffsetZField.getText();
		String string8 = this.newStartingDialogueIdentifierField.getText();
		String string9 = this.dataProvidingBlockPositionOffsetXField.getText();
		String string10 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string11 = this.dataProvidingBlockPositionOffsetZField.getText();
		this.init(client, width, height);
		this.dialogueUsedBlocksList.clear();
		this.dialogueTriggeredBlocksList.clear();
		this.startingDialogueList.clear();
		this.dialogueUsedBlocksList.addAll(list);
		this.dialogueTriggeredBlocksList.addAll(list1);
		this.startingDialogueList.addAll(list2);
		this.scrollPosition = number4;
		this.scrollAmount = number5;
		this.screenPage = var;
		this.newDialogueUsedBlockIdentifierField.setText(string);
		this.newDialogueUsedBlockPositionOffsetXField.setText(string1);
		this.newDialogueUsedBlockPositionOffsetYField.setText(string2);
		this.newDialogueUsedBlockPositionOffsetZField.setText(string3);
		this.newDialogueTriggeredBlockIdentifierField.setText(string4);
		this.newDialogueTriggeredBlockPositionOffsetXField.setText(string5);
		this.newDialogueTriggeredBlockPositionOffsetYField.setText(string6);
		this.newDialogueTriggeredBlockPositionOffsetZField.setText(string7);
		this.newStartingDialogueIdentifierField.setText(string8);
		this.dataProvidingBlockPositionOffsetXField.setText(string9);
		this.dataProvidingBlockPositionOffsetYField.setText(string10);
		this.dataProvidingBlockPositionOffsetZField.setText(string11);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
			int i = this.width / 2 - 152;
			int j = 44;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 92)) {
				this.mouseClicked = true;
			}
		} else if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
			int i = this.width / 2 - 152;
			int j = 44;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 92)) {
				this.mouseClicked = true;
			}
		} else if (this.screenPage == ScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES) {
			int i = this.width / 2 - 152;
			int j = 44;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 116)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dialogueUsedBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dialogueTriggeredBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.startingDialogueList.size() - VISIBLE_LARGE_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 153) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 116) {
			int i = this.dialogueUsedBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 153) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 116) {
			int i = this.dialogueTriggeredBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 153) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 140) {
			int i = this.startingDialogueList.size() - VISIBLE_LARGE_LIST_ENTRIES;
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

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_SMALL_LIST_ENTRIES, this.dialogueUsedBlocksList.size()); i++) {
				BlockPos usedBlockPos = this.dialogueUsedBlocksList.get(i).getRight();
				MutableText text = Text.translatable("gui.dialogue_block.used_blocks_list.entry", this.dialogueUsedBlocksList.get(i).getLeft(), usedBlockPos.getX(), usedBlockPos.getY(), usedBlockPos.getZ());
				context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueUsedBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newDialogueUsedBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_SMALL_LIST_ENTRIES, this.dialogueTriggeredBlocksList.size()); i++) {
				BlockPos usedBlockPos = this.dialogueTriggeredBlocksList.get(i).getRight().getLeft();
				MutableText text = Text.translatable("gui.dialogue_block.triggered_blocks_list.entry", this.dialogueTriggeredBlocksList.get(i).getLeft(), this.dialogueTriggeredBlocksList.get(i).getRight().getRight() ? Text.translatable("gui.dialogue_block.triggered_blocks_list.resets") : Text.translatable("gui.dialogue_block.triggered_blocks_list.triggers"), usedBlockPos.getX(), usedBlockPos.getY(), usedBlockPos.getZ());
				context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newDialogueTriggeredBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.STARTING_DIALOGUES) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LARGE_LIST_ENTRIES, this.startingDialogueList.size()); i++) {
				context.drawTextWithShadow(this.textRenderer, Text.translatable(this.startingDialogueList.get(i)), this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 44, 8, 116);
				int k = (int) (107.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newStartingDialogueIdentifierField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.DATA_BLOCK_OFFSET) {

			context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 106, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDialogueBlockPacket(
				this.dialogueBlockEntity.getPos(),
				this.dialogueUsedBlocksList,
				this.dialogueTriggeredBlocksList,
				this.startingDialogueList,
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		DATA_BLOCK_OFFSET("data_block_offset"),
		DIALOGUE_USED_BLOCKS("dialogue_used_blocks"),
		DIALOGUE_TRIGGERED_BLOCKS("dialogue_triggered_blocks"),
		STARTING_DIALOGUES("starting_dialogues");

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
			return Text.translatable("gui.dialogue_screen.screen_page." + this.name);
		}
	}
}
