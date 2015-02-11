package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.Ability;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public abstract class AIAbility extends Ability
{
	protected AIUnit owner;				// AIUnit this Ability belongs to
	
	private boolean usesAnimation;		// true = activate ability after pre-fire animation completes
	private int channelAnimDuration;	// Duration (in ms) of channel animation for this ability. Default: 0 (not channeled)
	
	private int targetFaction;			// Faction to be targeted by selectTarget method
	private int rangeSquared;			// Range of ability (set to Integer.MAX_VALUE if no limit)
	
	// false if ability should be fired immediately once off CD, true if a small additional cooldown is desired in order to
	//appear more human/random
	private boolean randomCooldowns;
	
	// true if this Ability has been scaled for difficulty level (informs new values if they should be scaled for difficulty
	//when they are received)
	private boolean difficultyScaled;
	
	// Default cooldown reduction difficulty scaling
	private static final float[] CD_REDUCTION = new float[] { 1.3f, 1.0f, 0.7f };
	
	private boolean decidedToFire;
	
	public AIAbility(AIUnit owner, double cooldownSeconds)
	{
		super();
		
		this.owner = owner;
		usesAnimation = true;
		channelAnimDuration = 0;
		targetFaction = GameScreen.gregs;
		rangeSquared = Integer.MAX_VALUE;
		randomCooldowns = true;
		difficultyScaled = false;
		
		decidedToFire = false;
		
		setMaxCooldown(cooldownSeconds);
		triggerCooldown();
	}
	
	public AIAbility(AIAbility copyMe)
	{
		super(copyMe);
		
		owner = copyMe.owner;
		usesAnimation = copyMe.usesAnimation;
		channelAnimDuration = copyMe.channelAnimDuration;
		targetFaction = copyMe.targetFaction;
		rangeSquared = copyMe.rangeSquared;
		randomCooldowns = copyMe.randomCooldowns;
		difficultyScaled = copyMe.difficultyScaled;
		decidedToFire = copyMe.decidedToFire;
	}
	
	@Override
	public boolean isReadyToFire()
	{
		// Paraphrase: return (cooldown ready) and (ability specific decision making about whether or not to fire)
		return super.isReadyToFire() && decideToFire();
	}
	
	public void fire()
	{
		decidedToFire = false;
		activate();
		playSoundEffect();
		triggerCooldown();
	}
	
	@Override
	public void triggerCooldown()
	{
		super.triggerCooldown();
		if (randomCooldowns)
			cooldown *= 1.0 + (0.2 * Util.rand.nextDouble());
	}
	
	@Override
	public void setMaxCooldown(double cooldownSeconds)
	{
		// Friendly unit cooldowns are unchanged. Also, cooldown remains unchanged if this AIAbility has not been told to
		//scale with difficulty levels
		if (owner.getFaction() == GameScreen.gregs || !difficultyScaled)
			super.setMaxCooldown(cooldownSeconds);
		else
		{
			// Grant enemy AI Units a percentage based cooldown reduction based on difficulty level
			final double adjustedCD = CD_REDUCTION[OptionsScreen.difficulty] * cooldownSeconds;
			super.setMaxCooldown(adjustedCD);
		}
	}
	
	// Returns true if difficulty was scaled, false if no changes were made (i.e. if it has already been scaled previously)
	public boolean scaleForDifficulty()
	{
		// If already scaled, do nothing
		if (difficultyScaled)
			return false;
		
		difficultyScaled = true;
		
		// Scale cooldown
		setMaxCooldown((double)getMaxCooldown() / 1000);
		
		return true;
	}
	
	// Helper method to choose a random map location
	protected Point getRandomLocation()
	{
		final Point ret = new Point();
		ret.x = Util.rand.nextFloat() * Screen.WIDTH;
		ret.y = Util.rand.nextFloat() * Screen.HEIGHT;
		return ret;
	}
	
	// Called when owner gets killed/stunned etc (regardless of whether or not this ability was currently firing).
	//Particularly used for channeled abilities
	public void interrupted() { }
	
	public boolean usesAnimation() { return usesAnimation; }
	public boolean isChanneled() { return channelAnimDuration > 0; }
	public int getChannelDuration() { return channelAnimDuration; }
	public int getTargetFaction() { return targetFaction; }
	
	public void setOwner(AIUnit owner) { this.owner = owner; }
	public void setUsesAnimation(boolean usesAnimation) { this.usesAnimation = usesAnimation; }
	public void setChannelAnimDuration(int channelAnimDuration) { this.channelAnimDuration = channelAnimDuration; }
	public void setCooldownRandomness(boolean randomCooldowns) { this.randomCooldowns = randomCooldowns; }
	public void setRange(int range) { rangeSquared = Math.max(range * range, Integer.MAX_VALUE); }
	public void targetEnemies() { targetFaction = owner.getOpponents(); }
	public void targetTeammates() { targetFaction = owner.getTeammates(); }
	
	// Override decideToFire() if this ability needs to decide whether or not to activate once off cooldown
	public boolean decideToFire()
	{
		return true;
	}
	
	// Called when ability is activated and cooldown is started
	public abstract void activate();
	
	@Override public abstract AIAbility copy();
}