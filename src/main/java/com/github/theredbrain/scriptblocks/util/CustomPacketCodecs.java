package com.github.theredbrain.scriptblocks.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

public class CustomPacketCodecs {

	public static final PacketCodec<ByteBuf, MutablePair<String, EntityAttributeModifier>> MUTABLE_PAIR_STRING_ENTITY_ATTRIBUTE_MODIFIER = new PacketCodec<>() {
		public MutablePair<String, EntityAttributeModifier> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), EntityAttributeModifier.fromNbt(PacketCodecs.NBT_COMPOUND.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, EntityAttributeModifier> pairStringEntityAttributeModifier) {
			PacketCodecs.STRING.encode(byteBuf, pairStringEntityAttributeModifier.getLeft());
			PacketCodecs.NBT_COMPOUND.encode(byteBuf, pairStringEntityAttributeModifier.getRight().toNbt());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<Identifier, EntityAttributeModifier>> MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER = new PacketCodec<>() {
		public MutablePair<Identifier, EntityAttributeModifier> decode(ByteBuf byteBuf) {
			return new MutablePair<>(Identifier.PACKET_CODEC.decode(byteBuf), EntityAttributeModifier.fromNbt(PacketCodecs.NBT_COMPOUND.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Identifier, EntityAttributeModifier> pairIdentifierEntityAttributeModifier) {
			Identifier.PACKET_CODEC.encode(byteBuf, pairIdentifierEntityAttributeModifier.getLeft());
			PacketCodecs.NBT_COMPOUND.encode(byteBuf, pairIdentifierEntityAttributeModifier.getRight().toNbt());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<String, String>> MUTABLE_PAIR_STRING_STRING = new PacketCodec<>() {
		public MutablePair<String, String> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), PacketCodecs.STRING.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, String> pairStringString) {
			PacketCodecs.STRING.encode(byteBuf, pairStringString.getLeft());
			PacketCodecs.STRING.encode(byteBuf, pairStringString.getLeft());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE = new PacketCodec<>() {
		public MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), new MutablePair<>(BlockPos.PACKET_CODEC.decode(byteBuf), new MutablePair<>(PacketCodecs.DOUBLE.decode(byteBuf), PacketCodecs.DOUBLE.decode(byteBuf))));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> pairStringPairBlockPosPairDoubleDouble) {
			PacketCodecs.STRING.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getLeft());
			BlockPos.PACKET_CODEC.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getLeft());
			PacketCodecs.DOUBLE.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getLeft());
			PacketCodecs.DOUBLE.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<String, BlockPos>> MUTABLE_PAIR_STRING_BLOCK_POS = new PacketCodec<>() {
		public MutablePair<String, BlockPos> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), BlockPos.PACKET_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, BlockPos> pairStringBlockPos) {
			PacketCodecs.STRING.encode(byteBuf, pairStringBlockPos.getLeft());
			BlockPos.PACKET_CODEC.encode(byteBuf, pairStringBlockPos.getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<String, MutablePair<BlockPos, Boolean>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new PacketCodec<>() {
		public MutablePair<String, MutablePair<BlockPos, Boolean>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), new MutablePair<>(BlockPos.PACKET_CODEC.decode(byteBuf), PacketCodecs.BOOL.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<BlockPos, Boolean>> pairStringPairBlockPosPairBoolean) {
			PacketCodecs.STRING.encode(byteBuf, pairStringPairBlockPosPairBoolean.getLeft());
			BlockPos.PACKET_CODEC.encode(byteBuf, pairStringPairBlockPosPairBoolean.getRight().getLeft());
			PacketCodecs.BOOL.encode(byteBuf, pairStringPairBlockPosPairBoolean.getRight().getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<Integer, MutablePair<BlockPos, Boolean>>> MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new PacketCodec<>() {
		public MutablePair<Integer, MutablePair<BlockPos, Boolean>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.INTEGER.decode(byteBuf), new MutablePair<>(BlockPos.PACKET_CODEC.decode(byteBuf), PacketCodecs.BOOL.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Integer, MutablePair<BlockPos, Boolean>> pairIntegerPairBlockPosBoolean) {
			PacketCodecs.INTEGER.encode(byteBuf, pairIntegerPairBlockPosBoolean.getLeft());
			BlockPos.PACKET_CODEC.encode(byteBuf, pairIntegerPairBlockPosBoolean.getRight().getLeft());
			PacketCodecs.BOOL.encode(byteBuf, pairIntegerPairBlockPosBoolean.getRight().getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<BlockPos, Boolean>> MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new PacketCodec<>() {
		public MutablePair<BlockPos, Boolean> decode(ByteBuf byteBuf) {
			return new MutablePair<>(BlockPos.PACKET_CODEC.decode(byteBuf), PacketCodecs.BOOL.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<BlockPos, Boolean> pairBlockPosBoolean) {
			BlockPos.PACKET_CODEC.encode(byteBuf, pairBlockPosBoolean.getLeft());
			PacketCodecs.BOOL.encode(byteBuf, pairBlockPosBoolean.getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<MutablePair<BlockPos, Boolean>, Integer>> MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER = new PacketCodec<>() {
		public MutablePair<MutablePair<BlockPos, Boolean>, Integer> decode(ByteBuf byteBuf) {
			return new MutablePair<>(new MutablePair<>(BlockPos.PACKET_CODEC.decode(byteBuf), PacketCodecs.BOOL.decode(byteBuf)), PacketCodecs.INTEGER.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<MutablePair<BlockPos, Boolean>, Integer> mutablePairMutablePairBlockPosBooleanInteger) {
			BlockPos.PACKET_CODEC.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getLeft().getLeft());
			PacketCodecs.BOOL.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getLeft().getRight());
			PacketCodecs.INTEGER.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<Integer, BlockPos>> MUTABLE_PAIR_INTEGER_BLOCK_POS = new PacketCodec<>() {
		public MutablePair<Integer, BlockPos> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.INTEGER.decode(byteBuf), BlockPos.PACKET_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Integer, BlockPos> pairIntegerBlockPos) {
			PacketCodecs.INTEGER.encode(byteBuf, pairIntegerBlockPos.getLeft());
			BlockPos.PACKET_CODEC.encode(byteBuf, pairIntegerBlockPos.getRight());
		}
	};

	public static final PacketCodec<ByteBuf, MutablePair<String, MutablePair<String, String>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_STRING_STRING = new PacketCodec<>() {
		public MutablePair<String, MutablePair<String, String>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), new MutablePair<>(PacketCodecs.STRING.decode(byteBuf), PacketCodecs.STRING.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<String, String>> pairStringPairStringString) {
			PacketCodecs.STRING.encode(byteBuf, pairStringPairStringString.getLeft());
			PacketCodecs.STRING.encode(byteBuf, pairStringPairStringString.getRight().getLeft());
			PacketCodecs.STRING.encode(byteBuf, pairStringPairStringString.getRight().getRight());
		}
	};
}
