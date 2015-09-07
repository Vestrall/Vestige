package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.MainMenuEyes;
import com.lescomber.vestige.Options;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.framework.PersistentData;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.ButtonGroup;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class MainMenuScreen extends Screen implements WidgetListener {
	private static final float BUTTON_WIDTH = 200;
	private static final float BUTTON_HEIGHT = 54;

	private int state;        // -10 -> -2 = eyes loading. -1 = title loading
	private static final int MAIN_STATE = 0;
	private static final int NEW_STATE = 1;

	private boolean popIn;

	private final boolean tutorialComplete;

	private final SpriteAnimation titleAnim;
	private final Sprite titleSprite;
	private int buttonAppearDelay;

	private final MainMenuEyes[] eyes;
	private static final int EYES_DELAY_MAX = 250;    // Delay (in ms) between beginning of each pair of eyes fading in
	private int eyesDelay;

	private final Button[] mainMenuButtons;
	private final Button[] newGameButtons;
	private final Button backButton;
	private final Button soundToggleButton;

	private boolean soundOn;

	private final Text versionText;

	// TODO: Implement disabled buttons for pre-tutorial completion functionality

	public MainMenuScreen(AndroidGame game, boolean introAnimation) {
		super(game);

		AudioManager.playMusic(AudioManager.MENU_MUSIC);

		SpriteManager.getInstance().setBackground(Assets.mainMenuScreen);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		tutorialComplete = PersistentData.getStageProgress(Options.EASY) > 0;

		titleAnim = new SpriteAnimation(SpriteManager.title);
		titleAnim.offsetTo(Screen.MIDX, 125);
		titleAnim.setFrameTime(33);
		titleAnim.setHoldLastFrame(true);
		titleSprite = new Sprite(SpriteManager.title[39], Screen.MIDX, 125);

		buttonAppearDelay = 0;

		eyes = new MainMenuEyes[10];
		eyes[0] = new MainMenuEyes(80, 302, 3);
		eyes[1] = new MainMenuEyes(646, 105, 2);
		eyes[2] = new MainMenuEyes(723, 205, 1);
		eyes[3] = new MainMenuEyes(202, 200, 2);
		eyes[4] = new MainMenuEyes(359, 37, 1);
		eyes[5] = new MainMenuEyes(90, 143, 2);
		eyes[6] = new MainMenuEyes(565, 282, 3);
		eyes[7] = new MainMenuEyes(139, 236, 1);
		eyes[8] = new MainMenuEyes(624, 243, 3);
		eyes[9] = new MainMenuEyes(267, 264, 2);

		eyes[0].fadeIn();
		eyesDelay = EYES_DELAY_MAX;
		state = -10;
		popIn = !introAnimation;

		final ButtonGroup buttonGroup = new ButtonGroup();
		final TextStyle buttonStyle = TextStyle.bodyStyleCyan();
		final TextStyle grayButtonStyle = TextStyle.bodyStyleWhite();
		grayButtonStyle.setColor(128, 128, 128);

		final Resources res = AndroidGame.res;

		mainMenuButtons = new Button[4];
		final String playText = tutorialComplete ? res.getString(R.string.play) : res.getString(R.string.tutorial);
		mainMenuButtons[0] = new Button(Screen.MIDX, 231, BUTTON_WIDTH, BUTTON_HEIGHT, buttonStyle, playText);
		mainMenuButtons[1] = new Button(Screen.MIDX, 288, BUTTON_WIDTH, BUTTON_HEIGHT, tutorialComplete ? buttonStyle : grayButtonStyle, res.getString(R.string.pewBall));
		mainMenuButtons[2] = new Button(Screen.MIDX, 344, BUTTON_WIDTH, BUTTON_HEIGHT, buttonStyle, res.getString(R.string.credits));
		mainMenuButtons[3] = new Button(Screen.MIDX, 400, BUTTON_WIDTH, BUTTON_HEIGHT, buttonStyle, res.getString(R.string.exitGame));

		for (final Button b : mainMenuButtons) {
			b.addWidgetListener(this);
			b.registerGroup(buttonGroup);
		}

		final TextStyle difficultyStyle = tutorialComplete ? buttonStyle : grayButtonStyle;

		newGameButtons = new Button[4];
		newGameButtons[0] = new Button(Screen.MIDX, 231, BUTTON_WIDTH, BUTTON_HEIGHT, buttonStyle, res.getString(R.string.tutorial));
		newGameButtons[1] = new Button(Screen.MIDX, 288, BUTTON_WIDTH, BUTTON_HEIGHT, difficultyStyle, res.getString(R.string.easy));
		newGameButtons[2] = new Button(Screen.MIDX, 344, BUTTON_WIDTH, BUTTON_HEIGHT, difficultyStyle, res.getString(R.string.medium));
		newGameButtons[3] = new Button(Screen.MIDX, 400, BUTTON_WIDTH, BUTTON_HEIGHT, difficultyStyle, res.getString(R.string.hard));

		for (final Button b : newGameButtons) {
			b.addWidgetListener(this);
			b.registerGroup(buttonGroup);
		}

		// Init back button
		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.scaleRect(1.25, 1.25);    // Enlarge "back" button hitbox slightly
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.addWidgetListener(this);
		backButton.registerGroup(buttonGroup);

		// Init sound toggle button
		soundOn = Options.getMusicVolume() > 0 || Options.getSfxVolume() > 0;
		final SpriteTemplate soundToggleTemplate = soundOn ? SpriteManager.soundToggleOn : SpriteManager.soundToggleOff;
		soundToggleButton = new Button(40, 440, null, null, soundToggleTemplate);
		soundToggleButton.scaleRect(1.25, 1.25);    // Enlarge button hitbox slightly
		soundToggleButton.addWidgetListener(this);

		// Init version text
		final String version = game.gameVersion();
		final TextStyle versionTextStyle = TextStyle.bodyStyleWhite(20);
		versionText = new Text(versionTextStyle, version, Screen.MIDX, 465, false);
	}

	private void loadMainMenu() {
		state = MAIN_STATE;

		for (final Button b : newGameButtons)
			b.setVisible(false);

		backButton.setVisible(false);

		for (final Button b : mainMenuButtons)
			b.setVisible(true);

		versionText.setVisible(true);
		soundToggleButton.setVisible(true);    // Only necessary when intro anim finishes on its own (i.e. no popIn)
	}

	private void loadNewGameMenu() {
		state = NEW_STATE;

		for (final Button b : mainMenuButtons)
			b.setVisible(false);

		for (final Button b : newGameButtons)
			b.setVisible(true);

		backButton.setVisible(true);
	}

	@Override
	public void update(int deltaTime) {
		final int titleAnimTime = titleAnim.getTimeRemaining();
		if (titleAnim.update(deltaTime)) {
			buttonAppearDelay = 90 + titleAnimTime - deltaTime;
			Swapper.swapImages(titleAnim, titleSprite);
		}
		if (buttonAppearDelay > 0) {
			buttonAppearDelay -= deltaTime;
			if (buttonAppearDelay <= 0)
				loadMainMenu();
		}

		for (final MainMenuEyes mme : eyes)
			mme.update(deltaTime);

		if (state < 0)
			updateLoading(deltaTime);
		else if (state == MAIN_STATE)
			updateMainMenu(deltaTime);
		else if (state == NEW_STATE)
			updateNewGameMenu(deltaTime);
	}

	private void updateLoading(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();
		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			if (touchEvents.get(i).type == TouchEvent.TOUCH_UP)
				popIn = true;
		}

		if (popIn) {
			buttonAppearDelay = 0;

			// Pop in all eyes (even ones that had already begun fading in)
			for (final MainMenuEyes mme : eyes)
				mme.popIn();

			// Pop in title, buttons, and version text
			Swapper.swapImages(titleAnim, titleSprite);
			loadMainMenu();        // Also updates state
			versionText.setVisible(true);
			soundToggleButton.setVisible(true);
		} else {
			eyesDelay -= deltaTime;
			if (eyesDelay <= 0) {
				eyesDelay = EYES_DELAY_MAX;

				if (state < -1)
					eyes[state + 11].fadeIn();
				else {
					titleAnim.play();
					titleAnim.setVisible(true);
				}

				state++;
			}
		}
	}

	private void updateMainMenu(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();

		for (final Button b : mainMenuButtons)
			b.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			for (int j = 0; j < mainMenuButtons.length; j++) {
				if (tutorialComplete || j != 1) {
					mainMenuButtons[j].handleEvent(event);
				}
			}

			soundToggleButton.handleEvent(event);
		}
	}

	private void updateNewGameMenu(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();

		for (final Button b : newGameButtons)
			b.update(deltaTime);

		backButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			newGameButtons[0].handleEvent(event);

			if (tutorialComplete) {
				for (int j = 1; j < 4; j++)
					newGameButtons[j].handleEvent(event);
			}

			backButton.handleEvent(event);
			soundToggleButton.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		final Object source = we.getSource();

		// Main menu buttons
		if (source == mainMenuButtons[0]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				if (tutorialComplete)
					loadNewGameMenu();
				else {
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, Levels.TUTORIAL_STAGE, 0));
				}
			}
		} else if (source == mainMenuButtons[1]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				prepScreenChange();
				game.setScreen(new PewBallPrepScreen(game));
			}
		} else if (source == mainMenuButtons[2]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				prepScreenChange();
				game.setScreen(new CreditsScreen(game));
			}
		} else if (source == mainMenuButtons[3]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				android.os.Process.killProcess(android.os.Process.myPid());

		// New game buttons
		} else if (source == newGameButtons[0]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				prepScreenChange();
				game.setScreen(new MapLoadingScreen(game, Levels.TUTORIAL_STAGE, 0));
			}
		} else if (source == newGameButtons[1]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				Options.difficulty = Options.EASY;

				prepScreenChange();
				game.setScreen(new StageSelectionScreen(game));
			}
		} else if (source == newGameButtons[2]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				Options.difficulty = Options.MEDIUM;

				prepScreenChange();
				game.setScreen(new StageSelectionScreen(game));
			}
		} else if (source == newGameButtons[3]) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				Options.difficulty = Options.HARD;

				prepScreenChange();
				game.setScreen(new StageSelectionScreen(game));
			}

		// Other
		} else if (source == backButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				loadMainMenu();
		} else if (source == soundToggleButton) {
			if (soundOn) {
				PersistentData.setLiveMusicVolume(Options.getMusicVolume());
				PersistentData.setLiveSfxVolume(Options.getSfxVolume());
				soundToggleButton.setImage(SpriteManager.soundToggleOff);
				Options.setMusicVolume(0);
				Options.setSfxVolume(0);
			} else {
				soundToggleButton.setImage(SpriteManager.soundToggleOn);
				Options.setMusicVolume(PersistentData.getLiveMusicVolume());
				Options.setSfxVolume(PersistentData.getLiveSfxVolume());
			}
			soundOn = !soundOn;
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
		if (state < 0)
			popIn = true;
		else if (state == NEW_STATE)
			backButton.click();            // Click back button
		else
			mainMenuButtons[3].click();    // Click exit button
	}
}