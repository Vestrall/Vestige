package com.lescomber.vestige.screens;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Preferences;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class LevelSelectionScreen extends Screen implements WidgetListener {
	// Button size/spacing constants
	private static final int SPACING = 25;
	private static final int MIN_SIDE_MARGIN = 95;
	private static final int TOP_MARGIN = 157;
	private static final int BUTTON_WIDTH = SpriteManager.levelSelectButtonLocked.getWidth();
	private static final int BUTTON_HEIGHT = SpriteManager.levelSelectButtonLocked.getHeight();

	private final int stageNum;
	private final int stageProgress;
	private final int levelProgress;
	private final int levelCount;

	private final Button levelButtons[];

	private final TextStyle levelNumStyle;

	private final Button backButton;

	public LevelSelectionScreen(AndroidGame game, int stageNum) {
		super(game);

		this.stageNum = stageNum;

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		// Level number TextStyle
		final int fontSize = (int) Math.round(0.7 * Math.min(BUTTON_WIDTH, BUTTON_HEIGHT));
		levelNumStyle = TextStyle.bodyStyleCyan(fontSize);

		levelCount = Levels.LEVEL_COUNT[stageNum - 1];

		// Create heading
		final TextStyle headingStyle = TextStyle.headingStyle();
		new Text(headingStyle, AndroidGame.res.getString(R.string.levelSelect), 400, 75);

		// Calculate number of rows and columns we will need to fit all the levels in
		int cols = 0;
		int rows = 0;

		int pos = 0;
		while (pos < Screen.WIDTH - (2 * MIN_SIDE_MARGIN)) {
			pos += BUTTON_WIDTH + SPACING;
			cols++;
		}
		cols--;        // Remove last entry (which extended off the screen)

		rows = (int) Math.ceil((float) levelCount / cols);

		// Calculate side margins based on leftover space
		final int leftMargin = (Screen.WIDTH - (cols * BUTTON_WIDTH) - ((cols - 1) * SPACING)) / 2;

		// Retrieve user's level progress
		stageProgress = Preferences.getStageProgress(OptionsScreen.difficulty);
		levelProgress = Preferences.getLevelProgress(OptionsScreen.difficulty);

		// Finally, create the Rectangles and their Sprites
		levelButtons = new Button[levelCount];
		for (int i = 0; i < rows; i++) {
			// Calculate the number of columns in this particular row (should only not be equal to cols for final row)
			final int thisColCount = Math.min(cols, levelCount - (i * cols));

			final int y = TOP_MARGIN + (BUTTON_HEIGHT / 2) + (i * (BUTTON_HEIGHT + SPACING));

			for (int j = 0; j < thisColCount; j++) {
				final int x = leftMargin + (j * (BUTTON_WIDTH + SPACING)) + (BUTTON_WIDTH / 2);
				final int index = i * cols + j;

				// Level select button
				final SpriteTemplate levelTemplate;
				if (stageProgress > stageNum || (stageProgress == stageNum && levelProgress > index))
					levelTemplate = SpriteManager.levelSelectButtonUnlocked;
				else
					levelTemplate = SpriteManager.levelSelectButtonLocked;

				levelButtons[index] = new Button(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, levelNumStyle, "" + (index + 1), levelTemplate);
				levelButtons[index].setClickSound(null);
				levelButtons[index].addWidgetListener(this);
				levelButtons[index].setVisible(true);

				// Score icon. TODO: Track and display perfect score icon
				final SpriteTemplate scoreTemplate;
				if (stageProgress > stageNum || (stageProgress == stageNum && levelProgress > index + 1))
					scoreTemplate = SpriteManager.scoreHalf;
				else
					scoreTemplate = SpriteManager.scoreEmpty;

				new Sprite(scoreTemplate, x + 0.45f * BUTTON_WIDTH, y + 0.34f * BUTTON_HEIGHT, true);
			}
		}

		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.addWidgetListener(this);
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.setVisible(true);
	}

	@Override
	public void update(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();

		backButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			for (final Button b : levelButtons)
				b.handleEvent(event);

			backButton.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		final Object source = we.getSource();

		// Load level (if it is unlocked)
		for (int i = 0; i < levelCount; i++) {
			if (source == levelButtons[i]) {
				if (stageProgress > stageNum || (stageProgress == stageNum) && levelProgress >= (i + 1)) {
					prepScreenChange();
					game.setScreen(new MapLoadingScreen(game, stageNum, i + 1));
				}

				break;
			}
		}

		if (source == backButton) {
			if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
				prepScreenChange();
				game.setScreen(new StageSelectionScreen(game));
			}
		}
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void backButton() {
		if (!Screen.isScreenChanging()) {
			backButton.click();
		}
	}
}