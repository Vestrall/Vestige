package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Player;
import com.lescomber.vestige.units.Unit;

import java.util.ArrayList;
import java.util.Iterator;

public class SOneDoubleTap extends DoubleTapAbility
{
	private static final float DAMAGE = 12;
	static final int RANGE_SQUARED = 400 * 400;
	
	private final HitBundle hitBundle;
	
	final ArrayList<SOneDoubleTapLaser> activeLasers;
	
	public SOneDoubleTap(Player player)
	{
		super(player);
		
		setMaxCooldown(8);
		
		setCDIndicator(SpriteManager.cdTeleportFull, SpriteManager.cdTeleportEmpty);
		hitBundle = new HitBundle(DAMAGE);
		hitBundle.setHitSound(AudioManager.sOneDoubleTapHit);
		
		activeLasers = new ArrayList<SOneDoubleTapLaser>(2);
	}
	
	public SOneDoubleTap(SOneDoubleTap copyMe)
	{
		super(copyMe);
		
		hitBundle = new HitBundle(copyMe.hitBundle);
		
		activeLasers = new ArrayList<SOneDoubleTapLaser>(2);
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		// Update any active lasers
		final Iterator<SOneDoubleTapLaser> itr = activeLasers.iterator();
		while (itr.hasNext())
		{
			final SOneDoubleTapLaser laser = itr.next();
			laser.update(deltaTime);
			if (laser.isFinished())
				itr.remove();
		}
	}
	
	@Override
	public void fire(Point p)
	{
		// Teleport player
		p = GameScreen.map.adjustDestination(p, player.getTopGap());
		player.offsetTo(p);
		player.setPath(null);
		
		// Pick target
		final Unit target = Unit.getNearestMember(player.getCenter(), GameScreen.steves, RANGE_SQUARED);
		
		// Fire projectile
		if (target != null)
		{
			final Line path = new Line(player.getImageCenter(), target.getImageCenter());
			activeLasers.add(new SOneDoubleTapLaser(path));
			
			// Deal the damage
			target.hit(hitBundle);
		}
	}
	
	@Override
	public SOneDoubleTap copy()
	{
		return new SOneDoubleTap(this);
	}
}