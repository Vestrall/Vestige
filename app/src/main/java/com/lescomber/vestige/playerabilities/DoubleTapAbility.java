package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.units.Player;

public abstract class DoubleTapAbility extends PlayerAbility
{
	public DoubleTapAbility(Player player)
	{
		super(player);
	}
	
	public DoubleTapAbility(DoubleTapAbility copyMe)
	{
		super(copyMe);
	}
	
	public abstract void fire(Point p);
	@Override public abstract DoubleTapAbility copy();
}