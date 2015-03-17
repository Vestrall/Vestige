package com.lescomber.vestige.screens;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Screen;

public class SplashScreen extends Screen
{
	private boolean firstTimeCreate;

	public SplashScreen(AndroidGame game)
	{
		super(game);

		firstTimeCreate = true;
	}

	@Override
	public void update(int deltaTime)
	{
		if (firstTimeCreate)
		{
			SpriteManager.getInstance().setBackground(Assets.mainMenuScreen);

			// Init audio
			AudioManager.initSoundEffects();

			firstTimeCreate = false;
		}
		else
		{
			prepScreenChange();
			game.setScreen(new MainMenuScreen(game, true));
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
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}