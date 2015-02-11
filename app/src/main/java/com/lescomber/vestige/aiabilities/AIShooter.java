package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.AIProjectileBehavior;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

// AIShooter is a basic projectile shooter that, once its cooldown is up, simply targets the nearest enemy and fires a copy
//of the prototype projectile
public class AIShooter extends AIAbility
{
	protected final Projectile prototype;	// Projectile to be fired whenever the cooldown is up
	private final AIProjectileBehavior behavior;
	
	private Unit target;			// Current target (selected during decideToFire())
	private Point firingLocation;	// Firing location (selected during decideToFire())
	
	// Damage and velocity difficulty scaling
	private static final float[] DAMAGE_SCALING = new float[] { 0.75f, 0.95f, 1.15f };
	private static final float[] VELOCITY_SCALING = new float[] { 0.9f, 1.0f, 1.1f };
	
	public AIShooter(AIUnit owner, Projectile prototype, double cooldownSeconds/*, boolean scaleForDifficulty*/)
	{
		super(owner, cooldownSeconds);
		
		this.prototype = prototype.copy();
		behavior = prototype.getBehavior();
		setRange(behavior.range);
		if (behavior.targetsEnemies)
			targetEnemies();
		else
			targetTeammates();
		target = null;
	}
	
	public AIShooter(AIShooter copyMe)
	{
		super(copyMe);
		
		prototype = copyMe.prototype.copy();
		behavior = new AIProjectileBehavior(copyMe.behavior);
		
		// Not copied
		target = null;
	}
	
	@Override
	public boolean scaleForDifficulty()
	{
		if (super.scaleForDifficulty())
		{
			// Scale prototype damage
			prototype.setDamage(DAMAGE_SCALING[OptionsScreen.difficulty] * prototype.getDamage());
			
			// Scale prototype velocity
			final int newVelocity = Math.round(VELOCITY_SCALING[OptionsScreen.difficulty] * prototype.getVelocityPerSecond());
			prototype.setVelocityPerSecond(newVelocity);
			
			return true;
		}
		else
			return false;
	}
	
	@Override
	public boolean decideToFire()
	{
		target = Unit.getNearestMember(owner.getCenter(), getTargetFaction());
		
		if (target != null)
		{
			owner.faceTowards(target.getX());
			firingLocation = owner.getFiringLocation();
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void activate()
	{
		// Determine destination based on target + a little randomness
		final Point dest = selectTargetLocation(target);
		
		// If prototype is a projectile that should shoot beyond its target and off the screen, extend its destination
		Line path = new Line(new Line(firingLocation.x, firingLocation.y, dest.x, dest.y));
		if (behavior.isExtended)
		{
			final Point startPoint = path.getStart();
			final Point endPoint = path.getExtEnd();
			path = new Line(new Line(startPoint, endPoint));
		}
		
		// Fire
		fire(path);
	}
	
	protected void fire(Line path)
	{
		// Init shot and set its targetFaction
		final Projectile shot = prototype.copy();
		
		// Update shot with path information
		shot.offsetTo(path.getStart());
		shot.setDestination(path.getEnd());
		shot.rotate(path.getDirection());
		
		// Fire
		owner.queueProjectile(shot);
	}
	
	// Add some randomness to the target location (i.e. don't always shoot directly at the center of target)
	Point selectTargetLocation(Unit target)
	{
		final int defaultRandomRange = 50;	// Radius from target center within which the target location will be chosen
		
		float tx = target.getX();
		float ty = target.getY();
		
		final float randX = (Util.rand.nextFloat() * 2 * defaultRandomRange) - defaultRandomRange;
		final float randY = (Util.rand.nextFloat() * 2 * defaultRandomRange) - defaultRandomRange;
		
		tx += randX;
		ty += randY;
		
		return new Point(tx, ty);
	}
	
	public void setIsExtended(boolean isExtended)
	{
		behavior.isExtended = isExtended;
	}
	
	@Override
	public AIShooter copy()
	{
		return new AIShooter(this);
	}
}