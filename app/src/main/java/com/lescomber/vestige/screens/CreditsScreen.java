package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

import java.util.List;

public class CreditsScreen extends Screen implements WidgetListener {
	private final Button backButton;

	public CreditsScreen(AndroidGame game) {
		super(game);

		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());

		final Resources res = AndroidGame.res;
		final TextStyle titleStyle = TextStyle.bodyStyleCyan();
		final TextStyle nameStyle = TextStyle.bodyStyleWhite(43);

		new Text(titleStyle, res.getString(R.string.art), Screen.MIDX, 75);
		new Text(nameStyle, res.getString(R.string.graceZhang), Screen.MIDX, 110);

		new Text(titleStyle, res.getString(R.string.softwareDevelopment), Screen.MIDX, 172);
		new Text(nameStyle, res.getString(R.string.lesComber), Screen.MIDX, 207);

		new Text(titleStyle, res.getString(R.string.audio), Screen.MIDX, 270);
		new Text(nameStyle, res.getString(R.string.musicBy), Screen.MIDX, 305);
		new Text(nameStyle, res.getString(R.string.soundEffects), 278, 332, Text.Alignment.RIGHT);
		new Text(nameStyle, res.getString(R.string.freeSFX), 298, 332, Text.Alignment.LEFT);
		new Text(nameStyle, res.getString(R.string.soundEffects), 278, 359, Text.Alignment.RIGHT);
		new Text(nameStyle, res.getString(R.string.audioMicro), 298, 359, Text.Alignment.LEFT);

		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.scaleRect(1.25, 1.25);    // Enlarge "back" button hitbox slightly
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.addWidgetListener(this);
		backButton.setVisible(true);
	}

	@Override
	public void update(int deltaTime) {
		final List<TouchEvent> touchEvents = game.getTouchEvents();

		backButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			final TouchEvent event = touchEvents.get(i);

			backButton.handleEvent(event);
		}
	}

	@Override
	public void widgetEvent(WidgetEvent we) {
		// We only have a back button so no need to check we.getSource()
		if (we.getCommand().equals(Button.ANIMATION_FINISHED)) {
			if (!Screen.isScreenChanging()) {
				prepScreenChange();
				game.setScreen(new MainMenuScreen(game, false));
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
		if (!Screen.isScreenChanging())
			backButton.click();
	}
}