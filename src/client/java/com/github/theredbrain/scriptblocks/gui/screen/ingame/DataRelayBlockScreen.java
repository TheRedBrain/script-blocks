package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataRelayBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class DataRelayBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final int VISIBLE_LIST_ENTRIES = 6;
	private static final Component NEW_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Component.translatable("gui.data_provider_block.new_data_providing_block_position_offset");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_x.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_y.placeholder");
	private static final Component NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Component.translatable("gui.triggered_block.triggered_block_position_offset_z.placeholder");
	private static final Component ADD_NEW_ENTRY_BUTTON_LABEL_TEXT = Component.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_140_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_140");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final DataRelayBlockEntity dataRelayBlockEntity;

	private Button removeEntryButton0;
	private Button removeEntryButton1;
	private Button removeEntryButton2;
	private Button removeEntryButton3;
	private Button removeEntryButton4;
	private Button removeEntryButton5;

	private EditBox newDataProvidingBlockPosOffsetXField;
	private EditBox newDataProvidingBlockPosOffsetYField;
	private EditBox newDataProvidingBlockPosOffsetZField;

	private final List<BlockPos> dataProvidingBlockPosOffsetList = new ArrayList<>();

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
		super(GameNarrator.NO_TITLE);
		this.dataRelayBlockEntity = dataRelayBlockEntity;
	}

	private void addNewEntry() {
		this.dataProvidingBlockPosOffsetList.add(new BlockPos(
				ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetXField.getValue()),
				ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetYField.getValue()),
				ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetZField.getValue())
		));
		this.updateWidgets();
	}

	private void removeEntry(int index) {
		if (index + this.scrollPosition < this.dataProvidingBlockPosOffsetList.size()) {
			this.dataProvidingBlockPosOffsetList.remove(index + this.scrollPosition);
		}
		this.updateWidgets();
	}

	@Override
	protected void init() {
		this.dataProvidingBlockPosOffsetList.clear();
		this.dataProvidingBlockPosOffsetList.addAll(this.dataRelayBlockEntity.getDataProvidingBlockPosOffsetList());

		this.removeEntryButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(0)));
		this.removeEntryButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(1)));
		this.removeEntryButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(2)));
		this.removeEntryButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(3)));
		this.removeEntryButton4 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(4)));
		this.removeEntryButton5 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 140, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(5)));

		this.newDataProvidingBlockPosOffsetXField = new EditBox(this.font, this.width / 2 - 154, 186, 50, 20, Component.empty());
		this.newDataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetXField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addWidget(this.newDataProvidingBlockPosOffsetXField);

		this.newDataProvidingBlockPosOffsetYField = new EditBox(this.font, this.width / 2 - 104, 186, 50, 20, Component.empty());
		this.newDataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetYField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addWidget(this.newDataProvidingBlockPosOffsetYField);

		this.newDataProvidingBlockPosOffsetZField = new EditBox(this.font, this.width / 2 - 54, 186, 50, 20, Component.empty());
		this.newDataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetZField.setHint(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addWidget(this.newDataProvidingBlockPosOffsetZField);

		this.addRenderableWidget(Button.builder(ADD_NEW_ENTRY_BUTTON_LABEL_TEXT, button -> this.addNewEntry()).bounds(this.width / 2 + 4, 186, 150, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newDataProvidingBlockPosOffsetXField);
	}

	@Override
	protected void updateWidgets() {

		this.removeEntryButton0.visible = false;
		this.removeEntryButton1.visible = false;
		this.removeEntryButton2.visible = false;
		this.removeEntryButton3.visible = false;
		this.removeEntryButton4.visible = false;
		this.removeEntryButton5.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_LIST_ENTRIES, this.dataProvidingBlockPosOffsetList.size()); i++) {
			if (index == 0) {
				this.removeEntryButton0.visible = true;
			} else if (index == 1) {
				this.removeEntryButton1.visible = true;
			} else if (index == 2) {
				this.removeEntryButton2.visible = true;
			} else if (index == 3) {
				this.removeEntryButton3.visible = true;
			} else if (index == 4) {
				this.removeEntryButton4.visible = true;
			} else if (index == 5) {
				this.removeEntryButton5.visible = true;
			}
			index++;
		}

	}

	@Override
	public void resize(Minecraft client, int width, int height) {
		List<BlockPos> list = new ArrayList<>(this.dataProvidingBlockPosOffsetList);
		String string = this.newDataProvidingBlockPosOffsetXField.getValue();
		String string1 = this.newDataProvidingBlockPosOffsetYField.getValue();
		String string2 = this.newDataProvidingBlockPosOffsetZField.getValue();
		this.init(client, width, height);
		this.dataProvidingBlockPosOffsetList.clear();
		this.dataProvidingBlockPosOffsetList.addAll(list);
		this.newDataProvidingBlockPosOffsetXField.setValue(string);
		this.newDataProvidingBlockPosOffsetYField.setValue(string1);
		this.newDataProvidingBlockPosOffsetZField.setValue(string2);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		int i;
		int j;
		if (this.dataProvidingBlockPosOffsetList.size() > VISIBLE_LIST_ENTRIES) {
			i = this.width / 2 - 152;
			j = 20;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 136)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.dataProvidingBlockPosOffsetList.size() > VISIBLE_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dataProvidingBlockPosOffsetList.size() - VISIBLE_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.dataProvidingBlockPosOffsetList.size() > VISIBLE_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 20 && mouseY <= 136) {
			int i = this.dataProvidingBlockPosOffsetList.size() - VISIBLE_LIST_ENTRIES;
			float f = (float) verticalAmount / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ENTRIES, this.dataProvidingBlockPosOffsetList.size()); i++) {
			BlockPos blockPos = this.dataProvidingBlockPosOffsetList.get(i);
			MutableComponent text = Component.translatable("gui.triggered_block.list.entry", blockPos.getX(), blockPos.getY(), blockPos.getZ());
			context.drawString(this.font, text, this.width / 2 - 117, 26 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.dataProvidingBlockPosOffsetList.size() > VISIBLE_LIST_ENTRIES) {
			context.blitSprite(SCROLL_BAR_BACKGROUND_8_140_TEXTURE, this.width / 2 - 153, 20, 8, 140);
			int k = (int) (131.0f * this.scrollAmount);
			context.blitSprite(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}

		context.drawString(this.font, NEW_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.newDataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.newDataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.newDataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDataRelayBlockPacket(
				this.dataRelayBlockEntity.getPos(),
				this.dataProvidingBlockPosOffsetList
		));
		return true;
	}
}
