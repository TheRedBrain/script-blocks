package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateStatusEffectManipulationBlockPacket(
		BlockPos statusEffectManipulationBlockPosition,
		String manipulationMode,
		String effectIdentifier,
		int effectAmplifier,
		int effectDuration,
		boolean effectIsAmbient,
		boolean effectShowsParticles,
		boolean effectShowsIcon,
		String manipulatedEffectTagIdentifierString,
		int amplifierModification,
		BlockPos uuidListHandlerPositionOffset
) implements CustomPayload {
	public static final Id<UpdateStatusEffectManipulationBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_status_effect_manipulation_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateStatusEffectManipulationBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateStatusEffectManipulationBlockPacket::write, UpdateStatusEffectManipulationBlockPacket::new);

	public UpdateStatusEffectManipulationBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.statusEffectManipulationBlockPosition);
		registryByteBuf.writeString(this.manipulationMode);
		registryByteBuf.writeString(this.effectIdentifier);
		registryByteBuf.writeInt(this.effectAmplifier);
		registryByteBuf.writeInt(this.effectDuration);
		registryByteBuf.writeBoolean(this.effectIsAmbient);
		registryByteBuf.writeBoolean(this.effectShowsParticles);
		registryByteBuf.writeBoolean(this.effectShowsIcon);
		registryByteBuf.writeString(this.manipulatedEffectTagIdentifierString);
		registryByteBuf.writeInt(this.amplifierModification);
		registryByteBuf.writeBlockPos(this.uuidListHandlerPositionOffset);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
