package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_X_NBT_KEY = "data_provider_block_pos_offset_x";
	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_Y_NBT_KEY = "data_provider_block_pos_offset_y";
	public static final String DATA_PROVIDER_BLOCK_POS_OFFSET_Z_NBT_KEY = "data_provider_block_pos_offset_z";
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

	private static final Vec3d DISPLAY_OFFSET_DEFAULT = new Vec3d(0, 0, 0);
	// text mode
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private static final Text INITIAL_TEXT = Text.literal("Loooooooooooooooooong test text");//Text.empty();
	private static final int INITIAL_LINE_WIDTH = 200;
	private static final byte INITIAL_TEXT_OPACITY = -1;
	public static final int INITIAL_BACKGROUND = 1073741824;
	//endregion default values

	// common
	private int teleportDuration = 0;
	private int startInterpolation = 0;
	private int interpolationDuration = 0;
	private Vector3f translation = new Vector3f();
	private Vector3f scale = new Vector3f(1, 1, 1);
	private Quaternionf leftRotation = new Quaternionf();
	private Quaternionf rightRotation = new Quaternionf();
	private Byte billboard = BillboardMode.FIXED.getIndex();
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
	private Box visibilityBoundingBox;
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
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private String dataIdentifierString = "";
	private Text text = INITIAL_TEXT;
	private int lineWidth = INITIAL_LINE_WIDTH;
	private int background = INITIAL_BACKGROUND;
	private Byte textOpacity = INITIAL_TEXT_OPACITY;
	private Byte textDisplayFlags = 0;
	@Nullable
	private TextLines textLines;
	@Nullable
	private Data data;

	private Vec3d displayOffset = DISPLAY_OFFSET_DEFAULT;
	private DisplayMode displayMode = DisplayMode.TEXT;
	private boolean isTriggered = true;

	// entity replacements
	private long age;
	private float displayYaw = 0.0F;
	private float displayPitch = 0.0F;
	private float displayPrevYaw = 0.0F;
	private float displayPrevPitch = 0.0F;

	public TriggeredDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_DISPLAY_BLOCK_ENTITY, pos, state);
		this.age = 0;
	}

	private static AffineTransformation getTransformation(TriggeredDisplayBlockEntity blockEntity) {
		Vector3f vector3f = blockEntity.getTranslation();
		Quaternionf quaternionf = blockEntity.getLeftRotation();
		Vector3f vector3f2 = blockEntity.getScale();
		Quaternionf quaternionf2 = blockEntity.getRightRotation();
		return new AffineTransformation(vector3f, quaternionf, vector3f2, quaternionf2);
	}

	private void setTransformation(AffineTransformation transformation) {
		this.setTranslation(transformation.getTranslation());
		this.setLeftRotation(transformation.getLeftRotation());
		this.setScale(transformation.getScale());
		this.setRightRotation(transformation.getRightRotation());
	}

	@Nullable
	public Data getData() {
		return this.data;
	}

	public void refreshData(boolean shouldLerp, float lerpProgress, World world) {
//		this.data = new Data();
		if (shouldLerp && this.data != null) {
			this.data = this.getLerpedRenderData(this.data, lerpProgress, world);
		} else {
			this.data = this.copyData(world);
		}

		this.textLines = null;
	}

	private Data getLerpedRenderData(Data data, float lerpProgress, World world) {
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

	private Data copyData(World world) {
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

	public static void tick(World world, BlockPos pos, BlockState state, TriggeredDisplayBlockEntity blockEntity) {

//		if (!world.isClient && blockEntity.remainingTicks > 0) {
//			blockEntity.remainingTicks--;
//			if (blockEntity.remainingTicks <= 0) {
//				blockEntity.triggerTriggeredBlock();
//			}
//		}
		blockEntity.age++;

		if (world.isClient) {
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

	private static byte readFlag(byte flags, NbtCompound nbt, String nbtKey, byte flag) {
		return nbt.getBoolean(nbtKey) ? (byte) (flags | flag) : flags;
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		super.readNbt(nbt, registryLookup);

		// common
		if (nbt.contains("transformation")) {
			AffineTransformation.ANY_CODEC
					.decode(NbtOps.INSTANCE, nbt.get("transformation"))
					.resultOrPartial(Util.addPrefix(ERROR_PREFIX, ScriptBlocks.LOGGER::error))
					.ifPresent(pair -> this.setTransformation((AffineTransformation) pair.getFirst()));
		}

		if (nbt.contains("interpolation_duration", NbtElement.NUMBER_TYPE)) {
			int i = nbt.getInt("interpolation_duration");
			this.setInterpolationDuration(i);
		}

		if (nbt.contains("start_interpolation", NbtElement.NUMBER_TYPE)) {
			int i = nbt.getInt("start_interpolation");
			this.setStartInterpolation(i);
		}

		if (nbt.contains("teleport_duration", NbtElement.NUMBER_TYPE)) {
			int i = nbt.getInt("teleport_duration");
			this.setTeleportDuration(MathHelper.clamp(i, 0, 59));
		}

		if (nbt.contains("billboard", NbtElement.STRING_TYPE)) {
			BillboardMode.CODEC
					.decode(NbtOps.INSTANCE, nbt.get("billboard"))
					.resultOrPartial(Util.addPrefix(ERROR_PREFIX, ScriptBlocks.LOGGER::error))
					.ifPresent(pair -> this.setBillboardMode((BillboardMode) pair.getFirst()));
		}

		if (nbt.contains("view_range", NbtElement.NUMBER_TYPE)) {
			this.setViewRange(nbt.getFloat("view_range"));
		}

		if (nbt.contains("shadow_radius", NbtElement.NUMBER_TYPE)) {
			this.setShadowRadius(nbt.getFloat("shadow_radius"));
		}

		if (nbt.contains("shadow_strength", NbtElement.NUMBER_TYPE)) {
			this.setShadowStrength(nbt.getFloat("shadow_strength"));
		}

		if (nbt.contains("width", NbtElement.NUMBER_TYPE)) {
			this.setDisplayWidth(nbt.getFloat("width"));
		}

		if (nbt.contains("height", NbtElement.NUMBER_TYPE)) {
			this.setDisplayHeight(nbt.getFloat("height"));
		}

		if (nbt.contains("glow_color_override", NbtElement.NUMBER_TYPE)) {
			this.setGlowColorOverride(nbt.getInt("glow_color_override"));
		}

		if (nbt.contains("brightness", NbtElement.COMPOUND_TYPE)) {
			Brightness.CODEC
					.decode(NbtOps.INSTANCE, nbt.get("brightness"))
					.resultOrPartial(Util.addPrefix(ERROR_PREFIX, ScriptBlocks.LOGGER::error))
					.ifPresent(pair -> this.setBrightness((Brightness) pair.getFirst()));
		} else {
			this.setBrightness(null);
		}

		if (nbt.contains(DISPLAY_MODE_NBT_KEY, NbtElement.STRING_TYPE)) {
			DisplayMode.CODEC
					.decode(NbtOps.INSTANCE, nbt.get(DISPLAY_MODE_NBT_KEY))
					.resultOrPartial(Util.addPrefix(ERROR_PREFIX, ScriptBlocks.LOGGER::error))
					.ifPresent(pair -> this.setDisplayMode((DisplayMode) pair.getFirst()));
		}

		if (nbt.contains(IS_TRIGGERED_NBT_KEY, NbtElement.BYTE_TYPE)) {
			this.isTriggered = nbt.getBoolean(IS_TRIGGERED_NBT_KEY);
		}

		if (nbt.contains(DISPLAY_OFFSET_X_NBT_KEY, NbtElement.NUMBER_TYPE) || nbt.contains(DISPLAY_OFFSET_Y_NBT_KEY, NbtElement.NUMBER_TYPE) || nbt.contains(DISPLAY_OFFSET_Z_NBT_KEY, NbtElement.NUMBER_TYPE)) {
			this.setDisplayOffset(new Vec3d(
					nbt.getDouble(DISPLAY_OFFSET_X_NBT_KEY),
					nbt.getDouble(DISPLAY_OFFSET_Y_NBT_KEY),
					nbt.getDouble(DISPLAY_OFFSET_Z_NBT_KEY)
			));
		}

		if (nbt.contains(DISPLAY_YAW_NBT_KEY)) {
			this.displayYaw = nbt.getFloat(DISPLAY_YAW_NBT_KEY);
		}
		if (nbt.contains(DISPLAY_PITCH_NBT_KEY)) {
			this.displayPitch = nbt.getFloat(DISPLAY_PITCH_NBT_KEY);
		}
		if (nbt.contains(DISPLAY_PREV_YAW_NBT_KEY)) {
			this.displayPrevYaw = nbt.getFloat(DISPLAY_PREV_YAW_NBT_KEY);
		}
		if (nbt.contains(DISPLAY_PREV_PITCH_NBT_KEY)) {
			this.displayPrevPitch = nbt.getFloat(DISPLAY_PREV_PITCH_NBT_KEY);
		}
//		// item mode
//		if (nbt.contains("item")) {
//			this.setItemStack((ItemStack)ItemStack.fromNbt(registryLookup, nbt.getCompound("item")).orElse(ItemStack.EMPTY));
//		} else {
//			this.setItemStack(ItemStack.EMPTY);
//		}
//
//		if (nbt.contains("item_display", NbtElement.STRING_TYPE)) {
//			ModelTransformationMode.CODEC
//					.decode(NbtOps.INSTANCE, nbt.get("item_display"))
//					.resultOrPartial(Util.addPrefix("Display entity", ScriptBlocks.LOGGER::error))
//					.ifPresent(mode -> this.setTransformationMode((ModelTransformationMode)mode.getFirst()));
//		}

		// text mode
		if (nbt.contains(LINE_WIDTH_NBT_KEY, NbtElement.NUMBER_TYPE)) {
			this.setLineWidth(nbt.getInt(LINE_WIDTH_NBT_KEY));
		}

		if (nbt.contains(TEXT_OPACITY_NBT_KEY, NbtElement.NUMBER_TYPE)) {
			this.setTextOpacity(nbt.getByte(TEXT_OPACITY_NBT_KEY));
		}

		if (nbt.contains(BACKGROUND_NBT_KEY, NbtElement.NUMBER_TYPE)) {
			this.setBackground(nbt.getInt(BACKGROUND_NBT_KEY));
		}

		byte b = readFlag((byte) 0, nbt, SHADOW_NBT_KEY, SHADOW_FLAG);
		b = readFlag(b, nbt, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
		b = readFlag(b, nbt, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
		Optional<TextAlignment> optional = TextAlignment.CODEC
				.decode(NbtOps.INSTANCE, nbt.get(ALIGNMENT_NBT_KEY))
				.resultOrPartial(Util.addPrefix(ERROR_PREFIX, ScriptBlocks.LOGGER::error))
				.map(Pair::getFirst);
		if (optional.isPresent()) {
			b = switch ((TextAlignment) optional.get()) {
				case CENTER -> b;
				case LEFT -> (byte) (b | LEFT_ALIGNMENT_FLAG);
				case RIGHT -> (byte) (b | RIGHT_ALIGNMENT_FLAG);
			};
		}
		this.setDisplayFlags(b);

		if (nbt.contains(TEXT_STRING_NBT_KEY, NbtElement.STRING_TYPE)) {
			this.setTextString(nbt.getString(TEXT_STRING_NBT_KEY));
		}

		if (nbt.contains(DATA_PROVIDER_BLOCK_POS_OFFSET_X_NBT_KEY, NbtElement.INT_TYPE) && nbt.contains(DATA_PROVIDER_BLOCK_POS_OFFSET_Y_NBT_KEY, NbtElement.INT_TYPE) && nbt.contains(DATA_PROVIDER_BLOCK_POS_OFFSET_Z_NBT_KEY, NbtElement.INT_TYPE)) {
			this.setDataProvidingBlockPosOffset(new BlockPos(nbt.getInt(DATA_PROVIDER_BLOCK_POS_OFFSET_X_NBT_KEY), nbt.getInt(DATA_PROVIDER_BLOCK_POS_OFFSET_Y_NBT_KEY), nbt.getInt(DATA_PROVIDER_BLOCK_POS_OFFSET_Z_NBT_KEY)));
		}

		if (nbt.contains(DATA_IDENTIFIER_NBT_KEY, NbtElement.STRING_TYPE)) {
			this.setDataIdentifierString(nbt.getString(DATA_IDENTIFIER_NBT_KEY));
		}

		if (nbt.contains(TEXT_NBT_KEY, NbtElement.STRING_TYPE)) {
			String string = nbt.getString(TEXT_NBT_KEY);

			try {
				Text text = Text.Serialization.fromJson(string, registryLookup);
				if (text != null) {
					Text text2 = Texts.parse(null, text, null, 0);
					this.setText(text2);
				} else {
					this.setText(Text.empty());
				}
			} catch (Exception var8) {
				ScriptBlocks.LOGGER.warn("Failed to parse Triggered Display Block text {}", string, var8);
			}
		}

	}

	private static void writeFlag(byte flags, NbtCompound nbt, String nbtKey, byte flag) {
		nbt.putBoolean(nbtKey, (flags & flag) != 0);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		super.writeNbt(nbt, registryLookup);

		// common
		AffineTransformation.ANY_CODEC
				.encodeStart(NbtOps.INSTANCE, getTransformation(this))
				.ifSuccess(transformations -> nbt.put("transformation", transformations));
		BillboardMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardMode()).ifSuccess(billboard -> nbt.put("billboard", billboard));
		nbt.putInt("interpolation_duration", this.getInterpolationDuration());
		nbt.putInt("teleport_duration", this.getTeleportDuration());
		nbt.putFloat("view_range", this.getViewRange());
		nbt.putFloat("shadow_radius", this.getShadowRadius());
		nbt.putFloat("shadow_strength", this.getShadowStrength());
		nbt.putFloat("width", this.getDisplayWidth());
		nbt.putFloat("height", this.getDisplayHeight());
		nbt.putInt("glow_color_override", this.getGlowColorOverride());
		Brightness brightness = this.getBrightnessUnpacked();
		if (brightness != null) {
			Brightness.CODEC.encodeStart(NbtOps.INSTANCE, brightness).ifSuccess(brightnessx -> nbt.put("brightness", brightnessx));
		}

		if (this.isTriggered) {
			nbt.putBoolean(IS_TRIGGERED_NBT_KEY, this.isTriggered);
		}
		if (this.displayOffset != DISPLAY_OFFSET_DEFAULT) {
			nbt.putDouble(DISPLAY_OFFSET_X_NBT_KEY, this.displayOffset.x);
			nbt.putDouble(DISPLAY_OFFSET_Y_NBT_KEY, this.displayOffset.y);
			nbt.putDouble(DISPLAY_OFFSET_Z_NBT_KEY, this.displayOffset.z);
		}

		nbt.putFloat(DISPLAY_YAW_NBT_KEY, this.displayYaw);
		nbt.putFloat(DISPLAY_PITCH_NBT_KEY, this.displayPitch);
		nbt.putFloat(DISPLAY_PREV_YAW_NBT_KEY, this.displayPrevYaw);
		nbt.putFloat(DISPLAY_PREV_PITCH_NBT_KEY, this.displayPrevPitch);

//		// item mode
//		if (!this.getItemStack().isEmpty()) {
//			nbt.put("item", this.getItemStack().encode(registryLookup));
//		}
//		ModelTransformationMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getTransformationMode()).ifSuccess(nbtx -> nbt.put("item_display", nbtx));

		// text mode
		nbt.putString(TEXT_STRING_NBT_KEY, this.textString);
		nbt.putInt(DATA_PROVIDER_BLOCK_POS_OFFSET_X_NBT_KEY, this.dataProvidingBlockPosOffset.getX());
		nbt.putInt(DATA_PROVIDER_BLOCK_POS_OFFSET_Y_NBT_KEY, this.dataProvidingBlockPosOffset.getY());
		nbt.putInt(DATA_PROVIDER_BLOCK_POS_OFFSET_Z_NBT_KEY, this.dataProvidingBlockPosOffset.getZ());
		nbt.putString(DATA_IDENTIFIER_NBT_KEY, this.dataIdentifierString);
		nbt.putString(TEXT_NBT_KEY, Text.Serialization.toJsonString(this.getText(), registryLookup));
		nbt.putInt(LINE_WIDTH_NBT_KEY, this.getLineWidth());
		nbt.putByte(TEXT_OPACITY_NBT_KEY, this.getTextOpacity());
		nbt.putInt(BACKGROUND_NBT_KEY, this.getBackground());
		byte b = this.getDisplayFlags();
		writeFlag(b, nbt, SHADOW_NBT_KEY, SHADOW_FLAG);
		writeFlag(b, nbt, SEE_THROUGH_NBT_KEY, SEE_THROUGH_FLAG);
		writeFlag(b, nbt, DEFAULT_BACKGROUND_NBT_KEY, DEFAULT_BACKGROUND_FLAG);
		TextAlignment.CODEC.encodeStart(NbtOps.INSTANCE, getAlignment(b)).ifSuccess(nbtElement -> nbt.put(ALIGNMENT_NBT_KEY, nbtElement));

	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	//region getter/setter
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

	public Vector3f getTranslation() {
		return this.translation;
	}

	public void setTranslation(Vector3f translation) {
		this.translation = translation;
	}

	public Vector3f getScale() {
		return this.scale;
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public Quaternionf getLeftRotation() {
		return this.leftRotation;
	}

	public void setLeftRotation(Quaternionf leftRotation) {
		this.leftRotation = leftRotation;
	}

	public Quaternionf getRightRotation() {
		return this.rightRotation;
	}

	public void setRightRotation(Quaternionf rightRotation) {
		this.rightRotation = rightRotation;
	}

	public void setBillboardMode(BillboardMode billboardMode) {
		this.billboard = billboardMode.getIndex();
	}

	public BillboardMode getBillboardMode() {
		return (BillboardMode) BillboardMode.FROM_INDEX.apply(this.billboard);
	}

	public void setBrightness(@Nullable Brightness brightness) {
		this.brightness = brightness != null ? brightness.pack() : -1;
	}

	@Nullable
	public Brightness getBrightnessUnpacked() {
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
			float h = MathHelper.clamp(MathHelper.getLerpProgress(g, 0.0F, (float) i), 0.0F, 1.0F);
			this.lerpProgress = h;
			return h;
		}
	}

	protected void lerpDisplayPosAndRotation(int step, double x, double y, double z, double yaw, double pitch) {
		double d = 1.0 / (double) step;
		double e = MathHelper.lerp(d, this.getDisplayOffset().x, x);
		double f = MathHelper.lerp(d, this.getDisplayOffset().y, y);
		double g = MathHelper.lerp(d, this.getDisplayOffset().z, z);
		float h = (float) MathHelper.lerpAngleDegrees(d, (double) this.getDisplayYaw(), yaw);
		float i = (float) MathHelper.lerp(d, (double) this.getDisplayPitch(), pitch);
		this.setDisplayOffset(new Vec3d(e, f, g));
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

	public String getCompleteTextString(World world) {
		String completeString = this.getTextString();
//		ScriptBlocks.info("getCompleteTextString pre, completeString:" + completeString);
//			ScriptBlocks.info("getCompleteTextString, dataIdentifierString:" + this.getDataIdentifierString());
			BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
			if (dataProvidingBlockPosOffset != BlockPos.ORIGIN) {
				BlockEntity blockEntity = world.getBlockEntity(this.getPos().add(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ()));
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

	public Text getText() {
		return this.text;
	}

	public void setText(Text text) {
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

	public Vec3d getDisplayOffset() {
		return this.displayOffset;
	}

	public void setDisplayOffset(Vec3d displayOffset) {
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
	//endregion getter/setter

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
//				this.entitySpawnPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.entitySpawnPositionOffset, blockRotation);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.rotateYaw(this.entitySpawnOrientationYaw, blockRotation);
//				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.useRelayBlockPositionOffset, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
//				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.FRONT_BACK);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.FRONT_BACK);
//				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
//				this.entitySpawnPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.entitySpawnPositionOffset, BlockMirror.LEFT_RIGHT);
//				this.entitySpawnOrientationYaw = BlockRotationUtils.mirrorYaw(this.entitySpawnOrientationYaw, BlockMirror.LEFT_RIGHT);
//				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
//				this.useRelayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.useRelayBlockPositionOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.isTriggered) {
			this.isTriggered = false;
			this.renderingDataSet = true;
		}
	}

	@Override
	public void trigger() {
		this.isTriggered = true;
		this.renderingDataSet = true;
	}

	private RenderState copyRenderState() {
		return new RenderState(
				AbstractInterpolator.constant(getTransformation(this)),
				this.getBillboardMode(),
				this.getBrightness(),
				FloatLerper.constant(this.getShadowRadius()),
				FloatLerper.constant(this.getShadowStrength()),
				this.getGlowColorOverride()
		);
	}

	private RenderState getLerpedRenderState(RenderState state, float lerpProgress) {
		AffineTransformation affineTransformation = state.transformation.interpolate(lerpProgress);
		float f = state.shadowRadius.lerp(lerpProgress);
		float g = state.shadowStrength.lerp(lerpProgress);
		return new RenderState(
				new AffineTransformationInterpolator(affineTransformation, getTransformation(this)),
				this.getBillboardMode(),
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

	static record AffineTransformationInterpolator(AffineTransformation previous, AffineTransformation current)
			implements AbstractInterpolator<AffineTransformation> {
		public AffineTransformation interpolate(float f) {
			return (double) f >= 1.0 ? this.current : this.previous.interpolate(this.current, f);
		}
	}

	static record ArgbLerper(int previous, int current) implements IntLerper {
		@Override
		public int lerp(float delta) {
			return ColorHelper.Argb.lerp(delta, this.previous, this.current);
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
			return MathHelper.lerp(delta, this.previous, this.current);
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
			return MathHelper.lerp(delta, this.previous, this.current);
		}
	}

	public static enum DisplayMode implements StringIdentifiable {
		BLOCK("block"),
		TEXT("text"),
		ITEM("item");

		private final String name;

		private DisplayMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static final Codec<DisplayMode> CODEC = StringIdentifiable.createCodec(DisplayMode::values);

		public static Optional<TriggeredDisplayBlockEntity.DisplayMode> byName(String name) {
			return Arrays.stream(TriggeredDisplayBlockEntity.DisplayMode.values()).filter(displayMode -> displayMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_display_block.display_mode." + this.name);
		}
	}

	public static enum BillboardMode implements StringIdentifiable {
		FIXED((byte) 0, "fixed"),
		VERTICAL((byte) 1, "vertical"),
		HORIZONTAL((byte) 2, "horizontal"),
		CENTER((byte) 3, "center");

		public static final Codec<BillboardMode> CODEC = StringIdentifiable.createCodec(BillboardMode::values);
		public static final IntFunction<BillboardMode> FROM_INDEX = ValueLists.createIdToValueFunction(
				BillboardMode::getIndex, values(), ValueLists.OutOfBoundsHandling.ZERO
		);
		private final byte index;
		private final String name;

		private BillboardMode(final byte index, final String name) {
			this.name = name;
			this.index = index;
		}

		@Override
		public String asString() {
			return this.name;
		}

		byte getIndex() {
			return this.index;
		}

		public static Optional<BillboardMode> byName(String name) {
			return Arrays.stream(BillboardMode.values()).filter(billboardMode -> billboardMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.triggered_display_block.billboard_mode." + this.name);
		}
	}

	public static record RenderState(
			AbstractInterpolator<AffineTransformation> transformation,
			BillboardMode billboardConstraints,
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
			entity.setDisplayOffset(new Vec3d(this.x, this.y, this.z));
			entity.setDisplayRotation((float) this.yaw, (float) this.pitch);
		}

		void applyInterpolated(TriggeredDisplayBlockEntity entity) {
			entity.lerpDisplayPosAndRotation(this.step, this.x, this.y, this.z, this.yaw, this.pitch);
		}
	}

	public TextLines splitLines(LineSplitter splitter) {
		if (this.textLines == null) {
			if (this.data != null) {
				this.textLines = splitter.split(Text.translatable(this.data.textString()), this.data.lineWidth());
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
		TextLines split(Text text, int lineWidth);
	}

	public static enum TextAlignment implements StringIdentifiable {
		CENTER("center"),
		LEFT("left"),
		RIGHT("right");

		public static final Codec<TextAlignment> CODEC = StringIdentifiable.createCodec(
				TextAlignment::values
		);
		private final String name;

		private TextAlignment(final String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}
	}

	public static record TextLine(OrderedText contents, int width) {
	}

	public static record TextLines(List<TextLine> lines, int width) {
	}
}
