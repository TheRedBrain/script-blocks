package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.network.DuckClientAdvancementManagerMixin;
import com.github.theredbrain.scriptblocks.network.packet.AddStatusEffectPacket;
import com.github.theredbrain.scriptblocks.network.packet.SetManualResetLocationControlBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.TeleportFromTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.registry.LocationsRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.github.theredbrain.slotcustomizationapi.api.SlotCustomization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class TeleporterBlockScreen extends HandledScreen<TeleporterBlockScreenHandler> {
	private static final Text EDIT_BUTTON_LABEL_TEXT = Text.translatable("gui.edit");
	private static final Text CANCEL_BUTTON_LABEL_TEXT = Text.translatable("gui.cancel");
	private static final Text CHOOSE_BUTTON_LABEL_TEXT = Text.translatable("gui.choose");
	private static final Text KEY_ITEM_IS_CONSUMED_TEXT = Text.translatable("gui.teleporter_block.key_item_is_consumed");
	private static final Text KEY_ITEM_IS_REQUIRED_TEXT = Text.translatable("gui.teleporter_block.key_item_is_required");
	private static final Text LOCATION_IS_PUBLIC_TEXT = Text.translatable("gui.teleporter_block.location_is_public");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_95_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_95");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final Identifier ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_screen.png");
	public static final Identifier ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE = ScriptBlocks.identifier("textures/gui/container/teleporter_block/adventure_teleporter_locations_screen.png");
	private final TeleporterBlockScreenHandler handler;
	private TeleporterBlockEntity teleporterBlock;

	private PlayerListEntry currentTargetOwner;
	private ButtonWidget openChooseTargetOwnerScreenButton;
	private ButtonWidget confirmChoosePublicButton;
	private ButtonWidget confirmChooseCurrentPlayerButton;
	private ButtonWidget confirmChooseTeamMember0Button;
	private ButtonWidget confirmChooseTeamMember1Button;
	private ButtonWidget confirmChooseTeamMember2Button;
	private ButtonWidget confirmChooseTeamMember3Button;
	private ButtonWidget cancelChooseTargetOwnerButton;
	private String currentTargetIdentifier;
	private String currentTargetDisplayName;
	private ButtonWidget openChooseTargetIdentifierScreenButton;
	private ButtonWidget confirmChooseTargetIdentifier0Button;
	private ButtonWidget confirmChooseTargetIdentifier1Button;
	private ButtonWidget confirmChooseTargetIdentifier2Button;
	private ButtonWidget confirmChooseTargetIdentifier3Button;
	private ButtonWidget cancelChooseTargetIdentifierButton;
	private String currentTargetEntrance;
	private String currentTargetEntranceDisplayName;
	private String currentTargetEntranceDataId;
	private int currentTargetEntranceData;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelTeleportButton;
	private ButtonWidget openDungeonRegenerationScreenButton;
	private ButtonWidget confirmDungeonRegenerationButton;
	private ButtonWidget cancelDungeonRegenerationButton;

	private boolean showChooseTargetOwnerScreen;
	private boolean showChooseTargetIdentifierScreen;
	private boolean showRegenerationConfirmScreen;
	private boolean canOwnerBeChosen;
	private boolean showAdventureScreen;
	private boolean showRegenerateButton;

	private TeleporterBlockEntity.TeleportationMode teleportationMode;

	List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList = new ArrayList<>();
	List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> visibleLocationsList = new ArrayList<>();
	List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> unlockedLocationsList = new ArrayList<>();
	List<PlayerListEntry> partyMemberList = new ArrayList<>();
	private int teamListScrollPosition = 0;
	private int visibleLocationsListScrollPosition = 0;
	private float teamListScrollAmount = 0.0f;
	private float visibleLocationsListScrollAmount = 0.0f;
	private boolean teamListMouseClicked = false;
	private boolean visibleLocationsListMouseClicked = false;

	private boolean isTeleportButtonActive = true;
	private boolean canLocationBeRegenerated = false;
	private boolean isRegenerateButtonActive = true;
	private boolean showCurrentLockAdvancement;
	private boolean showCurrentUnlockAdvancement;
	private boolean isCurrentLocationUnlocked;
	@Nullable
	private Advancement currentLockAdvancement;
	@Nullable
	private Advancement currentUnlockAdvancement;
	private boolean isCurrentLocationPublic;
	private boolean showCurrentLocationName;
	private boolean showCurrentLocationOwner;
	private boolean consumeKeyItem = false;
	private ItemStack currentKeyItemStack;

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
		if (index == -2) {
			this.currentTargetOwner = null;
		} else if (index == -1 && this.client != null && this.client.player != null) {
			this.currentTargetOwner = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
			this.canLocationBeRegenerated = true;
		} else {
			this.currentTargetOwner = this.partyMemberList.get(this.teamListScrollPosition + index);
		}
		this.showChooseTargetOwnerScreen = false;
		this.calculateUnlockedAndVisibleLocations(false);
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
		this.currentTargetIdentifier = this.visibleLocationsList.get(this.visibleLocationsListScrollPosition + index).getLeft().getLeft();
		this.currentTargetEntrance = this.visibleLocationsList.get(this.visibleLocationsListScrollPosition + index).getLeft().getRight();
		this.showChooseTargetIdentifierScreen = false;
		this.calculateUnlockedAndVisibleLocations(false);
		this.updateWidgets();
	}

	private void cancelChooseTargetLocation() {
		this.showChooseTargetIdentifierScreen = false;
		this.updateWidgets();
	}

	private void openDungeonRegenerationConfirmScreen() {
		this.showRegenerationConfirmScreen = true;
		this.updateWidgets();
	}

	private void confirmDungeonRegeneration() {
		if (this.tryDungeonRegeneration()) {
			this.showRegenerationConfirmScreen = false;
			this.updateWidgets();
		}
	}

	private void cancelDungeonRegeneration() {
		this.showRegenerationConfirmScreen = false;
		this.updateWidgets();
	}

	@Override
	public void close() {
		this.givePortalResistanceEffect();
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
				this.currentTargetOwner = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
				this.canLocationBeRegenerated = true;
			}
		}
		this.locationsList.clear();
		this.locationsList.addAll(this.teleporterBlock.getLocationsList());
		this.showAdventureScreen = this.teleporterBlock.getShowAdventureScreen();
		this.teleportationMode = this.teleporterBlock.getTeleportationMode();
		this.showRegenerateButton = this.teleporterBlock.showRegenerateButton();
		this.currentTargetIdentifier = "";
		this.currentTargetDisplayName = "";
		this.currentTargetEntrance = "";
		this.currentTargetEntranceDisplayName = "";
		this.currentTargetEntranceDataId = "";
		this.currentTargetEntranceData = 0;
		if ((this.teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT || this.teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) && !this.showAdventureScreen) {
			this.teleport();
		}
		this.backgroundWidth = 218;
		if (this.teleporterBlock.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
			this.backgroundHeight = 171;//147;
			this.calculateUnlockedAndVisibleLocations(true);
			if (!this.showAdventureScreen) {
				this.teleport();
			}
		} else {
			this.backgroundHeight = 47;
		}
		super.init();

		this.openChooseTargetIdentifierScreenButton = this.addDrawableChild(ButtonWidget.builder(EDIT_BUTTON_LABEL_TEXT, button -> this.openChooseTargetIdentifierScreen()).dimensions(this.x + this.backgroundWidth - 57, this.y + 21, 50, 20).build());
		this.confirmChooseTargetIdentifier0Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetIdentifier(0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 20, 50, 20).build());
		this.confirmChooseTargetIdentifier1Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetIdentifier(1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 44, 50, 20).build());
		this.confirmChooseTargetIdentifier2Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetIdentifier(2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 68, 50, 20).build());
		this.confirmChooseTargetIdentifier3Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetIdentifier(3)).dimensions(this.x + this.backgroundWidth - 57, this.y + 92, 50, 20).build());
		this.cancelChooseTargetIdentifierButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseTargetLocation()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		this.openChooseTargetOwnerScreenButton = this.addDrawableChild(ButtonWidget.builder(EDIT_BUTTON_LABEL_TEXT, button -> this.openChooseCurrentTargetOwnerScreen()).dimensions(this.x + this.backgroundWidth - 57, this.y + 71, 50, 20).build());
		this.confirmChoosePublicButton = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(-2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 20, 50, 20).build());
		this.confirmChooseCurrentPlayerButton = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(-1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 44, 50, 20).build());
		this.confirmChooseTeamMember0Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(0)).dimensions(this.x + this.backgroundWidth - 57, this.y + 68, 50, 20).build());
		this.confirmChooseTeamMember1Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(1)).dimensions(this.x + this.backgroundWidth - 57, this.y + 92, 50, 20).build());
		this.confirmChooseTeamMember2Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(2)).dimensions(this.x + this.backgroundWidth - 57, this.y + 116, 50, 20).build());
		this.confirmChooseTeamMember3Button = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.chooseTargetOwner(3)).dimensions(this.x + this.backgroundWidth - 57, this.y + 140, 50, 20).build());
		this.cancelChooseTargetOwnerButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelChooseCurrentTargetOwner()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, this.backgroundWidth - 14, 20).build());

		this.openDungeonRegenerationScreenButton = this.addDrawableChild(ButtonWidget.builder(EDIT_BUTTON_LABEL_TEXT, button -> this.openDungeonRegenerationConfirmScreen()).dimensions(this.x + 7, this.y + this.backgroundHeight - 51, this.backgroundWidth - 14, 20).build());
		this.confirmDungeonRegenerationButton = this.addDrawableChild(ButtonWidget.builder(CHOOSE_BUTTON_LABEL_TEXT, button -> this.confirmDungeonRegeneration()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, (this.backgroundWidth - 18) / 2, 20).build());
		this.cancelDungeonRegenerationButton = this.addDrawableChild(ButtonWidget.builder(CANCEL_BUTTON_LABEL_TEXT, button -> this.cancelDungeonRegeneration()).dimensions(this.x + this.backgroundWidth / 2 + 2, this.y + this.backgroundHeight - 27, (this.backgroundWidth - 18) / 2, 20).build());

		this.teleportButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable(this.teleporterBlock.getTeleportButtonLabel()), button -> this.teleport()).dimensions(this.x + 7, this.y + this.backgroundHeight - 27, 100, 20).build());
		this.cancelTeleportButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable(this.teleporterBlock.getCancelTeleportButtonLabel()), button -> this.cancelTeleport()).dimensions(this.x + this.backgroundWidth - 107, this.y + this.backgroundHeight - 27, 100, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.teleportButton);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

		this.openChooseTargetIdentifierScreenButton.visible = false;
		this.confirmChooseTargetIdentifier0Button.visible = false;
		this.confirmChooseTargetIdentifier1Button.visible = false;
		this.confirmChooseTargetIdentifier2Button.visible = false;
		this.confirmChooseTargetIdentifier3Button.visible = false;
		this.cancelChooseTargetIdentifierButton.visible = false;

		this.openChooseTargetOwnerScreenButton.visible = false;
		this.confirmChoosePublicButton.visible = false;
		this.confirmChooseCurrentPlayerButton.visible = false;
		this.confirmChooseTeamMember0Button.visible = false;
		this.confirmChooseTeamMember1Button.visible = false;
		this.confirmChooseTeamMember2Button.visible = false;
		this.confirmChooseTeamMember3Button.visible = false;
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
			for (int i = 0; i < Math.min(4, this.visibleLocationsList.size()); i++) {
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

			if (this.isCurrentLocationPublic) {
				this.confirmChooseCurrentPlayerButton.setY(this.y + 44);
				this.confirmChooseTeamMember0Button.setY(this.y + 68);
				this.confirmChooseTeamMember1Button.setY(this.y + 92);
				this.confirmChooseTeamMember2Button.setY(this.y + 116);
				this.confirmChooseTeamMember3Button.setY(this.y + 140);
				this.confirmChoosePublicButton.visible = true;
			} else {
				this.confirmChooseCurrentPlayerButton.setY(this.y + 20);
				this.confirmChooseTeamMember0Button.setY(this.y + 44);
				this.confirmChooseTeamMember1Button.setY(this.y + 68);
				this.confirmChooseTeamMember2Button.setY(this.y + 92);
				this.confirmChooseTeamMember3Button.setY(this.y + 116);
			}
			if (!this.isCurrentLocationPublic) {
				this.confirmChooseCurrentPlayerButton.visible = true;

				int index = 0;
				for (int i = 0; i < Math.min(4, this.partyMemberList.size()); i++) {
					if (index == 0) {
						this.confirmChooseTeamMember0Button.visible = true;
					} else if (index == 1) {
						this.confirmChooseTeamMember1Button.visible = true;
					} else if (index == 2) {
						this.confirmChooseTeamMember2Button.visible = true;
					} else if (index == 3) {
						this.confirmChooseTeamMember3Button.visible = true;
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

				this.openChooseTargetIdentifierScreenButton.visible = this.visibleLocationsList.size() > 1;

				this.openChooseTargetOwnerScreenButton.visible = this.canOwnerBeChosen && !this.isCurrentLocationPublic;

				this.openDungeonRegenerationScreenButton.visible = this.showRegenerateButton;
				this.openDungeonRegenerationScreenButton.active = this.isRegenerateButtonActive;
			}

			this.teleportButton.visible = true;
			this.cancelTeleportButton.visible = true;

			this.teleportButton.active = this.isTeleportButtonActive;
		}
	}

	private void calculateUnlockedAndVisibleLocations(boolean shouldInit) {

		ClientAdvancementManager advancementHandler = null;
		Identifier lockAdvancementIdentifier;
		Identifier unlockAdvancementIdentifier;
		boolean showLockedLocation;

		if (this.client != null && this.client.player != null) {
			advancementHandler = this.client.player.networkHandler.getAdvancementHandler();
		}

		if (shouldInit) {
			if (this.teleporterBlock.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				this.unlockedLocationsList.clear();
				this.visibleLocationsList.clear();
				for (MutablePair<MutablePair<String, String>, MutablePair<String, Integer>> entry : this.locationsList) {
					Location location = LocationsRegistry.registeredLocations.get(Identifier.of(entry.getLeft().getLeft()));
					String entrance = entry.getLeft().getRight();
					lockAdvancementIdentifier = LocationUtils.lockAdvancementForEntrance(location, entrance);
					unlockAdvancementIdentifier = LocationUtils.unlockAdvancementForEntrance(location, entrance);
					showLockedLocation = LocationUtils.showLockedLocationForEntrance(location, entrance);

					if (advancementHandler != null) {
						AdvancementEntry lockAdvancementEntry = null;
						if (lockAdvancementIdentifier != null) {
							lockAdvancementEntry = advancementHandler.get(lockAdvancementIdentifier);
						}
						AdvancementEntry unlockAdvancementEntry = null;
						if (unlockAdvancementIdentifier != null) {
							unlockAdvancementEntry = advancementHandler.get(unlockAdvancementIdentifier);
						}
						if ((lockAdvancementIdentifier == null || (lockAdvancementEntry != null && !((DuckClientAdvancementManagerMixin) advancementHandler).scriptblocks$getAdvancementProgress(lockAdvancementEntry.value()).isDone())) && (unlockAdvancementIdentifier == null || (unlockAdvancementEntry != null && ((DuckClientAdvancementManagerMixin) advancementHandler).scriptblocks$getAdvancementProgress(unlockAdvancementEntry.value()).isDone()))) {
							this.unlockedLocationsList.add(entry);
							this.visibleLocationsList.add(entry);
						} else if (showLockedLocation) {
							this.visibleLocationsList.add(entry);
						}
					}
				}
				if (!this.visibleLocationsList.isEmpty()) {
					this.currentTargetIdentifier = this.visibleLocationsList.getFirst().getLeft().getLeft();
					this.currentTargetEntrance = this.visibleLocationsList.getFirst().getLeft().getRight();
				}
			}
		}

		for (MutablePair<MutablePair<String, String>, MutablePair<String, Integer>> dungeonLocation : this.unlockedLocationsList) {
			if (Objects.equals(dungeonLocation.getLeft().getLeft(), this.currentTargetIdentifier)) {
				this.isCurrentLocationUnlocked = true;
			}
		}

		Location location = LocationsRegistry.registeredLocations.get(Identifier.tryParse(this.currentTargetIdentifier));
		if (location != null) {
			lockAdvancementIdentifier = LocationUtils.lockAdvancementForEntrance(location, this.currentTargetEntrance);
			unlockAdvancementIdentifier = LocationUtils.unlockAdvancementForEntrance(location, this.currentTargetEntrance);
			this.showCurrentLockAdvancement = LocationUtils.showLockAdvancementForEntrance(location, this.currentTargetEntrance);
			this.showCurrentUnlockAdvancement = LocationUtils.showUnlockAdvancementForEntrance(location, this.currentTargetEntrance);
			this.currentTargetDisplayName = location.displayName();
			this.currentTargetEntranceDisplayName = LocationUtils.getEntranceDisplayName(location, this.currentTargetEntrance);
			this.isCurrentLocationPublic = location.isPublic();
			this.consumeKeyItem = LocationUtils.consumeKeyAtEntrance(location, this.currentTargetEntrance);
			this.currentKeyItemStack = LocationUtils.getKeyForEntrance(location, this.currentTargetEntrance);
			this.canOwnerBeChosen = location.canOwnerBeChosen();

			this.showCurrentLocationName = LocationUtils.showLocationNameForEntrance(location, this.currentTargetEntrance);
			this.showCurrentLocationOwner = LocationUtils.showLocationOwnerForEntrance(location, this.currentTargetEntrance);
			if (advancementHandler != null) {
				AdvancementEntry lockAdvancementEntry = null;
				if (lockAdvancementIdentifier != null) {
					lockAdvancementEntry = advancementHandler.get(lockAdvancementIdentifier);
				}
				AdvancementEntry unlockAdvancementEntry = null;
				if (unlockAdvancementIdentifier != null) {
					unlockAdvancementEntry = advancementHandler.get(unlockAdvancementIdentifier);
//					unlockAdvancementEntry = advancementHandler.getManager().get(Identifier.tryParse(unlockAdvancementIdentifier);
				}
				if (lockAdvancementEntry != null) {
					this.currentLockAdvancement = lockAdvancementEntry.value();
				}
				if (unlockAdvancementEntry != null) {
					this.currentUnlockAdvancement = unlockAdvancementEntry.value();
				}

				Inventory inventory = new SimpleInventory(36);
				for (int k = 0; k < 36; k++) {
					ItemStack itemStack = this.handler.getPlayerInventory().getStack(k);
					inventory.setStack(k, itemStack.copy());
				}
				boolean bl = true;
				if (this.currentKeyItemStack != null) {
					ItemStack currentKeyItemStack = this.currentKeyItemStack;
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
						bl = false;
					}
				}

				this.isRegenerateButtonActive = this.isCurrentLocationUnlocked && this.canLocationBeRegenerated && this.currentTargetOwner != null;

				this.isTeleportButtonActive = this.isCurrentLocationUnlocked && bl;
			}
		}
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.currentTargetIdentifier;
		String string1 = this.currentTargetDisplayName;
		String string2 = this.currentTargetEntrance;
		String string3 = this.currentTargetEntranceDisplayName;
		String string4 = this.currentTargetEntranceDataId;
		int number = this.currentTargetEntranceData = 0;
		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> list = new ArrayList<>(this.locationsList);
		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> list1 = new ArrayList<>(this.visibleLocationsList);
		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> list2 = new ArrayList<>(this.unlockedLocationsList);
		List<PlayerListEntry> list3 = new ArrayList<>(this.partyMemberList);
		this.init(client, width, height);
		this.currentTargetIdentifier = string;
		this.currentTargetDisplayName = string1;
		this.currentTargetEntrance = string2;
		this.currentTargetEntranceDisplayName = string3;
		this.currentTargetEntranceDataId = string4;
		this.currentTargetEntranceData = number;
		this.locationsList.clear();
		this.visibleLocationsList.clear();
		this.unlockedLocationsList.clear();
		this.partyMemberList.clear();
		this.locationsList.addAll(list);
		this.visibleLocationsList.addAll(list1);
		this.unlockedLocationsList.addAll(list2);
		this.partyMemberList.addAll(list3);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.teamListMouseClicked = false;
		this.visibleLocationsListMouseClicked = false;
		int i;
		int j;
		// TODO team list
		if (this.showChooseTargetOwnerScreen) {
			i = this.x - 13;
			j = this.y + 134;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 30)) {
				this.teamListMouseClicked = true;
			}
		}
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.visibleLocationsList.size() > 4) {
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
		// TODO team list
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.visibleLocationsList.size() > 4
				&& this.visibleLocationsListMouseClicked) {
			int i = this.visibleLocationsList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.visibleLocationsListScrollAmount = MathHelper.clamp(this.visibleLocationsListScrollAmount + f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		// TODO team list
		if (this.showChooseTargetIdentifierScreen
				&& this.teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS
				&& this.visibleLocationsList.size() > 4
				&& mouseX >= this.x + 7 && mouseX <= this.x + this.backgroundWidth - 61 && mouseY >= this.y + 20 && mouseY <= this.y + 112) {
			int i = this.visibleLocationsList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.visibleLocationsListScrollAmount = MathHelper.clamp(this.visibleLocationsListScrollAmount - f, 0.0f, 1.0f);
			this.visibleLocationsListScrollPosition = (int) ((double) (this.visibleLocationsListScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		TeleporterBlockEntity.TeleportationMode mode = this.teleporterBlock.getTeleportationMode();

		if (this.showChooseTargetIdentifierScreen) {

			for (int i = this.visibleLocationsListScrollPosition; i < Math.min(this.visibleLocationsListScrollPosition + 4, this.visibleLocationsList.size()); i++) {
				context.drawText(this.textRenderer, this.visibleLocationsList.get(i).getLeft().getLeft(), this.x + 19, this.y + 26 + ((i - this.visibleLocationsListScrollPosition) * 24), 0x404040, false);
			}
			if (this.visibleLocationsList.size() > 4) {
				context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_95_TEXTURE, this.x + 7, this.y + 20, 8, 92);
				int k = (int) (83.0f * this.visibleLocationsListScrollAmount);
				context.drawGuiTexture(SCROLLER_TEXTURE, this.x + 8, this.y + 20 + 1 + k, 6, 7);
			}

		} else if (this.showChooseTargetOwnerScreen) {
			if (this.isCurrentLocationPublic) {
				// TODO
			} else {
				// TODO
			}
		} else if (this.showRegenerationConfirmScreen) {
			// TODO
		} else if (this.showAdventureScreen) {

			Text teleporterName = Text.translatable(this.teleporterBlock.getTeleporterName());
			int teleporterNameOffset = this.backgroundWidth / 2 - this.textRenderer.getWidth(teleporterName) / 2;
//                if (this.currentTargetOwner != null) {

			context.drawText(this.textRenderer, teleporterName, this.x + teleporterNameOffset, this.y + 7, 0x404040, false);

			if (mode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {

//                        context.drawText(this.textRenderer, Text.translatable(this.teleporterBlock.getCurrentTargetIdentifierLabel()), this.x + 8, this.y + 20, 0x404040, false);

				if (this.showCurrentLocationName || this.visibleLocationsList.size() > 1) {
					if (!this.currentTargetEntrance.isEmpty() && !this.currentTargetEntranceDisplayName.isEmpty()) {

						context.drawText(this.textRenderer, Text.translatable(this.currentTargetEntranceDisplayName), this.x + 8, this.y + 20, 0x404040, false);
						context.drawText(this.textRenderer, Text.translatable(this.currentTargetDisplayName), this.x + 8, this.y + 33, 0x404040, false);

					} else {

						context.drawText(this.textRenderer, Text.translatable(this.currentTargetDisplayName), this.x + 8, this.y + 27, 0x404040, false);

					}
				}

				if (this.isCurrentLocationPublic && this.showCurrentLocationOwner) {
					context.drawText(this.textRenderer, LOCATION_IS_PUBLIC_TEXT, this.x + 19, this.y + 77, 0x404040, false);
				} else if (this.currentTargetOwner != null && this.showCurrentLocationOwner) {
					context.drawText(this.textRenderer, Text.translatable(this.teleporterBlock.getCurrentTargetOwnerLabel()), this.x + 8, this.y + 58, 0x404040, false);

					context.drawTexture(currentTargetOwner.getSkinTextures().texture(), this.x + 7, this.y + 77, 8, 8, 8, 8, 8, 8, 64, 64);
					context.drawText(this.textRenderer, currentTargetOwner.getProfile().getName(), this.x + 19, this.y + 77, 0x404040, false);
				}

				if (this.currentKeyItemStack != null) {
					ItemStack currentKey = this.currentKeyItemStack;
//                            ScriptBlocksMod.info("should draw item: " + currentKey.toString());
					int x = this.x + 8;
					int y = this.y + 95;
					int k = x + y * this.backgroundWidth;
					context.drawItemWithoutEntity(currentKey, x, y/*, k*/);
					context.drawItemInSlot(this.textRenderer, currentKey, x, y);
					context.drawText(this.textRenderer, this.consumeKeyItem ? KEY_ITEM_IS_CONSUMED_TEXT : KEY_ITEM_IS_REQUIRED_TEXT, x + 18, y + 5, 0x404040, false);
				}
			}
//                }

		}
//        context.drawTextWithShadow(this.textRenderer, CONSUME_KEY_ITEMSTACK_LABEL_TEXT, this.width / 2 - 153, 221, 0x404040);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		if (this.showAdventureScreen) {
			int i = this.x;
			int j = this.y;
			if (this.teleporterBlock.getTeleportationMode() == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				context.drawTexture(ADVENTURE_TELEPORTER_LOCATIONS_SCREEN_BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
			} else {
				context.drawTexture(ADVENTURE_TELEPORTER_SCREEN_BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);
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

	private boolean tryDungeonRegeneration() {
		if (this.canLocationBeRegenerated) {
			Location location = LocationsRegistry.registeredLocations.get(Identifier.tryParse(this.currentTargetIdentifier));
			if (location != null) {
				ClientPlayNetworking.send(new SetManualResetLocationControlBlockPacket(
						location.controlBlockPos(),
						true
				));
				return true;
			}
		}
		return false;
	}

	private void teleport() {
		String currentWorld = "";
		String currentTargetOwnerName = "";
		if (this.teleporterBlock.getWorld() != null) {
			currentWorld = this.teleporterBlock.getWorld().getRegistryKey().getValue().toString();
		}
		if (this.currentTargetOwner != null) {
			currentTargetOwnerName = this.currentTargetOwner.getProfile().getName();
		}
		if (this.isCurrentLocationPublic) {
			currentTargetOwnerName = "";
		}
		ClientPlayNetworking.send(new TeleportFromTeleporterBlockPacket(
				this.teleporterBlock.getPos(),
				currentWorld,
				this.teleporterBlock.getAccessPositionOffset(),
				this.teleporterBlock.getSetAccessPosition(),
				this.teleporterBlock.teleportTeam(),
				this.teleporterBlock.getTeleportationMode().asString(),
				this.teleporterBlock.getDirectTeleportPositionOffset(),
				this.teleporterBlock.getDirectTeleportOrientationYaw(),
				this.teleporterBlock.getDirectTeleportOrientationPitch(),
				this.teleporterBlock.getSpawnPointType().asString(),
				currentTargetOwnerName,
				this.currentTargetIdentifier,
				this.currentTargetEntrance,
				this.teleporterBlock.getStatusEffectsToDecrementLevelOnTeleport(),
				this.currentTargetEntranceDataId,
				this.currentTargetEntranceData
		));
	}

	private void givePortalResistanceEffect() {
		ClientPlayNetworking.send(new AddStatusEffectPacket(
				Registries.STATUS_EFFECT.getId(StatusEffectsRegistry.PORTAL_RESISTANCE_EFFECT),
				40,
				0,
				false,
				false,
				false,
				false)
		);
	}
}
