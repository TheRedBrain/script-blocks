package com.github.theredbrain.scriptblocks.gui.screen.ingame;
//
//import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
//import com.github.theredbrain.scriptblocks.network.packet.UpdateLootableVaultBlockPacket;
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.gui.widget.ButtonWidget;
//import net.minecraft.client.gui.widget.TextFieldWidget;
//import net.minecraft.client.util.NarratorManager;
//import net.minecraft.screen.ScreenTexts;
//import net.minecraft.text.Text;
//import org.lwjgl.glfw.GLFW;
//
//@Environment(value = EnvType.CLIENT)
//public class LootableVaultBlockScreen extends Screen {
//	private static final Text LOOTABLE_VAULT_CONFIG_IDENTIFIER_LABEL_TEXT = Text.translatable("gui.lootable_vault_block.new_lootable_vault_config_identifier_label");
//	private static final Text CYCLE_IS_OMINOUS_BUTTON_LABEL_TEXT = Text.translatable("gui.lootable_vault_block.cycle_is_ominous_button_label");
//	private static final Text IS_OMINOUS_LABEL_TEXT = Text.translatable("gui.lootable_vault_block.is_ominous_label");
//	private static final Text IS_NOT_OMINOUS_LABEL_TEXT = Text.translatable("gui.lootable_vault_block.is_not_ominous_label");
//
//	private final LootableVaultBlockEntity lootableVaultBlock;
//
//	private TextFieldWidget newLootableVaultConfigIdentifierField;
//
//	private boolean isOminous;
//
//	public LootableVaultBlockScreen(LootableVaultBlockEntity lootableVaultBlock) {
//		super(NarratorManager.EMPTY);
//		this.lootableVaultBlock = lootableVaultBlock;
//	}
//
//	private void cycleIsOminous() {
//		this.isOminous = !this.isOminous;
//	}
//
//	private void done() {
//		if (this.updateLootableVaultBlock()) {
//			this.close();
//		}
//	}
//
//	private void cancel() {
//		this.close();
//	}
//
//	@Override
//	protected void init() {
//
//		this.newLootableVaultConfigIdentifierField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, 80, 300, 20, Text.empty());
//		this.newLootableVaultConfigIdentifierField.setMaxLength(128);
//		this.newLootableVaultConfigIdentifierField.setText(this.lootableVaultBlock.getConfigId());
//		this.addSelectableChild(this.newLootableVaultConfigIdentifierField);
//
//		this.isOminous = this.lootableVaultBlock.isOminous();
//		int i = this.textRenderer.getWidth(this.isOminous ? IS_OMINOUS_LABEL_TEXT : IS_NOT_OMINOUS_LABEL_TEXT) + 10;
//		this.addDrawableChild(ButtonWidget.builder(CYCLE_IS_OMINOUS_BUTTON_LABEL_TEXT, button -> this.cycleIsOminous()).dimensions(this.width / 2 - 4 - 150 + i, 115, 150, 20).build());
//
//		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.done()).dimensions(this.width / 2 - 4 - 150, 145, 150, 20).build());
//		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> this.cancel()).dimensions(this.width / 2 + 4, 145, 150, 20).build());
//	}
//
//	@Override
//	protected void setInitialFocus() {
//		this.setInitialFocus(this.newLootableVaultConfigIdentifierField);
//	}
//
//	@Override
//	public void resize(MinecraftClient client, int width, int height) {
//		String string = this.newLootableVaultConfigIdentifierField.getText();
//		boolean bl = this.isOminous;
//		this.init(client, width, height);
//		this.newLootableVaultConfigIdentifierField.setText(string);
//		this.isOminous = bl;
//	}
//
//	@Override
//	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
//			this.done();
//			return true;
//		}
//		return super.keyPressed(keyCode, scanCode, modifiers);
//	}
//
//	@Override
//	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//
//		super.render(context, mouseX, mouseY, delta);
//
//		context.drawTextWithShadow(this.textRenderer, LOOTABLE_VAULT_CONFIG_IDENTIFIER_LABEL_TEXT, this.width / 2 - 153, 70, 0xA0A0A0);
//		this.newLootableVaultConfigIdentifierField.render(context, mouseX, mouseY, delta);
//
//		context.drawTextWithShadow(this.textRenderer, this.isOminous ? IS_OMINOUS_LABEL_TEXT : IS_NOT_OMINOUS_LABEL_TEXT, this.width / 2 - 153, 121, 0xA0A0A0);
//	}
//
//	@Override
//	public boolean shouldPause() {
//		return false;
//	}
//
//	private boolean updateLootableVaultBlock() {
//		ClientPlayNetworking.send(new UpdateLootableVaultBlockPacket(
//				this.lootableVaultBlock.getPos(),
//				this.isOminous,
//				this.newLootableVaultConfigIdentifierField.getText()
//		));
//		return true;
//	}
//}
