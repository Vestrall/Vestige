package com.lescomber.vestige.graphics;

import java.util.ArrayList;

public class TextArea {
	private final float midX;
	private final float topY;
	private final int charsPerLine;
	private final float lineSpacing;
	private final TextStyle style;

	private final ArrayList<Text> lines;

	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style, String text, boolean isVisible) {
		lines = new ArrayList<>();

		this.midX = midX;
		this.topY = topY;
		this.charsPerLine = charsPerLine;
		this.lineSpacing = lineSpacing;
		this.style = style;

		if (text.length() > 0)
			setText(text, isVisible);
	}

	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style, String text) {
		this(midX, topY, charsPerLine, lineSpacing, style, text, true);
	}

	public TextArea(float midX, float topY, int charsPerLine, float lineSpacing, TextStyle style) {
		this(midX, topY, charsPerLine, lineSpacing, style, "", false);
	}

	public void setVisible(boolean isVisible) {
		for (Text t : lines)
			t.setVisible(isVisible);
	}

	public void setText(String text, boolean isVisible) {
		final ArrayList<String> lineStrings = new ArrayList<>();
		while (text.length() > charsPerLine) {
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
	}
}
