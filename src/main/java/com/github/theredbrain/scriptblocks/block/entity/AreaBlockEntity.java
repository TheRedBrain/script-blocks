package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Deprecated
public class AreaBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	private static final BlockPos TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 0, 0);
	private boolean calculateAreaBox = true;
	private Box area = null;
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
	protected void writeData(WriteView view) {

		if (this.showArea) {
			view.putBoolean("showArea", true);
		} else {
			view.remove("showArea");
		}

		if (this.area != null) {
			view.putDouble("areaMinX", this.area.minX);
			view.putDouble("areaMaxX", this.area.maxX);
			view.putDouble("areaMinY", this.area.minY);
			view.putDouble("areaMaxY", this.area.maxY);
			view.putDouble("areaMinZ", this.area.minZ);
			view.putDouble("areaMaxZ", this.area.maxZ);
		} else {
			view.remove("areaMinX");
			view.remove("areaMaxX");
			view.remove("areaMinY");
			view.remove("areaMaxY");
			view.remove("areaMinZ");
			view.remove("areaMaxZ");
		}

		if (this.areaDimensions.getX() != 0) {
			view.putInt("areaDimensionsX", this.areaDimensions.getX());
		} else {
			view.remove("areaDimensionsX");
		}

		if (this.areaDimensions.getY() != 0) {
			view.putInt("areaDimensionsY", this.areaDimensions.getY());
		} else {
			view.remove("areaDimensionsY");
		}

		if (this.areaDimensions.getZ() != 0) {
			view.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			view.remove("areaDimensionsZ");
		}

		if (this.areaPositionOffset.getX() != 0) {
			view.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
		} else {
			view.remove("areaPositionOffsetX");
		}

		if (this.areaPositionOffset.getY() != 0) {
			view.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
		} else {
			view.remove("areaPositionOffsetY");
		}

		if (this.areaPositionOffset.getZ() != 0) {
			view.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			view.remove("areaPositionOffsetZ");
		}


		if (!this.appliedStatusEffectIdentifier.isEmpty()) {
			view.putString("appliedStatusEffectIdentifier", this.appliedStatusEffectIdentifier);
		} else {
			view.remove("appliedStatusEffectIdentifier");
		}

		if (this.appliedStatusEffectAmplifier != 0) {
			view.putInt("appliedStatusEffectAmplifier", this.appliedStatusEffectAmplifier);
		} else {
			view.remove("appliedStatusEffectAmplifier");
		}

		if (this.appliedStatusEffectAmbient) {
			view.putBoolean("appliedStatusEffectAmbient", true);
		} else {
			view.remove("appliedStatusEffectAmbient");
		}

		if (this.appliedStatusEffectShowParticles) {
			view.putBoolean("appliedStatusEffectShowParticles", true);
		} else {
			view.remove("appliedStatusEffectShowParticles");
		}

		if (this.appliedStatusEffectShowIcon) {
			view.putBoolean("appliedStatusEffectShowIcon", true);
		} else {
			view.remove("appliedStatusEffectShowIcon");
		}


		if (this.messageMode != MessageMode.OVERLAY) {
			view.putString("messageMode", this.messageMode.asString());
		} else {
			view.remove("messageMode");
		}

		if (!this.joinMessage.isEmpty()) {
			view.putString("joinMessage", this.joinMessage);
		} else {
			view.remove("joinMessage");
		}

		if (!this.leaveMessage.isEmpty()) {
			view.putString("leaveMessage", this.leaveMessage);
		} else {
			view.remove("leaveMessage");
		}

		if (!this.triggeredMessage.isEmpty()) {
			view.putString("triggeredMessage", this.triggeredMessage);
		} else {
			view.remove("triggeredMessage");
		}


		if (this.hasTriggered) {
			view.putBoolean("hasTriggered", true);
		} else {
			view.remove("hasTriggered");
		}

		if (this.wasTriggered) {
			view.putBoolean("wasTriggered", true);
		} else {
			view.remove("wasTriggered");
		}

		if (this.triggerMode != TriggerMode.ALWAYS) {
			view.putString("triggerMode", this.triggerMode.asString());
		} else {
			view.remove("triggerMode");
		}

		if (this.triggeredMode != TriggeredMode.ONCE) {
			view.putString("triggeredMode", this.triggeredMode.asString());
		} else {
			view.remove("triggeredMode");
		}

		if (this.maxTimer > 1) {
			view.putInt("maxTimer", this.maxTimer);
		} else {
			view.remove("maxTimer");
		}

		if (this.triggeredBlock.getLeft() != TRIGGERED_BLOCK_POS_DEFAULT || !this.triggeredBlock.getRight()) {
			view.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
			view.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
			view.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
			view.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());
		} else {
			view.remove("triggeredBlockPositionOffsetX");
			view.remove("triggeredBlockPositionOffsetY");
			view.remove("triggeredBlockPositionOffsetZ");
			view.remove("triggeredBlockResets");
		}

		int playerListSize = playerList.size();
		view.putInt("playerListSize", playerListSize);
		for (int i = 0; i < playerListSize; i++) {
			view.putIntArray("playerListEntry_" + i, Uuids.toIntArray(this.playerList.get(i)));
		}

		int playerList2Size = playerList2.size();
		view.putInt("playerList2Size", playerList2Size);
		for (int i = 0; i < playerList2Size; i++) {
			view.putIntArray("playerList2Entry_" + i, Uuids.toIntArray(this.playerList2.get(i)));
		}

		List<UUID> keyList = this.playerMap.keySet().stream().toList();
		int playerMapSize = this.playerMap.keySet().size();
		view.putInt("playerMapSize", playerMapSize);
		for (int i = 0; i < playerMapSize; i++) {
			UUID key = keyList.get(i);
			view.putIntArray("key_" + i, Uuids.toIntArray(key));
			view.putInt("timer_" + i, this.playerMap.get(key));
		}

		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Area Block detected at: " + this.pos.toString() + ". This block will be removed in the future.", this.world != null ? this.world.getServer() : null);
		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		this.showArea = view.getBoolean("showArea", true);

		if (nbt.contains("areaMinX") && view.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
			this.area = new Box(view.getDouble("areaMinX"), view.getDouble("areaMinY"), view.getDouble("areaMinZ"), view.getDouble("areaMaxX"), view.getDouble("areaMaxY"), view.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
		}

		int i = MathHelper.clamp(view.getInt("areaDimensionsX", 0), 0, 48);
		int j = MathHelper.clamp(view.getInt("areaDimensionsY", 0), 0, 48);
		int k = MathHelper.clamp(view.getInt("areaDimensionsZ", 0), 0, 48);
		this.areaDimensions = new Vec3i(i, j, k);

		int l = MathHelper.clamp(view.getInt("areaPositionOffsetX", 0), -48, 48);
		int m = MathHelper.clamp(view.getInt("areaPositionOffsetY", 0), -48, 48);
		int n = MathHelper.clamp(view.getInt("areaPositionOffsetZ", 0), -48, 48);
		this.areaPositionOffset = new BlockPos(l, m, n);


		this.appliedStatusEffectIdentifier = view.getString("appliedStatusEffectIdentifier", "");

		this.appliedStatusEffectAmplifier = view.getInt("appliedStatusEffectAmplifier", 0);

		this.appliedStatusEffectAmbient = view.getBoolean("appliedStatusEffectAmbient");

		this.appliedStatusEffectShowParticles = view.getBoolean("appliedStatusEffectShowParticles");

		this.appliedStatusEffectShowIcon = view.getBoolean("appliedStatusEffectShowIcon");


		this.messageMode = MessageMode.byName(view.getString("messageMode", "")).orElse(MessageMode.OVERLAY);

		this.joinMessage = view.getString("joinMessage", "");

		this.leaveMessage = view.getString("leaveMessage", "");

		this.triggeredMessage = view.getString("triggeredMessage", "");


		this.hasTriggered = view.getBoolean("hasTriggered");

		this.wasTriggered = view.getBoolean("wasTriggered");

		this.triggerMode = TriggerMode.byName(view.getString("triggerMode", "")).orElse(TriggerMode.ALWAYS);

		this.triggeredMode = TriggeredMode.byName(view.getString("triggeredMode", "")).orElse(TriggeredMode.ONCE);

		if (nbt.contains("maxTimer", NbtElement.INT_TYPE)) {
			this.maxTimer = view.getInt("maxTimer", 1);
		} else {
			this.maxTimer = 1;
		}

		if (nbt.contains("triggeredBlockPositionOffsetX", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockPositionOffsetY", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(view.getInt("triggeredBlockPositionOffsetX", 0), -48, 48),
							MathHelper.clamp(view.getInt("triggeredBlockPositionOffsetY", 0), -48, 48),
							MathHelper.clamp(view.getInt("triggeredBlockPositionOffsetZ", 0), -48, 48)
					),
					view.getBoolean("triggeredBlockResets", false)
			);
		}

		int playerListSize = view.getInt("playerListSize", 0);
		for (i = 0; i < playerListSize; i++) {
			Optional<int[]> optionalUUID = view.getOptionalIntArray("playerListSize" + i);
			if (optionalUUID.isPresent()) {
				this.playerList.add(Uuids.toUuid(optionalUUID.get()));
			}
		}

		int playerList2Size = view.getInt("playerList2Size", 0);
		for (i = 0; i < playerList2Size; i++) {
			this.playerList2.add(view.read("playerList2Size" + i, Uuids.CODEC));
		}

		int playerMapSize = view.getInt("playerMapSize", 0);
		this.playerMap = new HashMap<>();
		for (i = 0; i < playerMapSize; i++) {
			UUID key = view.getUuid("key_" + i);
			int timer = view.getInt("timer_" + i);
			this.playerMap.put(key, timer);
		}

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, AreaBlockEntity areaBlockEntity) {
		if (!world.isClient && world.getTime() % 20L == 0L && areaBlockEntity.wasTriggered) {
			if (areaBlockEntity.calculateAreaBox || areaBlockEntity.area == null) {
				BlockPos areaPositionOffset = areaBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = areaBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				areaBlockEntity.area = new Box(areaStart, areaEnd);
				areaBlockEntity.calculateAreaBox = false;
			}

			boolean shouldTriggerBlock = false;

			Optional<RegistryEntry.Reference<StatusEffect>> statusEffect = Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(areaBlockEntity.appliedStatusEffectIdentifier));

			List<PlayerEntity> newPlayerList = world.getNonSpectatingEntities(PlayerEntity.class, areaBlockEntity.area);
			List<UUID> newPLayerUuidList = new ArrayList<>();
			for (PlayerEntity player : newPlayerList) {
				newPLayerUuidList.add(player.getUuid());
			}
			ArrayList<UUID> tempList = new ArrayList<>();

			Iterator<UUID> playerListIterator = areaBlockEntity.playerList.iterator();
			PlayerEntity playerEntity;
			UUID uuid;

			// old list
			while (playerListIterator.hasNext()) {
				uuid = playerListIterator.next();
				playerEntity = world.getPlayerByUuid(uuid);

				if (playerEntity != null) {
					if (newPLayerUuidList.contains(uuid)) {
						tempList.add(uuid);
						newPLayerUuidList.remove(uuid);
						if (statusEffect.isPresent() && areaBlockEntity.wasTriggered) {
							playerEntity.addStatusEffect(
									new StatusEffectInstance(
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
									playerEntity.sendMessage(Text.translatable(areaBlockEntity.leaveMessage), areaBlockEntity.messageMode == MessageMode.OVERLAY);
								}
							}
							if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS) {
								areaBlockEntity.playerList2.add(playerEntity.getUuid());
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
				playerEntity = world.getPlayerByUuid(uuid2);

				if (playerEntity != null) {
					tempList.add(uuid2);
					if (statusEffect.isPresent()) {
						playerEntity.addStatusEffect(
								new StatusEffectInstance(
										statusEffect.get(),
										100,
										areaBlockEntity.appliedStatusEffectAmplifier,
										areaBlockEntity.appliedStatusEffectAmbient,
										areaBlockEntity.appliedStatusEffectShowParticles,
										areaBlockEntity.appliedStatusEffectShowIcon
								)
						);
					}
					if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS && areaBlockEntity.playerMap.containsKey(playerEntity.getUuid())) {
						continue;
					}
					if (!areaBlockEntity.joinMessage.isEmpty()) {
						if (areaBlockEntity.messageMode == MessageMode.ANNOUNCEMENT) {
//							((DuckPlayerEntityMixin) playerEntity).scriptblocks$sendAnnouncement(Text.translatable(areaBlockEntity.joinMessage));
						} else {
							playerEntity.sendMessage(Text.translatable(areaBlockEntity.joinMessage), areaBlockEntity.messageMode == MessageMode.OVERLAY);
						}
					}
					areaBlockEntity.playerList2.remove(playerEntity.getUuid());
					if (areaBlockEntity.triggerMode != TriggerMode.ALWAYS) {
						areaBlockEntity.playerMap.put(playerEntity.getUuid(), areaBlockEntity.triggerMode == TriggerMode.ONCE ? 1 : areaBlockEntity.maxTimer);
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
		if (this.world != null && (x != 0 || y != 0 || z != 0)) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + x, this.pos.getY() + y, this.pos.getZ() + z));
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
		if (this.world instanceof ServerWorld) {
			ScriptBlocks.info("areablock sendMessage");
			if (this.calculateAreaBox || this.area == null) {
				BlockPos messageAreaPositionOffset = this.areaPositionOffset;
				Vec3i messageAreaDimensions = this.areaDimensions;
				Vec3d messageAreaStart = new Vec3d(pos.getX() + messageAreaPositionOffset.getX(), pos.getY() + messageAreaPositionOffset.getY(), pos.getZ() + messageAreaPositionOffset.getZ());
				Vec3d messageAreaEnd = new Vec3d(messageAreaStart.getX() + messageAreaDimensions.getX(), messageAreaStart.getY() + messageAreaDimensions.getY(), messageAreaStart.getZ() + messageAreaDimensions.getZ());
				this.area = new Box(messageAreaStart, messageAreaEnd);
				this.calculateAreaBox = false;
			}
			List<PlayerEntity> list = world.getNonSpectatingEntities(PlayerEntity.class, this.area);
			for (PlayerEntity playerEntity : list) {
				if (this.messageMode == MessageMode.ANNOUNCEMENT) {
//					((DuckPlayerEntityMixin) playerEntity).scriptblocks$sendAnnouncement(Text.translatable(message));
				} else {
					playerEntity.sendMessage(Text.translatable(message), this.messageMode == MessageMode.OVERLAY);
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
		if (Registries.STATUS_EFFECT.get(Identifier.tryParse(appliedStatusEffectIdentifier)) != null || appliedStatusEffectIdentifier.equals("")) {
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public static enum MessageMode implements StringIdentifiable {
		ANNOUNCEMENT("announcement"), // TODO rename to title, when this mode is active, multiple new text field are visible which define the title that is shown to the player
		CHAT("chat"),
		OVERLAY("overlay");

		private final String name;

		private MessageMode(String name) {
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
			return Text.translatable("gui.area_block.message_mode." + this.name);
		}
	}

	public static enum TriggerMode implements StringIdentifiable {
		ALWAYS("always"),
		ONCE("once"),
		TIMED("timed");

		private final String name;

		private TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<TriggerMode> byName(String name) {
			return Arrays.stream(TriggerMode.values()).filter(triggerMode -> triggerMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.area_block.trigger_mode." + this.name);
		}
	}

	public static enum TriggeredMode implements StringIdentifiable {
		CONTINUOUS("continuous"),
		ONCE("once");

		private final String name;

		private TriggeredMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<TriggeredMode> byName(String name) {
			return Arrays.stream(TriggeredMode.values()).filter(triggeredMode -> triggeredMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.area_block.triggered_mode." + this.name);
		}
	}
}
