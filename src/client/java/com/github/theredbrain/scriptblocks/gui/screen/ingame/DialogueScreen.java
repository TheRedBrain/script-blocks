package com.github.theredbrain.scriptblocks.gui.screen.ingame;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.network.DuckClientAdvancementManagerMixin;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacket;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(value = EnvType.CLIENT)
public class DialogueScreen extends HandledScreen<DialogueScreenHandler> {
	public static final Identifier BACKGROUND_218_197_TEXTURE = ScriptBlocks.identifier("textures/gui/container/generic_218_197_background.png");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_87_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_87");
	private static final Identifier SCROLL_BAR_BACKGROUND_8_92_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroll_bar_background_8_92");
	private static final Identifier SCROLLER_VERTICAL_6_7_TEXTURE = ScriptBlocks.identifier("scroll_bar/scroller_vertical_6_7");

	private ButtonWidget answerButton0;
	private ButtonWidget answerButton1;
	private ButtonWidget answerButton2;
	private ButtonWidget answerButton3;

	private int backgroundWidth;
	private int backgroundHeight;
	private int x;
	private int y;
	private int dialogueTextScrollPosition = 0;
	private float dialogueTextScrollAmount = 0.0f;
	private boolean dialogueTextMouseClicked = false;
	private int answersScrollPosition = 0;
	private float answersScrollAmount = 0.0f;
	private boolean answersMouseClicked = false;

	public DialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	private void answer(int index) {
		if (index + this.answersScrollPosition < this.handler.visibleAnswersList.size()) {
			Identifier currentAnswerIdentifier = Identifier.of(this.handler.visibleAnswersList.get(index + this.answersScrollPosition));

			ClientPlayNetworking.send(new DialogueAnswerPacket(
					currentAnswerIdentifier,
					this.handler.dataBlockPos,
					this.handler.dialogueUsedBlocksList,
					this.handler.dialogueTriggeredBlocksList
			));
		}
	}

	@Override
	protected void init() {
		if (this.handler.dialogue == null && this.client != null) {
			this.client.setScreen(null);
			return;
		}

		this.backgroundWidth = 218;
		this.backgroundHeight = 197;
		this.x = (this.width - this.backgroundWidth) / 2;
		this.y = (this.height - this.backgroundHeight) / 2;

		super.init();

		this.answerButton0 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(0)).dimensions(this.x + 7, this.y + 98, this.backgroundWidth - 14, 20).build());
		this.answerButton1 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(1)).dimensions(this.x + 7, this.y + 122, this.backgroundWidth - 14, 20).build());
		this.answerButton2 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(2)).dimensions(this.x + 7, this.y + 146, this.backgroundWidth - 14, 20).build());
		this.answerButton3 = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> this.answer(3)).dimensions(this.x + 7, this.y + 170, this.backgroundWidth - 14, 20).build());

		if (this.handler.visibleAnswersList.size() > 4) {
			this.answerButton0.setWidth(this.backgroundWidth - 26);
			this.answerButton1.setWidth(this.backgroundWidth - 26);
			this.answerButton2.setWidth(this.backgroundWidth - 26);
			this.answerButton3.setWidth(this.backgroundWidth - 26);
		}

		this.updateWidgets();
	}

	private void updateWidgets() {

		this.answerButton0.visible = false;
		this.answerButton1.visible = false;
		this.answerButton2.visible = false;
		this.answerButton3.visible = false;

		int index = 0;
		for (int i = 0; i < Math.min(4, this.handler.visibleAnswersList.size()); i++) {
			if (index == 0) {
				this.answerButton0.visible = true;
			} else if (index == 1) {
				this.answerButton1.visible = true;
			} else if (index == 2) {
				this.answerButton2.visible = true;
			} else if (index == 3) {
				this.answerButton3.visible = true;
			}
			index++;
		}
		this.dialogueTextScrollPosition = 0;
		this.dialogueTextScrollAmount = 0.0f;
		this.answersScrollPosition = 0;
		this.answersScrollAmount = 0.0f;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		int number = this.dialogueTextScrollPosition;
		float number1 = this.dialogueTextScrollAmount;
		int number2 = this.answersScrollPosition;
		float number3 = this.answersScrollAmount;
		this.init(client, width, height);
		this.dialogueTextScrollPosition = number;
		this.dialogueTextScrollAmount = number1;
		this.answersScrollPosition = number2;
		this.answersScrollAmount = number3;
		this.updateWidgets();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.dialogueTextMouseClicked = false;
		this.answersMouseClicked = false;
		if (this.handler.dialogueTextList.size() > 7) {
			int i = this.x + this.backgroundWidth - 14;
			int j = this.y + 8;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 87)) {
				this.dialogueTextMouseClicked = true;
			}
		}
		if (this.handler.visibleAnswersList.size() > 4) {
			int i = this.x + this.backgroundWidth - 14;
			int j = this.y + 99;
			if (mouseX >= (double) i && mouseX < (double) (i + 6) && mouseY >= (double) j && mouseY < (double) (j + 90)) {
				this.answersMouseClicked = true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.handler.dialogueTextList.size() > 7
				&& this.dialogueTextMouseClicked) {
			int i = this.handler.dialogueTextList.size() - 7;
			float f = (float) deltaY / (float) i;
			this.dialogueTextScrollAmount = MathHelper.clamp(this.dialogueTextScrollAmount + f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
		}
		if (this.handler.visibleAnswersList.size() > 4
				&& this.answersMouseClicked) {
			int i = this.handler.visibleAnswersList.size() - 4;
			float f = (float) deltaY / (float) i;
			this.answersScrollAmount = MathHelper.clamp(this.answersScrollAmount + f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.handler.dialogueTextList.size() > 7
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= (double) (this.y + 7) && mouseY <= (double) (this.y + 94)) {
			int i = this.handler.dialogueTextList.size() - 7;
			float f = (float) verticalAmount / (float) i;
			this.dialogueTextScrollAmount = MathHelper.clamp(this.dialogueTextScrollAmount - f, 0.0f, 1.0f);
			this.dialogueTextScrollPosition = (int) ((double) (this.dialogueTextScrollAmount * (float) i));
		}
		if (this.handler.visibleAnswersList.size() > 4
				&& mouseX >= (double) (this.x + 7) && mouseX <= (double) (this.x + this.backgroundWidth - 7)
				&& mouseY >= (double) (this.y + 98) && mouseY <= (double) (this.y + 190)) {
			int i = this.handler.visibleAnswersList.size() - 4;
			float f = (float) verticalAmount / (float) i;
			this.answersScrollAmount = MathHelper.clamp(this.answersScrollAmount - f, 0.0f, 1.0f);
			this.answersScrollPosition = (int) ((double) (this.answersScrollAmount * (float) i));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		this.drawBackground(context, delta, mouseX, mouseY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {

		super.render(context, mouseX, mouseY, delta);

		for (int i = this.dialogueTextScrollPosition; i < Math.min(this.dialogueTextScrollPosition + 7, this.handler.dialogueTextList.size()); i++) {
			String text = this.handler.dialogueTextList.get(i);
			context.drawText(this.textRenderer, Text.translatable(text), this.x + 8, this.y + 7 + ((i - this.dialogueTextScrollPosition) * 13), 0x404040, false);
		}
		if (this.handler.dialogueTextList.size() > 7) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_87_TEXTURE, this.x + this.backgroundWidth - 15, this.y + 7, 8, 87);
			int k = (int) (78.0f * this.dialogueTextScrollAmount);
			context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.x + this.backgroundWidth - 14, this.y + 7 + 1 + k, 6, 7);
		}
		int index = 0;
		for (int i = this.answersScrollPosition; i < Math.min(this.answersScrollPosition + 4, this.handler.visibleAnswersList.size()); i++) {
			String text = "";

			DialogueAnswer dialogueAnswer = null;
			if (this.handler.world != null) {
				Optional<RegistryEntry.Reference<DialogueAnswer>> optionalDialogueAnswerReference = this.handler.world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY).getEntry(Identifier.of(this.handler.visibleAnswersList.get(i)));
				if (optionalDialogueAnswerReference.isPresent()) {
					dialogueAnswer = optionalDialogueAnswerReference.get().value();
				}
			}

			if (dialogueAnswer != null) {
				text = dialogueAnswer.answerText();
			}
			if (index == 0) {
				this.answerButton0.setMessage(Text.translatable(text));
			} else if (index == 1) {
				this.answerButton1.setMessage(Text.translatable(text));
			} else if (index == 2) {
				this.answerButton2.setMessage(Text.translatable(text));
			} else if (index == 3) {
				this.answerButton3.setMessage(Text.translatable(text));
			}
			index++;
		}
		if (this.handler.visibleAnswersList.size() > 4) {
			context.drawGuiTexture(SCROLL_BAR_BACKGROUND_8_92_TEXTURE, this.x + this.backgroundWidth - 15, this.y + 98, 8, 92);
			int k = (int) (83.0f * this.answersScrollAmount);
			context.drawGuiTexture(SCROLLER_VERTICAL_6_7_TEXTURE, this.x + this.backgroundWidth - 14, this.y + 98 + 1 + k, 6, 7);
		}
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int i = this.x;
		int j = this.y;
		context.drawTexture(BACKGROUND_218_197_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, this.backgroundWidth, this.backgroundHeight);

	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.handler.dialogue == null || this.handler.dialogue.cancellable();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
