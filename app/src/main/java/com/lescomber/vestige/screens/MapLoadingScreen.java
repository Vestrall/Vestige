package com.lescomber.vestige.screens;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.ColorRectManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.TextManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.map.Map;

import java.util.List;

public class MapLoadingScreen extends Screen implements Runnable
{
	private static final long DESIRED_FRAME_TIME = 1000 / 60;    // Update speed for opacity Thread

	private static final int PROGRESS_FAKE_INTERVAL = 200;    // Time (in ms) between each 1% progress increase
	private float progressCap;    // Max progress to be displayed via progress "fake" system
	private int progressFakeCooldown;

	private GameScreen gameScreen;

	private Thread opacityThread;
	private volatile boolean opacityRunning;
	private float opacityFlux;
	private float opacityPerMs;
	private static final float OPACITY_PER_MS_MAX = 0.0005f;    // 50% per second

	private float progress;

	private final int stageNum;
	private final int levelNum;

	private boolean isLoading;
	private static final int PASS_DELAY_COUNT = 5;
	private int passCount;    // Used to pace loading in order to make screen transition smoother

	private Sprite loadingCircleFill;
	private final Text progressText;

	private static final float ALPHA_PER_MS = 0.67f / 2000;
	private float messageAlpha;
	private Text message;

	private boolean backPressed;

	private boolean downTouch;

	private boolean backing;

	public MapLoadingScreen(AndroidGame game, int stageNum, int levelNum)
	{
		super(game);

		AudioManager.playMusic(AudioManager.MENU_MUSIC);

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		this.stageNum = stageNum;
		this.levelNum = levelNum;

		opacityThread = null;
		opacityFlux = 1;
		opacityPerMs = -OPACITY_PER_MS_MAX;

		isLoading = true;
		passCount = 1;

		new Sprite(SpriteManager.loadingCircleBackground, Screen.MIDX, 200, true);

		//final TextStyle progressStyle = new TextStyle("BLANCH_CAPS.otf", 57, 0, 0, 255, 255, 255, 1);
		//progressStyle.setSpacing(2.5f);
		final TextStyle progressStyle = TextStyle.bodyStyleWhite();
		progressText = new Text(progressStyle, "0%", Screen.MIDX, 200);

		backPressed = false;

		downTouch = false;

		backing = false;
	}

	@Override
	public void update(int deltaTime)
	{
		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		if (isLoading)
		{
			if (passCount == PASS_DELAY_COUNT)
			{
				progress = 0.4f;
				loadingCircleFill = new Sprite(SpriteManager.loadingCircleFill, Screen.MIDX, 200, false);
				final float fillRadius = progress * SpriteManager.loadingCircleFill.getWidth();
				loadingCircleFill.scaleTo(fillRadius, fillRadius);
				loadingCircleFill.setAlpha(progress);
				loadingCircleFill.setVisible(true);
				progressText.setText("40%");
			}
			else if (passCount == PASS_DELAY_COUNT + 1)
			{
				messageAlpha = 0.33f;
				//final TextStyle messageFadeStyle = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255, messageAlpha);
				//messageFadeStyle.setSpacing(2.5f);
				final TextStyle messageFadeStyle = TextStyle.bodyStyleCyan();
				messageFadeStyle.setAlpha(messageAlpha);
				message = new Text(messageFadeStyle, "", Screen.MIDX, 340);
			}
			else if (passCount == PASS_DELAY_COUNT + 2)
			{
				prepScreenChange();

				setProgress(40, 58);
				progressFakeCooldown = 0;    // Speed up the first fake percent

				final GameScreen gs;
				if (stageNum == Levels.TUTORIAL_STAGE)
					gs = new TutorialScreen(game);
				else if (stageNum == Levels.PEW_BALL_STAGE)
					gs = new PewBallScreen(game);
				else
					gs = new GameScreen(game);
				//final TutorialScreen ts = new TutorialScreen(game);

				setProgress(60, 78);

				final Map level = Levels.loadLevel(stageNum, levelNum);

				setProgress(80, 93);

				gs.loadMap(level);
				gameScreen = gs;

				setProgress(95, 98);

				System.gc();

				setProgress(100, 100);

				isLoading = false;

				/*if (stageNum == Levels.TUTORIAL_STAGE)	// Load TutorialScreen
				{
					prepScreenChange();
					
					setProgress(40, 58);
					progressFakeCooldown = 0;	// Speed up the first fake percent
					
					final TutorialScreen ts = new TutorialScreen(game);
					
					setProgress(60, 78);
					
					final Map level = Levels.loadLevel(stageNum, levelNum);
					
					setProgress(80, 93);
					
					ts.loadMap(level);
					gameScreen = ts;
					
					setProgress(95, 98);
					
					System.gc();
					
					setProgress(100, 100);
					
					isLoading = false;
				}
				else			// Load GameScreen with appropriate stageNum/levelNum
				{
					prepScreenChange();
					
					setProgress(40, 58);
					progressFakeCooldown = 0;	// Speed up the first fake percent
					
					final GameScreen gs = new GameScreen(game);
					
					setProgress(60, 78);
					
					final Map level = Levels.loadLevel(stageNum, levelNum);
					
					setProgress(80, 93);
					
					gs.loadMap(level);
					gameScreen = gs;
					
					setProgress(95, 98);
					
					System.gc();
					
					setProgress(100, 100);
					
					isLoading = false;
				}*/
			}

			passCount++;

			game.getInput().getTouchEvents();    // Clear touchEvents
		}
		else if (backPressed)
			backUp();
		else
		{
			if (messageAlpha < 1.0f)
			{
				messageAlpha += (ALPHA_PER_MS * deltaTime);
				if (messageAlpha > 1.0f)
					messageAlpha = 1.0f;

				synchronized (this)
				{
					if (!backing)
						message.setAlpha(messageAlpha);
				}
			}

			final int len = touchEvents.size();
			for (int i = 0; i < len; i++)
			{
				final TouchEvent event = touchEvents.get(i);
				if (event.type == TouchEvent.TOUCH_DOWN)
				{
					downTouch = true;
				}
				else if (event.type == TouchEvent.TOUCH_UP)
				{
					if (downTouch)
					{
						// Close opacity updating thread before continuing to next screen
						opacityRunning = false;
						while (true)
						{
							try
							{
								opacityThread.join();
								break;
							} catch (final InterruptedException e)
							{
								// Retry
							}
						}

						game.setScreen(gameScreen);
						break;
					}
				}
			}
		}
	}

	private void backUp()
	{
		synchronized (this)
		{
			backing = true;
		}

		SpriteManager.getInstance().startBuffer();
		TextManager.clearBuild();
		ColorRectManager.clearBuild();
		AudioManager.clearQueue();

		if (stageNum == Levels.TUTORIAL_STAGE)
			game.setScreen(new MainMenuScreen(game, false));
		else if (stageNum == Levels.PEW_BALL_STAGE)
			game.setScreen(new PewBallPrepScreen(game));
		else
			game.setScreen(new LevelSelectionScreen(game, stageNum));

		//if (stageNum > 0)
		//	game.setScreen(new LevelSelectionScreen(game, stageNum));
		//else	// Case: Tutorial was loading
		//	game.setScreen(new MainMenuScreen(game, false));
	}

	private void setProgress(int progressPercent, int progressPercentCap)
	{
		progress = (float) progressPercent / 100;
		progressCap = (float) progressPercentCap / 100;
		progressFakeCooldown = PROGRESS_FAKE_INTERVAL;

		synchronized (this)
		{
			if (!backing)
			{
				final float fillRadius = progress * SpriteManager.loadingCircleFill.getWidth();
				progressText.setText("" + progressPercent + "%");
				loadingCircleFill.scaleTo(fillRadius, fillRadius);
				loadingCircleFill.setAlpha((float) progressPercent / 100);

				if (progressPercent >= 100)
					message.setText(AndroidGame.res.getString(R.string.mapIsLoaded));
			}
		}
	}

	private void setProgress(int progressPercent)
	{
		setProgress(progressPercent, (int) (progressCap * 100));
	}

	@Override
	public void pause()
	{
		opacityRunning = false;
		while (true)
		{
			try
			{
				opacityThread.join();
				break;
			} catch (final InterruptedException e)
			{
				// retry
			}
		}
	}

	@Override
	public void resume()
	{
		opacityRunning = true;
		opacityThread = new Thread(this);
		opacityThread.start();
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void backButton()
	{
		if (isLoading)
			backPressed = true;
		else
			backUp();
	}

	@Override
	public void run()
	{
		long startTime = System.nanoTime();
		long deltaTime = 0;

		while (opacityRunning)
		{
			// Throttle loop speed to a maximum of roughly 60 updates per second
			deltaTime = (System.nanoTime() - startTime) / 1000000;
			try
			{
				if (deltaTime < DESIRED_FRAME_TIME)
					Thread.sleep(DESIRED_FRAME_TIME - deltaTime);
			} catch (final InterruptedException ie)
			{
			}

			deltaTime = (System.nanoTime() - startTime) / 1000000;        // Note: deltaTime is in ms
			startTime = System.nanoTime();

			if (deltaTime > 30)
				deltaTime = 30;

			if (passCount <= PASS_DELAY_COUNT)
				continue;

			// "Fake" progress
			progressFakeCooldown -= deltaTime;
			if (progressFakeCooldown <= 0 && progress < progressCap)
				setProgress((int) (progress * 100) + 1);

			// Update opacity
			opacityFlux += (opacityPerMs * deltaTime);

			if (opacityFlux >= 1)
			{
				opacityFlux = 1;
				opacityPerMs = -opacityPerMs;
				//opacityPerMs *= -1;
			}
			else if (opacityFlux <= 0.75f)
			{
				opacityFlux = 0.75f;
				opacityPerMs = -opacityPerMs;
				//opacityPerMs *= -1;
			}

			synchronized (this)
			{
				if (!backing)
					loadingCircleFill.setAlpha(progress * opacityFlux);
			}
		}
	}
}