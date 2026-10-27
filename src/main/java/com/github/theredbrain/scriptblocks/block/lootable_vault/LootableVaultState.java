package com.github.theredbrain.scriptblocks.block.lootable_vault;

import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;

@Deprecated
public enum LootableVaultState implements StringRepresentable {
	INACTIVE("inactive", Light.HALF_LIT) {
		@Override
		protected void onChangedTo(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
			sharedData.setDisplayItem(ItemStack.EMPTY);
			world.levelEvent(LevelEvent.ANIMATION_VAULT_DEACTIVATE, pos, ominous ? 1 : 0);
		}
	},
	ACTIVE("active", Light.LIT) {
		@Override
		protected void onChangedTo(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
			if (!sharedData.hasDisplayItem()) {
				LootableVaultBlockEntity.Server.updateDisplayItem(world, this, config, sharedData, pos);
			}

			world.levelEvent(LevelEvent.ANIMATION_VAULT_ACTIVATE, pos, ominous ? 1 : 0);
		}
	},
	UNLOCKING("unlocking", Light.LIT) {
		@Override
		protected void onChangedTo(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
			world.playSound(null, pos, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
		}
	},
	EJECTING("ejecting", Light.LIT) {
		@Override
		protected void onChangedTo(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
			world.playSound(null, pos, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
		}

		@Override
		protected void onChangedFrom(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData) {
			world.playSound(null, pos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
		}
	};

	private static final int field_48903 = 20;
	private static final int field_48904 = 20;
	private static final int field_48905 = 20;
	private static final int field_48906 = 20;
	private final String id;
	private final Light light;

	LootableVaultState(final String id, final Light light) {
		this.id = id;
		this.light = light;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public int getLuminance() {
		return this.light.luminance;
	}

	public LootableVaultState update(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData) {
		return switch (this) {
			case INACTIVE -> updateActiveState(world, pos, config, serverData, sharedData, config.activationRange());
			case ACTIVE -> updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
			case UNLOCKING -> {
				serverData.setStateUpdatingResumeTime(world.getGameTime() + 20L);
				yield EJECTING;
			}
			case EJECTING -> {
//				if (serverData.getItemsToEject().isEmpty()) {
//					serverData.finishEjecting();
//					yield updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
//				} else {
////					sharedData.setDisplayItem(serverData.getItemToDisplay());
////					boolean bl = serverData.getItemsToEject().isEmpty();
////					int i = bl ? 20 : 20;
////					yield EJECTING;
//				}
//				float f = serverData.getEjectSoundPitchModifier();
				this.ejectReward(world, pos);
				serverData.setStateUpdatingResumeTime(world.getGameTime() + 20L);
				yield updateActiveState(world, pos, config, serverData, sharedData, config.deactivationRange());
			}
		};
	}

	private static LootableVaultState updateActiveState(
			ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultServerData serverData, LootableVaultSharedData sharedData, double radius
	) {
		sharedData.updateConnectedPlayers(world, pos, serverData, config, radius);
		serverData.setStateUpdatingResumeTime(world.getGameTime() + 20L);
		return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
	}

	public void onStateChange(ServerLevel world, BlockPos pos, LootableVaultState newState, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
		this.onChangedFrom(world, pos, config, sharedData);
		newState.onChangedTo(world, pos, config, sharedData, ominous);
	}

	protected void onChangedTo(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData, boolean ominous) {
	}

	protected void onChangedFrom(ServerLevel world, BlockPos pos, LootableVaultConfig config, LootableVaultSharedData sharedData) {
	}

	private void ejectReward(ServerLevel world, BlockPos pos) {
//		ItemDispenserBehavior.spawnItem(world, stack, 2, Direction.UP, Vec3d.ofBottomCenter(pos).offset(Direction.UP, 1.2));
		world.levelEvent(LevelEvent.ANIMATION_VAULT_EJECT_ITEM, pos, 0);
		world.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F);
	}

	static enum Light {
		HALF_LIT(6),
		LIT(12);

		final int luminance;

		private Light(final int luminance) {
			this.luminance = luminance;
		}
	}
}
