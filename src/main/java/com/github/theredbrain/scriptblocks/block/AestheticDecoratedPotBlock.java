package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class AestheticDecoratedPotBlock extends BlockWithEntity implements Waterloggable {
	public static final MapCodec<AestheticDecoratedPotBlock> CODEC = createCodec(AestheticDecoratedPotBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty CRACKED = Properties.CRACKED;
	private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	@Override
	public MapCodec<AestheticDecoratedPotBlock> getCodec() {
		return CODEC;
	}

	public AestheticDecoratedPotBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(
				this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(WATERLOGGED, Boolean.valueOf(false)).with(CRACKED, Boolean.valueOf(false))
		);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		if ((Boolean) state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return this.getDefaultState()
				.with(FACING, ctx.getHorizontalPlayerFacing())
				.with(WATERLOGGED, Boolean.valueOf(fluidState.getFluid() == Fluids.WATER))
				.with(CRACKED, Boolean.valueOf(false));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.getBlockEntity(pos) instanceof AestheticDecoratedPotBlockEntity aestheticDecoratedPotBlockEntity) {
			world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			aestheticDecoratedPotBlockEntity.wobble(DecoratedPotBlockEntity.WobbleType.NEGATIVE);
			world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

	@Override
	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, CRACKED);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AestheticDecoratedPotBlockEntity(pos, state);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		super.appendTooltip(stack, context, tooltip, options);
		Sherds sherds = stack.getOrDefault(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT);
		if (!sherds.equals(Sherds.DEFAULT)) {
			tooltip.add(ScreenTexts.EMPTY);
			Stream.of(sherds.front(), sherds.left(), sherds.right(), sherds.back())
					.forEach(sherd -> tooltip.add(new ItemStack((ItemConvertible) sherd.orElse(Items.BRICK), 1).getName().copyContentOnly().formatted(Formatting.GRAY)));
		}
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		return world.getBlockEntity(pos) instanceof AestheticDecoratedPotBlockEntity aestheticDecoratedPotBlockEntity
				? aestheticDecoratedPotBlockEntity.asStack()
				: super.getPickStack(world, pos, state);
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
}
