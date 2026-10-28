package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Deprecated
public class AreaBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private static final BlockPos TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 0, 0);
	private boolean calculateAreaBox = true;
	private AABB area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

	private String appliedStatusEffectIdentifier = "";
	private int appliedStatusEffectAmplifier = 0;
	private boolean appliedStatusEffectAmbient = false;
	private boolean appliedStatusEffectShowParticles = false;
	private boolean appliedStatusEffectShowIcon = false;

	private boolean hasTriggered = false;
	private boolean wasTriggered = false;
	private TriggerMode triggerMode = TriggerMode.ALWAYS;
	private TriggeredMode triggeredMode = TriggeredMode.ONCE;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(TRIGGERED_BLOCK_POS_DEFAULT, false);

	private MessageMode messageMode = MessageMode.OVERLAY;
	private String joinMessage = "";
	private String leaveMessage = "";
	private String triggeredMessage = "";

	private ArrayList<UUID> playerList = new ArrayList<>();
	private ArrayList<UUID> playerList2 = new ArrayList<>();
	private HashMap<UUID, Integer> playerMap = new HashMap<>();
	private int maxTimer = 1;

	public AreaBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.AREA_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		if (this.showArea) {
			view.putBoolean("showArea", true);
		} else {
			view.discard("showArea");
		}

		if (this.area != null) {
			view.putDouble("areaMinX", this.area.minX);
			view.putDouble("areaMaxX", this.area.maxX);
			view.putDouble("areaMinY", this.area.minY);
			view.putDouble("areaMaxY", this.area.maxY);
			view.putDouble("areaMinZ", this.area.minZ);
			view.putDouble("areaMaxZ", this.area.maxZ);
		} else {
			view.discard("areaMinX");
			view.discard("areaMaxX");
			view.discard("areaMinY");
			view.discard("areaMaxY");
			view.discard("areaMinZ");
			view.discard("areaMaxZ");
		}

		if (this.areaDimensions.getX() != 0) {
			view.putInt("areaDimensionsX", this.areaDimensions.getX());
		} else {
			view.discard("areaDimensionsX");
		}

		if (this.areaDimensions.getY() != 0) {
			view.putInt("areaDimensionsY", this.areaDimensions.getY());
		} else {
			view.discard("areaDimensionsY");
		}

		if (this.areaDimensions.getZ() != 0) {
			view.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			view.discard("areaDimensionsZ");
		}

		if (this.areaPositionOffset.getX() != 0) {
			view.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
		} else {
			view.discard("areaPositionOffsetX");
		}

		if (this.areaPositionOffset.getY() != 0) {
			view.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
		} else {
			view.discard("areaPositionOffsetY");
		}

		if (this.areaPositionOffset.getZ() != 0) {
			view.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			view.discard("areaPositionOffsetZ");
		}


		if (!this.appliedStatusEffectIdentifier.isEmpty()) {
			view.putString("appliedStatusEffectIdentifier", this.appliedStatusEffectIdentifier);
		} else {
			view.discard("appliedStatusEffectIdentifier");
		}

		if (this.appliedStatusEffectAmplifier != 0) {
			view.putInt("appliedStatusEffectAmplifier", this.appliedStatusEffectAmplifier);
		} else {
			view.discard("appliedStatusEffectAmplifier");
		}

		if (this.appliedStatusEffectAmbient) {
			view.putBoolean("appliedStatusEffectAmbient", true);
		} else {
			view.discard("appliedStatusEffectAmbient");
		}

		if (this.appliedStatusEffectShowParticles) {
			view.putBoolean("appliedStatusEffectShowParticles", true);
		} else {
			view.discard("appliedStatusEffectShowParticles");
		}

		if (this.appliedStatusEffectShowIcon) {
			view.putBoolean("appliedStatusEffectShowIcon", true);
		} else {
			view.discard("appliedStatusEffectShowIcon");
		}


		if (this.messageMode != MessageMode.OVERLAY) {
			view.putString("messageMode", this.messageMode.getSerializedName());
		} else {
			view.discard("messageMode");
		}

		if (!this.joinMessage.isEmpty()) {
			view.putString("joinMessage", this.joinMessage);
		} else {
			view.discard("joinMessage");
		}

		if (!this.leaveMessage.isEmpty()) {
			view.putString("leaveMessage", this.leaveMessage);
		} else {
			view.discard("leaveMessage");
		}

		if (!this.triggeredMessage.isEmpty()) {
			view.putString("triggeredMessage", this.triggeredMessage);
		} else {
			view.discard("triggeredMessage");
		}


		if (this.hasTriggered) {
			view.putBoolean("hasTriggered", true);
		} else {
			view.discard("hasTriggered");
		}

		if (this.wasTriggered) {
			view.putBoolean("wasTriggered", true);
		} else {
			view.discard("wasTriggered");
		}

		if (this.triggerMode != TriggerMode.ALWAYS) {
			view.putString("triggerMode", this.triggerMode.getSerializedName());
		} else {
			view.discard("triggerMode");
		}

		if (this.triggeredMode != TriggeredMode.ONCE) {
			view.putString("triggeredMode", this.triggeredMode.getSerializedName());
		} else {
			view.discard("triggeredMode");
		}

		if (this.maxTimer > 1) {
			view.putInt("maxTimer", this.maxTimer);
		} else {
			view.discard("maxTimer");
		}

		if (this.triggeredBlock.getLeft() != TRIGGERED_BLOCK_POS_DEFAULT || !this.triggeredBlock.getRight()) {
			view.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
			view.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
			view.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
			view.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());
		} else {
			view.discard("triggeredBlockPositionOffsetX");
			view.discard("triggeredBlockPositionOffsetY");
			view.discard("triggeredBlockPositionOffsetZ");
			view.discard("triggeredBlockResets");
		}

		int playerListSize = playerList.size();
		view.putInt("playerListSize", playerListSize);
		for (int i = 0; i < playerListSize; i++) {
			view.putIntArray("playerListEntry_" + i, UUIDUtil.uuidToIntArray(this.playerList.get(i)));
		}

		int playerList2Size = playerList2.size();
		view.putInt("playerList2Size", playerList2Size);
		for (int i = 0; i < playerList2Size; i++) {
			view.putIntArray("playerList2Entry_" + i, UUIDUtil.uuidToIntArray(this.playerList2.get(i)));
		}

		List<UUID> keyList = this.playerMap.keySet().stream().toList();
		int playerMapSize = this.playerMap.keySet().size();
		view.putInt("playerMapSize", playerMapSize);
		for (int i = 0; i < playerMapSize; i++) {
			UUID key = keyList.get(i);
			view.putIntArray("key_" + i, UUIDUtil.uuidToIntArray(key));
			view.putInt("timer_" + i, this.playerMap.get(key));
		}

		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Area Block detected at: " + this.worldPosition.toString() + ". This block will be removed in the future.", this.level != null ? this.level.getServer() : null);
		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.showArea = view.getBooleanOr("showArea", true);

		this.area = new AABB(view.getDoubleOr("areaMinX", 0), view.getDoubleOr("areaMinY", 0), view.getDoubleOr("areaMinZ", 0), view.getDoubleOr("areaMaxX", 0), view.getDoubleOr("areaMaxY", 0), view.getDoubleOr("areaMaxZ", 0));
			this.calculateAreaBox = true;

		int i = Mth.clamp(view.getIntOr("areaDimensionsX", 0), 0, 48);
		int j = Mth.clamp(view.getIntOr("areaDimensionsY", 0), 0, 48);
		int k = Mth.clamp(view.getIntOr("areaDimensionsZ", 0), 0, 48);
		this.areaDimensions = new Vec3i(i, j, k);

		int l = Mth.clamp(view.getIntOr("areaPositionOffsetX", 0), -48, 48);
		int m = Mth.clamp(view.getIntOr("areaPositionOffsetY", 0), -48, 48);
		int n = Mth.clamp(view.getIntOr("areaPositionOffsetZ", 0), -48, 48);
		this.areaPositionOffset = new BlockPos(l, m, n);


		this.appliedStatusEffectIdentifier = view.getStringOr("appliedStatusEffectIdentifier", "");

		this.appliedStatusEffectAmplifier = view.getIntOr("appliedStatusEffectAmplifier", 0);

		this.appliedStatusEffectAmbient = view.getBooleanOr("appliedStatusEffectAmbient", false);

		this.appliedStatusEffectShowParticles = view.getBooleanOr("appliedStatusEffectShowParticles", false);

		this.appliedStatusEffectShowIcon = view.getBooleanOr("appliedStatusEffectShowIcon", false);


		this.messageMode = MessageMode.byName(view.getStringOr("messageMode", "")).orElse(MessageMode.OVERLAY);

		this.joinMessage = view.getStringOr("joinMessage", "");

		this.leaveMessage = view.getStringOr("leaveMessage", "");

		this.triggeredMessage = view.getStringOr("triggeredMessage", "");


		this.hasTriggered = view.getBooleanOr("hasTriggered", false);

		this.wasTriggered = view.getBooleanOr("wasTriggered", false);

		this.triggerMode = TriggerMode.byName(view.getStringOr("triggerMode", "")).orElse(TriggerMode.ALWAYS);

		this.triggeredMode = TriggeredMode.byName(view.getStringOr("triggeredMode", "")).orElse(TriggeredMode.ONCE);

		this.maxTimer = view.getIntOr("maxTimer", 1);

		this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							Mth.clamp(view.getIntOr("triggeredBlockPositionOffsetX", 0), -48, 48),
							Mth.clamp(view.getIntOr("triggeredBlockPositionOffsetY", 0), -48, 48),
							Mth.clamp(view.getIntOr("triggeredBlockPositionOffsetZ", 0), -48, 48)
					),
					view.getBooleanOr("triggeredBlockResets", false)
			);

		int playerListSize = view.getIntOr("playerListSize", 0);
		for (i = 0; i < playerListSize; i++) {
			Optional<int[]> optionalUUID = view.getIntArray("playerListEntry_" + i);
			if (optionalUUID.isPresent()) {
				this.playerList.add(UUIDUtil.uuidFromIntArray(optionalUUID.get()));
			}
		}

		int playerList2Size = view.getIntOr("playerList2Size", 0);
		for (i = 0; i < playerList2Size; i++) {
			Optional<int[]> optionalUUID = view.getIntArray("playerList2Entry_" + i);
			if (optionalUUID.isPresent()) {
				this.playerList2.add(UUIDUtil.uuidFromIntArray(optionalUUID.get()));
			}
		}

		int playerMapSize = view.getIntOr("playerMapSize", 0);
		this.playerMap = new HashMap<>();
		for (i = 0; i < playerMapSize; i++) {
//			UUID key = view.getUuid("key_" + i);
			Optional<int[]> optionalUUID = view.getIntArray("key_" + i);
			int timer = view.getIntOr("timer_" + i, 0);
			if (optionalUUID.isPresent()) {
				this.playerMap.put(UUIDUtil.uuidFromIntArray(optionalUUID.get()), timer);
			}
		}

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, AreaBlockEntity areaBlockEntity) {
		if (!world.isClientSide() && world.getGameTime() % 20L == 0L && areaBlockEntity.wasTriggered) {
			if (areaBlockEntity.calculateAreaBox || areaBlockEntity.area == null) {
				BlockPos areaPositionOffset = areaBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = areaBlockEntity.areaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				areaBlockEntity.area = new AABB(areaStart, areaEnd);
				areaBlockEntity.calculateAreaBox = false;
			}

			boolean shouldTriggerBlock = false;

			Optional<Holder.Reference<MobEffect>> statusEffect = BuiltInRegistries.MOB_EFFECT.get(Identifier.tryParse(areaBlockEntity.appliedStatusEffectIdentifier));

			List<Player> newPlayerList = world.getEntitiesOfClass(Player.class, areaBlockEntity.area);
			List<UUID> newPLayerUuidList = new ArrayList<>();
			for (Player player : newPlayerList) {
				newPLayerUuidList.add(player.getUUID());
			}
			ArrayList<UUID> tempList = new ArrayList<>();

			Iterator<UUID> playerListIterator = areaBlockEntity.playerList.iterator();
			Player playerEntity;
			UUID uuid;

			// old list
			while (playerListIterator.hasNext()) {
				uuid = playerListIterator.next();
				playerEntity = world.getPlayerByUUID(uuid);

				if (playerEntity != null) {
					if (newPLayerUuidList.contains(uuid)) {
						tempList.add(uuid);
						newPLayerUuidList.remove(uuid);
						if (statusEffect.isPresent() && areaBlockEntity.wasTriggered) {
							playerEntity.addEffect(
									new MobEffectInstance(
											statusEffect.get(),
											100,
											areaBlockEntity.appliedStatusEffectAmplifier,
											areaBlockEntity.appliedStatusEffectAmbient,
											areaBlockEntity.appliedStatusEffectShowParticles,
											areaBlockEntity.appliedStatusEffectShowIcon
									)
							);
						}
					} else {
						if (areaBlockEntity.triggerMode == TriggerMode.ALWAYS) {
							areaBlockEntity.playerList2.clear();
						}
						if (!areaBlockEntity.playerList2.contains(uuid)) {
							if (!areaBlockEntity.leaveMessage.isEmpty()) {
								if (areaBlockEntity.messageMode == MessageMode.ANNOUNCEMENT) {
//									((DuckPlayerEntityMixin) playerEntity).scriptblocks$sendAnnouncement(Text.translatable(areaBlockEntity.leaveMessage));
								} else {
//									playerEntity.sendSystemMessage(Component.translatable(areaBlockEntity.leaveMessage), areaBlockEntity.messageMode == MessageMode.OVERLAY);
								}
							}
							if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS) {
								areaBlockEntity.playerList2.add(playerEntity.getUUID());
							}
						}
					}
				}
			}

			if (areaBlockEntity.triggerMode == TriggerMode.TIMED) {
				HashMap<UUID, Integer> newPlayerMap = new HashMap<>();
				areaBlockEntity.playerMap.forEach((uuid1, integer) -> {
					int newInteger = --integer;
					if (newInteger > 0) {
						newPlayerMap.put(uuid1, newInteger);
					}
				});
				areaBlockEntity.playerMap.clear();
				areaBlockEntity.playerMap.putAll(newPlayerMap);
			} else if (areaBlockEntity.triggerMode == TriggerMode.ALWAYS) {
				areaBlockEntity.playerMap.clear();
			}

			Iterator<UUID> newPlayerListIterator = newPLayerUuidList.iterator();
			// new list
			while (newPlayerListIterator.hasNext()) {
				UUID uuid2 = newPlayerListIterator.next();
				playerEntity = world.getPlayerByUUID(uuid2);

				if (playerEntity != null) {
					tempList.add(uuid2);
					if (statusEffect.isPresent()) {
						playerEntity.addEffect(
								new MobEffectInstance(
										statusEffect.get(),
										100,
										areaBlockEntity.appliedStatusEffectAmplifier,
										areaBlockEntity.appliedStatusEffectAmbient,
										areaBlockEntity.appliedStatusEffectShowParticles,
										areaBlockEntity.appliedStatusEffectShowIcon
								)
						);
					}
					if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS && areaBlockEntity.playerMap.containsKey(playerEntity.getUUID())) {
						continue;
					}
					if (!areaBlockEntity.joinMessage.isEmpty()) {
						if (areaBlockEntity.messageMode == MessageMode.ANNOUNCEMENT) {
//							((DuckPlayerEntityMixin) playerEntity).scriptblocks$sendAnnouncement(Text.translatable(areaBlockEntity.joinMessage));
						} else {
//							playerEntity.sendSystemMessage(Component.translatable(areaBlockEntity.joinMessage), areaBlockEntity.messageMode == MessageMode.OVERLAY);
						}
					}
					areaBlockEntity.playerList2.remove(playerEntity.getUUID());
					if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS) {
						areaBlockEntity.playerMap.put(playerEntity.getUUID(), areaBlockEntity.triggerMode == TriggerMode.ONCE ? 1 : areaBlockEntity.maxTimer);
					}
					shouldTriggerBlock = true;
				}
			}

			areaBlockEntity.playerList.clear();
			areaBlockEntity.playerList.addAll(tempList);

			if (shouldTriggerBlock && (areaBlockEntity.triggerMode == TriggerMode.ALWAYS || !areaBlockEntity.hasTriggered)) {
				areaBlockEntity.triggerBlock();
				areaBlockEntity.hasTriggered = true;
			}
		}
	}

	private void triggerBlock() {
		ScriptBlocks.info("areablock triggerBlock");
		int x = this.triggeredBlock.getLeft().getX();
		int y = this.triggeredBlock.getLeft().getY();
		int z = this.triggeredBlock.getLeft().getZ();
		if (this.level != null && (x != 0 || y != 0 || z != 0)) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + x, this.worldPosition.getY() + y, this.worldPosition.getZ() + z));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	private void sendMessage(String message) {
		if (this.level instanceof ServerLevel) {
			ScriptBlocks.info("areablock sendMessage");
			if (this.calculateAreaBox || this.area == null) {
				BlockPos messageAreaPositionOffset = this.areaPositionOffset;
				Vec3i messageAreaDimensions = this.areaDimensions;
				Vec3 messageAreaStart = new Vec3(worldPosition.getX() + messageAreaPositionOffset.getX(), worldPosition.getY() + messageAreaPositionOffset.getY(), worldPosition.getZ() + messageAreaPositionOffset.getZ());
				Vec3 messageAreaEnd = new Vec3(messageAreaStart.x() + messageAreaDimensions.getX(), messageAreaStart.y() + messageAreaDimensions.getY(), messageAreaStart.z() + messageAreaDimensions.getZ());
				this.area = new AABB(messageAreaStart, messageAreaEnd);
				this.calculateAreaBox = false;
			}
			List<Player> list = level.getEntitiesOfClass(Player.class, this.area);
			for (Player playerEntity : list) {
				if (this.messageMode == MessageMode.ANNOUNCEMENT) {
//					((DuckPlayerEntityMixin) playerEntity).scriptblocks$sendAnnouncement(Text.translatable(message));
				} else {
//					playerEntity.sendSystemMessage(Component.translatable(message), this.messageMode == MessageMode.OVERLAY);
				}
			}
		}
	}

	//region --- getter & setter ---
	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	// TODO check if input is valid
	public boolean setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
		return true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	// TODO check if input is valid
	public boolean setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
		return true;
	}

	public String getAppliedStatusEffectIdentifier() {
		return this.appliedStatusEffectIdentifier;
	}

	public boolean setAppliedStatusEffectIdentifier(String appliedStatusEffectIdentifier) {
		if (BuiltInRegistries.MOB_EFFECT.getValue(Identifier.tryParse(appliedStatusEffectIdentifier)) != null || appliedStatusEffectIdentifier.equals("")) {
			this.appliedStatusEffectIdentifier = appliedStatusEffectIdentifier;
			return true;
		}
		return false;
	}

	public int getAppliedStatusEffectAmplifier() {
		return appliedStatusEffectAmplifier;
	}

	public boolean setAppliedStatusEffectAmplifier(int appliedStatusEffectAmplifier) {
		if (appliedStatusEffectAmplifier >= 0 && appliedStatusEffectAmplifier < 127) {
			this.appliedStatusEffectAmplifier = appliedStatusEffectAmplifier;
			return true;
		}
		return false;
	}

	public boolean getAppliedStatusEffectAmbient() {
		return appliedStatusEffectAmbient;
	}

	public void setAppliedStatusEffectAmbient(boolean appliedStatusEffectAmbient) {
		this.appliedStatusEffectAmbient = appliedStatusEffectAmbient;
	}

	public boolean getAppliedStatusEffectShowParticles() {
		return appliedStatusEffectShowParticles;
	}

	public void setAppliedStatusEffectShowParticles(boolean appliedStatusEffectShowParticles) {
		this.appliedStatusEffectShowParticles = appliedStatusEffectShowParticles;
	}

	public boolean getAppliedStatusEffectShowIcon() {
		return appliedStatusEffectShowIcon;
	}

	public void setAppliedStatusEffectShowIcon(boolean appliedStatusEffectShowIcon) {
		this.appliedStatusEffectShowIcon = appliedStatusEffectShowIcon;
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public boolean getWasTriggered() {
		return this.wasTriggered;
	}

	public void setWasTriggered(boolean wasTriggered) {
		this.wasTriggered = wasTriggered;
	}

	public String getJoinMessage() {
		return this.joinMessage;
	}

	public void setJoinMessage(String joinMessage) {
		this.joinMessage = joinMessage;
	}

	public String getLeaveMessage() {
		return this.leaveMessage;
	}

	public void setLeaveMessage(String leaveMessage) {
		this.leaveMessage = leaveMessage;
	}

	public String getTriggeredMessage() {
		return this.triggeredMessage;
	}

	public void setTriggeredMessage(String triggeredMessage) {
		this.triggeredMessage = triggeredMessage;
	}

	public MessageMode getMessageMode() {
		return this.messageMode;
	}

	public void setMessageMode(MessageMode messageMode) {
		this.messageMode = messageMode;
	}

	public TriggerMode getTriggerMode() {
		return this.triggerMode;
	}

	public void setTriggerMode(TriggerMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	public TriggeredMode getTriggeredMode() {
		return this.triggeredMode;
	}

	public void setTriggeredMode(TriggeredMode triggeredMode) {
		this.triggeredMode = triggeredMode;
	}

	public int getMaxTimer() {
		return this.maxTimer;
	}

	public void setMaxTimer(int maxTimer) {
		this.maxTimer = maxTimer;
	}
	//endregion --- getter & setter ---

	@Override
	public void reset() {
		if (this.hasTriggered) {
			this.hasTriggered = false;
		}
		if (this.wasTriggered) {
			this.wasTriggered = false;
		}
		this.playerMap.clear();
		this.playerList.clear();
		this.playerList2.clear();
	}

	@Override
	public void trigger() {
		if ((this.triggeredMode == TriggeredMode.ONCE && !this.wasTriggered) || this.triggeredMode == TriggeredMode.CONTINUOUS) {
			this.sendMessage(this.triggeredMessage);
			this.wasTriggered = true;
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public static enum MessageMode implements StringRepresentable {
		ANNOUNCEMENT("announcement"), // TODO rename to title, when this mode is active, multiple new text field are visible which define the title that is shown to the player
		CHAT("chat"),
		OVERLAY("overlay");

		private final String name;

		private MessageMode(String name) {
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
			return Component.translatable("gui.area_block.message_mode." + this.name);
		}
	}

	public static enum TriggerMode implements StringRepresentable {
		ALWAYS("always"),
		ONCE("once"),
		TIMED("timed");

		private final String name;

		private TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<TriggerMode> byName(String name) {
			return Arrays.stream(TriggerMode.values()).filter(triggerMode -> triggerMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.area_block.trigger_mode." + this.name);
		}
	}

	public static enum TriggeredMode implements StringRepresentable {
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		private TriggeredMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<TriggeredMode> byName(String name) {
			return Arrays.stream(TriggeredMode.values()).filter(triggeredMode -> triggeredMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.area_block.triggered_mode." + this.name);
		}
	}
}
