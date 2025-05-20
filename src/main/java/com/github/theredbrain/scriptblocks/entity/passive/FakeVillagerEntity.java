package com.github.theredbrain.scriptblocks.entity.passive;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.Objects;

public class FakeVillagerEntity extends MobEntity implements VillagerDataContainer {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final TrackedData<Integer> NO_INTERACTION_DISPLAY_TIME_LEFT;
	private static final TrackedData<Integer> HEAD_ROLLING_TIME_LEFT;
	private static final TrackedData<VillagerData> VILLAGER_DATA;
//	private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
//			MemoryModuleType.NEAREST_PLAYERS,
//			MemoryModuleType.NEAREST_VISIBLE_PLAYER,
//			MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER
//	);
//	private static final ImmutableList<SensorType<? extends Sensor<? super FakeVillagerEntity>>> SENSORS = ImmutableList.of(
//			SensorType.NEAREST_PLAYERS
//	);

	public FakeVillagerEntity(EntityType<? extends MobEntity> entityType, World world) {
		super(entityType, world);
	}

//	@Override
//	public Brain<FakeVillagerEntity> getBrain() {
//		return (Brain<FakeVillagerEntity>)super.getBrain();
//	}
//
//	@Override
//	protected Brain.Profile<FakeVillagerEntity> createBrainProfile() {
//		return Brain.createProfile(MEMORY_MODULES, SENSORS);
//	}
//
//	@Override
//	protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
//		Brain<FakeVillagerEntity> brain = this.createBrainProfile().deserialize(dynamic);
//		this.initBrain(brain);
//		return brain;
//	}
//
//	public void reinitializeBrain(ServerWorld world) {
//		Brain<FakeVillagerEntity> brain = this.getBrain();
//		brain.stopAllTasks(world, this);
//		this.brain = brain.copy();
//		this.initBrain(this.getBrain());
//	}
//
//	private void initBrain(Brain<FakeVillagerEntity> brain) {
//		VillagerProfession villagerProfession = this.getVillagerData().getProfession();
//
//			brain.setSchedule(Schedule.VILLAGER_DEFAULT);
//			brain.setTaskList(
//					Activity.WORK,
//					VillagerTaskListProvider.createWorkTasks(villagerProfession, 0.5F),
//					ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT))
//			);
//
//		brain.setTaskList(Activity.CORE, VillagerTaskListProvider.createCoreTasks(villagerProfession, 0.5F));
//		brain.setTaskList(
//				Activity.MEET,
//				VillagerTaskListProvider.createMeetTasks(villagerProfession, 0.5F),
//				ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryModuleState.VALUE_PRESENT))
//		);
//		brain.setTaskList(Activity.REST, VillagerTaskListProvider.createRestTasks(villagerProfession, 0.5F));
//		brain.setTaskList(Activity.IDLE, VillagerTaskListProvider.createIdleTasks(villagerProfession, 0.5F));
//		brain.setTaskList(Activity.PANIC, VillagerTaskListProvider.createPanicTasks(villagerProfession, 0.5F));
//		brain.setTaskList(Activity.PRE_RAID, VillagerTaskListProvider.createPreRaidTasks(villagerProfession, 0.5F));
//		brain.setTaskList(Activity.RAID, VillagerTaskListProvider.createRaidTasks(villagerProfession, 0.5F));
//		brain.setTaskList(Activity.HIDE, VillagerTaskListProvider.createHideTasks(villagerProfession, 0.5F));
//		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
//		brain.setDefaultActivity(Activity.IDLE);
//		brain.doExclusively(Activity.IDLE);
//		brain.refreshActivities(this.getWorld().getTimeOfDay(), this.getWorld().getTime());
//	}
//
//	@Override
//	protected void mobTick() {
//		this.getWorld().getProfiler().push("fakeVillagerBrain");
//		this.getBrain().tick((ServerWorld) this.getWorld(), this);
//		this.getWorld().getProfiler().pop();
//	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(NO_INTERACTION_DISPLAY_TIME_LEFT, 0);
		builder.add(HEAD_ROLLING_TIME_LEFT, 0);
		builder.add(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		DataResult<NbtElement> var10000 = VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, this.getVillagerData());
		Logger var10001 = LOGGER;
		Objects.requireNonNull(var10001);
		var10000.resultOrPartial(var10001::error).ifPresent((nbtElement) -> {
			nbt.put("VillagerData", nbtElement);
		});

	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
			DataResult<VillagerData> dataResult = VillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.get("VillagerData")));
			Logger var10001 = LOGGER;
			Objects.requireNonNull(var10001);
			dataResult.resultOrPartial(var10001::error).ifPresent(this::setVillagerData);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getHeadRollingTimeLeft() > 0) {
			this.setHeadRollingTimeLeft(this.getHeadRollingTimeLeft() - 1);
		}
		if (this.getNoInteractionDisplayTimeLeft() > 0) {
			this.setNoInteractionDisplayTimeLeft(this.getNoInteractionDisplayTimeLeft() - 1);
		}
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ActionResult actionResult = super.interactMob(player, hand);
		if (this.getNoInteractionDisplayTimeLeft() <= 0 && !this.getWorld().isClient) {
			this.setNoInteractionDisplayTimeLeft(30);
			if (actionResult.isAccepted()) {
				this.playSound(SoundEvents.ENTITY_VILLAGER_YES);
			} else {
				this.setHeadRollingTimeLeft(20);
				this.playSound(SoundEvents.ENTITY_VILLAGER_NO);
			}
		}
		return actionResult;
	}

	public int getNoInteractionDisplayTimeLeft() {
		return this.dataTracker.get(NO_INTERACTION_DISPLAY_TIME_LEFT);
	}

	public void setNoInteractionDisplayTimeLeft(int ticks) {
		this.dataTracker.set(NO_INTERACTION_DISPLAY_TIME_LEFT, ticks);
	}

	public int getHeadRollingTimeLeft() {
		return this.dataTracker.get(HEAD_ROLLING_TIME_LEFT);
	}

	public void setHeadRollingTimeLeft(int ticks) {
		this.dataTracker.set(HEAD_ROLLING_TIME_LEFT, ticks);
	}

	public void setVillagerData(VillagerData villagerData) {
		this.dataTracker.set(VILLAGER_DATA, villagerData);
	}

	public VillagerData getVillagerData() {
		return (VillagerData) this.dataTracker.get(VILLAGER_DATA);
	}

	static {
		NO_INTERACTION_DISPLAY_TIME_LEFT = DataTracker.registerData(FakeVillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);
		HEAD_ROLLING_TIME_LEFT = DataTracker.registerData(FakeVillagerEntity.class, TrackedDataHandlerRegistry.INTEGER);
		VILLAGER_DATA = DataTracker.registerData(FakeVillagerEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
	}
}
