package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input;
import com.lescomber.vestige.framework.Preferences;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.Slider;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.ArrayList;
import java.util.List;

public class PewBallPrepScreen extends Screen implements WidgetListener
{
	private static final String PEW_BALL_CURRENT_LEVEL_KEY = "pewBallCurrentLevel";
	public static int currentLevel = Preferences.getInt(PEW_BALL_CURRENT_LEVEL_KEY, 1);

	private static final String PEW_BALL_HIGH_SCORE_KEY = "pewBallHighScore";
	public static int highScore = Preferences.getInt(PEW_BALL_HIGH_SCORE_KEY, 0);

	private static final String PEW_BALL_IN_PROGRESS_KEY = "pewBallInProgress";
	static boolean pewBallInProgress = Preferences.getBoolean(PEW_BALL_IN_PROGRESS_KEY, false);

	// Pew Ball description settings
	private static final int DESCRIPTION_CHARS_PER_LINE = 49;
	private static final int WARNING_CHARS_PER_LINE = 32;
	private static final int TEXT_TOP_Y = 130;
	private static final int LINE_SPACING = 45;

	private boolean warningVisible;
	private Text headingText;
	private ArrayList<Text> pewBallDescription;
	private ArrayList<Text> restartWarning;

	private Button restartButton;
	private Button continueButton;
	private Button backButton;

	// testing
	private Slider levelSlider;
	private Text levelText;

	public PewBallPrepScreen(AndroidGame game)
	{
		super(game);

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		// Check for attempted loss-dodge
		if (pewBallInProgress)
		{
			defeat();
			pewBallInProgress = false;
			Preferences.setBoolean(PEW_BALL_IN_PROGRESS_KEY, pewBallInProgress);
		}

		final Resources res = AndroidGame.res;

		final TextStyle headingStyle = new TextStyle("Tommaso.otf", 83, 87, 233, 255);
		final TextStyle descriptionStyle = new TextStyle("BLANCH_CAPS.otf", 57, 255, 255, 255);
		final TextStyle buttonStyle = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255);
		buttonStyle.setSpacing(2.5f);

		headingText = new Text(headingStyle, res.getString(R.string.pewBall), Screen.MIDX, 65);

		// Break pew ball description text onto multiple lines (adhering to DESCRIPTION_CHARS_PER_LINE)
		ArrayList<String> lines = new ArrayList<String>();
		String remaining = res.getString(R.string.pewBallDescription);
		while (remaining.length() > DESCRIPTION_CHARS_PER_LINE)
		{
			// Find the last space before DESCRIPTION_CHARS_PER_LINE (i.e. locate where the line break should happen)
			int spaceIndex = DESCRIPTION_CHARS_PER_LINE + 1;
			for (int i= DESCRIPTION_CHARS_PER_LINE; remaining.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lines.add(remaining.substring(0, spaceIndex));
			remaining = remaining.substring(spaceIndex + 1);
		}
		lines.add(remaining);	// Add the last line

		// Display pew ball description text
		pewBallDescription = new ArrayList<Text>();
		for (int i=0; i<lines.size(); i++)
			pewBallDescription.add(new Text(descriptionStyle, lines.get(i), Screen.MIDX, TEXT_TOP_Y + (i * LINE_SPACING)));

		// Break restart warning text onto multiple lines (adhering to DESCRIPTION_CHARS_PER_LINE)
		lines = new ArrayList<String>();
		remaining = res.getString(R.string.pewBallRestartWarning);
		while (remaining.length() > WARNING_CHARS_PER_LINE)
		{
			// Find the last space before WARNING_CHARS_PER_LINE (i.e. locate where the line break should happen)
			int spaceIndex = WARNING_CHARS_PER_LINE + 1;
			for (int i= WARNING_CHARS_PER_LINE; remaining.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lines.add(remaining.substring(0, spaceIndex));
			remaining = remaining.substring(spaceIndex + 1);
		}
		lines.add(remaining);	// Add the last line

		// Display pew ball description text
		restartWarning = new ArrayList<Text>();
		for (int i=0; i<lines.size(); i++)
			restartWarning.add(new Text(descriptionStyle, lines.get(i), Screen.MIDX, TEXT_TOP_Y + (i * LINE_SPACING), false));

		warningVisible = false;

		if (currentLevel > 1)
		{
			new Text(descriptionStyle, res.getString(R.string.pewBallHighScore) + ": " + highScore, 185, 380);
			new Text(descriptionStyle, res.getString(R.string.pewBallCurrentLevel) + ": " + currentLevel, 545, 380);

			continueButton = new Button(545, 440, 350, 62, buttonStyle, res.getString(R.string.pewBallContinue), SpriteManager.menuButtonPieces);
			continueButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
			continueButton.addWidgetListener(this);
			continueButton.setVisible(true);

			restartButton = new Button(185, 440, 350, 62, buttonStyle, res.getString(R.string.pewBallStartOver), SpriteManager.menuButtonPieces);
			restartButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
			restartButton.addWidgetListener(this);
			restartButton.setVisible(true);
		}
		else
		{
			if (highScore > 0)
				new Text(descriptionStyle, res.getString(R.string.pewBallHighScore) + ": " + highScore, 360, 380);
			restartButton = new Button(360, 440, 710, 62, buttonStyle, res.getString(R.string.pewBallBegin), SpriteManager.menuButtonPieces);
			restartButton.setClickAnimation(SpriteManager.menuButtonClickPieces);
			restartButton.addWidgetListener(this);
			restartButton.setVisible(true);
		}

		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.scaleRect(1.25, 1.25);	// Enlarge "back" button hitbox slightly
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.addWidgetListener(this);
		backButton.setVisible(true);

		//testing
		levelSlider = new Slider(Screen.MIDX, Screen.MIDY, 400, 80, 1, 30);
		levelSlider.setValue(currentLevel);
		levelSlider.addWidgetListener(this);
		levelSlider.setVisible(true);
		levelText = new Text(descriptionStyle, Integer.toString(currentLevel), 50, 50, false);
		levelText.setVisible(true);
	}

	private void displayRestartWarning(boolean warningVisible)
	{
		this.warningVisible = warningVisible;

		for (Text t : pewBallDescription)
			t.setVisible(!warningVisible);

		for (Text t: restartWarning)
			t.setVisible(warningVisible);

		if (warningVisible)
		{
			headingText.setText(AndroidGame.res.getString(R.string.pewBallWarning));
			restartButton.setText(AndroidGame.res.getString(R.string.pewBallRestartConfirm));
			continueButton.setText(AndroidGame.res.getString(R.string.pewBallCancel));
		}
		else
		{
			headingText.setText(AndroidGame.res.getString(R.string.pewBall));
			restartButton.setText(AndroidGame.res.getString(R.string.pewBallStartOver));
			continueButton.setText(AndroidGame.res.getString(R.string.pewBallContinue));
		}
	}

	public static void victory()
	{
		// Update high score (if appropriate)
		if (currentLevel > highScore)
		{
			highScore = currentLevel;
			Preferences.setInt(PewBallPrepScreen.PEW_BALL_HIGH_SCORE_KEY, highScore);
		}

		// Update current level
		currentLevel++;
		Preferences.setInt(PewBallPrepScreen.PEW_BALL_CURRENT_LEVEL_KEY, currentLevel);
	}

	public static void defeat()
	{
		// Update current level
		if (currentLevel > 1)
		{
			currentLevel = Math.max(1, currentLevel - 3);
			Preferences.setInt(PewBallPrepScreen.PEW_BALL_CURRENT_LEVEL_KEY, currentLevel);
		}
	}

	static void gameInProgress(boolean isInProgress)
	{
		pewBallInProgress = isInProgress;
		Preferences.setBoolean(PEW_BALL_IN_PROGRESS_KEY, pewBallInProgress);
	}

	@Override
	public void update(int deltaTime)
	{
		final List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();

		if (continueButton != null)
			continueButton.update(deltaTime);
		restartButton.update(deltaTime);
		backButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i=0; i<len; i++)
		{
			final Input.TouchEvent event = touchEvents.get(i);

			if (continueButton != null)
				continueButton.handleEvent(event);
			restartButton.handleEvent(event);
			backButton.handleEvent(event);

			//testing
			levelSlider.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we)
	{
		final Object source = we.getSource();

		if (continueButton != null && source == continueButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				if (warningVisible)
					displayRestartWarning(false);
				else
				{
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, currentLevel));
				}
			}
		}
		else if (source == restartButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				if (currentLevel == 1 || warningVisible)
				{
					currentLevel = 1;
					Preferences.setInt(PEW_BALL_CURRENT_LEVEL_KEY, currentLevel);
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, currentLevel));
				}
				else
					displayRestartWarning(true);
			}
		}
		else if (source == backButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
			{
				if (!Screen.isScreenChanging())
				{
					prepScreenChange();
					game.setScreen(new MainMenuScreen(game, false));
				}
			}
		}

		//testing
		else if (source == levelSlider)
		{
			currentLevel = levelSlider.getValue();
			Preferences.setInt(PewBallPrepScreen.PEW_BALL_CURRENT_LEVEL_KEY, currentLevel);
			levelText.setText(Integer.toString(currentLevel));
		}
	}

	@Override public void pause() { }
	@Override public void resume() { }
	@Override public void dispose() { }

	@Override
	public void backButton()
	{
		if (!Screen.isScreenChanging())
			backButton.click();
	}
}