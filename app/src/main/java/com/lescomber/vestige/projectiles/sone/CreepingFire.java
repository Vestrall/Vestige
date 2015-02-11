package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.AreaEffect;
import com.lescomber.vestige.projectiles.FireAnimation;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.units.Unit;

public class CreepingFire extends AreaEffect
{
	private static final float FLAME_WIDTH = 40;
	private static final float FLAME_HEIGHT = 50;
	
	private final double flameDuration;
	
	private static final float DIST_PER_FLAME = 30;
	private float flameAngle;
	
	private static final float RADIANS_PER_MS = (float)((Math.PI / 4) / 1000);
	private float storedRadians;	// Amount of rotation the next flame can use
	
	private final int nextCountdown;
	private int countdown;			// Countdown until next flame
	
	private boolean hasCrept;
	
	public CreepingFire(float dps, double durationSeconds, int countdown, float lastFlameAngle, HitGroup fireGroup)
	{
		super(FLAME_WIDTH, FLAME_HEIGHT, 0, durationSeconds);
		
		flameDuration = durationSeconds;
		
		setDamagePerSecond(dps);
		setImage(new FireAnimation());
		setImageOffsetY(FireAnimation.IMAGE_OFFSET_Y);
		setHitGroup(fireGroup);
		setTickFrequency(50);
		
		storedRadians = 0;
		
		this.flameAngle = lastFlameAngle;
		this.countdown = countdown;
		nextCountdown = Math.round(countdown * 0.9f);
		
		hasCrept = false;
	}
	
	public CreepingFire(CreepingFire copyMe)
	{
		super(copyMe);
		
		flameDuration = copyMe.flameDuration;
		storedRadians = copyMe.storedRadians;
		flameAngle = copyMe.flameAngle;
		countdown = copyMe.countdown;
		nextCountdown = copyMe.nextCountdown;
		hasCrept = copyMe.hasCrept;
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		if (hasCrept)
			return;
		
		storedRadians += RADIANS_PER_MS * deltaTime;
		
		countdown -= deltaTime;
		if (countdown <= 0)
		{
			hasCrept = true;
			
			// Assumes only one target faction
			final Unit target = Unit.getNearestMember(getCenter(), getTargets().get(0));
			final Line line = new Line(getCenter(), target.getCenter());
			final float targetAngle = line.getDirection();
			
			if (Angle.isInRange(targetAngle, flameAngle + storedRadians, flameAngle + (float)Math.PI))
				flameAngle += storedRadians;
			else if (Angle.isInRange(targetAngle, flameAngle - (float)Math.PI, flameAngle - storedRadians))
				flameAngle -= storedRadians;
			else
				flameAngle = targetAngle;
			
			final Point nextFlamePoint = new Point(getX() + DIST_PER_FLAME, getY());
			Point.rotate(nextFlamePoint, flameAngle, getX(), getY());
			
			if (Screen.contains(nextFlamePoint))
			{
				final CreepingFire nextFire = new CreepingFire(getDPS(), flameDuration, nextCountdown, flameAngle, getHitGroup());
				nextFire.offsetTo(nextFlamePoint);
				queueAreaEffect(nextFire);
			}
		}
	}
	
	@Override
	public CreepingFire copy()
	{
		return new CreepingFire(this);
	}
}