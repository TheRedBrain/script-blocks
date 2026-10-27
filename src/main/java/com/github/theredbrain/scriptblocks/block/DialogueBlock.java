package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DialogueBlock extends RotatedBlockWithEntity {
	public static final MapCodec<DialogueBlock> CODEC = simpleCodec(DialogueBlock::new);

	public DialogueBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<DialogueBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DialogueBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof DialogueBlockEntity dialogueBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openDialogueBlockScreen(dialogueBlockEntity);
				return InteractionResult.success(world.isClientSide);
			} else if (player instanceof ServerPlayer serverPlayerEntity) {
				String dialogue = DialogueAnchor.getDialogue(world, player, dialogueBlockEntity.getStartingDialogueList());
				MinecraftServer server = serverPlayerEntity.getServer();
				if (!dialogue.isEmpty() && server != null) {
					DialogueAnchor.openDialogueScreen(world, server, serverPlayerEntity, dialogue, dialogueBlockEntity.getDataBlockPos(), dialogueBlockEntity.getDialogueUsedBlocks(), dialogueBlockEntity.getDialogueTriggeredBlocks());
					return InteractionResult.CONSUME;
				}
			}
		}
		return InteractionResult.PASS;
	}
}
