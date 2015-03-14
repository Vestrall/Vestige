package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.TextManager;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Point;

public class Text
{
	public enum Alignment {
		LEFT, CENTER, RIGHT
	}
	
	private TextStyle style;
	private int listNum;
	private int index;
	
	private String text;
	private float swingX;
	private float swingY;
	private float centerX;
	private float centerY;
	private final Alignment alignment;
	private float direction;
	private final float[] color;
	
	private boolean isVisible;
	
	public Text(TextStyle style, String text, float swingX, float swingY, Alignment alignment, boolean isVisible)
	{
		this.style = style;
		index = -1;
		
		this.text = text;
		this.swingX = swingX;
		this.swingY = swingY - 2;	// Note: Hack to move text up slightly as it appears to be slightly lower than expected
		this.alignment = alignment;
		direction = 0;
		color = new float[4];
		final float[] styleColor = style.getColor();
		for (int i=0; i<4; i++)
			color[i] = styleColor[i];
		
		centerX = this.swingX;
		centerY = this.swingY;
		if (alignment == Alignment.LEFT)
			centerX = this.swingX + (measure() / 2);
		else if (alignment == Alignment.RIGHT)
			centerX = this.swingX - (measure() / 2);
		
		this.isVisible = false;
		setVisible(isVisible);
	}
	
	public Text(TextStyle style, String text, float swingX, float swingY, boolean isVisible)
	{
		this(style, text, swingX, swingY, Alignment.CENTER, isVisible);
	}
	
	public Text(TextStyle style, String text, float swingX, float swingY, Alignment alignment)
	{
		this(style, text, swingX, swingY, alignment, true);
	}
	
	public Text(TextStyle style, String text, float swingX, float swingY)
	{
		this(style, text, swingX, swingY, Alignment.CENTER, true);
	}
	
	public void setText(String text)
	{
		if (alignment == Alignment.CENTER)
		{
			this.text = text;
			if (isVisible)
				TextManager.setString(listNum, style.getType(), index, text);
		}
		else
		{
			final float curWidth = measure();
			final float offsetXRatio = (centerX - swingX) / curWidth;
			final float offsetYRatio = (centerY - swingY) / curWidth;
			
			this.text = text;
			
			final float newWidth = measure();
			final float newCenterX = swingX + (offsetXRatio * newWidth);
			final float newCenterY = swingY + (offsetYRatio * newWidth);
			
			if (isVisible)
			{
				TextManager.offsetString(listNum, style.getType(), index, newCenterX - centerX, newCenterY - centerY);
				TextManager.setString(listNum, style.getType(), index, text);
			}
			
			centerX = newCenterX;
			centerY = newCenterY;
		}
	}
	
	public void setColor(int r, int g, int b, float a)
	{
		color[0] = r / 255f;
		color[1] = g / 255f;
		color[2] = b / 255f;
		color[3] = a;
		
		if (isVisible)
			TextManager.setStringColor(listNum, style.getType(), index, color[0], color[1], color[2], color[3]);
	}
	
	public void setAlpha(float alpha)
	{
		color[3] = alpha;
		
		if (isVisible)
			TextManager.setStringColor(listNum, style.getType(), index, color[0], color[1], color[2], color[3]);
	}
	
	public void setStyle(TextStyle style)
	{
		final int oldType = this.style.getType();
		
		this.style = style;
		
		final float styleColor[] = style.getColor();
		for (int i=0; i<4; i++)
			color[i] = styleColor[i];
		
		if (alignment == Alignment.CENTER)
		{
			if (isVisible)
			{
				index = TextManager.setStringType(listNum, oldType, index, style.getType());
				TextManager.setStringColor(listNum, style.getType(), index, color[0], color[1], color[2], color[3]);
			}
		}
		else
		{
			final float curWidth = measure();
			final float offsetXRatio = (centerX - swingX) / curWidth;
			final float offsetYRatio = (centerY - swingY) / curWidth;
			
			if (isVisible)
			{
				index = TextManager.setStringType(listNum, oldType, index, style.getType());
				TextManager.setStringColor(listNum, style.getType(), index, color[0], color[1], color[2], color[3]);
			}
			
			final float newWidth = measure();
			final float newCenterX = swingX + (offsetXRatio * newWidth);
			final float newCenterY = swingY + (offsetYRatio * newWidth);
			
			if (isVisible)
				TextManager.offsetString(listNum, style.getType(), index, newCenterX - centerX, newCenterY - centerY);
			
			centerX = newCenterX;
			centerY = newCenterY;
		}
	}
	
	public void offset(float dx, float dy)
	{
		swingX += dx;
		swingY += dy;
		centerX += dx;
		centerY += dy;
		if (isVisible)
			TextManager.offsetString(listNum, style.getType(), index, dx, dy);
	}
	
	public void offsetTo(float x, float y)
	{
		offset(x - swingX, y - swingY);
	}
	
	public void offsetTo(Point p)
	{
		offsetTo(p.x, p.y);
	}
	
	public void rotate(float radians)
	{
		direction = Angle.normalizeRadians(direction + radians);
		if (isVisible)
			TextManager.rotateString(listNum, style.getType(), index, (float)Math.toDegrees(radians));
		
		if (alignment != Alignment.CENTER)
		{
			final Point center = new Point(centerX, centerY);
			Point.rotate(center, radians, swingX, swingY);
			final float dx = center.x - centerX;
			final float dy = center.y - centerY;
			centerX = center.x;
			centerY = center.y;
			if (isVisible)
				TextManager.offsetString(listNum, style.getType(), index, dx, dy);
		}
	}
	
	public void rotateTo(float radians)
	{
		rotate(radians - direction);
	}
	
	public float measure()
	{
		return style.measureText(text);
	}
	
	public float getX() { return swingX; }
	public float getY() { return swingY; }
	public TextStyle getStyle() { return style; }
	
	public void setVisible(boolean isVisible)
	{
		if (!this.isVisible && isVisible)
		{
			listNum = TextManager.getBuildListNum();
			index = TextManager.newString(style.getType(), text, centerX, centerY, (float)Math.toDegrees(direction),
					color[0], color[1], color[2], color[3]);
		}
		else if (this.isVisible && !isVisible)
		{
			TextManager.removeString(listNum, style.getType(), index);
			index = -1;
		}
		
		this.isVisible = isVisible;
	}
	
	public boolean isVisible() { return isVisible; }
	
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
	
	public void wasRemoved()
	{
		isVisible = false;
		index = -1;
	}
}