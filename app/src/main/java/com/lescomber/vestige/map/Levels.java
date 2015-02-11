package com.lescomber.vestige.map;

import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.units.Caster;
import com.lescomber.vestige.units.FloatingCreep;
import com.lescomber.vestige.units.TestBoss;
import com.lescomber.vestige.units.sone.BabyCreep;
import com.lescomber.vestige.units.sone.DanceCoordinator;
import com.lescomber.vestige.units.sone.DancingCaster;
import com.lescomber.vestige.units.sone.DancingCreep;
import com.lescomber.vestige.units.sone.OneEightBoss;
import com.lescomber.vestige.units.sone.OneFourBoss;
import com.lescomber.vestige.units.sone.OneSixBoss;
import com.lescomber.vestige.units.sone.OneTenBoss;

public class Levels
{
	public static final int STAGE_COUNT = 2;
	//public static final int[] LEVEL_COUNT = new int[] { 11, 0 };
	public static final int[] LEVEL_COUNT = new int[] { 10, 0 };
	
	public static Map loadLevel(int stageNum, int levelNum)
	{
		if (stageNum <= 0)
			return tutorialStage();
		else if (stageNum == 1)
			return stageOne(levelNum);
		else if (stageNum == 2)
			return stageTwo(levelNum);
		else
			return null;
	}
	
	private static Map tutorialStage()
	{
		// Init basic level requirements
		final TutorialMap level = new TutorialMap();
		
		return level;
	}
	
	private static Map stageOne(int levelNum)
	{
		if (levelNum == 1)
		{
			// Init basic level requirements
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(50, 240);
			level.addEnemySpawnPoint(875, 120);
			level.addEnemySpawnPoint(875, 240);
			level.addEnemySpawnPoint(875, 360);
			level.setPortalPoint(700, 240);
			
			// Init obstacles
			level.addObstacle(new Wall(600, 150, 630, 330));
			level.buildNodeNetwork();
			
			// Create enemy unit waves
			// 1
			Wave wave = new Wave(0);
			wave.addUnit(new FloatingCreep(), 2);
			level.addWave(wave);
			
			// 2
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			level.addWave(wave);
			
			// 3
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 3);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 2)
		{
			// Init basic level requirements
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(100, 140);
			level.addEnemySpawnPoint(650, -50);
			level.addEnemySpawnPoint(875, 240);
			level.addEnemySpawnPoint(650, 530);
			level.setPortalPoint(700, 240);
			
			// Init obstacles
			level.addObstacle(new Wall(600, 0, 620, 100));
			level.addObstacle(new Wall(600, 380, 620, 480));
			level.addObstacle(new Tree(550, 240));
			level.buildNodeNetwork();
			
			// Init RailCasters
			final Caster c = new Caster();
			c.createRailLocations(new Hitbox(new Rectangle(600, 0, 800, 480)), 15);
			
			// Create enemy unit waves
			// 1
			Wave wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			level.addWave(wave);
			
			// 2
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 3);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			// 3
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			// 4
			wave = new Wave(30);
			wave.addUnit(c.copy(), 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 3)
		{
			// Init basic level requirements
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(100, 240);
			level.addEnemySpawnPoint(875, 120);
			level.addEnemySpawnPoint(875, 240);
			level.addEnemySpawnPoint(875, 360);
			level.setPortalPoint(765, 240);
			
			// Init obstacles
			level.addObstacle(new Tree(615, 80));
			level.addObstacle(new Tree(300, 135));
			level.addObstacle(new Tree(700, 400));
			level.buildNodeNetwork();
			
			// Init RailCasters
			final Caster rc = new Caster();
			rc.createRailLocations(new Hitbox(new Rectangle(600, 0, 800, 480)), 15);
			
			// Create enemy unit waves
			// 1
			Wave wave = new Wave(0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(rc.copy(), 0);
			level.addWave(wave);
			
			// 2
			wave = new Wave(20);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 2);
			level.addWave(wave);
			
			// 3
			wave = new Wave(20);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			// 4
			wave = new Wave(20);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(rc.copy(), 5);
			level.addWave(wave);
			
			// 5
			wave = new Wave(30);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 1.5);
			level.addWave(wave);
			
			// 6
			wave = new Wave(40);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0.5);
			wave.addUnit(new FloatingCreep(), 3.5);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 4)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(100, 240);
			level.addEnemySpawnPoint(900, 120);
			level.addEnemySpawnPoint(900, 240);
			level.addEnemySpawnPoint(900, 360);
			level.setPortalPoint(765, 430);
			level.setBossLevel(true);
			
			// Create boss wave
			final Wave wave = new Wave(0);
			wave.addUnit(new OneFourBoss(), 2);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 5)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(75, 240);
			level.addEnemySpawnPoint(900, 120);
			level.addEnemySpawnPoint(900, 240);
			level.addEnemySpawnPoint(900, 360);
			level.setPortalPoint(765, 430);
			
			// Init obstacles
			level.addObstacle(new Wall(250, 190, 300, 290));
			level.addObstacle(new Wall(300, 220, 500, 260));
			level.addObstacle(new Wall(500, 190, 550, 290));
			level.buildNodeNetwork();
			
			// Init RailCasters
			final Caster rc = new Caster();
			rc.createRailLocations(level);
			
			// Create enemy unit waves
			// 1
			Wave wave = new Wave(0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0.5);
			wave.addUnit(new FloatingCreep(), 0.5);
			wave.addUnit(new FloatingCreep(), 0.5);
			level.addWave(wave);
			
			// 2
			wave = new Wave(8);
			wave.addUnit(rc.copy(), 0);
			level.addWave(wave);
			
			// 3
			wave = new Wave(9);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			level.addWave(wave);
			
			// 4
			wave = new Wave(11);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 1.5);
			wave.addUnit(new FloatingCreep(), 0.2);
			level.addWave(wave);
			
			// 5
			wave = new Wave(12);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0.5);
			level.addWave(wave);
			
			// 6
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			wave.addUnit(new FloatingCreep(), 0.2);
			
			final ContinuousWave cWave = new ContinuousWave(16, wave, 30);
			cWave.addUnit(rc.copy(), 0);
			cWave.addUnit(rc.copy(), 0.5);
			cWave.addUnit(new FloatingCreep(), 1);
			cWave.addUnit(new FloatingCreep(), 0.2);
			level.addWave(cWave);
			
			return level;
		}
		else if (levelNum == 6)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(100, 240);
			level.addEnemySpawnPoint(875, 240);
			level.setPortalPoint(765, 240);
			
			final Wave wave = new Wave(0);
			wave.addUnit(new OneSixBoss(), 2);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 7)
		{
			// Init basic level requirements
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(225, 240);
			level.addEnemySpawnPoint(875, 120);
			level.addEnemySpawnPoint(875, 360);
			level.addEnemySpawnPoint(500, -75);
			level.addEnemySpawnPoint(500, 555);
			level.setPortalPoint(765, 240);
			
			// Init obstacles
			level.addObstacle(new Wall(100, 160, 150, 320));
			level.addObstacle(new Wall(650, 160, 700, 320));
			level.buildNodeNetwork();
			
			// Init RailCasters
			final Caster rc = new Caster();
			rc.createRailLocations(level);
			
			// Create enemy unit waves
			// 1
			Wave wave = new Wave(0);
			wave.addUnit(new BabyCreep(), 0);
			level.addWave(wave);
			
			// 2
			wave = new Wave(60);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(rc.copy(), 1);
			level.addWave(wave);
			
			// 3
			wave = new Wave(200);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 4
			wave = new Wave(30);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 5);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			// 5
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(rc.copy(), 5);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 6
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 7
			final Wave cWave = new Wave(15);
			cWave.addUnit(new FloatingCreep(), 0);
			
			wave = new ContinuousWave(40, cWave, 10);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(rc.copy(), 0);
			wave.addUnit(new FloatingCreep(), 5);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 8)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(50, 240);
			level.addEnemySpawnPoint(900, 120);
			level.addEnemySpawnPoint(900, 240);
			level.addEnemySpawnPoint(900, 360);
			level.setPortalPoint(765, 430);
			level.setBossLevel(true);
			
			// Create boss wave
			final Wave wave = new Wave(0);
			wave.addUnit(new OneEightBoss(), 3);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 9)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(50, 240);
			level.addEnemySpawnPoint(900, 60);
			level.addEnemySpawnPoint(900, 180);
			level.addEnemySpawnPoint(900, 300);
			level.addEnemySpawnPoint(900, 420);
			level.setPortalPoint(765, 430);
			
			// Init obstacles
			level.addObstacle(new Tree(500, 180));
			level.addObstacle(new Tree(425, 380));
			level.buildNodeNetwork();
			
			// Init RailCasters
			final Caster c = new Caster();
			c.createRailLocations(level);
			
			// Init dancers
			final DanceCoordinator coordinator = new DanceCoordinator(650, 240);
			final DancingCaster dancingCaster = new DancingCaster(coordinator);
			final DancingCreep creepOne = new DancingCreep(coordinator);
			final DancingCreep creepTwo = new DancingCreep(coordinator);
			final DancingCreep creepThree = new DancingCreep(coordinator);
			coordinator.setCaster(dancingCaster);
			coordinator.addCreep(creepOne);
			coordinator.addCreep(creepTwo);
			coordinator.addCreep(creepThree);
			
			// 1
			Wave wave = new Wave(0);
			wave.addUnit(dancingCaster, 0);
			wave.addUnit(creepOne, 0);
			wave.addUnit(creepTwo, 0);
			wave.addUnit(creepThree, 0);
			level.addWave(wave);
			
			// 2
			wave = new Wave(600);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 3
			wave = new Wave(30);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			level.addWave(wave);
			
			// 4
			wave = new Wave(30);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0.3);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 5
			wave = new Wave(30);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 1);
			level.addWave(wave);
			
			// 6
			wave = new Wave(30);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 0.5);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0.5);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 7
			wave = new Wave(30);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(c.copy(), 2);
			wave.addUnit(new FloatingCreep(), 0.7);
			wave.addUnit(new FloatingCreep(), 2);
			wave.addUnit(new FloatingCreep(), 0.7);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 8
			wave = new Wave(40);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 1);
			wave.addUnit(c.copy(), 1);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 9
			wave = new Wave(45);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 3);
			wave.addUnit(new FloatingCreep(), 3);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 10
			wave = new Wave(45);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(c.copy(), 3);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addHealthPickUp();
			level.addWave(wave);
			
			// 11
			wave = new Wave(50);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(c.copy(), 0);
			wave.addUnit(new FloatingCreep(), 1);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(new FloatingCreep(), 0);
			wave.addUnit(c.copy(), 6);
			wave.addUnit(c.copy(), 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 10)
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(50, 240);
			level.addEnemySpawnPoint(900, 120);
			level.addEnemySpawnPoint(900, 240);
			level.addEnemySpawnPoint(900, 360);
			level.setPortalPoint(765, 430);
			level.setBossLevel(true);
			
			// Create boss wave
			final Wave wave = new Wave(0);
			wave.addUnit(new OneTenBoss(), 2);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 11)	// Test level
		{
			final Map level = new Map(1, levelNum);
			level.setPlayerSpawnPoint(50, 240);
			level.addEnemySpawnPoint(600, 100);
			level.addEnemySpawnPoint(500, 200);
			level.addEnemySpawnPoint(700, 300);
			
			level.setPortalPoint(765, 430);
			
			final Wave wave = new Wave(0);
			
			wave.addUnit(new TestBoss(), 0);
			wave.addUnit(new TestBoss(), 0);
			wave.addUnit(new TestBoss(), 0);
			
			level.addWave(wave);
			
			return level;
		}
		else
			return null;
	}
	
	private static Map stageTwo(int levelNum)
	{
		/*if (levelNum == 1)
		{
			Map level = new Map(2, levelNum);
			level.setPlayerSpawnPoint(50, 50);
			level.addEnemySpawnPoint(900, 240);
			level.setPortalPoint(765, 430);
			
			BossTest boss = new BossTest();
			Hitbox railBox = new Hitbox(new Rectangle(550, 50, 750, 430));
			boss.createRailLocations(railBox, 15);
			Wave wave = new Wave(0);
			wave.addUnit(boss, 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 2)
		{
			Map level = new Map(2, levelNum);
			level.setPlayerSpawnPoint(50, 50);
			level.addEnemySpawnPoint(900, 240);
			level.setPortalPoint(765, 430);
			
			// Init obstacles
			level.addObstacle(new Wall(300, 180, 360, 300));
			level.buildNodeNetwork();
			
			WaveBoss boss = new WaveBoss();
			Hitbox railBox = new Hitbox(new Rectangle(550, 50, 750, 430));
			boss.createRailLocations(railBox, 15);
			Wave wave = new Wave(0);
			wave.addUnit(boss, 0);
			level.addWave(wave);
			
			return level;
		}
		else if (levelNum == 3)
		{
			Map level = new Map(2, levelNum);
			level.setPlayerSpawnPoint(100, 240);
			level.addEnemySpawnPoint(900, 240);
			level.setPortalPoint(765, 430);
			
			Wave wave = new Wave(0);
			wave.addUnit(new FloatingCreep(), 0);
			level.addWave(wave);
			
			return level;
		}
		else*/
			return null;
	}
}