package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.projectiles.Beam;
import com.lescomber.vestige.units.Unit;

public class MovingBeam extends Beam
{
	private final float damagePerMs;
	private int ms;		// elapsed time of most recent frame
	
	private boolean unitHitThisFrame;
	
	public MovingBeam(float x, float y, float direction, float dps)
	{
		super(x, y, direction, 0);
		
		getHitBundle().setAbsorbSound(false);
		
		damagePerMs = dps / 1000;
		unitHitThisFrame = false;
	}
	
	public MovingBeam(MovingBeam copyMe)
	{
		super(copyMe);
		
		damagePerMs = copyMe.damagePerMs;
		unitHitThisFrame = copyMe.unitHitThisFrame;
	}
	
	@Override
	public void update(int deltaTime)
	{
		unitHitThisFrame = false;
		ms = deltaTime;
		
		super.update(deltaTime);
		
		if (unitHitThisFrame)
			AudioManager.movingBeamLoop.play();
		else
			AudioManager.movingBeamLoop.stop();
	}
	
	@Override	// Overridden to allow damage per MS
	public void unitHit(Unit unit)
	{
		if (unit.getShields() <= 0)
			unitHitThisFrame = true;
		setDamage(damagePerMs * ms);
		unit.hit(hitBundle);
	}
	
	@Override
	public MovingBeam copy()
	{
		return new MovingBeam(this);
	}
}