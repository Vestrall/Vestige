package com.lescomber.vestige.geometry;

import com.lescomber.vestige.framework.Util;

public class Point {
	public float x;
	public float y;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point(Point copyMe) {
		x = copyMe.x;
		y = copyMe.y;
	}

	public Point() {
		x = 0;
		y = 0;
	}

	public void offset(float dx, float dy) {
		x += dx;
		y += dy;
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public double distanceToPoint(Point other) {
		return Math.sqrt(distanceToPointSquared(other));
	}

	/**
	 * Distance calculation that avoids square root
	 */
	public double distanceToPointSquared(Point other) {
		return distanceToPointSquared(other.x, other.y);
	}

	public double distanceToPointSquared(float x, float y) {
		final float dx = this.x - x;
		final float dy = this.y - y;

		return (dx * dx + dy * dy);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Point))    // Note: can use getClass() instead of instanceof if subclasses
			return false;                                //are not meant to be compared positively with Points
		else {
			// Note: due to floating point inexactness, these Points are considered equal if their x and y coords
			//differ by less than our epsilon value
			final Point p = (Point) other;
			return Util.equals(x, p.x) && Util.equals(y, p.y);
		}
	}

	public Point getPointFromDirection(float direction, float distance) {
		return new Point(x + ((float) Math.cos(direction) * distance), y + ((float) Math.sin(direction) * distance));
	}

	/**
	 * Note: whenever rotating multiple points about the same (rotateX,rotateY) coords, use the rotate method below with the Point[] array as its
	 * first parameter for efficiency reasons
	 */
	public static void rotate(Point point, float radians, float rotateX, float rotateY) {
		final float angle = Angle.normalizeRadians(radians);
		final double cos = Math.cos(angle);
		final double sin = Math.sin(angle);

		final double tempX;
		final double tempY;

		tempX = cos * (point.x - rotateX) - sin * (point.y - rotateY) + rotateX;
		tempY = sin * (point.x - rotateX) + cos * (point.y - rotateY) + rotateY;
		point.set((float) tempX, (float) tempY);
	}

	public static void rotate(Point[] points, float radians, float rotateX, float rotateY) {
		final float angle = Angle.normalizeRadians(radians);
		final double cos = Math.cos(angle);
		final double sin = Math.sin(angle);

		double tempX;
		double tempY;
		for (final Point p : points) {
			tempX = cos * (p.x - rotateX) - sin * (p.y - rotateY) + rotateX;
			tempY = sin * (p.x - rotateX) + cos * (p.y - rotateY) + rotateY;
			p.set((float) tempX, (float) tempY);
		}
	}

	/**
	 * Note: coords array is assumed to be filled up (meaning that coords.length is both the capacity of the array and also the number of elements
	 * that have been stored in it)
	 */
	public static void rotate(float[] coords, float radians, float rotateX, float rotateY) {
		final float angle = Angle.normalizeRadians(radians);
		final double cos = Math.cos(angle);
		final double sin = Math.sin(angle);

		double tempX;
		double tempY;
		for (int i = 0; i < coords.length; i += 2) {
			tempX = cos * (coords[i] - rotateX) - sin * (coords[i + 1] - rotateY) + rotateX;
			tempY = sin * (coords[i] - rotateX) + cos * (coords[i + 1] - rotateY) + rotateY;
			coords[i] = (float) tempX;
			coords[i + 1] = (float) tempY;
		}
	}
}