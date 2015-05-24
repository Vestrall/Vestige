package com.lescomber.vestige.crossover;

import com.lescomber.vestige.cgl.CGLColorRect;

import java.util.ArrayList;
import java.util.LinkedList;

public class ColorRectManager {
	private static int buildList;
	private static int drawList;

	private static ArrayList<CGLColorRect>[] colorRects;
	private static LinkedList<Integer>[] nulls;

	private static boolean isInitialized = false;

	/**
	 * This is our static "constructor"
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void init() {
		if (!isInitialized) {
			buildList = 0;
			drawList = 0;

			colorRects = new ArrayList[2];
			colorRects[0] = new ArrayList<CGLColorRect>();
			colorRects[1] = new ArrayList<CGLColorRect>();

			nulls = new LinkedList[2];
			nulls[0] = new LinkedList<Integer>();
			nulls[1] = new LinkedList<Integer>();

			isInitialized = true;
		}
	}

	public static void switchBuild() {
		buildList = (buildList + 1) % 2;
	}

	public static void switchDraw() {
		drawList = (drawList + 1) % 2;
	}

	public synchronized static void clearBuild() {
		if (colorRects != null)
			colorRects[buildList] = new ArrayList<CGLColorRect>();
		if (nulls != null)
			nulls[buildList].clear();
	}

	public synchronized static void clearOtherBuild() {
		final int otherList = (buildList + 1) % 2;
		if (colorRects != null)
			colorRects[otherList] = new ArrayList<CGLColorRect>();
		if (nulls != null)
			nulls[otherList].clear();
	}

	public synchronized static int newColorRect(float x, float y, float width, float height, float r, float g, float b, float a) {
		final CGLColorRect newRect = new CGLColorRect(x, y, width, height, r, g, b, a);

		if (!nulls[buildList].isEmpty()) {
			final int index = nulls[buildList].removeFirst();
			colorRects[buildList].set(index, newRect);
			return index;
		} else {
			colorRects[buildList].add(newRect);
			return colorRects[buildList].size() - 1;
		}
	}

	public synchronized static void replaceColorRect(int list, int index, float x, float y, float width, float height, float r, float g, float b,
													 float a) {
		colorRects[list].set(index, new CGLColorRect(x, y, width, height, r, g, b, a));
	}

	public synchronized static void removeColorRect(int list, int index) {
		colorRects[list].set(index, null);
		nulls[list].add(index);
	}

	public synchronized static void setColorRectColor(int list, int index, float r, float g, float b, float a) {
		colorRects[list].get(index).setColor(r, g, b, a);
	}

	public synchronized static void setColorRectAlpha(int list, int index, float a) {
		colorRects[list].get(index).setAlpha(a);
	}

	public synchronized static void offsetColorRect(int list, int index, float dx, float dy) {
		colorRects[list].get(index).offset(dx, dy);
	}

	public synchronized static void rotateColorRect(int list, int index, float degrees) {
		colorRects[list].get(index).rotate(degrees);
	}

	public synchronized static void scaleColorRect(int list, int index, float widthRatio, float heightRatio) {
		colorRects[list].get(index).scale(widthRatio, heightRatio);
	}

	public synchronized static void draw() {
		for (final CGLColorRect cr : colorRects[drawList])
			if (cr != null)
				cr.draw();
	}

	public static int getBuildListNum() {
		return buildList;
	}
}