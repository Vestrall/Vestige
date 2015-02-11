package com.lescomber.vestige.map;

import com.lescomber.vestige.geometry.Point;

public class HorizontalBoundaryLine implements BoundaryLine
{
	float x1;
	float x2;
	float y;
	
	public HorizontalBoundaryLine(float x1, float x2, float y)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.y = y;
	}
	
	public HorizontalBoundaryLine(HorizontalBoundaryLine copyMe)
	{
		x1 = copyMe.x1;
		x2 = copyMe.x2;
		y = copyMe.y;
	}
	
	@Override
	public Point getClosestToPoint(Point p)
	{
		final Point ret = new Point();
		ret.y = y;
		if (p.x <= x1)
			ret.x = x1;
		else if (p.x >= x2)
			ret.x = x2;
		else
			ret.x = p.x;
		return ret;
	}
	
	@Override
	public boolean contains(float x, float y)
	{
		return (x >= x1 && x <= x2 && this.y == y);
	}
	
	@Override
	public HorizontalBoundaryLine copy()
	{
		return new HorizontalBoundaryLine(this);
	}
}