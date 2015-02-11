package com.lescomber.vestige.statuseffects;

import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.screens.GameScreen;

public class DisplacementEffect
{
	private final int velocityPerSecond;
	private Point destination;
	private final double direction;
	private final int distance;
	private int faction;	// The faction that is considered "friendly" for this displacement
	
	public DisplacementEffect(float destX, float destY, int velocityPerSecond)
	{
		this.velocityPerSecond = velocityPerSecond;
		destination = new Point(destX, destY);
		direction = 0.0;
		distance = 0;
		faction = GameScreen.steves;
	}
	
	public DisplacementEffect(Point destination, int velocity)
	{
		this(destination.x, destination.y, velocity);
	}
	
	public DisplacementEffect(float direction, int distance, int velocity)
	{
		velocityPerSecond = velocity;
		destination = null;
		this.direction = Angle.normalizeRadians(direction);
		this.distance = distance;
		faction = GameScreen.steves;
	}
	
	public DisplacementEffect(DisplacementEffect copyMe)
	{
		velocityPerSecond = copyMe.velocityPerSecond;
		if (copyMe.destination == null)
			destination = null;
		else
			destination = new Point(copyMe.destination);
		direction = copyMe.direction;
		distance = copyMe.distance;
		faction = copyMe.faction;
	}
	
	public int getVelocityPerSecond() { return velocityPerSecond; }
	public Point getDestination(float x, float y)
	{
		if (destination != null)
			return destination;
		else
		{
			final float dx = (float)Math.cos(direction) * distance;
			final float dy = (float)Math.sin(direction) * distance;
			return new Point(x + dx, y + dy);
		}
	}
	
	public int getFaction() { return faction; }
	
	public void setFaction(int faction) { this.faction = faction; }
	
	public DisplacementEffect copy()
	{
		return new DisplacementEffect(this);
	}
}