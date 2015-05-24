package com.lescomber.vestige.cgl;

import android.opengl.GLES20;

public class CGLColorRect extends CGLDrawingRect {
	private static final String VERTEX_SHADER_CODE = "uniform mat4 u_mvpMatrix;" +
			"attribute vec4 a_position;" +

			"void main() {" +
			"  gl_Position = u_mvpMatrix * a_position;" +
			"}";

	private static final String FRAGMENT_SHADER_CODE = "precision mediump float;" +
			"uniform vec4 u_color;" +

			"void main() {" +
			"  gl_FragColor = u_color;" +
			"}";

	// Handles to the GL program and various components of it.
	private static int sProgramHandle = -1;
	private static int sColorHandle = -1;
	private static int sPositionHandle = -1;
	private static int sMVPMatrixHandle = -1;

	// RGBA color vector.
	private final float[] mColor = new float[4];

	public CGLColorRect(float x, float y, float width, float height, float r, float g, float b, float a) {
		super(x, y, width, height);

		// Init color
		setColor(r, g, b, a);
	}

	public CGLColorRect(float x, float y, float width, float height, float r, float g, float b) {
		this(x, y, width, height, r, g, b, 1);
	}

	/**
	 * Creates the GL program and associated references.
	 */
	public static void createProgram() {
		sProgramHandle = CGLUtil.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);

		// get handle to vertex shader's a_position member
		sPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_position");

		// get handle to fragment shader's u_color member
		sColorHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_color");

		// get handle to transformation matrix
		sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");
	}

	/**
	 * Sets the color.
	 */
	public void setColor(float r, float g, float b, float a) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
		mColor[3] = a;
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1);
	}

	public void setAlpha(float a) {
		mColor[3] = a;
	}

	public float[] getColor() {
		return mColor;
	}

	/**
	 * Performs setup common to all CGLColorRects.
	 */
	public static void prepareToDraw() {
		/*
		 * We could do this setup in every draw() call. However, experiments on a couple of different devices
		 * indicated that we can increase the CPU time required to draw a frame by as much as 2x. Doing the setup
		 * once, then drawing all objects of that type (colored / textured) provides a substantial CPU cost
		 * savings.
		 * 
		 * It's a lot more awkward this way -- we want to draw similar types of objects together whenever possible,
		 * and we have to wrap calls with prepare/finish -- but avoiding configuration changes can improve efficiency,
		 * and the explicit prepare calls highlight potential efficiency problems.
		 */

		// Select the program.
		GLES20.glUseProgram(sProgramHandle);

		// Enable the "a_position" vertex attribute.
		GLES20.glEnableVertexAttribArray(sPositionHandle);

		// Since we aren't using a model/view matrix, we will use the projection matrix as our model/view/projection
		//matrix since the model/view matrix would not alter it
		GLES20.glUniformMatrix4fv(sMVPMatrixHandle, 1, false, CGLRenderer.mProjectionMatrix, 0);
	}

	/**
	 * Cleans up after drawing.
	 */
	public static void finishedDrawing() {
		// Disable vertex array and program. Not strictly necessary.
		GLES20.glDisableVertexAttribArray(sPositionHandle);
		GLES20.glUseProgram(0);
	}

	/**
	 * Draws the rect.
	 */
	public void draw() {
		//Connect vertexBuffer to "a_position".
		GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);

		// Copy the color vector into the program.
		GLES20.glUniform4fv(sColorHandle, 1, mColor, 0);

		// Draw the rect.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
	}
}