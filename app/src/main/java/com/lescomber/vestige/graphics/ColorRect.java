package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.ColorRectManager;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Point;

public class ColorRect
{
	private int listNum;
	private int index;	// ColorRectManager reference int
	
	private float x;
	private float y;
	private float width;
	private float height;
	private float direction;
	private final float[] color;
	
	private boolean isVisible;
	
	public ColorRect(float x, float y, float width, float height, int r, int g, int b, float a, boolean isVisible)
	{
		this.isVisible = false;
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		direction = 0;
		color = new float[4];
		setColor(r, g, b, a);
		
		index = -1;
		setVisible(isVisible);
	}
	
	public ColorRect(float x, float y, float width, float height, int r, int g, int b, float a)
	{
		this(x, y, width, height, r, g, b, a, false);
	}
	
	public ColorRect(float x, float y, float width, float height, int r, int g, int b)
	{
		this(x, y, width, height, r, g, b, 1, false);
	}
	
	public ColorRect(ColorRect copyMe)
	{
		x = copyMe.x;
		y = copyMe.y;
		width = copyMe.width;
		height = copyMe.height;
		direction = copyMe.direction;
		color = new float[4];
		for (int i=0; i<4; i++)
			color[i] = copyMe.color[i];
		index = -1;
		isVisible = false;
		setVisible(copyMe.isVisible);
	}
	
	public void setColor(int r, int g, int b, float a)
	{
		color[0] = r / 255f;
		color[1] = g / 255f;
		color[2] = b / 255f;
		color[3] = a;
		if (isVisible)
			ColorRectManager.setColorRectColor(listNum, index, color[0], color[1], color[2], color[3]);
	}
	
	public void setColor(int r, int g, int b)
	{
		setColor(r, g, b, 1);
	}
	
	public void setAlpha(float alpha)
	{
		color[3] = alpha;
		if (isVisible)
			ColorRectManager.setColorRectAlpha(listNum, index, alpha);
	}
	
	public void setSize(float width, float height)
	{
		final float widthRatio = width / this.width;
		final float heightRatio = height / this.height;
		scale(widthRatio, heightRatio);
	}
	
	public void offset(float dx, float dy)
	{
		x += dx;
		y += dy;
		if (isVisible)
			ColorRectManager.offsetColorRect(listNum, index, dx, dy);
	}
	
	public void offsetTo(float x, float y)
	{
		offset(x - this.x, y - this.y);
	}
	
	public void offsetTo(Point position)
	{
		offsetTo(position.x, position.y);
	}
	
	public void rotate(float radians)
	{
		direction = Angle.normalizeRadians(direction + radians);
		if (isVisible)
			ColorRectManager.rotateColorRect(listNum, index, (float)Math.toDegrees(radians));
	}
	
	public void rotateTo(float radians)
	{
		rotate(radians - direction);
	}
	
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		final Point center = new Point(x, y);
		Point.rotate(center, radians, rotateX, rotateY);
		offsetTo(center);
		rotate(radians);
	}
	
	public void scale(double widthRatio, double heightRatio)
	{
		width *= widthRatio;
		height *= heightRatio;
		if (isVisible)
			ColorRectManager.scaleColorRect(listNum, index, (float)widthRatio, (float)heightRatio);
	}
	
	public void scaleTo(float width, float height)
	{
		if ((this.width == 0 || this.height == 0) && isVisible)
		{
			this.width = width;
			this.height = height;
			setVisible(false);
			setVisible(true);
		}
		else
		{
			final double widthRatio = (double)width / this.width;
			final double heightRatio = (double)height / this.height;
			scale(widthRatio, heightRatio);
		}
	}
	
	public void setVisible(boolean isVisible)
	{
		if (!this.isVisible && isVisible)
		{
			listNum = ColorRectManager.getBuildListNum();
			index = ColorRectManager.newColorRect(x, y, width, height, color[0], color[1], color[2], color[3]);
			if (direction != 0)
				ColorRectManager.rotateColorRect(listNum, index, (float)Math.toDegrees(direction));
		}
		else if (this.isVisible && !isVisible)
		{
			ColorRectManager.removeColorRect(listNum, index);
			index = -1;
		}
		
		this.isVisible = isVisible;
	}
	
	public void recreate()
	{
		if (isVisible)
		{
			isVisible = false;
			setVisible(true);
		}
	}
	
	public void close()
	{
		setVisible(false);
	}
	
	public void setWidth(float width)
	{
		if (this.width <= 0)
		{
			this.width = width;
			if (isVisible)
				ColorRectManager.replaceColorRect(listNum, index, x, y, width, height, color[0], color[1], color[2], color[3]);
		}
		else
		{
			final double scaleX = width / this.width;
			scale(scaleX, 1.0);
		}
	}
	
	public void setHeight(float height)
	{
		if (this.height <= 0)
		{
			this.height = height;
			if (isVisible)
				ColorRectManager.replaceColorRect(listNum, index, x, y, width, height, color[0], color[1], color[2], color[3]);
		}
		else
		{
			final double scaleY = height / this.height;
			scale(1.0, scaleY);
		}
	}
	
	public float getX() { return x; }
	public float getY() { return y; }
	public float getWidth() { return width; }
	public float getHeight() { return height; }
	public float getLeft() { return x - (width / 2); }
	public float getTop() { return y - (height / 2); }
	public float getRight() { return x + (width / 2); }
	public float getBottom() { return y + (height / 2); }
	public boolean isVisible() { return isVisible; }
}