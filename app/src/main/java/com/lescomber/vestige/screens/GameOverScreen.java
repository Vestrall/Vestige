package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.ButtonGroup;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class GameOverScreen extends Screen implements WidgetListener
{
	private static final int FADE_IN_STATE = 0;
	private static final int RISING_STATE = 1;
	private static final int FINAL_STATE = 2;
	private static final int RETRY_STATE = 3;        // Effectively a screen change queue (to MapLoadingScreen)
	private static final int MAIN_MENU_STATE = 4;    // Effectively a screen change queue (to MainMenuScreen)
	private int state;

	private static final float ALPHA_PER_MS = 0.002f;    // 0 to 100% in half a second
	private static final float FINAL_TITLE_Y = 112;
	private static final float Y_PER_MS = (FINAL_TITLE_Y - Screen.MIDY) / 500;    // Half second animation duration
	private final Text titleText;
	private float titleAlpha;

	private final Button retryButton;
	private final Button mainMenuButton;

	private final int stageNum;
	private final int levelNum;

	public GameOverScreen(AndroidGame game, int stageNum, int levelNum)
	{
		super(game);

		// Note: no background is set here since the background is black anyway
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		AudioManager.playMusic(AudioManager.MENU_MUSIC);

		state = FADE_IN_STATE;
		titleAlpha = 0;

		final Resources res = AndroidGame.res;
		//final TextStyle headingStyle = new TextStyle("Tommaso.otf", 83, 87, 233, 255, titleAlpha);
		//headingStyle.setSpacing(2.5f);
		final TextStyle headingStyle = TextStyle.headingStyle();
		final TextStyle buttonStyle = TextStyle.bodyStyleCyan();
		//final TextStyle buttonStyle = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255);
		//buttonStyle.setSpacing(2.5f);

		final ButtonGroup buttonGroup = new ButtonGroup();
		titleText = new Text(headingStyle, res.getString(R.string.gameOver), Screen.MIDX, Screen.MIDY);
		titleText.setAlpha(titleAlpha);
		retryButton = new Button(Screen.MIDX, 212, 200, 54, buttonStyle, res.getString(R.string.retry));
		mainMenuButton = new Button(Screen.MIDX, 286, 200, 54, buttonStyle, res.getString(R.string.mainMenu));
		retryButton.addWidgetListener(this);
		retryButton.registerGroup(buttonGroup);
		mainMenuButton.addWidgetListener(this);
		mainMenuButton.registerGroup(buttonGroup);

		this.stageNum = stageNum;
		this.levelNum = levelNum;
	}

	@Override
	public void update(int deltaTime)
	{
		// Retrieve touchEvents first so that they will be discarded if we have not yet reached FINAL_STATE
		//List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		if (state == FADE_IN_STATE)
			updateFadeIn(deltaTime);
		else if (state == RISING_STATE)
			updateRising(deltaTime);
		else if (state == FINAL_STATE)
			updateFinal(deltaTime);
		else if (state == RETRY_STATE)
		{
			prepScreenChange();
			game.setScreen(new MapLoadingScreen(game, stageNum, levelNum));
		}
		else if (state == MAIN_MENU_STATE)
		{
			prepScreenChange();
			game.setScreen(new MainMenuScreen(game, false));
		}
	}

	private void updateFadeIn(int deltaTime)
	{
		titleAlpha += deltaTime * ALPHA_PER_MS;
		if (titleAlpha >= 1)
		{
			titleAlpha = 1;
			state = RISING_STATE;
		}

		titleText.setAlpha(titleAlpha);
	}

	private void updateRising(int deltaTime)
	{
		float newY = titleText.getY() + (deltaTime * Y_PER_MS);
		if (newY <= FINAL_TITLE_Y)
		{
			newY = FINAL_TITLE_Y;
			state = FINAL_STATE;

			retryButton.setVisible(true);
			mainMenuButton.setVisible(true);

			// Retrieve and discard touchEvents that have accumulated during fade-in/rising animations
			game.getInput().getTouchEvents();
		}

		titleText.offsetTo(Screen.MIDX, newY);
	}

	private void updateFinal(int deltaTime)
	{
		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		for (final TouchEvent event : touchEvents)
		{
			retryButton.handleEvent(event);
			mainMenuButton.handleEvent(event);
		}

		retryButton.update(deltaTime);
		mainMenuButton.update(deltaTime);
	}

	@Override
	public void widgetEvent(WidgetEvent we)
	{
		final Object source = we.getSource();

		if (source == retryButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				state = RETRY_STATE;
		}
		else if (source == mainMenuButton)
		{
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
				state = MAIN_MENU_STATE;
		}
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void backButton()
	{
		if (!Screen.isScreenChanging())
			mainMenuButton.click();
	}
}