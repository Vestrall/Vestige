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

public class PewBallMap extends Map {
	public static final float BUMPER_HEIGHT = 35;    // Height of the walls at the top and bottom of the map

	private static final int[] SCORE_LIMITS = new int[] { 5, 10 };

	private PewBallScreen pewBallScreen;

	private static final Point[] PORTAL_LOCATIONS = { new Point(590, 120), new Point(650, 240), new Point(590, 360) };
	private final SpriteAnimation PORTAL_ANIM;
	private final int BALL_SPAWN_DELAY;
	private int ballSpawnDelay;

	// Difficulty scaling mechanics change at these levels (e.g. # of balls spawning at once)
	public static final int[] KEY_LEVELS = new int[] { 10, 20 };

	// Interval drops from KEY_LEVELS[0] to KEY_LEVELS[1]
	private static final int PORTAL_SPAWN_INTERVAL[] = new int[] { 30000, 20000 };
	private final int portalSpawnInterval;
	private int portalSpawnCountdown;

	private final AIUnit spawner;    // Used to spawn projectiles (since Map has no mechanism in place to spawn them directly)

	private final ArrayList<PewBall> balls;        // Keep track of balls. Used for managing goalies and tracking points scored

	private final ArrayList<PewBallGoalie> goalies;

	private final int levelNum;

	public PewBallMap(int levelNum) {
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

		// Calculate portal spawn interval
		if (levelNum <= KEY_LEVELS[0])
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[0];
		else if (levelNum < KEY_LEVELS[1]) {
			final int totalDif = PORTAL_SPAWN_INTERVAL[0] - PORTAL_SPAWN_INTERVAL[1];
			final float reduction = totalDif * (((float) levelNum - KEY_LEVELS[0]) / (KEY_LEVELS[1] - KEY_LEVELS[0]));
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[0] - Math.round(reduction);
		} else
			portalSpawnInterval = PORTAL_SPAWN_INTERVAL[1];

		portalSpawnCountdown = 1500;    // 1.5s delay before first ball spawn

		// Init dummy unit used to spawn projectiles (Map does not have projectile spawning capabilities)
		spawner = new AIUnit(0, 0, 0, 0) {
			@Override
			public AIUnit copy() {
				return null;
			}

			@Override
			public void hit(HitBundle bundle) {
			}
		};
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 10000;
		baseStats.moveSpeed = 0;
		spawner.setBaseStats(baseStats);
		spawner.offsetTo(1000, 1000);
		queueGregsUnit(spawner);

		// Init goalies
		goalies = new ArrayList<PewBallGoalie>(2);
		if (levelNum < KEY_LEVELS[0])
			goalies.add(new PewBallGoalie(levelNum, Screen.MIDY));
		else {
			final float oneHeight = (Screen.HEIGHT - (2 * BUMPER_HEIGHT)) / 2;
			goalies.add(new PewBallGoalie(levelNum, BUMPER_HEIGHT + (oneHeight / 2)));
			goalies.add(new PewBallGoalie(levelNum, BUMPER_HEIGHT + (oneHeight * 1.5f)));
		}

		for (PewBallGoalie pbg : goalies)
			queueGregsUnit(pbg);
	}

	public void attachScreen(PewBallScreen pewBallScreen) {
		this.pewBallScreen = pewBallScreen;
	}

	/**
	 * Begins portal spawning animation
	 */
	public void startBallSpawn() {
		// Case: ball spawn has already started
		if (ballSpawnDelay > 0)
			return;

		portalSpawnCountdown = portalSpawnInterval;
		ballSpawnDelay = BALL_SPAWN_DELAY;

		for (Point p : PORTAL_LOCATIONS) {
			final SpriteAnimation anim = new SpriteAnimation(PORTAL_ANIM);
			anim.offsetTo(p);
			GameScreen.playAnimation(anim);
		}
	}

	private void spawnBall() {
		if (levelNum < KEY_LEVELS[0]) {
			final int index = Util.rand.nextInt(PORTAL_LOCATIONS.length);
			createBall(index);
		} else {
			final int index = Util.rand.nextInt(PORTAL_LOCATIONS.length);

			for (int i = 0; i < PORTAL_LOCATIONS.length; i++) {
				if (i != index)
					createBall(i);
			}
		}
	}

	private void createBall(int portalLocationIndex) {
		final PewBall ball = new PewBall(levelNum);
		ball.offsetTo(PORTAL_LOCATIONS[portalLocationIndex]);

		// Give the ball a random initial destination
		final Line startDest = new Line(ball.getX(), ball.getY(), ball.getX() + 10, ball.getY());
		final float angle = (float) (Math.PI * 3 / 4) + (Util.rand.nextFloat() * (float) Math.PI / 2);
		Point.rotate(startDest.point1, angle, startDest.point0.x, startDest.point0.y);
		ball.setDestination(startDest.getExtEnd());

		balls.add(ball);
		spawner.queueProjectile(ball);
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Spawn the next ball if appropriate
		if (ballSpawnDelay > 0) {
			ballSpawnDelay -= deltaTime;
			if (ballSpawnDelay <= 0)
				spawnBall();
		} else {
			portalSpawnCountdown -= deltaTime;
			if (portalSpawnCountdown <= 0)
				startBallSpawn();    // Note: portalSpawnCountdown = BALL_SPAWN_INTERVAL takes place in startBallSpawn()
		}

		// Decide which ball each goalie is tracking
		manageGoalies();

		final Iterator<PewBall> itr = balls.iterator();
		while (itr.hasNext()) {
			final PewBall ball = itr.next();

			// If ball is off the screen, notify pewBallScreen about who gets the point
			if (!Screen.overlaps(ball.getHitbox())) {
				itr.remove();
				if (ball.getX() < Screen.MIDX)
					pewBallScreen.awayGoal();
				else
					pewBallScreen.homeGoal();
			}
		}
	}

	/**
	 * Assigns each goalie to track a ball (if any exist)
	 */
	private void manageGoalies() {
		if (balls.isEmpty()) {
			for (PewBallGoalie pbg : goalies)
				pbg.track(null);
		} else {
			Collections.sort(balls);    // Sort by yPosition
			final ArrayList<PewBallGoalie> goalieCopy = new ArrayList<PewBallGoalie>(goalies);

			final int size = Math.min(balls.size(), goalieCopy.size());
			for (int i = 0; i < size; i++) {
				final PewBall ball = balls.get(i);
				int goalieIndex = 0;
				float minDist = Float.MAX_VALUE;
				for (int j = 0; j < goalieCopy.size(); j++) {
					final float thisDist = goalieCopy.get(j).distanceFromRange(ball.getY());
					if (thisDist < minDist) {
						minDist = thisDist;
						goalieIndex = j;
					}
				}

				goalieCopy.get(goalieIndex).track(ball);
				goalieCopy.remove(goalieIndex);
			}

			// If there are any goalies left, have them track balls.get(0)
			if (!balls.isEmpty()) {
				for (PewBallGoalie pbg : goalieCopy)
					pbg.track(balls.get(0));
			}
		}
	}

	public int getScoreLimit() {
		if (levelNum < KEY_LEVELS[0])
			return SCORE_LIMITS[0];
		else
			return SCORE_LIMITS[1];
	}

	@Override
	public void gameScreenEmpty() {
	}

	public boolean noActiveBalls() {
		return balls.isEmpty();
	}

	public int ballTimer() {
		return portalSpawnCountdown;
	}
}