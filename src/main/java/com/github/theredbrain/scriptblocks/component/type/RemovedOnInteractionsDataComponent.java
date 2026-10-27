package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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
	public static final StreamCodec<RegistryFriendlyByteBuf, RemovedOnInteractionsDataComponent> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
			RemovedOnInteractionsDataComponent::identifier_list,
			RemovedOnInteractionsDataComponent::new
	);

	public RemovedOnInteractionsDataComponent(
			List<String> identifier_list
	) {
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
