package com.lescomber.vestige.map;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;

import java.util.List;

public interface Obstacle
{
	public List<Point> getCorners();
	
	public Point adjustPathPoint(Point point);
	public boolean intersectsPath(Line line);
	public boolean containsPathPoint(Point p);
	public boolean contains(Point p);
	
	public void buildBoundaries(List<Obstacle> obstacles);
	public List<BoundaryLine> getBoundaryLines();
	public void removeBoundaryLine(BoundaryLine boundaryLine);
	
	public void becomeVisible();
	
	public Obstacle copy();
	
	public boolean overlaps(Entity other);
}