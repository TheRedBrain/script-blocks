package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.data.VisibleLocation;
import com.github.theredbrain.scriptblocks.network.packet.SetManualResetLocationControlBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.TeleportFromTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.slotcustomizationapi.api.SlotCustomization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TeleporterBlockScreen extends AbstractContainerScreen<TeleporterBlockScreenHandler> implements ContainerListener {
	private static final Component EDIT_BUTTON_LABEL_TEXT = Component.translatable("gui.edit");
	private static final Component CANCEL_BUTTON_LABEL_TEXT = Component.translatable("gui.cancel");
	private static final Component CHOOSE_BUTTON_LABEL_TEXT = Component.translatable("gui.choose");
	private static final Component REGENERATE_BUTTON_LABEL_TEXT = Component.translatable("gui.teleporter_block.regenerate_button_label");
	private static final Component REGENERATE_DUNGEON_TEXT = Component.translatable("gui.teleporter_block.regenerate_dungeon");
	private static final Component CONFIRM_REGENERATION_BUTTON_LABEL_TEXT = Component.translatable("gui.teleporter_block.confirm_regeneration_button_label");
	private static final Component CANCEL_REGENERATION_BUTTON_LABEL_TEXT = Component.translatable("gui.teleporter_block.cancel_regeneration_button_label");
	private static final Component KEY_ITEM_IS_CONSUMED_TEXT = Component.translatable("gui.teleporter_block.key_item_is_consumed");
	private static final Component KEY_ITEM_IS_REQUIRED_TEXT = Component.translatable("gui.teleporter_block.key_item_is_required");
	private static final Component LOCATION_IS_PUBLIC_TEXT = Component.translatable("gui.teleporter_block.location_is_public");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_95_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_95");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final Identifier ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_screen.png");
	public static final Identifier ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_locations_screen.png");
	public static final WidgetSprites CHOOSE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/choose_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/choose_entry_button_highlighted")
	);
	public static final WidgetSprites OPEN_LIST_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/open_list_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/open_list_button_highlighted")
	);
	private final TeleporterBlockScreenHandler handler;
	private TeleporterBlockEntity teleporterBlock;

	private PlayerInfo currentPlayer;
	private PlayerInfo currentTargetOwner;
	private Button openChooseTargetOwnerScreenButton;
	private Button confirmChooseCurrentPlayerButton;
	private Button confirmChooseTeamMember0Button;
	private Button confirmChooseTeamMember1Button;
	private Button confirmChooseTeamMember2Button;
	private Button confirmChooseTeamMember3Button;
	private Button confirmChooseTeamMember4Button;
	private Button cancelChooseTargetOwnerButton;
	private VisibleLocation currentTarget;
	private String currentTargetDisplayName;
	private Button openChooseTargetIdentifierScreenButton;
	private Button confirmChooseTargetIdentifier0Button;
	private Button confirmChooseTargetIdentifier1Button;
	private Button confirmChooseTargetIdentifier2Button;
	private Button confirmChooseTargetIdentifier3Button;
	private Button cancelChooseTargetIdentifierButton;
	private String currentTargetEntranceDisplayName;
	private Button teleportButton;
	private Button cancelTeleportButton;
	private Button openDungeonRegenerationScreenButton;
	private Button confirmDungeonRegenerationButton;
	private Button cancelDungeonRegenerationButton;

	private boolean showCurrentEntranceDisplayNameFirst;
	private boolean showChooseTargetOwnerScreen;
	private boolean showChooseTargetIdentifierScreen;
	private boolean showRegenerationConfirmScreen;
	private boolean canOwnerBeChosen;
	private boolean showAdventureScreen;
	private boolean showCancelButton;

	private TeleporterBlockEntity.TeleportationMode teleportationMode;

	List<PlayerInfo> partyMemberList = new ArrayList<>();
	private int partyMemberListScrollPosition = 0;
	private int visibleLocationsListScrollPosition = 0;
	private float partyMemberListScrollAmount = 0.0f;
	private float visibleLocationsListScrollAmount = 0.0f;
	private boolean partyMemberListMouseClicked = false;
	private boolean visibleLocationsListMouseClicked = false;

	private boolean canLocationBeRegenerated = true;
	private boolean isCurrentLocationUnlocked;
	private boolean areCurrentLocationKeysPresent;
	private boolean isCurrentLocationPublic;
	private boolean showCurrentLocationOwner;

	public TeleporterBlockScreen(TeleporterBlockScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		this.handler = handler;
	}

	private void cancelTeleport() {
		this.onClose();
	}

	private void openChooseCurrentTargetOwnerScreen() {
		this.partyMemberList.clear();
		if (this.minecraft != null && this.minecraft.player != null) {
			PlayerTeam team = this.minecraft.player.getTeam();
			if (team != null) {
				for (String name : team.getPlayers()) {
					if (!this.minecraft.player.getName().getString().equals(name)) {
						this.partyMemberList.add(this.minecraft.player.connection.getPlayerInfo(name));
					}
				}
			}
		}
		this.showChooseTargetOwnerScreen = true;
		this.updateWidgets();
	}

	private void chooseTargetOwner(int index) {
		if (index == -1 && this.minecraft != null && this.minecraft.player != null) {
			this.currentTargetOwner = this.minecraft.player.connection.getPlayerInfo(this.minecraft.player.getUUID());
		} else {
			this.currentTargetOwner = this.partyMemberList.get(this.partyMemberListScrollPosition + index);
		}
		this.showChooseTargetOwnerScreen = false;
		this.calculateUnlockedAndVisibleLocations();
		this.updateWidgets();
	}

	private void cancelChooseCurrentTargetOwner() {
		this.showChooseTargetOwnerScreen = false;
		this.updateWidgets();
	}

	private void openChooseTargetIdentifierScreen() {
		this.showChooseTargetIdentifierScreen = true;
		this.updateWidgets();
	}

	private void chooseTargetIdentifier(int index) {
		if (this.minecraft != null && this.minecraft.gameMode != null && this.handler.onButtonClick(this.minecraft.player, this.visibleLocationsListScrollPosition + index)) {

			this.minecraft.gameMode.handleInventoryButtonClick(this.handler.syncId, this.visibleLocationsListScrollPosition + index);
			this.currentTarget = this.handler.getVisibleLocationsList().get(this.visibleLocationsListScrollPosition + index);
			this.showChooseTargetIdentifierScreen = false;
			this.calculateUnlockedAndVisibleLocations();
			this.updateWidgets();
		}
	}

	private void cancelChooseTargetLocation() {
		this.showChooseTargetIdentifierScreen = false;
		this.updateWidgets();
	}

	private void openDungeonRegenerationConfirmScreen() {
		this.showRegenerationConfirmScreen = true;
		this.setBackgroundDimensions();
		this.confirmDungeonRegenerationButton.setY(this.topPos + 20);
		this.cancelDungeonRegenerationButton.setY(this.topPos + 20);
		this.updateWidgets();
	}

	private void confirmDungeonRegeneration() {
		this.tryDungeonRegeneration();
	}

	private void cancelDungeonRegeneration() {
		this.showRegenerationConfirmScreen = false;
		this.setBackgroundDimensions();
		this.updateWidgets();
	}

	private void setBackgroundDimensions() {
		if (this.showRegenerationConfirmScreen) {
			this.imageWidth = 230;
		} else {
			this.imageWidth = 218;
		}
		if (this.teleporterBlock.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS && !this.showRegenerationConfirmScreen) {
			this.imageHeight = 171;
		} else {
			this.imageHeight = 47;
		}
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	protected void init() {
		if (this.minecraft != null && this.minecraft.level != null) {
			BlockEntity blockEntity = this.minecraft.level.getBlockEntity(this.handler.getBlockPos());
			if (blockEntity instanceof TeleporterBlockEntity) {
				this.teleporterBlock = (TeleporterBlockEntity) blockEntity;
			}
			if (this.minecraft.player != null) {
				this.currentPlayer = this.minecraft.player.connection.getPlayerInfo(this.minecraft.player.getUUID());
				this.currentTargetOwner = this.currentPlayer;
			}
		}
		this.canOwnerBeChosen = this.teleporterBlock.canOwnerBeChosen();
		this.showAdventureScreen = this.teleporterBlock.getShowAdventureScreen();
		this.teleportationMode = this.teleporterBlock.getTeleportationMode();
		this.showCancelButton = this.teleporterBlock.showCancelButton();
		this.currentTarget = VisibleLocation.DEFAULT;
		this.currentTargetDisplayName = "";
		this.currentTargetEntranceDisplayName = "";
		if ((this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT || this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS)) {
			if (!this.showAdventureScreen) {
				this.teleport();
				this.onClose();
				return;
			}
		} else {

			if (this.handler.getVisibleLocationsList().isEmpty()) {
				this.onClose();
				return;
			}
			this.currentTarget = this.handler.getVisibleLocationsList().getFirst();
			if (this.minecraft.gameMode != null && this.handler.onButtonClick(this.minecraft.player, -1)) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.handler.syncId, -1);
			}
			this.calculateUnlockedAndVisibleLocations();

			if (!this.showAdventureScreen && this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {
				if (this.handler.getVisibleLocationsList().getFirst().isUnlocked()) {
					this.teleport();
				}
				// TODO send message?
				this.onClose();
				return;
			}
		}

		this.setBackgroundDimensions();

		this.openChooseTargetIdentifierScreenButton = this.addRenderableWidget(new ImageButton(this.leftPos + 7, this.topPos + 21, 20, 20, OPEN_LIST_BUTTON_TEXTURES, button -> this.openChooseTargetIdentifierScreen()));
		this.confirmChooseTargetIdentifier0Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 20, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(0)));
		this.confirmChooseTargetIdentifier1Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 44, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(1)));
		this.confirmChooseTargetIdentifier2Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 68, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(2)));
		this.confirmChooseTargetIdentifier3Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 92, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(3)));
		this.cancelChooseTargetIdentifierButton = this.addRenderableWidget(Button.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseTargetLocation()).bounds(this.leftPos + 7, this.topPos + this.imageHeight - 27, this.imageWidth - 14, 20).build());

		this.openChooseTargetOwnerScreenButton = this.addRenderableWidget(new ImageButton(this.leftPos + 7, this.topPos + 71, 20, 20, OPEN_LIST_BUTTON_TEXTURES, button -> this.openChooseCurrentTargetOwnerScreen()));
		this.confirmChooseCurrentPlayerButton = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 20, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(-1)));
		this.confirmChooseTeamMember0Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 44, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(0)));
		this.confirmChooseTeamMember1Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 68, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(1)));
		this.confirmChooseTeamMember2Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 92, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(2)));
		this.confirmChooseTeamMember3Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 116, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(3)));
		this.confirmChooseTeamMember4Button = this.addRenderableWidget(new ImageButton(this.leftPos + 19, this.topPos + 140, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(4)));
		this.cancelChooseTargetOwnerButton = this.addRenderableWidget(Button.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseCurrentTargetOwner()).bounds(this.leftPos + 7, this.topPos + this.imageHeight - 27, this.imageWidth - 14, 20).build());

		this.openDungeonRegenerationScreenButton = this.addRenderableWidget(Button.builder(REGENERATE_BUTTON_LABEL_TEXT, button -> this.openDungeonRegenerationConfirmScreen()).bounds(this.leftPos + 7, this.topPos + this.imageHeight - 51, this.imageWidth - 14, 20).build());
		this.confirmDungeonRegenerationButton = this.addRenderableWidget(Button.builder(CONFIRM_REGENERATION_BUTTON_LABEL_TEXT, button -> this.confirmDungeonRegeneration()).bounds(this.leftPos + 7, this.topPos + 20, (this.imageWidth - 18) / 2, 20).build());
		this.cancelDungeonRegenerationButton = this.addRenderableWidget(Button.builder(CANCEL_REGENERATION_BUTTON_LABEL_TEXT, button -> this.cancelDungeonRegeneration()).bounds(this.leftPos + this.imageWidth / 2 + 2, this.topPos + 20, (this.imageWidth - 18) / 2, 20).build());

		this.teleportButton = this.addRenderableWidget(Button.builder(Component.translatable(this.teleporterBlock.getTeleportButtonLabel()), button -> this.teleport()).dimensions(this.leftPos + 7, this.topPos + this.imageHeight - 27, 100, 20).build());
		this.cancelTeleportButton = this.addRenderableWidget(Button.builder(Component.translatable(this.teleporterBlock.getCancelTeleportButtonLabel()), button -> this.cancelTeleport()).dimensions(this.leftPos + this.imageWidth - 107, this.topPos + this.imageHeight - 27, 100, 20).build());

		this.setInitialFocus(this.teleportButton);
		this.updateWidgets();
	}

	@Override
	public void containerTick() {
		super.containerTick();
		if (this.minecraft != null) {
			if (this.minecraft.gameMode != null && this.handler.onButtonClick(this.minecraft.player, -1)) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.handler.syncId, -1);
			}
		}
	}

	private void updateWidgets() {

		this.openChooseTargetIdentifierScreenButton.visible = false;
		this.confirmChooseTargetIdentifier0Button.visible = false;
		this.confirmChooseTargetIdentifier1Button.visible = false;
		this.confirmChooseTargetIdentifier2Button.visible = false;
		this.confirmChooseTargetIdentifier3Button.visible = false;
		this.cancelChooseTargetIdentifierButton.visible = false;

		this.openChooseTargetOwnerScreenButton.visible = false;
		this.confirmChooseCurrentPlayerButton.visible = false;
		this.confirmChooseTeamMember0Button.visible = false;
		this.confirmChooseTeamMember1Button.visible = false;
		this.confirmChooseTeamMember2Button.visible = false;
		this.confirmChooseTeamMember3Button.visible = false;
		this.confirmChooseTeamMember4Button.visible = false;
		this.cancelChooseTargetOwnerButton.visible = false;

		this.openDungeonRegenerationScreenButton.visible = false;
		this.confirmDungeonRegenerationButton.visible = false;
		this.cancelDungeonRegenerationButton.visible = false;

		this.teleportButton.visible = false;
		this.cancelTeleportButton.visible = false;

		for (Slot slot : this.handler.slots) {
			((SlotCustomization) slot).slotcustomizationapi$setDisabledOverride(true);
		}

		if (this.showChooseTargetIdentifierScreen) {

			int index = 0;
			for (int i = 0; i < Math.min(4, this.handler.getVisibleLocationsList().size()); i++) {
				if (index == 0) {
					this.confirmChooseTargetIdentifier0Button.visible = true;
				} else if (index == 1) {
					this.confirmChooseTargetIdentifier1Button.visible = true;
				} else if (index == 2) {
					this.confirmChooseTargetIdentifier2Button.visible = true;
				} else if (index == 3) {
					this.confirmChooseTargetIdentifier3Button.visible = true;
				}
				index++;
			}

			this.cancelChooseTargetIdentifierButton.visible = true;

		} else if (this.showChooseTargetOwnerScreen) {

			if (!this.isCurrentLocationPublic) {
				this.confirmChooseCurrentPlayerButton.visible = true;

				int index = 0;
				for (int i = 0; i < Math.min(5, this.partyMemberList.size()); i++) {
					if (index == 0) {
						this.confirmChooseTeamMember0Button.visible = true;
					} else if (index == 1) {
						this.confirmChooseTeamMember1Button.visible = true;
					} else if (index == 2) {
						this.confirmChooseTeamMember2Button.visible = true;
					} else if (index == 3) {
						this.confirmChooseTeamMember3Button.visible = true;
					} else if (index == 4) {
						this.confirmChooseTeamMember4Button.visible = true;
					}
					index++;
				}

			}
			this.cancelChooseTargetOwnerButton.visible = true;

		} else if (this.showRegenerationConfirmScreen) {

			this.confirmDungeonRegenerationButton.visible = true;
			this.cancelDungeonRegenerationButton.visible = true;

		} else if (this.showAdventureScreen) {

			if (this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				this.openChooseTargetIdentifierScreenButton.visible = this.handler.getVisibleLocationsList().size() > 1;

				this.openChooseTargetOwnerScreenButton.visible = this.canOwnerBeChosen && !this.isCurrentLocationPublic;

				this.openDungeonRegenerationScreenButton.visible = this.currentTarget.allowRegeneration();
				this.openDungeonRegenerationScreenButton.active = this.isCurrentLocationUnlocked && this.canLocationBeRegenerated && this.currentTargetOwner != null;
			}

			this.teleportButton.visible = true;

			if (this.showCancelButton) {
				this.teleportButton.setWidth(100);
				this.cancelTeleportButton.visible = true;
			} else {
				this.teleportButton.setWidth(204);
			}
		}
	}

	private void calculateUnlockedAndVisibleLocations() {

		this.isCurrentLocationUnlocked = this.currentTarget.isUnlocked();

		Location location = null;
		Level world = this.teleporterBlock.getWorld();
		if (world != null) {
			Optional<Holder.Reference<Location>> optionalLocationReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(this.currentTarget.identifier()));
			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}
		}

		if (location != null) {
			this.currentTargetDisplayName = location.displayName();
			this.currentTargetEntranceDisplayName = LocationUtils.getEntranceDisplayName(location, this.currentTarget.entrance());
			this.showCurrentEntranceDisplayNameFirst = LocationUtils.isEntranceNameShownFirst(location, this.currentTarget.entrance());
			this.isCurrentLocationPublic = location.isPublic();

			this.showCurrentLocationOwner = LocationUtils.showLocationOwnerForEntrance(location, this.currentTarget.entrance());

			Container inventory = new SimpleContainer(36);
			for (int k = 0; k < 36; k++) {
				ItemStack itemStack = this.handler.getPlayerInventory().getStack(k);
				inventory.setItem(k, itemStack.copy());
			}
			this.areCurrentLocationKeysPresent = true;
			for (CommonDataStructures.ItemCost itemCost : LocationUtils.getKeyForEntrance(location, this.currentTarget.entrance())) {
				ItemStack currentKeyItemStack = itemCost.itemStack();
				int keyCount = currentKeyItemStack.getCount();
				for (int j = 0; j < inventory.getContainerSize(); j++) {
					ItemStack itemStack = inventory.getItem(j);
					if (ItemStack.isSameItemSameComponents(currentKeyItemStack, itemStack)) {
						int stackCount = inventory.getItem(j).getCount();
						if (stackCount >= keyCount) {
							inventory.removeItem(j, keyCount);
							keyCount = 0;
							break;
						} else {
							inventory.setItem(j, ItemStack.EMPTY);
							keyCount = keyCount - stackCount;
						}
					}
				}
				if (keyCount > 0) {
					this.areCurrentLocationKeysPresent = false;
				}
			}
			this.canLocationBeRegenerated = true;
			for (CommonDataStructures.ItemCost regenerationItemCost : LocationUtils.getResetCostForEntrance(location, this.currentTarget.entrance())) {
				ItemStack currentKeyItemStack = regenerationItemCost.itemStack();
				int keyCount = currentKeyItemStack.getCount();
				for (int j = 0; j < inventory.getContainerSize(); j++) {
					ItemStack itemStack = inventory.getItem(j);
					if (ItemStack.isSameItemSameComponents(currentKeyItemStack, itemStack)) {
						int stackCount = inventory.getItem(j).getCount();
						if (stackCount >= keyCount) {
							inventory.removeItem(j, keyCount);
							keyCount = 0;
							break;
						} else {
							inventory.setItem(j, ItemStack.EMPTY);
							keyCount = keyCount - stackCount;
						}
					}
				}
				if (keyCount > 0) {
					this.canLocationBeRegenerated = false;
				}
			}
		}
	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		String string = this.currentTargetDisplayName;
		String string1 = this.currentTargetEntranceDisplayName;
		VisibleLocation var = new VisibleLocation(this.currentTarget);
		List<PlayerInfo> list = new ArrayList<>(this.partyMemberList);
		this.init(client, width, height);
		this.currentTargetDisplayName = string;
		this.currentTargetEntranceDisplayName = string1;
		this.currentTarget = new VisibleLocation(var);
		this.partyMemberList.clear();
		this.partyMemberList.addAll(list);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.partyMemberListMouseClicked = false;
		this.visibleLocationsListMouseClicked = false;
		int i;
		int j;
		if (this.showChooseTargetOwnerScreen) {
			i = this.leftPos + 8;
			j = this.topPos + 45;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 114)) {
				this.partyMemberListMouseClicked = true;
			}
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4) {
			i = this.leftPos + 8;
			j = this.topPos + 21;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 90)) {
				this.visibleLocationsListMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.showChooseTargetOwnerScreen
				&& this.partyMemberList.size() > 5
				&& this.partyMemberListMouseClicked) {
			int i = this.partyMemberList.size() - 5;
			float f = (float) deltaY / (float) i;
			this.partyMemberListScrollAmount = Mth.clamp(this.partyMemberListScrollAmount + f, 0.0f, 1.0f);
			this.partyMemberListScrollPosition = (int) ((double) (this.partyMemberListScrollAmount * (float) i));
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4
				&& this.visibleLocationsListMouseClicked) {
			int i = this.handler.getVisibleLocationsList().size() - 4;
			float f = (float) deltaY / (float) i;
			this.visibleLocationsListScrollAmount = Mth.clamp(this.visibleLocationsListScrollAmount + f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.showChooseTargetOwnerScreen
				&& this.partyMemberList.size() > 5
				&& mouseX >= (double) (this.leftPos + 7) && mouseX <= (double) (this.leftPos + this.imageWidth - 7)
				&& mouseY >= this.topPos + 44 && mouseY <= this.topPos + 116) {
			int i = this.partyMemberList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.partyMemberListScrollAmount = Mth.clamp(this.partyMemberListScrollAmount - f, 0.0f, 1.0f);
			this.partyMemberListScrollPosition = (int) ((double) (this.partyMemberListScrollAmount * (float) i));
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4
				&& mouseX >= (double) (this.leftPos + 7) && mouseX <= (double) (this.leftPos + this.imageWidth - 7)
				&& mouseY >= this.topPos + 20 && mouseY <= this.topPos + 92) {
			int i = this.handler.getVisibleLocationsList().size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.visibleLocationsListScrollAmount = Mth.clamp(this.visibleLocationsListScrollAmount - f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.renderTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {

		int x = this.leftPos;
		int y = this.topPos;
		TeleporterBlockEntity.TeleportationMode mode = this.teleporterBlock.getTeleportationMode();

		if (this.showChooseTargetOwnerScreen) {
			context.blit(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

			context.blit(this.currentPlayer.getSkin().texture(), x + 43/*7*/, y + 26, 8, 8, 8, 8, 8, 8, 64, 64);
			context.drawString(this.font, this.currentPlayer.getProfile().getName(), x + 55/*19*/, y + 26, 0x404040, false);

			for (int i = this.partyMemberListScrollPosition; i < Math.min(this.partyMemberListScrollPosition + 4, this.partyMemberList.size()); i++) {

				PlayerInfo playerListEntry = this.partyMemberList.get(i);
				context.blit(playerListEntry.getSkin().texture(), x + 43/*7*/, y + 50 + ((i - this.partyMemberListScrollPosition) * 24), 8, 8, 8, 8, 8, 8, 64, 64);
				context.drawString(this.font, playerListEntry.getProfile().getName(), x + 55/*19*/, y + 50 + ((i - this.partyMemberListScrollPosition) * 24), 0x404040, false);
			}
			if (this.partyMemberList.size() > 5) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_95_TEXTURE, x + 7, y + 44, 8, 116);
				int k = (int) (105.0f * this.partyMemberListScrollAmount);
				context.blitSprite(SCROLLER_TEXTURE, x + 8, y + 44 + 1 + k, 6, 7);
			}
		} else if (this.showChooseTargetIdentifierScreen) {

			context.blit(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
			VisibleLocation visibleLocation;
			for (int i = this.visibleLocationsListScrollPosition; i < Math.min(this.visibleLocationsListScrollPosition + 4, this.handler.getVisibleLocationsList().size()); i++) {

				visibleLocation = this.handler.getVisibleLocationsList().get(i);
				if (!visibleLocation.entrance().isEmpty() && !visibleLocation.entranceDisplayName().isEmpty()) {
					if (visibleLocation.isEntranceNameShownFirst()) {
						context.drawString(this.font, Component.translatable(visibleLocation.entranceDisplayName()), x + 43, y + 19, 0x404040, false);
						context.drawString(this.font, Component.translatable(visibleLocation.displayName()), x + 43, y + 32, 0x404040, false);
					} else {
						context.drawString(this.font, Component.translatable(visibleLocation.displayName()), x + 43, y + 19, 0x404040, false);
						context.drawString(this.font, Component.translatable(visibleLocation.entranceDisplayName()), x + 43, y + 32, 0x404040, false);
					}
				} else {
					context.drawString(this.font, Component.translatable(visibleLocation.displayName()), x + 43, y + 26 + ((i - this.visibleLocationsListScrollPosition) * 24), 0x404040, false);
				}
			}
			if (this.handler.getVisibleLocationsList().size() > 4) {
				context.blitSprite(SCROLL_BAR_BACKGROUND_8_95_TEXTURE, x + 7, y + 20, 8, 92);
				int k = (int) (83.0f * this.visibleLocationsListScrollAmount);
				context.blitSprite(SCROLLER_TEXTURE, x + 8, y + 20 + 1 + k, 6, 7);
			}

		} else if (this.showRegenerationConfirmScreen) {
			context.blit(ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

			int regenerationTextOffset = this.imageWidth / 2 - this.font.width(REGENERATE_DUNGEON_TEXT) / 2;

			context.drawString(this.font, REGENERATE_DUNGEON_TEXT, x + regenerationTextOffset, y + 7, 0x404040, false);
		} else if (this.showAdventureScreen) {

			if (mode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				context.blit(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

				Component teleporterName = Component.translatable(this.teleporterBlock.getTeleporterName());
				int teleporterNameOffset = this.imageWidth / 2 - this.font.width(teleporterName) / 2;

				context.drawString(this.font, teleporterName, x + teleporterNameOffset, y + 7, 0x404040, false);

				if (!this.currentTargetDisplayName.isEmpty() || this.handler.getVisibleLocationsList().size() > 1) {
					if (!this.currentTarget.entrance().isEmpty() && !this.currentTargetEntranceDisplayName.isEmpty()) {
						if (this.showCurrentEntranceDisplayNameFirst) {
							context.drawString(this.font, Component.translatable(this.currentTargetEntranceDisplayName), x + 31, y + 20, 0x404040, false);
							context.drawString(this.font, Component.translatable(this.currentTargetDisplayName), x + 31, y + 33, 0x404040, false);
						} else {
							context.drawString(this.font, Component.translatable(this.currentTargetDisplayName), x + 31, y + 20, 0x404040, false);
							context.drawString(this.font, Component.translatable(this.currentTargetEntranceDisplayName), x + 31, y + 33, 0x404040, false);
						}

					} else {

						context.drawString(this.font, Component.translatable(this.currentTargetDisplayName), x + 31, y + 27, 0x404040, false);

					}
				}
				if (this.handler.getCurrentLocationCooldown() > 0) {
					this.teleportButton.setMessage(Component.translatable("gui.teleporter_block.teleport_button.cooldown_label", LocationUtils.getCooldownText(this.handler.getCurrentLocationCooldown())));
				} else {
					this.teleportButton.setMessage(Component.translatable(this.teleporterBlock.getTeleportButtonLabel()));
				}

				this.teleportButton.active = this.isCurrentLocationUnlocked && this.areCurrentLocationKeysPresent && this.handler.getCurrentLocationCooldown() <= 0;

				if (this.isCurrentLocationPublic && this.showCurrentLocationOwner) {
					context.drawString(this.font, LOCATION_IS_PUBLIC_TEXT, x + 19, y + 77, 0x404040, false);
				} else if (this.currentTargetOwner != null && this.showCurrentLocationOwner) {
					context.drawString(this.font, Component.translatable(this.teleporterBlock.getCurrentTargetOwnerLabel()), x + 8, y + 58, 0x404040, false);

					if (this.canOwnerBeChosen && !this.isCurrentLocationPublic) {
						context.blit(currentTargetOwner.getSkin().texture(), x + 31, y + 77, 8, 8, 8, 8, 8, 8, 64, 64);
						context.drawString(this.font, currentTargetOwner.getProfile().getName(), x + 43, y + 77, 0x404040, false);
					} else {
						context.blit(currentTargetOwner.getSkin().texture(), x + 7, y + 77, 8, 8, 8, 8, 8, 8, 64, 64);
						context.drawString(this.font, currentTargetOwner.getProfile().getName(), x + 19, y + 77, 0x404040, false);
					}
				}

				// TODO redo key rendering, possibly with slots
//				if (this.currentKeyItemStack != null && !this.currentKeyItemStack.isEmpty()) {
//					ItemStack currentKey = this.currentKeyItemStack;
//					context.drawItemWithoutEntity(currentKey, x + 8, y + 95);
//					context.drawItemInSlot(this.textRenderer, currentKey, x + 8, y + 95);
//					context.drawText(this.textRenderer, this.consumeKeyItem ? KEY_ITEM_IS_CONSUMED_TEXT : KEY_ITEM_IS_REQUIRED_TEXT, x + 26, y + 100, 0x404040, false);
//				}
			} else {
				context.blit(ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

				Component teleporterName = Component.translatable(this.teleporterBlock.getTeleporterName());
				int teleporterNameOffset = this.imageWidth / 2 - this.font.width(teleporterName) / 2;

				context.drawString(this.font, teleporterName, x + teleporterNameOffset, y + 7, 0x404040, false);
			}
		}
	}

	protected boolean isValidCharacterForDisplayName(String name, char character, int cursorPos) {
		int i = name.indexOf(58);
		int j = name.indexOf(47);
		if (character == ':') {
			return (j == -1 || cursorPos <= j) && i == -1;
		}
		if (character == '/') {
			return cursorPos > i;
		}
		return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z' || character >= '0' && character <= '9' || character == '.' || character == ' ';
	}

	private void renderSlotTooltip(GuiGraphics context, int mouseX, int mouseY) {
		Optional<Component> optional = Optional.empty();
		if (this.hoveredSlot != null) {
			ItemStack itemStack = this.handler.getSlot(37).getStack();
			if (itemStack.isEmpty()) {
				if (this.hoveredSlot.index == 37) {
					optional = Optional.of(Component.translatable("gui.teleporter_block.requiredItemStackSlot.tooltip"));
				}
			}
		}
		optional.ifPresent(text -> context.setTooltipForNextFrame(this.font, this.font.split((FormattedText) text, 115), mouseX, mouseY));
	}

	private void tryDungeonRegeneration() {
		if (this.canLocationBeRegenerated) {
			String currentTargetOwnerName = "";
			if (!this.isCurrentLocationPublic && this.currentTargetOwner != null) {
				currentTargetOwnerName = this.currentTargetOwner.getProfile().getName();
			}
			ClientPlayNetworking.send(new SetManualResetLocationControlBlockPacket(
					currentTargetOwnerName,
					this.currentTarget.identifier(),
					this.currentTarget.entrance(),
					true,
					true
			));
		}
	}

	private void teleport() {
		String currentWorld = "";
		String currentTargetOwnerName = "";
		if (this.teleporterBlock.getWorld() != null) {
			currentWorld = this.teleporterBlock.getWorld().getRegistryKey().getValue().toString();
		}
		if (this.currentTargetOwner != null && !this.isCurrentLocationPublic) {
			currentTargetOwnerName = this.currentTargetOwner.getProfile().getName();
		}
		ClientPlayNetworking.send(new TeleportFromTeleporterBlockPacket(
				this.teleporterBlock.getPos(),
				currentWorld,
				currentTargetOwnerName,
				this.currentTarget.identifier(),
				this.currentTarget.entrance(),
				this.currentTarget.dataIdentifier(),
				this.currentTarget.dataValue()
		));
	}

	@Override
	public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {

	}

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {
		if (property == 0 && value <= 0) {
			this.calculateUnlockedAndVisibleLocations();
		}
	}
}
