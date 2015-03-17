package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.TextManager;

public class TextStyle
{
	private final int listNum;
	private final int type;

	private final String fontFilename;
	private final int fontSize;
	private final int padX;
	private final int padY;
	private float spaceX;
	private final float[] color;    // Suggested color for each new text instance to be initialized with

	private TextStyle(String fontFilename, int fontSize, int padX, int padY, int r, int g, int b, float a)
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

	private TextStyle(String fontFilename, int fontSize, int r, int g, int b, float a)
	{
		this(fontFilename, fontSize, 3, 3, r, g, b, a);
	}

	private TextStyle(String fontFilename, int fontSize, int r, int g, int b)
	{
		this(fontFilename, fontSize, 3, 3, r, g, b, 1.0f);
	}

	private TextStyle(String fontFilename, int fontSize, int padX, int padY)
	{
		this(fontFilename, fontSize, padX, padY, 255, 255, 255, 1.0f);
	}
	
	/*public TextStyle(String fontFilename, int fontSize)
	{
		this(fontFilename, fontSize, 3, 3, 255, 255, 255, 1.0f);
	}*/

	public static TextStyle headingStyle()
	{
		final TextStyle style = new TextStyle("Tommaso.otf", 83, 87, 233, 255);
		style.setSpacing(2.5f);
		return style;
	}

	public static TextStyle bodyStyleCyan(int fontSize)
	{
		final TextStyle style = new TextStyle("BLANCH_CAPS.otf", fontSize, 87, 233, 255);
		style.setSpacing(2.5f);
		return style;
	}

	public static TextStyle bodyStyleCyan()
	{
		return bodyStyleCyan(57);
		//final TextStyle style = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255);
		//style.setSpacing(2.5f);
		//return style;
	}

	public static TextStyle bodyStyleWhite(int fontSize)
	{
		final TextStyle style = new TextStyle("BLANCH_CAPS.otf", fontSize, 255, 255, 255);
		style.setSpacing(2.5f);
		return style;
	}

	public static TextStyle bodyStyleWhite()
	{
		return bodyStyleWhite(57);
		//final TextStyle style = new TextStyle("BLANCH_CAPS.otf", 57, 255, 255, 255);
		//style.setSpacing(2.5f);
		//return style;
	}

	public void setSpacing(float spaceX)
	{
		this.spaceX = spaceX;
		TextManager.setStyleSpacing(listNum, type, spaceX);
	}

	/*public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
		type = TextManager.newTextStyle(fontFilename, fontSize, padX, padY, 0);
	}*/

	public void setColor(int r, int g, int b, float a)
	{
		setColor(r / 255f, g / 255f, b / 255f, a);
	}

	public void setColor(int r, int g, int b)
	{
		setColor(r / 255f, g / 255f, b / 255f, color[3]);
	}

	private void setColor(float r, float g, float b, float a)
	{
		color[0] = r;
		color[1] = g;
		color[2] = b;
		color[3] = a;
	}

	public void setAlpha(float alpha)
	{
		setColor(color[0], color[1], color[2], alpha);
	}

	public float measureText(String text)
	{
		return TextManager.measureText(listNum, type, text);
	}

	public float[] getColor()
	{
		return color;
	}

	public int getType()
	{
		return type;
	}

	public int getFontSize()
	{
		return fontSize;
	}
}