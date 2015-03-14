package com.lescomber.vestige.map;

import com.lescomber.vestige.screens.TutorialScreen;
import com.lescomber.vestige.units.TutorialCaster;

public class TutorialMap extends Map
{
	private final TutorialScreen tScreen;
	private boolean enablePortalSpawn;
	
	public TutorialMap(TutorialScreen tScreen, boolean withWalls)
	{
		super(Levels.TUTORIAL_STAGE, 0);
		
		this.tScreen = tScreen;
		
		setPlayerSpawnPoint(100, 240);
		addEnemySpawnPoint(0, 0);		// Not used
		setPortalPoint(730, 240);
		
		final Wave noWave = new Wave(0);
		addWave(noWave);
		
		enablePortalSpawn = true;
		
		if (withWalls)
		{
			addObstacle(new Wall(400, 0, 440, 180));
			addObstacle(new Wall(250, 110, 290, 370));
			addObstacle(new Wall(400, 300, 440, 480));
			buildNodeNetwork();
		}
	}
	
	public TutorialMap(TutorialScreen tScreen)
	{
		this(tScreen, false);
	}
	
	public TutorialMap()
	{
		this(null, false);
	}
	
	public void spawnTurretCaster()
	{
		final TutorialCaster tc = new TutorialCaster();
		tc.offsetTo(600, 240);
		queueStevesUnit(tc);
	}
	
	@Override
	public void gameScreenEmpty()
	{
		if (enablePortalSpawn)
			super.gameScreenEmpty();
		
		// Hack. enablePortalSpawn should only be false for the combat lesson so this case should only occur on
		//TutorialCaster death
		else
			tScreen.combatLessonCompleted();
	}
	
	public void disablePortalSpawn() { enablePortalSpawn = false; }
	public void enablePortalSpawn() { enablePortalSpawn = true; }
}