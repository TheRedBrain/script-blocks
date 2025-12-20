package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record RemovedOnTeleportComponent(
		List<String> identifier_list
) {
	public static final RemovedOnTeleportComponent DEFAULT = new RemovedOnTeleportComponent(new ArrayList<>());
	public static final Codec<RemovedOnTeleportComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.STRING.listOf().fieldOf("identifier_list").forGetter(RemovedOnTeleportComponent::identifier_list)
					)
					.apply(instance, RemovedOnTeleportComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, RemovedOnTeleportComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.STRING.collect(PacketCodecs.toList()),
			RemovedOnTeleportComponent::identifier_list,
			RemovedOnTeleportComponent::new
	);

	public RemovedOnTeleportComponent(
			List<String> identifier_list
	) {
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
