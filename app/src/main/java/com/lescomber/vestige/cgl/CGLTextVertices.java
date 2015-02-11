package com.lescomber.vestige.cgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class CGLTextVertices
{
	private static final int POSITION_CNT_2D = 2;		// Number of components in vertex position for 2D
	private static final int TEXCOORD_CNT = 2;			// Number of components in vertex texture coords
	private static final int MVP_MATRIX_INDEX_CNT = 1;	// Number of components in MVP matrix index
	
	private static final int INDEX_SIZE = Short.SIZE / 8;	// Index byte size (Short.SIZE = bits)
	
	public final int positionCnt;		// Number of position components (2=2D, 3=3D)
	public final int vertexStride;		// Vertex stride (element size of a single vertex)
	public final int vertexSize;		// Byte size of a single vertex
	private final IntBuffer vertices;	// Vertex buffer
	private final ShortBuffer indices;	// Index buffer
	public int numVertices;				// Number of vertices in buffer
	public int numIndices;				// Number of indices in buffer
	private final int[] tmpBuffer;		// Temp buffer for vertex conversion
	
	private final int mTextureCoordinateHandle;
	private final int mPositionHandle;
	private final int mMVPIndexHandle;
	
	//--Constructor--//
	// D: create the vertices/indices as specified (for 2d/3d)
	// A: maxVertices - maximum vertices allowed in buffer
	//    maxIndices - maximum indices allowed in buffer
	public CGLTextVertices(int maxVertices, int maxIndices, int programHandle)
	{
		positionCnt = POSITION_CNT_2D;  // Set position component count
		vertexStride = positionCnt + TEXCOORD_CNT + MVP_MATRIX_INDEX_CNT;
		vertexSize = vertexStride * 4;        // Calculate vertex byte size

		ByteBuffer buffer = ByteBuffer.allocateDirect( maxVertices * vertexSize );  // Allocate buffer for vertices (max)
		buffer.order(ByteOrder.nativeOrder());        // Set native byte order
		vertices = buffer.asIntBuffer();           // Save vertex buffer

		if (maxIndices > 0)
		{
			buffer = ByteBuffer.allocateDirect( maxIndices * INDEX_SIZE );  // Allocate buffer for indices (max)
			buffer.order(ByteOrder.nativeOrder());     // Set native byte order
			indices = buffer.asShortBuffer();       // Save index buffer
		}
		else	// Indices not required
			indices = null;		// No index buffer

		numVertices = 0;	// Zero vertices in buffer
		numIndices = 0;		// Zero indices in buffer

		tmpBuffer = new int[maxVertices * vertexSize / 4];  // Create temp buffer

		// Initialize the shader attribute handles
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate");
		mMVPIndexHandle = GLES20.glGetAttribLocation(programHandle, "a_MVPMatrixIndex");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
	}
	
	//--Set Vertices--//
	// D: set the specified vertices in the vertex buffer
	//    NOTE: optimized to use integer buffer!
	// A: vertices - array of vertices (floats) to set
	//    offset - offset to first vertex in array
	//    length - number of floats in the vertex array (total)
	//             for easy setting use: vtx_cnt * (this.vertexSize / 4)
	// R: [none]
	public void setVertices(float[] vertices, int offset, int length)
	{
		this.vertices.clear();			// Remove existing vertices
		final int last = offset + length;		// Calculate last element
		for (int i = offset, j = 0; i < last; i++, j++)	// For each specified vertex
			tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);	// Set vertex as raw integer bits in buffer
		this.vertices.put(tmpBuffer, 0, length);		// Set new vertices
		this.vertices.flip();							// Flip vertex buffer
		numVertices = length / vertexStride;	// Save number of vertices
	}
	
	//--Set Indices--//
	// D: set the specified indices in the index buffer
	// A: indices - array of indices (shorts) to set
	//    offset - offset to first index in array
	//    length - number of indices in array (from offset)
	// R: [none]
	public void setIndices(short[] indices, int offset, int length)
	{
		this.indices.clear();                           // Clear existing indices
		this.indices.put(indices, offset, length);    // Set new indices
		this.indices.flip();                            // Flip index buffer
		numIndices = length;                       // Save number of indices
	}
	
	//--Bind--//
	// D: perform all required binding/state changes before rendering batches.
	//    USAGE: call once before calling draw() multiple times for this buffer.
	// A: [none]
	// R: [none]
	public void bind()
	{
		// Bind vertex position pointer
		vertices.position(0);		// Set Vertex Buffer to Position
		GLES20.glVertexAttribPointer(mPositionHandle, positionCnt, GLES20.GL_FLOAT, false, vertexSize, vertices);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Bind texture position pointer
		vertices.position(positionCnt);	// Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXCOORD_CNT, GLES20.GL_FLOAT, false, vertexSize, vertices);
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
		
		// Bind MVP matrix index position handle
		vertices.position(positionCnt + TEXCOORD_CNT);
		GLES20.glVertexAttribPointer(mMVPIndexHandle, MVP_MATRIX_INDEX_CNT, GLES20.GL_FLOAT, false, vertexSize, vertices);
		GLES20.glEnableVertexAttribArray(mMVPIndexHandle);
	}
	
	//--Draw--//
	// D: draw the currently bound vertices in the vertex/index buffers
	//    USAGE: can only be called after calling bind() for this buffer.
	// A: primitiveType - the type of primitive to draw
	//    offset - the offset in the vertex/index buffer to start at
	//    numVertices - the number of vertices (indices) to draw
	// R: [none]
	public void draw(int primitiveType, int offset, int numVertices)
	{
		if (indices != null)
		{
			indices.position(offset);	// Set Index Buffer to Specified Offset
			
			GLES20.glDrawElements(primitiveType, numVertices, GLES20.GL_UNSIGNED_SHORT, indices);	// Draw indexed
		}
		else
			GLES20.glDrawArrays(primitiveType, offset, numVertices);	// Draw direct
	}
	
	//--Unbind--//
	// D: clear binding states when done rendering batches.
	//    USAGE: call once before calling draw() multiple times for this buffer.
	// A: [none]
	// R: [none]
	public void unbind()
	{
		GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
	}
}