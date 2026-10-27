package com.github.theredbrain.scriptblocks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.apache.commons.lang3.tuple.MutablePair;

public class BlockRotationUtils {

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	public static int calculateNewRotatedBlockState(int currentStateValue, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			return (currentStateValue + 1) % 4;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			return (currentStateValue + 2) % 4;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			return (currentStateValue + 3) % 4;
		} else {
			return currentStateValue;
		}
	}

	public static Direction calculateNewHorizontalFacingBlockState(Direction currentState, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			return currentState.getClockWise();
		} else if (rotation == Rotation.CLOCKWISE_180) {
			return currentState.getOpposite();
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			return currentState.getCounterClockWise();
		} else {
			return currentState;
		}
	}

	public static Rotation calculateRotationFromDifferentRotatedStates(int value1, int value2) {
		if ((value2 + 1) % 4 == value1) {
			return Rotation.CLOCKWISE_90;
		} else if ((value2 + 2) % 4 == value1) {
			return Rotation.CLOCKWISE_180;
		} else if ((value2 + 3) % 4 == value1) {
			return Rotation.COUNTERCLOCKWISE_90;
		} else {
			return Rotation.NONE;
		}
	}

	public static BlockPos rotateOffsetBlockPos(BlockPos blockPos, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			return new BlockPos(-(blockPos.getZ()), blockPos.getY(), blockPos.getX());
		} else if (rotation == Rotation.CLOCKWISE_180) {
			return new BlockPos(-(blockPos.getX()), blockPos.getY(), -(blockPos.getZ()));
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			return new BlockPos(blockPos.getZ(), blockPos.getY(), -(blockPos.getX()));
		} else {
			return new BlockPos(blockPos);
		}
	}

	public static BlockPos mirrorOffsetBlockPos(BlockPos blockPos, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return new BlockPos(-(blockPos.getX()), blockPos.getY(), blockPos.getZ());
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return new BlockPos(blockPos.getX(), blockPos.getY(), -(blockPos.getZ()));
		} else {
			return new BlockPos(blockPos);
		}
	}

	public static Vec3i rotateOffsetVec3i(Vec3i vec3i, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			return new Vec3i(-(vec3i.getZ()), vec3i.getY(), vec3i.getX());
		} else if (rotation == Rotation.CLOCKWISE_180) {
			return new Vec3i(-(vec3i.getX()), vec3i.getY(), -(vec3i.getZ()));
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			return new Vec3i(vec3i.getZ(), vec3i.getY(), -(vec3i.getX()));
		} else {
			return new Vec3i(vec3i.getX(), vec3i.getY(), vec3i.getZ());
		}
	}

	public static Vec3i mirrorOffsetVec3i(Vec3i vec3i, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return new Vec3i(-(vec3i.getX()), vec3i.getY(), vec3i.getZ());
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return new Vec3i(vec3i.getX(), vec3i.getY(), -(vec3i.getZ()));
		} else {
			return new Vec3i(vec3i.getX(), vec3i.getY(), vec3i.getZ());
		}
	}

	public static double rotateYaw(double yaw, Rotation rotation) {
		double tempYaw = yaw + 180;
		if (rotation == Rotation.CLOCKWISE_90) {
			tempYaw = (tempYaw + 90) % 360;
			return tempYaw - 180;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			tempYaw = (tempYaw + 180) % 360;
			return tempYaw - 180;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			tempYaw = (tempYaw - 90) % 360;
			return tempYaw - 180;
		} else {
			return yaw;
		}

	}

	public static double mirrorYaw(double yaw, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return -yaw;
		} else if (mirror == Mirror.LEFT_RIGHT) {
			if (yaw < -90) {
				return yaw + 90;
			} else if (yaw < 0) {
				return yaw - 90;
			} else if (yaw > 90) {
				return yaw - 90;
			} else if (yaw > 0) {
				return yaw + 90;
			} else {
				return yaw;
			}
		} else {
			return yaw;
		}
	}

	public static MutablePair<Integer, Integer> rotateOffset2DPosition(MutablePair<Integer, Integer> offsetPosition, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			return MutablePair.of(-(offsetPosition.getRight()), offsetPosition.getLeft());
		} else if (rotation == Rotation.CLOCKWISE_180) {
			return MutablePair.of(-(offsetPosition.getLeft()), -(offsetPosition.getRight()));
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			return MutablePair.of(offsetPosition.getRight(), -(offsetPosition.getLeft()));
		} else {
			return MutablePair.of(offsetPosition.getLeft(), offsetPosition.getRight());
		}
	}

	public static MutablePair<Integer, Integer> mirrorOffset2DPosition(MutablePair<Integer, Integer> offsetPosition, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return MutablePair.of(-(offsetPosition.getLeft()), offsetPosition.getRight());
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return MutablePair.of(offsetPosition.getLeft(), -(offsetPosition.getRight()));
		} else {
			return MutablePair.of(offsetPosition.getLeft(), offsetPosition.getRight());
		}
	}

	public static MutablePair<BlockPos, MutablePair<Double, Double>> rotateSpawnPoint(MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint, Rotation rotation) {
		return new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(spawnPoint.getLeft(), rotation), new MutablePair<>(BlockRotationUtils.rotateYaw(spawnPoint.getRight().getLeft(), rotation), spawnPoint.getRight().getRight()));
	}

	public static MutablePair<BlockPos, MutablePair<Double, Double>> mirrorSpawnPoint(MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint, Mirror mirror) {
		return new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(spawnPoint.getLeft(), mirror), new MutablePair<>(BlockRotationUtils.mirrorYaw(spawnPoint.getRight().getLeft(), mirror), spawnPoint.getRight().getRight()));
	}

	public static MutablePair<BlockPos, Vec3i> rotateOffsetArea(BlockPos positionOffset, Vec3i areaDimensions, Rotation rotation) {
		if (rotation == Rotation.CLOCKWISE_90) {
			Vec3i newAreaDimensions = new Vec3i(areaDimensions.getZ(), areaDimensions.getY(), areaDimensions.getX());
			BlockPos newPositionOffset = new BlockPos(-(positionOffset.getZ()) - newAreaDimensions.getX() + 1, positionOffset.getY(), positionOffset.getX());
			return MutablePair.of(newPositionOffset, newAreaDimensions);
		} else if (rotation == Rotation.CLOCKWISE_180) {
			BlockPos newPositionOffset = new BlockPos(-(positionOffset.getX()) - areaDimensions.getX() + 1, positionOffset.getY(), -(positionOffset.getZ()) - areaDimensions.getZ() + 1);
			return MutablePair.of(newPositionOffset, areaDimensions);
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			Vec3i newAreaDimensions = new Vec3i(areaDimensions.getZ(), areaDimensions.getY(), areaDimensions.getX());
			BlockPos newPositionOffset = new BlockPos(positionOffset.getZ(), positionOffset.getY(), -(positionOffset.getX()) - newAreaDimensions.getZ() + 1);
			return MutablePair.of(newPositionOffset, newAreaDimensions);
		} else {
			return MutablePair.of(positionOffset, areaDimensions);
		}
	}

	public static MutablePair<BlockPos, Vec3i> mirrorOffsetArea(BlockPos positionOffset, Vec3i areaDimensions, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			BlockPos newPositionOffset = new BlockPos(-(positionOffset.getX()) - areaDimensions.getX() + 1, positionOffset.getY(), positionOffset.getZ());
			return MutablePair.of(newPositionOffset, areaDimensions);
		} else if (mirror == Mirror.LEFT_RIGHT) {
			BlockPos newPositionOffset = new BlockPos(positionOffset.getX(), positionOffset.getY(), -(positionOffset.getZ()) - areaDimensions.getZ() + 1);
			return MutablePair.of(newPositionOffset, areaDimensions);
		} else {
			return MutablePair.of(positionOffset, areaDimensions);
		}
	}
}
