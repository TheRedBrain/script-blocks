package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PVPArenaSettings(
		String default_respawn_pos,
		Map<String, TeamSettings> team_settings
) {

	public PVPArenaSettings(
			String default_respawn_pos,
			Map<String, TeamSettings> team_settings
	) {
		this.default_respawn_pos = default_respawn_pos != null ? default_respawn_pos : "";
		this.team_settings = team_settings != null ? team_settings : new HashMap<>();
	}

	public static final Codec<PVPArenaSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("default_respawn_pos", "").forGetter(x -> x.default_respawn_pos),
			Codec.unboundedMap(Codec.STRING, TeamSettings.CODEC).optionalFieldOf("team_settings", new HashMap<>()).forGetter(x -> x.team_settings)
	).apply(instance, PVPArenaSettings::new));

	public record TeamSettings(
			String battle_respawn_pos,
			String end_of_battle_respawn_pos
	) {

		public static final Codec<TeamSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("battle_respawn_pos", "").forGetter(x -> x.battle_respawn_pos),
				Codec.STRING.optionalFieldOf("end_of_battle_respawn_pos", "").forGetter(x -> x.end_of_battle_respawn_pos)
		).apply(instance, TeamSettings::new));

		public TeamSettings(
				String battle_respawn_pos,
				String end_of_battle_respawn_pos
		) {
			this.battle_respawn_pos = battle_respawn_pos != null ? battle_respawn_pos : "";
			this.end_of_battle_respawn_pos = end_of_battle_respawn_pos != null ? end_of_battle_respawn_pos : "";
		}
	}
}
