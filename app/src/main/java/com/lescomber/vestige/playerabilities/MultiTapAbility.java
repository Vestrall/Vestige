package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.units.Player;

public abstract class MultiTapAbility extends PlayerAbility
{
	public MultiTapAbility(Player player)
	{
		super(player);
	}
	
	public MultiTapAbility(MultiTapAbility copyMe)
	{
		super(copyMe);
	}
	
	public abstract void activate();
	@Override public abstract MultiTapAbility copy();
}