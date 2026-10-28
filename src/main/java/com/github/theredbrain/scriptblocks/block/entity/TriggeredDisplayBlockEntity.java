package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

public class TriggeredDisplayBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {

	//region nbt keys
	// common
	public static final String TELEPORT_DURATION_KEY = "teleport_duration";
	public static final String INTERPOLATION_DURATION_KEY = "interpolation_duration";
	public static final String START_INTERPOLATION_KEY = "start_interpolation";
	public static final String TRANSFORMATION_NBT_KEY = "transformation";
	public static final String BILLBOARD_NBT_KEY = "billboard";
	public static final String BRIGHTNESS_NBT_KEY = "brightness";
	public static final String VIEW_RANGE_NBT_KEY = "view_range";
	public static final String SHADOW_RADIUS_NBT_KEY = "shadow_radius";
	public static final String SHADOW_STRENGTH_NBT_KEY = "shadow_strength";
	public static final String WIDTH_NBT_KEY = "width";
	public static final String HEIGHT_NBT_KEY = "height";
	public static final String GLOW_COLOR_OVERRIDE_NBT_KEY = "glow_color_override";

	public static final String ERROR_PREFIX = "Triggered Display Block";
	public static final String DISPLAY_MODE_NBT_KEY = "display_mode";
	public static final String IS_TRIGGERED_NBT_KEY = "is_triggered";
	public static final String DISPLAY_OFFSET_X_NBT_KEY = "display_offset_x";
	public static final String DISPLAY_OFFSET_Y_NBT_KEY = "display_offset_y";
	public static final String DISPLAY_OFFSET_Z_NBT_KEY = "display_offset_z";
	public static final String DISPLAY_OFFSET_NBT_KEY = "display_offset";
	public static final String DISPLAY_YAW_NBT_KEY = "display_yaw";
	public static final String DISPLAY_PITCH_NBT_KEY = "display_pitch";
	public static final String DISPLAY_PREV_YAW_NBT_KEY = "display_prev_yaw";
	public static final String DISPLAY_PREV_PITCH_NBT_KEY = "display_prev_pitch";
//	// block mode
//	public static final String BLOCK_STATE_NBT_KEY = "block_state";
//
//	// item mode
//	private static final String ITEM_NBT_KEY = "item";
//	private static final String ITEM_DISPLAY_NBT_KEY = "item_display";

	// text mode
	public static final String TEXT_STRING_NBT_KEY = "text_string";
//	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_X_NBT_KEY = "data_provider_block_pos_offset_x";
//	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_Y_NBT_KEY = "data_provider_block_pos_offset_y";
//	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_Z_NBT_KEY = "data_provider_block_pos_offset_z";
	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_NBT_KEY = "data_provider_block_pos_offset";
	public static final String DATA_IDENTIFIER_NBT_KEY = "data_identifier";
	public static final String TEXT_NBT_KEY = "text";
	private static final String LINE_WIDTH_NBT_KEY = "line_width";
	private static final String TEXT_OPACITY_NBT_KEY = "text_opacity";
	private static final String BACKGROUND_NBT_KEY = "background";
	private static final String SHADOW_NBT_KEY = "shadow";
	private static final String SEE_THROUGH_NBT_KEY = "see_through";
	private static final String DEFAULT_BACKGROUND_NBT_KEY = "default_background";
	private static final String ALIGNMENT_NBT_KEY = "alignment";
	//endregion nbt keys

	//region byte flags
	public static final byte SHADOW_FLAG = 1;
	public static final byte SEE_THROUGH_FLAG = 2;
	public static final byte DEFAULT_BACKGROUND_FLAG = 4;
	public static final byte LEFT_ALIGNMENT_FLAG = 8;
	public static final byte RIGHT_ALIGNMENT_FLAG = 16;
	//endregion byte flags

	//region default values

	private static final Vec3 DISPLAY_OFFSET_DEFAULT = new Vec3(0, 0, 0);
	// text mode
	private static final Component INITIAL_TEXT = Component.literal("Loooooooooooooooooong test text");//Text.empty();
	private static final int INITIAL_LINE_WIDTH = 200;
	private static final byte INITIAL_TEXT_OPACITY = -1;
	public static final int INITIAL_BACKGROUND = 1073741824;
	//endregion default values

	// common
	private int teleportDuration = 0;
	private int startInterpolation = 0;
	private int interpolationDuration = 0;
	private Vector3fc translation = new Vector3f();
	private Vector3fc scale = new Vector3f(1, 1, 1);
	private Quaternionfc leftRotation = new Quaternionf();
	private Quaternionfc rightRotation = new Quaternionf();
	private Byte billboard = BillboardConstraints.FIXED.getId();
	private int brightness = -1;
	private float viewRange = 1.0F;
	private float shadowRadius = 0.0F;
	private float shadowStrength = 1.0F;
	private float width = 0.0F;
	private float height = 0.0F;
	private int glowColorOverride = -1;

	private long interpolationStart = -2147483648L;
	private int currentInterpolationDuration;
	private float lerpProgress;
	private AABB visibilityBoundingBox;
	private boolean renderingDataSet;
	private boolean startInterpolationSet;
	private boolean interpolationDurationSet;
	@Nullable
	private RenderState renderState;
	@Nullable
	private InterpolationTarget interpolationTarget;


//	// block mode
//
//	// item mode
//	private ItemStack itemStack = ItemStack.EMPTY;
//	private Byte itemDisplayMode = ModelTransformationMode.NONE.getIndex();

	// text mode
	private String textString = "";
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
	private String dataIdentifierString = "";
	private Component text = INITIAL_TEXT;
	private int lineWidth = INITIAL_LINE_WIDTH;
	private int background = INITIAL_BACKGROUND;
	private Byte textOpacity = INITIAL_TEXT_OPACITY;
	private Byte textDisplayFlags = 0;
	@Nullable
	private TextLines textLines;
	@Nullable
	private Data data;

	private Vec3 displayOffset = DISPLAY_OFFSET_DEFAULT;
	private DisplayMode displayMode = DisplayMode.TEXT;
	private boolean isTriggered = true;

	// entity replacements
	private long age;
	private float displayYaw = 0.0F;
	private float displayPitch = 0.0F;
	private float displayPrevYaw = 0.0F;
	private float displayPrevPitch = 0.0F;

	public TriggeredDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_DISPLAY_BLOCK_ENTITY, pos, state);
		this.age = 0;
	}

	private static Transformation getTransformation(TriggeredDisplayBlockEntity blockEntity) {
		Vector3fc vector3f = blockEntity.getTranslation();
		Quaternionfc quaternionfc = blockEntity.getLeftRotation();
		Vector3fc vector3f1 = blockEntity.getScale();
		Quaternionfc quaternionfc1 = blockEntity.getRightRotation();
		return new Transformation(vector3f, quaternionfc, vector3f1, quaternionfc1);
	}

	private void setTransformation(Transformation transformation) {
		this.setTranslation(transformation.translation());
		this.setLeftRotation(transformation.leftRotation());
		this.setScale(transformation.scale());
		this.setRightRotation(transformation.rightRotation());
	}

	@Nullable
	public Data getData() {
		return this.data;
	}

	public void refreshData(boolean shouldLerp, float lerpProgress, Level world) {
//		this.data = new Data();
		if (shouldLerp && this.data != null) {
			this.data = this.getLerpedRenderData(this.data, lerpProgress, world);
		} else {
			this.data = this.copyData(world);
		}

		this.textLines = null;
	}

	private Data getLerpedRenderData(Data data, float lerpProgress, Level world) {
		int i = data.backgroundColor.lerp(lerpProgress);
		int j = data.textOpacity.lerp(lerpProgress);
		return new Data(
				this.getCompleteTextString(world),
				this.getLineWidth(),
				new IntLerperImpl(j, this.getTextOpacity()),
				new ArgbLerper(i, this.getBackground()),
				this.getDisplayFlags()
		);
	}

	private Data copyData(Level world) {
		return new Data(
				this.getCompleteTextString(world),
				this.getLineWidth(),
				IntLerper.constant(this.getTextOpacity()),
				IntLerper.constant(this.getBackground()),
				this.getDisplayFlags()
		);
	}

	public final void resetPosition() {
//		double d = this.getDisplayOffsetX();
//		double e = this.getDisplayOffsetY();
//		double f = this.getDisplayOffsetZ();
//		this.prevX = d;
//		this.prevY = e;
//		this.prevZ = f;
//		this.lastRenderX = d;
//		this.lastRenderY = e;
//		this.lastRenderZ = f;
		this.displayPrevYaw = this.getDisplayYaw();
		this.displayPrevPitch = this.getDisplayPitch();
	}

	public static void tick(Level world, BlockPos pos, BlockState state, TriggeredDisplayBlockEntity blockEntity) {

//		if (!world.isClient && blockEntity.remainingTicks > 0) {
//			blockEntity.remainingTicks--;
//			if (blockEntity.remainingTicks <= 0) {
//				blockEntity.triggerTriggeredBlock();
//			}
//		}
		blockEntity.age++;

		if (world.isClientSide()) {
			if (blockEntity.startInterpolationSet) {
				blockEntity.startInterpolationSet = false;
				int i = blockEntity.getStartInterpolation();
				blockEntity.interpolationStart = (long) (blockEntity.age + i);
			}

			if (blockEntity.interpolationDurationSet) {
				blockEntity.interpolationDurationSet = false;
				blockEntity.currentInterpolationDuration = blockEntity.getInterpolationDuration();
			}

			if (blockEntity.renderingDataSet) {
				blockEntity.renderingDataSet = false;
				boolean bl = blockEntity.currentInterpolationDuration != 0;
				if (bl && blockEntity.renderState != null) {
					blockEntity.renderState = blockEntity.getLerpedRenderState(blockEntity.renderState, blockEntity.lerpProgress);
				} else {
					blockEntity.renderState = blockEntity.copyRenderState();
				}

				blockEntity.refreshData(bl, blockEntity.lerpProgress, world);
			}

			if (blockEntity.interpolationTarget != null) {
				if (blockEntity.interpolationTarget.step == 0) {
					blockEntity.interpolationTarget.apply(blockEntity);
					blockEntity.resetPosition();
					blockEntity.interpolationTarget = null;
				} else {
					blockEntity.interpolationTarget.applyInterpolated(blockEntity);
					blockEntity.interpolationTarget.step--;
					if (blockEntity.interpolationTarget.step == 0) {
						blockEntity.interpolationTarget = null;
					}
				}
			}
		}
	}

	private static byte readFlag(byte flags, ValueInput view, String nbtKey, byte flag) {
		return view.getBooleanOr(nbtKey, false) ? (byte) (flags | flag) : flags;
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		// common
		this.setTransformation(view.read(TRANSFORMATION_NBT_KEY, Transformation.EXTENDED_CODEC).orElse(Transformation.IDENTITY));

		this.setInterpolationDuration(view.getIntOr(INTERPOLATION_DURATION_KEY, 0));

		this.setStartInterpolation(view.getIntOr(START_INTERPOLATION_KEY, 0));

		this.setTeleportDuration(Mth.clamp(view.getIntOr(TELEPORT_DURATION_KEY, 0), 0, 59));

		this.setBillboardConstraints(view.read(BILLBOARD_NBT_KEY, BillboardConstraints.CODEC).orElse(BillboardConstraints.FIXED));

		this.setViewRange(view.getFloatOr(VIEW_RANGE_NBT_KEY, 1.0F));

		this.setShadowRadius(view.getFloatOr(SHADOW_RADIUS_NBT_KEY, 0.0F));

		this.setShadowStrength(view.getFloatOr(SHADOW_STRENGTH_NBT_KEY, 1.0F));

		this.setDisplayWidth(view.getFloatOr(WIDTH_NBT_KEY, 0.0F));

		this.setDisplayHeight(view.getFloatOr(HEIGHT_NBT_KEY, 0.0F));

		this.setGlowColorOverride(view.getIntOr(GLOW_COLOR_OVERRIDE_NBT_KEY, -1));

		this.setBrightnessOverride(view.read(BRIGHTNESS_NBT_KEY, Brightness.CODEC).orElse(null));

		this.setDisplayMode(view.read(DISPLAY_MODE_NBT_KEY, DisplayMode.CODEC).orElse(DisplayMode.TEXT));

		this.isTriggered = view.getBooleanOr(IS_TRIGGERED_NBT_KEY, true);

		this.setDisplayOffset(view.read(DISPLAY_OFFSET_NBT_KEY, Vec3.CODEC).orElse(DISPLAY_OFFSET_DEFAULT));

		this.displayYaw = view.getFloatOr(DISPLAY_YAW_NBT_KEY, 0.0F);

		this.displayPitch = view.getFloatOr(DISPLAY_PITCH_NBT_KEY, 0.0F);

		this.displayPrevYaw = view.getFloatOr(DISPLAY_PREV_YAW_NBT_KEY, 0.0F);

		this.displayPrevPitch = view.getFloatOr(DISPLAY_PREV_PITCH_NBT_KEY, 0.0F);
//		// item mode
//		if (nbt.contains("item")) {
//			this.setItemStack((ItemStack)ItemStack.fromNbt(registryLookup, view.getCompound("item")).orElse(ItemStack.EMPTY));
//		} else {
//			this.setItemStack(ItemStack.EMPTY);
//		}
//
//		if (nbt.contains("item_display", NbtElement.STRING_TYPE)) {
//			ModelTransformationMode.CODEC
//					.decode(NbtOps.INSTANCE, view.get("item_display"))
//					.resultOrPartial(Util.addPrefix("Display entity", ScriptBlocks.LOGGER::error))
//					.ifPresent(mode -> this.setTransformationMode((ModelTransformationMode)mode.getFirst()));
//		}

		// text mode
		this.setLineWidth(view.getIntOr(LINE_WIDTH_NBT_KEY, INITIAL_LINE_WIDTH));

		this.setTextOpacity(view.getByteOr(TEXT_OPACITY_NBT_KEY, INITIAL_TEXT_OPACITY));

		this.setBackground(view.getIntOr(BACKGROUND_NBT_KEY, INITIAL_BACKGROUND));

		byte b = readFlag((byte) 0, view, SHADOW_NBT_KEY, SHADOW_FLAG);
		b = readFlag(b, view, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
		b = readFlag(b, view, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
		Optional<TextAlignment> optional = view.read(ALIGNMENT_NBT_KEY, TextAlignment.CODEC);
		if (optional.isPresent()) {
			b = switch (optional.get()) {
				case CENTER -> b;
				case LEFT -> (byte) (b | LEFT_ALIGNMENT_FLAG);
				case RIGHT -> (byte) (b | RIGHT_ALIGNMENT_FLAG);
			};
		}
		this.setDisplayFlags(b);

		this.setTextString(view.getStringOr(TEXT_STRING_NBT_KEY, ""));

		this.setDataProvidingBlockPosOffset(view.read(DATA_PROVIDER_BLOCK_POS_OFFSET_NBT_KEY, BlockPos.CODEC).orElse(BlockPos.ZERO));

		this.setDataIdentifierString(view.getStringOr(DATA_IDENTIFIER_NBT_KEY, ""));

		super.loadAdditional(view);

	}

	private static void writeFlag(byte flags, ValueOutput view, String nbtKey, byte flag) {
		view.putBoolean(nbtKey, (flags & flag) != 0);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		// common
		view.store(TRANSFORMATION_NBT_KEY, Transformation.EXTENDED_CODEC, getTransformation(this));
		view.store(BILLBOARD_NBT_KEY, BillboardConstraints.CODEC, this.getBillboardConstraints());
		view.putInt(INTERPOLATION_DURATION_KEY, this.getInterpolationDuration());
		view.putInt(START_INTERPOLATION_KEY, this.getStartInterpolation());
		view.putInt(TELEPORT_DURATION_KEY, this.getTeleportDuration());
		view.putFloat(VIEW_RANGE_NBT_KEY, this.getViewRange());
		view.putFloat(SHADOW_RADIUS_NBT_KEY, this.getShadowRadius());
		view.putFloat(SHADOW_STRENGTH_NBT_KEY, this.getShadowStrength());
		view.putFloat(WIDTH_NBT_KEY, this.getDisplayWidth());
		view.putFloat(HEIGHT_NBT_KEY, this.getDisplayHeight());
		view.putInt(GLOW_COLOR_OVERRIDE_NBT_KEY, this.getGlowColorOverride());
		view.storeNullable(BRIGHTNESS_NBT_KEY, Brightness.CODEC, this.getBrightnessOverride());

		view.putBoolean(IS_TRIGGERED_NBT_KEY, this.isTriggered);
		view.store(DISPLAY_OFFSET_NBT_KEY, Vec3.CODEC, this.displayOffset);

		view.putFloat(DISPLAY_YAW_NBT_KEY, this.displayYaw);
		view.putFloat(DISPLAY_PITCH_NBT_KEY, this.displayPitch);
		view.putFloat(DISPLAY_PREV_YAW_NBT_KEY, this.displayPrevYaw);
		view.putFloat(DISPLAY_PREV_PITCH_NBT_KEY, this.displayPrevPitch);

//		// item mode
//		if (!this.getItemStack().isEmpty()) {
//			view.put("item", this.getItemStack().encode(registryLookup));
//		}
//		ModelTransformationMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getTransformationMode()).ifSuccess(nbtx -> view.put("item_display", nbtx));

		// text mode
		view.putString(TEXT_STRING_NBT_KEY, this.textString);
		view.store(DATA_PROVIDER_BLOCK_POS_OFFSET_NBT_KEY, BlockPos.CODEC, this.dataProvidingBlockPosOffset);
		view.putString(DATA_IDENTIFIER_NBT_KEY, this.dataIdentifierString);
		view.putInt(LINE_WIDTH_NBT_KEY, this.getLineWidth());
		view.putByte(TEXT_OPACITY_NBT_KEY, this.getTextOpacity());
		view.putInt(BACKGROUND_NBT_KEY, this.getBackground());
		byte b = this.getDisplayFlags();
		writeFlag(b, view, SHADOW_NBT_KEY, SHADOW_FLAG);
		writeFlag(b, view, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
		writeFlag(b, view, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
		view.store(ALIGNMENT_NBT_KEY, TextAlignment.CODEC, getAlignment(b));

		super.saveAdditional(view);

	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---
	// common

	public double getLerpTargetX() {
		return this.interpolationTarget != null ? this.interpolationTarget.x : this.getDisplayOffset().x;
	}

	public double getLerpTargetY() {
		return this.interpolationTarget != null ? this.interpolationTarget.y : this.getDisplayOffset().y;
	}

	public double getLerpTargetZ() {
		return this.interpolationTarget != null ? this.interpolationTarget.z : this.getDisplayOffset().z;
	}

	public float getLerpTargetPitch() {
		return this.interpolationTarget != null ? (float) this.interpolationTarget.pitch : this.getDisplayPitch();
	}

	public float getLerpTargetYaw() {
		return this.interpolationTarget != null ? (float) this.interpolationTarget.yaw : this.getDisplayYaw();
	}

	@Nullable
	public RenderState getRenderState() {
		return this.renderState;
	}

	public int getTeleportDuration() {
		return this.teleportDuration;
	}

	public void setTeleportDuration(int teleportDuration) {
		this.teleportDuration = teleportDuration;
	}

	public int getStartInterpolation() {
		return this.startInterpolation;
	}

	public void setStartInterpolation(int startInterpolation) {
		this.startInterpolation = startInterpolation;
	}

	public int getInterpolationDuration() {
		return this.interpolationDuration;
	}

	public void setInterpolationDuration(int interpolationDuration) {
		this.interpolationDuration = interpolationDuration;
	}

	public Vector3fc getTranslation() {
		return this.translation;
	}

	public void setTranslation(Vector3fc translation) {
		this.translation = translation;
	}

	public Vector3fc getScale() {
		return this.scale;
	}

	public void setScale(Vector3fc scale) {
		this.scale = scale;
	}

	public Quaternionfc getLeftRotation() {
		return this.leftRotation;
	}

	public void setLeftRotation(Quaternionfc leftRotation) {
		this.leftRotation = leftRotation;
	}

	public Quaternionfc getRightRotation() {
		return this.rightRotation;
	}

	public void setRightRotation(Quaternionfc rightRotation) {
		this.rightRotation = rightRotation;
	}

	public void setBillboardConstraints(BillboardConstraints billboardConstraints) {
		this.billboard = billboardConstraints.getId();
	}

	public BillboardConstraints getBillboardConstraints() {
		return (BillboardConstraints) BillboardConstraints.BY_ID.apply(this.billboard);
	}

	public void setBrightnessOverride(@Nullable Brightness brightness) {
		this.brightness = brightness != null ? brightness.pack() : -1;
	}

	@Nullable
	public Brightness getBrightnessOverride() {
		int i = this.brightness;
		return i != -1 ? Brightness.unpack(i) : null;
	}

	public int getBrightness() {
		return this.brightness;
	}

	public void setViewRange(float viewRange) {
		this.viewRange = viewRange;
	}

	public float getViewRange() {
		return this.viewRange;
	}

	public void setShadowRadius(float shadowRadius) {
		this.shadowRadius = shadowRadius;
	}

	public float getShadowRadius() {
		return this.shadowRadius;
	}

	public void setShadowStrength(float shadowStrength) {
		this.shadowStrength = shadowStrength;
	}

	public float getShadowStrength() {
		return this.shadowStrength;
	}

	public void setDisplayWidth(float width) {
		this.width = width;
	}

	public float getDisplayWidth() {
		return this.width;
	}

	public void setDisplayHeight(float height) {
		this.height = height;
	}

	public int getGlowColorOverride() {
		return this.glowColorOverride;
	}

	public void setGlowColorOverride(int glowColorOverride) {
		this.glowColorOverride = glowColorOverride;
	}

	public float getLerpProgress(float delta) {
		int i = this.interpolationDuration;
		if (i <= 0) {
			return 1.0F;
		} else {
			float f = (float) ((long) this.age - this.interpolationStart);
			float g = f + delta;
			float h = Mth.clamp(Mth.inverseLerp(g, 0.0F, (float) i), 0.0F, 1.0F);
			this.lerpProgress = h;
			return h;
		}
	}

	protected void lerpDisplayPosAndRotation(int step, double x, double y, double z, double yaw, double pitch) {
		double d = 1.0 / (double) step;
		double e = Mth.lerp(d, this.getDisplayOffset().x, x);
		double f = Mth.lerp(d, this.getDisplayOffset().y, y);
		double g = Mth.lerp(d, this.getDisplayOffset().z, z);
		float h = (float) Mth.rotLerp(d, (double) this.getDisplayYaw(), yaw);
		float i = (float) Mth.lerp(d, (double) this.getDisplayPitch(), pitch);
		this.setDisplayOffset(new Vec3(e, f, g));
		this.setDisplayRotation(h, i);
	}

	private float getDisplayHeight() {
		return this.height;
	}


//	// item mode
//	public ItemStack getItemStack() {
//		return this.itemStack;
//	}
//
//	public void setItemStack(ItemStack stack) {
//		this.itemStack = stack;
//	}
//
//	public ModelTransformationMode getTransformationMode() {
//		return (ModelTransformationMode)ModelTransformationMode.FROM_INDEX.apply(this.itemDisplayMode);
//	}
//
//	public void setTransformationMode(ModelTransformationMode transformationMode) {
//		this.itemDisplayMode = transformationMode.getIndex();
//	}

	// text mode

	public String getCompleteTextString(Level world) {
		String completeString = this.getTextString();
//		ScriptBlocks.info("getCompleteTextString pre, completeString:" + completeString);
//			ScriptBlocks.info("getCompleteTextString, dataIdentifierString:" + this.getDataIdentifierString());
		BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ZERO) {
			BlockEntity blockEntity = world.getBlockEntity(this.getBlockPos().offset(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ()));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				completeString = completeString + providesDataBlockEntity.getData(this.getDataIdentifierString());
			}
		}
//		ScriptBlocks.info("getCompleteTextString post, completeString:" + completeString);
		return completeString;
	}

	public String getTextString() {
		return this.textString;
	}

	public void setTextString(String textString) {
		this.textString = textString;
		this.renderingDataSet = true;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public String getDataIdentifierString() {
		return this.dataIdentifierString;
	}

	public void setDataIdentifierString(String dataIdentifierString) {
		this.dataIdentifierString = dataIdentifierString;
	}

	public Component getText() {
		return this.text;
	}

	public void setText(Component text) {
		this.text = text;
		this.renderingDataSet = true;
	}

	public int getLineWidth() {
		return this.lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
		this.renderingDataSet = true;
	}

	public byte getTextOpacity() {
		return this.textOpacity;
	}

	public void setTextOpacity(byte textOpacity) {
		this.textOpacity = textOpacity;
		this.renderingDataSet = true;
	}

	public int getBackground() {
		return this.background;
	}

	public void setBackground(int background) {
		this.background = background;
		this.renderingDataSet = true;
	}

	public byte getDisplayFlags() {
		return this.textDisplayFlags;
	}

	public void setDisplayFlags(byte flags) {
		this.textDisplayFlags = flags;
		this.renderingDataSet = true;
	}

	// utility
	public float getDisplayYaw() {
		return this.displayYaw;
	}

	public float getDisplayPitch() {
		return this.displayPitch;
	}

	public float getPrevDisplayYaw() {
		return this.displayPrevYaw;
	}

	public float getPrevDisplayPitch() {
		return this.displayPrevPitch;
	}

	public Vec3 getDisplayOffset() {
		return this.displayOffset;
	}

	public void setDisplayOffset(Vec3 displayOffset) {
		this.displayOffset = displayOffset;
	}

	public void setDisplayRotation(float yaw, float pitch) {
		this.displayYaw = yaw;
		this.displayPitch = pitch;
		this.displayPrevYaw = yaw;
		this.displayPrevPitch = pitch;
	}

	public DisplayMode getDisplayMode() {
		return this.displayMode;
	}

	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
	}

	public boolean getIsTriggered() {
		return this.isTriggered;
	}

	public void setIsTriggered(boolean isTriggered) {
		this.isTriggered = isTriggered;
	}
	// endregion --- getter & setter ---

	// TODO
	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
//				this.entitySpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.entitySpawnPositionOffset, blockRotation);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.entitySpawnOrientationYaw, blockRotation);
//				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.useRelayBlockPositionOffset, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
//				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.FRONT_BACK);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.FRONT_BACK);
//				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
//				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.LEFT_RIGHT);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.LEFT_RIGHT);
//				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.isTriggered) {
			this.isTriggered = false;
			this.renderingDataSet = true;
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
		}
	}

	@Override
	public void trigger() {
		this.isTriggered = true;
		this.renderingDataSet = true;
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	private RenderState copyRenderState() {
		return new RenderState(
				AbstractInterpolator.constant(getTransformation(this)),
				this.getBillboardConstraints(),
				this.getBrightness(),
				FloatLerper.constant(this.getShadowRadius()),
				FloatLerper.constant(this.getShadowStrength()),
				this.getGlowColorOverride()
		);
	}

	private RenderState getLerpedRenderState(RenderState state, float lerpProgress) {
		Transformation affineTransformation = state.transformation.interpolate(lerpProgress);
		float f = state.shadowRadius.lerp(lerpProgress);
		float g = state.shadowStrength.lerp(lerpProgress);
		return new RenderState(
				new AffineTransformationInterpolator(affineTransformation, getTransformation(this)),
				this.getBillboardConstraints(),
				this.getBrightness(),
				new FloatLerperImpl(f, this.getShadowRadius()),
				new FloatLerperImpl(g, this.getShadowStrength()),
				this.getGlowColorOverride()
		);
	}

	@FunctionalInterface
	public interface AbstractInterpolator<T> {
		static <T> AbstractInterpolator<T> constant(T value) {
			return delta -> value;
		}

		T interpolate(float delta);
	}

	static record AffineTransformationInterpolator(Transformation previous, Transformation current)
			implements AbstractInterpolator<Transformation> {
		public Transformation interpolate(float f) {
			return (double) f >= 1.0 ? this.current : this.previous.slerp(this.current, f);
		}
	}

	static record ArgbLerper(int previous, int current) implements IntLerper {
		@Override
		public int lerp(float delta) {
			return ARGB.srgbLerp(delta, this.previous, this.current);
		}
	}

	@FunctionalInterface
	public interface FloatLerper {
		static FloatLerper constant(float value) {
			return delta -> value;
		}

		float lerp(float delta);
	}

	static record FloatLerperImpl(float previous, float current) implements FloatLerper {
		@Override
		public float lerp(float delta) {
			return Mth.lerp(delta, this.previous, this.current);
		}
	}

	@FunctionalInterface
	public interface IntLerper {
		static IntLerper constant(int value) {
			return delta -> value;
		}

		int lerp(float delta);
	}

	static record IntLerperImpl(int previous, int current) implements IntLerper {
		@Override
		public int lerp(float delta) {
			return Mth.lerpInt(delta, this.previous, this.current);
		}
	}

	public enum DisplayMode implements StringRepresentable {
		BLOCK("block"),
		TEXT("text"),
		ITEM("item");

		private final String name;

		private DisplayMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static final Codec<DisplayMode> CODEC = StringRepresentable.fromEnum(DisplayMode::values);

		public static Optional<DisplayMode> byName(String name) {
			return Arrays.stream(DisplayMode.values()).filter(displayMode -> displayMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.triggered_display_block.display_mode." + this.name);
		}
	}

	public enum BillboardConstraints implements StringRepresentable {
		FIXED((byte) 0, "fixed"),
		VERTICAL((byte) 1, "vertical"),
		HORIZONTAL((byte) 2, "horizontal"),
		CENTER((byte) 3, "center");

		public static final Codec<BillboardConstraints> CODEC = StringRepresentable.fromEnum(BillboardConstraints::values);
		public static final IntFunction<BillboardConstraints> BY_ID = ByIdMap.continuous(
				BillboardConstraints::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
		);
		private final byte id;
		private final String name;

		BillboardConstraints(final byte id, final String name) {
			this.name = name;
			this.id = id;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		private byte getId() {
			return this.id;
		}

		public Component asText() {
			return Component.translatable("gui.triggered_display_block.billboard_constraints." + this.name);
		}
	}

	public record RenderState(
			AbstractInterpolator<Transformation> transformation,
			BillboardConstraints billboardConstraints,
			int brightnessOverride,
			FloatLerper shadowRadius,
			FloatLerper shadowStrength,
			int glowColorOverride
	) {
	}

	static class InterpolationTarget {
		int step;
		final double x;
		final double y;
		final double z;
		final double yaw;
		final double pitch;

		InterpolationTarget(int step, double x, double y, double z, double yaw, double pitch) {
			this.step = step;
			this.x = x;
			this.y = y;
			this.z = z;
			this.yaw = yaw;
			this.pitch = pitch;
		}

		void apply(TriggeredDisplayBlockEntity entity) {
			entity.setDisplayOffset(new Vec3(this.x, this.y, this.z));
			entity.setDisplayRotation((float) this.yaw, (float) this.pitch);
		}

		void applyInterpolated(TriggeredDisplayBlockEntity entity) {
			entity.lerpDisplayPosAndRotation(this.step, this.x, this.y, this.z, this.yaw, this.pitch);
		}
	}

	public TextLines splitLines(LineSplitter splitter) {
		if (this.textLines == null) {
			if (this.data != null) {
				this.textLines = splitter.split(Component.translatable(this.data.textString()), this.data.lineWidth());
			} else {
				this.textLines = new TextLines(List.of(), 0);
			}
		}

		return this.textLines;
	}

	public static TextAlignment getAlignment(byte flags) {
		if ((flags & LEFT_ALIGNMENT_FLAG) != 0) {
			return TextAlignment.LEFT;
		} else {
			return (flags & RIGHT_ALIGNMENT_FLAG) != 0 ? TextAlignment.RIGHT : TextAlignment.CENTER;
		}
	}

	public static record Data(/*BlockState blockState, ItemStack itemStack, ModelTransformationMode itemTransform, */
			String textString, int lineWidth, IntLerper textOpacity, IntLerper backgroundColor, byte flags) {
	}

	@FunctionalInterface
	public interface LineSplitter {
		TextLines split(Component text, int lineWidth);
	}

	public static enum TextAlignment implements StringRepresentable {
		CENTER("center"),
		LEFT("left"),
		RIGHT("right");

		public static final Codec<TextAlignment> CODEC = StringRepresentable.fromEnum(
				TextAlignment::values
		);
		private final String name;

		private TextAlignment(final String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	public static record TextLine(FormattedCharSequence contents, int width) {
	}

	public static record TextLines(List<TextLine> lines, int width) {
	}
}
