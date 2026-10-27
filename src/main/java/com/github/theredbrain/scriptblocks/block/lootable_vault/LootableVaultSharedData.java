package com.github.theredbrain.scriptblocks.block.lootable_vault;

import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;

@Deprecated
public class LootableVaultSharedData {
	static final String SHARED_DATA_KEY = "shared_data";
	public static Codec<LootableVaultSharedData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							ItemStack.lenientOptionalFieldOf("display_item").forGetter(data -> data.displayItem),
							UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("connected_players", Set.of()).forGetter(data -> data.connectedPlayers),
							Codec.DOUBLE
									.lenientOptionalFieldOf("connected_particles_range", Double.valueOf(LootableVaultConfig.DEFAULT.deactivationRange()))
									.forGetter(data -> data.connectedParticlesRange)
					)
					.apply(instance, LootableVaultSharedData::new)
	);
	private ItemStack displayItem = ItemStack.EMPTY;
	private Set<UUID> connectedPlayers = new ObjectLinkedOpenHashSet<>();
	private double connectedParticlesRange = LootableVaultConfig.DEFAULT.deactivationRange();
	boolean dirty;

	LootableVaultSharedData(ItemStack displayItem, Set<UUID> connectedPlayers, double connectedParticlesRange) {
		this.displayItem = displayItem;
		this.connectedPlayers.addAll(connectedPlayers);
		this.connectedParticlesRange = connectedParticlesRange;
	}

	public LootableVaultSharedData() {
	}

	public ItemStack getDisplayItem() {
		return this.displayItem;
	}

	public boolean hasDisplayItem() {
		return !this.displayItem.isEmpty();
	}

	public void setDisplayItem(ItemStack stack) {
		if (!ItemStack.matches(this.displayItem, stack)) {
			this.displayItem = stack.copy();
			this.markDirty();
		}
	}

	boolean hasConnectedPlayers() {
		return !this.connectedPlayers.isEmpty();
	}

	public Set<UUID> getConnectedPlayers() {
		return this.connectedPlayers;
	}

	public double getConnectedParticlesRange() {
		return this.connectedParticlesRange;
	}

	public void updateConnectedPlayers(ServerLevel world, BlockPos pos, LootableVaultServerData serverData, LootableVaultConfig config, double radius) {
		Set<UUID> set = (Set<UUID>) PlayerDetector.INCLUDING_CREATIVE_PLAYERS
				.detect(world, PlayerDetector.EntitySelector.SELECT_FROM_LEVEL, pos, radius, false)
				.stream()
				.filter(uuid -> !serverData.getRewardedPlayers().contains(uuid))
				.collect(Collectors.toSet());
		if (!this.connectedPlayers.equals(set)) {
			this.connectedPlayers = set;
			this.markDirty();
		}
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void markDirty() {
		this.dirty = true;
	}

	public void markClean() {
		this.dirty = false;
	}

	public void copyFrom(LootableVaultSharedData data) {
		this.displayItem = data.displayItem;
		this.connectedPlayers = data.connectedPlayers;
		this.connectedParticlesRange = data.connectedParticlesRange;
	}
}
