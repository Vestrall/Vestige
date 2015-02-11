package com.lescomber.vestige.geometry;

public class Circle extends Shape
{
	private float radius;
	private float radiusSquared;	// Convenience variable to store radius*radius for most distance calculations
	
	public Circle(float centerX, float centerY, float radius)
	{
		center = new Point(centerX, centerY);
		this.radius = radius;
		radiusSquared = radius * radius;
		boundingBox = new Rectangle(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
		direction = 0.0f;
	}
	
	public Circle(Circle copyMe)
	{
		center = new Point(copyMe.center);
		radius = copyMe.radius;
		radiusSquared = copyMe.radiusSquared;
		boundingBox = new Rectangle(copyMe.boundingBox);
		direction = copyMe.direction;
	}
	
	@Override
	public boolean overlaps(Circle circle)
	{
		// Time saver for most cases
		if (!boundingBox.overlaps(circle.boundingBox))
			return false;
		
		// Compare distance between centers of circles and their radii
		return center.distanceToPointSquared(circle.center) <= (radius + circle.radius) * (radius + circle.radius);
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
		
		return rect.distanceToPointSquared(center) <= radiusSquared;
	}
	
	@Override
	public boolean overlaps(RotatedRect rect)
	{
		return rect.overlaps(this);
	}
	
	@Override
	public boolean overlaps(Line line)
	{
		return (line.distanceToPointSquared(center) <= radiusSquared);
	}
	
	@Override
	public boolean contains(float x, float y)
	{
		// Time saver for most cases
		if (!boundingBox.contains(x, y))
			return false;

		// Compare distance to (x,y) with radius
		return (center.distanceToPointSquared(new Point(x, y)) <= radiusSquared);
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
	}
	
	@Override
	public void offsetTo(float x, float y)
	{
		final float dx = x - center.x;
		final float dy = y - center.y;
		offset(dx, dy);
	}
	
	// Has no effect on this circle, but keeps track of direction in case images or other things want to use it
	@Override
	public void rotate(float radians)
	{
		direction = Angle.normalizeRadians(direction + radians);
	}
	
	@Override
	public void rotateTo(float radians)
	{
		direction = Angle.normalizeRadians(radians);
	}
	
	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		Point.rotate(center, radians, rotateX, rotateY);
		boundingBox.offsetTo(center.x, center.y);
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
	
	public void setRadius(float radius)
	{
		this.radius = radius;
		radiusSquared = radius * radius;
		boundingBox.set(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
	}
	
	public float getRadius() { return radius; }
	
	@Override
	public Circle copy()
	{
		return new Circle(this);
	}
}