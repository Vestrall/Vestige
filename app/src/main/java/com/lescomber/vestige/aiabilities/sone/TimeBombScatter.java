package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIChanneledAbility;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.sone.TimeBomb;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class TimeBombScatter extends AIChanneledAbility
{
	private static final double CHANNEL_DURATION = 3;
	private static final int[] SPAWN_INTERVAL = new int[] { 150, 125, 100 };  // Interval (in ms) between each bomb summoned
	
	private static final int[] BOMB_TIMER = new int[] { 1000, 850, 700 };
	
	private int spawnCooldown;
	
	// Time bomb explosion sounds limited to EXPLOSION_SOUND_MAX quantity (since we don't want 20 explosion sounds going off at
	//the same time)
	private static final int EXPLOSION_SOUND_MAX = 3;
	private int explosionSoundCount;
	
	public TimeBombScatter(AIUnit owner, double cooldownSeconds)
	{
		super(owner, CHANNEL_DURATION, cooldownSeconds);
		
		spawnCooldown = SPAWN_INTERVAL[OptionsScreen.difficulty];
		explosionSoundCount = 0;
	}
	
	public TimeBombScatter(TimeBombScatter copyMe)
	{
		super(copyMe);
		
		spawnCooldown = copyMe.spawnCooldown;
		explosionSoundCount = copyMe.explosionSoundCount;
	}
	
	@Override
	public void activate()
	{
		super.activate();
		
		explosionSoundCount = 0;
	}
	
	@Override
	protected void channeling(int deltaTime)
	{
		spawnCooldown -= deltaTime;
		
		while (spawnCooldown <= 0)
		{
			spawnCooldown += SPAWN_INTERVAL[OptionsScreen.difficulty];
			
			// Spawn projectile
			final Point firingLocation = owner.getFiringLocation();
			
			// Magic number 0.1 = chance to target player location
			Point dest;
			if (getChannelDuration() > 3 * SPAWN_INTERVAL[OptionsScreen.difficulty] && Util.rand.nextFloat() < 0.1)
				dest = new Point(GameScreen.player.getCenter());
			else
				dest = getRandomLocation();
			
			final int bombTimer = getChannelDuration() + BOMB_TIMER[OptionsScreen.difficulty];
			final TimeBomb tb = new TimeBomb(firingLocation.x, firingLocation.y, dest.x, dest.y, bombTimer);
			if (explosionSoundCount < EXPLOSION_SOUND_MAX)
			{
				tb.setExplosionSound(AudioManager.purpleExplosion);
				explosionSoundCount++;
			}
			owner.queueProjectile(tb);
		}
	}
	
	@Override protected void channelFinished() { }
	
	@Override
	public TimeBombScatter copy()
	{
		return new TimeBombScatter(this);
	}
}