package com.lescomber.vestige.map;

import com.lescomber.vestige.geometry.Point;

public interface BoundaryLine
{
	public Point getClosestToPoint(Point p);
	
	public boolean contains(float x, float y);
	
	public BoundaryLine copy();
}