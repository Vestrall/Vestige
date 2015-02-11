package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.units.AIUnit;

public abstract class AIChanneledAbility extends AIAbility
{
	private boolean isChanneling;	// true when channeling is taking place
	private int channelDuration;	// Time (in ms) remaining for current channel (during isChanneling)
	private final int maxChannelDuration;	// In ms
	
	public AIChanneledAbility(AIUnit owner, double channelDurationSeconds, double cooldownSeconds/*, boolean scaleForDifficulty*/)
	{
		super(owner, cooldownSeconds);
		
		isChanneling = false;
		this.channelDuration = (int)(channelDurationSeconds * 1000);
		maxChannelDuration = this.channelDuration;
		
		setChannelAnimDuration(maxChannelDuration);
	}
	
	public AIChanneledAbility(AIChanneledAbility copyMe)
	{
		super(copyMe);
		
		isChanneling = copyMe.isChanneling;
		channelDuration = copyMe.channelDuration;
		maxChannelDuration = copyMe.maxChannelDuration;
	}
	
	protected abstract void channeling(int deltaTime);
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		if (isChanneling)
		{
			channeling(deltaTime);
			channelDuration -= deltaTime;
			if (channelDuration <= 0)
				stopChannel();
		}
	}
	
	@Override	// Called when owner is killed/stunned/etc (even if this ability isn't currently channeling)
	public void interrupted()
	{
		if (isChanneling)
			stopChannel();
	}
	
	// End current channel (either due to channelDuration <= 0 or due to interruption from owner death/stun/etc)
	public void stopChannel()
	{
		isChanneling = false;
		channelFinished();
		owner.channelFinished();
		triggerCooldown();
	}
	
	protected abstract void channelFinished();	// Called when current channel has ended
	
	@Override
	public void activate()
	{
		isChanneling = true;
		channelDuration = maxChannelDuration;
	}
	
	@Override public int getChannelDuration() { return channelDuration; }
}