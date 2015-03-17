package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

import java.util.ArrayList;

public class MultiShooter extends AIShooter
{
	public MultiShooter(AIUnit owner, Projectile prototype, double cooldownSeconds)
	{
		super(owner, prototype, cooldownSeconds);
	}

	public MultiShooter(MultiShooter copyMe)
	{
		super(copyMe);
	}

	@Override
	protected void fire(Line path)
	{
		final ArrayList<Line> paths = new ArrayList<Line>(4);

		if (OptionsScreen.difficulty == OptionsScreen.HARD)
		{
			Line line = new Line(path);
			Point.rotate(line.point1, -(float) (Math.PI / 7), line.point0.x, line.point0.y);
			paths.add(line);

			line = new Line(path);
			Point.rotate(line.point1, -(float) (Math.PI / 21), line.point0.x, line.point0.y);
			paths.add(line);

			line = new Line(path);
			Point.rotate(line.point1, (float) (Math.PI / 21), line.point0.x, line.point0.y);
			paths.add(line);

			line = new Line(path);
			Point.rotate(line.point1, (float) (Math.PI / 7), line.point0.x, line.point0.y);
			paths.add(line);
		}
		else if (OptionsScreen.difficulty == OptionsScreen.MEDIUM)
		{
			Line line = new Line(path);
			Point.rotate(line.point1, -(float) (Math.PI / 12), line.point0.x, line.point0.y);
			paths.add(line);

			paths.add(new Line(path));

			line = new Line(path);
			Point.rotate(line.point1, (float) (Math.PI / 12), line.point0.x, line.point0.y);
			paths.add(line);
		}
		else    // difficulty = EASY
		{
			Line line = new Line(path);
			Point.rotate(line.point1, -(float) (Math.PI / 12), line.point0.x, line.point0.y);
			paths.add(line);

			line = new Line(path);
			Point.rotate(line.point1, (float) (Math.PI / 12), line.point0.x, line.point0.y);
			paths.add(line);
		}

		for (final Line l : paths)
			super.fire(l);
	}

	@Override
	public MultiShooter copy()
	{
		return new MultiShooter(this);
	}
}