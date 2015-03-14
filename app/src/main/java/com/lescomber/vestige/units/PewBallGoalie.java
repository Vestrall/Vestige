package com.lescomber.vestige.units;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.map.PewBallMap;
import com.lescomber.vestige.projectiles.PewBall;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;

public class PewBallGoalie extends AIUnit
{
	private static final float BASE_SPEED = 30;
	private static final float[] SPEED_INCREASE_PER_LEVEL = new float[] { 7, 6 };
	//private static final float SPEED_INCREASE_MAX = 90;

	private static final double SCALE = 0.65;

	private static final float X = 735;
	private float[] yRange;

	private static final float BASE_MOVE_RANGE = 60;
	private static final float MOVE_RANGE_DECREASE_PER_LEVEL = 4;
	private static final float MOVE_RANGE_DECREASE_MAX = 40;
	private float moveRange;
	//private static final float MOVE_RANGE = 40;

	private PewBall targetBall;		// targetBall is the ball this goalie has been assigned (by PewBallMap) to track and stop (for now)

	private int destinationDelay;

	public PewBallGoalie(int levelNum, float startY)
	{
		super(60, 40, -23, 30);

		// PewBallGoalies are treated like good guys so they don't eat player projectiles
		setFaction(GameScreen.gregs);

		// Init stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 100;

		float speedIncrease;
		if (levelNum < PewBallMap.KEY_LEVELS[0])
			speedIncrease = levelNum * SPEED_INCREASE_PER_LEVEL[0];
		else
		{
			speedIncrease = (levelNum - PewBallMap.KEY_LEVELS[0]) * SPEED_INCREASE_PER_LEVEL[1];
			//if (speedIncrease > SPEED_INCREASE_MAX)
			//	speedIncrease = SPEED_INCREASE_MAX;
		}
		baseStats.moveSpeed = BASE_SPEED + speedIncrease;
		setDifficultyScaledBaseStats(baseStats);

		// Init idle sprites
		setIdleLeftSprite(SpriteManager.casterWalkLeft[0]);

		// Init walking animations
		final SpriteAnimation walkLeftAnim = new SpriteAnimation(SpriteManager.casterWalkLeft);
		setWalkLeftAnimation(walkLeftAnim);

		// Get rid of health bars
		createHealthBar(null, null);

		scale(SCALE, SCALE);

		offsetTo(X, startY);

		// Calculate yRange
		float halfRange;
		float totalRange = Screen.HEIGHT - (2 * PewBallMap.BUMPER_HEIGHT);
		if (levelNum < PewBallMap.KEY_LEVELS[0])
			halfRange = totalRange / 2;
		else
			halfRange = totalRange / 4;

		// Init yRange
		yRange = new float[2];
		yRange[0] = startY - halfRange;
		yRange[1] = startY + halfRange;

		// Prevent goalie overlap
		float halfGoalieHeight = getHitbox().getHeight() / 2;
		if (Math.abs(yRange[0] - PewBallMap.BUMPER_HEIGHT) > 5)
			yRange[0] += halfGoalieHeight;
		if (Math.abs(Screen.HEIGHT - yRange[1] - PewBallMap.BUMPER_HEIGHT) > 5)
			yRange[1] -= halfGoalieHeight;

		// Calculate moveRange
		float moveRangeDecrease;
		if (levelNum < PewBallMap.KEY_LEVELS[0])
			moveRangeDecrease = levelNum * MOVE_RANGE_DECREASE_PER_LEVEL;
		else
		{
			moveRangeDecrease = (levelNum - PewBallMap.KEY_LEVELS[0]) * MOVE_RANGE_DECREASE_PER_LEVEL;
			if (moveRangeDecrease > MOVE_RANGE_DECREASE_MAX)
				moveRangeDecrease = MOVE_RANGE_DECREASE_MAX;
		}

		moveRange = BASE_MOVE_RANGE - moveRangeDecrease;

		targetBall = null;
		destinationDelay = 0;
	}

	public PewBallGoalie(PewBallGoalie copyMe)
	{
		super(copyMe);

		yRange = new float[2];
		yRange[0] = copyMe.yRange[0];
		yRange[1] = copyMe.yRange[1];
		moveRange = copyMe.moveRange;

		// Not copied
		targetBall = null;
		destinationDelay = 1;
	}

	private boolean isInRange(float y)
	{
		return y >= yRange[0] && y <= yRange[1];
	}

	public float distanceFromRange(float y)
	{
		if (isInRange(y))
			return 0;
		else
		{
			if (y > yRange[1])
				return y - yRange[1];
			else
				return yRange[0] - y;
		}
	}

	public void track(PewBall ball)
	{
		this.targetBall = ball;
		if (getDestination() == null)
			chooseDestination();
	}

	private void chooseDestination()
	{
		if (targetBall == null)
		{
			destinationDelay = 60;
			return;
		}

		float yDest;

		// Move towards targetBall's current location
		if (Math.abs(targetBall.getY() - getY()) < moveRange)
			yDest = targetBall.getY();
		else if (targetBall.getY() < getY())
			yDest = getY() - moveRange;
		else
			yDest = getY() + moveRange;

		// Cap intended destination by yRange
		if (yDest <= yRange[0])
			yDest = yRange[0];
		else if (yDest > yRange[1])
			yDest = yRange[1];

		// Don't bother setting a new destination if it is very close to our current position (prevents infinitely looping
		//pathDestinationReached() -> chooseDestination() -> repeat)
		if (Math.abs(yDest - getY()) < 5)
		{
			destinationDelay = 60;
			return;
		}

		setDestination(getX(), yDest);
	}

	@Override
	protected void pathDestinationReached()
	{
		chooseDestination();

		super.pathDestinationReached();
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		if (destinationDelay > 0)
		{
			destinationDelay -= deltaTime;
			if (destinationDelay <= 0)
				chooseDestination();
		}
	}

	@Override public void hit(HitBundle bundle) { }		// Invincible

	@Override
	public AIUnit copy()
	{
		return new PewBallGoalie(this);
	}
}