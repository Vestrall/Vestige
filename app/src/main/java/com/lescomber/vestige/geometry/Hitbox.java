package com.lescomber.vestige.geometry;

import com.lescomber.vestige.framework.Screen;

import java.util.ArrayList;
import java.util.List;

public class Hitbox {
	private float x;
	private float y;
	private float width;
	private float height;
	private float direction;

	public ArrayList<Shape> shapes;

	public Hitbox() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		direction = 0;
		shapes = new ArrayList<Shape>(3);
	}

	public Hitbox(Shape shape) {
		shapes = new ArrayList<Shape>(3);
		addShape(shape);
	}

	public Hitbox(Hitbox copyMe) {
		x = copyMe.x;
		y = copyMe.y;
		width = copyMe.width;
		height = copyMe.height;
		direction = copyMe.direction;
		shapes = new ArrayList<Shape>(3);
		for (final Shape s : copyMe.shapes)
			shapes.add(s.copy());
	}

	public void addShape(Shape shape) {
		if (shapes.isEmpty())    // If we have no shapes, init fields based on shape
		{
			x = shape.getCenterX();
			y = shape.getCenterY();
			direction = shape.getDirection();
		}

		shapes.add(shape);

		// To get direction from shape, we rotate shape until it is axis aligned and then use its getRight(), getLeft() etc methods to calculate
		//width/height
		final float curDirection = direction;
		rotate(-curDirection);
		width = getRight() - getLeft();
		height = getBottom() - getTop();
		rotate(curDirection);
	}

	public boolean overlaps(Hitbox other) {
		for (final Shape sOne : shapes) {
			for (final Shape sTwo : other.shapes) {
				if (sOne.overlaps(sTwo))
					return true;
			}
		}

		return false;
	}

	public boolean overlaps(Shape shape) {
		for (final Shape s : shapes)
			if (s.overlaps(shape))
				return true;

		return false;
	}

	public boolean contains(float x, float y) {
		for (final Shape s : shapes)
			if (s.contains(x, y))
				return true;

		return false;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public void offset(float dx, float dy) {
		x += dx;
		y += dy;
		for (final Shape s : shapes)
			s.offset(dx, dy);
	}

	public void offsetTo(float x, float y) {
		final float dx = x - this.x;
		final float dy = y - this.y;

		offset(dx, dy);
	}

	public void rotate(float radians) {
		if (shapes.size() == 1) {
			direction = Angle.normalizeRadians(direction + radians);
			shapes.get(0).rotate(radians);
		} else
			rotateAbout(radians, getX(), getY());
	}

	public void rotateTo(float radians) {
		rotate(radians - direction);
	}

	public void rotateAbout(float radians, float rotateX, float rotateY) {
		direction = Angle.normalizeRadians(direction + radians);

		for (final Shape s : shapes)
			s.rotateAbout(radians, rotateX, rotateY);

		final Point temp = new Point(x, y);
		Point.rotate(temp, radians, rotateX, rotateY);
		x = temp.x;
		y = temp.y;
	}

	/**
	 * Scale hitbox based on ratio of width and height to current hitbox width and height
	 */
	public void scaleTo(float width, float height) {
		final double widthRatio = (double) width / this.width;
		final double heightRatio = (double) height / this.height;
		scale(widthRatio, heightRatio);
	}

	public void scaleWidthTo(float width) {
		final double widthRatio = (double) width / this.width;
		scale(widthRatio, 1);
	}

	public void scaleHeightTo(float height) {
		final double heightRatio = (double) height / this.height;
		scale(1, heightRatio);
	}

	public void scale(double wRatio, double hRatio) {
		for (final Shape s : shapes) {
			// Scale each shape in its current location
			s.scale(wRatio, hRatio);

			// Adjust location for each shape (ie. scale its distance from center of hitbox)
			final Point shapeCenter = s.getCenter();
			float offsetX = shapeCenter.x - getX();
			float offsetY = shapeCenter.y - getY();
			offsetX = (float) wRatio * offsetX;
			offsetY = (float) hRatio * offsetY;
			s.offsetTo(getX() + offsetX, getY() + offsetY);
		}

		width *= wRatio;
		height *= hRatio;
	}

	public void clear() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		direction = 0;
		shapes = new ArrayList<Shape>(3);
	}

	public boolean isCompletelyOffScreen() {
		return (getLeft() > Screen.WIDTH || getTop() > Screen.HEIGHT || getRight() < 0 || getBottom() < 0);
	}

	public boolean isCompletelyOnScreen() {
		return (getLeft() >= 0 && getTop() >= 0 && getRight() <= Screen.WIDTH && getBottom() <= Screen.HEIGHT);
	}

	public float getLeft() {
		if (shapes.isEmpty())
			return x;

		float left = Float.MAX_VALUE;
		for (final Shape s : shapes)
			left = Math.min(left, s.getLeft());
		return left;
	}

	public float getTop() {
		if (shapes.isEmpty())
			return y;

		float top = Float.MAX_VALUE;
		for (final Shape s : shapes)
			top = Math.min(top, s.getTop());
		return top;
	}

	public float getRight() {
		if (shapes.isEmpty())
			return x;

		float right = Float.MIN_VALUE;
		for (final Shape s : shapes)
			right = Math.max(right, s.getRight());
		return right;
	}

	public float getBottom() {
		if (shapes.isEmpty())
			return y;

		float bottom = Float.MIN_VALUE;
		for (final Shape s : shapes)
			bottom = Math.max(bottom, s.getBottom());
		return bottom;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public List<Shape> getShapes() {
		return shapes;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public Point getCenter() {
		return new Point(x, y);
	}

	public float getDirection() {
		return direction;
	}
}