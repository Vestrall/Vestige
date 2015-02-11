package com.lescomber.vestige.framework;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import com.lescomber.vestige.FPSCounter;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.cgl.CGLRenderer;
import com.lescomber.vestige.crossover.ColorRectManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.TextManager;
import com.lescomber.vestige.screens.SplashScreen;

@SuppressLint("NewApi")
public class AndroidGame implements Runnable
{
	private static final int DESIRED_FRAME_TIME = 1000 / 60;	// ~60 fps
	private static final int MAX_FRAME_TIME = 60;
	
	public static Resources res;
	
	private String gameVersion;
	
	private final Input input;
	private final AndroidFileIO fileIO;
	private Screen curScreen;
	
	private Thread gameThread = null;
	private volatile boolean running = false;
	
	private final AndroidGameActivity gameActivity;
	
	private final FPSCounter upsCounter;
	
	public AndroidGame(AndroidGameActivity gameActivity)
	{
		this.gameActivity = gameActivity;
		
		res = gameActivity.getResources();
		
		final boolean isPortrait = gameActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		final int deviceWidth = isPortrait ? Screen.HEIGHT : Screen.WIDTH;
		final int deviceHeight = isPortrait ? Screen.WIDTH : Screen.HEIGHT;
		float scaleX;
		float scaleY;
		
		final Point screenSize = getScreenSize();
		
		// TODO: Adjust screenSize for status/navigation bars if either are present
		
		scaleX = (float) deviceWidth / screenSize.x;
		scaleY = (float) deviceHeight / screenSize.y;
		
		try {
			final PackageInfo pInfo = gameActivity.getPackageManager().getPackageInfo(gameActivity.getPackageName(), 0);
			gameVersion = pInfo.versionName;
		}
		catch (final NameNotFoundException nnfe) {
			gameVersion = "";
		}
		
		input = new AndroidInput(gameActivity.getView(), scaleX, scaleY);
		fileIO = new AndroidFileIO(gameActivity);
		upsCounter = new FPSCounter();
		curScreen = new SplashScreen(this);
	}
	
	// Returns the size of the screen if status/nav bars are not visible. Will need to subtract status/nav bar sizes if desired
	private Point getScreenSize()
	{
		final Point size = new Point();
		
		final Display display = gameActivity.getWindowManager().getDefaultDisplay();
		if(Build.VERSION.SDK_INT < 14)
		{
			final DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			size.x = metrics.widthPixels;
			size.y = metrics.heightPixels;
		}
		else if (Build.VERSION.SDK_INT < 17)
		{
			try {
				size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
				size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
			} catch (final Exception e) { }
		}
		else
		{
			display.getRealSize(size);
		}
		
		return size;
	}
	
	public String gameVersion()
	{
		return gameVersion;
	}
	
	@Override
	public void run()
	{
		while (!CGLRenderer.isReady())
		{
        	try {
        		Thread.sleep(50);
        	}
        	catch (final InterruptedException ie) { }
		}
		
		long startTime = System.nanoTime();
		long deltaTime = 0;
		while (running)
		{
			// Throttle loop speed to a maximum of roughly 60 updates per second
			deltaTime = (System.nanoTime() - startTime) / 1000000;
			try
			{
				if (deltaTime < DESIRED_FRAME_TIME)
					Thread.sleep(DESIRED_FRAME_TIME - deltaTime);
			}
			catch(final InterruptedException ie) { }
			
			deltaTime = (System.nanoTime() - startTime) / 1000000;		// Note: deltaTime is in ms
			startTime = System.nanoTime();
			
			upsCounter.addFrame((int)deltaTime);
			
			if (deltaTime > MAX_FRAME_TIME)
				deltaTime = MAX_FRAME_TIME;
			
			curScreen.update((int)deltaTime);
		}
	}
	
	public void setScreen(Screen screen)
	{
		if (screen == null)
			throw new IllegalArgumentException("Screen must not be null");
		
		curScreen.pause();
		curScreen.dispose();
		
		SpriteManager.getInstance().swapBuffer();
		TextManager.switchDraw();
		ColorRectManager.switchDraw();
		
		TextManager.clearOtherBuild();
		ColorRectManager.clearOtherBuild();
		
		screen.resume();
		curScreen = screen;
		
		Screen.notifyScreenChanged();
	}
	
	public Input getInput()	{ return input; }
	public AndroidFileIO getFileIO() { return fileIO; }
	public Screen getCurrentScreen() { return curScreen; }
	public double getFPS() { return upsCounter.getFPS(); }
	
	public void pauseScreen()
	{
		curScreen.pause();
	}
	
	public void resume()
	{
		AudioManager.resume();
		curScreen.resume();
		running = true;
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void pause(boolean isFinishing)
	{
		running = false;
		while (true)
		{
			try
			{
				gameThread.join();
				break;
			} catch (final InterruptedException e) {
				// retry
			}
		}
		
		curScreen.pause();
		AudioManager.pause(isFinishing);
		
		if (isFinishing)
			curScreen.dispose();
	}
	
	public void backButton()
	{
		if (curScreen != null)
			curScreen.backButton();
	}
}