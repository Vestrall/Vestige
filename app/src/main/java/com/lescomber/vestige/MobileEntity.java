package com.lescomber.vestige;

import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;

public class MobileEntity extends Entity
{
	// Current destination this MobileEntity is moving towards
	private Point destination;
	
	private float velocity;		// move speed in units travelled per ms
	
	// Change in x and y coords per ms of elapsed time for easy per frame calculations
	private double dxPerMs;
	private double dyPerMs;
	
	public MobileEntity()
	{
		super();
		
		destination = null;
		velocity = 300;	// Default move speed
		
		dxPerMs = 0.0;
		dyPerMs = 0.0;
	}
	
	public MobileEntity(MobileEntity copyMe)
	{
		super(copyMe);
		
		velocity = copyMe.velocity;
		if (copyMe.destination != null)
		{
			destination = new Point(copyMe.destination);
			updateXYVelocities();
		}
		else
		{
			destination = null;
			dxPerMs = 0.0;
			dyPerMs = 0.0;
		}
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		if (destination != null)
		{
			float dx = (float)(dxPerMs * deltaTime);
			float dy = (float)(dyPerMs * deltaTime);
			
			if (Math.abs(dx) >= Math.abs(destination.x - getX()))
				dx = destination.x - getX();
			if (Math.abs(dy) >= Math.abs(destination.y - getY()))
				dy = destination.y - getY();
			
			move(dx, dy);
		}
	}
	
	// Move towards destination
	protected void move(float dx, float dy)
	{
		super.offset(dx, dy);	// super.offset() is used instead of offset() in order to avoid re-calculating XY velocities
		
		if (isDestinationReached())
		{
			destination = null;
			destinationReached();
		}
	}
	
	public void setDestination(Point p)
	{
		if (p == null)
			destination = null;
		else
			setDestination(p.x, p.y);
	}
	
	public void setDestination(float destX, float destY)
	{
		destination = new Point(destX, destY);
		
		// Prevent NaN results caused by 0/0 in the Math.atan method
		if (isDestinationReached())
		{
			destination = null;
			destinationReached();
			return;
		}
		
		updateXYVelocities();
	}
	
	// Determine x and y distances traveled per ms. Use this info to update frame destination each frame (in order to avoid
	//trig calculations every frame)
	private void updateXYVelocities()
	{
		if (destination == null)
		{
			dxPerMs = 0.0;
			dyPerMs = 0.0;
			return;
		}
		
		final double dx = destination.x - getX();
		final double dy = destination.y - getY();
		
		final double theta = Math.atan(dy / dx);
		dxPerMs = Math.cos(theta) * velocity;
		dyPerMs = Math.sin(theta) * velocity;
		
		if (dx < 0)
		{
			dxPerMs *= -1.0;
			dyPerMs *= -1.0;
		}
	}
	
	@Override
	public void offset(float dx, float dy)
	{
		super.offset(dx, dy);
		
		if (isDestinationReached())
		{
			destination = null;
			destinationReached();
		}
		else
			updateXYVelocities();	// Recalculate x/y velocities in case we have moved in some fashion outside of normal move()
	}
	
	private boolean isDestinationReached()
	{
		return (destination != null && Util.equals(destination.x, getX()) && Util.equals(destination.y, getY()));
	}
	
	protected void destinationReached() { }		// Triggered when current destination has been reached
	
	// Set velocity in terms of units traveled per second
	public void setVelocityPerSecond(float velocity)
	{
		this.velocity = velocity / 1000;
		updateXYVelocities();
	}
	
	// Set velocity in terms of units traveled per ms
	public void setVelocity(float velocity)
	{
		this.velocity = velocity;
		updateXYVelocities();
	}
	
	public Point getDestination() { return destination; }
	public int getVelocityPerSecond() { return (int)(velocity * 1000); }
	public float getVelocity() { return velocity; }
}