package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIChanneledAbility;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.sone.Meteor;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.AIUnit;

import java.util.LinkedList;

public class ShieldMeteorShower extends AIChanneledAbility
{
	private static final int[] SPAWN_INTERVAL = new int[] { 1500, 1150, 800 };
	public static final float[] SHIELD_STRENGTH = new float[] { 75, 85, 95 };
	private static final int SPAWN_ROWS = 4;
	private static final int SPAWN_COLS = 8;
	private static final float DPS[] = new float[] { 12, 16, 20 };
	private static final double FIRE_DURATION[] = new double[] { 10, 13.5f, 17 };
	
	private final StatusEffect shieldEffect;
	
	private final LinkedList<Point> spawnPoints;
	
	private int countdown;
	
	public ShieldMeteorShower(AIUnit owner, double cooldownSeconds)
	{
		super(owner, 3600, cooldownSeconds);
		
		final StatPack sp = new StatPack();
		sp.bonusShields = SHIELD_STRENGTH[OptionsScreen.difficulty];
		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.shield);
		anim.scale(1.5, 1.5);
		anim.setSequenceLimit(-1);
		anim.setFadeIn(0.2f, 0.5);
		shieldEffect = new StatusEffect(sp, 60);
		shieldEffect.setAnimation(anim);
		spawnPoints = new LinkedList<Point>();
	}
	
	public ShieldMeteorShower(ShieldMeteorShower copyMe)
	{
		super(copyMe);
		
		shieldEffect = copyMe.shieldEffect.copy();
		countdown = copyMe.countdown;
		spawnPoints = new LinkedList<Point>();
		for (final Point p : copyMe.spawnPoints)
			spawnPoints.add(new Point(p));
	}
	
	@Override
	public void activate()
	{
		super.activate();
		
		final float halfXGap = (Screen.WIDTH / SPAWN_COLS) / 2;
		final float halfYGap = (Screen.HEIGHT / SPAWN_ROWS) / 2;
		spawnPoints.clear();
		
		// Build tempArray with the list of possible spawnPoints (in order from top left to bottom right)
		final LinkedList<Point> tempArray = new LinkedList<Point>();
		for (int i=0; i<SPAWN_ROWS; i++)
		{
			for (int j=0; j<SPAWN_COLS; j++)
				tempArray.add(new Point((j * 2 + 1) * halfXGap, (i * 2 + 1) * halfYGap));
		}
		
		// Take elements from tempArray (in random order) and add them to spawnPoints
		while (!tempArray.isEmpty())
		{
			spawnPoints.add(tempArray.remove(Util.rand.nextInt(tempArray.size())));
		}
		
		owner.addStatusEffect(shieldEffect);
		
		countdown = SPAWN_INTERVAL[OptionsScreen.difficulty] / 2;
	}
	
	@Override
	protected void channeling(int deltaTime)
	{
		countdown -= deltaTime;
		
		while (countdown <= 0)
		{
			countdown += SPAWN_INTERVAL[OptionsScreen.difficulty];
			
			// Remove next spawnPoint from the start of the list and return it to the back of the list
			final Point spawnPoint = spawnPoints.removeFirst();
			spawnPoints.add(spawnPoint);
			
			owner.queueProjectile(new Meteor(Screen.WIDTH / SPAWN_COLS, Screen.HEIGHT / SPAWN_ROWS, spawnPoint,
					DPS[OptionsScreen.difficulty], FIRE_DURATION[OptionsScreen.difficulty]));
		}
		
		if (owner.getShields() <= 0)
			stopChannel();
	}
	
	@Override
	protected void channelFinished()
	{
		if (owner.getShields() > 0)
			owner.hit(new HitBundle(owner.getShields()));
	}
	
	@Override
	public ShieldMeteorShower copy()
	{
		return new ShieldMeteorShower(this);
	}
}