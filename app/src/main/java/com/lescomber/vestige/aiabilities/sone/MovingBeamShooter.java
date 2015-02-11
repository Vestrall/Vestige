package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIChanneledAbility;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.sone.MovingBeam;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

public class MovingBeamShooter extends AIChanneledAbility
{
	private static final float DPS[] = new float[] { 15, 20, 25 };
	private static double CHANNEL_DURATION = 4;
	private static final int DELAY_MAX[] = new int[] { 1600, 1300, 1000 };
	private static final float RADIANS_PER_MS[] = new float[] { 0.00044f, 0.00048f, 0.00052f };
	
	private MovingBeam beam;	// Latest beam to have been fired. Used to grow/rotate beam
	private Unit target;		// Current target to aim for
	private Point beamStart;	// Starting point for the beam (a.k.a. firingLocation)
	private int delay;			// Delay before beam starts rotating
	
	public MovingBeamShooter(AIUnit owner, double cooldownSeconds)
	{
		super(owner, CHANNEL_DURATION, cooldownSeconds);
		
		beam = null;
		target = null;
		beamStart = null;
		delay = 0;
	}
	
	public MovingBeamShooter(MovingBeamShooter copyMe)
	{
		super(copyMe);
		
		beam = copyMe.beam;
		target = copyMe.target;
		beamStart = copyMe.beamStart;
		delay = copyMe.delay;
	}
	
	@Override
	public void activate()
	{
		super.activate();
		
		// Init delay
		delay = DELAY_MAX[OptionsScreen.difficulty];
		
		// Init original beam destination
		final Line line = new Line(beamStart, target.getCenter());
		beam = new MovingBeam(beamStart.x, beamStart.y, line.getDirection(), DPS[OptionsScreen.difficulty]);
		
		// Fire
		owner.queueProjectile(beam);
	}
	
	@Override
	protected void channeling(int deltaTime)
	{
		if (delay > 0)
			delay -= deltaTime;
		else
		{
			// Rotate beam to track target
			final float maxRotation = RADIANS_PER_MS[OptionsScreen.difficulty] * deltaTime;
			final Line line = new Line(beamStart, target.getCenter());
			final float targetAngle = Angle.normalizeRadians(line.getDirection());
			final float curAngle = Angle.normalizeRadians(beam.getDirection());
			
			if (Angle.isInRange(targetAngle, curAngle + maxRotation, curAngle + (float)Math.PI))
				beam.rotateAbout(maxRotation, beamStart);
			else if (Angle.isInRange(targetAngle, curAngle - (float)Math.PI, curAngle - maxRotation))
				beam.rotateAbout(-maxRotation, beamStart);
			else
				beam.rotateAbout(targetAngle - curAngle, beamStart);
		}
	}
	
	@Override
	protected void channelFinished()
	{
		if (beam != null)
			beam.stopGrowth();
	}
	
	@Override
	public boolean decideToFire()
	{
		target = Unit.getNearestMember(owner.getCenter(), getTargetFaction());
		
		if (target != null)
		{
			owner.faceTowards(target.getX());
			beamStart = owner.getFiringLocation(target.getCenter());
			return true;
		}
		else
			return false;
	}
	
	@Override
	public MovingBeamShooter copy()
	{
		return new MovingBeamShooter(this);
	}
}