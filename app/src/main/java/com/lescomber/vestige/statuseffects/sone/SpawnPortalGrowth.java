package com.lescomber.vestige.statuseffects.sone;

import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.Unit;

public class SpawnPortalGrowth extends StatusEffect
{
	public SpawnPortalGrowth(StatPack stats)
	{
		super(stats, 1000);
	}
	
	public SpawnPortalGrowth(SpawnPortalGrowth copyMe)
	{
		super(copyMe);
	}
	
	@Override
	public void attach(Unit unit)
	{
		unit.scale(1.05, 1.05);
	}
	
	@Override
	public void reattach(Unit unit)
	{
		unit.scale(1.05, 1.05);
	}
	
	@Override
	public SpawnPortalGrowth copy()
	{
		return new SpawnPortalGrowth(this);
	}
}