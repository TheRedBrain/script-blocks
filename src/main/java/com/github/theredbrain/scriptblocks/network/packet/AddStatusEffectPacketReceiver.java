package com.github.theredbrain.scriptblocks.network.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AddStatusEffectPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<AddStatusEffectPacket> {

	@Override
	public void receive(AddStatusEffectPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		Identifier effectId = payload.effectId();
		int duration = payload.duration();
		int amplifier = payload.amplifier();
		boolean ambient = payload.ambient();
		boolean showParticles = payload.showParticles();
		boolean showIcon = payload.showIcon();
		boolean toggle = payload.toggle();

		StatusEffect statusEffect = Registries.STATUS_EFFECT.get(effectId);

		if (statusEffect != null) {
//            if (serverPlayerEntity.hasStatusEffect(statusEffect)) {
//                serverPlayerEntity.removeStatusEffect(statusEffect);
//            }
//            if (!toggle) {
//                serverPlayerEntity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon));
//            }
			if (toggle && serverPlayerEntity.hasStatusEffect(statusEffect)) {
				serverPlayerEntity.removeStatusEffect(statusEffect);
			} else if (serverPlayerEntity.hasStatusEffect(statusEffect)) {
				serverPlayerEntity.removeStatusEffect(statusEffect);
				serverPlayerEntity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon));
			} else {
				serverPlayerEntity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon));
			}
		}
	}
}
