package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUUIDListRelayBlockPacket;
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
public class UUIDListRelayBlockScreen extends Screen {
	private static final Text NEW_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT = Text.translatable("gui.uuid_list_relay_block.new_uuid_list_provider_position_offset");
	private static final Text NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.block_pos.x");
	private static final Text NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.block_pos.y");
	private static final Text NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT = Text.translatable("gui.block_pos.z");
	private static final Text ADD_NEW_UUID_LIST_HANDLER_BUTTON_LABEL_TEXT = Text.translatable("gui.list_entry.add");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_116_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_116");
	private static final Identifier SCROLLER_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");
	public static final ButtonTextures REMOVE_ENTRY_BUTTON_TEXTURES = new ButtonTextures(
			Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button"), Identifier.of(ScriptBlocks.MOD_ID, "widgets/remove_entry_button_highlighted")
	);
	private static final int VISIBLE_LIST_ELEMENTS = 5;
	private final UUIDListRelayBlockEntity uuidListRelayBlock;
	private final List<BlockPos> uuidListHandlers = new ArrayList<>(List.of());
	private ButtonWidget removeListEntryButton0;
	private ButtonWidget removeListEntryButton1;
	private ButtonWidget removeListEntryButton2;
	private ButtonWidget removeListEntryButton3;
	private ButtonWidget removeListEntryButton4;
	private TextFieldWidget newUUIDListProviderPositionOffsetXField;
	private TextFieldWidget newUUIDListProviderPositionOffsetYField;
	private TextFieldWidget newUUIDListProviderPositionOffsetZField;
	private int scrollPosition = 0;
	private float scrollAmount = 0.0f;
	private boolean mouseClicked = false;

	public UUIDListRelayBlockScreen(UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
		super(NarratorManager.EMPTY);
		this.uuidListRelayBlock = uuidListRelayBlockEntity;
	}

	private void addNewUUIDListHandler() {
		BlockPos newUUIDListHandler = new BlockPos(
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetXField.getText()),
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetYField.getText()),
				ItemUtils.parseInt(this.newUUIDListProviderPositionOffsetZField.getText())
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

	private void done() {
		if (this.updateDelayTriggerBlock()) {
			this.close();
		}
	}

	private void cancel() {
		this.close();
	}

	@Override
	protected void init() {

		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(this.uuidListRelayBlock.getUuidListHandlers());

		this.removeListEntryButton0 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 20, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(0)));
		this.removeListEntryButton1 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 44, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(1)));
		this.removeListEntryButton2 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 68, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(2)));
		this.removeListEntryButton3 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 92, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(3)));
		this.removeListEntryButton4 = this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 141, 116, 20, 20, REMOVE_ENTRY_BUTTON_TEXTURES, button -> this.removeUUIDListHandler(4)));

		this.addDrawableChild(ButtonWidget.builder(ADD_NEW_UUID_LIST_HANDLER_BUTTON_LABEL_TEXT, button -> this.addNewUUIDListHandler()).dimensions(this.width / 2 - 154, 140, 300, 20).build());

		this.newUUIDListProviderPositionOffsetXField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 175, 100, 20, Text.empty());
		this.newUUIDListProviderPositionOffsetXField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetXField.setPlaceholder(NEW_POSITION_X_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newUUIDListProviderPositionOffsetXField);
		this.newUUIDListProviderPositionOffsetYField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, 175, 100, 20, Text.empty());
		this.newUUIDListProviderPositionOffsetYField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetYField.setPlaceholder(NEW_POSITION_Y_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newUUIDListProviderPositionOffsetYField);
		this.newUUIDListProviderPositionOffsetZField = new TextFieldWidget(this.textRenderer, this.width / 2 + 54, 175, 100, 20, Text.empty());
		this.newUUIDListProviderPositionOffsetZField.setMaxLength(128);
		this.newUUIDListProviderPositionOffsetZField.setPlaceholder(NEW_POSITION_Z_FIELD_PLACEHOLDER_TEXT);
		this.addSelectableChild(this.newUUIDListProviderPositionOffsetZField);

		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 206, 150, 20).build());
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 206, 150, 20).build());
		this.updateWidgets();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.newUUIDListProviderPositionOffsetXField);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderInGameBackground(context);
	}

	private void updateWidgets() {

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
	public void resize(MinecraftClient client, int width, int height) {
		List<BlockPos> list = new ArrayList<>(this.uuidListHandlers);
		String string = this.newUUIDListProviderPositionOffsetXField.getText();
		String string1 = this.newUUIDListProviderPositionOffsetYField.getText();
		String string2 = this.newUUIDListProviderPositionOffsetZField.getText();
		this.init(client, width, height);
		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(list);
		this.newUUIDListProviderPositionOffsetXField.setText(string);
		this.newUUIDListProviderPositionOffsetYField.setText(string1);
		this.newUUIDListProviderPositionOffsetZField.setText(string2);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS) {
			int i = this.width / 2 - 152;
			int j = 20;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 136)) {
				this.mouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS
				&& this.mouseClicked) {
			int i = this.uuidListHandlers.size() - VISIBLE_LIST_ELEMENTS;
			float f = (float) deltaY / (float) i;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount + f, 0.0f, 1.0f);
			this.scrollPosition = (int) ((double) (this.scrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS
				&& mouseX >= (double) (this.width / 2 - 152) && mouseX <= (double) (this.width / 2 + 154)
				&& mouseY >= 20 && mouseY <= 136) {
			int i = this.uuidListHandlers.size() - VISIBLE_LIST_ELEMENTS;
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

	private boolean updateDelayTriggerBlock() {
		ClientPlayNetworking.send(new UpdateUUIDListRelayBlockPacket(
				this.uuidListRelayBlock.getPos(),
				this.uuidListHandlers
		));
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		for (int i = this.scrollPosition; i < Math.min(this.scrollPosition + VISIBLE_LIST_ELEMENTS, this.uuidListHandlers.size()); i++) {
			BlockPos uuidListHandler = this.uuidListHandlers.get(i);
			MutableText text = Text.translatable("gui.uuid_list_relay_block.list_entry", uuidListHandler.getX(), uuidListHandler.getY(), uuidListHandler.getZ());
			context.drawTextWithShadow(this.textRenderer, text, this.width / 2 - 117, 26 + ((i - this.scrollPosition) * 24), 0xA0A0A0);
		}
		if (this.uuidListHandlers.size() > VISIBLE_LIST_ELEMENTS) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_116_TEXTURE, this.width / 2 - 153, 20, 8, 116);
			int k = (int) (107.0f * this.scrollAmount);
			context.drawGuiTexture(SCROLLER_TEXTURE, this.width / 2 - 152, 20 + 1 + k, 6, 7);
		}

		context.drawTextWithShadow(this.textRenderer, NEW_UUID_LIST_PROVIDER_POSITION_OFFSET_LABEL_TEXT, this.width / 2 - 153, 165, 0xA0A0A0);
		this.newUUIDListProviderPositionOffsetXField.render(context, mouseX, mouseY, delta);
		this.newUUIDListProviderPositionOffsetYField.render(context, mouseX, mouseY, delta);
		this.newUUIDListProviderPositionOffsetZField.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
