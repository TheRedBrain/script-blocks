package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataRelayBlockPacket;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class DataRelayBlockScreen extends Screen {
	private static final int VISIBLE_LIST_ENTRIES = 6;
	private static final Text NEW_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.data_provider_block.newDataProvidingBlockPositionOffset");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetX.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetY.placeholder");
	private static final Text NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT = Text.translatable("gui.triggered_block.triggeredBlockPositionOffsetZ.placeholder");
	private static final Text ADD_NEW_ENTRY_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_140_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_140");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final DataRelayBlockEntity dataRelayBlockEntity;

	private ButtonWidget removeEntryButton0;
	private ButtonWidget removeEntryButton1;
	private ButtonWidget removeEntryButton2;
	private ButtonWidget removeEntryButton3;
	private ButtonWidget removeEntryButton4;
	private ButtonWidget removeEntryButton5;

	private TextFieldWidget newDataProvidingBlockPosOffsetXField;
	private TextFieldWidget newDataProvidingBlockPosOffsetYField;
	private TextFieldWidget newDataProvidingBlockPosOffsetZField;

	private final List<BlockPos> dataProvidingBlockPosOffsetList = new ArrayList<>();

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
		super(NarratorManager.EMPTY);
		this.dataRelayBlockEntity = dataRelayBlockEntity;
	}

	private void addNewEntry() {
			this.dataProvidingBlockPosOffsetList.add(new BlockPos(
					ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetXField.getText()),
					ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetYField.getText()),
					ItemUtils.parseInt(this.newDataProvidingBlockPosOffsetZField.getText())
			));
			this.updateWidgets();
	}

	private void removeEntry(int index) {
		if (index + this.scrollPosition < this.dataProvidingBlockPosOffsetList.size()) {
			this.dataProvidingBlockPosOffsetList.remove(index + this.scrollPosition);
		}
		this.updateWidgets();
	}

	private void done() {
		if (this.updateDataRelayBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {
		this.dataProvidingBlockPosOffsetList.clear();
		this.dataProvidingBlockPosOffsetList.addAll(this.dataRelayBlockEntity.getDataProvidingBlockPosOffsetList());

		this.removeEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(0)));
		this.removeEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(1)));
		this.removeEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(2)));
		this.removeEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(3)));
		this.removeEntryButton4 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(4)));
		this.removeEntryButton5 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 140, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeEntry(5)));

		this.newDataProvidingBlockPosOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 186, 50, 20, Text.empty());
		this.newDataProvidingBlockPosOffsetXField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetXField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_X_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDataProvidingBlockPosOffsetXField);

		this.newDataProvidingBlockPosOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 104, 186, 50, 20, Text.empty());
		this.newDataProvidingBlockPosOffsetYField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetYField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Y_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDataProvidingBlockPosOffsetYField);

		this.newDataProvidingBlockPosOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 - 54, 186, 50, 20, Text.empty());
		this.newDataProvidingBlockPosOffsetZField.setMaxLength(128);
		this.newDataProvidingBlockPosOffsetZField.setPlaceholder(NEW_TRIGGERED_BLOCK_POS_OFFSET_Z_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDataProvidingBlockPosOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ADD_NEW_ENTRY_BUTTON_LABEL_TEXT, button -> this.addNewEntry()).dimensions(this.width / 2 + 4, 186, 150, 20).build());

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 210, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 210, 150, 20).build());

		this.updateWidgets();
	}

	private void updateWidgets() {

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
	public void resize(MinecraftClient client, int width, int height) {
		List<BlockPos> list = new ArrayList<>(this.dataProvidingBlockPosOffsetList);
		String string = this.newDataProvidingBlockPosOffsetXField.getText();
		String string1 = this.newDataProvidingBlockPosOffsetYField.getText();
		String string2 = this.newDataProvidingBlockPosOffsetZField.getText();
		this.init(client, width, height);
		this.dataProvidingBlockPosOffsetList.clear();
		this.dataProvidingBlockPosOffsetList.addAll(list);
		this.newDataProvidingBlockPosOffsetXField.setText(string);
		this.newDataProvidingBlockPosOffsetYField.setText(string1);
		this.newDataProvidingBlockPosOffsetZField.setText(string2);
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
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
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
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0f, 1.0f);
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ENTRIES, this.dataProvidingBlockPosOffsetList.size()); i++) {
			BlockPos blockPos = this.dataProvidingBlockPosOffsetList.get(i);
			MutableText text = Text.translatable("gui.triggered_block.list.entry", blockPos.getX(), blockPos.getY(), blockPos.getZ());
			context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 26 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.dataProvidingBlockPosOffsetList.size() > VISIBLE_LIST_ENTRIES) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_140_TEXTURE, this.width / 2 - 153, 20, 8, 140);
			int k = (int) (131.0f * this.scrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}

		context.drawTextWithShadow(this.textRenderer, NEW_DATA_PROVIDING_BLOCK_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 176, 0xA0A0A0);
		this.newDataProvidingBlockPosOffsetXField.render(context, mouseX, mouseY, delta);
		this.newDataProvidingBlockPosOffsetYField.render(context, mouseX, mouseY, delta);
		this.newDataProvidingBlockPosOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataRelayBlock() {
		ClientPlayNetworking.send(new UpdateDataRelayBlockPacket(
				this.dataRelayBlockEntity.getPos(),
				this.dataProvidingBlockPosOffsetList
		));
		return true;
	}
}
