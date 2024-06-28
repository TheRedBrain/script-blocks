package com.github.theredbrain.scriptblocks.util;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

public class PacketByteBufUtils {

	public static class MutablePairStringEntityAttributeModifierReader implements PacketByteBuf.PacketReader<MutablePair<String, EntityAttributeModifier>> {
		@Override
		public MutablePair<String, EntityAttributeModifier> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readString(), EntityAttributeModifier.fromNbt(packetByteBuf.readNbt()));
		}
	}

	public static class MutablePairStringEntityAttributeModifierWriter implements PacketByteBuf.PacketWriter<MutablePair<String, EntityAttributeModifier>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<String, EntityAttributeModifier> stringEntityAttributeModifierPair) {
			packetByteBuf.writeString(stringEntityAttributeModifierPair.getLeft());
			packetByteBuf.writeNbt(stringEntityAttributeModifierPair.getRight().toNbt());
		}
	}

	public static class MutablePairStringStringReader implements PacketByteBuf.PacketReader<Pair<String, String>> {
		@Override
		public Pair<String, String> apply(PacketByteBuf packetByteBuf) {
			return new Pair<>(packetByteBuf.readString(), packetByteBuf.readString());
		}
	}

	public static class MutablePairStringStringWriter implements PacketByteBuf.PacketWriter<Pair<String, String>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, Pair<String, String> stringStringPair) {
			packetByteBuf.writeString(stringStringPair.getLeft());
			packetByteBuf.writeString(stringStringPair.getRight());
		}
	}

	public static class MutablePairStringMutablePairBlockPosMutablePairDoubleDoubleReader implements PacketByteBuf.PacketReader<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> {
		@Override
		public MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readString(), new MutablePair<>(packetByteBuf.readBlockPos(), new MutablePair<>(packetByteBuf.readDouble(), packetByteBuf.readDouble())));
		}
	}

	public static class MutablePairStringMutablePairBlockPosMutablePairDoubleDoubleWriter implements PacketByteBuf.PacketWriter<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> pairStringPairBlockPosPairDoubleDouble) {
			packetByteBuf.writeString(pairStringPairBlockPosPairDoubleDouble.getLeft());
			packetByteBuf.writeBlockPos(pairStringPairBlockPosPairDoubleDouble.getRight().getLeft());
			packetByteBuf.writeDouble(pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getLeft());
			packetByteBuf.writeDouble(pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getRight());
		}
	}

	public static class BlockPosReader implements PacketByteBuf.PacketReader<BlockPos> {
		@Override
		public BlockPos apply(PacketByteBuf packetByteBuf) {
			return new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt());
		}
	}

	public static class BlockPosWriter implements PacketByteBuf.PacketWriter<BlockPos> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, BlockPos blockPos) {
			packetByteBuf.writeInt(blockPos.getX());
			packetByteBuf.writeInt(blockPos.getY());
			packetByteBuf.writeInt(blockPos.getZ());
		}
	}

	public static class MutablePairStringBlockPosReader implements PacketByteBuf.PacketReader<MutablePair<String, BlockPos>> {
		@Override
		public MutablePair<String, BlockPos> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readString(), new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()));
		}
	}

	public static class MutablePairStringBlockPosWriter implements PacketByteBuf.PacketWriter<MutablePair<String, BlockPos>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<String, BlockPos> pairStringBlockPos) {
			packetByteBuf.writeString(pairStringBlockPos.getLeft());
			packetByteBuf.writeInt(pairStringBlockPos.getRight().getX());
			packetByteBuf.writeInt(pairStringBlockPos.getRight().getY());
			packetByteBuf.writeInt(pairStringBlockPos.getRight().getZ());
		}
	}

	public static class MutablePairStringMutablePairBlockPosBooleanReader implements PacketByteBuf.PacketReader<MutablePair<String, MutablePair<BlockPos, Boolean>>> {
		@Override
		public MutablePair<String, MutablePair<BlockPos, Boolean>> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readString(), new MutablePair<>(new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()), packetByteBuf.readBoolean()));
		}
	}

	public static class MutablePairStringMutablePairBlockPosBooleanWriter implements PacketByteBuf.PacketWriter<MutablePair<String, MutablePair<BlockPos, Boolean>>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<String, MutablePair<BlockPos, Boolean>> pairStringPairBlockPosBoolean) {
			packetByteBuf.writeString(pairStringPairBlockPosBoolean.getLeft());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getX());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getY());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getZ());
			packetByteBuf.writeBoolean(pairStringPairBlockPosBoolean.getRight().getRight());
		}
	}

	public static class MutablePairIntegerMutablePairBlockPosBooleanReader implements PacketByteBuf.PacketReader<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> {
		@Override
		public MutablePair<Integer, MutablePair<BlockPos, Boolean>> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readInt(), new MutablePair<>(new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()), packetByteBuf.readBoolean()));
		}
	}

	public static class MutablePairIntegerMutablePairBlockPosBooleanWriter implements PacketByteBuf.PacketWriter<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<Integer, MutablePair<BlockPos, Boolean>> pairStringPairBlockPosBoolean) {
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getLeft());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getX());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getY());
			packetByteBuf.writeInt(pairStringPairBlockPosBoolean.getRight().getLeft().getZ());
			packetByteBuf.writeBoolean(pairStringPairBlockPosBoolean.getRight().getRight());
		}
	}

	public static class MutablePairBlockPosBooleanReader implements PacketByteBuf.PacketReader<MutablePair<BlockPos, Boolean>> {
		@Override
		public MutablePair<BlockPos, Boolean> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()), packetByteBuf.readBoolean());
		}
	}

	public static class MutablePairBlockPosBooleanWriter implements PacketByteBuf.PacketWriter<MutablePair<BlockPos, Boolean>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<BlockPos, Boolean> pairBlockPosBoolean) {
			packetByteBuf.writeInt(pairBlockPosBoolean.getLeft().getX());
			packetByteBuf.writeInt(pairBlockPosBoolean.getLeft().getY());
			packetByteBuf.writeInt(pairBlockPosBoolean.getLeft().getZ());
			packetByteBuf.writeBoolean(pairBlockPosBoolean.getRight());
		}
	}

	public static class MutablePairMutablePairBlockPosBooleanIntegerReader implements PacketByteBuf.PacketReader<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> {
		@Override
		public MutablePair<MutablePair<BlockPos, Boolean>, Integer> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(new MutablePair<>(new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()), packetByteBuf.readBoolean()), packetByteBuf.readInt());
		}
	}

	public static class MutablePairMutablePairBlockPosBooleanIntegerWriter implements PacketByteBuf.PacketWriter<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<MutablePair<BlockPos, Boolean>, Integer> mutablePairMutablePairBlockPosBooleanInteger) {
			packetByteBuf.writeInt(mutablePairMutablePairBlockPosBooleanInteger.getLeft().getLeft().getX());
			packetByteBuf.writeInt(mutablePairMutablePairBlockPosBooleanInteger.getLeft().getLeft().getY());
			packetByteBuf.writeInt(mutablePairMutablePairBlockPosBooleanInteger.getLeft().getLeft().getZ());
			packetByteBuf.writeBoolean(mutablePairMutablePairBlockPosBooleanInteger.getLeft().getRight());
			packetByteBuf.writeInt(mutablePairMutablePairBlockPosBooleanInteger.getRight());
		}
	}

	public static class MutablePairIntegerBlockPosReader implements PacketByteBuf.PacketReader<MutablePair<Integer, BlockPos>> {
		@Override
		public MutablePair<Integer, BlockPos> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readInt(), new BlockPos(packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readInt()));
		}
	}

	public static class MutablePairIntegerBlockPosWriter implements PacketByteBuf.PacketWriter<MutablePair<Integer, BlockPos>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<Integer, BlockPos> pairIntegerBlockPos) {
			packetByteBuf.writeInt(pairIntegerBlockPos.getLeft());
			packetByteBuf.writeInt(pairIntegerBlockPos.getRight().getX());
			packetByteBuf.writeInt(pairIntegerBlockPos.getRight().getY());
			packetByteBuf.writeInt(pairIntegerBlockPos.getRight().getZ());
		}
	}

	public static class MutablePairStringMutablePairStringStringReader implements PacketByteBuf.PacketReader<MutablePair<String, MutablePair<String, String>>> {
		@Override
		public MutablePair<String, MutablePair<String, String>> apply(PacketByteBuf packetByteBuf) {
			return new MutablePair<>(packetByteBuf.readString(), new MutablePair<>(packetByteBuf.readString(), packetByteBuf.readString()));
		}
	}

	public static class MutablePairStringMutablePairStringStringWriter implements PacketByteBuf.PacketWriter<MutablePair<String, MutablePair<String, String>>> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, MutablePair<String, MutablePair<String, String>> pairStringPairStringString) {
			packetByteBuf.writeString(pairStringPairStringString.getLeft());
			packetByteBuf.writeString(pairStringPairStringString.getRight().getLeft());
			packetByteBuf.writeString(pairStringPairStringString.getRight().getRight());
		}
	}

	public static class StringReader implements PacketByteBuf.PacketReader<String> {
		@Override
		public String apply(PacketByteBuf packetByteBuf) {
			return packetByteBuf.readString();
		}
	}

	public static class StringWriter implements PacketByteBuf.PacketWriter<String> {
		@Override
		public void accept(PacketByteBuf packetByteBuf, String string) {
			packetByteBuf.writeString(string);
		}
	}
}
