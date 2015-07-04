package com.lescomber.vestige.crossover;

import com.lescomber.vestige.cgl.CGLText;

import java.util.ArrayList;
import java.util.LinkedList;

public class TextManager {
	private static int buildList;
	private static int drawList;

	private static ArrayList<TMStyle> styles[];
	private static LinkedList<Integer> initTexts[];

	private static boolean isInitialized = false;

	/**
	 * This is our static "constructor"
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void init() {
		// This method will be called when the game starts but also if the GL context is recreated (or resumed?), e.g.
		//from the screen turning off and back on. We don't want to lose our existing text info in the latter case
		if (!isInitialized) {
			styles = new ArrayList[2];
			styles[0] = new ArrayList<>();
			styles[1] = new ArrayList<>();

			initTexts = new LinkedList[2];
			initTexts[0] = new LinkedList<>();
			initTexts[1] = new LinkedList<>();

			buildList = 0;
			drawList = 0;

			isInitialized = true;
		}
	}

	public synchronized static void recreateCGLTexts() {
		initTexts[0].clear();
		initTexts[1].clear();

		for (int i = 0; i < 2; i++) {
			final int len = styles[i].size();
			for (int j = 0; j < len; j++) {
				if (styles[i].get(j) != null)
					initTexts[i].add(j);
			}
		}
	}

	/**
	 * CGLTexts need to be created and load()'ed by the Renderer thread. However, we would like our game logic thread to be deciding which CGLTexts
	 * to create. Therefore, when the game logic thread would like a new TextStyle created, it must queue up the corresponding info for the creation
	 * of its CGLText, which, in this method, will be initialized by the Renderer thread before any drawing takes place
	 */
	private synchronized static void initCGLTexts() {
		if (!initTexts[0].isEmpty() || !initTexts[1].isEmpty()) {
			for (int i = 0; i < 2; i++) {
				while (!initTexts[i].isEmpty()) {
					final int index = initTexts[i].removeFirst();
					styles[i].get(index).initCGLText();
				}
			}
		}
	}

	public static void switchBuild() {
		buildList = (buildList + 1) % 2;
	}

	public static void switchDraw() {
		drawList = (drawList + 1) % 2;
	}

	public synchronized static void clearBuild() {
		// Reset our first dimension to max 5 spots in order to avoid hanging on to more space than we're likely to
		//use again
		if (styles != null)
			styles[buildList] = new ArrayList<>(5);

		if (initTexts != null)
			initTexts[buildList].clear();
	}

	public synchronized static void clearOtherBuild() {
		final int otherList = (buildList + 1) % 2;

		// Reset our first dimension to max 5 spots in order to avoid hanging on to more space than we're likely to
		//use again
		if (styles != null)
			styles[otherList] = new ArrayList<>(5);

		if (initTexts != null)
			initTexts[otherList].clear();
	}

	public synchronized static int newTextStyle(String filename, int fontSize, int padX, int padY, float spaceX) {
		styles[buildList].add(new TMStyle(filename, fontSize, padX, padY, spaceX));
		final int index = styles[buildList].size() - 1;
		initTexts[buildList].add(index);
		return index;
	}

	public synchronized static void setStyleSpacing(int list, int type, float spaceX) {
		styles[list].get(type).setSpacing(spaceX);
	}

	public synchronized static int newString(int type, String text, float x, float y, float direction, float r, float g, float b, float a) {
		return styles[buildList].get(type).newString(text, x, y, direction, r, g, b, a);
	}

	public synchronized static void setString(int list, int type, int index, String newText) {
		styles[list].get(type).instances.get(index).text = newText;
	}

	public synchronized static int setStringType(int list, int currentType, int index, int newType) {
		final TextInstance mover = styles[list].get(currentType).instances.get(index);
		removeString(list, currentType, index);
		return styles[list].get(newType).addInstance(mover);
	}

	public synchronized static void removeString(int list, int type, int index) {
		styles[list].get(type).removeString(index);
	}

	public synchronized static void setStringColor(int list, int type, int index, float r, float g, float b, float a) {
		styles[list].get(type).instances.get(index).setColor(r, g, b, a);
	}

	public synchronized static void offsetString(int list, int type, int index, float dx, float dy) {
		styles[list].get(type).offsetString(index, dx, dy);
	}

	public synchronized static void rotateString(int list, int type, int index, float radians) {
		styles[list].get(type).rotateString(index, radians);
	}

	public synchronized static float measureText(int list, int type, String text) {
		while (initTexts[list].contains(type)) {
			try {
				TextManager.class.wait();
			} catch (final InterruptedException ie) {
			}
		}

		return styles[list].get(type).measureText(text);
	}

	public synchronized static void draw() {
		// Initialize any CGLTexts that have been queue'd up by the game logic thread
		initCGLTexts();

		// Draw all strings
		for (final TMStyle ts : styles[drawList])
			ts.draw();

		// Inform gameLoop thread that a draw has been completed (if it was waiting for one)
		TextManager.class.notifyAll();
	}

	public static int getBuildListNum() {
		return buildList;
	}

	/**
	 * First dimension of our TextManager array
	 */
	private static class TMStyle {
		private CGLText cglText;
		private final String filename;
		private final int fontSize;
		private final int padX;
		private final int padY;
		private float spaceX;

		private final ArrayList<TextInstance> instances;
		private final LinkedList<Integer> nulls;

		private TMStyle(String filename, int fontSize, int padX, int padY, float spaceX) {
			this.filename = filename;
			this.fontSize = fontSize;
			this.padX = padX;
			this.padY = padY;
			this.spaceX = spaceX;
			instances = new ArrayList<>();
			nulls = new LinkedList<>();
		}

		private void initCGLText() {
			cglText = new CGLText();
			cglText.load(filename, fontSize, padX, padY);
			cglText.setSpace(spaceX);
		}

		private int newString(String text, float x, float y, float direction, float r, float g, float b, float a) {
			final TextInstance ti = new TextInstance(text, x, y, direction, r, g, b, a);

			if (!nulls.isEmpty()) {
				final int newIndex = nulls.removeFirst();
				instances.set(newIndex, ti);
				return newIndex;
			} else {
				instances.add(ti);
				return instances.size() - 1;
			}
		}

		private int addInstance(TextInstance newInstance) {
			if (!nulls.isEmpty()) {
				final int newIndex = nulls.removeFirst();
				instances.set(newIndex, newInstance);
				return newIndex;
			} else {
				instances.add(newInstance);
				return instances.size() - 1;
			}
		}

		private void removeString(int index) {
			instances.set(index, null);
			nulls.add(index);
		}

		private void offsetString(int index, float dx, float dy) {
			instances.get(index).x += dx;
			instances.get(index).y += dy;
		}

		private void rotateString(int index, float radians) {
			instances.get(index).direction += radians;
		}

		private float measureText(String text) {
			return cglText.getLength(text);
		}

		private void setSpacing(float spaceX) {
			this.spaceX = spaceX;
			if (cglText != null)
				cglText.setSpace(spaceX);
		}

		private void draw() {
			if (!instances.isEmpty()) {
				for (final TextInstance ti : instances) {
					if (ti != null) {
						cglText.prepareToDraw(ti.color[0], ti.color[1], ti.color[2], ti.color[3]);
						cglText.drawC(ti.text, ti.x, ti.y, ti.direction);
						cglText.finishedDrawing();
					}
				}
			}
		}
	}

	/**
	 * Second dimension of our TextManager array
	 */
	private static class TextInstance {
		private String text;
		private float x;
		private float y;
		private float direction;
		private final float[] color;

		private TextInstance(String text, float x, float y, float direction, float r, float g, float b, float a) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.direction = direction;
			color = new float[4];
			setColor(r, g, b, a);
		}

		private void setColor(float r, float g, float b, float a) {
			color[0] = r;
			color[1] = g;
			color[2] = b;
			color[3] = a;
		}
	}
}