package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDisplayBlockPacket;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredDisplayBlockScreen extends AbstractCreativeBlockSettingsScreen {

	private final TriggeredDisplayBlockEntity triggeredDisplayBlock;

	private CycleButton<ScreenPage> cycleScreenPageButton;

	// common
	private CycleButton<TriggeredDisplayBlockEntity.BillboardMode> billboardModeButton;
	private CycleButton<TriggeredDisplayBlockEntity.DisplayMode> displayModeButton;
	private CycleButton<Boolean> toggleIsTriggeredButton;
	private EditBox displayOffsetXField;
	private EditBox displayOffsetYField;
	private EditBox displayOffsetZField;
	private EditBox displayYawField;
	private EditBox displayPitchField;


	// text mode
	private EditBox displayTextField;
	private EditBox dataProvidingBlockPosOffsetXField;
	private EditBox dataProvidingBlockPosOffsetYField;
	private EditBox dataProvidingBlockPosOffsetZField;
	private EditBox dataIdentifierTextField;
	private EditBox lineWidthField;
	private EditBox textOpacityField;
	private EditBox textBackgroundField;

	private ScreenPage screenPage;
	private TriggeredDisplayBlockEntity.BillboardMode billboardMode;
	private TriggeredDisplayBlockEntity.DisplayMode displayMode;
	private boolean isTriggered;

	public TriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlock) {
		super(GameNarrator.NO_TITLE);
		this.triggeredDisplayBlock = triggeredDisplayBlock;
	}

	@Override
	protected void init() {
		this.screenPage = ScreenPage.COMMON;
		this.billboardMode = this.triggeredDisplayBlock.getBillboardMode();
		this.displayMode = this.triggeredDisplayBlock.getDisplayMode();
		this.isTriggered = this.triggeredDisplayBlock.getIsTriggered();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- common ---
		this.billboardModeButton = this.addRenderableWidget(CycleButton.builder(TriggeredDisplayBlockEntity.BillboardMode::asText).values((TriggeredDisplayBlockEntity.BillboardMode[]) TriggeredDisplayBlockEntity.BillboardMode.values()).initially(this.billboardMode).omitKeyText().build(this.width / 2 - 154, 44, 300, 20, Component.empty(), (button, billboardMode) -> {
			this.billboardMode = billboardMode;
		}));

		this.displayModeButton = this.addRenderableWidget(CycleButton.builder(TriggeredDisplayBlockEntity.DisplayMode::asText).values((TriggeredDisplayBlockEntity.DisplayMode[]) TriggeredDisplayBlockEntity.DisplayMode.values()).initially(this.displayMode).omitKeyText().build(this.width / 2 - 154, 68, 300, 20, Component.empty(), (button, displayMode) -> {
			this.displayMode = displayMode;
		}));

		this.toggleIsTriggeredButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_display_block.toggle_is_triggered_button_label.on"), Component.translatable("gui.triggered_display_block.toggle_is_triggered_button_label.off")).initially(this.isTriggered).omitKeyText().build(this.width / 2 - 154, 92, 300, 20, Component.empty(), (button, isTriggered) -> {
			this.isTriggered = isTriggered;
		}));

		this.displayOffsetXField = new EditBox(this.font, this.width / 2 - 154, 116, 50, 20, Component.empty());
		this.displayOffsetXField.setMaxLength(128);
		this.displayOffsetXField.setValue(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getX()));
		this.addWidget(this.displayOffsetXField);

		this.displayOffsetYField = new EditBox(this.font, this.width / 2 - 100, 116, 50, 20, Component.empty());
		this.displayOffsetYField.setMaxLength(128);
		this.displayOffsetYField.setValue(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getY()));
		this.addWidget(this.displayOffsetYField);

		this.displayOffsetZField = new EditBox(this.font, this.width / 2 - 46, 116, 50, 20, Component.empty());
		this.displayOffsetZField.setMaxLength(128);
		this.displayOffsetZField.setValue(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getZ()));
		this.addWidget(this.displayOffsetZField);

		this.displayYawField = new EditBox(this.font, this.width / 2 + 8, 116, 50, 20, Component.empty());
		this.displayYawField.setMaxLength(128);
		this.displayYawField.setValue(Float.toString(this.triggeredDisplayBlock.getDisplayYaw()));
		this.addWidget(this.displayYawField);

		this.displayPitchField = new EditBox(this.font, this.width / 2 + 62, 116, 50, 20, Component.empty());
		this.displayPitchField.setMaxLength(128);
		this.displayPitchField.setValue(Float.toString(this.triggeredDisplayBlock.getDisplayPitch()));
		this.addWidget(this.displayPitchField);

		// --- text mode ---
		this.displayTextField = new EditBox(this.font, this.width / 2 - 154, 44, 300, 20, Component.empty());
		this.displayTextField.setMaxLength(128);
		this.displayTextField.setValue(this.triggeredDisplayBlock.getTextString());
		this.addWidget(this.displayTextField);

		this.dataProvidingBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 68, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setValue(Integer.toString(this.triggeredDisplayBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 104, 68, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setValue(Integer.toString(this.triggeredDisplayBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 54, 68, 50, 20, Component.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setValue(Integer.toString(this.triggeredDisplayBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPosOffsetZField);

		this.dataIdentifierTextField = new EditBox(this.font, this.width / 2 + 4, 68, 150, 20, Component.empty());
		this.dataIdentifierTextField.setMaxLength(128);
		this.dataIdentifierTextField.setValue(this.triggeredDisplayBlock.getDataIdentifierString());
		this.addWidget(this.dataIdentifierTextField);

		this.lineWidthField = new EditBox(this.font, this.width / 2 - 154, 92, 100, 20, Component.empty());
		this.lineWidthField.setMaxLength(128);
		this.lineWidthField.setValue(Integer.toString(this.triggeredDisplayBlock.getLineWidth()));
		this.addWidget(this.lineWidthField);

		this.textOpacityField = new EditBox(this.font, this.width / 2 - 50, 92, 100, 20, Component.empty());
		this.textOpacityField.setMaxLength(128);
		this.textOpacityField.setValue(Byte.toString(this.triggeredDisplayBlock.getTextOpacity()));
		this.addWidget(this.textOpacityField);

		this.textBackgroundField = new EditBox(this.font, this.width / 2 + 54, 92, 100, 20, Component.empty());
		this.textBackgroundField.setMaxLength(128);
		this.textBackgroundField.setValue(Integer.toString(this.triggeredDisplayBlock.getBackground()));
		this.addWidget(this.textBackgroundField);

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

		this.billboardModeButton.visible = false;
		this.displayModeButton.visible = false;
		this.toggleIsTriggeredButton.visible = false;
		this.displayOffsetXField.setVisible(false);
		this.displayOffsetYField.setVisible(false);
		this.displayOffsetZField.setVisible(false);
		this.displayYawField.setVisible(false);
		this.displayPitchField.setVisible(false);

		this.displayTextField.setVisible(false);
		this.dataProvidingBlockPosOffsetXField.setVisible(false);
		this.dataProvidingBlockPosOffsetYField.setVisible(false);
		this.dataProvidingBlockPosOffsetZField.setVisible(false);
		this.dataIdentifierTextField.setVisible(false);
		this.lineWidthField.setVisible(false);
		this.textOpacityField.setVisible(false);
		this.textBackgroundField.setVisible(false);

		if (this.screenPage == ScreenPage.COMMON) {

			this.billboardModeButton.visible = true;
			this.displayModeButton.visible = true;
			this.toggleIsTriggeredButton.visible = true;
			this.displayOffsetXField.setVisible(true);
			this.displayOffsetYField.setVisible(true);
			this.displayOffsetZField.setVisible(true);
			this.displayYawField.setVisible(true);
			this.displayPitchField.setVisible(true);

		} else if (this.screenPage == ScreenPage.TEXT_MODE) {

			this.displayTextField.setVisible(true);
			this.dataProvidingBlockPosOffsetXField.setVisible(true);
			this.dataProvidingBlockPosOffsetYField.setVisible(true);
			this.dataProvidingBlockPosOffsetZField.setVisible(true);
			this.dataIdentifierTextField.setVisible(true);
			this.lineWidthField.setVisible(true);
			this.textOpacityField.setVisible(true);
			this.textBackgroundField.setVisible(true);
		}

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		// TODO
		this.init(client, width, height);
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

		if (this.screenPage == ScreenPage.COMMON) {

			this.displayOffsetXField.render(context, mouseX, mouseY, delta);
			this.displayOffsetYField.render(context, mouseX, mouseY, delta);
			this.displayOffsetZField.render(context, mouseX, mouseY, delta);
			this.displayYawField.render(context, mouseX, mouseY, delta);
			this.displayPitchField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TEXT_MODE) {

			this.displayTextField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
			this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
			this.dataIdentifierTextField.render(context, mouseX, mouseY, delta);
			this.lineWidthField.render(context, mouseX, mouseY, delta);
			this.textOpacityField.render(context, mouseX, mouseY, delta);
			this.textBackgroundField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredDisplayBlockPacket(
				this.triggeredDisplayBlock.getPos(),
				this.billboardMode.asString(),
				this.displayMode.asString(),
				this.isTriggered,
				new Vec3(
						ItemUtils.parseDouble(this.displayOffsetXField.getValue()),
						ItemUtils.parseDouble(this.displayOffsetYField.getValue()),
						ItemUtils.parseDouble(this.displayOffsetZField.getValue())
				),
				ItemUtils.parseFloat(this.displayYawField.getValue()),
				ItemUtils.parseFloat(this.displayPitchField.getValue()),
				this.displayTextField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getValue())
				),
				this.dataIdentifierTextField.getValue(),
				ItemUtils.parseInt(this.lineWidthField.getValue()),
				ItemUtils.parseByte(this.textOpacityField.getValue()),
				ItemUtils.parseInt(this.textBackgroundField.getValue())
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		COMMON("common"),
		BLOCK_MODE("block_mode"),
		ITEM_MODE("item_mode"),
		TEXT_MODE("text_mode");

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
			return Component.translatable("gui.triggered_display_block.screen_page." + this.name);
		}
	}
}
