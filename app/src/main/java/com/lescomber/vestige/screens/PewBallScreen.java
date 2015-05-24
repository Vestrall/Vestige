package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextArea;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.graphics.UIThreePatchSprite;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.map.Map;
import com.lescomber.vestige.map.PewBallMap;
import com.lescomber.vestige.units.PewBallPlayer;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.ButtonGroup;
import com.lescomber.vestige.widgets.WidgetEvent;

import java.text.DecimalFormat;
import java.util.List;

public class PewBallScreen extends GameScreen {
	private static final int PEW_BALL_WARNING_STATE = 200;

	private int homeScore;
	private int awayScore;

	private final DecimalFormat timerFormat;

	private final UIThreePatchSprite scoreBackground;
	private final UIThreePatchSprite timerBackground;
	private final UIThreePatchSprite levelBackground;
	private final Text scoreText;
	private final Text timerText;
	private final Text levelText;

	// Victory/Defeat result display
	private static final int RESULT_TEXT_DELAY = 700;
	private static final float RESULT_COVER_ALPHA_PER_MS = 0.6f / RESULT_TEXT_DELAY;
	private static final int RESULT_TEXT_TIME = 2500;
	private int resultTextDelay;
	private final Text victoryHeading;
	private final Text victoryBody;
	private final Text defeatHeading;
	private final Text defeatBody;

	// Auto-concede warning
	private static final int CHARS_PER_LINE = 48;
	private static final int WARNING_TOP_Y = 160;
	private static final int LINE_SPACING = 45;
	private final Text concedeHeading;
	private final TextArea concedeWarning;
	private final TextArea mainMenuWarning;
	private final Button confirmConcedeButton;
	private final Button confirmMainMenuButton;
	private final Button cancelButton;

	public PewBallScreen(AndroidGame game) {
		super(game);

		homeScore = 0;
		awayScore = 0;

		final Resources res = AndroidGame.res;
		final TextStyle uiStyle = TextStyle.bodyStyleWhite(25);
		uiStyle.setAlpha(0.65f);

		timerFormat = new DecimalFormat("#0.0");
		timerText = new Text(uiStyle, " ", Screen.MIDX - 145, 35);
		timerBackground = new UIThreePatchSprite(SpriteManager.uiTextBackgroundPieces, Screen.MIDX - 145, 35);
		timerBackground.scaleWidthTo(80);
		timerBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		timerBackground.setVisible(true);

		scoreText = new Text(uiStyle, " ", Screen.MIDX, 35);
		scoreBackground = new UIThreePatchSprite(SpriteManager.uiTextBackgroundPieces, Screen.MIDX, 35);
		scoreBackground.scaleWidthTo(90);
		scoreBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		scoreBackground.setVisible(true);

		levelText = new Text(uiStyle, res.getString(R.string.pewBallLevel) + ": " + PewBallPrepScreen.currentLevel, Screen.MIDX + 185, 35);
		levelBackground = new UIThreePatchSprite(SpriteManager.uiTextBackgroundPieces, Screen.MIDX + 185, 35);
		levelBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		levelBackground.setVisible(true);

		resultTextDelay = RESULT_TEXT_DELAY;
		final TextStyle resultHeadingStyle = TextStyle.bodyStyleWhite(80);
		final TextStyle resultBodyStyle = TextStyle.bodyStyleWhite();
		resultBodyStyle.setSpacing(0);
		victoryHeading = new Text(resultHeadingStyle, res.getString(R.string.pewBallVictoryHeading), Screen.MIDX, 160, false);
		victoryBody = new Text(resultBodyStyle, res.getString(R.string.pewBallVictoryBody), Screen.MIDX, 310, false);
		defeatHeading = new Text(resultHeadingStyle, res.getString(R.string.pewBallDefeatHeading), Screen.MIDX, 160, false);
		defeatBody = new Text(resultBodyStyle, res.getString(R.string.pewBallDefeatBody), Screen.MIDX, 310, false);

		// Change restart button into a concede button
		pRestartButton.setText(AndroidGame.res.getString(R.string.pewBallConcede));

		final TextStyle warningHeadingStyle = TextStyle.headingStyle();

		concedeHeading = new Text(warningHeadingStyle, res.getString(R.string.pewBallWarning), Screen.MIDX, 75, false);
		concedeWarning = new TextArea(Screen.MIDX, WARNING_TOP_Y, CHARS_PER_LINE, LINE_SPACING, resultBodyStyle, res
				.getString(R.string.pewBallConcedeBody), false);
		mainMenuWarning = new TextArea(Screen.MIDX, WARNING_TOP_Y, CHARS_PER_LINE, LINE_SPACING, resultBodyStyle, res
				.getString(R.string.pewBallMainMenuBody, false), false);

		final ButtonGroup buttonGroup = new ButtonGroup();
		final TextStyle buttonStyle = TextStyle.bodyStyleCyan();

		confirmConcedeButton = new Button(225, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallConfirmConcede));
		confirmConcedeButton.addWidgetListener(this);
		confirmConcedeButton.registerGroup(buttonGroup);

		confirmMainMenuButton = new Button(225, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallConfirmMainMenu));
		confirmMainMenuButton.addWidgetListener(this);
		confirmMainMenuButton.registerGroup(buttonGroup);

		cancelButton = new Button(575, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallCancel));
		cancelButton.addWidgetListener(this);
		cancelButton.registerGroup(buttonGroup);
	}

	private void updateScore() {
		int scoreLimit = ((PewBallMap) map).getScoreLimit();
		final int scoreMin = Math.min(awayScore, homeScore);
		if (scoreLimit - scoreMin <= 1)
			scoreLimit = scoreMin + 2;

		scoreText.setText(homeScore + " (" + scoreLimit + ") " + awayScore);
	}

	private void updateTimer() {
		if (map != null) {
			final String time = timerFormat.format(((PewBallMap) map).ballTimer() / 1000.0);
			timerText.setText(time);
		}
	}

	public void homeGoal() {
		homeScore++;
		updateScore();

		if (homeScore >= ((PewBallMap) map).getScoreLimit() && homeScore - awayScore > 1)
			victory();
		else {
			if (!isConcedeTerritory())
				PewBallPrepScreen.setGameInProgress(false);

			if (((PewBallMap) map).noActiveBalls())
				((PewBallMap) map).startBallSpawn();
		}
	}

	public void awayGoal() {
		awayScore++;
		updateScore();

		if (awayScore >= ((PewBallMap) map).getScoreLimit() && awayScore - homeScore > 1)
			defeat();
		else {
			if (isConcedeTerritory())
				PewBallPrepScreen.setGameInProgress(true);

			if (((PewBallMap) map).noActiveBalls())
				((PewBallMap) map).startBallSpawn();
		}
	}

	private void victory() {
		PewBallPrepScreen.setGameInProgress(false);
		PewBallPrepScreen.victory();

		gameOverCover.setVisible(true);
		state = GAME_OVER_TRANSITION;
	}

	private void defeat() {
		PewBallPrepScreen.setGameInProgress(false);
		PewBallPrepScreen.defeat();

		gameOverCover.setVisible(true);
		state = GAME_OVER_TRANSITION;
	}

	@Override
	public void update(int deltaTime) {
		if (state == PEW_BALL_WARNING_STATE) {
			confirmConcedeButton.update(deltaTime);
			confirmMainMenuButton.update(deltaTime);
			cancelButton.update(deltaTime);

			final List<TouchEvent> touchEvents = game.getTouchEvents();
			final int len = touchEvents.size();
			for (int i = 0; i < len; i++) {
				final TouchEvent event = touchEvents.get(i);

				confirmConcedeButton.handleEvent(event);
				confirmMainMenuButton.handleEvent(event);
				cancelButton.handleEvent(event);
			}
		} else {
			updateTimer();

			super.update(deltaTime);
		}
	}

	/**
	 * Note: GameOverTransition is overridden to display victory/defeat message prior to next/previous level loading screen
	 */
	@Override
	void updateGameOverTransition(int deltaTime) {
		if (!victoryHeading.isVisible() && !defeatHeading.isVisible()) {
			resultTextDelay -= deltaTime;
			coverAlpha += RESULT_COVER_ALPHA_PER_MS * deltaTime;
			gameOverCover.setAlpha(coverAlpha);
			if (resultTextDelay <= 0) {
				resultTextDelay = RESULT_TEXT_TIME;
				if (homeScore > awayScore) {
					victoryHeading.setVisible(true);
					victoryBody.setVisible(true);
				} else {
					defeatHeading.setVisible(true);
					defeatBody.setVisible(true);
				}
			}
		} else {
			resultTextDelay -= deltaTime;
			if (resultTextDelay <= 0) {
				if (homeScore > awayScore)
					screenChangeQueue = NEXT_LEVEL_SCREEN;
				else
					screenChangeQueue = GAME_OVER_SCREEN;
			}
		}
	}

	@Override
	void processScreenChangeQueue() {
		if (screenChangeQueue == NEXT_LEVEL_SCREEN || screenChangeQueue == GAME_OVER_SCREEN) {
			prepScreenChange();
			game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, PewBallPrepScreen.currentLevel));
		} else
			super.processScreenChangeQueue();
	}

	@Override
	public void loadMap(Map map) {
		GameScreen.map = map;

		map.setBackground();
		((PewBallMap) map).attachScreen(this);
		updateTimer();
		updateScore();

		player = new PewBallPlayer();
		player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
		gestureHandler.addListener(player);
		player.setVisible(true);
		swipeArrow.setPlayer(player);
		chargeSwipeArrow.setPlayer(player);
		units[gregs].add(player);
	}

	@Override
	void pauseGame() {
		super.pauseGame();

		// No FPS/wave text for PewBallMap so we hide FPS/wave-related options
		pDisplayFpsText.setVisible(false);
		pDisplayFpsBox.setVisible(false);
		pDisplayWaveText.setVisible(false);
		pDisplayWaveBox.setVisible(false);

		// Hide pew ball-specific UI
		timerText.setVisible(false);
		timerBackground.setVisible(false);
		scoreText.setVisible(false);
		scoreBackground.setVisible(false);
		levelText.setVisible(false);
		levelBackground.setVisible(false);
	}

	@Override
	void unpauseGame() {
		super.unpauseGame();

		// Keep fps background invisible (fps text is blank so it doesn't need to be invisible)
		fpsTextBackground.setVisible(false);

		// Show pew ball-specific UI
		timerText.setVisible(true);
		timerBackground.setVisible(true);
		scoreText.setVisible(true);
		scoreBackground.setVisible(true);
		levelText.setVisible(true);
		levelBackground.setVisible(true);
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		final Object source = we.getSource();

		if (source == confirmConcedeButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				state = PAUSED_STATE;
				defeat();
				screenChangeQueue = GAME_OVER_SCREEN;
			}
		} else if (source == confirmMainMenuButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				state = PAUSED_STATE;
				defeat();
				screenChangeQueue = MAIN_MENU_SCREEN;
			}
		} else if (source == cancelButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				hideWarning();
		} else if (source == pMainMenuButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				if (isConcedeTerritory())
					displayMainMenuWarning();
				else
					screenChangeQueue = MAIN_MENU_SCREEN;
			}
		} else if (source == pRestartButton)    // Concede button
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				displayConcedeWarning();
		} else
			super.widgetEvent(we);
	}

	/**
	 * Returns true if leaving the game now will result in an auto-concede
	 */
	private boolean isConcedeTerritory() {
		// Can't go any lower than level 1
		if (PewBallPrepScreen.currentLevel <= 1)
			return false;

		return (awayScore > homeScore) || (awayScore == homeScore && awayScore >= ((PewBallMap) map).getScoreLimit() - 1);
	}

	private void displayConcedeWarning() {
		state = PEW_BALL_WARNING_STATE;

		pauseUIVisible(false);

		concedeHeading.setVisible(true);
		concedeWarning.setVisible(true);
		confirmConcedeButton.setVisible(true);
		cancelButton.setVisible(true);
	}

	private void displayMainMenuWarning() {
		state = PEW_BALL_WARNING_STATE;

		pauseUIVisible(false);

		concedeHeading.setVisible(true);
		mainMenuWarning.setVisible(true);
		confirmMainMenuButton.setVisible(true);
		cancelButton.setVisible(true);
	}

	private void hideWarning() {
		state = PAUSED_STATE;

		concedeHeading.setVisible(false);
		concedeWarning.setVisible(false);
		mainMenuWarning.setVisible(false);
		confirmConcedeButton.setVisible(false);
		confirmMainMenuButton.setVisible(false);
		cancelButton.setVisible(false);

		pauseUIVisible(true);
	}

	/**
	 * Keeps pause cover up but hides/displays everything else
	 */
	private void pauseUIVisible(boolean isVisible) {
		pHeadingText.setVisible(isVisible);
		pMusicVolumeText.setVisible(isVisible);
		pMusicVolumeSlider.setVisible(isVisible);
		pSfxText.setVisible(isVisible);
		pSfxSlider.setVisible(isVisible);
		pMainMenuButton.setVisible(isVisible);
		pRestartButton.setVisible(isVisible);
		pResumeButton.setVisible(isVisible);
		pButton.setVisible(isVisible);
	}

	@Override
	void firstRun() {
		super.firstRun();

		fpsTextBackground.setVisible(false);
	}

	@Override
	void updateFpsText() {
	}
}