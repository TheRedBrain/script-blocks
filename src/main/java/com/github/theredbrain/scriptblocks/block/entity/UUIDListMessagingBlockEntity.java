package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class UUIDListMessagingBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private MessageMode messageMode = MessageMode.CHAT;
	private String messageLocalisationString = "";
	private String subTitleLocalisationString = "";
	private int titleFadeIn = 0;
	private int titleStay = 0;
	private int titleFadeOut = 0;
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
	private String dataIdentifier = "";

	public UUIDListMessagingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public UUIDListMessagingBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.UUID_LIST_MESSAGING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("message_mode", this.messageMode.getSerializedName());

		view.putString("message_localisation_string", this.messageLocalisationString);

		view.putString("subtitle_localisation_string", this.subTitleLocalisationString);

		view.putInt("title_fade_in", this.titleFadeIn);

		view.putInt("title_stay", this.titleStay);

		view.putInt("title_fade_out", this.titleFadeOut);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);
		nbtCompound.putString("data_identifier", this.dataIdentifier);
		view.store("data_provider", CompoundTag.CODEC, nbtCompound.copy());

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.messageMode = MessageMode.byName(view.getStringOr("message_mode", "")).orElse(MessageMode.CHAT);

		this.messageLocalisationString = view.getStringOr("message_localisation_string", "");

		this.subTitleLocalisationString = view.getStringOr("subtitle_localisation_string", "");

		this.titleFadeIn = view.getIntOr("title_fade_in", 0);

		this.titleStay = view.getIntOr("title_stay", 0);

		this.titleFadeOut = view.getIntOr("title_fade_out", 0);

		CompoundTag nbtCompound = view.read("data_provider", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);
		this.dataIdentifier = nbtCompound.getStringOr("data_identifier", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
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

	public Component getMessageText() {
		String dataProvidedMessageAppendix = "";
		if (this.getLevel() != null && this.getLevel().getBlockEntity(getActualOffsetBlockPosition(this.getBlockPos(), this.dataProvidingBlockPosOffset)) instanceof ProvidesData providesData) {
			dataProvidedMessageAppendix = providesData.getData(this.dataIdentifier);
		}
		return Component.translatable(this.messageLocalisationString + dataProvidedMessageAppendix);
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		Iterator<UUID> listIterator = list.iterator();
		Player playerEntity;
		UUID uuid;
		if (this.getLevel() != null) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = this.getLevel().getPlayerByUUID(uuid);

				if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
					if (this.messageMode == MessageMode.TITLE) {
						serverPlayerEntity.connection.send(new ClientboundSetTitlesAnimationPacket(this.titleFadeIn, this.titleStay, this.titleFadeOut));
						serverPlayerEntity.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(this.subTitleLocalisationString)));
						serverPlayerEntity.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(this.messageLocalisationString)));
					} else {
						serverPlayerEntity.sendSystemMessage(this.getMessageText(), this.messageMode == MessageMode.OVERLAY);
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.FRONT_BACK);
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.LEFT_RIGHT);
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum MessageMode implements StringRepresentable {
		CHAT("chat"),
		OVERLAY("overlay"),
		TITLE("title");

		private final String name;

		MessageMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<MessageMode> byName(String name) {
			return Arrays.stream(MessageMode.values()).filter(messageMode -> messageMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.uuid_list_messaging_block.message_mode." + this.name);
		}
	}

}
