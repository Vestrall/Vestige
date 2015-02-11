package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.projectiles.sone.CreepingFire;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class MultiCreepingFireShooter extends AIAbility
{
	private static final int COUNTDOWN_INIT = 300;
	private static final float[] DIRECTION_GAP = new float[] { (float)Math.PI, (float)Math.PI * 2 / 3, (float)Math.PI / 2 };
	private static final float DPS[] = new float[] { 12, 17, 22 };
	private static final double FLAME_DURATION = 6.5;
	
	private final HitGroup fireGroup;
	
	public MultiCreepingFireShooter(AIUnit owner, double cooldownSeconds)
	{
		super(owner, cooldownSeconds);
		
		fireGroup = new HitGroup();
	}
	
	public MultiCreepingFireShooter(MultiCreepingFireShooter copyMe)
	{
		super(copyMe);
		
		// Not copied
		fireGroup = new HitGroup();
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		fireGroup.update(deltaTime);
	}
	
	@Override
	public void activate()
	{
		float direction = Util.rand.nextFloat() * (float)Math.PI * 2;
		for (int i=0; i<OptionsScreen.difficulty + 2; i++)
		{
			final CreepingFire newFire = new CreepingFire(DPS[OptionsScreen.difficulty], FLAME_DURATION, COUNTDOWN_INIT,
					direction, fireGroup);
			newFire.offsetTo(owner.getCenter());
			owner.queueAreaEffect(newFire);
			direction += DIRECTION_GAP[OptionsScreen.difficulty];
		}
	}
	
	@Override
	public MultiCreepingFireShooter copy()
	{
		return new MultiCreepingFireShooter(this);
	}
}