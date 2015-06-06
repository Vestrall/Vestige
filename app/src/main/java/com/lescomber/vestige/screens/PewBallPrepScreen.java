package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.PersistentData;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextArea;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.Slider;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class PewBallPrepScreen extends Screen implements WidgetListener {
	public static int currentLevel = PersistentData.getPewBallCurrentLevel();
	public static int highScore = PersistentData.getPewBallHighScore();
	public static boolean pewBallInProgress = PersistentData.isPewBallInProgress();

	// Pew Ball description settings
	private static final int DESCRIPTION_CHARS_PER_LINE = 49;
	private static final int WARNING_CHARS_PER_LINE = 32;
	private static final int TEXT_TOP_Y = 130;
	private static final int LINE_SPACING = 45;

	private boolean warningVisible;
	private final Text headingText;
	private final TextArea pewBallDescription;
	private final TextArea restartWarning;
	private final Text highScoreText;
	private final Text currentLevelText;

	private final Button restartButton;
	private Button continueButton;
	private final Button backButton;

	// testing
	private final Slider levelSlider;
	private final Text levelText;

	public PewBallPrepScreen(AndroidGame game) {
		super(game);

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		// Check for attempted loss-dodge
		if (pewBallInProgress) {
			defeat();
			pewBallInProgress = false;
			PersistentData.setPewBallInProgress(pewBallInProgress);
		}

		final Resources res = AndroidGame.res;

		final TextStyle headingStyle = TextStyle.headingStyle();
		final TextStyle descriptionStyle = TextStyle.bodyStyleWhite();
		descriptionStyle.setSpacing(0);
		final TextStyle buttonStyle = TextStyle.bodyStyleWhite();

		headingText = new Text(headingStyle, res.getString(R.string.pewBall), Screen.MIDX, 65);

		pewBallDescription = new TextArea(Screen.MIDX, TEXT_TOP_Y, DESCRIPTION_CHARS_PER_LINE, LINE_SPACING, descriptionStyle, res
				.getString(R.string.pewBallDescription));

		restartWarning = new TextArea(Screen.MIDX, TEXT_TOP_Y, DESCRIPTION_CHARS_PER_LINE, LINE_SPACING, descriptionStyle, res
				.getString(R.string.pewBallRestartWarning), false);

		warningVisible = false;

		// Case: there is a run in progress
		if (currentLevel > 1) {
			highScoreText = new Text(descriptionStyle, res.getString(R.string.pewBallHighScore) + ": " + highScore, 185, 380);
			currentLevelText = new Text(descriptionStyle, res.getString(R.string.pewBallCurrentLevel) + ": " + currentLevel, 545, 380);

			continueButton = new Button(545, 440, 350, 62, buttonStyle, res.getString(R.string.pewBallContinue));
			continueButton.addWidgetListener(this);
			continueButton.setVisible(true);

			restartButton = new Button(185, 440, 350, 62, buttonStyle, res.getString(R.string.pewBallStartOver));
			restartButton.addWidgetListener(this);
			restartButton.setVisible(true);
		} else {   // Case: No run in progress
			if (highScore > 0)
				highScoreText = new Text(descriptionStyle, res.getString(R.string.pewBallHighScore) + ": " + highScore, 360, 380);
			else
				highScoreText = new Text(descriptionStyle, "", 360, 380);
			currentLevelText = new Text(descriptionStyle, "", 545, 380);
			restartButton = new Button(Screen.MIDX, 440, 275, 62, buttonStyle, res.getString(R.string.pewBallBegin));
			restartButton.addWidgetListener(this);
			restartButton.setVisible(true);
		}

		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.scaleRect(1.25, 1.25);    // Enlarge "back" button hitbox slightly
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.addWidgetListener(this);
		backButton.setVisible(true);

		//testing
		levelSlider = new Slider(Screen.MIDX, Screen.MIDY, 400, 80, 1, 30);
		levelSlider.setValue(currentLevel);
		levelSlider.addWidgetListener(this);
		//levelSlider.setVisible(true);
		levelText = new Text(descriptionStyle, Integer.toString(currentLevel), 50, 50, false);
		//levelText.setVisible(true);
	}

	private void displayRestartWarning(boolean warningVisible) {
		this.warningVisible = warningVisible;

		pewBallDescription.setVisible(!warningVisible);
		restartWarning.setVisible(warningVisible);
		highScoreText.setVisible(!warningVisible);
		currentLevelText.setVisible(!warningVisible);

		if (warningVisible) {
			headingText.setText(AndroidGame.res.getString(R.string.pewBallWarning));
			restartButton.setText(AndroidGame.res.getString(R.string.pewBallRestartConfirm));
			continueButton.setText(AndroidGame.res.getString(R.string.pewBallCancel));
		} else {
			headingText.setText(AndroidGame.res.getString(R.string.pewBall));
			restartButton.setText(AndroidGame.res.getString(R.string.pewBallStartOver));
			continueButton.setText(AndroidGame.res.getString(R.string.pewBallContinue));
		}
	}

	public static void victory() {
		// Update high score (if appropriate)
		if (currentLevel > highScore) {
			highScore = currentLevel;
			PersistentData.setPewBallHighScore(highScore);
		}

		// Update current level
		currentLevel++;
		PersistentData.setPewBallCurrentLevel(currentLevel);
	}

	public static void defeat() {
		// Update current level
		if (currentLevel > 1) {
			currentLevel = Math.max(1, currentLevel - 3);
			PersistentData.setPewBallCurrentLevel(currentLevel);
		}
	}

	/**
	 * Used to track loss-dodge attempt. Toggled on when a game is in concede territory and toggled off when the game is not in concede territory or
	 * when the game ends. If the user enters PewBallPrepScreen with this flag still on, they must have left a game in-progress and shall be
	 * credited with a loss
	 */
	static void setGameInProgress(boolean isInProgress) {
		pewBallInProgress = isInProgress;
		PersistentData.setPewBallInProgress(pewBallInProgress);
	}

	@Override
	public void update(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();

		if (continueButton != null)
			continueButton.update(deltaTime);
		restartButton.update(deltaTime);
		backButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			if (continueButton != null)
				continueButton.handleEvent(event);
			restartButton.handleEvent(event);
			backButton.handleEvent(event);

			//testing
			levelSlider.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		final Object source = we.getSource();

		if (continueButton != null && source == continueButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				if (warningVisible)
					displayRestartWarning(false);
				else {
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, currentLevel));
				}
			}
		} else if (source == restartButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				// Case: restartButton is currently a "Begin" button or it is currently the "Yes, Restart" button
				if (currentLevel == 1 || warningVisible) {
					currentLevel = 1;
					PersistentData.setPewBallCurrentLevel(currentLevel);
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, Levels.PEW_BALL_STAGE, currentLevel));
				} else
					displayRestartWarning(true);
			}
		} else if (source == backButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				if (!Screen.isScreenChanging()) {
					prepScreenChange();
					game.setScreen(new MainMenuScreen(game, false));
				}
			}
		}

		//testing
		else if (source == levelSlider) {
			currentLevel = levelSlider.getValue();
			PersistentData.setPewBallCurrentLevel(currentLevel);
			levelText.setText(Integer.toString(currentLevel));
		}
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void backButton() {
		if (!Screen.isScreenChanging())
			backButton.click();
	}
}