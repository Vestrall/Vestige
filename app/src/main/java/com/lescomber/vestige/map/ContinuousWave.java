package com.lescomber.vestige.map;

import java.util.List;

import com.lescomber.vestige.units.AIUnit;

public class ContinuousWave extends Wave
{
	private boolean spawning;				// true when still spawning continuous units
	
	private final Wave continuousWaveProto;		// continuousWave prototype. Is copied to create each new continuousWave
	private Wave continuousWave;			// Current (or upcoming) continuous wave
	private int waveIntervalMax;			// Total interval between each wave. Can be changed as the wave goes along
	private int waveInterval;				// Current interval between continuousWaves. Will be <= 0 during continuousWave
	
	private double waveCDReduction;			// Percentage amount to reduce continuousWave spawning cooldowns each iteration
	private final int waveIntervalMaxOriginal;	// Original waveIntervalMax cooldown
	private double waveCDPercent;			// Current percentage multiplier for cooldowns of continuousWave spawning
	private double waveCDMin;				// Remaining iterations to apply waveCDRecution (enforces lower limit)
	
	public ContinuousWave(double waveCountdownSeconds, Wave continuousWave, double continuousWaveIntervalSeconds)
	{
		super(waveCountdownSeconds);
		
		spawning = true;
		
		continuousWaveProto = new Wave(continuousWave);
		for (final AIUnit aiu : continuousWaveProto.units)
			aiu.setMainUnit(false);
		this.continuousWave = new Wave(continuousWaveProto);
		waveIntervalMax = (int)(continuousWaveIntervalSeconds * 1000);
		waveIntervalMaxOriginal = waveIntervalMax;
		waveInterval = continuousWave.getWaveCountdown();
		
		waveCDReduction = 0;
		waveCDPercent = 1;
		waveCDMin = 1;
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		if (waveInterval > 0)
			waveInterval -= deltaTime;
		else if (spawning)
		{
			continuousWave.update(deltaTime);
			final List<AIUnit> continuousUnits = continuousWave.getEnemiesQueue();
			for (final AIUnit aiu : continuousUnits)
				queueEnemiesUnit(aiu);
			
			if (continuousWave.isFinished())
			{
				if (waveCDPercent > waveCDMin)
				{
					waveCDPercent -= waveCDReduction;
					
					// If this is the last reduction, permanently change continuousWaveProto to have final values
					if (waveCDPercent <= waveCDMin)
					{
						waveCDPercent = waveCDMin;
						for (int i=0; i<continuousWaveProto.unitCountdowns.size(); i++)
						{
							final int newCooldown = (int)Math.round(continuousWaveProto.unitCountdowns.get(i) * waveCDPercent);
							continuousWaveProto.unitCountdowns.set(i, newCooldown);
						}
						continuousWave = new Wave(continuousWaveProto);
					}
					else
					{
						continuousWave = new Wave(continuousWaveProto);
						for (int i=0; i<continuousWave.unitCountdowns.size(); i++)
						{
							final int newCooldown = (int)Math.round(continuousWaveProto.unitCountdowns.get(i) * waveCDPercent);
							continuousWave.unitCountdowns.set(i, newCooldown);
						}
					}
					
					waveInterval = waveIntervalMax;
					waveIntervalMax = (int)Math.round(waveIntervalMaxOriginal * waveCDPercent);
				}
				else
				{
					waveInterval = waveIntervalMax;
					continuousWave = new Wave(continuousWaveProto);
				}
			}
		}
	}
	
	@Override
	public void gameScreenEmpty()
	{
		if (isFinished())
			spawning = false;
	}
	
	public void setWaveCooldownReduction(double reductionPercentPerWave, double minCooldownPercent)
	{
		waveCDReduction = reductionPercentPerWave;
		waveCDMin = minCooldownPercent;
	}
}