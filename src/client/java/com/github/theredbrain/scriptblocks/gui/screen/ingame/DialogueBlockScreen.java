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
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
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
	private static final Component ADD_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Component ENTRY_ALREADY_IN_LIST_TEXT = Component.translatable("gui.dialogue_block.entry_already_in_list");
	private static final Component EMPTY_IDENTIFIER_TEXT = Component.translatable("gui.dialogue_block.empty_identifier");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final Component IDENTIFIER_PLACEHOLDER_TEXT = Component.translatable("gui.dialogue_block.identifier_placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private final DialogueBlockEntity dialogueBlockEntity;

	private CycleButton<ScreenPage> cycleScreenPageButton;

	private Button removeDialogueUsedBlockEntryButton0;
	private Button removeDialogueUsedBlockEntryButton1;
	private Button removeDialogueUsedBlockEntryButton2;
	private Button removeDialogueUsedBlockEntryButton3;
	private EditBox newDialogueUsedBlockIdentifierField;
	private EditBox newDialogueUsedBlockPositionOffsetXField;
	private EditBox newDialogueUsedBlockPositionOffsetYField;
	private EditBox newDialogueUsedBlockPositionOffsetZField;
	private Button addDialogueUsedBlockButton;

	private Button removeDialogueTriggeredBlockEntryButton0;
	private Button removeDialogueTriggeredBlockEntryButton1;
	private Button removeDialogueTriggeredBlockEntryButton2;
	private Button removeDialogueTriggeredBlockEntryButton3;
	private EditBox newDialogueTriggeredBlockIdentifierField;
	private EditBox newDialogueTriggeredBlockPositionOffsetXField;
	private EditBox newDialogueTriggeredBlockPositionOffsetYField;
	private EditBox newDialogueTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleNewDialogueTriggeredBlockResetsButton;
	private boolean newDialogueTriggeredBlockResets;
	private Button addDialogueTriggeredBlockButton;

	private Button removeStartingDialogueEntryButton0;
	private Button removeStartingDialogueEntryButton1;
	private Button removeStartingDialogueEntryButton2;
	private Button removeStartingDialogueEntryButton3;
	private Button removeStartingDialogueEntryButton4;
	private EditBox newStartingDialogueIdentifierField;
	private Button addStartingDialogueButton;

	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;

	private ScreenPage screenPage;
	private final List<MutablePair<String, BlockPos>> dialogueUsedBlocksList = new ArrayList<>(List.of());
	private final List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList = new ArrayList<>(List.of());
	private final List<String> startingDialogueList = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
		super(GameNarrator.NO_TITLE);
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
		String newDialogueUsedBlockIdentifier = this.newDialogueUsedBlockIdentifierField.getValue();
		if (newDialogueUsedBlockIdentifier.isEmpty()) {
			if (this.minecraft != null && this.minecraft.player != null) {
				this.minecraft.player.displayClientMessage(EMPTY_IDENTIFIER_TEXT);
			}
			return;
		}
		for (MutablePair<String, BlockPos> entry : this.dialogueUsedBlocksList) {
			if (entry.getLeft().equals(newDialogueUsedBlockIdentifier)) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.dialogueUsedBlocksList.add(
				new MutablePair<>(newDialogueUsedBlockIdentifier,
						new BlockPos(
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.newDialogueUsedBlockPositionOffsetZField.getValue())
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
		String newDialogueTriggeredBlockIdentifier = this.newDialogueTriggeredBlockIdentifierField.getValue();
		if (newDialogueTriggeredBlockIdentifier.isEmpty()) {
			if (this.minecraft != null && this.minecraft.player != null) {
				this.minecraft.player.displayClientMessage(EMPTY_IDENTIFIER_TEXT);
			}
			return;
		}
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : this.dialogueTriggeredBlocksList) {
			if (entry.getLeft().equals(newDialogueTriggeredBlockIdentifier)) {
				if (this.minecraft != null && this.minecraft.player != null) {
					this.minecraft.player.displayClientMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.dialogueTriggeredBlocksList.add(
				new MutablePair<>(newDialogueTriggeredBlockIdentifier,
						new MutablePair<>(
								new BlockPos(
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetXField.getValue()),
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetYField.getValue()),
										ItemUtils.parseInt(this.newDialogueTriggeredBlockPositionOffsetZField.getValue())
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
		String newStartingDialogueIdentifier = this.newStartingDialogueIdentifierField.getValue();

		Dialogue newStartingDialogue = null;
		Level world = this.dialogueBlockEntity.getWorld();
		if (world != null) {
			Optional<Holder.Reference<Dialogue>> optionalDialogueReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(newStartingDialogueIdentifier));
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
		if (this.minecraft != null && this.minecraft.player != null && !message.isEmpty()) {
			this.minecraft.player.displayClientMessage(Component.translatable(message));
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

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- dialogue used blocks page ---

		this.removeDialogueUsedBlockEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(0)));
		this.removeDialogueUsedBlockEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(1)));
		this.removeDialogueUsedBlockEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(2)));
		this.removeDialogueUsedBlockEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueUsedBlockEntry(3)));

		this.addDialogueUsedBlockButton = this.addRenderableWidget(Button.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueUsedBlockEntry()).bounds(this.width / 2 - 4 - 150, 140, 300, 20).build());

		this.newDialogueUsedBlockIdentifierField = new EditBox(this.font, this.width / 2 - 154, 164, 300, 20, Component.empty());
		this.newDialogueUsedBlockIdentifierField.setMaxLength(128);
		this.newDialogueUsedBlockIdentifierField.setHint(IDENTIFIER_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueUsedBlockIdentifierField);

		this.newDialogueUsedBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 188, 100, 20, Component.empty());
		this.newDialogueUsedBlockPositionOffsetXField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetXField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueUsedBlockPositionOffsetXField);

		this.newDialogueUsedBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 188, 100, 20, Component.empty());
		this.newDialogueUsedBlockPositionOffsetYField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetYField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueUsedBlockPositionOffsetYField);

		this.newDialogueUsedBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 188, 100, 20, Component.empty());
		this.newDialogueUsedBlockPositionOffsetZField.setMaxLength(128);
		this.newDialogueUsedBlockPositionOffsetZField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueUsedBlockPositionOffsetZField);

		// --- dialogue used blocks page ---

		this.removeDialogueTriggeredBlockEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(0)));
		this.removeDialogueTriggeredBlockEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(1)));
		this.removeDialogueTriggeredBlockEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(2)));
		this.removeDialogueTriggeredBlockEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDialogueTriggeredBlockEntry(3)));

		this.addDialogueTriggeredBlockButton = this.addRenderableWidget(Button.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueTriggeredBlockEntry()).bounds(this.width / 2 - 4 - 150, 140, 300, 20).build());

		this.newDialogueTriggeredBlockIdentifierField = new EditBox(this.font, this.width / 2 - 154, 164, 300, 20, Component.empty());
		this.newDialogueTriggeredBlockIdentifierField.setMaxLength(128);
		this.newDialogueTriggeredBlockIdentifierField.setHint(IDENTIFIER_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueTriggeredBlockIdentifierField);

		this.newDialogueTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 188, 50, 20, Component.empty());
		this.newDialogueTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetXField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueTriggeredBlockPositionOffsetXField);

		this.newDialogueTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 188, 50, 20, Component.empty());
		this.newDialogueTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetYField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueTriggeredBlockPositionOffsetYField);

		this.newDialogueTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 188, 50, 20, Component.empty());
		this.newDialogueTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.newDialogueTriggeredBlockPositionOffsetZField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addWidget(this.newDialogueTriggeredBlockPositionOffsetZField);

		this.newDialogueTriggeredBlockResets = false;
		this.toggleNewDialogueTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newDialogueTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 188, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.newDialogueTriggeredBlockResets = triggeredBlockResets;
		}));

		// --- starting dialogues page ---

		this.removeStartingDialogueEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(0)));
		this.removeStartingDialogueEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(1)));
		this.removeStartingDialogueEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(2)));
		this.removeStartingDialogueEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(3)));
		this.removeStartingDialogueEntryButton4 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 140, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStartingDialogueEntry(4)));

		this.addStartingDialogueButton = this.addRenderableWidget(Button.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addStartingDialogueEntry()).bounds(this.width / 2 - 4 - 150, 164, 300, 20).build());

		this.newStartingDialogueIdentifierField = new EditBox(this.font, this.width / 2 - 154, 188, 300, 20, Component.empty());
		this.newStartingDialogueIdentifierField.setMaxLength(128);
		this.addWidget(this.newStartingDialogueIdentifierField);

		// --- data providing block page ---

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 116, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);

		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 116, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);

		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 116, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.dialogueBlockEntity.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 212, 150, 20).build());

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
	public void resize(Minecraft client, int width, int height) {
		List<MutablePair<String, BlockPos>> list = new ArrayList<>(this.dialogueUsedBlocksList);
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> list1 = new ArrayList<>(this.dialogueTriggeredBlocksList);
		List<String> list2 = new ArrayList<>(this.startingDialogueList);
		int number4 = this.scrollPosition;
		float number5 = this.scrollAmount;
		ScreenPage var = this.screenPage;
		String string = this.newDialogueUsedBlockIdentifierField.getValue();
		String string1 = this.newDialogueUsedBlockPositionOffsetXField.getValue();
		String string2 = this.newDialogueUsedBlockPositionOffsetYField.getValue();
		String string3 = this.newDialogueUsedBlockPositionOffsetZField.getValue();
		String string4 = this.newDialogueTriggeredBlockIdentifierField.getValue();
		String string5 = this.newDialogueTriggeredBlockPositionOffsetXField.getValue();
		String string6 = this.newDialogueTriggeredBlockPositionOffsetYField.getValue();
		String string7 = this.newDialogueTriggeredBlockPositionOffsetZField.getValue();
		String string8 = this.newStartingDialogueIdentifierField.getValue();
		String string9 = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string10 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string11 = this.dataProvidingBlockPositionOffsetZField.getValue();
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
		this.newDialogueUsedBlockIdentifierField.setValue(string);
		this.newDialogueUsedBlockPositionOffsetXField.setValue(string1);
		this.newDialogueUsedBlockPositionOffsetYField.setValue(string2);
		this.newDialogueUsedBlockPositionOffsetZField.setValue(string3);
		this.newDialogueTriggeredBlockIdentifierField.setValue(string4);
		this.newDialogueTriggeredBlockPositionOffsetXField.setValue(string5);
		this.newDialogueTriggeredBlockPositionOffsetYField.setValue(string6);
		this.newDialogueTriggeredBlockPositionOffsetZField.setValue(string7);
		this.newStartingDialogueIdentifierField.setValue(string8);
		this.dataProvidingBlockPositionOffsetXField.setValue(string9);
		this.dataProvidingBlockPositionOffsetYField.setValue(string10);
		this.dataProvidingBlockPositionOffsetZField.setValue(string11);
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
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dialogueTriggeredBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.startingDialogueList.size() - VISIBLE_LARGE_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 153) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 116) {
			int i = this.dialogueTriggeredBlocksList.size() - VISIBLE_SMALL_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 153) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 140) {
			int i = this.startingDialogueList.size() - VISIBLE_LARGE_LIST_ENTRIES;
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

		if (this.screenPage == ScreenPage.DIALOGUE_USED_BLOCKS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_SMALL_LIST_ENTRIES, this.dialogueUsedBlocksList.size()); i++) {
				BlockPos usedBlockPos = this.dialogueUsedBlocksList.get(i).getRight();
				MutableComponent text = Component.translatable("gui.dialogue_block.used_blocks_list.entry", this.dialogueUsedBlocksList.get(i).getLeft(), usedBlockPos.getX(), usedBlockPos.getY(), usedBlockPos.getZ());
				context.drawString(this.font, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueUsedBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newDialogueUsedBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.DIALOGUE_TRIGGERED_BLOCKS) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_SMALL_LIST_ENTRIES, this.dialogueTriggeredBlocksList.size()); i++) {
				BlockPos usedBlockPos = this.dialogueTriggeredBlocksList.get(i).getRight().getLeft();
				MutableComponent text = Component.translatable("gui.dialogue_block.triggered_blocks_list.entry", this.dialogueTriggeredBlocksList.get(i).getLeft(), this.dialogueTriggeredBlocksList.get(i).getRight().getRight() ? Component.translatable("gui.dialogue_block.triggered_blocks_list.resets") : Component.translatable("gui.dialogue_block.triggered_blocks_list.triggers"), usedBlockPos.getX(), usedBlockPos.getY(), usedBlockPos.getZ());
				context.drawString(this.font, text, this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueTriggeredBlocksList.size() > VISIBLE_SMALL_LIST_ENTRIES) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 44, 8, 92);
				int k = (int) (83.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newDialogueTriggeredBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.STARTING_DIALOGUES) {
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LARGE_LIST_ENTRIES, this.startingDialogueList.size()); i++) {
				context.drawString(this.font, Component.translatable(this.startingDialogueList.get(i)), this.width / 2 - 117, 50 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.startingDialogueList.size() > VISIBLE_LARGE_LIST_ENTRIES) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 44, 8, 116);
				int k = (int) (107.0f * this.scrollAmount);
				context.blitSprite(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			this.newStartingDialogueIdentifierField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.DATA_BLOCK_OFFSET) {

			context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 106, 0xA0A0A0);
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
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				)
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		DATA_BLOCK_OFFSET("data_block_offset"),
		DIALOGUE_USED_BLOCKS("dialogue_used_blocks"),
		DIALOGUE_TRIGGERED_BLOCKS("dialogue_triggered_blocks"),
		STARTING_DIALOGUES("starting_dialogues");

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
			return Component.translatable("gui.dialogue_screen.screen_page." + this.name);
		}
	}
}
