package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateStatusEffectManipulationBlockPacket(
		BlockPos statusEffectManipulationBlockPosition,
		StatusEffectManipulationBlockEntity.ManipulationMode manipulationMode,
		String effectIdentifier,
		int effectAmplifier,
		int effectDuration,
		boolean effectIsAmbient,
		boolean effectShowsParticles,
		boolean effectShowsIcon,
		String manipulatedEffectTagIdentifierString,
		int amplifierModification,
		BlockPos uuidListHandlerPositionOffset
) implements CustomPacketPayload {
	public static final Type<UpdateStatusEffectManipulationBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_status_effect_manipulation_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateStatusEffectManipulationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateStatusEffectManipulationBlockPacket::write, UpdateStatusEffectManipulationBlockPacket::new);

	public UpdateStatusEffectManipulationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readEnum(StatusEffectManipulationBlockEntity.ManipulationMode.class),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.statusEffectManipulationBlockPosition);
		registryByteBuf.writeEnum(this.manipulationMode);
		registryByteBuf.writeUtf(this.effectIdentifier);
		registryByteBuf.writeInt(this.effectAmplifier);
		registryByteBuf.writeInt(this.effectDuration);
		registryByteBuf.writeBoolean(this.effectIsAmbient);
		registryByteBuf.writeBoolean(this.effectShowsParticles);
		registryByteBuf.writeBoolean(this.effectShowsIcon);
		registryByteBuf.writeUtf(this.manipulatedEffectTagIdentifierString);
		registryByteBuf.writeInt(this.amplifierModification);
		registryByteBuf.writeBlockPos(this.uuidListHandlerPositionOffset);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
