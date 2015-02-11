package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.projectiles.WildFire;

public class Meteor extends Projectile
{
	private static final float X_DIST = 250;
	
	public Meteor(float width, float height, float destX, float destY, float dps, double durationSeconds)
	{
		super(SpriteManager.purpleComet, 0, 17);
		
		setImageDrop(0.05);
		
		setGlow(SpriteManager.purpleGlow);
		setUnitPassThrough(true);
		setWallPassThrough(true);
		setOffScreenRemoval(false);
		
		setVelocityPerSecond(150);
		
		offset(destX + X_DIST, destY);
		setDestination(destX, destY);
		setImageOffsetY(-X_DIST * 2);	// Meteor angle of approach is 30 degrees so it makes a 60/30 triangle, therefore y = x * 2
		rotateTo((float)(2 * Math.PI) / 3);		// 120 degrees = straight down + 30 degrees
		
		final WildFire fireEffect = new WildFire(width, height, dps, durationSeconds);
		setAreaEffect(fireEffect);
	}
	
	public Meteor(float width, float height, Point dest, float dps, double durationSeconds)
	{
		this(width, height, dest.x, dest.y, dps, durationSeconds);
	}
	
	public Meteor(Meteor copyMe)
	{
		super(copyMe);
	}
	
	@Override
	public Meteor copy()
	{
		return new Meteor(this);
	}
}