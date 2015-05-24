package com.lescomber.vestige.cgl;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.lescomber.vestige.Assets;

public class CGLTexture {
	private int width;
	private int height;
	private int textureHandle;

	public CGLTexture() {
		width = 0;
		height = 0;
		textureHandle = -1;
	}

	private int textureFromBitmap(Bitmap bitmap) {
		// Generate texture handle and bind it
		final int[] textureHandles = new int[1];
		GLES20.glGenTextures(1, textureHandles, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandles[0]);

		// Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering is smaller or
		//larger than the source image
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		//GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		//GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Load the data from the buffer into the texture handle.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Recycle bitmap now that it is loaded in as a texture
		bitmap.recycle();

		return textureHandles[0];
	}

	public void createImageTexture(String filename, Config format) {
		final Bitmap bitmap = Assets.newBitmap(filename, format);
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		textureHandle = textureFromBitmap(bitmap);
	}

	public void createImageTexture(String filename) {
		createImageTexture(filename, Config.ARGB_8888);
	}

	public void createImageTexture(Bitmap bitmap) {
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		textureHandle = textureFromBitmap(bitmap);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getTextureHandle() {
		return textureHandle;
	}
}