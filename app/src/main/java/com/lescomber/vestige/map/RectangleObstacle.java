package com.lescomber.vestige.map;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;

public abstract class RectangleObstacle extends Entity implements Obstacle {
	private final Rectangle wallRect;    // Copy of Hitbox Rectangle for convenience

	private final ArrayList<BoundaryLine> boundaryLines;

	// Player pathing variables
	private final Rectangle pathRect;

	public RectangleObstacle(float left, float top, float right, float bottom) {
		super();

		offsetTo(left + (right - left) / 2, top + (bottom - top) / 2);
		createRectangleHitbox(right - left, bottom - top);

		wallRect = new Rectangle(left, top, right, bottom);

		boundaryLines = new ArrayList<BoundaryLine>();

		pathRect = new Rectangle(wallRect.left - Map.PLAYER_HALF_WIDTH, wallRect.top - Map.PLAYER_HALF_HEIGHT, wallRect.right + Map
				.PLAYER_HALF_WIDTH, wallRect.bottom + Map.PLAYER_HALF_HEIGHT);
	}

	public RectangleObstacle(RectangleObstacle copyMe) {
		super(copyMe);

		wallRect = new Rectangle(copyMe.wallRect);
		boundaryLines = new ArrayList<BoundaryLine>();
		for (final BoundaryLine bl : copyMe.boundaryLines)
			boundaryLines.add(bl.copy());
		pathRect = new Rectangle(copyMe.pathRect);
	}

	@Override
	public List<Point> getCorners() {
		final boolean pathableCorners[] = new boolean[4];
		for (int i = 0; i < 4; i++)
			pathableCorners[i] = false;
		final ArrayList<Point> corners = new ArrayList<Point>(4);

		for (final BoundaryLine bl : boundaryLines) {
			if (bl.contains(pathRect.left - 1, pathRect.top - 1))
				pathableCorners[0] = true;
			if (bl.contains(pathRect.right + 1, pathRect.top - 1))
				pathableCorners[1] = true;
			if (bl.contains(pathRect.right + 1, pathRect.bottom + 1))
				pathableCorners[2] = true;
			if (bl.contains(pathRect.left - 1, pathRect.bottom + 1))
				pathableCorners[3] = true;
		}

		if (pathableCorners[0])
			corners.add(new Point(pathRect.left - 1, pathRect.top - 1));
		if (pathableCorners[1])
			corners.add(new Point(pathRect.right + 1, pathRect.top - 1));
		if (pathableCorners[2])
			corners.add(new Point(pathRect.right + 1, pathRect.bottom + 1));
		if (pathableCorners[3])
			corners.add(new Point(pathRect.left - 1, pathRect.bottom + 1));

		return corners;
	}

	@Override
	public Point adjustPathPoint(Point point) {
		Point ret = new Point();
		double d2 = Double.MAX_VALUE;
		for (final BoundaryLine bl : boundaryLines) {
			final Point blPoint = bl.getClosestToPoint(point);
			if (point.distanceToPointSquared(blPoint) < d2) {
				ret = blPoint;
				d2 = point.distanceToPointSquared(blPoint);
			}
		}
		if (d2 == Double.MAX_VALUE)
			return null;
		else
			return ret;
	}

	@Override
	public boolean intersectsPath(Line line) {
		return pathRect.overlaps(line);
	}

	@Override
	public boolean containsPathPoint(Point p) {
		if (p == null)
			return false;
		else
			return pathRect.contains(p.x, p.y);
	}

	@Override
	public boolean contains(Point p) {
		return wallRect.contains(p);
	}

	@Override
	public List<BoundaryLine> getBoundaryLines() {
		return boundaryLines;
	}

	@Override
	public void removeBoundaryLine(BoundaryLine boundaryLine) {
		boundaryLines.remove(boundaryLine);
	}

	@Override
	public void buildBoundaries(List<Obstacle> obstacles) {
		buildHorizontalBoundaryLine(obstacles, new Line(pathRect.left - 1, pathRect.top - 1, pathRect.right + 1, pathRect.top - 1));
		buildHorizontalBoundaryLine(obstacles, new Line(pathRect.left - 1, pathRect.bottom + 1, pathRect.right + 1, pathRect.bottom + 1));
		buildVerticalBoundaryLine(obstacles, new Line(pathRect.left - 1, pathRect.top - 1, pathRect.left - 1, pathRect.bottom + 1));
		buildVerticalBoundaryLine(obstacles, new Line(pathRect.right + 1, pathRect.top - 1, pathRect.right + 1, pathRect.bottom + 1));
	}

	private void buildHorizontalBoundaryLine(List<Obstacle> obstacles, Line testLine) {
		// Check if testLine is completely outside the Map's playable area
		if (testLine.point0.y < Map.PLAYER_HALF_HEIGHT || testLine.point0.y > Screen.HEIGHT - Map.PLAYER_HALF_HEIGHT ||
				testLine.point0.x > Screen.WIDTH - Map.PLAYER_HALF_WIDTH || testLine.point1.x < Map.PLAYER_HALF_WIDTH)
			return;

		// Adjust horizontal endpoints to fit within Map's playable area (if needed)
		testLine.point0.x = Math.max(testLine.point0.x, Map.PLAYER_HALF_WIDTH);
		testLine.point1.x = Math.min(testLine.point1.x, Screen.WIDTH - Map.PLAYER_HALF_WIDTH);

		// Find left endpoint for Boundary Line
		final int size = obstacles.size();
		for (int i = 0; i < size; i++) {
			if (obstacles.get(i) instanceof RectangleObstacle) {
				final RectangleObstacle ro = (RectangleObstacle) obstacles.get(i);
				if (ro.containsPathPoint(testLine.point0)) {
					testLine.point0.x = ro.pathRect.right + 1;
					i = 0;
				}
			}
		}
		// Find right endpoint for Boundary Line
		for (int i = 0; i < size; i++) {
			if (obstacles.get(i) instanceof RectangleObstacle) {
				final RectangleObstacle ro = (RectangleObstacle) obstacles.get(i);
				if (testLine.point0.x >= testLine.point1.x)
					return;
				else if (ro.intersectsPath(testLine)) {
					testLine.point1.x = ro.pathRect.left - 1;
					i = 0;
				}
			}
		}

		// Check if we have a valid Boundary Line
		if (testLine.point0.x < testLine.point1.x) {
			// Add our new Boundary Line to boundaryLines
			boundaryLines.add(new HorizontalBoundaryLine(testLine.point0.x, testLine.point1.x, testLine.point1.y));

			// If we haven't reached the rightmost edge of the this obstacle (or Map's playable area), recursively
			//start a search for the next boundary line (if any)
			if (testLine.point1.x < Math.min(pathRect.right, Screen.WIDTH - Map.PLAYER_HALF_WIDTH)) {
				testLine.point0.x = testLine.point1.x + 1;
				testLine.point1.x = pathRect.right + 1;
				buildHorizontalBoundaryLine(obstacles, testLine);
			}
		}
	}

	private void buildVerticalBoundaryLine(List<Obstacle> obstacles, Line testLine) {
		// Check if testLine is completely outside the Map's playable area
		if (testLine.point0.x < Map.PLAYER_HALF_WIDTH || testLine.point0.x > Screen.WIDTH - Map.PLAYER_HALF_WIDTH ||
				testLine.point0.y > Screen.HEIGHT - Map.PLAYER_HALF_HEIGHT || testLine.point1.y < Map.PLAYER_HALF_HEIGHT)
			return;

		// Adjust vertical endpoints to fit within Map's playable area (if needed)
		testLine.point0.y = Math.max(testLine.point0.y, Map.PLAYER_HALF_HEIGHT);
		testLine.point1.y = Math.min(testLine.point1.y, Screen.HEIGHT - Map.PLAYER_HALF_HEIGHT);

		// Find top endpoint for Boundary Line
		final int size = obstacles.size();
		for (int i = 0; i < size; i++) {
			if (obstacles.get(i) instanceof RectangleObstacle) {
				final RectangleObstacle ro = (RectangleObstacle) obstacles.get(i);
				if (ro.containsPathPoint(testLine.point0)) {
					testLine.point0.y = ro.pathRect.bottom + 1;
					i = 0;
				}
			}
		}
		// Find bottom endpoint for Boundary Line
		for (int i = 0; i < size; i++) {
			if (obstacles.get(i) instanceof RectangleObstacle) {
				final RectangleObstacle ro = (RectangleObstacle) obstacles.get(i);
				if (testLine.point0.y >= testLine.point1.y)
					return;
				else if (ro.intersectsPath(testLine)) {
					testLine.point1.y = ro.pathRect.top - 1;
					i = 0;
				}
			}
		}

		// Check if we have a valid Boundary Line
		if (testLine.point0.y < testLine.point1.y) {
			// Add our new Boundary Line to boundaryLines
			boundaryLines.add(new VerticalBoundaryLine(testLine.point0.x, testLine.point0.y, testLine.point1.y));

			// If we haven't reached the bottommost edge of the this obstacle (or Map's playable area), recursively
			//start a search for the next boundary line (if any)
			if (testLine.point1.y < Math.min(pathRect.bottom, Screen.HEIGHT - Map.PLAYER_HALF_HEIGHT)) {
				testLine.point0.y = testLine.point1.y + 1;
				testLine.point1.y = pathRect.bottom + 1;
				buildVerticalBoundaryLine(obstacles, testLine);
			}
		}
	}
}