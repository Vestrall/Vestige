package com.lescomber.vestige.screens;

import java.util.List;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

public class CreditsScreen extends Screen implements WidgetListener
{
	private final TextStyle titleStyle;
	private final TextStyle nameStyle;
	
	private final Button backButton;
	
	public CreditsScreen(AndroidGame game)
	{
		super(game);
		
		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());
		
		final Resources res = AndroidGame.res;
		titleStyle = new TextStyle("BLANCH_CAPS.otf", 57, 87, 233, 255);
		titleStyle.setSpacing(2.5f);
		nameStyle = new TextStyle("BLANCH_CAPS.otf", 43, 255, 255, 255);
		nameStyle.setSpacing(2.5f);
		
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
		
		/*new Text(titleStyle, "ART", Screen.MIDX, 75);
		new Text(nameStyle, "GRACE ZHANG", Screen.MIDX, 110);
		
		new Text(titleStyle, "SOFTWARE DEVELOPMENT", Screen.MIDX, 172);
		new Text(nameStyle, "LES COMBER", Screen.MIDX, 207);
		
		new Text(titleStyle, "AUDIO", Screen.MIDX, 270);
		new Text(nameStyle, "MUSIC BY AUDIONAUTIX.COM", Screen.MIDX, 305);
		new Text(nameStyle, "SOUND EFFECTS", 278, 332, Text.Alignment.RIGHT);
		new Text(nameStyle, "http://www.freesfx.co.uk", 298, 332, Text.Alignment.LEFT);
		new Text(nameStyle, "SOUND EFFECTS", 278, 359, Text.Alignment.RIGHT);
		new Text(nameStyle, "http://www.audiomicro.com", 298, 359, Text.Alignment.LEFT);*/
		
		// Template for lining up sound effects section:
		//new Text(nameStyle, "SOUND EFFECTS  http://www.audiomicro.com/", Screen.MIDX, 385);
		
		/*new Text(titleStyle, "MUSIC", Screen.MIDX, 270);
		new Text(nameStyle, "AUDIONAUTIX.COM", Screen.MIDX, 305);
		new Text(nameStyle, "MAIN MENU", 336, 346, Text.Alignment.RIGHT);
		new Text(nameStyle, "TEMPTATION MARCH", 352, 346, Text.Alignment.LEFT);
		new Text(nameStyle, "IN GAME", 336, 373, Text.Alignment.RIGHT);
		new Text(nameStyle, "SNEAKY SNOOPER", 352, 373, Text.Alignment.LEFT);
		new Text(nameStyle, "BOSS FIGHT", 336, 398, Text.Alignment.RIGHT);
		new Text(nameStyle, "PENDULUM WALTZ", 352, 398, Text.Alignment.LEFT);*/
		
		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.scaleRect(1.25, 1.25);	// Enlarge "back" button hitbox slightly
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.addWidgetListener(this);
		backButton.setVisible(true);
	}
	
	@Override
	public void update(int deltaTime)
	{
		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		
		backButton.update(deltaTime);
		
		final int len = touchEvents.size();
		for (int i=0; i<len; i++)
		{
			final TouchEvent event = touchEvents.get(i);
			
			backButton.handleEvent(event);
		}
	}
	
	@Override
	public void widgetEvent(WidgetEvent we)
	{
		// We only have a back button so no need to check we.getSource()
		if (we.getCommand().equals(Button.ANIMATION_FINISHED))
		{
			if (!Screen.isScreenChanging())
			{
				prepScreenChange();
				game.setScreen(new MainMenuScreen(game, false));
			}
		}
	}
	
	@Override public void pause() { }
	@Override public void resume() { }
	@Override public void dispose() { }
	
	@Override
	public void backButton()
	{
		if (!Screen.isScreenChanging())
			backButton.click();
	}
}