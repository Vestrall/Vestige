// This is a OpenGL ES 2.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a TOP-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.lescomber.vestige.cgl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.lescomber.vestige.Assets;

public class CGLText
{
	private static final String VERTEX_SHADER_CODE =
			"uniform mat4 u_MVPMatrix[24];"     // An array representing the combined model/view/projection
												//matrices for each sprite
		  + "attribute float a_MVPMatrixIndex;"	// The index of the MVPMatrix of the particular sprite
		  + "attribute vec4 a_Position;"		// Per-vertex position information we will pass in
		  + "attribute vec2 a_TexCoordinate;"	// Per-vertex texture coordinate information we will pass in
		  + "varying vec2 v_TexCoordinate;"		// This will be passed into the fragment shader
		  
		  + "void main()"						// The entry point for our vertex shader.
		  + "{"
		  + "   int mvpMatrixIndex = int(a_MVPMatrixIndex);"
		  + "   v_TexCoordinate = a_TexCoordinate;"
		  + "   gl_Position = u_MVPMatrix[mvpMatrixIndex]"	// gl_Position is a special variable used to store the final position
		  + "               * a_Position;"		// Multiply the vertex by the matrix to get the final point in
		  + "}";								//normalized screen coordinates
	
	private static final String FRAGMENT_SHADER_CODE =
			"uniform sampler2D u_Texture;"		// The input texture
			+ "precision mediump float;"		// Set the default precision to medium. We don't need as high of a
			+ "uniform vec4 u_Color;"			//precision in the fragment shader
			+ "varying vec2 v_TexCoordinate;"	// Interpolated texture coordinate per fragment
			
			+ "void main()"     				// The entry point for our fragment shader
			+ "{"
			+ "   gl_FragColor = texture2D(u_Texture, v_TexCoordinate).a * u_Color;"	// Texture is grayscale so
			+ "}";	//take only grayscale value from it when computing color output (otherwise font is always black)
	
	private static final String fontsDir = "Fonts/";
	
	public static final int CHAR_START = 32;	// First character (ASCII code)
	public static final int CHAR_END = 126;		// Last character (ASCII code)
	public static final int CHAR_CNT = (((CHAR_END - CHAR_START) + 1) + 1); // Character count (including character to
																			//use for unknown)
	
	public static final int CHAR_NONE = 32;					// Character to use for unknown (ASCII code)
	public static final int CHAR_UNKNOWN = (CHAR_CNT - 1);	// Index of the unknown character
	
	public static final int FONT_SIZE_MIN = 6;		// Minumum font size (pixels)
	public static final int FONT_SIZE_MAX = 180;	// Maximum font size (pixels)
	
	public static final int CHAR_BATCH_SIZE = 24;	// Number of characters to render per batch. Must be the same as
													//the size of u_MVPMatrix in BatchTextProgram
	
	private final CGLTextBatch batch; // Batch renderer
	
	// Font padding on each side, ie. doubled on both x and y axis (in pixels)
	private int fontPadX;
	private int fontPadY;

	private float fontHeight;	// Font height (actual pixels)
	private float fontAscent;	// Font ascent (above baseline, in pixels)
	private float fontDescent;	// Font descent (Below Baseline, in pixels)
	
	private CGLTexture img;		// Font map texture
	private int textureSize; 	// Texture size for font (square)
	
	private float charWidthMax;		// Character width (maximum pixels)
	private float charHeight;		// Character height (maximum pixels)
	private final float[] charWidths;	// Width of each character (actual pixels)
	private final CGLTextureRegion[] charRgn;	// Region of each character (texture coordinates)
	private int cellWidth;		// Character cell width
	private int cellHeight;		// Character cell height
	//private float cellWidth;
	//private float cellHeight;
	
	private float scaleX;	// Font scale (x axis)
	private float scaleY;	// Font scale (y axis)
	private float spaceX;	// Additional (x,y axis) spacing (unscaled)
	
	private static int mProgramHandle;			// Program handle
	private static int mColorHandle;			// Shader color handle
	
	// Scratch storage
	private static float[] scratchMVMatrix = new float[16];
	private static float[] color = new float[4];
	
	// --Constructor--//
	// D: save program + asset manager, create arrays, and initialize the members
	public CGLText()
	{
		batch = new CGLTextBatch(CHAR_BATCH_SIZE, mProgramHandle); // Create sprite batch (with defined size)
		
		charWidths = new float[CHAR_CNT];		// Create the array of character widths
		charRgn = new CGLTextureRegion[CHAR_CNT];	// Create the array of character regions
		
		// Initialize remaining members to defaults
		fontPadX = 0;
		fontPadY = 0;
		
		fontHeight = 0.0f;
		fontAscent = 0.0f;
		fontDescent = 0.0f;
		
		textureSize = 0;
		
		charWidthMax = 0;
		charHeight = 0;
		
		cellWidth = 0;
		cellHeight = 0;
		
		scaleX = 1.0f; // Default scale = 1 (unscaled)
		scaleY = 1.0f; // Default scale = 1 (unscaled)
		spaceX = 0.0f;
	}
	
	public static void createProgram()
	{
		mProgramHandle = CGLUtil.createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
		
		// Get handle to fragment shader's u_Color member
		mColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Color");
		
		// Get handle to texture reference.
		final int textureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
		
		// Set u_Texture to reference texture unit 0 (We don't change the value, so we can just set it here)
		GLES20.glUseProgram(mProgramHandle);
		GLES20.glUniform1i(textureUniformHandle, 0);
		GLES20.glUseProgram(0);
	}
	
	// --Load Font--//
	// description
	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// arguments:
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	// size - Requested pixel size of font (height)
	// padX, padY - Extra padding per character (X+Y Axis); to prevent overlapping characters.
	public boolean load(String file, int size, int padX, int padY)
	{
		fontPadX = padX;	// Set requested x axis padding
		fontPadY = padY;	// Set requested y axis padding
		
		// Load the font and setup paint instance for drawing
		final Typeface tf = Assets.newTypeface(fontsDir + file);
		final Paint paint = new Paint();	// Create Android paint instance
		paint.setAntiAlias(true);	// Enable anti alias
		paint.setTextSize(size);	// Set text size
		paint.setColor(0xffffffff);	// Set ARGB (white, opaque)
		paint.setTypeface(tf);		// Set Typeface
		
		// Get font metrics
		final Paint.FontMetrics fm = paint.getFontMetrics(); 	// Get font metrics
		fontHeight = Math.abs(fm.bottom - fm.top); 		// Calculate font height
		fontAscent = Math.abs(fm.ascent);				// Save font ascent
		fontDescent = Math.abs(fm.descent);				// Save font descent
		//fontHeight = (float) Math.ceil(Math.abs(fm.bottom - fm.top)); // Calculate font height
		//fontAscent = (float) Math.ceil(Math.abs(fm.ascent));	// Save font ascent
		//fontDescent = (float) Math.ceil(Math.abs(fm.descent));	// Save font descent
		
		// Determine the width of each character (including unknown character) and determine the maximum
		//character width
		final char[] s = new char[2];			// Create character array
		charWidthMax = charHeight = 0;	// Reset character width/height maximums
		final float[] w = new float[2];		// Working width value
		int cnt = 0;					// Array counter
		for (char c = CHAR_START; c <= CHAR_END; c++)	// For each character
		{
			s[0] = c; // Set character
			paint.getTextWidths(s, 0, 1, w); // Get character bounds
			charWidths[cnt] = w[0]; // Get width
			if (charWidths[cnt] > charWidthMax) // Width larger than max width
				charWidthMax = charWidths[cnt]; // Save new max width
			cnt++; // Advance array counter
		}
		s[0] = CHAR_NONE; // Set Unknown Character
		paint.getTextWidths(s, 0, 1, w); // Get character bounds
		charWidths[cnt] = w[0]; // Get width
		if (charWidths[cnt] > charWidthMax) // Width larger than max width
			charWidthMax = charWidths[cnt]; // Save new max width
		cnt++; // Advance array counter		// TESTME: was commented out?
		
		// Set character height to font height
		charHeight = fontHeight/* + 2*/; // Set character height. The + 2 is there as added buffer room between characters
		
		// Find the maximum size, validate, and setup cell sizes
		//cellWidth = (int) charWidthMax + (2 * fontPadX); // Set cell width
		//cellHeight = (int) charHeight + (2 * fontPadY); // Set cell height
		//cellWidth = (int)(Math.ceil(charWidthMax + (2 * fontPadX))); // Set cell width
		// TODO: Proper fix for improper character drawing. Currently adding +5 to both cellWidth and cellHeight because
		//otheriwse the drawing might accidentally draw pieces of other characters that are nearby on the text texture
		cellWidth = (int)(Math.ceil(charWidthMax + (2 * fontPadX))) + 5; // Set cell width
		//cellHeight = (int)(Math.ceil(charHeight + (2 * fontPadY))); // Set cell height
		cellHeight = (int)(Math.ceil(charHeight + (2 * fontPadY))) + 5; // Set cell height
		final int maxSize = cellWidth > cellHeight ? cellWidth : cellHeight; // Save max size (width/height)
		//float maxSize = cellWidth > cellHeight ? cellWidth : cellHeight; // Save max size (width/height)
		if (maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX) // Maximum size outside valid bounds
			return false;	// Return error

		// Set texture size based on max font size (width or height)
		// NOTE: these values are fixed, based on the defined characters. when  changing start/end characters
		//(CHAR_START/CHAR_END) this will need adjustment too!
		if (maxSize <= 24) // Max size is 18 or less
			textureSize = 256; // Set 256 texture size
		else if (maxSize <= 40) // Max size is 40 or less
			textureSize = 512; // Set 512 texture size
		else if (maxSize <= 80) // Max size is 80 or less
			textureSize = 1024; // Set 1024 texture size
		else	// Max size is larger than 80 (and less than FONT_SIZE_MAX)
			textureSize = 2048; // Set 2048 texture size
			
		// Create an empty bitmap (alpha only)
		final Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize, Bitmap.Config.ALPHA_8); // Create bitmap
		final Canvas canvas = new Canvas(bitmap);	// Create canvas for rendering to bitmap
		bitmap.eraseColor(0x00000000);		// Set transparent background (ARGB)
		
		// Render each of the characters to the canvas (ie. build the font map)
		float x = fontPadX;		// Set Start Position (x)
		//float y = (cellHeight - 1) - fontDescent - fontPadY; // Set start position (y)
		//float y = (cellHeight - 1) - fm.bottom - fontPadY; // Set start position (y)
		float y = fontPadY + charHeight - fm.bottom;
		
		for (char c = CHAR_START; c <= CHAR_END; c++)	// For each character
		{
			s[0] = c; // Set character to draw
			canvas.drawText(s, 0, 1, x, y, paint); // Draw character
			x += cellWidth; // Move to next character
			if ((x + cellWidth - fontPadX) > textureSize)	// If end of line reached
			{
				x = fontPadX;		// Set x for new row
				y += cellHeight;	// Move down a row
			}
		}
		s[0] = CHAR_NONE;	// Set character to use for NONE
		canvas.drawText(s, 0, 1, x, y, paint);	// Draw character
		
		// Save the bitmap in a texture
		img = new CGLTexture();
		img.createImageTexture(bitmap);
		
		// Setup the array of character texture regions
		x = 0;
		y = 0;
		for (int c = 0; c < CHAR_CNT; c++)	// For each character (on texture)
		{
			// TODO: Proper fix for overlapping characters. Currently putting in a -5 here for cellWidth/cellHeight to workaround
			charRgn[c] = new CGLTextureRegion(textureSize, textureSize, x, y, cellWidth - 5, cellHeight - 5);
			x += cellWidth;		// Move to next char (cell)
			if (x + cellWidth > textureSize)
			{
				x = 0;				// Reset x position to start
				y += cellHeight;	// Move to next row (cell)
			}
		}
		
		return true;
	}
	
	// --Begin/End Text Drawing--//
	// D: call these methods before/after (respectively all draw() calls using a text instance
	// NOTE: color is set on a per-batch basis, and fonts should be 8-bit alpha only!!!
	// A: red, green, blue - RGB values for font (default = 1.0)
	// alpha - optional alpha value for font (default = 1.0)
	// vpMatrix - View and projection matrix to use
	// R: [none]
	public void prepareToDraw()
	{
		prepareToDraw(1.0f, 1.0f, 1.0f, 1.0f);	// Default white opaque
	}
	
	public void prepareToDraw(float alpha)
	{
		prepareToDraw(1.0f, 1.0f, 1.0f, alpha);	// Default white (explicit alpha)
	}
	
	public void prepareToDraw(float red, float green, float blue, float alpha)
	{
		initDraw(red, green, blue, alpha);
		batch.beginBatch();
	}
	
	private void initDraw(float red, float green, float blue, float alpha)
	{
		GLES20.glUseProgram(mProgramHandle);	// Specify the program to use
		
		// TODO: Fix this awful hack. It seems that our current system would prefer we use 0 as the 4th (alpha) value in the
		//color[] array for black, but 1 as the 4th (alpha) value for white. Not sure why. Setting color[3] in this fashion
		//mitigates the damage (i.e. works well enough for the colors currently being used in-game)
		
		// Set color
		color[0] = red * alpha;
		color[1] = green * alpha;
		color[2] = blue * alpha;
		color[3] = 1 - ((red + green + blue) / 3);
		//float[] color = { red, green, blue, alpha };
		//float[] color = { red*alpha, green*alpha, blue*alpha, 0 };
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		
		//GLES20.glEnableVertexAttribArray(mColorHandle);
		
		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);	// Set the active texture unit to texture unit 0
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, img.getTextureHandle());	// Bind the texture to this unit
		
		// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0
		//GLES20.glUniform1i(mTextureUniformHandle, 0);
	}
	
	public void finishedDrawing()
	{
		batch.endBatch();
	}
	
	// --Draw Text--//
	// D: draw text at the specified x,y position
	// A: text - the string to draw
	// x, y, z - the x, y, z position to draw text at (bottom left of text; including descent)
	// angleDeg - angle to rotate the text
	// R: [none]
	public void draw(String text, float x, float y, float degrees, boolean isCenter)
	{
		final float chrHeight = cellHeight * scaleY;	// Calculate scaled character height
		final float chrWidth = cellWidth * scaleX;	// Calculate scaled character width
		final int len = text.length();				// Get string length
		
		//x += (chrWidth / 2.0f) - (fontPadX * scaleX);	// Adjust Start X
		//y += (chrHeight / 2.0f) - (fontPadY * scaleY);	// Adjust Start Y
		
		// Create a model matrix based on x, y and degrees
		final float[] modelViewMatrix = scratchMVMatrix;
		Matrix.setIdentityM(modelViewMatrix, 0);
		Matrix.translateM(modelViewMatrix, 0, x, y, 0);
		Matrix.rotateM(modelViewMatrix, 0, degrees, 0, 0, 1);
		
		//float letterX = 0;
		//float letterY = 0;
		// Adjust starting positions for the first character
		float letterX = (chrWidth / 2.0f) - (fontPadX * scaleX);
		float letterY = (chrHeight / 2.0f) - (fontPadY * scaleY);
		if (isCenter)
		{
			letterX -= getLength(text) / 2;
			letterY -= getHeight() / 2;
		}
		
		for (int i = 0; i < len; i++)	 // For each character in the string
		{
			int c = text.charAt(i) - CHAR_START; // Calculate character index (offset by first char in font)
			
			if (c < 0 || c >= CHAR_CNT) // If character not in font, set to unknown character index
				c = CHAR_UNKNOWN;
			
			batch.drawSprite(letterX, letterY, chrWidth, chrHeight, charRgn[c], modelViewMatrix); // Draw the character
			letterX += (charWidths[c] + spaceX) * scaleX; // Advance x position by scaled character width
		}
	}
	
	public void draw(String text, float x, float y)
	{
		draw(text, x, y, 0, false);
	}
	
	// --Draw Text Centered--//
	// D: draw text CENTERED at the specified x,y position
	// A: text - the string to draw
	// x, y, z - the x, y, z position to draw text at (bottom left of text)
	// angleDeg - angle to rotate the text
	// R: the total width of the text that was drawn
	public float drawC(String text, float x, float y, float degrees)
	{
		final float len = getLength(text);
		draw(text, x, y, degrees, true);
		
		return len;
	}
	
	public float drawC(String text, float x, float y)
	{
		final float len = getLength(text);
		return drawC(text, x - (len / 2.0f), y - (getCharHeight() / 2.0f), 0);
	}
	
	// Draw text centered (x-axis only)
	public float drawCX(String text, float x, float y)
	{
		final float len = getLength(text);
		draw(text, x - (len / 2.0f), y);
		return len;
	}
	
	// Draw text centered (y-axis only)
	public void drawCY(String text, float x, float y)
	{
		draw(text, x, y - (getCharHeight() / 2.0f));
	}
	
	// --Set Scale--//
	// D: set the scaling to use for the font
	// A: scale - uniform scale for both x and y axis scaling
	// sx, sy - separate x and y axis scaling factors
	// R: [none]
	public void setScale(float scale)
	{
		scaleX = scale;
		scaleY = scale;
	}
	
	public void setScale(float sx, float sy)
	{
		scaleX = sx;
		scaleY = sy;
	}
	
	// --Get Scale--//
	// D: get the current scaling used for the font
	// A: [none]
	// R: the x/y scale currently used for scale
	public float getScaleX()
	{
		return scaleX;
	}
	
	public float getScaleY()
	{
		return scaleY;
	}
	
	// --Set Space--//
	// D: set the spacing (unscaled; ie. pixel size) to use for the font
	// A: space - space for x axis spacing
	// R: [none]
	public void setSpace(float space)
	{
		spaceX = space;
	}
	
	// --Get Space--//
	// D: get the current spacing used for the font
	// A: [none]
	// R: the x/y space currently used for scale
	public float getSpace()
	{
		return spaceX;
	}
	
	// --Get Length of a String--//
	// D: return the length of the specified string if rendered using current settings
	// A: text - the string to get length for
	// R: the length of the specified string (pixels)
	public float getLength(String text)
	{
		float len = 0.0f; // Working length
		final int strLen = text.length(); // Get string length (characters)
		for (int i = 0; i < strLen; i++)	// For each character in string
		{
			final int c = text.charAt(i) - CHAR_START; // Calculate character index (offset by first char in font)
			len += (charWidths[c] * scaleX); // Add scaled character width to total length
		}
		len += (strLen > 1 ? ((strLen - 1) * spaceX) * scaleX : 0); // Add space length
		return len; // Return total length
	}
	
	// --Get Width/Height of Character--//
	// D: return the scaled width/height of a character, or max character width
	// NOTE: since all characters are the same height, no character index is required!
	// NOTE: excludes spacing!!
	// A: chr - the character to get width for
	// R: the requested character size (scaled)
	public float getCharWidth(char chr)
	{
		final int c = chr - CHAR_START;			// Calculate character index (offset by first char in font)
		return (charWidths[c] * scaleX);	// Return scaled character width
	}
	
	public float getCharWidthMax()
	{
		return (charWidthMax * scaleX); // Return scaled max character width
	}
	
	public float getCharHeight()
	{
		return (charHeight * scaleY); // Return scaled character height
	}
	
	// --Get Font Metrics--//
	// D: return the specified (scaled) font metric
	// A: [none]
	// R: the requested font metric (scaled)
	public float getAscent()
	{
		return (fontAscent * scaleY); // Return font ascent
	}
	
	public float getDescent()
	{
		return (fontDescent * scaleY); // Return font descent
	}
	
	public float getHeight()
	{
		return (fontHeight * scaleY); // Return font height (actual)
	}
	
	// --Draw Font Texture--//
	// D: draw the entire font texture (NOTE: for testing purposes only)
	// A: x, y - the center point of the area to draw to. this is used
	// to draw the texture to the top-left corner.
	public void drawTexture(int x, int y)
	{
		initDraw(1.0f, 1.0f, 1.0f, 1.0f);
		
		batch.beginBatch();	// Begin batch (bind texture)
		
		final float[] idMatrix = new float[16];
		Matrix.setIdentityM(idMatrix, 0);
		
		final CGLTextureRegion textureRgn = new CGLTextureRegion(textureSize, textureSize, 0, 0, textureSize, textureSize);
		batch.drawSprite(x, y, textureSize, textureSize, textureRgn, idMatrix);
		batch.endBatch();
	}
}