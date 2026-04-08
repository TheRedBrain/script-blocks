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

@Deprecated
@Environment(value = EnvType.CLIENT)
public class InteractiveLootBlockScreen extends Screen {
	private static final Text LOOT_TABLE_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.loot_table_identifier_label");
	private static final Text LOOTABLE_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.lootable_identifier_label");
	private static final Text ROLLS_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.rolls_label");
	private static final Text CHOICES_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.choices_label");
	private static final Text TRACK_PLAYERS_LABEL_TEXT = Text.translatable("gui.interactive_loot_block.track_players_label");
	private static final Text LOOT_ACQUIRED_MESSAGE_PLACEHOLDER_TEXT = Text.translatable("gui.interactive_loot_block.loot_acquired_message_placeholder");
	private static final Text LOOT_ACQUIRED_SOUND_ID_PLACEHOLDER_TEXT = Text.translatable("gui.interactive_loot_block.loot_acquired_sound_id_placeholder");
	private static final Text ALREADY_LOOTED_MESSAGE_PLACEHOLDER_TEXT = Text.translatable("gui.interactive_loot_block.already_looted_message_placeholder");
	private static final Text ALREADY_LOOTED_SOUND_ID_PLACEHOLDER_TEXT = Text.translatable("gui.interactive_loot_block.already_looted_sound_id_placeholder");
	private final InteractiveLootBlockEntity interactiveLootBlockEntity;
	private TextFieldWidget lootTableIdentifierStringField;
	private InteractiveLootBlockEntity.Mode lootBlockMode;
	private CyclingButtonWidget<InteractiveLootBlockEntity.Mode> lootBlockModeCycleButton;
	private TextFieldWidget rollsField;
	private TextFieldWidget choicesField;
	private boolean trackPlayers;
	private CyclingButtonWidget<Boolean> trackPlayersCycleButton;
	private TextFieldWidget lootAcquiredMessageField;
	private TextFieldWidget lootAcquiredSoundIdField;
	private TextFieldWidget alreadyLootedMessageField;
	private TextFieldWidget alreadyLootedSoundIdField;

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
		this.lootTableIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 30, 308, 20, Text.translatable(""));
		this.lootTableIdentifierStringField.setMaxLength(128);
		this.lootTableIdentifierStringField.setText(this.interactiveLootBlockEntity.getLootTableIdentifierString());
		this.addSelectableChild(this.lootTableIdentifierStringField);

		this.lootBlockMode = this.interactiveLootBlockEntity.getMode();

		this.lootBlockModeCycleButton = this.addDrawableChild(CyclingButtonWidget.builder(InteractiveLootBlockEntity.Mode::asText).values((InteractiveLootBlockEntity.Mode[]) InteractiveLootBlockEntity.Mode.values()).initially(this.lootBlockMode).omitKeyText().build(this.width / 2 - 154, 54, 308, 20, Text.empty(), (button, lootBlockMode) -> {
			this.lootBlockMode = lootBlockMode;
			this.updateWidgets();
		}));

		this.rollsField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 89, 100, 20, Text.empty());
		this.rollsField.setMaxLength(128);
		this.rollsField.setText(Integer.toString(this.interactiveLootBlockEntity.getRolls()));
		this.addSelectableChild(this.rollsField);

		this.choicesField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 89, 100, 20, Text.empty());
		this.choicesField.setMaxLength(128);
		this.choicesField.setText(Integer.toString(this.interactiveLootBlockEntity.getChoices()));
		this.addSelectableChild(this.choicesField);

		this.trackPlayers = this.interactiveLootBlockEntity.getTrackPlayers();

		this.trackPlayersCycleButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder().initially(this.trackPlayers).omitKeyText().build(this.width / 2 + 54, 89, 100, 20, Text.empty(), (button, trackPlayers) -> {
			this.trackPlayers = trackPlayers;
		}));

		this.lootAcquiredMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 113, 308, 20, Text.translatable(""));
		this.lootAcquiredMessageField.setMaxLength(128);
		this.lootAcquiredMessageField.setText(this.interactiveLootBlockEntity.getLootAcquiredMessage());
		this.lootAcquiredMessageField.setPlaceholder(LOOT_ACQUIRED_MESSAGE_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.lootAcquiredMessageField);

		this.lootAcquiredSoundIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 137, 308, 20, Text.translatable(""));
		this.lootAcquiredSoundIdField.setMaxLength(128);
		this.lootAcquiredSoundIdField.setText(this.interactiveLootBlockEntity.getLootAcquiredSoundId());
		this.lootAcquiredSoundIdField.setPlaceholder(LOOT_ACQUIRED_SOUND_ID_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.lootAcquiredSoundIdField);

		this.alreadyLootedMessageField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 161, 308, 20, Text.translatable(""));
		this.alreadyLootedMessageField.setMaxLength(128);
		this.alreadyLootedMessageField.setText(this.interactiveLootBlockEntity.getAlreadyLootedMessage());
		this.alreadyLootedMessageField.setPlaceholder(ALREADY_LOOTED_MESSAGE_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.alreadyLootedMessageField);

		this.alreadyLootedSoundIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 185, 308, 20, Text.translatable(""));
		this.alreadyLootedSoundIdField.setMaxLength(128);
		this.alreadyLootedSoundIdField.setText(this.interactiveLootBlockEntity.getAlreadyLootedSoundId());
		this.alreadyLootedSoundIdField.setPlaceholder(ALREADY_LOOTED_SOUND_ID_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.alreadyLootedSoundIdField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 209, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 209, 150, 20).build());
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
		boolean bool = this.trackPlayers;
		String string = this.lootTableIdentifierStringField.getText();
		String string1 = this.rollsField.getText();
		String string2 = this.choicesField.getText();
		String string3 = this.lootAcquiredMessageField.getText();
		String string4 = this.lootAcquiredSoundIdField.getText();
		String string5 = this.alreadyLootedMessageField.getText();
		String string6 = this.alreadyLootedSoundIdField.getText();
		InteractiveLootBlockEntity.Mode var = this.lootBlockMode;
		this.init(client, width, height);
		this.trackPlayers = bool;
		this.lootTableIdentifierStringField.setText(string);
		this.rollsField.setText(string1);
		this.choicesField.setText(string2);
		this.lootAcquiredMessageField.setText(string3);
		this.lootAcquiredSoundIdField.setText(string4);
		this.alreadyLootedMessageField.setText(string5);
		this.alreadyLootedSoundIdField.setText(string6);
		this.lootBlockMode = var;
		this.lootBlockModeCycleButton.setValue(this.lootBlockMode);
		this.trackPlayersCycleButton.setValue(this.trackPlayers);
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
				ItemUtils.parseInt(this.choicesField.getText()),
				this.trackPlayers,
				this.lootAcquiredMessageField.getText(),
				this.lootAcquiredSoundIdField.getText(),
				this.alreadyLootedMessageField.getText(),
				this.alreadyLootedSoundIdField.getText()
		));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawTextWithShadow(this.textRenderer, LOOT_TABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		} else {
			context.drawTextWithShadow(this.textRenderer, LOOTABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		}
		this.lootTableIdentifierStringField.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode != InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawTextWithShadow(this.textRenderer, ROLLS_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
			this.rollsField.render(context, mouseX, mouseY, delta);
		}

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.CHOICE) {
			context.drawTextWithShadow(this.textRenderer, CHOICES_LABEL_TEXT, this.width / 2 - 49, 79, 0xA0A0A0);
			this.choicesField.render(context, mouseX, mouseY, delta);
		}
		context.drawTextWithShadow(this.textRenderer, TRACK_PLAYERS_LABEL_TEXT, this.width / 2 + 55, 79, 0xA0A0A0);
		this.lootAcquiredMessageField.render(context, mouseX, mouseY, delta);
		this.lootAcquiredSoundIdField.render(context, mouseX, mouseY, delta);
		this.alreadyLootedMessageField.render(context, mouseX, mouseY, delta);
		this.alreadyLootedSoundIdField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
