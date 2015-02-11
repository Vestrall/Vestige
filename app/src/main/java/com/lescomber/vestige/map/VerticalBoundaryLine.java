package com.lescomber.vestige.map;

import com.lescomber.vestige.geometry.Point;

public class VerticalBoundaryLine implements BoundaryLine
{
	float x;
	float y1;
	float y2;
	
	public VerticalBoundaryLine(float x, float y1, float y2)
	{
		this.x = x;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public VerticalBoundaryLine(VerticalBoundaryLine copyMe)
	{
		x = copyMe.x;
		y1 = copyMe.y1;
		y2 = copyMe.y2;
	}
	
	@Override
	public Point getClosestToPoint(Point p)
	{
		final Point ret = new Point();
		ret.x = x;
		if (p.y <= y1)
			ret.y = y1;
		else if (p.y >= y2)
			ret.y = y2;
		else
			ret.y = p.y;
		return ret;
	}
	
	@Override
	public boolean contains(float x, float y)
	{
		return (this.x == x && y >= y1 && y <= y2);
	}
	
	@Override
	public VerticalBoundaryLine copy()
	{
		return new VerticalBoundaryLine(this);
	}
}
