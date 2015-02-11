package com.lescomber.vestige;

public class FPSCounter
{
	private static final int MAX_SAMPLES = 50;
	
	private int tickIndex;
	private int sum;		// In ms
	private final int tickList[];
	
	public FPSCounter()
	{
		tickIndex = 0;
		sum = 0;
		tickList = new int[MAX_SAMPLES];
	}
	
	// Returns average time per frame (in ms)
	public double addFrame(int newFrameTime)
	{
		sum -= tickList[tickIndex];
		sum += newFrameTime;
		tickList[tickIndex] = newFrameTime;
		if (++tickIndex == MAX_SAMPLES)
			tickIndex = 0;
		
		return ((double)sum / MAX_SAMPLES);
	}
	
	public double getFPS()
	{
		return (MAX_SAMPLES / (sum / 1000.0));
	}
}