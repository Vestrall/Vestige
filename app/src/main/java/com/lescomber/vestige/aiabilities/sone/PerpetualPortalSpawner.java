package com.lescomber.vestige.aiabilities.sone;

import java.util.ArrayList;

import com.lescomber.vestige.aiabilities.AIChanneledAbility;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.sone.OneTenBoss;
import com.lescomber.vestige.units.sone.PerpetualSpawnPortal;

public class PerpetualPortalSpawner extends AIChanneledAbility
{
	private static final double CHANNEL_DURATION = 18;
	private static final int[] INTERVAL = new int[] { 9000, 6000, 4500 };
	private int countdown;
	
	private static final float BOSS_SPACE_HALF_WIDTH = 80;
	private static final float BOSS_SPACE_HALF_HEIGHT = 80;
	private static final float SPACE_HALF_WIDTH = 50;
	private static final float SPACE_HALF_HEIGHT = 50;
	private final ArrayList<Rectangle> portalPersonalSpaces;
	
	private static final float HEAL_AMOUNT = 15;
	
	private final OneTenBoss owner;
	
	public PerpetualPortalSpawner(OneTenBoss owner, double cooldownSeconds)
	{
		super(owner, CHANNEL_DURATION, cooldownSeconds);
		
		this.owner = owner;
		countdown = 0;
		
		portalPersonalSpaces = new ArrayList<Rectangle>();
	}
	
	public PerpetualPortalSpawner(PerpetualPortalSpawner copyMe)
	{
		super(copyMe);
		
		this.owner = copyMe.owner;
		countdown = copyMe.countdown;
		
		portalPersonalSpaces = new ArrayList<Rectangle>();
		for (final Rectangle r : copyMe.portalPersonalSpaces)
			portalPersonalSpaces.add(new Rectangle(r));
	}
	
	@Override
	public void activate()
	{
		super.activate();
		
		countdown = 0;
		portalPersonalSpaces.clear();
		
		final Rectangle newRect = new Rectangle(owner.getX() - BOSS_SPACE_HALF_WIDTH, owner.getY() - BOSS_SPACE_HALF_HEIGHT,
				owner.getX() + BOSS_SPACE_HALF_WIDTH, owner.getY() + BOSS_SPACE_HALF_HEIGHT);
		portalPersonalSpaces.add(newRect);
	}
	
	@Override
	protected void channeling(int deltaTime)
	{
		countdown -= deltaTime;
		if (countdown <= 0)
		{
			countdown += INTERVAL[OptionsScreen.difficulty];
			
			float x = 0;
			float y = 0;
			boolean occupado = true;
			while (occupado)
			{
				x = 40 + (Util.rand.nextFloat() * 720);
				y = 40 + (Util.rand.nextFloat() * 400);
				
				occupado = false;
				for (final Rectangle r : portalPersonalSpaces)
				{
					if (r.contains(x, y))
						occupado = true;
				}
			}
			
			final Rectangle newRect = new Rectangle(x - SPACE_HALF_WIDTH, y - SPACE_HALF_HEIGHT, x + SPACE_HALF_WIDTH, y + SPACE_HALF_HEIGHT);
			portalPersonalSpaces.add(newRect);
			final PerpetualSpawnPortal psp = new PerpetualSpawnPortal(x, y);
			if (getChannelDuration() < INTERVAL[OptionsScreen.difficulty])	// If last portal, give it a HealPickUp
				psp.setPickUp(new HealPickUp(HEAL_AMOUNT));
			owner.queueAIUnit(psp);
		}
	}
	
	@Override
	protected void channelFinished()
	{
		owner.cooldownSync();
	}
	
	@Override
	public PerpetualPortalSpawner copy()
	{
		return new PerpetualPortalSpawner(this);
	}
}