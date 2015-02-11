package com.lescomber.vestige.geometry;

public class Cone extends Shape
{
	private float radius;
	private float halfWidth;			// Half the width of the cone (in radians)
	private final Line line0;			// One side of the cone (smaller angle)
	private final Line line1;			// Other side of the cone (larger angle)
	private final Circle bigCircle;		// Circle encompassing cone
	private boolean boundingBoxDirty;	// Does the bounding box need updating before use?
	
	public Cone(float centerX, float centerY, float radius, float direction, float width)
	{
		center = new Point(centerX, centerY);
		this.radius = radius;
		this.direction = Angle.normalizeRadians(direction);
		halfWidth = width / 2;
		bigCircle = new Circle(centerX, centerY, radius);
		line0 = new Line();
		line1 = new Line();
		boundingBoxDirty = true;
		buildSides();
	}
	
	public Cone(Cone copyMe)
	{
		center = new Point(copyMe.center);
		radius = copyMe.radius;
		direction = copyMe.direction;
		halfWidth = copyMe.halfWidth;
		line0 = new Line(copyMe.line0);
		line1 = new Line(copyMe.line1);
		bigCircle = new Circle(copyMe.bigCircle);
		boundingBoxDirty = copyMe.boundingBoxDirty;
		if (!boundingBoxDirty)
			boundingBox = new Rectangle(copyMe.boundingBox);
	}

	@Override
	public boolean overlaps(Circle circle)
	{
		// Check if circle encompassing cone intersects with circle
		if (!bigCircle.overlaps(circle))
			return false;
		
		// Check if sides of cone overlap circle
		if (circle.overlaps(line0) || circle.overlaps(line1))
			return true;
		
		// Check the angle of the line between center of cone and center of circle. If this angle falls within the cone's width,
		//then the cone and circle intersect because we already know bigCircle overlaps with circle
		final Line centers = new Line(center, circle.getCenter());
		if (Angle.isInRange(centers.getDirection(), direction - halfWidth, direction + halfWidth))
			return true;
		else
			return false;
	}
	
	@Override
	public boolean overlaps(Cone cone)
	{
		// Time saver for most cases
		if (!bigCircle.overlaps(cone.bigCircle))
			return false;
		
		// Check if either cone is entirely encompassed in the other
		if (contains(cone.center.x, cone.center.y) || cone.contains(center.x, center.y))
			return true;
		
		// Check if any sides of the cones intersect with each other. Avoids redundancy of double checking each possible line intersection
		//compared to simply using overlaps(Line) method for all four lines
		if (overlaps(cone.line0) || overlaps(cone.line1))
			return true;
		Point p = line0.getClosestToPoint(cone.center);
		if (cone.contains(p.x, p.y))
			return true;
		p = line1.getClosestToPoint(cone.center);
		if (cone.contains(p.x, p.y))
			return true;
		
		// Check for case where cones are facing each other and only their arcs intersect
		final Line centers = new Line(center, cone.center);
		final float connectAngle = centers.getDirection();
		if (Angle.isInRange(connectAngle, direction - halfWidth, direction + halfWidth) &&
			Angle.isInRange(connectAngle + (float)Math.PI, cone.direction - cone.halfWidth, cone.direction + cone.halfWidth))
			return true;
		
		// If none of the above have been hit, cone does not overlap
		return false;
	}
	
	@Override
	public boolean overlaps(Rectangle rect)
	{
		// Time saver for most cases
		if (!bigCircle.getBoundingBox().overlaps(rect))
			return false;
		
		// Check if cone is entirely contained within rect
		if (rect.contains(center.x, center.y))
			return true;
		
		// Check if any of rect's sides overlap with this Cone
		final Line[] rectSides = rect.getSides();
		for (int i=0; i<4; i++)
		{
			if (overlaps(rectSides[i]))
				return true;
		}
		
		// If none of the above have been hit, rect does not overlap
		return false;
	}

	@Override
	public boolean overlaps(RotatedRect rect)
	{
		// Time saver for most cases
		if (!bigCircle.getBoundingBox().overlaps(rect.getBoundingBox()))
			return false;
		
		// Check if this Cone is entirely contained within rect
		if (rect.contains(center.x, center.y))
			return true;
		
		// Check if any side of rect overlaps this Cone
		final Line[] rectSides = rect.getSides();
		for (int i=0; i<4; i++)
		{
			if (overlaps(rectSides[i]))
				return true;
		}
		
		// If none of the above have been hit, rect does not overlap
		return false;
	}
	
	@Override
	public boolean overlaps(Line line)
	{
		// Check sides of cone against line
		if (line0.intersects(line) || line1.intersects(line))
			return true;
		
		// Check if closest point of line is within cone.
		final Point p = line.getClosestToPoint(center);
		if (contains(p.x, p.y))
			return true;
		
		// If neither above condition is true, line does not overlap
		return false;
	}

	@Override
	public boolean contains(float x, float y)
	{
		// Time saver for most cases
		if (!bigCircle.contains(x, y))
			return false;
		
		// Calculate angle between point (x,y) and center. Check if this angle is between (direction - halfWidth) and (direction + halfWidth)
		final Line xy = new Line(center.x, center.y, x, y);
		if (Angle.isInRange(xy.getDirection(), direction - halfWidth, direction + halfWidth))
			return true;
		else
			return false;
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
		line0.offset(dx, dy);
		line1.offset(dx, dy);
		bigCircle.offset(dx, dy);
		boundingBoxDirty = true;
	}

	@Override
	public void offsetTo(float x, float y)
	{
		final float dx = x - center.x;
		final float dy = y - center.y;
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
		boundingBoxDirty = true;
	}
	
	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		Point.rotate(center, radians, rotateX, rotateY);
		bigCircle.offsetTo(center.x, center.y);
		rotate(radians);
	}
	
	@Override
	public void scaleTo(float width, float height)
	{
		// Scale to whichever parameter is lower
		setRadius(Math.min(width, height));
	}
	
	@Override
	public void scale(double wRatio, double hRatio)
	{
		// Scale to whichever parameter is lower
		final double smallerRatio = Math.min(wRatio, hRatio);
		setRadius(radius * (float)smallerRatio);
	}
	
	private void buildSides()
	{
		// Set line start points to center of cone
		line0.point0.x = center.x;
		line0.point0.y = center.y;
		line1.point0.x = center.x;
		line1.point0.y = center.y;
		
		// Set line end points
		line0.point1.x = center.x + (float)Math.cos(direction - (halfWidth)) * radius;
		line0.point1.y = center.y + (float)Math.sin(direction - (halfWidth)) * radius;
		line1.point1.x = center.x + (float)Math.cos(direction + (halfWidth)) * radius;
		line1.point1.y = center.y + (float)Math.sin(direction + (halfWidth)) * radius;
	}
	
	public void setWidth(float width)
	{
		halfWidth = Angle.normalizeRadians(width) / 2;
		buildSides();
		boundingBoxDirty = true;
	}
	
	public void setRadius(float radius)
	{
		this.radius = radius;
		bigCircle.setRadius(radius);
		buildSides();
		boundingBoxDirty = true;
	}
	
	public Line[] getSides() { return new Line[] {line0, line1}; }
	
	@Override public float getLeft() { return getBoundingBox().left; }
	@Override public float getTop() { return getBoundingBox().top; }
	@Override public float getRight() { return getBoundingBox().right; }
	@Override public float getBottom() { return getBoundingBox().bottom; }
	
	@Override	// Cone only updates the position/size of boundingBox when it is requested via getBoundingBox()
	public Rectangle getBoundingBox()
	{
		if (!boundingBoxDirty)
			return boundingBox;
		
		// Determine which quadrants our sides are in. 0 = bottom right, 1 = bottom left, 2 = top left, 3 = top right
		int line0Quad;
		int line1Quad;
		final double angle0 = Angle.normalizeRadians(direction - halfWidth);
		final double angle1 = Angle.normalizeRadians(direction + halfWidth);
		
		if (angle0 < Angle.SOUTH)
			line0Quad = 0;
		else if (angle0 < Angle.WEST)
			line0Quad = 1;
		else if (angle0 < Angle.NORTH)
			line0Quad = 2;
		else
			line0Quad = 3;
		
		if (angle1 < Angle.SOUTH)
			line1Quad = 0;
		else if (angle1 < Angle.WEST)
			line1Quad = 1;
		else if (angle1 < Angle.NORTH)
			line1Quad = 2;
		else
			line1Quad = 3;
		
		// Include center point and side endpoints in boundingBox rectangle
		boundingBox = new Rectangle(center.x, center.y, center.x, center.y);
		boundingBox.left = Math.min(boundingBox.left, line0.point1.x);
		boundingBox.top = Math.min(boundingBox.top, line0.point1.y);
		boundingBox.right = Math.max(boundingBox.right, line0.point1.x);
		boundingBox.bottom = Math.max(boundingBox.bottom, line0.point1.y);
		boundingBox.left = Math.min(boundingBox.left, line1.point1.x);
		boundingBox.top = Math.min(boundingBox.top, line1.point1.y);
		boundingBox.right = Math.max(boundingBox.right, line1.point1.x);
		boundingBox.bottom = Math.max(boundingBox.bottom, line1.point1.y);
		
		// Include the arc of the cone in boundingBox rectangle
		if (angle1 > angle0)
		{
			if (line0Quad == line1Quad)
				return boundingBox;
			if (line0Quad == 0)
				boundingBox.bottom = bigCircle.getBottom();
			if (line0Quad <= 1 && line1Quad > 1)
				boundingBox.left = bigCircle.getLeft();
			if (line0Quad <= 2 && line1Quad == 3)
				boundingBox.top = bigCircle.getTop();
		}
		else
		{
			boundingBox.right = bigCircle.getRight();
			if (line0Quad == 0 || line1Quad > 0)
				boundingBox.bottom = bigCircle.getBottom();
			if (line0Quad <= 1 || line1Quad > 1)
				boundingBox.left = bigCircle.getLeft();
			if (line0Quad <= 2 || line1Quad == 3)
				boundingBox.top = bigCircle.getTop();
		}
		
		boundingBoxDirty = false;
		
		return boundingBox;
	}
	
	@Override
	public Cone copy()
	{
		return new Cone(this);
	}
}