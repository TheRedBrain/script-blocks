package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDisplayBlockPacket;
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
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredDisplayBlockScreen extends Screen {

	private final TriggeredDisplayBlockEntity triggeredDisplayBlock;

	private CyclingButtonWidget<TriggeredDisplayBlockScreen.ScreenPage> screenPageButton;

	// common
	private CyclingButtonWidget<TriggeredDisplayBlockEntity.BillboardMode> billboardModeButton;
	private CyclingButtonWidget<TriggeredDisplayBlockEntity.DisplayMode> displayModeButton;
	private CyclingButtonWidget<Boolean> toggleIsTriggeredButton;
	private TextFieldWidget displayOffsetXField;
	private TextFieldWidget displayOffsetYField;
	private TextFieldWidget displayOffsetZField;


	// text mode
	private TextFieldWidget displayTextField;
	private TextFieldWidget lineWidthField;
	private TextFieldWidget textOpacityField;
	private TextFieldWidget textBackgroundField;

	private ButtonWidget doneButton;
	private ButtonWidget cancelButton;

	private ScreenPage screenPage;
	private TriggeredDisplayBlockEntity.BillboardMode billboardMode;
	private TriggeredDisplayBlockEntity.DisplayMode displayMode;
	private boolean isTriggered;

	public TriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlock) {
		super(NarratorManager.EMPTY);
		this.triggeredDisplayBlock = triggeredDisplayBlock;
	}

	private void done() {
		if (this.updateTriggeredDisplayBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.screenPage = ScreenPage.COMMON;
		this.billboardMode = this.triggeredDisplayBlock.getBillboardMode();
		this.displayMode = this.triggeredDisplayBlock.getDisplayMode();
		this.isTriggered = this.triggeredDisplayBlock.getIsTriggered();

		this.screenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- common ---
		this.billboardModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredDisplayBlockEntity.BillboardMode::asText).values((TriggeredDisplayBlockEntity.BillboardMode[]) TriggeredDisplayBlockEntity.BillboardMode.values()).initially(this.billboardMode).omitKeyText().build(this.width / 2 - 154, 44, 300, 20, Text.empty(), (button, billboardMode) -> {
			this.billboardMode = billboardMode;
		}));

		this.displayModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TriggeredDisplayBlockEntity.DisplayMode::asText).values((TriggeredDisplayBlockEntity.DisplayMode[]) TriggeredDisplayBlockEntity.DisplayMode.values()).initially(this.displayMode).omitKeyText().build(this.width / 2 - 154, 68, 300, 20, Text.empty(), (button, displayMode) -> {
			this.displayMode = displayMode;
		}));

		this.toggleIsTriggeredButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_display_block.toggle_is_triggered_button_label.on"), Text.translatable("gui.triggered_display_block.toggle_is_triggered_button_label.off")).initially(this.isTriggered).omitKeyText().build(this.width / 2 - 154, 92, 300, 20, Text.empty(), (button, isTriggered) -> {
			this.isTriggered = isTriggered;
		}));

		this.displayOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 116, 50, 20, Text.empty());
		this.displayOffsetXField.setMaxLength(128);
		this.displayOffsetXField.setText(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getX()));
		this.addSelectableChild(this.displayOffsetXField);

		this.displayOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 116, 50, 20, Text.empty());
		this.displayOffsetYField.setMaxLength(128);
		this.displayOffsetYField.setText(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getY()));
		this.addSelectableChild(this.displayOffsetYField);

		this.displayOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 116, 50, 20, Text.empty());
		this.displayOffsetZField.setMaxLength(128);
		this.displayOffsetZField.setText(Double.toString(this.triggeredDisplayBlock.getDisplayOffset().getZ()));
		this.addSelectableChild(this.displayOffsetZField);

		// --- text mode ---
		this.displayTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 44, 300, 20, Text.empty());
		this.displayTextField.setMaxLength(128);
		this.displayTextField.setText(this.triggeredDisplayBlock.getTextString());
		this.addSelectableChild(this.displayTextField);

		this.lineWidthField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 68, 100, 20, Text.empty());
		this.lineWidthField.setMaxLength(128);
		this.lineWidthField.setText(Integer.toString(this.triggeredDisplayBlock.getLineWidth()));
		this.addSelectableChild(this.lineWidthField);

		this.textOpacityField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 92, 100, 20, Text.empty());
		this.textOpacityField.setMaxLength(128);
		this.textOpacityField.setText(Byte.toString(this.triggeredDisplayBlock.getTextOpacity()));
		this.addSelectableChild(this.textOpacityField);

		this.textBackgroundField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 116, 100, 20, Text.empty());
		this.textBackgroundField.setMaxLength(128);
		this.textBackgroundField.setText(Integer.toString(this.triggeredDisplayBlock.getBackground()));
		this.addSelectableChild(this.textBackgroundField);

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.screenPageButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.billboardModeButton.visible = false;
		this.displayModeButton.visible = false;
		this.toggleIsTriggeredButton.visible = false;
		this.displayOffsetXField.setVisible(false);
		this.displayOffsetYField.setVisible(false);
		this.displayOffsetZField.setVisible(false);

		this.displayTextField.setVisible(false);
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

		} else if (this.screenPage == ScreenPage.TEXT_MODE) {

			this.displayTextField.setVisible(true);
			this.lineWidthField.setVisible(true);
			this.textOpacityField.setVisible(true);
			this.textBackgroundField.setVisible(true);
		}

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.COMMON) {

			this.displayOffsetXField.render(context, mouseX, mouseY, delta);
			this.displayOffsetYField.render(context, mouseX, mouseY, delta);
			this.displayOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.screenPage == ScreenPage.TEXT_MODE) {

			this.displayTextField.render(context, mouseX, mouseY, delta);
			this.lineWidthField.render(context, mouseX, mouseY, delta);
			this.textOpacityField.render(context, mouseX, mouseY, delta);
			this.textBackgroundField.render(context, mouseX, mouseY, delta);
		}
	}

	private boolean updateTriggeredDisplayBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredDisplayBlockPacket(
				this.triggeredDisplayBlock.getPos(),
				this.billboardMode.asString(),
				this.displayMode.asString(),
				this.isTriggered,
				new Vec3d(
						ItemUtils.parseDouble(this.displayOffsetXField.getText()),
						ItemUtils.parseDouble(this.displayOffsetYField.getText()),
						ItemUtils.parseDouble(this.displayOffsetZField.getText())
				),
				this.displayTextField.getText(),
				ItemUtils.parseInt(this.lineWidthField.getText()),
				ItemUtils.parseByte(this.textOpacityField.getText()),
				ItemUtils.parseInt(this.textBackgroundField.getText())
		));
		return true;
	}

	public enum ScreenPage implements StringIdentifiable {
		COMMON("common"),
		BLOCK_MODE("block_mode"),
		ITEM_MODE("item_mode"),
		TEXT_MODE("text_mode");

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
			return Text.translatable("gui.triggered_display_block.screenPage." + this.name);
		}
	}
}
