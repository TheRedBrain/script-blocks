package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaBlockPacket;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import java.util.Arrays;
import java.util.Optional;

@Deprecated
@Environment(value = EnvType.CLIENT)
public class AreaBlockScreen extends Screen {
	private static final Component TRIGGERED_TRUE_LABEL_TEXT = Component.translatable("gui.area_block.triggered_true_label");
	private static final Component TRIGGERED_FALSE_LABEL_TEXT = Component.translatable("gui.area_block.triggered_false_label");
	private static final Component HIDE_AREA_LABEL_TEXT = Component.translatable("gui.area_block.hide_area_label");
	private static final Component SHOW_AREA_LABEL_TEXT = Component.translatable("gui.area_block.show_area_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.area_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.area_block.area_position_offset_label");
	private static final Component STATUS_EFFECT_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.area_block.status_effect_identifier_label");
	private static final Component STATUS_EFFECT_AMPLIFIER_LABEL_TEXT = Component.translatable("gui.area_block.status_effect_amplifier_label");
	private static final Component AMBIENT_TRUE_LABEL_TEXT = Component.translatable("gui.area_block.ambient_true_label");
	private static final Component AMBIENT_FALSE_LABEL_TEXT = Component.translatable("gui.area_block.ambient_false_label");
	private static final Component HIDE_PARTICLES_LABEL_TEXT = Component.translatable("gui.area_block.hide_particles_label");
	private static final Component SHOW_PARTICLES_LABEL_TEXT = Component.translatable("gui.area_block.show_particles_label");
	private static final Component HIDE_ICON_LABEL_TEXT = Component.translatable("gui.area_block.hide_icon_label");
	private static final Component SHOW_ICON_LABEL_TEXT = Component.translatable("gui.area_block.show_icon_label");
	private static final Component JOIN_MESSAGE_LABEL_TEXT = Component.translatable("gui.area_block.join_message_label");
	private static final Component LEAVE_MESSAGE_LABEL_TEXT = Component.translatable("gui.area_block.leave_message_label");
	private static final Component TRIGGERED_MESSAGE_LABEL_TEXT = Component.translatable("gui.area_block.triggered_message_label");
	private static final Component TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset");
	private static final Component PRE_TIMER_LABEL_TEXT = Component.translatable("gui.area_block.pre_timer_label");
	private static final Component POST_TIMER_LABEL_TEXT = Component.translatable("gui.area_block.post_timer_label");
	private final AreaBlockEntity areaBlock;
	private ScreenPage screenPage;
	private CycleButton<Boolean> toggleShowAreaButton;
	private EditBox areaDimensionsXField;
	private EditBox areaDimensionsYField;
	private EditBox areaDimensionsZField;
	private EditBox areaPositionOffsetXField;
	private EditBox areaPositionOffsetYField;
	private EditBox areaPositionOffsetZField;
	private boolean showArea;

	private EditBox appliedStatusEffectIdentifierField;
	private EditBox appliedStatusEffectAmplifierField;
	private CycleButton<Boolean> toggleAppliedStatusEffectAmbientButton;
	private CycleButton<Boolean> toggleAppliedStatusEffectShowParticlesButton;
	private CycleButton<Boolean> toggleAppliedStatusEffectShowIconButton;
	private boolean appliedStatusEffectAmbient;
	private boolean appliedStatusEffectShowParticles;
	private boolean appliedStatusEffectShowIcon;

	private EditBox joinMessageField;
	private EditBox leaveMessageField;
	private EditBox triggeredMessageField;
	private CycleButton<AreaBlockEntity.MessageMode> cycleMessageModeButton;
	private AreaBlockEntity.MessageMode messageMode;

	private EditBox triggeredBlockPositionOffsetXField;
	private EditBox triggeredBlockPositionOffsetYField;
	private EditBox triggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> toggleTriggeredBlockResetsButton;
	private CycleButton<Boolean> toggleWasTriggeredButton;
	private CycleButton<AreaBlockEntity.TriggerMode> cycleTriggerModeButton;
	private CycleButton<AreaBlockEntity.TriggeredMode> cycleTriggeredModeButton;
	private EditBox timerField;
	private boolean wasTriggered;
	private boolean triggeredBlockResets;
	private AreaBlockEntity.TriggerMode triggerMode;
	private AreaBlockEntity.TriggeredMode triggeredMode;

	public AreaBlockScreen(AreaBlockEntity areaBlock) {
		super(GameNarrator.NO_TITLE);
		this.areaBlock = areaBlock;
		this.screenPage = ScreenPage.AREA;
	}

	private void done() {
		if (this.updateAreaBlock()) {
			this.onClose();
		}
	}

	private void cancel() {
		this.onClose();
	}

	@Override
	protected void init() {
		this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 30, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showArea = this.areaBlock.showArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 54, 300, 20, Component.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 89, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.areaBlock.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 89, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.areaBlock.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 89, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.areaBlock.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 124, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.areaBlock.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 124, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.areaBlock.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 124, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.areaBlock.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);


		this.appliedStatusEffectIdentifierField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.appliedStatusEffectIdentifierField.setMaxLength(128);
		this.appliedStatusEffectIdentifierField.setValue(this.areaBlock.getAppliedStatusEffectIdentifier());
		this.addWidget(this.appliedStatusEffectIdentifierField);

		this.appliedStatusEffectAmplifierField = new EditBox(this.font, this.width / 2 - 154, 100, 150, 20, Component.empty());
		this.appliedStatusEffectAmplifierField.setValue(Integer.toString(this.areaBlock.getAppliedStatusEffectAmplifier()));
		this.addWidget(this.appliedStatusEffectAmplifierField);

		this.appliedStatusEffectAmbient = this.areaBlock.getAppliedStatusEffectAmbient();
		this.toggleAppliedStatusEffectAmbientButton = this.addRenderableWidget(CycleButton.booleanBuilder(AMBIENT_TRUE_LABEL_TEXT, AMBIENT_FALSE_LABEL_TEXT).initially(this.appliedStatusEffectAmbient).omitKeyText().build(this.width / 2 + 4, 100, 150, 20, Component.empty(), (button, appliedStatusEffectAmbient) -> {
			this.appliedStatusEffectAmbient = appliedStatusEffectAmbient;
		}));

		this.appliedStatusEffectShowParticles = this.areaBlock.getAppliedStatusEffectShowParticles();
		this.toggleAppliedStatusEffectShowParticlesButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_PARTICLES_LABEL_TEXT, HIDE_PARTICLES_LABEL_TEXT).initially(this.appliedStatusEffectShowParticles).omitKeyText().build(this.width / 2 - 154, 124, 150, 20, Component.empty(), (button, appliedStatusEffectShowParticles) -> {
			this.appliedStatusEffectShowParticles = appliedStatusEffectShowParticles;
		}));

		this.appliedStatusEffectShowIcon = this.areaBlock.getAppliedStatusEffectShowIcon();
		this.toggleAppliedStatusEffectShowIconButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_ICON_LABEL_TEXT, HIDE_ICON_LABEL_TEXT).initially(this.appliedStatusEffectShowIcon).omitKeyText().build(this.width / 2 + 4, 124, 150, 20, Component.empty(), (button, appliedStatusEffectShowIcon) -> {
			this.appliedStatusEffectShowIcon = appliedStatusEffectShowIcon;
		}));


		this.joinMessageField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.joinMessageField.setMaxLength(128);
		this.joinMessageField.setValue(this.areaBlock.getJoinMessage());
		this.addWidget(this.joinMessageField);

		this.leaveMessageField = new EditBox(this.font, this.width / 2 - 154, 100, 300, 20, Component.empty());
		this.leaveMessageField.setMaxLength(128);
		this.leaveMessageField.setValue(this.areaBlock.getLeaveMessage());
		this.addWidget(this.leaveMessageField);

		this.triggeredMessageField = new EditBox(this.font, this.width / 2 - 154, 135, 300, 20, Component.empty());
		this.triggeredMessageField.setMaxLength(128);
		this.triggeredMessageField.setValue(this.areaBlock.getTriggeredMessage());
		this.addWidget(this.triggeredMessageField);

		this.messageMode = this.areaBlock.getMessageMode();
		this.cycleMessageModeButton = this.addRenderableWidget(CycleButton.builder(AreaBlockEntity.MessageMode::asText).values((AreaBlockEntity.MessageMode[]) AreaBlockEntity.MessageMode.values()).initially(this.messageMode).omitKeyText().build(this.width / 2 - 154, 159, 300, 20, Component.empty(), (button, messageMode) -> {
			this.messageMode = messageMode;
			this.updateWidgets();
		}));


		this.triggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 65, 50, 20, Component.translatable(""));
		this.triggeredBlockPositionOffsetXField.setMaxLength(128);
		this.triggeredBlockPositionOffsetXField.setValue(Integer.toString(this.areaBlock.getTriggeredBlock().getLeft().getX()));
		this.addWidget(this.triggeredBlockPositionOffsetXField);
		this.triggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 65, 50, 20, Component.translatable(""));
		this.triggeredBlockPositionOffsetYField.setMaxLength(128);
		this.triggeredBlockPositionOffsetYField.setValue(Integer.toString(this.areaBlock.getTriggeredBlock().getLeft().getY()));
		this.addWidget(this.triggeredBlockPositionOffsetYField);
		this.triggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 65, 50, 20, Component.translatable(""));
		this.triggeredBlockPositionOffsetZField.setMaxLength(128);
		this.triggeredBlockPositionOffsetZField.setValue(Integer.toString(this.areaBlock.getTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.triggeredBlockPositionOffsetZField);
		this.triggeredBlockResets = this.areaBlock.getTriggeredBlock().getRight();
		this.toggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.triggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 65, 150, 20, Component.empty(), (button, triggeredBlockResets) -> {
			this.triggeredBlockResets = triggeredBlockResets;
		}));

		this.wasTriggered = this.areaBlock.getWasTriggered();
		this.toggleWasTriggeredButton = this.addRenderableWidget(CycleButton.booleanBuilder(TRIGGERED_TRUE_LABEL_TEXT, TRIGGERED_FALSE_LABEL_TEXT).initially(this.wasTriggered).omitKeyText().build(this.width / 2 - 154, 89, 300, 20, Component.empty(), (button, triggered) -> {
			this.wasTriggered = triggered;
		}));

		this.triggeredMode = this.areaBlock.getTriggeredMode();
		this.cycleTriggeredModeButton = this.addRenderableWidget(CycleButton.builder(AreaBlockEntity.TriggeredMode::asText).values((AreaBlockEntity.TriggeredMode[]) AreaBlockEntity.TriggeredMode.values()).initially(this.triggeredMode).omitKeyText().build(this.width / 2 - 154, 113, 300, 20, Component.empty(), (button, triggeredMode) -> {
			this.triggeredMode = triggeredMode;
			this.updateWidgets();
		}));

		this.triggerMode = this.areaBlock.getTriggerMode();
		this.cycleTriggerModeButton = this.addRenderableWidget(CycleButton.builder(AreaBlockEntity.TriggerMode::asText).values((AreaBlockEntity.TriggerMode[]) AreaBlockEntity.TriggerMode.values()).initially(this.triggerMode).omitKeyText().build(this.width / 2 - 154, 137, 300, 20, Component.empty(), (button, triggerMode) -> {
			this.triggerMode = triggerMode;
			this.updateWidgets();
		}));

		int i = this.font.width(PRE_TIMER_LABEL_TEXT) + 10;
		this.timerField = new EditBox(this.font, this.width / 2 - 152 + i, 161, 50, 20, Component.translatable(""));
		this.timerField.setValue(Integer.toString(this.areaBlock.getMaxTimer()));
		this.addWidget(this.timerField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 207, 150, 20).build());
		this.updateWidgets();
	}

	private void updateWidgets() {

		this.toggleWasTriggeredButton.visible = false;

		this.toggleShowAreaButton.visible = false;
		this.areaDimensionsXField.visible = false;
		this.areaDimensionsYField.visible = false;
		this.areaDimensionsZField.visible = false;
		this.areaPositionOffsetXField.visible = false;
		this.areaPositionOffsetYField.visible = false;
		this.areaPositionOffsetZField.visible = false;

		this.appliedStatusEffectIdentifierField.setVisible(false);
		this.appliedStatusEffectAmplifierField.setVisible(false);
		this.toggleAppliedStatusEffectAmbientButton.visible = false;
		this.toggleAppliedStatusEffectShowParticlesButton.visible = false;
		this.toggleAppliedStatusEffectShowIconButton.visible = false;

		this.joinMessageField.setVisible(false);
		this.leaveMessageField.setVisible(false);
		this.triggeredMessageField.setVisible(false);
		this.cycleMessageModeButton.visible = false;

		this.triggeredBlockPositionOffsetXField.setVisible(false);
		this.triggeredBlockPositionOffsetYField.setVisible(false);
		this.triggeredBlockPositionOffsetZField.setVisible(false);
		this.toggleTriggeredBlockResetsButton.visible = false;
		this.toggleWasTriggeredButton.visible = false;
		this.cycleTriggerModeButton.visible = false;
		this.cycleTriggeredModeButton.visible = false;
		this.timerField.setVisible(false);

		if (this.screenPage == ScreenPage.AREA) {

			this.toggleShowAreaButton.visible = true;
			this.areaDimensionsXField.visible = true;
			this.areaDimensionsYField.visible = true;
			this.areaDimensionsZField.visible = true;
			this.areaPositionOffsetXField.visible = true;
			this.areaPositionOffsetYField.visible = true;
			this.areaPositionOffsetZField.visible = true;

		} else if (this.screenPage == ScreenPage.APPLIED_EFFECT) {

			this.appliedStatusEffectIdentifierField.setVisible(true);
			this.appliedStatusEffectAmplifierField.setVisible(true);

			this.toggleAppliedStatusEffectAmbientButton.visible = true;
			this.toggleAppliedStatusEffectShowParticlesButton.visible = true;
			this.toggleAppliedStatusEffectShowIconButton.visible = true;

		} else if (this.screenPage == ScreenPage.MESSAGES) {

			this.joinMessageField.setVisible(true);
			this.leaveMessageField.setVisible(true);
			this.triggeredMessageField.setVisible(true);
			this.cycleMessageModeButton.visible = true;

		} else if (this.screenPage == ScreenPage.TRIGGER_SETTINGS) {

			this.triggeredBlockPositionOffsetXField.setVisible(true);
			this.triggeredBlockPositionOffsetYField.setVisible(true);
			this.triggeredBlockPositionOffsetZField.setVisible(true);
			this.toggleTriggeredBlockResetsButton.visible = true;
			this.toggleWasTriggeredButton.visible = true;
			this.cycleTriggerModeButton.visible = true;
			this.cycleTriggeredModeButton.visible = true;
			if (this.triggerMode == AreaBlockEntity.TriggerMode.TIMED) {
				this.timerField.setVisible(true);
			}
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		AreaBlockEntity.TriggerMode var1 = this.triggerMode;
		AreaBlockEntity.TriggeredMode var2 = this.triggeredMode;
		boolean bool = this.showArea;
		boolean bool1 = this.appliedStatusEffectAmbient;
		boolean bool2 = this.appliedStatusEffectShowParticles;
		boolean bool3 = this.appliedStatusEffectShowIcon;
		boolean bool4 = this.wasTriggered;
		boolean bool5 = this.triggeredBlockResets;
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.appliedStatusEffectIdentifierField.getValue();
		String string7 = this.appliedStatusEffectAmplifierField.getValue();
		String string8 = this.joinMessageField.getValue();
		String string9 = this.leaveMessageField.getValue();
		String string10 = this.triggeredMessageField.getValue();
		String string11 = this.triggeredBlockPositionOffsetXField.getValue();
		String string12 = this.triggeredBlockPositionOffsetYField.getValue();
		String string13 = this.triggeredBlockPositionOffsetZField.getValue();
		String string14 = this.timerField.getValue();
		this.init(client, width, height);
		this.screenPage = var;
		this.triggerMode = var1;
		this.triggeredMode = var2;
		this.showArea = bool;
		this.appliedStatusEffectAmbient = bool1;
		this.appliedStatusEffectShowParticles = bool2;
		this.appliedStatusEffectShowIcon = bool3;
		this.wasTriggered = bool4;
		this.triggeredBlockResets = bool5;
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.appliedStatusEffectIdentifierField.setValue(string6);
		this.appliedStatusEffectAmplifierField.setValue(string7);
		this.joinMessageField.setValue(string8);
		this.leaveMessageField.setValue(string9);
		this.triggeredMessageField.setValue(string10);
		this.triggeredBlockPositionOffsetXField.setValue(string11);
		this.triggeredBlockPositionOffsetYField.setValue(string12);
		this.triggeredBlockPositionOffsetZField.setValue(string13);
		this.timerField.setValue(string14);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.AREA) {
			context.drawString(this.font, AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 79, 0xA0A0A0);
			this.areaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.areaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, AREA_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 114, 0xA0A0A0);
			this.areaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.areaPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.APPLIED_EFFECT) {
			context.drawString(this.font, STATUS_EFFECT_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
			this.appliedStatusEffectIdentifierField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, STATUS_EFFECT_AMPLIFIER_LABEL_TEXT, this.width / 2 - 153, 90, 0xA0A0A0);
			this.appliedStatusEffectAmplifierField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.MESSAGES) {
			context.drawString(this.font, JOIN_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
			this.joinMessageField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, LEAVE_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 90, 0xA0A0A0);
			this.leaveMessageField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, TRIGGERED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 125, 0xA0A0A0);
			this.triggeredMessageField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TRIGGER_SETTINGS) {
			context.drawString(this.font, TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 55, 0xA0A0A0);
			this.triggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.triggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			if (this.triggerMode == AreaBlockEntity.TriggerMode.TIMED) {
				context.drawString(this.font, PRE_TIMER_LABEL_TEXT, this.width / 2 - 153, 167, 0xA0A0A0);
				this.timerField.render(context, mouseX, mouseY, delta);
				int i = this.timerField.getX() + 60;
				context.drawString(this.font, POST_TIMER_LABEL_TEXT, i, 167, 0xA0A0A0);
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private boolean updateAreaBlock() {
		ClientPlayNetworking.send(new UpdateAreaBlockPacket(
				this.areaBlock.getPos(),
				this.showArea,
				new Vec3i(
						ItemUtils.parseInt(this.areaDimensionsXField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsYField.getValue()),
						ItemUtils.parseInt(this.areaDimensionsZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.areaPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.areaPositionOffsetZField.getValue())
				),
				this.appliedStatusEffectIdentifierField.getValue(),
				ItemUtils.parseInt(this.appliedStatusEffectAmplifierField.getValue()),
				this.appliedStatusEffectAmbient,
				this.appliedStatusEffectShowParticles,
				this.appliedStatusEffectShowIcon,
				new BlockPos(
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.triggeredBlockPositionOffsetZField.getValue())
				),
				this.triggeredBlockResets,
				this.wasTriggered,
				this.joinMessageField.getValue(),
				this.leaveMessageField.getValue(),
				this.triggeredMessageField.getValue(),
				this.messageMode.asString(),
				this.triggerMode.asString(),
				this.triggeredMode.asString(),
				ItemUtils.parseInt(this.timerField.getValue())
		));
		return true;
	}

	public static enum ScreenPage implements StringRepresentable {
		AREA("area"),
		APPLIED_EFFECT("applied_effect"),
		MESSAGES("messages"),
		TRIGGER_SETTINGS("trigger_settings");

		private final String name;

		private ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.area_block.screenPage." + this.name);
		}
	}
}
