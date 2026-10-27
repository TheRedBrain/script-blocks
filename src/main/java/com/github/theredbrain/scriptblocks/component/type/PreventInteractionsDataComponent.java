package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PreventInteractionsDataComponent(
		List<String> identifier_list
) {
	public static final PreventInteractionsDataComponent DEFAULT = new PreventInteractionsDataComponent(new ArrayList<>());
	public static final Codec<PreventInteractionsDataComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.STRING.listOf().fieldOf("identifier_list").forGetter(PreventInteractionsDataComponent::identifier_list)
					)
					.apply(instance, PreventInteractionsDataComponent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, PreventInteractionsDataComponent> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
			PreventInteractionsDataComponent::identifier_list,
			PreventInteractionsDataComponent::new
	);

	public PreventInteractionsDataComponent(
			List<String> identifier_list
	) {
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
