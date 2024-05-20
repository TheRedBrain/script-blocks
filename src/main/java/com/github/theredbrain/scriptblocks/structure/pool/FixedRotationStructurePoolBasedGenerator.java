package com.github.theredbrain.scriptblocks.structure.pool;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import net.minecraft.block.JigsawBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.*;
import net.minecraft.structure.pool.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FixedRotationStructurePoolBasedGenerator extends StructurePoolBasedGenerator {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.StructurePosition> generate(
            Structure.Context context,
            RegistryEntry<StructurePool> structurePool,
            Optional<Identifier> id,
            int size,
            BlockPos pos,
            boolean useExpansionHack,
            Optional<Heightmap.Type> projectStartToHeightmap,
            int maxDistanceFromCenter,
            BlockRotation blockRotation
    ) {
        DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
        HeightLimitView heightLimitView = context.world();
        ChunkRandom chunkRandom = context.random(); // generates always same jigsaw combination in the same chunk/position
        chunkRandom.setSeed(Random.create().nextLong()); // this randomizes the jigsaw generation even in the same chunk/position
        Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.TEMPLATE_POOL);
//        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool2 = structurePool.value();
        StructurePoolElement structurePoolElement = structurePool2.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        } else {
            BlockPos blockPos;
            if (id.isPresent()) {
                Identifier identifier = (Identifier)id.get();
                Optional<BlockPos> optional = findStartingJigsawPos(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
                if (optional.isEmpty()) {
                    LOGGER.error(
                            "No starting jigsaw {} found in start pool {}", identifier, structurePool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>")
                    );
                    return Optional.empty();
                }

                blockPos = (BlockPos)optional.get();
            } else {
                blockPos = pos;
            }

            Vec3i vec3i = blockPos.subtract(pos);
            BlockPos blockPos2 = pos.subtract(vec3i);
            PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
                    structureTemplateManager,
                    structurePoolElement,
                    blockPos2,
                    structurePoolElement.getGroundLevelDelta(),
                    blockRotation,
                    structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation)
            );
            BlockBox blockBox = poolStructurePiece.getBoundingBox();
            int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
            int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
            int k;
            if (projectStartToHeightmap.isPresent()) {
                k = pos.getY() + chunkGenerator.getHeightOnGround(i, j, (Heightmap.Type)projectStartToHeightmap.get(), heightLimitView, context.noiseConfig());
            } else {
                k = blockPos2.getY();
            }

            int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
            poolStructurePiece.translate(0, k - l, 0);
            int m = k + vec3i.getY();
            return Optional.of(
                    new Structure.StructurePosition(
                            new BlockPos(i, m, j),
                            collector -> {
                                List<PoolStructurePiece> list = Lists.<PoolStructurePiece>newArrayList();
                                list.add(poolStructurePiece);
                                if (size > 0) {
                                    Box box = new Box(
                                            (double)(i - maxDistanceFromCenter),
                                            (double)(m - maxDistanceFromCenter),
                                            (double)(j - maxDistanceFromCenter),
                                            (double)(i + maxDistanceFromCenter + 1),
                                            (double)(m + maxDistanceFromCenter + 1),
                                            (double)(j + maxDistanceFromCenter + 1)
                                    );
                                    VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);
                                    generate(
                                            context.noiseConfig(),
                                            size,
                                            useExpansionHack,
                                            chunkGenerator,
                                            structureTemplateManager,
                                            heightLimitView,
                                            chunkRandom,
                                            registry,
                                            poolStructurePiece,
                                            list,
                                            voxelShape
                                    );
                                    list.forEach(collector::addPiece);
                                }
                            }
                    )
            );
        }
    }

//    public static Optional<Structure.StructurePosition> generate(Structure.Context context, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter/*, StructurePoolAliasLookup aliasLookup*/, BlockRotation blockRotation) {
//        DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
//        ChunkGenerator chunkGenerator = context.chunkGenerator();
//        StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
//        HeightLimitView heightLimitView = context.world();
//        ChunkRandom chunkRandom = context.random(); // generates always same jigsaw combination in the same chunk/position
//        chunkRandom.setSeed(Random.create().nextLong()); // this randomizes the jigsaw generation even in the same chunk/position
//        Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.TEMPLATE_POOL);
//        StructurePool structurePool2 = (StructurePool)structurePool.getKey().flatMap((registryKey) -> {
//            return registry.getOrEmpty(aliasLookup.lookup(registryKey));
//        }).orElse((StructurePool)structurePool.value());
//        StructurePoolElement structurePoolElement = structurePool2.getRandomElement(chunkRandom);
//        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
//            return Optional.empty();
//        } else {
//            BlockPos blockPos;
//            if (id.isPresent()) {
//                Identifier identifier = (Identifier)id.get();
//                Optional<BlockPos> optional = FixedRotationStructurePoolBasedGenerator.findStartingJigsawPos(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
//                if (optional.isEmpty()) {
//                    ScriptBlocksMod.LOGGER.error("No starting jigsaw {} found in start pool {}", identifier, structurePool.getKey().map((key) -> {
//                        return key.getValue().toString();
//                    }).orElse("<unregistered>"));
//                    return Optional.empty();
//                }
//
//                blockPos = (BlockPos)optional.get();
//            } else {
//                blockPos = pos;
//            }
//
//            Vec3i vec3i = blockPos.subtract(pos);
//            BlockPos blockPos2 = pos.subtract(vec3i);
//            PoolStructurePiece poolStructurePiece = new PoolStructurePiece(structureTemplateManager, structurePoolElement, blockPos2, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation));
//            BlockBox blockBox = poolStructurePiece.getBoundingBox();
//            int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
//            int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
//            int k;
//            if (projectStartToHeightmap.isPresent()) {
//                k = pos.getY() + chunkGenerator.getHeightOnGround(i, j, (Heightmap.Type)projectStartToHeightmap.get(), heightLimitView, context.noiseConfig());
//            } else {
//                k = blockPos2.getY();
//            }
//
//            int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
//            poolStructurePiece.translate(0, k - l, 0);
//            int m = k + vec3i.getY();
//            return Optional.of(new Structure.StructurePosition(new BlockPos(i, m, j), (collector) -> {
//                List<PoolStructurePiece> list = Lists.newArrayList();
//                list.add(poolStructurePiece);
//                if (size > 0) {
//                    Box box = new Box((double)(i - maxDistanceFromCenter), (double)(m - maxDistanceFromCenter), (double)(j - maxDistanceFromCenter), (double)(i + maxDistanceFromCenter + 1), (double)(m + maxDistanceFromCenter + 1), (double)(j + maxDistanceFromCenter + 1));
//                    VoxelShape voxelShape = VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST);
//                    FixedRotationStructurePoolBasedGenerator.generate(context.noiseConfig(), size, useExpansionHack, chunkGenerator, structureTemplateManager, heightLimitView, chunkRandom, registry, poolStructurePiece, list, voxelShape, aliasLookup);
//                    Objects.requireNonNull(collector);
//                    list.forEach(collector::addPiece);
//                }
//            }));
//        }
//    }

    public static Optional<BlockPos> findStartingJigsawPos(
            StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random
    ) {
        List<StructureTemplate.StructureBlockInfo> list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
        Optional<BlockPos> optional = Optional.empty();

        for(StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            Identifier identifier = Identifier.tryParse(
                    ((NbtCompound)Objects.requireNonNull(structureBlockInfo.nbt(), () -> structureBlockInfo + " nbt was null")).getString("name")
            );
            if (id.equals(identifier)) {
                optional = Optional.of(structureBlockInfo.pos());
                break;
            }
        }

        return optional;
    }

    private static void generate(
            NoiseConfig noiseConfig,
            int maxSize,
            boolean modifyBoundingBox,
            ChunkGenerator chunkGenerator,
            StructureTemplateManager structureTemplateManager,
            HeightLimitView heightLimitView,
            Random random,
            Registry<StructurePool> structurePoolRegistry,
            PoolStructurePiece firstPiece,
            List<PoolStructurePiece> pieces,
            VoxelShape pieceShape
    ) {
        StructurePoolGenerator structurePoolGenerator = new StructurePoolGenerator(
                structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random
        );
        structurePoolGenerator.structurePieces.addLast(new ShapedPoolStructurePiece(firstPiece, new MutableObject<>(pieceShape), 0));

        while(!structurePoolGenerator.structurePieces.isEmpty()) {
            ShapedPoolStructurePiece shapedPoolStructurePiece = (ShapedPoolStructurePiece)structurePoolGenerator.structurePieces
                    .removeFirst();
            structurePoolGenerator.generatePiece(
                    shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, modifyBoundingBox, heightLimitView, noiseConfig
            );
        }
    }

//    public static void generate(
//            NoiseConfig noiseConfig,
//            int maxSize,
//            boolean modifyBoundingBox,
//            ChunkGenerator chunkGenerator,
//            StructureTemplateManager structureTemplateManager,
//            HeightLimitView heightLimitView,
//            Random random,
//            Registry<StructurePool> structurePoolRegistry,
//            PoolStructurePiece firstPiece,
//            List<PoolStructurePiece> pieces,
//            VoxelShape pieceShape/*,
//            StructurePoolAliasLookup aliasLookup*/
//    ) {
//        FixedRotationStructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator = new FixedRotationStructurePoolBasedGenerator.StructurePoolGenerator(
//                structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random
//        );
//        structurePoolGenerator.generatePiece(firstPiece, new MutableObject<>(pieceShape), 0, modifyBoundingBox, heightLimitView, noiseConfig, aliasLookup);
//
//        while(structurePoolGenerator.structurePieces.hasNext()) {
//            FixedRotationStructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = (FixedRotationStructurePoolBasedGenerator.ShapedPoolStructurePiece)structurePoolGenerator.structurePieces
//                    .next();
//            structurePoolGenerator.generatePiece(
//                    shapedPoolStructurePiece.piece,
//                    shapedPoolStructurePiece.pieceShape,
//                    shapedPoolStructurePiece.currentSize,
//                    modifyBoundingBox,
//                    heightLimitView,
//                    noiseConfig/*,
//                    aliasLookup*/
//            );
//        }
//    }

    public static boolean generate(ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, BlockRotation blockRotation) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
        StructureAccessor structureAccessor = world.getStructureAccessor();
        Random random = world.getRandom();
        Structure.Context context = new Structure.Context(
                world.getRegistryManager(),
                chunkGenerator,
                chunkGenerator.getBiomeSource(),
                world.getChunkManager().getNoiseConfig(),
                structureTemplateManager,
                world.getSeed(),
                new ChunkPos(pos),
                world,
                biome -> true
        );
        Optional<Structure.StructurePosition> optional = generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 128, blockRotation);
        if (optional.isPresent()) {
            StructurePiecesCollector structurePiecesCollector = ((Structure.StructurePosition)optional.get()).generate();

            for(StructurePiece structurePiece : structurePiecesCollector.toList().pieces()) {
                if (structurePiece instanceof PoolStructurePiece poolStructurePiece) {
                    poolStructurePiece.generate(world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), pos, keepJigsaws);
                }
            }

            return true;
        } else {
            return false;
        }
    }

//    public static boolean generate(ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, BlockRotation blockRotation) {
//        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
//        StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
//        StructureAccessor structureAccessor = world.getStructureAccessor();
//        Random random = world.getRandom();
//        Structure.Context context = new Structure.Context(world.getRegistryManager(), chunkGenerator, chunkGenerator.getBiomeSource(), world.getChunkManager().getNoiseConfig(), structureTemplateManager, world.getSeed(), new ChunkPos(pos), world, biome -> true);
//        Optional<Structure.StructurePosition> optional = FixedRotationStructurePoolBasedGenerator.generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 128, StructurePoolAliasLookup.EMPTY, blockRotation);
//        if (optional.isPresent()) {
//            StructurePiecesCollector structurePiecesCollector = optional.get().generate();
//            for (StructurePiece structurePiece : structurePiecesCollector.toList().pieces()) {
//                if (!(structurePiece instanceof PoolStructurePiece poolStructurePiece)) continue;
//                poolStructurePiece.generate((StructureWorldAccess)world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), pos, keepJigsaws);
//            }
//            return true;
//        }
//        return false;
//    }

    record ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {}

    static final class StructurePoolGenerator {
        private final Registry<StructurePool> registry;
        private final int maxSize;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolStructurePiece> children;
        private final Random random;
        final Deque<ShapedPoolStructurePiece> structurePieces = Queues.<ShapedPoolStructurePiece>newArrayDeque(

        );

        StructurePoolGenerator(
                Registry<StructurePool> registry,
                int maxSize,
                ChunkGenerator chunkGenerator,
                StructureTemplateManager structureTemplateManager,
                List<? super PoolStructurePiece> children,
                Random random
        ) {
            this.registry = registry;
            this.maxSize = maxSize;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.children = children;
            this.random = random;
        }

        void generatePiece(
                PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int minY, boolean modifyBoundingBox, HeightLimitView world, NoiseConfig noiseConfig
        ) {
            StructurePoolElement structurePoolElement = piece.getPoolElement();
            BlockPos blockPos = piece.getPos();
            BlockRotation blockRotation = piece.getRotation();
            StructurePool.Projection projection = structurePoolElement.getProjection();
            boolean bl = projection == StructurePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
            BlockBox blockBox = piece.getBoundingBox();
            int i = blockBox.getMinY();

            label129:
            for(StructureTemplate.StructureBlockInfo structureBlockInfo : structurePoolElement.getStructureBlockInfos(
                    this.structureTemplateManager, blockPos, blockRotation, this.random
            )) {
                Direction direction = JigsawBlock.getFacing(structureBlockInfo.state());
                BlockPos blockPos2 = structureBlockInfo.pos();
                BlockPos blockPos3 = blockPos2.offset(direction);
                int j = blockPos2.getY() - i;
                int k = -1;
                RegistryKey<StructurePool> registryKey = getPoolKey(structureBlockInfo);
                Optional<? extends RegistryEntry<StructurePool>> optional = this.registry.getEntry(registryKey);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", registryKey.getValue());
                } else {
                    RegistryEntry<StructurePool> registryEntry = (RegistryEntry)optional.get();
                    if (registryEntry.value().getElementCount() == 0 && !registryEntry.matchesKey(StructurePools.EMPTY)) {
                        LOGGER.warn("Empty or non-existent pool: {}", registryKey.getValue());
                    } else {
                        RegistryEntry<StructurePool> registryEntry2 = registryEntry.value().getFallback();
                        if (registryEntry2.value().getElementCount() == 0 && !registryEntry2.matchesKey(StructurePools.EMPTY)) {
                            LOGGER
                                    .warn("Empty or non-existent fallback pool: {}", registryEntry2.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                        } else {
                            boolean bl2 = blockBox.contains(blockPos3);
                            MutableObject<VoxelShape> mutableObject2;
                            if (bl2) {
                                mutableObject2 = mutableObject;
                                if (mutableObject.getValue() == null) {
                                    mutableObject.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                                }
                            } else {
                                mutableObject2 = pieceShape;
                            }

                            List<StructurePoolElement> list = Lists.<StructurePoolElement>newArrayList();
                            if (minY != this.maxSize) {
                                list.addAll(registryEntry.value().getElementIndicesInRandomOrder(this.random));
                            }

                            list.addAll(registryEntry2.value().getElementIndicesInRandomOrder(this.random));

                            for(StructurePoolElement structurePoolElement2 : list) {
                                if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
                                    break;
                                }

                                for(BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(this.random)) {
                                    List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(
                                            this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2, this.random
                                    );
                                    BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2);
                                    int l;
                                    if (modifyBoundingBox && blockBox2.getBlockCountY() <= 16) {
                                        l = list2.stream().mapToInt(blockInfo -> {
                                            if (!blockBox2.contains(blockInfo.pos().offset(JigsawBlock.getFacing(blockInfo.state())))) {
                                                return 0;
                                            } else {
                                                RegistryKey<StructurePool> registryKeyxx = getPoolKey(blockInfo);
                                                Optional<? extends RegistryEntry<StructurePool>> optionalxx = this.registry.getEntry(registryKeyxx);
                                                Optional<RegistryEntry<StructurePool>> optional2 = optionalxx.map(entry -> ((StructurePool)entry.value()).getFallback());
                                                int ixx = optionalxx.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                                                int jxx = optional2.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
                                                return Math.max(ixx, jxx);
                                            }
                                        }).max().orElse(0);
                                    } else {
                                        l = 0;
                                    }

                                    for(StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
                                        if (JigsawBlock.attachmentMatches(structureBlockInfo, structureBlockInfo2)) {
                                            BlockPos blockPos4 = structureBlockInfo2.pos();
                                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, blockRotation2);
                                            int m = blockBox3.getMinY();
                                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
                                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
                                            int n = blockPos4.getY();
                                            int o = j - n + JigsawBlock.getFacing(structureBlockInfo.state()).getOffsetY();
                                            int p;
                                            if (bl && bl3) {
                                                p = i + o;
                                            } else {
                                                if (k == -1) {
                                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                                }

                                                p = k - n;
                                            }

                                            int q = p - m;
                                            BlockBox blockBox4 = blockBox3.offset(0, q, 0);
                                            BlockPos blockPos6 = blockPos5.add(0, q, 0);
                                            if (l > 0) {
                                                int r = Math.max(l + 1, blockBox4.getMaxY() - blockBox4.getMinY());
                                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + r, blockBox4.getMinZ()));
                                            }

                                            if (!VoxelShapes.matchesAnywhere(mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)), BooleanBiFunction.ONLY_SECOND)) {
                                                mutableObject2.setValue(VoxelShapes.combine(mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
                                                int r = piece.getGroundLevelDelta();
                                                int s;
                                                if (bl3) {
                                                    s = r - o;
                                                } else {
                                                    s = structurePoolElement2.getGroundLevelDelta();
                                                }

                                                PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
                                                        this.structureTemplateManager, structurePoolElement2, blockPos6, s, blockRotation2, blockBox4
                                                );
                                                int t;
                                                if (bl) {
                                                    t = i + j;
                                                } else if (bl3) {
                                                    t = p + n;
                                                } else {
                                                    if (k == -1) {
                                                        k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
                                                    }

                                                    t = k + o / 2;
                                                }

                                                piece.addJunction(new JigsawJunction(blockPos3.getX(), t - j + r, blockPos3.getZ(), o, projection2));
                                                poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), t - n + s, blockPos2.getZ(), -o, projection));
                                                this.children.add(poolStructurePiece);
                                                if (minY + 1 <= this.maxSize) {
                                                    this.structurePieces.addLast(new ShapedPoolStructurePiece(poolStructurePiece, mutableObject2, minY + 1));
                                                }
                                                continue label129;
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

        private static RegistryKey<StructurePool> getPoolKey(StructureTemplate.StructureBlockInfo blockInfo) {
            return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, new Identifier(blockInfo.nbt().getString("pool")));
        }
    }
//    static final class StructurePoolGenerator {
//        private final Registry<StructurePool> registry;
//        private final int maxSize;
//        private final ChunkGenerator chunkGenerator;
//        private final StructureTemplateManager structureTemplateManager;
//        private final List<? super PoolStructurePiece> children;
//        private final Random random;
//        final PriorityIterator<FixedRotationStructurePoolBasedGenerator.ShapedPoolStructurePiece> structurePieces = new PriorityIterator();
//
//        StructurePoolGenerator(
//                Registry<StructurePool> registry,
//                int maxSize,
//                ChunkGenerator chunkGenerator,
//                StructureTemplateManager structureTemplateManager,
//                List<? super PoolStructurePiece> children,
//                Random random
//        ) {
//            this.registry = registry;
//            this.maxSize = maxSize;
//            this.chunkGenerator = chunkGenerator;
//            this.structureTemplateManager = structureTemplateManager;
//            this.children = children;
//            this.random = random;
//        }
//
//        void generatePiece(
//                PoolStructurePiece piece,
//                MutableObject<VoxelShape> pieceShape,
//                int minY,
//                boolean modifyBoundingBox,
//                HeightLimitView world,
//                NoiseConfig noiseConfig/*,
//                StructurePoolAliasLookup aliasLookup*/
//        ) {
//            StructurePoolElement structurePoolElement = piece.getPoolElement();
//            BlockPos blockPos = piece.getPos();
//            BlockRotation blockRotation = piece.getRotation();
//            StructurePool.Projection projection = structurePoolElement.getProjection();
//            boolean bl = projection == StructurePool.Projection.RIGID;
//            MutableObject<VoxelShape> mutableObject = new MutableObject<>();
//            BlockBox blockBox = piece.getBoundingBox();
//            int i = blockBox.getMinY();
//
//            label134:
//            for(StructureTemplate.StructureBlockInfo structureBlockInfo : structurePoolElement.getStructureBlockInfos(
//                    this.structureTemplateManager, blockPos, blockRotation, this.random
//            )) {
//                Direction direction = JigsawBlock.getFacing(structureBlockInfo.state());
//                BlockPos blockPos2 = structureBlockInfo.pos();
//                BlockPos blockPos3 = blockPos2.offset(direction);
//                int j = blockPos2.getY() - i;
//                int k = -1;
//                RegistryKey<StructurePool> registryKey = lookupPool(structureBlockInfo, aliasLookup);
//                Optional<? extends RegistryEntry<StructurePool>> optional = this.registry.getEntry(registryKey);
//                if (optional.isEmpty()) {
//                    FixedRotationStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", registryKey.getValue());
//                } else {
//                    RegistryEntry<StructurePool> registryEntry = (RegistryEntry)optional.get();
//                    if (registryEntry.value().getElementCount() == 0 && !registryEntry.matchesKey(StructurePools.EMPTY)) {
//                        FixedRotationStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", registryKey.getValue());
//                    } else {
//                        RegistryEntry<StructurePool> registryEntry2 = registryEntry.value().getFallback();
//                        if (registryEntry2.value().getElementCount() == 0 && !registryEntry2.matchesKey(StructurePools.EMPTY)) {
//                            FixedRotationStructurePoolBasedGenerator.LOGGER
//                                    .warn("Empty or non-existent fallback pool: {}", registryEntry2.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
//                        } else {
//                            boolean bl2 = blockBox.contains(blockPos3);
//                            MutableObject<VoxelShape> mutableObject2;
//                            if (bl2) {
//                                mutableObject2 = mutableObject;
//                                if (mutableObject.getValue() == null) {
//                                    mutableObject.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
//                                }
//                            } else {
//                                mutableObject2 = pieceShape;
//                            }
//
//                            List<StructurePoolElement> list = Lists.<StructurePoolElement>newArrayList();
//                            if (minY != this.maxSize) {
//                                list.addAll(registryEntry.value().getElementIndicesInRandomOrder(this.random));
//                            }
//
//                            list.addAll(registryEntry2.value().getElementIndicesInRandomOrder(this.random));
//                            int l = structureBlockInfo.nbt() != null ? structureBlockInfo.nbt().getInt("placement_priority") : 0;
//
//                            for(StructurePoolElement structurePoolElement2 : list) {
//                                if (structurePoolElement2 == EmptyPoolElement.INSTANCE) {
//                                    break;
//                                }
//
//                                for(BlockRotation blockRotation2 : BlockRotation.randomRotationOrder(this.random)) {
//                                    List<StructureTemplate.StructureBlockInfo> list2 = structurePoolElement2.getStructureBlockInfos(
//                                            this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2, this.random
//                                    );
//                                    BlockBox blockBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, blockRotation2);
//                                    int m;
//                                    if (modifyBoundingBox && blockBox2.getBlockCountY() <= 16) {
//                                        m = list2.stream().mapToInt(structureBlockInfox -> {
//                                            if (!blockBox2.contains(structureBlockInfox.pos().offset(JigsawBlock.getFacing(structureBlockInfox.state())))) {
//                                                return 0;
//                                            } else {
//                                                RegistryKey<StructurePool> registryKeyxx = lookupPool(structureBlockInfox, aliasLookup);
//                                                Optional<? extends RegistryEntry<StructurePool>> optionalxx = this.registry.getEntry(registryKeyxx);
//                                                Optional<RegistryEntry<StructurePool>> optional2 = optionalxx.map(entry -> ((StructurePool)entry.value()).getFallback());
//                                                int ixx = optionalxx.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
//                                                int jxx = optional2.map(entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)).orElse(0);
//                                                return Math.max(ixx, jxx);
//                                            }
//                                        }).max().orElse(0);
//                                    } else {
//                                        m = 0;
//                                    }
//
//                                    for(StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
//                                        if (JigsawBlock.attachmentMatches(structureBlockInfo, structureBlockInfo2)) {
//                                            BlockPos blockPos4 = structureBlockInfo2.pos();
//                                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
//                                            BlockBox blockBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, blockRotation2);
//                                            int n = blockBox3.getMinY();
//                                            StructurePool.Projection projection2 = structurePoolElement2.getProjection();
//                                            boolean bl3 = projection2 == StructurePool.Projection.RIGID;
//                                            int o = blockPos4.getY();
//                                            int p = j - o + JigsawBlock.getFacing(structureBlockInfo.state()).getOffsetY();
//                                            int q;
//                                            if (bl && bl3) {
//                                                q = i + p;
//                                            } else {
//                                                if (k == -1) {
//                                                    k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
//                                                }
//
//                                                q = k - o;
//                                            }
//
//                                            int r = q - n;
//                                            BlockBox blockBox4 = blockBox3.offset(0, r, 0);
//                                            BlockPos blockPos6 = blockPos5.add(0, r, 0);
//                                            if (m > 0) {
//                                                int s = Math.max(m + 1, blockBox4.getMaxY() - blockBox4.getMinY());
//                                                blockBox4.encompass(new BlockPos(blockBox4.getMinX(), blockBox4.getMinY() + s, blockBox4.getMinZ()));
//                                            }
//
//                                            if (!VoxelShapes.matchesAnywhere(mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4).contract(0.25)), BooleanBiFunction.ONLY_SECOND)) {
//                                                mutableObject2.setValue(VoxelShapes.combine(mutableObject2.getValue(), VoxelShapes.cuboid(Box.from(blockBox4)), BooleanBiFunction.ONLY_FIRST));
//                                                int s = piece.getGroundLevelDelta();
//                                                int t;
//                                                if (bl3) {
//                                                    t = s - p;
//                                                } else {
//                                                    t = structurePoolElement2.getGroundLevelDelta();
//                                                }
//
//                                                PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
//                                                        this.structureTemplateManager, structurePoolElement2, blockPos6, t, blockRotation2, blockBox4
//                                                );
//                                                int u;
//                                                if (bl) {
//                                                    u = i + j;
//                                                } else if (bl3) {
//                                                    u = q + o;
//                                                } else {
//                                                    if (k == -1) {
//                                                        k = this.chunkGenerator.getHeightOnGround(blockPos2.getX(), blockPos2.getZ(), Heightmap.Type.WORLD_SURFACE_WG, world, noiseConfig);
//                                                    }
//
//                                                    u = k + p / 2;
//                                                }
//
//                                                piece.addJunction(new JigsawJunction(blockPos3.getX(), u - j + s, blockPos3.getZ(), p, projection2));
//                                                poolStructurePiece.addJunction(new JigsawJunction(blockPos2.getX(), u - o + t, blockPos2.getZ(), -p, projection));
//                                                this.children.add(poolStructurePiece);
//                                                if (minY + 1 <= this.maxSize) {
//                                                    FixedRotationStructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = new FixedRotationStructurePoolBasedGenerator.ShapedPoolStructurePiece(
//                                                            poolStructurePiece, mutableObject2, minY + 1
//                                                    );
//                                                    this.structurePieces.enqueue(shapedPoolStructurePiece, l);
//                                                }
//                                                continue label134;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        private static RegistryKey<StructurePool> lookupPool(StructureTemplate.StructureBlockInfo structureBlockInfo, StructurePoolAliasLookup aliasLookup) {
//            NbtCompound nbtCompound = (NbtCompound)Objects.requireNonNull(structureBlockInfo.nbt(), () -> structureBlockInfo + " nbt was null");
//            RegistryKey<StructurePool> registryKey = StructurePools.of(nbtCompound.getString("pool"));
//            return aliasLookup.lookup(registryKey);
//        }
//    }
}
