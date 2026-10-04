package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record RemovedOnInteractionsDataComponent(
		List<String> identifier_list
) {
	public static final RemovedOnInteractionsDataComponent DEFAULT = new RemovedOnInteractionsDataComponent(new ArrayList<>());
	public static final Codec<RemovedOnInteractionsDataComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.STRING.listOf().fieldOf("identifier_list").forGetter(RemovedOnInteractionsDataComponent::identifier_list)
					)
					.apply(instance, RemovedOnInteractionsDataComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, RemovedOnInteractionsDataComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.STRING.collect(PacketCodecs.toList()),
			RemovedOnInteractionsDataComponent::identifier_list,
			RemovedOnInteractionsDataComponent::new
	);

	public RemovedOnInteractionsDataComponent(
			List<String> identifier_list
	) {
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
