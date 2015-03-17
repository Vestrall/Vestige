package com.lescomber.vestige.units;

import com.lescomber.vestige.Ability;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.gestures.GestureHandlerListener;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.playerabilities.DoubleTapAbility;
import com.lescomber.vestige.playerabilities.MultiTapAbility;
import com.lescomber.vestige.playerabilities.SOneChargeSwipe;
import com.lescomber.vestige.playerabilities.SOneDoubleTap;
import com.lescomber.vestige.playerabilities.SOneMultiTap;
import com.lescomber.vestige.playerabilities.SOneSwipe;
import com.lescomber.vestige.playerabilities.SwipeAbility;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.StatPack;

public class Player extends Unit implements GestureHandlerListener
{
	private static final float FIRING_OFFSET_X = 18;
	private static final float FIRING_OFFSET_Y = -30;

	SwipeAbility swipeAbility;
	SwipeAbility chargeSwipeAbility;
	DoubleTapAbility doubleTapAbility;
	MultiTapAbility multiTapAbility;

	Projectile swipeQueue;
	Projectile chargeSwipeQueue;

	Projectile lastSwipeQueue;            // Remember the last swipe in order to restore it if a multi-tap occurs
	Projectile lastChargeSwipeQueue;    // Remember the last chargeSwipe in order to restore it if a multi-tap occurs

	private boolean swipeFacingEast;

	// Scratch storage for Swipe related movement
	private static final float SWIPE_MOVE_CLOSE_SQUARED = 30 * 30;
	private static Point curProjPoint;
	private static Point swipeDestination;

	Point tapPoint;                // Remember tap point in order to return there if a swipe is cancelled
	Point previousTapPoint;        // Remember two taps ago in order to restore our previous move when a multi-tap occurs
	private boolean isSwiping;

	public Player()
	{
		super(37, 31, -27);

		createHealthBar(SpriteManager.hpBarBackground, SpriteManager.hpGregHealth);
		constrainHealthBar(true);

		setFiringOffsets(FIRING_OFFSET_X, FIRING_OFFSET_Y);
		setTopGap(30);

		// Init abilities
		swipeAbility = new SOneSwipe(this);
		chargeSwipeAbility = new SOneChargeSwipe(this);
		doubleTapAbility = new SOneDoubleTap(this);
		multiTapAbility = new SOneMultiTap(this);

		// Position CDIndicators
		chargeSwipeAbility.offsetCDIndicatorTo(35, 35);
		doubleTapAbility.offsetCDIndicatorTo(80, 35);
		multiTapAbility.offsetCDIndicatorTo(125, 35);

		swipeQueue = null;
		chargeSwipeQueue = null;
		lastSwipeQueue = null;
		lastChargeSwipeQueue = null;
		swipeFacingEast = true;

		curProjPoint = new Point();
		swipeDestination = new Point();

		tapPoint = null;
		previousTapPoint = null;
		isSwiping = false;

		// Init base stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 100;
		baseStats.moveSpeed = 300;
		setBaseStats(baseStats);

		// Observe dancers
		if (GameScreen.map.getStageNum() == 1 && GameScreen.map.getLevelNum() == 9)
			allCooldown(4);

		// Init idle sprites
		setIdleLeftSprite(SpriteManager.playerWalkLeft[0]);
		setIdleRightSprite(SpriteManager.playerWalkRight[0]);

		// Init walking animations
		final SpriteAnimation walkLeftAnim = new SpriteAnimation(SpriteManager.playerWalkLeft);
		setWalkLeftAnimation(walkLeftAnim);

		final SpriteAnimation walkRightAnim = new SpriteAnimation(SpriteManager.playerWalkRight);
		setWalkRightAnimation(walkRightAnim);

		// Set firing animations
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.playerFiringLeft, 0, 2);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation postFiringLeftAnim = new SpriteAnimation(SpriteManager.playerFiringLeft, 3, 5);
		setPostFiringLeftAnimation(postFiringLeftAnim);

		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.playerFiringRight, 0, 2);
		setPreFiringRightAnimation(preFiringRightAnim);
		final SpriteAnimation postFiringRightAnim = new SpriteAnimation(SpriteManager.playerFiringRight, 3, 5);
		setPostFiringRightAnimation(postFiringRightAnim);

		faceRight();
		idleSprite();    // Update image to face right
	}

	private void allCooldown(double cooldownSeconds)
	{
		swipeAbility.setCooldown(cooldownSeconds);
		chargeSwipeAbility.setCooldown(cooldownSeconds);
		doubleTapAbility.setCooldown(cooldownSeconds);
		multiTapAbility.setCooldown(cooldownSeconds);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		// Update abilities
		swipeAbility.update(deltaTime);
		doubleTapAbility.update(deltaTime);
		chargeSwipeAbility.update(deltaTime);
		multiTapAbility.update(deltaTime);

		// Detect if we're standing still waiting for a (charge)swipeAbility to come off CD. Start firing animation if so
		// Case: we are standing still, swipe/chargeSwipe is queue'd up and off cooldown, and we aren't preFireAnimating
		if (getDestination() == null && (((chargeSwipeQueue != null && chargeSwipeAbility.getCooldown() <= 0) ||
				(swipeQueue != null && swipeAbility.getCooldown() <= 0)) && getCurrentAnimID() != getPreFiringLeftIndex() &&
				getCurrentAnimID() != getPreFiringRightIndex()))
		{
			if (swipeFacingEast)
				faceRight();
			else
				faceLeft();

			preFiringAnim();
		}
	}

	@Override
	protected void pathDestinationReached()
	{
		Ability queuedAbility = null;
		if (chargeSwipeQueue != null)
			queuedAbility = chargeSwipeAbility;
		else if (swipeQueue != null)
			queuedAbility = swipeAbility;

		if (queuedAbility != null)
		{
			if (swipeFacingEast)
				faceRight();
			else
				faceLeft();

			if (queuedAbility.getCooldown() <= 0)
				preFiringAnim();
			else
				idleSprite();
		}
		else if (isSwiping)
		{
			if (swipeFacingEast)
				faceRight();
			else
				faceLeft();

			idleSprite();
		}
		else
			idleSprite();
	}

	@Override
	protected void animationFinished(int animID)
	{
		if (animID == getPreFiringRightIndex())
		{
			if (chargeSwipeQueue != null)
				fireChargeSwipe();
			else if (swipeQueue != null)
				fireSwipe();

			postFiringAnim(false);
		}
		else if (animID == getPreFiringLeftIndex())
		{
			if (chargeSwipeQueue != null)
				fireChargeSwipe();
			else if (swipeQueue != null)
				fireSwipe();

			postFiringAnim(true);
		}
		else
			idleSprite();
	}

	void fireSwipe()
	{
		isSwiping = false;
		if (swipeFacingEast)
			swipeQueue.offsetTo(getX() + getFiringOffsetX(), getY() + getFiringOffsetY());
		else
			swipeQueue.offsetTo(getX() - getFiringOffsetX(), getY() + getFiringOffsetY());
		swipeQueue.setVisible(true);
		queueProjectile(swipeQueue);
		swipeAbility.playSoundEffect();
		swipeQueue = null;
		lastSwipeQueue = null;
		swipeAbility.triggerCooldown();
	}

	void fireChargeSwipe()
	{
		isSwiping = false;
		if (swipeFacingEast)
			chargeSwipeQueue.offsetTo(getX() + getFiringOffsetX(), getY() + getFiringOffsetY());
		else
			chargeSwipeQueue.offsetTo(getX() - getFiringOffsetX(), getY() + getFiringOffsetY());
		chargeSwipeQueue.setVisible(true);
		queueProjectile(chargeSwipeQueue);
		chargeSwipeAbility.playSoundEffect();
		chargeSwipeQueue = null;
		lastChargeSwipeQueue = null;
		chargeSwipeAbility.triggerCooldown();
	}

	@Override
	public void pathTo(Point end)
	{
		end = GameScreen.map.adjustDestination(end, getTopGap());
		setPath(GameScreen.map.getPath(getCenter(), end/*, 0*/));
	}

	public void pathTo(float x, float y)
	{
		pathTo(new Point(x, y));
	}

	@Override
	public void handleTap(Point tapPoint)
	{
		previousTapPoint = this.tapPoint;
		this.tapPoint = tapPoint;
		pathTo(tapPoint);
		lastSwipeQueue = swipeQueue;
		swipeQueue = null;
		lastChargeSwipeQueue = chargeSwipeQueue;
		chargeSwipeQueue = null;
	}

	@Override
	public void swipeBuilding(Line swipe)
	{
		swipeFacingEast = swipe.getStart().x <= swipe.getEnd().x;
		moveToSwipe(swipe);
		isSwiping = true;
	}

	@Override
	public void chargeSwipeBuilding(Line swipe)
	{
		swipeFacingEast = swipe.getStart().x <= swipe.getEnd().x;
		moveToSwipe(swipe);
		isSwiping = true;
	}

	private void moveToSwipe(Line swipe)
	{
		final float offsetX = swipeFacingEast ? getFiringOffsetX() : -getFiringOffsetX();

		curProjPoint.set(getX() - offsetX, getY());

		if (getCenter().distanceToPointSquared(swipe.getStart()) <= SWIPE_MOVE_CLOSE_SQUARED)
			swipeDestination = swipe.getClosestToPoint(curProjPoint);
		else
			swipeDestination.set(swipe.getStart().x - offsetX, swipe.getStart().y);

		pathTo(swipeDestination);
	}

	@Override
	public void swipeCancelled()
	{
		isSwiping = false;
		pathTo(tapPoint);
	}

	// TODO: Elegantly handle swipes that begin in obstacles or near map edges.. perhaps recognize them and disallow them?
	@Override
	public void handleSwipe(Line swipe)
	{
		swipeQueue = swipeAbility.prepare(swipe, getFiringOffsetY());
		lastSwipeQueue = swipeQueue;
		chargeSwipeQueue = null;
		lastChargeSwipeQueue = null;
	}

	@Override
	public void handleChargeSwipe(Line swipe)
	{
		chargeSwipeQueue = chargeSwipeAbility.prepare(swipe, getFiringOffsetY());
		lastChargeSwipeQueue = chargeSwipeQueue;
		swipeQueue = null;
		lastSwipeQueue = null;
	}

	@Override
	public void handleDoubleTap(Point tapPoint)
	{
		if (doubleTapAbility.getCooldown() <= 0)
		{
			doubleTapAbility.fire(tapPoint);
			doubleTapAbility.playSoundEffect();
			doubleTapAbility.triggerCooldown();
		}
	}

	@Override
	public void handleMultiTap()
	{
		if (multiTapAbility.getCooldown() <= 0)
		{
			multiTapAbility.activate();
			multiTapAbility.playSoundEffect();
			multiTapAbility.triggerCooldown();
		}

		// Re-establish the move command prior to multi-tap
		if (previousTapPoint != null)
		{
			pathTo(previousTapPoint);

			tapPoint = previousTapPoint;    // Return tapPoint to the tap before multi-tap
		}
		else
			pathTo(getCenter());    // Cancel move command that was started by the first tap of the multi-tap

		// Re-establish (charge)swipeQueue (if any)
		swipeQueue = lastSwipeQueue;
		chargeSwipeQueue = lastChargeSwipeQueue;
	}

	public boolean isSwipeQueued()
	{
		return swipeQueue != null;
	}

	public boolean isChargeSwipeQueued()
	{
		return chargeSwipeQueue != null;
	}

	public boolean isChargeSwipeReady()
	{
		return chargeSwipeAbility.getCooldown() <= 0;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		super.setVisible(isVisible);

		setCDIndicatorsVisible(isVisible);
	}

	public void setCDIndicatorsVisible(boolean isVisible)
	{
		chargeSwipeAbility.setCDIndicatorVisible(isVisible);
		doubleTapAbility.setCDIndicatorVisible(isVisible);
		multiTapAbility.setCDIndicatorVisible(isVisible);
	}
}