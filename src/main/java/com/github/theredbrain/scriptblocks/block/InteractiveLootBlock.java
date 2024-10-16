package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InteractiveLootBlock extends BlockWithEntity {

	public InteractiveLootBlock(Settings settings) {
		super(settings);
	}

	// TODO Block Codecs
	public MapCodec<InteractiveLootBlock> getCodec() {
		return null;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new InteractiveLootBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.isOf(this)) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {

				if (player.isCreativeLevelTwoOp()) {
					((DuckPlayerEntityMixin) player).scriptblocks$openInteractiveLootBlockScreen(interactiveLootBlockEntity);
					return ActionResult.success(world.isClient);
				} else if (world instanceof ServerWorld serverWorld && !serverWorld.isClient()) {
					if (interactiveLootBlockEntity.addPlayerToSet(player)) {
						List<ItemStack> lootStacks = getLootItems(serverWorld, pos, player, interactiveLootBlockEntity);
						for (ItemStack itemStack : lootStacks) {
							player.getInventory().offerOrDrop(itemStack);
						}
						int i = world.getRandom().nextInt(5);
						player.sendMessage(Text.translatable("gui.interactive_loot_block.loot_acquired_" + i), true);
						return ActionResult.SUCCESS;
					}
				}
			}
		}
		return ActionResult.PASS;
	}

	private static List<ItemStack> getLootItems(ServerWorld serverWorld, BlockPos pos, PlayerEntity player, InteractiveLootBlockEntity interactiveLootBlockEntity) {
		List<ItemStack> list = new ArrayList<>();
		Identifier lootTableIdentifier = Identifier.tryParse(interactiveLootBlockEntity.getLootTableIdentifierString());
		if (lootTableIdentifier != null) {
			LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableIdentifier));
			list = lootTable.generateLoot(new LootContextParameterSet.Builder(serverWorld).add(LootContextParameters.ORIGIN, new Vec3d(pos.getX(), pos.getY(), pos.getZ())).add(LootContextParameters.THIS_ENTITY, player).build(LootContextTypes.CHEST)); // TODO when changed to LootContextTypes.BLOCK, can do stuff depending on held item
		}
		return list;
	}
}
