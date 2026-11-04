package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataSavingBlockPacket;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class DataSavingBlockScreen extends AbstractCreativeBlockSettingsScreen {
	private static final int VISIBLE_DATA_LIST_ENTRIES = 5;
	private static final Component NEW_DATA_IDENTIFIER_PLACEHOLDER_TEXT = Component.translatable("gui.data_saving_block.new_data_identifier_placeholder");
	private static final Component NEW_DATA_VALUE_PLACEHOLDER_TEXT = Component.translatable("gui.data_saving_block.new_data_value_placeholder");
	private static final Component ADD_NEW_DATA_BUTTON_LABEL_TEXT = Component.translatable("gui.data_saving_block.add_new_data_button_label");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final WidgetSprites REMOVE_ENTRY_BUTTON_TEXTURES = new WidgetSprites(
			Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.fromNamespaceAndPath(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private final DataSavingBlockEntity dataSavingBlock;

	private Button removeDataButton0;
	private Button removeDataButton1;
	private Button removeDataButton2;
	private Button removeDataButton3;
	private Button removeDataButton4;

	private EditBox newDataIdentifierField;
	private EditBox newDataValueField;

	private List<MutablePair<String, String>> dataList = new ArrayList<>();

	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public DataSavingBlockScreen(DataSavingBlockEntity dataSavingBlock) {
		super(GameNarrator.NO_TITLE);
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

	@Override
	protected void init() {

		this.dataList = this.dataSavingBlock.getDataList();

		this.removeDataButton0 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(0)));
		this.removeDataButton1 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(1)));
		this.removeDataButton2 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(2)));
		this.removeDataButton3 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(3)));
		this.removeDataButton4 = this.addRenderableWidget(new ImageButton(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeData(4)));

		this.newDataIdentifierField = new EditBox(this.font, this.width / 2 - 154, 140, 300, 20, Component.empty());
		this.newDataIdentifierField.setMaxLength(128);
		this.newDataIdentifierField.setHint(NEW_DATA_IDENTIFIER_PLACEHOLDER_TEXT);
		this.addWidget(this.newDataIdentifierField);

		this.newDataValueField = new EditBox(this.font, this.width / 2 - 154, 164, 300, 20, Component.empty());
		this.newDataValueField.setMaxLength(128);
		this.newDataValueField.setHint(NEW_DATA_VALUE_PLACEHOLDER_TEXT);
		this.addWidget(this.newDataValueField);

		this.addRenderableWidget(Button.builder(ADD_NEW_DATA_BUTTON_LABEL_TEXT, button -> this.addNewData(this.newDataIdentifierField.getValue(), this.newDataValueField.getValue())).bounds(this.width / 2 - 154, 188, 300, 20).build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(this.width / 2 - 4 - 150, 212, 150, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(this.width / 2 + 4, 212, 150, 20).build());

		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newDataIdentifierField);
	}

	@Override
	protected void updateWidgets() {

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
	public void resize(final int width, final int height) {
		List<MutablePair<String, String>> list = new ArrayList<>(this.dataList);
		String string = this.newDataIdentifierField.getValue();
		String string1 = this.newDataValueField.getValue();
		this.init(width, height);
		this.dataList.clear();
		this.dataList.addAll(list);
		this.newDataIdentifierField.setValue(string);
		this.newDataValueField.setValue(string1);
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		this.mouseClicked = false;
		int i;
		int j;
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES) {
			i = this.width / 2 - 152;
			j = 20;
			if (event.x() >= (double) i && event.x() < (double) (i + 6) && event.y() >= (double) j && event.y() < (double) (j + 136)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES
				&& this.mouseClicked) {
			int i = this.dataList.size() - VISIBLE_DATA_LIST_ENTRIES;
			float f = (float) dy / (float) i;
			this.scrollAmount = Mth.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES
				&& x >= (double) (this.width / 2 - 152) && x <= (double) (this.width / 2 + 154)
				&& y >= 20 && y <= 136) {
			int i = this.dataList.size() - VISIBLE_DATA_LIST_ENTRIES;
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

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_DATA_LIST_ENTRIES, this.dataList.size()); i++) {
			graphics.text(this.font, this.dataList.get(i).left, this.width / 2 - 117, 20 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
			graphics.text(this.font, this.dataList.get(i).right, this.width / 2 - 117, 30 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.dataList.size() > VISIBLE_DATA_LIST_ENTRIES) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, this.width / 2 - 152, 44 + 1 + k, 6, 7);
		}

		this.newDataIdentifierField.extractRenderState(graphics, mouseX, mouseY, a);

		this.newDataValueField.extractRenderState(graphics, mouseX, mouseY, a);

	}

	@Override
	protected boolean updateBlock() {
		ClientPlayNetworking.send(new UpdateDataSavingBlockPacket(
				this.dataSavingBlock.getBlockPos(),
				this.dataList
		));
		return true;
	}
}
