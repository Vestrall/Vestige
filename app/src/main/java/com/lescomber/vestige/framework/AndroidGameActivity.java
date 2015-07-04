package com.lescomber.vestige.framework;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.view.WindowManager;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.cgl.CGLSurfaceView;

@SuppressLint("NewApi")
public class AndroidGameActivity extends Activity {
	private AndroidGame gameLoop;

	private GLSurfaceView glView;

	// TODO: Create flags for android versions 16, 17 (Jellybean)
	private static final int UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View
			.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View
			.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
	private static View decorView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Disabled for release versions
		//StrictMode.enableDefaults();

		// Init Preferences reference
		PersistentData.initPrefs(getPreferences(MODE_PRIVATE));

		// Init AssetManager reference
		Assets.initAssetManager(getAssets());

		// Init AudioManager
		AudioManager.init(this);

		// Ensure that the window depth is set to 32-bits
		getWindow().setFormat(PixelFormat.RGBA_8888);

		// Set full screen mode
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (Build.VERSION.SDK_INT < 19)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else {
			decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(UI_FLAGS);
			decorView.setOnSystemUiVisibilityChangeListener(createUIListener());
		}

		// Create a GLSurfaceView instance and set it as the ContentView for this Activity.
		glView = new CGLSurfaceView(this);
		setContentView(glView);

		gameLoop = new AndroidGame(this);
	}

	public View getView() {
		return glView;
	}

	@Override
	public void onResume() {
		super.onResume();
		gameLoop.resume();
		glView.onResume();
	}

	private OnSystemUiVisibilityChangeListener createUIListener() {
		return new OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {    // Case: Status bar visible
					decorView = getWindow().getDecorView();
					decorView.setSystemUiVisibility(UI_FLAGS);
				}
			}
		};
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!hasFocus)
			gameLoop.pauseScreen();
		else if (Build.VERSION.SDK_INT >= 18) {
			decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(UI_FLAGS);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		glView.onPause();
		gameLoop.pause(isFinishing());
	}

	@Override
	public void onBackPressed() {
		gameLoop.backButton();
	}
}