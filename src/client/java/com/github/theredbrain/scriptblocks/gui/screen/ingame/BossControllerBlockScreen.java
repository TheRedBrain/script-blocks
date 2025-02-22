package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacket;
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
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class BossControllerBlockScreen extends Screen {
	private static final Text HIDE_AREA_LABEL_TEXT = Text.translatable("gui.area_block.hide_area_label");
	private static final Text SHOW_AREA_LABEL_TEXT = Text.translatable("gui.area_block.show_area_label");
	private static final Text AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.area_block.area_dimensions_label");
	private static final Text AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.area_block.area_position_offset_label");
	private static final Text ADD_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Text REMOVE_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.remove");
	private static final Text ENTRY_ALREADY_IN_LIST_TEXT = Text.translatable("gui.dialogue_block.entry_already_in_list");
	private static final Text BOSS_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.boss_controller_block.boss_identifier_label");
	private static final Text ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_position_offset_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_pitch_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Text.translatable("gui.triggered_spawner_block.entity_spawn_orientation_yaw_label");

	private static final Identifier SCROLL_BAR_BACKGROUND_8_96_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_96");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");

	private final BossControllerBlockEntity bossControllerBlock;

	private CyclingButtonWidget<ScreenPage> screenPageButton;

	private CyclingButtonWidget<Boolean> toggleShowAreaButton;
	private TextFieldWidget areaDimensionsXField;
	private TextFieldWidget areaDimensionsYField;
	private TextFieldWidget areaDimensionsZField;
	private TextFieldWidget areaPositionOffsetXField;
	private TextFieldWidget areaPositionOffsetYField;
	private TextFieldWidget areaPositionOffsetZField;
	private boolean showArea;

	private TextFieldWidget bossIdentifierField;
	private TextFieldWidget entitySpawnPositionOffsetXField;
	private TextFieldWidget entitySpawnPositionOffsetYField;
	private TextFieldWidget entitySpawnPositionOffsetZField;
	private TextFieldWidget entitySpawnOrientationPitchField;
	private TextFieldWidget entitySpawnOrientationYawField;

	private ButtonWidget removeBossTriggeredBlockEntryButton0;
	private ButtonWidget removeBossTriggeredBlockEntryButton1;
	private ButtonWidget removeBossTriggeredBlockEntryButton2;
	private ButtonWidget removeBossTriggeredBlockEntryButton3;
	private TextFieldWidget newBossTriggeredBlockIdentifierField;
	private TextFieldWidget newBossTriggeredBlockPositionOffsetXField;
	private TextFieldWidget newBossTriggeredBlockPositionOffsetYField;
	private TextFieldWidget newBossTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleNewBossTriggeredBlockResetsButton;
	private boolean newBossTriggeredBlockResets;
	private ButtonWidget addBossTriggeredBlockButton;

	private List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = new ArrayList<>(List.of());

	private ButtonWidget doneButton;
	private ButtonWidget cancelButton;

	private ScreenPage screenPage;

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public BossControllerBlockScreen(BossControllerBlockEntity bossControllerBlock) {
		super(NarratorManager.EMPTY);
		this.bossControllerBlock = bossControllerBlock;
		this.screenPage = ScreenPage.SPAWN_POSITION;
	}

	private void removeDialogueTriggeredBlockEntry(int index) {
		if (index + this.scrollPosition < this.bossTriggeredBlocksList.size()) {
			this.bossTriggeredBlocksList.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void addDialogueTriggeredBlockEntry() {
		String newDialogueTriggeredBlockIdentifier = this.newBossTriggeredBlockIdentifierField.getText();
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : this.bossTriggeredBlocksList) {
			if (entry.getLeft().equals(newDialogueTriggeredBlockIdentifier)) {
				if (this.client != null && this.client.player != null) {
					this.client.player.sendMessage(ENTRY_ALREADY_IN_LIST_TEXT);
				}
				return;
			}
		}
		this.bossTriggeredBlocksList.add(
				new MutablePair<>(newDialogueTriggeredBlockIdentifier,
						new MutablePair<>(
								new BlockPos(
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetXField.getText()),
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetYField.getText()),
										ItemUtils.parseInt(this.newBossTriggeredBlockPositionOffsetZField.getText())
								),
								this.newBossTriggeredBlockResets
						)
				)
		);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void done() {
		this.updateBossControllerBlock();
		this.close();
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.bossTriggeredBlocksList.clear();
		List<String> keyList = new ArrayList<>(this.bossControllerBlock.getBossTriggeredBlocks().keySet());
		for (String key : keyList) {
			this.bossTriggeredBlocksList.add(new MutablePair<>(key, this.bossControllerBlock.getBossTriggeredBlocks().get(key)));
		}

		super.init();

		this.screenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- arena area page ---

		this.showArea = this.bossControllerBlock.showArea();
		this.toggleShowAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 54, 300, 20, Text.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 89, 100, 20, Text.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setText(Integer.toString(this.bossControllerBlock.getAreaDimensions().getX()));
		this.addSelectableChild(this.areaDimensionsXField);

		this.areaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 89, 100, 20, Text.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setText(Integer.toString(this.bossControllerBlock.getAreaDimensions().getY()));
		this.addSelectableChild(this.areaDimensionsYField);

		this.areaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 89, 100, 20, Text.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setText(Integer.toString(this.bossControllerBlock.getAreaDimensions().getZ()));
		this.addSelectableChild(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 124, 100, 20, Text.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setText(Integer.toString(this.bossControllerBlock.getAreaPositionOffset().getX()));
		this.addSelectableChild(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 124, 100, 20, Text.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setText(Integer.toString(this.bossControllerBlock.getAreaPositionOffset().getY()));
		this.addSelectableChild(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 124, 100, 20, Text.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setText(Integer.toString(this.bossControllerBlock.getAreaPositionOffset().getZ()));
		this.addSelectableChild(this.areaPositionOffsetZField);

		// --- spawning position page ---

		this.bossIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 300, 20, Text.empty());
		this.bossIdentifierField.setMaxLength(128);
		this.bossIdentifierField.setText(this.bossControllerBlock.getBossIdentifier().toString());
		this.addSelectableChild(this.bossIdentifierField);

		this.entitySpawnPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetXField.setText(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getX()));
		this.addSelectableChild(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 90, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetYField.setText(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getY()));
		this.addSelectableChild(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 90, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetZField.setText(Integer.toString(this.bossControllerBlock.getBossSpawnPositionOffset().getZ()));
		this.addSelectableChild(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 150, 20, Text.empty());
		this.entitySpawnOrientationPitchField.setText(Double.toString(this.bossControllerBlock.getBossSpawnOrientationPitch()));
		this.addSelectableChild(this.entitySpawnOrientationPitchField);
		this.entitySpawnOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 125, 150, 20, Text.empty());
		this.entitySpawnOrientationYawField.setText(Double.toString(this.bossControllerBlock.getBossSpawnOrientationYaw()));
		this.addSelectableChild(this.entitySpawnOrientationYawField);

		// --- boss triggered blocks page ---

		this.removeBossTriggeredBlockEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(0)).dimensions(this.width / 2 + 54, 42, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(1)).dimensions(this.width / 2 + 54, 66, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(2)).dimensions(this.width / 2 + 54, 90, 100, 20).build());
		this.removeBossTriggeredBlockEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeDialogueTriggeredBlockEntry(3)).dimensions(this.width / 2 + 54, 114, 100, 20).build());

		this.newBossTriggeredBlockIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 138, 300, 20, Text.empty());
		this.newBossTriggeredBlockIdentifierField.setMaxLength(128);
		this.addSelectableChild(this.newBossTriggeredBlockIdentifierField);

		this.newBossTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 162, 50, 20, Text.empty());
		this.newBossTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.addSelectableChild(this.newBossTriggeredBlockPositionOffsetXField);

		this.newBossTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 162, 50, 20, Text.empty());
		this.newBossTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.addSelectableChild(this.newBossTriggeredBlockPositionOffsetYField);

		this.newBossTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 162, 50, 20, Text.empty());
		this.newBossTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.addSelectableChild(this.newBossTriggeredBlockPositionOffsetZField);

		this.newBossTriggeredBlockResets = false;
		this.toggleNewBossTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.newBossTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 162, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.newBossTriggeredBlockResets = triggeredBlockResets;
		}));

		this.addBossTriggeredBlockButton = this.addDrawableChild(ButtonWidget.builder(ADD_ENTRY_BUTTON_LABEL_TEXT, button -> this.addDialogueTriggeredBlockEntry()).dimensions(this.width / 2 - 4 - 150, 186, 300, 20).build());

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());
		this.setInitialFocus(this.entitySpawnPositionOffsetXField);
		this.updateWidgets();
	}

	private void updateWidgets() {

		this.screenPageButton.visible = false;

		this.toggleShowAreaButton.visible = false;
		this.areaDimensionsXField.visible = false;
		this.areaDimensionsYField.visible = false;
		this.areaDimensionsZField.visible = false;
		this.areaPositionOffsetXField.visible = false;
		this.areaPositionOffsetYField.visible = false;
		this.areaPositionOffsetZField.visible = false;


		this.bossIdentifierField.setVisible(false);
		this.entitySpawnPositionOffsetXField.setVisible(false);
		this.entitySpawnPositionOffsetYField.setVisible(false);
		this.entitySpawnPositionOffsetZField.setVisible(false);
		this.entitySpawnOrientationPitchField.setVisible(false);
		this.entitySpawnOrientationYawField.setVisible(false);


		this.removeBossTriggeredBlockEntryButton0.visible = false;
		this.removeBossTriggeredBlockEntryButton1.visible = false;
		this.removeBossTriggeredBlockEntryButton2.visible = false;
		this.removeBossTriggeredBlockEntryButton3.visible = false;

		this.newBossTriggeredBlockIdentifierField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetXField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetYField.setVisible(false);
		this.newBossTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleNewBossTriggeredBlockResetsButton.visible = false;

		this.addBossTriggeredBlockButton.visible = false;

		this.doneButton.visible = false;
		this.cancelButton.visible = false;

		this.screenPageButton.visible = true;

		if (this.screenPage == ScreenPage.AREA) {

			this.toggleShowAreaButton.visible = true;
			this.areaDimensionsXField.visible = true;
			this.areaDimensionsYField.visible = true;
			this.areaDimensionsZField.visible = true;
			this.areaPositionOffsetXField.visible = true;
			this.areaPositionOffsetYField.visible = true;
			this.areaPositionOffsetZField.visible = true;

		} else if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS) {

			int index = 0;
			for (int i = 0; i < Math.min(4, this.bossTriggeredBlocksList.size()); i++) {
				if (index == 0) {
					this.removeBossTriggeredBlockEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeBossTriggeredBlockEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeBossTriggeredBlockEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeBossTriggeredBlockEntryButton3.visible = true;
				}
				index++;
			}

			this.newBossTriggeredBlockIdentifierField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetXField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetYField.setVisible(true);
			this.newBossTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleNewBossTriggeredBlockResetsButton.visible = true;

			this.addBossTriggeredBlockButton.visible = true;

		} else if (this.screenPage == ScreenPage.SPAWN_POSITION) {

			this.bossIdentifierField.setVisible(true);
			this.entitySpawnPositionOffsetXField.setVisible(true);
			this.entitySpawnPositionOffsetYField.setVisible(true);
			this.entitySpawnPositionOffsetZField.setVisible(true);
			this.entitySpawnOrientationPitchField.setVisible(true);
			this.entitySpawnOrientationYawField.setVisible(true);

		}

		this.doneButton.visible = true;
		this.cancelButton.visible = true;

		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> list1 = new ArrayList<>(this.bossTriggeredBlocksList);
		int number4 = this.scrollPosition;
		float number5 = this.scrollAmount;
		ScreenPage var = this.screenPage;

		String string = this.areaDimensionsXField.getText();
		String string1 = this.areaDimensionsYField.getText();
		String string2 = this.areaDimensionsZField.getText();
		String string3 = this.areaPositionOffsetXField.getText();
		String string4 = this.areaPositionOffsetYField.getText();
		String string5 = this.areaPositionOffsetZField.getText();

		String string6 = this.bossIdentifierField.getText();
		String string7 = this.entitySpawnPositionOffsetXField.getText();
		String string8 = this.entitySpawnPositionOffsetYField.getText();
		String string9 = this.entitySpawnPositionOffsetZField.getText();
		String string10 = this.entitySpawnOrientationPitchField.getText();
		String string11 = this.entitySpawnOrientationYawField.getText();

		String string12 = this.newBossTriggeredBlockIdentifierField.getText();
		String string13 = this.newBossTriggeredBlockPositionOffsetXField.getText();
		String string14 = this.newBossTriggeredBlockPositionOffsetYField.getText();
		String string15 = this.newBossTriggeredBlockPositionOffsetZField.getText();

		this.init(client, width, height);

		this.bossTriggeredBlocksList.clear();
		this.bossTriggeredBlocksList.addAll(list1);
		this.scrollPosition = number4;
		this.scrollAmount = number5;
		this.screenPage = var;

		this.areaDimensionsXField.setText(string);
		this.areaDimensionsYField.setText(string1);
		this.areaDimensionsZField.setText(string2);
		this.areaPositionOffsetXField.setText(string3);
		this.areaPositionOffsetYField.setText(string4);
		this.areaPositionOffsetZField.setText(string5);

		this.bossIdentifierField.setText(string6);
		this.entitySpawnPositionOffsetXField.setText(string7);
		this.entitySpawnPositionOffsetYField.setText(string8);
		this.entitySpawnPositionOffsetZField.setText(string9);
		this.entitySpawnOrientationPitchField.setText(string10);
		this.entitySpawnOrientationYawField.setText(string11);

		this.newBossTriggeredBlockIdentifierField.setText(string12);
		this.newBossTriggeredBlockPositionOffsetXField.setText(string13);
		this.newBossTriggeredBlockPositionOffsetYField.setText(string14);
		this.newBossTriggeredBlockPositionOffsetZField.setText(string15);

		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4) {
			int i = this.width / 2 - 153;
			int j = 43;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 94)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4
				&& this.mouseClicked) {
			int i = this.bossTriggeredBlocksList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS
				&& this.bossTriggeredBlocksList.size() > 4
				&& mouseX >= (double) (this.width / 2 - 154) && mouseX <= (double) (this.width / 2 + 50)
				&& mouseY >= (double) (42) && mouseY <= (double) (138)) {
			int i = this.bossTriggeredBlocksList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {
			context.drawTextWithShadow(this.textRenderer, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 114, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.SPAWN_POSITION) {
			context.drawTextWithShadow(this.textRenderer, BOSS_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.bossIdentifierField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 + 5, 115, 0xA0A0A0);
			this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.BOSS_TRIGGERED_BLOCKS) {
			int x = this.bossTriggeredBlocksList.size() > 4 ? this.width / 2 - 142 : this.width / 2 - 153;
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 4, this.bossTriggeredBlocksList.size()); i++) {
				context.drawTextWithShadow(this.textRenderer, this.bossTriggeredBlocksList.get(i).getLeft() + ": " + this.bossTriggeredBlocksList.get(i).getRight().toString(), x, 48 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			}
			if (this.bossTriggeredBlocksList.size() > 4) {
//                    context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_96_TEXTURE, this.width / 2 - 154, 42, 8, 96);
				context.drawTexture(SCROLL_BAR_BACKGROUND_8_96_TEXTURE, this.width / 2 - 154, 42, 0, 0, 8, 96);
				int k = (int) (85.0f * this.scrollAmount);
//                    context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 42 + 1 + k, 6, 7);
				context.drawTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.width / 2 - 153, 42 + 1 + k, 0, 0, 6, 7);
			}
			this.newBossTriggeredBlockIdentifierField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.newBossTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	private void updateBossControllerBlock() {
		ClientPlayNetworking.send(new UpdateBossControllerBlockPacket(
				this.bossControllerBlock.getPos(),
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
				this.bossIdentifierField.getText(),
				new BlockPos(
						ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getText())
				),
				ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getText()),
				ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getText()),
				this.bossTriggeredBlocksList
		));
	}

	public static enum ScreenPage implements StringIdentifiable {
		AREA("area"),
		BOSS_TRIGGERED_BLOCKS("boss_triggered_blocks"),
		SPAWN_POSITION("spawn_position");

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
			return Text.translatable("gui.boss_controller_block.creativeScreenPage." + this.name);
		}
	}
}
