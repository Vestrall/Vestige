package com.lescomber.vestige.units;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.screens.TutorialScreen;
import com.lescomber.vestige.statuseffects.HitBundle;

public class TutorialPlayer extends Player
{
	private final TutorialScreen tScreen;
	private final boolean[] abilityAvailable;

	public TutorialPlayer(TutorialScreen tScreen)
	{
		super();

		this.tScreen = tScreen;
		abilityAvailable = new boolean[4];
		for (int i = 0; i < 4; i++)
			abilityAvailable[i] = false;
	}

	public void unlockAbility(int abilityNum)
	{
		if (!abilityAvailable[abilityNum])
		{
			abilityAvailable[abilityNum] = true;
			if (abilityNum == 1)
				chargeSwipeAbility.setCDIndicatorVisible(true);
			else if (abilityNum == 2)
				doubleTapAbility.setCDIndicatorVisible(true);
			else if (abilityNum == 3)
				multiTapAbility.setCDIndicatorVisible(true);
		}
	}

	public boolean isAbilityUnlocked(int abilityNum)
	{
		return abilityAvailable[abilityNum];
	}

	@Override
	protected void destinationReached()
	{
		super.destinationReached();

		tScreen.tapLessonCompleted();
	}

	@Override
	void fireSwipe()
	{
		super.fireSwipe();

		tScreen.swipeLessonCompleted();
	}

	@Override
	void fireChargeSwipe()
	{
		super.fireChargeSwipe();

		tScreen.chargeSwipeLessonCompleted();
	}

	@Override
	public void handleSwipe(Line swipe)
	{
		if (abilityAvailable[0])
			super.handleSwipe(swipe);
	}

	@Override
	public void handleChargeSwipe(Line swipe)
	{
		if (abilityAvailable[1])
			super.handleChargeSwipe(swipe);
		else
			handleSwipe(swipe);
	}

	@Override
	public void handleDoubleTap(Point tapPoint)
	{
		if (abilityAvailable[2])
			super.handleDoubleTap(tapPoint);
	}

	@Override
	public void handleMultiTap()
	{
		if (abilityAvailable[3])
			super.handleMultiTap();
	}

	@Override    // Invincible!
	public void hit(HitBundle bundle)
	{
		super.hit(bundle);

		if (getHp() <= 0)
			hit(new HitBundle(getHp() - 1));
	}

	public void clearActions()
	{
		swipeQueue = null;
		chargeSwipeQueue = null;
		lastSwipeQueue = null;
		lastChargeSwipeQueue = null;
		tapPoint = null;
		previousTapPoint = null;
	}

	@Override
	public void setCDIndicatorsVisible(boolean isVisible)
	{
		if (abilityAvailable[1])
			chargeSwipeAbility.setCDIndicatorVisible(isVisible);
		if (abilityAvailable[2])
			doubleTapAbility.setCDIndicatorVisible(isVisible);
		if (abilityAvailable[3])
			multiTapAbility.setCDIndicatorVisible(isVisible);
	}
}