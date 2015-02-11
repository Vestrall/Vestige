package com.lescomber.vestige.graphics;

import com.lescomber.vestige.geometry.Point;

public interface Image
{
	public void offset(float dx, float dy);
	public void offsetTo(float x, float y);
	public void offsetTo(Point p);
	public void rotate(float radians);
	public void rotateTo(float radians);
	public void rotateAbout(float radians, float rotateX, float rotateY);
	public void scale(double widthRatio, double heightRatio);
	public void scaleTo(float width, float height);
	public void setAlpha(float alpha);
	public void setLayerHeight(int layerHeight);
	
	public float getX();
	public float getY();
	public float getDirection();
	public int getIndex();
	public Sprite getSprite();
	
	public void setVisible(boolean isVisible);
	public boolean isVisible();
	public void close();
	public void wasReplaced();
	public void wasAdded(int newIndex);
	
	public Image copy();
}