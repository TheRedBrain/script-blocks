package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUUIDListRelayBlockPacket;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class UUIDListRelayBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final Component NEW_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.uuid_list_relay_block.new_uuid_list_provider_position_offset");
	private static final Component NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.block_pos.x");
	private static final Component NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.block_pos.y");
	private static final Component NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT = Component.translatable("gui.block_pos.z");
	private static final Component ADD_NEW_UUID_LIST_HANDLER_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 5;
	private final UUIDListRelayBlockEntity uuidListRelayBlock;
	private final List<BlockPos> uuidListHandlers = new ArrayList<>(List.of());
	private Button removeListEntryButton0;
	private Button removeListEntryButton1;
	private Button removeListEntryButton2;
	private Button removeListEntryButton3;
	private Button removeListEntryButton4;
	private EditBox newUUIDListProviderPositionOffsetXField;
	private EditBox newUUIDListProviderPositionOffsetYField;
	private EditBox newUUIDListProviderPositionOffsetZField;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public UUIDListRelayBlockScreen(UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.uuidListRelayBlock = uuidListRelayBlockEntity;
	}

	private void addNewUUIDListHandler() {
		BlockPos newUUIDListHandler = new BlockPos(
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetXField.getValue()),
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetYField.getValue()),
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetZField.getValue())
		);
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			if (uuidListHandler.equals(newUUIDListHandler)) {
				return;
			}
		}
		this.uuidListHandlers.add(newUUIDListHandler);
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	private void removeUUIDListHandler(int index) {
		if (index + this.scrollPosition < this.uuidListHandlers.size()) {
			this.uuidListHandlers.remove(index + this.scrollPosition);
		}
		this.scrollPosition = 0;
		this.scrollAmount = 0.0f;
		this.updateWidgets();
	}

	@Override
	protected void init() {

		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(this.uuidListRelayBlock.getUuidListHandlers());

		this.removeListEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(0)));
		this.removeListEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(1)));
		this.removeListEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(2)));
		this.removeListEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(3)));
		this.removeListEntryButton4 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(4)));

		this.addRenderableWidget(Button.builder(ADD_NEW_UUID_LIST_HANDLER_BUTTON_LABEL_TEXT, button -> this.addNewUUIDListHandler()).bounds(this.width / 2 - 154, 140, 300, 20).build());

		this.newUUIDListProviderPositionOffsetXField = new EditBox(this.font, this.width / 2 - 154, 175, 100, 20, Component.empty());
		this.newUUIDListProviderPositionOffsetXField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetXField.setHint(NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newUUIDListProviderPositionOffsetXField);
		this.newUUIDListProviderPositionOffsetYField = new EditBox(this.font, this.width / 2 - 50, 175, 100, 20, Component.empty());
		this.newUUIDListProviderPositionOffsetYField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetYField.setHint(NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newUUIDListProviderPositionOffsetYField);
		this.newUUIDListProviderPositionOffsetZField = new EditBox(this.font, this.width / 2 + 54, 175, 100, 20, Component.empty());
		this.newUUIDListProviderPositionOffsetZField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetZField.setHint(NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT);
		this.addWidget(this.newUUIDListProviderPositionOffsetZField);

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 206, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 206, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newUUIDListProviderPositionOffsetXField);
	}

	@Override
	protected void updateWidgets() {

		this.removeListEntryButton0.visible = false;
		this.removeListEntryButton1.visible = false;
		this.removeListEntryButton2.visible = false;
		this.removeListEntryButton3.visible = false;
		this.removeListEntryButton4.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_LIST_ELEMENTS, this.uuidListHandlers.size()); i++) {
			if (index == 0) {
				this.removeListEntryButton0.visible = true;
			} else if (index == 1) {
				this.removeListEntryButton1.visible = true;
			} else if (index == 2) {
				this.removeListEntryButton2.visible = true;
			} else if (index == 3) {
				this.removeListEntryButton3.visible = true;
			} else if (index == 4) {
				this.removeListEntryButton4.visible = true;
			}
			index++;
		}
	}

	@Override
	public void resize(final int width, final int height) {
		List<BlockPos> list = new ArrayList<>(this.uuidListHandlers);
		String string = this.newUUIDListProviderPositionOffsetXField.getValue();
		String string1 = this.newUUIDListProviderPositionOffsetYField.getValue();
		String string2 = this.newUUIDListProviderPositionOffsetZField.getValue();
		this.init(width, height);
		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(list);
		this.newUUIDListProviderPositionOffsetXField.setValue(string);
		this.newUUIDListProviderPositionOffsetYField.setValue(string1);
		this.newUUIDListProviderPositionOffsetZField.setValue(string2);
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 20;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 136)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.uuidListHandlers.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS
				&& x >= (double) (this.width / 2 - 152) && x <= (double) (this.width / 2 + 154)
				&& y >= 20 && y <= 136) {
			int i = this.uuidListHandlers.size() - VISIBLE_LIST_ELEMENTS;
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.uuidListHandlers.size()); i++) {
			BlockPos uuidListHandler = this.uuidListHandlers.get(i);
			MutableComponent text = Component.translatable("gui.uuid_list_relay_block.list_entry", uuidListHandler.getX(), uuidListHandler.getY(), uuidListHandler.getZ());
			graphics.text(this.font, text, this.width / 2 - 117, 26 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}

		graphics.text(this.font, NEW_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 165, 0xA0A0A0);
		this.newUUIDListProviderPositionOffsetXField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newUUIDListProviderPositionOffsetYField.extractRenderState(graphics, mouseX, mouseY, a);
		this.newUUIDListProviderPositionOffsetZField.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateUUIDListRelayBlockPacket(
				this.uuidListRelayBlock.getBlockPos(),
				this.uuidListHandlers
		));
		return true;
	}
}
