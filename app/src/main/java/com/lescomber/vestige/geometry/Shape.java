package com.lescomber.vestige.geometry;

public abstract class Shape {
	Point center;
	Rectangle boundingBox;
	float direction;

	public boolean overlaps(Shape shape) {
		if (shape instanceof Circle)
			return overlaps((Circle) shape);
		else if (shape instanceof Cone)
			return overlaps((Cone) shape);
		else if (shape instanceof Rectangle)
			return overlaps((Rectangle) shape);
		else    // shape instanceof RotatedRect
			return overlaps((RotatedRect) shape);
	}

	public abstract boolean overlaps(Circle circle);

	public abstract boolean overlaps(Cone cone);

	public abstract boolean overlaps(Rectangle rect);

	public abstract boolean overlaps(RotatedRect rect);

	public abstract boolean overlaps(Line line);

	public abstract boolean contains(float x, float y);

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public abstract void offset(float dx, float dy);

	public abstract void offsetTo(float x, float y);

	public void offsetTo(Point p) {
		offsetTo(p.x, p.y);
	}

	public abstract void rotate(float radians);

	public abstract void rotateTo(float radians);

	public abstract void rotateAbout(float radians, float rotateX, float rotateY);

	public void rotateAbout(float radians, Point p) {
		rotateAbout(radians, p.x, p.y);
	}

	public abstract void scaleTo(float width, float height);

	public abstract void scale(double wRatio, double hRatio);

	public Point getCenter() {
		return center;
	}

	public float getCenterX() {
		return center.x;
	}

	public float getCenterY() {
		return center.y;
	}

	public float getLeft() {
		return boundingBox.left;
	}

	public float getTop() {
		return boundingBox.top;
	}

	public float getRight() {
		return boundingBox.right;
	}

	public float getBottom() {
		return boundingBox.bottom;
	}

	public float getDirection() {
		return direction;
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public abstract Shape copy();
}