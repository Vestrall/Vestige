package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.projectiles.sone.RoamingExploder;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class RoamingExploderShooter extends AIAbility
{
	private static final double[] SPAWN_COOLDOWN = new double[] { 8.5, 5.3, 3.5 };
	
	public RoamingExploderShooter(AIUnit owner)
	{
		super(owner, SPAWN_COOLDOWN[OptionsScreen.difficulty]);
	}
	
	public RoamingExploderShooter(RoamingExploderShooter copyMe)
	{
		super(copyMe);
	}
	
	@Override
	public void activate()
	{
		owner.queueProjectile(new RoamingExploder(owner, owner.getX(), owner.getY()));
	}
	
	@Override
	public RoamingExploderShooter copy()
	{
		return new RoamingExploderShooter(this);
	}
}