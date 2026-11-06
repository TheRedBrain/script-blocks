package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateStatusEffectManipulationBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(value = EnvType.CLIENT)
public class StatusEffectManipulationBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component MANIPULATION_MODE_LABEL_TEXT = Component.translatable("gui.status_effect_manipulation_block.manipulation_mode_label");
	private static final Component STATUS_EFFECT_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.status_effect_instance.status_effect_identifier_label");
	private static final Component STATUS_EFFECT_AMPLIFIER_LABEL_TEXT = Component.translatable("gui.status_effect_instance.status_effect_amplifier_label");
	private static final Component STATUS_EFFECT_DURATION_LABEL_TEXT = Component.translatable("gui.status_effect_instance.status_effect_duration_label");
	private static final Component IS_AMBIENT_LABEL_TEXT = Component.translatable("gui.status_effect_instance.ambient_false_label");
	private static final Component SHOWS_PARTICLES_LABEL_TEXT = Component.translatable("gui.status_effect_instance.show_particles_label");
	private static final Component SHOWS_ICON_LABEL_TEXT = Component.translatable("gui.status_effect_instance.show_icon_label");
	private static final Component MANIPULATED_EFFECT_TAG_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.status_effect_manipulation_block.manipulated_effect_tag_identifier_label");
	private static final Component AMPLIFIER_MODIFICATION_LABEL_TEXT = Component.translatable("gui.status_effect_manipulation_block.amplifier_modification_label");
	private static final Component UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.status_effect_manipulation_block.uuid_list_handler_position_offset_label");
	private final StatusEffectManipulationBlockEntity statusEffectManipulationBlock;

	private CycleButton<StatusEffectManipulationBlockEntity.ManipulationMode> cycleManipulationModeButton;
	private EditBox effectIdentifierField;
	private EditBox effectAmplifierField;
	private EditBox effectDurationField;
	private CycleButton<Boolean> toggleEffectIsAmbientButton;
	private CycleButton<Boolean> toggleEffectShowsParticlesButton;
	private CycleButton<Boolean> toggleEffectShowsIconButton;
	private EditBox manipulatedEffectTagIdentifierStringField;
	private EditBox amplifierModificationField;
	private EditBox uuidListHandlerPositionOffsetXField;
	private EditBox uuidListHandlerPositionOffsetYField;
	private EditBox uuidListHandlerPositionOffsetZField;

	private StatusEffectManipulationBlockEntity.ManipulationMode manipulationMode;
	private boolean effectIsAmbient;
	private boolean effectShowsParticles;
	private boolean effectShowsIcon;

	public StatusEffectManipulationBlockScreen(StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.statusEffectManipulationBlock = statusEffectManipulationBlockEntity;
	}

	@Override
	protected void init() {

		int i = this.font.width(MANIPULATION_MODE_LABEL_TEXT) + 10;
		this.manipulationMode = this.statusEffectManipulationBlock.getManipulationMode();
		this.cycleManipulationModeButton = this.addRenderableWidget(CycleButton.builder(StatusEffectManipulationBlockEntity.ManipulationMode::asText, this.manipulationMode)
				.withValues(StatusEffectManipulationBlockEntity.ManipulationMode.values())
				.displayOnlyValue()
				.create(this.width / 2 - 152 + i, 20, 300 - i, 20, Component.empty(), (button, manipulationMode) -> {
			this.manipulationMode = manipulationMode;
			this.updateWidgets();
		}));

		this.effectIdentifierField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.effectIdentifierField.setMaxLength(128);
		this.effectIdentifierField.setValue(this.statusEffectManipulationBlock.getEffectIdentifier());
		this.addWidget(this.effectIdentifierField);

		this.effectAmplifierField = new EditBox(this.font, this.width / 2 - 154, 100, 75, 20, Component.empty());
		this.effectAmplifierField.setValue(Integer.toString(this.statusEffectManipulationBlock.getEffectAmplifier()));
		this.addWidget(this.effectAmplifierField);

		this.effectDurationField = new EditBox(this.font, this.width / 2 - 75, 100, 75, 20, Component.empty());
		this.effectDurationField.setValue(Integer.toString(this.statusEffectManipulationBlock.getEffectDuration()));
		this.addWidget(this.effectDurationField);

		i = this.font.width(IS_AMBIENT_LABEL_TEXT) + 10;
		this.effectIsAmbient = this.statusEffectManipulationBlock.getEffectIsAmbient();
		this.toggleEffectIsAmbientButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.effectIsAmbient)
				.displayOnlyValue()
				.create(this.width / 2 + 6 + i, 100, 150 - i, 20, Component.empty(), (button, appliedStatusEffectAmbient) -> {
			this.effectIsAmbient = appliedStatusEffectAmbient;
		}));

		i = this.font.width(SHOWS_PARTICLES_LABEL_TEXT) + 10;
		this.effectShowsParticles = this.statusEffectManipulationBlock.getEffectShowsParticles();
		this.toggleEffectShowsParticlesButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.effectShowsParticles)
				.displayOnlyValue()
				.create(this.width / 2 - 152 + i, 124, 150 - i, 20, Component.empty(), (button, appliedStatusEffectShowParticles) -> {
			this.effectShowsParticles = appliedStatusEffectShowParticles;
		}));

		i = this.font.width(SHOWS_ICON_LABEL_TEXT) + 10;
		this.effectShowsIcon = this.statusEffectManipulationBlock.getEffectShowsIcon();
		this.toggleEffectShowsIconButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.effectShowsIcon)
				.displayOnlyValue()
				.create(this.width / 2 + 6 + i, 124, 150 - i, 20, Component.empty(), (button, appliedStatusEffectShowIcon) -> {
			this.effectShowsIcon = appliedStatusEffectShowIcon;
		}));

		this.manipulatedEffectTagIdentifierStringField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.manipulatedEffectTagIdentifierStringField.setMaxLength(128);
		this.manipulatedEffectTagIdentifierStringField.setValue(this.statusEffectManipulationBlock.getManipulatedEffectTagIdentifierString());
		this.addWidget(this.manipulatedEffectTagIdentifierStringField);

		this.amplifierModificationField = new EditBox(this.font, this.width / 2 - 154, 100, 300, 20, Component.empty());
		this.amplifierModificationField.setMaxLength(128);
		this.amplifierModificationField.setValue(Integer.toString(this.statusEffectManipulationBlock.getAmplifierModification()));
		this.addWidget(this.amplifierModificationField);


		this.uuidListHandlerPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 169, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetXField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetXField.setValue(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getX()));
		this.addWidget(this.uuidListHandlerPositionOffsetXField);

		this.uuidListHandlerPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 169, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetYField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetYField.setValue(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getY()));
		this.addWidget(this.uuidListHandlerPositionOffsetYField);

		this.uuidListHandlerPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 169, 100, 20, Component.empty());
		this.uuidListHandlerPositionOffsetZField.setMaxLength(128);
		this.uuidListHandlerPositionOffsetZField.setValue(Integer.toString(this.statusEffectManipulationBlock.getUuidListHandlerPositionOffset().getZ()));
		this.addWidget(this.uuidListHandlerPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 207, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleManipulationModeButton);
	}

	@Override
	protected void updateWidgets() {

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
	public void resize(final int width, final int height) {
		StatusEffectManipulationBlockEntity.ManipulationMode var2 = this.manipulationMode;
		boolean bool = this.effectIsAmbient;
		boolean bool1 = this.effectShowsParticles;
		boolean bool2 = this.effectShowsIcon;
		String string = this.effectIdentifierField.getValue();
		String string1 = this.effectAmplifierField.getValue();
		String string2 = this.effectDurationField.getValue();
		String string3 = this.manipulatedEffectTagIdentifierStringField.getValue();
		String string4 = this.amplifierModificationField.getValue();
		this.init(width, height);
		this.manipulationMode = var2;
		this.cycleManipulationModeButton.setValue(this.manipulationMode);
		this.effectIsAmbient = bool;
		this.toggleEffectIsAmbientButton.setValue(this.effectIsAmbient);
		this.effectShowsParticles = bool1;
		this.toggleEffectShowsParticlesButton.setValue(this.effectShowsParticles);
		this.effectShowsIcon = bool2;
		this.toggleEffectShowsIconButton.setValue(this.effectShowsIcon);
		this.effectIdentifierField.setValue(string);
		this.effectAmplifierField.setValue(string1);
		this.effectDurationField.setValue(string2);
		this.manipulatedEffectTagIdentifierStringField.setValue(string3);
		this.amplifierModificationField.setValue(string4);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		graphics.text(this.font, MANIPULATION_MODE_LABEL_TEXT, this.width / 2 - 152, 26, -6250336);

		if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.APPLY) {

			graphics.text(this.font, STATUS_EFFECT_IDENTIFIER_LABEL_TEXT, this.width / 2 - 152, 55, -6250336);
			this.effectIdentifierField.extractRenderState(graphics, mouseX, mouseY, a);
			graphics.text(this.font, STATUS_EFFECT_AMPLIFIER_LABEL_TEXT, this.width / 2 - 152, 90, -6250336);
			this.effectAmplifierField.extractRenderState(graphics, mouseX, mouseY, a);
			graphics.text(this.font, STATUS_EFFECT_DURATION_LABEL_TEXT, this.width / 2 - 74, 90, -6250336);
			this.effectDurationField.extractRenderState(graphics, mouseX, mouseY, a);
			graphics.text(this.font, IS_AMBIENT_LABEL_TEXT, this.width / 2 + 6, 106, -6250336);
			graphics.text(this.font, SHOWS_PARTICLES_LABEL_TEXT, this.width / 2 - 152, 130, -6250336);
			graphics.text(this.font, SHOWS_ICON_LABEL_TEXT, this.width / 2 + 6, 130, -6250336);

		} else {

			graphics.text(this.font, MANIPULATED_EFFECT_TAG_IDENTIFIER_LABEL_TEXT, this.width / 2 - 152, 55, -6250336);
			this.manipulatedEffectTagIdentifierStringField.extractRenderState(graphics, mouseX, mouseY, a);

			if (this.manipulationMode == StatusEffectManipulationBlockEntity.ManipulationMode.DECREMENT_AMPLIFIER) {

				graphics.text(this.font, AMPLIFIER_MODIFICATION_LABEL_TEXT, this.width / 2 - 152, 90, -6250336);
				this.amplifierModificationField.extractRenderState(graphics, mouseX, mouseY, a);

			}
		}

		graphics.text(this.font, UUID_LIST_HANDLER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 152, 159, -6250336);
		this.uuidListHandlerPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.uuidListHandlerPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.uuidListHandlerPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateStatusEffectManipulationBlockPacket(
				this.statusEffectManipulationBlock.getBlockPos(),
				this.manipulationMode,
				this.effectIdentifierField.getValue(),
				ItemUtils.parseInt(this.effectAmplifierField.getValue()),
				ItemUtils.parseInt(this.effectDurationField.getValue()),
				this.effectIsAmbient,
				this.effectShowsParticles,
				this.effectShowsIcon,
				this.manipulatedEffectTagIdentifierStringField.getValue(),
				ItemUtils.parseInt(this.amplifierModificationField.getValue()),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListHandlerPositionOffsetZField.getValue())
				)
		));
		return true;
	}
}
