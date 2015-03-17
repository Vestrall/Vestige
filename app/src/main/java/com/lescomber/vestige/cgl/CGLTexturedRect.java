package com.lescomber.vestige.cgl;

import android.graphics.Rect;
import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class CGLTexturedRect extends CGLDrawingRect
{
	private static final String VERTEX_SHADER_CODE =
			"uniform mat4 u_mvpMatrix;" +    // model/view/projection matrix
					"attribute vec4 a_position;" +    // vertex data for us to transform
					"attribute vec2 a_texCoord;" +    // texture coordinate for vertex...
					"varying vec2 v_texCoord;" +    // ...which we forward to the fragment shader

					"void main() {" +
					"  gl_Position = u_mvpMatrix * a_position;" +
					"  v_texCoord = a_texCoord;" +
					"}";

	private static final String FRAGMENT_SHADER_CODE =
			"precision mediump float;" +        // medium is fine for texture maps
					"uniform mediump float u_alpha;" +    // opacity to display texture with
					"uniform sampler2D u_texture;" +    // texture data
					"varying vec2 v_texCoord;" +        // linearly interpolated texture coordinate

					"void main() {" +
					"  gl_FragColor = texture2D(u_texture, v_texCoord) * u_alpha;" +
					"}";

	// References to vertex data.
	private final FloatBuffer mTexBuffer;

	// Handles to uniforms and attributes in the shader.
	private static int sProgramHandle = -1;
	private static int sTexCoordHandle = -1;
	private static int sPositionHandle = -1;
	private static int sMVPMatrixHandle = -1;
	private static int sAlphaHandle = -1;

	private int textureHandle;

	private float alpha;    // Opacity to display this texture with. 0 = invisible, 1 = original image

	public CGLTexturedRect(float x, float y, int texWidth, int texHeight, Rect subTexRect)
	{
		super(x, y, subTexRect.right - subTexRect.left, subTexRect.bottom - subTexRect.top);

		// Init texture vertices
		final float left = (float) subTexRect.left / texWidth;
		final float right = (float) subTexRect.right / texWidth;
		final float top = (float) subTexRect.top / texHeight;
		final float bottom = (float) subTexRect.bottom / texHeight;

		final float[] coords = new float[8];

		coords[0] = left;
		coords[1] = top;
		coords[2] = right;
		coords[3] = top;
		coords[4] = left;
		coords[5] = bottom;
		coords[6] = right;
		coords[7] = bottom;
		mTexBuffer = createVertexArray(coords);

		alpha = 1.0f;
	}

	public CGLTexturedRect(int textureHandle, float x, float y, int texWidth, int texHeight, Rect subTexRect)
	{
		this(x, y, texWidth, texHeight, subTexRect);

		this.textureHandle = textureHandle;
	}

	public CGLTexturedRect(float x, float y, int texWidth, int texHeight)
	{
		this(x, y, texWidth, texHeight, new Rect(0, 0, texWidth, texHeight));
	}

	public void setTexRect(float left, float top, float right, float bottom)
	{
		mTexBuffer.put(0, left);
		mTexBuffer.put(1, top);
		mTexBuffer.put(2, right);
		mTexBuffer.put(3, top);
		mTexBuffer.put(4, left);
		mTexBuffer.put(5, bottom);
		mTexBuffer.put(6, right);
		mTexBuffer.put(7, bottom);
	}

	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}

	public int getTextureHandle()
	{
		return textureHandle;
	}

	/**
	 * Creates the GL program and associated references.
	 */
	public static void createProgram()
	{
		// Load the shaders
		sProgramHandle = CGLUtil.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);

		// Get handle to vertex shader's a_position member
		sPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_position");

		// Get handle to vertex shader's a_texCoord member
		sTexCoordHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_texCoord");

		// Get handle to transformation matrix
		sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");

		// Get handle to fragment shader's u_alpha member
		sAlphaHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_alpha");

		// Get handle to texture reference
		final int textureUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_texture");

		// Set u_texture to reference texture unit 0 (We don't change the value, so we can just set it here)
		GLES20.glUseProgram(sProgramHandle);
		GLES20.glUniform1i(textureUniformHandle, 0);
		GLES20.glUseProgram(0);
	}

	/**
	 * Performs setup common to all CGLTexturedRects.
	 */
	public static void prepareToDraw()
	{
		// Select our program
		GLES20.glUseProgram(sProgramHandle);

		// Enable the "a_position" vertex attribute
		GLES20.glEnableVertexAttribArray(sPositionHandle);

		// Since we aren't using a model/view matrix, we will use the projection matrix as our model/view/projection
		//matrix since the model/view matrix would not alter it
		GLES20.glUniformMatrix4fv(sMVPMatrixHandle, 1, false, CGLRenderer.mProjectionMatrix, 0);

		// Enable the "a_texCoord" vertex attribute
		GLES20.glEnableVertexAttribArray(sTexCoordHandle);
	}

	/**
	 * Cleans up after drawing.
	 */
	public static void finishedDrawing()
	{
		// Disable vertex array and program. Not strictly necessary
		GLES20.glDisableVertexAttribArray(sPositionHandle);
		GLES20.glUseProgram(0);
	}

	/**
	 * Draws the TexturedRect.
	 */
	public void draw()
	{
		// Connect vertexBuffer to "a_position"
		GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);

		// Connect mTexBuffer to "a_texCoord"
		GLES20.glVertexAttribPointer(sTexCoordHandle, TEX_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, TEX_VERTEX_STRIDE, mTexBuffer);

		// Connect alpha to "u_alpha"
		GLES20.glUniform1f(sAlphaHandle, alpha);

		// Draw the rect
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
	}

	/**
	 * Binds the appropriate texture and draws the TexturedRect
	 */
	public void bindDraw()
	{
		// Connect vertexBuffer to "a_position"
		GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);

		// Connect mTexBuffer to "a_texCoord"
		GLES20.glVertexAttribPointer(sTexCoordHandle, TEX_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, TEX_VERTEX_STRIDE, mTexBuffer);

		// Connect alpha to "u_alpha"
		GLES20.glUniform1f(sAlphaHandle, alpha);

		// Set the active texture unit to unit 0
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture data to the 2D texture target
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

		// Draw the rect
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
	}
}