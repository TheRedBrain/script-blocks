package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.LootableVaultBlock;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultClientData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultServerData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultSharedData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultState;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Deprecated
public class LootableVaultBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final LootableVaultServerData serverData = new LootableVaultServerData();
	private final LootableVaultSharedData sharedData = new LootableVaultSharedData();
	private final LootableVaultClientData clientData = new LootableVaultClientData();
	//	private LootableVaultConfig config = LootableVaultConfig.DEFAULT;
	private String lootableVaultConfigIdentifier = "";

	public LootableVaultBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.LOOTABLE_VAULT_BLOCK_ENTITY, pos, state);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return Util.make(new CompoundTag(), tag -> tag.store("shared_data", LootableVaultSharedData.CODEC, registries.createSerializationContext(NbtOps.INSTANCE), this.sharedData));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
//		if (!this.lootableVaultConfigIdentifier.isEmpty()) {
		output.putString("config_identifier", this.lootableVaultConfigIdentifier);
//		}
//		view.put("config", encodeValue(LootableVaultConfig.CODEC, this.config, registryLookup));
		output.store("shared_data", LootableVaultSharedData.CODEC, this.sharedData);
		output.store("server_data", LootableVaultServerData.CODEC, this.serverData);

		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Lootable Vault Block detected at: " + this.worldPosition.toString() + ". This block will be removed in the future and should be replaced with the corresponding block from the 'Lootable Blocks' mod.", this.level != null ? this.level.getServer() : null);
		super.saveAdditional(output);
	}

	private static <T> Tag encodeValue(Codec<T> codec, T value, HolderLookup.Provider registries) {
		return codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), value).getOrThrow();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
//		DynamicOps<Tag> dynamicOps = registryLookup.getOps(NbtOps.INSTANCE);
//		if (nbt.contains("server_data")) {
//			LootableVaultServerData.CODEC.parse(dynamicOps, view.get("server_data")).resultOrPartial(LOGGER::error).ifPresent(this.serverData::copyFrom);
//		}
		input.read("server_data", LootableVaultServerData.CODEC).ifPresent(this.serverData::set);

//		if (nbt.contains("config")) {
//			LootableVaultConfig.CODEC.parse(dynamicOps, view.get("config")).resultOrPartial(LOGGER::error).ifPresent(config -> this.config = config);
//		}

//		if (nbt.contains("config_identifier")) {
		this.lootableVaultConfigIdentifier = input.getStringOr("config_identifier", "");
//		}

//		if (nbt.contains("shared_data")) {
//			LootableVaultSharedData.CODEC.parse(dynamicOps, input.get("shared_data")).resultOrPartial(LOGGER::error).ifPresent(this.sharedData::copyFrom);
//		}
		input.read("shared_data", LootableVaultSharedData.CODEC).ifPresent(this.sharedData::set);

		super.loadAdditional(input);
	}

//	public boolean isOminous() {
//		if (this.world != null) {
//			BlockState state = this.world.getBlockState(this.pos);
//			if (state.isOf(BlockRegistry.LOOTABLE_VAULT_BLOCK)) {
//				return state.get(LootableVaultBlock.OMINOUS);
//			}
//		}
//		return false;
//	}

	@Nullable
	public LootableVaultServerData getServerData() {
		return this.level != null && !this.level.isClientSide() ? this.serverData : null;
	}

	public LootableVaultSharedData getSharedData() {
		return this.sharedData;
	}

	public LootableVaultClientData getClientData() {
		return this.clientData;
	}

	public void markAsRewarded(ServerPlayer serverPlayerEntity, ItemStack itemStack) {
		if (this.level instanceof ServerLevel serverWorld) {
			LootableVaultServerData serverData = this.getServerData();
			LootableVaultSharedData sharedData = this.getSharedData();
			LootableVaultConfig config = this.getConfig(serverWorld);
			if (serverData != null && sharedData != null) {
//				ScriptBlocks.info("loot supplied");
				serverPlayerEntity.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
				itemStack.consume(config.keyItem().getCount(), serverPlayerEntity);
				Server.unlock(serverWorld, serverWorld.getBlockState(this.worldPosition), this.worldPosition, config, serverData, sharedData);
				serverData.markPlayerAsRewarded(serverPlayerEntity);
				sharedData.updateConnectedPlayers(serverWorld, this.worldPosition, serverData, config, config.deactivationRange());
			}
		}
	}

//	public void unmarkAsRewarded(ServerPlayerEntity serverPlayerEntity) {
//		if (this.world instanceof ServerWorld serverWorld) {
//			LootableVaultServerData serverData = this.getServerData();
//			LootableVaultSharedData sharedData = this.getSharedData();
//			LootableVaultConfig config = this.getConfig(serverWorld);
//			if (serverData != null && sharedData != null) {
//				serverData.unmarkPlayerAsRewarded(serverPlayerEntity);
//				sharedData.updateConnectedPlayers(serverWorld, this.pos, serverData, config, config.deactivationRange());
//			}
//		}
//	}

//	public String getConfigId() {
//		ScriptBlocks.info("getConfigId: " + this.lootableVaultConfigIdentifier);
//		return this.lootableVaultConfigIdentifier;
//	}
//
//	public void setConfigId(String lootableVaultConfigIdentifier) {
//		ScriptBlocks.info("setConfigId: " + lootableVaultConfigIdentifier);
//		this.lootableVaultConfigIdentifier = lootableVaultConfigIdentifier;
//	}

	public LootableVaultConfig getConfig(Level world) {
//		return this.config;
		Optional<Holder.Reference<LootableVaultConfig>> lootableVaultConfig = world.registryAccess().get(ScriptBlocksDynamicRegistries.LOOTABLE_VAULT_CONFIG_REGISTRY_KEY).get().value().get(Identifier.parse(this.lootableVaultConfigIdentifier));
		if (lootableVaultConfig.isPresent()) {
			return lootableVaultConfig.get().value();
		}
		DebuggingHelper.sendLootableVaultLogMessage("Config not valid: " + this.lootableVaultConfigIdentifier, null);
		return LootableVaultConfig.DEFAULT;
//		return LootableVaultConfigsRegistry.registeredLootableVaultConfigs.getOrDefault(Identifier.of(this.lootableVaultConfigIdentifier), LootableVaultConfig.DEFAULT);
//		return LootableVaultConfigsRegistry.entry(this.world, this.lootableVaultConfigIdentifier).value();
	}

//	@VisibleForTesting
//	public void setConfig(LootableVaultConfig config) {
//		this.config = config;
//	}

	public static final class Client {
		private static final int field_48870 = 20;
		private static final float field_48871 = 0.5F;
		private static final float field_48872 = 0.02F;
		private static final int field_48873 = 20;
		private static final int field_48874 = 20;

		public static void tick(Level world, BlockPos pos, BlockState state, LootableVaultClientData clientData, LootableVaultSharedData sharedData) {
			clientData.rotateDisplay();
			if (world.getGameTime() % 20L == 0L) {
				spawnConnectedParticles(world, pos, state, sharedData);
			}

			spawnAmbientParticles(world, pos, sharedData, state.getValue(VaultBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
			playAmbientSound(world, pos, sharedData);
		}

		public static void spawnActivateParticles(Level world, BlockPos pos, BlockState state, LootableVaultSharedData sharedData, ParticleOptions particle) {
			spawnConnectedParticles(world, pos, state, sharedData);
			RandomSource random = world.getRandom();

			for (int i = 0; i < 20; i++) {
				Vec3 vec3d = getRegularParticlesPos(pos, random);
				world.addParticle(ParticleTypes.SMOKE, vec3d.x(), vec3d.y(), vec3d.z(), 0.0, 0.0, 0.0);
				world.addParticle(particle, vec3d.x(), vec3d.y(), vec3d.z(), 0.0, 0.0, 0.0);
			}
		}

		public static void spawnDeactivateParticles(Level world, BlockPos pos, ParticleOptions particle) {
			RandomSource random = world.getRandom();

			for (int i = 0; i < 20; i++) {
				Vec3 vec3d = getDeactivateParticlesPos(pos, random);
				Vec3 vec3d2 = new Vec3(random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
				world.addParticle(particle, vec3d.x(), vec3d.y(), vec3d.z(), vec3d2.x(), vec3d2.y(), vec3d2.z());
			}
		}

		private static void spawnAmbientParticles(Level world, BlockPos pos, LootableVaultSharedData sharedData, ParticleOptions particle) {
			RandomSource random = world.getRandom();
			if (random.nextFloat() <= 0.5F) {
				Vec3 vec3d = getRegularParticlesPos(pos, random);
				world.addParticle(ParticleTypes.SMOKE, vec3d.x(), vec3d.y(), vec3d.z(), 0.0, 0.0, 0.0);
				if (hasDisplayItem(sharedData)) {
					world.addParticle(particle, vec3d.x(), vec3d.y(), vec3d.z(), 0.0, 0.0, 0.0);
				}
			}
		}

		private static void spawnConnectedParticlesFor(Level world, Vec3 pos, Player player) {
			RandomSource random = world.getRandom();
			Vec3 vec3d = pos.vectorTo(player.position().add(0.0, (double) (player.getBbHeight() / 2.0F), 0.0));
			int i = Mth.nextInt(random, 2, 5);

			for (int j = 0; j < i; j++) {
				Vec3 vec3d2 = vec3d.offsetRandom(random, 1.0F);
				world.addParticle(ParticleTypes.VAULT_CONNECTION, pos.x(), pos.y(), pos.z(), vec3d2.x(), vec3d2.y(), vec3d2.z());
			}
		}

		private static void spawnConnectedParticles(Level world, BlockPos pos, BlockState state, LootableVaultSharedData sharedData) {
			Set<UUID> set = sharedData.getConnectedPlayers();
			if (!set.isEmpty()) {
				Vec3 vec3d = getConnectedParticlesOrigin(pos, state.getValue(VaultBlock.FACING));

				for (UUID uUID : set) {
					Player playerEntity = world.getPlayerByUUID(uUID);
					if (playerEntity != null && isPlayerWithinConnectedParticlesRange(pos, sharedData, playerEntity)) {
						spawnConnectedParticlesFor(world, vec3d, playerEntity);
					}
				}
			}
		}

		private static boolean isPlayerWithinConnectedParticlesRange(BlockPos pos, LootableVaultSharedData sharedData, Player player) {
			return player.blockPosition().distSqr(pos) <= Mth.square(sharedData.getConnectedParticlesRange());
		}

		private static void playAmbientSound(Level world, BlockPos pos, LootableVaultSharedData sharedData) {
			if (hasDisplayItem(sharedData)) {
				RandomSource random = world.getRandom();
				if (random.nextFloat() <= 0.02F) {
					world.playLocalSound(
							pos, SoundEvents.VAULT_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false
					);
				}
			}
		}

		public static boolean hasDisplayItem(LootableVaultSharedData sharedData) {
			return sharedData.hasDisplayItem();
		}

		private static Vec3 getDeactivateParticlesPos(BlockPos pos, RandomSource random) {
			return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.4, 0.6), Mth.nextDouble(random, 0.4, 0.6), Mth.nextDouble(random, 0.4, 0.6));
		}

		private static Vec3 getRegularParticlesPos(BlockPos pos, RandomSource random) {
			return Vec3.atLowerCornerOf(pos).add(Mth.nextDouble(random, 0.1, 0.9), Mth.nextDouble(random, 0.25, 0.75), Mth.nextDouble(random, 0.1, 0.9));
		}

		private static Vec3 getConnectedParticlesOrigin(BlockPos pos, Direction direction) {
			return Vec3.atBottomCenterOf(pos).add((double) direction.getStepX() * 0.5, 1.75, (double) direction.getStepZ() * 0.5);
		}
	}

	public static final class Server {
		private static final int UNLOCK_TIME = 14;
		private static final int DISPLAY_UPDATE_INTERVAL = 20;
		private static final int FAILED_UNLOCK_COOLDOWN = 15;

		public static void tick(ServerLevel world, BlockPos pos, BlockState state, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData) {
			LootableVaultState vaultState = state.getValue(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			if (shouldUpdateDisplayItem(world.getGameTime(), vaultState)) {
				updateDisplayItem(world, vaultState, config, sharedData, pos);
			}

			BlockState blockState = state;
			if (world.getGameTime() >= serverData.getStateUpdatingResumeTime()) {
				blockState = state.setValue(LootableVaultBlock.LOOTABLE_VAULT_STATE, vaultState.update(world, pos, config, serverData, sharedData));
				if (!state.equals(blockState)) {
					changeVaultState(world, pos, state, blockState, config, sharedData);
				}
			}

			if (serverData.isDirty() || sharedData.isDirty()) {
				LootableVaultBlockEntity.setChanged(world, pos, state);
				if (sharedData.isDirty()) {
					world.sendBlockUpdated(pos, state, blockState, Block.UPDATE_CLIENTS);
				}

				serverData.markClean();
				sharedData.markClean();
			}
		}

		public static void tryUnlock(
				ServerLevel world,
				BlockPos pos,
				BlockState state,
				LootableVaultConfig config,
				LootableVaultServerData serverData,
				LootableVaultSharedData sharedData,
				Player player,
				ItemStack stack
		) {
			LootableVaultState vaultState = state.getValue(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			if (canBeUnlocked(config, vaultState)) {
				if (!isValidKey(config, stack)) {
					ScriptBlocks.info("invalid key");
					playFailedUnlockSound(world, serverData, pos, SoundEvents.VAULT_INSERT_ITEM_FAIL);
				} else if (serverData.hasRewardedPlayer(player)) {
					ScriptBlocks.info("player already rewarded");
					playFailedUnlockSound(world, serverData, pos, SoundEvents.VAULT_REJECT_REWARDED_PLAYER);
				} else if (player instanceof ServerPlayer serverPlayerEntity) {
					// TODO loot
//					Vec3d lootPos = new Vec3d(this.getPos().getX(), interactiveLootBlockEntity.getPos().getY(), interactiveLootBlockEntity.getPos().getZ());
//					boolean lootSupplied = ScriptBlocks.supplyLootableLoot(Identifier.of(config.lootableIdentifier()), serverPlayerEntity, Vec3d.of(pos), config.rolls(), config.choices(), config.withChoice());

//					List<ItemStack> list = generateLoot(world, config, pos, player);
					/*if (*/
					ScriptBlocks.supplyLootableLoot(Identifier.parse(config.lootableIdentifier()), world, serverPlayerEntity, Vec3.atLowerCornerOf(pos), config.rolls(), config.choices(), config.withChoice(), stack);/*) {
						ScriptBlocks.info("loot supplied");
						player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
						stack.decrementUnlessCreative(config.keyItem().getCount(), player);
						unlock(world, state, pos, config, serverData, sharedData);
					}*/
					serverData.markPlayerAsRewarded(player);
					sharedData.updateConnectedPlayers(world, pos, serverData, config, config.deactivationRange());
				}
			}
		}

		static void changeVaultState(ServerLevel world, BlockPos pos, BlockState oldState, BlockState newState, LootableVaultConfig config, LootableVaultSharedData sharedData) {
			LootableVaultState vaultState = oldState.getValue(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			LootableVaultState vaultState2 = newState.getValue(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			world.setBlock(pos, newState, Block.UPDATE_ALL);
			vaultState.onStateChange(world, pos, vaultState2, config, sharedData, (Boolean) newState.getValue(VaultBlock.OMINOUS));
		}

		public static void updateDisplayItem(ServerLevel world, LootableVaultState state, LootableVaultConfig config, LootableVaultSharedData sharedData, BlockPos pos) {
			if (!canBeUnlocked(config, state)) {
				sharedData.setDisplayItem(ItemStack.EMPTY);
			} else {
				ItemStack itemStack = ItemStack.EMPTY;
				if (!config.displayLootTableIdentifier().isEmpty()) {
					itemStack = getRandomDisplayItemFromLootTable(world, pos, ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(config.displayLootTableIdentifier())));
				}
				sharedData.setDisplayItem(itemStack);
			}
		}

		private static ItemStack getRandomDisplayItemFromLootTable(final ServerLevel serverLevel, final BlockPos pos, final ResourceKey<LootTable> lootTableId) {
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableId);
			LootParams params = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.VAULT);
			List<ItemStack> list = lootTable.getRandomItems(params);
			return list.isEmpty() ? ItemStack.EMPTY : Util.getRandom(list, serverLevel.getRandom());
		}

		public static void unlock(
				ServerLevel world, BlockState state, BlockPos pos, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData
		) {
//			serverData.setItemsToEject(itemsToEject);
			sharedData.setDisplayItem(ItemStack.EMPTY);
			serverData.setStateUpdatingResumeTime(world.getGameTime() + 14L);
			changeVaultState(world, pos, state, state.setValue(LootableVaultBlock.LOOTABLE_VAULT_STATE, LootableVaultState.UNLOCKING), config, sharedData);
		}

//		private static List<ItemStack> generateLoot(ServerWorld world, LootableVaultConfig config, BlockPos pos, PlayerEntity player) {
//			LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(config.lootTable());
//			LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(world)
//					.add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
//					.luck(player.getLuck())
//					.add(LootContextParameters.THIS_ENTITY, player)
//					.build(LootContextTypes.VAULT);
//			return lootTable.generateLoot(lootContextParameterSet);
//		}

		private static boolean canBeUnlocked(LootableVaultConfig config, LootableVaultState state) {
			return !Objects.equals(config.lootableIdentifier(), "") && !config.keyItem().isEmpty() && state != LootableVaultState.INACTIVE;
		}

		private static boolean isValidKey(LootableVaultConfig config, ItemStack stack) {
			return ItemStack.isSameItemSameComponents(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
		}

		private static boolean shouldUpdateDisplayItem(long time, LootableVaultState state) {
			return time % 20L == 0L && state == LootableVaultState.ACTIVE;
		}

		private static void playFailedUnlockSound(ServerLevel world, LootableVaultServerData serverData, BlockPos pos, SoundEvent sound) {
			if (world.getGameTime() >= serverData.getLastFailedUnlockTime() + 15L) {
				world.playSound(null, pos, sound, SoundSource.BLOCKS);
				serverData.setLastFailedUnlockTime(world.getGameTime());
			}
		}
	}
}
