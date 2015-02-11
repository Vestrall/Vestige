package com.lescomber.vestige.screens;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Preferences;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Levels;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.WidgetEvent;
import com.lescomber.vestige.widgets.WidgetListener;

public class StageSelectionScreen extends Screen implements WidgetListener
{
	private static final int STAGE_GAP = 300;
	private static final float SELECTION_WINDOW_LEFT = (Screen.MIDX) - (STAGE_GAP / 2) + 15;
	private static final float SELECTION_WINDOW_RIGHT = SELECTION_WINDOW_LEFT + STAGE_GAP - 15;
	private static final int DRAG_TOLERANCE = 5;  // How far we can drag during a tap but still be allowed to select a stage
	private static final float GLIDE_SPEED = 1.5f;	// In pixels per ms
	private static final float GLIDE_SHORT_RANGE = 20;
	
	private static final float STAGE_NAME_Y = 340;
	private static final float STAGE_PROGRESS_Y = 393;
	private final TextStyle stageStyle;
	
	private int levelProgress;
	
	private final int stageProgress;
	private final ArrayList<Stage> stages;
	
	private float cameraX;
	private float cameraDestX;
	private boolean dragging;
	private final Point downPoint;
	private final Point lastDragPoint;
	
	private final Button backButton;
	private boolean backing;
	
	private int stageClickCountdown;
	private static final int STAGE_CLICK_COUNTDOWN_MAX = 300;	// In ms
	
	public StageSelectionScreen(AndroidGame game)
	{
		super(game);
		
		SpriteManager.getInstance().setBackground(Assets.genericBackground);
		SpriteManager.getInstance().setUITextureHandle(Assets.menuUITexture.getTextureHandle());
		
		stageStyle = new TextStyle("BLANCH_CAPS.otf", 90, 87, 233, 255, 1);
		stageStyle.setSpacing(2.5f);
		
		switch (OptionsScreen.difficulty)
		{
		case OptionsScreen.EASY:
			stageProgress = Preferences.getInt("easyStageProgress", 0);
			levelProgress = Preferences.getInt("easyLevelProgress", 0);
			break;
		case OptionsScreen.MEDIUM:
			stageProgress = Preferences.getInt("mediumStageProgress", 0);
			levelProgress = Preferences.getInt("mediumLevelProgress", 0);
			break;
		default:
			stageProgress = Preferences.getInt("hardStageProgress", 0);
			levelProgress = Preferences.getInt("hardLevelProgress", 0);
			break;
		}
		
		final Resources res = AndroidGame.res;
		final int lastStage = Preferences.getInt("lastStage", 1);
		stages = new ArrayList<Stage>(Levels.STAGE_COUNT);
		stages.add(new Stage(1, res.getString(R.string.darkWoods), SpriteManager.darkWoodsSelected, SpriteManager.darkWoods));
		stages.add(new Stage(2, res.getString(R.string.unknown), SpriteManager.stageLockedSelected, SpriteManager.stageLocked));
		
		cameraX = 0;
		cameraDestX = (lastStage - 1) * STAGE_GAP;
		dragging = false;
		downPoint = new Point();
		lastDragPoint = new Point();
		
		offsetStages((lastStage - 1) * -STAGE_GAP);
		
		backButton = new Button(760, 440, null, null, SpriteManager.backButton);
		backButton.addWidgetListener(this);
		backButton.setClickAnimation(SpriteManager.backButtonClick);
		backButton.setVisible(true);
		backing = false;
		
		stageClickCountdown = 0;
		
		for (final Stage s : stages)
			s.initVisible();
	}
	
	@Override
	public void update(int deltaTime)
	{
		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		
		// Perform camera "glide" towards nearest stage
		if (!dragging && !Util.equals(cameraX, cameraDestX))
		{
			float maxGlideDistance;
			if (Math.abs(cameraDestX - cameraX) < GLIDE_SHORT_RANGE)
				maxGlideDistance = (GLIDE_SPEED / 2) * deltaTime;
			else
				maxGlideDistance = GLIDE_SPEED * deltaTime;
			
			float dx;
			if (cameraDestX - cameraX < 0)
				dx = Math.max(cameraDestX - cameraX, -maxGlideDistance);
			else
				dx = Math.min(cameraDestX - cameraX, maxGlideDistance);
			
			offsetStages(-dx);
		}
		
		// Case: stage has been selected and we are just displaying the bigger stage briefly before loading it
		if (stageClickCountdown > 0)
		{
			stageClickCountdown -= deltaTime;
			
			if (stageClickCountdown <= 0)
			{
				for (int j=0; j<stages.size(); j++)
				{
					if (stages.get(j).centered)
					{
						Preferences.setInt("lastStage", j + 1);		// Remember last stage
						prepScreenChange();
						game.setScreen(new LevelSelectionScreen(game, j + 1));
						return;
					}
				}
			}
			
			return;
		}
		
		backButton.update(deltaTime);
		
		final int len = touchEvents.size();
		for (int i=0; i<len; i++)
		{
			final TouchEvent event = touchEvents.get(i);
			
			backButton.handleEvent(event);
			if (backing)
				return;
			
			if (event.type == TouchEvent.TOUCH_DOWN)
			{
				downPoint.set(event.x, event.y);
				lastDragPoint.set(event.x, event.y);
			}
			else if (event.type == TouchEvent.TOUCH_DRAGGED)
			{
				if (Math.abs(event.x - downPoint.x) > DRAG_TOLERANCE)
					dragging = true;
				
				offsetStages(event.x - lastDragPoint.x);
				lastDragPoint.set(event.x, event.y);
			}
			else if (event.type == TouchEvent.TOUCH_UP)
			{
				if (dragging)
				{
					// Find nearest stage from camera center
					final int index = Math.round(cameraX / STAGE_GAP);
					cameraDestX = index * STAGE_GAP;
				}
				
				// Case: User selected centered stage
				else if (event.x > SELECTION_WINDOW_LEFT && event.x < SELECTION_WINDOW_RIGHT)
				{
					// Enter currently selected stage if it is more or less centered on screen
					if (Math.abs(cameraDestX - cameraX) < 50)
					{
						for (int j=0; j<stages.size(); j++)
						{
							if (stages.get(j).centered && stageProgress > j)
							{
								stageClickCountdown = STAGE_CLICK_COUNTDOWN_MAX;
								stages.get(j).select();
							}
						}
					}
				}
				
				else if (event.x > SELECTION_WINDOW_RIGHT)
				{
					for (int j=0; j<stages.size(); j++)
					{
						if (stages.get(j).centered)
						{
							if (stages.size() > (j + 1))
								cameraDestX = (j + 1) * STAGE_GAP;
						}
					}
				}
				else if (event.x < SELECTION_WINDOW_LEFT)
				{
					for (int j=0; j<stages.size(); j++)
					{
						if (stages.get(j).centered)
						{
							if (j > 0)
								cameraDestX = (j - 1) * STAGE_GAP;
						}
					}
				}
				
				dragging = false;
			}
		}
	}
	
	private void offsetStages(float dx)
	{
		// Limit the camera's movement
		if (cameraX - dx < 0)
			dx = cameraX;
		else if (cameraX - dx > (Levels.STAGE_COUNT - 1) * STAGE_GAP)
			dx = -(((Levels.STAGE_COUNT - 1) * STAGE_GAP) - cameraX);
		
		// Update camera position
		cameraX -= dx;
		
		// Offset stages and change selection if necessary
		for (final Stage s : stages)
		{
			s.offsetX(dx);
			s.setCentered(s.getX() > SELECTION_WINDOW_LEFT && s.getX() < SELECTION_WINDOW_RIGHT);
		}
	}
	
	@Override
	public void widgetEvent(WidgetEvent we)
	{
		final Object source = we.getSource();
		
		if (source == backButton)
		{
			backing = true;
			
			if (we.getCommand().equals(Button.ANIMATION_FINISHED))
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
	
	private class Stage
	{
		private Text nameText;
		private Text progressText;
		private boolean centered;
		private Sprite selectedSprite;
		private Sprite unselectedSprite;
		
		private Stage(int stageNum, String name, SpriteTemplate selectedTemplate, SpriteTemplate unselectedTemplate)
		{
			centered = false;
			final float x = (Screen.MIDX) + (stageNum - 1) * STAGE_GAP;
			
			if (stageProgress >= stageNum)
			{
				selectedSprite = new Sprite(selectedTemplate, x, Screen.MIDY);
				unselectedSprite = new Sprite(unselectedTemplate, x, Screen.MIDY);
				nameText = new Text(stageStyle, name, x, STAGE_NAME_Y, false);
				String progress;
				if (stageProgress == stageNum)
					progress = "" + (levelProgress - 1) + "/" + Levels.LEVEL_COUNT[stageNum - 1];
				else
					progress = "" + Levels.LEVEL_COUNT[stageNum - 1] + "/" + Levels.LEVEL_COUNT[stageNum - 1];
				
				progressText = new Text(stageStyle, progress, x, STAGE_PROGRESS_Y, false);
			}
			else
			{
				selectedSprite = new Sprite(SpriteManager.stageLockedSelected, x, Screen.MIDY);
				unselectedSprite = new Sprite(SpriteManager.stageLocked, x, Screen.MIDY/* + 70*/);
				nameText = new Text(stageStyle, "???", x, STAGE_NAME_Y, false);
				progressText = new Text(stageStyle, "", x, STAGE_PROGRESS_Y, false);
			}
		}
		
		private void offsetX(float dx)
		{
			selectedSprite.offset(dx, 0);
			unselectedSprite.offset(dx, 0);
			nameText.offset(dx, 0);
			progressText.offset(dx, 0);
		}
		
		private void setCentered(boolean centered)
		{
			this.centered = centered;
			
			nameText.setVisible(centered);
			progressText.setVisible(centered);
		}
		
		private void select()
		{
			if (!selectedSprite.isVisible())
			{
				Swapper.swapImages(unselectedSprite, selectedSprite);
			}
		}
		
		private float getX() { return selectedSprite.getX(); }
		
		private void initVisible()
		{
			unselectedSprite.setVisible(true);
		}
	}
}