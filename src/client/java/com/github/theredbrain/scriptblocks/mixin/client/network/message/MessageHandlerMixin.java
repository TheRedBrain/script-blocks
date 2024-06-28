package com.github.theredbrain.scriptblocks.mixin.client.network.message;

import com.github.theredbrain.scriptblocks.gui.hud.DuckInGameHudMixin;
import com.github.theredbrain.scriptblocks.network.message.DuckMessageHandlerMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Environment(value = EnvType.CLIENT)
@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin implements DuckMessageHandlerMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	protected abstract UUID extractSender(Text text);

	@Override
	public void scriptblocks$onAnnouncement(Text announcement) {
		if (!this.client.options.getHideMatchedNames().getValue() || !this.client.shouldBlockMessages(this.extractSender(announcement))) {
			((DuckInGameHudMixin) this.client.inGameHud).scriptblocks$setAnnouncementMessage(announcement);

			this.client.getNarratorManager().narrateSystemMessage(announcement);
		}
	}
}
