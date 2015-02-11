package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Point;

public class SwingSprite extends Sprite
{
	private float swingX;
	private float swingY;
	private final float offsetX;
	private final float offsetY;
	
	public SwingSprite(SpriteTemplate template, float x, float y, float offsetX, float offsetY, boolean isVisible)
	{
		super(template, x - offsetX, y - offsetY, isVisible);
		
		swingX = x;
		swingY = y;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public SwingSprite(SpriteTemplate template, float x, float y, float offsetX, float offsetY)
	{
		this(template, x, y, offsetX, offsetY, false);
	}
	
	public SwingSprite(SpriteInfo info, float offsetX, float offsetY)
	{
		super(info);
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		final Point p = new Point(info.x + offsetX, info.y + offsetY);
		Point.rotate(p, info.direction, info.x, info.y);
		swingX = p.x;
		swingY = p.y;
	}
	
	public SwingSprite(SwingSprite copyMe)
	{
		super(copyMe);
		
		swingX = copyMe.swingX;
		swingY = copyMe.swingY;
		offsetX = copyMe.offsetX;
		offsetY = copyMe.offsetY;
	}
	
	@Override
	public void offset(float dx, float dy)
	{
		swingX += dx;
		swingY += dy;
		super.offset(dx, dy);
	}
	
	@Override
	public void offsetTo(float x, float y)
	{
		offset(x - swingX, y - swingY);
	}
	
	@Override
	public void rotate(float radians)
	{
		// Rotate the Sprite's center point about our swing point
		final Point center = new Point(getX(), getY());
		Point.rotate(center, radians, swingX, swingY);
		final float dx = center.x - getX();
		final float dy = center.y - getY();
		super.offset(dx, dy);		// Note super.offset() will move the center point of the sprite
		
		// Then rotate the Sprite in place
		super.rotate(radians);
	}
	
	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		final Point swingPoint = new Point(swingX, swingY);
		Point.rotate(swingPoint, radians, rotateX, rotateY);
		offsetTo(swingPoint);
		rotate(radians);
	}
	
	@Override
	public void scale(double widthRatio, double heightRatio)
	{
		final float halfWidthDif = (float)((widthRatio - 1.0) * getWidth()) / 2;
		final float halfHeightDif = (float)((heightRatio - 1.0) * getHeight()) / 2;
		
		super.scale(widthRatio, heightRatio);
		
		final float widthSign = offsetX <= 0 ? 1 : -1;
		final float heightSign = offsetY <= 0 ? 1 : -1;
		
		float xOff = widthSign * ((float)(Math.cos(getDirection()) * halfWidthDif));
		float yOff = widthSign * ((float)(Math.sin(getDirection()) * halfWidthDif));
		xOff += heightSign * ((float)(Math.cos(getDirection() + (Math.PI / 2)) * halfHeightDif));
		yOff += heightSign * ((float)(Math.sin(getDirection() + (Math.PI / 2)) * halfHeightDif));
		
		super.offset(xOff, yOff);
	}
	
	public float getSwingX() { return swingX; }
	public float getSwingY() { return swingY; }
}