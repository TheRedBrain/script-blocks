package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListMessagingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUUIDListMessagingBlockPacket;
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
public class UUIDListMessagingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component MESSAGE_LOCALISATION_STRING_LABEL_TEXT = Component.translatable("gui.uuid_list_messaging_block.message_localisation_string_label");
	private static final Component SUBTITLE_LOCALISATION_STRING_LABEL_TEXT = Component.translatable("gui.uuid_list_messaging_block.subtitle_localisation_string_label");
	private static final Component TITLE_FADE_IN_LABEL_TEXT = Component.translatable("gui.uuid_list_messaging_block.title_fade_in_label");
	private static final Component TITLE_STAY_LABEL_TEXT = Component.translatable("gui.uuid_list_messaging_block.title_stay_label");
	private static final Component TITLE_FADE_OUT_LABEL_TEXT = Component.translatable("gui.uuid_list_messaging_block.title_fade_out_label");
	private static final Component DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Component DATA_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.data_provider_block.data_identifier");

	private final UUIDListMessagingBlockEntity uuidListMessagingBlock;

	private CycleButton<UUIDListMessagingBlockEntity.MessageMode> cycleMessageModeButton;
	private UUIDListMessagingBlockEntity.MessageMode messageMode;

	private EditBox messageLocalisationStringField;
	private EditBox subTitleLocalisationStringField;
	private EditBox titleFadeInField;
	private EditBox titleStayField;
	private EditBox titleFadeOutField;

	private EditBox dataProvidingBlockPositionOffsetXField;
	private EditBox dataProvidingBlockPositionOffsetYField;
	private EditBox dataProvidingBlockPositionOffsetZField;
	private EditBox dataIdentifierField;

	public UUIDListMessagingBlockScreen(UUIDListMessagingBlockEntity uuidListMessagingBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.uuidListMessagingBlock = uuidListMessagingBlockEntity;
	}

	@Override
	protected void init() {

		super.init();

		this.messageMode = this.uuidListMessagingBlock.getMessageMode();
		this.cycleMessageModeButton = this.addRenderableWidget(CycleButton.builder(UUIDListMessagingBlockEntity.MessageMode::asText).values(UUIDListMessagingBlockEntity.MessageMode.values()).initially(this.messageMode).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, messageMode) -> {
			this.messageMode = messageMode;
			this.updateWidgets();
		}));

		this.messageLocalisationStringField = new EditBox(this.font, this.width / 2 - 154, 55, 300, 20, Component.empty());
		this.messageLocalisationStringField.setMaxLength(128);
		this.messageLocalisationStringField.setValue(this.uuidListMessagingBlock.getMessageLocalisationString());
		this.addWidget(this.messageLocalisationStringField);

		this.subTitleLocalisationStringField = new EditBox(this.font, this.width / 2 - 154, 90, 300, 20, Component.empty());
		this.subTitleLocalisationStringField.setMaxLength(128);
		this.subTitleLocalisationStringField.setValue(this.uuidListMessagingBlock.getSubTitleLocalisationString());
		this.addWidget(this.subTitleLocalisationStringField);

		this.titleFadeInField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.titleFadeInField.setMaxLength(128);
		this.titleFadeInField.setValue(Integer.toString(this.uuidListMessagingBlock.getTitleFadeIn()));
		this.addWidget(this.titleFadeInField);

		this.titleStayField = new EditBox(this.font, this.width / 2 - 50, 125, 100, 20, Component.empty());
		this.titleStayField.setMaxLength(128);
		this.titleStayField.setValue(Integer.toString(this.uuidListMessagingBlock.getTitleStay()));
		this.addWidget(this.titleStayField);

		this.titleFadeOutField = new EditBox(this.font, this.width / 2 + 54, 125, 100, 20, Component.empty());
		this.titleFadeOutField.setMaxLength(128);
		this.titleFadeOutField.setValue(Integer.toString(this.uuidListMessagingBlock.getTitleFadeOut()));
		this.addWidget(this.titleFadeOutField);

		this.dataProvidingBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 160, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setValue(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 160, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setValue(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 160, 100, 20, Component.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setValue(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new EditBox(this.font, this.width / 2 - 154, 195, 300, 20, Component.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setValue(this.uuidListMessagingBlock.getDataIdentifier());
		this.addWidget(this.dataIdentifierField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 154, 220, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 220, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleMessageModeButton);
	}

	@Override
	protected void updateWidgets() {

		this.subTitleLocalisationStringField.setVisible(false);
		this.titleFadeInField.setVisible(false);
		this.titleStayField.setVisible(false);
		this.titleFadeOutField.setVisible(false);

		if (this.messageMode == UUIDListMessagingBlockEntity.MessageMode.TITLE) {

			this.subTitleLocalisationStringField.setVisible(true);
			this.titleFadeInField.setVisible(true);
			this.titleStayField.setVisible(true);
			this.titleFadeOutField.setVisible(true);

		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		UUIDListMessagingBlockEntity.MessageMode var = this.messageMode;
		String string = this.messageLocalisationStringField.getValue();
		String string1 = this.subTitleLocalisationStringField.getValue();
		String string2 = this.titleFadeInField.getValue();
		String string3 = this.titleStayField.getValue();
		String string4 = this.titleFadeOutField.getValue();
		String string5 = this.dataProvidingBlockPositionOffsetXField.getValue();
		String string6 = this.dataProvidingBlockPositionOffsetYField.getValue();
		String string7 = this.dataProvidingBlockPositionOffsetZField.getValue();
		String string8 = this.dataIdentifierField.getValue();
		this.init(client, width, height);
		this.messageMode = var;
		this.cycleMessageModeButton.setValue(this.messageMode);
		this.messageLocalisationStringField.setValue(string);
		this.subTitleLocalisationStringField.setValue(string1);
		this.titleFadeInField.setValue(string2);
		this.titleStayField.setValue(string3);
		this.titleFadeOutField.setValue(string4);
		this.dataProvidingBlockPositionOffsetXField.setValue(string5);
		this.dataProvidingBlockPositionOffsetYField.setValue(string6);
		this.dataProvidingBlockPositionOffsetZField.setValue(string7);
		this.dataIdentifierField.setValue(string8);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, MESSAGE_LOCALISATION_STRING_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.messageLocalisationStringField.render(context, mouseX, mouseY, delta);

		if (this.messageMode == UUIDListMessagingBlockEntity.MessageMode.TITLE) {

			context.drawString(this.font, SUBTITLE_LOCALISATION_STRING_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.subTitleLocalisationStringField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TITLE_FADE_IN_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.titleFadeInField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TITLE_STAY_LABEL_TEXT, this.width / 2 - 49, 115, 0xA0A0A0);
			this.titleStayField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, TITLE_FADE_OUT_LABEL_TEXT, this.width / 2 + 55, 115, 0xA0A0A0);
			this.titleFadeOutField.render(context, mouseX, mouseY, delta);

		}

		context.drawString(this.font, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawString(this.font, DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateUUIDListMessagingBlockPacket(
				this.uuidListMessagingBlock.getPos(),
				this.messageMode.asString(),
				this.messageLocalisationStringField.getValue(),
				this.subTitleLocalisationStringField.getValue(),
				ItemUtils.parseInt(this.titleFadeInField.getValue()),
				ItemUtils.parseInt(this.titleStayField.getValue()),
				ItemUtils.parseInt(this.titleFadeOutField.getValue()),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getValue())
				),
				this.dataIdentifierField.getValue()
		));
		return true;
	}
}
