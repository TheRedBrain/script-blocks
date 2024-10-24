package com.github.theredbrain.scriptblocks.world.gen.chunk.placement;

import com.github.theredbrain.scriptblocks.registry.StructurePlacementTypesRegistry;
import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FixedStructurePlacement extends StructurePlacement {
	public static final MapCodec<FixedStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
			instance -> buildFixedStructuresCodec(instance).apply(instance, FixedStructurePlacement::new)
	);

	private final List<BlockPos> positions;
	private final List<BlockPos> blacklist;

	private static Products.P6<RecordCodecBuilder.Mu<FixedStructurePlacement>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>, List<BlockPos>> buildFixedStructuresCodec(
			RecordCodecBuilder.Instance<FixedStructurePlacement> instance
	) {
		Products.P5<RecordCodecBuilder.Mu<FixedStructurePlacement>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>> p5 = buildCodec(
				instance
		);
		Products.P1<RecordCodecBuilder.Mu<FixedStructurePlacement>, List<BlockPos>> p1 = instance.group(
				BlockPos.CODEC.listOf().fieldOf("positions").forGetter(FixedStructurePlacement::getPositions)
		);
		return new Products.P6<>(p5.t1(), p5.t2(), p5.t3(), p5.t4(), p5.t5(), p1.t1());
	}

	public FixedStructurePlacement(
			Vec3i locateOffset,
			StructurePlacement.FrequencyReductionMethod generationPredicateType,
			float frequency,
			int salt,
			Optional<StructurePlacement.ExclusionZone> exclusionZone,
			List<BlockPos> positions
	) {
		super(locateOffset, generationPredicateType, frequency, salt, exclusionZone);
		this.positions = positions;
		this.blacklist = new ArrayList<>();
	}

	public List<BlockPos> getPositions() {
		return this.positions;
	}

	protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
		if (this.positions.isEmpty()) {
			return false;
		} else {
			ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
			Iterator var5 = this.positions.iterator();

			while (var5.hasNext()) {
				BlockPos pos = (BlockPos) var5.next();
				if (!this.blacklist.contains(pos)) {
					long x = (long) chunkPos.x * 16L;
					long z = (long) chunkPos.z * 16L;
					boolean bl1 = (long) pos.getX() >= x;
					boolean bl2 = (long) pos.getX() <= x + 15L;
					boolean bl3 = (long) pos.getZ() >= z;
					boolean bl4 = (long) pos.getZ() <= z + 15L;
					if (bl1 && bl2 && bl3 && bl4) {
						this.blacklist.add(pos);
						return true;
					}
				}
			}

			return false;
		}
	}

	public boolean shouldGenerate(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
		return this.isStartChunk(calculator, chunkX, chunkZ);
	}

	public StructurePlacementType<?> getType() {
		return StructurePlacementTypesRegistry.FIXED;
	}
}
