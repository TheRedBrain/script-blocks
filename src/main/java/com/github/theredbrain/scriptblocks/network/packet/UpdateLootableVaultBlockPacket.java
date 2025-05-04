package com.github.theredbrain.scriptblocks.network.packet;
//
//import com.github.theredbrain.scriptblocks.ScriptBlocks;
//import net.minecraft.network.RegistryByteBuf;
//import net.minecraft.network.codec.PacketCodec;
//import net.minecraft.network.packet.CustomPayload;
//import net.minecraft.util.math.BlockPos;
//
//public record UpdateLootableVaultBlockPacket(
//		BlockPos lootableVaultBlockPosition,
//		boolean isOminous,
//		String lootableVaultConfigIdentifier
//) implements CustomPayload {
//	public static final Id<UpdateLootableVaultBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_lootable_vault_block"));
//	public static final PacketCodec<RegistryByteBuf, UpdateLootableVaultBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateLootableVaultBlockPacket::write, UpdateLootableVaultBlockPacket::new);
//
//	public UpdateLootableVaultBlockPacket(RegistryByteBuf registryByteBuf) {
//		this(
//				registryByteBuf.readBlockPos(),
//				registryByteBuf.readBoolean(),
//				registryByteBuf.readString()
//		);
//	}
//
//	private void write(RegistryByteBuf registryByteBuf) {
//		registryByteBuf.writeBlockPos(this.lootableVaultBlockPosition);
//		registryByteBuf.writeBoolean(this.isOminous);
//		registryByteBuf.writeString(this.lootableVaultConfigIdentifier);
//	}
//
//	@Override
//	public Id<? extends CustomPayload> getId() {
//		return PACKET_ID;
//	}
//}
