package com.lescomber.vestige.units;

import java.util.ArrayList;
import java.util.List;

import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.map.Map;
import com.lescomber.vestige.screens.GameScreen;

public abstract class AIRailUnit extends AIUnit
{
	private final ArrayList<Point> locations;
	private final boolean randomLocations;
	private int index;		// for use when randomLocations == false
	
	public AIRailUnit(float hitboxWidth, float hitboxHeight, float imageOffsetY, float topGap)
	{
		super(hitboxWidth, hitboxHeight, imageOffsetY, topGap);
		
		locations = new ArrayList<Point>();
		randomLocations = true;
		index = 0;
	}
	
	public AIRailUnit(AIRailUnit copyMe)
	{
		super(copyMe);
		
		locations = new ArrayList<Point>();
		for (final Point p : copyMe.locations)
			locations.add(new Point(p));
		randomLocations = copyMe.randomLocations;
		index = copyMe.index;
	}
	
	@Override
	protected void pathDestinationReached()
	{
		chooseDestination();
		
		super.pathDestinationReached();
	}
	
	@Override
	protected void finishedFiring()
	{
		chooseDestination();
		
		super.finishedFiring();
	}
	
	public void chooseDestination()
	{
		if (locations.size() <= 0)
			return;
		
		if (randomLocations)
		{
			final int i = Util.rand.nextInt(locations.size());
			setPath(GameScreen.map.getPath(getCenter(), locations.get(i)));
		}
		else
		{
			setPath(GameScreen.map.getPath(getCenter(), locations.get(index)));
			index++;
			if (index >= locations.size())
				index = 0;
		}
	}
	
	// Creates a set of locationCount random rail locations from within the given hitbox
	public void createRailLocations(Hitbox locationBox, int locationCount, Map level)
	{
		final float minX = locationBox.getLeft();
		final float minY = locationBox.getTop();
		final float maxX = minX + locationBox.getWidth();
		final float maxY = minY + locationBox.getHeight();
		
		final int attemptCreateLimit = locationCount * 25;
		int createCount = 0;
		for (int i=0; i<attemptCreateLimit; i++)
		{
			final float newX = Util.rand.nextFloat() * (maxX - minX) + minX;
			final float newY = Util.rand.nextFloat() * (maxY - minY) + minY;
			final Point newPoint = new Point(newX, newY);
			if (locationBox.contains(newPoint) && (level == null || !level.overlapsObstacle(newPoint)))
			{
				locations.add(newPoint);
				createCount++;
				if (createCount == locationCount)
					break;
			}
		}
	}
	
	public void createRailLocations(Hitbox locationBox, int locationCount)
	{
		createRailLocations(locationBox, locationCount, null);
	}
	
	// Creates a set of rail locations with default parameter values
	public void createRailLocations()
	{
		createRailLocations(new Hitbox(new Rectangle(0, getTopGap(), Screen.WIDTH, Screen.HEIGHT)), 30, null);
	}
	
	public void createRailLocations(Map level)
	{
		createRailLocations(new Hitbox(new Rectangle(0, getTopGap(), Screen.WIDTH, Screen.HEIGHT)), 30, level);
	}
	
	public void createRailLocations(int locationCount)
	{
		createRailLocations(new Hitbox(new Rectangle(0, getTopGap(), Screen.WIDTH, Screen.HEIGHT)), locationCount, null);
	}
	
	public void createRailLocations(int locationCount, Map level)
	{
		createRailLocations(new Hitbox(new Rectangle(0, getTopGap(), Screen.WIDTH, Screen.HEIGHT)), locationCount, level);
	}
	
	public void createRailLocations(Rectangle locationRect, int locationCount)
	{
		createRailLocations(new Hitbox(locationRect), locationCount);
	}
	
	public void addLocation(Point location)
	{
		if (location != null)
			locations.add(new Point(location));
	}
	public void addLocations(List<Point> locations)
	{
		for (final Point p : locations)
			this.locations.add(new Point(p));
	}
	
	@Override public abstract AIRailUnit copy();
}