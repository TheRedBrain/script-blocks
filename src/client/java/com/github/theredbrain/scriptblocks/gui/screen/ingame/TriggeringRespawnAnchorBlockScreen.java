package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeringRespawnAnchorBlockPacket;
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

@Environment(value = EnvType.CLIENT)
public class TriggeringRespawnAnchorBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.spawn_position_offset_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.spawn_orientation_pitch_label");
	private static final Component ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.spawn_orientation_yaw_label");
	private static final Component TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component SEND_MESSAGE_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.send_message_label");
	private static final Component TOGGLE_SEND_MESSAGE_BUTTON_TRUE_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.toggle_send_message_button_label.true");
	private static final Component TOGGLE_SEND_MESSAGE_BUTTON_FALSE_LABEL_TEXT = Component.translatable("gui.triggering_respawn_anchor_block.toggle_send_message_button_label.false");

	private final TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity;

	private EditBox entitySpawnPositionOffsetXField;
	private EditBox entitySpawnPositionOffsetYField;
	private EditBox entitySpawnPositionOffsetZField;
	private EditBox entitySpawnOrientationPitchField;
	private EditBox entitySpawnOrientationYawField;

	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	private CycleButton<Boolean> toggleSendMessageButton;
	private boolean sendMessage;

	public TriggeringRespawnAnchorBlockScreen(TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.triggeringRespawnAnchorBlockEntity = triggeringRespawnAnchorBlockEntity;
	}

	@Override
	protected void init() {

		this.entitySpawnPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 50, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetXField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getX()));
		this.addWidget(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 50, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetYField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getY()));
		this.addWidget(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 50, 100, 20, Component.empty());
		this.entitySpawnPositionOffsetZField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getZ()));
		this.addWidget(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationYawField = new EditBox(this.font, this.width / 2 - 154, 85, 150, 20, Component.empty());
		this.entitySpawnOrientationYawField.setValue(Double.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getRight().getLeft()));
		this.addWidget(this.entitySpawnOrientationYawField);
		this.entitySpawnOrientationPitchField = new EditBox(this.font, this.width / 2 + 4, 85, 150, 20, Component.empty());
		this.entitySpawnOrientationPitchField.setValue(Double.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getRight().getRight()));
		this.addWidget(this.entitySpawnOrientationPitchField);

		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 130, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 130, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 130, 50, 20, Component.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 130, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.sendMessage = this.triggeringRespawnAnchorBlockEntity.sendMessage();
		int i = this.font.width(SEND_MESSAGE_LABEL_TEXT) + 10;
		this.toggleSendMessageButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_SEND_MESSAGE_BUTTON_TRUE_LABEL_TEXT, TOGGLE_SEND_MESSAGE_BUTTON_FALSE_LABEL_TEXT).initially(this.sendMessage).omitKeyText().build(this.width / 2 - 152 + i, 164, 300 - i, 20, Component.empty(), (button, sendMessage) -> {
			this.sendMessage = sendMessage;
		}));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 200, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 200, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.entitySpawnPositionOffsetXField);
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.entitySpawnPositionOffsetXField.getValue();
		String string1 = this.entitySpawnPositionOffsetYField.getValue();
		String string2 = this.entitySpawnPositionOffsetZField.getValue();
		String string3 = this.entitySpawnOrientationPitchField.getValue();
		String string4 = this.entitySpawnOrientationYawField.getValue();
		String string5 = this.triggeredBlockPositionOffsetXField.getValue();
		String string6 = this.triggeredBlockPositionOffsetYField.getValue();
		String string7 = this.triggeredBlockPositionOffsetZField.getValue();
		boolean bl = this.triggeredBlockResets;
		boolean bl1 = this.sendMessage;
		this.init(client, width, height);
		this.entitySpawnPositionOffsetXField.setValue(string);
		this.entitySpawnPositionOffsetYField.setValue(string1);
		this.entitySpawnPositionOffsetZField.setValue(string2);
		this.entitySpawnOrientationPitchField.setValue(string3);
		this.entitySpawnOrientationYawField.setValue(string4);
		this.triggeredBlockPositionOffsetXField.setValue(string5);
		this.triggeredBlockPositionOffsetYField.setValue(string6);
		this.triggeredBlockPositionOffsetZField.setValue(string7);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.sendMessage = bl1;
		this.toggleSendMessageButton.setValue(this.sendMessage);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 40, 0xA0A0A0);
		this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 - 153, 75, 0xA0A0A0);
		this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);
		context.drawString(this.font, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 + 5, 75, 0xA0A0A0);
		this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, SEND_MESSAGE_LABEL_TEXT, this.width / 2 - 152, 170, 0xA0A0A0);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(
				new UpdateTriggeringRespawnAnchorBlockPacket(
						this.triggeringRespawnAnchorBlockEntity.getPos(),
						new BlockPos(
								ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getValue())
						),
						ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getValue()),
						ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getValue()),
						new BlockPos(
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
						),
						this.triggeredBlockResets,
						this.sendMessage
				)
		);
		return true;
	}

}
