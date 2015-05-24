package com.lescomber.vestige.map;

import com.lescomber.vestige.geometry.Point;

public interface BoundaryLine {
	Point getClosestToPoint(Point p);

	boolean contains(float x, float y);

	BoundaryLine copy();
}