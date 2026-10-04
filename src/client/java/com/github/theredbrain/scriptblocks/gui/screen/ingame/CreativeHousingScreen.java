package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockCreativePacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class CreativeHousingScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text HIDE_INFLUENCE_AREA_LABEL_TEXT = Text.translatable("gui.housing_block.hide_influence_area_label");
	private static final Text SHOW_INFLUENCE_AREA_LABEL_TEXT = Text.translatable("gui.housing_block.show_influence_area_label");
	private static final Text INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.housing_block.influence_area_dimensions_label");
	private static final Text INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.housing_block.influence_area_position_offset_label");
	private static final Text RESET_OWNER_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_block.reset_owner_button_label");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset");
	@Nullable
	private final HousingBlockEntity housingBlockEntity;

	private CyclingButtonWidget<ScreenPage> cycleScreenPageButton;
	private CyclingButtonWidget<Boolean> showInfluenceAreaButton;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsXField;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsYField;
	private TextFieldWidget restrictBlockBreakingAreaDimensionsZField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetXField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetYField;
	private TextFieldWidget restrictBlockBreakingAreaPositionOffsetZField;
	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private CyclingButtonWidget<HousingBlockEntity.OwnerMode> toggleOwnerModeButton;
	private ButtonWidget resetOwnerButton;

	private ScreenPage screenPage;
	private boolean showInfluenceArea = false;
	private HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.DIMENSION_OWNER;

	public CreativeHousingScreen(@Nullable HousingBlockEntity housingBlockEntity) {
		super(NarratorManager.EMPTY);
		this.housingBlockEntity = housingBlockEntity;
		this.screenPage = ScreenPage.INFLUENCE;
	}

	private void resetHouseOwner() {
		if (this.housingBlockEntity != null && this.client != null && this.client.player != null) {
			ClientPlayNetworking.send(new SetHousingBlockOwnerPacket(
					this.housingBlockEntity.getPos(),
					""
			));
		}
		this.close();
	}

	@Override
	protected void init() {
		if (this.housingBlockEntity != null) {
			this.showInfluenceArea = housingBlockEntity.getShowInfluenceArea();
			this.ownerMode = housingBlockEntity.getOwnerMode();
		}
		super.init();

		this.cycleScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values(ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- influence area page ---

		this.showInfluenceAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_INFLUENCE_AREA_LABEL_TEXT, SHOW_INFLUENCE_AREA_LABEL_TEXT).initially(this.showInfluenceArea).omitKeyText().build(this.width / 2 - 153, 45, 300, 20, Text.empty(), (button, showInfluenceArea) -> {
			this.showInfluenceArea = showInfluenceArea;
		}));

		this.restrictBlockBreakingAreaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsXField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getX() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsXField);

		this.restrictBlockBreakingAreaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsYField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getY() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsYField);

		this.restrictBlockBreakingAreaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaDimensionsZField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getZ() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaDimensionsZField);

		this.restrictBlockBreakingAreaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetXField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getX() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetXField);

		this.restrictBlockBreakingAreaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetYField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getY() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetYField);

		this.restrictBlockBreakingAreaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 115, 100, 20, Text.empty());
		this.restrictBlockBreakingAreaPositionOffsetZField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getZ() : 0));
		this.addSelectableChild(this.restrictBlockBreakingAreaPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getX() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);

		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getY() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);

		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 80, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getZ() : 0));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getRight() : false;
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 80, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));
		// --- owner page ---

		this.toggleOwnerModeButton = this.addDrawableChild(CyclingButtonWidget.builder(HousingBlockEntity.OwnerMode::asText).values((HousingBlockEntity.OwnerMode[]) HousingBlockEntity.OwnerMode.values()).initially(this.ownerMode).omitKeyText().build(this.width / 2 - 153, 70, 300, 20, Text.empty(), (button, ownerMode) -> {
			this.ownerMode = ownerMode;
		}));

		this.resetOwnerButton = this.addDrawableChild(ButtonWidget.builder(RESET_OWNER_BUTTON_LABEL_TEXT, button -> this.resetHouseOwner()).dimensions(this.width / 2 - 4 - 150, 94, 300, 20).build());

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.showInfluenceAreaButton.visible = false;
		this.restrictBlockBreakingAreaDimensionsXField.setVisible(false);
		this.restrictBlockBreakingAreaDimensionsYField.setVisible(false);
		this.restrictBlockBreakingAreaDimensionsZField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetXField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetYField.setVisible(false);
		this.restrictBlockBreakingAreaPositionOffsetZField.setVisible(false);

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;

		this.toggleOwnerModeButton.visible = false;
		this.resetOwnerButton.visible = false;

		if (this.screenPage == ScreenPage.INFLUENCE) {

			this.showInfluenceAreaButton.visible = true;
			this.restrictBlockBreakingAreaDimensionsXField.setVisible(true);
			this.restrictBlockBreakingAreaDimensionsYField.setVisible(true);
			this.restrictBlockBreakingAreaDimensionsZField.setVisible(true);
			this.restrictBlockBreakingAreaPositionOffsetXField.setVisible(true);
			this.restrictBlockBreakingAreaPositionOffsetYField.setVisible(true);
			this.restrictBlockBreakingAreaPositionOffsetZField.setVisible(true);

		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockResetsButton.visible = true;

		} else if (this.screenPage == ScreenPage.OWNER) {

			this.toggleOwnerModeButton.visible = true;
			this.resetOwnerButton.visible = true;

		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		boolean bool = this.showInfluenceArea;
		boolean boolean1 = this.triggeredBlockResets;
		HousingBlockEntity.OwnerMode var = this.ownerMode;
		String string3 = this.restrictBlockBreakingAreaDimensionsXField.getText();
		String string4 = this.restrictBlockBreakingAreaDimensionsYField.getText();
		String string5 = this.restrictBlockBreakingAreaDimensionsZField.getText();
		String string6 = this.restrictBlockBreakingAreaPositionOffsetXField.getText();
		String string7 = this.restrictBlockBreakingAreaPositionOffsetYField.getText();
		String string8 = this.restrictBlockBreakingAreaPositionOffsetZField.getText();
		String string9 = this.triggeredBlockPositionOffsetXField.getText();
		String string10 = this.triggeredBlockPositionOffsetYField.getText();
		String string11 = this.triggeredBlockPositionOffsetZField.getText();
		this.init(client, width, height);
		this.showInfluenceArea = bool;
		this.showInfluenceAreaButton.setValue(this.showInfluenceArea);
		this.triggeredBlockResets = boolean1;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.ownerMode = var;
		this.toggleOwnerModeButton.setValue(this.ownerMode);
		this.restrictBlockBreakingAreaDimensionsXField.setText(string3);
		this.restrictBlockBreakingAreaDimensionsYField.setText(string4);
		this.restrictBlockBreakingAreaDimensionsZField.setText(string5);
		this.restrictBlockBreakingAreaPositionOffsetXField.setText(string6);
		this.restrictBlockBreakingAreaPositionOffsetYField.setText(string7);
		this.restrictBlockBreakingAreaPositionOffsetZField.setText(string8);
		this.triggeredBlockPositionOffsetXField.setText(string9);
		this.triggeredBlockPositionOffsetYField.setText(string10);
		this.triggeredBlockPositionOffsetZField.setText(string11);
		this.updateWidgets();
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

		if (this.screenPage == ScreenPage.INFLUENCE) {
			context.drawTextWithShadow(this.textRenderer, INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.restrictBlockBreakingAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.restrictBlockBreakingAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.restrictBlockBreakingAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.restrictBlockBreakingAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.restrictBlockBreakingAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.restrictBlockBreakingAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {
			context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		BlockPos housingBlockPos = new BlockPos(0, 0, 0);
		if (this.housingBlockEntity != null) {
			housingBlockPos = this.housingBlockEntity.getPos();
		}
		ClientPlayNetworking.send(new UpdateHousingBlockCreativePacket(
				housingBlockPos,
				this.showInfluenceArea,
				new Vec3i(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsXField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsYField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
				),
				this.triggeredBlockResets,
				this.ownerMode.asString()
		));
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		INFLUENCE("influence"),
		TRIGGERED_BLOCK("triggered_block"),
		OWNER("owner");

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
			return Text.translatable("gui.housing_block.screen_page." + this.name);
		}
	}
}
