package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListMessagingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUUIDListMessagingBlockPacket;
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
public class UUIDListMessagingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Text MESSAGE_LOCALISATION_STRING_LABEL_TEXT = Text.translatable("gui.uuid_list_messaging_block.message_localisation_string_label");
	private static final Text SUBTITLE_LOCALISATION_STRING_LABEL_TEXT = Text.translatable("gui.uuid_list_messaging_block.subtitle_localisation_string_label");
	private static final Text TITLE_FADE_IN_LABEL_TEXT = Text.translatable("gui.uuid_list_messaging_block.title_fade_in_label");
	private static final Text TITLE_STAY_LABEL_TEXT = Text.translatable("gui.uuid_list_messaging_block.title_stay_label");
	private static final Text TITLE_FADE_OUT_LABEL_TEXT = Text.translatable("gui.uuid_list_messaging_block.title_fade_out_label");
	private static final Text DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.data_providing_block_position_offset");
	private static final Text DATA_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.data_provider_block.data_identifier");

	private final UUIDListMessagingBlockEntity uuidListMessagingBlock;

	private CyclingButtonWidget<UUIDListMessagingBlockEntity.MessageMode> cycleMessageModeButton;
	private UUIDListMessagingBlockEntity.MessageMode messageMode;

	private TextFieldWidget messageLocalisationStringField;
	private TextFieldWidget subTitleLocalisationStringField;
	private TextFieldWidget titleFadeInField;
	private TextFieldWidget titleStayField;
	private TextFieldWidget titleFadeOutField;

	private TextFieldWidget dataProvidingBlockPositionOffsetXField;
	private TextFieldWidget dataProvidingBlockPositionOffsetYField;
	private TextFieldWidget dataProvidingBlockPositionOffsetZField;
	private TextFieldWidget dataIdentifierField;

	public UUIDListMessagingBlockScreen(UUIDListMessagingBlockEntity uuidListMessagingBlockEntity) {
		super(NarratorManager.EMPTY);
		this.uuidListMessagingBlock = uuidListMessagingBlockEntity;
	}

	@Override
	protected void init() {

		super.init();

		this.messageMode = this.uuidListMessagingBlock.getMessageMode();
		this.cycleMessageModeButton = this.addDrawableChild(CyclingButtonWidget.builder(UUIDListMessagingBlockEntity.MessageMode::asText).values(UUIDListMessagingBlockEntity.MessageMode.values()).initially(this.messageMode).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, messageMode) -> {
			this.messageMode = messageMode;
			this.updateWidgets();
		}));

		this.messageLocalisationStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 55, 300, 20, Text.empty());
		this.messageLocalisationStringField.setMaxLength(128);
		this.messageLocalisationStringField.setText(this.uuidListMessagingBlock.getMessageLocalisationString());
		this.addSelectableChild(this.messageLocalisationStringField);

		this.subTitleLocalisationStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 90, 300, 20, Text.empty());
		this.subTitleLocalisationStringField.setMaxLength(128);
		this.subTitleLocalisationStringField.setText(this.uuidListMessagingBlock.getSubTitleLocalisationString());
		this.addSelectableChild(this.subTitleLocalisationStringField);

		this.titleFadeInField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 125, 100, 20, Text.empty());
		this.titleFadeInField.setMaxLength(128);
		this.titleFadeInField.setText(Integer.toString(this.uuidListMessagingBlock.getTitleFadeIn()));
		this.addSelectableChild(this.titleFadeInField);

		this.titleStayField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 125, 100, 20, Text.empty());
		this.titleStayField.setMaxLength(128);
		this.titleStayField.setText(Integer.toString(this.uuidListMessagingBlock.getTitleStay()));
		this.addSelectableChild(this.titleStayField);

		this.titleFadeOutField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 125, 100, 20, Text.empty());
		this.titleFadeOutField.setMaxLength(128);
		this.titleFadeOutField.setText(Integer.toString(this.uuidListMessagingBlock.getTitleFadeOut()));
		this.addSelectableChild(this.titleFadeOutField);

		this.dataProvidingBlockPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 160, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetXField.setText(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getX()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetXField);
		this.dataProvidingBlockPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 160, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetYField.setText(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getY()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetYField);
		this.dataProvidingBlockPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 160, 100, 20, Text.empty());
		this.dataProvidingBlockPositionOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPositionOffsetZField.setText(Integer.toString(this.uuidListMessagingBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addSelectableChild(this.dataProvidingBlockPositionOffsetZField);

		this.dataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 195, 300, 20, Text.empty());
		this.dataIdentifierField.setMaxLength(128);
		this.dataIdentifierField.setText(this.uuidListMessagingBlock.getDataIdentifier());
		this.addSelectableChild(this.dataIdentifierField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 154, 220, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 220, 150, 20).build());
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
	public void resize(MinecraftClient client, int width, int height) {
		UUIDListMessagingBlockEntity.MessageMode var = this.messageMode;
		String string = this.messageLocalisationStringField.getText();
		String string1 = this.subTitleLocalisationStringField.getText();
		String string2 = this.titleFadeInField.getText();
		String string3 = this.titleStayField.getText();
		String string4 = this.titleFadeOutField.getText();
		String string5 = this.dataProvidingBlockPositionOffsetXField.getText();
		String string6 = this.dataProvidingBlockPositionOffsetYField.getText();
		String string7 = this.dataProvidingBlockPositionOffsetZField.getText();
		String string8 = this.dataIdentifierField.getText();
		this.init(client, width, height);
		this.messageMode = var;
		this.cycleMessageModeButton.setValue(this.messageMode);
		this.messageLocalisationStringField.setText(string);
		this.subTitleLocalisationStringField.setText(string1);
		this.titleFadeInField.setText(string2);
		this.titleStayField.setText(string3);
		this.titleFadeOutField.setText(string4);
		this.dataProvidingBlockPositionOffsetXField.setText(string5);
		this.dataProvidingBlockPositionOffsetYField.setText(string6);
		this.dataProvidingBlockPositionOffsetZField.setText(string7);
		this.dataIdentifierField.setText(string8);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, MESSAGE_LOCALISATION_STRING_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
		this.messageLocalisationStringField.render(context, mouseX, mouseY, delta);

		if (this.messageMode == UUIDListMessagingBlockEntity.MessageMode.TITLE) {

			context.drawTextWithShadow(this.textRenderer, SUBTITLE_LOCALISATION_STRING_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.subTitleLocalisationStringField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TITLE_FADE_IN_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.titleFadeInField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TITLE_STAY_LABEL_TEXT, this.width / 2 - 49, 115, 0xA0A0A0);
			this.titleStayField.render(context, mouseX, mouseY, delta);

			context.drawTextWithShadow(this.textRenderer, TITLE_FADE_OUT_LABEL_TEXT, this.width / 2 + 55, 115, 0xA0A0A0);
			this.titleFadeOutField.render(context, mouseX, mouseY, delta);

		}

		context.drawTextWithShadow(this.textRenderer, DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
		this.dataProvidingBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.dataProvidingBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 185, 0xA0A0A0);
		this.dataIdentifierField.render(context, mouseX, mouseY, delta);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateUUIDListMessagingBlockPacket(
				this.uuidListMessagingBlock.getPos(),
				this.messageMode.asString(),
				this.messageLocalisationStringField.getText(),
				this.subTitleLocalisationStringField.getText(),
				ItemUtils.parseInt(this.titleFadeInField.getText()),
				ItemUtils.parseInt(this.titleStayField.getText()),
				ItemUtils.parseInt(this.titleFadeOutField.getText()),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.dataProvidingBlockPositionOffsetZField.getText())
				),
				this.dataIdentifierField.getText()
		));
		return true;
	}
}
