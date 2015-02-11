package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIChanneledAbility;
import com.lescomber.vestige.projectiles.sone.Meteor;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class MeteorShower extends AIChanneledAbility
{
	private static final int[] SPAWN_INTERVAL = new int[] { 400, 350, 300 };
	private static final float FIRE_WIDTH[] = new float[] { 100, 130, 160 };
	private static final float DPS[] = new float[] { 8, 12, 16 };
	private static final double FIRE_DURATION[] = new double[] { 3, 4.5, 6 };
	private static final double DURATION = 2;
	
	private int countdown;
	
	public MeteorShower(AIUnit owner, double cooldownSeconds)
	{
		super(owner, DURATION, cooldownSeconds);
	}
	
	public MeteorShower(MeteorShower copyMe)
	{
		super(copyMe);
		
		countdown = copyMe.countdown;
	}
	
	@Override
	public void activate()
	{
		super.activate();
		
		countdown = SPAWN_INTERVAL[OptionsScreen.difficulty] / 2;
	}
	
	@Override
	protected void channeling(int deltaTime)
	{
		countdown -= deltaTime;
		
		while (countdown <= 0)
		{
			countdown += SPAWN_INTERVAL[OptionsScreen.difficulty];
			
			final int difficulty = OptionsScreen.difficulty;
			owner.queueProjectile(new Meteor(FIRE_WIDTH[difficulty], FIRE_WIDTH[difficulty], getRandomLocation(),
					DPS[OptionsScreen.difficulty], FIRE_DURATION[difficulty]));
		}
	}
	
	@Override protected void channelFinished() { }
	
	@Override
	public MeteorShower copy()
	{
		return new MeteorShower(this);
	}
}