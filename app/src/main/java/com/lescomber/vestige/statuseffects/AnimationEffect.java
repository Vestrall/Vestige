package com.lescomber.vestige.statuseffects;

import com.lescomber.vestige.graphics.SpriteAnimation;

public class AnimationEffect extends StatusEffect
{
	public AnimationEffect(SpriteAnimation anim)
	{
		super(new StatPack(), anim.getTimeRemaining() / 1000.0);

		setAnimation(anim);
		setNoEffectRemoval(false);
	}

	public AnimationEffect(AnimationEffect copyMe)
	{
		super(copyMe);
	}

	@Override
	public AnimationEffect copy()
	{
		return new AnimationEffect(this);
	}
}