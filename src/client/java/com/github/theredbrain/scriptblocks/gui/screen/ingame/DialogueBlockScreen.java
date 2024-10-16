package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDialogueBlockPacket;
import com.github.theredbrain.scriptblocks.registry.DialoguesRegistry;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DialogueBlockScreen extends Screen {
	private static final Text ADD_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Text REMOVE_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.remove");
	private static final Text ENTRY_ALREADY_IN_LIST_TEXT = Text.translatable("gui.dialogue_block.entry_already_in_list");
	public static final Identifier BACKGROUND_218_197_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_197_background.png");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_35_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_35");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_87_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_87");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_96_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_96");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	private DialogueBlockEntity dialogueBlockEntity;

	private CyclingButtonWidget<CreativeScreenPage> creativeScreenPageButton;

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

	private ButtonWidget removeStartingDialogueEntryButton;
	private TextFieldWidget newStartingDialogueIdentifierField;
	private ButtonWidget addStartingDialogueButton;

	private ButtonWidget saveCreativeButton;
	private ButtonWidget cancelCreativeButton;
	private CreativeScreenPage creativeScreenPage;
	private List<MutablePair<String, BlockPos>> dialogueUsedBlocksList = new ArrayList<>(List.of());
	private List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList = new ArrayList<>(List.of());
	private List<String> startingDialogueList = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
		super(NarratorManager.EMPTY);
		this.dialogueBlockEntity = dialogueBlockEntity;
		this.creativeScreenPage = CreativeScreenPage.STARTING_DIALOGUES;
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

	private void removeStartingDialogueEntry() {
		if (this.scrollPosition < this.startingDialogueList.size()) {
			this.startingDialogueList.remove(this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addStartingDialogueEntry() {
		String message = "";
		String newStartingDialogueIdentifier = this.newStartingDialogueIdentifierField.getText();
		Dialogue newStartingDialogue = DialoguesRegistry.registeredDialogues.get(Identifier.tryParse(newStartingDialogueIdentifier));
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

	private void saveCreative() {
		if (this.updateDialogueBlockCreative()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.dialogueUsedBlocksList.clear();
		this.dialogueTriggeredBlocksList.clear();
		this.startingDialogueList.clear();
		List<String> keyList = new ArrayList<>(this.dialogueBlockEntity.getDialogueUsedBlocksMap().keySet());
		for (String key : keyList) {
			this.dialogueUsedBlocksList.add(new MutablePair<>(key, this.dialogueBlockEntity.getDialogueUsedBlocksMap().get(key)));
		}
		keyList = new ArrayList<>(this.dialogueBlockEntity.getDialogueTriggeredBlocksMap().keySet());
		for (String key : keyList) {
			this.dialogueTriggeredBlocksList.add(new MutablePair<>(key, this.dialogueBlockEntity.getDialogueTriggeredBlocksMap().get(key)));
		}
		this.startingDialogueList.addAll(this.dialogueBlockEntity.getStartingDialogueList());

		super.init();

		this.creativeScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(CreativeScreenPage::asText).values((CreativeScreenPage[]) CreativeScreenPage.values()).initially(this.creativeScreenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, creativeScreenPage) -> {
			this.creativeScreenPage = creativeScreenPage;
			this.updateWidgets();
		}));

		// --- dialogue used blocks page ---

		this.removeDialogueUsedBlockEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueUsedBlockEntry(0)).dimensions(this.width / 2 + 54, 42, 100, 20).build());
		this.removeDialogueUsedBlockEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueUsedBlockEntry(1)).dimensions(this.width / 2 + 54, 66, 100, 20).build());
		this.removeDialogueUsedBlockEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueUsedBlockEntry(2)).dimensions(this.width / 2 + 54, 90, 100, 20).build());
		this.removeDialogueUsedBlockEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueUsedBlockEntry(3)).dimensions(this.width / 2 + 54, 114, 100, 20).build());

		this.newDialogueUsedBlockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 138, 300, 20, Text.empty());
		this.newDialogueUsedBlockIdentifierField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueUsedBlockIdentifierField);

		this.newDialogueUsedBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 162, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetXField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetXField);

		this.newDialogueUsedBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 162, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetYField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetYField);

		this.newDialogueUsedBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 162, 100, 20, Text.empty());
		this.newDialogueUsedBlockPositionOffsetZField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueUsedBlockPositionOffsetZField);

		this.addDialogueUsedBlockButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueUsedBlockEntry()).dimensions(this.width / 2 - 4 - 150, 186, 300, 20).build());

		// --- dialogue used blocks page ---

		this.removeDialogueTriggeredBlockEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(0)).dimensions(this.width / 2 + 54, 42, 100, 20).build());
		this.removeDialogueTriggeredBlockEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(1)).dimensions(this.width / 2 + 54, 66, 100, 20).build());
		this.removeDialogueTriggeredBlockEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(2)).dimensions(this.width / 2 + 54, 90, 100, 20).build());
		this.removeDialogueTriggeredBlockEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(3)).dimensions(this.width / 2 + 54, 114, 100, 20).build());

		this.newDialogueTriggeredBlockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 138, 300, 20, Text.empty());
		this.newDialogueTriggeredBlockIdentifierField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueTriggeredBlockIdentifierField);

		this.newDialogueTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 162, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetXField);

		this.newDialogueTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 162, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetYField);

		this.newDialogueTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 162, 50, 20, Text.empty());
		this.newDialogueTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.addSelectableChild(this.newDialogueTriggeredBlockPositionOffsetZField);

		this.newDialogueTriggeredBlockResets = false;
		this.toggleNewDialogueTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newDialogueTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 162, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.newDialogueTriggeredBlockResets = triggeredBlockResets;
		}));

		this.addDialogueTriggeredBlockButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueTriggeredBlockEntry()).dimensions(this.width / 2 - 4 - 150, 186, 300, 20).build());

		// --- starting dialogues page ---

		this.removeStartingDialogueEntryButton = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeStartingDialogueEntry()).dimensions(this.width / 2 + 54, 78, 100, 20).build());
		// TODO add more visible entries

		this.newStartingDialogueIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 162, 300, 20, Text.empty());
		this.newStartingDialogueIdentifierField.setMaxLength(128);
		this.addSelectableChild(this.newStartingDialogueIdentifierField);

		this.addStartingDialogueButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addStartingDialogueEntry()).dimensions(this.width / 2 - 4 - 150, 186, 300, 20).build());

		this.saveCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.saveCreative()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelCreativeButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.creativeScreenPageButton.visible = false;

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

		this.removeStartingDialogueEntryButton.visible = false; // TODO add more visible entries

		this.newStartingDialogueIdentifierField.setVisible(false);

		this.addStartingDialogueButton.visible = false;

		this.saveCreativeButton.visible = false;
		this.cancelCreativeButton.visible = false;

		this.creativeScreenPageButton.visible = true;

		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_USED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(4, this.dialogueUsedBlocksList.size()); i++) {
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

		} else if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_TRIGGERED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(4, this.dialogueTriggeredBlocksList.size()); i++) {
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

		} else if (this.creativeScreenPage == CreativeScreenPage.STARTING_DIALOGUES) {

			if (!this.startingDialogueList.isEmpty()) {
				this.removeStartingDialogueEntryButton.visible = true; // TODO add more visible entries
			}

			this.newStartingDialogueIdentifierField.setVisible(true);

			this.addStartingDialogueButton.visible = true;

		}

		this.saveCreativeButton.visible = true;
		this.cancelCreativeButton.visible = true;

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
		CreativeScreenPage var = this.creativeScreenPage;
		String string = this.newDialogueUsedBlockIdentifierField.getText();
		String string1 = this.newDialogueUsedBlockPositionOffsetXField.getText();
		String string2 = this.newDialogueUsedBlockPositionOffsetYField.getText();
		String string3 = this.newDialogueUsedBlockPositionOffsetZField.getText();
		String string4 = this.newDialogueTriggeredBlockIdentifierField.getText();
		String string5 = this.newDialogueTriggeredBlockPositionOffsetXField.getText();
		String string6 = this.newDialogueTriggeredBlockPositionOffsetYField.getText();
		String string7 = this.newDialogueTriggeredBlockPositionOffsetZField.getText();
		String string8 = this.newStartingDialogueIdentifierField.getText();
		this.init(client, width, height);
		this.dialogueUsedBlocksList.clear();
		this.dialogueTriggeredBlocksList.clear();
		this.startingDialogueList.clear();
		this.dialogueUsedBlocksList.addAll(list);
		this.dialogueTriggeredBlocksList.addAll(list1);
		this.startingDialogueList.addAll(list2);
		this.scrollPosition = number4;
		this.scrollAmount = number5;
		this.creativeScreenPage = var;
		this.newDialogueUsedBlockIdentifierField.setText(string);
		this.newDialogueUsedBlockPositionOffsetXField.setText(string1);
		this.newDialogueUsedBlockPositionOffsetYField.setText(string2);
		this.newDialogueUsedBlockPositionOffsetZField.setText(string3);
		this.newDialogueTriggeredBlockIdentifierField.setText(string4);
		this.newDialogueTriggeredBlockPositionOffsetXField.setText(string5);
		this.newDialogueTriggeredBlockPositionOffsetYField.setText(string6);
		this.newDialogueTriggeredBlockPositionOffsetZField.setText(string7);
		this.newStartingDialogueIdentifierField.setText(string8);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > 4) {
			int i = this.width / 2 - 153;
			int j = 43;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 94)) {
				this.mouseClicked = true;
			}
		}
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > 4) {
			int i = this.width / 2 - 153;
			int j = 43;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 94)) {
				this.mouseClicked = true;
			}
		}
		if (this.creativeScreenPage == CreativeScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > 1) {
			int i = this.width / 2 - 153;
			int j = 72;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 33)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > 4
				&& this.mouseClicked) {
			int i = this.dialogueUsedBlocksList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > 4
				&& this.mouseClicked) {
			int i = this.dialogueTriggeredBlocksList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.creativeScreenPage == CreativeScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > 1
				&& this.mouseClicked) {
			int i = this.startingDialogueList.size() - 1;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_USED_BLOCKS
				&& this.dialogueUsedBlocksList.size() > 4
				&& mouseX >= (double) (this.width / 2 - 154) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= (double) (42) && mouseY <= (double) (138)) {
			int i = this.dialogueUsedBlocksList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_TRIGGERED_BLOCKS
				&& this.dialogueTriggeredBlocksList.size() > 4
				&& mouseX >= (double) (this.width / 2 - 154) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= (double) (42) && mouseY <= (double) (138)) {
			int i = this.dialogueTriggeredBlocksList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.creativeScreenPage == CreativeScreenPage.STARTING_DIALOGUES
				&& this.startingDialogueList.size() > 1
				&& mouseX >= (double) (this.width / 2 - 154) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= (double) (71) && mouseY <= (double) (106)) {
			int i = this.startingDialogueList.size() - 1;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.saveCreative();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_USED_BLOCKS) {
			int x = this.dialogueUsedBlocksList.size() > 4 ? this.width / 2 - 142 : this.width / 2 - 153;
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 4, this.dialogueUsedBlocksList.size()); i++) {
				context.drawTextWithShadow(this.textRenderer, this.dialogueUsedBlocksList.get(i).getLeft() + ": " + this.dialogueUsedBlocksList.get(i).getRight().toString(), x, 48 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueUsedBlocksList.size() > 4) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_96_TEXTURE, this.width / 2 - 154, 42, 8, 96);
				int k = (int) (85.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 42 + 1 + k, 6, 7);
			}
			this.newDialogueUsedBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueUsedBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.creativeScreenPage == CreativeScreenPage.DIALOGUE_TRIGGERED_BLOCKS) {
			int x = this.dialogueTriggeredBlocksList.size() > 4 ? this.width / 2 - 142 : this.width / 2 - 153;
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 4, this.dialogueTriggeredBlocksList.size()); i++) {
				context.drawTextWithShadow(this.textRenderer, this.dialogueTriggeredBlocksList.get(i).getLeft() + ": " + this.dialogueTriggeredBlocksList.get(i).getRight().toString(), x, 48 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.dialogueTriggeredBlocksList.size() > 4) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_96_TEXTURE, this.width / 2 - 154, 42, 8, 96);
				int k = (int) (85.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 42 + 1 + k, 6, 7);
			}
			this.newDialogueTriggeredBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newDialogueTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.creativeScreenPage == CreativeScreenPage.STARTING_DIALOGUES) {
			int x = this.startingDialogueList.size() > 1 ? this.width / 2 - 142 : this.width / 2 - 153;
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 1, this.startingDialogueList.size()); i++) {
				context.drawTextWithShadow(this.textRenderer, Text.translatable(this.startingDialogueList.get(i)), x, 71, 0xA0A0A0);
			}
			if (this.startingDialogueList.size() > 1) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_35_TEXTURE, this.width / 2 - 154, 71, 8, 35);
				int k = (int) (26.0f * this.scrollAmount);
				// TODO add label
				context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 71 + 1 + k, 6, 7);
			}
			this.newStartingDialogueIdentifierField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	private boolean updateDialogueBlockCreative() {
		ClientPlayNetworking.send(new UpdateDialogueBlockPacket(
				this.dialogueBlockEntity.getPos(),
				this.dialogueUsedBlocksList,
				this.dialogueTriggeredBlocksList,
				this.startingDialogueList
		));
		return true;
	}

	public static enum CreativeScreenPage implements StringIdentifiable {
		DIALOGUE_USED_BLOCKS("dialogue_used_blocks"),
		DIALOGUE_TRIGGERED_BLOCKS("dialogue_triggered_blocks"),
		STARTING_DIALOGUES("starting_dialogues");

		private final String name;

		private CreativeScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<CreativeScreenPage> byName(String name) {
			return Arrays.stream(CreativeScreenPage.values()).filter(creativeScreenPage -> creativeScreenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.dialogue_screen.creativeScreenPage." + this.name);
		}
	}
}
