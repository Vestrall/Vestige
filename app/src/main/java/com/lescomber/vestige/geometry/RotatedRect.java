package com.lescomber.vestige.geometry;

public class RotatedRect extends Shape
{
	private float halfWidth;
	private float halfHeight;
	private final Line[] sides;    // index 0 == top when direction = 0, then go clockwise

	public RotatedRect(float midX, float midY, float width, float height, float radians)
	{
		center = new Point(midX, midY);
		halfWidth = width / 2;
		halfHeight = height / 2;
		boundingBox = new Rectangle();

		sides = new Line[4];
		for (int i = 0; i < 4; i++)
			sides[i] = new Line();

		rotateTo(radians);
	}

	public RotatedRect(float midX, float midY, float width, float height)
	{
		this(midX, midY, width, height, 0);
	}

	public RotatedRect()
	{
		this(0, 0, 0, 0, 0);
	}

	public RotatedRect(Rectangle rect)
	{
		this(rect.getCenterX(), rect.getCenterY(), rect.getWidth(), rect.getHeight(), 0);
	}

	public RotatedRect(RotatedRect copyMe)
	{
		center = new Point(copyMe.center);
		halfWidth = copyMe.halfWidth;
		halfHeight = copyMe.halfHeight;
		boundingBox = new Rectangle(copyMe.boundingBox);
		sides = new Line[4];
		for (int i = 0; i < 4; i++)
			sides[i] = new Line(copyMe.sides[i]);
		direction = copyMe.direction;
	}

	@Override
	public boolean overlaps(Circle circle)
	{
		// Time saver for most cases
		if (!boundingBox.overlaps(circle.getBoundingBox()))
			return false;

		// Check if circle is entirely contained within this RotatedRect
		if (contains(circle.getCenterX(), circle.getCenterY()))
			return true;

		// Check if any side overlaps circle
		for (int i = 0; i < 4; i++)
		{
			if (circle.overlaps(sides[i]))
				return true;
		}

		// If none of the above have been hit, circle does not overlap
		return false;
	}

	@Override
	public boolean overlaps(Cone cone)
	{
		return cone.overlaps(this);
	}

	@Override
	public boolean overlaps(Rectangle rect)
	{
		// Time saver for most cases
		if (!boundingBox.overlaps(rect))
			return false;

		// Check if any side of this RotatedRect intersects rect
		for (int i = 0; i < 4; i++)
			if (rect.overlaps(sides[i]))
				return true;

		// If none of the above have been hit, rect does not overlap
		return false;
	}

	@Override
	public boolean overlaps(RotatedRect rect)
	{
		// Time saver for most cases
		if (!boundingBox.overlaps(rect.boundingBox))
			return false;

		// Check case where rect is entirely encompassed in this RotatedRect or vice versa
		if (contains(rect.center.x, rect.center.y) || rect.contains(center.x, center.y))
			return true;

		// Check if any sides of rect intersect sides of this RotatedRect
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				if (rect.sides[i].intersects(sides[j]))
					return true;
			}
		}

		// If none of the above have been hit, rect does not overlap
		return false;
	}

	@Override
	public boolean overlaps(Line line)
	{
		// Time saver for most cases
		if (!boundingBox.overlaps(line))
			return false;

		// Check line endpoints
		if (contains(line.point0.x, line.point0.y) || contains(line.point1.x, line.point1.y))
			return true;

		// Check if line intersects any sides of this RotatedRect
		//Note: 4th side does not need to be checked since an overlapping line will either intersect multiple sides
		//or one of its endpoints will be inside this RotatedRect (already checked above)
		for (int i = 0; i < 3; i++)
			if (sides[i].intersects(line))
				return true;

		// If none of the above have been hit, line must not overlap
		return false;
	}

	@Override
	public boolean contains(float x, float y)
	{
		// Time saver for most cases
		if (!boundingBox.contains(x, y))
			return false;

		// Handle "top"
		if (direction > Angle.WEST || direction == Angle.EAST)
		{
			if (sides[0].isPointRight(x, y) < 0)
				return false;
		}
		else if (sides[0].isPointRight(x, y) > 0)
			return false;

		// Handle "right"
		if (direction > Angle.SOUTH && direction <= Angle.NORTH)
		{
			if (sides[1].isPointRight(x, y) < 0)
				return false;
		}
		else if (sides[1].isPointRight(x, y) > 0)
			return false;

		// Handle "bottom"
		if (direction <= Angle.WEST && direction > Angle.EAST)
		{
			if (sides[2].isPointRight(x, y) < 0)
				return false;
		}
		else if (sides[2].isPointRight(x, y) > 0)
			return false;

		// Handle "left"
		if (direction > Angle.NORTH || direction <= Angle.SOUTH)
		{
			if (sides[3].isPointRight(x, y) < 0)
				return false;
		}
		else if (sides[3].isPointRight(x, y) > 0)
			return false;

		// If none of the above have been hit, (x,y) must be in this RotatedRect
		return true;
	}

	@Override
	public boolean contains(Point p)
	{
		return contains(p.x, p.y);
	}

	@Override
	public void offset(float dx, float dy)
	{
		center.offset(dx, dy);
		boundingBox.offset(dx, dy);
		for (int i = 0; i < 4; i++)
			sides[i].offset(dx, dy);
	}

	@Override
	public void offsetTo(float midX, float midY)
	{
		final float dx = midX - center.x;
		final float dy = midY - center.y;

		offset(dx, dy);
	}

	@Override
	public void rotate(float radians)
	{
		rotateTo(direction + radians);
	}

	@Override
	public void rotateTo(float radians)
	{
		direction = Angle.normalizeRadians(radians);
		buildSides();
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		Point.rotate(center, radians, rotateX, rotateY);
		direction = Angle.normalizeRadians(direction + radians);
		buildSides();
	}

	@Override
	public void scaleTo(float width, float height)
	{
		halfWidth = width / 2;
		halfHeight = height / 2;
		buildSides();
	}

	@Override
	public void scale(double wRatio, double hRatio)
	{
		halfWidth = (float) wRatio * halfWidth;
		halfHeight = (float) hRatio * halfHeight;
		buildSides();
	}

	public void setWidth(float width)
	{
		halfWidth = width / 2;
		buildSides();
	}

	public void setHeight(float height)
	{
		halfHeight = height / 2;
		buildSides();
	}

	private void buildSides()
	{
		final Point[] ps = new Point[4];
		ps[0] = new Point(center.x - halfWidth, center.y - halfHeight);
		ps[1] = new Point(center.x + halfWidth, center.y - halfHeight);
		ps[2] = new Point(center.x + halfWidth, center.y + halfHeight);
		ps[3] = new Point(center.x - halfWidth, center.y + halfHeight);

		Point.rotate(ps, direction, center.x, center.y);

		sides[0] = new Line(ps[0], ps[1]);
		sides[1] = new Line(ps[1], ps[2]);
		sides[2] = new Line(ps[2], ps[3]);
		sides[3] = new Line(ps[3], ps[0]);

		// Update boundingBox
		boundingBox.set(ps[0].x, ps[0].y, ps[0].x, ps[0].y);
		for (int i = 1; i < 4; i++)
		{
			boundingBox.left = Math.min(boundingBox.left, ps[i].x);
			boundingBox.right = Math.max(boundingBox.right, ps[i].x);
			boundingBox.top = Math.min(boundingBox.top, ps[i].y);
			boundingBox.bottom = Math.max(boundingBox.bottom, ps[i].y);
		}
	}

	public float getWidth()
	{
		return halfWidth * 2;
	}

	public float getHeight()
	{
		return halfHeight * 2;
	}

	public Line[] getSides()
	{
		return sides;
	}

	@Override
	public RotatedRect copy()
	{
		return new RotatedRect(this);
	}
}