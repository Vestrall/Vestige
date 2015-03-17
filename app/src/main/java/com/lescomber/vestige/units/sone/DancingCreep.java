package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.FloatingCreep;

public class DancingCreep extends FloatingCreep
{
	private final int dancingLeftAnim;
	private int dancingRightAnim;
	private int danceSequenceDuration;

	private boolean isAngry;    // Dances until isAngry == false
	private final boolean isCaptain;    // Only the captain calls danceCoordinator.update() (rather than making a custom map class do it)
	private final DanceCoordinator danceCoordinator;
	private boolean isActuallyEntering;        // Replaces usual isEntering boolean because isEntering will be used during dancing

	public DancingCreep(DanceCoordinator danceCoordinator)
	{
		super();

		// Init dancing animations
		SpriteAnimation anim = new SpriteAnimation();
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[0]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[3]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[0]);
		anim.setFrameTime(200);
		anim.setSequenceLimit(-1);
		dancingLeftAnim = addAnimation(anim);

		anim = new SpriteAnimation();
		anim.addFrame(SpriteManager.floatingCreepAttackRight[0]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[3]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[1]);
		anim.addFrame(SpriteManager.floatingCreepAttackLeft[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[2]);
		anim.addFrame(SpriteManager.floatingCreepAttackRight[0]);
		anim.setFrameTime(200);
		anim.setSequenceLimit(-1);
		dancingRightAnim = addAnimation(anim);

		danceSequenceDuration = anim.getFrameTime() * anim.getFrameCount();

		isAngry = false;
		isCaptain = false;
		isActuallyEntering = true;
		this.danceCoordinator = danceCoordinator;
	}

	public DancingCreep(DancingCreep copyMe)
	{
		super(copyMe);

		dancingLeftAnim = copyMe.dancingLeftAnim;
		isAngry = copyMe.isAngry;
		isCaptain = copyMe.isCaptain;
		danceCoordinator = copyMe.danceCoordinator;
		isActuallyEntering = copyMe.isActuallyEntering;
	}

	public void danceLeft()
	{
		setEntering(true);
		restartAnimation(dancingLeftAnim);
	}

	public void danceRight()
	{
		setEntering(true);
		restartAnimation(dancingRightAnim);
	}

	public void dancePosition(Point dest)
	{
		setEntering(false);
		setDestination(dest);
		setEntering(true);
	}

	public void aggro()
	{
		isAngry = true;
		setEntering(isActuallyEntering);
		chooseDestination();
	}

	public int getDanceSequenceDuration()
	{
		return danceSequenceDuration;
	}

	@Override
	public void hasEntered()
	{
		super.hasEntered();

		setEntering(!isAngry);
		isActuallyEntering = false;
	}

	@Override
	public void hit(HitBundle bundle)
	{
		super.hit(bundle);

		if (!isAngry)
			danceCoordinator.aggro();
	}

	@Override
	public DancingCreep copy()
	{
		return new DancingCreep(this);
	}
}