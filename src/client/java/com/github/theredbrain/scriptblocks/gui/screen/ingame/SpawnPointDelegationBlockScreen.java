package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateSpawnPointDelegationBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelData;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class SpawnPointDelegationBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component ADD_NEW_DELEGATED_SPAWN_POINT_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Component NEW_DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT = Component.translatable("gui.spawn_point_delegation_block.spawn_point_position_offset");
	private static final Component NEW_DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT = Component.translatable("gui.spawn_point_delegation_block.spawn_point_orientation");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 4;
	private final SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity;
	private Button removeListEntryButton0;
	private Button removeListEntryButton1;
	private Button removeListEntryButton2;
	private Button removeListEntryButton3;
	private EditBox newDelegatedSpawnPointPositionOffsetXField;
	private EditBox newDelegatedSpawnPointPositionOffsetYField;
	private EditBox newDelegatedSpawnPointPositionOffsetZField;
	private EditBox newDelegatedSpawnPointOrientationYawField;
	private EditBox newDelegatedSpawnPointOrientationPitchField;
	private final List<LevelData.RespawnData> delegatedSpawnPoints = new ArrayList<>(List.of());
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public SpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.spawnPointDelegationBlockEntity = spawnPointDelegationBlockEntity;
	}

	private void addNewDelegatedSpawnPoint() {
		boolean bl = false;
		int x = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetXField.getValue());
		if (x < -48 || x > 48) {
			x = Math.max(-48, Math.min(x, 48));
			this.newDelegatedSpawnPointPositionOffsetXField.setValue(Integer.toString(x));
			bl = true;
		}
		int y = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetYField.getValue());
		if (y < -48 || y > 48) {
			y = Math.max(-48, Math.min(y, 48));
			this.newDelegatedSpawnPointPositionOffsetYField.setValue(Integer.toString(y));
			bl = true;
		}
		int z = ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetZField.getValue());
		if (z < -48 || z > 48) {
			z = Math.max(-48, Math.min(z, 48));
			this.newDelegatedSpawnPointPositionOffsetZField.setValue(Integer.toString(z));
			bl = true;
		}
		int yaw = ItemUtils.parseInt(this.newDelegatedSpawnPointOrientationYawField.getValue());
		if (yaw < -180 || yaw > 180) {
			yaw = Math.max(-180, Math.min(yaw, 180));
			this.newDelegatedSpawnPointOrientationYawField.setValue(Integer.toString(yaw));
			bl = true;
		}
		int pitch = ItemUtils.parseInt(this.newDelegatedSpawnPointOrientationPitchField.getValue());
		if (pitch < -90 || pitch > 90) {
			pitch = Math.max(-90, Math.min(pitch, 90));
			this.newDelegatedSpawnPointOrientationPitchField.setValue(Integer.toString(pitch));
			bl = true;
		}
		if (bl && this.minecraft.player != null) {
			this.minecraft.player.sendSystemMessage(Component.translatable("gui.spawn_point_delegation_block.invalid_entry_was_corrected"));
			return;
		}
		if (this.spawnPointDelegationBlockEntity.getLevel() == null) {
			return;
		}
		LevelData.RespawnData newDelegatedSpawnPoint = LevelData.RespawnData.of(
				this.spawnPointDelegationBlockEntity.getLevel().dimension(),
				new BlockPos(
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetXField.getValue()),
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetYField.getValue()),
						ItemUtils.parseInt(this.newDelegatedSpawnPointPositionOffsetZField.getValue())
				),
				ItemUtils.parseFloat(this.newDelegatedSpawnPointOrientationYawField.getValue()),
				ItemUtils.parseFloat(this.newDelegatedSpawnPointOrientationPitchField.getValue())
		);
		bl = false;
		for (LevelData.RespawnData delegatedSpawnPoint : this.delegatedSpawnPoints) {
			if (delegatedSpawnPoint.equals(newDelegatedSpawnPoint)) {
				bl = true;
				break;
			}
		}
		if (bl && this.minecraft.player != null) {
			this.minecraft.player.sendSystemMessage(Component.translatable("gui.spawn_point_delegation_block.spawn_point_already_in_list"));
		} else {
			this.delegatedSpawnPoints.add(newDelegatedSpawnPoint);
			this.scrollPosition = 0;
			this.scrollAmount = 0.0f;
			this.updateWidgets();
		}
	}

	private void removeDelegatedSpawnPoint(int index) {
		if (index + this.scrollPosition < this.delegatedSpawnPoints.size()) {
			this.delegatedSpawnPoints.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {

		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(this.spawnPointDelegationBlockEntity.getDelegatedSpawnPoints());

		this.removeListEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(0)));
		this.removeListEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(1)));
		this.removeListEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(2)));
		this.removeListEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeDelegatedSpawnPoint(3)));

		this.addRenderableWidget(Button.builder(ADD_NEW_DELEGATED_SPAWN_POINT_BUTTON_LABEL_TEXT, button -> this.addNewDelegatedSpawnPoint()).bounds(this.width / 2 - 154, 116, 300, 20).build());

		this.newDelegatedSpawnPointPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 151, 100, 20, Component.empty());
		this.newDelegatedSpawnPointPositionOffsetXField.setMaxLength(128);
		this.addWidget(this.newDelegatedSpawnPointPositionOffsetXField);

		this.newDelegatedSpawnPointPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 151, 100, 20, Component.empty());
		this.newDelegatedSpawnPointPositionOffsetYField.setMaxLength(128);
		this.addWidget(this.newDelegatedSpawnPointPositionOffsetYField);

		this.newDelegatedSpawnPointPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 151, 100, 20, Component.empty());
		this.newDelegatedSpawnPointPositionOffsetZField.setMaxLength(128);
		this.addWidget(this.newDelegatedSpawnPointPositionOffsetZField);

		this.newDelegatedSpawnPointOrientationYawField = new EditBox(this.font, this.width / 2 - 154, 186, 100, 20, Component.empty());
		this.newDelegatedSpawnPointOrientationYawField.setMaxLength(128);
		this.addWidget(this.newDelegatedSpawnPointOrientationYawField);

		this.newDelegatedSpawnPointOrientationPitchField = new EditBox(this.font, this.width / 2 - 50, 186, 100, 20, Component.empty());
		this.newDelegatedSpawnPointOrientationPitchField.setMaxLength(128);
		this.addWidget(this.newDelegatedSpawnPointOrientationPitchField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newDelegatedSpawnPointPositionOffsetXField);
	}

	@Override
	protected void updateWidgets() {

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.removeListEntryButton3.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.delegatedSpawnPoints.size()); i++) {
			if (index == 0) {
				this.removeListEntryButton0.visible = true;
			} else if (index == 1) {
				this.removeListEntryButton1.visible = true;
			} else if (index == 2) {
				this.removeListEntryButton2.visible = true;
			} else if (index == 3) {
				this.removeListEntryButton3.visible = true;
			}
			index++;
		}

	}

	@Override
	public void resize(final int width, final int height) {
		List<LevelData.RespawnData> list = new ArrayList<>(this.delegatedSpawnPoints);
		String string = this.newDelegatedSpawnPointPositionOffsetXField.getValue();
		String string1 = this.newDelegatedSpawnPointPositionOffsetYField.getValue();
		String string2 = this.newDelegatedSpawnPointPositionOffsetZField.getValue();
		String string3 = this.newDelegatedSpawnPointOrientationYawField.getValue();
		String string4 = this.newDelegatedSpawnPointOrientationPitchField.getValue();
		this.init(width, height);
		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(list);
		this.newDelegatedSpawnPointPositionOffsetXField.setValue(string);
		this.newDelegatedSpawnPointPositionOffsetYField.setValue(string1);
		this.newDelegatedSpawnPointPositionOffsetZField.setValue(string2);
		this.newDelegatedSpawnPointOrientationYawField.setValue(string3);
		this.newDelegatedSpawnPointOrientationPitchField.setValue(string4);
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 20;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 92)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.delegatedSpawnPoints.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS
				&& x >= (double) (this.width / 2 - 152) && x <= (double) (this.width / 2 + 154)
				&& y >= 20 && y <= 112) {
			int i = this.delegatedSpawnPoints.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) scrollY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(final KeyEvent event) {
		if (event.isConfirmation()) {
			this.done();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {

		super.extractRenderState(graphics, mouseX, mouseY, a);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.delegatedSpawnPoints.size()); i++) {
			LevelData.RespawnData delegatedSpawnPoint = this.delegatedSpawnPoints.get(i);

			graphics.text(this.font,
					Component.translatable("gui.spawn_point_delegation_block.list.entry.1", delegatedSpawnPoint.pos().getX(), delegatedSpawnPoint.pos().getY(), delegatedSpawnPoint.pos().getZ()),
					this.width / 2 - 117, 21 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			graphics.text(this.font,
					Component.translatable("gui.spawn_point_delegation_block.list.entry.2", delegatedSpawnPoint.yaw(), delegatedSpawnPoint.pitch()),
					this.width / 2 - 117, 31 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.delegatedSpawnPoints.size() > VISIBLE_LIST_ELEMENTS) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.width / 2 - 153, 20, 8, 92);
			int k = (int) (83.0f * this.scrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}
		graphics.text(this.font, NEW_DELEGATED_SPAWN_POINT_POSITION_OFFET_LABEL_TEXT, this.width / 2 - 153, 141, 0xA0A0A0);
		this.newDelegatedSpawnPointPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newDelegatedSpawnPointPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newDelegatedSpawnPointPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.text(this.font, NEW_DELEGATED_SPAWN_POINT_ORIENTATION_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.newDelegatedSpawnPointOrientationYawField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newDelegatedSpawnPointOrientationPitchField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateSpawnPointDelegationBlockPacket(
				this.spawnPointDelegationBlockEntity.getBlockPos(),
				this.delegatedSpawnPoints
		));
		return true;
	}
}
