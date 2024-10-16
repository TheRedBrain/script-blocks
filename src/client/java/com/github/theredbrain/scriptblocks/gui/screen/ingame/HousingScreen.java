package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.AddStatusEffectPacket;
import com.github.theredbrain.scriptblocks.network.packet.LeaveHouseFromHousingScreenPacket;
import com.github.theredbrain.scriptblocks.network.packet.ResetHouseHousingBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockAdventurePacket;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class HousingScreen extends Screen {
	private static final Text TITLE_OWNER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.owner");
	private static final Text TITLE_CO_OWNER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.co_owner");
	private static final Text TITLE_CO_OWNER_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.co_owner_list.title");
	private static final Text TITLE_CO_OWNER_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.co_owner_list.description");
	private static final Text TITLE_TRUSTED_LABEL_TEXT = Text.translatable("gui.housing_screen.title.trusted");
	private static final Text TITLE_TRUSTED_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.trusted_list.title");
	private static final Text TITLE_TRUSTED_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.trusted_list.description");
	private static final Text TITLE_GUEST_LABEL_TEXT = Text.translatable("gui.housing_screen.title.guest");
	private static final Text TITLE_GUEST_LIST_LABEL_TEXT = Text.translatable("gui.housing_screen.guest_list.title");
	private static final Text TITLE_GUEST_LIST_DESCRIPTION_LABEL_TEXT = Text.translatable("gui.housing_screen.guest_list.description");
	private static final Text TITLE_STRANGER_LABEL_TEXT = Text.translatable("gui.housing_screen.title.stranger");
	private static final Text LEAVE_CURRENT_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.leave_current_house_button_label");
	private static final Text CLOSE_HOUSING_SCREEN_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.close_housing_screen_button_label");
	private static final Text OPEN_RESET_HOUSE_SCREEN_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_reset_house_screen_button_label");
	private static final Text TOGGLE_ADVENTURE_BUILDING_OFF_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.toggle_adventure_building_off_button_label");
	private static final Text TOGGLE_ADVENTURE_BUILDING_ON_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.toggle_adventure_building_on_button_label");
	private static final Text UNCLAIM_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.unclaim_house_button_label");
	private static final Text CLAIM_HOUSE_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.claim_house_button_label");
	private static final Text OPEN_CO_OWNER_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_co_owner_list_button_label");
	private static final Text NEW_CO_OWNER_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_co_owner_field.place_holder");
	private static final Text ADD_NEW_CO_OWNER_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_co_owner_button.label");
	private static final Text ADD_NEW_CO_OWNER_BUTTON_TOOLTIP_TEXT = Text.translatable("gui.housing_screen.add_new_co_owner_button.tooltip");
	private static final Text OPEN_TRUSTED_PERSONS_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_trusted_list_button_label");
	private static final Text NEW_TRUSTED_PERSON_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_trusted_person_field.place_holder");
	private static final Text ADD_NEW_TRUSTED_PERSON_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_trusted_person_button.label");
	private static final Text ADD_NEW_TRUSTED_PERSON_BUTTON_TOOLTIP_TEXT = Text.translatable("gui.housing_screen.add_new_trusted_person_button.tooltip");
	private static final Text OPEN_GUEST_LIST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.open_guest_list_button_label");
	private static final Text NEW_GUEST_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.housing_screen.new_guest_field.place_holder");
	private static final Text ADD_NEW_GUEST_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.add_new_guest_button.label");
	private static final Text ADD_NEW_GUEST_BUTTON_TOOLTIP_TEXT = Text.translatable("gui.housing_screen.add_new_guest_button.tooltip");
	private static final Text REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.housing_screen.remove_list_entry_button_label");

	public static final Identifier BACKGROUND_218_215_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_215_background.png");
	public static final Identifier BACKGROUND_218_95_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_95_background.png");
	public static final Identifier BACKGROUND_218_71_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_71_background.png");
	private static final Identifier PLAYER_LISTS_SCROLLER_BACKGROUND_TEXTURE = ScriptBlocks.identifier("container/housing_screen/player_lists_scroller_background");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("container/scroller");

	private final HousingBlockEntity housingBlockEntity;

	private ButtonWidget leaveCurrentHouseButton;

	private ButtonWidget openResetHouseScreenButton;
	private ButtonWidget resetHouseButton;
	private ButtonWidget closeResetHouseScreenButton;

	private ButtonWidget toggleAdventureBuildingEffectButton;
	private ButtonWidget unclaimHouseButton;
	private ButtonWidget claimHouseButton;

	private ButtonWidget openCoOwnerListScreenButton;
	private TextFieldWidget newCoOwnerField;
	private ButtonWidget addNewCoOwnerButton;
	private ButtonWidget removeCoOwnerListEntryButton0;
	private ButtonWidget removeCoOwnerListEntryButton1;
	private ButtonWidget removeCoOwnerListEntryButton2;
	private ButtonWidget removeCoOwnerListEntryButton3;
	private ButtonWidget removeCoOwnerListEntryButton4;

	private ButtonWidget openTrustedPersonsListScreenButton;
	private TextFieldWidget newTrustedPersonField;
	private ButtonWidget addNewTrustedPersonButton;
	private ButtonWidget removeTrustedPersonListEntryButton0;
	private ButtonWidget removeTrustedPersonListEntryButton1;
	private ButtonWidget removeTrustedPersonListEntryButton2;
	private ButtonWidget removeTrustedPersonListEntryButton3;
	private ButtonWidget removeTrustedPersonListEntryButton4;

	private ButtonWidget openGuestListScreenButton;
	private TextFieldWidget newGuestField;
	private ButtonWidget addNewGuestButton;
	private ButtonWidget removeGuestListEntryButton0;
	private ButtonWidget removeGuestListEntryButton1;
	private ButtonWidget removeGuestListEntryButton2;
	private ButtonWidget removeGuestListEntryButton3;
	private ButtonWidget removeGuestListEntryButton4;

	private ButtonWidget closeListEditScreensButton;

	private ButtonWidget closeAdventureScreenButton;

	private final int currentPermissionLevel;
	private List<String> coOwnerList = new ArrayList<>(List.of());
	private List<String> trustedPersonsList = new ArrayList<>(List.of());
	private List<String> guestList = new ArrayList<>(List.of());
	private boolean showResetHouseScreen = false;
	private boolean showCoOwnerListScreen = false;
	private boolean showTrustedListScreen = false;
	private boolean showGuestListScreen = false;
	private int backgroundWidth;
	private int backgroundHeight;
	private int x;
	private int y;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;
	private HousingBlockEntity.OwnerMode ownerMode = HousingBlockEntity.OwnerMode.DIMENSION_OWNER;

	public HousingScreen(HousingBlockEntity housingBlockEntity, int currentPermissionLevel) {
		super(NarratorManager.EMPTY);
		this.housingBlockEntity = housingBlockEntity;
		this.currentPermissionLevel = currentPermissionLevel;
	}

	private void openResetHouseScreen() {
		this.showResetHouseScreen = true;
		this.updateWidgets();
	}

	private void closeResetHouseScreen() {
		this.showResetHouseScreen = false;
		this.updateWidgets();
	}

	private void openListScreen(int listType) {
		if (listType == 0) {
			this.showCoOwnerListScreen = true;
		} else if (listType == 1) {
			this.showTrustedListScreen = true;
		} else if (listType == 2) {
			this.showGuestListScreen = true;
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void closeListScreens() {
		this.showCoOwnerListScreen = false;
		this.showTrustedListScreen = false;
		this.showGuestListScreen = false;
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateHousingBlockAdventure();
		this.updateWidgets();
	}

	private void addNewEntryToList(String newEntry, int listType) {
		if (listType == 0) {
			this.coOwnerList.add(newEntry);
		} else if (listType == 1) {
			this.trustedPersonsList.add(newEntry);
		} else if (listType == 2) {
			this.guestList.add(newEntry);
		}
		this.updateWidgets();
	}

	private void removeEntryFromList(int index, int listType) {
		if (listType == 0 && index + this.scrollPosition < this.coOwnerList.size()) {
			this.coOwnerList.remove(index + this.scrollPosition);
		} else if (listType == 1 && index + this.scrollPosition < this.trustedPersonsList.size()) {
			this.trustedPersonsList.remove(index + this.scrollPosition);
		} else if (listType == 2 && index + this.scrollPosition < this.guestList.size()) {
			this.guestList.remove(index + this.scrollPosition);
		}
		this.updateWidgets();
	}

	private void cancel() {
		this.close();
	}

	private void leaveCurrentHouse() {
		ClientPlayNetworking.send(new LeaveHouseFromHousingScreenPacket());
		this.close();
	}

	@Override
	protected void init() {
		this.coOwnerList.addAll(this.housingBlockEntity.getCoOwnerList());
		this.trustedPersonsList.addAll(this.housingBlockEntity.getTrustedList());
		this.guestList.addAll(this.housingBlockEntity.getGuestList());
		this.ownerMode = housingBlockEntity.getOwnerMode();
		if (this.currentPermissionLevel == 0) {
			this.backgroundWidth = 218;
			this.backgroundHeight = 215;
			this.x = (this.width - this.backgroundWidth) / 2;
			this.y = (this.height - this.backgroundHeight) / 2;
		} else if (this.currentPermissionLevel == 1) {
			this.backgroundWidth = 218;
			this.backgroundHeight = 95;
			this.x = (this.width - this.backgroundWidth) / 2;
			this.y = (this.height - this.backgroundHeight) / 2;
		} else if (this.currentPermissionLevel == 2 || this.currentPermissionLevel == 3) {
			this.backgroundWidth = 218;
			this.backgroundHeight = 71;
			this.x = (this.width - this.backgroundWidth) / 2;
			this.y = (this.height - this.backgroundHeight) / 2;
		} else {
			this.backgroundWidth = 218;
			this.backgroundHeight = this.ownerMode == HousingBlockEntity.OwnerMode.INTERACTION ? 95 : 71;
			this.x = (this.width - this.backgroundWidth) / 2;
			this.y = (this.height - this.backgroundHeight) / 2;
		}
		super.init();

		this.resetHouseButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.PROCEED, button -> this.resetHouse()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth / 2 - 18, 20).build());
		this.closeResetHouseScreenButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.closeResetHouseScreen()).dimensions(this.x + this.backgroundWidth / 2 - 18 + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth / 2 - 18, 20).build());

		this.removeCoOwnerListEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(0, 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 33, 50, 20).build());
		this.removeCoOwnerListEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(1, 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 57, 50, 20).build());
		this.removeCoOwnerListEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(2, 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 81, 50, 20).build());
		this.removeCoOwnerListEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(3, 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 105, 50, 20).build());
		this.removeCoOwnerListEntryButton4 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(4, 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 129, 50, 20).build());
		this.newCoOwnerField = new TextFieldWidget(this.textRenderer, this.x + 7, this.y + this.backgroundHeight - 27 - 24, this.backgroundWidth - 57 - 7 - 4, 20, Text.empty());
		this.newCoOwnerField.setMaxLength(128);
		this.newCoOwnerField.setPlaceholder(NEW_CO_OWNER_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newCoOwnerField);

		this.addNewCoOwnerButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_CO_OWNER_BUTTON_LABEL_TEXT, button -> this.addNewEntryToList(this.newCoOwnerField.getText(), 0)).dimensions(this.x + this.backgroundWidth - 57, this.y + this.backgroundHeight - 27 - 24, 50, 20).build());
		this.addNewCoOwnerButton.setTooltip(Tooltip.of(ADD_NEW_CO_OWNER_BUTTON_TOOLTIP_TEXT));

		this.removeTrustedPersonListEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(0, 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 33, 50, 20).build());
		this.removeTrustedPersonListEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(1, 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 57, 50, 20).build());
		this.removeTrustedPersonListEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(2, 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 81, 50, 20).build());
		this.removeTrustedPersonListEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(3, 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 105, 50, 20).build());
		this.removeTrustedPersonListEntryButton4 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(4, 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 129, 50, 20).build());
		this.newTrustedPersonField = new TextFieldWidget(this.textRenderer, this.x + 7, this.y + this.backgroundHeight - 27 - 24, this.backgroundWidth - 57 - 7 - 4, 20, Text.empty());
		this.newTrustedPersonField.setMaxLength(128);
		this.newTrustedPersonField.setPlaceholder(NEW_TRUSTED_PERSON_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newTrustedPersonField);

		this.addNewTrustedPersonButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_TRUSTED_PERSON_BUTTON_LABEL_TEXT, button -> this.addNewEntryToList(this.newTrustedPersonField.getText(), 1)).dimensions(this.x + this.backgroundWidth - 57, this.y + this.backgroundHeight - 27 - 24, 50, 20).build());
		this.addNewTrustedPersonButton.setTooltip(Tooltip.of(ADD_NEW_TRUSTED_PERSON_BUTTON_TOOLTIP_TEXT));

		this.removeGuestListEntryButton0 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(0, 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 33, 50, 20).build());
		this.removeGuestListEntryButton1 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(1, 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 57, 50, 20).build());
		this.removeGuestListEntryButton2 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(2, 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 81, 50, 20).build());
		this.removeGuestListEntryButton3 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(3, 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 105, 50, 20).build());
		this.removeGuestListEntryButton4 = this.addDrawableChild(ButtonWidget.builder(REMOVE_LIST_ENTRY_BUTTON_LABEL_TEXT, button -> this.removeEntryFromList(4, 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 129, 50, 20).build());
		this.newGuestField = new TextFieldWidget(this.textRenderer, this.x + 7, this.y + this.backgroundHeight - 27 - 24, this.backgroundWidth - 57 - 7 - 4, 20, Text.empty());
		this.newGuestField.setMaxLength(128);
		this.newGuestField.setPlaceholder(NEW_GUEST_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newGuestField);

		this.addNewGuestButton = this.addDrawableChild(ButtonWidget.builder(ADD_NEW_GUEST_BUTTON_LABEL_TEXT, button -> this.addNewEntryToList(this.newGuestField.getText(), 2)).dimensions(this.x + this.backgroundWidth - 57, this.y + this.backgroundHeight - 27 - 24, 50, 20).build());
		this.addNewGuestButton.setTooltip(Tooltip.of(ADD_NEW_GUEST_BUTTON_TOOLTIP_TEXT));

		this.closeListEditScreensButton = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.closeListScreens()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		boolean isAdventureBuilding = false;
		RegistryEntry<StatusEffect> building_mode_status_effect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.BUILDING_MODE);
		if (this.client != null && this.client.player != null) {
			isAdventureBuilding = this.client.player.hasStatusEffect(building_mode_status_effect);
		}
		this.toggleAdventureBuildingEffectButton = this.addDrawableChild(ButtonWidget.builder(isAdventureBuilding ? TOGGLE_ADVENTURE_BUILDING_OFF_BUTTON_LABEL_TEXT : TOGGLE_ADVENTURE_BUILDING_ON_BUTTON_LABEL_TEXT, button -> this.toggleAdventureBuildingEffect()).dimensions(this.x + 7, this.y + 20, this.backgroundWidth - 14, 20).build());

		this.openCoOwnerListScreenButton = this.addDrawableChild(ButtonWidget.builder(OPEN_CO_OWNER_LIST_BUTTON_LABEL_TEXT, button -> this.openListScreen(0)).dimensions(this.x + 7, this.y + 44, this.backgroundWidth - 14, 20).build());
		this.openTrustedPersonsListScreenButton = this.addDrawableChild(ButtonWidget.builder(OPEN_TRUSTED_PERSONS_LIST_BUTTON_LABEL_TEXT, button -> this.openListScreen(1)).dimensions(this.x + 7, this.y + 68, this.backgroundWidth - 14, 20).build());
		this.openGuestListScreenButton = this.addDrawableChild(ButtonWidget.builder(OPEN_GUEST_LIST_BUTTON_LABEL_TEXT, button -> this.openListScreen(2)).dimensions(this.x + 7, this.y + 92, this.backgroundWidth - 14, 20).build());

		this.openResetHouseScreenButton = this.addDrawableChild(ButtonWidget.builder(OPEN_RESET_HOUSE_SCREEN_BUTTON_LABEL_TEXT, button -> this.openResetHouseScreen()).dimensions(this.x + 7, this.y + 116, this.backgroundWidth - 14, 20).build());

		this.unclaimHouseButton = this.addDrawableChild(ButtonWidget.builder(UNCLAIM_HOUSE_BUTTON_LABEL_TEXT, button -> this.trySetHouseOwner(false)).dimensions(this.x + 7, this.y + this.backgroundHeight - 27 - 48, this.backgroundWidth - 14, 20).build());
		this.claimHouseButton = this.addDrawableChild(ButtonWidget.builder(CLAIM_HOUSE_BUTTON_LABEL_TEXT, button -> this.trySetHouseOwner(true)).dimensions(this.x + 7, this.y + this.backgroundHeight - 27 - 48, this.backgroundWidth - 14, 20).build());

		this.leaveCurrentHouseButton = this.addDrawableChild(ButtonWidget.builder(LEAVE_CURRENT_HOUSE_BUTTON_LABEL_TEXT, button -> this.leaveCurrentHouse()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27 - 24, this.backgroundWidth - 14, 20).build());

		this.closeAdventureScreenButton = this.addDrawableChild(ButtonWidget.builder(CLOSE_HOUSING_SCREEN_BUTTON_LABEL_TEXT, button -> this.cancel()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.leaveCurrentHouseButton.visible = false;

		this.openResetHouseScreenButton.visible = false;
		this.resetHouseButton.visible = false;
		this.closeResetHouseScreenButton.visible = false;

		this.toggleAdventureBuildingEffectButton.visible = false;
		this.unclaimHouseButton.visible = false;
		this.claimHouseButton.visible = false;

		this.openCoOwnerListScreenButton.visible = false;
		this.newCoOwnerField.setVisible(false);
		this.addNewCoOwnerButton.visible = false;
		this.removeCoOwnerListEntryButton0.visible = false;
		this.removeCoOwnerListEntryButton1.visible = false;
		this.removeCoOwnerListEntryButton2.visible = false;
		this.removeCoOwnerListEntryButton3.visible = false;
		this.removeCoOwnerListEntryButton4.visible = false;

		this.openTrustedPersonsListScreenButton.visible = false;
		this.newTrustedPersonField.setVisible(false);
		this.addNewTrustedPersonButton.visible = false;
		this.removeTrustedPersonListEntryButton0.visible = false;
		this.removeTrustedPersonListEntryButton1.visible = false;
		this.removeTrustedPersonListEntryButton2.visible = false;
		this.removeTrustedPersonListEntryButton3.visible = false;
		this.removeTrustedPersonListEntryButton4.visible = false;

		this.openGuestListScreenButton.visible = false;
		this.newGuestField.setVisible(false);
		this.addNewGuestButton.visible = false;
		this.removeGuestListEntryButton0.visible = false;
		this.removeGuestListEntryButton1.visible = false;
		this.removeGuestListEntryButton2.visible = false;
		this.removeGuestListEntryButton3.visible = false;
		this.removeGuestListEntryButton4.visible = false;

		this.closeListEditScreensButton.visible = false;

		this.closeAdventureScreenButton.visible = false;

		if (this.showCoOwnerListScreen) {

			this.newCoOwnerField.setVisible(true);
			this.addNewCoOwnerButton.visible = true;

			int index = 0;
			for (int i = 0; i < Math.min(5, this.coOwnerList.size()); i++) {
				if (index == 0) {
					this.removeCoOwnerListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeCoOwnerListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeCoOwnerListEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeCoOwnerListEntryButton3.visible = true;
				} else if (index == 4) {
					this.removeCoOwnerListEntryButton4.visible = true;
				}
				index++;
			}

			this.closeListEditScreensButton.visible = true;

		} else if (this.showTrustedListScreen) {

			this.newTrustedPersonField.setVisible(true);
			this.addNewTrustedPersonButton.visible = true;
			int index = 0;
			for (int i = 0; i < Math.min(5, this.trustedPersonsList.size()); i++) {
				if (index == 0) {
					this.removeTrustedPersonListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeTrustedPersonListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeTrustedPersonListEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeTrustedPersonListEntryButton3.visible = true;
				} else if (index == 4) {
					this.removeTrustedPersonListEntryButton4.visible = true;
				}
				index++;
			}

			this.closeListEditScreensButton.visible = true;

		} else if (this.showGuestListScreen) {

			this.newGuestField.setVisible(true);
			this.addNewGuestButton.visible = true;

			int index = 0;
			for (int i = 0; i < Math.min(5, this.guestList.size()); i++) {
				if (index == 0) {
					this.removeGuestListEntryButton0.visible = true;
				} else if (index == 1) {
					this.removeGuestListEntryButton1.visible = true;
				} else if (index == 2) {
					this.removeGuestListEntryButton2.visible = true;
				} else if (index == 3) {
					this.removeGuestListEntryButton3.visible = true;
				} else if (index == 4) {
					this.removeGuestListEntryButton4.visible = true;
				}
				index++;
			}

			this.closeListEditScreensButton.visible = true;

		} else if (this.showResetHouseScreen) {

			this.resetHouseButton.visible = true;
			this.closeResetHouseScreenButton.visible = true;

		} else {

			if (this.currentPermissionLevel == 0) {

				this.toggleAdventureBuildingEffectButton.visible = true;

				this.openCoOwnerListScreenButton.visible = true;
				this.openTrustedPersonsListScreenButton.visible = true;
				this.openGuestListScreenButton.visible = true;

				this.openResetHouseScreenButton.visible = true;

				if (this.housingBlockEntity != null && this.housingBlockEntity.getOwnerMode() == HousingBlockEntity.OwnerMode.INTERACTION) {
					this.unclaimHouseButton.visible = true;
				}

			} else if (this.currentPermissionLevel == 1) {

				this.toggleAdventureBuildingEffectButton.visible = true;

			} else if (this.currentPermissionLevel == 4) {

				if (this.housingBlockEntity != null && this.housingBlockEntity.getOwnerMode() == HousingBlockEntity.OwnerMode.INTERACTION && !this.housingBlockEntity.isOwnerSet()) {
					this.claimHouseButton.visible = true;
				}

			}

			this.leaveCurrentHouseButton.visible = true;

			this.closeAdventureScreenButton.visible = true;
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		List<String> list = new ArrayList<>(this.coOwnerList);
		List<String> list1 = new ArrayList<>(this.trustedPersonsList);
		List<String> list2 = new ArrayList<>(this.guestList);
		HousingBlockEntity.OwnerMode var = this.ownerMode;
		int number = this.scrollPosition;
		float number1 = this.scrollAmount;
		String string = this.newCoOwnerField.getText();
		String string1 = this.newTrustedPersonField.getText();
		String string2 = this.newGuestField.getText();
		this.init(client, width, height);
		this.coOwnerList.clear();
		this.trustedPersonsList.clear();
		this.guestList.clear();
		this.coOwnerList.addAll(list);
		this.trustedPersonsList.addAll(list1);
		this.guestList.addAll(list2);
		this.ownerMode = var;
		this.scrollPosition = number;
		this.scrollAmount = number1;
		this.newCoOwnerField.setText(string);
		this.newTrustedPersonField.setText(string1);
		this.newGuestField.setText(string2);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (((this.showCoOwnerListScreen && this.coOwnerList.size() > 5)
				|| (this.showTrustedListScreen && this.coOwnerList.size() > 5)
				|| (this.showGuestListScreen && this.coOwnerList.size() > 5))) {
			int i = this.x + 8;
			int j = this.y + 34;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 115)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.showCoOwnerListScreen
				&& this.coOwnerList.size() > 5
				&& this.mouseClicked) {
			int i = this.coOwnerList.size() - 5;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.showTrustedListScreen
				&& this.trustedPersonsList.size() > 5
				&& this.mouseClicked) {
			int i = this.trustedPersonsList.size() - 5;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.showGuestListScreen
				&& this.guestList.size() > 5
				&& this.mouseClicked) {
			int i = this.guestList.size() - 5;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.showCoOwnerListScreen
				&& this.coOwnerList.size() > 5
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 61)
				&& mouseY >= 34 && mouseY <= 148) {
			int i = this.coOwnerList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.showTrustedListScreen
				&& this.trustedPersonsList.size() > 5
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 61)
				&& mouseY >= 34 && mouseY <= 148) {
			int i = this.trustedPersonsList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		if (this.showGuestListScreen
				&& this.guestList.size() > 5
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 61)
				&& mouseY >= 34 && mouseY <= 148) {
			int i = this.guestList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		if (this.showResetHouseScreen) {
		} else if (this.showCoOwnerListScreen) {
			context.drawText(this.textRenderer, TITLE_CO_OWNER_LIST_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			context.drawText(this.textRenderer, TITLE_CO_OWNER_LIST_DESCRIPTION_LABEL_TEXT, this.x + 8, this.y + 20, 0x404040, false);
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 5, this.coOwnerList.size()); i++) {
				String text = this.coOwnerList.get(i);
				context.drawText(this.textRenderer, text, this.x + 19, this.y + 39 + ((i - this.scrollPosition) * 24), 0x404040, false);
			}
			if (this.coOwnerList.size() > 5) {
				context.drawGuiTexture(PLAYER_LISTS_SCROLLER_BACKGROUND_TEXTURE, this.x + 7, this.y + 33, 8, 116);
				int k = (int) (107.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.x + 8, this.y + 33 + 1 + k, 6, 7);
			}
			this.newCoOwnerField.render(context, mouseX, mouseY, delta);
		} else if (this.showTrustedListScreen) {
			context.drawText(this.textRenderer, TITLE_TRUSTED_LIST_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			context.drawText(this.textRenderer, TITLE_TRUSTED_LIST_DESCRIPTION_LABEL_TEXT, this.x + 8, this.y + 20, 0x404040, false);
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 5, this.trustedPersonsList.size()); i++) {
				String text = this.trustedPersonsList.get(i);
				context.drawText(this.textRenderer, text, this.x + 19, this.y + 39 + ((i - this.scrollPosition) * 24), 0x404040, false);
			}
			if (this.trustedPersonsList.size() > 5) {
				context.drawGuiTexture(PLAYER_LISTS_SCROLLER_BACKGROUND_TEXTURE, this.x + 7, this.y + 33, 8, 116);
				int k = (int) (107.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.x + 8, this.y + 33 + 1 + k, 6, 7);
			}
			this.newTrustedPersonField.render(context, mouseX, mouseY, delta);
		} else if (this.showGuestListScreen) {
			context.drawText(this.textRenderer, TITLE_GUEST_LIST_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			context.drawText(this.textRenderer, TITLE_GUEST_LIST_DESCRIPTION_LABEL_TEXT, this.x + 8, this.y + 20, 0x404040, false);
			for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + 5, this.guestList.size()); i++) {
				String text = this.guestList.get(i);
				context.drawText(this.textRenderer, text, this.x + 19, this.y + 39 + ((i - this.scrollPosition) * 24), 0x404040, false);
			}
			if (this.guestList.size() > 5) {
				context.drawGuiTexture(PLAYER_LISTS_SCROLLER_BACKGROUND_TEXTURE, this.x + 7, this.y + 33, 8, 116);
				int k = (int) (107.0f * this.scrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.x + 8, this.y + 33 + 1 + k, 6, 7);
			}
			this.newGuestField.render(context, mouseX, mouseY, delta);
		} else {
			if (this.currentPermissionLevel == 0) {
				context.drawText(this.textRenderer, TITLE_OWNER_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			} else if (this.currentPermissionLevel == 1) {
				context.drawText(this.textRenderer, TITLE_CO_OWNER_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			} else if (this.currentPermissionLevel == 2) {
				context.drawText(this.textRenderer, TITLE_TRUSTED_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			} else if (this.currentPermissionLevel == 3) {
				context.drawText(this.textRenderer, TITLE_GUEST_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			} else if (this.currentPermissionLevel == 4) {
				context.drawText(this.textRenderer, TITLE_STRANGER_LABEL_TEXT, this.x + 8, this.y + 7, 0x404040, false);
			}

		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		this.drawBackground(context, mouseX, mouseY, delta);
	}

	public void drawBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		int i = this.x;
		int j = this.y;
		if (this.currentPermissionLevel == 0) {
			context.drawTexture(BACKGROUND_218_215_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
		} else if (this.currentPermissionLevel == 1 || this.ownerMode == HousingBlockEntity.OwnerMode.INTERACTION) {
			context.drawTexture(BACKGROUND_218_95_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
		} else {
			context.drawTexture(BACKGROUND_218_71_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
		}
	}

	private void updateHousingBlockAdventure() {
		if (this.housingBlockEntity != null) {
			ClientPlayNetworking.send(new UpdateHousingBlockAdventurePacket(
					this.housingBlockEntity.getPos(),
					this.coOwnerList,
					this.trustedPersonsList,
					this.guestList
			));
		}
	}

	private void toggleAdventureBuildingEffect() {
		ClientPlayNetworking.send(new AddStatusEffectPacket(
				Registries.STATUS_EFFECT.getId(StatusEffectsRegistry.BUILDING_MODE),
				-1,
				0,
				false,
				false,
				true,
				true
		));
		this.close();
	}

	private void resetHouse() {
		if (this.housingBlockEntity != null) {
			ClientPlayNetworking.send(new ResetHouseHousingBlockPacket(
					this.housingBlockEntity.getPos()
			));
		}
		this.close();
	}

	private void trySetHouseOwner(boolean claim) {
		if (this.housingBlockEntity != null && this.client != null && this.client.player != null) {
			ClientPlayNetworking.send(new SetHousingBlockOwnerPacket(
					this.housingBlockEntity.getPos(),
					claim ? this.client.player.getUuidAsString() : ""
			));
		}
		this.close();
	}
}
