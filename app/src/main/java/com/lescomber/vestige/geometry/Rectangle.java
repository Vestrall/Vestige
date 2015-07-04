package com.lescomber.vestige.geometry;

public class Rectangle extends Shape {
	public float left;
	public float top;
	public float right;
	public float bottom;

	// Line overlap fields
	private static final int INSIDE = 0;    // 0000
	private static final int LEFT = 1;        // 0001
	private static final int RIGHT = 2;     // 0010
	private static final int BOTTOM = 4;    // 0100
	private static final int TOP = 8;       // 1000

	public Rectangle(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;

		direction = 0;
		center = new Point();
	}

	public Rectangle(Rectangle copyMe) {
		left = copyMe.left;
		top = copyMe.top;
		right = copyMe.right;
		bottom = copyMe.bottom;
		direction = 0;
		center = new Point();
	}

	public Rectangle() {
		left = 0;
		top = 0;
		right = 0;
		bottom = 0;

		direction = 0;
		center = new Point();
	}

	@Override
	public boolean overlaps(Circle circle) {
		return circle.overlaps(this);
	}

	@Override
	public boolean overlaps(Cone cone) {
		return cone.overlaps(this);
	}

	@Override
	public boolean overlaps(Rectangle rect) {
		return ((left <= rect.right) && (right >= rect.left) && (bottom >= rect.top) && (top <= rect.bottom));
	}

	@Override
	public boolean overlaps(RotatedRect rect) {
		return rect.overlaps(this);
	}

	@Override
	public boolean overlaps(Line line) {
		final int code1 = computeOutCode(line.point0.x, line.point0.y);
		final int code2 = computeOutCode(line.point1.x, line.point1.y);

		if (code1 == 0 || code2 == 0)    // Check if either line endpoint is inside this Rectangle
			return true;
		else if ((code1 & code2) != 0)    // Check if line is entirely outside this Rectangle on the same side
			return false;                //(eg. both line endpoints are above this Rectangle, or both to the left, etc)
		else {
			// Check if infinite line passes between diagonal points of this Rectangle. If so, line segment must as well, otherwise, line would have
			//hit one of the previous bitwise tests
			int isRight0 = line.isPointRight(left, top);
			int isRight1 = line.isPointRight(right, bottom);

			if (isRight0 == 0 || isRight1 == 0)
				return true;
			else if (isRight0 != isRight1)
				return true;

			isRight0 = line.isPointRight(right, top);
			isRight1 = line.isPointRight(left, bottom);

			if (isRight0 == 0 || isRight1 == 0)
				return true;
			else if (isRight0 != isRight1)
				return true;

			// If none of the above have been hit, line does not overlap
			return false;
		}
	}

	@Override
	public boolean contains(float x, float y) {
		return x >= left && x <= right && y >= top && y <= bottom;
	}

	@Override
	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	@Override
	public void offset(float dx, float dy) {
		left += dx;
		right += dx;
		top += dy;
		bottom += dy;
	}

	@Override
	public void offsetTo(float x, float y) {
		updateCenter();
		final float dx = x - center.x;
		final float dy = y - center.y;

		offset(dx, dy);
	}

	/**
	 * Cannot be rotated. Use RotatedRect if rotations are desired
	 */
	@Override
	public void rotate(float radians) {
	}

	@Override
	public void rotateTo(float radians) {
	}

	/**
	 * Moves the rectangle but does not rotate it. Use RotatedRect if rotations are desired
	 */
	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY) {
		updateCenter();
		Point.rotate(center, radians, rotateX, rotateY);
		offsetTo(center.x, center.y);
	}

	@Override
	public void scaleTo(float width, float height) {
		final float halfDw = (width - getWidth()) / 2;
		final float halfDh = (height - getHeight()) / 2;
		left -= halfDw;
		top -= halfDh;
		right += halfDw;
		bottom += halfDh;
	}

	@Override
	public void scale(double wRatio, double hRatio) {
		final double newWidth = wRatio * getWidth();
		final double newHeight = hRatio * getHeight();
		scaleTo((float) newWidth, (float) newHeight);
	}

	public double distanceToPointSquared(Point p) {
		double dist2 = 0.0;
		if (p.x < left)
			dist2 += (left - p.x) * (left - p.x);
		else if (p.x > right)
			dist2 += (p.x - right) * (p.x - right);
		if (p.y < top)
			dist2 += (top - p.y) * (top - p.y);
		else if (p.y > bottom)
			dist2 += (p.y - bottom) * (p.y - bottom);

		return dist2;
	}

	public void set(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public void set(Rectangle other) {
		left = other.left;
		top = other.top;
		right = other.right;
		bottom = other.bottom;
	}

	public boolean isEmpty() {
		return left >= right || top >= bottom;
	}

	public float getWidth() {
		return right - left;
	}

	public float getHeight() {
		return bottom - top;
	}

	public Line[] getSides() {
		final Line[] sides = new Line[4];
		sides[0] = new Line(left, top, right, top);
		sides[1] = new Line(right, top, right, bottom);
		sides[2] = new Line(right, bottom, left, bottom);
		sides[3] = new Line(left, bottom, left, top);

		return sides;
	}

	private void updateCenter() {
		center.set((left + right) / 2, (top + bottom) / 2);
	}

	@Override
	public Point getCenter() {
		updateCenter();
		return center;
	}

	@Override
	public float getCenterX() {
		updateCenter();
		return center.x;
	}

	@Override
	public float getCenterY() {
		updateCenter();
		return center.y;
	}

	@Override
	public float getLeft() {
		return left;
	}

	@Override
	public float getTop() {
		return top;
	}

	@Override
	public float getRight() {
		return right;
	}

	@Override
	public float getBottom() {
		return bottom;
	}

	@Override
	public Rectangle getBoundingBox() {
		return this;
	}

	@Override
	public Rectangle copy() {
		return new Rectangle(this);
	}

	private int computeOutCode(float x, float y) {
		int code = INSIDE;

		if (x < left)
			code |= LEFT;
		else if (x > right)
			code |= RIGHT;
		if (y < top)
			code |= TOP;
		else if (y > bottom)
			code |= BOTTOM;

		return code;
	}
}