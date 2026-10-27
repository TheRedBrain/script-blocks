package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveLootBlockPacket;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Deprecated
@Environment(value = EnvType.CLIENT)
public class InteractiveLootBlockScreen extends Screen {
	private static final Component LOOT_TABLE_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.interactive_loot_block.loot_table_identifier_label");
	private static final Component LOOTABLE_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.interactive_loot_block.lootable_identifier_label");
	private static final Component ROLLS_LABEL_TEXT = Component.translatable("gui.interactive_loot_block.rolls_label");
	private static final Component CHOICES_LABEL_TEXT = Component.translatable("gui.interactive_loot_block.choices_label");
	private static final Component TRACK_PLAYERS_LABEL_TEXT = Component.translatable("gui.interactive_loot_block.track_players_label");
	private static final Component LOOT_ACQUIRED_MESSAGE_PLACEHOLDER_TEXT = Component.translatable("gui.interactive_loot_block.loot_acquired_message_placeholder");
	private static final Component LOOT_ACQUIRED_SOUND_ID_PLACEHOLDER_TEXT = Component.translatable("gui.interactive_loot_block.loot_acquired_sound_id_placeholder");
	private static final Component ALREADY_LOOTED_MESSAGE_PLACEHOLDER_TEXT = Component.translatable("gui.interactive_loot_block.already_looted_message_placeholder");
	private static final Component ALREADY_LOOTED_SOUND_ID_PLACEHOLDER_TEXT = Component.translatable("gui.interactive_loot_block.already_looted_sound_id_placeholder");
	private final InteractiveLootBlockEntity interactiveLootBlockEntity;
	private EditBox lootTableIdentifierStringField;
	private InteractiveLootBlockEntity.Mode lootBlockMode;
	private CycleButton<InteractiveLootBlockEntity.Mode> lootBlockModeCycleButton;
	private EditBox rollsField;
	private EditBox choicesField;
	private boolean trackPlayers;
	private CycleButton<Boolean> trackPlayersCycleButton;
	private EditBox lootAcquiredMessageField;
	private EditBox lootAcquiredSoundIdField;
	private EditBox alreadyLootedMessageField;
	private EditBox alreadyLootedSoundIdField;

	public InteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.interactiveLootBlockEntity = interactiveLootBlockEntity;
	}

	private void done() {
		this.updateInteractiveLootBlock();
		this.onClose();
	}

	private void cancel() {
		this.onClose();
	}

	@Override
	protected void init() {
		this.lootTableIdentifierStringField = new EditBox(this.font, this.width / 2 - 154, 30, 308, 20, Component.translatable(""));
		this.lootTableIdentifierStringField.setMaxLength(128);
		this.lootTableIdentifierStringField.setValue(this.interactiveLootBlockEntity.getLootTableIdentifierString());
		this.addWidget(this.lootTableIdentifierStringField);

		this.lootBlockMode = this.interactiveLootBlockEntity.getMode();

		this.lootBlockModeCycleButton = this.addRenderableWidget(CycleButton.builder(InteractiveLootBlockEntity.Mode::asText).values((InteractiveLootBlockEntity.Mode[]) InteractiveLootBlockEntity.Mode.values()).initially(this.lootBlockMode).omitKeyText().build(this.width / 2 - 154, 54, 308, 20, Component.empty(), (button, lootBlockMode) -> {
			this.lootBlockMode = lootBlockMode;
			this.updateWidgets();
		}));

		this.rollsField = new EditBox(this.font, this.width / 2 - 154, 89, 100, 20, Component.empty());
		this.rollsField.setMaxLength(128);
		this.rollsField.setValue(Integer.toString(this.interactiveLootBlockEntity.getRolls()));
		this.addWidget(this.rollsField);

		this.choicesField = new EditBox(this.font, this.width / 2 - 50, 89, 100, 20, Component.empty());
		this.choicesField.setMaxLength(128);
		this.choicesField.setValue(Integer.toString(this.interactiveLootBlockEntity.getChoices()));
		this.addWidget(this.choicesField);

		this.trackPlayers = this.interactiveLootBlockEntity.getTrackPlayers();

		this.trackPlayersCycleButton = this.addRenderableWidget(CycleButton.onOffBuilder().initially(this.trackPlayers).omitKeyText().build(this.width / 2 + 54, 89, 100, 20, Component.empty(), (button, trackPlayers) -> {
			this.trackPlayers = trackPlayers;
		}));

		this.lootAcquiredMessageField = new EditBox(this.font, this.width / 2 - 154, 113, 308, 20, Component.translatable(""));
		this.lootAcquiredMessageField.setMaxLength(128);
		this.lootAcquiredMessageField.setValue(this.interactiveLootBlockEntity.getLootAcquiredMessage());
		this.lootAcquiredMessageField.setHint(LOOT_ACQUIRED_MESSAGE_PLACEHOLDER_TEXT);
		this.addWidget(this.lootAcquiredMessageField);

		this.lootAcquiredSoundIdField = new EditBox(this.font, this.width / 2 - 154, 137, 308, 20, Component.translatable(""));
		this.lootAcquiredSoundIdField.setMaxLength(128);
		this.lootAcquiredSoundIdField.setValue(this.interactiveLootBlockEntity.getLootAcquiredSoundId());
		this.lootAcquiredSoundIdField.setHint(LOOT_ACQUIRED_SOUND_ID_PLACEHOLDER_TEXT);
		this.addWidget(this.lootAcquiredSoundIdField);

		this.alreadyLootedMessageField = new EditBox(this.font, this.width / 2 - 154, 161, 308, 20, Component.translatable(""));
		this.alreadyLootedMessageField.setMaxLength(128);
		this.alreadyLootedMessageField.setValue(this.interactiveLootBlockEntity.getAlreadyLootedMessage());
		this.alreadyLootedMessageField.setHint(ALREADY_LOOTED_MESSAGE_PLACEHOLDER_TEXT);
		this.addWidget(this.alreadyLootedMessageField);

		this.alreadyLootedSoundIdField = new EditBox(this.font, this.width / 2 - 154, 185, 308, 20, Component.translatable(""));
		this.alreadyLootedSoundIdField.setMaxLength(128);
		this.alreadyLootedSoundIdField.setValue(this.interactiveLootBlockEntity.getAlreadyLootedSoundId());
		this.alreadyLootedSoundIdField.setHint(ALREADY_LOOTED_SOUND_ID_PLACEHOLDER_TEXT);
		this.addWidget(this.alreadyLootedSoundIdField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 209, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 209, 150, 20).build());
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
	public void resize(Minecraft client, int width, int height) {
		boolean bool = this.trackPlayers;
		String string = this.lootTableIdentifierStringField.getValue();
		String string1 = this.rollsField.getValue();
		String string2 = this.choicesField.getValue();
		String string3 = this.lootAcquiredMessageField.getValue();
		String string4 = this.lootAcquiredSoundIdField.getValue();
		String string5 = this.alreadyLootedMessageField.getValue();
		String string6 = this.alreadyLootedSoundIdField.getValue();
		InteractiveLootBlockEntity.Mode var = this.lootBlockMode;
		this.init(client, width, height);
		this.trackPlayers = bool;
		this.lootTableIdentifierStringField.setValue(string);
		this.rollsField.setValue(string1);
		this.choicesField.setValue(string2);
		this.lootAcquiredMessageField.setValue(string3);
		this.lootAcquiredSoundIdField.setValue(string4);
		this.alreadyLootedMessageField.setValue(string5);
		this.alreadyLootedSoundIdField.setValue(string6);
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
				this.lootTableIdentifierStringField.getValue(),
				this.lootBlockMode.asString(),
				ItemUtils.parseInt(this.rollsField.getValue()),
				ItemUtils.parseInt(this.choicesField.getValue()),
				this.trackPlayers,
				this.lootAcquiredMessageField.getValue(),
				this.lootAcquiredSoundIdField.getValue(),
				this.alreadyLootedMessageField.getValue(),
				this.alreadyLootedSoundIdField.getValue()
		));
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawString(this.font, LOOT_TABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		} else {
			context.drawString(this.font, LOOTABLE_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 20, 0xA0A0A0);
		}
		this.lootTableIdentifierStringField.render(context, mouseX, mouseY, delta);

		if (this.lootBlockMode != InteractiveLootBlockEntity.Mode.VANILLA) {
			context.drawString(this.font, ROLLS_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
			this.rollsField.render(context, mouseX, mouseY, delta);
		}

		if (this.lootBlockMode == InteractiveLootBlockEntity.Mode.CHOICE) {
			context.drawString(this.font, CHOICES_LABEL_TEXT, this.width / 2 - 49, 79, 0xA0A0A0);
			this.choicesField.render(context, mouseX, mouseY, delta);
		}
		context.drawString(this.font, TRACK_PLAYERS_LABEL_TEXT, this.width / 2 + 55, 79, 0xA0A0A0);
		this.lootAcquiredMessageField.render(context, mouseX, mouseY, delta);
		this.lootAcquiredSoundIdField.render(context, mouseX, mouseY, delta);
		this.alreadyLootedMessageField.render(context, mouseX, mouseY, delta);
		this.alreadyLootedSoundIdField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
