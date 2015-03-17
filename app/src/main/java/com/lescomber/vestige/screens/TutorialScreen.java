package com.lescomber.vestige.screens;

import android.content.res.Resources;

import com.lescomber.vestige.R;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AndroidGame;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Preferences;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.gestures.GestureHandlerListener;
import com.lescomber.vestige.graphics.ColorRect;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextArea;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.map.Map;
import com.lescomber.vestige.map.TutorialMap;
import com.lescomber.vestige.units.TutorialPlayer;
import com.lescomber.vestige.widgets.Button;
import com.lescomber.vestige.widgets.WidgetEvent;

import java.util.List;

public class TutorialScreen extends GameScreen implements GestureHandlerListener
{
	/*====================================================================================================================
	 * 
	 * Lesson #1: Welcome / Tap to move
	 * Lesson #2: Move to portal
	 * Lesson #3: Move around wall to portal
	 * Lesson #4: Swipe
	 * Lesson #5: Charge Swipe
	 * Lesson #6: Double Tap
	 * Lesson #7: Multi-Tap
	 * Lesson #8: Fight a Turret Caster
	 * Lesson #9: Congrats and Finished
	 * 
	  ====================================================================================================================*/

	private static final int TAP_TO_MOVE = 1;
	private static final int MOVE_TO_PORTAL = 2;
	private static final int PATH_AROUND_WALLS = 3;
	private static final int SWIPE = 4;
	private static final int CHARGE_SWIPE = 5;
	private static final int DOUBLE_TAP = 6;
	private static final int MULTI_TAP = 7;
	private static final int COMBAT = 8;
	private static final int EXIT_TUTORIAL = 9;

	private int lessonNum;            // Current lesson being learned
	private int lessonDelay;        // Remaining delay (in ms) before next lesson begins

	private int messagingNum;        // Current "page" of the current lesson message. 0 = Not currently messaging

	private ColorRect messageBackground;
	private ColorRect messageReminderBackground;

	private static final int CHAR_LIMIT_PER_LINE = 28;
	private static final int LINE_SPACING = 43;
	private static final int LINE_LIMIT = 6;
	//private Text messages[];
	private TextArea messageArea;
	private Text messageReminder;

	private static final double NEXT_BUTTON_CLICK_TIME = 0.2;
	private Button nextButton;

	public TutorialScreen(AndroidGame game)
	{
		super(game);

		gestureHandler.addListener(this);
		swipeArrow.setDisabled(true);
		chargeSwipeArrow.setDisabled(true);

		lessonNum = TAP_TO_MOVE;
		messagingNum = 1;

		messageBackground = new ColorRect(Screen.MIDX, 250, 440, 300, 0, 0, 0, 0.5f, true);
		messageReminderBackground = new ColorRect(Screen.MIDX, 450, Screen.WIDTH, 60, 0, 0, 0, 0.5f, true);

		//final TextStyle messageStyle = new TextStyle("BLANCH_CAPS.otf", 57, 255, 255, 255);
		final TextStyle messageStyle = TextStyle.bodyStyleWhite();
		messageStyle.setSpacing(0);
		//final TextStyle messageReminderStyle = new TextStyle("BLANCH_CAPS.otf", 43, 255, 255, 255);
		final TextStyle messageReminderStyle = TextStyle.bodyStyleWhite(43);
		messageReminderStyle.setSpacing(0);
		//messageReminderStyle.setFontSize(43);

		nextButton = new Button(587, 369, null, null, SpriteManager.tutorialNextButton);
		nextButton.addWidgetListener(this);
		nextButton.setClickAnimation(SpriteManager.tutorialNextButtonClick, NEXT_BUTTON_CLICK_TIME);
		nextButton.scaleRect(2, 2);

		messageArea = new TextArea(Screen.MIDX, 143, CHAR_LIMIT_PER_LINE, LINE_SPACING, messageStyle);
		//messages = new Text[LINE_LIMIT];
		//for (int i = 0; i < LINE_LIMIT; i++)
		//	messages[i] = new Text(messageStyle, "", Screen.MIDX, 143 + (i * LINE_SPACING));
		messageReminder = new Text(messageReminderStyle, "", Screen.MIDX, 450, false);
		displayMessage(AndroidGame.res.getString(R.string.welcome));
	}

	@Override
	public void update(int deltaTime)
	{
		if (state == PAUSED_STATE)
		{
			super.update(deltaTime);
			return;
		}

		if (lessonDelay > 0)
		{
			lessonDelay -= deltaTime;
			if (lessonDelay <= 0)
				nextMessage();
		}
		else if (messagingNum > 0)
			messagingUpdate(deltaTime);

		// Note: if a message is being displayed (messagingNum > 0) then user input will have been retrieved by the
		//messagingUpdate method above and therefore, we do not have to worry about super.update() processing user input
		super.update(deltaTime);
	}

	private void messagingUpdate(int deltaTime)
	{
		// Retrieve touch events
		final List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		nextButton.update(deltaTime);

		final int len = touchEvents.size();
		for (int i = 0; i < len; i++)
		{
			final TouchEvent event = touchEvents.get(i);
			pButton.handleEvent(event);
			nextButton.handleEvent(event);
		}
	}

	private void nextMessage()
	{
		// Clear swipe arrows
		swipeArrow.setVisible(false);
		chargeSwipeArrow.setVisible(false);

		if (messagingNum == 0)    // Lesson completed
		{
			lessonNum++;
			gestureHandler.clear();
		}

		// Clear player's swipe queue and destination
		if (lessonNum <= EXIT_TUTORIAL)
		{
			((TutorialPlayer) player).clearActions();
			player.setDestination(null);
		}

		messagingNum++;

		final Resources res = AndroidGame.res;

		if (lessonNum == TAP_TO_MOVE)
		{
			// message 1 was init'd in the constructor so we go straight to messagingNum == 2
			if (messagingNum == 2)
				displayMessage(res.getString(R.string.move));
			else
				endMessage(res.getString(R.string.moveReminder));
		}
		else if (lessonNum == MOVE_TO_PORTAL)
		{
			if (messagingNum == 1)
			{
				if (player.getX() > Screen.MIDX)
					map.setPortalPoint(70, Screen.MIDY);
				map.gameScreenEmpty();    // Trigger portal spawn
				displayMessage(res.getString(R.string.portal));
			}
			else
				endMessage(res.getString(R.string.portalReminder));
		}
		else if (lessonNum == PATH_AROUND_WALLS)
		{
			if (messagingNum == 1)
			{
				map.clearPortal();
				map = new TutorialMap(this, true);
				map.setBackground();
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				map.gameScreenEmpty();    // Trigger portal spawn
				displayMessage(res.getString(R.string.pathing));
			}
			else
				endMessage(res.getString(R.string.pathingReminder));
		}
		else if (lessonNum == SWIPE)
		{
			if (messagingNum == 1)
			{
				swipeArrow.setDisabled(false);
				swipeArrow.setBothSwipes(true);
				map.clearPortal();
				map = new TutorialMap(this, false);
				map.setBackground();
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				((TutorialPlayer) player).unlockAbility(0);
				displayMessage(res.getString(R.string.swipe));
			}
			else
				endMessage(res.getString(R.string.swipeReminder));
		}
		else if (lessonNum == CHARGE_SWIPE)
		{
			if (messagingNum == 1)
			{
				swipeArrow.setBothSwipes(false);
				chargeSwipeArrow.setDisabled(false);
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				((TutorialPlayer) player).unlockAbility(1);
				displayMessage(res.getString(R.string.chargeSwipe));
			}
			else
				endMessage(res.getString(R.string.chargeSwipeReminder));
		}
		else if (lessonNum == DOUBLE_TAP)
		{
			if (messagingNum == 1)
			{
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				((TutorialPlayer) player).unlockAbility(2);
				displayMessage(res.getString(R.string.doubleTapOne));
			}
			else if (messagingNum == 2)
				displayMessage(res.getString(R.string.doubleTapTwo));
			else
				endMessage(res.getString(R.string.doubleTapReminder));
		}
		else if (lessonNum == MULTI_TAP)
		{
			if (messagingNum == 1)
			{
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				((TutorialPlayer) player).unlockAbility(3);
				displayMessage(res.getString(R.string.multiTap));
			}
			else
				endMessage(res.getString(R.string.multiTapReminder));
		}
		else if (lessonNum == COMBAT)
		{
			if (messagingNum == 1)
			{
				player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
				((TutorialMap) map).spawnTurretCaster();
				((TutorialMap) map).disablePortalSpawn();
				displayMessage(res.getString(R.string.combatOne));
			}
			else if (messagingNum == 2)
				displayMessage(res.getString(R.string.combatTwo));
			else
				endMessage(res.getString(R.string.combatReminder));
		}
		else if (lessonNum == EXIT_TUTORIAL)
		{
			if (messagingNum == 1)
			{
				saveProgress();
				if (player.getX() > Screen.MIDX)
					map.setPortalPoint(70, Screen.MIDY);
				((TutorialMap) map).enablePortalSpawn();
				map.gameScreenEmpty();    // Trigger portal spawn
				((TutorialMap) map).disablePortalSpawn();
				displayMessage(res.getString(R.string.exitTutorial));
			}
			else
			{
				((TutorialMap) map).spawnTurretCaster();
				endMessage(res.getString(R.string.exitTutorialReminder));
			}
		}
		else if (lessonNum > EXIT_TUTORIAL)
		{
			((TutorialMap) map).spawnTurretCaster();
			messagingNum = 0;
		}
	}

	private void displayMessage(String message)
	{
		// Set color rect visibility
		messageBackground.setVisible(true);
		messageReminderBackground.setVisible(false);

		// Display next button
		nextButton.setVisible(true);

		messageReminder.setText("");

		messageArea.setText(message, true);

		/*final int messageIndexLength = message.length() - 1;
		int lastLineEndIndex = -1;
		int i = 0;
		int thisLineLimit;

		while (lastLineEndIndex < messageIndexLength && i < LINE_LIMIT)
		{
			thisLineLimit = lastLineEndIndex + CHAR_LIMIT_PER_LINE;

			if (thisLineLimit >= messageIndexLength)
			{
				// Set text for this (final) line
				messages[i].setText(message.substring(lastLineEndIndex + 1));
				lastLineEndIndex = messageIndexLength;
			}
			else
			{
				// Find appropriate location for a line break
				int spaceIndex = 0;
				int occurrence = lastLineEndIndex + 1;
				while (occurrence >= 0 && occurrence < thisLineLimit)
				{
					occurrence = message.indexOf(' ', occurrence + 1);
					if (occurrence >= 0)
						spaceIndex = occurrence;
				}

				// Set the text for this line
				messages[i].setText(message.substring(lastLineEndIndex + 1, spaceIndex));
				lastLineEndIndex = spaceIndex;
			}

			// Increment index counter
			i++;
		}

		// Set the remaining messages blank
		for (int j = i; j < LINE_LIMIT; j++)
			messages[j].setText("");*/
	}

	private void endMessage(String reminderMessage)
	{
		// Hide next button
		nextButton.setVisible(false);
		messageBackground.setVisible(false);
		messageReminderBackground.setVisible(true);
		messageArea.setVisible(false);
		//for (int i = 0; i < LINE_LIMIT; i++)
		//	messages[i].setText("");
		messageReminder.setText(reminderMessage);
		messageReminder.setVisible(true);
		messagingNum = 0;
	}

	@Override
	public void widgetEvent(WidgetEvent we)
	{
		super.widgetEvent(we);

		if (we.getSource() == nextButton && we.getCommand().equals(Button.ANIMATION_FINISHED))
		{
			nextMessage();
		}
	}

	private void delayLesson(double delaySeconds)
	{
		lessonDelay = (int) (delaySeconds * 1000);
	}

	public void tapLessonCompleted()
	{
		if (lessonNum == 1)
			nextMessage();
	}

	public void swipeLessonCompleted()
	{
		if (lessonNum == SWIPE)
			delayLesson(1);
	}

	public void chargeSwipeLessonCompleted()
	{
		if (lessonNum == CHARGE_SWIPE)
			delayLesson(1);
	}

	public void combatLessonCompleted()
	{
		delayLesson(1);
	}

	@Override
	public void loadMap(Map map)
	{
		GameScreen.map = map;

		//AudioManager.playMusic(AudioManager.GAME_MUSIC);

		map.setBackground();

		player = new TutorialPlayer(this);
		player.offsetTo(map.getPlayerSpawnPoint().x, map.getPlayerSpawnPoint().y);
		player.setVisible(true);
		gestureHandler.addListener(player);
		swipeArrow.setPlayer(player);
		chargeSwipeArrow.setPlayer(player);
		units[gregs].add(player);
	}

	@Override
	void processScreenChangeQueue()
	{
		if (screenChangeQueue != NO_SCREEN_CHANGE)
		{
			// TODO: are nullify()'s needed here?
			if (screenChangeQueue == NEXT_LEVEL_SCREEN)
			{
				if (lessonNum < EXIT_TUTORIAL)
					nextMessage();
				else
				{
					prepScreenChange();
					game.setScreen(new MainMenuScreen(game, false));
				}
			}
			else if (screenChangeQueue == GAME_OVER_SCREEN)
			{
				// Should never be reached. Player is invincible in tutorial mode.
				prepScreenChange();
				game.setScreen(new MainMenuScreen(game, false));
			}
			else
				super.processScreenChangeQueue();

			screenChangeQueue = NO_SCREEN_CHANGE;
		}
	}

	@Override
	void saveProgress()
	{
		final int stageProgress = Preferences.getInt("easyStageProgress", 0);

		if (stageProgress == 0)
		{
			Preferences.setInt("easyStageProgress", 1);
			Preferences.setInt("easyLevelProgress", 1);
			Preferences.setInt("mediumStageProgress", 1);
			Preferences.setInt("mediumLevelProgress", 1);
			Preferences.setInt("hardStageProgress", 1);
			Preferences.setInt("hardLevelProgress", 1);
		}
	}

	@Override
	public void handleDoubleTap(Point tapPoint)
	{
		if (lessonNum == DOUBLE_TAP)
			delayLesson(0.5);
	}

	@Override
	public void handleMultiTap()
	{
		if (lessonNum == MULTI_TAP)
			delayLesson(1);
	}

	@Override
	public void swipeBuilding(Line swipe)
	{
	}

	@Override
	public void chargeSwipeBuilding(Line swipe)
	{
	}

	@Override
	public void handleTap(Point tapPoint)
	{
	}

	@Override
	public void swipeCancelled()
	{
	}

	@Override
	public void handleSwipe(Line swipe)
	{
	}

	@Override
	public void handleChargeSwipe(Line swipe)
	{
	}

	@Override
	void updateFPSText()
	{
	}

	@Override
	void firstRun()
	{
		super.firstRun();

		fpsTextBackground.setVisible(false);
	}

	@Override
	void pauseGame()
	{
		super.pauseGame();

		//for (int i = 0; i < LINE_LIMIT; i++)
		//	messages[i].setVisible(false);
		messageArea.setVisible(false);
		messageReminder.setVisible(false);
		messageBackground.setVisible(false);
		messageReminderBackground.setVisible(false);
		nextButton.setVisible(false);
	}

	@Override
	void unpauseGame()
	{
		super.unpauseGame();

		fpsTextBackground.setVisible(false);

		//for (int i = 0; i < LINE_LIMIT; i++)
		//	messages[i].setVisible(true);
		messageReminder.setVisible(true);
		if (messagingNum > 0)
		{
			messageArea.setVisible(true);
			messageBackground.setVisible(true);
			nextButton.setVisible(true);
		}
		else
			messageReminderBackground.setVisible(true);
	}

	@Override
	void nullify()
	{
		super.nullify();

		messageBackground = null;
		messageReminderBackground = null;
		//messages = null;
		messageArea =  null;
		messageReminder = null;
		nextButton = null;
	}
}