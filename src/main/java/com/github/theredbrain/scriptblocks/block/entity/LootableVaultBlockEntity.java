package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.LootableVaultBlock;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultClientData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultServerData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultSharedData;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultState;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class LootableVaultBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final LootableVaultServerData serverData = new LootableVaultServerData();
	private final LootableVaultSharedData sharedData = new LootableVaultSharedData();
	private final LootableVaultClientData clientData = new LootableVaultClientData();
	//	private LootableVaultConfig config = LootableVaultConfig.DEFAULT;
	private String lootableVaultConfigIdentifier = "";

	public LootableVaultBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.LOOTABLE_VAULT_BLOCK_ENTITY, pos, state);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return Util.make(new NbtCompound(), nbt -> nbt.put("shared_data", encodeValue(LootableVaultSharedData.CODEC, this.sharedData, registryLookup)));
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
//		if (!this.lootableVaultConfigIdentifier.isEmpty()) {
		nbt.putString("config_identifier", this.lootableVaultConfigIdentifier);
//		}
//		nbt.put("config", encodeValue(LootableVaultConfig.CODEC, this.config, registryLookup));
		nbt.put("shared_data", encodeValue(LootableVaultSharedData.CODEC, this.sharedData, registryLookup));
		nbt.put("server_data", encodeValue(LootableVaultServerData.CODEC, this.serverData, registryLookup));

		super.writeNbt(nbt, registryLookup);
	}

	private static <T> NbtElement encodeValue(Codec<T> codec, T value, RegistryWrapper.WrapperLookup registries) {
		return codec.encodeStart(registries.getOps(NbtOps.INSTANCE), value).getOrThrow();
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		DynamicOps<NbtElement> dynamicOps = registryLookup.getOps(NbtOps.INSTANCE);
		if (nbt.contains("server_data")) {
			LootableVaultServerData.CODEC.parse(dynamicOps, nbt.get("server_data")).resultOrPartial(LOGGER::error).ifPresent(this.serverData::copyFrom);
		}

//		if (nbt.contains("config")) {
//			LootableVaultConfig.CODEC.parse(dynamicOps, nbt.get("config")).resultOrPartial(LOGGER::error).ifPresent(config -> this.config = config);
//		}

//		if (nbt.contains("config_identifier")) {
		this.lootableVaultConfigIdentifier = nbt.getString("config_identifier");
//		}

		if (nbt.contains("shared_data")) {
			LootableVaultSharedData.CODEC.parse(dynamicOps, nbt.get("shared_data")).resultOrPartial(LOGGER::error).ifPresent(this.sharedData::copyFrom);
		}

		super.readNbt(nbt, registryLookup);
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
		return this.world != null && !this.world.isClient ? this.serverData : null;
	}

	public LootableVaultSharedData getSharedData() {
		return this.sharedData;
	}

	public LootableVaultClientData getClientData() {
		return this.clientData;
	}

	public void unmarkAsRewarded(ServerPlayerEntity serverPlayerEntity) {
		if (this.world instanceof ServerWorld serverWorld) {
			LootableVaultServerData serverData = this.getServerData();
			LootableVaultSharedData sharedData = this.getSharedData();
			LootableVaultConfig config = this.getConfig(serverWorld);
			if (serverData != null && sharedData != null) {
				serverData.unmarkPlayerAsRewarded(serverPlayerEntity);
				sharedData.updateConnectedPlayers(serverWorld, this.pos, serverData, config, config.deactivationRange());
			}
		}
	}

//	public String getConfigId() {
//		ScriptBlocks.info("getConfigId: " + this.lootableVaultConfigIdentifier);
//		return this.lootableVaultConfigIdentifier;
//	}
//
//	public void setConfigId(String lootableVaultConfigIdentifier) {
//		ScriptBlocks.info("setConfigId: " + lootableVaultConfigIdentifier);
//		this.lootableVaultConfigIdentifier = lootableVaultConfigIdentifier;
//	}

	public LootableVaultConfig getConfig(World world) {
//		return this.config;
		LootableVaultConfig lootableVaultConfig = world.getRegistryManager().get(CustomDynamicRegistries.LOOTABLE_VAULT_CONFIG_REGISTRY_KEY).get(Identifier.of(this.lootableVaultConfigIdentifier));
		if (lootableVaultConfig != null) {
			return lootableVaultConfig;
		}
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

		public static void tick(World world, BlockPos pos, BlockState state, LootableVaultClientData clientData, LootableVaultSharedData sharedData) {
			clientData.rotateDisplay();
			if (world.getTime() % 20L == 0L) {
				spawnConnectedParticles(world, pos, state, sharedData);
			}

			spawnAmbientParticles(world, pos, sharedData, state.get(VaultBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
			playAmbientSound(world, pos, sharedData);
		}

		public static void spawnActivateParticles(World world, BlockPos pos, BlockState state, LootableVaultSharedData sharedData, ParticleEffect particle) {
			spawnConnectedParticles(world, pos, state, sharedData);
			Random random = world.random;

			for (int i = 0; i < 20; i++) {
				Vec3d vec3d = getRegularParticlesPos(pos, random);
				world.addParticle(ParticleTypes.SMOKE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
				world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
			}
		}

		public static void spawnDeactivateParticles(World world, BlockPos pos, ParticleEffect particle) {
			Random random = world.random;

			for (int i = 0; i < 20; i++) {
				Vec3d vec3d = getDeactivateParticlesPos(pos, random);
				Vec3d vec3d2 = new Vec3d(random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
				world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ());
			}
		}

		private static void spawnAmbientParticles(World world, BlockPos pos, LootableVaultSharedData sharedData, ParticleEffect particle) {
			Random random = world.getRandom();
			if (random.nextFloat() <= 0.5F) {
				Vec3d vec3d = getRegularParticlesPos(pos, random);
				world.addParticle(ParticleTypes.SMOKE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
				if (hasDisplayItem(sharedData)) {
					world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
				}
			}
		}

		private static void spawnConnectedParticlesFor(World world, Vec3d pos, PlayerEntity player) {
			Random random = world.random;
			Vec3d vec3d = pos.relativize(player.getPos().add(0.0, (double) (player.getHeight() / 2.0F), 0.0));
			int i = MathHelper.nextInt(random, 2, 5);

			for (int j = 0; j < i; j++) {
				Vec3d vec3d2 = vec3d.addRandom(random, 1.0F);
				world.addParticle(ParticleTypes.VAULT_CONNECTION, pos.getX(), pos.getY(), pos.getZ(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ());
			}
		}

		private static void spawnConnectedParticles(World world, BlockPos pos, BlockState state, LootableVaultSharedData sharedData) {
			Set<UUID> set = sharedData.getConnectedPlayers();
			if (!set.isEmpty()) {
				Vec3d vec3d = getConnectedParticlesOrigin(pos, state.get(VaultBlock.FACING));

				for (UUID uUID : set) {
					PlayerEntity playerEntity = world.getPlayerByUuid(uUID);
					if (playerEntity != null && isPlayerWithinConnectedParticlesRange(pos, sharedData, playerEntity)) {
						spawnConnectedParticlesFor(world, vec3d, playerEntity);
					}
				}
			}
		}

		private static boolean isPlayerWithinConnectedParticlesRange(BlockPos pos, LootableVaultSharedData sharedData, PlayerEntity player) {
			return player.getBlockPos().getSquaredDistance(pos) <= MathHelper.square(sharedData.getConnectedParticlesRange());
		}

		private static void playAmbientSound(World world, BlockPos pos, LootableVaultSharedData sharedData) {
			if (hasDisplayItem(sharedData)) {
				Random random = world.getRandom();
				if (random.nextFloat() <= 0.02F) {
					world.playSoundAtBlockCenter(
							pos, SoundEvents.BLOCK_VAULT_AMBIENT, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false
					);
				}
			}
		}

		public static boolean hasDisplayItem(LootableVaultSharedData sharedData) {
			return sharedData.hasDisplayItem();
		}

		private static Vec3d getDeactivateParticlesPos(BlockPos pos, Random random) {
			return Vec3d.of(pos).add(MathHelper.nextDouble(random, 0.4, 0.6), MathHelper.nextDouble(random, 0.4, 0.6), MathHelper.nextDouble(random, 0.4, 0.6));
		}

		private static Vec3d getRegularParticlesPos(BlockPos pos, Random random) {
			return Vec3d.of(pos).add(MathHelper.nextDouble(random, 0.1, 0.9), MathHelper.nextDouble(random, 0.25, 0.75), MathHelper.nextDouble(random, 0.1, 0.9));
		}

		private static Vec3d getConnectedParticlesOrigin(BlockPos pos, Direction direction) {
			return Vec3d.ofBottomCenter(pos).add((double) direction.getOffsetX() * 0.5, 1.75, (double) direction.getOffsetZ() * 0.5);
		}
	}

	public static final class Server {
		private static final int UNLOCK_TIME = 14;
		private static final int DISPLAY_UPDATE_INTERVAL = 20;
		private static final int FAILED_UNLOCK_COOLDOWN = 15;

		public static void tick(ServerWorld world, BlockPos pos, BlockState state, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData) {
			LootableVaultState vaultState = state.get(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			if (shouldUpdateDisplayItem(world.getTime(), vaultState)) {
				updateDisplayItem(world, vaultState, config, sharedData, pos);
			}

			BlockState blockState = state;
			if (world.getTime() >= serverData.getStateUpdatingResumeTime()) {
				blockState = state.with(LootableVaultBlock.LOOTABLE_VAULT_STATE, vaultState.update(world, pos, config, serverData, sharedData));
				if (!state.equals(blockState)) {
					changeVaultState(world, pos, state, blockState, config, sharedData);
				}
			}

			if (serverData.isDirty() || sharedData.isDirty()) {
				LootableVaultBlockEntity.markDirty(world, pos, state);
				if (sharedData.isDirty()) {
					world.updateListeners(pos, state, blockState, Block.NOTIFY_LISTENERS);
				}

				serverData.markClean();
				sharedData.markClean();
			}
		}

		public static void tryUnlock(
				ServerWorld world,
				BlockPos pos,
				BlockState state,
				LootableVaultConfig config,
				LootableVaultServerData serverData,
				LootableVaultSharedData sharedData,
				PlayerEntity player,
				ItemStack stack
		) {
			LootableVaultState vaultState = state.get(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			if (canBeUnlocked(config, vaultState)) {
				if (!isValidKey(config, stack)) {
					ScriptBlocks.info("invalid key");
					playFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL);
				} else if (serverData.hasRewardedPlayer(player)) {
					ScriptBlocks.info("player already rewarded");
					playFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
				} else if (player instanceof ServerPlayerEntity serverPlayerEntity) {
					// TODO loot
//					Vec3d lootPos = new Vec3d(this.getPos().getX(), interactiveLootBlockEntity.getPos().getY(), interactiveLootBlockEntity.getPos().getZ());
//					boolean lootSupplied = ScriptBlocks.supplyLootableLoot(Identifier.of(config.lootableIdentifier()), serverPlayerEntity, Vec3d.of(pos), config.rolls(), config.choices(), config.withChoice());

//					List<ItemStack> list = generateLoot(world, config, pos, player);
					if (ScriptBlocks.supplyLootableLoot(Identifier.of(config.lootableIdentifier()), serverPlayerEntity, Vec3d.of(pos), config.rolls(), config.choices(), config.withChoice())) {
						ScriptBlocks.info("loot supplied");
						player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
						stack.decrementUnlessCreative(config.keyItem().getCount(), player);
						unlock(world, state, pos, config, serverData, sharedData);
						serverData.markPlayerAsRewarded(player);
						sharedData.updateConnectedPlayers(world, pos, serverData, config, config.deactivationRange());
					}
				}
			}
		}

		static void changeVaultState(ServerWorld world, BlockPos pos, BlockState oldState, BlockState newState, LootableVaultConfig config, LootableVaultSharedData sharedData) {
			LootableVaultState vaultState = oldState.get(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			LootableVaultState vaultState2 = newState.get(LootableVaultBlock.LOOTABLE_VAULT_STATE);
			world.setBlockState(pos, newState, Block.NOTIFY_ALL);
			vaultState.onStateChange(world, pos, vaultState2, config, sharedData, (Boolean) newState.get(VaultBlock.OMINOUS));
		}

		public static void updateDisplayItem(ServerWorld world, LootableVaultState state, LootableVaultConfig config, LootableVaultSharedData sharedData, BlockPos pos) {
			if (!canBeUnlocked(config, state)) {
				sharedData.setDisplayItem(ItemStack.EMPTY);
			} else {
				ItemStack itemStack = generateDisplayItem(world, pos, (RegistryKey<LootTable>) config.overrideLootTableToDisplay().orElse(LootTables.EMPTY));
				sharedData.setDisplayItem(itemStack);
			}
		}

		private static ItemStack generateDisplayItem(ServerWorld world, BlockPos pos, RegistryKey<LootTable> lootTable) {
			LootTable lootTable2 = world.getServer().getReloadableRegistries().getLootTable(lootTable);
			LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(world)
					.add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
					.build(LootContextTypes.VAULT);
			List<ItemStack> list = lootTable2.generateLoot(lootContextParameterSet, world.getRandom());
			return list.isEmpty() ? ItemStack.EMPTY : Util.getRandom(list, world.getRandom());
		}

		private static void unlock(
				ServerWorld world, BlockState state, BlockPos pos, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData
		) {
//			serverData.setItemsToEject(itemsToEject);
			sharedData.setDisplayItem(ItemStack.EMPTY);
			serverData.setStateUpdatingResumeTime(world.getTime() + 14L);
			changeVaultState(world, pos, state, state.with(LootableVaultBlock.LOOTABLE_VAULT_STATE, LootableVaultState.UNLOCKING), config, sharedData);
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
			return ItemStack.areItemsAndComponentsEqual(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
		}

		private static boolean shouldUpdateDisplayItem(long time, LootableVaultState state) {
			return time % 20L == 0L && state == LootableVaultState.ACTIVE;
		}

		private static void playFailedUnlockSound(ServerWorld world, LootableVaultServerData serverData, BlockPos pos, SoundEvent sound) {
			if (world.getTime() >= serverData.getLastFailedUnlockTime() + 15L) {
				world.playSound(null, pos, sound, SoundCategory.BLOCKS);
				serverData.setLastFailedUnlockTime(world.getTime());
			}
		}
	}
}
