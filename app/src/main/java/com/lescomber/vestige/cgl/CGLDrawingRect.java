package com.lescomber.vestige.cgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CGLDrawingRect
{
	FloatBuffer vertexBuffer;
	
	public static final int COORDS_PER_VERTEX = 2;		// x,y
	public static final int TEX_COORDS_PER_VERTEX = 2;	// s,t
	public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;	// 4 bytes per float
	public static final int TEX_VERTEX_STRIDE = TEX_COORDS_PER_VERTEX * 4;
	
	// vertex count should be the same for both COORDS and TEX_COORDS
	public static final int VERTEX_COUNT = 4;
	public static final int COORDS_PER_RECT = COORDS_PER_VERTEX * VERTEX_COUNT;
	
	private float halfWidth;
	private float halfHeight;
	
	public CGLDrawingRect(float x, float y, float width, float height)
	{
		// Init vertices
		halfWidth = width / 2;
		halfHeight = height / 2;
		
		final float[] coords = {
			    x - halfWidth, y - halfHeight,   // 0 bottom left
			    x + halfWidth, y - halfHeight,   // 1 bottom right
			    x - halfWidth, y + halfHeight,   // 2 top left
			    x + halfWidth, y + halfHeight,   // 3 top right
				};
		vertexBuffer = createVertexArray(coords);
	}
	
	/**
	 * Allocates a direct float buffer, and populates it with the vertex data.
	 */
	static FloatBuffer createVertexArray(float[] coords)
	{
		// Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
		final ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		final FloatBuffer fb = bb.asFloatBuffer();
		fb.put(coords);
		fb.position(0);
		return fb;
	}
	
	public float getXPosition()
	{
		return (vertexBuffer.get(0) + vertexBuffer.get(6)) / 2;
	}
	
	public float getYPosition()
	{
		return (vertexBuffer.get(1) + vertexBuffer.get(7)) / 2;
	}
	
	public void offset(float dx, float dy)
	{
		for (int i=0; i<COORDS_PER_RECT; i+=2)
		{
			vertexBuffer.put(i, vertexBuffer.get(i) + dx);
			vertexBuffer.put(i+1, vertexBuffer.get(i+1) + dy);
		}
	}
	
	// Rotate this rectangle clockwise by degrees
	public void rotate(float radians)
	{
		// Calculate the sin and cos for the angle of rotation (calculate once, will be the same for each vertex)
		final double cos = Math.cos(radians);
		final double sin = Math.sin(radians);
		
		// The (x,y) coords about which we will rotate
		final float rotateX = getXPosition();
		final float rotateY = getYPosition();
		
		// Rotate the vertices
		double tempX;
		double tempY;
		for (int i=0; i<COORDS_PER_RECT; i+=2)
		{
			tempX = cos * (vertexBuffer.get(i) - rotateX) - sin * (vertexBuffer.get(i+1) - rotateY) + rotateX;
			tempY = sin * (vertexBuffer.get(i) - rotateX) + cos * (vertexBuffer.get(i+1) - rotateY) + rotateY;
			vertexBuffer.put(i, (float)tempX);
			vertexBuffer.put(i+1, (float)tempY);
		}
	}
	
	public void scale(float widthRatio, float heightRatio)
	{
		// Update halfWidth/Height
		halfWidth *= widthRatio;
		halfHeight *= heightRatio;
		
		// Get current center point
		final float centerX = getXPosition();
		final float centerY = getYPosition();
		
		// Calculate the current angle and its sin and cos (calculate once, will be the same for each vertex)
		final double ddx = vertexBuffer.get(2) - vertexBuffer.get(0);
		double curAngle = Math.atan((vertexBuffer.get(3) - vertexBuffer.get(1)) / ddx);
		if (ddx < 0)
			curAngle += Math.PI;
		final double cos = Math.cos(curAngle);
		final double sin = Math.sin(curAngle);
		
		// Each vertex is placed as if the rectangle is axis aligned (i.e. angle = 0) and then rotated into position
		//(e.g. bottom left vertex would be placed at (centerX - halfWidth, centerY - halfHeight) then rotated
		
		// Bottom left vertex
		vertexBuffer.put(0, (float)(cos * (-halfWidth) - sin * (-halfHeight) + centerX));
		vertexBuffer.put(1, (float)(sin * (-halfWidth) + cos * (-halfHeight) + centerY));
		
		// Bottom right vertex
		vertexBuffer.put(2, (float)(cos * (halfWidth) - sin * (-halfHeight) + centerX));
		vertexBuffer.put(3, (float)(sin * (halfWidth) + cos * (-halfHeight) + centerY));
		
		// Top left vertex
		vertexBuffer.put(4, (float)(cos * (-halfWidth) - sin * (halfHeight) + centerX));
		vertexBuffer.put(5, (float)(sin * (-halfWidth) + cos * (halfHeight) + centerY));
		
		// Top right vertex
		vertexBuffer.put(6, (float)(cos * (halfWidth) - sin * (halfHeight) + centerX));
		vertexBuffer.put(7, (float)(sin * (halfWidth) + cos * (halfHeight) + centerY));
	}
	
	public float getWidth() { return 2 * halfWidth; }
	public float getHeight() { return 2 * halfHeight; }
}