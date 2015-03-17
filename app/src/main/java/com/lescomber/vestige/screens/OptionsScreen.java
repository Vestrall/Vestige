package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Preferences;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.Text.Alignment;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.CheckBox;
import com.lescomber.vestige.widgets.Slider;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class OptionsScreen extends Screen implements WidgetListener
{
	// Global option fields
	public static float musicVolume = Preferences.getFloat("musicVolume", 1.0f);
	public static float sfxVolume = Preferences.getFloat("sfxVolume", 1.0f);
	public static boolean displayFPS = Preferences.getBoolean("displayFPS", true);
	public static boolean displayWave = Preferences.getBoolean("displayWave", true);

	public static int difficulty = 0;
	public static final int EASY = 0;
	public static final int MEDIUM = 1;
	public static final int HARD = 2;

	private final Button resetProgressButton;
	private final Button unlockAllButton;
	private final Slider musicVolumeSlider;
	private final Slider sfxVolumeSlider;
	private final CheckBox displayFPSBox;
	private final CheckBox displayWaveBox;
	private final Button backButton;

	public OptionsScreen(AndroidGame game)
	{
		super(game);

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		final Resources res = AndroidGame.res;
		final TextStyle headingStyle = TextStyle.headingStyle();
		final TextStyle optionsStyle = TextStyle.bodyStyleWhite();
		//final TextStyle optionsStyle = new TextStyle("BLANCH_CAPS.otf", 57, 255, 255, 255);
		//optionsStyle.setSpacing(2.5f);

		final float LEFT_COLUMN_X = 220;
		new Text(headingStyle, res.getString(R.string.options), Screen.MIDX, 75);
		new Text(optionsStyle, res.getString(R.string.music), LEFT_COLUMN_X, 169);
		new Text(optionsStyle, res.getString(R.string.sfx), LEFT_COLUMN_X, 223);
		new Text(optionsStyle, res.getString(R.string.displayFPS), 276, 277, Alignment.LEFT);
		new Text(optionsStyle, res.getString(R.string.displayWave), 276, 328, Alignment.LEFT);

		musicVolumeSlider = new Slider(460, 169, 368, 30, 0, 100);
		musicVolumeSlider.setValue((int) (musicVolume * 100));
		musicVolumeSlider.addWidgetListener(this);
		musicVolumeSlider.setVisible(true);
		sfxVolumeSlider = new Slider(460, 223, 368, 30, 0, 100);
		sfxVolumeSlider.setValue((int) (sfxVolume * 100));
		sfxVolumeSlider.addWidgetListener(this);
		sfxVolumeSlider.setVisible(true);
		displayFPSBox = new CheckBox(LEFT_COLUMN_X, 277);
		displayFPSBox.setValue(displayFPS);
		displayFPSBox.addWidgetListener(this);
		displayFPSBox.setVisible(true);
		displayWaveBox = new CheckBox(LEFT_COLUMN_X, 328);
		displayWaveBox.setValue(displayWave);
		displayWaveBox.addWidgetListener(this);
		displayWaveBox.setVisible(true);

		resetProgressButton = new Button(Screen.MIDX, 390, 401, 54, optionsStyle, "RESET LEVEL PROGRESS");
		resetProgressButton.addWidgetListener(this);
		//resetProgressButton.setVisible(true);
		unlockAllButton = new Button(Screen.MIDX, 440, 401, 54, optionsStyle, "UNLOCK ALL LEVELS");
		unlockAllButton.addWidgetListener(this);
		//unlockAllButton.setVisible(true);
		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.addWidgetListener(this);
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.setVisible(true);
	}

	@Override
	public void update(int deltaTime)
	{
		resetProgressButton.update(deltaTime);
		unlockAllButton.update(deltaTime);
		backButton.update(deltaTime);

		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++)
		{
			final TouchEvent event = touchEvents.get(i);

			resetProgressButton.handleEvent(event);
			unlockAllButton.handleEvent(event);
			musicVolumeSlider.handleEvent(event);
			sfxVolumeSlider.handleEvent(event);
			displayFPSBox.handleEvent(event);
			displayWaveBox.handleEvent(event);
			backButton.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we)
	{
		final Object source = we.getSource();

		if (source == backButton)
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
		else if (source == resetProgressButton)
		{
			if (we.getCommand().equals(Button.BUTTON_PRESSED))
			{
				Preferences.clear();
			}
		}
		else if (source == unlockAllButton)
		{
			if (we.getCommand().equals(Button.BUTTON_PRESSED))
			{
				Preferences.setInt("easyStageProgress", 1);
				Preferences.setInt("easyLevelProgress", Levels.LEVEL_COUNT[0] + 1);
				Preferences.setInt("mediumStageProgress", 1);
				Preferences.setInt("mediumLevelProgress", Levels.LEVEL_COUNT[0] + 1);
				Preferences.setInt("hardStageProgress", 1);
				Preferences.setInt("hardLevelProgress", Levels.LEVEL_COUNT[0] + 1);
			}
		}
		else if (source == musicVolumeSlider)
		{
			musicVolume = (float) musicVolumeSlider.getValue() / 100;
			Preferences.setFloat("musicVolume", musicVolume);

			// Inform AudioManager that music volume has changed so it can update the volume
			AudioManager.musicVolumeChanged();
		}
		else if (source == sfxVolumeSlider)
		{
			sfxVolume = (float) sfxVolumeSlider.getValue() / 100;
			Preferences.setFloat("sfxVolume", sfxVolume);

			// Inform AudioManager that sfx volume has changed so it can update the volume of all sound effects
			AudioManager.sfxVolumeChanged();
		}
		else if (source == displayFPSBox)
		{
			displayFPS = displayFPSBox.isChecked();
			Preferences.setBoolean("displayFPS", displayFPS);
		}
		else if (source == displayWaveBox)
		{
			displayWave = displayWaveBox.isChecked();
			Preferences.setBoolean("displayWave", displayWave);
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
			backButton.click();
	}
}