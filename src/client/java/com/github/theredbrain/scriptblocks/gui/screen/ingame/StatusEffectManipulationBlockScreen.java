package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateStatusEffectManipulationBlockPacket;
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
import net.minecraft.util.math.BlockPos;

@Environment(value = EnvType.CLIENT)
public class StatusEffectManipulationBlockScreen extends Screen {
	private static final Text MANIPULATION_MODE_LABEL_TEXT = Text.translatable("gui.status_effect_manipulation_block.manipulation_mode_label");
	private static final Text STATUS_EFFECT_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.status_effect_instance.status_effect_identifier_label");
	private static final Text STATUS_EFFECT_AMPLIFIER_LABEL_TEXT = Text.translatable("gui.status_effect_instance.status_effect_amplifier_label");
	private static final Text STATUS_EFFECT_DURATION_LABEL_TEXT = Text.translatable("gui.status_effect_instance.status_effect_duration_label");
	private static final Text IS_AMBIENT_LABEL_TEXT = Text.translatable("gui.status_effect_instance.ambient_false_label");
	private static final Text SHOWS_PARTICLES_LABEL_TEXT = Text.translatable("gui.status_effect_instance.show_particles_label");
	private static final Text SHOWS_ICON_LABEL_TEXT = Text.translatable("gui.status_effect_instance.show_icon_label");
	private static final Text MANIPULATED_EFFECT_TAG_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.status_effect_manipulation_block.manipulated_effect_tag_identifier_label");
	private static final Text AMPLIFIER_MODIFICATION_LABEL_TEXT = Text.translatable("gui.status_effect_manipulation_block.amplifier_modification_label");
	private static final Text UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.status_effect_manipulation_block.uuid_list_handler_position_offset_label");
	private final StatusEffectManipulationBlockEntity statusEffectManipulationBlock;

	private CyclingButtonWidget<StatusEffectManipulationBlockEntity.ManipulationMode> cycleManipulationModeButton;
	private TextFieldWidget effectIdentifierField;
	private TextFieldWidget effectAmplifierField;
	private TextFieldWidget effectDurationField;
	private CyclingButtonWidget<Boolean> toggleEffectIsAmbientButton;
	private CyclingButtonWidget<Boolean> toggleEffectShowsParticlesButton;
	private CyclingButtonWidget<Boolean> toggleEffectShowsIconButton;
	private TextFieldWidget manipulatedEffectTagIdentifierStringField;
	private TextFieldWidget amplifierModificationField;
	private TextFieldWidget uuidListHandlerPositionOffsetXField;
	private TextFieldWidget uuidListHandlerPositionOffsetYField;
	private TextFieldWidget uuidListHandlerPositionOffsetZField;

	private StatusEffectManipulationBlockEntity.ManipulationMode manipulationMode;
	private boolean effectIsAmbient;
	private boolean effectShowsParticles;
	private boolean effectShowsIcon;

	public StatusEffectManipulationBlockScreen(StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
		super(NarratorManager.EMPTY);
		this.statusEffectManipulationBlock = statusEffectManipulationBlockEntity;
	}

	private void done() {
		if (this.updateStatusEffectManipulationBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		int i = this.textRenderer.getWidth(MANIPULATION_MODE_LABEL_TEXT) + 10;
		this.manipulationMode = this.statusEffectManipulationBlock.getManipulationMode();
		this.cycleManipulationModeButton = this.addDrawableChild(CyclingButtonWidget.builder(StatusEffectManipulationBlockEntity.ManipulationMode::asText).values((StatusEffectManipulationBlockEntity.ManipulationMode[]) StatusEffectManipulationBlockEntity.ManipulationMode.values()).initially(this.manipulationMode).omitKeyText().build(this.width / 2 - 152 + i, 20, 300 - i, 20, Text.empty(), (button, triggeredMode) -> {
			this.manipulationMode = triggeredMode;
			this.updateWidgets();
		}));

		this.effectIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 65, 300, 20, Text.empty());
		this.effectIdentifierField.setMaxLength(128);
		this.effectIdentifierField.setText(this.statusEffectManipulationBlock.getEffectIdentifier());
		this.addSelectableChild(this.effectIdentifierField);

		this.effectAmplifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 100, 75, 20, Text.empty());
		this.effectAmplifierField.setText(Integer.toString(this.statusEffectManipulationBlock.getEffectAmplifier()));
		this.addSelectableChild(this.effectAmplifierField);

		this.effectDurationField = new TextFieldWidget(this.textRenderer, this.width / 2 - 75, 100, 75, 20, Text.empty());
		this.effectDurationField.setText(Integer.toString(this.statusEffectManipulationBlock.getEffectDuration()));
		this.addSelectableChild(this.effectDurationField);

		i = this.textRenderer.getWidth(IS_AMBIENT_LABEL_TEXT) + 10;
		this.effectIsAmbient = this.statusEffectManipulationBlock.getEffectIsAmbient();
		this.toggleEffectIsAmbientButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder().initially(this.effectIsAmbient).omitKeyText().build(this.width / 2 + 6 + i, 100, 150 - i, 20, Text.empty(), (button, appliedStatusEffectAmbient) -> {
			this.effectIsAmbient = appliedStatusEffectAmbient;
		}));

		i = this.textRenderer.getWidth(SHOWS_PARTICLES_LABEL_TEXT) + 10;
		this.effectShowsParticles = this.statusEffectManipulationBlock.getEffectShowsParticles();
		this.toggleEffectShowsParticlesButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder().initially(this.effectShowsParticles).omitKeyText().build(this.width / 2 - 152 + i, 124, 150 - i, 20, Text.empty(), (button, appliedStatusEffectShowParticles) -> {
			this.effectShowsParticles = appliedStatusEffectShowParticles;
		}));

		i = this.textRenderer.getWidth(SHOWS_ICON_LABEL_TEXT) + 10;
		this.effectShowsIcon = this.statusEffectManipulationBlock.getEffectShowsIcon();
		this.toggleEffectShowsIconButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder().initially(this.effectShowsIcon).omitKeyText().build(this.width / 2 + 6 + i, 124, 150 - i, 20, Text.empty(), (button, appliedStatusEffectShowIcon) -> {
			this.effectShowsIcon = appliedStatusEffectShowIcon;
		}));

		this.manipulatedEffectTagIdentifierStringField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 65, 300, 20, Text.empty());
		this.manipulatedEffectTagIdentifierStringField.setMaxLength(128);
		this.manipulatedEffectTagIdentifierStringField.setText(this.statusEffectManipulationBlock.getManipulatedEffectTagIdentifierString());
		this.addSelectableChild(this.manipulatedEffectTagIdentifierStringField);

		this.amplifierModificationField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 100, 300, 20, Text.empty());
		this.amplifierModificationField.setMaxLength(128);
		this.amplifierModificationField.setText(Integer.toString(this.statusEffectManipulationBlock.getAmplifierModification()));
		this.addSelectableChild(this.amplifierModificationField);


		this.uuidListHandlerPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 169, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetXField.setText(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getX()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetXField);

		this.uuidListHandlerPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 169, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetYField.setText(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getY()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetYField);

		this.uuidListHandlerPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 169, 100, 20, Text.empty());
		this.uuidListHandlerPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetZField.setText(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getZ()));
		this.addSelectableChild(this.uuidListHandlerPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 207, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleManipulationModeButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.effectIdentifierField.setVisible(false);
		this.effectAmplifierField.setVisible(false);
		this.effectDurationField.setVisible(false);
		this.toggleEffectIsAmbientButton.visible = false;
		this.toggleEffectShowsParticlesButton.visible = false;
		this.toggleEffectShowsIconButton.visible = false;

		this.manipulatedEffectTagIdentifierStringField.setVisible(false);

		this.amplifierModificationField.setVisible(false);

		if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.APPLY) {

			this.effectIdentifierField.setVisible(true);
			this.effectAmplifierField.setVisible(true);
			this.effectDurationField.setVisible(true);

			this.toggleEffectIsAmbientButton.visible = true;
			this.toggleEffectShowsParticlesButton.visible = true;
			this.toggleEffectShowsIconButton.visible = true;

		} else {

			this.manipulatedEffectTagIdentifierStringField.setVisible(true);

			if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.DECREMENT_AMPLIFIER) {

				this.amplifierModificationField.setVisible(true);

			}
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		StatusEffectManipulationBlockEntity.ManipulationMode var2 = this.manipulationMode;
		boolean bool = this.effectIsAmbient;
		boolean bool1 = this.effectShowsParticles;
		boolean bool2 = this.effectShowsIcon;
		String string = this.effectIdentifierField.getText();
		String string1 = this.effectAmplifierField.getText();
		String string2 = this.effectDurationField.getText();
		String string3 = this.manipulatedEffectTagIdentifierStringField.getText();
		String string4 = this.amplifierModificationField.getText();
		this.init(client, width, height);
		this.manipulationMode = var2;
		this.effectIsAmbient = bool;
		this.effectShowsParticles = bool1;
		this.effectShowsIcon = bool2;
		this.effectIdentifierField.setText(string);
		this.effectAmplifierField.setText(string1);
		this.effectDurationField.setText(string2);
		this.manipulatedEffectTagIdentifierStringField.setText(string3);
		this.amplifierModificationField.setText(string4);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(this.textRenderer, MANIPULATION_MODE_LABEL_TEXT, this.width / 2 - 152, 26, 0xA0A0A0);

		if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.APPLY) {

			context.drawTextWithShadow(this.textRenderer, STATUS_EFFECT_IDENTIFIER_LABEL_TEXT, this.width / 2 - 152, 55, 0xA0A0A0);
			this.effectIdentifierField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, STATUS_EFFECT_AMPLIFIER_LABEL_TEXT, this.width / 2 - 152, 90, 0xA0A0A0);
			this.effectAmplifierField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, STATUS_EFFECT_DURATION_LABEL_TEXT, this.width / 2 - 74, 90, 0xA0A0A0);
			this.effectDurationField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, IS_AMBIENT_LABEL_TEXT, this.width / 2 + 6, 106, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, SHOWS_PARTICLES_LABEL_TEXT, this.width / 2 - 152, 130, 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, SHOWS_ICON_LABEL_TEXT, this.width / 2 + 6, 130, 0xA0A0A0);

		} else {

			context.drawTextWithShadow(this.textRenderer, MANIPULATED_EFFECT_TAG_IDENTIFIER_LABEL_TEXT, this.width / 2 - 152, 55, 0xA0A0A0);
			this.manipulatedEffectTagIdentifierStringField.render(context, mouseX, mouseY, delta);

			if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.DECREMENT_AMPLIFIER) {

				context.drawTextWithShadow(this.textRenderer, AMPLIFIER_MODIFICATION_LABEL_TEXT, this.width / 2 - 152, 90, 0xA0A0A0);
				this.amplifierModificationField.render(context, mouseX, mouseY, delta);

			}
		}

		context.drawTextWithShadow(this.textRenderer, UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 152, 159, 0xA0A0A0);
		this.uuidListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.uuidListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateStatusEffectManipulationBlock() {
		ClientPlayNetworking.send(new UpdateStatusEffectManipulationBlockPacket(
				this.statusEffectManipulationBlock.getPos(),
				this.manipulationMode.asString(),
				this.effectIdentifierField.getText(),
				ItemUtils.parseInt(this.effectAmplifierField.getText()),
				ItemUtils.parseInt(this.effectDurationField.getText()),
				this.effectIsAmbient,
				this.effectShowsParticles,
				this.effectShowsIcon,
				this.manipulatedEffectTagIdentifierStringField.getText(),
				ItemUtils.parseInt(this.amplifierModificationField.getText()),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetZField.getText())
				)
		));
		return true;
	}
}
