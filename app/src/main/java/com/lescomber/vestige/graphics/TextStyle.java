package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.TextManager;

public class TextStyle
{
	private int listNum;
	private int type;
	
	private final String fontFilename;
	private final int fontSize;
	private final int padX;
	private final int padY;
	private float spaceX;
	private final float[] color;	// Suggested color for each new text instance to be initialized with
	
	public TextStyle(String fontFilename, int fontSize, int padX, int padY, int r, int g, int b, float a)
	{
		this.fontFilename = fontFilename;
		this.fontSize = fontSize;
		this.padX = padX;
		this.padY = padY;
		spaceX = 0;
		color = new float[4];
		color[0] = r / 255f;
		color[1] = g / 255f;
		color[2] = b / 255f;
		color[3] = a;
		
		listNum = TextManager.getBuildListNum();
		type = TextManager.newTextStyle(fontFilename, fontSize, padX, padY, 0);
	}
	
	public TextStyle(String fontFilename, int fontSize, int r, int g, int b, float a)
	{
		this(fontFilename, fontSize, 3, 3, r, g, b, a);
	}
	
	public TextStyle(String fontFilename, int fontSize, int r, int g, int b)
	{
		this(fontFilename, fontSize, 3, 3, r, g, b, 1.0f);
	}
	
	public TextStyle(String fontFilename, int fontSize, int padX, int padY)
	{
		this(fontFilename, fontSize, padX, padY, 255, 255, 255, 1.0f);
	}
	
	public TextStyle(String fontFilename, int fontSize)
	{
		this(fontFilename, fontSize, 3, 3, 255, 255, 255, 1.0f);
	}
	
	public void setSpacing(float spaceX)
	{
		this.spaceX = spaceX;
		TextManager.setStyleSpacing(listNum, type, spaceX);
	}
	
	public float measureText(String text)
	{
		return TextManager.measureText(listNum, type, text);
	}
	
	public void recreate()
	{
		listNum = TextManager.getBuildListNum();
		type = TextManager.newTextStyle(fontFilename, fontSize, padX, padY, spaceX);
	}
	
	public float[] getColor() { return color; }
	public int getType() { return type; }
	public int getFontSize() { return fontSize; }
}