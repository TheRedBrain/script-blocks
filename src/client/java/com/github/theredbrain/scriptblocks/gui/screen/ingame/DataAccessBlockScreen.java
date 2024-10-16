package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataAccessBlockPacket;
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

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DataAccessBlockScreen extends Screen {
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.dataProvidingBlockPositionOffset");
	private static final Text FIRST_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_advancement_checker_block.first_triggered_block_position_offset_label");
	private static final Text SECOND_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_advancement_checker_block.second_triggered_block_position_offset_label");
	private final DataAccessBlockEntity dataAccessBlock;
	private CyclingButtonWidget<ScreenPage> screenPageButton;
	private ScreenPage screenPage = ScreenPage.BLOCK_OFFSETS;
	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private TextFieldWidget firstTriggeredBlockPositionOffsetXField;
	private TextFieldWidget firstTriggeredBlockPositionOffsetYField;
	private TextFieldWidget firstTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleFirstTriggeredBlockResetsButton;
	private boolean firstTriggeredBlockResets;
	private TextFieldWidget secondTriggeredBlockPositionOffsetXField;
	private TextFieldWidget secondTriggeredBlockPositionOffsetYField;
	private TextFieldWidget secondTriggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleSecondTriggeredBlockResetsButton;
	private boolean secondTriggeredBlockResets;
	private boolean isWriting;
	private CyclingButtonWidget<Boolean> toggleIsWritingButton;
	private TextFieldWidget dataIdentifierField;
	private TextFieldWidget comparedDataValueField;
	private DataAccessBlockEntity.DataReadingMode dataReadingMode;
	private CyclingButtonWidget<DataAccessBlockEntity.DataReadingMode> dataReadingModeButton;
	private boolean isAdding;
	private CyclingButtonWidget<Boolean> toggleIsAddingButton;
	private TextFieldWidget newDataValueField;

	public DataAccessBlockScreen(DataAccessBlockEntity dataAccessBlock) {
		super(NarratorManager.EMPTY);
		this.dataAccessBlock = dataAccessBlock;
	}

	private void done() {
		if (this.updateDataAccessBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.screenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- block offsets ---

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.dataAccessBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.dataAccessBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 55, 50, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.dataAccessBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.firstTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 50, 20, Text.empty());
		this.firstTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetXField.setText(Integer.toString(this.dataAccessBlock.getFirstTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.firstTriggeredBlockPositionOffsetXField);
		this.firstTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 90, 50, 20, Text.empty());
		this.firstTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetYField.setText(Integer.toString(this.dataAccessBlock.getFirstTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.firstTriggeredBlockPositionOffsetYField);
		this.firstTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 90, 50, 20, Text.empty());
		this.firstTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.firstTriggeredBlockPositionOffsetZField.setText(Integer.toString(this.dataAccessBlock.getFirstTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.firstTriggeredBlockPositionOffsetZField);

		this.firstTriggeredBlockResets = this.dataAccessBlock.getFirstTriggeredBlock().getRight();
		this.toggleFirstTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.firstTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Text.empty(), (button, firstTriggeredBlockResets) -> {
			this.firstTriggeredBlockResets = firstTriggeredBlockResets;
		}));

		this.secondTriggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 50, 20, Text.empty());
		this.secondTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetXField.setText(Integer.toString(this.dataAccessBlock.getSecondTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.secondTriggeredBlockPositionOffsetXField);
		this.secondTriggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 125, 50, 20, Text.empty());
		this.secondTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetYField.setText(Integer.toString(this.dataAccessBlock.getSecondTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.secondTriggeredBlockPositionOffsetYField);
		this.secondTriggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 125, 50, 20, Text.empty());
		this.secondTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.secondTriggeredBlockPositionOffsetZField.setText(Integer.toString(this.dataAccessBlock.getSecondTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.secondTriggeredBlockPositionOffsetZField);

		this.secondTriggeredBlockResets = this.dataAccessBlock.getSecondTriggeredBlock().getRight();
		this.toggleSecondTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.secondTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 125, 150, 20, Text.empty(), (button, secondTriggeredBlockResets) -> {
			this.secondTriggeredBlockResets = secondTriggeredBlockResets;
		}));

		// --- interaction ---

		this.isWriting = this.dataAccessBlock.isWriting();
		this.toggleIsWritingButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.data_access_block.toggle_is_writing_button_label.on"), Text.translatable("gui.data_access_block.toggle_is_writing_button_label.off")).initially(this.isWriting).omitKeyText().build(this.width / 2 - 154, 55, 300, 20, Text.empty(), (button, isWriting) -> {
			this.isWriting = isWriting;
			this.updateWidgets();
		}));

		this.dataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 300, 20, Text.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setText(this.dataAccessBlock.getDataIdentifier());
		this.addSelectableChild(this.dataIdentifierField);

		this.comparedDataValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 50, 20, Text.empty());
		this.comparedDataValueField.setMaxLength(128);
		this.comparedDataValueField.setText(Integer.toString(this.dataAccessBlock.getComparedDataValue()));
		this.addSelectableChild(this.comparedDataValueField);

		this.dataReadingMode = this.dataAccessBlock.getDataReadingMode();
		this.dataReadingModeButton = this.addDrawableChild(CyclingButtonWidget.builder(DataAccessBlockEntity.DataReadingMode::asText).values((DataAccessBlockEntity.DataReadingMode[]) DataAccessBlockEntity.DataReadingMode.values()).initially(this.dataReadingMode).omitKeyText().build(this.width / 2 - 100, 125, 250, 20, Text.empty(), (button, dataReadingMode) -> {
			this.dataReadingMode = dataReadingMode;
			this.updateWidgets();
		}));

		this.isAdding = this.dataAccessBlock.isAdding();
		this.toggleIsAddingButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.data_access_block.toggle_is_adding_button_label.on"), Text.translatable("gui.data_access_block.toggle_is_adding_button_label.off")).initially(this.isAdding).omitKeyText().build(this.width / 2 - 50, 90, 200, 20, Text.empty(), (button, isAdding) -> {
			this.isAdding = isAdding;
		}));

		this.newDataValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 100, 20, Text.empty());
		this.newDataValueField.setMaxLength(128);
		this.newDataValueField.setText(Integer.toString(this.dataAccessBlock.getNewDataValue()));
		this.addSelectableChild(this.newDataValueField);

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

		this.firstTriggeredBlockPositionOffsetXField.setVisible(false);
		this.firstTriggeredBlockPositionOffsetYField.setVisible(false);
		this.firstTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleFirstTriggeredBlockResetsButton.visible = false;

		this.secondTriggeredBlockPositionOffsetXField.setVisible(false);
		this.secondTriggeredBlockPositionOffsetYField.setVisible(false);
		this.secondTriggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleSecondTriggeredBlockResetsButton.visible = false;

		this.toggleIsWritingButton.visible = false;

		this.dataIdentifierField.setVisible(false);
		this.comparedDataValueField.setVisible(false);
		this.dataReadingModeButton.visible = false;

		this.toggleIsAddingButton.visible = false;

		this.newDataValueField.setVisible(false);

		if (this.screenPage == ScreenPage.BLOCK_OFFSETS) {

			this.dataProvidingBlockPositionOffsetXField.setVisible(true);
			this.dataProvidingBlockPositionOffsetYField.setVisible(true);
			this.dataProvidingBlockPositionOffsetZField.setVisible(true);

			this.firstTriggeredBlockPositionOffsetXField.setVisible(true);
			this.firstTriggeredBlockPositionOffsetYField.setVisible(true);
			this.firstTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleFirstTriggeredBlockResetsButton.visible = true;

			this.secondTriggeredBlockPositionOffsetXField.setVisible(true);
			this.secondTriggeredBlockPositionOffsetYField.setVisible(true);
			this.secondTriggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleSecondTriggeredBlockResetsButton.visible = true;

		} else if (this.screenPage == ScreenPage.INTERACTION) {

			this.toggleIsWritingButton.visible = true;
			this.dataIdentifierField.setVisible(true);

			if (this.isWriting) {

				this.toggleIsAddingButton.visible = true;
				this.newDataValueField.setVisible(true);

			} else {

				this.comparedDataValueField.setVisible(true);
				this.dataReadingModeButton.visible = true;

			}
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		DataAccessBlockEntity.DataReadingMode var1 = this.dataReadingMode;
		String string = this.dataProvidingBlockPositionOffsetXField.getText();
		String string1 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string2 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string3 = this.firstTriggeredBlockPositionOffsetXField.getText();
		String string4 = this.firstTriggeredBlockPositionOffsetYField.getText();
		String string5 = this.firstTriggeredBlockPositionOffsetZField.getText();
		String string6 = this.secondTriggeredBlockPositionOffsetXField.getText();
		String string7 = this.secondTriggeredBlockPositionOffsetYField.getText();
		String string8 = this.secondTriggeredBlockPositionOffsetZField.getText();
		String string9 = this.dataIdentifierField.getText();
		String string10 = this.comparedDataValueField.getText();
		String string11 = this.newDataValueField.getText();
		boolean bl = this.firstTriggeredBlockResets;
		boolean bl2 = this.secondTriggeredBlockResets;
		boolean bl3 = this.isWriting;
		boolean bl4 = this.isAdding;
		this.init(client, width, height);
		this.screenPage = var;
		this.dataReadingMode = var1;
		this.dataProvidingBlockPositionOffsetXField.setText(string);
		this.dataProvidingBlockPositionOffsetYField.setText(string1);
		this.dataProvidingBlockPositionOffsetZField.setText(string2);
		this.firstTriggeredBlockPositionOffsetXField.setText(string3);
		this.firstTriggeredBlockPositionOffsetYField.setText(string4);
		this.firstTriggeredBlockPositionOffsetZField.setText(string5);
		this.secondTriggeredBlockPositionOffsetXField.setText(string6);
		this.secondTriggeredBlockPositionOffsetYField.setText(string7);
		this.secondTriggeredBlockPositionOffsetZField.setText(string8);
		this.dataIdentifierField.setText(string9);
		this.comparedDataValueField.setText(string10);
		this.newDataValueField.setText(string11);
		this.firstTriggeredBlockResets = bl;
		this.secondTriggeredBlockResets = bl2;
		this.isWriting = bl3;
		this.isAdding = bl4;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.BLOCK_OFFSETS) {
			context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, FIRST_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.firstTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.firstTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.firstTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, SECOND_TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.secondTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.secondTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.secondTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.INTERACTION) {
			this.dataIdentifierField.render(context, mouseX, mouseY, delta);
			if (this.isWriting) {

				this.newDataValueField.render(context, mouseX, mouseY, delta);
			} else {

				this.comparedDataValueField.render(context, mouseX, mouseY, delta);
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataAccessBlock() {
		ClientPlayNetworking.send(new UpdateDataAccessBlockPacket(
				this.dataAccessBlock.getPos(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.firstTriggeredBlockPositionOffsetZField.getText())
				),
				this.firstTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.secondTriggeredBlockPositionOffsetZField.getText())
				),
				this.secondTriggeredBlockResets,
				this.isWriting,
				this.dataIdentifierField.getText(),
				ItemUtils.parseInt(this.comparedDataValueField.getText()),
				this.dataReadingMode.name(),
				this.isAdding,
				ItemUtils.parseInt(this.dataIdentifierField.getText())
		));
		return true;
	}

	public static enum ScreenPage implements StringIdentifiable {
		BLOCK_OFFSETS("block_offsets"),
		INTERACTION("interaction");

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
			return Text.translatable("gui.data_access_block.screenPage." + this.name);
		}
	}
}
