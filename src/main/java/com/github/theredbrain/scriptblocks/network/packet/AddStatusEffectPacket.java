package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AddStatusEffectPacket(
		Identifier effectId,
		int duration,
		int amplifier,
		boolean ambient,
		boolean showParticles,
		boolean showIcon,
		boolean toggle
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AddStatusEffectPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("add_status_effect"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AddStatusEffectPacket> PACKET_CODEC = StreamCodec.ofMember(AddStatusEffectPacket::write, AddStatusEffectPacket::new);

	public AddStatusEffectPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(registryByteBuf.readIdentifier(), registryByteBuf.readInt(), registryByteBuf.readInt(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean());
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.effectId);
		registryByteBuf.writeInt(this.duration);
		registryByteBuf.writeInt(this.amplifier);
		registryByteBuf.writeBoolean(this.ambient);
		registryByteBuf.writeBoolean(this.showParticles);
		registryByteBuf.writeBoolean(this.showIcon);
		registryByteBuf.writeBoolean(this.toggle);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
