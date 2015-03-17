package com.lescomber.vestige.units;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.statuseffects.StatPack;

public class Boss extends AIRailUnit
{
	public Boss(float maxHp, int moveSpeed)
	{
		super(120, 70, -47, 100);

		createHealthBar(SpriteManager.hpBossBackground, SpriteManager.hpBossHealth);
		offsetHealthBar(0, 12);

		// Init stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = maxHp;
		baseStats.moveSpeed = moveSpeed;
		setBaseStats(baseStats);

		//setTopGap(70);
		createRailLocations(50);

		// Set default boss immunities
		setBossDefaults();

		// Init idle sprites
		setIdleLeftSprite(SpriteManager.bossWalkLeft[0]);
		setIdleRightSprite(SpriteManager.bossWalkRight[0]);

		// Init walking animations
		setWalkLeftAnimation(new SpriteAnimation(SpriteManager.bossWalkLeft));
		setWalkRightAnimation(new SpriteAnimation(SpriteManager.bossWalkRight));

		// Init firing left animations
		setPreFiringLeftAnimation(new SpriteAnimation(SpriteManager.bossFiringLeft, 0, 15));
		setPostFiringLeftAnimation(new SpriteAnimation(SpriteManager.bossFiringLeft, 16, 16));

		// Init firing right animations
		setPreFiringRightAnimation(new SpriteAnimation(SpriteManager.bossFiringRight, 0, 15));
		setPostFiringRightAnimation(new SpriteAnimation(SpriteManager.bossFiringRight, 16, 16));

		// Init pre-channel animations
		setPreChannelingLeftAnimation(new SpriteAnimation(SpriteManager.bossFiringLeft, 0, 5));
		setPreChannelingRightAnimation(new SpriteAnimation(SpriteManager.bossFiringRight, 0, 5));

		// Init channel animations
		setChannelingLeftAnimation(new SpriteAnimation(SpriteManager.bossChannelingLeft));
		setChannelingRightAnimation(new SpriteAnimation(SpriteManager.bossChannelingRight));

		// Init post-channel animations
		final SpriteAnimation postChannelLeftAnim = new SpriteAnimation();
		for (int i = 4; i >= 0; i--)
			postChannelLeftAnim.addFrame(SpriteManager.bossFiringLeft[i]);
		setPostChannelingLeftAnimation(postChannelLeftAnim);
		final SpriteAnimation postChannelRightAnim = new SpriteAnimation();
		for (int i = 4; i >= 0; i--)
			postChannelRightAnim.addFrame(SpriteManager.bossFiringRight[i]);
		setPostChannelingRightAnimation(postChannelRightAnim);

		// Init death animations
		setDeathAnimationLeft(new SpriteAnimation(SpriteManager.bossDeathLeft));
		setDeathAnimationRight(new SpriteAnimation(SpriteManager.bossDeathRight));
		setDeathAnimXOffset(22);
	}

	public Boss(Boss copyMe)
	{
		super(copyMe);
	}

	@Override
	public Boss copy()
	{
		return new Boss(this);
	}
}