package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record InteractiveKeyComponent(
		boolean is_consumed,
		List<Identifier> identifier_list
) {
	public static final InteractiveKeyComponent DEFAULT = new InteractiveKeyComponent(false, new ArrayList<>());
	public static final Codec<InteractiveKeyComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.BOOL.fieldOf("is_consumed").forGetter(InteractiveKeyComponent::is_consumed),
							Identifier.CODEC.listOf().fieldOf("identifier_list").forGetter(InteractiveKeyComponent::identifier_list)
					)
					.apply(instance, InteractiveKeyComponent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, InteractiveKeyComponent> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			InteractiveKeyComponent::is_consumed,
			Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
			InteractiveKeyComponent::identifier_list,
			InteractiveKeyComponent::new
	);

	public InteractiveKeyComponent(
			boolean is_consumed,
			List<Identifier> identifier_list
	) {
		this.is_consumed = is_consumed;
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
