package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class InteractiveLootBlock extends BaseEntityBlock {
	public static final MapCodec<InteractiveLootBlock> CODEC = simpleCodec(InteractiveLootBlock::new);

	public InteractiveLootBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<InteractiveLootBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InteractiveLootBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.is(this)) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {

				if (player.canUseGameMasterBlocks()) {
					((DuckPlayerEntityMixin) player).scriptblocks$openInteractiveLootBlockScreen(interactiveLootBlockEntity);
					return InteractionResult.success(world.isClientSide);
				} else if (world instanceof ServerLevel serverWorld && !serverWorld.isClientSide() && player instanceof ServerPlayer serverPlayerEntity) {
					if (interactiveLootBlockEntity.isPlayerInSet(serverPlayerEntity)) {
						String alreadyLootedSoundId = interactiveLootBlockEntity.getAlreadyLootedSoundId();
						if (!alreadyLootedSoundId.isEmpty()) {
							SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(Identifier.parse(alreadyLootedSoundId));
							if (BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent) != null) {
								serverPlayerEntity.playSoundToPlayer(soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
							} else {
								ScriptBlocks.info("No registered sound event of id '" + alreadyLootedSoundId + "' found.");
							}
						}
						String alreadyLootedMessage = interactiveLootBlockEntity.getAlreadyLootedMessage();
						if (!alreadyLootedMessage.isEmpty()) {
							serverPlayerEntity.displayClientMessage(Component.translatable(alreadyLootedMessage), true);
						}
					} else {
						Vec3 lootPos = new Vec3(interactiveLootBlockEntity.getBlockPos().getX(), interactiveLootBlockEntity.getBlockPos().getY(), interactiveLootBlockEntity.getBlockPos().getZ());
						if (interactiveLootBlockEntity.getMode() == InteractiveLootBlockEntity.Mode.CHOICE) {
							ScriptBlocks.supplyLootableLoot(Identifier.parse(interactiveLootBlockEntity.getLootTableIdentifierString()), serverWorld, serverPlayerEntity, lootPos, interactiveLootBlockEntity.getRolls(), interactiveLootBlockEntity.getChoices(), true, null);
						} else if (interactiveLootBlockEntity.getMode() == InteractiveLootBlockEntity.Mode.RANDOM) {
							ScriptBlocks.supplyLootableLoot(Identifier.parse(interactiveLootBlockEntity.getLootTableIdentifierString()), serverWorld, serverPlayerEntity, lootPos, interactiveLootBlockEntity.getRolls(), interactiveLootBlockEntity.getChoices(), false, null);
						} else {
							List<ItemStack> lootStacks = getLootItems(serverWorld, pos, player, interactiveLootBlockEntity);
							for (ItemStack itemStack : lootStacks) {
								player.getInventory().placeItemBackInInventory(itemStack);
							}
							lootWasSupplied(serverPlayerEntity, interactiveLootBlockEntity);
						}
						return InteractionResult.SUCCESS;
					}
				}
			}
		}
		return InteractionResult.PASS;
	}

	public static void lootWasSupplied(ServerPlayer serverPlayerEntity, InteractiveLootBlockEntity interactiveLootBlockEntity) {
		if (interactiveLootBlockEntity.getTrackPlayers()) {
			interactiveLootBlockEntity.addPlayerToSet(serverPlayerEntity);
		}
		String lootAcquiredSoundId = interactiveLootBlockEntity.getLootAcquiredSoundId();
		if (!lootAcquiredSoundId.isEmpty()) {
			SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(Identifier.parse(lootAcquiredSoundId));
			if (BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent) != null) {
				serverPlayerEntity.playSoundToPlayer(soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			} else {
				ScriptBlocks.info("No registered sound event of id '" + lootAcquiredSoundId + "' found.");
			}
		}
		String lootAcquiredMessage = interactiveLootBlockEntity.getLootAcquiredMessage();
		if (!lootAcquiredMessage.isEmpty()) {
			serverPlayerEntity.displayClientMessage(Component.translatable(lootAcquiredMessage), true);
		}
	}

	private static List<ItemStack> getLootItems(ServerLevel serverWorld, BlockPos pos, Player player, InteractiveLootBlockEntity interactiveLootBlockEntity) {
		List<ItemStack> list = new ArrayList<>();
		Identifier lootTableIdentifier = Identifier.tryParse(interactiveLootBlockEntity.getLootTableIdentifierString());
		if (lootTableIdentifier != null) {
			LootTable lootTable = serverWorld.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableIdentifier));
			list = lootTable.getRandomItems(new LootContextParameterSet.Builder(serverWorld).add(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ())).add(LootContextParams.THIS_ENTITY, player).build(LootContextParamSets.CHEST)); // TODO when changed to LootContextTypes.BLOCK, can do stuff depending on held item
		}
		return list;
	}
}
