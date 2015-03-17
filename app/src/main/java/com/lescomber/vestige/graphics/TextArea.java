package com.lescomber.vestige.graphics;

import java.util.ArrayList;

public class TextArea
{
	private float midX;
	private float topY;
	private int charsPerLine;
	private float lineSpacing;
	private TextStyle style;

	private ArrayList<Text> lines;
	
	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style, String text, boolean isVisible)
	{
		lines = new ArrayList<Text>();

		this.midX = midX;
		this.topY = topY;
		this.charsPerLine = charsPerLine;
		this.lineSpacing = lineSpacing;
		this.style = style;

		if (text.length() > 0)
			setText(text, isVisible);
	}

	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style, String text)
	{
		this(midX, topY, charsPerLine, lineSpacing, style, text, true);
	}

	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style)
	{
		this(midX, topY, charsPerLine, lineSpacing, style, "", false);
	}

	public void setVisible(boolean isVisible)
	{
		for (Text t : lines)
			t.setVisible(isVisible);
	}

	public void setText(String text, boolean isVisible)
	{
		ArrayList<String> lineStrings = new ArrayList<String>();
		while (text.length() > charsPerLine)
		{
			// Find the last space before charsPerLine (i.e. locate where the line break should happen)
			int spaceIndex = charsPerLine + 1;
			for (int i = charsPerLine; text.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lineStrings.add(text.substring(0, spaceIndex));
			text = text.substring(spaceIndex + 1);
		}
		lineStrings.add(text);    // Add the last line

		for (Text t : lines)
			t.close();
		lines.clear();
		for (int i = 0; i < lineStrings.size(); i++)
			lines.add(new Text(style, lineStrings.get(i), midX, topY + (i * lineSpacing), isVisible));
	}/*

	public String setText(String text, boolean isVisible, int maxLines)
	{
		int lineNum = 0;
		ArrayList<String> lineStrings = new ArrayList<String>();
		while (text.length() > charsPerLine && lineNum != maxLines)
		{
			// Find the last space before charsPerLine (i.e. locate where the line break should happen)
			int spaceIndex = charsPerLine + 1;
			for (int i = charsPerLine; text.charAt(i) != ' '; i--)
				spaceIndex = i;
			spaceIndex--;

			lineStrings.add(text.substring(0, spaceIndex));
			text = text.substring(spaceIndex + 1);
			lineNum++;
		}

		if (lineNum != maxLines)
			lineStrings.add(text);    // Add the last line if we haven't reached maxLines

		for (Text t : lines)
			t.close();
		lines.clear();
		for (int i = 0; i < lineStrings.size(); i++)
			lines.add(new Text(style, lineStrings.get(i), midX, topY + (i * lineSpacing), isVisible));

		if (lineNum == maxLines)
			return text;
		else
			return "";
	}*/
}
