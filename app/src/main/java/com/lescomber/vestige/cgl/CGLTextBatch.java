package com.lescomber.vestige.cgl;


import android.opengl.GLES20;
import android.opengl.Matrix;

public class CGLTextBatch
{
	private final static int VERTEX_SIZE = 5;            // Vertex Size (in Components) ie. (X,Y,U,V,M), M is MVP matrix index
	private final static int VERTICES_PER_SPRITE = 4;    // Vertices Per Sprite
	private final static int INDICES_PER_SPRITE = 6;    // Indices Per Sprite

	private final CGLTextVertices vertices;    // Vertices Instance Used for Rendering
	private final float[] vertexBuffer;        // Vertex Buffer
	private int bufferIndex;            // Vertex Buffer Start Index
	private final int maxSprites;            // Maximum Sprites Allowed in Buffer
	private int numSprites;            // Number of Sprites Currently in Buffer

	private final float[] uMVPMatrices = new float[CGLText.CHAR_BATCH_SIZE * 16];    // MVP matrix array to pass to shader
	private final int mMVPMatricesHandle;                                            // shader handle of the MVP matrix array
	private final float[] mMVPMatrix = new float[16];                                // used to calculate MVP matrix of each sprite

	// --Constructor--//
	// D: prepare the sprite batcher for specified maximum number of sprites
	// A: maxSprites - the maximum allowed sprites per batch
	// program - program to use when drawing
	public CGLTextBatch(int maxSprites, int programHandle/* , Program program */)
	{
		vertexBuffer = new float[maxSprites * VERTICES_PER_SPRITE * VERTEX_SIZE]; // Create Vertex Buffer

		// Create rendering Vertices
		vertices = new CGLTextVertices(maxSprites * VERTICES_PER_SPRITE, maxSprites * INDICES_PER_SPRITE, programHandle);

		bufferIndex = 0;                // Reset Buffer Index
		this.maxSprites = maxSprites;    // Save Maximum Sprites
		numSprites = 0;                // Clear Sprite Counter

		final short[] indices = new short[maxSprites * INDICES_PER_SPRITE]; // Create Temp Index Buffer
		final int len = indices.length; // Get Index Buffer Length
		short j = 0; // Counter
		for (int i = 0; i < len; i += INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE)
		{ // FOR Each Index Set (Per Sprite)
			indices[i + 0] = (short) (j + 0); // Calculate Index 0
			indices[i + 1] = (short) (j + 1); // Calculate Index 1
			indices[i + 2] = (short) (j + 2); // Calculate Index 2
			indices[i + 3] = (short) (j + 2); // Calculate Index 3
			indices[i + 4] = (short) (j + 3); // Calculate Index 4
			indices[i + 5] = (short) (j + 0); // Calculate Index 5
		}
		vertices.setIndices(indices, 0, len); // Set Index Buffer for Rendering
		mMVPMatricesHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
	}

	public void beginBatch()
	{
		numSprites = 0;    // Empty Sprite Counter
		bufferIndex = 0;    // Reset Buffer Index (Empty)
	}

	// --End Batch--//
	// D: signal the end of a batch. render the batched sprites
	// A: [none]
	// R: [none]
	public void endBatch()
	{
		if (numSprites > 0) // IF Any Sprites to render, bind MVP matrices array to shader
		{
			GLES20.glUniformMatrix4fv(mMVPMatricesHandle, numSprites, false, uMVPMatrices, 0);
			GLES20.glEnableVertexAttribArray(mMVPMatricesHandle);

			vertices.setVertices(vertexBuffer, 0, bufferIndex); // Set Vertices from Buffer
			vertices.bind(); // Bind Vertices
			vertices.draw(GLES20.GL_TRIANGLES, 0, numSprites * INDICES_PER_SPRITE); // Render Batched Sprites
			vertices.unbind(); // Unbind Vertices
		}
	}

	// --Draw Sprite to Batch--//
	// D: batch specified sprite to batch. adds vertices for sprite to vertex buffer
	// NOTE: MUST be called after beginBatch(), and before endBatch()!
	// NOTE: if the batch overflows, this will render the current batch, restart it,
	// and then batch this sprite.
	// A: x, y - the x,y position of the sprite (center)
	// width, height - the width and height of the sprite
	// region - the texture region to use for sprite
	// modelMatrix - the model matrix to assign to the sprite
	// R: [none]
	public void drawSprite(float x, float y, float width, float height, CGLTextureRegion region, float[] mModelView)
	{
		if (numSprites == maxSprites)
		{ // IF Sprite Buffer is Full
			endBatch(); // End Batch
			// NOTE: leave current texture bound!!
			numSprites = 0; // Empty Sprite Counter
			bufferIndex = 0; // Reset Buffer Index (Empty)
		}

		final float halfWidth = width / 2.0f; // Calculate Half Width
		final float halfHeight = height / 2.0f; // Calculate Half Height
		final float x1 = x - halfWidth; // Calculate Left X
		final float y1 = y + halfHeight; // Calculate Bottom Y
		final float x2 = x + halfWidth; // Calculate Right X
		final float y2 = y - halfHeight; // Calculate Top Y

		vertexBuffer[bufferIndex++] = x1;            // Add X for Vertex 0
		vertexBuffer[bufferIndex++] = y1;            // Add Y for Vertex 0
		vertexBuffer[bufferIndex++] = region.u1;    // Add U for Vertex 0
		vertexBuffer[bufferIndex++] = region.v2;    // Add V for Vertex 0
		vertexBuffer[bufferIndex++] = numSprites;

		vertexBuffer[bufferIndex++] = x2;            // Add X for Vertex 1
		vertexBuffer[bufferIndex++] = y1;            // Add Y for Vertex 1
		vertexBuffer[bufferIndex++] = region.u2;    // Add U for Vertex 1
		vertexBuffer[bufferIndex++] = region.v2;    // Add V for Vertex 1
		vertexBuffer[bufferIndex++] = numSprites;

		vertexBuffer[bufferIndex++] = x2;            // Add X for Vertex 2
		vertexBuffer[bufferIndex++] = y2;            // Add Y for Vertex 2
		vertexBuffer[bufferIndex++] = region.u2;    // Add U for Vertex 2
		vertexBuffer[bufferIndex++] = region.v1;    // Add V for Vertex 2
		vertexBuffer[bufferIndex++] = numSprites;

		vertexBuffer[bufferIndex++] = x1;            // Add X for Vertex 3
		vertexBuffer[bufferIndex++] = y2;            // Add Y for Vertex 3
		vertexBuffer[bufferIndex++] = region.u1;    // Add U for Vertex 3
		vertexBuffer[bufferIndex++] = region.v1;    // Add V for Vertex 3
		vertexBuffer[bufferIndex++] = numSprites;

		// Add the sprite mvp matrix to uMVPMatrices array

		Matrix.multiplyMM(mMVPMatrix, 0, CGLRenderer.mProjectionMatrix, 0, mModelView, 0);

		// TODO: make sure numSprites < 24
		for (int i = 0; i < 16; ++i)
		{
			uMVPMatrices[numSprites * 16 + i] = mMVPMatrix[i];
		}

		numSprites++; // Increment Sprite Count
	}
}