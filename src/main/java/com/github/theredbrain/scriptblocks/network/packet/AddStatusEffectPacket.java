package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AddStatusEffectPacket(Identifier effectId, int duration, int amplifier, boolean ambient,
									boolean showParticles, boolean showIcon, boolean toggle) implements CustomPayload {
	public static final CustomPayload.Id<AddStatusEffectPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("add_status_effect"));
	public static final PacketCodec<RegistryByteBuf, AddStatusEffectPacket> PACKET_CODEC = PacketCodec.of(AddStatusEffectPacket::write, AddStatusEffectPacket::new);

	public AddStatusEffectPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readIdentifier(), registryByteBuf.readInt(), registryByteBuf.readInt(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean(), registryByteBuf.readBoolean());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.effectId);
		registryByteBuf.writeInt(this.duration);
		registryByteBuf.writeInt(this.amplifier);
		registryByteBuf.writeBoolean(this.ambient);
		registryByteBuf.writeBoolean(this.showParticles);
		registryByteBuf.writeBoolean(this.showIcon);
		registryByteBuf.writeBoolean(this.toggle);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
