package com.github.theredbrain.scriptblocks.network.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.Optional;

public class AddStatusEffectPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<AddStatusEffectPacket> {

	@Override
	public void receive(AddStatusEffectPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		Identifier effectId = payload.effectId();
		int duration = payload.duration();
		int amplifier = payload.amplifier();
		boolean ambient = payload.ambient();
		boolean showParticles = payload.showParticles();
		boolean showIcon = payload.showIcon();
		boolean toggle = payload.toggle();

		Optional<Holder.Reference<MobEffect>> statusEffect = BuiltInRegistries.MOB_EFFECT.get(effectId);
		if (statusEffect.isPresent()) {
//            if (serverPlayerEntity.hasStatusEffect(statusEffect)) {
//                serverPlayerEntity.removeStatusEffect(statusEffect);
//            }
//            if (!toggle) {
//                serverPlayerEntity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon));
//            }
			if (toggle && serverPlayerEntity.hasEffect(statusEffect.get())) {
				serverPlayerEntity.removeEffect(statusEffect.get());
			} else if (serverPlayerEntity.hasEffect(statusEffect.get())) {
				serverPlayerEntity.removeEffect(statusEffect.get());
				serverPlayerEntity.addEffect(new MobEffectInstance(statusEffect.get(), duration, amplifier, ambient, showParticles, showIcon));
			} else {
				serverPlayerEntity.addEffect(new MobEffectInstance(statusEffect.get(), duration, amplifier, ambient, showParticles, showIcon));
			}
		}
	}
}
