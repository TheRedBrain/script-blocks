package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.network.packet.OpenDialogueScreenPacket;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DialogueBlock extends RotatedBlockWithEntity {

	public DialogueBlock(Settings settings) {
		super(settings);
	}

	// TODO Block Codecs
	public MapCodec<DialogueBlock> getCodec() {
		return null;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DialogueBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof DialogueBlockEntity dialogueBlockEntity) {
			if (player.isCreativeLevelTwoOp()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openDialogueBlockScreen(dialogueBlockEntity);
				return ActionResult.success(world.isClient);
			} else if (player instanceof ServerPlayerEntity serverPlayerEntity) {
				Dialogue dialogue = DialogueBlockEntity.getDialogue(player, dialogueBlockEntity);
				if (dialogue != null) {
					((DuckPlayerEntityMixin) player).scriptblocks$openDialogueScreen(dialogue, dialogueBlockEntity.getDialogueUsedBlocks(), dialogueBlockEntity.getDialogueTriggeredBlocks());
					return ActionResult.CONSUME;
				}
			}
		}
		return ActionResult.PASS;
	}
}
