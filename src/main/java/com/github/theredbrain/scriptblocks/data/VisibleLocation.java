package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record VisibleLocation(
		String identifier,
		String entrance,
		String displayName,
		String entranceDisplayName,
		boolean isEntranceNameShownFirst,
		String dataIdentifier,
		String dataValue,
		boolean isUnlocked,
		boolean allowRegeneration
) {
	public static final VisibleLocation DEFAULT = new VisibleLocation("", "", "", "", false, "", "", true, true/*, 0L, 0L*/);
	public static final Codec<VisibleLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("identifier").forGetter(x -> x.identifier),
			Codec.STRING.fieldOf("entrance").forGetter(x -> x.entrance),
			Codec.STRING.fieldOf("display_name").forGetter(x -> x.displayName),
			Codec.STRING.fieldOf("entrance_display_name").forGetter(x -> x.entranceDisplayName),
			Codec.BOOL.fieldOf("is_entrance_name_shown_first").forGetter(x -> x.isEntranceNameShownFirst),
			Codec.STRING.fieldOf("data_identifier").forGetter(x -> x.dataIdentifier),
			Codec.STRING.fieldOf("data_value").forGetter(x -> x.dataValue),
			Codec.BOOL.fieldOf("is_unlocked").forGetter(x -> x.isUnlocked),
			Codec.BOOL.fieldOf("allow_regeneration").forGetter(x -> x.allowRegeneration)
	).apply(instance, VisibleLocation::new));

	public static final PacketCodec<ByteBuf, VisibleLocation> PACKET_CODEC = new PacketCodec<>() {
		public VisibleLocation decode(ByteBuf byteBuf) {
			return new VisibleLocation(
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf),
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.STRING.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf)
			);
		}

		public void encode(ByteBuf byteBuf, VisibleLocation visibleLocation) {
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.identifier);
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.entrance);
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.displayName);
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.entranceDisplayName);
			PacketCodecs.BOOL.encode(byteBuf, visibleLocation.isEntranceNameShownFirst);
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.dataIdentifier);
			PacketCodecs.STRING.encode(byteBuf, visibleLocation.dataValue);
			PacketCodecs.BOOL.encode(byteBuf, visibleLocation.isUnlocked);
			PacketCodecs.BOOL.encode(byteBuf, visibleLocation.allowRegeneration);
		}
	};

	public VisibleLocation(VisibleLocation visibleLocation) {
		this(
				visibleLocation.identifier(),
				visibleLocation.entrance(),
				visibleLocation.displayName(),
				visibleLocation.entranceDisplayName(),
				visibleLocation.isEntranceNameShownFirst(),
				visibleLocation.dataIdentifier(),
				visibleLocation.dataValue(),
				visibleLocation.isUnlocked(),
				visibleLocation.allowRegeneration()
		);
	}

}
