package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class CreativeTeleporterBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT = Component.translatable("gui.teleporter_block.prevent_interaction_identifier_string_label");
	private static final Component INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT = Component.translatable("gui.teleporter_block.interaction_prevented_message_label");
	private static final Component ONLY_DIMENSION_OWNER_CAN_TELEPORT_MESSAGE_LABEL_TEXT = Component.translatable("gui.teleporter_block.only_dimension_owner_can_teleport_message_label");
	private static final Component ACCESS_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.teleporter_block.access_position_offset_label");
	private static final Component UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.uuid_list_provider.uuid_list_provider_position_offset");
	private static final Component TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.teleporter_block.toggle_set_access_position_button_label.on");
	private static final Component TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.teleporter_block.toggle_set_access_position_button_label.off");
	private static final Component TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.teleporter_block.toggle_only_teleport_dimension_owner_button_label.on");
	private static final Component TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.teleporter_block.toggle_only_teleport_dimension_owner_button_label.off");
	private static final Component TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.teleporter_block.toggle_teleport_team_button_label.on");
	private static final Component TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.teleporter_block.toggle_teleport_team_button_label.off");
	private static final Component TELEPORTATION_MODE_LABEL_TEXT = Component.translatable("gui.teleporter_block.teleportation_mode_label");
	private static final Component DIRECT_TELEPORT_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.teleporter_block.direct_teleport_position_offset_label");
	private static final Component DIRECT_TELEPORT_ORIENTATION_LABEL_TEXT = Component.translatable("gui.teleporter_block.direct_teleport_orientation_label");
	private static final Component SPAWN_POINT_TYPE_LABEL_TEXT = Component.translatable("gui.teleporter_block.spawn_point_type_label");
	private static final Component ADD_NEW_LOCATION_BUTTON_LABEL_TEXT = Component.translatable("gui.teleporter_block.add_new_location_button_label");

	private static final Component LOCATION_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_identifier_label");
	private static final Component LOCATION_ENTRANCE_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_entrance_label");
	private static final Component LOCATION_DATA_ID_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_data_id_label");
	private static final Component LOCATION_ENTRANCE_DATA_ID_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_entrance_data_id_label");
	private static final Component LOCATION_DATA_IDENTIFIER_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_data_identifier_label");
	private static final Component LOCATION_DATA_LABEL_TEXT = Component.translatable("gui.teleporter_block.location_data_label");
	private static final Component SEND_DATA_ID_LABEL_TEXT = Component.translatable("gui.teleporter_block.send_data_id_label");
	private static final Component SEND_DATA_VALUE_LABEL_TEXT = Component.translatable("gui.teleporter_block.send_data_value_label");
	private static final Component DATA_PROVIDING_BLOCK_POS_OFFSET_LABEL_TEXT = Component.translatable("gui.teleporter_block.data_providing_block_pos_offset_label");

	private static final Component PRE_TELEPORT_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.teleporter_block.pre_teleport_triggered_block_position_offset_label");
	private static final Component ON_TELEPORT_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.teleporter_block.on_teleport_uuid_list_provider_position_offset");
	private static final Component POST_TELEPORT_TRIGGERED_BLOCK_POSITION_TEXT = Component.translatable("gui.teleporter_block.post_teleport_triggered_block_position_offset_label");

	private static final Component SHOW_ADVENTURE_SCREEN_FALSE_LABEL_TEXT = Component.translatable("gui.teleporter_block.show_adventure_screen_label.false");
	private static final Component SHOW_ADVENTURE_SCREEN_TRUE_LABEL_TEXT = Component.translatable("gui.teleporter_block.show_adventure_screen_label.true");
	private static final Component TOGGLE_SHOW_CANCEL_BUTTON_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.teleporter_block.toggle_show_cancel_button_button_label.on");
	private static final Component TOGGLE_SHOW_CANCEL_BUTTON_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.teleporter_block.toggle_show_cancel_button_button_label.off");
	private static final Component TOGGLE_CAN_OWNER_BE_CHOSEN_BUTTON_LABEL_TEXT_ON = Component.translatable("gui.teleporter_block.toggle_can_owner_be_chosen_button_label.on");
	private static final Component TOGGLE_CAN_OWNER_BE_CHOSEN_BUTTON_LABEL_TEXT_OFF = Component.translatable("gui.teleporter_block.toggle_can_owner_be_chosen_button_label.off");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_70_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_70");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);

	private final TeleporterBlockEntity teleporterBlock;

	private CycleButton<TeleporterBlockEntity.CreativeScreenPage> cycleScreenPageButton;
	private EditBox preventInteractionIdentifierStringField;
	private EditBox interactionPreventedMessageField;
	private EditBox uuidListProviderBlockPositionOffsetXField;
	private EditBox uuidListProviderBlockPositionOffsetYField;
	private EditBox uuidListProviderBlockPositionOffsetZField;
	private EditBox accessPositionOffsetXField;
	private EditBox accessPositionOffsetYField;
	private EditBox accessPositionOffsetZField;
	private CycleButton<Boolean> toggleSetAccessPositionButton;
	private CycleButton<Boolean> toggleOnlyTeleportDimensionOwnerButton;
	private CycleButton<Boolean> toggleTeleportTeamButton;
	private EditBox onlyDimensionOwnerCanTeleportMessageField;
	private CycleButton<TeleporterBlockEntity.TeleportationMode> teleportationModeButton;
	private EditBox directTeleportPositionOffsetXField;
	private EditBox directTeleportPositionOffsetYField;
	private EditBox directTeleportPositionOffsetZField;
	private EditBox directTeleportOrientationYawField;
	private EditBox directTeleportOrientationPitchField;
	private CycleButton<TeleporterBlockEntity.SpawnPointType> spawnPointTypeButton;
	private Button removeLocationButton0;
	private Button removeLocationButton1;
	private Button removeLocationButton2;
	private EditBox newLocationIdentifierField;
	private EditBox newLocationEntranceField;
	private EditBox newDataIdField;
	private EditBox newDataField;
	private Button addNewLocationButton;
	private CycleButton<LocationModeScreenPage> locationModeScreenPageButton;
	private EditBox locationIdentifierField;
	private EditBox locationEntranceField;
	private EditBox locationDataIdField;
	private EditBox locationDataField;
	private EditBox dataProvidingBlockPosOffsetXField;
	private EditBox dataProvidingBlockPosOffsetYField;
	private EditBox dataProvidingBlockPosOffsetZField;
	private EditBox locationDataIdentifierField;
	private EditBox entranceDataIdentifierField;
	private EditBox sendDataIdentifierDataIdentifierField;
	private EditBox sendDataValueDataIdentifierField;
	private EditBox preTeleportTriggeredBlockPositionOffsetXField;
	private EditBox preTeleportTriggeredBlockPositionOffsetYField;
	private EditBox preTeleportTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> preTeleportToggleTriggeredBlockResetsButton;
	private boolean preTeleportTriggeredBlockResets;
	private EditBox onTeleportUUIDListHandlerPositionOffsetXField;
	private EditBox onTeleportUUIDListHandlerPositionOffsetYField;
	private EditBox onTeleportUUIDListHandlerPositionOffsetZField;
	private EditBox postTeleportTriggeredBlockPositionOffsetXField;
	private EditBox postTeleportTriggeredBlockPositionOffsetYField;
	private EditBox postTeleportTriggeredBlockPositionOffsetZField;
	private CycleButton<Boolean> postTeleportToggleTriggeredBlockResetsButton;
	private boolean postTeleportTriggeredBlockResets;
	private CycleButton<Boolean> toggleShowAdventureScreenButton;
	private EditBox teleporterNameField;
	private EditBox currentTargetOwnerLabelField;
	private EditBox currentTargetIdentifierLabelField;
	private CycleButton<Boolean> toggleCanOwnerBeChosenButton;
	private CycleButton<Boolean> toggleShowCancelButtonButton;
	private EditBox teleportButtonLabelField;
	private EditBox cancelTeleportButtonLabelField;

	private TeleporterBlockEntity.CreativeScreenPage creativeScreenPage;
	private LocationModeScreenPage locationModeScreenPage;
	private boolean showAdventureScreen;
	private boolean setAccessPosition;
	private boolean onlyTeleportDimensionOwner;
	private boolean teleportTeam;
	private boolean showCancelButton;
	private boolean canOwnerBeChosen;

	private TeleporterBlockEntity.TeleportationMode teleportationMode;
	private TeleporterBlockEntity.SpawnPointType spawnPointType;

	private final List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> locationsList = new ArrayList<>();

	private int creativeLocationsListScrollPosition = 0;
	private float creativeLocationsListScrollAmount = 0.0f;
	private boolean creativeLocationsListMouseClicked = false;

	public CreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlock) {
		super(GameNarrator.NO_TITLE);
		this.teleporterBlock = teleporterBlock;
	}

	private void addLocationToList(String identifier, String entrance, String dataId, String data) {
		Component message = Component.literal("");

		Location location = null;
		Level world = this.teleporterBlock.getWorld();
		if (world != null) {
			Optional<Holder.Reference<Location>> optionalLocationReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(identifier));
			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}
		}

		if (location != null) {
			if (!LocationUtils.hasEntrance(location, entrance)) {
				entrance = "";
			}
			boolean bl = false;
			for (MutablePair<MutablePair<String, String>, MutablePair<String, String>> locationsListEntry : this.locationsList) {
				if (locationsListEntry.getLeft().getLeft().equals(identifier) && locationsListEntry.getLeft().getRight().equals(entrance) && locationsListEntry.getRight().getLeft().equals(dataId) && locationsListEntry.getRight().getRight().equals(data)) {
					bl = true;
					break;
				}
			}
			if (bl) {
				message = Component.translatable("gui.teleporter_block.location_already_in_list");
			} else {
				this.locationsList.add(new MutablePair<>(new MutablePair<>(identifier, entrance), new MutablePair<>(dataId, data)));
			}
		} else {
			message = Component.translatable("gui.teleporter_block.location_not_found");
		}
		if (this.minecraft != null && this.minecraft.player != null && !message.getString().isEmpty()) {
			this.minecraft.player.displayClientMessage(message);
		}
		this.updateWidgets();
	}

	private void removeLocationFromLocationList(int index) {
		if (index + this.creativeLocationsListScrollPosition < this.locationsList.size()) {
			this.locationsList.remove(index + this.creativeLocationsListScrollPosition);
		}
		this.updateWidgets();
	}

	@Override
	protected void cancel() {
		this.teleporterBlock.setShowAdventureScreen(this.showAdventureScreen);
		this.teleporterBlock.setEnableLocationAccessPoint(this.setAccessPosition);
		this.teleporterBlock.setOnlyTeleportDimensionOwner(this.onlyTeleportDimensionOwner);
		this.teleporterBlock.setTeleportTeam(this.teleportTeam);
		this.teleporterBlock.setShowCancelButton(this.showCancelButton);
		this.teleporterBlock.setCanOwnerBeChosen(this.canOwnerBeChosen);
		this.teleporterBlock.setTeleportationMode(this.teleportationMode);
		this.teleporterBlock.setSpawnPointType(this.spawnPointType);
		this.onClose();
	}

	@Override
	protected void init() {
		this.creativeScreenPage = this.teleporterBlock.getCreativeScreenPage();
		this.locationsList.addAll(this.teleporterBlock.getLocationsList());
		this.showAdventureScreen = this.teleporterBlock.getShowAdventureScreen();
		this.teleportationMode = this.teleporterBlock.getTeleportationMode();
		this.showCancelButton = this.teleporterBlock.showCancelButton();
		this.canOwnerBeChosen = this.teleporterBlock.canOwnerBeChosen();
		this.locationModeScreenPage = LocationModeScreenPage.PAGE_1;

		super.init();

		this.cycleScreenPageButton = this.addRenderableWidget(CycleButton.builder(TeleporterBlockEntity.CreativeScreenPage::asText).values((TeleporterBlockEntity.CreativeScreenPage[]) TeleporterBlockEntity.CreativeScreenPage.values()).initially(this.creativeScreenPage).omitKeyText().build(this.width / 2 - 154, 20, 300, 20, Component.empty(), (button, creativeScreenPage) -> {
			this.creativeScreenPage = creativeScreenPage;
			this.updateWidgets();
		}));

		// --- activation page ---

		this.preventInteractionIdentifierStringField = new EditBox(this.font, this.width / 2 - 154, 55, 300, 20, Component.empty());
		this.preventInteractionIdentifierStringField.setMaxLength(128);
		this.preventInteractionIdentifierStringField.setValue(this.teleporterBlock.getPreventInteractionIdentifierString());
		this.addWidget(this.preventInteractionIdentifierStringField);

		this.interactionPreventedMessageField = new EditBox(this.font, this.width / 2 - 154, 90, 300, 20, Component.empty());
		this.interactionPreventedMessageField.setMaxLength(128);
		this.interactionPreventedMessageField.setValue(this.teleporterBlock.getInteractionPreventedMessage());
		this.addWidget(this.interactionPreventedMessageField);

		this.uuidListProviderBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 50, 20, Component.empty());
		this.uuidListProviderBlockPositionOffsetXField.setMaxLength(128);
		this.uuidListProviderBlockPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getUuidListHandlerPositionOffset().getX()));
		this.addWidget(this.uuidListProviderBlockPositionOffsetXField);

		this.uuidListProviderBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 125, 50, 20, Component.empty());
		this.uuidListProviderBlockPositionOffsetYField.setMaxLength(128);
		this.uuidListProviderBlockPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getUuidListHandlerPositionOffset().getY()));
		this.addWidget(this.uuidListProviderBlockPositionOffsetYField);

		this.uuidListProviderBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 125, 50, 20, Component.empty());
		this.uuidListProviderBlockPositionOffsetZField.setMaxLength(128);
		this.uuidListProviderBlockPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getUuidListHandlerPositionOffset().getZ()));
		this.addWidget(this.uuidListProviderBlockPositionOffsetZField);

		this.teleportTeam = this.teleporterBlock.teleportTeam();
		this.toggleTeleportTeamButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_ON, TOGGLE_TELEPORT_TEAM_BUTTON_LABEL_TEXT_OFF).initially(this.teleportTeam).omitKeyText().build(this.width / 2 + 8, 125, 150, 20, Component.empty(), (button, teleportTeam) -> {
			this.teleportTeam = teleportTeam;
		}));

		this.accessPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 160, 50, 20, Component.empty());
		this.accessPositionOffsetXField.setMaxLength(128);
		this.accessPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getLocationAccessPoint().pos().getX()));
		this.addWidget(this.accessPositionOffsetXField);

		this.accessPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 160, 50, 20, Component.empty());
		this.accessPositionOffsetYField.setMaxLength(128);
		this.accessPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getLocationAccessPoint().pos().getY()));
		this.addWidget(this.accessPositionOffsetYField);

		this.accessPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 160, 50, 20, Component.empty());
		this.accessPositionOffsetZField.setMaxLength(128);
		this.accessPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getLocationAccessPoint().pos().getZ()));
		this.addWidget(this.accessPositionOffsetZField);

		this.setAccessPosition = this.teleporterBlock.getEnableLocationAccessPoint();
		this.toggleSetAccessPositionButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_ON, TOGGLE_SET_ACCESS_POSITION_BUTTON_LABEL_TEXT_OFF).initially(this.setAccessPosition).omitKeyText().build(this.width / 2 + 8, 160, 150, 20, Component.empty(), (button, setAccessPosition) -> {
			this.setAccessPosition = setAccessPosition;
		}));

		this.onlyTeleportDimensionOwner = this.teleporterBlock.onlyTeleportDimensionOwner();
		this.toggleOnlyTeleportDimensionOwnerButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_ON, TOGGLE_ONLY_TELEPORT_DIMENSION_OWNER_BUTTON_LABEL_TEXT_OFF).initially(this.onlyTeleportDimensionOwner).omitKeyText().build(this.width / 2 - 154, 195, 150, 20, Component.empty(), (button, onlyTeleportDimensionOwner) -> {
			this.onlyTeleportDimensionOwner = onlyTeleportDimensionOwner;
		}));

		this.onlyDimensionOwnerCanTeleportMessageField = new EditBox(this.font, this.width / 2 + 4, 195, 150, 20, Component.empty());
		this.onlyDimensionOwnerCanTeleportMessageField.setMaxLength(128);
		this.onlyDimensionOwnerCanTeleportMessageField.setValue(this.teleporterBlock.getOnlyDimensionOwnerCanTeleportMessage());
		this.addWidget(this.onlyDimensionOwnerCanTeleportMessageField);


		// --- teleportation mode page ---

		int i = this.font.width(TELEPORTATION_MODE_LABEL_TEXT) + 10;
		this.teleportationModeButton = this.addRenderableWidget(CycleButton.builder(TeleporterBlockEntity.TeleportationMode::asText).values((TeleporterBlockEntity.TeleportationMode[]) TeleporterBlockEntity.TeleportationMode.values()).initially(this.teleportationMode).omitKeyText().build(this.width / 2 - 152 + i, 45, 300 - i, 20, TELEPORTATION_MODE_LABEL_TEXT, (button, teleportationMode) -> {
			this.teleportationMode = teleportationMode;
			this.updateWidgets();
		}));

		// teleportation mode: direct

		this.directTeleportPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 80, 100, 20, Component.empty());
		this.directTeleportPositionOffsetXField.setMaxLength(128);
		this.directTeleportPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getDirectTeleportTarget().pos().getX()));
		this.addWidget(this.directTeleportPositionOffsetXField);

		this.directTeleportPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 80, 100, 20, Component.empty());
		this.directTeleportPositionOffsetYField.setMaxLength(128);
		this.directTeleportPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getDirectTeleportTarget().pos().getY()));
		this.addWidget(this.directTeleportPositionOffsetYField);

		this.directTeleportPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 80, 100, 20, Component.empty());
		this.directTeleportPositionOffsetZField.setMaxLength(128);
		this.directTeleportPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getDirectTeleportTarget().pos().getZ()));
		this.addWidget(this.directTeleportPositionOffsetZField);

		this.directTeleportOrientationYawField = new EditBox(this.font, this.width / 2 - 154, 115, 100, 20, Component.empty());
		this.directTeleportOrientationYawField.setMaxLength(128);
		this.directTeleportOrientationYawField.setValue(Double.toString(this.teleporterBlock.getDirectTeleportTarget().yaw()));
		this.addWidget(this.directTeleportOrientationYawField);

		this.directTeleportOrientationPitchField = new EditBox(this.font, this.width / 2 - 50, 115, 100, 20, Component.empty());
		this.directTeleportOrientationPitchField.setMaxLength(128);
		this.directTeleportOrientationPitchField.setValue(Double.toString(this.teleporterBlock.getDirectTeleportTarget().pitch()));
		this.addWidget(this.directTeleportOrientationPitchField);

		// teleportation mode: spawn_points

		this.spawnPointType = this.teleporterBlock.getSpawnPointType();
		i = this.font.width(SPAWN_POINT_TYPE_LABEL_TEXT) + 10;
		this.spawnPointTypeButton = this.addRenderableWidget(CycleButton.builder(TeleporterBlockEntity.SpawnPointType::asText).values((TeleporterBlockEntity.SpawnPointType[]) TeleporterBlockEntity.SpawnPointType.values()).initially(this.spawnPointType).omitKeyText().build(this.width / 2 - 152 + i, 70, 300 - i, 20, SPAWN_POINT_TYPE_LABEL_TEXT, (button, locationType) -> {
			this.spawnPointType = locationType;
		}));

		// teleportation mode: locations

		this.removeLocationButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 70, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(0)));
		this.removeLocationButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 95, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(1)));
		this.removeLocationButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 120, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeLocationFromLocationList(2)));

		this.newLocationIdentifierField = new EditBox(this.font, this.width / 2 - 4 - 150, 160, 150, 20, Component.empty());
		this.newLocationIdentifierField.setMaxLength(128);
		this.newLocationIdentifierField.setHint(Component.translatable("gui.teleporter_block.target_identifier_field.place_holder"));
		this.addWidget(this.newLocationIdentifierField);

		this.newLocationEntranceField = new EditBox(this.font, this.width / 2 + 4, 160, 150, 20, Component.empty());
		this.newLocationEntranceField.setMaxLength(128);
		this.newLocationEntranceField.setHint(Component.translatable("gui.teleporter_block.target_entrance_field.place_holder"));
		this.addWidget(this.newLocationEntranceField);

		this.newDataIdField = new EditBox(this.font, this.width / 2 - 50, 185, 100, 20, Component.empty());
		this.newDataIdField.setMaxLength(128);
		this.newDataIdField.setHint(Component.translatable("gui.teleporter_block.new_data_id_field.place_holder"));
		this.addWidget(this.newDataIdField);

		this.newDataField = new EditBox(this.font, this.width / 2 + 54, 185, 100, 20, Component.empty());
		this.newDataField.setMaxLength(128);
		this.newDataField.setHint(Component.translatable("gui.teleporter_block.new_data_field.place_holder"));
		this.addWidget(this.newDataField);

		this.addNewLocationButton = this.addRenderableWidget(Button.builder(ADD_NEW_LOCATION_BUTTON_LABEL_TEXT, button -> this.addLocationToList(this.newLocationIdentifierField.getValue(), this.newLocationEntranceField.getValue(), this.newDataIdField.getValue(), this.newDataField.getValue())).bounds(this.width / 2 - 154, 185, 100, 20).build());

		// teleportation mode: location

		this.locationModeScreenPageButton = this.addRenderableWidget(CycleButton.builder(LocationModeScreenPage::asText).values((LocationModeScreenPage[]) LocationModeScreenPage.values()).initially(this.locationModeScreenPage).omitKeyText().build(this.width / 2 - 154, 69, 300, 20, Component.empty(), (button, locationModeScreenPage) -> {
			this.locationModeScreenPage = locationModeScreenPage;
			this.updateWidgets();
		}));

		this.dataProvidingBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 104, 100, 20, Component.empty());
		this.dataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetXField.setValue(String.valueOf(this.teleporterBlock.getDataProvidingBlockPosOffset().getX()));
		this.addWidget(this.dataProvidingBlockPosOffsetXField);

		this.dataProvidingBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 54, 104, 100, 20, Component.empty());
		this.dataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetYField.setValue(String.valueOf(this.teleporterBlock.getDataProvidingBlockPosOffset().getY()));
		this.addWidget(this.dataProvidingBlockPosOffsetYField);

		this.dataProvidingBlockPosOffsetZField = new EditBox(this.font, this.width / 2 + 46, 104, 100, 20, Component.empty());
		this.dataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.dataProvidingBlockPosOffsetZField.setValue(String.valueOf(this.teleporterBlock.getDataProvidingBlockPosOffset().getZ()));
		this.addWidget(this.dataProvidingBlockPosOffsetZField);

		// page 1

		MutablePair<MutablePair<String, String>, MutablePair<String, String>> location = this.teleporterBlock.getLocation();
		this.locationIdentifierField = new EditBox(this.font, this.width / 2 - 154, 139, 150, 20, Component.empty());
		this.locationIdentifierField.setMaxLength(128);
		this.locationIdentifierField.setValue(location.left.left);
		this.addWidget(this.locationIdentifierField);

		this.locationEntranceField = new EditBox(this.font, this.width / 2 - 154, 174, 150, 20, Component.empty());
		this.locationEntranceField.setMaxLength(128);
		this.locationEntranceField.setValue(location.left.right);
		this.addWidget(this.locationEntranceField);

		this.locationDataIdentifierField = new EditBox(this.font, this.width / 2 + 4, 139, 150, 20, Component.empty());
		this.locationDataIdentifierField.setMaxLength(128);
		this.locationDataIdentifierField.setValue(this.teleporterBlock.getLocationAppendix());
		this.addWidget(this.locationDataIdentifierField);

		this.entranceDataIdentifierField = new EditBox(this.font, this.width / 2 + 4, 174, 150, 20, Component.empty());
		this.entranceDataIdentifierField.setMaxLength(128);
		this.entranceDataIdentifierField.setValue(this.teleporterBlock.getEntranceAppendix());
		this.addWidget(this.entranceDataIdentifierField);

		// page 2

		this.locationDataIdField = new EditBox(this.font, this.width / 2 - 154, 139, 150, 20, Component.empty());
		this.locationDataIdField.setMaxLength(128);
		this.locationDataIdField.setValue(location.right.left);
		this.addWidget(this.locationDataIdField);

		this.locationDataField = new EditBox(this.font, this.width / 2 - 154, 174, 150, 20, Component.empty());
		this.locationDataField.setMaxLength(128);
		this.locationDataField.setValue(location.right.right);
		this.addWidget(this.locationDataField);

		this.sendDataIdentifierDataIdentifierField = new EditBox(this.font, this.width / 2 + 4, 139, 150, 20, Component.empty());
		this.sendDataIdentifierDataIdentifierField.setMaxLength(128);
		this.sendDataIdentifierDataIdentifierField.setValue(this.teleporterBlock.getSentDataIdentifierAppendix());
		this.addWidget(this.sendDataIdentifierDataIdentifierField);

		this.sendDataValueDataIdentifierField = new EditBox(this.font, this.width / 2 + 4, 174, 150, 20, Component.empty());
		this.sendDataValueDataIdentifierField.setMaxLength(128);
		this.sendDataValueDataIdentifierField.setValue(this.teleporterBlock.getSentDataValueAppendix());
		this.addWidget(this.sendDataValueDataIdentifierField);

		// --- on teleport events page ---

		this.preTeleportTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 90, 50, 20, Component.empty());
		this.preTeleportTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.preTeleportTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getPreTeleportTriggeredBlock().getLeft().getX()));
		this.addWidget(this.preTeleportTriggeredBlockPositionOffsetXField);
		this.preTeleportTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 90, 50, 20, Component.empty());
		this.preTeleportTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.preTeleportTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getPreTeleportTriggeredBlock().getLeft().getY()));
		this.addWidget(this.preTeleportTriggeredBlockPositionOffsetYField);
		this.preTeleportTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 90, 50, 20, Component.empty());
		this.preTeleportTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.preTeleportTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getPreTeleportTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.preTeleportTriggeredBlockPositionOffsetZField);
		this.preTeleportTriggeredBlockResets = this.teleporterBlock.getPreTeleportTriggeredBlock().getRight();
		this.preTeleportToggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.preTeleportTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 90, 150, 20, Component.empty(), (button, preTeleportTriggeredBlockResets) -> {
			this.preTeleportTriggeredBlockResets = preTeleportTriggeredBlockResets;
		}));

		this.onTeleportUUIDListHandlerPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 125, 100, 20, Component.empty());
		this.onTeleportUUIDListHandlerPositionOffsetXField.setMaxLength(128);
		this.onTeleportUUIDListHandlerPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getOnTeleportUUIDListHandlerPositionOffset().getX()));
		this.addWidget(this.onTeleportUUIDListHandlerPositionOffsetXField);
		this.onTeleportUUIDListHandlerPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 125, 100, 20, Component.empty());
		this.onTeleportUUIDListHandlerPositionOffsetYField.setMaxLength(128);
		this.onTeleportUUIDListHandlerPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getOnTeleportUUIDListHandlerPositionOffset().getY()));
		this.addWidget(this.onTeleportUUIDListHandlerPositionOffsetYField);
		this.onTeleportUUIDListHandlerPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 125, 100, 20, Component.empty());
		this.onTeleportUUIDListHandlerPositionOffsetZField.setMaxLength(128);
		this.onTeleportUUIDListHandlerPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getOnTeleportUUIDListHandlerPositionOffset().getZ()));
		this.addWidget(this.onTeleportUUIDListHandlerPositionOffsetZField);

		this.postTeleportTriggeredBlockPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 160, 50, 20, Component.empty());
		this.postTeleportTriggeredBlockPositionOffsetXField.setMaxLength(128);
		this.postTeleportTriggeredBlockPositionOffsetXField.setValue(Integer.toString(this.teleporterBlock.getPostTeleportTriggeredBlock().getLeft().getX()));
		this.addWidget(this.postTeleportTriggeredBlockPositionOffsetXField);
		this.postTeleportTriggeredBlockPositionOffsetYField = new EditBox(this.font, this.width / 2 - 100, 160, 50, 20, Component.empty());
		this.postTeleportTriggeredBlockPositionOffsetYField.setMaxLength(128);
		this.postTeleportTriggeredBlockPositionOffsetYField.setValue(Integer.toString(this.teleporterBlock.getPostTeleportTriggeredBlock().getLeft().getY()));
		this.addWidget(this.postTeleportTriggeredBlockPositionOffsetYField);
		this.postTeleportTriggeredBlockPositionOffsetZField = new EditBox(this.font, this.width / 2 - 46, 160, 50, 20, Component.empty());
		this.postTeleportTriggeredBlockPositionOffsetZField.setMaxLength(128);
		this.postTeleportTriggeredBlockPositionOffsetZField.setValue(Integer.toString(this.teleporterBlock.getPostTeleportTriggeredBlock().getLeft().getZ()));
		this.addWidget(this.postTeleportTriggeredBlockPositionOffsetZField);
		this.postTeleportTriggeredBlockResets = this.teleporterBlock.getPostTeleportTriggeredBlock().getRight();
		this.postTeleportToggleTriggeredBlockResetsButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.on"), Component.translatable("gui.triggered_block.toggle_triggered_block_resets_button_label.off")).initially(this.postTeleportTriggeredBlockResets).omitKeyText().build(this.width / 2 + 8, 160, 150, 20, Component.empty(), (button, postTeleportTriggeredBlockResets) -> {
			this.postTeleportTriggeredBlockResets = postTeleportTriggeredBlockResets;
		}));

		// --- adventure screen customization page ---

		this.toggleShowAdventureScreenButton = this.addRenderableWidget(CycleButton.booleanBuilder(SHOW_ADVENTURE_SCREEN_TRUE_LABEL_TEXT, SHOW_ADVENTURE_SCREEN_FALSE_LABEL_TEXT).initially(this.showAdventureScreen).omitKeyText().build(this.width / 2 - 154, 44, 300, 20, SHOW_ADVENTURE_SCREEN_TRUE_LABEL_TEXT, (button, showAdventureScreen) -> {
			this.showAdventureScreen = showAdventureScreen;
		}));

		this.teleporterNameField = new EditBox(this.font, this.width / 2 - 154, 68, 300, 20, Component.empty());
		this.teleporterNameField.setMaxLength(128);
		this.teleporterNameField.setHint(Component.translatable("gui.teleporter_block.teleporter_name_field.place_holder"));
		this.teleporterNameField.setValue(this.teleporterBlock.getTeleporterName());
		this.addWidget(this.teleporterNameField);

		this.currentTargetOwnerLabelField = new EditBox(this.font, this.width / 2 - 154, 92, 300, 20, Component.empty());
		this.currentTargetOwnerLabelField.setMaxLength(128);
		this.currentTargetOwnerLabelField.setHint(Component.translatable("gui.teleporter_block.target_owner_field.place_holder"));
		this.currentTargetOwnerLabelField.setValue(this.teleporterBlock.getCurrentTargetOwnerLabel());
		this.addWidget(this.currentTargetOwnerLabelField);

		this.currentTargetIdentifierLabelField = new EditBox(this.font, this.width / 2 - 154, 116, 300, 20, Component.empty());
		this.currentTargetIdentifierLabelField.setMaxLength(128);
		this.currentTargetIdentifierLabelField.setHint(Component.translatable("gui.teleporter_block.target_identifier_field.place_holder"));
		this.currentTargetIdentifierLabelField.setValue(this.teleporterBlock.getCurrentTargetIdentifierLabel());
		this.addWidget(this.currentTargetIdentifierLabelField);

		this.toggleCanOwnerBeChosenButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_CAN_OWNER_BE_CHOSEN_BUTTON_LABEL_TEXT_ON, TOGGLE_CAN_OWNER_BE_CHOSEN_BUTTON_LABEL_TEXT_OFF).initially(this.canOwnerBeChosen).omitKeyText().build(this.width / 2 + 4, 140, 150, 20, Component.empty(), (button, canOwnerBeChosen) -> {
			this.canOwnerBeChosen = canOwnerBeChosen;
		}));

		this.teleportButtonLabelField = new EditBox(this.font, this.width / 2 - 154, 164, 300, 20, Component.empty());
		this.teleportButtonLabelField.setMaxLength(128);
		this.teleportButtonLabelField.setHint(Component.translatable("gui.teleporter_block.teleport_button.place_holder"));
		this.teleportButtonLabelField.setValue(this.teleporterBlock.getTeleportButtonLabel());
		this.addWidget(this.teleportButtonLabelField);

		this.cancelTeleportButtonLabelField = new EditBox(this.font, this.width / 2 - 154, 188, 150, 20, Component.empty());
		this.cancelTeleportButtonLabelField.setMaxLength(128);
		this.cancelTeleportButtonLabelField.setHint(Component.translatable("gui.teleporter_block.cancel_teleport_button.place_holder"));
		this.cancelTeleportButtonLabelField.setValue(this.teleporterBlock.getCancelTeleportButtonLabel());
		this.addWidget(this.cancelTeleportButtonLabelField);

		this.toggleShowCancelButtonButton = this.addRenderableWidget(CycleButton.booleanBuilder(TOGGLE_SHOW_CANCEL_BUTTON_BUTTON_LABEL_TEXT_ON, TOGGLE_SHOW_CANCEL_BUTTON_BUTTON_LABEL_TEXT_OFF).initially(this.showCancelButton).omitKeyText().build(this.width / 2 + 4, 188, 150, 20, Component.empty(), (button, showCancelButton) -> {
			this.showCancelButton = showCancelButton;
		}));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 220, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 220, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.cycleScreenPageButton);
	}

	@Override
	protected void updateWidgets() {

		this.preventInteractionIdentifierStringField.setVisible(false);
		this.interactionPreventedMessageField.setVisible(false);
		this.uuidListProviderBlockPositionOffsetXField.setVisible(false);
		this.uuidListProviderBlockPositionOffsetYField.setVisible(false);
		this.uuidListProviderBlockPositionOffsetZField.setVisible(false);
		this.accessPositionOffsetXField.setVisible(false);
		this.accessPositionOffsetYField.setVisible(false);
		this.accessPositionOffsetZField.setVisible(false);
		this.toggleSetAccessPositionButton.visible = false;
		this.toggleOnlyTeleportDimensionOwnerButton.visible = false;
		this.toggleTeleportTeamButton.visible = false;
		this.onlyDimensionOwnerCanTeleportMessageField.setVisible(false);

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

		this.locationModeScreenPageButton.visible = false;
		this.locationIdentifierField.setVisible(false);
		this.locationEntranceField.setVisible(false);
		this.locationDataIdField.setVisible(false);
		this.locationDataField.setVisible(false);
		this.dataProvidingBlockPosOffsetXField.setVisible(false);
		this.dataProvidingBlockPosOffsetYField.setVisible(false);
		this.dataProvidingBlockPosOffsetZField.setVisible(false);
		this.locationDataIdentifierField.setVisible(false);
		this.entranceDataIdentifierField.setVisible(false);
		this.sendDataIdentifierDataIdentifierField.setVisible(false);
		this.sendDataValueDataIdentifierField.setVisible(false);

		this.preTeleportTriggeredBlockPositionOffsetXField.setVisible(false);
		this.preTeleportTriggeredBlockPositionOffsetYField.setVisible(false);
		this.preTeleportTriggeredBlockPositionOffsetZField.setVisible(false);
		this.preTeleportToggleTriggeredBlockResetsButton.visible = false;

		this.onTeleportUUIDListHandlerPositionOffsetXField.setVisible(false);
		this.onTeleportUUIDListHandlerPositionOffsetYField.setVisible(false);
		this.onTeleportUUIDListHandlerPositionOffsetZField.setVisible(false);

		this.postTeleportTriggeredBlockPositionOffsetXField.setVisible(false);
		this.postTeleportTriggeredBlockPositionOffsetYField.setVisible(false);
		this.postTeleportTriggeredBlockPositionOffsetZField.setVisible(false);
		this.postTeleportToggleTriggeredBlockResetsButton.visible = false;

		this.toggleShowAdventureScreenButton.visible = false;
		this.teleporterNameField.setVisible(false);
		this.currentTargetIdentifierLabelField.setVisible(false);
		this.currentTargetOwnerLabelField.setVisible(false);
		this.toggleCanOwnerBeChosenButton.visible = false;
		this.toggleShowCancelButtonButton.visible = false;
		this.teleportButtonLabelField.setVisible(false);
		this.cancelTeleportButtonLabelField.setVisible(false);

		if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ACTIVATION) {

			this.preventInteractionIdentifierStringField.setVisible(true);
			this.interactionPreventedMessageField.setVisible(true);
			this.uuidListProviderBlockPositionOffsetXField.setVisible(true);
			this.uuidListProviderBlockPositionOffsetYField.setVisible(true);
			this.uuidListProviderBlockPositionOffsetZField.setVisible(true);
			this.toggleOnlyTeleportDimensionOwnerButton.visible = true;
			this.toggleTeleportTeamButton.visible = true;
			this.onlyDimensionOwnerCanTeleportMessageField.setVisible(true);

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
					|| this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {

				this.accessPositionOffsetXField.setVisible(true);
				this.accessPositionOffsetYField.setVisible(true);
				this.accessPositionOffsetZField.setVisible(true);
				this.toggleSetAccessPositionButton.visible = true;

			}

		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.TELEPORTATION_MODE) {

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

			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {

				this.locationModeScreenPageButton.visible = true;

				this.dataProvidingBlockPosOffsetXField.setVisible(true);
				this.dataProvidingBlockPosOffsetYField.setVisible(true);
				this.dataProvidingBlockPosOffsetZField.setVisible(true);

				if (this.locationModeScreenPage == LocationModeScreenPage.PAGE_1) {

					this.locationIdentifierField.setVisible(true);
					this.locationEntranceField.setVisible(true);

					this.locationDataIdentifierField.setVisible(true);
					this.entranceDataIdentifierField.setVisible(true);

				} else if (this.locationModeScreenPage == LocationModeScreenPage.PAGE_2) {

					this.locationDataIdField.setVisible(true);
					this.locationDataField.setVisible(true);

					this.sendDataIdentifierDataIdentifierField.setVisible(true);
					this.sendDataValueDataIdentifierField.setVisible(true);

				}
			}
		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ON_TELEPORT_EVENTS) {

			this.preTeleportTriggeredBlockPositionOffsetXField.setVisible(true);
			this.preTeleportTriggeredBlockPositionOffsetYField.setVisible(true);
			this.preTeleportTriggeredBlockPositionOffsetZField.setVisible(true);
			this.preTeleportToggleTriggeredBlockResetsButton.visible = true;

			this.onTeleportUUIDListHandlerPositionOffsetXField.setVisible(true);
			this.onTeleportUUIDListHandlerPositionOffsetYField.setVisible(true);
			this.onTeleportUUIDListHandlerPositionOffsetZField.setVisible(true);

			this.postTeleportTriggeredBlockPositionOffsetXField.setVisible(true);
			this.postTeleportTriggeredBlockPositionOffsetYField.setVisible(true);
			this.postTeleportTriggeredBlockPositionOffsetZField.setVisible(true);
			this.postTeleportToggleTriggeredBlockResetsButton.visible = true;

		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ADVENTURE_SCREEN_CUSTOMIZATION) {

			if (this.teleportationMode != TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				this.toggleShowAdventureScreenButton.visible = true;
			}

			this.teleporterNameField.setVisible(true);

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				this.currentTargetIdentifierLabelField.setVisible(true);
				this.currentTargetOwnerLabelField.setVisible(true);
				this.toggleCanOwnerBeChosenButton.visible = true;
			}

			this.toggleShowCancelButtonButton.visible = true;
			this.teleportButtonLabelField.setVisible(true);
			this.cancelTeleportButtonLabelField.setVisible(true);

		}

		this.creativeLocationsListScrollPosition = 0;
		this.creativeLocationsListScrollAmount = 0.0f;
		this.creativeLocationsListMouseClicked = false;

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		TeleporterBlockEntity.CreativeScreenPage var = this.creativeScreenPage;
		TeleporterBlockEntity.TeleportationMode var1 = this.teleportationMode;
		TeleporterBlockEntity.SpawnPointType var2 = this.spawnPointType;
		boolean bool1 = this.showAdventureScreen;
		boolean bool2 = this.setAccessPosition;
		boolean bool3 = this.onlyTeleportDimensionOwner;
		boolean bool4 = this.teleportTeam;
		boolean bool6 = this.canOwnerBeChosen;
		boolean bool8 = this.showCancelButton;
		boolean bool9 = this.preTeleportTriggeredBlockResets;
		boolean bool10 = this.postTeleportTriggeredBlockResets;
		String string3 = this.uuidListProviderBlockPositionOffsetXField.getValue();
		String string4 = this.uuidListProviderBlockPositionOffsetYField.getValue();
		String string5 = this.uuidListProviderBlockPositionOffsetZField.getValue();
		String string6 = this.accessPositionOffsetXField.getValue();
		String string7 = this.accessPositionOffsetYField.getValue();
		String string8 = this.accessPositionOffsetZField.getValue();
		String string9 = this.directTeleportPositionOffsetXField.getValue();
		String string10 = this.directTeleportPositionOffsetYField.getValue();
		String string11 = this.directTeleportPositionOffsetZField.getValue();
		String string12 = this.directTeleportOrientationYawField.getValue();
		String string13 = this.directTeleportOrientationPitchField.getValue();
		String string14 = this.newLocationIdentifierField.getValue();
		String string15 = this.newLocationEntranceField.getValue();
		String string16 = this.newDataIdField.getValue();
		String string17 = this.newDataField.getValue();
		String string19 = this.teleporterNameField.getValue();
		String string20 = this.currentTargetOwnerLabelField.getValue();
		String string21 = this.currentTargetIdentifierLabelField.getValue();
		String string22 = this.teleportButtonLabelField.getValue();
		String string23 = this.cancelTeleportButtonLabelField.getValue();
		String string24 = this.locationIdentifierField.getValue();
		String string25 = this.locationEntranceField.getValue();
		String string26 = this.locationDataIdField.getValue();
		String string27 = this.locationDataField.getValue();
		String string30 = this.preTeleportTriggeredBlockPositionOffsetXField.getValue();
		String string31 = this.preTeleportTriggeredBlockPositionOffsetYField.getValue();
		String string32 = this.preTeleportTriggeredBlockPositionOffsetZField.getValue();
		String string33 = this.onTeleportUUIDListHandlerPositionOffsetXField.getValue();
		String string34 = this.onTeleportUUIDListHandlerPositionOffsetYField.getValue();
		String string35 = this.onTeleportUUIDListHandlerPositionOffsetZField.getValue();
		String string36 = this.postTeleportTriggeredBlockPositionOffsetXField.getValue();
		String string37 = this.postTeleportTriggeredBlockPositionOffsetYField.getValue();
		String string38 = this.postTeleportTriggeredBlockPositionOffsetZField.getValue();
		String string39 = this.preventInteractionIdentifierStringField.getValue();
		String string40 = this.interactionPreventedMessageField.getValue();
		String string41 = this.onlyDimensionOwnerCanTeleportMessageField.getValue();
		List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> list1 = new ArrayList<>(this.locationsList);
		this.init(client, width, height);
		this.creativeScreenPage = var;
		this.teleportationMode = var1;
		this.spawnPointType = var2;
		this.showAdventureScreen = bool1;
		this.setAccessPosition = bool2;
		this.onlyTeleportDimensionOwner = bool3;
		this.teleportTeam = bool4;
		this.canOwnerBeChosen = bool6;
		this.showCancelButton = bool8;
		this.preTeleportTriggeredBlockResets = bool9;
		this.postTeleportTriggeredBlockResets = bool10;
		this.cycleScreenPageButton.setValue(this.creativeScreenPage);
		this.teleportationModeButton.setValue(this.teleportationMode);
		this.spawnPointTypeButton.setValue(this.spawnPointType);
		this.toggleShowAdventureScreenButton.setValue(this.showAdventureScreen);
		this.toggleSetAccessPositionButton.setValue(this.setAccessPosition);
		this.toggleOnlyTeleportDimensionOwnerButton.setValue(this.onlyTeleportDimensionOwner);
		this.toggleTeleportTeamButton.setValue(this.teleportTeam);
		this.toggleCanOwnerBeChosenButton.setValue(this.canOwnerBeChosen);
		this.toggleShowCancelButtonButton.setValue(this.showCancelButton);
		this.preTeleportToggleTriggeredBlockResetsButton.setValue(this.preTeleportTriggeredBlockResets);
		this.postTeleportToggleTriggeredBlockResetsButton.setValue(this.postTeleportTriggeredBlockResets);
		this.uuidListProviderBlockPositionOffsetXField.setValue(string3);
		this.uuidListProviderBlockPositionOffsetYField.setValue(string4);
		this.uuidListProviderBlockPositionOffsetZField.setValue(string5);
		this.accessPositionOffsetXField.setValue(string6);
		this.accessPositionOffsetYField.setValue(string7);
		this.accessPositionOffsetZField.setValue(string8);
		this.directTeleportPositionOffsetXField.setValue(string9);
		this.directTeleportPositionOffsetYField.setValue(string10);
		this.directTeleportPositionOffsetZField.setValue(string11);
		this.directTeleportOrientationYawField.setValue(string12);
		this.directTeleportOrientationPitchField.setValue(string13);
		this.newLocationIdentifierField.setValue(string14);
		this.newLocationEntranceField.setValue(string15);
		this.newDataIdField.setValue(string16);
		this.newDataField.setValue(string17);
		this.teleporterNameField.setValue(string19);
		this.currentTargetOwnerLabelField.setValue(string20);
		this.currentTargetIdentifierLabelField.setValue(string21);
		this.teleportButtonLabelField.setValue(string22);
		this.cancelTeleportButtonLabelField.setValue(string23);
		this.locationIdentifierField.setValue(string24);
		this.locationEntranceField.setValue(string25);
		this.locationDataIdField.setValue(string26);
		this.locationDataField.setValue(string27);
		this.preTeleportTriggeredBlockPositionOffsetXField.setValue(string30);
		this.preTeleportTriggeredBlockPositionOffsetYField.setValue(string31);
		this.preTeleportTriggeredBlockPositionOffsetZField.setValue(string32);
		this.onTeleportUUIDListHandlerPositionOffsetXField.setValue(string33);
		this.onTeleportUUIDListHandlerPositionOffsetYField.setValue(string34);
		this.onTeleportUUIDListHandlerPositionOffsetZField.setValue(string35);
		this.postTeleportTriggeredBlockPositionOffsetXField.setValue(string36);
		this.postTeleportTriggeredBlockPositionOffsetYField.setValue(string37);
		this.postTeleportTriggeredBlockPositionOffsetZField.setValue(string38);
		this.preventInteractionIdentifierStringField.setValue(string39);
		this.interactionPreventedMessageField.setValue(string40);
		this.onlyDimensionOwnerCanTeleportMessageField.setValue(string41);
		this.locationsList.clear();
		this.locationsList.addAll(list1);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.creativeLocationsListMouseClicked = false;
		int i;
		int j;
		if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3) {
			i = this.width / 2 - 152;
			j = 71;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 68)) {
				this.creativeLocationsListMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3
				&& this.creativeLocationsListMouseClicked) {
			int i = this.locationsList.size() - 3;
			float f = (float) deltaY / (float) i;
			this.creativeLocationsListScrollAmount = Mth.clamp(this.creativeLocationsListScrollAmount + f, 0.0f, 1.0f);
			this.creativeLocationsListScrollPosition = (int) ((double) (this.creativeLocationsListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.TELEPORTATION_MODE
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.locationsList.size() > 3
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 70 && mouseY <= 140) {
			int i = this.locationsList.size() - 3;
			float f = (float) verticalAmount / (float) i;
			this.creativeLocationsListScrollAmount = Mth.clamp(this.creativeLocationsListScrollAmount - f, 0.0f, 1.0f);
			this.creativeLocationsListScrollPosition = (int) ((double) (this.creativeLocationsListScrollAmount * (float) i));
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
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ACTIVATION) {
			context.drawString(this.font, PREVENT_INTERACTION_IDENTIFIER_STRING_LABEL_TEXT, this.width / 2 - 153, 45, 0xA0A0A0);
			this.preventInteractionIdentifierStringField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, INTERACTION_PREVENTED_MESSAGE_LABEL_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.interactionPreventedMessageField.render(context, mouseX, mouseY, delta);
			context.drawString(this.font, UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.uuidListProviderBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.uuidListProviderBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.uuidListProviderBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);
			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
					|| this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {
				context.drawString(this.font, ACCESS_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
				this.accessPositionOffsetXField.render(context, mouseX, mouseY, delta);
				this.accessPositionOffsetYField.render(context, mouseX, mouseY, delta);
				this.accessPositionOffsetZField.render(context, mouseX, mouseY, delta);
			}
			context.drawString(this.font, ONLY_DIMENSION_OWNER_CAN_TELEPORT_MESSAGE_LABEL_TEXT, this.width / 2 + 5, 185, 0xA0A0A0);
			this.onlyDimensionOwnerCanTeleportMessageField.render(context, mouseX, mouseY, delta);
		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.TELEPORTATION_MODE) {
			context.drawString(this.font, TELEPORTATION_MODE_LABEL_TEXT, this.width / 2 - 153, 51, 0xA0A0A0);
			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
				context.drawString(this.font, DIRECT_TELEPORT_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
				this.directTeleportPositionOffsetXField.render(context, mouseX, mouseY, delta);
				this.directTeleportPositionOffsetYField.render(context, mouseX, mouseY, delta);
				this.directTeleportPositionOffsetZField.render(context, mouseX, mouseY, delta);
				context.drawString(this.font, DIRECT_TELEPORT_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 105, 0xA0A0A0);
				this.directTeleportOrientationYawField.render(context, mouseX, mouseY, delta);
				this.directTeleportOrientationPitchField.render(context, mouseX, mouseY, delta);
			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {
				context.drawString(this.font, SPAWN_POINT_TYPE_LABEL_TEXT, this.width / 2 - 153, 76, 0xA0A0A0);
			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				for (int i = this.creativeLocationsListScrollPosition; i < Math.min(this.creativeLocationsListScrollPosition + 3, this.locationsList.size()); i++) {
					String text = this.locationsList.get(i).getLeft().getLeft();
					String text2 = "";
					// entrance
					if (!this.locationsList.get(i).getLeft().getRight().isEmpty()) {
						text = text + ", " + this.locationsList.get(i).getLeft().getRight();
					}
					// data
					if (!this.locationsList.get(i).getRight().getLeft().isEmpty()) {
						text2 = this.locationsList.get(i).getRight().getLeft() + ", " + this.locationsList.get(i).getRight().getRight();
					}
					if (text2.isEmpty()) {
						context.drawString(this.font, text, this.width / 2 - 117, 76 + ((i - this.creativeLocationsListScrollPosition) * 25), 0xA0A0A0);
					} else {
						int y = 76 + ((i - this.creativeLocationsListScrollPosition) * 25);
						context.drawString(this.font, text, this.width / 2 - 117, y - 7, 0xA0A0A0);
						context.drawString(this.font, text2, this.width / 2 - 117, y + 6, 0xA0A0A0);

					}
				}
				if (this.locationsList.size() > 3) {
					context.blitSprite(SCROLL_BAR_BACKGROUND_8_70_TEXTURE, this.width / 2 - 153, 70, 8, 70);
					int k = (int) (61.0f * this.creativeLocationsListScrollAmount);
					context.blitSprite(SCROLLER_TEXTURE, this.width / 2 - 152, 70 + 1 + k, 6, 7);
				}
				this.newLocationIdentifierField.render(context, mouseX, mouseY, delta);
				this.newLocationEntranceField.render(context, mouseX, mouseY, delta);
				this.newDataIdField.render(context, mouseX, mouseY, delta);
				this.newDataField.render(context, mouseX, mouseY, delta);
			} else if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {

				context.drawString(this.font, DATA_PROVIDING_BLOCK_POS_OFFSET_LABEL_TEXT, this.width / 2 - 153, 94, 0xA0A0A0);
				this.dataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
				this.dataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
				this.dataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);

				if (this.locationModeScreenPage == LocationModeScreenPage.PAGE_1) {

					context.drawString(this.font, LOCATION_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 129, 0xA0A0A0);
					this.locationIdentifierField.render(context, mouseX, mouseY, delta);
					context.drawString(this.font, LOCATION_ENTRANCE_LABEL_TEXT, this.width / 2 - 153, 164, 0xA0A0A0);
					this.locationEntranceField.render(context, mouseX, mouseY, delta);

					context.drawString(this.font, LOCATION_DATA_ID_LABEL_TEXT, this.width / 2 + 5, 129, 0xA0A0A0);
					this.locationDataIdentifierField.render(context, mouseX, mouseY, delta);

					context.drawString(this.font, LOCATION_ENTRANCE_DATA_ID_LABEL_TEXT, this.width / 2 + 5, 164, 0xA0A0A0);
					this.entranceDataIdentifierField.render(context, mouseX, mouseY, delta);

				} else if (this.locationModeScreenPage == LocationModeScreenPage.PAGE_2) {

					context.drawString(this.font, LOCATION_DATA_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 129, 0xA0A0A0);
					this.locationDataIdField.render(context, mouseX, mouseY, delta);
					context.drawString(this.font, LOCATION_DATA_LABEL_TEXT, this.width / 2 - 153, 164, 0xA0A0A0);
					this.locationDataField.render(context, mouseX, mouseY, delta);

					context.drawString(this.font, SEND_DATA_ID_LABEL_TEXT, this.width / 2 + 5, 129, 0xA0A0A0);
					this.sendDataIdentifierDataIdentifierField.render(context, mouseX, mouseY, delta);
					context.drawString(this.font, SEND_DATA_VALUE_LABEL_TEXT, this.width / 2 + 5, 164, 0xA0A0A0);
					this.sendDataValueDataIdentifierField.render(context, mouseX, mouseY, delta);

				}
			}
		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ON_TELEPORT_EVENTS) {

			context.drawString(this.font, PRE_TELEPORT_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 80, 0xA0A0A0);
			this.preTeleportTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.preTeleportTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.preTeleportTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, ON_TELEPORT_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 115, 0xA0A0A0);
			this.onTeleportUUIDListHandlerPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.onTeleportUUIDListHandlerPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.onTeleportUUIDListHandlerPositionOffsetZField.render(context, mouseX, mouseY, delta);

			context.drawString(this.font, POST_TELEPORT_TRIGGERED_BLOCK_POSITION_TEXT, this.width / 2 - 153, 150, 0xA0A0A0);
			this.postTeleportTriggeredBlockPositionOffsetXField.render(context, mouseX, mouseY, delta);
			this.postTeleportTriggeredBlockPositionOffsetYField.render(context, mouseX, mouseY, delta);
			this.postTeleportTriggeredBlockPositionOffsetZField.render(context, mouseX, mouseY, delta);

		} else if (this.creativeScreenPage == TeleporterBlockEntity.CreativeScreenPage.ADVENTURE_SCREEN_CUSTOMIZATION) {

			this.teleporterNameField.render(context, mouseX, mouseY, delta);

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				this.currentTargetIdentifierLabelField.render(context, mouseX, mouseY, delta);
				this.currentTargetOwnerLabelField.render(context, mouseX, mouseY, delta);

			}

			this.teleportButtonLabelField.render(context, mouseX, mouseY, delta);
			this.cancelTeleportButtonLabelField.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected boolean updateBlock() {
		if (this.teleporterBlock.getLevel() == null) {
			return false;
		}
		ClientPlayNetworking.send(new UpdateTeleporterBlockPacket(
				this.teleporterBlock.getBlockPos(),
				this.creativeScreenPage.asString(),
				this.showAdventureScreen,
				this.preventInteractionIdentifierStringField.getValue(),
				this.interactionPreventedMessageField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.uuidListProviderBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.uuidListProviderBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.uuidListProviderBlockPositionOffsetZField.getValue())
				),
				LevelData.RespawnData.of(
						this.teleporterBlock.getLevel().dimension(),
				new BlockPos(
						ItemUtils.parseInt(this.accessPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.accessPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.accessPositionOffsetZField.getValue())
				),
						0.0F,//ItemUtils.parseFloat(this.directTeleportOrientationYawField.getValue()),
						0.0F//ItemUtils.parseFloat(this.directTeleportOrientationPitchField.getValue())
				),
				this.setAccessPosition,
				this.onlyTeleportDimensionOwner,
				this.onlyDimensionOwnerCanTeleportMessageField.getValue(),
				this.teleportTeam,
				this.teleportationMode.asString(),
				LevelData.RespawnData.of(
						this.teleporterBlock.getLevel().dimension(),
						new BlockPos(
								ItemUtils.parseInt(this.directTeleportPositionOffsetXField.getValue()),
								ItemUtils.parseInt(this.directTeleportPositionOffsetYField.getValue()),
								ItemUtils.parseInt(this.directTeleportPositionOffsetZField.getValue())
						),
						ItemUtils.parseFloat(this.directTeleportOrientationYawField.getValue()),
						ItemUtils.parseFloat(this.directTeleportOrientationPitchField.getValue())
				),
				this.spawnPointType.asString(),
				new ArrayList<>(this.locationsList),
				new MutablePair<>(new MutablePair<>(this.locationIdentifierField.getValue(), this.locationEntranceField.getValue()), new MutablePair<>(this.locationDataIdField.getValue(), this.locationDataField.getValue())),
				new BlockPos(
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetXField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetYField.getValue()),
						ItemUtils.parseInt(this.dataProvidingBlockPosOffsetZField.getValue())
				),
				this.locationDataIdentifierField.getValue(),
				this.entranceDataIdentifierField.getValue(),
				this.sendDataIdentifierDataIdentifierField.getValue(),
				this.sendDataValueDataIdentifierField.getValue(),
				new BlockPos(
						ItemUtils.parseInt(this.preTeleportTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.preTeleportTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.preTeleportTriggeredBlockPositionOffsetZField.getValue())
				),
				this.preTeleportTriggeredBlockResets,
				new BlockPos(
						ItemUtils.parseInt(this.onTeleportUUIDListHandlerPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.onTeleportUUIDListHandlerPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.onTeleportUUIDListHandlerPositionOffsetZField.getValue())
				),
				new BlockPos(
						ItemUtils.parseInt(this.postTeleportTriggeredBlockPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.postTeleportTriggeredBlockPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.postTeleportTriggeredBlockPositionOffsetZField.getValue())
				),
				this.postTeleportTriggeredBlockResets,
				this.teleporterNameField.getValue(),
				this.currentTargetIdentifierLabelField.getValue(),
				this.currentTargetOwnerLabelField.getValue(),
				this.showCancelButton,
				this.canOwnerBeChosen,
				this.teleportButtonLabelField.getValue(),
				this.cancelTeleportButtonLabelField.getValue()
		));
		return true;
	}

	public enum LocationModeScreenPage implements StringRepresentable {
		PAGE_1("page_1"),
		PAGE_2("page_2");

		private final String name;

		LocationModeScreenPage(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public Component asText() {
			return Component.translatable("gui.teleporter_block.location_mode_screen_page." + this.name);
		}
	}
}
