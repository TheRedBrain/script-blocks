package com.github.theredbrain.scriptblocks.components;
//
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.registry.RegistryWrapper;
//
//public class LongComponent implements ILongComponent {
//	private long value = 0;
//
//	@Override
//	public long getValue() {
//		return this.value;
//	}
//
//	@Override
//	public void setValue(long value) {
//		this.value = value;
//	}
//
//	@Override
//	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
//		this.value = 0;
//		if (tag.getKeys().contains("value")) {
//			value = tag.getLong("value");
//		}
//	}
//
//	@Override
//	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
//		tag.putLong("value", this.value);
//	}
//}
