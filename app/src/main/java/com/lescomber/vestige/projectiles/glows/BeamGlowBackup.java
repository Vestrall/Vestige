package com.lescomber.vestige.projectiles.glows;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;

public class BeamGlowBackup extends RotatedRectGlow
{
	private static final float MIN_WIDTH = (SpriteManager.redLaserGlowEnd.getWidth() * 2) + 1;

	private float x;
	private float y;

	private final Sprite body;
	private final Sprite[] ends;

	public BeamGlowBackup()
	{
		super((SpriteTemplate) null);

		x = 0;
		y = 0;

		body = new Sprite(SpriteManager.redLaserGlowBody);

		// Note: endsOffset includes a -1 for overlap purposes
		final float endsOffset = ((SpriteManager.redLaserGlowBody.getWidth() + SpriteManager.redLaserGlowEnd.getWidth()) / 2) - 1;

		ends = new Sprite[2];
		ends[0] = new Sprite(SpriteManager.redLaserGlowEnd, -endsOffset, 0);
		ends[0].rotate((float) Math.PI);
		ends[1] = new Sprite(SpriteManager.redLaserGlowEnd, endsOffset, 0);
	}

	public BeamGlowBackup(BeamGlowBackup copyMe)
	{
		super(copyMe);

		x = copyMe.x;
		y = copyMe.y;

		body = new Sprite(copyMe.body);
		ends = new Sprite[2];
		ends[0] = new Sprite(copyMe.ends[0]);
		ends[1] = new Sprite(copyMe.ends[1]);
	}

	@Override
	public void offset(float dx, float dy)
	{
		x += dx;
		y += dy;
		body.offset(dx, dy);
		ends[0].offset(dx, dy);
		ends[1].offset(dx, dy);
	}

	@Override
	public void offsetTo(float x, float y)
	{
		offset(x - this.x, y - this.y);
	}

	@Override
	public void offsetTo(Point p)
	{
		offsetTo(p.x, p.y);
	}

	@Override
	public void rotate(float radians)
	{
		// Rotate body
		body.rotateAbout(radians, x, y);

		// Rotate ends
		ends[0].rotateAbout(radians, x, y);
		ends[1].rotateAbout(radians, x, y);
	}

	@Override
	public void rotateTo(float radians)
	{
		rotate(radians - getDirection());
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		// Update coords
		final Point xy = new Point(x, y);
		Point.rotate(xy, radians, rotateX, rotateY);
		x = xy.x;
		y = xy.y;

		// Update image(s)
		body.rotateAbout(radians, rotateX, rotateY);
		ends[0].rotateAbout(radians, rotateX, rotateY);
		ends[1].rotateAbout(radians, rotateX, rotateY);
	}

	@Override
	public void scale(double widthRatio, double heightRatio)
	{
		if (heightRatio != 1 && heightRatio >= 0)
			scaleHeightTo((float) (heightRatio * getHeight()));
		if (widthRatio != 1 && widthRatio >= 0)
			scaleWidthTo((float) (widthRatio * getWidth()));
		
		/*if (heightRatio == 1)
			scaleWidthTo((float)(widthRatio * getWidth()));
		else if (widthRatio == 1)
			scaleHeightTo((float)(heightRatio * getHeight()));
		else
			scaleTo((float)(widthRatio * getWidth()), (float)(heightRatio * getHeight()));*/
	}

	@Override
	public void scaleTo(float width, float height)
	{
		scaleWidthTo(width);
		scaleHeightTo(height);
		
		/*float dWidth = width - getWidth();
		body.scaleWidthTo(body.getWidth() + dWidth);
		scaleHeightTo(height);
		
		for (int i=0; i<2; i++)
		{
			Point endSpriteLocation = new Point(ends[i].getX(), ends[i].getY());
			endSpriteLocation = endSpriteLocation.getPointFromDirection(ends[i].getDirection(), dWidth / 2);
			ends[i].offsetTo(endSpriteLocation);
		}*/
	}

	@Override
	public void scaleWidthTo(float width)
	{
		if (width <= 0)
		{
			return;        // FIXME
			
			/*body.setVisible(false);
			ends[0].setVisible(false);
			ends[1].setVisible(false);
			return;*/
		}
		else if (width <= MIN_WIDTH)
		{
			//float dWidth = width - getWidth();

			// Shrink body to a width of 1 and record the shrinkage amount for moving end pieces later
			float bodyDWidth = 0;
			if (body.getWidth() > 1)
			{
				bodyDWidth = 1 - body.getWidth();
				body.scaleWidthTo(1);
			}

			if (width <= 3)
			{
				final float endDWidth = 1 - ends[0].getWidth();
				for (int i = 0; i < 2; i++)
				{
					ends[i].scaleWidthTo(1);
					Point endSpriteLocation = new Point(ends[i].getX(), ends[i].getY());
					endSpriteLocation =
							endSpriteLocation.getPointFromDirection(ends[i].getDirection(), (endDWidth + bodyDWidth) / 2);
					ends[i].offsetTo(endSpriteLocation);
				}
			}
			else
			{
				final float endWidth = (width - 1) / 2;
				final float endDWidth = endWidth - ends[0].getWidth();
				for (int i = 0; i < 2; i++)
				{
					ends[i].scaleWidthTo(ends[i].getWidth() + endDWidth);
					Point endSpriteLocation = new Point(ends[i].getX(), ends[i].getY());
					endSpriteLocation =
							endSpriteLocation.getPointFromDirection(ends[i].getDirection(), (endDWidth + bodyDWidth) / 2);
					ends[i].offsetTo(endSpriteLocation);
				}
			}
		}
		else
		{
			float endDWidth = 0;

			// If end pieces had previously been scaled down, scale them back to original size and reposition them accordingly
			if (ends[0].getWidth() < ends[0].getTemplate().getWidth())    // ends[1] is assumed to also have been shrunk
			{
				endDWidth = ends[0].getTemplate().getWidth() - ends[0].getWidth();

				for (int i = 0; i < 2; i++)
				{
					ends[i].scaleWidthTo(ends[i].getTemplate().getWidth());
					Point endSpriteLocation = new Point(ends[i].getX(), ends[i].getY());
					endSpriteLocation = endSpriteLocation.getPointFromDirection(ends[i].getDirection(), endDWidth / 2);
					ends[i].offsetTo(endSpriteLocation);
				}
			}

			final float dWidth = width - (2 * endDWidth) - getWidth();
			body.scaleWidthTo(body.getWidth() + dWidth);

			for (int i = 0; i < 2; i++)
			{
				Point endSpriteLocation = new Point(ends[i].getX(), ends[i].getY());
				endSpriteLocation = endSpriteLocation.getPointFromDirection(ends[i].getDirection(), dWidth / 2);
				ends[i].offsetTo(endSpriteLocation);
			}
		}
	}

	@Override
	public void scaleHeightTo(float height)
	{
		body.scaleHeightTo(height);
		ends[0].scaleHeightTo(height);
		ends[1].scaleHeightTo(height);
	}

	@Override
	public void setAlpha(float alpha)
	{
		body.setAlpha(alpha);
		ends[0].setAlpha(alpha);
		ends[1].setAlpha(alpha);
	}

	@Override
	public void setLayerHeight(int layerHeight)
	{
		body.setLayerHeight(layerHeight);
		ends[0].setLayerHeight(layerHeight);
		ends[1].setLayerHeight(layerHeight);
	}

	@Override
	public int getLayerHeight()
	{
		return body.getLayerHeight();
	}

	@Override
	public void setTexWidth(float percentage)
	{
	}        //Note: setTexWidth doesn't work for BeamGlow

	@Override
	public float getX()
	{
		return x;
	}

	@Override
	public float getY()
	{
		return y;
	}

	@Override
	public float getDirection()
	{
		return body.getDirection();
	}

	@Override
	public int getIndex()
	{
		return body.getIndex();
	}

	@Override
	public SpriteInfo getInfo()
	{
		return body.getInfo();
	}

	@Override
	public Sprite getSprite()
	{
		return body;
	}

	@Override
	public SpriteTemplate getTemplate()
	{
		return body.getTemplate();
	}

	@Override
	public float getWidth()
	{
		return body.getWidth() + ends[0].getWidth() + ends[1].getWidth();
	}

	@Override
	public float getHeight()
	{
		return body.getHeight();
	}

	@Override
	public float getWidthScale()
	{
		return getWidth() / (SpriteManager.redLaserGlowBody.getWidth() + (2 * SpriteManager.redLaserGlowEnd.getWidth()));
	}

	@Override
	public float getHeightScale()
	{
		return getHeight() / (SpriteManager.redLaserGlowBody.getHeight());
	}

	@Override
	public float getSubTexWidth()
	{
		return 0;
	}        // Doesn't work

	@Override
	public float getSubTexHeight()
	{
		return 0;
	}        // Doesn't work

	@Override
	public void setVisible(boolean isVisible)
	{
		if (body != null)
			body.setVisible(isVisible);

		if (ends != null)
		{
			for (int i = 0; i < 2; i++)
				ends[i].setVisible(isVisible);
		}
	}

	@Override
	public boolean isVisible()
	{
		return body.isVisible();
	}

	@Override
	public void close()
	{
		body.close();
		ends[0].close();
		ends[1].close();
	}

	@Override
	public void wasReplaced()
	{
		body.wasReplaced();
		ends[0].setVisible(false);
		ends[1].setVisible(false);
	}

	@Override
	public void wasAdded(int newIndex)
	{
		body.wasAdded(newIndex);
		ends[0].setVisible(true);
		ends[1].setVisible(true);
	}

	@Override
	public BeamGlowBackup copy()
	{
		return new BeamGlowBackup(this);
	}
}