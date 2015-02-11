package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.Player;

public class SOneMultiTap extends MultiTapAbility
{
	private final StatusEffect shieldEffect;
	
	public SOneMultiTap(Player player)
	{
		super(player);
		
		setMaxCooldown(12);
		
		setCDIndicator(SpriteManager.cdShieldFull, SpriteManager.cdShieldEmpty);
		
		final StatPack sp = new StatPack();
		sp.bonusShields = 25;
		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.shield);
		anim.setSequenceLimit(-1);
		anim.setFadeIn(0.2f, 0.5);
		shieldEffect = new StatusEffect(sp, 2);
		shieldEffect.setAnimation(anim);
	}
	
	public SOneMultiTap(SOneMultiTap copyMe)
	{
		super(copyMe);
		
		shieldEffect = copyMe.shieldEffect.copy();
	}
	
	@Override
	public void activate()
	{
		player.addStatusEffect(shieldEffect);
	}
	
	@Override
	public SOneMultiTap copy()
	{
		return new SOneMultiTap(this);
	}
}