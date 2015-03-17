package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.units.FloatingCreep;

public class BabyCreep extends FloatingCreep
{
	private final int dancingAnim;

	public BabyCreep()
	{
		super();

		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 45 + (15 * OptionsScreen.difficulty);
		baseStats.moveSpeed = 400 + (50 * OptionsScreen.difficulty);
		setBaseStats(baseStats);

		clearAbilities();

		final SpriteAnimation anim = new SpriteAnimation();
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[0]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[3]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[0]);
		anim.setFrameTime(40);
		dancingAnim = addAnimation(anim);

		scale(0.5, 0.5);
	}

	private void randomMove()
	{
		final Point dest = new Point((Util.rand.nextFloat() * 700) + 50, (Util.rand.nextFloat() * 380) + 50);
		pathTo(dest);
	}

	@Override
	public void die()
	{
		super.die();

		// Spawn the angries
		FloatingCreep angry = new FloatingCreep();
		angry.offsetTo(300, -50);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(450, -50);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(600, -50);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(850, 240);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(600, 530);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(450, 530);
		queueAIUnit(angry);

		angry = new FloatingCreep();
		angry.offsetTo(300, 530);
		queueAIUnit(angry);
	}

	@Override
	protected void pathDestinationReached()
	{
		// Random chance to dance
		if (Util.rand.nextFloat() < 0.3f)
			restartAnimation(dancingAnim);
		else
			randomMove();
	}

	@Override
	protected void animationFinished(int animIndex)
	{
		super.animationFinished(animIndex);

		randomMove();
	}

	@Override
	public void chooseDestination()
	{
	}
}