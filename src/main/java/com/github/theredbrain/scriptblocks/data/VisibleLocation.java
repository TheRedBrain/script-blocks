package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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

	public static final StreamCodec<ByteBuf, VisibleLocation> PACKET_CODEC = new StreamCodec<>() {
		public VisibleLocation decode(ByteBuf byteBuf) {
			return new VisibleLocation(
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf),
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					ByteBufCodecs.STRING_UTF8.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf),
					PacketCodecs.BOOL.decode(byteBuf)
			);
		}

		public void encode(ByteBuf byteBuf, VisibleLocation visibleLocation) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.identifier);
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.entrance);
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.displayName);
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.entranceDisplayName);
			PacketCodecs.BOOL.encode(byteBuf, visibleLocation.isEntranceNameShownFirst);
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.dataIdentifier);
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, visibleLocation.dataValue);
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
