package com.github.theredbrain.scriptblocks.structure.pool;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FixedRotationStructurePoolBasedGenerator extends JigsawPlacement {
	static final Logger LOGGER = LogUtils.getLogger();

	public static Optional<Structure.GenerationStub> generate(
			Structure.GenerationContext context,
			Holder<StructureTemplatePool> structurePool,
			Optional<Identifier> id,
			int size,
			BlockPos pos,
			boolean useExpansionHack,
			Optional<Heightmap.Types> projectStartToHeightmap,
			int maxDistanceFromCenter,
			PoolAliasLookup aliasLookup,
			DimensionPadding dimensionPadding,
			LiquidSettings liquidSettings,
			Rotation blockRotation,
			RandomSource random
	) {
		RegistryAccess dynamicRegistryManager = context.registryAccess();
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
		LevelHeightAccessor heightLimitView = context.heightAccessor();
		WorldgenRandom chunkRandom = context.random(); // generates always same jigsaw combination in the same chunk/position
		chunkRandom.setSeed(random.nextLong()); // this randomizes the jigsaw generation even in the same chunk/position
		Registry<StructureTemplatePool> registry = dynamicRegistryManager.get(Registries.TEMPLATE_POOL);
		StructureTemplatePool structurePool2 = (StructureTemplatePool) structurePool.unwrapKey()
				.flatMap(key -> registry.getOrEmpty(aliasLookup.lookup(key)))
				.orElse(structurePool.value());
		StructurePoolElement structurePoolElement = structurePool2.getRandomTemplate(chunkRandom);
		if (structurePoolElement == EmptyPoolElement.INSTANCE) {
			return Optional.empty();
		} else {
			BlockPos blockPos;
			if (id.isPresent()) {
				Identifier identifier = (Identifier) id.get();
				Optional<BlockPos> optional = getRandomNamedJigsaw(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
				if (optional.isEmpty()) {
					LOGGER.error(
							"No starting jigsaw {} found in start pool {}", identifier, structurePool.unwrapKey().map(key -> key.identifier().toString()).orElse("<unregistered>")
					);
					return Optional.empty();
				}

				blockPos = (BlockPos) optional.get();
			} else {
				blockPos = pos;
			}

			Vec3i vec3i = blockPos.subtract(pos);
			BlockPos blockPos2 = pos.subtract(vec3i);
			PoolElementStructurePiece poolStructurePiece = new PoolElementStructurePiece(
					structureTemplateManager,
					structurePoolElement,
					blockPos2,
					structurePoolElement.getGroundLevelDelta(),
					blockRotation,
					structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation),
					liquidSettings
			);
			BoundingBox blockBox = poolStructurePiece.getBoundingBox();
			int i = (blockBox.maxX() + blockBox.minX()) / 2;
			int j = (blockBox.maxZ() + blockBox.minZ()) / 2;
			int k;
			if (projectStartToHeightmap.isPresent()) {
				k = pos.getY() + chunkGenerator.getFirstFreeHeight(i, j, (Heightmap.Types) projectStartToHeightmap.get(), heightLimitView, context.randomState());
			} else {
				k = blockPos2.getY();
			}

			int l = blockBox.minY() + poolStructurePiece.getGroundLevelDelta();
			poolStructurePiece.move(0, k - l, 0);
			int m = k + vec3i.getY();
			return Optional.of(
					new Structure.GenerationStub(
							new BlockPos(i, m, j),
							collector -> {
								List<PoolElementStructurePiece> list = Lists.<PoolElementStructurePiece>newArrayList();
								list.add(poolStructurePiece);
								if (size > 0) {
									AABB box = new AABB(
											(double) (i - maxDistanceFromCenter),
											(double) Math.max(m - maxDistanceFromCenter, heightLimitView.getMinY() + dimensionPadding.bottom()),
											(double) (j - maxDistanceFromCenter),
											(double) (i + maxDistanceFromCenter + 1),
											(double) Math.min(m + maxDistanceFromCenter + 1, heightLimitView.getTopY() - dimensionPadding.top()),
											(double) (j + maxDistanceFromCenter + 1)
									);
									VoxelShape voxelShape = Shapes.join(Shapes.create(box), Shapes.create(AABB.of(blockBox)), BooleanOp.ONLY_FIRST);
									addPieces(
											context.randomState(),
											size,
											useExpansionHack,
											chunkGenerator,
											structureTemplateManager,
											heightLimitView,
											chunkRandom,
											registry,
											poolStructurePiece,
											list,
											voxelShape,
											aliasLookup,
											liquidSettings
									);
									list.forEach(collector::addPiece);
								}
							}
					)
			);
		}
	}

	public static Optional<BlockPos> getRandomNamedJigsaw(
			StructurePoolElement pool, Identifier id, BlockPos pos, Rotation rotation, StructureTemplateManager structureManager, WorldgenRandom random
	) {
		List<StructureTemplate.StructureBlockInfo> list = pool.getShuffledJigsawBlocks(structureManager, pos, rotation, random);
		Optional<BlockPos> optional = Optional.empty();

		for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
			Identifier identifier = Identifier.tryParse(
					((CompoundTag) Objects.requireNonNull(structureBlockInfo.nbt(), () -> structureBlockInfo + " nbt was null")).getString("name")
			);
			if (id.equals(identifier)) {
				optional = Optional.of(structureBlockInfo.pos());
				break;
			}
		}

		return optional;
	}

	private static void addPieces(
			RandomState noiseConfig,
			int maxSize,
			boolean modifyBoundingBox,
			ChunkGenerator chunkGenerator,
			StructureTemplateManager structureTemplateManager,
			LevelHeightAccessor heightLimitView,
			RandomSource random,
			Registry<StructureTemplatePool> structurePoolRegistry,
			PoolElementStructurePiece firstPiece,
			List<PoolElementStructurePiece> pieces,
			VoxelShape pieceShape,
			PoolAliasLookup aliasLookup,
			LiquidSettings liquidSettings
	) {
		StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(
				structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random
		);
		structurePoolGenerator.generatePiece(
				firstPiece, new MutableObject<>(pieceShape), 0, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup, liquidSettings
		);

		while (structurePoolGenerator.structurePieces.hasNext()) {
			ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.next();
			structurePoolGenerator.generatePiece(
					shapedPoolStructurePiece.piece,
					shapedPoolStructurePiece.pieceShape,
					shapedPoolStructurePiece.currentSize,
					modifyBoundingBox,
					heightLimitView,
					noiseConfig,
					aliasLookup,
					liquidSettings
			);
		}
	}

	public static boolean generate(ServerLevel world, Holder<StructureTemplatePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, Rotation blockRotation) {
		ChunkGenerator chunkGenerator = world.getChunkSource().getGenerator();
		StructureTemplateManager structureTemplateManager = world.getStructureManager();
		StructureManager structureAccessor = world.structureManager();
		RandomSource random = RandomSource.create();
		Structure.GenerationContext context = new Structure.GenerationContext(
				world.registryAccess(),
				chunkGenerator,
				chunkGenerator.getBiomeSource(),
				world.getChunkSource().randomState(),
				structureTemplateManager,
				world.getSeed(),
				new ChunkPos(pos),
				world,
				biome -> true
		);
		Optional<Structure.GenerationStub> optional = generate(
				context,
				structurePool,
				Optional.of(id),
				size,
				pos,
				false,
				Optional.empty(),
				128,
				PoolAliasLookup.EMPTY,
				JigsawStructure.DEFAULT_DIMENSION_PADDING,
				JigsawStructure.DEFAULT_LIQUID_SETTINGS,
				blockRotation,
				random
		);
		if (optional.isPresent()) {
			StructurePiecesBuilder structurePiecesCollector = ((Structure.GenerationStub) optional.get()).getPiecesBuilder();

			for (StructurePiece structurePiece : structurePiecesCollector.build().pieces()) {
				if (structurePiece instanceof PoolElementStructurePiece poolStructurePiece) {
					poolStructurePiece.place(world, structureAccessor, chunkGenerator, random, BoundingBox.infinite(), pos, keepJigsaws);
				}
			}

			return true;
		} else {
			return false;
		}
	}

	record ShapedPoolStructurePiece(PoolElementStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {
	}

	static final class StructurePoolGenerator {
		private final Registry<StructureTemplatePool> registry;
		private final int maxSize;
		private final ChunkGenerator chunkGenerator;
		private final StructureTemplateManager structureTemplateManager;
		private final List<? super PoolElementStructurePiece> children;
		private final RandomSource random;
		final SequencedPriorityIterator<ShapedPoolStructurePiece> structurePieces = new SequencedPriorityIterator<>();

		StructurePoolGenerator(
				Registry<StructureTemplatePool> registry,
				int maxSize,
				ChunkGenerator chunkGenerator,
				StructureTemplateManager structureTemplateManager,
				List<? super PoolElementStructurePiece> children,
				RandomSource random
		) {
			this.registry = registry;
			this.maxSize = maxSize;
			this.chunkGenerator = chunkGenerator;
			this.structureTemplateManager = structureTemplateManager;
			this.children = children;
			this.random = random;
		}

		void generatePiece(
				PoolElementStructurePiece piece,
				MutableObject<VoxelShape> pieceShape,
				int minY,
				boolean modifyBoundingBox,
				LevelHeightAccessor world,
				RandomState noiseConfig,
				PoolAliasLookup aliasLookup,
				LiquidSettings liquidSettings
		) {
			StructurePoolElement structurePoolElement = piece.getElement();
			BlockPos blockPos = piece.getPosition();
			Rotation blockRotation = piece.getRotation();
			StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
			boolean bl = projection == StructureTemplatePool.Projection.RIGID;
			MutableObject<VoxelShape> mutableObject = new MutableObject<>();
			BoundingBox blockBox = piece.getBoundingBox();
			int i = blockBox.minY();

			label134:
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : structurePoolElement.getShuffledJigsawBlocks(
					this.structureTemplateManager, blockPos, blockRotation, this.random
			)) {
				Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo.state());
				BlockPos blockPos2 = structureBlockInfo.pos();
				BlockPos blockPos3 = blockPos2.relative(direction);
				int j = blockPos2.getY() - i;
				int k = -1;
				ResourceKey<StructureTemplatePool> registryKey = lookupPool(structureBlockInfo, aliasLookup);
				Optional<? extends Holder<StructureTemplatePool>> optional = this.registry.get(registryKey);
				if (optional.isEmpty()) {
					LOGGER.warn("Empty or non-existent pool: {}", registryKey.identifier());
				} else {
					Holder<StructureTemplatePool> registryEntry = (Holder<StructureTemplatePool>) optional.get();
					if (registryEntry.value().size() == 0 && !registryEntry.is(Pools.EMPTY)) {
						LOGGER.warn("Empty or non-existent pool: {}", registryKey.identifier());
					} else {
						Holder<StructureTemplatePool> registryEntry2 = registryEntry.value().getFallback();
						if (registryEntry2.value().size() == 0 && !registryEntry2.is(Pools.EMPTY)) {
							LOGGER
									.warn("Empty or non-existent fallback pool: {}", registryEntry2.unwrapKey().map(key -> key.identifier().toString()).orElse("<unregistered>"));
						} else {
							boolean bl2 = blockBox.isInside(blockPos3);
							MutableObject<VoxelShape> mutableObject2;
							if (bl2) {
								mutableObject2 = mutableObject;
								if (mutableObject.getValue() == null) {
									mutableObject.setValue(Shapes.create(AABB.of(blockBox)));
								}
							} else {
								mutableObject2 = pieceShape;
							}

							List<StructurePoolElement> list = Lists.<StructurePoolElement>newArrayList();
							if (minY != this.maxSize) {
								list.addAll(registryEntry.value().getShuffledTemplates(this.random));
							}

							list.addAll(registryEntry2.value().getShuffledTemplates(this.random));
							int l = structureBlockInfo.nbt() != null ? structureBlockInfo.nbt().getInt("placement_priority") : 0;

							for (StructurePoolElement structurePoolElement2 : list) {
								if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
									break;
								}

								for (Rotation blockRotation2 : Rotation.getShuffled(this.random)) {
									List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getShuffledJigsawBlocks(
											this.structureTemplateManager, BlockPos.ZERO, blockRotation2, this.random
									);
									BoundingBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, blockRotation2);
									int m;
									if (modifyBoundingBox && blockBox2.getYSpan() <= 16) {
										m = list2.stream().mapToInt(structureBlockInfox -> {
											if (!blockBox2.isInside(structureBlockInfox.pos().relative(JigsawBlock.getFrontFacing(structureBlockInfox.state())))) {
												return 0;
											} else {
												ResourceKey<StructureTemplatePool> registryKeyx = lookupPool(structureBlockInfox, aliasLookup);
												Optional<? extends Holder<StructureTemplatePool>> optionalx = this.registry.get(registryKeyx);
												Optional<Holder<StructureTemplatePool>> optional2 = optionalx.map(entry -> ((StructureTemplatePool) entry.value()).getFallback());
												int ix = (Integer) optionalx.map(entry -> ((StructureTemplatePool) entry.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
												int jx = (Integer) optional2.map(entry -> ((StructureTemplatePool) entry.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
												return Math.max(ix, jx);
											}
										}).max().orElse(0);
									} else {
										m = 0;
									}

									for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
										if (JigsawBlock.canAttach(structureBlockInfo, structureBlockInfo2)) {
											BlockPos blockPos4 = structureBlockInfo2.pos();
											BlockPos blockPos5 = blockPos3.subtract(blockPos4);
											BoundingBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, blockRotation2);
											int n = blockBox3.minY();
											StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
											boolean bl3 = projection2 == StructureTemplatePool.Projection.RIGID;
											int o = blockPos4.getY();
											int p = j - o + JigsawBlock.getFrontFacing(structureBlockInfo.state()).getStepY();
											int q;
											if (bl && bl3) {
												q = i + p;
											} else {
												if (k == -1) {
													k = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, world, noiseConfig);
												}

												q = k - o;
											}

											int r = q - n;
											BoundingBox blockBox4 = blockBox3.moved(0, r, 0);
											BlockPos blockPos6 = blockPos5.offset(0, r, 0);
											if (m > 0) {
												int s = Math.max(m + 1, blockBox4.maxY() - blockBox4.minY());
												blockBox4.encapsulate(new BlockPos(blockBox4.minX(), blockBox4.minY() + s, blockBox4.minZ()));
											}

											if (!Shapes.joinIsNotEmpty(mutableObject2.getValue(), Shapes.create(AABB.of(blockBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
												mutableObject2.setValue(Shapes.joinUnoptimized(mutableObject2.getValue(), Shapes.create(AABB.of(blockBox4)), BooleanOp.ONLY_FIRST));
												int s = piece.getGroundLevelDelta();
												int t;
												if (bl3) {
													t = s - p;
												} else {
													t = structurePoolElement2.getGroundLevelDelta();
												}

												PoolElementStructurePiece poolStructurePiece = new PoolElementStructurePiece(
														this.structureTemplateManager, structurePoolElement2, blockPos6, t, blockRotation2, blockBox4, liquidSettings
												);
												int u;
												if (bl) {
													u = i + j;
												} else if (bl3) {
													u = q + o;
												} else {
													if (k == -1) {
														k = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, world, noiseConfig);
													}

													u = k + p / 2;
												}

												piece.addJunction(new JigsawJunction(blockPos3.getX(), u - j + s, blockPos3.getZ(), p, projection2));
												poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), u - o + t, blockPos2.getZ(), -p, projection));
												this.children.add(poolStructurePiece);
												if (minY + 1 <= this.maxSize) {
													ShapedPoolStructurePiece shapedPoolStructurePiece = new ShapedPoolStructurePiece(
															poolStructurePiece, mutableObject2, minY + 1
													);
													this.structurePieces.add(shapedPoolStructurePiece, l);
												}
												continue label134;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		private static ResourceKey<StructureTemplatePool> lookupPool(StructureTemplate.StructureBlockInfo structureBlockInfo, PoolAliasLookup aliasLookup) {
			CompoundTag nbtCompound = (CompoundTag) Objects.requireNonNull(structureBlockInfo.nbt(), () -> structureBlockInfo + " nbt was null");
			ResourceKey<StructureTemplatePool> registryKey = Pools.createKey(nbtCompound.getString("pool"));
			return aliasLookup.lookup(registryKey);
		}
	}
}
