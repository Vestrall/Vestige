package com.lescomber.vestige.map;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.PewBall;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.PewBallScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.PewBallGoalie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class PewBallMap extends Map
{
	public static final float BUMPER_HEIGHT = 35;

	//private static final int[] SCORE_LIMITS = new int[] { 5, 8, 11 };
	private static final int[] SCORE_LIMITS = new int[] { 5, 10 };

	private PewBallScreen pewBallScreen;

	private static final Point[] PORTAL_LOCATIONS = { new Point(590, 120), new Point(650, 240), new Point(590, 360) };
	private final SpriteAnimation PORTAL_ANIM;
	private final int BALL_SPAWN_DELAY;
	private int ballSpawnDelay;
	//private static final int BALL_DOUBLE_SPAWN_LEVEL = 10;

	// Difficulty scaling mechanics change at these levels (e.g. # of balls spawning at once)
	public static final int[] KEY_LEVELS = new int[] { 10, 20 };
	//public static final int KEY_LEVEL = 10;

	//private static final int BALL_SPAWN_INTERVAL = 10000;	// In ms
	// Interval drops from KEY_LEVELS[0] to KEY_LEVELS[1]
	private static final int PORTAL_SPAWN_INTERVAL[] = new int[] { 30000, 20000 };
	private int portalSpawnInterval;
	//private static final int BASE_SPAWN_INTERVAL = 30000;		// In ms
	//private static final int SPAWN_REDUCTION_PER_LEVEL = 1500;	// In ms. Occurs starting at KEL_LEVEL
	//private static final int MIN_SPAWN_INTERVAL = 15000;		// In ms
	private int portalSpawnCountdown;

	private AIUnit spawner;

	private ArrayList<PewBall> balls;

	private ArrayList<PewBallGoalie> goalies;
	private ArrayList<PewBall> orderedBalls;	// Used for goalie ball tracking in manageGoalies() only. Reference is kept here to
												//prevent re-creating this list every frame

	//private static final int GOALIE_INTERVAL[] = { 1000, 2000 };
	//private int goalieSpawnInterval;

	private int levelNum;

	public PewBallMap(int levelNum)
	{
		super(Levels.PEW_BALL_STAGE, 0);

		this.levelNum = levelNum;

		setPlayerSpawnPoint(100, 240);
		addEnemySpawnPoint(700, 240);
		setPortalPoint(200, 240);

		final Wave wave = new Wave(0);
		addWave(wave);

		// Init bumpers along top and bottom of screen
		addObstacle(new Wall(-15, 0, Screen.WIDTH + 15, BUMPER_HEIGHT));
		addObstacle(new Wall(-15, Screen.HEIGHT - BUMPER_HEIGHT, Screen.WIDTH + 15, Screen.HEIGHT));
		buildNodeNetwork();

		// Init portal spawning animation
		PORTAL_ANIM = new SpriteAnimation(SpriteManager.spawnPortalSpawn);
		PORTAL_ANIM.addFrames(SpriteManager.spawnPortalOpen);
		BALL_SPAWN_DELAY = PORTAL_ANIM.getTimeRemaining();
		PORTAL_ANIM.addFrames(SpriteManager.spawnPortalEnd);

		balls = new ArrayList<PewBall>();

		if (levelNum <= KEY_LEVELS[0])
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[0];
		else if (levelNum < KEY_LEVELS[1])
		{
			int totalDif = PORTAL_SPAWN_INTERVAL[0] - PORTAL_SPAWN_INTERVAL[1];
			float reduction = totalDif * (((float)levelNum - KEY_LEVELS[0]) / (KEY_LEVELS[1] - KEY_LEVELS[0]));
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[0] - Math.round(reduction);
		}
		else
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[1];

		//portalSpawnCountdown = BALL_SPAWN_INTERVAL;
		portalSpawnCountdown = 2000;	// 2s delay before first ball spawn

		// Init dummy unit used to spawn projectiles (Map does not have projectile spawning capabilities)
		spawner = new AIUnit(0, 0, 0, 0) {
			@Override public AIUnit copy() {
				return null;
			}
			@Override public void hit(HitBundle bundle) { }
		};
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 10000;
		baseStats.moveSpeed = 0;
		spawner.setBaseStats(baseStats);

		spawner.offsetTo(1000, 1000);
		queueGregsUnit(spawner);

		goalies = new ArrayList<PewBallGoalie>(2);
		if (levelNum < KEY_LEVELS[0])
			goalies.add(new PewBallGoalie(levelNum, Screen.MIDY));
		else
		{
			float oneHeight = (Screen.HEIGHT - (2 * BUMPER_HEIGHT)) / 2;
			goalies.add(new PewBallGoalie(levelNum, BUMPER_HEIGHT + (oneHeight / 2)));
			goalies.add(new PewBallGoalie(levelNum, BUMPER_HEIGHT + (oneHeight * 1.5f)));
		}

		for (PewBallGoalie pbg : goalies)
			queueGregsUnit(pbg);

		orderedBalls = new ArrayList<PewBall>();

		/*PewBall wallBall = new PewBall(levelNum);
		wallBall.setVelocity(0);
		wallBall.offsetTo(550, 240);
		wallBall.setDestination(100, 240);
		spawner.queueProjectile(wallBall);*/
	}

	public void attachScreen(PewBallScreen pewBallScreen)
	{
		this.pewBallScreen = pewBallScreen;
	}

	public void startBallSpawn()
	{
		// Case: ball spawn has already started
		if (ballSpawnDelay > 0)
			return;

		portalSpawnCountdown = portalSpawnInterval;

		/*if (levelNum < KEY_LEVELS[0])
			portalSpawnCountdown = BASE_SPAWN_INTERVAL;
		else
		{
			int reduction = SPAWN_REDUCTION_PER_LEVEL * (levelNum - KEY_LEVELS[0]);
			portalSpawnCountdown = BASE_SPAWN_INTERVAL - reduction;
			if (portalSpawnCountdown < MIN_SPAWN_INTERVAL)
				portalSpawnCountdown = MIN_SPAWN_INTERVAL;
		}*/

		ballSpawnDelay = BALL_SPAWN_DELAY;

		for (Point p : PORTAL_LOCATIONS)
		{
			SpriteAnimation anim = new SpriteAnimation(PORTAL_ANIM);
			anim.offsetTo(p);
			GameScreen.playAnimation(anim);
		}
	}

	private void spawnBall()
	{
		if (levelNum < KEY_LEVELS[0])
		{
			int index = Util.rand.nextInt(PORTAL_LOCATIONS.length);
			createBall(index);
		}
		else
		{
			int index = Util.rand.nextInt(PORTAL_LOCATIONS.length);

			for (int i=0; i<PORTAL_LOCATIONS.length; i++)
			{
				if (i != index)
					createBall(i);
			}
		}
	}

	private void createBall(int portalLocationIndex)
	{
		/*PewBall ball = new PewBall(levelNum);
		ball.offsetTo(650, 240);

		// Give the ball a random initial destination
		Line startDest = new Line(ball.getX(), ball.getY(), ball.getX() - 10, ball.getY());
		ball.setDestination(startDest.getExtEnd());

		balls.add(ball);
		spawner.queueProjectile(ball);*/

		PewBall ball = new PewBall(levelNum);
		ball.offsetTo(PORTAL_LOCATIONS[portalLocationIndex]);

		// Give the ball a random initial destination
		Line startDest = new Line(ball.getX(), ball.getY(), ball.getX() + 10, ball.getY());
		float angle = (float)(Math.PI * 3 / 4) + (Util.rand.nextFloat() * (float)Math.PI / 2);
		Point.rotate(startDest.point1, angle, startDest.point0.x, startDest.point0.y);
		ball.setDestination(startDest.getExtEnd());

		balls.add(ball);
		spawner.queueProjectile(ball);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		// Spawn the next ball if appropriate
		if (ballSpawnDelay > 0)
		{
			ballSpawnDelay -= deltaTime;
			if (ballSpawnDelay <= 0)
				spawnBall();
		}
		else
		{
			portalSpawnCountdown -= deltaTime;
			if (portalSpawnCountdown <= 0)
			{
				// Note: portalSpawnCountdown = BALL_SPAWN_INTERVAL takes place in startBallSpawn()
				startBallSpawn();
			}
		}

		// Decide which ball each goalie is tracking
		manageGoalies();

		// Spawn the next goalie if appropriate
		/*goalieSpawnInterval -= deltaTime;
		if (goalieSpawnInterval <= 0)
		{
			goalieSpawnInterval += GOALIE_INTERVAL[0] + (Util.rand.nextFloat() * (GOALIE_INTERVAL[1] - GOALIE_INTERVAL[0]));
			spawner.queueProjectile(new PewBallGoalieOld());
		}*/

		Iterator<PewBall> itr = balls.iterator();
		while (itr.hasNext())
		{
			PewBall ball = itr.next();

			// If ball is off the screen, notify pewBallScreen about who gets the point
			if (!Screen.overlaps(ball.getHitbox()))
			{
				itr.remove();
				if (ball.getX() < Screen.MIDX)
					pewBallScreen.awayGoal();
				else
					pewBallScreen.homeGoal();
			}
		}
	}

	private void manageGoalies()
	{
		if (balls.isEmpty())
		{
			for (PewBallGoalie pbg : goalies)
				pbg.track(null);
		}
		else
		{
			Collections.sort(balls);
			ArrayList<PewBallGoalie> goalieCopy = new ArrayList<PewBallGoalie>(goalies);

			int size = Math.min(balls.size(), goalieCopy.size());
			for (int i=0; i<size; i++)
			{
				PewBall ball = balls.get(i);
				int goalieIndex = 0;
				float minDist = Float.MAX_VALUE;
				for (int j=0; j<goalieCopy.size(); j++)
				{
					float thisDist = goalieCopy.get(j).distanceFromRange(ball.getY());
					if (thisDist < minDist)
					{
						minDist = thisDist;
						goalieIndex = j;
					}
				}

				goalieCopy.get(goalieIndex).track(ball);
				goalieCopy.remove(goalieIndex);
			}

			// If there are any goalies left, have them track balls.get(0)
			if (!balls.isEmpty())
			{
				for (PewBallGoalie pbg : goalieCopy)
				{
					pbg.track(balls.get(0));
				}
			}
		}
	}

	public int getScoreLimit()
	{
		if (levelNum < KEY_LEVELS[0])
			return SCORE_LIMITS[0];
		else
			return SCORE_LIMITS[1];
		/*if (levelNum < KEY_LEVELS[0])
			return SCORE_LIMITS[0];
		else if (levelNum < KEY_LEVELS[1])
			return SCORE_LIMITS[1];
		else
			return SCORE_LIMITS[2];*/
	}

	@Override public void gameScreenEmpty() { }

	public boolean noActiveBalls() { return balls.isEmpty(); }

	public int ballTimer() { return portalSpawnCountdown; }
}