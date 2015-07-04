package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.ChargeSwipeArrow;
import com.lescomber.vestige.Options;
import com.lescomber.vestige.R;
import com.lescomber.vestige.SwipeArrow;
import com.lescomber.vestige.cgl.CGLRenderer;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.framework.PersistentData;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.gestures.GestureHandler;
import com.lescomber.vestige.graphics.ColorRect;
import com.lescomber.vestige.graphics.Image;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.Text.Alignment;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.graphics.UIThreePatchSprite;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.map.Map;
import com.lescomber.vestige.map.Obstacle;
import com.lescomber.vestige.projectiles.AreaEffect;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.FireAnimation;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Player;
import com.lescomber.vestige.units.Unit;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.ButtonGroup;
import com.lescomber.vestige.widgets.CheckBox;
import com.lescomber.vestige.widgets.Slider;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GameScreen extends Screen implements WidgetListener {
	// Screen change values
	int screenChangeQueue;
	static final int NO_SCREEN_CHANGE = 0;
	static final int PAUSE_SCREEN = 1;
	static final int RESTART_LEVEL = 2;
	static final int NEXT_LEVEL_SCREEN = 3;
	static final int MAIN_MENU_SCREEN = 4;
	static final int GAME_OVER_SCREEN = 5;
	static final int CREDITS_SCREEN = 6;

	static final int GAME_STATE = 0;
	static final int PAUSED_STATE = 1;
	static final int GAME_OVER_TRANSITION = 2;
	int state;

	// Paused game fields
	private static final int P_BUTTON_DELAY_MAX = 650;
	private int pButtonDelay;
	Button pButton;
	private ColorRect pCover;
	Text pHeadingText;
	Button pRestartButton;
	Button pMainMenuButton;
	Button pResumeButton;
	Text pMusicVolumeText;
	Slider pMusicVolumeSlider;
	Text pSfxText;
	Slider pSfxSlider;
	Text pDisplayFpsText;
	CheckBox pDisplayFpsBox;
	Text pDisplayWaveText;
	CheckBox pDisplayWaveBox;

	// Game over transition fields
	private static final float ALPHA_PER_MS = 0.001f;    // 0-100% alpha in 1s
	float coverAlpha;
	ColorRect gameOverCover;

	GestureHandler gestureHandler;

	public static Map map;

	// UI related elements
	SwipeArrow swipeArrow;
	ChargeSwipeArrow chargeSwipeArrow;

	private static final float WAVE_LEFT_X = 320;
	private static final float WAVE_FPS_CENTER_X = 420;
	private static final float FPS_RIGHT_X = 520;
	private Text waveText;
	private int waveNum;
	private Text fpsText;    // Frames per second
	private DecimalFormat fpsFormat;
	final UIThreePatchSprite waveTextBackground;
	final UIThreePatchSprite fpsTextBackground;

	// Unit related elements
	public static Player player;
	public static LinkedList<Unit>[] units;
	public static final int gregs = 0;        // units index for good guy greg units
	public static final int steves = 1;        // units index for scumbag steve units

	// Active projectiles / areaEffects
	public static LinkedList<Projectile> projectiles;
	private LinkedList<AreaEffect> areaEffects;
	private LinkedList<Explosion> explosions;
	private static LinkedList<SpriteAnimation> standaloneAnims;

	// Convenience fields
	List<Projectile> newProjectiles;
	List<AreaEffect> newAreaEffects;
	List<Explosion> newExplosions;
	final List<AIUnit> newUnits;

	@SuppressWarnings("unchecked")
	public GameScreen(AndroidGame game) {
		super(game);

		SpriteManager.getInstance().setBackgroundTextureHandle(Assets.backgroundTexture.getTextureHandle());
		SpriteManager.getInstance().setUITextureHandle(Assets.gameUITexture.getTextureHandle());

		screenChangeQueue = NO_SCREEN_CHANGE;

		state = GAME_STATE;

		// Paused game fields
		final Resources res = AndroidGame.res;
		final TextStyle pausedHeadingStyle = TextStyle.headingStyle();
		final TextStyle pausedOptionsStyle = TextStyle.bodyStyleWhite();
		final TextStyle pausedButtonStyle = TextStyle.bodyStyleCyan();

		// Pause button
		pButton = new Button(750, 35, null, null, SpriteManager.pauseButton);
		pButton.setClickSound(null);
		pButton.scaleRect(1.3, 1.3);
		pButton.setListenedEvent(TouchEvent.TOUCH_DOWN);
		pButton.addWidgetListener(this);
		pButton.setVisible(true);

		pCover = new ColorRect(Screen.MIDX, Screen.MIDY, Screen.WIDTH, Screen.HEIGHT, 0, 0, 0, 0.5f);

		pHeadingText = new Text(pausedHeadingStyle, res.getString(R.string.paused), Screen.MIDX, 75, false);

		// Pause menu buttons
		final ButtonGroup pButtonGroup = new ButtonGroup();

		pRestartButton = new Button(Screen.MIDX, 412, 232, 54, pausedButtonStyle, res.getString(R.string.restart));
		pRestartButton.registerGroup(pButtonGroup);
		pRestartButton.addWidgetListener(this);

		pMainMenuButton = new Button(144, 412, 232, 54, pausedButtonStyle, res.getString(R.string.mainMenu));
		pMainMenuButton.registerGroup(pButtonGroup);
		pMainMenuButton.addWidgetListener(this);

		pResumeButton = new Button(656, 412, 232, 54, pausedButtonStyle, res.getString(R.string.resume));
		pResumeButton.registerGroup(pButtonGroup);
		pResumeButton.addWidgetListener(this);

		// Pause menu options
		final float LEFT_COLUMN_X = 220;

		pMusicVolumeText = new Text(pausedOptionsStyle, res.getString(R.string.music), LEFT_COLUMN_X, 172, Alignment.CENTER, false);
		pMusicVolumeSlider = new Slider(460, 174, 368, 30, 0, 100);
		pMusicVolumeSlider.setValue((int) (Options.getMusicVolume() * 100));
		pMusicVolumeSlider.addWidgetListener(this);

		pSfxText = new Text(pausedOptionsStyle, res.getString(R.string.sfx), LEFT_COLUMN_X, 223, Alignment.CENTER, false);
		pSfxSlider = new Slider(460, 223, 368, 30, 0, 100);
		pSfxSlider.setValue((int) (Options.getSfxVolume() * 100));
		pSfxSlider.addWidgetListener(this);

		pDisplayFpsText = new Text(pausedOptionsStyle, res.getString(R.string.displayFpsCounter), 276, 280, Alignment.LEFT, false);
		pDisplayFpsBox = new CheckBox(LEFT_COLUMN_X, 280);
		pDisplayFpsBox.setValue(Options.displayFps);
		pDisplayFpsBox.addWidgetListener(this);

		pDisplayWaveText = new Text(pausedOptionsStyle, res.getString(R.string.displayWaveCounter), 276, 335, Alignment.LEFT, false);
		pDisplayWaveBox = new CheckBox(LEFT_COLUMN_X, 335);
		pDisplayWaveBox.setValue(Options.displayWave);
		pDisplayWaveBox.addWidgetListener(this);

		// Game over transition
		coverAlpha = 0;
		gameOverCover = new ColorRect(Screen.MIDX, Screen.MIDY, Screen.WIDTH, Screen.HEIGHT, 0, 0, 0, coverAlpha);

		gestureHandler = new GestureHandler();

		swipeArrow = new SwipeArrow(gestureHandler);
		chargeSwipeArrow = new ChargeSwipeArrow(gestureHandler);

		units = new LinkedList[2];
		units[0] = new LinkedList<>();
		units[1] = new LinkedList<>();

		projectiles = new LinkedList<>();
		newProjectiles = new LinkedList<>();
		areaEffects = new LinkedList<>();
		newAreaEffects = new LinkedList<>();
		explosions = new LinkedList<>();
		newExplosions = new LinkedList<>();
		newUnits = new LinkedList<>();
		standaloneAnims = new LinkedList<>();

		final TextStyle uiStyle = TextStyle.bodyStyleWhite(25);
		uiStyle.setSpacing(0);
		uiStyle.setAlpha(0.65f);
		waveText = new Text(uiStyle, " ", WAVE_LEFT_X, 35);
		fpsText = new Text(uiStyle, " ", FPS_RIGHT_X, 35);
		waveTextBackground = new UIThreePatchSprite(SpriteManager.uiTextBackgroundPieces, WAVE_LEFT_X, 35);
		waveTextBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		fpsTextBackground = new UIThreePatchSprite(SpriteManager.uiTextBackgroundPieces, FPS_RIGHT_X, 35);
		fpsTextBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		fpsTextBackground.setVisible(Options.displayFps);

		waveNum = 0;

		fpsFormat = new DecimalFormat("#.#");
	}

	public void loadMap(Map map) {
		GameScreen.map = map;

		map.setBackground();

		player = new Player();
		player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
		gestureHandler.addListener(player);
		player.setVisible(true);
		swipeArrow.setPlayer(player);
		chargeSwipeArrow.setPlayer(player);
		units[gregs].add(player);
	}

	@Override
	public void update(int deltaTime) {
		if (state == GAME_STATE)
			updateRunning(deltaTime);
		else if (state == PAUSED_STATE)
			updatePaused(deltaTime);
		else if (state == GAME_OVER_TRANSITION)
			updateGameOverTransition(deltaTime);

		// Process the screen change queue. This is done here at the end of the update method so that this update method does not perform a screen
		//change and then throw errors when control falls back to this update method
		processScreenChangeQueue();
	}

	void updateGameOverTransition(int deltaTime) {
		coverAlpha += (deltaTime * ALPHA_PER_MS);
		if (coverAlpha >= 1) {
			coverAlpha = 1;
			screenChangeQueue = GAME_OVER_SCREEN;
		}

		gameOverCover.setAlpha(coverAlpha);
	}

	private void updatePaused(int deltaTime) {
		pButtonDelay -= deltaTime;

		// Note: no pButton update method because it doesn't have a traditional click animation
		pRestartButton.update(deltaTime);
		pMainMenuButton.update(deltaTime);
		pResumeButton.update(deltaTime);

		final List<TouchEvent> touchEvents = game.getTouchEvents();
		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			pButton.handleEvent(event);
			if (state != PAUSED_STATE)
				return;

			pRestartButton.handleEvent(event);
			pMainMenuButton.handleEvent(event);
			pResumeButton.handleEvent(event);
			pMusicVolumeSlider.handleEvent(event);
			pSfxSlider.handleEvent(event);
			pDisplayFpsBox.handleEvent(event);
			pDisplayWaveBox.handleEvent(event);
		}
	}

	private void updateRunning(int deltaTime) {
		// Retrieve touch events
		final List<TouchEvent> touchEvents = game.getTouchEvents();
		gestureHandler.update(deltaTime);
		final int len = touchEvents.size();

		// Pass touch events to pause button. If pause button has been clicked, discard this set of events and pauseGame()
		for (int i = 0; i < len; i++) {
			pButton.handleEvent(touchEvents.get(i));
			if (state == PAUSED_STATE)
				return;
		}

		// Pass touch events to the gesture handler
		for (int i = 0; i < len; i++)
			gestureHandler.addTouch(touchEvents.get(i));

		swipeArrow.update(deltaTime);
		chargeSwipeArrow.update(deltaTime);

		// Update map
		map.update(deltaTime);

		// Update wave counter
		if (map.getWaveNum() != waveNum)
			updateWaveText();

		// Update FPS display
		updateFpsText();

		// Update and move units
		updateUnits(deltaTime);

		// Update projectiles and retrieve any new areaEffects created by said projectiles
		updateProjectiles(deltaTime);

		// Update areaEffect animations and check their unit collisions
		updateAreaEffects(deltaTime);

		// Update explosion animations and check their unit collisions
		updateExplosions(deltaTime);

		// Inform any dead units that they have died so they may spawn any last units/areaEffects/projectiles on death
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				if (u.getHp() <= 0)
					u.die();
			}
		}

		// Retrieve any new projectiles queue'd up by existing units
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				newProjectiles = u.getProjectileQueue();
				for (final Projectile p : newProjectiles) {
					p.setVisible(true);
					projectiles.add(p);
				}
			}
		}

		// Retrieve any areaEffects queue'd up by existing units
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				newAreaEffects = u.getAreaEffectQueue();
				for (final AreaEffect ae : newAreaEffects) {
					ae.setVisible(true);
					areaEffects.add(ae);
				}
			}
		}

		// Retrieve any new explosions queue'd up by existing units
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				newExplosions = u.getExplosionQueue();
				for (final Explosion e : newExplosions) {
					e.setVisible(true);
					explosions.add(e);
				}
			}
		}

		// Retrieve any new AIUnits spawned by existing units
		newUnits.clear();
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				newUnits.addAll(u.getAIUnitQueue());
				for (final AIUnit aiu : newUnits)
					aiu.setVisible(true);
			}
		}
		for (final AIUnit aiu : newUnits)
			units[aiu.getFaction()].add(aiu);

		// Update standalone animations
		if (!standaloneAnims.isEmpty()) {
			final Iterator<SpriteAnimation> itr = standaloneAnims.iterator();
			while (itr.hasNext()) {
				final SpriteAnimation gsa = itr.next();
				if (gsa.update(deltaTime))
					itr.remove();
			}
		}

		// Remove any dead units
		cleanupDeadUnits();

		// Check for player death
		if (player.getHp() <= 0) {
			state = GAME_OVER_TRANSITION;
			gameOverCover.setVisible(true);
			return;
		}

		// Add any new enemy units from map
		final List<AIUnit> newEnemies = map.getEnemiesQueue();
		for (final AIUnit aiu : newEnemies)
			aiu.setVisible(true);
		units[steves].addAll(newEnemies);

		// Add any new friendly units from map
		final List<AIUnit> newFriends = map.getFriendsQueue();
		for (final AIUnit aiu : newFriends)
			aiu.setVisible(true);
		units[gregs].addAll(newFriends);

		// Check for level completion
		if (map.isLevelComplete(player)) {
			saveProgress();
			if (map.getLevelNum() == Levels.LEVEL_COUNT[0])        // TODO: Support multiple stages
				screenChangeQueue = CREDITS_SCREEN;
			else
				screenChangeQueue = NEXT_LEVEL_SCREEN;
		}
	}

	void updateWaveText() {
		if (!Options.displayWave)
			return;

		if (map.getWaveCount() > 1) {
			waveNum = map.getWaveNum();
			waveText.setText(AndroidGame.res.getString(R.string.wave) + " " + waveNum + " / " + map.getWaveCount());
		}
	}

	void updateFpsText() {
		if (!Options.displayFps)
			return;

		final double curUPS = game.getFps();
		final double fps;
		if (!Double.isInfinite(curUPS))
			fps = Math.min(Double.valueOf(fpsFormat.format(CGLRenderer.getFps())), Double.valueOf(fpsFormat.format(curUPS)));
		else
			fps = Double.valueOf(fpsFormat.format(CGLRenderer.getFps()));

		fpsText.setText(AndroidGame.res.getString(R.string.fps) + ": " + fps);
	}

	/**
	 * Performs a screen change if screenChangeQueue is populated by anything other than NO_SCREEN_CHANGE
	 */
	void processScreenChangeQueue() {
		if (screenChangeQueue != NO_SCREEN_CHANGE) {
			// TODO: are nullify()'s needed here?
			if (screenChangeQueue == PAUSE_SCREEN)
				pauseGame();
			else {
				FireAnimation.fireCount = 0;    // FIXME: ugly hack. Maybe close() all entities before screen change?
				prepScreenChange();

				if (screenChangeQueue == RESTART_LEVEL) {
					game.setScreen(new MapLoadingScreen(game, map.getStageNum(), map.getLevelNum()));
				} else if (screenChangeQueue == NEXT_LEVEL_SCREEN) {
					final int stageNum = map.getStageNum();

					// Case: User just completed last level in the current stage
					if (map.getLevelNum() == Levels.LEVEL_COUNT[stageNum - 1]) {
						// Case: There exists one or more stages to progress to
						//if (stageNum < Levels.STAGE_COUNT)
						//{
						//	stageNum++;
						//	final int newLevelNum = 1;
						//	Preferences.setInt("lastStage", stageNum);
						//	prepScreenChange();
						//	game.setScreen(new MapLoadingScreen(game, stageNum, newLevelNum));
						//	nullify();
						//}

						// Case: User has completed the last level of the last stage. GG
						//else
						//{
						nullify();
						game.setScreen(new CreditsScreen(game));
						//}
					}

					// Case: User has at least one more level to progress to within the current stage
					else {
						final int newLevelNum = map.getLevelNum() + 1;
						game.setScreen(new MapLoadingScreen(game, stageNum, newLevelNum));
						nullify();
					}
				} else if (screenChangeQueue == MAIN_MENU_SCREEN) {
					game.setScreen(new MainMenuScreen(game, false));
				} else if (screenChangeQueue == GAME_OVER_SCREEN) {
					game.setScreen(new GameOverScreen(game, map.getStageNum(), map.getLevelNum()));
					nullify();
				} else if (screenChangeQueue == CREDITS_SCREEN) {
					game.setScreen(new CreditsScreen(game));
					nullify();
				}
			}

			screenChangeQueue = NO_SCREEN_CHANGE;
		}
	}

	void saveProgress() {
		final int curStage = map.getStageNum();
		final int curLevel = map.getLevelNum();

		for (int i = Options.difficulty; i >= Options.EASY; i--) {
			final int stageProgress = PersistentData.getStageProgress(i);
			final int levelProgress = PersistentData.getLevelProgress(i);
			if (stageProgress == curStage && levelProgress == curLevel)
				PersistentData.setLevelProgress(i, levelProgress + 1);
			else
				break;
		}
	}

	private void updateUnits(int deltaTime) {
		for (int i = 0; i < 2; i++) {
			for (final Unit u : units[i]) {
				// Update unit
				u.update(deltaTime);

				// Move unit
				if (u.isDisplacing()) {
					for (final Obstacle o : map.getObstacles()) {
						if (o.overlaps(u)) {
							u.setDestination(null);
							final Point landingSpot = map.adjustDestination(u.getCenter());
							u.offsetTo(landingSpot);
							break;
						}
					}
				}
			}
		}
	}

	private void updateProjectiles(int deltaTime) {
		newProjectiles.clear();
		final Iterator<Projectile> itr = projectiles.iterator();
		while (itr.hasNext()) {
			final Projectile p = itr.next();
			p.update(deltaTime);

			// Retrieve projectiles before potentially removing current projectile from queue
			newProjectiles.addAll(p.getProjectileQueue());
			// Note: newAreaEffects added to areaEffects after while() to avoid concurrent mod exception

			// Retrieve explosions before potentially removing their projectile from queue
			newExplosions = p.getExplosionQueue();
			for (final Explosion e : newExplosions)
				e.setVisible(true);
			explosions.addAll(newExplosions);

			// Retrieve areaEffects before potentially removing their projectile from queue
			newAreaEffects = p.getAreaEffectQueue();
			for (final AreaEffect ae : newAreaEffects)
				ae.setVisible(true);
			areaEffects.addAll(newAreaEffects);

			if (p.isFinished()) {
				p.close();
				itr.remove();
			}
		}

		for (final Projectile p : newProjectiles) {
			p.setVisible(true);
			projectiles.add(p);
		}
	}

	private void updateAreaEffects(int deltaTime) {
		newAreaEffects.clear();
		final Iterator<AreaEffect> itr = areaEffects.iterator();
		while (itr.hasNext()) {
			final AreaEffect ae = itr.next();
			ae.update(deltaTime);

			// Retrieve any AreaEffects queue'd up by current AreaEffect before potentially removing it from the queue
			newAreaEffects.addAll(ae.getAreaEffectQueue());
			// Note: newAreaEffects added to areaEffects after while() to avoid concurrent mod exception

			if (ae.isFinished()) {
				ae.close();
				itr.remove();
			}
		}

		for (final AreaEffect ae : newAreaEffects) {
			ae.setVisible(true);
			areaEffects.add(ae);
		}
	}

	private void updateExplosions(int deltaTime) {
		final Iterator<Explosion> itr = explosions.iterator();
		while (itr.hasNext()) {
			final Explosion e = itr.next();
			e.update(deltaTime);
			if (e.isFinished()) {
				e.close();
				itr.remove();
			}
		}
	}

	private void cleanupDeadUnits() {
		for (int i = 0; i < 2; i++) {
			final Iterator<Unit> itr = units[i].iterator();
			while (itr.hasNext()) {
				final Unit u = itr.next();
				if (u.getHp() <= 0) {
					u.close();
					itr.remove();

					// If we have removed the last main steve, inform map
					if (wasLastMainUnit())
						map.gameScreenEmpty();
				}
			}
		}
	}

	private boolean wasLastMainUnit() {
		for (final Unit u : units[steves]) {
			if (u.isMainUnit())
				return false;
		}

		return true;
	}

	public static void playAnimation(SpriteAnimation anim) {
		anim.setVisible(true);
		anim.play();
		standaloneAnims.add(anim);
	}

	public static void playAnimation(SpriteAnimation anim, Image previousImage) {
		Swapper.swapImages(previousImage, anim);
		standaloneAnims.add(anim);
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		final Object source = we.getSource();

		if (source == pButton) {
			if (state == GAME_STATE)
				pauseGame();
			else if (state == PAUSED_STATE && pButtonDelay <= 0)
				unpauseGame();
		} else if (source == pRestartButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				screenChangeQueue = RESTART_LEVEL;
		} else if (source == pMainMenuButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				screenChangeQueue = MAIN_MENU_SCREEN;
		} else if (source == pResumeButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				unpauseGame();
		} else if (source == pMusicVolumeSlider) {
			Options.setMusicVolume((float) pMusicVolumeSlider.getValue() / 100);
		} else if (source == pSfxSlider) {
			Options.setSfxVolume((float) pSfxSlider.getValue() / 100);
		} else if (source == pDisplayFpsBox) {
			Options.displayFps = pDisplayFpsBox.isChecked();
			PersistentData.setFpsDisplayed(Options.displayFps);
		} else if (source == pDisplayWaveBox) {
			Options.displayWave = pDisplayWaveBox.isChecked();
			PersistentData.setWaveDisplayed(Options.displayWave);
		}
	}

	void pauseGame() {
		AudioManager.pauseMusicEffects();

		state = PAUSED_STATE;

		waveTextBackground.setVisible(false);
		fpsTextBackground.setVisible(false);

		pButtonDelay = P_BUTTON_DELAY_MAX;
		pButton.setImage(SpriteManager.pauseButtonClick);
		pButton.setListenedEvent(TouchEvent.TOUCH_UP);

		pCover.setVisible(true);

		// Disable UI during pause
		fpsText.setVisible(false);
		waveText.setVisible(false);
		player.setCDIndicatorsVisible(false);

		pHeadingText.setVisible(true);
		pRestartButton.setVisible(true);
		pMainMenuButton.setVisible(true);
		pResumeButton.setVisible(true);
		pMusicVolumeText.setVisible(true);
		pMusicVolumeSlider.setVisible(true);
		pSfxText.setVisible(true);
		pSfxSlider.setVisible(true);
		pDisplayFpsText.setVisible(true);
		pDisplayFpsBox.setVisible(true);
		pDisplayWaveText.setVisible(true);
		pDisplayWaveBox.setVisible(true);
	}

	void unpauseGame() {
		AudioManager.resumeMusicEffects();

		state = GAME_STATE;

		positionUIText();

		if (Options.displayWave) {
			updateWaveText();
			waveTextBackground.setVisible(map.getWaveCount() > 1);
		} else
			waveText.setText(" ");

		if (Options.displayFps)
			fpsTextBackground.setVisible(true);
		else
			fpsText.setText(" ");

		pButton.setImage(SpriteManager.pauseButton);
		pButton.setListenedEvent(TouchEvent.TOUCH_DOWN);

		pCover.setVisible(false);

		// Re-enable UI
		fpsText.setVisible(true);
		waveText.setVisible(true);
		player.setCDIndicatorsVisible(true);

		pHeadingText.setVisible(false);
		pRestartButton.setVisible(false);
		pMainMenuButton.setVisible(false);
		pResumeButton.setVisible(false);
		pMusicVolumeText.setVisible(false);
		pMusicVolumeSlider.setVisible(false);
		pSfxText.setVisible(false);
		pSfxSlider.setVisible(false);
		pDisplayFpsText.setVisible(false);
		pDisplayFpsBox.setVisible(false);
		pDisplayWaveText.setVisible(false);
		pDisplayWaveBox.setVisible(false);
	}

	private void positionUIText() {
		if (Options.displayWave && map.getWaveCount() > 1) {
			if (Options.displayFps) {
				waveText.offsetTo(WAVE_LEFT_X, waveText.getY());
				waveTextBackground.offsetTo(WAVE_LEFT_X, waveTextBackground.getY());
				fpsText.offsetTo(FPS_RIGHT_X, fpsText.getY());
				fpsTextBackground.offsetTo(FPS_RIGHT_X, fpsTextBackground.getY());
			} else {
				waveText.offsetTo(WAVE_FPS_CENTER_X, waveText.getY());
				waveTextBackground.offsetTo(WAVE_FPS_CENTER_X, waveTextBackground.getY());
			}
		} else {
			fpsText.offsetTo(WAVE_FPS_CENTER_X, fpsText.getY());
			fpsTextBackground.offsetTo(WAVE_FPS_CENTER_X, fpsTextBackground.getY());
		}
	}

	@Override
	public void pause() {
		if (!Screen.isScreenChanging())
			pauseGame();
	}

	@Override
	public void resume() {
		firstRun();

		if (map.isBossLevel())
			AudioManager.playMusic(AudioManager.BOSS_MUSIC);
		else
			AudioManager.playMusic(AudioManager.GAME_MUSIC);
	}

	void firstRun() {
		// Should only be true when the level first launches. After that, any resume() will be preceeded by a pause() which will set the state to
		//PAUSED_STATE. Here, we use this to set the waveTextBackground visible only when the level is first loaded (but only if we have > 1 wave
		//this level)
		if (state == GAME_STATE) {
			positionUIText();

			if (Options.displayWave)
				waveTextBackground.setVisible(map.getWaveCount() > 1);
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void backButton() {
		if (state == GAME_STATE && screenChangeQueue == NO_SCREEN_CHANGE)
			screenChangeQueue = PAUSE_SCREEN;
		else if (state == PAUSED_STATE && screenChangeQueue == NO_SCREEN_CHANGE)
			pResumeButton.click();
	}

	// TODO: when exactly does nullify needs to be called?

	/**
	 * Clear GameScreen variables to avoid memory leaks. They will be re-created when the game is restarted
	 */
	void nullify() {
		pButton = null;
		pCover = null;
		pHeadingText = null;
		pRestartButton = null;
		pMainMenuButton = null;
		pResumeButton = null;
		pMusicVolumeText = null;
		pMusicVolumeSlider = null;
		pSfxText = null;
		pSfxSlider = null;
		pDisplayFpsText = null;
		pDisplayFpsBox = null;
		pDisplayWaveText = null;
		pDisplayWaveBox = null;
		gameOverCover = null;
		gestureHandler = null;
		map = null;
		swipeArrow = null;
		chargeSwipeArrow = null;
		waveText = null;
		fpsText = null;
		fpsFormat = null;
		player = null;
		units = null;
		projectiles = null;
		areaEffects = null;
		explosions = null;
		standaloneAnims = null;

		System.gc();
	}
}