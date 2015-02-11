package com.lescomber.vestige.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.units.AIUnit;

public class Wave
{
	private static final int DEFAULT_HEALTH_PICK_UP = 15;
	
	LinkedList<AIUnit> units;
	LinkedList<Integer> unitCountdowns;
	private int waveCountdown;	// in ms
	
	private final ArrayList<AIUnit> enemiesBuffer;
	private final ArrayList<AIUnit> enemiesReady;
	
	public Wave(double waveCountdownSeconds)
	{
		waveCountdown = (int)(waveCountdownSeconds * 1000);
		units = new LinkedList<AIUnit>();
		unitCountdowns = new LinkedList<Integer>();
		enemiesBuffer = new ArrayList<AIUnit>(5);
		enemiesReady = new ArrayList<AIUnit>(5);
	}
	
	public Wave(Wave copyMe, double waveCountdownSeconds)
	{
		waveCountdown = (int)(waveCountdownSeconds * 1000);
		
		units = new LinkedList<AIUnit>();
		for (final AIUnit aiu : copyMe.units)
			units.add(aiu.copy());
		unitCountdowns = new LinkedList<Integer>();
		for (final Integer i : copyMe.unitCountdowns)
			unitCountdowns.add(i);

		// Not copied
		enemiesBuffer = new ArrayList<AIUnit>(5);
		enemiesReady = new ArrayList<AIUnit>(5);
	}
	
	public Wave(Wave copyMe)
	{
		this(copyMe, copyMe.getWaveCountdown());
	}
	
	public void update(int deltaTime)
	{
		if (!units.isEmpty())
		{
			unitCountdowns.set(0, unitCountdowns.get(0) - deltaTime);
			
			while (unitCountdowns.get(0) <= 0)
			{
				final int leftover = unitCountdowns.get(0);
				queueEnemiesUnit(units.remove());
				unitCountdowns.remove();
				
				if (units.isEmpty())
					break;
				else
					unitCountdowns.set(0, unitCountdowns.get(0) + leftover);
			}
		}
	}
	
	public boolean updateWaveCountdown(int deltaTime)
	{
		waveCountdown -= deltaTime;
		
		return waveCountdown <= 0;
	}
	
	public void addUnit(AIUnit unit, double cooldownSeconds)
	{
		unit.setVisible(false);
		units.add(unit);
		unitCountdowns.add((int)(cooldownSeconds * 1000));
	}
	
	public void addHealthPickUp(float healAmount)
	{
		if (units.isEmpty())
			return;
		
		final int index = Util.rand.nextInt(units.size());
		units.get(index).setPickUp(new HealPickUp(healAmount));
	}
	
	public void addHealthPickUp()
	{
		addHealthPickUp(DEFAULT_HEALTH_PICK_UP);
	}
	
	public void gameScreenEmpty() { }
	
	public int getWaveCountdown() { return waveCountdown; }
	public boolean isFinished() { return (units.isEmpty() && enemiesBuffer.isEmpty()); }
	
	public void addToWaveCountdown(int additionalCountdown) { waveCountdown += additionalCountdown; }
	public void setWaveCountdown(int waveCountdown) { this.waveCountdown = waveCountdown; }
	
	public void queueEnemiesUnit(AIUnit unit)
	{
		enemiesBuffer.add(unit);
	}
	
	public List<AIUnit> getEnemiesQueue()
	{
		enemiesReady.clear();
		enemiesReady.addAll(enemiesBuffer);
		enemiesBuffer.clear();
		return enemiesReady;
	}
}