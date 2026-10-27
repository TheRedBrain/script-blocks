package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredBeaconBlockPacket;
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
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import java.util.Arrays;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TriggeredBeaconBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component TRIGGERED_TRUE_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.triggered_true_label");
	private static final Component TRIGGERED_FALSE_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.triggered_false_label");
	private static final Component HIDE_AREA_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.hide_area_label");
	private static final Component SHOW_AREA_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.show_area_label");
	private static final Component AREA_DIMENSIONS_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.area_dimensions_label");
	private static final Component AREA_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.area_position_offset_label");
	private static final Component STATUS_EFFECT_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.status_effect_identifier_label");
	private static final Component STATUS_EFFECT_AMPLIFIER_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.status_effect_amplifier_label");
	private static final Component STATUS_EFFECT_DURATION_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.status_effect_duration_label");
	private static final Component AMBIENT_TRUE_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.ambient_true_label");
	private static final Component AMBIENT_FALSE_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.ambient_false_label");
	private static final Component HIDE_PARTICLES_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.hide_particles_label");
	private static final Component SHOW_PARTICLES_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.show_particles_label");
	private static final Component HIDE_ICON_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.hide_icon_label");
	private static final Component SHOW_ICON_LABEL_TEXT = Component.translatable("gui.triggered_beacon_block.show_icon_label");
	private final TriggeredBeaconBlockEntity triggeredBeaconBlock;
	private ScreenPage screenPage;
	private CycleButton<ScreenPage> cycleScreenPageButton;
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
	private EditBox appliedStatusEffectDurationField;
	private CycleButton<Boolean> toggleAppliedStatusEffectAmbientButton;
	private CycleButton<Boolean> toggleAppliedStatusEffectShowParticlesButton;
	private CycleButton<Boolean> toggleAppliedStatusEffectShowIconButton;
	private boolean appliedStatusEffectAmbient;
	private boolean appliedStatusEffectShowParticles;
	private boolean appliedStatusEffectShowIcon;

	private CycleButton<Boolean> toggleWasTriggeredButton;
	private CycleButton<TriggeredBeaconBlockEntity.TriggeredMode> cycleTriggeredModeButton;
	private boolean triggered;
	private boolean triggeredBlockResets;
	private TriggeredBeaconBlockEntity.TriggeredMode triggeredMode;

	public TriggeredBeaconBlockScreen(TriggeredBeaconBlockEntity triggeredBeaconBlock) {
		super(GameNarrator.NO_TITLE);
		this.triggeredBeaconBlock = triggeredBeaconBlock;
		this.screenPage = ScreenPage.AREA;
	}

	@Override
	protected void init() {
		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 30, 300, 20, Component.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		this.showArea = this.triggeredBeaconBlock.showArea();
		this.toggleShowAreaButton = this.addRenderableWidget(CycleButton.booleanBuilder(HIDE_AREA_LABEL_TEXT, SHOW_AREA_LABEL_TEXT).initially(this.showArea).omitKeyText().build(this.width / 2 - 154, 54, 300, 20, Component.empty(), (button, showApplicationArea) -> {
			this.showArea = showApplicationArea;
		}));

		this.areaDimensionsXField = new EditBox(this.font, this.width / 2 - 154, 89, 100, 20, Component.empty());
		this.areaDimensionsXField.setMaxLength(128);
		this.areaDimensionsXField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaDimensions().getX()));
		this.addWidget(this.areaDimensionsXField);

		this.areaDimensionsYField = new EditBox(this.font, this.width / 2 - 50, 89, 100, 20, Component.empty());
		this.areaDimensionsYField.setMaxLength(128);
		this.areaDimensionsYField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaDimensions().getY()));
		this.addWidget(this.areaDimensionsYField);

		this.areaDimensionsZField = new EditBox(this.font, this.width / 2 + 54, 89, 100, 20, Component.empty());
		this.areaDimensionsZField.setMaxLength(128);
		this.areaDimensionsZField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaDimensions().getZ()));
		this.addWidget(this.areaDimensionsZField);

		this.areaPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 124, 100, 20, Component.empty());
		this.areaPositionOffsetXField.setMaxLength(128);
		this.areaPositionOffsetXField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaPositionOffset().getX()));
		this.addWidget(this.areaPositionOffsetXField);

		this.areaPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 124, 100, 20, Component.empty());
		this.areaPositionOffsetYField.setMaxLength(128);
		this.areaPositionOffsetYField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaPositionOffset().getY()));
		this.addWidget(this.areaPositionOffsetYField);

		this.areaPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 124, 100, 20, Component.empty());
		this.areaPositionOffsetZField.setMaxLength(128);
		this.areaPositionOffsetZField.setValue(Integer.toString(this.triggeredBeaconBlock.getAreaPositionOffset().getZ()));
		this.addWidget(this.areaPositionOffsetZField);


		this.appliedStatusEffectIdentifierField = new EditBox(this.font, this.width / 2 - 154, 65, 300, 20, Component.empty());
		this.appliedStatusEffectIdentifierField.setMaxLength(128);
		this.appliedStatusEffectIdentifierField.setValue(this.triggeredBeaconBlock.getAppliedStatusEffectIdentifier());
		this.addWidget(this.appliedStatusEffectIdentifierField);

		this.appliedStatusEffectAmplifierField = new EditBox(this.font, this.width / 2 - 154, 100, 75, 20, Component.empty());
		this.appliedStatusEffectAmplifierField.setValue(Integer.toString(this.triggeredBeaconBlock.getAppliedStatusEffectAmplifier()));
		this.addWidget(this.appliedStatusEffectAmplifierField);

		this.appliedStatusEffectDurationField = new EditBox(this.font, this.width / 2 - 75, 100, 75, 20, Component.empty());
		this.appliedStatusEffectDurationField.setValue(Integer.toString(this.triggeredBeaconBlock.getAppliedStatusEffectDuration()));
		this.addWidget(this.appliedStatusEffectDurationField);

		this.appliedStatusEffectAmbient = this.triggeredBeaconBlock.getAppliedStatusEffectAmbient();
		this.toggleAppliedStatusEffectAmbientButton = this.addRenderableWidget(CycleButton.booleanBuilder(AMBIENT_TRUE_LABEL_TEXT, AMBIENT_FALSE_LABEL_TEXT).initially(this.appliedStatusEffectAmbient).omitKeyText().build(this.width / 2 + 4, 100, 150, 20, Component.empty(), (button, appliedStatusEffectAmbient) -> {
			this.appliedStatusEffectAmbient = appliedStatusEffectAmbient;
		}));

		this.appliedStatusEffectShowParticles = this.triggeredBeaconBlock.getAppliedStatusEffectShowParticles();
		this.toggleAppliedStatusEffectShowParticlesButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_PARTICLES_LABEL_TEXT, HIDE_PARTICLES_LABEL_TEXT).initially(this.appliedStatusEffectShowParticles).omitKeyText().build(this.width / 2 - 154, 124, 150, 20, Component.empty(), (button, appliedStatusEffectShowParticles) -> {
			this.appliedStatusEffectShowParticles = appliedStatusEffectShowParticles;
		}));

		this.appliedStatusEffectShowIcon = this.triggeredBeaconBlock.getAppliedStatusEffectShowIcon();
		this.toggleAppliedStatusEffectShowIconButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_ICON_LABEL_TEXT, HIDE_ICON_LABEL_TEXT).initially(this.appliedStatusEffectShowIcon).omitKeyText().build(this.width / 2 + 4, 124, 150, 20, Component.empty(), (button, appliedStatusEffectShowIcon) -> {
			this.appliedStatusEffectShowIcon = appliedStatusEffectShowIcon;
		}));


		this.triggered = this.triggeredBeaconBlock.getTriggered();
		this.toggleWasTriggeredButton = this.addRenderableWidget(CycleButton.booleanBuilder(TRIGGERED_TRUE_LABEL_TEXT, TRIGGERED_FALSE_LABEL_TEXT).initially(this.triggered).omitKeyText().build(this.width / 2 - 154, 89, 300, 20, Component.empty(), (button, triggered) -> {
			this.triggered = triggered;
		}));

		this.triggeredMode = this.triggeredBeaconBlock.getTriggeredMode();
		this.cycleTriggeredModeButton = this.addRenderableWidget(CycleButton.builder(TriggeredBeaconBlockEntity.TriggeredMode::asText).values((TriggeredBeaconBlockEntity.TriggeredMode[]) TriggeredBeaconBlockEntity.TriggeredMode.values()).initially(this.triggeredMode).omitKeyText().build(this.width / 2 - 154, 113, 300, 20, Component.empty(), (button, triggeredMode) -> {
			this.triggeredMode = triggeredMode;
			this.updateWidgets();
		}));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 207, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 207, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

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
		this.appliedStatusEffectDurationField.setVisible(false);
		this.toggleAppliedStatusEffectAmbientButton.visible = false;
		this.toggleAppliedStatusEffectShowParticlesButton.visible = false;
		this.toggleAppliedStatusEffectShowIconButton.visible = false;

		this.toggleWasTriggeredButton.visible = false;
		this.cycleTriggeredModeButton.visible = false;

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
			this.appliedStatusEffectDurationField.setVisible(true);

			this.toggleAppliedStatusEffectAmbientButton.visible = true;
			this.toggleAppliedStatusEffectShowParticlesButton.visible = true;
			this.toggleAppliedStatusEffectShowIconButton.visible = true;

		} else if (this.screenPage == ScreenPage.TRIGGER_SETTINGS) {

			this.toggleWasTriggeredButton.visible = true;
			this.cycleTriggeredModeButton.visible = true;

		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		ScreenPage var = this.screenPage;
		TriggeredBeaconBlockEntity.TriggeredMode var2 = this.triggeredMode;
		boolean bool = this.showArea;
		boolean bool1 = this.appliedStatusEffectAmbient;
		boolean bool2 = this.appliedStatusEffectShowParticles;
		boolean bool3 = this.appliedStatusEffectShowIcon;
		boolean bool4 = this.triggered;
		boolean bool5 = this.triggeredBlockResets;
		String string = this.areaDimensionsXField.getValue();
		String string1 = this.areaDimensionsYField.getValue();
		String string2 = this.areaDimensionsZField.getValue();
		String string3 = this.areaPositionOffsetXField.getValue();
		String string4 = this.areaPositionOffsetYField.getValue();
		String string5 = this.areaPositionOffsetZField.getValue();
		String string6 = this.appliedStatusEffectIdentifierField.getValue();
		String string7 = this.appliedStatusEffectAmplifierField.getValue();
		String string8 = this.appliedStatusEffectDurationField.getValue();
		this.init(client, width, height);
		this.screenPage = var;
		this.triggeredMode = var2;
		this.showArea = bool;
		this.appliedStatusEffectAmbient = bool1;
		this.appliedStatusEffectShowParticles = bool2;
		this.appliedStatusEffectShowIcon = bool3;
		this.triggered = bool4;
		this.triggeredBlockResets = bool5;
		this.areaDimensionsXField.setValue(string);
		this.areaDimensionsYField.setValue(string1);
		this.areaDimensionsZField.setValue(string2);
		this.areaPositionOffsetXField.setValue(string3);
		this.areaPositionOffsetYField.setValue(string4);
		this.areaPositionOffsetZField.setValue(string5);
		this.appliedStatusEffectIdentifierField.setValue(string6);
		this.appliedStatusEffectAmplifierField.setValue(string7);
		this.appliedStatusEffectDurationField.setValue(string8);
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
			context.drawString(this.font, STATUS_EFFECT_DURATION_LABEL_TEXT, this.width / 2 - 74, 90, 0xA0A0A0);
			this.appliedStatusEffectDurationField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TRIGGER_SETTINGS) {
		}
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateTriggeredBeaconBlockPacket(
				this.triggeredBeaconBlock.getPos(),
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
				ItemUtils.parseInt(this.appliedStatusEffectDurationField.getValue()),
				this.appliedStatusEffectAmbient,
				this.appliedStatusEffectShowParticles,
				this.appliedStatusEffectShowIcon,
				this.triggered,
				this.triggeredMode.asString()
		));
		return true;
	}

	public enum ScreenPage implements StringRepresentable {
		AREA("area"),
		APPLIED_EFFECT("applied_effect"),
		TRIGGER_SETTINGS("trigger_settings");

		private final String name;

		ScreenPage(String name) {
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
			return Component.translatable("gui.triggered_beacon_block.screen_page." + this.name);
		}
	}
}
