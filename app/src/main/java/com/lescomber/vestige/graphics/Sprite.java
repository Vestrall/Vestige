package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Point;

public class Sprite implements Image
{
	int index;
	final SpriteInfo info;
	
	boolean isVisible;
	
	public Sprite(SpriteTemplate template, float x, float y, boolean isVisible)
	{
		index = -1;
		info = new SpriteInfo(template, x, y);
		this.isVisible = false;
		setVisible(isVisible);
	}
	
	public Sprite(SpriteTemplate template, float x, float y)
	{
		this(template, x, y, false);
	}
	
	public Sprite(SpriteTemplate template)
	{
		this(template, 0, 0, false);
	}
	
	// This constructor is essentially just a wrapper to place a Sprite around a SpriteInfo. This Sprite will reference the
	//parameter info rather than making a copy
	public Sprite(SpriteInfo info)
	{
		index = -1;
		this.info = info;
		isVisible = false;
	}
	
	public Sprite(Sprite copyMe)
	{
		index = copyMe.index;
		info = new SpriteInfo(copyMe.info);
		isVisible = false;
		setVisible(copyMe.isVisible);
	}
	
	@Override
	public void offset(float dx, float dy)
	{
		info.x += dx;
		info.y += dy;
		if (isVisible)
			SpriteManager.getInstance().offset(index, dx, dy);
	}
	
	@Override
	public void offsetTo(float x, float y)
	{
		offset(x - info.x, y - info.y);
	}
	
	@Override
	public void offsetTo(Point p)
	{
		offsetTo(p.x, p.y);
	}
	
	@Override
	public void rotate(float radians)
	{
		info.direction = Angle.normalizeRadians(info.direction + radians);
		if (isVisible)
			SpriteManager.getInstance().rotate(index, radians);
	}
	
	@Override
	public void rotateTo(float radians)
	{
		rotate(radians - info.direction);
	}
	
	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		final Point center = new Point(info.x, info.y);
		Point.rotate(center, radians, rotateX, rotateY);
		offsetTo(center);
		rotate(radians);
	}
	
	@Override
	public void scale(double widthRatio, double heightRatio)
	{
		info.widthScale *= widthRatio;
		info.heightScale *= heightRatio;
		if (isVisible)
			SpriteManager.getInstance().scale(index, (float)widthRatio, (float)heightRatio);
	}
	
	@Override
	public void scaleTo(float width, float height)
	{
		if (info.widthScale <= 0 || info.heightScale <= 0)
		{
			info.widthScale = width / getSubTexWidth();
			info.heightScale = height / getSubTexHeight();
			
			if (isVisible)
				Swapper.swapImages(this, this);
			return;
		}
		
		final double widthRatio = (double)width / getWidth();
		final double heightRatio = (double)height / getHeight();
		
		scale(widthRatio, heightRatio);
	}
	
	public void scaleWidthTo(float width)
	{
		if (info.widthScale <= 0)
		{
			info.widthScale = width / getSubTexWidth();
			if (isVisible)
			{
				if (isVisible)
					Swapper.swapImages(this, this);
			}
		}
		else
		{
			final double widthRatio = (double)width / getWidth();
			scale(widthRatio, 1.0);
		}
	}
	
	public void scaleHeightTo(float height)
	{
		if (info.heightScale <= 0)
		{
			info.heightScale = height / getSubTexHeight();
			if (isVisible)
			{
				if (isVisible)
					Swapper.swapImages(this, this);
			}
		}
		else
		{
			final double heightRatio = (double)height / getHeight();
			scale(1.0, heightRatio);
		}
	}
	
	@Override
	public void setAlpha(float alpha)
	{
		info.alpha = alpha;
		if (isVisible)
			SpriteManager.getInstance().setAlpha(index, alpha);
	}
	
	@Override
	public void setLayerHeight(int layerHeight)
	{
		if (layerHeight >= SpriteManager.UI_LAYER_UNDER_ONE)
			layerHeight = SpriteManager.UI_LAYER_UNDER_ONE - 1;
		
		info.layerHeight = layerHeight;
		if (isVisible)
			SpriteManager.getInstance().setLayerHeight(index, layerHeight);
	}
	
	public int getLayerHeight() { return info.layerHeight; }
	
	// Sets the texRect based on a percentage of the original texRect width.
	public void setTexWidth(float percentage)
	{
		final android.graphics.Rect str = info.template.getSubTexRect();
		float curPercentage;
		
		// Init texRect if necessary
		if (info.texRect == null)
		{
			info.texRect = new float[4];
			info.texRect[0] = (float)str.left / info.template.getTexWidth();
			info.texRect[1] = (float)str.top / info.template.getTexHeight();
			// Note: skipping texRect[2] since it will be initialized right after this code block
			info.texRect[3] = (float)str.bottom / info.template.getTexHeight();
			curPercentage = 1;
		}
		else
			curPercentage = (info.texRect[2] - info.texRect[0]) / ((float)(str.right - str.left) / info.template.getTexWidth());
		
		info.texRect[2] = info.texRect[0] + (percentage * ((float)(str.right - str.left) / info.template.getTexWidth()));
		
		// Calculate xOffset required to keep left edge of the image in place while we shrink the right side
		//dx = (difference in width) / 2
		final float dx = ((percentage - curPercentage) * info.template.getWidth()) / 2;
		info.x += dx;
		
		// Avoid divide by 0 in wScalePercentage calculation below
		if (curPercentage > 0)
		{
			// Calculate vertex scaling (only in x direction)
			final float wScalePercentage = percentage / curPercentage;
			info.widthScale *= wScalePercentage;
			
			if (isVisible)
				SpriteManager.getInstance().setTexRect(index, info.texRect, dx, wScalePercentage);
		}
		else
		{
			info.widthScale = percentage;
			Swapper.swapImages(this, this);
		}
	}
	
	@Override public float getX() { return info.x; }
	@Override public float getY() { return info.y; }
	@Override public float getDirection() { return info.direction; }
	@Override public int getIndex() { return index; }
	public SpriteInfo getInfo() { return info; }
	@Override public Sprite getSprite() { return this; }
	public SpriteTemplate getTemplate() { return info.template; }
	public float getWidth() { return info.widthScale * info.template.getWidth(); }
	public float getHeight() { return info.heightScale * info.template.getHeight(); }
	public float getWidthScale() { return info.widthScale; }
	public float getHeightScale() { return info.heightScale; }
	
	public float getSubTexWidth()
	{
		if (info.texRect == null)
			return info.template.getWidth();
		else
			return info.texRect[2] - info.texRect[0];
	}
	
	public float getSubTexHeight()
	{
		if (info.texRect == null)
			return info.template.getHeight();
		else
			return info.texRect[3] - info.texRect[1];
	}
	
	@Override
	public void setVisible(boolean isVisible)
	{
		if (this.isVisible && !isVisible)
		{
			SpriteManager.getInstance().removeSprite(index);
			index = -1;
		}
		else if (!this.isVisible && isVisible)
		{
			index = SpriteManager.getInstance().newSprite(info);
		}
		
		this.isVisible = isVisible;
	}
	
	@Override public boolean isVisible() { return isVisible; }
	
	@Override
	public void close()
	{
		setVisible(false);
	}
	
	@Override
	public void wasReplaced()
	{
		isVisible = false;
		index = -1;
	}
	
	@Override
	public void wasAdded(int newIndex)
	{
		isVisible = true;
		index = newIndex;
	}
	
	@Override
	public Sprite copy()
	{
		return new Sprite(this);
	}
}