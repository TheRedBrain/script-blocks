package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.registry.LocationsRegistry;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class CreativeTeleporterBlockScreen extends Screen {
	private static final int VISIBLE_STATUS_EFFECT_LIST_ENTRIES = 4;
	private static final Text HIDE_ADVENTURE_SCREEN_LABEL_TEXT = Text.translatable("gui.teleporter_block.hide_adventure_screen_label");
	private static final Text SHOW_ADVENTURE_SCREEN_LABEL_TEXT = Text.translatable("gui.teleporter_block.show_adventure_screen_label");
	private static final Text HIDE_ACTIVATION_AREA_LABEL_TEXT = Text.translatable("gui.teleporter_block.hide_activation_area_label");
	private static final Text SHOW_ACTIVATION_AREA_LABEL_TEXT = Text.translatable("gui.teleporter_block.show_activation_area_label");
	private static final Text ACTIVATION_AREA_DIMENSIONS_LABEL_TEXT = Text.translatable("gui.teleporter_block.activation_area_dimensions_label");
	private static final Text ACTIVATION_AREA_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.teleporter_block.activation_area_position_offset_label");
	private static final Text ACCESS_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.teleporter_block.access_position_offset_label");
	private static final Text TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.teleporter_block.toggle_set_access_position_button_label.on");
	private static final Text TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.teleporter_block.toggle_set_access_position_button_label.off");
	private static final Text TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.teleporter_block.toggle_only_teleport_dimension_owner_button_label.on");
	private static final Text TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.teleporter_block.toggle_only_teleport_dimension_owner_button_label.off");
	private static final Text TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.teleporter_block.toggle_teleport_team_button_label.on");
	private static final Text TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.teleporter_block.toggle_teleport_team_button_label.off");
	private static final Text TELEPORTATION_MODE_LABEL_TEXT = Text.translatable("gui.teleporter_block.teleportation_mode_label");
	private static final Text DIRECT_TELEPORT_POSITION_OFFET_LABEL_TEXT = Text.translatable("gui.teleporter_block.direct_teleport_position_offset_label");
	private static final Text DIRECT_TELEPORT_ORIENTATION_LABEL_TEXT = Text.translatable("gui.teleporter_block.direct_teleport_orientation_label");
	private static final Text SPAWN_POINT_TYPE_LABEL_TEXT = Text.translatable("gui.teleporter_block.spawn_point_type_label");
	private static final Text ADD_NEW_LOCATION_BUTTON_LABEL_TEXT = Text.translatable("gui.teleporter_block.add_new_location_button_label");
	private static final Text ADD_NEW_STATUS_EFFECT_BUTTON_LABEL_TEXT = Text.translatable("gui.teleporter_block.add_new_status_effect_button_label");
	private static final Text TOGGLE_SHOW_REGENERATE_BUTTON_BUTTON_LABEL_TEXT_ON = Text.translatable("gui.teleporter_block.toggle_show_regenerate_button_button_label.on");
	private static final Text TOGGLE_SHOW_REGENERATE_BUTTON_BUTTON_LABEL_TEXT_OFF = Text.translatable("gui.teleporter_block.toggle_show_regenerate_button_button_label.off");
	private static final Text NEW_STATUS_EFFECT_FIELD_TEXT = Text.translatable("gui.teleporter_block.new_status_effect_field");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_88_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_88");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TeleporterBlockEntity teleporterBlock;

	private CyclingButtonWidget<ScreenPage> creativeScreenPageButton;
	private CyclingButtonWidget<Boolean> toggleShowAdventureScreenButton;
	private CyclingButtonWidget<Boolean> toggleShowActivationAreaButton;
	private TextFieldWidget activationAreaDimensionsXField;
	private TextFieldWidget activationAreaDimensionsYField;
	private TextFieldWidget activationAreaDimensionsZField;
	private TextFieldWidget activationAreaPositionOffsetXField;
	private TextFieldWidget activationAreaPositionOffsetYField;
	private TextFieldWidget activationAreaPositionOffsetZField;
	private TextFieldWidget accessPositionOffsetXField;
	private TextFieldWidget accessPositionOffsetYField;
	private TextFieldWidget accessPositionOffsetZField;
	private ButtonWidget removeStatusEffectButton0;
	private ButtonWidget removeStatusEffectButton1;
	private ButtonWidget removeStatusEffectButton2;
	private ButtonWidget removeStatusEffectButton3;
	private TextFieldWidget newStatusEffectField;
	private ButtonWidget addNewStatusEffectButton;
	private CyclingButtonWidget<Boolean> toggleSetAccessPositionButton;
	private CyclingButtonWidget<Boolean> toggleOnlyTeleportDimensionOwnerButton;
	private CyclingButtonWidget<Boolean> toggleTeleportTeamButton;
	private CyclingButtonWidget<TeleporterBlockEntity.TeleportationMode> teleportationModeButton;
	private TextFieldWidget directTeleportPositionOffsetXField;
	private TextFieldWidget directTeleportPositionOffsetYField;
	private TextFieldWidget directTeleportPositionOffsetZField;
	private TextFieldWidget directTeleportOrientationYawField;
	private TextFieldWidget directTeleportOrientationPitchField;
	private CyclingButtonWidget<TeleporterBlockEntity.SpawnPointType> spawnPointTypeButton;
	private ButtonWidget removeLocationButton0;
	private ButtonWidget removeLocationButton1;
	private ButtonWidget removeLocationButton2;
	private TextFieldWidget newLocationIdentifierField;
	private TextFieldWidget newLocationEntranceField;
	private TextFieldWidget newDataIdField;
	private TextFieldWidget newDataField;
	private ButtonWidget addNewLocationButton;
	private TextFieldWidget teleporterNameField;
	private TextFieldWidget currentTargetOwnerLabelField;
	private TextFieldWidget currentTargetIdentifierLabelField;
	private CyclingButtonWidget<Boolean> toggleShowRegenerateButtonButton;
	private TextFieldWidget teleportButtonLabelField;
	private TextFieldWidget cancelTeleportButtonLabelField;
	private ButtonWidget doneButton;
	private ButtonWidget cancelButton;

	private ScreenPage screenPage;
	private boolean showActivationArea;
	private boolean showAdventureScreen;
	private boolean setAccessPosition;
	private boolean onlyTeleportDimensionOwner;
	private boolean teleportTeam;
	private boolean showRegenerateButton;

	private TeleporterBlockEntity.TeleportationMode teleportationMode;
	private TeleporterBlockEntity.SpawnPointType spawnPointType;

	private final List<String> statusEffectsToDecrementLevelOnTeleport = new ArrayList<>();

	private final List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList = new ArrayList<>();

	private int creativeLocationsListScrollPosition = 0;
	private int statusEffectListScrollPosition = 0;
	private float creativeLocationsListScrollAmount = 0.0f;
	private float statusEffectListScrollAmount = 0.0f;
	private boolean creativeLocationsListMouseClicked = false;
	private boolean statusEffectListMouseClicked = false;

	public CreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlock) {
		super(NarratorManager.EMPTY);
		this.teleporterBlock = teleporterBlock;
		this.screenPage = ScreenPage.ACTIVATION;
	}

	private void done() {
		ScriptBlocks.info("done");
		this.updateTeleporterBlock();
		this.close();
	}

	private void cancel() {
		ScriptBlocks.info("cancel");
		this.teleporterBlock.setShowActivationArea(this.showActivationArea);
		this.teleporterBlock.setShowAdventureScreen(this.showAdventureScreen);
		this.teleporterBlock.setSetAccessPosition(this.setAccessPosition);
		this.teleporterBlock.setOnlyTeleportDimensionOwner(this.onlyTeleportDimensionOwner);
		this.teleporterBlock.setTeleportTeam(this.teleportTeam);
		this.teleporterBlock.setShowRegenerateButton(this.showRegenerateButton);
		this.teleporterBlock.setTeleportationMode(this.teleportationMode);
		this.teleporterBlock.setSpawnPointType(this.spawnPointType);
		this.close();
	}

	private void addLocationToList(String identifier, String entrance, String dataId, String dataString) {
		ScriptBlocks.LOGGER.info("addLocationToList");
		Text message = Text.literal("");
//		if (Identifier.isValid(identifier)) {
		Location location = LocationsRegistry.registeredLocations.get(Identifier.tryParse(identifier));
		if (location != null) {
			if (!LocationUtils.hasEntrance(location, entrance)) {
				entrance = "";
			}
			boolean bl = false;
			int data = 0;
			if (!dataString.isEmpty()) {
				data = Integer.parseInt(dataString);
			}
			for (MutablePair<MutablePair<String, String>, MutablePair<String, Integer>> locationsListEntry : this.locationsList) {
				if (locationsListEntry.getLeft().getLeft().equals(identifier) && locationsListEntry.getLeft().getRight().equals(entrance) && locationsListEntry.getRight().getLeft().equals(dataId) && locationsListEntry.getRight().getRight().equals(data)) {
					bl = true;
					break;
				}
			}
			if (bl) {
				message = Text.translatable("gui.teleporter_block.location_already_in_list");
			} else {
				this.locationsList.add(new MutablePair<>(new MutablePair<>(identifier, entrance), new MutablePair<>(dataId, data)));
			}
		} else {
			message = Text.translatable("gui.teleporter_block.location_not_found");
		}
//		} else {
//			message = Text.translatable("gui.invalid_identifier");
//		}
		if (this.client != null && this.client.player != null && !message.getString().isEmpty()) {
			this.client.player.sendMessage(message);
		}
		this.updateWidgets();
	}

	private void removeLocationFromLocationList(int index) {
		ScriptBlocks.info("removeLocationFromLocationList");
		if (index + this.creativeLocationsListScrollPosition < this.locationsList.size()) {
			this.locationsList.remove(index + this.creativeLocationsListScrollPosition);
		}
		this.updateWidgets();
	}

	private void addStatusEffectToList(String identifier) {
		ScriptBlocks.info("addStatusEffectToList");
		if (!identifier.isEmpty()) {
			this.statusEffectsToDecrementLevelOnTeleport.add(identifier);
		}
		this.updateWidgets();
	}

	private void removeStatusEffectFromStatusEffectList(int index) {
		ScriptBlocks.info("removeStatusEffectFromStatusEffectList");
		if (index + this.statusEffectListScrollPosition < this.statusEffectsToDecrementLevelOnTeleport.size()) {
			this.statusEffectsToDecrementLevelOnTeleport.remove(index + this.statusEffectListScrollPosition);
		}
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.locationsList.addAll(this.teleporterBlock.getLocationsList());
		this.statusEffectsToDecrementLevelOnTeleport.addAll(this.teleporterBlock.getStatusEffectsToDecrementLevelOnTeleport());
		this.showAdventureScreen = this.teleporterBlock.getShowAdventureScreen();
		this.teleportationMode = this.teleporterBlock.getTeleportationMode();
		this.showRegenerateButton = this.teleporterBlock.showRegenerateButton();

		super.init();

		this.creativeScreenPageButton = this.addDrawableChild(CyclingButtonWidget.builder(ScreenPage::asText).values((ScreenPage[]) ScreenPage.values()).initially(this.screenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Text.empty(), (button, screenPage) -> {
			this.screenPage = screenPage;
			this.updateWidgets();
		}));

		// --- activation page ---

		this.toggleShowAdventureScreenButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_ADVENTURE_SCREEN_LABEL_TEXT, SHOW_ADVENTURE_SCREEN_LABEL_TEXT).initially(this.showAdventureScreen).omitKeyText().build(this.width / 2 - 154, 45, 150, 20, SHOW_ADVENTURE_SCREEN_LABEL_TEXT, (button, showAdventureScreen) -> {
			this.showAdventureScreen = showAdventureScreen;
		}));

		this.showActivationArea = this.teleporterBlock.getShowActivationArea();
		this.toggleShowActivationAreaButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(HIDE_ACTIVATION_AREA_LABEL_TEXT, SHOW_ACTIVATION_AREA_LABEL_TEXT).initially(this.showActivationArea).omitKeyText().build(this.width / 2 + 4, 45, 150, 20, Text.empty(), (button, showActivationArea) -> {
			this.showActivationArea = showActivationArea;
		}));

		this.activationAreaDimensionsXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.activationAreaDimensionsXField.setMaxLength(128);
		this.activationAreaDimensionsXField.setText(Integer.toString(this.teleporterBlock.getActivationAreaDimensions().getX()));
		this.addSelectableChild(this.activationAreaDimensionsXField);

		this.activationAreaDimensionsYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.activationAreaDimensionsYField.setMaxLength(128);
		this.activationAreaDimensionsYField.setText(Integer.toString(this.teleporterBlock.getActivationAreaDimensions().getY()));
		this.addSelectableChild(this.activationAreaDimensionsYField);

		this.activationAreaDimensionsZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.activationAreaDimensionsZField.setMaxLength(128);
		this.activationAreaDimensionsZField.setText(Integer.toString(this.teleporterBlock.getActivationAreaDimensions().getZ()));
		this.addSelectableChild(this.activationAreaDimensionsZField);

		this.activationAreaPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 100, 20, Text.empty());
		this.activationAreaPositionOffsetXField.setMaxLength(128);
		this.activationAreaPositionOffsetXField.setText(Integer.toString(this.teleporterBlock.getActivationAreaPositionOffset().getX()));
		this.addSelectableChild(this.activationAreaPositionOffsetXField);

		this.activationAreaPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.activationAreaPositionOffsetYField.setMaxLength(128);
		this.activationAreaPositionOffsetYField.setText(Integer.toString(this.teleporterBlock.getActivationAreaPositionOffset().getY()));
		this.addSelectableChild(this.activationAreaPositionOffsetYField);

		this.activationAreaPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 115, 100, 20, Text.empty());
		this.activationAreaPositionOffsetZField.setMaxLength(128);
		this.activationAreaPositionOffsetZField.setText(Integer.toString(this.teleporterBlock.getActivationAreaPositionOffset().getZ()));
		this.addSelectableChild(this.activationAreaPositionOffsetZField);

		this.accessPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 150, 50, 20, Text.empty());
		this.accessPositionOffsetXField.setMaxLength(128);
		this.accessPositionOffsetXField.setText(Integer.toString(this.teleporterBlock.getAccessPositionOffset().getX()));
		this.addSelectableChild(this.accessPositionOffsetXField);

		this.accessPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 150, 50, 20, Text.empty());
		this.accessPositionOffsetYField.setMaxLength(128);
		this.accessPositionOffsetYField.setText(Integer.toString(this.teleporterBlock.getAccessPositionOffset().getY()));
		this.addSelectableChild(this.accessPositionOffsetYField);

		this.accessPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 46, 150, 50, 20, Text.empty());
		this.accessPositionOffsetZField.setMaxLength(128);
		this.accessPositionOffsetZField.setText(Integer.toString(this.teleporterBlock.getAccessPositionOffset().getZ()));
		this.addSelectableChild(this.accessPositionOffsetZField);

//        int i = this.textRenderer.getWidth(SET_ACCESS_POSITION_LABEL_TEXT) + 10;
		this.setAccessPosition = this.teleporterBlock.getSetAccessPosition();
		this.toggleSetAccessPositionButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_ON, TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_OFF).initially(this.setAccessPosition).omitKeyText().build(this.width / 2 + 8, 150, 150, 20, Text.empty(), (button, setAccessPosition) -> {
			this.setAccessPosition = setAccessPosition;
		}));

		this.onlyTeleportDimensionOwner = this.teleporterBlock.onlyTeleportDimensionOwner();
		this.toggleOnlyTeleportDimensionOwnerButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_ON, TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_OFF).initially(this.onlyTeleportDimensionOwner).omitKeyText().build(this.width / 2 - 154, 175, 150, 20, Text.empty(), (button, onlyTeleportDimensionOwner) -> {
			this.onlyTeleportDimensionOwner = onlyTeleportDimensionOwner;
		}));

		this.teleportTeam = this.teleporterBlock.teleportTeam();
		this.toggleTeleportTeamButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_ON, TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_OFF).initially(this.teleportTeam).omitKeyText().build(this.width / 2 + 4, 175, 150, 20, Text.empty(), (button, teleportTeam) -> {
			this.teleportTeam = teleportTeam;
		}));


		// --- teleportation mode page ---

		int i = this.textRenderer.getWidth(TELEPORTATION_MODE_LABEL_TEXT) + 10;
		this.teleportationModeButton = this.addDrawableChild(CyclingButtonWidget.builder(TeleporterBlockEntity.TeleportationMode::asText).values((TeleporterBlockEntity.TeleportationMode[]) TeleporterBlockEntity.TeleportationMode.values()).initially(this.teleportationMode).omitKeyText().build(this.width / 2 - 152 + i, 45, 300 - i, 20, TELEPORTATION_MODE_LABEL_TEXT, (button, teleportationMode) -> {
			this.teleportationMode = teleportationMode;
			this.updateWidgets();
		}));

		// teleportation mode: direct

		this.directTeleportPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 100, 20, Text.empty());
		this.directTeleportPositionOffsetXField.setMaxLength(128);
		this.directTeleportPositionOffsetXField.setText(Integer.toString(this.teleporterBlock.getDirectTeleportPositionOffset().getX()));
		this.addSelectableChild(this.directTeleportPositionOffsetXField);

		this.directTeleportPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 80, 100, 20, Text.empty());
		this.directTeleportPositionOffsetYField.setMaxLength(128);
		this.directTeleportPositionOffsetYField.setText(Integer.toString(this.teleporterBlock.getDirectTeleportPositionOffset().getY()));
		this.addSelectableChild(this.directTeleportPositionOffsetYField);

		this.directTeleportPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 80, 100, 20, Text.empty());
		this.directTeleportPositionOffsetZField.setMaxLength(128);
		this.directTeleportPositionOffsetZField.setText(Integer.toString(this.teleporterBlock.getDirectTeleportPositionOffset().getZ()));
		this.addSelectableChild(this.directTeleportPositionOffsetZField);

		this.directTeleportOrientationYawField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 115, 100, 20, Text.empty());
		this.directTeleportOrientationYawField.setMaxLength(128);
		this.directTeleportOrientationYawField.setText(Double.toString(this.teleporterBlock.getDirectTeleportOrientationYaw()));
		this.addSelectableChild(this.directTeleportOrientationYawField);

		this.directTeleportOrientationPitchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 115, 100, 20, Text.empty());
		this.directTeleportOrientationPitchField.setMaxLength(128);
		this.directTeleportOrientationPitchField.setText(Double.toString(this.teleporterBlock.getDirectTeleportOrientationPitch()));
		this.addSelectableChild(this.directTeleportOrientationPitchField);

		// teleportation mode: spawn_points

		this.spawnPointType = this.teleporterBlock.getSpawnPointType();
		i = this.textRenderer.getWidth(SPAWN_POINT_TYPE_LABEL_TEXT) + 10;
		this.spawnPointTypeButton = this.addDrawableChild(CyclingButtonWidget.builder(TeleporterBlockEntity.SpawnPointType::asText).values((TeleporterBlockEntity.SpawnPointType[]) TeleporterBlockEntity.SpawnPointType.values()).initially(this.spawnPointType).omitKeyText().build(this.width / 2 - 152 + i, 70, 300 - i, 20, SPAWN_POINT_TYPE_LABEL_TEXT, (button, locationType) -> {
			this.spawnPointType = locationType;
		}));

		// teleportation mode: locations

		this.removeLocationButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 70, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(0)));
		this.removeLocationButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 95, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(1)));
		this.removeLocationButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 120, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(2)));

		this.newLocationIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 4 - 150, 160, 150, 20, Text.empty());
		this.newLocationIdentifierField.setMaxLength(128);
		this.newLocationIdentifierField.setPlaceholder(Text.translatable("gui.teleporter_block.target_identifier_field.place_holder"));
		this.addSelectableChild(this.newLocationIdentifierField);

		this.newLocationEntranceField = new TextFieldWidget(this.textRenderer, this.width / 2 + 4, 160, 150, 20, Text.empty());
		this.newLocationEntranceField.setMaxLength(128);
		this.newLocationEntranceField.setPlaceholder(Text.translatable("gui.teleporter_block.target_entrance_field.place_holder"));
		this.addSelectableChild(this.newLocationEntranceField);

		this.newDataIdField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 185, 100, 20, Text.empty());
		this.newDataIdField.setMaxLength(128);
		this.newDataIdField.setPlaceholder(Text.translatable("gui.teleporter_block.new_data_id_field.place_holder"));
		this.addSelectableChild(this.newDataIdField);

		this.newDataField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 185, 100, 20, Text.empty());
		this.newDataField.setMaxLength(128);
		this.newDataField.setPlaceholder(Text.translatable("gui.teleporter_block.new_data_field.place_holder"));
		this.addSelectableChild(this.newDataField);

		this.addNewLocationButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_LOCATION_BUTTON_LABEL_TEXT, button -> this.addLocationToList(this.newLocationIdentifierField.getText(), this.newLocationEntranceField.getText(), this.newDataIdField.getText(), this.newDataField.getText())).dimensions(this.width / 2 - 154, 185, 100, 20).build());

		// --- status effect page ---

		this.removeStatusEffectButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStatusEffectFromStatusEffectList(0)));
		this.removeStatusEffectButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStatusEffectFromStatusEffectList(1)));
		this.removeStatusEffectButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStatusEffectFromStatusEffectList(2)));
		this.removeStatusEffectButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeStatusEffectFromStatusEffectList(3)));

		this.newStatusEffectField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 160, 300, 20, Text.empty());
		this.newStatusEffectField.setMaxLength(128);
		this.newStatusEffectField.setPlaceholder(NEW_STATUS_EFFECT_FIELD_TEXT);
		this.addSelectableChild(this.newStatusEffectField);

		this.addNewStatusEffectButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_STATUS_EFFECT_BUTTON_LABEL_TEXT, button -> this.addStatusEffectToList(this.newStatusEffectField.getText())).dimensions(this.width / 2 - 154, 185, 300, 20).build());

		// --- adventure screen customization page ---

		this.teleporterNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 44, 300, 20, Text.empty());
		this.teleporterNameField.setMaxLength(128);
		this.teleporterNameField.setPlaceholder(Text.translatable("gui.teleporter_block.teleporter_name_field.place_holder"));
		this.teleporterNameField.setText(this.teleporterBlock.getTeleporterName());
		this.addSelectableChild(this.teleporterNameField);

		this.currentTargetOwnerLabelField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 68, 300, 20, Text.empty());
		this.currentTargetOwnerLabelField.setMaxLength(128);
		this.currentTargetOwnerLabelField.setPlaceholder(Text.translatable("gui.teleporter_block.target_owner_field.place_holder"));
		this.currentTargetOwnerLabelField.setText(this.teleporterBlock.getCurrentTargetOwnerLabel());
		this.addSelectableChild(this.currentTargetOwnerLabelField);

		this.currentTargetIdentifierLabelField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 92, 300, 20, Text.empty());
		this.currentTargetIdentifierLabelField.setMaxLength(128);
		this.currentTargetIdentifierLabelField.setPlaceholder(Text.translatable("gui.teleporter_block.target_identifier_field.place_holder"));
		this.currentTargetIdentifierLabelField.setText(this.teleporterBlock.getCurrentTargetIdentifierLabel());
		this.addSelectableChild(this.currentTargetIdentifierLabelField);

		this.toggleShowRegenerateButtonButton = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(TOGGLE_SHOW_REGENERATE_BUTTON_BUTTON_LABEL_TEXT_ON, TOGGLE_SHOW_REGENERATE_BUTTON_BUTTON_LABEL_TEXT_OFF).initially(this.showRegenerateButton).omitKeyText().build(this.width / 2 - 154, 116, 300, 20, Text.empty(), (button, showRegenerateButton) -> {
			this.showRegenerateButton = showRegenerateButton;
		}));

		this.teleportButtonLabelField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 140, 300, 20, Text.empty());
		this.teleportButtonLabelField.setMaxLength(128);
		this.teleportButtonLabelField.setPlaceholder(Text.translatable("gui.teleporter_block.teleport_button.place_holder"));
		this.teleportButtonLabelField.setText(this.teleporterBlock.getTeleportButtonLabel());
		this.addSelectableChild(this.teleportButtonLabelField);

		this.cancelTeleportButtonLabelField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 300, 20, Text.empty());
		this.cancelTeleportButtonLabelField.setMaxLength(128);
		this.cancelTeleportButtonLabelField.setPlaceholder(Text.translatable("gui.teleporter_block.cancel_teleport_button.place_holder"));
		this.cancelTeleportButtonLabelField.setText(this.teleporterBlock.getCancelTeleportButtonLabel());
		this.addSelectableChild(this.cancelTeleportButtonLabelField);

		this.doneButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.cancelButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.creativeScreenPageButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.creativeScreenPageButton.visible = false;

		this.toggleShowAdventureScreenButton.visible = false;
		this.toggleShowActivationAreaButton.visible = false;
		this.activationAreaDimensionsXField.setVisible(false);
		this.activationAreaDimensionsYField.setVisible(false);
		this.activationAreaDimensionsZField.setVisible(false);
		this.activationAreaPositionOffsetXField.setVisible(false);
		this.activationAreaPositionOffsetYField.setVisible(false);
		this.activationAreaPositionOffsetZField.setVisible(false);
		this.accessPositionOffsetXField.setVisible(false);
		this.accessPositionOffsetYField.setVisible(false);
		this.accessPositionOffsetZField.setVisible(false);
		this.toggleSetAccessPositionButton.visible = false;
		this.toggleOnlyTeleportDimensionOwnerButton.visible = false;
		this.toggleTeleportTeamButton.visible = false;

		this.teleportationModeButton.visible = false;

		this.directTeleportPositionOffsetXField.setVisible(false);
		this.directTeleportPositionOffsetYField.setVisible(false);
		this.directTeleportPositionOffsetZField.setVisible(false);
		this.directTeleportOrientationYawField.setVisible(false);
		this.directTeleportOrientationPitchField.setVisible(false);

		this.spawnPointTypeButton.visible = false;

		this.removeLocationButton0.visible = false;
		this.removeLocationButton1.visible = false;
		this.removeLocationButton2.visible = false;

		this.newLocationIdentifierField.setVisible(false);
		this.newLocationEntranceField.setVisible(false);
		this.newDataIdField.setVisible(false);
		this.newDataField.setVisible(false);
		this.addNewLocationButton.visible = false;

		this.removeStatusEffectButton0.visible = false;
		this.removeStatusEffectButton1.visible = false;
		this.removeStatusEffectButton2.visible = false;
		this.removeStatusEffectButton3.visible = false;
		this.newStatusEffectField.setVisible(false);
		this.addNewStatusEffectButton.visible = false;

		this.teleporterNameField.setVisible(false);
		this.currentTargetIdentifierLabelField.setVisible(false);
		this.currentTargetOwnerLabelField.setVisible(false);
		this.toggleShowRegenerateButtonButton.visible = false;
		this.teleportButtonLabelField.setVisible(false);
		this.cancelTeleportButtonLabelField.setVisible(false);

		this.doneButton.visible = false;
		this.cancelButton.visible = false;

		this.creativeScreenPageButton.visible = true;

		if (this.screenPage == ScreenPage.ACTIVATION) {

			this.toggleShowAdventureScreenButton.visible = true;
			this.toggleShowActivationAreaButton.visible = true;
			this.activationAreaDimensionsXField.setVisible(true);
			this.activationAreaDimensionsYField.setVisible(true);
			this.activationAreaDimensionsZField.setVisible(true);
			this.activationAreaPositionOffsetXField.setVisible(true);
			this.activationAreaPositionOffsetYField.setVisible(true);
			this.activationAreaPositionOffsetZField.setVisible(true);
			this.accessPositionOffsetXField.setVisible(true);
			this.accessPositionOffsetYField.setVisible(true);
			this.accessPositionOffsetZField.setVisible(true);
			this.toggleSetAccessPositionButton.visible = true;
			this.toggleOnlyTeleportDimensionOwnerButton.visible = true;
			this.toggleTeleportTeamButton.visible = true;

		} else if (this.screenPage == ScreenPage.TELEPORTATION_MODE) {

			this.teleportationModeButton.visible = true;

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {

				this.directTeleportPositionOffsetXField.setVisible(true);
				this.directTeleportPositionOffsetYField.setVisible(true);
				this.directTeleportPositionOffsetZField.setVisible(true);
				this.directTeleportOrientationYawField.setVisible(true);
				this.directTeleportOrientationPitchField.setVisible(true);

			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {

				this.spawnPointTypeButton.visible = true;

			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				int index = 0;
				for (int i = 0; i < Math.min(3, this.locationsList.size()); i++) {
					if (index == 0) {
						this.removeLocationButton0.visible = true;
					} else if (index == 1) {
						this.removeLocationButton1.visible = true;
					} else if (index == 2) {
						this.removeLocationButton2.visible = true;
					}
					index++;
				}

				this.newLocationIdentifierField.setVisible(true);
				this.newLocationEntranceField.setVisible(true);
				this.newDataIdField.setVisible(true);
				this.newDataField.setVisible(true);
				this.addNewLocationButton.visible = true;

			}
		} else if (this.screenPage == ScreenPage.STATUS_EFFECTS_TO_DECREMENT) {

			int index = 0;
			for (int i = 0; i < Math.min(VISIBLE_STATUS_EFFECT_LIST_ENTRIES, this.statusEffectsToDecrementLevelOnTeleport.size()); i++) {
				if (index == 0) {
					this.removeStatusEffectButton0.visible = true;
				} else if (index == 1) {
					this.removeStatusEffectButton1.visible = true;
				} else if (index == 2) {
					this.removeStatusEffectButton2.visible = true;
				} else if (index == 3) {
					this.removeStatusEffectButton3.visible = true;
				}
				index++;
			}

			this.newStatusEffectField.setVisible(true);
			this.addNewStatusEffectButton.visible = true;

		} else if (this.screenPage == ScreenPage.ADVENTURE_SCREEN_CUSTOMIZATION) {

			this.teleporterNameField.setVisible(true);

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				this.currentTargetIdentifierLabelField.setVisible(true);
				this.currentTargetOwnerLabelField.setVisible(true);
			}

			this.toggleShowRegenerateButtonButton.visible = true;
			this.teleportButtonLabelField.setVisible(true);
			this.cancelTeleportButtonLabelField.setVisible(true);

		}

		this.doneButton.visible = true;
		this.cancelButton.visible = true;

		this.creativeLocationsListScrollPosition = 0;
		this.statusEffectListScrollPosition = 0;
		this.statusEffectListScrollAmount = 0.0f;
		this.creativeLocationsListScrollAmount = 0.0f;
		this.creativeLocationsListMouseClicked = false;
		this.statusEffectListMouseClicked = false;

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		ScreenPage var = this.screenPage;
		TeleporterBlockEntity.TeleportationMode var1 = this.teleportationMode;
		TeleporterBlockEntity.SpawnPointType var2 = this.spawnPointType;
		boolean bool = this.showActivationArea;
		boolean bool1 = this.showAdventureScreen;
		boolean bool2 = this.setAccessPosition;
		boolean bool3 = this.onlyTeleportDimensionOwner;
		boolean bool4 = this.teleportTeam;
		boolean bool5 = this.showRegenerateButton;
		String string0 = this.activationAreaDimensionsXField.getText();
		String string1 = this.activationAreaDimensionsYField.getText();
		String string2 = this.activationAreaDimensionsZField.getText();
		String string3 = this.activationAreaPositionOffsetXField.getText();
		String string4 = this.activationAreaPositionOffsetYField.getText();
		String string5 = this.activationAreaPositionOffsetZField.getText();
		String string6 = this.accessPositionOffsetXField.getText();
		String string7 = this.accessPositionOffsetYField.getText();
		String string8 = this.accessPositionOffsetZField.getText();
		String string9 = this.directTeleportPositionOffsetXField.getText();
		String string10 = this.directTeleportPositionOffsetYField.getText();
		String string11 = this.directTeleportPositionOffsetZField.getText();
		String string12 = this.directTeleportOrientationYawField.getText();
		String string13 = this.directTeleportOrientationPitchField.getText();
		String string14 = this.newLocationIdentifierField.getText();
		String string15 = this.newLocationEntranceField.getText();
		String string16 = this.newDataIdField.getText();
		String string17 = this.newDataField.getText();
		String string18 = this.newStatusEffectField.getText();
		String string19 = this.teleporterNameField.getText();
		String string20 = this.currentTargetOwnerLabelField.getText();
		String string21 = this.currentTargetIdentifierLabelField.getText();
		String string22 = this.teleportButtonLabelField.getText();
		String string23 = this.cancelTeleportButtonLabelField.getText();
		List<String> list = new ArrayList<>(this.statusEffectsToDecrementLevelOnTeleport);
		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> list1 = new ArrayList<>(this.locationsList);
		this.init(client, width, height);
		this.screenPage = var;
		this.teleportationMode = var1;
		this.spawnPointType = var2;
		this.showActivationArea = bool;
		this.showAdventureScreen = bool1;
		this.setAccessPosition = bool2;
		this.onlyTeleportDimensionOwner = bool3;
		this.teleportTeam = bool4;
		this.showRegenerateButton = bool5;
		this.activationAreaDimensionsXField.setText(string0);
		this.activationAreaDimensionsYField.setText(string1);
		this.activationAreaDimensionsZField.setText(string2);
		this.activationAreaPositionOffsetXField.setText(string3);
		this.activationAreaPositionOffsetYField.setText(string4);
		this.activationAreaPositionOffsetZField.setText(string5);
		this.accessPositionOffsetXField.setText(string6);
		this.accessPositionOffsetYField.setText(string7);
		this.accessPositionOffsetZField.setText(string8);
		this.directTeleportPositionOffsetXField.setText(string9);
		this.directTeleportPositionOffsetYField.setText(string10);
		this.directTeleportPositionOffsetZField.setText(string11);
		this.directTeleportOrientationYawField.setText(string12);
		this.directTeleportOrientationPitchField.setText(string13);
		this.newLocationIdentifierField.setText(string14);
		this.newLocationEntranceField.setText(string15);
		this.newDataIdField.setText(string16);
		this.newDataField.setText(string17);
		this.newStatusEffectField.setText(string18);
		this.teleporterNameField.setText(string19);
		this.currentTargetOwnerLabelField.setText(string20);
		this.currentTargetIdentifierLabelField.setText(string21);
		this.teleportButtonLabelField.setText(string22);
		this.cancelTeleportButtonLabelField.setText(string23);
		this.statusEffectsToDecrementLevelOnTeleport.clear();
		this.locationsList.clear();
		this.statusEffectsToDecrementLevelOnTeleport.addAll(list);
		this.locationsList.addAll(list1);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.creativeLocationsListMouseClicked = false;
		this.statusEffectListMouseClicked = false;
		int i;
		int j;
		if (this.screenPage == ScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3) {
			i = this.width / 2 - 152;
			j = 71;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 68)) {
				this.creativeLocationsListMouseClicked = true;
			}
		}
		if (this.screenPage == ScreenPage.STATUS_EFFECTS_TO_DECREMENT
				&& this.statusEffectsToDecrementLevelOnTeleport.size() > VISIBLE_STATUS_EFFECT_LIST_ENTRIES) {
			i = this.width / 2 - 152;
			j = 44;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 112)) {
				this.statusEffectListMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.screenPage == ScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3
				&& this.creativeLocationsListMouseClicked) {
			int i = this.locationsList.size() - 3;
			float f = (float) deltaY / (float) i;
			this.creativeLocationsListScrollAmount = MathHelper.clamp(this.creativeLocationsListScrollAmount + f, 0.0f, 1.0f);
			this.creativeLocationsListScrollPosition = (int) ((double) (this.creativeLocationsListScrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STATUS_EFFECTS_TO_DECREMENT
				&& this.statusEffectsToDecrementLevelOnTeleport.size() > VISIBLE_STATUS_EFFECT_LIST_ENTRIES
				&& this.statusEffectListMouseClicked) {
			int i = this.statusEffectsToDecrementLevelOnTeleport.size() - VISIBLE_STATUS_EFFECT_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.statusEffectListScrollAmount = MathHelper.clamp(this.statusEffectListScrollAmount + f, 0.0f, 1.0f);
			this.statusEffectListScrollPosition = (int) ((double) (this.statusEffectListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.screenPage == ScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 70 && mouseY <= 140) {
			int i = this.locationsList.size() - 3;
			float f = (float) verticalAmount / (float) i;
			this.creativeLocationsListScrollAmount = MathHelper.clamp(this.creativeLocationsListScrollAmount - f, 0.0f, 1.0f);
			this.creativeLocationsListScrollPosition = (int) ((double) (this.creativeLocationsListScrollAmount * (float) i));
		}
		if (this.screenPage == ScreenPage.STATUS_EFFECTS_TO_DECREMENT
				&& this.statusEffectsToDecrementLevelOnTeleport.size() > VISIBLE_STATUS_EFFECT_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 44 && mouseY <= 112) {
			int i = this.statusEffectsToDecrementLevelOnTeleport.size() - VISIBLE_STATUS_EFFECT_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.statusEffectListScrollAmount = MathHelper.clamp(this.statusEffectListScrollAmount - f, 0.0f, 1.0f);
			this.statusEffectListScrollPosition = (int) ((double) (this.statusEffectListScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.done();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.screenPage == ScreenPage.ACTIVATION) {
			context.drawTextWithShadow(this.textRenderer, ACTIVATION_AREA_DIMENSIONS_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
			this.activationAreaDimensionsXField.render(context, mouseX, mouseY, delta);
			this.activationAreaDimensionsYField.render(context, mouseX, mouseY, delta);
			this.activationAreaDimensionsZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, ACTIVATION_AREA_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
			this.activationAreaPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.activationAreaPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.activationAreaPositionOffsetZField.render(context, mouseX, mouseY, delta);
			context.drawTextWithShadow(this.textRenderer, ACCESS_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 140, 0xA0A0A0);
			this.accessPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.accessPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.accessPositionOffsetZField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.TELEPORTATION_MODE) {
			context.drawTextWithShadow(this.textRenderer, TELEPORTATION_MODE_LABEL_TEXT, this.width / 2 - 153, 51, 0xA0A0A0);
			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
				context.drawTextWithShadow(this.textRenderer, DIRECT_TELEPORT_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
				this.directTeleportPositionOffsetXField.render(context, mouseX, mouseY, delta);
				this.directTeleportPositionOffsetYField.render(context, mouseX, mouseY, delta);
				this.directTeleportPositionOffsetZField.render(context, mouseX, mouseY, delta);
				context.drawTextWithShadow(this.textRenderer, DIRECT_TELEPORT_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
				this.directTeleportOrientationYawField.render(context, mouseX, mouseY, delta);
				this.directTeleportOrientationPitchField.render(context, mouseX, mouseY, delta);
			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {
				context.drawTextWithShadow(this.textRenderer, SPAWN_POINT_TYPE_LABEL_TEXT, this.width / 2 - 153, 76, 0xA0A0A0);
			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				for (int i = this.creativeLocationsListScrollPosition; i < Math.min(this.creativeLocationsListScrollPosition + 3, this.locationsList.size()); i++) {
					String text = this.locationsList.get(i).getLeft().getLeft();
					if (!this.locationsList.get(i).getLeft().getRight().isEmpty()) {
						text = text + ", " + this.locationsList.get(i).getLeft().getRight();
					}
					if (!this.locationsList.get(i).getRight().getLeft().isEmpty()) {
						text = text + ", " + this.locationsList.get(i).getRight().getLeft() + ", " + this.locationsList.get(i).getRight().getRight();
					}
					context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 76 + ((i - this.creativeLocationsListScrollPosition) * 25), 0xA0A0A0);
				}
				if (this.locationsList.size() > 3) {
					context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 70, 8, 70);
					int k = (int) (61.0f * this.creativeLocationsListScrollAmount);
					context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 70 + 1 + k, 6, 7);
				}
				this.newLocationIdentifierField.render(context, mouseX, mouseY, delta);
				this.newLocationEntranceField.render(context, mouseX, mouseY, delta);
				this.newDataIdField.render(context, mouseX, mouseY, delta);
				this.newDataField.render(context, mouseX, mouseY, delta);
			}
		} else if (this.screenPage == ScreenPage.STATUS_EFFECTS_TO_DECREMENT) {
			for (int i = this.statusEffectListScrollPosition; i < Math.min(this.statusEffectListScrollPosition + VISIBLE_STATUS_EFFECT_LIST_ENTRIES, this.statusEffectsToDecrementLevelOnTeleport.size()); i++) {
				String text = this.statusEffectsToDecrementLevelOnTeleport.get(i);
				context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 50 + ((i - this.statusEffectListScrollPosition) * 24), 0xA0A0A0);
			}
			if (this.statusEffectsToDecrementLevelOnTeleport.size() > VISIBLE_STATUS_EFFECT_LIST_ENTRIES) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_88_TEXTURE, this.width / 2 - 153, 44, 8, 88);
				int k = (int) (79.0f * this.statusEffectListScrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
			}
			context.drawTextWithShadow(this.textRenderer, NEW_STATUS_EFFECT_FIELD_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			this.newStatusEffectField.render(context, mouseX, mouseY, delta);
		} else if (this.screenPage == ScreenPage.ADVENTURE_SCREEN_CUSTOMIZATION) {

			this.teleporterNameField.render(context, mouseX, mouseY, delta);

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				this.currentTargetIdentifierLabelField.render(context, mouseX, mouseY, delta);
				this.currentTargetOwnerLabelField.render(context, mouseX, mouseY, delta);

			}

			this.teleportButtonLabelField.render(context, mouseX, mouseY, delta);
			this.cancelTeleportButtonLabelField.render(context, mouseX, mouseY, delta);
		}
	}

	private void updateTeleporterBlock() {

		BlockPos directTeleportPositionOffset;
		double directTeleportPositionOffsetYaw;
		double directTeleportPositionOffsetPitch;
		if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
			directTeleportPositionOffset = new BlockPos(
					ItemUtils.parseInt(this.directTeleportPositionOffsetXField.getText()),
					ItemUtils.parseInt(this.directTeleportPositionOffsetYField.getText()),
					ItemUtils.parseInt(this.directTeleportPositionOffsetZField.getText())
			);

			directTeleportPositionOffsetYaw = ItemUtils.parseDouble(this.directTeleportOrientationYawField.getText());
			directTeleportPositionOffsetPitch = ItemUtils.parseDouble(this.directTeleportOrientationPitchField.getText());
		} else {
			directTeleportPositionOffset = new BlockPos(0, 0, 0);
			directTeleportPositionOffsetYaw = 0.0;
			directTeleportPositionOffsetPitch = 0.0;
		}
		TeleporterBlockEntity.SpawnPointType spawnPointType;
		if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {

			spawnPointType = this.spawnPointType;

		} else {
			spawnPointType = TeleporterBlockEntity.SpawnPointType.WORLD_SPAWN;
		}

		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList = new ArrayList<>();
		if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
			locationsList = this.locationsList;
		}

		ClientPlayNetworking.send(new UpdateTeleporterBlockPacket(
				this.teleporterBlock.getPos(),
				this.showActivationArea,
				this.showAdventureScreen,
				new Vec3i(
						ItemUtils.parseInt(this.activationAreaDimensionsXField.getText()),
						ItemUtils.parseInt(this.activationAreaDimensionsYField.getText()),
						ItemUtils.parseInt(this.activationAreaDimensionsZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.activationAreaPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.activationAreaPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.activationAreaPositionOffsetZField.getText())
				),
				new BlockPos(
						ItemUtils.parseInt(this.accessPositionOffsetXField.getText()),
						ItemUtils.parseInt(this.accessPositionOffsetYField.getText()),
						ItemUtils.parseInt(this.accessPositionOffsetZField.getText())
				),
				this.setAccessPosition,
				this.statusEffectsToDecrementLevelOnTeleport,
				this.onlyTeleportDimensionOwner,
				this.teleportTeam,
				this.teleportationMode.asString(),
				directTeleportPositionOffset,
				directTeleportPositionOffsetYaw,
				directTeleportPositionOffsetPitch,
				spawnPointType.asString(),
				locationsList,
				this.teleporterNameField.getText(),
				this.currentTargetIdentifierLabelField.getText(),
				this.currentTargetOwnerLabelField.getText(),
				this.showRegenerateButton,
				this.teleportButtonLabelField.getText(),
				this.cancelTeleportButtonLabelField.getText()
		));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	public enum ScreenPage implements StringIdentifiable {
		ACTIVATION("activation"),
		TELEPORTATION_MODE("teleportation_mode"),
		STATUS_EFFECTS_TO_DECREMENT("status_effect_to_decrement"),
		ADVENTURE_SCREEN_CUSTOMIZATION("adventure_screen_customization");

		private final String name;

		ScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<ScreenPage> byName(String name) {
			return Arrays.stream(ScreenPage.values()).filter(screenPage -> screenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.teleporter_block.screenPage." + this.name);
		}
	}
}
