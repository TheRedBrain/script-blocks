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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TeleporterBlockScreen extends HandledScreen<TeleporterBlockScreenHandler> implements ScreenHandlerListener {
	private static final Text EDIT_BUTTON_LABEL_TEXT = Text.translatable("gui.edit");
	private static final Text CANCEL_BUTTON_LABEL_TEXT = Text.translatable("gui.cancel");
	private static final Text CHOOSE_BUTTON_LABEL_TEXT = Text.translatable("gui.choose");
	private static final Text REGENERATE_BUTTON_LABEL_TEXT = Text.translatable("gui.teleporter_block.regenerate_button_label");
	private static final Text REGENERATE_DUNGEON_TEXT = Text.translatable("gui.teleporter_block.regenerate_dungeon");
	private static final Text CONFIRM_REGENERATION_BUTTON_LABEL_TEXT = Text.translatable("gui.teleporter_block.confirm_regeneration_button_label");
	private static final Text CANCEL_REGENERATION_BUTTON_LABEL_TEXT = Text.translatable("gui.teleporter_block.cancel_regeneration_button_label");
	private static final Text KEY_ITEM_IS_CONSUMED_TEXT = Text.translatable("gui.teleporter_block.key_item_is_consumed");
	private static final Text KEY_ITEM_IS_REQUIRED_TEXT = Text.translatable("gui.teleporter_block.key_item_is_required");
	private static final Text LOCATION_IS_PUBLIC_TEXT = Text.translatable("gui.teleporter_block.location_is_public");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_95_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_95");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final Identifier ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_screen.png");
	public static final Identifier ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_locations_screen.png");
	public static final ButtonTextures CHOOSE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/choose_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/choose_entry_button_highlighted")
	);
	public static final ButtonTextures OPEN_LIST_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/open_list_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/open_list_button_highlighted")
	);
	private final TeleporterBlockScreenHandler handler;
	private TeleporterBlockEntity teleporterBlock;

	private PlayerListEntry currentPlayer;
	private PlayerListEntry currentTargetOwner;
	private ButtonWidget openChooseTargetOwnerScreenButton;
	private ButtonWidget confirmChooseCurrentPlayerButton;
	private ButtonWidget confirmChooseTeamMember0Button;
	private ButtonWidget confirmChooseTeamMember1Button;
	private ButtonWidget confirmChooseTeamMember2Button;
	private ButtonWidget confirmChooseTeamMember3Button;
	private ButtonWidget confirmChooseTeamMember4Button;
	private ButtonWidget cancelChooseTargetOwnerButton;
	private VisibleLocation currentTarget;
	private String currentTargetDisplayName;
	private ButtonWidget openChooseTargetIdentifierScreenButton;
	private ButtonWidget confirmChooseTargetIdentifier0Button;
	private ButtonWidget confirmChooseTargetIdentifier1Button;
	private ButtonWidget confirmChooseTargetIdentifier2Button;
	private ButtonWidget confirmChooseTargetIdentifier3Button;
	private ButtonWidget cancelChooseTargetIdentifierButton;
	private String currentTargetEntranceDisplayName;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelTeleportButton;
	private ButtonWidget openDungeonRegenerationScreenButton;
	private ButtonWidget confirmDungeonRegenerationButton;
	private ButtonWidget cancelDungeonRegenerationButton;

	private boolean showCurrentEntranceDisplayNameFirst;
	private boolean showChooseTargetOwnerScreen;
	private boolean showChooseTargetIdentifierScreen;
	private boolean showRegenerationConfirmScreen;
	private boolean canOwnerBeChosen;
	private boolean showAdventureScreen;
	private boolean showCancelButton;

	private TeleporterBlockEntity.TeleportationMode teleportationMode;

	List<PlayerListEntry> partyMemberList = new ArrayList<>();
	private int partyMemberListScrollPosition = 0;
	private int visibleLocationsListScrollPosition = 0;
	private float partyMemberListScrollAmount = 0.0f;
	private float visibleLocationsListScrollAmount = 0.0f;
	private boolean partyMemberListMouseClicked = false;
	private boolean visibleLocationsListMouseClicked = false;

	private boolean canLocationBeRegenerated = false;
	private boolean isCurrentLocationUnlocked;
	private boolean areCurrentLocationKeysPresent;
	private boolean isCurrentLocationPublic;
	private boolean showCurrentLocationOwner;

	public TeleporterBlockScreen(TeleporterBlockScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.handler = handler;
	}

	private void cancelTeleport() {
		this.close();
	}

	private void openChooseCurrentTargetOwnerScreen() {
		this.partyMemberList.clear();
		if (this.client != null && this.client.player != null) {
			Team team = this.client.player.getScoreboardTeam();
			if (team != null) {
				for (String name : team.getPlayerList()) {
					if (!this.client.player.getName().getString().equals(name)) {
						this.partyMemberList.add(this.client.player.networkHandler.getPlayerListEntry(name));
					}
				}
			}
		}
		this.showChooseTargetOwnerScreen = true;
		this.updateWidgets();
	}

	private void chooseTargetOwner(int index) {
		this.canLocationBeRegenerated = false;
		if (index == -1 && this.client != null && this.client.player != null) {
			this.currentTargetOwner = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
			this.canLocationBeRegenerated = this.currentTarget.allowRegeneration();
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
		if (this.client != null && this.client.interactionManager != null && this.handler.onButtonClick(this.client.player, this.visibleLocationsListScrollPosition + index)) {

			this.client.interactionManager.clickButton(this.handler.syncId, this.visibleLocationsListScrollPosition + index);
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
		this.confirmDungeonRegenerationButton.setY(this.y + 20);
		this.cancelDungeonRegenerationButton.setY(this.y + 20);
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
			this.backgroundWidth = 230;
		} else {
			this.backgroundWidth = 218;
		}
		if (this.teleporterBlock.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS && !this.showRegenerationConfirmScreen) {
			this.backgroundHeight = 171;
		} else {
			this.backgroundHeight = 47;
		}
		this.x = (this.width - this.backgroundWidth) / 2;
		this.y = (this.height - this.backgroundHeight) / 2;
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	protected void init() {
		if (this.client != null && this.client.world != null) {
			BlockEntity blockEntity = this.client.world.getBlockEntity(this.handler.getBlockPos());
			if (blockEntity instanceof TeleporterBlockEntity) {
				this.teleporterBlock = (TeleporterBlockEntity) blockEntity;
			}
			if (this.client.player != null) {
				this.currentPlayer = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
				this.currentTargetOwner = this.currentPlayer;
				this.canLocationBeRegenerated = true;
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
				this.close();
				return;
			}
		} else {

			if (this.handler.getVisibleLocationsList().isEmpty()) {
				this.close();
				return;
			}
			this.currentTarget = this.handler.getVisibleLocationsList().getFirst();
			if (this.client.interactionManager != null && this.handler.onButtonClick(this.client.player, -1)) {
				this.client.interactionManager.clickButton(this.handler.syncId, -1);
			}
			this.calculateUnlockedAndVisibleLocations();

			if (!this.showAdventureScreen && this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATION) {
				if (this.handler.getVisibleLocationsList().getFirst().isUnlocked()) {
					this.teleport();
				}
				// TODO send message?
				this.close();
				return;
			}
		}
		this.canLocationBeRegenerated = this.currentTarget.allowRegeneration();

		this.setBackgroundDimensions();

		this.openChooseTargetIdentifierScreenButton = this.addDrawableChild(new TexturedButtonWidget(this.x + 7, this.y + 21, 20, 20, OPEN_LIST_BUTTON_TEXTURES, button -> this.openChooseTargetIdentifierScreen()));
		this.confirmChooseTargetIdentifier0Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 20, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(0)));
		this.confirmChooseTargetIdentifier1Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 44, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(1)));
		this.confirmChooseTargetIdentifier2Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 68, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(2)));
		this.confirmChooseTargetIdentifier3Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 92, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetIdentifier(3)));
		this.cancelChooseTargetIdentifierButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseTargetLocation()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		this.openChooseTargetOwnerScreenButton = this.addDrawableChild(new TexturedButtonWidget(this.x + 7, this.y + 71, 20, 20, OPEN_LIST_BUTTON_TEXTURES, button -> this.openChooseCurrentTargetOwnerScreen()));
		this.confirmChooseCurrentPlayerButton = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 20, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(-1)));
		this.confirmChooseTeamMember0Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 44, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(0)));
		this.confirmChooseTeamMember1Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 68, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(1)));
		this.confirmChooseTeamMember2Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 92, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(2)));
		this.confirmChooseTeamMember3Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 116, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(3)));
		this.confirmChooseTeamMember4Button = this.addDrawableChild(new TexturedButtonWidget(this.x + 19, this.y + 140, 20, 20, CHOOSE_ENTRY_BUTTON_TEXTURES, button -> this.chooseTargetOwner(4)));
		this.cancelChooseTargetOwnerButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseCurrentTargetOwner()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		this.openDungeonRegenerationScreenButton = this.addDrawableChild(ButtonWidget.builder(REGENERATE_BUTTON_LABEL_TEXT, button -> this.openDungeonRegenerationConfirmScreen()).dimensions(this.x + 7, this.y + this.backgroundHeight - 51, this.backgroundWidth - 14, 20).build());
		this.confirmDungeonRegenerationButton = this.addDrawableChild(ButtonWidget.builder(CONFIRM_REGENERATION_BUTTON_LABEL_TEXT, button -> this.confirmDungeonRegeneration()).dimensions(this.x + 7, this.y + 20, (this.backgroundWidth - 18) / 2, 20).build());
		this.cancelDungeonRegenerationButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_REGENERATION_BUTTON_LABEL_TEXT, button -> this.cancelDungeonRegeneration()).dimensions(this.x + this.backgroundWidth / 2 + 2, this.y + 20, (this.backgroundWidth - 18) / 2, 20).build());

		this.teleportButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable(this.teleporterBlock.getTeleportButtonLabel()), button -> this.teleport()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, 100, 20).build());
		this.cancelTeleportButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable(this.teleporterBlock.getCancelTeleportButtonLabel()), button -> this.cancelTeleport()).dimensions(this.x + this.backgroundWidth - 107, this.y + this.backgroundHeight - 27, 100, 20).build());

		this.setInitialFocus(this.teleportButton);
		this.updateWidgets();
	}

	@Override
	public void handledScreenTick() {
		super.handledScreenTick();
		if (this.client != null) {
			if (this.client.interactionManager != null && this.handler.onButtonClick(this.client.player, -1)) {
				this.client.interactionManager.clickButton(this.handler.syncId, -1);
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
		World world = this.teleporterBlock.getWorld();
		if (world != null) {
			Optional<RegistryEntry.Reference<Location>> optionalLocationReference = world.getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(this.currentTarget.identifier()));
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

			Inventory inventory = new SimpleInventory(36);
			for (int k = 0; k < 36; k++) {
				ItemStack itemStack = this.handler.getPlayerInventory().getStack(k);
				inventory.setStack(k, itemStack.copy());
			}
			this.areCurrentLocationKeysPresent = true;
				for (CommonDataStructures.ItemCost itemCost : LocationUtils.getKeyForEntrance(location, this.currentTarget.entrance())) {
					ItemStack currentKeyItemStack = itemCost.itemStack();
					int keyCount = currentKeyItemStack.getCount();
					for (int j = 0; j < inventory.size(); j++) {
						ItemStack itemStack = inventory.getStack(j);
						if (ItemStack.areItemsAndComponentsEqual(currentKeyItemStack, itemStack)) {
							int stackCount = inventory.getStack(j).getCount();
							if (stackCount >= keyCount) {
								inventory.removeStack(j, keyCount);
								keyCount = 0;
								break;
							} else {
								inventory.setStack(j, ItemStack.EMPTY);
								keyCount = keyCount - stackCount;
							}
						}
					}
					if (keyCount > 0) {
						this.areCurrentLocationKeysPresent = false;
					}
			}
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.currentTargetDisplayName;
		String string1 = this.currentTargetEntranceDisplayName;
		VisibleLocation var = new VisibleLocation(this.currentTarget);
		List<PlayerListEntry> list = new ArrayList<>(this.partyMemberList);
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
			i = this.x + 8;
			j = this.y + 45;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 114)) {
				this.partyMemberListMouseClicked = true;
			}
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4) {
			i = this.x + 8;
			j = this.y + 21;
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
			this.partyMemberListScrollAmount = MathHelper.clamp(this.partyMemberListScrollAmount + f, 0.0f, 1.0f);
			this.partyMemberListScrollPosition = (int) ((double) (this.partyMemberListScrollAmount * (float) i));
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4
				&& this.visibleLocationsListMouseClicked) {
			int i = this.handler.getVisibleLocationsList().size() - 4;
			float f = (float) deltaY / (float) i;
			this.visibleLocationsListScrollAmount = MathHelper.clamp(this.visibleLocationsListScrollAmount + f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.showChooseTargetOwnerScreen
				&& this.partyMemberList.size() > 5
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= this.y + 44 && mouseY <= this.y + 116) {
			int i = this.partyMemberList.size() - 5;
			float f = (float) verticalAmount / (float) i;
			this.partyMemberListScrollAmount = MathHelper.clamp(this.partyMemberListScrollAmount - f, 0.0f, 1.0f);
			this.partyMemberListScrollPosition = (int) ((double) (this.partyMemberListScrollAmount * (float) i));
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.handler.getVisibleLocationsList().size() > 4
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= this.y + 20 && mouseY <= this.y + 92) {
			int i = this.handler.getVisibleLocationsList().size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.visibleLocationsListScrollAmount = MathHelper.clamp(this.visibleLocationsListScrollAmount - f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

		int x = this.x;
		int y = this.y;
		TeleporterBlockEntity.TeleportationMode mode = this.teleporterBlock.getTeleportationMode();

		if (this.showChooseTargetOwnerScreen) {
			context.drawTexture(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

			context.drawTexture(this.currentPlayer.getSkinTextures().texture(), x + 43/*7*/, y + 26, 8, 8, 8, 8, 8, 8, 64, 64);
			context.drawText(this.textRenderer, this.currentPlayer.getProfile().getName(), x + 55/*19*/, y + 26, 0x404040, false);

			for (int i = this.partyMemberListScrollPosition; i < Math.min(this.partyMemberListScrollPosition + 4, this.partyMemberList.size()); i++) {

				PlayerListEntry playerListEntry = this.partyMemberList.get(i);
				context.drawTexture(playerListEntry.getSkinTextures().texture(), x + 43/*7*/, y + 50 + ((i - this.partyMemberListScrollPosition) * 24), 8, 8, 8, 8, 8, 8, 64, 64);
				context.drawText(this.textRenderer, playerListEntry.getProfile().getName(), x + 55/*19*/, y + 50 + ((i - this.partyMemberListScrollPosition) * 24), 0x404040, false);
			}
			if (this.partyMemberList.size() > 5) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_95_TEXTURE, x + 7, y + 44, 8, 116);
				int k = (int) (105.0f * this.partyMemberListScrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, x + 8, y + 44 + 1 + k, 6, 7);
			}
		} else if (this.showChooseTargetIdentifierScreen) {

			context.drawTexture(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
			VisibleLocation visibleLocation;
			for (int i = this.visibleLocationsListScrollPosition; i < Math.min(this.visibleLocationsListScrollPosition + 4, this.handler.getVisibleLocationsList().size()); i++) {

				visibleLocation = this.handler.getVisibleLocationsList().get(i);
				if (!visibleLocation.entrance().isEmpty() && !visibleLocation.entranceDisplayName().isEmpty()) {
					if (visibleLocation.isEntranceNameShownFirst()) {
						context.drawText(this.textRenderer, Text.translatable(visibleLocation.entranceDisplayName()), x + 43, y + 19, 0x404040, false);
						context.drawText(this.textRenderer, Text.translatable(visibleLocation.displayName()), x + 43, y + 32, 0x404040, false);
					} else {
						context.drawText(this.textRenderer, Text.translatable(visibleLocation.displayName()), x + 43, y + 19, 0x404040, false);
						context.drawText(this.textRenderer, Text.translatable(visibleLocation.entranceDisplayName()), x + 43, y + 32, 0x404040, false);
					}
				} else {
					context.drawText(this.textRenderer, Text.translatable(visibleLocation.displayName()), x + 43, y + 26 + ((i - this.visibleLocationsListScrollPosition) * 24), 0x404040, false);
				}
			}
			if (this.handler.getVisibleLocationsList().size() > 4) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_95_TEXTURE, x + 7, y + 20, 8, 92);
				int k = (int) (83.0f * this.visibleLocationsListScrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, x + 8, y + 20 + 1 + k, 6, 7);
			}

		} else if (this.showRegenerationConfirmScreen) {
			context.drawTexture(ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

			int regenerationTextOffset = this.backgroundWidth / 2 - this.textRenderer.getWidth(REGENERATE_DUNGEON_TEXT) / 2;

			context.drawText(this.textRenderer, REGENERATE_DUNGEON_TEXT, x + regenerationTextOffset, y + 7, 0x404040, false);
		} else if (this.showAdventureScreen) {

			if (mode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

				context.drawTexture(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

				Text teleporterName = Text.translatable(this.teleporterBlock.getTeleporterName());
				int teleporterNameOffset = this.backgroundWidth / 2 - this.textRenderer.getWidth(teleporterName) / 2;

				context.drawText(this.textRenderer, teleporterName, x + teleporterNameOffset, y + 7, 0x404040, false);

				if (!this.currentTargetDisplayName.isEmpty() || this.handler.getVisibleLocationsList().size() > 1) {
					if (!this.currentTarget.entrance().isEmpty() && !this.currentTargetEntranceDisplayName.isEmpty()) {
						if (this.showCurrentEntranceDisplayNameFirst) {
							context.drawText(this.textRenderer, Text.translatable(this.currentTargetEntranceDisplayName), x + 31, y + 20, 0x404040, false);
							context.drawText(this.textRenderer, Text.translatable(this.currentTargetDisplayName), x + 31, y + 33, 0x404040, false);
						} else {
							context.drawText(this.textRenderer, Text.translatable(this.currentTargetDisplayName), x + 31, y + 20, 0x404040, false);
							context.drawText(this.textRenderer, Text.translatable(this.currentTargetEntranceDisplayName), x + 31, y + 33, 0x404040, false);
						}

					} else {

						context.drawText(this.textRenderer, Text.translatable(this.currentTargetDisplayName), x + 31, y + 27, 0x404040, false);

					}
				}
				if (this.handler.getCurrentLocationCooldown() > 0) {
					this.teleportButton.setMessage(Text.translatable("gui.teleporter_block.teleport_button.cooldown_label", LocationUtils.getCooldownText(this.handler.getCurrentLocationCooldown())));
				} else {
					this.teleportButton.setMessage(Text.translatable(this.teleporterBlock.getTeleportButtonLabel()));
				}

				this.teleportButton.active = this.isCurrentLocationUnlocked && this.areCurrentLocationKeysPresent && this.handler.getCurrentLocationCooldown() <= 0;

				if (this.isCurrentLocationPublic && this.showCurrentLocationOwner) {
					context.drawText(this.textRenderer, LOCATION_IS_PUBLIC_TEXT, x + 19, y + 77, 0x404040, false);
				} else if (this.currentTargetOwner != null && this.showCurrentLocationOwner) {
					context.drawText(this.textRenderer, Text.translatable(this.teleporterBlock.getCurrentTargetOwnerLabel()), x + 8, y + 58, 0x404040, false);

					if (this.canOwnerBeChosen && !this.isCurrentLocationPublic) {
						context.drawTexture(currentTargetOwner.getSkinTextures().texture(), x + 31, y + 77, 8, 8, 8, 8, 8, 8, 64, 64);
						context.drawText(this.textRenderer, currentTargetOwner.getProfile().getName(), x + 43, y + 77, 0x404040, false);
					} else {
						context.drawTexture(currentTargetOwner.getSkinTextures().texture(), x + 7, y + 77, 8, 8, 8, 8, 8, 8, 64, 64);
						context.drawText(this.textRenderer, currentTargetOwner.getProfile().getName(), x + 19, y + 77, 0x404040, false);
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
				context.drawTexture(ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

				Text teleporterName = Text.translatable(this.teleporterBlock.getTeleporterName());
				int teleporterNameOffset = this.backgroundWidth / 2 - this.textRenderer.getWidth(teleporterName) / 2;

				context.drawText(this.textRenderer, teleporterName, x + teleporterNameOffset, y + 7, 0x404040, false);
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

	private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY) {
		Optional<Text> optional = Optional.empty();
		if (this.focusedSlot != null) {
			ItemStack itemStack = this.handler.getSlot(37).getStack();
			if (itemStack.isEmpty()) {
				if (this.focusedSlot.id == 37) {
					optional = Optional.of(Text.translatable("gui.teleporter_block.requiredItemStackSlot.tooltip"));
				}
			}
		}
		optional.ifPresent(text -> context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines((StringVisitable) text, 115), mouseX, mouseY));
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
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {

	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
		if (property == 0 && value <= 0) {
			this.calculateUnlockedAndVisibleLocations();
		}
	}
}
