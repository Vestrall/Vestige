package com.lescomber.vestige;

public class FpsCounter {
	private static final int MAX_SAMPLES = 50;

	private int tickIndex;
	private int sum;        // In ms
	private final int tickList[];

	public FpsCounter() {
		tickIndex = 0;
		sum = 0;
		tickList = new int[MAX_SAMPLES];
	}

	public void addFrame(int newFrameTime) {
		sum -= tickList[tickIndex];
		sum += newFrameTime;
		tickList[tickIndex] = newFrameTime;
		if (++tickIndex == MAX_SAMPLES)
			tickIndex = 0;
	}

	public double getFps() {
		return (MAX_SAMPLES / (sum / 1000.0));
	}
}