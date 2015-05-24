package com.lescomber.vestige.map;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;

import java.util.List;

public interface Obstacle {
	List<Point> getCorners();

	Point adjustPathPoint(Point point);

	boolean intersectsPath(Line line);

	boolean containsPathPoint(Point p);

	boolean contains(Point p);

	void buildBoundaries(List<Obstacle> obstacles);

	List<BoundaryLine> getBoundaryLines();

	void removeBoundaryLine(BoundaryLine boundaryLine);

	void becomeVisible();

	Obstacle copy();

	boolean overlaps(Entity other);
}