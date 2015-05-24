package com.lescomber.vestige.graphics;

import com.lescomber.vestige.geometry.Point;

public interface Image {
	void offset(float dx, float dy);

	void offsetTo(float x, float y);

	void offsetTo(Point p);

	void rotate(float radians);

	void rotateTo(float radians);

	void rotateAbout(float radians, float rotateX, float rotateY);

	void scale(double widthRatio, double heightRatio);

	void scaleTo(float width, float height);

	void setAlpha(float alpha);

	void setLayerHeight(int layerHeight);

	float getX();

	float getY();

	float getDirection();

	int getIndex();

	Sprite getSprite();

	void setVisible(boolean isVisible);

	boolean isVisible();

	void close();

	void wasReplaced();

	void wasAdded(int newIndex);

	Image copy();
}