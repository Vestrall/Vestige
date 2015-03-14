package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.graphics.UISprite;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.map.Map;
import com.lescomber.vestige.map.PewBallMap;
import com.lescomber.vestige.units.PewBallPlayer;
import com.lescomber.vestige.units.Player;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.ButtonGroup;
import com.lescomber.vestige.widgets.WidgetEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PewBallScreen extends GameScreen
{
	private static final int PEW_BALL_WARNING_STATE = 200;

	private int homeScore;
	private int awayScore;

	private DecimalFormat timerFormat;

	private UISprite scoreBackground;
	private UISprite timerBackground;
	private UISprite levelBackground;
	private Text scoreText;
	private Text timerText;
	private Text levelText;

	// Victory/Defeat result display
	private static final int RESULT_TEXT_DELAY = 700;
	private static final float RESULT_COVER_ALPHA_PER_MS = 0.6f / RESULT_TEXT_DELAY;
	private static final int RESULT_TEXT_TIME = 2500;
	private int resultTextDelay;
	private Text victoryHeading;
	private Text victoryBody;
	private Text defeatHeading;
	private Text defeatBody;

	// Auto-concede warning
	private static final int CHARS_PER_LINE = 48;
	private static final int WARNING_TOP_Y = 160;
	private static final int LINE_SPACING = 45;
	private Text concedeHeading;
	private ArrayList<Text> concedeWarning;
	private ArrayList<Text> mainMenuWarning;
	private Button confirmConcedeButton;
	private Button confirmMainMenuButton;
	private Button cancelButton;

	public PewBallScreen(AndroidGame game)
	{
		super(game);

		homeScore = 0;
		awayScore = 0;

		final Resources res = AndroidGame.res;
		final TextStyle uiStyle = new TextStyle("BLANCH_CAPS.otf", 25, 255, 255, 255, 0.65f);

		timerFormat = new DecimalFormat("#0.0");
		timerText = new Text(uiStyle, " ", Screen.MIDX - 145, 35);
		timerBackground = new UISprite(SpriteManager.uiTextBackground, Screen.MIDX - 145, 35);
		timerBackground.scaleWidthTo(80);
		timerBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		timerBackground.setVisible(true);

		scoreText = new Text(uiStyle, " ", Screen.MIDX, 35);
		scoreBackground = new UISprite(SpriteManager.uiTextBackground, Screen.MIDX, 35);
		scoreBackground.scaleWidthTo(90);
		scoreBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		scoreBackground.setVisible(true);

		levelText = new Text(uiStyle, res.getString(R.string.pewBallLevel) + ": " + PewBallPrepScreen.currentLevel, Screen.MIDX + 185, 35);
		levelBackground = new UISprite(SpriteManager.uiTextBackground, Screen.MIDX + 185, 35);
		levelBackground.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		levelBackground.setVisible(true);

		resultTextDelay = RESULT_TEXT_DELAY;
		final TextStyle resultHeadingStyle = new TextStyle("BLANCH_CAPS.otf", 80, 255, 255, 255);
		final TextStyle resultBodyStyle = new TextStyle("BLANCH_CAPS.otf", 57, 255, 255, 255);
		victoryHeading = new Text(resultHeadingStyle, res.getString(R.string.pewBallVictoryHeading), Screen.MIDX, 160, false);
		victoryBody = new Text(resultBodyStyle, res.getString(R.string.pewBallVictoryBody), Screen.MIDX, 310, false);
		defeatHeading = new Text(resultHeadingStyle, res.getString(R.string.pewBallDefeatHeading), Screen.MIDX, 160, false);
		defeatBody = new Text(resultBodyStyle, res.getString(R.string.pewBallDefeatBody), Screen.MIDX, 310, false);

		// Change restart button into a concede button
		pRestartButton.setText(AndroidGame.res.getString(R.string.pewBallConcede));

		final TextStyle warningHeadingStyle = new TextStyle("Tommaso.otf", 83, 87, 233, 255);
		warningHeadingStyle.setSpacing(2.5f);

		concedeHeading = new Text(warningHeadingStyle, res.getString(R.string.pewBallWarning), Screen.MIDX, 75, false);
		concedeWarning = new ArrayList<Text>(5);
		mainMenuWarning = new ArrayList<Text>(5);

		// Format concede warning text
		ArrayList<String> lines = new ArrayList<String>(5);
		String remaining = res.getString(R.string.pewBallConcedeBody);
		while (remaining.length() > CHARS_PER_LINE)
		{
			// Find the last space before charsPerLine (i.e. locate where the line break should happen)
			int spaceIndex = CHARS_PER_LINE + 1;
			for (int i=CHARS_PER_LINE; remaining.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lines.add(remaining.substring(0, spaceIndex));
			remaining = remaining.substring(spaceIndex + 1);
		}
		lines.add(remaining);	// Add the last line

		// Store concede warning text
		for (int i=0; i<lines.size(); i++)
			concedeWarning.add(new Text(resultBodyStyle, lines.get(i), Screen.MIDX, WARNING_TOP_Y + (i * LINE_SPACING), false));

		// Format main menu warning text
		lines.clear();
		remaining = res.getString(R.string.pewBallMainMenuBody);
		while (remaining.length() > CHARS_PER_LINE)
		{
			// Find the last space before charsPerLine (i.e. locate where the line break should happen)
			int spaceIndex = CHARS_PER_LINE + 1;
			for (int i=CHARS_PER_LINE; remaining.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lines.add(remaining.substring(0, spaceIndex));
			remaining = remaining.substring(spaceIndex + 1);
		}
		lines.add(remaining);	// Add the last line

		// Store main menu warning text
		for (int i=0; i<lines.size(); i++)
			mainMenuWarning.add(new Text(resultBodyStyle, lines.get(i), Screen.MIDX, WARNING_TOP_Y + (i * LINE_SPACING), false));

		final ButtonGroup buttonGroup = new ButtonGroup();
		final TextStyle buttonStyle = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255);
		buttonStyle.setSpacing(2.5f);

		confirmConcedeButton = new Button(225, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallConfirmConcede), SpriteManager.menuButtonPieces);
		confirmConcedeButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
		confirmConcedeButton.addWidgetListener(this);
		confirmConcedeButton.registerGroup(buttonGroup);

		confirmMainMenuButton = new Button(225, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallConfirmMainMenu), SpriteManager.menuButtonPieces);
		confirmMainMenuButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
		confirmMainMenuButton.addWidgetListener(this);
		confirmMainMenuButton.registerGroup(buttonGroup);

		cancelButton = new Button(575, 380, 300, 60, buttonStyle, AndroidGame.res.getString(R.string.pewBallCancel), SpriteManager.menuButtonPieces);
		cancelButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
		cancelButton.addWidgetListener(this);
		cancelButton.registerGroup(buttonGroup);
	}

	private void updateScore()
	{
		int scoreLimit = ((PewBallMap)map).getScoreLimit();
		int scoreMin = Math.min(awayScore, homeScore);
		if (scoreLimit - scoreMin <= 1)
			scoreLimit = scoreMin + 2;

		scoreText.setText(homeScore + "  (" + scoreLimit + ")  " + awayScore);
	}

	private void updateTimer()
	{
		if (map != null)
		{
			String time = timerFormat.format(((PewBallMap)map).ballTimer() / 1000.0);
			timerText.setText(time);
		}
	}

	public void homeGoal()
	{
		homeScore++;
		updateScore();

		if (homeScore >= ((PewBallMap)map).getScoreLimit() && homeScore - awayScore > 1)
			victory();
		else
		{
			if (!isConcedeTerritory())
				PewBallPrepScreen.gameInProgress(false);

			if (((PewBallMap)map).noActiveBalls())
				((PewBallMap)map).startBallSpawn();
		}
	}

	public void awayGoal()
	{
		awayScore++;
		updateScore();

		if (awayScore >= ((PewBallMap)map).getScoreLimit() && awayScore - homeScore > 1)
			defeat();
		else
		{
			if (isConcedeTerritory())
				PewBallPrepScreen.gameInProgress(true);

			if (((PewBallMap)map).noActiveBalls())
				((PewBallMap)map).startBallSpawn();
		}
	}

	private void victory()
	{
		PewBallPrepScreen.gameInProgress(false);
		PewBallPrepScreen.victory();

		gameOverCover.setVisible(true);
		state = GAME_OVER_TRANSITION;
	}

	private void defeat()
	{
		PewBallPrepScreen.gameInProgress(false);
		PewBallPrepScreen.defeat();

		gameOverCover.setVisible(true);
		state = GAME_OVER_TRANSITION;
	}

	@Override
	public void update(int deltaTime)
	{
		if (state == PEW_BALL_WARNING_STATE)
		{
			confirmConcedeButton.update(deltaTime);
			confirmMainMenuButton.update(deltaTime);
			cancelButton.update(deltaTime);

			final List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
			final int len = touchEvents.size();
			for (int i=0; i<len; i++)
			{
				final Input.TouchEvent event = touchEvents.get(i);

				confirmConcedeButton.handleEvent(event);
				confirmMainMenuButton.handleEvent(event);
				cancelButton.handleEvent(event);
			}
		}
		else
		{
			updateTimer();

			super.update(deltaTime);
		}
	}

	// Note: GameOverTransition is overridden to display victory/defeat message prior to next/previous level loading screen
	@Override
	void updateGameOverTransition(int deltaTime)
	{
		if (!victoryHeading.isVisible() && !defeatHeading.isVisible())
		{
			resultTextDelay -= deltaTime;
			coverAlpha += RESULT_COVER_ALPHA_PER_MS * deltaTime;
			gameOverCover.setAlpha(coverAlpha);
			if (resultTextDelay <= 0)
			{
				resultTextDelay = RESULT_TEXT_TIME;
				if (homeScore > awayScore)
				{
					victoryHeading.setVisible(true);
					victoryBody.setVisible(true);
				}
				else
				{
					defeatHeading.setVisible(true);
					defeatBody.setVisible(true);
				}
			}
		}
		else
		{
			resultTextDelay -= deltaTime;
			if (resultTextDelay <= 0)
			{
				if (homeScore > awayScore)
					screenChangeQueue = NEXT_LEVEL_SCREEN;
				else
					screenChangeQueue = GAME_OVER_SCREEN;
			}
		}
	}

	@Override
	void processScreenChangeQueue()
	{
		if (screenChangeQueue == NEXT_LEVEL_SCREEN || screenChangeQueue == GAME_OVER_SCREEN)
		{
			prepScreenChange();
			game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, PewBallPrepScreen.currentLevel));
		}
		else
			super.processScreenChangeQueue();
	}

	@Override
	public void loadMap(Map map)
	{
		GameScreen.map = map;

		map.setBackground();
		((PewBallMap)map).attachScreen(this);
		updateTimer();
		updateScore();

		//player = new Player();
		player = new PewBallPlayer();
		player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
		gestureHandler.addListener(player);
		player.setVisible(true);
		swipeArrow.setPlayer(player);
		chargeSwipeArrow.setPlayer(player);
		units[gregs].add(player);
	}

	@Override
	void pauseGame()
	{
		super.pauseGame();

		// No wave/fps text for PewBallMap
		pDisplayFPSText.setVisible(false);
		pDisplayFPSBox.setVisible(false);
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
	void unpauseGame()
	{
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
	public void widgetEvent(WidgetEvent we)
	{
		Object source = we.getSource();

		if (source == confirmConcedeButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				state = PAUSED_STATE;
				defeat();
				screenChangeQueue = GAME_OVER_SCREEN;
			}
		}
		else if (source == confirmMainMenuButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				state = PAUSED_STATE;
				defeat();
				screenChangeQueue = MAIN_MENU_SCREEN;
			}
		}
		else if (source == cancelButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				hideWarning();
		}
		else if (source == pMainMenuButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				if (isConcedeTerritory())
					displayMainMenuWarning();
				else
					screenChangeQueue = MAIN_MENU_SCREEN;
			}
		}
		else if (source == pRestartButton)	// Concede button
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				displayConcedeWarning();
				//if (isConcedeTerritory())
				//	displayConcedeWarning();
				//else
				//	screenChangeQueue = RESTART_LEVEL;
			}
		}
		else
			super.widgetEvent(we);
	}

	// Returns true if leaving the game now will result in an auto-concede
	private boolean isConcedeTerritory()
	{
		// Can't go any lower than level 1
		if (PewBallPrepScreen.currentLevel <= 1)
			return false;

		return (awayScore > homeScore) || (awayScore == homeScore && awayScore >= ((PewBallMap)map).getScoreLimit() - 1);
	}

	private void displayConcedeWarning()
	{
		state = PEW_BALL_WARNING_STATE;

		pauseUIVisible(false);

		concedeHeading.setVisible(true);
		for (Text t : concedeWarning)
			t.setVisible(true);
		confirmConcedeButton.setVisible(true);
		cancelButton.setVisible(true);
	}

	private void displayMainMenuWarning()
	{
		state = PEW_BALL_WARNING_STATE;

		pauseUIVisible(false);

		concedeHeading.setVisible(true);
		for (Text t : mainMenuWarning)
			t.setVisible(true);
		confirmMainMenuButton.setVisible(true);
		cancelButton.setVisible(true);
	}

	private void hideWarning()
	{
		state = PAUSED_STATE;

		concedeHeading.setVisible(false);
		for (Text t : concedeWarning)
			t.setVisible(false);
		for (Text t : mainMenuWarning)
			t.setVisible(false);
		confirmConcedeButton.setVisible(false);
		confirmMainMenuButton.setVisible(false);
		cancelButton.setVisible(false);

		pauseUIVisible(true);
	}

	// Keeps pause cover up but hides everything else
	private void pauseUIVisible(boolean isVisible)
	{
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
	void firstRun()
	{
		super.firstRun();

		fpsTextBackground.setVisible(false);
	}

	@Override void updateFPSText() { }
}