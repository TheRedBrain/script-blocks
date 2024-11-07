package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataSavingBlockPacket;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class DataSavingBlockScreen extends Screen {
	private static final int VISIBLE_DATA_LIST_ENTRIES = 5;
	private static final Text NEW_DATA_IDENTIFIER_PLACEHOLDER_TEXT = Text.translatable("gui.data_saving_block.new_data_identifier_placeholder");
	private static final Text NEW_DATA_VALUE_PLACEHOLDER_TEXT = Text.translatable("gui.data_saving_block.new_data_value_placeholder");
	private static final Text ADD_NEW_DATA_BUTTON_LABEL_TEXT = Text.translatable("gui.data_saving_block.add_new_data_button_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final DataSavingBlockEntity dataSavingBlock;

	private ButtonWidget removeDataButton0;
	private ButtonWidget removeDataButton1;
	private ButtonWidget removeDataButton2;
	private ButtonWidget removeDataButton3;
	private ButtonWidget removeDataButton4;

	private TextFieldWidget newDataIdentifierField;
	private TextFieldWidget newDataValueField;

	private List<MutablePair<String, String>> dataList = new ArrayList<>();

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DataSavingBlockScreen(DataSavingBlockEntity dataSavingBlock) {
		super(NarratorManager.EMPTY);
		this.dataSavingBlock = dataSavingBlock;
	}

	private void addNewData(String dataIdentifier, String dataValue) {
		if (!dataIdentifier.isEmpty() && !dataValue.isEmpty()) {
			for (MutablePair<String, String> entry : this.dataList) {
				if (entry.getLeft().equals(dataIdentifier)) {
					entry.setRight(dataValue);
					return;
				}
			}
			this.dataList.add(new MutablePair<>(dataIdentifier, dataValue));
			this.updateWidgets();
		}
	}

	private void removeData(int index) {
		if (index + this.scrollPosition < this.dataList.size()) {
			this.dataList.remove(index + this.scrollPosition);
		}
		this.updateWidgets();
	}

	private void done() {
		if (this.updateDataWritingBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.dataList = this.dataSavingBlock.getDataList();

		this.removeDataButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(0)));
		this.removeDataButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(1)));
		this.removeDataButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(2)));
		this.removeDataButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(3)));
		this.removeDataButton4 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(4)));

		this.newDataIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 140, 300, 20, Text.empty());
		this.newDataIdentifierField.setMaxLength(128);
		this.newDataIdentifierField.setPlaceholder(NEW_DATA_IDENTIFIER_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDataIdentifierField);

		this.newDataValueField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 164, 300, 20, Text.empty());
		this.newDataValueField.setMaxLength(128);
		this.newDataValueField.setPlaceholder(NEW_DATA_VALUE_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newDataValueField);

		this.addDrawableChild(ButtonWidget.builder(ADD_NEW_DATA_BUTTON_LABEL_TEXT, button -> this.addNewData(this.newDataIdentifierField.getText(), this.newDataValueField.getText())).dimensions(this.width / 2 - 154, 188, 300, 20).build());

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 212, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newDataIdentifierField);
	}

	private void updateWidgets() {

		this.removeDataButton0.visible = false;
		this.removeDataButton1.visible = false;
		this.removeDataButton2.visible = false;
		this.removeDataButton3.visible = false;
		this.removeDataButton4.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(VISIBLE_DATA_LIST_ENTRIES, this.dataList.size()); i++) {
			if (index == 0) {
				this.removeDataButton0.visible = true;
			} else if (index == 1) {
				this.removeDataButton1.visible = true;
			} else if (index == 2) {
				this.removeDataButton2.visible = true;
			} else if (index == 3) {
				this.removeDataButton3.visible = true;
			} else if (index == 4) {
				this.removeDataButton4.visible = true;
			}
			index++;
		}

	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String string = this.newDataIdentifierField.getText();
		String string1 = this.newDataValueField.getText();
		this.init(client, width, height);
		this.newDataIdentifierField.setText(string);
		this.newDataValueField.setText(string1);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		int i;
		int j;
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES) {
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
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dataList.size() - VISIBLE_DATA_LIST_ENTRIES;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 20 && mouseY <= 136) {
			int i = this.dataList.size() - VISIBLE_DATA_LIST_ENTRIES;
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_DATA_LIST_ENTRIES, this.dataList.size()); i++) {
			context.drawTextWithShadow(this.textRenderer, this.dataList.get(i).left, this.width / 2 - 117, 20 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			context.drawTextWithShadow(this.textRenderer, this.dataList.get(i).right, this.width / 2 - 117, 30 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
		}

		this.newDataIdentifierField.render(context, mouseX, mouseY, delta);

		this.newDataValueField.render(context, mouseX, mouseY, delta);

	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private boolean updateDataWritingBlock() {
		ClientPlayNetworking.send(new UpdateDataSavingBlockPacket(
				this.dataSavingBlock.getPos(),
				this.dataList
		));
		return true;
	}
}
