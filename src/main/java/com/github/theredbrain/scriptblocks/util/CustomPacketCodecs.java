package com.github.theredbrain.scriptblocks.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

public class CustomPacketCodecs {

	public static final StreamCodec<ByteBuf, MutablePair<String, AttributeModifier>> MUTABLE_PAIR_STRING_ENTITY_ATTRIBUTE_MODIFIER = new StreamCodec<>() {
		public MutablePair<String, AttributeModifier> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), AttributeModifier.STREAM_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, AttributeModifier> pairStringEntityAttributeModifier) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringEntityAttributeModifier.getLeft());
			AttributeModifier.STREAM_CODEC.encode(byteBuf, pairStringEntityAttributeModifier.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<Identifier, AttributeModifier>> MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER = new StreamCodec<>() {
		public MutablePair<Identifier, AttributeModifier> decode(ByteBuf byteBuf) {
			return new MutablePair<>(Identifier.STREAM_CODEC.decode(byteBuf), AttributeModifier.STREAM_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Identifier, AttributeModifier> pairIdentifierEntityAttributeModifier) {
			Identifier.STREAM_CODEC.encode(byteBuf, pairIdentifierEntityAttributeModifier.getLeft());
			AttributeModifier.STREAM_CODEC.encode(byteBuf, pairIdentifierEntityAttributeModifier.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, String>> MUTABLE_PAIR_STRING_STRING = new StreamCodec<>() {
		public MutablePair<String, String> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, String> pairStringString) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_INTEGER = new StreamCodec<>() {
		public MutablePair<MutablePair<String, String>, MutablePair<String, Integer>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)), new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.INT.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<MutablePair<String, String>, MutablePair<String, Integer>> pairStringString) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getRight());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getRight().getLeft());
			ByteBufCodecs.INT.encode(byteBuf, pairStringString.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<MutablePair<String, String>, MutablePair<String, String>>> MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING = new StreamCodec<>() {
		public MutablePair<MutablePair<String, String>, MutablePair<String, String>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)), new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<MutablePair<String, String>, MutablePair<String, String>> pairStringString) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getRight());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getRight().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<MutablePair<MutablePair<String, String>, MutablePair<String, String>>, Boolean>> MUTABLE_PAIR_MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING_BOOLEAN = new StreamCodec<>() {
		public MutablePair<MutablePair<MutablePair<String, String>, MutablePair<String, String>>, Boolean> decode(ByteBuf byteBuf) {
			return new MutablePair<>(new MutablePair<>(new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)), new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf))), ByteBufCodecs.BOOL.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<MutablePair<MutablePair<String, String>, MutablePair<String, String>>, Boolean> pairStringString) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getLeft().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getLeft().getRight());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getRight().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringString.getLeft().getRight().getRight());
			ByteBufCodecs.BOOL.encode(byteBuf, pairStringString.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE = new StreamCodec<>() {
		public MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), new MutablePair<>(ByteBufCodecs.DOUBLE.decode(byteBuf), ByteBufCodecs.DOUBLE.decode(byteBuf))));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>> pairStringPairBlockPosPairDoubleDouble) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getLeft());
			BlockPos.STREAM_CODEC.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getLeft());
			ByteBufCodecs.DOUBLE.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getLeft());
			ByteBufCodecs.DOUBLE.encode(byteBuf, pairStringPairBlockPosPairDoubleDouble.getRight().getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, LevelData.RespawnData>> MUTABLE_PAIR_STRING_RESPAWN_DATA = new StreamCodec<>() {
		public MutablePair<String, LevelData.RespawnData> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), LevelData.RespawnData.STREAM_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, LevelData.RespawnData> pairStringRespawnData) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringRespawnData.getLeft());
			LevelData.RespawnData.STREAM_CODEC.encode(byteBuf, pairStringRespawnData.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, BlockPos>> MUTABLE_PAIR_STRING_BLOCK_POS = new StreamCodec<>() {
		public MutablePair<String, BlockPos> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), BlockPos.STREAM_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, BlockPos> pairStringBlockPos) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringBlockPos.getLeft());
			BlockPos.STREAM_CODEC.encode(byteBuf, pairStringBlockPos.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, MutablePair<BlockPos, Boolean>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new StreamCodec<>() {
		public MutablePair<String, MutablePair<BlockPos, Boolean>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), ByteBufCodecs.BOOL.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<BlockPos, Boolean>> pairStringPairBlockPosPairBoolean) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairBlockPosPairBoolean.getLeft());
			BlockPos.STREAM_CODEC.encode(byteBuf, pairStringPairBlockPosPairBoolean.getRight().getLeft());
			ByteBufCodecs.BOOL.encode(byteBuf, pairStringPairBlockPosPairBoolean.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<Integer, MutablePair<BlockPos, Boolean>>> MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new StreamCodec<>() {
		public MutablePair<Integer, MutablePair<BlockPos, Boolean>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.INT.decode(byteBuf), new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), ByteBufCodecs.BOOL.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Integer, MutablePair<BlockPos, Boolean>> pairIntegerPairBlockPosBoolean) {
			ByteBufCodecs.INT.encode(byteBuf, pairIntegerPairBlockPosBoolean.getLeft());
			BlockPos.STREAM_CODEC.encode(byteBuf, pairIntegerPairBlockPosBoolean.getRight().getLeft());
			ByteBufCodecs.BOOL.encode(byteBuf, pairIntegerPairBlockPosBoolean.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<BlockPos, Boolean>> MUTABLE_PAIR_BLOCK_POS_BOOLEAN = new StreamCodec<>() {
		public MutablePair<BlockPos, Boolean> decode(ByteBuf byteBuf) {
			return new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), ByteBufCodecs.BOOL.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<BlockPos, Boolean> pairBlockPosBoolean) {
			BlockPos.STREAM_CODEC.encode(byteBuf, pairBlockPosBoolean.getLeft());
			ByteBufCodecs.BOOL.encode(byteBuf, pairBlockPosBoolean.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<MutablePair<BlockPos, Boolean>, Integer>> MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER = new StreamCodec<>() {
		public MutablePair<MutablePair<BlockPos, Boolean>, Integer> decode(ByteBuf byteBuf) {
			return new MutablePair<>(new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), ByteBufCodecs.BOOL.decode(byteBuf)), ByteBufCodecs.INT.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<MutablePair<BlockPos, Boolean>, Integer> mutablePairMutablePairBlockPosBooleanInteger) {
			BlockPos.STREAM_CODEC.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getLeft().getLeft());
			ByteBufCodecs.BOOL.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getLeft().getRight());
			ByteBufCodecs.INT.encode(byteBuf, mutablePairMutablePairBlockPosBooleanInteger.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<Integer, BlockPos>> MUTABLE_PAIR_INTEGER_BLOCK_POS = new StreamCodec<>() {
		public MutablePair<Integer, BlockPos> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.INT.decode(byteBuf), BlockPos.STREAM_CODEC.decode(byteBuf));
		}

		public void encode(ByteBuf byteBuf, MutablePair<Integer, BlockPos> pairIntegerBlockPos) {
			ByteBufCodecs.INT.encode(byteBuf, pairIntegerBlockPos.getLeft());
			BlockPos.STREAM_CODEC.encode(byteBuf, pairIntegerBlockPos.getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<String, MutablePair<String, String>>> MUTABLE_PAIR_STRING_MUTABLE_PAIR_STRING_STRING = new StreamCodec<>() {
		public MutablePair<String, MutablePair<String, String>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<String, MutablePair<String, String>> pairStringPairStringString) {
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairStringString.getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairStringString.getRight().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairStringString.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<BlockPos, MutablePair<String, String>>> MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_STRING_STRING = new StreamCodec<>() {
		public MutablePair<BlockPos, MutablePair<String, String>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), new MutablePair<>(ByteBufCodecs.STRING_UTF8.decode(byteBuf), ByteBufCodecs.STRING_UTF8.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<BlockPos, MutablePair<String, String>> pairStringPairStringString) {
			BlockPos.STREAM_CODEC.encode(byteBuf, pairStringPairStringString.getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairStringString.getRight().getLeft());
			ByteBufCodecs.STRING_UTF8.encode(byteBuf, pairStringPairStringString.getRight().getRight());
		}
	};

	public static final StreamCodec<ByteBuf, MutablePair<BlockPos, MutablePair<Double, Double>>> MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE = new StreamCodec<>() {
		public MutablePair<BlockPos, MutablePair<Double, Double>> decode(ByteBuf byteBuf) {
			return new MutablePair<>(BlockPos.STREAM_CODEC.decode(byteBuf), new MutablePair<>(ByteBufCodecs.DOUBLE.decode(byteBuf), ByteBufCodecs.DOUBLE.decode(byteBuf)));
		}

		public void encode(ByteBuf byteBuf, MutablePair<BlockPos, MutablePair<Double, Double>> pairStringPairDoubleDouble) {
			BlockPos.STREAM_CODEC.encode(byteBuf, pairStringPairDoubleDouble.getLeft());
			ByteBufCodecs.DOUBLE.encode(byteBuf, pairStringPairDoubleDouble.getRight().getLeft());
			ByteBufCodecs.DOUBLE.encode(byteBuf, pairStringPairDoubleDouble.getRight().getRight());
		}
	};
}
