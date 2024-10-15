package com.github.theredbrain.scriptblocks.util;

public class ItemUtils {
	public static byte parseByte(String string) {
		try {
			return Byte.parseByte(string);
		} catch (NumberFormatException numberFormatException) {
			return 0;
		}
	}

	public static short parseShort(String string) {
		try {
			return Short.parseShort(string);
		} catch (NumberFormatException numberFormatException) {
			return 0;
		}
	}

	public static int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException numberFormatException) {
			return 0;
		}
	}

	public static long parseLong(String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException numberFormatException) {
			return 0;
		}
	}

	public static float parseFloat(String string) {
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException numberFormatException) {
			return 0.0f;
		}
	}

	public static double parseDouble(String string) {
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException numberFormatException) {
			return 0.0;
		}
	}
}
