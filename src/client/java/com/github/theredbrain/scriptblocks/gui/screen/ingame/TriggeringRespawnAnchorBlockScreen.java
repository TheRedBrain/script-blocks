package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeringRespawnAnchorBlockPacket;
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
import net.minecraft.util.math.BlockPos;

@Environment(value = EnvType.CLIENT)
public class TriggeringRespawnAnchorBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.spawn_position_offset_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.spawn_orientation_pitch_label");
	private static final Text ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.spawn_orientation_yaw_label");
	private static final Text TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Text SEND_MESSAGE_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.send_message_label");
	private static final Text TOGGLE_SEND_MESSAGE_BUTTON_TRUE_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.toggle_send_message_button_label.true");
	private static final Text TOGGLE_SEND_MESSAGE_BUTTON_FALSE_LABEL_TEXT = Text.translatable("gui.triggering_respawn_anchor_block.toggle_send_message_button_label.false");

	private final TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity;

	private TextFieldWidget entitySpawnPositionOffsetXField;
	private TextFieldWidget entitySpawnPositionOffsetYField;
	private TextFieldWidget entitySpawnPositionOffsetZField;
	private TextFieldWidget entitySpawnOrientationPitchField;
	private TextFieldWidget entitySpawnOrientationYawField;

	private TextFieldWidget triggeredBlockPositionOffsetXField;
	private TextFieldWidget triggeredBlockPositionOffsetYField;
	private TextFieldWidget triggeredBlockPositionOffsetZField;
	private CyclingButtonWidget<Boolean> toggleTriggeredBlockResetsButton;
	private boolean triggeredBlockResets;

	private CyclingButtonWidget<Boolean> toggleSendMessageButton;
	private boolean sendMessage;

	public TriggeringRespawnAnchorBlockScreen(TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
		super(NarratorManager.EMPTY);
		this.triggeringRespawnAnchorBlockEntity = triggeringRespawnAnchorBlockEntity;
	}

	@Override
	protected void init() {

		this.entitySpawnPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 50, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetXField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getX()));
		this.addSelectableChild(this.entitySpawnPositionOffsetXField);
		this.entitySpawnPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 50, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetYField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getY()));
		this.addSelectableChild(this.entitySpawnPositionOffsetYField);
		this.entitySpawnPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 50, 100, 20, Text.empty());
		this.entitySpawnPositionOffsetZField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getLeft().getZ()));
		this.addSelectableChild(this.entitySpawnPositionOffsetZField);

		this.entitySpawnOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 85, 150, 20, Text.empty());
		this.entitySpawnOrientationYawField.setText(Double.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getRight().getLeft()));
		this.addSelectableChild(this.entitySpawnOrientationYawField);
		this.entitySpawnOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 85, 150, 20, Text.empty());
		this.entitySpawnOrientationPitchField.setText(Double.toString(this.triggeringRespawnAnchorBlockEntity.getSpawnPoint().getRight().getRight()));
		this.addSelectableChild(this.entitySpawnOrientationPitchField);

		this.triggeredBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 130, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getX()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 130, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getY()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 130, 50, 20, Text.empty());
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setText(Integer.toString(this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getLeft().getZ()));
		this.addSelectableChild(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.triggeringRespawnAnchorBlockEntity.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Text.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 130, 150, 20, Text.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.sendMessage = this.triggeringRespawnAnchorBlockEntity.sendMessage();
		int i = this.textRenderer.getWidth(SEND_MESSAGE_LABEL_TEXT) + 10;
		this.toggleSendMessageButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_SEND_MESSAGE_BUTTON_TRUE_LABEL_TEXT, TOGGLE_SEND_MESSAGE_BUTTON_FALSE_LABEL_TEXT).initially(this.sendMessage).omitKeyText().build(this.width / 2 - 152 + i, 164, 300 - i, 20, Text.empty(), (button, sendMessage) -> {
			this.sendMessage = sendMessage;
		}));

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 200, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 200, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.entitySpawnPositionOffsetXField);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.entitySpawnPositionOffsetXField.getText();
		String string1 = this.entitySpawnPositionOffsetYField.getText();
		String string2 = this.entitySpawnPositionOffsetZField.getText();
		String string3 = this.entitySpawnOrientationPitchField.getText();
		String string4 = this.entitySpawnOrientationYawField.getText();
		String string5 = this.triggeredBlockPositionOffsetXField.getText();
		String string6 = this.triggeredBlockPositionOffsetYField.getText();
		String string7 = this.triggeredBlockPositionOffsetZField.getText();
		boolean bl = this.triggeredBlockResets;
		boolean bl1 = this.sendMessage;
		this.init(client, width, height);
		this.entitySpawnPositionOffsetXField.setText(string);
		this.entitySpawnPositionOffsetYField.setText(string1);
		this.entitySpawnPositionOffsetZField.setText(string2);
		this.entitySpawnOrientationPitchField.setText(string3);
		this.entitySpawnOrientationYawField.setText(string4);
		this.triggeredBlockPositionOffsetXField.setText(string5);
		this.triggeredBlockPositionOffsetYField.setText(string6);
		this.triggeredBlockPositionOffsetZField.setText(string7);
		this.triggeredBlockResets = bl;
		this.toggleTriggeredBlockResetsButton.setValue(this.triggeredBlockResets);
		this.sendMessage = bl1;
		this.toggleSendMessageButton.setValue(this.sendMessage);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 40, 0xA0A0A0);
		this.entitySpawnPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.entitySpawnPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.entitySpawnPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_YAW_LABEL_TEXT, this.width / 2 - 153, 75, 0xA0A0A0);
		this.entitySpawnOrientationYawField.render(context, mouseX, mouseY, delta);
		context.drawTextWithShadow(this.textRenderer, ENTITY_SPAWN_ORIENTATION_PITCH_LABEL_TEXT, this.width / 2 + 5, 75, 0xA0A0A0);
		this.entitySpawnOrientationPitchField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, TRIGGERED_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 120, 0xA0A0A0);
		this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, SEND_MESSAGE_LABEL_TEXT, this.width / 2 - 152, 170, 0xA0A0A0);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(
				new UpdateTriggeringRespawnAnchorBlockPacket(
						this.triggeringRespawnAnchorBlockEntity.getPos(),
						new BlockPos(
								ItemUtils.parseInt(this.entitySpawnPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.entitySpawnPositionOffsetZField.getText())
						),
						ItemUtils.parseDouble(this.entitySpawnOrientationYawField.getText()),
						ItemUtils.parseDouble(this.entitySpawnOrientationPitchField.getText()),
						new BlockPos(
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getText()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getText()),
								ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getText())
						),
						this.triggeredBlockResets,
						this.sendMessage
				)
		);
		return true;
	}

}
