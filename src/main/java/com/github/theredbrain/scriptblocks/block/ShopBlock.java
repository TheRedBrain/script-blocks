package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ShopBlock extends BaseEntityBlock {
	public static final MapCodec<ShopBlock> CODEC = simpleCodec(ShopBlock::new);

	public ShopBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<ShopBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ShopBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openShopBlockScreen(shopBlockEntity);
				return InteractionResult.SUCCESS;
			} else if (!world.isClientSide()) {
				player.openMenu(createShopMenuProvider(Component.empty(), pos));
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	static MenuProvider createShopMenuProvider(
			Component title,
			BlockPos shopBlockPos
	) {
		return new ExtendedMenuProvider<>() {
			@Override
			public ShopScreenHandler.ShopBlockData getScreenOpeningData(ServerPlayer player) {
				return new ShopScreenHandler.ShopBlockData(shopBlockPos);
			}

			@Override
			public Component getDisplayName() {
				return title;
			}

			@Nullable
			@Override
			public ShopScreenHandler createMenu(int syncId, Inventory playerInventory, Player player) {
				return new ShopScreenHandler(
						syncId,
						playerInventory,
						shopBlockPos
				);
			}
		};
	}
}
