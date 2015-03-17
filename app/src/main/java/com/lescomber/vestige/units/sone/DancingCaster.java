package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Caster;

import java.util.ArrayList;

public class DancingCaster extends Caster
{
	private static final float MOONWALK_HALF_WIDTH = 20;

	private static final int MOONWALK_INTERVAL_MS = 3500;
	private int moonwalkCountdown;
	private final int walkingLeftIndex;
	private final int walkingRightIndex;
	private final float baseMoveSpeed;

	private boolean isAngry;        // Dances until isAngry == true
	private final DanceCoordinator danceCoordinator;
	private boolean isActuallyEntering;        // Replaces usual isEntering boolean because isEntering will be used during dancing

	public DancingCaster(DanceCoordinator danceCoordinator)
	{
		super();

		createRailLocations(50);

		// Set up moonwalking
		moonwalkCountdown = 0;
		walkingLeftIndex = getWalkingLeftIndex();
		walkingRightIndex = getWalkingRightIndex();
		baseMoveSpeed = getMoveSpeed();

		isAngry = false;
		isActuallyEntering = true;
		this.danceCoordinator = danceCoordinator;
	}

	public DancingCaster(DancingCaster copyMe)
	{
		super(copyMe);

		moonwalkCountdown = copyMe.moonwalkCountdown;
		walkingLeftIndex = copyMe.walkingLeftIndex;
		walkingRightIndex = copyMe.walkingRightIndex;
		baseMoveSpeed = copyMe.baseMoveSpeed;
		isAngry = copyMe.isAngry;
		danceCoordinator = copyMe.danceCoordinator;
		isActuallyEntering = copyMe.isActuallyEntering;
	}

	public void dancePosition(Point dest)
	{
		setEntering(false);
		setDestination(dest);
		setEntering(true);
	}

	public void dance()
	{
		moonwalkCountdown = 500;
		setWalkLeftAnimation(walkingRightIndex);
		setWalkRightAnimation(walkingLeftIndex);
		setBaseMoveSpeed(40);
	}

	public void moonwalk(int times)
	{
		final Point left = new Point(getX() - MOONWALK_HALF_WIDTH, getY());
		final Point right = new Point(getX() + MOONWALK_HALF_WIDTH, getY());
		final ArrayList<Point> path = new ArrayList<Point>(20);
		for (int i = 0; i < times; i += 2)
		{
			path.add(right);
			if (i + 1 < times)
				path.add(left);
		}

		path.add(new Point(getX(), getY()));

		setEntering(false);
		setPath(path);
		setEntering(true);
	}

	public boolean isMoonwalking()
	{
		return getWalkingLeftIndex() == walkingRightIndex;
	}

	public void aggro()
	{
		isAngry = true;
		setEntering(isActuallyEntering);

		// Undo moonwalking
		setWalkLeftAnimation(walkingLeftIndex);
		setWalkRightAnimation(walkingRightIndex);
		setBaseMoveSpeed(baseMoveSpeed);

		chooseDestination();
	}

	@Override
	public void idleSprite()
	{
		if (!isAngry)
			faceLeft();

		super.idleSprite();
	}

	@Override
	public void hasEntered()
	{
		super.hasEntered();

		setEntering(!isAngry);
		isActuallyEntering = false;
	}

	@Override
	public void hit(HitBundle bundle)
	{
		super.hit(bundle);

		if (!isAngry)
			danceCoordinator.aggro();
	}

	@Override
	public void chooseDestination()
	{
		if (isAngry)
			super.chooseDestination();
	}

	@Override
	public void setDestination(float destX, float destY)
	{
		if (!isAngry)
		{
			setEntering(false);
			super.setDestination(destX, destY);
			setEntering(true);
		}
		else
			super.setDestination(destX, destY);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		if (!isAngry)
		{
			danceCoordinator.update(deltaTime);

			if (isMoonwalking())
			{
				moonwalkCountdown -= deltaTime;
				if (moonwalkCountdown <= 0)
				{
					moonwalkCountdown += MOONWALK_INTERVAL_MS;

					moonwalk(2);
				}
			}
		}
	}

	@Override
	public DancingCaster copy()
	{
		return new DancingCaster(this);
	}
}