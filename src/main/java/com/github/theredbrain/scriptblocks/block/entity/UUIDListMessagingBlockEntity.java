package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UUIDListMessagingBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private MessageMode messageMode = MessageMode.CHAT;
	private String messageLocalisationString = "";
	private String subTitleLocalisationString = "";
	private int titleFadeIn = 0;
	private int titleStay = 0;
	private int titleFadeOut = 0;
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private String dataIdentifier = "";

	public UUIDListMessagingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public UUIDListMessagingBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.UUID_LIST_MESSAGING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putString("message_mode", this.messageMode.asString());

		nbt.putString("message_localisation_string", this.messageLocalisationString);

		nbt.putString("subtitle_localisation_string", this.subTitleLocalisationString);

		nbt.putInt("title_fade_in", this.titleFadeIn);

		nbt.putInt("title_stay", this.titleStay);

		nbt.putInt("title_fade_out", this.titleFadeOut);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.dataProvidingBlockPosOffset));
		nbtCompound.putString("data_identifier", this.dataIdentifier);
		nbt.put("data_provider", nbtCompound);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.messageMode = MessageMode.byName(nbt.getString("message_mode")).orElse(MessageMode.CHAT);

		this.messageLocalisationString = nbt.getString("message_localisation_string");

		this.subTitleLocalisationString = nbt.getString("subtitle_localisation_string");

		this.titleFadeIn = nbt.getInt("title_fade_in");

		this.titleStay = nbt.getInt("title_stay");

		this.titleFadeOut = nbt.getInt("title_fade_out");

		NbtCompound nbtCompound = nbt.getCompound("data_provider");
		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);
		this.dataIdentifier = nbtCompound.getString("data_identifier");

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	// region --- getter & setter ---
	public MessageMode getMessageMode() {
		return this.messageMode;
	}

	public void setMessageMode(MessageMode messageMode) {
		this.messageMode = messageMode;
	}

	public String getMessageLocalisationString() {
		return this.messageLocalisationString;
	}

	public void setMessageLocalisationString(String messageLocalisationString) {
		this.messageLocalisationString = messageLocalisationString;
	}

	public String getSubTitleLocalisationString() {
		return this.subTitleLocalisationString;
	}

	public void setSubTitleLocalisationString(String subTitleLocalisationString) {
		this.subTitleLocalisationString = subTitleLocalisationString;
	}

	public int getTitleFadeIn() {
		return this.titleFadeIn;
	}

	public void setTitleFadeIn(int titleFadeIn) {
		this.titleFadeIn = titleFadeIn;
	}

	public int getTitleStay() {
		return this.titleStay;
	}

	public void setTitleStay(int titleStay) {
		this.titleStay = titleStay;
	}

	public int getTitleFadeOut() {
		return this.titleFadeOut;
	}

	public void setTitleFadeOut(int titleFadeOut) {
		this.titleFadeOut = titleFadeOut;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = new BlockPos(dataProvidingBlockPosOffset);
	}

	public String getDataIdentifier() {
		return this.dataIdentifier;
	}

	public void setDataIdentifier(String dataIdentifier) {
		this.dataIdentifier = dataIdentifier;
	}
	// endregion --- getter & setter ---

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	public Text getMessageText() {
		String dataProvidedMessageAppendix = "";
		if (this.getWorld() != null && this.getWorld().getBlockEntity(getActualOffsetBlockPosition(this.getPos(), this.dataProvidingBlockPosOffset)) instanceof ProvidesData providesData) {
			dataProvidedMessageAppendix = providesData.getData(this.dataIdentifier);
		}
		return Text.translatable(this.messageLocalisationString + dataProvidedMessageAppendix);
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		Iterator<UUID> listIterator = list.iterator();
		PlayerEntity playerEntity;
		UUID uuid;
		if (this.getWorld() != null) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = this.getWorld().getPlayerByUuid(uuid);

				if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
					if (this.messageMode == MessageMode.TITLE) {
						serverPlayerEntity.networkHandler.sendPacket(new TitleFadeS2CPacket(this.titleFadeIn, this.titleStay, this.titleFadeOut));
						serverPlayerEntity.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable(this.subTitleLocalisationString)));
						serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(this.messageLocalisationString)));
					} else {
						serverPlayerEntity.sendMessage(this.getMessageText(), this.messageMode == MessageMode.OVERLAY);
					}
				}
			}
		}
	}

	@Override
	public void reset() {
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum MessageMode implements StringIdentifiable {
		CHAT("chat"),
		OVERLAY("overlay"),
		TITLE("title");

		private final String name;

		MessageMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<MessageMode> byName(String name) {
			return Arrays.stream(MessageMode.values()).filter(messageMode -> messageMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.uuid_list_messaging_block.message_mode." + this.name);
		}
	}

}
