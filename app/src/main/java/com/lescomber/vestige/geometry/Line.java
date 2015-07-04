package com.lescomber.vestige.geometry;

import com.lescomber.vestige.framework.Screen;

public class Line {
	public Point point0;
	public Point point1;

	public Line(Point p0, Point p1) {
		point0 = new Point(p0);
		point1 = new Point(p1);
	}

	public Line(float x0, float y0, float x1, float y1) {
		point0 = new Point(x0, y0);
		point1 = new Point(x1, y1);
	}

	public Line(Line copyMe) {
		point0 = new Point(copyMe.point0);
		point1 = new Point(copyMe.point1);
	}

	public Line() {
		point0 = new Point();
		point1 = new Point();
	}

	public Point intersectionPoint(Line other) {
		final float rx = point1.x - point0.x;
		final float ry = point1.y - point0.y;
		final float sx = other.point1.x - other.point0.x;
		final float sy = other.point1.y - other.point0.y;
		final float qmpx = other.point0.x - point0.x;
		final float qmpy = other.point0.y - point0.y;
		final float rcs = (rx * sy - ry * sx);

		if (rcs == 0) {
			if (qmpx * ry - qmpy * rx == 0) {
				if (other.isInBigRect(point0.x, point0.y))
					return new Point(point0);

				if (point0.distanceToPointSquared(other.point0) < point0.distanceToPointSquared(other.point1)) {
					if (isInBigRect(other.point0.x, other.point0.y))
						return new Point(other.point0);
					else
						return null;
				} else {
					if (isInBigRect(other.point1.x, other.point1.y))
						return new Point(other.point1);
					else
						return null;
				}
			} else
				return null;
		}

		final float t = (qmpx * sy - qmpy * sx) / rcs;
		final float u = (qmpx * ry - qmpy * rx) / rcs;

		if (t >= 0 && t <= 1 && u >= 0 && u <= 1)
			return new Point((point0.x + t * rx), (point0.y + t * ry));
		else
			return null;
	}

	public boolean intersects(Line other) {
		final int isRight1 = other.isPointRight(point0.x, point0.y);
		final int isRight2 = other.isPointRight(point1.x, point1.y);

		if ((isRight1 > 0 && isRight2 > 0) || (isRight1 < 0 && isRight2 < 0))
			return false;

		final int oIsRight1 = isPointRight(other.point0.x, other.point0.y);
		final int oIsRight2 = isPointRight(other.point1.x, other.point1.y);

		if ((oIsRight1 > 0 && oIsRight2 > 0) || (oIsRight1 < 0 && oIsRight2 < 0))
			return false;

		// Handle case where one of the isRight vars is equal to 0 (ie. can be equal to 0 without being on the line segment)
		if (isRight1 == 0 || isRight2 == 0 || oIsRight1 == 0 || oIsRight2 == 0) {
			return (isRight1 == 0 && other.isInBigRect(point0.x, point0.y)) || (isRight2 == 0 && other.isInBigRect(point1.x, point1.y))
					|| (oIsRight1 == 0 && isInBigRect(other.point0.x, other.point0.y))
					|| (oIsRight2 == 0 && isInBigRect(other.point1.x, other.point1.y));
		}

		return true;
	}

	/**
	 * Returns true if (x,y) is inside encompassing Rectangle
	 */
	public boolean isInBigRect(float x, float y) {
		return ((x >= point0.x || x >= point1.x) && (x <= point0.x || x <= point1.x) &&
				(y >= point0.y || y >= point1.y) && (y <= point0.y || y <= point1.y));
	}

	public double distanceToPoint(Point p) {
		return Math.sqrt(distanceToPointSquared(p));
	}

	/**
	 * Distance calculation that avoids square root like the plague
	 */
	public double distanceToPointSquared(Point p) {
		final double lineLengthSquared = getLengthSquared();

		if (lineLengthSquared == 0.0)
			return point0.distanceToPointSquared(p);

		final double t = (((p.x - point0.x) * (point1.x - point0.x)) + ((p.y - point0.y) * (point1.y - point0.y))) / lineLengthSquared;

		if (t < 0.0)        // Beyond the p0 end of the line segment
			return point0.distanceToPointSquared(p);
		else if (t > 1.0)    // Beyond the p1 end of the line segment
			return point1.distanceToPointSquared(p);
		else                // Projection falls on line segment
		{
			final double projx = point0.x + t * (point1.x - point0.x);
			final double projy = point0.y + t * (point1.y - point0.y);

			final double dx = p.x - projx;
			final double dy = p.y - projy;

			return (dx * dx + dy * dy);
		}
	}

	public Point getClosestToPoint(Point p) {
		final double lineLengthSquared = getLengthSquared();

		if (lineLengthSquared == 0.0)
			return point0;

		final double t = (((p.x - point0.x) * (point1.x - point0.x)) + ((p.y - point0.y) * (point1.y - point0.y))) / lineLengthSquared;

		if (t < 0.0)
			return point0;
		else if (t > 1.0)
			return point1;
		else {
			final double projx = point0.x + t * (point1.x - point0.x);
			final double projy = point0.y + t * (point1.y - point0.y);

			return new Point((float) projx, (float) projy);
		}
	}

	/**
	 * Returns the angle of this line (from point0 to point1) in radians
	 */
	public float getDirection() {
		float angle;
		if (point0.x == point1.x) {
			if (point1.y > point0.y)
				angle = Angle.SOUTH;
			else
				angle = Angle.NORTH;
		} else {
			angle = (float) Math.atan((double) (point1.y - point0.y) / (point1.x - point0.x));
			if (point0.x > point1.x)
				angle += Math.PI;
		}

		return Angle.normalizeRadians(angle);
	}

	public Point getStart() {
		return point0;
	}

	public Point getEnd() {
		return point1;
	}

	/**
	 * Extends the line off screen and returns the resulting endpoint
	 */
	public Point getExtEnd() {
		return getExtEnd(Screen.SCREEN_HITBOX_PADDING, Screen.SCREEN_HITBOX_PADDING);
	}

	public Point getExtEnd(float xBuffer, float yBuffer) {
		if (point0.equals(point1))
			return new Point(point1);

		final float dx = point1.x - point0.x;
		final float dy = point1.y - point0.y;
		final Point ret = new Point(point1);

		while (ret.x > -xBuffer && ret.y > -yBuffer && ret.x < Screen.WIDTH + xBuffer && ret.y < Screen.HEIGHT + yBuffer) {
			ret.x += dx * Screen.WIDTH;
			ret.y += dy * Screen.WIDTH;
		}

		return ret;
	}

	public Point getCenter() {
		return new Point((point0.x + point1.x) / 2, (point0.y + point1.y) / 2);
	}

	public double getLength() {
		return Math.sqrt(getLengthSquared());
	}

	public double getLengthSquared() {
		return (((point1.x - point0.x) * (point1.x - point0.x)) + ((point1.y - point0.y) * (point1.y - point0.y)));
	}

	/**
	 * Returns a 1 if point (x,y) is right of this line, 0 if it is on this line, and -1 if it is to the left. If this line is horizontal, returns 1
	 * if point (x,y) is below the line, 0 if it is on the line, and -1 above the line.
	 */
	public int isPointRight(float x, float y) {
		final Point first;
		final Point second;

		if (point0.y == point1.y) {
			if (point0.x < point1.x) {
				first = point0;
				second = point1;
			} else {
				first = point1;
				second = point0;
			}
		} else {
			if (point0.y > point1.y) {
				first = point0;
				second = point1;
			} else {
				first = point1;
				second = point0;
			}
		}

		final float dpFirstHalf = (second.x - first.x) * (y - first.y);
		final float dpSecondHalf = (second.y - first.y) * (x - first.x);
		if (dpFirstHalf > dpSecondHalf)
			return 1;
		else if (dpSecondHalf > dpFirstHalf)
			return -1;
		else
			return 0;
	}

	public void offset(float dx, float dy) {
		point0.offset(dx, dy);
		point1.offset(dx, dy);
	}

	public void offsetTo(float x, float y) {
		final Point center = getCenter();
		final float dx = x - center.x;
		final float dy = y - center.y;
		offset(dx, dy);
	}

	public void offsetTo(Point p) {
		offsetTo(p.x, p.y);
	}
}