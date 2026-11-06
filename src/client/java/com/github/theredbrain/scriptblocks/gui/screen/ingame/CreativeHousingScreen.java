package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockCreativePacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class CreativeHousingScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component HIDE_INFLUENCE_AREA_LABEL_TEXT = Component.translatable("gui.housing_block.hide_influence_area_label");
	private static final Component SHOW_INFLUENCE_AREA_LABEL_TEXT = Component.translatable("gui.housing_block.show_influence_area_label");
	private static final Component INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.housing_block.influence_area_dimensions_label");
	private static final Component INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.housing_block.influence_area_position_offset_label");
	private static final Component RESET_OWNER_BUTTON_LABEL_TEXT = Component.translatable("gui.housing_block.reset_owner_button_label");
	private static final Component TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	@Nullable
	private final HousingBlockEntity housingBlockEntity;

	private CycleButton<ScreenPage> cycleScreenPageButton;
	private CycleButton<Boolean> showInfluenceAreaButton;
	private EditBox restrictBlockBreakingAreaDimensionsXField;
	private EditBox restrictBlockBreakingAreaDimensionsYField;
	private EditBox restrictBlockBreakingAreaDimensionsZField;
	private EditBox restrictBlockBreakingAreaPositionOffsetXField;
	private EditBox restrictBlockBreakingAreaPositionOffsetYField;
	private EditBox restrictBlockBreakingAreaPositionOffsetZField;
	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;
	private CycleButton<HousingBlockEntity.OwnerMode> toggleOwnerModeButton;
	private Button resetOwnerButton;

	private ScreenPage screenPage;
	private boolean showInfluenceArea = false;
	private HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.DIMENSION_OWNER;

	public CreativeHousingScreen(@Nullable HousingBlockEntity housingBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.housingBlockEntity = housingBlockEntity;
		this.screenPage = ScreenPage.INFLUENCE;
	}

	private void resetHouseOwner() {
		if (this.housingBlockEntity != null && this.minecraft.player != null) {
			ClientPlayNetworking.send(new SetHousingBlockOwnerPacket(
					this.housingBlockEntity.getBlockPos(),
					""
			));
		}
		this.onClose();
	}

	@Override
	protected void init() {
		if (this.housingBlockEntity != null) {
			this.showInfluenceArea = housingBlockEntity.getShowInfluenceArea();
			this.ownerMode = housingBlockEntity.getOwnerMode();
		}
		super.init();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText, this.screenPage).withValues(ScreenPage.values())
				.displayOnlyValue()
				.create(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- influence area page ---

		this.showInfluenceAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(HIDE_INFLUENCE_AREA_LABEL_TEXT, SHOW_INFLUENCE_AREA_LABEL_TEXT, this.showInfluenceArea)
				.displayOnlyValue()
				.create(this.width / 2 - 153, 45, 300, 20, Component.empty(), (button, showInfluenceArea) -> {
			this.showInfluenceArea = showInfluenceArea;
		}));

		this.restrictBlockBreakingAreaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaDimensionsXField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsXField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getX() : 0));
		this.addWidget(this.restrictBlockBreakingAreaDimensionsXField);

		this.restrictBlockBreakingAreaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaDimensionsYField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsYField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getY() : 0));
		this.addWidget(this.restrictBlockBreakingAreaDimensionsYField);

		this.restrictBlockBreakingAreaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaDimensionsZField.setMaxLength(128);
		this.restrictBlockBreakingAreaDimensionsZField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getInfluenceAreaDimensions().getZ() : 0));
		this.addWidget(this.restrictBlockBreakingAreaDimensionsZField);

		this.restrictBlockBreakingAreaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaPositionOffsetXField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetXField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getX() : 0));
		this.addWidget(this.restrictBlockBreakingAreaPositionOffsetXField);

		this.restrictBlockBreakingAreaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaPositionOffsetYField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetYField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getY() : 0));
		this.addWidget(this.restrictBlockBreakingAreaPositionOffsetYField);

		this.restrictBlockBreakingAreaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 115, 100, 20, Component.empty());
		this.restrictBlockBreakingAreaPositionOffsetZField.setMaxLength(128);
		this.restrictBlockBreakingAreaPositionOffsetZField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getRestrictBlockBreakingAreaPositionOffset().getZ() : 0));
		this.addWidget(this.restrictBlockBreakingAreaPositionOffsetZField);

		// --- triggered block page ---

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getX() : 0));
		this.addWidget(this.triggeredBlockPositionOffsetXField);

		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getY() : 0));
		this.addWidget(this.triggeredBlockPositionOffsetYField);

		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 80, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getLeft().getZ() : 0));
		this.addWidget(this.triggeredBlockPositionOffsetZField);

		this.triggeredBlockResets = this.housingBlockEntity != null ? this.housingBlockEntity.getTriggeredBlock().getRight() : false;
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off"), this.triggeredBlockResets)
				.displayOnlyValue()
				.create(this.width / 2 + 8, 80, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));
		// --- owner page ---

		this.toggleOwnerModeButton = this.addRenderableWidget(CycleButton.builder(HousingBlockEntity.OwnerMode::asText, this.ownerMode)
				.withValues(HousingBlockEntity.OwnerMode.values())
				.displayOnlyValue()
				.create(this.width / 2 - 153, 70, 300, 20, Component.empty(), (button, ownerMode) -> {
			this.ownerMode = ownerMode;
		}));

		this.resetOwnerButton = this.addRenderableWidget(Button.builder(RESET_OWNER_BUTTON_LABEL_TEXT, button -> this.resetHouseOwner()).bounds(this.width / 2 - 4 - 150, 94, 300, 20).build());

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
	public void resize(final int width, final int height) {
		boolean bool = this.showInfluenceArea;
		boolean boolean1 = this.triggeredBlockResets;
		HousingBlockEntity.OwnerMode var = this.ownerMode;
		String string3 = this.restrictBlockBreakingAreaDimensionsXField.getValue();
		String string4 = this.restrictBlockBreakingAreaDimensionsYField.getValue();
		String string5 = this.restrictBlockBreakingAreaDimensionsZField.getValue();
		String string6 = this.restrictBlockBreakingAreaPositionOffsetXField.getValue();
		String string7 = this.restrictBlockBreakingAreaPositionOffsetYField.getValue();
		String string8 = this.restrictBlockBreakingAreaPositionOffsetZField.getValue();
		String string9 = this.triggeredBlockPositionOffsetXField.getValue();
		String string10 = this.triggeredBlockPositionOffsetYField.getValue();
		String string11 = this.triggeredBlockPositionOffsetZField.getValue();
		this.init(width, height);
		this.showInfluenceArea = bool;
		this.showInfluenceAreaButton.setValue(this.showInfluenceArea);
		this.triggeredBlockResets = boolean1;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.ownerMode = var;
		this.toggleOwnerModeButton.setValue(this.ownerMode);
		this.restrictBlockBreakingAreaDimensionsXField.setValue(string3);
		this.restrictBlockBreakingAreaDimensionsYField.setValue(string4);
		this.restrictBlockBreakingAreaDimensionsZField.setValue(string5);
		this.restrictBlockBreakingAreaPositionOffsetXField.setValue(string6);
		this.restrictBlockBreakingAreaPositionOffsetYField.setValue(string7);
		this.restrictBlockBreakingAreaPositionOffsetZField.setValue(string8);
		this.triggeredBlockPositionOffsetXField.setValue(string9);
		this.triggeredBlockPositionOffsetYField.setValue(string10);
		this.triggeredBlockPositionOffsetZField.setValue(string11);
		this.updateWidgets();
	}

	@Override
	public boolean keyPressed(final KeyEvent event) {
		if (event.isConfirmation()) {
			this.done();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		if (this.screenPage == ScreenPage.INFLUENCE) {
			graphics.text(this.font, INFLUENCE_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 70, -6250336);
			this.restrictBlockBreakingAreaDimensionsXField.extractRenderState(graphics, mouseX, mouseY, a);
			this.restrictBlockBreakingAreaDimensionsYField.extractRenderState(graphics, mouseX, mouseY, a);
			this.restrictBlockBreakingAreaDimensionsZField.extractRenderState(graphics, mouseX, mouseY, a);
			graphics.text(this.font, INFLUENCE_AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 105, -6250336);
			this.restrictBlockBreakingAreaPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
			this.restrictBlockBreakingAreaPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
			this.restrictBlockBreakingAreaPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
		} else if (this.screenPage == ScreenPage.TRIGGERED_BLOCK) {
			graphics.text(this.font, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 70, -6250336);
			this.triggeredBlockPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
			this.triggeredBlockPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
			this.triggeredBlockPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
		}
	}

	@Override
	protected boolean updateBlock() {
		BlockPos housingBlockPos = new BlockPos(0, 0, 0);
		if (this.housingBlockEntity != null) {
			housingBlockPos = this.housingBlockEntity.getBlockPos();
		}
		ClientPlayNetworking.send(new UpdateHousingBlockCreativePacket(
				housingBlockPos,
				this.showInfluenceArea,
				new Vec3i(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.restrictBlockBreakingAreaPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				this.ownerMode
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		INFLUENCE("influence"),
		TRIGGERED_BLOCK("triggered_block"),
		OWNER("owner");

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
			return Component.translatable("gui.housing_block.screen_page." + this.name);
		}
	}
}
