package com.lescomber.vestige.cgl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.FpsCounter;
import com.lescomber.vestige.crossover.ColorRectManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.TextManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CGLRenderer implements Renderer {
	private static boolean isInitialized = false;

	private static final int SCREEN_WIDTH = 800;
	private static final int SCREEN_HEIGHT = 480;

	static final float[] mProjectionMatrix = new float[16];

	private static FpsCounter fpsCounter;
	private static long lastFrameTime;

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Create shaders for whatever we plan to draw
		CGLTexturedRect.createProgram();
		CGLColorRect.createProgram();
		CGLText.createProgram();

		// Set the background color.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// Disable depth testing -- we're 2D only
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		// Don't need backface culling
		GLES20.glDisable(GLES20.GL_CULL_FACE);

		// Ensure dithering is disabled since our ARGB images should all be 32 bits. Consider enabling GL_DITHER if using ARGB4444
		GLES20.glDisable(GLES20.GL_DITHER);

		// Enable alpha blending
		GLES20.glEnable(GLES20.GL_BLEND);
		// Blend based on the fragment's alpha value
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		if (!isInitialized)    // Initialize textures / managers on application started
		{
			Assets.initTextures();
			Assets.createTextures();
			SpriteManager.initTemplates();
			TextManager.init();
			ColorRectManager.init();
			fpsCounter = new FpsCounter();
			lastFrameTime = System.nanoTime();
		} else                // re-create textures/CGLTexts if OpenGL context may have been lost
		{
			Assets.createTextures();
			TextManager.recreateCGLTexts();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Sets the current view port to the new size.
		GLES20.glViewport(0, 0, width, height);

		// Select the projection matrix
		Matrix.orthoM(mProjectionMatrix, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 0, -1, 1);

		isInitialized = true;
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		// Update fps counter
		final long curTime = System.nanoTime();
		final long dTime = (curTime - lastFrameTime) / 1000000;
		fpsCounter.addFrame((int) dTime);
		lastFrameTime = curTime;

		// Clear entire screen to background color.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		// Draw background and game layers
		CGLTexturedRect.prepareToDraw();
		SpriteManager.getInstance().drawGameLayer();

		// Draw color rects
		CGLColorRect.prepareToDraw();
		ColorRectManager.draw();

		// Draw UI layer
		CGLTexturedRect.prepareToDraw();
		SpriteManager.getInstance().drawUILayer();

		// Draw text
		TextManager.draw();
	}

	public static boolean isReady() {
		return isInitialized;
	}

	public static double getFps() {
		return fpsCounter.getFps();
	}
}