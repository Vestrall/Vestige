package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.projectiles.sone.CreepingFire;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

public class CreepingFireShooter extends AIAbility
{
	private static final int COUNTDOWN_INIT = 800;
	private static final float ANGLE_RANGE = (float)(Math.PI / 1.5);
	private static final float DPS[] = new float[] { 12, 17, 22 };
	private static final double FLAME_DURATION = 7;
	
	private HitGroup fireGroup;
	
	public CreepingFireShooter(AIUnit owner, double cooldownSeconds, HitGroup hitGroup)
	{
		super(owner, cooldownSeconds);
		
		setUsesAnimation(false);
		
		fireGroup = hitGroup;
	}
	
	public CreepingFireShooter(CreepingFireShooter copyMe)
	{
		super(copyMe);
		
		fireGroup = copyMe.fireGroup;
	}
	
	@Override
	public void activate()
	{
		final Unit target = Unit.getNearestMember(owner.getCenter(), owner.getOpponents());
		final Line line = new Line(owner.getCenter(), target.getCenter());
		final float firstFlameAngle = line.getDirection() - (ANGLE_RANGE / 2) + (Util.rand.nextFloat() * ANGLE_RANGE);
		
		final CreepingFire firstFire = new CreepingFire(DPS[OptionsScreen.difficulty], FLAME_DURATION, COUNTDOWN_INIT,
				firstFlameAngle, fireGroup);
		firstFire.offsetTo(owner.getCenter());
		owner.queueAreaEffect(firstFire);
	}
	
	@Override
	public CreepingFireShooter copy()
	{
		return new CreepingFireShooter(this);
	}
	
	public void setFireGroup(HitGroup fireGroup)
	{
		this.fireGroup = fireGroup;
	}
}