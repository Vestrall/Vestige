package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.units.AIUnit;

public class DummyAbility extends AIAbility
{
	public DummyAbility(AIUnit owner, double cooldownSeconds)
	{
		super(owner, cooldownSeconds);
	}

	public DummyAbility(DummyAbility copyMe)
	{
		super(copyMe);
	}

	@Override
	public void activate()
	{
		// Does nothing, hence the dummy part of the name
	}

	@Override
	public DummyAbility copy()
	{
		return new DummyAbility(this);
	}
}