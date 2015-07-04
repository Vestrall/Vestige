package com.lescomber.vestige.geometry;

public class Angle {
	private static final float epsilon = 0.0001f;

	public static final float EAST = 0.0f;
	public static final float SOUTH = (float) Math.PI / 2;
	public static final float WEST = (float) Math.PI;
	public static final float NORTH = 3 * (float) Math.PI / 2;

	public static float normalizeRadians(float radians) {
		float nRadians = radians;

		while (nRadians < 0)
			nRadians += 2 * Math.PI;
		while (nRadians >= 2 * Math.PI)
			nRadians -= 2 * Math.PI;

		return nRadians;
	}

	public static boolean isInRange(float angle, float start, float end) {
		final float theta = normalizeRadians(angle);
		final float low = normalizeRadians(start);
		final float high = normalizeRadians(end);

		if (low <= high)
			return (theta <= high && theta >= low);
		else
			return (theta <= high || theta >= low);
	}
}