package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveLootBlockPacket;
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
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class InteractiveLootBlockScreen extends Screen {
	private static final Text LOOT_TABLE_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.loot_table_identifier_label");
	private static final Text LOOTABLE_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.lootable_identifier_label");
	private static final Text ROLLS_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.rolls_label");
	private static final Text CHOICES_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.choices_label");
	private final InteractiveLootBlockEntity interactiveLootBlockEntity;
	private TextFieldWidget lootTableIdentifierStringField;
	private InteractiveLootBlockEntity.Mode lootBlockMode;
	private TextFieldWidget rollsField;
	private TextFieldWidget choicesField;

	public InteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
		super(NarratorManager.EMPTY);
		this.interactiveLootBlockEntity = interactiveLootBlockEntity;
	}

	private void done() {
		this.updateInteractiveLootBlock();
		this.close();
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.lootTableIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 56, 308, 20, Text.translatable(""));
		this.lootTableIdentifierStringField.setMaxLength(128);
		this.lootTableIdentifierStringField.setText(this.interactiveLootBlockEntity.getLootTableIdentifierString());
		this.addSelectableChild(this.lootTableIdentifierStringField);

		this.lootBlockMode = this.interactiveLootBlockEntity.getMode();

		this.addDrawableChild(CyclingButtonWidget.builder(InteractiveLootBlockEntity.Mode::asText).values((InteractiveLootBlockEntity.Mode[]) InteractiveLootBlockEntity.Mode.values()).initially(this.lootBlockMode).omitKeyText().build(this.width / 2 - 154, 80, 308, 20, Text.empty(), (button, lootBlockMode) -> {
			this.lootBlockMode = lootBlockMode;
			this.updateWidgets();
		}));

		this.rollsField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 150, 20, Text.empty());
		this.rollsField.setMaxLength(128);
		this.rollsField.setText(Integer.toString(this.interactiveLootBlockEntity.getRolls()));
		this.addSelectableChild(this.rollsField);

		this.choicesField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 115, 150, 20, Text.empty());
		this.choicesField.setMaxLength(128);
		this.choicesField.setText(Integer.toString(this.interactiveLootBlockEntity.getChoices()));
		this.addSelectableChild(this.choicesField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 174, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 174, 150, 20).build());
		this.setInitialFocus(this.lootTableIdentifierStringField);

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.rollsField.setVisible(false);
		this.choicesField.setVisible(false);

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.CHOICE) {

			this.rollsField.setVisible(true);
			this.choicesField.setVisible(true);

		} else if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.RANDOM) {

			this.rollsField.setVisible(true);

		}

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.lootTableIdentifierStringField.getText();
		String string1 = this.rollsField.getText();
		String string2 = this.choicesField.getText();
		InteractiveLootBlockEntity.Mode var = this.lootBlockMode;
		this.init(client, width, height);
		this.lootTableIdentifierStringField.setText(string);
		this.rollsField.setText(string1);
		this.choicesField.setText(string2);
		this.lootBlockMode = var;
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

	private void updateInteractiveLootBlock() {
		ClientPlayNetworking.send(new UpdateInteractiveLootBlockPacket(
				this.interactiveLootBlockEntity.getPos(),
				this.lootTableIdentifierStringField.getText(),
				this.lootBlockMode.asString(),
				ItemUtils.parseInt(this.rollsField.getText()),
				ItemUtils.parseInt(this.choicesField.getText())
		));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawTextWithShadow(this.textRenderer, LOOT_TABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 46, 0xA0A0A0);
		} else {
			context.drawTextWithShadow(this.textRenderer, LOOTABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 46, 0xA0A0A0);
		}
		this.lootTableIdentifierStringField.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode != InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawTextWithShadow(this.textRenderer, ROLLS_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.rollsField.render(context, mouseX, mouseY, delta);
		}

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.CHOICE) {
			context.drawTextWithShadow(this.textRenderer, CHOICES_LABEL_TEXT, this.width / 2 + 5, 105, 0xA0A0A0);
			this.choicesField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
